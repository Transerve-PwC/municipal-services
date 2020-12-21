package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.model.calculation.PaymentDetail;
import org.egov.ps.model.calculation.PaymentRequest;
import org.egov.ps.repository.ApplicationRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class PaymentNotificationService {
	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private MDMSService mdmsservice;

	@Autowired
	Util util;

	@Autowired
	private ApplicationsNotificationService applicationNotificationService;

	@Autowired
	private PropertyNotificationService propertyNotificationService;

	@Autowired
	private PropertyService propertyService;


	final String tenantId = "tenantId";

	final String businessServiceKey = "businessService";

	final String consumerCode = "consumerCode";

	final String mobileKey = "mobileKey";

	final String emailKey = "ownerEmail";

	final String payerMobileNumberKey = "mobileNumber";

	final String paidByKey = "paidBy";

	final String amountPaidKey = "amountPaid";

	final String receiptNumberKey = "receiptNumber";

	final String payerName = "payerName";
	

	Map<String, String> valMap = new HashMap<>();

	/**
	 * Process the message from kafka and updates the status to paid
	 * 
	 * @param record The incoming message from receipt create consumer
	 */
	public void process(HashMap<String, Object> record) {

		try {
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();

			String jsonString = new JSONObject(record).toString();
			DocumentContext documentContext = JsonPath.parse(jsonString);
			List<String> businessServiceList = documentContext
					.read("$.Payment.paymentDetails[?(@.businessService)].businessService");

			Map<String, String> valMap = enrichValMap(documentContext, businessServiceList.get(0));

			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			for (PaymentDetail paymentDetail : paymentDetails) {

				valMap.put(mobileKey, paymentDetail.getBill().getMobileNumber());
				valMap.put(emailKey, paymentDetail.getBill().getPayerEmail());

				switch (paymentDetail.getBusinessService()) {
				case PSConstants.BUSINESS_SERVICE_EB_RENT:
				case PSConstants.BUSINESS_SERVICE_BB_RENT:
				case PSConstants.BUSINESS_SERVICE_MB_RENT:
					log.info("Post enrichment need to do");
					break;
				case PSConstants.BUSINESS_SERVICE_EB_PENALTY:
				case PSConstants.BUSINESS_SERVICE_BB_PENALTY:
				case PSConstants.BUSINESS_SERVICE_MB_PENALTY:
					String consumerCode = paymentDetail.getBill().getConsumerCode();

					PropertyCriteria searchCriteria = new PropertyCriteria();
					searchCriteria.setFileNumber(util.getFileNumberFromRentFeeConsumerCode(consumerCode));
					List<Property> properties = propertyService.searchProperty(searchCriteria, requestInfo);

					if (CollectionUtils.isEmpty(properties))
						throw new CustomException("INVALID RECEIPT",
								"No Owner found for the comsumerCode " + consumerCode);
					properties.forEach(property -> {
						if(paymentRequest.getPayment().getPaymentMode().equalsIgnoreCase(PSConstants.PAYMENT_MODE_OFFLINE)) 
							property.setTransactionNumber(property.getPropertyDetails().getOfflinePaymentDetails().get(0).getTransactionNumber());
						else
							property.setTransactionNumber(paymentRequest.getPayment().getTransactionNumber());
						
						 property.setPaymentAmount(new BigDecimal(valMap.get(amountPaidKey)));
						 
						if(paymentRequest.getPayment().getPaymentMode().equalsIgnoreCase(PSConstants.PAYMENT_MODE_ONLINE) && valMap.get(payerMobileNumberKey)!=null){
							User payer = User.builder().mobileNumber(valMap.get(payerMobileNumberKey))
									.emailId(valMap.get(emailKey))
									.name(valMap.get(payerName))
									.build();
							
							property.setNotificationCode(String.format("%s_%s", property.getPropertyDetails().getBranchType(),PSConstants.PROPERTY_PAYMENT_PAYER));
							  property.setPayer(payer); 

							/**
							 * Process the notification config for payer
							 */
							propertyNotificationService.processPaymentNotification(property, paymentRequest.getPayment().getPaymentMode());
						}
						/**
						 * Process the notification config for Owner
						 */
						property.setNotificationCode(String.format("%s_%s", property.getPropertyDetails().getBranchType(),PSConstants.PROPERTY_PAYMENT_OWNER));
						propertyNotificationService.processPaymentNotification(property, paymentRequest.getPayment().getPaymentMode());

					});
					break;
				default: {
					if (paymentDetail.getBusinessService().startsWith(PSConstants.ESTATE_SERVICE)) {
						ApplicationCriteria applicationCriteria = ApplicationCriteria.builder()
								.applicationNumber(paymentDetail.getBill().getConsumerCode()).build();
						List<Application> applications = applicationRepository.getApplications(applicationCriteria);
						if (CollectionUtils.isEmpty(applications))
							throw new CustomException("INVALID RECEIPT",
									"No Owner found for the comsumerCode " + applicationCriteria.getApplicationNumber());
						applications.forEach(application -> {
							/**
							 * Get the notification config from mdms.
							 */
							List<Map<String, Object>> notificationConfigs = mdmsservice.getNotificationConfig(
									application.getMDMSModuleName(), requestInfo, application.getTenantId());

							if(valMap.get(payerMobileNumberKey)!=null){
								User payer = User.builder().mobileNumber(valMap.get(payerMobileNumberKey))
										.emailId(valMap.get(emailKey))
										.name(valMap.get(payerName))
										.build();
								application.setPayer(payer);
								application.setState("PAYMENT_NOTIFICATION_PAYER");
								application.setPaymentAmount(new BigDecimal(valMap.get(amountPaidKey)));
								application.setRecieptNumber(valMap.get(receiptNumberKey));

								/**
								 * Process the notification config for payer
								 */
								applicationNotificationService.processNotification(notificationConfigs, application, requestInfo);
							}
							/**
							 * Process the notification config for Owner
							 */
							application.setState("PAYMENT_NOTIFICATION");
							applicationNotificationService.processNotification(notificationConfigs, application, requestInfo);

						});
					}
				}
				}
			}
		}catch (Exception e) {
			log.error("Failed to notify the payment information to payer ",e);
		}
	}
	private Map<String, String> enrichValMap(DocumentContext context, String businessService) {
		Map<String, String> valMap = new HashMap<>();
		try {

			List<String> businessServiceList = context
					.read("$.Payment.paymentDetails[?(@.businessService=='" + businessService + "')].businessService");
			List<String> consumerCodeList = context.read(
					"$.Payment.paymentDetails[?(@.businessService=='" + businessService + "')].bill.consumerCode");
			List<String> mobileNumberList = context.read(
					"$.Payment.paymentDetails[?(@.businessService=='" + businessService + "')].bill.mobileNumber");
			List<Integer> amountPaidList = context
					.read("$.Payment.paymentDetails[?(@.businessService=='" + businessService + "')].bill.amountPaid");
			List<String> receiptNumberList = context
					.read("$.Payment.paymentDetails[?(@.businessService=='" + businessService + "')].receiptNumber");
			valMap.put(businessServiceKey, businessServiceList.isEmpty() ? null : businessServiceList.get(0));
			valMap.put(consumerCode, consumerCodeList.isEmpty() ? null : consumerCodeList.get(0));
			valMap.put(tenantId, context.read("$.Payment.tenantId"));
			valMap.put(payerMobileNumberKey, mobileNumberList.isEmpty() ? null : mobileNumberList.get(0));
			valMap.put(paidByKey, context.read("$.Payment.paidBy"));
			valMap.put(amountPaidKey, amountPaidList.isEmpty() ? null : String.valueOf(amountPaidList.get(0)));
			valMap.put(receiptNumberKey, receiptNumberList.isEmpty() ? null : receiptNumberList.get(0));
			valMap.put(payerName, context.read("$.Payment.payerName"));
		} catch (Exception e) {
			log.error("Error while fetching payment reciept values ",e);
			throw new CustomException("RECEIPT ERROR", "Unable to fetch values from receipt");
		}
		return valMap;
	}
}
