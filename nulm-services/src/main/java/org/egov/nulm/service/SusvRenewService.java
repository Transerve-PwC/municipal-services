
package org.egov.nulm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.idgen.model.IdGenerationResponse;
import org.egov.nulm.model.NulmSusvRenewRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SusvApplication;
import org.egov.nulm.model.SusvApplicationDocument;
import org.egov.nulm.model.SusvRenewApplication;
import org.egov.nulm.repository.SusvRenewRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.nulm.util.WorkFlowRepository;
import org.egov.nulm.workflow.model.ProcessInstance;
import org.egov.nulm.workflow.model.ProcessInstanceRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

@Service
public class SusvRenewService {

	private final ObjectMapper objectMapper;

	private SusvRenewRepository repository;

	private NULMConfiguration config;

	private AuditDetailsUtil auditDetailsUtil;
	private WorkFlowRepository workFlowRepository;
	private IdGenRepository idgenrepository;

	private static final String PROCESSINSTANCESJOSNKEY = "$.ProcessInstances";

	private static final String BUSINESSIDJOSNKEY = "$.businessId";
	private static final String STATUSJSONKEY = "$.state.applicationStatus";

	@Autowired
	public SusvRenewService(SusvRenewRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config, AuditDetailsUtil auditDetailsUtil, WorkFlowRepository workFlowRepository) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.auditDetailsUtil = auditDetailsUtil;
		this.workFlowRepository = workFlowRepository;
		this.idgenrepository = idgenrepository;
		this.config = config;
	}

	public ResponseEntity<ResponseInfoWrapper> createSusvRenewApplication(NulmSusvRenewRequest request) {
		try {
			SusvRenewApplication susvRenewapplication = objectMapper.convertValue(request.getNulmSusvRenewRequest(),
					SusvRenewApplication.class);
			String susvId = UUID.randomUUID().toString();
			susvRenewapplication.setApplicationUuId(susvId);
			susvRenewapplication.setIsActive(true);
			susvRenewapplication.setAuditDetails(
					auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));

			// idgen service call to genrate event id
			IdGenerationResponse id = idgenrepository.getId(request.getRequestInfo(),
					susvRenewapplication.getTenantId(), config.getSusvRenewApplicationNumberIdgenName(),
					config.getSusvRenewApplicationNumberIdgenFormat(), 1);
			if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
				susvRenewapplication.setApplicationId(id.getIdResponses().get(0).getId());
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);

			// save document to nulm_susv_application_document table
			List<SusvApplicationDocument> susvdoc = new ArrayList<>();
			for (SusvApplicationDocument docobj : susvRenewapplication.getApplicationDocument()) {
				SusvApplicationDocument documnet = new SusvApplicationDocument();
				documnet.setDocumnetUuid(UUID.randomUUID().toString());
				documnet.setApplicationUuid(susvId);
				documnet.setDocumentType(docobj.getDocumentType());
				documnet.setFilestoreId(docobj.getFilestoreId());
				documnet.setAuditDetails(
						auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
				documnet.setIsActive(true);
				documnet.setTenantId(susvRenewapplication.getTenantId());
				susvdoc.add(documnet);
			}
			susvRenewapplication.setApplicationDocument(susvdoc);
			if (susvRenewapplication.getSusvApplicationFamilyDetails() != null) {
				susvRenewapplication.getSusvApplicationFamilyDetails().stream().forEach(element -> {
					element.setUuid(UUID.randomUUID().toString());
					element.setApplicationUuid(susvId);
					element.setIsActive(true);
					element.setTenantId(susvRenewapplication.getTenantId());
					element.setAuditDetails(
							auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));

				});
			}
			if (!susvRenewapplication.getAction().toString()
					.equalsIgnoreCase(SusvApplication.StatusEnum.DRAFTED.toString())) {
				// workflow service call to integrate nulm workflow
				String workflowStatus = workflowIntegration(request.getRequestInfo(), susvRenewapplication);
				susvRenewapplication.setApplicationStatus(SusvRenewApplication.StatusEnum.fromValue(workflowStatus));
			}

			repository.createSusvRenewApplication(susvRenewapplication);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(susvRenewapplication).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUSV_RENEW_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> updateSusvRenewApplication(NulmSusvRenewRequest request) {
		try {
			SusvRenewApplication susvRenewApplication = objectMapper.convertValue(request.getNulmSusvRenewRequest(),
					SusvRenewApplication.class);
			susvRenewApplication.setIsActive(true);
			susvRenewApplication.setAuditDetails(
					auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			// update document to nulm_susv_application_document table
			List<SusvApplicationDocument> susvdoc = new ArrayList<>();
			for (SusvApplicationDocument docobj : susvRenewApplication.getApplicationDocument()) {
				SusvApplicationDocument document = new SusvApplicationDocument();
				document.setDocumnetUuid(UUID.randomUUID().toString());
				document.setApplicationUuid(susvRenewApplication.getApplicationUuId());
				document.setDocumentType(docobj.getDocumentType());
				document.setFilestoreId(docobj.getFilestoreId());
				document.setAuditDetails(
						auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
				document.setIsActive(true);
				document.setTenantId(susvRenewApplication.getTenantId());
				susvdoc.add(document);
			}
			susvRenewApplication.setApplicationDocument(susvdoc);
			if (susvRenewApplication.getSusvApplicationFamilyDetails() != null) {
				susvRenewApplication.getSusvApplicationFamilyDetails().stream().forEach(element -> {
					element.setUuid(UUID.randomUUID().toString());
					element.setApplicationUuid(susvRenewApplication.getApplicationUuId());
					element.setIsActive(true);
					element.setTenantId(susvRenewApplication.getTenantId());
					element.setAuditDetails(
							auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
				});
			}
			if (!susvRenewApplication.getAction().toString()
					.equalsIgnoreCase(SusvApplication.StatusEnum.DRAFTED.toString())) {
				// workflow service call to integrate nulm workflow
				String workflowStatus = workflowIntegration(request.getRequestInfo(), susvRenewApplication);
				susvRenewApplication.setApplicationStatus(SusvRenewApplication.StatusEnum.fromValue(workflowStatus));
			}
			repository.updateSusvRenewApplication(susvRenewApplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(susvRenewApplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUSV_RENEW_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> updateAppStatus(NulmSusvRenewRequest request) {
		try {
			SusvRenewApplication susvRenewApplication = objectMapper.convertValue(request.getNulmSusvRenewRequest(),
					SusvRenewApplication.class);
			susvRenewApplication.setIsActive(true);
			susvRenewApplication.setAuditDetails(
					auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));

			// workflow service call to integrate nulm workflow
			String workflowStatus = workflowIntegration(request.getRequestInfo(), susvRenewApplication);
			susvRenewApplication.setApplicationStatus(SusvRenewApplication.StatusEnum.fromValue(workflowStatus));
			repository.updateSusvApplicationStatus(susvRenewApplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(susvRenewApplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUSV_RENEW_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> getSusvRenewApplication(NulmSusvRenewRequest request) {
		try {

			SusvRenewApplication susvRenewapplication = objectMapper.convertValue(request.getNulmSusvRenewRequest(),
					SusvRenewApplication.class);

			List<SusvRenewApplication> SuhApplicationresult = repository.getSusvRenewApplication(susvRenewapplication,
					request.getRequestInfo().getUserInfo().getType(), request.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SuhApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SUSV_RENEW_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	/**
	 * Method to integrate with workflow
	 *
	 * takes the tender request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to susv object
	 *
	 * @param susvRenew
	 *            object
	 */
	private String workflowIntegration(RequestInfo requestInfo, SusvRenewApplication susvRenewApplication) {
		String workflowResponse = null;
		try {
			ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest();
			workflowRequest.setRequestInfo(requestInfo);
			ProcessInstance processInstances = new ProcessInstance();
			processInstances.setTenantId(susvRenewApplication.getTenantId());
			processInstances.setAction(susvRenewApplication.getAction());
			processInstances.setBusinessId(susvRenewApplication.getApplicationId());
			processInstances.setModuleName(config.getBusinessservice());
			processInstances.setBusinessService(config.getBusinessservice());
			processInstances.setDocuments(susvRenewApplication.getWfDocuments());
			processInstances.setComment(susvRenewApplication.getRemark());

			if (!CollectionUtils.isEmpty(susvRenewApplication.getAssignee())) {
				// Adding assignes to processInstance
				User user = new User();
				susvRenewApplication.getAssignee().forEach(assignee -> {
					user.setUuid(assignee);
				});
				processInstances.setAssignee(user);
			}

			List<ProcessInstance> processList = Arrays.asList(processInstances);
			workflowRequest.setProcessInstances(processList);
			workflowResponse = workFlowRepository.createWorkflowRequest(workflowRequest);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUSV_RENEW_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}

		/*
		 * on success result from work-flow read the data and set the status back object
		 */
		DocumentContext responseContext = JsonPath.parse(workflowResponse);
		List<Map<String, Object>> responseArray = responseContext.read(PROCESSINSTANCESJOSNKEY);
		Map<String, String> idStatusMap = new HashMap<>();
		responseArray.forEach(object -> {

			DocumentContext instanceContext = JsonPath.parse(object);
			idStatusMap.put(instanceContext.read(BUSINESSIDJOSNKEY), instanceContext.read(STATUSJSONKEY));
		});
		System.out.println(idStatusMap);
		return idStatusMap.get(susvRenewApplication.getApplicationId());

	}

}