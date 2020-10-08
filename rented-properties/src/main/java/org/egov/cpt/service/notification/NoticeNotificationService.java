package org.egov.cpt.service.notification;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.web.contracts.NoticeGenerationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class NoticeNotificationService {

	private PropertyConfiguration config;

	private NotificationUtil util;

	@Autowired
	public NoticeNotificationService(PropertyConfiguration config, NotificationUtil util) {
		this.config = config;
		this.util = util;
	}

	public void process(NoticeGenerationRequest request, List<Property> propertiesFromDb) {

		List<SMSRequest> smsRequestsProperty = new LinkedList<>();
		List<EmailRequest> emailRequest = new LinkedList<>();

		if (config.getIsSMSNotificationEnabled() != null) {
			if (config.getIsSMSNotificationEnabled()) {
				enrichSMSRequest(request, propertiesFromDb, smsRequestsProperty);
				if (!CollectionUtils.isEmpty(smsRequestsProperty)) {
					util.sendSMS(smsRequestsProperty, true);
				}
			}
		}
		if (null != config.getIsEMAILNotificationEnabled()) {
			if (config.getIsEMAILNotificationEnabled()) {
				enrichEMAILRequest(request, propertiesFromDb, emailRequest);
				if (!CollectionUtils.isEmpty(emailRequest))
					util.sendEMAIL(emailRequest, true);
			}
		}

	}

	private void enrichEMAILRequest(NoticeGenerationRequest request, List<Property> propertiesFromDb,
			List<EmailRequest> emailRequest) {
		String tenantId = request.getNoticeApplications().get(0).getTenantId();
		for (NoticeGeneration notice : request.getNoticeApplications()) {
			Map<String, String> emailIdToApplicant = new HashMap<>();

			/*
			 * for(Property property:propertiesFromDb){
			 * List<Owner>owners=property.getOwners(); for(Owner owner:owners){
			 * if(owner.getOwnerDetails().getName().equals(property.getPropertyDetails().
			 * getCurrentOwner())){
			 * 
			 * } } }
			 */

			Owner ownerDtl = propertiesFromDb.get(0).getOwners().stream().filter(owner -> {
				return owner.getId().equalsIgnoreCase(propertiesFromDb.get(0).getPropertyDetails().getCurrentOwner());
			}).findFirst().get();

			if (ownerDtl.getOwnerDetails().getEmail() != null) {
				emailIdToApplicant.put(ownerDtl.getOwnerDetails().getEmail(), ownerDtl.getOwnerDetails().getName());
			}

			if (emailIdToApplicant.isEmpty()) {
				continue;
			}
			String message = null;
			String localizationMessages;
			localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
			message = util.getCustomizedNoticeMsg(request.getRequestInfo(), notice, ownerDtl, localizationMessages);
			if (message == null)
				continue;

			String emailSignature = util.getMessageTemplate(PTConstants.EMAIL_SIGNATURE, localizationMessages);
			message=message.concat(emailSignature);
			emailRequest.addAll(util.createEMAILRequest(message, emailIdToApplicant));
		}

	}

	private void enrichSMSRequest(NoticeGenerationRequest request, List<Property> propertiesFromDb,
			List<SMSRequest> smsRequests) {
		String tenantId = request.getNoticeApplications().get(0).getTenantId();
		for (NoticeGeneration notice : request.getNoticeApplications()) {
			String message = null;
			String localizationMessages;

			Owner ownerDtl = propertiesFromDb.get(0).getOwners().stream().filter(owner -> {
				return owner.getId().equalsIgnoreCase(propertiesFromDb.get(0).getPropertyDetails().getCurrentOwner());
			}).findFirst().get();

			localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
			message = util.getCustomizedNoticeMsg(request.getRequestInfo(), notice, ownerDtl, localizationMessages);

			if (message == null)
				continue;
			
			message = message.replaceAll("<br/>", "");
			Map<String, String> mobileNumberToOwner = new HashMap<>();

			if (ownerDtl.getOwnerDetails().getPhone() != null) {
				mobileNumberToOwner.put(ownerDtl.getOwnerDetails().getPhone(), ownerDtl.getOwnerDetails().getName());
			}
			smsRequests.addAll(util.createSMSRequest(message, mobileNumberToOwner));
		}

	}

}
