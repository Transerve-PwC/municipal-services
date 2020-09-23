package org.egov.waterconnection.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.model.RequestInfoWrapper;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.workflow.*;
import org.egov.waterconnection.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class WorkflowService {

	@Autowired
    private WSConfiguration config;

	@Autowired
    private WorkflowIntegrator workflowIntegrator;
	
	@Autowired
    private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
    private ObjectMapper mapper;

    /**
     * Get the workflow-config for the given tenant
     * @param tenantId    The tenantId for which businessService is requested
     * @param requestInfo The RequestInfo object of the request
     * @return BusinessService for the the given tenantId
     */
    public BusinessService getBusinessService(String tenantId, RequestInfo requestInfo, String activityType) {
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        String BusinessService= workflowIntegrator.getBusinessService(activityType); 
        Object result = serviceRequestRepository.fetchResult(getSearchURLWithParams(tenantId, "NewWS1"), requestInfoWrapper);
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result,BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
        }
        return response.getBusinessServices().get(0);
    }


    /**
     * Creates url for search based on given tenantId
     *
     * @param tenantId The tenantId for which url is generated
     * @return The search url
     */
    private StringBuilder getSearchURLWithParams(String tenantId, String businessServiceName) {
        StringBuilder url = new StringBuilder(config.getWfHost());
        url.append(config.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessServices=");
        url.append(businessServiceName);
        return url;
    }


    /**
     * Returns boolean value to specifying if the state is updatable
     * @param stateCode The stateCode of the license
     * @param businessService The BusinessService of the application flow
     * @return State object to be fetched
     */
    public Boolean isStateUpdatable(String stateCode, BusinessService businessService){
       for(State state : businessService.getStates()){
           if(state.getApplicationStatus()!=null && state.getApplicationStatus().equalsIgnoreCase(stateCode))
               return state.getIsStateUpdatable();
       }
       return null;
    }
    
   /**
    * Return sla based on state code
    * 
    * @param tenantId
    * @param requestInfo
    * @param stateCode
    * @return no of days for sla
    */
	public BigDecimal getSlaForState(String tenantId, RequestInfo requestInfo, String stateCode, String businessServiceName) {
		BusinessService businessService = getBusinessService(tenantId, requestInfo, businessServiceName);
		return new BigDecimal(businessService.getStates().stream().filter(state -> state.getApplicationStatus() != null
				&& state.getApplicationStatus().equalsIgnoreCase(stateCode)).map(state -> {
					if (state.getSla() == null) {
						return 0l;
					}
					return state.getSla();
				}).findFirst().orElse(0l));
	}
	
	/**
	 * Get the workflow processInstance for the given tenant
	 * 
	 * @param tenantId
	 *            The tenantId for which businessService is requested
	 * @param requestInfo
	 *            The RequestInfo object of the request
	 * @return BusinessService for the the given tenantId
	 */
	public ProcessInstance getProcessInstance(RequestInfo requestInfo, String applicationNo, String tenantId, String businessServiceValue) {
		StringBuilder url = getProcessInstanceSearchURL(tenantId, applicationNo, businessServiceValue);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ProcessInstanceResponse response = null;
		try {
			response = mapper.convertValue(result, ProcessInstanceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of process instance");
		}
		Optional<ProcessInstance> processInstance = response.getProcessInstances().stream().findFirst();
		return processInstance.get();
	}
	/**
	 * 
	 * @param tenantId
	 * @param applicationNo
	 * @return
	 */
	private StringBuilder getProcessInstanceSearchURL(String tenantId, String applicationNo, String businessServiceValue) {
		StringBuilder url = new StringBuilder(config.getWfHost());
		url.append(config.getWfProcessSearchPath());
		url.append("?tenantId=");
		url.append(tenantId);
		url.append("&businessservices=");
		url.append(businessServiceValue);
		url.append("&businessIds=");
		url.append(applicationNo);
		return url;
	}
	/**
	 * 
	 * @param requestInfo
	 * @param applicationNo
	 * @return
	 */
	public String getApplicationStatus(RequestInfo requestInfo, String applicationNo, String tenantId, String businessServiceValue) {
		return getProcessInstance(requestInfo, applicationNo, tenantId, businessServiceValue).getState().getApplicationStatus();
	}

	private List<ProcessInstance> getProcessInstance(RequestInfo requestInfo, Set<String> applicationNos, String tenantId, String businessServiceValue) {
		StringBuilder url = getProcessInstanceSearchURL(tenantId, applicationNos, businessServiceValue);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		ProcessInstanceResponse response = null;
		try {
			response = mapper.convertValue(result, ProcessInstanceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of process instance");
		}
		return response.getProcessInstances();
	}
	
	private StringBuilder getProcessInstanceSearchURL(String tenantId, Set<String> applicationNos, String businessServiceValue) {
		StringBuilder url = new StringBuilder(config.getWfHost());
		url.append(config.getWfProcessSearchPath());
		url.append("?tenantId=");
		url.append(tenantId);
		url.append("&businessservices=");
		url.append(businessServiceValue);
		url.append("&businessIds=");
		for(String appNo : applicationNos) {
			url.append(appNo).append(",");
		}
		url.setLength(url.length()-1);
		return url;
	}

	/**
	 *
	 * @param waterConnectionList
	 * @param requestInfo
	 * @param tenantId
	 */
	/*
	 * public void validateInProgressWF(List<WaterConnection> waterConnectionList,
	 * RequestInfo requestInfo, String tenantId) { WaterConnection
	 * waterConnectionWithWF = null; Set<String> applicationNos =
	 * waterConnectionList.stream().map(WaterConnection::getApplicationNo).collect(
	 * Collectors.toSet()); List<ProcessInstance> processInstanceList =
	 * getProcessInstance(requestInfo, applicationNos, tenantId,
	 * config.getModifyWSBusinessServiceName());
	 * processInstanceList.forEach(processInstance -> { if
	 * (!processInstance.getState().getIsTerminateState()) { throw new
	 * CustomException("WS_APP_EXIST_IN_WF",
	 * "Application already exist in WorkFlow. Cannot modify connection."); } }); }
	 */
}
