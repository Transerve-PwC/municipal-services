package org.egov.cpt.workflow;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.RequestInfoWrapper;
import org.egov.cpt.models.calculation.BusinessService;
import org.egov.cpt.models.calculation.BusinessServiceResponse;
import org.egov.cpt.models.calculation.State;
import org.egov.cpt.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowService {

	private PropertyConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	@Autowired
	public WorkflowService(PropertyConfiguration config, ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	/**
	 * Get the workflow config for the given tenant
	 * 
	 * @param tenantId    The tenantId for which businessService is requested
	 * @param requestInfo The RequestInfo object of the request
	 * @return BusinessService for the the given tenantId
	 */
	@Cacheable(value = "businessService", key = "{#tenantId, #businessServiceName}")
	public BusinessService getBusinessService(String tenantId, RequestInfo requestInfo, String businessServiceName) {

		StringBuilder url = getSearchURLWithParams(tenantId, businessServiceName);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		try {
			BusinessServiceResponse response = null;
			response = mapper.convertValue(result, BusinessServiceResponse.class);
			if (CollectionUtils.isEmpty(response.getBusinessServices())) {
				throw new CustomException("NO BUSINESS SERVICE",
						String.format("No business service found with name %s", businessServiceName));
			}
			return response.getBusinessServices().get(0);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of calculate");
		}
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
	 * 
	 * @param stateCode       The stateCode of the license
	 * @param businessService The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public Boolean isStateUpdatable(String stateCode, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null && state.getApplicationStatus().equalsIgnoreCase(stateCode))
				return state.getIsStateUpdatable();
		}
		return null;
	}

}
