package org.egov.cpt.service.notification;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.web.contracts.OwnershipTransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DemandNotificationService {

	private PropertyConfiguration config;

	private NotificationUtil util;

	@Autowired
	public DemandNotificationService(PropertyConfiguration config, NotificationUtil util) {
		this.config = config;
		this.util = util;
	}

	/**
	 * Creates and send the sms based on the OwnershipTransferRequest
	 * 
	 * @param request The OwnershipTransferRequest listenend on the kafka topic
	 */
	public void process(RentDemand rentDemand, Property property, RequestInfo requestInfo) {

		List<SMSRequest> smsRequestsProperty = new LinkedList<>();
		List<EmailRequest> emailRequest = new LinkedList<>();

		if (config.getIsSMSNotificationEnabled() != null) {
			if (config.getIsSMSNotificationEnabled()) {
				enrichSMSRequest(rentDemand, property, smsRequestsProperty, requestInfo);
				if (!CollectionUtils.isEmpty(smsRequestsProperty)) {
					util.sendSMS(smsRequestsProperty, true);
				}
			}
		}
		if (null != config.getIsEMAILNotificationEnabled()) {
			if (config.getIsEMAILNotificationEnabled()) {
				//enrichEMAILRequest(rentDemand, emailRequest);
				if (!CollectionUtils.isEmpty(emailRequest))
					util.sendEMAIL(emailRequest, true);
			}
		}

	}

	/**
	 * Enriches the smsRequest with the customized messages
	 * 
	 * @param request     The OwnershipTransferRequest from kafka topic
	 * @param smsRequests List of SMSRequets
	 */
	
	private void enrichSMSRequest(RentDemand rentDemand, Property property, List<SMSRequest> smsRequests, RequestInfo requestInfo) {
		String tenantId = property.getOwners().get(0).getTenantId();
		for (Owner owner : property.getOwners()) {
			String message = null;
			String localizationMessages;

			localizationMessages = util.getLocalizationMessages(tenantId, requestInfo);
			message = util.getDemandGenerationMsg(rentDemand, property, localizationMessages);

			if (message == null)
				continue;

			Map<String, String> mobileNumberToOwner = new HashMap<>();

			if (owner.getOwnerDetails().getPhone() != null && owner.getActiveState()) {
				mobileNumberToOwner.put(owner.getOwnerDetails().getPhone(), owner.getOwnerDetails().getName());
			}
			smsRequests.addAll(util.createSMSRequest(message, mobileNumberToOwner));
		}

	}
	 
}
