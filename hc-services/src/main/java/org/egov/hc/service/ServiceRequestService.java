package org.egov.hc.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import java.util.UUID;

import java.util.stream.Collectors;


import org.apache.commons.lang3.StringUtils;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.hc.consumer.HCNotificationConsumer;
import org.egov.hc.contract.AuditDetails;

import org.egov.hc.contract.RequestInfoWrapper;
import org.egov.hc.contract.ResponseInfoWrapper;

import org.egov.hc.contract.ServiceRequest;

import org.egov.hc.contract.ServiceResponse;
import org.egov.hc.model.ActionHistory;
import org.egov.hc.model.ActionInfo;
import org.egov.hc.model.ProcessInstance;
import org.egov.hc.model.ProcessInstanceRequest;
import org.egov.hc.model.ServiceRequestData;
import org.egov.hc.model.State;
import org.egov.hc.model.RequestData;

import org.egov.hc.model.user.Citizen;
import org.egov.hc.model.user.CreateUserRequest;
import org.egov.hc.model.user.UserResponse;
import org.egov.hc.model.user.UserSearchRequest;
import org.egov.hc.model.user.UserType;
import org.egov.hc.producer.HCProducer;
import org.egov.hc.producer.HCConfiguration;

import org.egov.hc.repository.IdGenRepository;
import org.egov.hc.repository.ServiceRepository;

import org.egov.hc.utils.DeviceSource;

import org.egov.hc.utils.HCConstants;
import org.egov.hc.utils.HCUtils;
import org.egov.hc.utils.ResponseInfoFactory;
import org.egov.hc.utils.WorkFlowConfigs;

import org.egov.hc.web.models.Idgen.IdGenerationResponse;
import org.egov.hc.workflow.Document;
import org.egov.hc.workflow.WorkflowIntegrator;

import org.egov.tracer.model.CustomException;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class ServiceRequestService {

	@Autowired
	private ResponseInfoFactory factory;

	@Autowired
	private HCConfiguration hcConfiguration;

	@Autowired
	private HCUtils hCUtils;

	@Autowired
	private HCProducer hCProducer;

	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private HCNotificationConsumer notificationConsumer;
	
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private IdGenRepository idgenrepository;

	@Autowired
	private DeviceSource deviceSource;

	@Autowired
	private RestTemplate rest;

	@Autowired
	public ServiceRequestService(HCConfiguration config, HCConfiguration hcConfiguration,
			WorkflowIntegrator wfIntegrator, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			ServiceRepository serviceRepository, HCNotificationConsumer notificationConsumer, HCConstants hcConstants) {

		
		this.hcConfiguration = hcConfiguration;
		this.idgenrepository = idgenrepository;
		this.notificationConsumer = notificationConsumer;
		this.wfIntegrator = wfIntegrator;

	}


	/***
	 * Asynchronous method performs business logic if any and adds the data to
	 * persister queue on create topic
	 * 
	 * @param request
	 * @throws JSONException 
	 */

	public ServiceResponse create(ServiceRequest request, String requestHeader) throws JSONException {
		
		
			log.info("Service layer for createss");
			
			// generate Service request id using IdGen
	
			String service_request_id = generateServiceRequestId(request);
			log.info("Generate service request id :"+service_request_id);
			enrichserviceRequestForcreate(request, service_request_id);
			
			String role = wfIntegrator.parseBussinessServiceData(wfIntegrator.getbussinessServiceDatafromprocesinstanceEdit(request),request);
			// call workflow service if it's enable else uses internal workflow process
		
			
			boolean flag = false;
			if (hcConfiguration.getIsExternalWorkFlowEnabled()) {
				flag = wfIntegrator.callWorkFlow(request, service_request_id,role,null);
			}
			if(flag)
			{
				//String bussinessServiceData = getbussinessServiceDatafromprocesinstanceEdit(request);
				
				String status = HCConstants.INITIATED_STATUS;
				String history_service_request_id = null;
				String service_request_id_new_gen = null;
				String action = HCConstants.ACTION_OPEN;		
	
				serviceRequest(request, service_request_id, requestHeader, role,
						status, history_service_request_id, service_request_id_new_gen,action);
			}
			else
			{
				request.getServices().get(0).setService_request_id("null");
			}
			return getServiceResponse(request);	

	}




	private String generateServiceRequestId(ServiceRequest request) {

		String service_request_id = "";
		IdGenerationResponse id = idgenrepository.getId(request.getRequestInfo(), request.getServices().get(0).getTenantId(),
				hcConfiguration.getApplicationNumberIdgenName(), hcConfiguration.getApplicationNumberIdgenFormat(), 1);
		if (id.getIdResponses() != null && id.getIdResponses().get(0) != null) {
			service_request_id = id.getIdResponses().get(0).getId();
		} else
			throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),
					"service_request_id Generation Failed");

		return service_request_id;

	}

	private ServiceRequestData parseServiceRequestData(JSONObject serviceRequest) {
		ServiceRequestData serviceRequestData = new ServiceRequestData();
		try {

			// Get service request object within list
			
			 String service_request_id = (String) serviceRequest.get("service_request_id");   
		        serviceRequestData.setService_request_id(service_request_id);
				 

			String ownerName = (String) serviceRequest.get("owner_name");
			serviceRequestData.setOwnerName(ownerName);

			String serviceType = (String) serviceRequest.get("service_type");
			serviceRequestData.setServiceType(serviceType);

			String current_assignee = (String) serviceRequest.get("current_assignee");
			serviceRequestData.setCurrent_assignee(current_assignee);

			String houseNoAndStreetName = (String) serviceRequest.get("houseNoAndStreetName");
			serviceRequestData.setHouseNoAndStreetName(houseNoAndStreetName);

			String landmark = (String) serviceRequest.get("landmark");
			serviceRequestData.setLandmark(landmark);

			String latitude = (String) serviceRequest.get("latitude");
			serviceRequestData.setLatitude(latitude);

			String longitude = (String) serviceRequest.get("longitude");
			serviceRequestData.setLongitude(longitude);

			String mohalla = (String) serviceRequest.get("mohalla");
			serviceRequestData.setMohalla(mohalla);

			String createdBy = (String) serviceRequest.get("createdby");
			serviceRequestData.setCreatedBy(createdBy);

			String service_request_status = (String) serviceRequest.get("service_request_status");
			serviceRequestData.setService_request_status(service_request_status);

			String service_request_uuid = (String) serviceRequest.get("service_request_uuid");
			serviceRequestData.setService_request_uuid(service_request_uuid);
			
			String servicerequest_lang = (String) serviceRequest.get("servicerequest_lang");
			serviceRequestData.setServicerequest_lang(servicerequest_lang);

			String contactNumber = (String) serviceRequest.get("contact_number");
			
			serviceRequestData.setContactNumber(contactNumber);

			String email = (String) serviceRequest.get("email_id");

			serviceRequestData.setEmail(email);

			String tenantId = (String) serviceRequest.get("tenant_id");
			serviceRequestData.setTenantId(tenantId);

			org.json.JSONObject objDocument = new org.json.JSONObject(
					serviceRequest.get("service_request_document").toString());

			org.json.JSONArray documentList = objDocument.getJSONArray("document");

			List<String> newdoclist = new ArrayList<>();

			for (int i = 0; i < documentList.length(); i++) {

				org.json.JSONObject empDetails = new org.json.JSONObject(documentList.get(i).toString());

				String document = empDetails.getString("media");

				newdoclist.add(document);

			}
			serviceRequestData.setMediaList(newdoclist);

		      

		} catch (Exception ex) {

		}

		return serviceRequestData;
	}

	/**
	 * Asynchronous method performs business logic if any and adds the data to
	 * persister queue on update topic
	 * 
	 * @param request
	 * @throws JSONException 
	 */
	public ServiceResponse update(ServiceRequest request, String requestHeader) throws JSONException {
		
		try
		{
			enrichServiceRequestForUpdate(request, requestHeader);
			if (null == request.getActionInfo())
				request.setActionInfo(new ArrayList<ActionInfo>());
	
			return getServiceResponse(request);
		}
		catch (Exception e) {
			throw new CustomException(HCConstants.UPDATE_REQUEST_EXCEPTION_CODE, e.getMessage());
		}
 
	}

	/**
	 * private method to enrich request with Ids and action infos for create
	 * 
	 * @param serviceRequest
	 */
	private void enrichserviceRequestForcreate(ServiceRequest serviceRequest, String service_request_id) {
		log.info("enriching service request create.");
		Map<String, String> actionStatusMap = WorkFlowConfigs.getActionStatusMap();
		RequestInfo requestInfo = serviceRequest.getRequestInfo();

		List<ServiceRequestData> serviceReqs = serviceRequest.getServices();

		String tenantId = serviceReqs.get(0).getTenantId();
		overRideCitizenAccountId(serviceRequest);
		validateAndCreateUser(serviceRequest);


		AuditDetails auditDetails = hCUtils.getAuditDetails(String.valueOf(requestInfo.getUserInfo().getId()), true);
		String by = auditDetails.getCreatedBy() + ":" + requestInfo.getUserInfo().getRoles().get(0).getName();
		List<ActionInfo> actionInfos = new LinkedList<>();
		if (!CollectionUtils.isEmpty(serviceRequest.getActionInfo())) {
			actionInfos = serviceRequest.getActionInfo();
		}
		for (int servReqCount = 0; servReqCount < serviceReqs.size(); servReqCount++) {

			serviceRequest.getServices().get(servReqCount).setIsRoleSpecific(true);
			serviceRequest.getServices().get(servReqCount).setAction(HCConstants.INITIATE);
			serviceRequest.getServices().get(servReqCount).setService_request_id(service_request_id);

			ActionInfo actionInfo = null;
			try {
				actionInfo = actionInfos.get(servReqCount);
				if (null != actionInfo) {
					actionInfo.setUuid(UUID.randomUUID().toString());
					actionInfo.setBusinessKey(serviceReqs.get(servReqCount).getBusinessService());
					actionInfo.setAction(WorkFlowConfigs.ACTION_OPEN);
					actionInfo.setAssignee(requestInfo.getUserInfo().getId().toString());
					actionInfo.setBy(by);
					actionInfo.setWhen(auditDetails.getCreatedTime());
					actionInfo.setTenantId(tenantId);
					actionInfo.setStatus(actionStatusMap.get(WorkFlowConfigs.ACTION_OPEN));
				} else {
					ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
							.businessKey(serviceReqs.get(servReqCount).getBusinessService())
							.action(WorkFlowConfigs.ACTION_OPEN).assignee(null).by(by)
							.when(auditDetails.getCreatedTime()).tenantId(tenantId)
							.status(actionStatusMap.get(WorkFlowConfigs.ACTION_OPEN)).build();
					actionInfos.add(newActionInfo);
				}
			} catch (Exception e) {
				ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
						.businessKey(serviceReqs.get(servReqCount).getBusinessService())
						.action(WorkFlowConfigs.ACTION_OPEN).assignee(null).by(by).when(auditDetails.getCreatedTime())
						.tenantId(tenantId).status(actionStatusMap.get(WorkFlowConfigs.ACTION_OPEN)).build();
				actionInfos.add(newActionInfo);
			}

		}
		serviceRequest.setActionInfo(actionInfos);
	}

	/**
	 * Override the accountId of every request with the user id
	 * 
	 * @param serviceRequest
	 */
	private void overRideCitizenAccountId(ServiceRequest serviceRequest) {
		User user = serviceRequest.getRequestInfo().getUserInfo();
		List<String> codes = user.getRoles().stream().map(Role::getCode).collect(Collectors.toList());
		if (codes.contains(HCConstants.ROLE_CITIZEN) || codes.contains(HCConstants.ROLE_NAME_CITIZEN))
			serviceRequest.getServices().forEach(service -> service.setAccountId(String.valueOf(user.getId())));
	}

	/**
	 * Checks if the user is present for the given citizen object.
	 * 
	 * @param citizen
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	private String isUserPresent(Citizen citizen, RequestInfo requestInfo, String tenantId) {
		ObjectMapper mapper = hCUtils.getObjectMapper();
		UserSearchRequest searchRequest = UserSearchRequest.builder().userName(citizen.getMobileNumber())
				.tenantId(tenantId).userType(HCConstants.ROLE_CITIZEN).requestInfo(requestInfo).build();
		StringBuilder url = new StringBuilder(
				hcConfiguration.getUserBasePath() + hcConfiguration.getUserSearchEndPoint());
		UserResponse res = mapper.convertValue(serviceRepository.fetchResult(url, searchRequest), UserResponse.class);
		if (CollectionUtils.isEmpty(res.getUser())) {
			return null;
		}
		return res.getUser().get(0).getId().toString();
	}

	/**
	 * When CSR files a complaint, this method captures the user information if the
	 * user exists otherwise creates the user.
	 * 
	 * @param serviceRequest
	 * @param errorMap
	 */
	private void validateAndCreateUser(ServiceRequest serviceRequest) {
		RequestInfo requestInfo = serviceRequest.getRequestInfo();
		List<String> roles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode)
				.collect(Collectors.toList());
		if (roles.contains(HCConstants.ROLE_NAME_CSR) || roles.contains(HCConstants.ROLE_CSR)) {
			serviceRequest.getServices().stream().forEach(request -> {
				String accId = null;
				if (null != request.getCitizen()) {
					accId = isUserPresent(request.getCitizen(), requestInfo, request.getTenantId());
					if (StringUtils.isEmpty(accId)) {
						accId = createUser(request.getCitizen(), requestInfo, request.getTenantId());
					}
					request.setAccountId(accId);
				}
			});
		}
	}

	/**
	 * This method creates user in user svc.
	 * 
	 * @param citizen
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	private String createUser(Citizen citizen, RequestInfo requestInfo, String tenantId) {
		ObjectMapper mapper = hCUtils.getObjectMapper();
		citizen.setUserName(citizen.getMobileNumber());
		citizen.setActive(true);
		citizen.setTenantId(tenantId);
		citizen.setType(UserType.CITIZEN);
		citizen.setRoles(Arrays.asList(org.egov.hc.model.user.Role.builder().code(HCConstants.ROLE_CITIZEN).build()));
		StringBuilder url = new StringBuilder(
				hcConfiguration.getUserBasePath() + hcConfiguration.getUserCreateEndPoint());
		CreateUserRequest req = CreateUserRequest.builder().citizen(citizen).requestInfo(requestInfo).build();
		UserResponse res = mapper.convertValue(serviceRepository.fetchResult(url, req), UserResponse.class);
		return res.getUser().get(0).getId().toString();
	}

	/**
	 * Util method for the update to enrich the actions in the request
	 * 
	 * @param request
	 * @throws JSONException 
	 */
	private void enrichServiceRequestForUpdate(ServiceRequest request, String requestHeader) throws JSONException {
		
		Map<String, List<String>> errorMap = new HashMap<>();
		RequestInfo requestInfo = request.getRequestInfo();
		List<ServiceRequestData> serviceReqs = request.getServices();
		List<ActionInfo> actionInfos = new ArrayList<>();
		String action = null;
		String getroles = null; 
	
		final AuditDetails auditDetail = hCUtils.getAuditDetail(String.valueOf(requestInfo.getUserInfo().getId()),
				false);
		
		auditDetail.setCreatedBy(requestInfo.getUserInfo().getRoles().get(0).getName());
		request.setAuditDetails(auditDetail);
		
		String jsonRole = "";
		String employeeUuid = null;
		
		for (int servReqCount = 0; servReqCount < serviceReqs.size(); servReqCount++) {

			String service_request_id = request.getServices().get(servReqCount).getService_request_id();
			List<String> roleList = request.getServices().get(servReqCount).getRoleList();
			List<String> assigneeList = new ArrayList<>();
			if (null != roleList && !roleList.isEmpty()) {
				request.getServices().get(servReqCount).setRole(roleList.get(0));
			}
			if (request.getServices().get(servReqCount).getIsRoleSpecific().equals(true)) {

				
				String bussinessServiceData = getbussinessServiceDatafromprocesinstance(request);
				
				if ((request.getServices().get(servReqCount).getAction().equals(HCConstants.APPROVE))
						|| request.getServices().get(servReqCount).getAction().equals(HCConstants.VERIFY_AND_FORWARD_TO_SDO)
						|| request.getServices().get(servReqCount).getAction().equals(HCConstants.FORWARD_FOR_INSPECTION))
				{
				 getroles = wfIntegrator.parseBussinessServiceData(bussinessServiceData,request);
				
				 request.getServices().get(servReqCount).setRole(getroles);
				}
				if(request.getServices().get(servReqCount).getAction().equals(HCConstants.INSPECT))
				{
					getroles = parseInspectedBussinessServiceData(bussinessServiceData,request);
					 request.getServices().get(servReqCount).setRole(getroles);
				}
				
				
				roleList.add(getroles);
				jsonRole = getroles;

				log.info("Selected role depend upon the action");

				if (null != request.getServices().get(servReqCount).getRoleList()
						&& request.getServices().get(servReqCount).getRoleList().size() > 0) {

					String role = request.getServices().get(servReqCount).getRoleList().get(0);
					assigneeList = serviceRepository.getRoleDetails(request, role);
					jsonRole = role;
				}

			} else {

				//assigneeList = request.getServices().get(servReqCount).getAssignee();
				
				String list = request.getServices().get(servReqCount).getAssignee().get(0).toString();
				
				String roleSplit [] =  list.split("#");
				employeeUuid = roleSplit[1];
				
				List<String> assigneeListSplited = new ArrayList<>();
				assigneeListSplited.add(roleSplit[0]);
				assigneeList = assigneeListSplited;
				
			}

			RequestData requestData = new RequestData();
			requestData.setService_request_id(service_request_id);
			requestData.setRequestInfo(request.getRequestInfo());

			// get data from service request table with request id

			ServiceRequestData serviceRequest = null;
			JSONObject serviceRequestJson = serviceRepository.getServiceRequestData(requestData);
			serviceRequest = parseServiceRequestData(serviceRequestJson);

			deviceSource.saveDeviceDetails(requestHeader, HCConstants.DEVICE_HORTICULTURE,
					serviceRequest.getTenantId(), HCConstants.MODULE_CODE, request.getAuditDetails(),
					serviceRequest.getService_request_uuid());
			
			log.info("Get data from service request table with service request id is: " + serviceRequest );
			
			request.getServices().get(servReqCount).setCity(request.getRequestInfo().getUserInfo().getTenantId());

			// call workflow

			final AuditDetails auditDetails = hCUtils.getAuditDetails(String.valueOf(requestInfo.getUserInfo().getId()),
					false);
			
			
			String by = auditDetails.getCreatedBy() + ":" + requestInfo.getUserInfo().getRoles().get(0).getName();
			Boolean flag = false;
			
			
			if(null != request.getServices().get(0).getSubType())
			{
				JSONObject subtype = new JSONObject();
				subtype.put("subservicetype", request.getServices().get(0).getServiceRequestSubtype());
				request.getServices().get(0).setServiceRequestSubtype(subtype.toJSONString());
			}
			
			if (hcConfiguration.getIsExternalWorkFlowEnabled()) {
				flag = wfIntegrator.callWorkFlow(request, service_request_id,jsonRole,employeeUuid);
			}
			
			if(flag)
			{
				
			// Update service request form

			ServiceRequestData updateRequest = new ServiceRequestData();

			List<String> documentlist = new ArrayList<>(serviceRequest.getMediaList());

			for (int fileCnt = 0; fileCnt < request.getServices().get(0).getWfDocuments().size(); fileCnt++) {
				documentlist.add(request.getServices().get(0).getWfDocuments().get(fileCnt).getFileStoreId());
			}

			JSONObject documentDetailsJson = new JSONObject();
			JSONArray jsonArray = new JSONArray();

			for (String document : documentlist) {

				JSONObject formDetailsJson = new JSONObject();
				formDetailsJson.put("media", document);

				jsonArray.add(formDetailsJson);
			}
			documentDetailsJson.put("document", jsonArray); 

			serviceRequest.setServiceMedia(documentDetailsJson.toJSONString());

			updateRequest.setService_request_id(service_request_id);
			updateRequest.setCreatedTime(request.getAuditDetails().getCreatedTime());
			updateRequest.setService_request_uuid(serviceRequest.getService_request_uuid());
			updateRequest.setTenantId(serviceRequest.getTenantId());
			

			if (request.getServices().get(servReqCount).getAction().equals(HCConstants.APPROVE))
				updateRequest.setService_request_status(HCConstants.APPROVED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.COMPLETE))
				updateRequest.setService_request_status(HCConstants.COMPLETED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.VERIFY_AND_FORWARD))
				updateRequest.setService_request_status(HCConstants.ASSIGNED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.VERIFY_AND_FORWARD_TO_SDO))
				updateRequest.setService_request_status(HCConstants.ASSIGNED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.INSPECT))
				updateRequest.setService_request_status(HCConstants.INSPECTED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.REQUEST_CLARIFICATION))
				updateRequest.setService_request_status(HCConstants.ASSIGNED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.REJECT))
				updateRequest.setService_request_status(HCConstants.REJECTED_STATUS);
			else if (request.getServices().get(servReqCount).getAction().equals(HCConstants.FORWARD_FOR_INSPECTION))
				updateRequest.setService_request_status(HCConstants.INSPECTED_STATUS);

			updateRequest.setServiceMedia(documentDetailsJson.toJSONString());
			updateRequest.setLastModifiedBy(auditDetails.getLastModifiedBy());
			updateRequest.setLastModifiedTime(auditDetails.getLastModifiedTime());
			request.getServices().get(0).setLastModifiedTime(auditDetails.getLastModifiedTime());

			if (request.getServices().get(servReqCount).getIsRoleSpecific().equals(true))
				updateRequest.setCurrent_assignee(request.getServices().get(servReqCount).getRole());
			else {
				if (null != request.getServices().get(servReqCount).getAssignee()
						&& !request.getServices().get(servReqCount).getAssignee().isEmpty())
					updateRequest.setCurrent_assignee(request.getServices().get(servReqCount).getAssignee().get(0));
			}
			RequestInfoWrapper infowraperforupdate = RequestInfoWrapper.builder().requestBody(updateRequest).build();

			// notification to user when reject or completed request

			action = request.getServices().get(servReqCount).getAction().replace(" ", "");
			request.getServices().get(servReqCount).setAction(action);
			
			log.info(" Checking action ");
			
			
			if (request.getServices().get(servReqCount).getAction().equals(WorkFlowConfigs.ACTION_REJECT)
					|| (request.getServices().get(servReqCount).getAction().equals(WorkFlowConfigs.ACTION_COMPLETE)))

			{
				User user = new User();
				
				String tenantIdSplited = getTenantid(serviceRequest.getTenantId());
				
				ArrayList list = new ArrayList<>();
				list.add(serviceRequest.getCreatedBy());
				JSONArray array = new JSONArray();
				JSONObject obj = new JSONObject();
				obj.put("RequestInfo", request.getRequestInfo());
				obj.put("id", list);
				obj.put("tenantId", tenantIdSplited);
				
				String response = null;
				try {
					response = rest.postForObject(hcConfiguration.getUserBasePath().concat(hcConfiguration.getUserSearchEndPoint()), obj, String.class);
				} catch (HttpClientErrorException e) {
				}
				
					org.json.JSONObject userDetails = new org.json.JSONObject(
							response.toString());
					org.json.JSONArray responseObj = userDetails.getJSONArray("user");
				
				for (int userCnt = 0; userCnt < responseObj.length(); userCnt++) 
				{
					org.json.JSONObject userinfo = new org.json.JSONObject(
							responseObj.get(userCnt).toString());
					
					org.json.JSONArray wfRoles = userinfo.getJSONArray("roles");
					List<Role> citizenRoleList = new ArrayList();
					if (null != wfRoles) {
						org.json.JSONObject roleDetails = null;
						for (int roleCnt = 0; roleCnt < wfRoles.length(); roleCnt++) {
							roleDetails = new org.json.JSONObject(wfRoles.get(roleCnt).toString());
							Role role = new Role();
							role.setCode(roleDetails.getString("code"));
							role.setName(roleDetails.getString("name"));
							citizenRoleList.add(role);
						}
						}
					user.setRoles(citizenRoleList);
					String uuid = userinfo.getString("uuid");
					Long id = userinfo.getLong("id");
					String type = userinfo.getString("type");
					String userName = userinfo.getString("userName");
					String mobileNumber = userinfo.getString("mobileNumber");
					String name = userinfo.getString("name");
					String tenantId = userinfo.getString("tenantId");
					user.setUuid(uuid);
					user.setId(id);
					user.setMobileNumber(mobileNumber);
					user.setUserName(userName);
					user.setType(type);
					user.setName(name);
					user.setTenantId(tenantId);
				}

				request.getRequestInfo().setUserInfo(user);
				request.getServices().get(0).setContactNumber(serviceRequest.getContactNumber());
				request.getServices().get(0).setOwnerName(serviceRequest.getOwnerName());
				request.getServices().get(0).setEmail(serviceRequest.getEmail());
				
				//request.getServices().get(0).setCity(request.getRequestInfo().getUserInfo().getTenantId());
				request.getServices().get(0).setService_request_status(request.getServices().get(servReqCount).getAction());
				String msgId = requestInfo.getMsgId().split("[|]")[0];
				requestInfo.setMsgId(msgId+"|"+serviceRequest.getServicerequest_lang());
				
				if (serviceRequest != null) {
					ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
							.businessKey(serviceReqs.get(servReqCount).getBusinessService())
							.action(action)
							.assignee(serviceRequest.getCreatedBy())
							.by(by)
							.when(auditDetails.getCreatedTime())
							.tenantId(serviceReqs.get(servReqCount).getTenantId())
							.status(action).build();
					actionInfos.add(newActionInfo);
				}
				
				

				infowraperforupdate = RequestInfoWrapper.builder().actionInfo(actionInfos).requestInfo(requestInfo)
						.requestBody(updateRequest).services(request.getServices()).build();

				log.info("Update service request with action "+ action +" and sending notification to citizen");
				
				hCProducer.push(hcConfiguration.getUpdateTopic(), infowraperforupdate);

			} else {
				
				String msgId = request.getRequestInfo().getMsgId().split("[|]")[0];
				request.getRequestInfo().setMsgId(msgId+"|"+hcConfiguration.getFallbackLocale());
				
				
				if(request.getServices().get(servReqCount).getIsRoleSpecific().equals(true))
				{
					String tenantid = request.getRequestInfo().getUserInfo().getTenantId().split("[.]")[0];
					Map<String, String> messageMap = notificationConsumer.getLocalizationMessage(tenantid,
							request.getRequestInfo());
					String role = request.getServices().get(0).getRole();
					
					List<String> list = new ArrayList<String>(Arrays.asList(role.split(",")));
					
					for (int roleCnt = 0; roleCnt < list.size(); roleCnt++) 
					{	
					notificationConsumer.getRolewiseUserList(request, role, messageMap);
					}
				}
				else
				{
	
				if (null != assigneeList && assigneeList.size() > 0) {
					for (String assignee : assigneeList) {
						ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
								.businessKey(serviceReqs.get(servReqCount).getBusinessService())
								.action(action).assignee(assignee).by(by)
								.when(auditDetails.getCreatedTime())
								.tenantId(serviceReqs.get(servReqCount).getTenantId())
								.status(action).build();
						actionInfos.add(newActionInfo);
					}

				}
				}
				
				updateRequest.setLastModifiedTime(auditDetails.getLastModifiedTime());

				infowraperforupdate = RequestInfoWrapper.builder().actionInfo(actionInfos).requestInfo(requestInfo)
						.requestBody(updateRequest).services(request.getServices()).build();

				hCProducer.push(hcConfiguration.getUpdateTopic(), infowraperforupdate);

			}

		}

		if (!errorMap.isEmpty()) {
			Map<String, String> newMap = new HashMap<>();
			errorMap.keySet().forEach(key -> newMap.put(key, errorMap.get(key).toString()));
			throw new CustomException(newMap);
		}
		}
	}

	private String parseInspectedBussinessServiceData(String bussinessServiceData, ServiceRequest request) throws JSONException {
		String newactions = null;

		String roles = null;
		Boolean found = false;
		
		for (int businessCnt = 0; businessCnt <= bussinessServiceData.length(); businessCnt++) 
		{
			org.json.JSONObject bussinessServiceDetails = new org.json.JSONObject(
					bussinessServiceData.toString());

			org.json.JSONArray businessServicesObj = bussinessServiceDetails.getJSONArray("BusinessServices");
			
			for (int businessServiceCnt = 0; businessServiceCnt <= businessServicesObj.length(); businessServiceCnt++) 
			{
			
				org.json.JSONObject businessServicesSingleObj = new org.json.JSONObject(
						businessServicesObj.get(businessServiceCnt).toString());
				
				org.json.JSONArray stateObj = businessServicesSingleObj.getJSONArray("states");
				
				for (int stateCnt = 0; stateCnt <= stateObj.length(); stateCnt++) 
				{
					org.json.JSONObject stateSingleObj = new org.json.JSONObject(
							stateObj.get(stateCnt).toString());
					
					org.json.JSONArray actionsObj = stateSingleObj.getJSONArray("actions");
					
					for (int actionCnt = 0; actionCnt < actionsObj.length(); actionCnt++) 
					{
						org.json.JSONObject actionsSingleObj = new org.json.JSONObject(
								actionsObj.get(actionCnt).toString());
						
						
						newactions = actionsSingleObj.getString("action");

						if(newactions.equals(request.getServices().get(0).getAction()))
						{
				
							org.json.JSONArray roleObj = actionsSingleObj.getJSONArray("roles");
	
									for (int roleCnt = 0; roleCnt < roleObj.length(); roleCnt++) 
										{
											String role = roleObj.getString(roleCnt);
											
											if(null != roles)
												{
												roles = roles +","+role;
												
												}
											else 
												{	
												roles = role;
												}
												found = true;
												log.info("businessServicesObj" + actionsObj);
												
										 	}
										break;
							}
							if(found) break; 
						}
						
						if(found) break;
					}	
					if(found) break;
			}
			if(found) break;	
		}
		return roles ;
	}


	/**
	 * returns ServiceResponse fetched from database/built based on the given
	 * ServiceRequest
	 * 
	 * @param serviceReqRequest
	 * @return serviceReqResponse
	 */
	public ServiceResponse getServiceResponse(ServiceRequest serviceReqRequest) {
		return ServiceResponse.builder()
				.responseInfo(factory.createResponseInfoFromRequestInfo(serviceReqRequest.getRequestInfo(), true))
				.services(serviceReqRequest.getServices())
				.actionHistory(convertActionInfosToHistorys(serviceReqRequest.getActionInfo())).build();
	}

	public ServiceResponse getServiceResponse(ResponseEntity<ServiceRequest> serviceReqRequest) {
		ServiceRequest objServiceRequest = serviceReqRequest.getBody();

		return ServiceResponse.builder()
				.responseInfo(factory.createResponseInfoFromRequestInfo(objServiceRequest.getRequestInfo(), true))
				.services(objServiceRequest.getServices())
				.actionHistory(convertActionInfosToHistorys(objServiceRequest.getActionInfo())).build();
	}

	/**
	 * helper method to convert list of actioninfos to list of actionHistorys
	 * 
	 * @param actionInfos
	 * @return
	 */
	private List<ActionHistory> convertActionInfosToHistorys(List<ActionInfo> actionInfos) {
		List<ActionHistory> historys = new ArrayList<>();
		if (!CollectionUtils.isEmpty(actionInfos))
			actionInfos.forEach(a -> {
				List<ActionInfo> infos = new ArrayList<>();
				infos.add(a);
				historys.add(new ActionHistory(infos));
			});
		return historys;
	}

	/**
	 * Method fetches a map of code vs name of the boundary types as per the
	 * request.
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param mohallaCodes
	 * @param hierarchyType
	 * @param boundaryType
	 * @return
	 */

	private ResponseEntity<ServiceRequest> serviceRequest(ServiceRequest request, String service_request_id,
			String requestHeader, String role, String status, String history_service_request_id, String service_request_id_new_gen, String action) {

		RequestInfoWrapper infoWrapper = new RequestInfoWrapper();
		try {

			if (null != request.getServices() && !request.getServices().isEmpty()) {
			
				String service_request_uuid = UUID.randomUUID().toString();

				deviceSource.saveDeviceDetails(requestHeader, HCConstants.DEVICE_HORTICULTURE,
						request.getServices().get(0).getTenantId(), HCConstants.MODULE_CODE, request.getAuditDetails(),
						service_request_uuid);

				List<String> documentList = new ArrayList<>();

				if (request.getServices().get(0).getIsEditState() == 1) {
					documentList.addAll(request.getServices().get(0).getMediaList());

					JSONObject documentDetailsJson = new JSONObject();
					JSONArray jsonArray = new JSONArray();

					for (String document : documentList) {

						JSONObject formDetailsJson = new JSONObject();
						formDetailsJson.put("media", document);

						jsonArray.add(formDetailsJson);
					}
					documentDetailsJson.put("document", jsonArray);

					request.getServices().get(0).setServiceMedia(documentDetailsJson.toJSONString());
					request.getServices().get(0).setService_request_id(service_request_id);
					request.getServices().get(0).setHistory_service_request_id(history_service_request_id);

				} else {

					documentList.addAll(request.getServices().get(0).getMedia());

					JSONObject documentDetailsJson = new JSONObject();
					JSONArray jsonArray = new JSONArray();

					for (String document : documentList) {

						JSONObject formDetailsJson = new JSONObject();
						formDetailsJson.put("media", document);

						jsonArray.add(formDetailsJson);
					}
					documentDetailsJson.put("document", jsonArray); // Here you can see the data in json forma

					request.getServices().get(0).setServiceMedia(documentDetailsJson.toJSONString());

				}
				
				if(null != request.getServices().get(0).getSubType())
				{
					JSONObject subtype = new JSONObject();
					subtype.put("subservicetype", request.getServices().get(0).getSubType());
					request.getServices().get(0).setServiceRequestSubtype(subtype.toJSONString());
				}
				
				String langData = request.getRequestInfo().getMsgId().replace("|", "#");
				String langDataSplit[] = langData.split("#");

				request.getServices().get(0).setServicerequest_lang(langDataSplit[1]);

				request.getServices().get(0)
						.setServiceType(request.getServices().get(0).getServiceType());
				request.getServices().get(0).setService_request_uuid(service_request_uuid);
				request.getServices().get(0).setCurrent_assignee(role);
				request.getServices().get(0).setService_request_status(status);
				request.getServices().get(0).setCreatedBy(request.getAuditDetails().getCreatedBy());
				request.getServices().get(0).setCreatedTime(request.getAuditDetails().getCreatedTime());
				request.getServices().get(0).setLastModifiedBy(request.getAuditDetails().getLastModifiedBy());
				request.getServices().get(0).setLastModifiedTime(request.getAuditDetails().getLastModifiedTime());

				List<ServiceRequestData> applicatinFormList = new ArrayList<>();
				applicatinFormList.add(request.getServices().get(0));

				List<ActionInfo> actionInfos = new ArrayList<>();

				final AuditDetails auditDetails = hCUtils
						.getAuditDetails(String.valueOf(request.getRequestInfo().getUserInfo().getId()), false);
				String by = auditDetails.getCreatedBy() + ":"
						+ request.getRequestInfo().getUserInfo().getRoles().get(0).getName();

				ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())

						.action(action).assignee(request.getServices().get(0).getCreatedBy())
						.by(by).when(request.getServices().get(0).getCreatedTime())
						.tenantId(request.getServices().get(0).getTenantId()).status(action)
						.build();
				actionInfos.add(newActionInfo);

				infoWrapper = RequestInfoWrapper.builder().services(applicatinFormList).actionInfo(actionInfos)
						.requestInfo(request.getRequestInfo()).requestBody(request.getServices().get(0)).build();
				
				log.info("Service request create : " + infoWrapper);

				hCProducer.push(hcConfiguration.getSaveTopic(), infoWrapper);

			}
		} catch (Exception e) {
			throw new CustomException("COMMITTEE_EXCEPTION", e.getMessage());
		}

		return new ResponseEntity<>(ServiceRequest.builder()
				.responseInfo(ResponseInfo.builder().status("Success").build()).responseBody(infoWrapper).build(),
				HttpStatus.OK);

	}

	public ResponseEntity<?> getServiceRequestDetails(RequestData requestData) {
		
		String responseValidate = "";
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(requestData, RequestData.class);
		
		responseValidate = hCUtils.validateJsonAddUpdateData(payloadData,HCConstants.SERVICEREQUESTGETDETAIL);
		if (responseValidate.equals("")) 
		   {
				JSONArray serviceRequest = null;
				try {
				 serviceRequest = serviceRepository.getServiceRequest(requestData);
			
				} catch (Exception e) {
					log.info("null");
				}
				String bisinesssla = getBussinessServiceSla(requestData);
		
				if (serviceRequest == null || serviceRequest.isEmpty()) {
					
				
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().ver("false").msgId("Invalid service request Id").build())
							.responseBody(serviceRequest).build(), HttpStatus.OK);
					
		
				} else {
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(HCConstants.SUCCESS).resMsgId(bisinesssla).build())
						.responseBody(serviceRequest).build(), HttpStatus.OK);
				}
		   }
					else
					{
						return new ResponseEntity<>(ResponseInfoWrapper.builder()
								.responseInfo(ResponseInfo.builder().status(HCConstants.FAIL).build())
							.responseBody(responseValidate).build(), HttpStatus.BAD_REQUEST);
					}
			
	}

	private String getBussinessServiceSla(RequestData requestData) {
		long sla = 0;
		int days = 0;
		String serviceRequestStatus;
		String serviceRequestId = requestData.getService_request_id();
		String payloadData ="{\"RequestInfo\": {\"userInfo\": {\"roles\": [{ \"code\": \"EE\",\"name\": \"EE\",\"tenantId\": \"ch.chandigarh\"}]}}}" ;

		//String payloadData ="{\"RequestInfo\": {\"userInfo\": {\"roles\": [{ \"code\": \"role_code\",\"name\": \"role_name\",\"tenantId\": \"tenantId\"}]}}}" ;
		//payloadData.replace("role_code", requestData.getRequestInfo().getUserInfo().getRoles().)(regex, replacement)
		
		try {
			String procesinstanceData = sendPostRequest(
					hcConfiguration.getWfHost().concat(hcConfiguration.getWfProcessSearch()).concat("?").concat("businessIds="
							+ serviceRequestId + "&history=" + HCConstants.TRUE + "&tenantId=" + requestData.getTenantId()),
					payloadData);
			
			

			org.json.JSONObject obj = new org.json.JSONObject(procesinstanceData);
			org.json.JSONArray ProcessInstances = obj.getJSONArray("ProcessInstances");

			org.json.JSONObject ProcessInstancesDetails = new org.json.JSONObject(ProcessInstances.get(0).toString());

			org.json.JSONObject stateObject = ProcessInstancesDetails.getJSONObject("state");

			serviceRequestStatus = stateObject.getString("applicationStatus");
			sla = ProcessInstancesDetails.getLong("businesssServiceSla");
		
			org.json.JSONObject ProcessInstancesDetailsNew = new org.json.JSONObject(
						ProcessInstances.get(0).toString());
				
			String serviceType = ProcessInstancesDetailsNew.getString("businessService");
		
			
			if (serviceRequestStatus.equalsIgnoreCase(HCConstants.REJECTED) || serviceRequestStatus.equalsIgnoreCase(HCConstants.COMPLETED_STATUS))
				days = 0;
			else
				if(sla > 0)
				{
					
					double doubleDays =  (double)sla/(double)(1000 * 60 * 60 * 24); //(sla / (1000 * 60 * 60 * 24)); 		
					days = (int)Math.ceil(doubleDays); 
					
				}
			
				else
					
				{
					sla = -sla;
					double doubleDays =  (double)sla/(double)(1000 * 60 * 60 * 24); //(sla / (1000 * 60 * 60 * 24)); 		
					days = (int)Math.ceil(doubleDays); 
					days = -days;
				}

		} catch (Exception e) {

		}

		return days + "";
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

	public ArrayList<String> getUserDetails(String service_request_id) {
		JSONArray serviceRequest = serviceRepository.getuserDetails(service_request_id);
		ArrayList<String> list = new ArrayList<String>();
		JSONArray jsonArray = (JSONArray) serviceRequest;
		if (jsonArray != null) {
			int len = jsonArray.size();
			for (int i = 0; i < len; i++) {
				list.add(jsonArray.get(i).toString());
			}
		}

		return list;

	}

	public ResponseEntity<ServiceResponse> searchRequest(RequestData requestData, RequestInfo requestInfo) {

		List<ServiceRequestData> serviceRequest = null;
		List<ServiceRequestData> serviceRequestList= new ArrayList<>();
		String responseValidate = "";
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(requestData, RequestData.class);
		
		responseValidate = hCUtils.validateJsonAddUpdateData(payloadData,HCConstants.SERVICEREQUESTGET);
		if (responseValidate.equals("")) 
		   {

			try {
				serviceRequest = serviceRepository.findRequest(requestData);
				 
			} catch (ParseException e) {
				log.info("Parse Exception");
			}
			if (serviceRequestList == null) {
				return new ResponseEntity(ServiceResponse.builder()
						.responseInfo(ResponseInfo.builder().msgId("Invalid role  or blank data ").build())
						.services(serviceRequest).build(), HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity(
						ServiceResponse.builder().responseInfo(ResponseInfo.builder().status(HCConstants.SUCCESS).build())
								.services(serviceRequest).build(),
						HttpStatus.OK);
			}
		   }
		else
		{
			return new ResponseEntity(ServiceResponse.builder()
					.responseInfo(ResponseInfo.builder().msgId(responseValidate).status(HCConstants.FAIL).build())
					.build(), HttpStatus.BAD_REQUEST);
	
		}
	}

	public ServiceResponse updateServiceRequest(ServiceRequest serviceRequest, String requestHeader) throws JSONException, InterruptedException {
		
		ResponseEntity<ServiceRequest> responseBody = null;
		String service_request_id_new = null;
		
		RequestInfo employeeInfo = new RequestInfo();
		List<String> documentList = null;
		
		String service_request_id = serviceRequest.getServices().get(0).getService_request_id();

		// making request data

		ServiceRequestData serviceRequestGet = getData(service_request_id);
		String role =null;
		String  status =null;
		String service_request_uuid = null;
		
		if(null !=serviceRequestGet)
		{
		
			role = serviceRequestGet.getCurrent_assignee();
			status = serviceRequestGet.getService_request_status();
			service_request_uuid = serviceRequestGet.getService_request_uuid();
		}
		
				documentList = new ArrayList<>();
				for (int docCnt = 0; docCnt < serviceRequestGet.getMediaList().size(); docCnt++) {
		
					documentList.add(serviceRequestGet.getMediaList().get(docCnt).toString());
		
				}
				for (int docCnt = 0; docCnt < serviceRequest.getServices().get(0).getMedia().size(); docCnt++) {
					documentList.add((String) serviceRequest.getServices().get(0).getMedia().get(docCnt).toString());
				}
				
				serviceRequest.getServices().get(0).setMediaList(documentList);
		
				serviceRequest.getAuditDetails().setCreatedBy(serviceRequestGet.getCreatedBy());
				
				deviceDetailSave(requestHeader,serviceRequest,service_request_id,serviceRequestGet);
				
				String servicetype = serviceRequestGet.getServiceType();
				String requestType = serviceRequest.getServices().get(0).getServiceType();
				
				// service request type
				String serviceRequestServiceType = serviceRequestGet.getServiceType().toUpperCase();
				String requestdataServiceType = serviceRequest.getServices().get(0).getServiceType().toUpperCase();
				serviceRequest.getServices().get(0).setTenantId(serviceRequestGet.getTenantId());
				serviceRequest.getServices().get(0).setService_request_uuid(serviceRequestGet.getService_request_uuid());
		
				// checking service request type
				
		
				log.info("Checking service requet type");
				if (serviceRequestServiceType.equals(requestdataServiceType)) {
							
					responseBody = updateServiceRequest(serviceRequest, service_request_id, requestHeader, role, status,
							service_request_uuid,servicetype);
					
					updateServiceRequestStatusEdit(serviceRequest, service_request_id, serviceRequestServiceType,role);
		
				} else {
					
					serviceRequest.getServices().get(0).setServiceType(requestdataServiceType);
		
					service_request_id_new = generateServiceRequestId(service_request_id);
		
					// add entry in service request table with service_request_id = service_request_id_old_1
					
					employeeInfo.setUserInfo(serviceRequest.getRequestInfo().getUserInfo());
					
					ServiceRequest serviceRequestdata = getUserInfo(serviceRequest);
		
					serviceRequestdata.getServices().get(0).setService_request_id_old(service_request_id);
					responseBody = serviceRequestEdit(serviceRequestdata, service_request_id_new, role, status,
							service_request_id,service_request_id_new,requestType);
					
					serviceRequest.setRequestInfo(employeeInfo);
		
					updateProcesinstancedata(serviceRequest,service_request_id,service_request_id_new,serviceRequestServiceType);
		
					log.info("updated service request");
					serviceRequest.getServices().get(0).setService_request_id(service_request_id_new);
				}
				serviceRequest.getServices().get(0).setService_request_id(service_request_id_new);
		

		return getServiceResponse(responseBody);
	}

	private void updateServiceRequestStatusEdit(ServiceRequest serviceRequest, String service_request_id,
			String serviceRequestServiceType, String role) throws JSONException {
		
		serviceRequest.getRequestInfo().setAction(HCConstants.EDIT);
		String comment = HCConstants.EDITED;
		serviceRequest.getServices().get(0).setService_request_id(service_request_id);
		serviceRequest.getServices().get(0).setServiceType(serviceRequestServiceType);
		serviceRequest.getServices().get(0).setComment(comment);
		serviceRequest.getServices().get(0).setAction(HCConstants.EDIT);

		log.info("Update processinstance data : "+serviceRequest);
		String userUuid=null;
		try 
        { 
            // checking valid integer using parseInt() method 
            Integer.parseInt(role); 
            
            userUuid=getEmployeeUuid(serviceRequest,role);
            System.out.println(role + " is a valid integer number"); 
        }  
        catch (NumberFormatException e)  
        { 
            System.out.println(role + " is not a valid integer number"); 
        } 

		if (hcConfiguration.getIsExternalWorkFlowEnabled()) {
			wfIntegrator.callWorkFlow(serviceRequest, service_request_id,role,userUuid);
		}

	}

	private String getEmployeeUuid(ServiceRequest request ,String role ) throws JSONException {
		
		//role ="194";
		String uuid = null;
		ArrayList list = new ArrayList<>();
		list.add(role);
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("RequestInfo", request.getRequestInfo());
		obj.put("id", list);
		obj.put("tenantId", request.getServices().get(0).getTenantId());
		
		String response = null;
		try {
			response = rest.postForObject(hcConfiguration.getUserBasePath().concat(hcConfiguration.getUserSearchEndPoint()), obj, String.class);
		} catch (HttpClientErrorException e) {
		}
		
			org.json.JSONObject userDetails = new org.json.JSONObject(
					response.toString());
			org.json.JSONArray responseObj = userDetails.getJSONArray("user");
			
		for (int userCnt = 0; userCnt < responseObj.length(); userCnt++) 
		{
			org.json.JSONObject userinfo = new org.json.JSONObject(
					responseObj.get(userCnt).toString());
			
			uuid = userinfo.getString("uuid");
			
		}
		return uuid;
	}


	private void updateProcesinstancedata(ServiceRequest serviceRequest, String service_request_id,
			String service_request_id_new, String serviceRequestServiceType) throws JSONException {
		
		// geting data from processinstance with service_request_id
		ServiceRequest procesinstancedata = getProcesinstanceData(serviceRequest, service_request_id_new,
				service_request_id,serviceRequestServiceType); 

		// service request id (old) this service request status update(mark as Rejected)
		updateStatus(procesinstancedata, service_request_id_new,
				service_request_id);
		
		updateServiceRequestStatus(procesinstancedata, service_request_id, serviceRequestServiceType);

	}

	private String generateServiceRequestId(String service_request_id) {
		
		// generate id Split old Service Request id and append proper version
		
		 String service_request_id_new = null;
		
		String[] splitServiceRequestId = service_request_id.split("_");
		int version = 1;

		if (null != splitServiceRequestId && splitServiceRequestId.length > 1) {
			version = Integer.parseInt(splitServiceRequestId[1]);
			version += 1;
		}
		String service_version = "_" + version;

		if (splitServiceRequestId != null) {
			  service_request_id_new = splitServiceRequestId[0].concat(service_version);
			log.info("Generate new service requet ID : " + service_request_id_new);
		}
		return service_request_id_new;
	}

	private void deviceDetailSave(String requestHeader, ServiceRequest serviceRequest, String service_request_id, ServiceRequestData serviceRequestGet) {
		// add data in device source detail
		log.info("Added data in deviceSource ");
		deviceSource.saveDeviceDetails(requestHeader, HCConstants.DEVICE_HORTICULTURE,
						serviceRequestGet.getTenantId(), HCConstants.MODULE_CODE, serviceRequest.getAuditDetails(),
						service_request_id);

		
	}

	private String getroleStatusId(ServiceRequestData serviceRequestGet) {
		
		String role = serviceRequestGet.getCurrent_assignee();
		String  status = serviceRequestGet.getService_request_status();
		String service_request_uuid = serviceRequestGet.getService_request_uuid();
		return role +"#"+ status +"#"+ service_request_uuid;
	}

	private ServiceRequestData getData(String service_request_id) {
		ServiceRequestData serviceRequestGet = null;
		RequestData requestdata = new RequestData();
		requestdata.setService_request_id(service_request_id);

		// get service_request data for this id

		JSONObject serviceRequestJson = serviceRepository.getServiceRequestData(requestdata);
		serviceRequestGet = parseServiceRequestData(serviceRequestJson);
		log.info("get deta : "+ serviceRequestGet);
		return serviceRequestGet;
	}

	private ServiceRequest getUserInfo(ServiceRequest serviceRequest) throws JSONException {
		User user = new User();
		
		RequestInfo requestinfo = new RequestInfo(); 
		requestinfo.setAction(serviceRequest.getRequestInfo().getAction());
		requestinfo.setApiId(serviceRequest.getRequestInfo().getApiId());
		requestinfo.setCorrelationId(serviceRequest.getRequestInfo().getCorrelationId());
		requestinfo.setDid(serviceRequest.getRequestInfo().getDid());
		requestinfo.setMsgId(serviceRequest.getRequestInfo().getMsgId());
		
		String tenantIdSplited = getTenantid(serviceRequest.getServices().get(0).getTenantId());
		
		ArrayList list = new ArrayList<>();
		list.add(serviceRequest.getAuditDetails().getCreatedBy());
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("RequestInfo", serviceRequest.getRequestInfo());
		obj.put("id", list);
		obj.put("tenantId", tenantIdSplited);
		
		String response = null;
		try {
			response = rest.postForObject(hcConfiguration.getUserBasePath().concat(hcConfiguration.getUserSearchEndPoint()), obj, String.class);
		} catch (HttpClientErrorException e) {
		}
		
			org.json.JSONObject userDetails = new org.json.JSONObject(
					response.toString());
			org.json.JSONArray responseObj = userDetails.getJSONArray("user");
		
		for (int userCnt = 0; userCnt < responseObj.length(); userCnt++) 
		{
			org.json.JSONObject userinfo = new org.json.JSONObject(
					responseObj.get(userCnt).toString());
			
			org.json.JSONArray wfRoles = userinfo.getJSONArray("roles");
			List<Role> citizenRoleList = new ArrayList();
			if (null != wfRoles) {
				org.json.JSONObject roleDetails = null;
				for (int roleCnt = 0; roleCnt < wfRoles.length(); roleCnt++) {
					roleDetails = new org.json.JSONObject(wfRoles.get(roleCnt).toString());
					Role role = new Role();
					role.setCode(roleDetails.getString("code"));
					role.setName(roleDetails.getString("name"));
					citizenRoleList.add(role);
				}
				}
			user.setRoles(citizenRoleList);
			String uuid = userinfo.getString("uuid");
			Long id = userinfo.getLong("id");
			String type = userinfo.getString("type");
			String userName = userinfo.getString("userName");
			String mobileNumber = userinfo.getString("mobileNumber");
			String name = userinfo.getString("name");
			String tenantId = userinfo.getString("tenantId");
			user.setUuid(uuid);
			user.setId(id);
			user.setMobileNumber(mobileNumber);
			user.setUserName(userName);
			user.setType(type);
			user.setName(name);
			user.setTenantId(tenantId);
		}

		requestinfo.setUserInfo(user);
		serviceRequest.setRequestInfo(requestinfo);
		return serviceRequest;
		
	}

	private String getTenantid(String tenantId) {
		
		String id = tenantId.replace(".", "#");
		
		 String  tenantIdSplit[] = null;
		 String tenantid = null;
		 
		 if(tenantId != null) 
		    {
			 tenantIdSplit = id.split("#");
			}
		 
		 if(tenantIdSplit != null) {	
			 tenantid= tenantIdSplit[0];
			}
		
		return tenantid;
	}


	private void updateServiceRequestStatus(ServiceRequest serviceRequest, String serviceRequestId,
			String serviceRequestTypeOld) throws JSONException {

		String comment = HCConstants.COMMENT;

		serviceRequest.getServices().get(0).setService_request_id(serviceRequestId);
		serviceRequest.getServices().get(0).setServiceType(serviceRequestTypeOld);
		serviceRequest.getServices().get(0).setComment(comment);
		serviceRequest.getServices().get(0).setAction(HCConstants.REJECT);
		

		log.info("Update processinstance data : "+serviceRequest);
		
		if (hcConfiguration.getIsExternalWorkFlowEnabled()) {
			wfIntegrator.callWorkFlow(serviceRequest, serviceRequestId,null,null);
		}

	}

	private ResponseEntity<ServiceRequest> updateServiceRequest(ServiceRequest request, String service_request_id,
			String requestHeader, String role, String status, String service_request_uuid, String type) {
		RequestInfoWrapper infoWrapper = new RequestInfoWrapper();
		try {

			if (null != request.getServices() && !request.getServices().isEmpty()) {
				
				List<String> documentList = new ArrayList<>();
				documentList.addAll(request.getServices().get(0).getMediaList());

				JSONObject documentDetailsJson = new JSONObject();
				JSONArray jsonArray = new JSONArray();

				for (String document : documentList) {

					JSONObject formDetailsJson = new JSONObject();
					formDetailsJson.put("media", document);

					jsonArray.add(formDetailsJson);
				}
				
				if(null != request.getServices().get(0).getSubType())
				{
					JSONObject subtype = new JSONObject();
					subtype.put("subservicetype", request.getServices().get(0).getSubType());
					request.getServices().get(0).setServiceRequestSubtype(subtype.toJSONString());
				}
				
				documentDetailsJson.put("document", jsonArray);
				request.getServices().get(0).setServiceMedia(documentDetailsJson.toJSONString());
				request.getServices().get(0).setService_request_id(service_request_id);
				request.getServices().get(0).setServiceType(type);
				request.getServices().get(0).setCurrent_assignee(role);
				request.getServices().get(0).setService_request_status(HCConstants.EDITED);
				request.getServices().get(0).setCreatedBy(request.getAuditDetails().getCreatedBy());
				request.getServices().get(0).setCreatedTime(request.getAuditDetails().getCreatedTime());
				request.getServices().get(0).setLastModifiedBy(request.getAuditDetails().getLastModifiedBy());
				request.getServices().get(0).setLastModifiedTime(request.getAuditDetails().getLastModifiedTime());
				List<ServiceRequestData> applicatinFormList = new ArrayList<>();
				applicatinFormList.add(request.getServices().get(0));

				List<ActionInfo> actionInfos = new ArrayList<>();
				final AuditDetails auditDetails = hCUtils
						.getAuditDetails(String.valueOf(request.getRequestInfo().getUserInfo().getId()), false);
				String by = auditDetails.getCreatedBy() + ":"
						+ request.getRequestInfo().getUserInfo().getRoles().get(0).getName();

				ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
						.action(HCConstants.ACTION_UPDATE).assignee(request.getServices().get(0).getCreatedBy()).by(by)
						.when(request.getServices().get(0).getCreatedTime())
						.tenantId(request.getServices().get(0).getTenantId()).status(HCConstants.ACTION_UPDATE).build();
				actionInfos.add(newActionInfo);

				request.setActionInfo(actionInfos);

				infoWrapper = RequestInfoWrapper.builder().services(applicatinFormList).actionInfo(actionInfos)
						.requestInfo(request.getRequestInfo()).requestBody(request.getServices().get(0)).build();
				log.info("Update service request :" +infoWrapper);
				hCProducer.push(hcConfiguration.getUpdateServiceRequestData(), infoWrapper);
				

			}
		} catch (Exception e) {
			throw new CustomException("COMMITTEE_EXCEPTION", e.getMessage());
		}

		return new ResponseEntity<>(ServiceRequest.builder()
				.responseInfo(ResponseInfo.builder().status("Success").build()).responseBody(infoWrapper).build(),
				HttpStatus.OK);

	}

	private String updateStatus(ServiceRequest serviceRequest, String service_request_id_new,
			String service_request_id) {

		final AuditDetails auditDetail = hCUtils.getAuditDetail(String.valueOf(serviceRequest.getRequestInfo().getUserInfo().getId()),
				false);
		
		String action = serviceRequest.getServices().get(0).getAction();
		ServiceRequestData updateRequest = new ServiceRequestData();
		
		updateRequest.setLastModifiedTime(auditDetail.getLastModifiedTime() - 10000);
		
		updateRequest.setService_request_status(HCConstants.REJECTED_STATUS);
		updateRequest.setService_request_id(service_request_id);
		updateRequest.setComment(HCConstants.COMMENT);
		updateRequest.setAction(action);
		updateRequest.setCurrent_assignee("");
		updateRequest.setTenantId(serviceRequest.getServices().get(0).getTenantId());
		updateRequest.setService_request_uuid(serviceRequest.getServices().get(0).getService_request_uuid());
		RequestInfoWrapper infowraperforupdate = RequestInfoWrapper.builder().requestBody(updateRequest).build();
		
		log.info("Update service request : " + infowraperforupdate);
		hCProducer.push(hcConfiguration.getUpdateStatusTopic(), infowraperforupdate);

		return null;
	}

	private ServiceRequest getProcesinstanceData(ServiceRequest serviceRequestGetData, String service_request_id_new,
			String service_request_id, String serviceRequestServiceType) {

		User employeeData = employeeStoredData(serviceRequestGetData);

		try {
			String procesinstanceData = getProcesinstanceDataFromprocesinstance(service_request_id,serviceRequestGetData);
			String bussinessServiceData = getbussinessServiceDatafromprocesinstanceEdit(serviceRequestGetData);
			String bussinessServiceDataOld = getbussinessServiceDataOldServiceType(serviceRequestGetData,serviceRequestServiceType);
		
			log.info("get data from procesinstance :" + procesinstanceData );
			serviceRequestGetData = procesinstanceBulkData(procesinstanceData,bussinessServiceData,serviceRequestGetData,service_request_id,service_request_id_new,bussinessServiceDataOld);
	
		} catch (HttpClientErrorException e) {
			log.info("Handled exception");
		}

		serviceRequestGetData.getRequestInfo().setUserInfo(employeeData);
		return serviceRequestGetData;
	}
	
	

	private String getbussinessServiceDataOldServiceType(ServiceRequest serviceRequestGetData,
			String serviceRequestServiceType) {
		
		String bussinessServiceData = null;
		{
			
			 bussinessServiceData = rest.postForObject(
					hcConfiguration.getWfHost().concat(hcConfiguration.getWfBusinessServiceSearchPath()).concat("?").concat(
							"tenantId=" + serviceRequestGetData.getServices().get(0).getCity() + "&businessServices=" + serviceRequestServiceType.toUpperCase()
							),
					serviceRequestGetData, String.class);
			
		}
		return bussinessServiceData;
	}


	public String getbussinessServiceDatafromprocesinstanceEdit(ServiceRequest serviceRequestGetData) {
		
		String bussinessServiceData = null;
		{
			
			 bussinessServiceData = rest.postForObject(
					hcConfiguration.getWfHost().concat(hcConfiguration.getWfBusinessServiceSearchPath()).concat("?").concat(
							"tenantId=" + serviceRequestGetData.getServices().get(0).getCity() + "&businessServices=" + serviceRequestGetData.getServices().get(0).getServiceType().toUpperCase()
							),
					serviceRequestGetData, String.class);
			
		}
		return bussinessServiceData;
	}

	

	private ServiceRequest procesinstanceBulkData(String procesinstanceData, String bussinessServiceData, ServiceRequest serviceRequestGetData, 
			String service_request_id,
			String service_request_id_new , String bussinessServiceDataOld) {
		

		String businessService = null;
		String nextStateAndSla =null;
		ArrayList <ProcessInstance> ProcessInstanceList = new ArrayList<>();
		
		try { 
			
			org.json.JSONObject obj = new org.json.JSONObject(procesinstanceData);
			org.json.JSONArray ProcessInstances = obj.getJSONArray("ProcessInstances");

			
			ServiceRequest serviceRequestData = serviceRequestGetData.clone();

			for (int i = ProcessInstances.length() - 1; i >= 0; i--) 
			{
			
				org.json.JSONObject ProcessInstancesDetails = new org.json.JSONObject(
						ProcessInstances.get(i).toString());

				org.json.JSONObject wfAssigner = ProcessInstancesDetails.getJSONObject("assigner");
			
				User wfUser = new User();
				wfUser = getUserdata(wfAssigner);
				
				List<Document> wfAddDocument = new ArrayList<>();
				wfAddDocument = getDocument(ProcessInstancesDetails);
				
				org.json.simple.JSONObject jsonObject = null;
				
				if (!ProcessInstancesDetails.isNull("additionalDetails"))
				{
				
					org.json.JSONObject additionalDetails = ProcessInstancesDetails.getJSONObject("additionalDetails");
	
					
					  JSONParser parser=new JSONParser(); 
	
					Object obj1 = parser.parse(additionalDetails.toString());
			        jsonObject=(org.json.simple.JSONObject)obj1;
				}
		
		        User asigneeUser = new User();
		        if (!ProcessInstancesDetails.isNull("assignee"))
		        {		    
		        
		        	org.json.JSONObject wfAssignee = ProcessInstancesDetails.getJSONObject("assignee");
		        	
		        	String assignee = wfAssignee.getString("uuid") ;  
		        	
		        	if(assignee !=null  )
		        		asigneeUser.setUuid(assignee);
		        }
		
				ServiceRequestData request = new ServiceRequestData();
				request = getProcessData(ProcessInstancesDetails);

			
				businessService = serviceRequestData.getServices().get(0).getServiceType();	
				
				if(request.getAction().equals(HCConstants.EDIT))
				{
					nextStateAndSla = getNextStateEditAction(request,bussinessServiceData);
				}
				else
				{
					nextStateAndSla = getNextState(bussinessServiceData,request);
				}
				
				List<ProcessInstance> processInstances = new ArrayList<>();
				ProcessInstance process = new ProcessInstance();
				process = setProcessInstanceData(wfUser,nextStateAndSla,wfAddDocument,request,serviceRequestGetData,businessService,service_request_id_new,bussinessServiceDataOld,jsonObject,asigneeUser);
				
				processInstances.add(process);
				ProcessInstanceList.addAll(processInstances);
				
			}
			
			saveBulkProcessInstance(ProcessInstanceList);
			
			serviceRequestGetData = setActionInfo(serviceRequestGetData,serviceRequestData);
	
		} catch (Exception ex) {
			
			System.out.println("  DATA PARSING EXCEPTION " + ex);

		}
		return serviceRequestGetData;
	}

	private String getNextStateEditAction(ServiceRequestData request, String bussinessServiceData) throws JSONException {
		String nextState = null;
		String newactions = null;
		String newState = null;
		long sla = 0;
		Boolean found = false;
		
		for (int businessCnt = 0; businessCnt <= bussinessServiceData.length(); businessCnt++) 
		{
			org.json.JSONObject bussinessServiceDetails = new org.json.JSONObject(
					bussinessServiceData.toString());

			org.json.JSONArray businessServicesObj = bussinessServiceDetails.getJSONArray("BusinessServices");
			
			for (int businessServiceCnt = 0; businessServiceCnt <= businessServicesObj.length(); businessServiceCnt++) 
			{
			
				org.json.JSONObject businessServicesSingleObj = new org.json.JSONObject(
						businessServicesObj.get(businessServiceCnt).toString());
				
				org.json.JSONArray stateObj = businessServicesSingleObj.getJSONArray("states");
				
				for (int stateCnt = 0; stateCnt <= stateObj.length(); stateCnt++) 
				{
					org.json.JSONObject stateSingleObj = new org.json.JSONObject(
							stateObj.get(stateCnt).toString());
					
					 String businessserviceState = stateSingleObj.getString("state");
					 sla = stateSingleObj.getLong("sla");
					 org.json.JSONArray actionsObj = stateSingleObj.getJSONArray("actions");
					
					for (int actionCnt = 0; actionCnt < actionsObj.length(); actionCnt++) 
					{
						org.json.JSONObject actionsSingleObj = new org.json.JSONObject(
								actionsObj.get(actionCnt).toString());
						
						newactions = actionsSingleObj.getString("action");
						newState = actionsSingleObj.getString("nextState");
						if(businessserviceState.isEmpty())
						{
							businessserviceState = HCConstants.INITIATED_STATUS;
						}

						if(newactions.equals(request.getAction()) && businessserviceState.equalsIgnoreCase(request.getState()))
						{
							  nextState = newState;
							  found = true;
							  
							  log.info("businessServicesObj" + actionsObj);
							  break;  
						}
						
						if(found) break;
					}	
					if(found) break;
				}	
				if(found) break;
			}
			if(found) break;	
		}
		return nextState + "#" + sla;
	}


	private String getbussinessServiceDatafromprocesinstance(ServiceRequest serviceRequestGetData) {
		
		String bussinessServiceData = rest.postForObject(
				hcConfiguration.getWfHost().concat(hcConfiguration.getWfBusinessServiceSearchPath()).concat("?").concat(
						"tenantId=" + serviceRequestGetData.getServices().get(0).getLocality() + "&businessServices=" + serviceRequestGetData.getServices().get(0).getServiceType().toUpperCase()
						),
				serviceRequestGetData, String.class);
		return bussinessServiceData;
	}

	private String getProcesinstanceDataFromprocesinstance(String service_request_id,
			ServiceRequest serviceRequestGetData) {
		

		String procesinstanceData = rest.postForObject(
				hcConfiguration.getWfHost().concat(hcConfiguration.getWfProcessSearch()).concat("?").concat(
						"businessIds=" + service_request_id + "&history=true&tenantId=" + serviceRequestGetData.getServices().get(0).getCity()
						),
				serviceRequestGetData, String.class);
		
		
		return procesinstanceData;
	}

	private ServiceRequest setActionInfo(ServiceRequest serviceRequestGetData, ServiceRequest serviceRequestData) {
		
		List<ActionInfo> actionInfos = new ArrayList<>();
		ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())
				.businessKey(serviceRequestData.getServices().get(0).getBusinessService())
				.action(serviceRequestData.getServices().get(0).getAction())

				.tenantId(serviceRequestData.getServices().get(0).getTenantId())
				.status(serviceRequestData.getServices().get(0).getAction()).build();
		actionInfos.add(newActionInfo);
		serviceRequestGetData.setActionInfo(actionInfos);
		return serviceRequestGetData;
	}

	private void saveBulkProcessInstance(ArrayList<ProcessInstance> processInstanceList) {

		ProcessInstanceRequest processInstanceRequest=new ProcessInstanceRequest();
		processInstanceRequest.setProcessInstances(processInstanceList);
		hCProducer.push(hcConfiguration.getSaveTransitionTopic(),processInstanceRequest);
		
	}

	private ProcessInstance setProcessInstanceData(User wfUser, String nextStateAndSla, List<Document> wfAddDocument,
			ServiceRequestData request, ServiceRequest serviceRequestGetData, String businessService,
			String service_request_id_new,String bussinessServiceDataOld, JSONObject jsonObject, User asigneeUser) throws JSONException {
		
		
		String  nextStateAndSlaSplit[] = null;
		if(nextStateAndSla != null) {
			nextStateAndSlaSplit = nextStateAndSla.split("#");
			}
		
		long getSla = getCalculatedSla(request.getBusinessservicesla(),serviceRequestGetData,bussinessServiceDataOld);
		
		long sla = Long.parseLong(nextStateAndSlaSplit[1]);
		ProcessInstance process = new ProcessInstance();
		User user = new User();
		user.setUuid(wfUser.getUuid());

		State state = new State();
		state.setUuid(nextStateAndSlaSplit[0]);
		
		String proccessId = UUID.randomUUID().toString();
		  
		 // state.setTenantId(tenentId);
		final AuditDetails auditDetail = hCUtils.getAuditDetail(String.valueOf(serviceRequestGetData.getRequestInfo().getUserInfo().getId()),
				false);
		List<Document> wfDocument = new ArrayList<>();
		  
		 for(Document wf: wfAddDocument)
		 {
			 String documentId = UUID.randomUUID().toString();
			 wf.setId(documentId);
			 wfDocument.add(wf);
			 wfDocument.get(0).setActive(true);
			 wfDocument.get(0).setAuditDetails(auditDetail);
		 }
		  

	        process.setId(proccessId);
			process.setTenantId(serviceRequestGetData.getServices().get(0).getCity());
			process.setBusinessService(businessService.toUpperCase());
			process.setBusinessId(service_request_id_new);
			process.setModuleName(HCConstants.MODULENAMEVALUE);
			process.setAction(request.getAction());
			process.setState(state);
			process.setComment(request.getComment());
			process.setAssigner(user);
			process.setBusinesssServiceSla(getSla);
			process.setStateSla(sla);
			process.setDocuments(wfDocument);
			process.setAuditDetails(request.getAuditDetails());
			process.setAssignee(asigneeUser);
		
			process.setAdditionalDetails(jsonObject);


		return process;
	}

	private long getCalculatedSla(long sla, ServiceRequest serviceRequestGetData, String bussinessServiceDataOld) throws JSONException {
		
		long timeInMiliSecondNewServiceType = 0;
		long timeInMiliSecondOldServiceType = 0;
		long finalSla = 0;
		
		String bussinessServiceData = getAllbussinessService(serviceRequestGetData);
		
		org.json.JSONObject bussinessServiceDetails = new org.json.JSONObject(
					bussinessServiceData.toString());
		org.json.JSONArray businessServicesObj = bussinessServiceDetails.getJSONArray("BusinessServices");
			
		org.json.JSONObject businessServicesSingleObj = new org.json.JSONObject(
						businessServicesObj.get(0).toString());
		timeInMiliSecondNewServiceType = businessServicesSingleObj.getLong("businessServiceSla");
		
		
		org.json.JSONObject bussinessServiceDetailsOld = new org.json.JSONObject(
				bussinessServiceDataOld.toString());
		org.json.JSONArray businessServicesObjOld = bussinessServiceDetailsOld.getJSONArray("BusinessServices");
		
		org.json.JSONObject businessServicesSingleObjOld = new org.json.JSONObject(
					businessServicesObjOld.get(0).toString());
		timeInMiliSecondOldServiceType = businessServicesSingleObjOld.getLong("businessServiceSla");
			
		// sla = total sla - old sla
		
		if (sla > 0)
		{
			if(timeInMiliSecondOldServiceType > timeInMiliSecondNewServiceType)
				finalSla = timeInMiliSecondNewServiceType -(timeInMiliSecondOldServiceType - sla) ;
			else
				finalSla = timeInMiliSecondNewServiceType -(timeInMiliSecondOldServiceType - sla) ;
		}
		else 
			
		{
			if(timeInMiliSecondOldServiceType > timeInMiliSecondNewServiceType)
			{
				long oldsla = timeInMiliSecondOldServiceType -(sla) ;
				finalSla = timeInMiliSecondNewServiceType -(oldsla);
			}
			else
				finalSla = timeInMiliSecondNewServiceType -(timeInMiliSecondOldServiceType - (sla));
		}
		
		return finalSla;
	}


	private String getAllbussinessService(ServiceRequest serviceRequestGetData) {
		
			
			String bussinessServiceData = null;
			{
				
				 bussinessServiceData = rest.postForObject(
						hcConfiguration.getWfHost().concat(hcConfiguration.getWfBusinessServiceSearchPath()).concat("?").concat(
								"tenantId=" + serviceRequestGetData.getServices().get(0).getTenantId() + "&businessServices=" + serviceRequestGetData.getServices().get(0).getServiceType().toUpperCase()
								),
						serviceRequestGetData, String.class);
				
			}
			return bussinessServiceData;
	}


	private String getNextState(String bussinessServiceData, ServiceRequestData request) throws JSONException {
		
		String nextState = null;
		String newactions = null;
		String newState = null;
		long sla = 0;
		Boolean found = false;
		
		for (int businessCnt = 0; businessCnt <= bussinessServiceData.length(); businessCnt++) 
		{
			org.json.JSONObject bussinessServiceDetails = new org.json.JSONObject(
					bussinessServiceData.toString());

			org.json.JSONArray businessServicesObj = bussinessServiceDetails.getJSONArray("BusinessServices");
			
			for (int businessServiceCnt = 0; businessServiceCnt <= businessServicesObj.length(); businessServiceCnt++) 
			{
			
				org.json.JSONObject businessServicesSingleObj = new org.json.JSONObject(
						businessServicesObj.get(businessServiceCnt).toString());
				
				org.json.JSONArray stateObj = businessServicesSingleObj.getJSONArray("states");
				
				for (int stateCnt = 0; stateCnt <= stateObj.length(); stateCnt++) 
				{
					org.json.JSONObject stateSingleObj = new org.json.JSONObject(
							stateObj.get(stateCnt).toString());
					
					 String businessserviceState = stateSingleObj.getString("state");
					 sla = stateSingleObj.getLong("sla");
					 org.json.JSONArray actionsObj = stateSingleObj.getJSONArray("actions");
					
					for (int actionCnt = 0; actionCnt < actionsObj.length(); actionCnt++) 
					{
						org.json.JSONObject actionsSingleObj = new org.json.JSONObject(
								actionsObj.get(actionCnt).toString());
						
						newactions = actionsSingleObj.getString("action");
						newState = actionsSingleObj.getString("nextState");
						
						
						if(newactions.equals(request.getAction()))
						{
							  nextState = newState;
							  found = true;
							  
							  log.info("businessServicesObj" + actionsObj);
							  break;  
						}
						
						if(found) break;
					}	
					if(found) break;
				}	
				if(found) break;
			}
			if(found) break;	
		}
		return nextState + "#" + sla;
	}

	private ServiceRequestData getProcessData(org.json.JSONObject processInstancesDetails) throws JSONException {
		
		ServiceRequestData data = new ServiceRequestData();
		
		if (!processInstancesDetails.isNull("tenantId"))
			
			data.setTenantId(processInstancesDetails.getString("tenantId"));
		else
			data.setTenantId("");
		if (!processInstancesDetails.isNull("action"))
			data.setAction(processInstancesDetails.getString("action"));
		else
			data.setAction("");

		if (!processInstancesDetails.isNull("comment"))
			data.setComment(processInstancesDetails.getString("comment"));
		else
			data.setComment("");
		
		org.json.JSONObject auditDetails = processInstancesDetails.getJSONObject("auditDetails");
		
		AuditDetails details = new AuditDetails();
		details.setCreatedBy(auditDetails.getString("createdBy"));
		details.setLastModifiedBy(auditDetails.getString("lastModifiedBy"));
		details.setCreatedTime(auditDetails.getLong("createdTime"));
		details.setLastModifiedTime(auditDetails.getLong("lastModifiedTime"));
		data.setAuditDetails(details);
		
		

		long businesssServiceSla = processInstancesDetails.getLong("businesssServiceSla");
		
		data.setBusinessservicesla(businesssServiceSla);
		
		org.json.JSONObject stateDetails = processInstancesDetails.getJSONObject("state"); 
		
		
		
		String state = stateDetails.getString("state");
		data.setState(state);
		
		return data;
	}

	private List<Document> getDocument(org.json.JSONObject processInstancesDetails) throws JSONException {
		
		List<Document> wfAddDocument = new ArrayList<>();
		
		if (!processInstancesDetails.isNull("documents")) {
			org.json.JSONArray wfDocuments = processInstancesDetails.getJSONArray("documents");
			if (null != wfDocuments) {
				for (int docCnt = 0; docCnt < wfDocuments.length(); docCnt++) {

					org.json.JSONObject docDetails = new org.json.JSONObject(
							wfDocuments.get(docCnt).toString());
					Document doc = new Document();
					doc.setId(docDetails.getString("id"));

					if (!docDetails.isNull("documentType"))
						doc.setDocumentType(docDetails.getString("documentType"));
					else
						doc.setDocumentType("");
					doc.setFileStoreId(docDetails.getString("fileStoreId"));
					doc.setTenantId(docDetails.getString("tenantId"));

					wfAddDocument.add(doc);

				}
			}
		}
		return wfAddDocument;
	}

	private User getUserdata(org.json.JSONObject wfAssigner) throws JSONException {
		User wfUser = new User();
		if (null != wfAssigner.getString("mobileNumber"))
			wfUser.setMobileNumber(wfAssigner.getString("mobileNumber"));
		else
			wfUser.setMobileNumber("");
		if (null != wfAssigner.getString("name"))
			wfUser.setName(wfAssigner.getString("name"));
		else
			wfUser.setName("");
		if (null != wfAssigner.getString("tenantId"))
			wfUser.setTenantId(wfAssigner.getString("tenantId"));
		else
			wfUser.setTenantId("");
		if (null != wfAssigner.getString("type"))
			wfUser.setType(wfAssigner.getString("type"));
		else
			wfUser.setType("");
		if (!wfAssigner.isNull("emailId"))
			wfUser.setEmailId(wfAssigner.getString("emailId").toString());
		else
			wfUser.setEmailId("");
		wfUser.setUuid(wfAssigner.getString("uuid"));
		long id = wfAssigner.getLong("id");
		wfUser.setId(id);
		if (!wfAssigner.isNull("userName"))
			wfUser.setUserName(wfAssigner.getString("userName"));
		else
			wfUser.setUserName("");

		org.json.JSONArray wfRoles = wfAssigner.getJSONArray("roles");
		List<Role> roleList = new ArrayList();
		if (null != wfRoles) {
			org.json.JSONObject roleDetails = null;
			for (int roleCnt = 0; roleCnt < wfRoles.length(); roleCnt++) {
				roleDetails = new org.json.JSONObject(wfRoles.get(roleCnt).toString());
				Role role = new Role();
				role.setCode(roleDetails.getString("code"));
				role.setName(roleDetails.getString("name"));
				roleList.add(role);

			}
			wfUser.setRoles(roleList);
			

		}
		return wfUser;
	}

	private User employeeStoredData(ServiceRequest serviceRequestGetData) {

		User userData = new User();
		userData.setType(serviceRequestGetData.getRequestInfo().getUserInfo().getType());
		userData.setEmailId(serviceRequestGetData.getRequestInfo().getUserInfo().getEmailId());
		userData.setName(serviceRequestGetData.getRequestInfo().getUserInfo().getName());
		userData.setId(serviceRequestGetData.getRequestInfo().getUserInfo().getId());
		userData.setUuid(serviceRequestGetData.getRequestInfo().getUserInfo().getUuid());
		userData.setRoles(serviceRequestGetData.getRequestInfo().getUserInfo().getRoles());
		userData.setMobileNumber(serviceRequestGetData.getRequestInfo().getUserInfo().getMobileNumber());
		userData.setUserName(serviceRequestGetData.getRequestInfo().getUserInfo().getUserName());
		userData.setTenantId(serviceRequestGetData.getRequestInfo().getUserInfo().getTenantId());
		return userData;
	}

	public ServiceResponse scheduler(ServiceRequest requestInfo, String tenantId) {
		
		log.info("Scheduler started ......");
		
		

		String serviceRequestId = null;
		//Long serviceRequestDateEpoc = null;
				
	//get data from service request
 		List <ServiceRequestData> serviceRequestList = getServiceRequest();
 		
 		//generate current date
 	//	String currentDate = getCurrentDateTimeMS();
 		//long currentDateepoch=0 ; 
 		try
		{ 
		
		/*SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		Date date = df.parse(currentDate);
		currentDateepoch = date.getTime();	*/
			
		//log.info("service current date in mili  :  "+currentDate);
		//log.info("currentDate in Epoc convert  "+ currentDateepoch);
		}
 		catch(Exception ex)
 		{
 			
 		}
		
 		if(null !=serviceRequestList || serviceRequestList.size()>0)
 		{
 		for(ServiceRequestData request: serviceRequestList)
 		{
 			String role = null;
			role = request.getCurrent_assignee();
			serviceRequestId = request.getService_request_id();
			//serviceRequestDateEpoc = request.getCreatedTime();
			
			log.info("Role :" +role + " And  serviceRequestId "+ serviceRequestId);

	//convert in epoc
		try
		{	
		
   //old date - current date		
		  /*long dateDifference = (serviceRequestDateEpoc - currentDateepoch); 
			  
		  log.info("Date difference  :  "+ dateDifference);*/

   // taking days from bussines service
		 String  processInstanceSplit[] = null;
		 long businessSla = 0;
		
		String processIntanceData = getBussinessServiceSla(serviceRequestId,tenantId);
		if(processIntanceData != null) {
		 processInstanceSplit =processIntanceData.split("#");
		}
		if(processInstanceSplit != null) {	
		 businessSla= Long.parseLong(processInstanceSplit[1]);
		}
		 
		//String processInstanceData = getProcessInstanceData(request,serviceRequestId,tenantId);
		
		//String serviceRequestType = getServiceRequestType(processInstanceData);
	//check days greter or less than

		if( businessSla >= 0 )
		{
			//geting role details
			
			int days = getDays(businessSla);
			
			log.info("REMINDER days remaining is : "+ days );
			
			if(days < hcConfiguration.getSlaDays())
				sendReminderOverdueSlaNotification(role,serviceRequestId,HCConstants.REMINDER,request.getService_request_date(),tenantId,request.getServiceType(),days);
			else
				continue;
			
			
		}
		
		else  if(businessSla < 0 ) {
			
			int days = getDays(businessSla);
			
			log.info("OVERDAYS are : "+ days );
			if(days<0)
			{
				days =-(days);
			}
			
			sendReminderOverdueSlaNotification(role,serviceRequestId,HCConstants.OVERDAYS,request.getService_request_date(),tenantId,request.getServiceType(),days);
			
		}
		
		}
		catch(Exception ex)
		{
			
		}
		
 		}
 		}
		
			
		return null;
	}
	
	private String getServiceRequestType(String processInstanceData) throws JSONException {
		
		org.json.JSONObject obj = new org.json.JSONObject(processInstanceData);
		org.json.JSONArray ProcessInstances = obj.getJSONArray("ProcessInstances");

			org.json.JSONObject ProcessInstancesDetails = new org.json.JSONObject(
					ProcessInstances.get(ProcessInstances.length() - 1).toString());
			
			String serviceType = ProcessInstancesDetails.getString("businessService");
			System.out.println(ProcessInstancesDetails);
			

		return serviceType;
	}


	private String getProcessInstanceData(ServiceRequestData request,String service_request_id,String tenantId) {
		String procesinstanceData = null;
		String payloadData="{\"RequestInfo\": {\"userInfo\": {\"roles\": [{ \"code\": \"EE\",\"name\": \"EE\",\"tenantId\": \"ch.chandigarh\"}]}}}";
   try {
		 procesinstanceData = sendPostRequest(hcConfiguration.getWfHost().concat(hcConfiguration.getWfProcessSearch()).concat("?").concat("businessIds="+service_request_id+"&history="+HCConstants.TRUE+"&tenantId="+tenantId), payloadData);

         }
    catch (Exception e) {
	     
                }

		return procesinstanceData;
		
	}


	private int getDays(long businessSla) {
		
		int days = 0;
		
		if(businessSla > 0)
		{
			    double doubleDays =  (double)businessSla/(double)(1000 * 60 * 60 * 24); 		
		        days = (int)Math.ceil(doubleDays); 
		}
		else
		{
			businessSla = -businessSla;
			double doubleDays =  (double)businessSla/(double)(1000 * 60 * 60 * 24); 		
		    days = (int)Math.ceil(doubleDays); 
			days = -days;
		}
		
		return days;
	}

		//Fetch all the service request whos status is not COmpleted and Rejected
		private List<ServiceRequestData>  getServiceRequest() {
			
			
			List<ServiceRequestData> ServiceRequestList= new ArrayList<>() ;
			
			ServiceRequestData serviceRequestDetails = null;
	    	JSONArray  serviceRequestArray = serviceRepository.getServiceRequestList();
	    	
	    	
	    	//JSONObject serviceDetailjsonObject = (JSONObject) actualResult.get(0);
	    	for (int cnt = 0; cnt < serviceRequestArray.size(); cnt++) {
	    		
	    		//createdtime,service_request_id,current_assignee
	    	JSONObject serviceDetailjsonObject = (JSONObject) serviceRequestArray.get(cnt);
	    		
			serviceRequestDetails= parseSchedulerServiceRequestData(serviceDetailjsonObject);
			ServiceRequestList.add(serviceRequestDetails);
	    	}
			
			return ServiceRequestList;
		}
		
		public static String getCurrentDateTimeMS() {
	        Date dNow = new Date();
	        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd");
	        String datetime = ft.format(dNow);
	        return datetime;
	    }
		
		
		
		private String getBussinessServiceSla(String serviceRequestId, String tenantId) {
			
			long sla = 0;
			String processIntanceData = null;
			String payloadData="{\"RequestInfo\": {\"userInfo\": {\"roles\": [{ \"code\": \"EE\",\"name\": \"EE\",\"tenantId\": \"ch.chandigarh\"}]}}}";
	   try {
			String procesinstanceData = sendPostRequest(hcConfiguration.getWfHost().concat(hcConfiguration.getWfProcessSearch()).concat("?").concat("businessIds="+serviceRequestId+"&history="+HCConstants.TRUE+"&tenantId="+tenantId), payloadData);
			
				org.json.JSONObject obj = new org.json.JSONObject(procesinstanceData);
				org.json.JSONArray ProcessInstances  = obj.getJSONArray("ProcessInstances");
		        
				
		        org.json.JSONObject ProcessInstancesDetails = new org.json.JSONObject(ProcessInstances.get(0).toString());
		        sla = ProcessInstancesDetails.getLong("businesssServiceSla");
		        
		        processIntanceData = tenantId+"#"+sla;
		        //System.out.println(processIntanceData);
		        
	         }
	    catch (Exception e) {
		     
	                }

			return processIntanceData;
			
		}
		
		private List<String> sendReminderOverdueSlaNotification(String role,String service_request_id,String action,String serviceRequestDate,String tenantId,String serviceType,int days) {
			
			List requestInfoList = new ArrayList();
			String mobileNumber = null;
			String uuid = null;
			String emailId = null;
			String userName =null;
			String tenantid = null;
			String type=null;
			
			
			List Actioninfolist= new ArrayList();
	 		RequestInfoWrapper infoWrapper = null;
			String employeeDetails = null;

		    RequestInfo requestInfo = new RequestInfo();
		    
		    String requestData = null;
		    boolean digit =false;
		    
		     digit = containsDigit(role);
			
			if (digit)
			{
				
				String employeeDetailsRetrived = null;
				
				String data = null;
				
	          //  new BigInteger(role); 
	            System.out.println("Sending notification to " + role + " this Single Employee"); 
	            
	            employeeDetailsRetrived = notificationService.getMobileAndIdForNotificationService(requestInfo,
	            		tenantId, role, HCConstants.ROLE_EMPLOYEE);
				data = employeeDetailsRetrived.replace("|", "#");
			
				ServiceRequestData serviceRequest = new ServiceRequestData();
				
				serviceRequest.setOwnerName(data.split("#")[1]);
	        	serviceRequest.setEmail(data.split("#")[2]);
	        	serviceRequest.setContactNumber(data.split("#")[0]);
	        	serviceRequest.setService_request_id(service_request_id);
	        	serviceRequest.setServiceType(serviceType);
	        	String by = serviceRequest.getCreatedBy() ;
	        	RequestInfo requestInfoDetails =new RequestInfo();
				
				 ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())

		        			.action(action)
		        			.assignee(uuid)
		        			.by(by)
							.when(serviceRequest.getCreatedTime())
		        			.tenantId(tenantId)
		        			.status(action).build();
		          Actioninfolist.add(newActionInfo);
		          
		          
		      	List<ServiceRequestData> serviceRequestList = new ArrayList<>();
		    	serviceRequestList.add(serviceRequest);	
		    	
//		    	
		    	infoWrapper = RequestInfoWrapper.builder().services(serviceRequestList)
						.actionInfo(Actioninfolist).requestInfo(requestInfoDetails).requestBody(serviceRequest).build();
						
		    	
		    	ServiceRequest serviceRequestobj= new ServiceRequest();
		    	serviceRequestobj.setServices(serviceRequestList);
		    	serviceRequestobj.setActionInfo(Actioninfolist);
		    	serviceRequestobj.setRequestInfo(requestInfoDetails);
		    	notificationConsumer.sendSchedulerNotification(serviceRequestobj,action,serviceRequestDate,tenantId,days);
				
			}
			else
			{
				System.out.println(" Sending notification to " + role +" all Employee"); 
			    log.info("Get data from Hrms with related role");
			    String payload="{ \"RequestInfo\": {\"USER_INFO\": {\"tenantId \": \"ch\" } }}";

				try {
					
				
					 employeeDetails= sendPostRequest(hcConfiguration.getEgovHRMShost().concat(hcConfiguration.getEgovHRMSSearchEndpoint()).concat("?").concat("roles="+role+"&tenantId="+tenantId),payload);

					try {
						if(null !=employeeDetails)
						{
							
					org.json.JSONObject obj = new org.json.JSONObject(employeeDetails);
				
					org.json.JSONArray employeesList  = obj.getJSONArray("Employees");

				
					
					ServiceRequestData serviceRequest = new ServiceRequestData();
				    
			        for (int i = 0; i < employeesList.length(); i++) {
			        	Actioninfolist= new ArrayList();

			            org.json.JSONObject empDetails = new org.json.JSONObject(employeesList.get(i).toString());
			        	
			            org.json.JSONObject user = empDetails.getJSONObject("user");
			        	mobileNumber = user.getString("mobileNumber");

			        	uuid = user.getString("uuid");
			        	tenantid = user.getString("tenantId");
			        	
			        	type = user.getString("type");
			        	if(!user.isNull("emailId")) {
			        		emailId = user.getString("emailId").toString();
			        	}
			        	else {
			        		emailId = "";
			        		}
			        	long id= user.getLong("id");
			        		

			        	userName = user.getString("userName");
			        	List<Role> roleList= new ArrayList();
			        	org.json.JSONArray Roles  = user.getJSONArray("roles");
			        	if(null != Roles)
			        	{
			        		org.json.JSONObject docDetails=null;
			        	for(int roleCnt=0;roleCnt<Roles.length();roleCnt++)
			        	{
			        	    docDetails = new org.json.JSONObject(Roles.get(roleCnt).toString());
			        		Role getrole= new Role();
			        		getrole.setCode(docDetails.getString("code"));
			        		getrole.setName(docDetails.getString("name"));
			        		roleList.add(getrole);

			        	}
			        	}

			        	serviceRequest.setOwnerName(userName);
			        	serviceRequest.setEmail(emailId);
			        	serviceRequest.setContactNumber(mobileNumber);
			        	serviceRequest.setService_request_uuid(uuid);
			        	serviceRequest.setService_request_id(service_request_id);
			        	serviceRequest.setServiceType(serviceType);
			        	String by = serviceRequest.getCreatedBy() ;
			        	RequestInfo requestInfoDetails =new RequestInfo();
			        	User userInfoDetails = new User();
			        	userInfoDetails.setUuid(uuid);
			        	userInfoDetails.setUserName(userName);
			        	userInfoDetails.setEmailId(emailId);
			        	userInfoDetails.setMobileNumber(mobileNumber);
			        	userInfoDetails.setRoles(roleList);
			        	userInfoDetails.setId(id);
			        	userInfoDetails.setTenantId(tenantid);
			        	userInfoDetails.setType(type);
			        	requestInfoDetails.setUserInfo(userInfoDetails);
			       
			          ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())

			        			.action(action)
			        			.assignee(uuid)
			        			.by(by)
								.when(serviceRequest.getCreatedTime())
			        			.tenantId(tenantId)
			        			.status(action).build();
			          Actioninfolist.add(newActionInfo);
			          
			          
			      	List<ServiceRequestData> serviceRequestList = new ArrayList<>();
			    	serviceRequestList.add(serviceRequest);	
			    	
//			    	
			    	infoWrapper = RequestInfoWrapper.builder().services(serviceRequestList)
							.actionInfo(Actioninfolist).requestInfo(requestInfoDetails).requestBody(serviceRequest).build();
							
			    	
			    	ServiceRequest serviceRequestobj= new ServiceRequest();
			    	serviceRequestobj.setServices(serviceRequestList);
			    	serviceRequestobj.setActionInfo(Actioninfolist);
			    	serviceRequestobj.setRequestInfo(requestInfoDetails);

			    	notificationConsumer.sendSchedulerNotification(serviceRequestobj,action,serviceRequestDate,tenantId,days);

			        }
			        }
			    
					}
					
					
			        catch( Exception ex) {
			        	
			        }
				
				} catch (HttpClientErrorException ex) {
					System.out.print("Handled exception");
				}
			}
	        
				return requestInfoList;
			}
		
		public final boolean containsDigit(String s) {
		    boolean containsDigit = false;

		    if (s != null && !s.isEmpty()) {
		        for (char c : s.toCharArray()) {
		            if (containsDigit = Character.isDigit(c)) {
		                break;
		            }
		        }
		    }

		    return containsDigit;
		}
		
		private  ServiceRequestData parseSchedulerServiceRequestData(JSONObject serviceRequest)
	    {
			 ServiceRequestData serviceRequestData = new ServiceRequestData();

	        //Get service request object within list

		 try
		    {
			 
			 String service_request_id = (String) serviceRequest.get("service_request_id");   
		        serviceRequestData.setService_request_id(service_request_id);
				 
		   
		     Long createdtime=Long.parseLong( (String)serviceRequest.get("createdtime"));
		     
	       serviceRequestData.setCreatedTime(createdtime);
	       
	        String current_assignee = (String) serviceRequest.get("current_assignee");   
	        serviceRequestData.setCurrent_assignee(current_assignee);
	        

	        String serviceRequestDate=(String) serviceRequest.get("servicerequestdate"); 
	        serviceRequestData.setService_request_date(serviceRequestDate);
	        
	        String serviceType=(String) serviceRequest.get("service_type"); 
	        serviceRequestData.setServiceType(serviceType);
	   

		        }
			 catch(Exception ex)
			 {
			 
			 }
	        
			 return serviceRequestData;
	    }
		
		private ResponseEntity<ServiceRequest> serviceRequestEdit(ServiceRequest request, String service_request_id,
				 String role, String status, String history_service_request_id, String service_request_id_new_gen, String requestType) {

			String action = HCConstants.ACTION_UPDATE;
			
			RequestInfoWrapper infoWrapper = new RequestInfoWrapper();
			try {

				if (null != request.getServices() && !request.getServices().isEmpty()) {
				
					String service_request_uuid = UUID.randomUUID().toString();


					List<String> documentList = new ArrayList<>();

					if (request.getServices().get(0).getIsEditState() == 1) {
						documentList.addAll(request.getServices().get(0).getMediaList());

						JSONObject documentDetailsJson = new JSONObject();
						JSONArray jsonArray = new JSONArray();

						for (String document : documentList) {

							JSONObject formDetailsJson = new JSONObject();
							formDetailsJson.put("media", document);

							jsonArray.add(formDetailsJson);
						}
						documentDetailsJson.put("document", jsonArray);

						request.getServices().get(0).setServiceMedia(documentDetailsJson.toJSONString());
						request.getServices().get(0).setService_request_id(service_request_id);
						request.getServices().get(0).setHistory_service_request_id(history_service_request_id);

					} else {

						documentList.addAll(request.getServices().get(0).getMedia());

						JSONObject documentDetailsJson = new JSONObject();
						JSONArray jsonArray = new JSONArray();

						for (String document : documentList) {

							JSONObject formDetailsJson = new JSONObject();
							formDetailsJson.put("media", document);

							jsonArray.add(formDetailsJson);
						}
						documentDetailsJson.put("document", jsonArray); 

						request.getServices().get(0).setServiceMedia(documentDetailsJson.toJSONString());

					}
					String langData = request.getRequestInfo().getMsgId().replace("|", "#");
					String langDataSplit[] = langData.split("#");
					
					if(null != request.getServices().get(0).getSubType())
					{
						JSONObject subtype = new JSONObject();
						subtype.put("subservicetype", request.getServices().get(0).getSubType());
						request.getServices().get(0).setServiceRequestSubtype(subtype.toJSONString());
					}

					request.getServices().get(0).setServicerequest_lang(langDataSplit[1]);

					request.getServices().get(0).setServiceType(requestType);
					request.getServices().get(0).setService_request_uuid(service_request_uuid);
					request.getServices().get(0).setCurrent_assignee(role);
					request.getServices().get(0).setService_request_status(status);
					request.getServices().get(0).setCreatedBy(request.getAuditDetails().getCreatedBy());
					request.getServices().get(0).setCreatedTime(request.getAuditDetails().getCreatedTime());
					request.getServices().get(0).setLastModifiedBy(request.getAuditDetails().getLastModifiedBy());
					request.getServices().get(0).setLastModifiedTime(request.getAuditDetails().getLastModifiedTime());

					List<ServiceRequestData> applicatinFormList = new ArrayList<>();
					applicatinFormList.add(request.getServices().get(0));

					List<ActionInfo> actionInfos = new ArrayList<>();

					final AuditDetails auditDetails = hCUtils
							.getAuditDetails(String.valueOf(request.getRequestInfo().getUserInfo().getId()), false);
					String by = auditDetails.getCreatedBy() + ":"
							+ request.getRequestInfo().getUserInfo().getRoles().get(0).getName();

					ActionInfo newActionInfo = ActionInfo.builder().uuid(UUID.randomUUID().toString())

							.action(action).assignee(request.getServices().get(0).getCreatedBy())
							.by(by).when(request.getServices().get(0).getCreatedTime())
							.tenantId(request.getServices().get(0).getTenantId()).status(status)
							.build();
					actionInfos.add(newActionInfo);

					infoWrapper = RequestInfoWrapper.builder().services(applicatinFormList).actionInfo(actionInfos)
							.requestInfo(request.getRequestInfo()).requestBody(request.getServices().get(0)).build();
					
					log.info("Service request create : " + infoWrapper);

					hCProducer.push(hcConfiguration.getSaveTopic(), infoWrapper);

				}
			} catch (Exception e) {
				throw new CustomException("COMMITTEE_EXCEPTION", e.getMessage());
			}

			return new ResponseEntity<>(ServiceRequest.builder()
					.responseInfo(ResponseInfo.builder().status("Success").build()).responseBody(infoWrapper).build(),
					HttpStatus.OK);

		}

}
