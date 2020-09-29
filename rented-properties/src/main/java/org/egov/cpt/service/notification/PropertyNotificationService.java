package org.egov.cpt.service.notification;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.web.contracts.OwnershipTransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyNotificationService {

	private PropertyConfiguration config;

	private NotificationUtil util;

	@Autowired
	public PropertyNotificationService(PropertyConfiguration config, NotificationUtil util) {
		this.config = config;
		this.util = util;
	}

	/**
	 * Creates and send the sms based on the OwnershipTransferRequest
	 * 
	 * @param request The OwnershipTransferRequest listenend on the kafka topic
	 */
	public void process(OwnershipTransferRequest request) {

		List<SMSRequest> smsRequestsProperty = new LinkedList<>();
		List<EmailRequest> emailRequest = new LinkedList<>();

		if (config.getIsSMSNotificationEnabled() != null) {
			if (config.getIsSMSNotificationEnabled()) {
				enrichSMSRequest(request, smsRequestsProperty);
				if (!CollectionUtils.isEmpty(smsRequestsProperty)) {
					util.sendSMS(smsRequestsProperty, true);
				}
			}
		}
		if (null != config.getIsEMAILNotificationEnabled()) {
			if (config.getIsEMAILNotificationEnabled()) {
				enrichEMAILRequest(request, emailRequest);
				if (!CollectionUtils.isEmpty(emailRequest))
					util.sendEMAIL(emailRequest, true);
			}
		}

	}

	private void enrichEMAILRequest(OwnershipTransferRequest request, List<EmailRequest> emailRequest) {
		String tenantId = request.getOwners().get(0).getTenantId();
		for (Owner owner : request.getOwners()) {
			Map<String, String> emailIdToApplicant = new HashMap<>();

			if (owner.getOwnerDetails().getEmail() != null)
				emailIdToApplicant.put(owner.getOwnerDetails().getEmail(), owner.getOwnerDetails().getName());

			if (emailIdToApplicant.isEmpty()) {
				continue;
			}
			String message = null;
			String localizationMessages;
			localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
			message = util.getCustomizedOTMsg(request.getRequestInfo(), owner, localizationMessages);
			if (message == null)
				continue;
			String emailSignature = util.getMessageTemplate(PTConstants.EMAIL_SIGNATURE, localizationMessages);
			message.concat(emailSignature);
			emailRequest.addAll(util.createEMAILRequest(message, emailIdToApplicant));
		}

	}

	/**
	 * Enriches the smsRequest with the customized messages
	 * 
	 * @param request     The OwnershipTransferRequest from kafka topic
	 * @param smsRequests List of SMSRequets
	 */
	private void enrichSMSRequest(OwnershipTransferRequest request, List<SMSRequest> smsRequests) {
		String tenantId = request.getOwners().get(0).getTenantId();
		for (Owner owner : request.getOwners()) {
			String message = null;
			String localizationMessages;

			localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
			message = util.getCustomizedOTMsg(request.getRequestInfo(), owner, localizationMessages);

			if (message == null)
				continue;
			message.replaceAll("<br/>", "");
			Map<String, String> mobileNumberToOwner = new HashMap<>();

			if (owner.getOwnerDetails().getPhone() != null) {
				mobileNumberToOwner.put(owner.getOwnerDetails().getPhone(), owner.getOwnerDetails().getName());
			}
			smsRequests.addAll(util.createSMSRequest(message, mobileNumberToOwner));
		}

	}

}
