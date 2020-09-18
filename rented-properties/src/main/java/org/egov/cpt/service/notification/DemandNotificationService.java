package org.egov.cpt.service.notification;

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

	public void process(RentDemand rentDemand, Property property) {
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setMsgId("20170310130900|en_IN");

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
				enrichEMAILRequest(rentDemand, property, emailRequest, requestInfo);
				if (!CollectionUtils.isEmpty(emailRequest))
					util.sendEMAIL(emailRequest, true);
			}
		}

	}

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

	private void enrichEMAILRequest(RentDemand rentDemand, Property property, List<EmailRequest> emailRequest, RequestInfo requestInfo) {
		String tenantId = property.getOwners().get(0).getTenantId();
		for (Owner owner : property.getOwners()) {
			Map<String, String> emailIdToApplicant = new HashMap<>();

			if (owner.getOwnerDetails().getEmail() != null && owner.getActiveState())
				emailIdToApplicant.put(owner.getOwnerDetails().getEmail(), owner.getOwnerDetails().getName());

			if (emailIdToApplicant.isEmpty()) {
				continue;
			}
			String message = null;
			String localizationMessages;
			localizationMessages = util.getLocalizationMessages(tenantId, requestInfo);
			message = util.getDemandGenerationMsg(rentDemand, property, localizationMessages);
			if (message == null)
				continue;

			message = message.replace("\\n", "\n");
			emailRequest.addAll(util.createEMAILRequest(message, emailIdToApplicant));
		}

	}

}
