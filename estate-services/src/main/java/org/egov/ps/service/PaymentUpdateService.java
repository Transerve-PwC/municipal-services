package org.egov.ps.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.calculation.PaymentDetail;
import org.egov.ps.model.calculation.PaymentRequest;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.BusinessService;
import org.egov.ps.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentUpdateService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private Util util;

	/**
	 * Process the message from kafka and updates the status to paid
	 * 
	 * @param record The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			for (PaymentDetail paymentDetail : paymentDetails) {
				switch (paymentDetail.getBusinessService()) {
				case PSConstants.BUSINESS_SERVICE_EB_RENT:
				case PSConstants.BUSINESS_SERVICE_BB_RENT:
				case PSConstants.BUSINESS_SERVICE_MB_RENT:
					log.info("Post enrichment need to do");
				break;
				default:
				{
				ApplicationCriteria searchCriteria = new ApplicationCriteria();
				searchCriteria.setApplicationNumber(paymentDetail.getBill().getConsumerCode());

				List<Application> applications = applicationService.searchApplication(searchCriteria, requestInfo);

				BusinessService otBusinessService = workflowService.getBusinessService(
						applications.get(0).getTenantId(), requestInfo,
						applications.get(0).getWorkFlowBusinessService());

				if (CollectionUtils.isEmpty(applications))
					throw new CustomException("INVALID RECEIPT",
							"No Owner found for the comsumerCode " + searchCriteria.getApplicationNumber());

				applications.forEach(application -> application.setAction(PSConstants.ACTION_PAY));

				Role role = Role.builder().code("SYSTEM_PAYMENT").build();
				requestInfo.getUserInfo().getRoles().add(role);
				
				
				ApplicationRequest updateRequest = ApplicationRequest.builder().requestInfo(requestInfo)
						.applications(applications).build();

				updateRequest.getApplications()
						.forEach(obj -> log.info(" the status of the application is : " + obj.getState()));

				/**
				 * Payment is not the end state for Ownership Transfer. No need to postEnrich
				 */

				Map<String, Boolean> idToIsStateUpdatableMap = util.getIdToIsStateUpdatableMap(otBusinessService,
						applications);

				applicationService.updatePostPayment(updateRequest, idToIsStateUpdatableMap);
				break;
			}
				
		  }
		} 
		}catch (

		Exception e) {
			log.error("Error while processing the payment ", e);
		}

	}
}