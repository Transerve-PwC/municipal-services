package org.egov.waterconnection.service;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.SearchCriteria;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.model.workflow.BusinessService;
import org.egov.waterconnection.repository.WaterDao;
import org.egov.waterconnection.repository.WaterDaoImpl;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.egov.waterconnection.validator.ActionValidator;
import org.egov.waterconnection.validator.MDMSValidator;
import org.egov.waterconnection.validator.ValidateProperty;
import org.egov.waterconnection.validator.WaterConnectionValidator;
import org.egov.waterconnection.workflow.WorkflowIntegrator;
import org.egov.waterconnection.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WaterServiceImpl implements WaterService {

	@Autowired
	private WaterDao waterDao;
	
	@Autowired
	private WaterConnectionValidator waterConnectionValidator;

	@Autowired
	private ValidateProperty validateProperty;
	
	@Autowired
	private MDMSValidator mDMSValidator;

	@Autowired
	private EnrichmentService enrichmentService;
	
	@Autowired
	private WorkflowIntegrator wfIntegrator;
	
	@Autowired
	private WSConfiguration config;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private ActionValidator actionValidator;
	
	@Autowired
	private WaterServicesUtil waterServiceUtil;
	
	@Autowired
	private CalculationService calculationService;
	
	@Autowired
	private WaterDaoImpl waterDaoImpl;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private WaterServicesUtil wsUtil;
	
	
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be created
	 * @return List of WaterConnection after create
	 */
	@Override
	public List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, false);
		Property property = validateProperty.getOrValidateProperty(waterConnectionRequest);
		enrichmentService.enrichWaterConnection(waterConnectionRequest);
		userService.createUser(waterConnectionRequest);
		// call work-flow
		if (config.getIsExternalWorkFlowEnabled())
			wfIntegrator.callWorkFlow(waterConnectionRequest, property);
		waterConnectionRequest.getWaterConnection().getWaterApplication().setApplicationStatus(
				waterConnectionRequest.getWaterConnection().getApplicationStatus());
		waterDao.saveWaterConnection(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}
	/**
	 * 
	 * @param criteria WaterConnectionSearchCriteria contains search criteria on water connection
	 * @param requestInfo 
	 * @return List of matching water connection
	 */
	public List<WaterConnection> search(SearchCriteria criteria, RequestInfo requestInfo) {
		List<WaterConnection> waterConnectionList;
		waterConnectionList = getWaterConnectionsList(criteria, requestInfo);
		waterConnectionValidator.validatePropertyForConnection(waterConnectionList);
		enrichmentService.enrichConnectionHolderDeatils(waterConnectionList, criteria, requestInfo);
		return waterConnectionList;
	}
	/**
	 * 
	 * @param criteria WaterConnectionSearchCriteria contains search criteria on water connection
	 * @param requestInfo 
	 * @return List of matching water connection
	 */
	public List<WaterConnection> getWaterConnectionsList(SearchCriteria criteria,
			RequestInfo requestInfo) {
		return waterDao.getWaterConnectionList(criteria, requestInfo);
	}
	/**
	 * 
	 * @param waterConnectionRequest WaterConnectionRequest contains water connection to be updated
	 * @return List of WaterConnection after update
	 */
	@Override
	public List<WaterConnection> updateWaterConnection(WaterConnectionRequest waterConnectionRequest) {
		log.info("Update WaterConnection: {}", waterConnectionRequest.getWaterConnection());
		waterConnectionValidator.validateWaterConnection(waterConnectionRequest, true);
		mDMSValidator.validateMasterData(waterConnectionRequest);
		Property property = validateProperty.getOrValidateProperty(waterConnectionRequest);
		validateProperty.validatePropertyCriteria(property);
		boolean isStateUpdatable = true;
		BusinessService businessService = null;
			
		if(WCConstants.WS_ACTION_REACTIVATION.equalsIgnoreCase(
				waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			waterConnectionRequest.getWaterConnection().setActivityType(WCConstants.WS_REACTIVATE);

			HashMap<String, Object> additionalDetails = mapper
					.convertValue(waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
			additionalDetails.put(WCConstants.ADHOC_PENALTY, null);
			additionalDetails.put(WCConstants.ADHOC_PENALTY_REASON, null);
			additionalDetails.put(WCConstants.ADHOC_PENALTY_COMMENT, null);
			additionalDetails.put(WCConstants.ADHOC_REBATE, null);
			additionalDetails.put(WCConstants.ADHOC_REBATE_REASON, null);
			additionalDetails.put(WCConstants.ADHOC_REBATE_COMMENT, null);

			waterConnectionRequest.getWaterConnection().setAdditionalDetails(additionalDetails);

		}
		if (WCConstants.STATUS_PENDING_FOR_REGULAR.equalsIgnoreCase(
				waterConnectionRequest.getWaterConnection().getApplicationStatus())
				&& WCConstants.ACTION_APPLY_FOR_REGULAR_CONNECTION.equalsIgnoreCase(
						waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())){
			waterConnectionRequest.getWaterConnection().setActivityType(WCConstants.WS_APPLY_FOR_REGULAR_CON);
			waterConnectionRequest.getWaterConnection().setWaterApplicationType(WCConstants.APPLICATION_TYPE_REGULAR);
		}
		
		if (WCConstants.ACTION_INITIATE.equalsIgnoreCase(
				waterConnectionRequest.getWaterConnection().getProcessInstance().getAction())) {
			waterConnectionRequest.getWaterConnection().setDocuments(null);
			enrichmentService.enrichWaterApplication(waterConnectionRequest);
			enrichmentService.enrichUpdateWaterConnection(waterConnectionRequest);
		}else {
		
			businessService = workflowService.getBusinessService(waterConnectionRequest.getWaterConnection().getTenantId(), 
					waterConnectionRequest.getRequestInfo(), waterConnectionRequest.getWaterConnection().getActivityType());
			log.info("businessService: {},Business: {}",businessService.getBusinessService(),businessService.getBusiness());
			WaterConnection searchResult = getConnectionForUpdateRequest(waterConnectionRequest.getWaterConnection().getWaterApplication().getId(), waterConnectionRequest.getRequestInfo());
			String previousApplicationStatus = workflowService.getApplicationStatus(waterConnectionRequest.getRequestInfo(),
					waterConnectionRequest.getWaterConnection().getApplicationNo(),
					waterConnectionRequest.getWaterConnection().getTenantId(),wfIntegrator.getBusinessService(waterConnectionRequest.getWaterConnection().getActivityType()));
			enrichmentService.enrichUpdateWaterConnection(waterConnectionRequest);
			actionValidator.validateUpdateRequest(waterConnectionRequest, businessService, previousApplicationStatus);
			waterConnectionValidator.validateUpdate(waterConnectionRequest, searchResult);
			calculationService.calculateFeeAndGenerateDemand(waterConnectionRequest, property);		
			//check for edit and send edit notification
			waterDaoImpl.pushForEditNotification(waterConnectionRequest);
			//Enrich file store Id After payment
			enrichmentService.enrichFileStoreIds(waterConnectionRequest);
			userService.updateUser(waterConnectionRequest, searchResult);
			isStateUpdatable = waterServiceUtil.getStatusForUpdate(businessService, previousApplicationStatus);
		}
		//Call workflow
		wfIntegrator.callWorkFlow(waterConnectionRequest, property);
		enrichmentService.postStatusEnrichment(waterConnectionRequest);
		
		waterConnectionRequest.getWaterConnection().getWaterApplication().setApplicationStatus(
				waterConnectionRequest.getWaterConnection().getApplicationStatus());
		waterConnectionRequest.getWaterConnection().getWaterApplication().setAction(
				waterConnectionRequest.getWaterConnection().getProcessInstance().getAction());
		
		log.info("Next applicationStatus: {}",waterConnectionRequest.getWaterConnection().getApplicationStatus());
		
		boolean isTerminateState = workflowService.isTerminateState(waterConnectionRequest.getWaterConnection().getApplicationStatus(), businessService);
		if(isTerminateState) {
			waterConnectionRequest.getWaterConnection().setInWorkflow(false);
		}
		waterDao.updateWaterConnection(waterConnectionRequest, isStateUpdatable);
		
		enrichmentService.postForMeterReading(waterConnectionRequest);
		return Arrays.asList(waterConnectionRequest.getWaterConnection());
	}
	
	/**
	 * Search Water connection to be update
	 * 
	 * @param id
	 * @param requestInfo
	 * @return water connection
	 */
	public WaterConnection getConnectionForUpdateRequest(String id, RequestInfo requestInfo) {
		log.info("Water Application Id:{}",id);
		Set<String> ids = new HashSet<>(Arrays.asList(id));
		SearchCriteria criteria = new SearchCriteria();
		criteria.setIds(ids);
		List<WaterConnection> connections = getWaterConnectionsList(criteria, requestInfo);
		if (CollectionUtils.isEmpty(connections)) {
			StringBuilder builder = new StringBuilder();
			builder.append("WATER CONNECTION NOT FOUND FOR: ").append(id).append(" :ID");
			throw new CustomException("INVALID_WATERCONNECTION_SEARCH", builder.toString());
		}
			
		return connections.get(0);
	}
}
