package org.egov.cpt.service.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.models.web.Event;
import org.egov.cpt.models.web.EventRequest;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;


@Service
public class DemandNotificationService {

	private PropertyConfiguration config;

	private NotificationUtil util;
	
	private PropertyUtil propertyUtil;
	
	@Autowired
	public DemandNotificationService(PropertyConfiguration config, NotificationUtil util,PropertyUtil propertyUtil) {
		this.config = config;
		this.util = util;
		this.propertyUtil = propertyUtil;
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
		if(null != config.getIsUserEventsNotificationEnabledForRP()) {
			if(config.getIsUserEventsNotificationEnabledForRP()) {
				EventRequest eventRequest = getEventsForDemand(rentDemand, property,requestInfo);
				if(null != eventRequest)
					util.sendEventNotification(eventRequest);
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
			message = message.replaceAll("<br/>", "");
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

			String emailSignature = util.getMessageTemplate(PTConstants.EMAIL_SIGNATURE, localizationMessages);
			message=message.concat(emailSignature);
			emailRequest.addAll(util.createEMAILRequest(message, emailIdToApplicant));
		}

	}
	
	/**
     * Creates and registers an event at the egov-user-event service at defined trigger points as that of sms notifs.
     * @param request
     * @return
     */
    public EventRequest getEventsForDemand(RentDemand rentDemand, Property property, RequestInfo requestInfo) {
    	List<Event> events = new ArrayList<>();
        String tenantId = property.getTenantId();
        String localizationMessages = util.getLocalizationMessages(tenantId,requestInfo);
        Owner curentOwner=propertyUtil.getCurrentOwnerFromProperty(property);
			String message = null;
			message = util.getDemandGenerationMsg(rentDemand, property, localizationMessages);

			if (message == null)
				return null;

			Map<String, String> mobileNumberToOwner = new HashMap<>();

			if (curentOwner.getOwnerDetails().getPhone() != null) {
				mobileNumberToOwner.put(curentOwner.getOwnerDetails().getPhone(), curentOwner.getOwnerDetails().getName());
			}
			message = message.replaceAll("<br/>", "");
			 events = util.createEvent(message,mobileNumberToOwner,requestInfo,tenantId,PTConstants.PAYRENT,propertyUtil.getPropertyRentConsumerCode(property.getTransitNumber()));
       
        if(!CollectionUtils.isEmpty(events)) {
        	Object user = util.userDetails(requestInfo,tenantId,curentOwner.getOwnerDetails().getPhone());
        	int id= JsonPath.read(user, "$.user[0].id");
        	User userInfo = User.builder().uuid(JsonPath.read(user, "$.user[0].uuid"))
        			.mobileNumber(JsonPath.read(user, "$.user[0].mobileNumber"))
        			.id(Long.valueOf(id))
        			.tenantId(JsonPath.read(user, "$.user[0].tenantId"))
        			.roles(JsonPath.read(user, "$.user[0].roles"))
        			.build();
        	requestInfo.setUserInfo(userInfo);
    		return EventRequest.builder().requestInfo(requestInfo).events(events).build();
        }else {
        	return null;
        }
		
    }

}
