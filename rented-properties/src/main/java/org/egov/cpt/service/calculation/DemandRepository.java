package org.egov.cpt.service.calculation;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.BillResponseV2;
import org.egov.cpt.models.BillV2;
import org.egov.cpt.models.CollectionPaymentRequest;
import org.egov.cpt.models.CollectionPaymentResponse;
import org.egov.cpt.models.calculation.Demand;
import org.egov.cpt.models.calculation.DemandRequest;
import org.egov.cpt.models.calculation.DemandResponse;
import org.egov.cpt.repository.ServiceRequestRepository;
import org.egov.cpt.util.PTConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DemandRepository {

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Creates demand
	 * 
	 * @param requestInfo The RequestInfo of the calculation Request
	 * @param demands     The demands to be created
	 * @return The list of demand created
	 */
	public List<Demand> saveDemand(RequestInfo requestInfo, List<Demand> demands) {
		StringBuilder url = new StringBuilder(config.getBillingHost());
		url.append(config.getDemandCreateEndpoint());
		DemandRequest request = new DemandRequest(requestInfo, demands);
		Object result = serviceRequestRepository.fetchResult(url, request);
		DemandResponse response = null;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of create demand");
		}
		return response.getDemands();
	}

	/**
	 * Updates the demand
	 * 
	 * @param requestInfo The RequestInfo of the calculation Request
	 * @param demands     The demands to be updated
	 * @return The list of demand updated
	 */
	public List<Demand> updateDemand(RequestInfo requestInfo, List<Demand> demands) {
		StringBuilder url = new StringBuilder(config.getBillingHost());
		url.append(config.getDemandUpdateEndpoint());
		DemandRequest request = new DemandRequest(requestInfo, demands);
		Object result = serviceRequestRepository.fetchResult(url, request);
		DemandResponse response = null;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of update demand");
		}
		return response.getDemands();

	}

	public List<BillV2> fetchBill(RequestInfo requestInfo, String tenantId, String consumerCode,String businessService) {
		StringBuilder url = new StringBuilder(config.getBillingHost());
		String uri = config.getBillGenearateEndpoint().replace("$tenantId", tenantId)
				.replace("$consumerCode", consumerCode)
				.replace("$businessService", businessService);
		url.append(uri);
		Object result = serviceRequestRepository.fetchResult(url, Collections.singletonMap("requestInfo", requestInfo));
		BillResponseV2 response = null;
		try {
			response = mapper.convertValue(result, BillResponseV2.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of update demand");
		}
		return response.getBill();
	}

	public Object savePayment(CollectionPaymentRequest paymentRequest) {
		StringBuilder url = new StringBuilder(config.getCollectionPaymentHost());
		url.append(config.getCollectionPaymentEndPoint());
		Object result = serviceRequestRepository.fetchResult(url, paymentRequest);
		try {
			CollectionPaymentResponse response = mapper.convertValue(result, CollectionPaymentResponse.class);
			return response.getPayments();
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response of update demand");
		}
	}
}
