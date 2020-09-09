package org.egov.cpt.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Component
@Slf4j
public class NotificationUtil {

	private PropertyConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private Producer producer;

	private static DecimalFormat decimalFormat = new DecimalFormat("0.00");

	@Autowired
	public NotificationUtil(PropertyConfiguration config, ServiceRequestRepository serviceRequestRepository,
			Producer producer) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.producer = producer;
	}

	final String receiptNumberKey = "receiptNumber";

	final String amountPaidKey = "amountPaid";

	final String consumerCodeKey = "consumerCodeKey";

	/**
	 * Creates customised message based on ownershipTransfer
	 * 
	 * @param application
	 * 
	 * @param license             The tradeLicense for which message is to be sent
	 * @param localizationMessage The messages from localisation
	 * @return customised message based on ownershipTransfer
	 */
	public String getCustomizedOTMsg(RequestInfo requestInfo, Owner owner, String localizationMessage) {
		String message = null, messageTemplate;
		String ACTION_STATUS = owner.getApplicationAction() + "_" + owner.getApplicationState();

		switch (ACTION_STATUS) {

			case PTConstants.OT_ACTION_STATUS_SUBMIT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_SUBMIT, localizationMessage);
				message = getInitiatedOtMsg(owner, messageTemplate);
				break;

			case PTConstants.OT_ACTION_STATUS_REJECTED:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_REJECTED, localizationMessage);
				message = getInitiatedOtMsg(owner, messageTemplate);
				break;

			case PTConstants.OT_ACTION_STATUS_SENDBACK:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_SENDBACK, localizationMessage);
				message = getInitiatedOtMsg(owner, messageTemplate);
				break;

			case PTConstants.OT_ACTION_STATUS_APPROVED:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_APPROVED, localizationMessage);
				message = getInitiatedOtMsg(owner, messageTemplate);
				break;

			case PTConstants.OT_ACTION_STATUS_PAYMENT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_PAYMENT, localizationMessage);
				message = getInitiatedOtMsg(owner, messageTemplate);
				break;
			case PTConstants.OT_ACTION_STATUS_REJECTED_AFTER_PAYMENT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_REJECTED, localizationMessage);
				message = getInitiatedOtMsg(owner, messageTemplate);
				break;
		}
		return message;
	}

	private String getInitiatedOtMsg(Owner owner, String message) {
		BigDecimal due = owner.getOwnerDetails().getDueAmount();
		BigDecimal charge = owner.getOwnerDetails().getAproCharge();
		message = message.replace("<2>", owner.getOwnerDetails().getName());
		message = message.replace("<3>", PTConstants.OWNERSHIP_TRANSFER_APPLICATION);
		message = message.replace("<4>", owner.getOwnerDetails().getApplicationNumber());
		if (message.contains("<5>")) {
			message = message.replace("<5>", due.add(charge) + "");
		}
		return message;
	}

	@SuppressWarnings("unchecked")
	private String getMessageTemplate(String notificationCode, String localizationMessage) {
		String path = "$..messages[?(@.code==\"{}\")].message";
		path = path.replace("{}", notificationCode);
		String message = null;
		try {
			Object messageObj = JsonPath.parse(localizationMessage).read(path);
			message = ((ArrayList<String>) messageObj).get(0);
		} catch (Exception e) {
			// log.warn("Fetching from localization failed", e);
			return "" + e;
		}
		return message;
	}

	/**
	 * Creates sms request for the each owners
	 * 
	 * @param message                 The message for the specific ownershipTransfer
	 * @param mobileNumberToOwnerName Map of mobileNumber to OwnerName
	 * @return List of SMSRequest
	 */
	public List<SMSRequest> createSMSRequest(String message, Map<String, String> mobileNumberToOwner) {
		List<SMSRequest> smsRequest = new LinkedList<>();
		for (Map.Entry<String, String> entryset : mobileNumberToOwner.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			smsRequest.add(new SMSRequest(entryset.getKey(), customizedMsg));
		}
		return smsRequest;
	}

	public List<EmailRequest> createEMAILRequest(String message, Map<String, String> emailIdToApplicant) {
		List<EmailRequest> emailRequest = new LinkedList<>();
		for (Map.Entry<String, String> entryset : emailIdToApplicant.entrySet()) {
			String customizedMsg = message.replace("<1>", entryset.getValue());
			emailRequest.add(EmailRequest.builder().email(entryset.getKey()).subject(PTConstants.EMAIL_SUBJECT)
					.body(customizedMsg).isHTML(false).build());
		}
		return emailRequest;
	}

	public void sendSMS(List<SMSRequest> smsRequestsList, boolean isSMSEnabled) {
		if (isSMSEnabled) {
			if (CollectionUtils.isEmpty(smsRequestsList)) {
				// log.info("Messages from localization couldn't be fetched!");
			}
			for (SMSRequest smsRequest : smsRequestsList) {
				producer.push(config.getSmsNotifTopic(), smsRequest);
				// log.info("MobileNumber: " + smsRequest.getMobileNumber() + " Messages: " +
				// smsRequest.getMessage());
			}
		}

	}

	public void sendEMAIL(List<EmailRequest> emailRequestList, boolean isEMAILEnabled) {
		if (isEMAILEnabled) {
			if (CollectionUtils.isEmpty(emailRequestList))
				log.info("Messages from localization couldn't be fetched!");
			for (EmailRequest emailRequest : emailRequestList) {
				producer.pushEmail(config.getEmailNotifTopic(), emailRequest.getEmail(), emailRequest.getBody(),
						PTConstants.EMAIL_SUBJECT, false);
				log.info("EmailAddress: " + emailRequest.getEmail() + " Messages: " + emailRequest.getBody());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public String getLocalizationMessages(String tenantId, RequestInfo requestInfo) {
		LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) serviceRequestRepository
				.fetchResult(getUri(tenantId, requestInfo), requestInfo);
		String jsonString = new JSONObject(responseMap).toString();
		return jsonString;
	}

	private StringBuilder getUri(String tenantId, RequestInfo requestInfo) {

		tenantId = tenantId.split("\\.")[0];

		String locale = PTConstants.NOTIFICATION_LOCALE;
		if (!StringUtils.isEmpty(requestInfo.getMsgId()) && requestInfo.getMsgId().split("|").length >= 2) {
			locale = requestInfo.getMsgId().split("\\|")[1];
		}

		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
				.append(config.getLocalizationSearchEndpoint()).append("?").append("locale=").append(locale)
				.append("&tenantId=").append(tenantId).append("&module=").append(PTConstants.MODULE);

		return uri;
	}

	// Duplicate Copy Notifications

	public String getCustomizedDcMsg(RequestInfo requestInfo, DuplicateCopy copy, String localizationMessage) {

		String message = null, messageTemplate;
		String ACTION_STATUS = copy.getAction() + "_" + copy.getState();

		switch (ACTION_STATUS) {

			case PTConstants.DC_ACTION_STATUS_SUBMIT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_SUBMIT, localizationMessage);
				message = getInitiatedDcMsg(copy, messageTemplate);
				break;

			case PTConstants.DC_ACTION_STATUS_REJECTED:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_REJECTED, localizationMessage);
				message = getInitiatedDcMsg(copy, messageTemplate);
				break;

			case PTConstants.DC_ACTION_STATUS_SENDBACK:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_SENDBACK, localizationMessage);
				message = getInitiatedDcMsg(copy, messageTemplate);
				break;

			case PTConstants.DC_ACTION_STATUS_APPROVED:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_APPROVED, localizationMessage);
				message = getInitiatedDcMsg(copy, messageTemplate);
				break;

			case PTConstants.DC_ACTION_STATUS_PAYMENT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_PAYMENT, localizationMessage);
				message = getInitiatedDcMsg(copy, messageTemplate);
				break;
			case PTConstants.DC_ACTION_STATUS_REJECTED_AFTER_PAYMENT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_REJECTED, localizationMessage);
				message = getInitiatedDcMsg(copy, messageTemplate);
				break;
		}
		return message;
	}

	private String getInitiatedDcMsg(DuplicateCopy copy, String message) {
		BigDecimal fee = copy.getApplicant().get(0).getFeeAmount();
		BigDecimal charge = copy.getApplicant().get(0).getAproCharge();
		message = message.replace("<2>", copy.getApplicant().get(0).getName());
		message = message.replace("<3>", PTConstants.DUPLICATE_COPY_APPLICATION);
		message = message.replace("<4>", copy.getApplicationNumber());
		if (message.contains("<5>")) {
			message = message.replace("<5>", fee.add(charge) + "");
		}
		return message;
	}

	public String getOTOwnerPaymentMsg(Owner owner, String localizationMessages) {
		String messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_PAYMENT_SUCCESS, localizationMessages);
		messageTemplate = messageTemplate.replace("<2>", owner.getOwnerDetails().getName());
		messageTemplate = messageTemplate.replace("<3>", PTConstants.OWNERSHIP_TRANSFER_APPLICATION);
		messageTemplate = messageTemplate.replace("<4>", owner.getOwnerDetails().getApplicationNumber());

		return messageTemplate;
	}

	public String getDCOwnerPaymentMsg(DuplicateCopy copy, String localizationMessages) {
		String messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_PAYMENT_SUCCESS, localizationMessages);
		messageTemplate = messageTemplate.replace("<2>", copy.getApplicant().get(0).getName());
		messageTemplate = messageTemplate.replace("<3>", PTConstants.DUPLICATE_COPY_APPLICATION);
		messageTemplate = messageTemplate.replace("<4>", copy.getApplicationNumber());

		return messageTemplate;
	}

	// Mortgage Notifications

	public String getCustomizedMGMsg(RequestInfo requestInfo, Mortgage mortgage, String localizationMessage) {
		String message = null, messageTemplate;
		String ACTION_STATUS = mortgage.getAction() + "_" + mortgage.getState();

		switch (ACTION_STATUS) {

			case PTConstants.MG_ACTION_STATUS_SUBMIT:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_SUBMIT, localizationMessage);
				message = getInitiatedMGMsg(mortgage, messageTemplate);
				break;

			case PTConstants.MG_ACTION_STATUS_REJECTED:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_REJECTED, localizationMessage);
				message = getInitiatedMGMsg(mortgage, messageTemplate);
				break;

			case PTConstants.MG_ACTION_STATUS_SENDBACK:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_SENDBACK, localizationMessage);
				message = getInitiatedMGMsg(mortgage, messageTemplate);
				break;

			case PTConstants.MG_ACTION_STATUS_MORTGAGE_APPROVED:
				messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_APPROVED, localizationMessage);
				message = getInitiatedMGMsg(mortgage, messageTemplate);
				break;

		}
		return message;
	}

	private String getInitiatedMGMsg(Mortgage mortgage, String message) {
		message = message.replace("<2>", mortgage.getApplicant().get(0).getName());
		message = message.replace("<3>", PTConstants.MORTGAGE_APPLICATION);
		message = message.replace("<4>", mortgage.getApplicationNumber());

		return message;
	}

	public String getCustomizedNoticeMsg(RequestInfo requestInfo, NoticeGeneration notice, Owner ownerDtl,
			String localizationMessages) {
		String message = null, messageTemplate;
		if (notice.getNoticeType().equalsIgnoreCase(PTConstants.NG_TYPE_VIOLATION)) {
			messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_NG_VIOLATION, localizationMessages);
			message = getViolationNoticeMsg(notice, ownerDtl, messageTemplate);
		} else {
			messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_NG_RECOVERY, localizationMessages);
			message = getRecoveryNoticeMsg(notice, ownerDtl, messageTemplate);
		}
		return message;
	}

	private String getViolationNoticeMsg(NoticeGeneration notice, Owner ownerDtl, String message) {
		message = message.replace("<1>", ownerDtl.getOwnerDetails().getName());
		message = message.replace("<2>", ownerDtl.getAllotmenNumber());
		message = message.replace("<3>", notice.getMemoNumber());

		return message;
	}

	private String getRecoveryNoticeMsg(NoticeGeneration notice, Owner ownerDtl, String message) {
		message = message.replace("<1>", ownerDtl.getOwnerDetails().getName());
		message = message.replace("<2>", notice.getMemoNumber());
		message = message.replace("<3>", decimalFormat.format(notice.getAmount()).toString());

		return message;
	}

	public String getOTPayerPaymentMsg(Owner owner, Map<String, String> valMap, String localizationMessages) {
		String messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_PAYMENT_SUCCESS_PAYER,
				localizationMessages);
		messageTemplate = messageTemplate.replace("<2>", valMap.get(amountPaidKey));
		messageTemplate = messageTemplate.replace("<3>", owner.getOwnerDetails().getApplicationNumber());
		messageTemplate = messageTemplate.replace("<4>", valMap.get(receiptNumberKey));
		return messageTemplate;
	}

	public String getDCPayerPaymentMsg(DuplicateCopy copy, Map<String, String> valMap, String localizationMessages) {
		String messageTemplate = getMessageTemplate(PTConstants.NOTIFICATION_OT_PAYMENT_SUCCESS_PAYER,
				localizationMessages);
		messageTemplate = messageTemplate.replace("<2>", valMap.get(amountPaidKey));
		messageTemplate = messageTemplate.replace("<3>", copy.getApplicationNumber());
		messageTemplate = messageTemplate.replace("<4>", valMap.get(receiptNumberKey));
		return messageTemplate;
	}

}
