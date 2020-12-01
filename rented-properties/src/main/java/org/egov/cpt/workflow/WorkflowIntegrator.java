package org.egov.cpt.workflow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.web.contracts.DuplicateCopyRequest;
import org.egov.cpt.web.contracts.MortgageRequest;
import org.egov.cpt.web.contracts.OwnershipTransferRequest;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class WorkflowIntegrator {

	private static final String TENANTIDKEY = "tenantId";

	private static final String BUSINESSSERVICEKEY = "businessService";

	private static final String BUSINESSIDKEY = "businessId";

	private static final String ACTIONKEY = "action";

	private static final String MODULENAMEKEY = "moduleName";

	private static final String COMMENTKEY = "comment";

	private static final String DOCUMENTSKEY = "documents";

	private static final String ASSIGNEEKEY = "assignee";

	private static final String UUIDKEY = "uuid";

	private static final String MODULENAMEVALUE = "csp";

	private static final String WORKFLOWREQUESTARRAYKEY = "ProcessInstances";

	private static final String REQUESTINFOKEY = "RequestInfo";

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";

	private static final String STATUSJSONKEY = "$.state.applicationStatus";

	private static final String AUDITDETAILSKEY = "auditDetails";

	private RestTemplate rest;

	private PropertyConfiguration config;

	@Autowired
	public WorkflowIntegrator(RestTemplate rest, PropertyConfiguration config) {
		this.rest = rest;
		this.config = config;
	}

	/**
	 * Method to integrate with workflow
	 *
	 * takes the property request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to property object
	 *
	 * @param request
	 */
	public void callWorkFlow(PropertyRequest request) {

		String wfTenantId = request.getProperties().get(0).getTenantId();
		JSONArray array = new JSONArray();
		for (Property property : request.getProperties()) {
			JSONObject obj = new JSONObject();
			List<Map<String, String>> uuidmaps = new LinkedList<>();
			List<Map<String, String>> assigneeUuidmaps = new LinkedList<>();

			if (!CollectionUtils.isEmpty(property.getAssignee())) {

				// Adding assignees to processInstance
				property.getAssignee().forEach(assignee -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, assignee);
					assigneeUuidmaps.add(uuidMap);
				});
			}

			if (!CollectionUtils.isEmpty(property.getOwners())) {
				property.getOwners().forEach(owners -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, owners.getId());
					uuidmaps.add(uuidMap);
				});
			}
			obj.put(TENANTIDKEY, wfTenantId);
			obj.put(BUSINESSSERVICEKEY, config.getCSPBusinessServiceValue());
			obj.put(BUSINESSIDKEY, property.getTransitNumber());
			obj.put(ACTIONKEY, property.getMasterDataAction());
			obj.put(MODULENAMEKEY, MODULENAMEVALUE);
			obj.put(AUDITDETAILSKEY, property.getAuditDetails());
			obj.put(COMMENTKEY, property.getComment());
			if (!CollectionUtils.isEmpty(property.getAssignee())) {
				if (uuidmaps.size() == 1) {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps.get(0));
				} else {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps);
				}
			}

			array.add(obj);
		}
		Map<String, String> idStatusMap = callCommonWorkflow(array, request.getRequestInfo());

		// setting the status back to Property object from wf response
		request.getProperties().forEach(property -> {
			property.setMasterDataState(idStatusMap.get(property.getTransitNumber()));
		});
	}

	public void callDuplicateCopyWorkFlow(DuplicateCopyRequest request) {

		String wfTenantId = request.getDuplicateCopyApplications().get(0).getTenantId();
		JSONArray array = new JSONArray();
		for (DuplicateCopy application : request.getDuplicateCopyApplications()) {
			JSONObject obj = new JSONObject();
			List<Map<String, String>> uuidmaps = new LinkedList<>();
			List<Map<String, String>> assigneeUuidmaps = new LinkedList<>();
			if (!CollectionUtils.isEmpty(application.getAssignee())) {
				application.getAssignee().forEach(assignee -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, assignee);
					assigneeUuidmaps.add(uuidMap);
				});
			}
			if (!CollectionUtils.isEmpty(application.getApplicant())) {
				application.getApplicant().forEach(owners -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, owners.getId());
					uuidmaps.add(uuidMap);
				});
			}
			obj.put(TENANTIDKEY, wfTenantId);
			obj.put(BUSINESSSERVICEKEY, config.getDuplicateCopyBusinessServiceValue());
			obj.put(BUSINESSIDKEY, application.getApplicationNumber());
			obj.put(ACTIONKEY, application.getAction());
			obj.put(MODULENAMEKEY, MODULENAMEVALUE);
			obj.put(AUDITDETAILSKEY, application.getAuditDetails());
			obj.put(COMMENTKEY, application.getComment());
			obj.put(DOCUMENTSKEY, application.getWfdocuments());

			if (!CollectionUtils.isEmpty(application.getAssignee())) {
				if (uuidmaps.size() == 1) {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps.get(0));
				} else {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps);
				}
			}

			array.add(obj);
		}
		Map<String, String> idStatusMap = callCommonWorkflow(array, request.getRequestInfo());

		// setting the status back to Application object from wf response
		request.getDuplicateCopyApplications()
				.forEach(application -> application.setState(idStatusMap.get(application.getApplicationNumber())));
	}

	public void callOwnershipTransferWorkFlow(OwnershipTransferRequest request) {

		String wfTenantId = request.getOwners().get(0).getTenantId();
		JSONArray array = new JSONArray();
		for (Owner owner : request.getOwners()) {
			JSONObject obj = new JSONObject();
			List<Map<String, String>> assigneeUuidmaps = new LinkedList<>();
			if (!CollectionUtils.isEmpty(owner.getAssignee())) {
				owner.getAssignee().forEach(assignee -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, assignee);
					assigneeUuidmaps.add(uuidMap);
				});
			}

			obj.put(TENANTIDKEY, wfTenantId);
			obj.put(BUSINESSSERVICEKEY, config.getOwnershipTransferBusinessServiceValue());
			obj.put(BUSINESSIDKEY, owner.getOwnerDetails().getApplicationNumber());
			obj.put(ACTIONKEY, owner.getApplicationAction());
			obj.put(MODULENAMEKEY, MODULENAMEVALUE);
			obj.put(AUDITDETAILSKEY, owner.getAuditDetails());
			obj.put(COMMENTKEY, owner.getComment());
			obj.put(DOCUMENTSKEY, owner.getWfdocuments());
			if (!CollectionUtils.isEmpty(owner.getAssignee())) {
				if (assigneeUuidmaps.size() == 1) {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps.get(0));
				} else {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps);
				}
			}

			array.add(obj);
		}
		Map<String, String> idStatusMap = callCommonWorkflow(array, request.getRequestInfo());

		// setting the status back to Application object from wf response
		request.getOwners().forEach(owner -> {
			owner.setApplicationState(idStatusMap.get(owner.getOwnerDetails().getApplicationNumber()));
		});
	}

	public void callMortgageWorkFlow(MortgageRequest request) {
		String wfTenantId = request.getMortgageApplications().get(0).getTenantId();
		JSONArray array = new JSONArray();
		for (Mortgage application : request.getMortgageApplications()) {
			JSONObject obj = new JSONObject();
			List<Map<String, String>> uuidmaps = new LinkedList<>();
			List<Map<String, String>> assigneeUuidmaps = new LinkedList<>();
			if (!CollectionUtils.isEmpty(application.getAssignee())) {
				application.getAssignee().forEach(assignee -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, assignee);
					assigneeUuidmaps.add(uuidMap);
				});
			}
			if (!CollectionUtils.isEmpty(application.getApplicant())) {
				application.getApplicant().forEach(owners -> {
					Map<String, String> uuidMap = new HashMap<>();
					uuidMap.put(UUIDKEY, owners.getId());
					uuidmaps.add(uuidMap);
				});
			}
			obj.put(TENANTIDKEY, wfTenantId);
			obj.put(BUSINESSSERVICEKEY, config.getMortgageBusinessServiceValue());
			obj.put(BUSINESSIDKEY, application.getApplicationNumber());
			obj.put(ACTIONKEY, application.getAction());
			obj.put(MODULENAMEKEY, MODULENAMEVALUE);
			obj.put(AUDITDETAILSKEY, application.getAuditDetails());
			obj.put(COMMENTKEY, application.getComment());

			if (!CollectionUtils.isEmpty(application.getAssignee())) {
				if (uuidmaps.size() == 1) {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps.get(0));
				} else {
					obj.put(ASSIGNEEKEY, assigneeUuidmaps);
				}
			}

			array.add(obj);
		}
		Map<String, String> idStatusMap = callCommonWorkflow(array, request.getRequestInfo());

		// setting the status back to Application object from wf response
		request.getMortgageApplications()
				.forEach(application -> application.setState(idStatusMap.get(application.getApplicationNumber())));
	}

	private Map<String, String> callCommonWorkflow(JSONArray array, RequestInfo requestInfo) {
		Map<String, String> idStatusMap = new HashMap<>();
		if (!array.isEmpty()) {
			JSONObject workFlowRequest = new JSONObject();
			workFlowRequest.put(REQUESTINFOKEY, requestInfo);
			workFlowRequest.put(WORKFLOWREQUESTARRAYKEY, array);
			String response = null;
			try {
				response = rest.postForObject(config.getWfHost().concat(config.getWfTransitionPath()), workFlowRequest,
						String.class);
			} catch (HttpClientErrorException e) {

				/*
				 * extracting message from client error exception
				 */
				DocumentContext responseContext = JsonPath.parse(e.getResponseBodyAsString());
				List<Object> errros = null;
				try {
					errros = responseContext.read("$.Errors");
				} catch (PathNotFoundException pnfe) {
					log.error("EG_CSP_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
					throw new CustomException("EG_CSP_WF_ERROR_KEY_NOT_FOUND",
							" Unable to read the json path in error object : " + pnfe.getMessage());
				}
				throw new CustomException("EG_WF_ERROR", errros.toString());
			} catch (Exception e) {
				throw new CustomException("EG_WF_ERROR",
						" Exception occured while integrating with workflow : " + e.getMessage());
			}

			/*
			 * on success result from work-flow read the data and set the status back to
			 * Property object
			 */
			DocumentContext responseContext = JsonPath.parse(response);
			List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
			responseArray.forEach(object -> {
				DocumentContext instanceContext = JsonPath.parse(object);
				idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
			});
		}
		return idStatusMap;
	}
}