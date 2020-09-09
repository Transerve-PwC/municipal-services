package org.egov.cpt.service.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.models.calculation.PaymentDetail;
import org.egov.cpt.models.calculation.PaymentRequest;
import org.egov.cpt.repository.OwnershipTransferRepository;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.service.DuplicateCopyService;
import org.egov.cpt.service.EnrichmentService;
import org.egov.cpt.service.OwnershipTransferService;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class PaymentNotificationService {

	private OwnershipTransferService ownershipTransferService;

	private DuplicateCopyService duplicateCopyService;

	private ObjectMapper mapper;

	private NotificationUtil util;

	private PropertyConfiguration config;

	@Value("${workflow.bpa.businessServiceCode.fallback_enabled}")
	private Boolean pickWFServiceNameFromPropertyTypeOnly;

	@Value("${egov.allowed.businessServices}")
	private String allowedBusinessServices;

	@Autowired
	public PaymentNotificationService(OwnershipTransferService ownershipTransferService,
			OwnershipTransferRepository repositoryOt, DuplicateCopyService duplicateCopyService,
			PropertyRepository propertyRepository, EnrichmentService enrichmentService, ObjectMapper mapper,
			WorkflowService workflowService, NotificationUtil util, PropertyConfiguration config) {
		this.ownershipTransferService = ownershipTransferService;
		this.duplicateCopyService = duplicateCopyService;
		this.mapper = mapper;
		this.util = util;
		this.config = config;
	}

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
			List<String> allowedservices = Arrays.asList(allowedBusinessServices.split(","));
			for (PaymentDetail paymentDetail : paymentDetails) {
				if (allowedservices.contains(paymentDetail.getBusinessService())) {

					valMap.put(mobileKey, paymentDetail.getBill().getMobileNumber());
					valMap.put(emailKey, paymentDetail.getBill().getPayerEmail());

					switch (paymentDetail.getBusinessService()) {
						case PTConstants.BILLING_BUSINESS_SERVICE_OT:

							DuplicateCopySearchCriteria searchCriteria = new DuplicateCopySearchCriteria();
							searchCriteria.setApplicationNumber(paymentDetail.getBill().getConsumerCode());

							List<Owner> owners = ownershipTransferService.searchOwnershipTransfer(searchCriteria,
									requestInfo);
							owners.forEach(owner -> {
								String localizationMessages = util.getLocalizationMessages(owner.getTenantId(),
										requestInfo);
								List<SMSRequest> smsRequests = getOTSMSRequests(owner, valMap, localizationMessages);
								util.sendSMS(smsRequests, config.getIsSMSNotificationEnabled());

								if (config.getIsEMAILNotificationEnabled()) {
									if (owner.getOwnerDetails().getEmail() != null) {
										List<EmailRequest> emailRequests = getOTEmailRequests(owner, valMap,
												localizationMessages);
										util.sendEMAIL(emailRequests, true);
									}
								}
							});

							if (CollectionUtils.isEmpty(owners))
								throw new CustomException("INVALID RECEIPT",
										"No Owner found for the comsumerCode " + searchCriteria.getApplicationNumber());

							break;

						case PTConstants.BILLING_BUSINESS_SERVICE_DC:

							DuplicateCopySearchCriteria searchCriteriaDc = new DuplicateCopySearchCriteria();
							searchCriteriaDc.setApplicationNumber(paymentDetail.getBill().getConsumerCode());

							List<DuplicateCopy> dcApplications = duplicateCopyService
									.searchApplication(searchCriteriaDc, requestInfo);

							dcApplications.forEach(copy -> {
								String localizationMessages = util.getLocalizationMessages(copy.getTenantId(),
										requestInfo);
								List<SMSRequest> smsRequests = getDCSMSRequests(copy, localizationMessages);
								util.sendSMS(smsRequests, config.getIsSMSNotificationEnabled());

								if (config.getIsEMAILNotificationEnabled()) {
									if (copy.getApplicant().get(0).getEmail() != null) {
										List<EmailRequest> emailRequests = getDCEmailRequests(copy, valMap,
												localizationMessages);
										util.sendEMAIL(emailRequests, true);
									}
								}
							});

							if (CollectionUtils.isEmpty(dcApplications))
								throw new CustomException("INVALID RECEIPT", "No Owner found for the comsumerCode "
										+ searchCriteriaDc.getApplicationNumber());

							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * private List<SMSRequest> getDCSMSRequests(DuplicateCopy copy, String
	 * localizationMessages) {
	 * 
	 * SMSRequest payerSmsRequest = getDCSMSRequest(copy, localizationMessages);
	 * 
	 * List<SMSRequest> totalSMS = new LinkedList<>();
	 * totalSMS.add(payerSmsRequest);
	 * 
	 * return totalSMS; }
	 */

	private List<EmailRequest> getDCEmailRequests(DuplicateCopy copy, Map<String, String> valMap2,
			String localizationMessages) {
		EmailRequest ownersEmailRequest = getDCOwnerEmailRequest(copy, valMap, localizationMessages);

		List<EmailRequest> totalEmails = new LinkedList<>();
		totalEmails.add(ownersEmailRequest);

		return totalEmails;
	}

	private EmailRequest getDCOwnerEmailRequest(DuplicateCopy copy, Map<String, String> valMap2,
			String localizationMessages) {
		String message = util.getDCOwnerPaymentMsg(copy, localizationMessages);
		message = message.replace("\\n", "<br/>");
		EmailRequest emailRequest = EmailRequest.builder().subject(PTConstants.EMAIL_SUBJECT).isHTML(true)
				.email(copy.getApplicant().get(0).getEmail()).body(message).build();

		return emailRequest;
	}

	private List<EmailRequest> getOTEmailRequests(Owner owner, Map<String, String> valMap,
			String localizationMessages) {
		EmailRequest ownersEmailRequest = getOTOwnerEmailRequest(owner, valMap, localizationMessages);

		List<EmailRequest> totalEmails = new LinkedList<>();
		totalEmails.add(ownersEmailRequest);

		return totalEmails;
	}

	private EmailRequest getOTOwnerEmailRequest(Owner owner, Map<String, String> valMap2, String localizationMessages) {
		String message = util.getOTOwnerPaymentMsg(owner, localizationMessages);
		message = message.replace("\\n", "<br/>");
		EmailRequest emailRequest = EmailRequest.builder().subject(PTConstants.EMAIL_SUBJECT).isHTML(true)
				.email(owner.getOwnerDetails().getEmail()).body(message).build();

		return emailRequest;
	}

	private List<SMSRequest> getDCSMSRequests(DuplicateCopy copy, String localizationMessages) {
		String message = util.getDCOwnerPaymentMsg(copy, localizationMessages);
		SMSRequest ownerSmsRequest = new SMSRequest(copy.getApplicant().get(0).getPhone(), message);

		/*
		 * String payerMessage = util.getDCPayerPaymentMsg(copy,valMap,
		 * localizationMessages); payerMessage =
		 * payerMessage.replace("<1>",valMap.get(paidByKey)); SMSRequest payerSmsRequest
		 * = new SMSRequest(valMap.get(mobileKey), payerMessage);
		 */

		List<SMSRequest> totalSMS = new LinkedList<>();
		totalSMS.add(ownerSmsRequest);
		// totalSMS.add(payerSmsRequest);

		return totalSMS;
	}

	private List<SMSRequest> getOTSMSRequests(Owner owner, Map<String, String> valMap, String localizationMessages) {
		String ownerMessage = util.getOTOwnerPaymentMsg(owner, localizationMessages);
		SMSRequest ownerSmsRequest = new SMSRequest(owner.getOwnerDetails().getPhone(), ownerMessage);

		/*
		 * String payerMessage = util.getOTPayerPaymentMsg(owner,valMap,
		 * localizationMessages); payerMessage =
		 * payerMessage.replace("<1>",valMap.get(paidByKey)); SMSRequest payerSmsRequest
		 * = new SMSRequest(valMap.get(mobileKey), payerMessage);
		 */

		List<SMSRequest> totalSMS = new LinkedList<>();
		totalSMS.add(ownerSmsRequest);
		// totalSMS.add(payerSmsRequest);

		return totalSMS;
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
			e.printStackTrace();
			throw new CustomException("RECEIPT ERROR", "Unable to fetch values from receipt");
		}
		return valMap;
	}

	/*
	 * private SMSRequest getOTSMSRequest(Owner owner, String localizationMessages)
	 * { String message = util.getOTPaymentMsg(owner, localizationMessages);
	 * SMSRequest smsRequest = new SMSRequest(valMap.get(mobileKey), message);
	 * return smsRequest; }
	 */

}
