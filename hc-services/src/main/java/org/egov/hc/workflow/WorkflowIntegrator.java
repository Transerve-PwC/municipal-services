package org.egov.hc.workflow;


import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.User;
import org.egov.hc.contract.ServiceRequest;
import org.egov.hc.model.ServiceRequestData;
import org.egov.hc.producer.HCConfiguration;
import org.egov.hc.utils.HCConstants;
import org.egov.tracer.model.CustomException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class WorkflowIntegrator {

	private static final String TENANTIDKEY = "tenantId";

	private static final String BUSINESSSERVICEKEY = "businessService";

	private static final String ACTIONKEY = "action";

	private static final String COMMENTKEY = "comment";

	private static final String MODULENAMEKEY = "moduleName";

	private static final String BUSINESSIDKEY = "businessId";

	private static final String DOCUMENTSKEY = "documents";

	private static final String ASSIGNEEKEY = "assignee";

	private static final String MODULENAMEVALUE = "HORTICULTURE";

	private static final String WORKFLOWREQUESTARRAYKEY = "ProcessInstances";

	private static final String REQUESTINFOKEY = "RequestInfo";

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";

	private static final String STATUSJSONKEY = "$.state.applicationStatus";
	
	private static final String ADITIONALDETAILSKEY = "additionalDetails";

	@Autowired
	private RestTemplate rest;
	
	@Autowired
	private HCConfiguration config;
	
	@Autowired
	private HCConfiguration hcConfiguration;  	

	@Autowired
	public WorkflowIntegrator(RestTemplate rest, HCConfiguration config) {
		this.rest = rest;
		this.config = config;
	}

	/**
	 * Method to integrate with workflow
	 *
	 * takes the service request request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to horticulture object
	 *
	 * @param ServiceRequest
	 * @throws JSONException 
	 */
	public  boolean callWorkFlow(ServiceRequest request, String service_request_id,String role, String employeeUuid) throws JSONException {
		boolean status = false;
		
		if(!request.getServices().isEmpty())
		{

		String wfTenantId = request.getServices().get(0).getCity();
		JSONArray array = new JSONArray();
		for (ServiceRequestData servicerequestdata : request.getServices()) {
				JSONObject obj = new JSONObject();

				List<Document> wfDocument = new ArrayList<>();
				
		
				
				if(servicerequestdata.getAction().equals(HCConstants.INITIATE)  ) 
				{
					
					if(servicerequestdata.getIsEditState()==0)
					{
						if( !request.getServices().get(0).getMedia().isEmpty())
							for (int index = 0; index < servicerequestdata.getMedia().size(); index++) {
						
							Document document= new Document();
						
							document.setId(UUID.randomUUID().toString());
							document.setFileStoreId(servicerequestdata.getMedia().get(index).toString());
							document.setActive(true);
							
							if(index==0)
								document.setDocumentType("ID_Proof");
								
							else
								document.setDocumentType(servicerequestdata.getDocumentType());
							
							document.setTenantId(wfTenantId);
							wfDocument.add(document);  				    
				 
							}
						}
					else
					  {
						  if(null !=request.getServices().get(0).getWfDocuments() && !request.getServices().get(0).getWfDocuments().isEmpty())
							{
							for (int index = 0; index < servicerequestdata.getWfDocuments().size(); index++) {
								
								Document document= new Document();
							
								document.setId(UUID.randomUUID().toString());
								document.setFileStoreId(servicerequestdata.getWfDocuments().get(index).getFileStoreId().toString());
								document.setActive(true);
								document.setDocumentType(servicerequestdata.getWfDocuments().get(index).getDocumentType());
								document.setTenantId(wfTenantId);
								wfDocument.add(document);  
							 }
							}
					  }
				}
				
				else
				{
					if(null !=request.getServices().get(0).getWfDocuments() && !request.getServices().get(0).getWfDocuments().isEmpty())
						{
						for (int index = 0; index < servicerequestdata.getWfDocuments().size(); index++) {
							
							Document document= new Document();
						
							document.setId(UUID.randomUUID().toString());
							document.setFileStoreId(servicerequestdata.getWfDocuments().get(index).getFileStoreId().toString());
							document.setActive(true);
							document.setDocumentType(servicerequestdata.getWfDocuments().get(index).getDocumentType());
							document.setTenantId(wfTenantId);
							wfDocument.add(document);  
					}
						}
				}
				
				//obj.put("businesssServiceSla", servicerequestdata.getBusinessservicesla());

				obj.put(DOCUMENTSKEY, wfDocument);
				obj.put(BUSINESSIDKEY, service_request_id);
				obj.put(TENANTIDKEY, wfTenantId);	
				
				String businesskey= servicerequestdata.getServiceType().toUpperCase().toString();
				obj.put(BUSINESSSERVICEKEY, businesskey );  
				
		
				obj.put(MODULENAMEKEY, MODULENAMEVALUE);
				obj.put(ACTIONKEY, servicerequestdata.getAction());		
				obj.put(COMMENTKEY, servicerequestdata.getComment());

				User user = new User();
				if(null != employeeUuid)
				{	
					user.setUuid(employeeUuid);
					obj.put(ASSIGNEEKEY, user);
				}
				else
				{
			
				JSONObject roleJson = new JSONObject();

				roleJson.put("role", role);

				obj.put(ADITIONALDETAILSKEY, roleJson);

				}
				array.add(obj);
			
		}
		if(!array.isEmpty())
		{
			
			JSONObject workFlowRequest = new JSONObject();
			workFlowRequest.put(REQUESTINFOKEY, request.getRequestInfo());
			workFlowRequest.put(WORKFLOWREQUESTARRAYKEY, array);
			String response = null;
			try {
				response = rest.postForObject(config.getWfHost().concat(config.getWfTransitionPath()), workFlowRequest, String.class);
			} catch (HttpClientErrorException e) {
				
				/*
				 * extracting message from client error exception
				 */
				
				DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
				List<Object> errros = null;
				try {
					errros = responseContext.read("$.Errors");
				} catch (PathNotFoundException pnfe) {
					log.error("EG_HC_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
					throw new CustomException("EG_HC_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
				}
				throw new CustomException("EG_WF_ERROR", errros.toString());
			} catch (Exception e) {
				throw new CustomException("EG_WF_ERROR",
						" Exception occured while integrating with workflow : " + e.getMessage());
			}

			/*
			 * on success result from work-flow read the data and set the status back to HC
			 * object
			 */
			DocumentContext responseContext = JsonPath.parse(response);
			
			List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
			Map<String, String> idStatusMap = new HashMap<>();
			responseArray.forEach(
					object -> {

						DocumentContext instanceContext = JsonPath.parse(object);
						idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
					});
			
			org.json.JSONObject responseDetails = new org.json.JSONObject(response.toString());
			org.json.JSONObject responseObj = responseDetails.getJSONObject("ResponseInfo");
			String flagStatus = responseObj.getString("status");
			if(flagStatus.equals(HCConstants.SUCCESSFUL))
			{
				status = true;
			}
		

			request.getServices()
					.forEach(hcObj -> hcObj.setService_request_status(idStatusMap.get(hcObj.getService_request_id())));


		}
	}
		return status;
		
	}
	

	public String parseBussinessServiceData(String bussinessServiceData, ServiceRequest request) throws JSONException {
		
		String newactions = null;
		String newState = null;
		
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
						newState = actionsSingleObj.getString("nextState");

						if(newactions.equals(request.getServices().get(0).getAction()))
						{
							for (int stateCnt1 = 0; stateCnt1 <= stateObj.length(); stateCnt1++) 
							{
								
								org.json.JSONObject stateSingleObj1 = new org.json.JSONObject(
										stateObj.get(stateCnt1).toString());
								
								String uuid = stateSingleObj1.getString("uuid");
								
								if(newState.equals(uuid))
								{
								
									org.json.JSONArray actionsObj1 = stateSingleObj1.getJSONArray("actions");
									
									for (int actionCnt1 = 0; actionCnt1 <= actionsObj1.length(); actionCnt1++) 
									{
										org.json.JSONObject actionsSingleObj1 = new org.json.JSONObject(
												actionsObj1.get(actionCnt1).toString());
										
										org.json.JSONArray roleObj = actionsSingleObj1.getJSONArray("roles");
										
										
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
										///if(found) break;
									}
									if(found) break;
								}
								if(found) break;
							}
							if(found) break; 
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

	
}