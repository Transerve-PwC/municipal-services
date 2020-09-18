package org.egov.ec.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.repository.FineMasterRepository;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.Auction;
import org.egov.ec.web.models.FineMaster;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
import org.egov.ec.web.models.workflow.ProcessInstance;
import org.egov.ec.web.models.workflow.ProcessInstanceRequest;
import org.egov.ec.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FineService {

	private final ObjectMapper objectMapper;
	private WorkflowIntegrator wfIntegrator;
	private FineMasterRepository repository;
	private CustomBeanValidator validate;
	private DeviceSourceService deviceSource;

	@Autowired
	public FineService(WorkflowIntegrator wfIntegrator, ObjectMapper objectMapper, FineMasterRepository repository, CustomBeanValidator validate,
			DeviceSourceService deviceSource) {
		this.objectMapper = objectMapper;
		this.wfIntegrator = wfIntegrator;
		this.repository = repository;
		this.validate=validate;
		this.deviceSource = deviceSource;
	}

	/**
	*This method will add entry into fine master which will be eligible for getting approved from apellate authority
	*
	* @param RequestInfoWrapper containing fine record
	* @param requestHeader for saving device source information
	* @return HTTP status on success
	* @throws CustomException FINEMASTER_ADD_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> saveFine(RequestInfoWrapper requestInfoWrapper, String requestHeader) {
		log.info("Fine Service - Save Fine");
		try {
			FineMaster fine = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), FineMaster.class);	
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(fine, FineMaster.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.FINEMASTERCREATE);
		
			if(responseValidate.equals("")) 
			{
		
				fine.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
				fine.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
				fine.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
				fine.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
				validate.validateFields(fine);
				fine.setFineUuid(UUID.randomUUID().toString());
				
				String sourceUuid = deviceSource.saveDeviceDetails(requestHeader, "addFineEvent",
						fine.getTenantId(), requestInfoWrapper.getAuditDetails());
				fine.setSourceUuid(sourceUuid);
	
				ProcessInstance processInstance = new ProcessInstance();
				processInstance.setBusinessId(fine.getFineUuid());
				processInstance.setTenantId(fine.getTenantId());
				processInstance.setBusinessService(EcConstants.WORKFLOW_FINE);
				processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
				processInstance.setAction(fine.getApprovalStatus());
				List<ProcessInstance> processList = Arrays.asList(processInstance);
	
				ResponseInfo response=wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList)
						.requestInfo(requestInfoWrapper.getRequestInfo()).build());
				if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
				repository.saveFine(fine);
				}
	
				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(fine).build(),
						HttpStatus.OK);
			}
			else
			{
				throw new CustomException("FINEMASTER_ADD_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Fine Service - Save Fine Exception"+e.getMessage());
			throw new CustomException("FINEMASTER_ADD_EXCEPTION", e.getMessage());
		}

	}

	/**
	*This method will fetch fine master entries
	*
	* @param RequestInfoWrapper SearchCriteria
	* @return ResponseInfoWrapper containing list of fines
	* @throws CustomException FINEMASTER_GET_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> getFine(RequestInfoWrapper requestInfoWrapper) {
		log.info("Fine Service - Get Fine");
		try {
			
			
				List<FineMaster> fineMasterList = repository.getFine(requestInfoWrapper);
				
	
				return new ResponseEntity<>(
						ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
								.responseBody(fineMasterList).build(),
						HttpStatus.OK);
			
		} catch (Exception e) {
			log.error("Fine Service - Get Fine Exception"+e.getMessage());
			throw new CustomException("FINEMASTER_GET_EXCEPTION", e.getMessage());
		}

	}

	/**
	*This method will update fine master record and status post approval/rejection from HOD
	*
	* @param RequestInfoWrapper containing fine master object
	* @return HTTP status on success
	* @throws CustomException FINEMASTER_UPDATE_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> updateFine(RequestInfoWrapper requestInfoWrapper) {
		log.info("Fine Service - Update Fine");
		try {
			FineMaster fineMaster = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), FineMaster.class);	
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(fineMaster, FineMaster.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.FINEMASTERUPDATE);
		
			if(responseValidate.equals("")) 
			{
					fineMaster.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					fineMaster.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					fineMaster.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					fineMaster.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					validate.validateFields(fineMaster);
		
					ProcessInstance processInstance = new ProcessInstance();
					processInstance.setBusinessId(fineMaster.getFineUuid());
					processInstance.setTenantId(fineMaster.getTenantId());
					processInstance.setBusinessService(EcConstants.WORKFLOW_FINE);
					processInstance.setAction(fineMaster.getApprovalStatus());
					processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList = Arrays.asList(processInstance);
		
					ResponseInfo response=wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList)
							.requestInfo(requestInfoWrapper.getRequestInfo()).build());
					if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						if(fineMaster.getApprovalStatus().equalsIgnoreCase(EcConstants.ACTION_APPROVE)) 
						{
							fineMaster.setApprovalStatus(EcConstants.STATUS_APPROVED);
						}
						if(fineMaster.getApprovalStatus().equalsIgnoreCase(EcConstants.ACTION_REJECT))
							{
									fineMaster.setApprovalStatus(EcConstants.STATUS_REJECTED);
							}
						repository.updateFine(fineMaster);
					}
		
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(fineMaster).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("FINEMASTER_UPDATE_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Fine Service - Update Fine Exception"+e.getMessage());
			throw new CustomException("FINEMASTER_UPDATE_EXCEPTION", e.getMessage());
		}

	}

}
