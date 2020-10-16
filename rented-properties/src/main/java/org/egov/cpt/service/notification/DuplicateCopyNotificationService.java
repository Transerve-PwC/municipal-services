package org.egov.cpt.service.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.EmailRequest;
import org.egov.cpt.models.SMSRequest;
import org.egov.cpt.models.web.Event;
import org.egov.cpt.models.web.EventRequest;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.web.contracts.DuplicateCopyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DuplicateCopyNotificationService {

	private PropertyConfiguration config;

	private NotificationUtil util;

	@Autowired
	public DuplicateCopyNotificationService(PropertyConfiguration config, NotificationUtil util) {
		this.config = config;
		this.util = util;
	}

	public void process(DuplicateCopyRequest request) {

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
		if(null != config.getIsEMAILNotificationEnabled()) {
			if(config.getIsEMAILNotificationEnabled()) {
				enrichEMAILRequest(request,emailRequest);
				if(!CollectionUtils.isEmpty(emailRequest))
					util.sendEMAIL(emailRequest,true);
			}
		}
		if(null != config.getIsUserEventsNotificationEnabledForRP()) {
			if(config.getIsUserEventsNotificationEnabledForRP()) {
				EventRequest eventRequest = getEventsForDC(request);
				if(null != eventRequest)
					util.sendEventNotification(eventRequest);
			}
		}

	}

	private void enrichEMAILRequest(DuplicateCopyRequest request, List<EmailRequest> emailRequest) {
		String tenantId = request.getDuplicateCopyApplications().get(0).getTenantId();
        for(DuplicateCopy duplicateCopy : request.getDuplicateCopyApplications()){
        	Map<String,String > emailIdToApplicant = new HashMap<>();
            
        	 
        	duplicateCopy.getApplicant().forEach(applicant -> {
                if(applicant.getEmail()!= null)
                	emailIdToApplicant.put(applicant.getEmail(),applicant.getName());
            });
                if (emailIdToApplicant.isEmpty()) {
            	continue;
            }
			String message = null;
			String localizationMessages;
					localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
					message = util.getCustomizedDcMsg(request.getRequestInfo(), duplicateCopy, localizationMessages);
            if(message==null) continue;

            String emailSignature = util.getMessageTemplate(PTConstants.EMAIL_SIGNATURE, localizationMessages);
			message=message.concat(emailSignature);
			emailRequest.addAll(util.createEMAILRequest(message,emailIdToApplicant));
        }
	
		
	}

	private void enrichSMSRequest(DuplicateCopyRequest request, List<SMSRequest> smsRequests) {
		String tenantId = request.getDuplicateCopyApplications().get(0).getTenantId();
		for (DuplicateCopy copy : request.getDuplicateCopyApplications()) {
			String message = null;
			String localizationMessages;

			localizationMessages = util.getLocalizationMessages(tenantId, request.getRequestInfo());
			message = util.getCustomizedDcMsg(request.getRequestInfo(), copy, localizationMessages);

			if (message == null)
				continue;
			
			message = message.replaceAll("<br/>", "");
			Map<String, String> mobileNumberToOwner = new HashMap<>();

			if (copy.getApplicant().get(0).getPhone() != null) {
				mobileNumberToOwner.put(copy.getApplicant().get(0).getPhone(), copy.getApplicant().get(0).getName());
			}
			smsRequests.addAll(util.createSMSRequest(message, mobileNumberToOwner));
		}

	}
	
	/**
     * Creates and registers an event at the egov-user-event service at defined trigger points as that of sms notifs.
     * @param request
     * @return
     */
    public EventRequest getEventsForDC(DuplicateCopyRequest request) {
    	List<Event> events = new ArrayList<>();
        String tenantId = request.getDuplicateCopyApplications().get(0).getTenantId();
        String localizationMessages = util.getLocalizationMessages(tenantId,request.getRequestInfo());
        for(DuplicateCopy application : request.getDuplicateCopyApplications()){

            String message = util.getCustomizedDcMsg(request.getRequestInfo(), application, localizationMessages);
            if(message == null) continue;
            message = message.replaceAll("<br/>", "");
            Map<String,String > mobileNumberToOwner = new HashMap<>();
            if (application.getApplicant().get(0).getPhone() != null) {
				mobileNumberToOwner.put(application.getApplicant().get(0).getPhone(), application.getApplicant().get(0).getName());
			}
            
            events = util.createEvent(message,mobileNumberToOwner,request.getRequestInfo(),tenantId,application.getState(),application.getApplicationNumber());
        }
        if(!CollectionUtils.isEmpty(events)) {
    		return EventRequest.builder().requestInfo(request.getRequestInfo()).events(events).build();
        }else {
        	return null;
        }
		
    }

}
