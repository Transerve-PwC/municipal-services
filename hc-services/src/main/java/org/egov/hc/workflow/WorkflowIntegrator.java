package org.egov.hc.workflow;


import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.hc.contract.ServiceRequest;
import org.egov.hc.model.ServiceRequestData;
import org.egov.hc.producer.HCConfiguration;
import org.egov.hc.utils.HCConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	private static final String ASSIGNEEKEY = "assignes";

	private static final String TLMODULENAMEVALUE = "HORTICULTURE";

	private static final String WORKFLOWREQUESTARRAYKEY = "ProcessInstances";

	private static final String REQUESTINFOKEY = "RequestInfo";

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";

	private static final String STATUSJSONKEY = "$.state.applicationStatus";

	@Autowired
	private RestTemplate rest;
	@Autowired
	private HCConfiguration config;
	

	@Value("${workflow.bpa.businessServiceCode.fallback_enabled}")
	private Boolean pickWFServiceNameFromTradeTypeOnly;

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
	 */
	public boolean callWorkFlow(ServiceRequest request, String service_request_id) {
		boolean status = false;
		
		if(!request.getServices().isEmpty())
		{

			String wfTenantId = HCConstants.TENANT_ID;
		//String businessServiceFromMDMS = request.getServices().isEmpty()?null:request.getServices().get(0).getServiceType().toUpperCase();
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

				obj.put(DOCUMENTSKEY, wfDocument);
				obj.put(BUSINESSIDKEY, service_request_id);
				obj.put(TENANTIDKEY, wfTenantId);	
				
				String businesskey= servicerequestdata.getServiceType().toUpperCase().toString();
				obj.put(BUSINESSSERVICEKEY, businesskey );  
				
		
				obj.put(MODULENAMEKEY, TLMODULENAMEVALUE);
				obj.put(ACTIONKEY, servicerequestdata.getAction());		
				obj.put(COMMENTKEY, servicerequestdata.getComment());
//				if (!servicerequestdata.getIsRoleSpecific())
//					obj.put(ASSIGNEEKEY, servicerequestdata.getAssignee());
//				else
//					obj.put(ASSIGNEEKEY, servicerequestdata.getAssignee());
				obj.put(ASSIGNEEKEY, servicerequestdata.getAssignee());
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

			// setting the status back to hc object from wf response
			request.getServices()
					.forEach(hcObj -> hcObj.setService_request_status(idStatusMap.get(hcObj.getService_request_id())));


		}
	}
		return status;
		
	}
}