package org.egov.ps.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
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
	private PropertyService propertyService;

	@Autowired
	private RentEnrichmentService rentEnrichmentService;

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
					case PSConstants.BUSINESS_SERVICE_BB_RENT: {
						String consumerCode = paymentDetail.getBill().getConsumerCode();

						PropertyCriteria searchCriteria = new PropertyCriteria();
						searchCriteria.setFileNumber(util.getFileNumberFromConsumerCode(consumerCode));

						List<Property> properties = propertyService.searchProperty(searchCriteria, requestInfo);

						if (CollectionUtils.isEmpty(properties))
							throw new CustomException("INVALID RECEIPT",
									"No Property found for the comsumerCode " + consumerCode);

						rentEnrichmentService.postEnrichmentForRentPayment(requestInfo, properties.get(0),
								paymentDetail);

						break;
					}

					case PSConstants.BUSINESS_SERVICE_MB_RENT: {
						String consumerCode = paymentDetail.getBill().getConsumerCode();

						PropertyCriteria searchCriteria = new PropertyCriteria();
						searchCriteria.setFileNumber(util.getMmFileNumberFromConsumerCode(consumerCode));
						searchCriteria.setRelations(Collections.singletonList("finance"));

						List<Property> properties = propertyService.searchProperty(searchCriteria, requestInfo);

						if (CollectionUtils.isEmpty(properties))
							throw new CustomException("INVALID RECEIPT",
									"No Property found for the comsumerCode " + consumerCode);

						rentEnrichmentService.postEnrichmentForMmRentPayment(requestInfo, properties.get(0),
								paymentDetail);

						break;
					}
					case PSConstants.BUSINESS_SERVICE_EB_PENALTY:
					case PSConstants.BUSINESS_SERVICE_BB_PENALTY:
					case PSConstants.BUSINESS_SERVICE_MB_PENALTY:
						log.info("Post enrichment need to do");
						break;

					case PSConstants.BUSINESS_SERVICE_EB_EXTENSION_FEE:
					case PSConstants.BUSINESS_SERVICE_BB_EXTENSION_FEE:
					case PSConstants.BUSINESS_SERVICE_MB_EXTENSION_FEE:
						log.info("Post enrichment need to do");
						break;

					case PSConstants.BUSINESS_SERVICE_EB_SECURITY_DEPOSIT:
					case PSConstants.BUSINESS_SERVICE_BB_SECURITY_DEPOSIT:
					case PSConstants.BUSINESS_SERVICE_MB_SECURITY_DEPOSIT:
						log.info("Post enrichment need to do");
						break;

					default: {
						if (paymentDetail.getBusinessService().startsWith(PSConstants.ESTATE_SERVICE)) {

							ApplicationCriteria searchCriteria = new ApplicationCriteria();
							searchCriteria.setApplicationNumber(paymentDetail.getBill().getConsumerCode());

							if (paymentDetail.getBusinessService().contains(PSConstants.ESTATE_BRANCH)) {
								searchCriteria.setBranchType(Arrays.asList(PSConstants.APPLICATION_ESTATE_BRANCH));
							} else if (paymentDetail.getBusinessService().contains(PSConstants.BUILDING_BRANCH)) {
								searchCriteria.setBranchType(Arrays.asList(PSConstants.APPLICATION_BUILDING_BRANCH));
							} else {
								searchCriteria.setBranchType(Arrays.asList(PSConstants.APPLICATION_MANI_MAJRA));
							}
							List<Application> applications = applicationService.searchApplication(searchCriteria,
									requestInfo);

							if (CollectionUtils.isEmpty(applications))
								throw new CustomException("INVALID RECEIPT",
										"No Owner found for the comsumerCode " + searchCriteria.getApplicationNumber());

							BusinessService otBusinessService = workflowService.getBusinessService(
									applications.get(0).getTenantId(), requestInfo,
									applications.get(0).getWorkFlowBusinessService());

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

							Map<String, Boolean> idToIsStateUpdatableMap = util
									.getIdToIsStateUpdatableMap(otBusinessService, applications);

							applicationService.updatePostPayment(updateRequest, idToIsStateUpdatableMap);
						}
						break;
					}

				}
			}
		} catch (

		Exception e) {
			log.error("Error while processing the payment ", e);
		}

	}
}