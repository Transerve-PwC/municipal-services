package org.egov.assets.wf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.MdmsRepository;
import org.egov.assets.model.IndentRequest;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialIssue;
import org.egov.assets.model.MaterialIssue.IssueTypeEnum;
import org.egov.assets.model.MaterialIssueDetail;
import org.egov.assets.model.MaterialIssueRequest;
import org.egov.assets.model.MaterialIssueResponse;
import org.egov.assets.model.MaterialIssueSearchContract;
import org.egov.assets.model.MaterialIssuedFromReceipt;
import org.egov.assets.model.User;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.service.MaterialIssueService;
import org.egov.assets.wf.model.ProcessInstanceResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

	private static final String UUIDKEY = "uuid";

	private static final String MODULENAMEVALUE = "StoreManagement";

	private static final String WORKFLOWREQUESTARRAYKEY = "ProcessInstances";

	private static final String REQUESTINFOKEY = "RequestInfo";

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";

	private static final String STATUSJSONKEY = "$.state.applicationStatus";

	private RestTemplate rest;

	private MdmsRepository mdmsRepository;

	private final String wfServiceTransitionUrl;
	private final String wfServiceProcessSearchUrl;

	
	@Autowired
	
	MaterialIssueService service;
	@Autowired
	public WorkflowIntegrator(RestTemplate rest, MdmsRepository mdmsRepository,
			@Value("${egov.services.egov_workflow.hostname}") final String wfServiceHostname,
			@Value("${egov.services.egov_workflow.transition}") final String wfServiceTransitionPath,
			@Value("${egov.services.egov_workflow.process.search}") final String wfServiceProcessSearchUrl) {
		this.rest = rest;
		this.wfServiceTransitionUrl = wfServiceHostname + wfServiceTransitionPath;
		this.wfServiceProcessSearchUrl = wfServiceHostname + wfServiceProcessSearchUrl;
		this.mdmsRepository = mdmsRepository;
	}

	public String getBusinessService(IndentRequest indentRequest, String tenantId) {
		JSONArray responseJSONArray = mdmsRepository.getByCriteria(tenantId, "store-asset", "businessService", null,
				null, indentRequest.getRequestInfo());
		Map<String, Material> materialMap = new HashMap<>();
		List<String> businessSeviceNames = new ArrayList<>();
		if (responseJSONArray != null && responseJSONArray.size() > 0) {
			for (int i = 0; i < responseJSONArray.size(); i++) {
				for (Role role : indentRequest.getRequestInfo().getUserInfo().getRoles()) {
					JSONObject object = (JSONObject) responseJSONArray.get(i);
					if (object.get("role").toString().equals(role.getName())) {
						businessSeviceNames.add(object.get("name").toString());
					}
				}
			}
		}

		return businessSeviceNames.get(0);
	}

	/**
	 * Method to integrate with workflow
	 *
	 * takes the trade-license request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to trade-license object
	 *
	 * @param tradeLicenseRequest
	 * @return
	 */
	public WorkFlowDetails callWorkFlow(RequestInfo requestInfo, WorkFlowDetails workFlowDetails, String tenantId) {

		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		if (workFlowDetails.getWfDocuments() != null) {
			workFlowDetails.getWfDocuments().forEach(document -> {
				document.setDocumentType("Document");
			});
		}
		obj.put(BUSINESSIDKEY, workFlowDetails.getBusinessId());
		obj.put(TENANTIDKEY, tenantId);
		obj.put(BUSINESSSERVICEKEY, workFlowDetails.getBusinessService());
		obj.put(MODULENAMEKEY, MODULENAMEVALUE);
		obj.put(ACTIONKEY, workFlowDetails.getAction());
		obj.put(COMMENTKEY, workFlowDetails.getComments());
		obj.put(DOCUMENTSKEY, workFlowDetails.getWfDocuments());

		List<Map<String, String>> uuidmaps = new LinkedList<>();

		if (!CollectionUtils.isEmpty(workFlowDetails.getAssignee())) {
			// Adding assignes to processInstance
			User user = new User();
			workFlowDetails.getAssignee().forEach(assignee -> {
				user.setUuid(assignee);
			});
			obj.put(ASSIGNEEKEY, user);
		}

		array.add(obj);
		if (!array.isEmpty()) {
			JSONObject workFlowRequest = new JSONObject();
			workFlowRequest.put(REQUESTINFOKEY, requestInfo);
			workFlowRequest.put(WORKFLOWREQUESTARRAYKEY, array);
			String response = null;
			try {
				response = rest.postForObject(wfServiceTransitionUrl, workFlowRequest, String.class);
			} catch (HttpClientErrorException e) {
				
			//	backUpdateIndentMinusForWorkFlow();
				/*
				 * extracting message from client error exception
				 */
				DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
				List<Object> errros = null;
				try {
					errros = responseContext.read("$.Errors");
				} catch (PathNotFoundException pnfe) {
					
					log.error("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
					throw new CustomException("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
				}
				throw new CustomException("EG_WF_ERROR", errros.toString());
			} catch (Exception e) {
				
			
					/*	MaterialIssueDetail issueDetail=new MaterialIssueDetail();
						service.backUpdateIndentMinusForWorkFlow(issueDetail, issueDetail.getTenantId());*/
				
				//backUpdateIndentMinusForWorkFlow();
				throw new CustomException("EG_WF_ERROR",
						" Exception occured while integrating with workflow : " + e.getMessage());
			}

			/*
			 * on success result from work-flow read the data and set the status back to TL
			 * object
			 */
			DocumentContext responseContext = JsonPath.parse(response);
			List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
			Map<String, String> idStatusMap = new HashMap<>();
			responseArray.forEach(object -> {

				DocumentContext instanceContext = JsonPath.parse(object);
				idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
			});
			System.out.println(idStatusMap);
			workFlowDetails.setStatus(idStatusMap.get(workFlowDetails.getBusinessId()));
		}

		return workFlowDetails;
	}
	public WorkFlowDetails callWorkFlowForIndent(RequestInfo requestInfo, WorkFlowDetails workFlowDetails, String tenantId,MaterialIssueRequest materialIssueRequest) {

		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		if (workFlowDetails.getWfDocuments() != null) {
			workFlowDetails.getWfDocuments().forEach(document -> {
				document.setDocumentType("Document");
			});
		}
		obj.put(BUSINESSIDKEY, workFlowDetails.getBusinessId());
		obj.put(TENANTIDKEY, tenantId);
		obj.put(BUSINESSSERVICEKEY, workFlowDetails.getBusinessService());
		obj.put(MODULENAMEKEY, MODULENAMEVALUE);
		obj.put(ACTIONKEY, workFlowDetails.getAction());
		obj.put(COMMENTKEY, workFlowDetails.getComments());
		obj.put(DOCUMENTSKEY, workFlowDetails.getWfDocuments());

		List<Map<String, String>> uuidmaps = new LinkedList<>();

		if (!CollectionUtils.isEmpty(workFlowDetails.getAssignee())) {
			// Adding assignes to processInstance
			User user = new User();
			workFlowDetails.getAssignee().forEach(assignee -> {
				user.setUuid(assignee);
			});
			obj.put(ASSIGNEEKEY, user);
		}

		array.add(obj);
		if (!array.isEmpty()) {
			JSONObject workFlowRequest = new JSONObject();
			workFlowRequest.put(REQUESTINFOKEY, requestInfo);
			workFlowRequest.put(WORKFLOWREQUESTARRAYKEY, array);
			String response = null;
			try {
				response = rest.postForObject(wfServiceTransitionUrl, workFlowRequest, String.class);
			} catch (HttpClientErrorException e) {
				
				for (MaterialIssue materialIssue : materialIssueRequest.getMaterialIssues()) {
					for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
						service.backUpdateIndentMinusForWorkFlow(materialIssueDetail, materialIssueDetail.getTenantId());
					}
				}
				/*
				 * extracting message from client error exception
				 */
				DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
				List<Object> errros = null;
				try {
					errros = responseContext.read("$.Errors");
				} catch (PathNotFoundException pnfe) {
					
					log.error("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
					throw new CustomException("EG_TL_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
				}
				throw new CustomException("EG_WF_ERROR", errros.toString());
			} catch (Exception e) {
				
						for (MaterialIssue materialIssue : materialIssueRequest.getMaterialIssues()) {
							for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
								service.backUpdateIndentMinusForWorkFlow(materialIssueDetail, materialIssueDetail.getTenantId());
							}
						}
				throw new CustomException("EG_WF_ERROR",
						" Exception occured while integrating with workflow : " + e.getMessage());
			}

			/*
			 * on success result from work-flow read the data and set the status back to TL
			 * object
			 */
			DocumentContext responseContext = JsonPath.parse(response);
			List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
			Map<String, String> idStatusMap = new HashMap<>();
			responseArray.forEach(object -> {

				DocumentContext instanceContext = JsonPath.parse(object);
				idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
			});
			System.out.println(idStatusMap);
			workFlowDetails.setStatus(idStatusMap.get(workFlowDetails.getBusinessId()));
		}

		return workFlowDetails;
	}

	public ProcessInstanceResponse getWorkflowDataByID(RequestInfo requestInfo, String businessId, String tenantId) {
		StringBuilder builder = new StringBuilder();
		String searchCriteria = "?businessIds=" + businessId + "&tenantId=" + tenantId + "&history=true";
		builder.append(wfServiceProcessSearchUrl).append(searchCriteria);
		JSONObject workFlowRequest = new JSONObject();
		workFlowRequest.put(REQUESTINFOKEY, requestInfo);
		ProcessInstanceResponse response = null;
		try {
			response = rest.postForObject(builder.toString(), workFlowRequest, ProcessInstanceResponse.class);
		} catch (HttpClientErrorException e) {

			/*
			 * extracting message from client error exception
			 */
			DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
			List<Object> errros = null;
			try {
				errros = responseContext.read("$.Errors");
			} catch (PathNotFoundException pnfe) {
				log.error("EG_SA_WF_ERROR_KEY_NOT_FOUND",
						" Unable to read the json path in error object : " + pnfe.getMessage());
				throw new CustomException("EG_SA_WF_ERROR_KEY_NOT_FOUND",
						" Unable to read the json path in error object : " + pnfe.getMessage());
			}
			throw new CustomException("EG_WF_ERROR", errros.toString());
		} catch (Exception e) {
			throw new CustomException("EG_WF_ERROR",
					" Exception occured while integrating with workflow : " + e.getMessage());
		}
		return response;
	}

	
}