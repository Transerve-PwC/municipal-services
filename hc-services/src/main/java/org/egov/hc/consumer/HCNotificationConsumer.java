package org.egov.hc.consumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;

import org.egov.hc.contract.EmailRequest;
import org.egov.hc.contract.Event;
import org.egov.hc.contract.EventRequest;
import org.egov.hc.contract.Recepient;
import org.egov.hc.contract.RequestInfoWrapper;
import org.egov.hc.contract.SMSRequest;
import org.egov.hc.contract.ServiceRequest;

import org.egov.hc.model.ActionInfo;
import org.egov.hc.model.ServiceRequestData;
import org.egov.hc.model.Source;
import org.egov.hc.producer.HCConfiguration;
import org.egov.hc.producer.HCProducer;
import org.egov.hc.service.NotificationService;
import org.egov.hc.service.ServiceRequestService;
import org.egov.hc.utils.HCConstants;

import org.egov.hc.utils.WorkFlowConfigs;
import org.egov.hc.workflow.WorkflowIntegrator;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class HCNotificationConsumer {
	

	@Autowired
	private HCProducer hCProducer;
	
	@Autowired
	private HCConfiguration hcConfiguration;  	

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private WorkflowIntegrator workflowIntegrator;
	

	@Autowired
	private RestTemplate rest;

	@KafkaListener(topics = { "${kafka.topics.save.service}", "${kafka.topics.update.service}" })

	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws JSONException {
		ObjectMapper mapper = new ObjectMapper();
		ServiceRequest serviceReqRequest = new ServiceRequest();
		try {
			serviceReqRequest = mapper.convertValue(record, ServiceRequest.class);

		} catch (final Exception e) {
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
		process(serviceReqRequest);
	}

	/**
	 * Sends notifications on different topics for the consumer to pick.
	 *
	 * @param serviceReqRequest
	 * @throws JSONException 
	 */
	public void process(ServiceRequest serviceReqRequest) throws JSONException {

		// get all the messages from localization table
		Map<String, String> messageMap = getLocalizationMessage(serviceReqRequest.getRequestInfo().getUserInfo().getTenantId(),
				serviceReqRequest.getRequestInfo());
		log.info("Get localization Message Succesfully");

		if (!CollectionUtils.isEmpty(serviceReqRequest.getActionInfo())) {

			sendNotification(serviceReqRequest, messageMap, null);
			

			// check citizen raised a request, then get Executive engg role user list from
			
			String msgId = serviceReqRequest.getRequestInfo().getMsgId().split("[|]")[0];
			serviceReqRequest.getRequestInfo().setMsgId(msgId+"|"+hcConfiguration.getFallbackLocale());
			
			 messageMap = getLocalizationMessage(serviceReqRequest.getServices().get(0).getTenantId(),
					serviceReqRequest.getRequestInfo());
			
			if (serviceReqRequest.getActionInfo().get(0).getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
				//String role = HCConstants.EXECUTIVE_ENGINEER;
				
				String role = workflowIntegrator.parseBussinessServiceData(workflowIntegrator.getbussinessServiceDatafromprocesinstanceEdit(serviceReqRequest),serviceReqRequest);

				getRolewiseUserList(serviceReqRequest, role, messageMap);

			}

		}

	}

	public void getRolewiseUserList(ServiceRequest serviceReqRequest, String role, Map<String, String> messageMap) {

		
		String mobileNumber = null;
		String uuid = null;
	
		String userName = null;

		String allRoles = null;
		String city = serviceReqRequest.getServices().get(0).getCity();

		log.info("Get Employee data using  : "+ role + " role" );

		try {

			allRoles = rest.postForObject(hcConfiguration.getEgovHRMShost().concat(hcConfiguration.getEgovHRMSSearchEndpoint()).concat("?")
					.concat("roles=" + role + "&tenantId=" + serviceReqRequest.getServices().get(0).getCity()), serviceReqRequest, String.class);

			try {
				org.json.JSONObject obj = new org.json.JSONObject(allRoles);

				log.info(" All "+role+ " details are " + obj);

				org.json.JSONArray employeesList = obj.getJSONArray("Employees");

				
				ServiceRequestData serviceRequest = new ServiceRequestData();

				for (int i = 0; i < employeesList.length(); i++) {

					log.info("Feaching all Employee details ");
					
					
					RequestInfoWrapper infoWrapper = null;
					org.json.JSONObject empDetails = new org.json.JSONObject(employeesList.get(i).toString());

					org.json.JSONObject user = empDetails.getJSONObject("user");
					
					if (!user.isNull("mobileNumber"))
						mobileNumber = user.getString("mobileNumber");
					else
						mobileNumber = user.getString("");
					uuid = user.getString("uuid");
					
					userName = user.getString("userName");
					
					if (!user.isNull("emailId"))
						serviceRequest.setEmail(user.getString("emailId").toString());
					else
						serviceRequest.setEmail("");

					serviceRequest.setOwnerName(userName);
					serviceRequest.setCity(city);
					serviceRequest.setContactNumber(mobileNumber);
					serviceRequest.setService_request_uuid(uuid);
					serviceRequest.setService_request_id(serviceReqRequest.getServices().get(0).getService_request_id());
					serviceRequest.setIsRoleSpecific(true);
					String by = serviceRequest.getCreatedBy();
					
					List Actioninfolist = new ArrayList();
					
					ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
							.businessKey(serviceReqRequest.getServices().get(0).getBusinessService())
							.action(serviceReqRequest.getServices().get(0).getAction())
							.assignee(uuid).by(by)
							.when(serviceRequest.getCreatedTime())
							.tenantId(serviceReqRequest.getServices().get(0).getTenantId())
							.status(serviceReqRequest.getServices().get(0).getAction())
							.build();
					Actioninfolist.add(newActionInfo);

					List<ServiceRequestData> serviceRequestList = new ArrayList<>();
					serviceRequestList.add(serviceRequest);
					RequestInfo requestInfoset = serviceReqRequest.getRequestInfo();
					requestInfoset.getUserInfo().setUuid(uuid);
					requestInfoset.getUserInfo().setName(userName);

					infoWrapper = RequestInfoWrapper.builder().services(serviceRequestList).actionInfo(Actioninfolist)
							.requestInfo(requestInfoset).requestBody(serviceRequest).build();

					ServiceRequest serviceRequestobj = new ServiceRequest();
					serviceRequestobj.setServices(serviceRequestList);
					serviceRequestobj.setActionInfo(Actioninfolist);
					serviceRequestobj.setRequestInfo(requestInfoset);
					
					log.info(" Sending notification to "+ userName);
					
					sendNotification(serviceRequestobj, messageMap, null);

				}

			}

			catch (Exception ex) {

			}

		} catch (HttpClientErrorException e) {
			System.out.print("Handled exception");
		}
		System.out.println(allRoles);

	}

	public void sendNotification(ServiceRequest serviceReqRequest, Map<String, String> messageMap, 
			String service_request_id_new) {

		for (ActionInfo actionInfo : serviceReqRequest.getActionInfo()) {
			if (null != actionInfo && (!StringUtils.isEmpty(actionInfo.getStatus()))) {
				ServiceRequestData service = serviceReqRequest.getServices().get(0);
				
				//call to mdms and get servicetype
				
				String mdmsServiceTypeName = getMdmsServiceTypeName(service);
				
				service.setServiceType(mdmsServiceTypeName); 
				 
				if (isNotificationEnabled(actionInfo.getStatus())) {
					if (hcConfiguration.getIsSMSNotificationEnabled()) {
						SMSRequest smsRequest = prepareSMSRequest(service, actionInfo,
								serviceReqRequest.getRequestInfo(), messageMap, service_request_id_new);
						if(null != smsRequest)
							hCProducer.push(hcConfiguration.getSmsNotifTopic(), smsRequest);
						else
							log.info("SMS notification is empty in this case");
					}

					if (hcConfiguration.getIsEmailNotificationEnabled() && (null != service.getEmail() && !service.getEmail().isEmpty())) {
						EmailRequest emailRequests = prepareEmailRequest(service, actionInfo, serviceReqRequest.getRequestInfo(),messageMap,service_request_id_new);
						if(null != emailRequests)
						{   log.info("Sending Email");
							hCProducer.push(hcConfiguration.getEmailNotifTopic(), emailRequests);
						}
						else
							log.info("email body is empty in this case");
					}

					if (hcConfiguration.getIsUsrEventNotificationEnabled()) {
						EventRequest request = preparePushNotificationRequest(service, actionInfo,
								serviceReqRequest.getRequestInfo(), messageMap, service_request_id_new);
						if(null != request)
							{
							log.info(" Sending user event");
							hCProducer.push(hcConfiguration.getSaveUserEventsTopic(), request);
							}
						else
							log.info("PUSH body is empty in this case");
					}

				} else {
					log.info("Notification disabled for this case!");
					continue;
				}
			} else {
				continue;
			}
		}

	}

	private String getMdmsServiceTypeName(ServiceRequestData service) {
		
		String servicetype = null;
		try
		{
			String payloadData ="{\"MdmsCriteria\": {\"tenantId\": \"ch\",\"moduleDetails\": [{\"moduleName\": \"eg-horticulture\",\"masterDetails\": [{\"name\":\"ServiceType\"}]}]}}" ;

			String mdmsData = sendPostRequest(
					hcConfiguration.getMdmsHost().concat(hcConfiguration.getMdmsEndpoint()).concat("?").concat("&tenantId=" + service.getTenantId()),
					payloadData);

			org.json.JSONObject mdmsDataobj = new org.json.JSONObject(mdmsData);
			org.json.JSONObject MdmsResObj = mdmsDataobj.getJSONObject("MdmsRes");
			org.json.JSONObject eghorticultureObj = MdmsResObj.getJSONObject("eg-horticulture");
		
			org.json.JSONArray mdmsServiceTypes = eghorticultureObj.getJSONArray("ServiceType");
			
			for(int cnt =0 ; cnt <= mdmsServiceTypes.length() ; cnt ++)
			{
				org.json.JSONObject mdmsServiceTypesObj = new org.json.JSONObject(
						mdmsServiceTypes.get(cnt).toString());
				
				String servicecode = mdmsServiceTypesObj.getString("code");
				
				if(servicecode.equals(service.getServiceType()))
				{
					servicetype = mdmsServiceTypesObj.getString("name");
					break;
				}
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		return servicetype;
	}

	/**
	 * Prepares event to be registered in user-event service, 
	 *
	 * @param serviceReq
	 * @param actionInfo
	 * @param messageMap
	 * @param service_request_id_new
	 * @param requestInf
	 * @return
	 * @messageMap
	 */

	public EventRequest preparePushNotificationRequest(ServiceRequestData serviceReq, ActionInfo actionInfo,
			RequestInfo requestInfo, Map<String, String> messageMap, String service_request_id_new) {

		String employeeNameRetrived = null;
		String message = null;
		List<Event> events = new ArrayList<>();

		if (StringUtils.isEmpty(actionInfo.getAssignee())
				&& actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {

			message = getMessage(WorkFlowConfigs.ACTION_OPEN, serviceReq, requestInfo, messageMap, null,
					HCConstants.PUSH, service_request_id_new);
			
		} else if (actionInfo.getAction().equals(WorkFlowConfigs.APPROVE)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_ASSIGN)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_INITIATE)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_REQUEST_FOR_REASSIGN)
				||actionInfo.getAction().equals(WorkFlowConfigs.VERIFY_AND_FORWARD_TO_SDO)
				||actionInfo.getAction().equals(WorkFlowConfigs.FORWARDFORINSPECTION)
				||actionInfo.getAction().equals(WorkFlowConfigs.REQUESTCLARIFICATION)
				||actionInfo.getAction().equals(WorkFlowConfigs.INSPECT)
				||actionInfo.getAction().equals(WorkFlowConfigs.VERIFYANDFORWARD)) {

			employeeNameRetrived = serviceReq.getOwnerName();
			message = getMessage(actionInfo.getAction(), serviceReq, requestInfo, messageMap, employeeNameRetrived,
					HCConstants.PUSH, service_request_id_new);
	

		} else if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_INITIATE)) {
			message = getMessage(WorkFlowConfigs.ACTION_INITIATE, serviceReq, requestInfo, messageMap, null,
					HCConstants.PUSH, service_request_id_new);

		} else

		{
			message = getMessage(actionInfo.getAction(), serviceReq, requestInfo, messageMap, null, HCConstants.PUSH,
					service_request_id_new);
		}

		log.info("Bell icon message  : "+ message);
		List<String> toUsers = new ArrayList<>();
		toUsers.add(requestInfo.getUserInfo().getUuid());

		List<String> toRole = new ArrayList<>();
		toRole.add("All");
		Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(toRole).build();
		
		if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)
			||actionInfo.getAction().equals(WorkFlowConfigs.ACTION_REJECT)
			||actionInfo.getAction().equals(WorkFlowConfigs.ACTION_COMPLETE)
			||actionInfo.getAction().equals(WorkFlowConfigs.ACTION_UPDATE)) {
			Event event = Event.builder()
					
					.tenantId(requestInfo.getUserInfo().getTenantId())
					.description(message).eventType(HCConstants.USREVENTS_EVENT_TYPE)
					.name(HCConstants.USREVENTS_EVENT_NAME).postedBy(HCConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).referenceId(serviceReq.getService_request_id())

					.eventDetails(null).build();
			events.add(event);

		}
		else
		{
			Event event = Event.builder()
					
					.tenantId(serviceReq.getCity())
					.description(message).eventType(HCConstants.USREVENTS_EVENT_TYPE)
					.name(HCConstants.USREVENTS_EVENT_NAME).postedBy(HCConstants.USREVENTS_EVENT_POSTEDBY)
					.source(Source.WEBAPP).recepient(recepient).referenceId(null)

					.eventDetails(null).build();
			events.add(event);
		}


		log.info("generated user event is : " + events);
		return EventRequest.builder().requestInfo(requestInfo).events(events).build();

	}

	public EmailRequest prepareEmailRequest(ServiceRequestData serviceReq, ActionInfo actionInfo,
			RequestInfo requestInfo, Map<String, String> messageMap, String service_request_id_new) {

		String emailIdRetrived = null;
		String employeeNameRetrived = null;
		String message = null;
		String subject = null;

		
		if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
			emailIdRetrived = serviceReq.getEmail();
			message = getMessage(WorkFlowConfigs.ACTION_OPEN, serviceReq, requestInfo, messageMap, null,
					HCConstants.EMAIL, service_request_id_new);
			subject = getSubject(actionInfo.getAction(), serviceReq, requestInfo, messageMap, null, HCConstants.EMAIL,
					service_request_id_new);
			
		} else if (actionInfo.getAction().equals(WorkFlowConfigs.APPROVE)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_ASSIGN)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_INITIATE)
				|| actionInfo.getAction().equals(WorkFlowConfigs.VERIFY_AND_FORWARD_TO_SDO)
				|| actionInfo.getAction().equals(WorkFlowConfigs.FORWARDFORINSPECTION)
				|| actionInfo.getAction().equals(WorkFlowConfigs.REQUESTCLARIFICATION)
				||actionInfo.getAction().equals(WorkFlowConfigs.INSPECT)
				|| actionInfo.getAction().equals(WorkFlowConfigs.VERIFYANDFORWARD)) {

			emailIdRetrived = serviceReq.getEmail();
			employeeNameRetrived=serviceReq.getOwnerName();
			message = getMessage(actionInfo.getAction(), serviceReq, requestInfo, messageMap, employeeNameRetrived,
					HCConstants.EMAIL, service_request_id_new);
			subject = getSubject(actionInfo.getAction(), serviceReq, requestInfo, messageMap, employeeNameRetrived, HCConstants.EMAIL,
					service_request_id_new);

		} 


		else

		{
			emailIdRetrived = serviceReq.getEmail();
			message = getMessage(actionInfo.getAction(), serviceReq, requestInfo, messageMap, null, HCConstants.EMAIL,
					service_request_id_new);
			subject = getSubject(actionInfo.getAction(), serviceReq, requestInfo, messageMap, null, HCConstants.EMAIL,
					service_request_id_new);
		}

		if (emailIdRetrived == null || message == null) {
			log.info("Sending Email message is eampty");
			return null;
			}
		
		log.info("get massage from localization and Email Id from userInfo");
		
		log.info("Sending the Email : Email Id : " +emailIdRetrived + "And  Massage :" + message + "And  subject :" + subject );
		return EmailRequest.builder().email(emailIdRetrived).subject(subject).body(message).isHTML(true).build();
	}

	private String getSubject(String action, ServiceRequestData serviceReq, RequestInfo requestInfo,
			Map<String, String> messageMap, String employeeName, String email, String service_request_id_new) {

		String text = null;
		
		switch (action) {

		case WorkFlowConfigs.ACTION_OPEN:
			text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_EMAIL_SUBJECT_ACTION_OPEN);
			break;
			
		case WorkFlowConfigs.APPROVE:
			text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_ASSIGN_EMAIL_SUBJECT);
			break;
			
		case WorkFlowConfigs.ACTION_ASSIGN:
			text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_EMAIL_SUBJECT_ACTION_ASSIGN);
			break;
			
		case WorkFlowConfigs.VERIFY_AND_FORWARD_TO_SDO:
			text = messageMap.get(HCConstants.HC_EMPLOYEE_FORWARD_TO_SDO_EMAIL_SUBJECT);
			break;
			
		case WorkFlowConfigs.FORWARDFORINSPECTION:
			text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECTION_EMAIL_SUBJECT);
			break;
			
		case WorkFlowConfigs.REQUESTCLARIFICATION:
			text = messageMap.get(HCConstants.HC_EMPLOYEE_CLARIFICATION_EMAIL_SUBJECT);
			break;
			
		case WorkFlowConfigs.VERIFYANDFORWARD:
			text = messageMap.get(HCConstants.HC_EMPLOYEE_FORWARDREQUEST_EMAIL_SUBJECT);
			break;
			
		case WorkFlowConfigs.ACTION_INITIATE:
			text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_EMAIL_SUBJECT_ACTION_ASSIGN);
			break;
			
		case WorkFlowConfigs.INSPECT:
			text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECT_EMAIL_SUBJECT_NOTIFICATION);
			break;
			
		case WorkFlowConfigs.ACTION_UPDATE:
			text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_EMAIL_NOTIFICATION_SUBJECT);
			break;
			
		case WorkFlowConfigs.ACTION_REJECT:
			text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_EMAIL_SUBJECT_ACTION_REJECT);
			break;
			
		case WorkFlowConfigs.ACTION_COMPLETE:

			text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_EMAIL_SUBJECT_ACTION_COMPLETE);
			break;

		
		}
		if(text != null)
		{
		text = text.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id());
		}
		return text;
	}

	/**
	 * Prepares event to be registered in user-event service.
	 *
	 * @param serviceReq
	 * @param actionInfo
	 * @param requestInfo
	 * @param messageMap
	 * @param service_request_id_new
	 * @return
	 */

	public SMSRequest prepareSMSRequest(ServiceRequestData serviceReq, ActionInfo actionInfo, RequestInfo requestInfo,
			Map<String, String> messageMap, String service_request_id_new) {

		String phoneNumberRetrived = null;
		String employeeDetailsRetrived = null;
		String employeeNameRetrived = null;
		String message = null;
		String data = null;
		// List<SMSRequest> smsRequestsTobeSent = new ArrayList<>();
		if (actionInfo.getAction().equals(WorkFlowConfigs.ACTION_OPEN)) {
			phoneNumberRetrived = serviceReq.getContactNumber();
			message = getMessage(WorkFlowConfigs.ACTION_OPEN, serviceReq, requestInfo, messageMap, null,
					HCConstants.SMS, service_request_id_new);

		} 
		
		else if (actionInfo.getAction().equals(WorkFlowConfigs.APPROVE)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_ASSIGN)
				||actionInfo.getAction().equals(WorkFlowConfigs.VERIFY_AND_FORWARD_TO_SDO)
				||actionInfo.getAction().equals(WorkFlowConfigs.FORWARDFORINSPECTION)
				||actionInfo.getAction().equals(WorkFlowConfigs.REQUESTCLARIFICATION)
				|| actionInfo.getAction().equals(WorkFlowConfigs.ACTION_INITIATE)
				||actionInfo.getAction().equals(WorkFlowConfigs.INSPECT)
				|| actionInfo.getAction().equals(WorkFlowConfigs.VERIFYANDFORWARD)) {
			
			
			if (!serviceReq.getIsRoleSpecific())
			{
				employeeDetailsRetrived = notificationService.getMobileAndIdForNotificationService(requestInfo,
						serviceReq.getCity(), actionInfo.getAssignee(), HCConstants.ROLE_EMPLOYEE);
				data = employeeDetailsRetrived.replace("|", "#");
				phoneNumberRetrived = data.split("#")[0];
				employeeNameRetrived = data.split("#")[1];
				serviceReq.setOwnerName(employeeNameRetrived);
				serviceReq.setEmail( data.split("#")[2]);

		
			}
			else
			{
				phoneNumberRetrived = serviceReq.getContactNumber();
				employeeNameRetrived = serviceReq.getOwnerName();
			
				
			}
			message = getMessage(actionInfo.getAction(), serviceReq, requestInfo, messageMap, employeeNameRetrived,
					HCConstants.SMS, service_request_id_new);
			
			
			

		} 
		else 
		{
			phoneNumberRetrived = serviceReq.getContactNumber();
			message = getMessage(actionInfo.getAction(), serviceReq, requestInfo, messageMap, null, HCConstants.SMS,
					service_request_id_new);
		}
		if (phoneNumberRetrived == null || message == null)
			{
			log.info("Sending sms message is empty or phonenumber not found");
			return null;
			}
			
		log.info("get massage from localization and contact Number from userInfo");
		
		log.info("Sending the sms : Contact number : " +phoneNumberRetrived + "And  Massage :" + message );
		return SMSRequest.builder().mobileNumber(phoneNumberRetrived).message(message).build();
	}

	public Map<String, String> getLocalizationMessage(String tenantId, RequestInfo requestInfo) {
		
		String locale = null;
		String tenant_id = null;
		try {
			locale = requestInfo.getMsgId().split("[|]")[1]; 
															
			if (StringUtils.isEmpty(locale))
				locale = hcConfiguration.getFallbackLocale();
		} catch (Exception e) {
			locale = hcConfiguration.getFallbackLocale();
		}

		try {
			tenant_id = tenantId.split("[.]")[0]; 
													
		} catch (Exception e) {
			locale = hcConfiguration.getFallbackLocale();
		}

		if (null == NotificationService.localizedMessageMap.get(locale + "|" + tenant_id)) 
																																														
			notificationService.getLocalisedMessages(requestInfo, tenant_id, locale,
					HCConstants.LOCALIZATION_MODULE_NAME);
		Map<String, String> messageMap = NotificationService.localizedMessageMap.get(locale + "|" + tenant_id);
		if (null == messageMap)
			return null;
		return messageMap;
	}

	public String getMessage(String action, ServiceRequestData serviceReq, RequestInfo requestInfo,
			Map<String, String> messageMap, String employeeName, String notifcationType,
			String service_request_id_new) {
		String text = null;
		DateFormat df = new SimpleDateFormat(hcConfiguration.getNotificationDateFormat());
		Date dateobj = new Date();
		String genratedDate = df.format(dateobj);

		switch (action) {
		
		case WorkFlowConfigs.ACTION_OPEN:
			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_SUBMITREQUEST_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_CITIZEN_SUBMITREQUEST_EMAIL_NOTIFICATION); 
																						
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_SUBMITREQUEST_PUSH_NOTIFICATION);
			}

			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_TYPE, serviceReq.getServiceType())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.APPROVE:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_APPROVE_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_APPROVE_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_APPROVE_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeName)
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;


		case WorkFlowConfigs.ACTION_ASSIGN:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_ASSIGN_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_ASSIGN_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_ACTION_ASSIGN_PUSH_NOTIFICATION);
			}
			
			if(text != null) {
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employeeName)
					.replace(HCConstants.SMS_NOTIFICATION_USER_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;

		case WorkFlowConfigs.VERIFY_AND_FORWARD_TO_SDO:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_FORWARD_TO_SDO_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_FORWARD_TO_SDO_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_FORWARD_TO_SDO_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
		case WorkFlowConfigs.INSPECT:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECT_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECT_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECT_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.FORWARDFORINSPECTION:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECTION_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECTION_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_INSPECTION_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.VERIFYANDFORWARD:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_REQUEST_FORWARD_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_REQUEST_FORWARD_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_REQUEST_FORWARD_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.REQUESTCLARIFICATION:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_CLARIFICATION_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_CLARIFICATION_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_CLARIFICATION_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
			
			
		case WorkFlowConfigs.ACTION_INITIATE:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_REQUEST_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_EMPLOYEE_REQUEST_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_EMPLOYEE_REQUEST_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;

		case WorkFlowConfigs.ACTION_REJECT:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REJECTSERVICEREQUEST_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_CITIZEN_REJECTSERVICEREQUEST_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REJECTSERVICEREQUEST_PUSH_NOTIFICATION);
			}

			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_TYPE, serviceReq.getServiceType())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
		case WorkFlowConfigs.ACTION_COMPLETE:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_SERVICEREQUESTCOMPLETED_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_CITIZEN_SERVICEREQUESTCOMPLETED_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_SERVICEREQUESTCOMPLETED_PUSH_NOTIFICATION);
			}
			
			if(text != null)
			{
			text = text.replaceAll(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, serviceReq.getOwnerName())
					.replaceAll(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
					.replaceAll(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_TYPE, serviceReq.getServiceType())
					.replaceAll(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.ACTION_UPDATE:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY,  serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id_old())
					.replace(HCConstants.NOTIFICATION_SERVICEREQUEST_ID_NEW, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.EDIT:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY,  serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id_old())
					.replace(HCConstants.NOTIFICATION_SERVICEREQUEST_ID_NEW, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
			
		case WorkFlowConfigs.ACTION_INITIATED:

			if (notifcationType.equals(HCConstants.SMS)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_SMS_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.EMAIL)) {

				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_EMAIL_NOTIFICATION);
			} else if (notifcationType.equals(HCConstants.PUSH)) {
				text = messageMap.get(HCConstants.HC_CITIZEN_REQUEST_FOR_UPDATE_PUSH_NOTIFICATION);
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY,  serviceReq.getOwnerName())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id_old())
					.replace(HCConstants.NOTIFICATION_SERVICEREQUEST_ID_NEW, serviceReq.getService_request_id())
					.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, genratedDate);
			}
			break;
		}
		

		return text;
	}


	public boolean isNotificationEnabled(String status) {
		boolean isNotifEnabled = false;
		List<String> notificationEnabledStatusList = Arrays.asList(hcConfiguration.getNotificationEnabledStatuses().split(","));
		if (notificationEnabledStatusList.contains(status.toUpperCase())) {
			isNotifEnabled = true;
		}
		return isNotifEnabled;
	}

	public void sendNotificationToEmployee(ServiceRequest serviceReqRequest, String role, Map<String, String> messageMap,String assigneeUuid) {
		
		String mobileNumber = null;
		String uuid = null;
		
		String userName = null;

		String allRoles = null;

		try {

			allRoles = rest.postForObject(hcConfiguration.getEgovHRMShost().concat(hcConfiguration.getEgovHRMSSearchEndpoint()).concat("?")
					.concat("roles=" + role + "&tenantId=" + serviceReqRequest.getServices().get(0).getCity()), serviceReqRequest, String.class);

			try {
				org.json.JSONObject obj = new org.json.JSONObject(allRoles);

				org.json.JSONArray employeesList = obj.getJSONArray("Employees");

				ServiceRequestData serviceRequest = new ServiceRequestData();

				for (int i = 0; i < employeesList.length(); i++) {

					List Actioninfolist = new ArrayList();
					RequestInfoWrapper infoWrapper = null;
					org.json.JSONObject empDetails = new org.json.JSONObject(employeesList.get(i).toString());

					org.json.JSONObject user = empDetails.getJSONObject("user");

					if (user.getString("uuid").equals(assigneeUuid))
					
					{
						if (!user.isNull("mobileNumber"))
							mobileNumber = user.getString("mobileNumber");
						else
							mobileNumber = user.getString("");
						uuid = user.getString("uuid");
						
						userName = user.getString("userName");
						
						if (!user.isNull("emailId"))
							serviceRequest.setEmail(user.getString("emailId").toString());
						else
							serviceRequest.setEmail("");
	
						serviceRequest.setOwnerName(userName);
						//serviceRequest.setEmail(emailId);
						serviceRequest.setContactNumber(mobileNumber);
						serviceRequest.setService_request_uuid(uuid);
						serviceRequest.setService_request_id(serviceReqRequest.getServices().get(0).getService_request_id());
						String by = serviceRequest.getCreatedBy();
	
						ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
								.businessKey(serviceReqRequest.getServices().get(0).getBusinessService())
								.action(HCConstants.VERIFYANDFORWARD).assignee(uuid).by(by)
								.when(serviceRequest.getCreatedTime())
								.tenantId(serviceReqRequest.getServices().get(0).getTenantId()).status("VERIFYANDFORWARD")
								.build();
						Actioninfolist.add(newActionInfo);
	
						List<ServiceRequestData> serviceRequestList = new ArrayList<>();
						serviceRequestList.add(serviceRequest);
						RequestInfo requestInfoset = serviceReqRequest.getRequestInfo();
						requestInfoset.getUserInfo().setUuid(uuid);
						requestInfoset.getUserInfo().setName(userName);
	
						infoWrapper = RequestInfoWrapper.builder().services(serviceRequestList).actionInfo(Actioninfolist)
								.requestInfo(requestInfoset).requestBody(serviceRequest).build();
	
						ServiceRequest serviceRequestobj = new ServiceRequest();
						serviceRequestobj.setServices(serviceRequestList);
						serviceRequestobj.setActionInfo(Actioninfolist);
						serviceRequestobj.setRequestInfo(requestInfoset);
						sendNotification(serviceRequestobj, messageMap, null);
					}
				}

			}

			catch (Exception ex) {

			}

		} catch (HttpClientErrorException e) {
			System.out.print("Handled exception");
		}
		System.out.println(allRoles);
		
	}
	
	public void sendSchedulerNotification(ServiceRequest serviceReqRequest,String action,String serviceRequestDate,String tenantId,int days)
    {
		//geting massage from localization
		
		Map<String, String> messageMap = getLocalizationMessage(tenantId, serviceReqRequest.getRequestInfo());
		
		log.info("Get data from localization");
		
    	 for (ActionInfo actionInfo : serviceReqRequest.getActionInfo()) {
             if (null != actionInfo && (!StringUtils.isEmpty(actionInfo.getStatus()))) {
                 ServiceRequestData service = serviceReqRequest.getServices().get(0);      
                 
                 	//call to mdms and get servicetype
 				
 					String mdmsServiceTypeName = getMdmsServiceTypeName(service);
 				
 					service.setServiceType(mdmsServiceTypeName); 
             
                	{ 	
                     if (hcConfiguration.getIsSMSNotificationEnabled()) {
                     	SMSRequest smsRequest = prepareSMSRequest(service, actionInfo, serviceReqRequest.getRequestInfo(),messageMap,action,serviceRequestDate,days);
                     	
                     	log.info(" Sending sms : " + smsRequest);
                     	hCProducer.push(hcConfiguration.getSmsNotifTopic(), smsRequest);
 
                     }
                     
                     
                     if (hcConfiguration.getIsEmailNotificationEnabled()
                             && (null != service.getEmail() && !service.getEmail().isEmpty())) {
                     	
                     	EmailRequest emailRequests = prepareEmailRequest(service, actionInfo, serviceReqRequest.getRequestInfo(),messageMap,action,serviceRequestDate,days);
                     	log.info(" Sending mail : "+ emailRequests);
                     	hCProducer.push(hcConfiguration.getEmailNotifTopic(), emailRequests);
                   

                     }
//                     
//
                     if (hcConfiguration.getIsUsrEventNotificationEnabled()) {
                         EventRequest request = preparePushNotificationRequest(service, actionInfo, serviceReqRequest.getRequestInfo(),messageMap,action,serviceRequestDate,tenantId,days);
                         log.info("Sending event notification : "+ request);
                         hCProducer.push(hcConfiguration.getSaveUserEventsTopic(), request);
                     }

                 }
             } else {
                 continue;
             }}
         }
	public SMSRequest prepareSMSRequest(ServiceRequestData serviceReq, ActionInfo actionInfo, RequestInfo requestInfo,Map<String, String> messageMap,String action,String serviceRequestDate, int days) {
    	
    	String phoneNumberRetrived = null;
    	String message=null;
        if ((action.equals(HCConstants.REMINDER))) {    
        	   phoneNumberRetrived = serviceReq.getContactNumber();
               message =  getMessage( HCConstants.REMINDER, serviceReq, requestInfo,  messageMap, null,HCConstants.SMS,serviceRequestDate,days );
         
        	}
       
     	  
     	else if (action.equals(HCConstants.OVERDAYS)) { 
     	   phoneNumberRetrived = serviceReq.getContactNumber();
           message =  getMessage( HCConstants.OVERDAYS, serviceReq, requestInfo,  messageMap, null,HCConstants.SMS,serviceRequestDate,days );
       
     	}
       
        if(phoneNumberRetrived==null ||message== null)
    	   return null;
        return SMSRequest.builder().mobileNumber(phoneNumberRetrived).message(message).build();
    }
	
	public String getMessage(String action, ServiceRequestData serviceReq,RequestInfo requestInfo,  Map<String, String> messageMap, String employeeName,String  notifcationType,String serviceRequestDate,int days )
	 {
	 	String text=null;
	 	
			//epoc date to date convert

			String employee=serviceReq.getOwnerName();
			
			switch(action)
			{
			
			case HCConstants.OVERDAYS :
				
				if(notifcationType.equals(HCConstants.SMS))
				{
					text = messageMap.get(HCConstants.HC_EMPLOYEE_OVERDAYS_SMS_NOTIFICATION);
				}
				else if (notifcationType.equals(HCConstants.EMAIL))
				{
					
					text = messageMap.get(HCConstants.HC_EMPLOYEE_OVERDAYS_EMAIL_NOTIFICATION);
				}
				else  if (notifcationType.equals(HCConstants.PUSH))
					text = messageMap.get(HCConstants.HC_EMPLOYEE_OVERDAYS_PUSH_NOTIFICATION);
				
				if(text != null)
				{
				text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employee)  
						.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_TYPE, serviceReq.getServiceType())
						.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
	                  .replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, serviceRequestDate)
	                  .replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DAYS_KEY, days+"");
				}
				break;
				
				
			case HCConstants.REMINDER :
				if(notifcationType.equals(HCConstants.SMS))
				{
					text = messageMap.get(HCConstants.HC_EMPLOYEE_REMAINDER_SMS_NOTIFICATION);
				}
				else if (notifcationType.equals(HCConstants.EMAIL))
				{
					
					text = messageMap.get(HCConstants.HC_EMPLOYEE_REMAINDER_EMAIL_NOTIFICATION);
				}
				else  if (notifcationType.equals(HCConstants.PUSH))
					text = messageMap.get(HCConstants.HC_EMPLOYEE_REMAINDER_PUSH_NOTIFICATION);
					
		          if(text != null)
		          {
				text = text.replace(HCConstants.SMS_NOTIFICATION_EMP_NAME_KEY, employee) 
						.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_TYPE, serviceReq.getServiceType())
						.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id())
	                  .replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DATE_KEY, serviceRequestDate);
	                 // .replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_DAYS_KEY, days+"");	
		          }
				break;			
				
			
			
			}
	 	
	 	return text;
	 }private String getSubject(String action, ServiceRequestData serviceReq, RequestInfo requestInfo,
				Map<String, String> messageMap, String employeeName, String email) {
			
			String text=null;

			switch(action)
			{
			
			case HCConstants.REMINDER :
				
				text = messageMap.get(HCConstants.HC_EMPLOYEE_REMAINDER_EMAIL_SUBJECT);

				break;

			case HCConstants.OVERDAYS:
				
				text = messageMap.get(HCConstants.HC_EMPLOYEE_OVERDAYS_EMAIL_SUBJECT);
				break;
			
			}
			if(text != null)
			{
			text = text.replace(HCConstants.SMS_NOTIFICATION_SERVICEREQUEST_ID, serviceReq.getService_request_id());
			}

			return text;
		}
	 
	 public EventRequest preparePushNotificationRequest(ServiceRequestData serviceReq, ActionInfo actionInfo, RequestInfo requestInfo,Map<String, String> messageMap,String action,String serviceRequestDate,String tenantId,int days) {
	 	
	 	String message=null;
	 	
	 	List<Event> events = new ArrayList<>();
	    
	 	if (action.equals(HCConstants.REMINDER)) {    
	 	   
	        message =  getMessage( HCConstants.REMINDER, serviceReq, requestInfo,  messageMap, null,HCConstants.PUSH,serviceRequestDate,days );
	  
	 	}

		 else if (action.equals(HCConstants.OVERDAYS)) {    
			   message =  getMessage( HCConstants.OVERDAYS, serviceReq, requestInfo,  messageMap, null,HCConstants.PUSH,serviceRequestDate,days );

		}
	     
	     List<String> toUsers = new ArrayList<>();
	     toUsers.add(requestInfo.getUserInfo().getUuid());
	     
	     List<String> toRole= new ArrayList<>();
	     toRole.add("All");
	     Recepient recepient = Recepient.builder()
	             .toUsers(toUsers).toRoles(toRole).build();

	     Event event = Event.builder()
	             .tenantId(tenantId)
	             .description(message)
	             .eventType(HCConstants.USREVENTS_EVENT_TYPE)
	             .name(HCConstants.USREVENTS_EVENT_NAME)
	             .postedBy(HCConstants.USREVENTS_EVENT_POSTEDBY)
	             .recepient(recepient)
	             .referenceId(null)

	             .eventDetails(null).build();
	     		 events.add(event);

	 return EventRequest.builder().requestInfo(requestInfo).events(events).build();
	    
	     
	 }
	 
	 public EmailRequest prepareEmailRequest(ServiceRequestData serviceReq, ActionInfo actionInfo, RequestInfo requestInfo,Map<String, String> messageMap,String action,String serviceRequestDate,int days) {
		 	
		
	 	String emailIdRetrived = null;
	 	String message=null;
	 	String subject = null;
	   
	     if (action.equals(HCConstants.REMINDER)) {    
	     	    emailIdRetrived = serviceReq.getEmail();
	            message = getMessage( HCConstants.REMINDER, serviceReq, requestInfo,  messageMap, null,HCConstants.EMAIL,serviceRequestDate,days );           	
	            subject = getSubject( HCConstants.REMINDER, serviceReq, requestInfo,  messageMap, null ,HCConstants.EMAIL);
	     	}
	    
	     else if (action.equals(HCConstants.OVERDAYS))
	     
	     {
	     	emailIdRetrived=serviceReq.getEmail();
	     	message=  getMessage(HCConstants.OVERDAYS, serviceReq, requestInfo,  messageMap, null ,HCConstants.EMAIL,serviceRequestDate,days);
	    	subject = getSubject( HCConstants.OVERDAYS, serviceReq, requestInfo,  messageMap, null ,HCConstants.EMAIL);
	     }
	    
	     
	     if(emailIdRetrived==null ||message== null)
	 	   return null;
	     return EmailRequest.builder().email(emailIdRetrived).subject(subject).body(message).isHTML(true).build();
	 }
	 
	 public static String sendPostRequest(String requestUrl, String payload) {
			StringBuffer jsonString = new StringBuffer();
			try {
				URL url = new URL(requestUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestProperty("Content-Type", "application/json");
				OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
				writer.write(payload);
				writer.close();
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String line;
				while ((line = br.readLine()) != null) {
					jsonString.append(line);
					System.out.println(line);
				}
				br.close();
				connection.disconnect();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			return jsonString.toString();
		}
	 

}
