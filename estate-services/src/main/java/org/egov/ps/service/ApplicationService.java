package org.egov.ps.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.ApplicationRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.validator.ApplicationValidatorService;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.RequestInfoMapper;
import org.egov.ps.web.contracts.State;
import org.egov.ps.workflow.WorkflowIntegrator;
import org.egov.ps.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ApplicationService {

	@Autowired
	private ApplicationEnrichmentService applicationEnrichmentService;

	@Autowired
	private ApplicationsNotificationService applicationNotificationService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	private ApplicationValidatorService validator;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private DemandService demandService;

	@Autowired
	private WorkflowService wfService;

	public List<Application> createApplication(ApplicationRequest request) {
		validator.validateCreateRequest(request);
		applicationEnrichmentService.enrichCreateApplicationRequest(request);
		producer.push(config.getSaveApplicationTopic(), request);
		return request.getApplications();
	}

	public List<Application> searchApplication(ApplicationCriteria criteria, RequestInfo requestInfo) {
		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_CITIZEN)) {
			criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
		}
		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_EMPLOYEE))
		{
			if(	CollectionUtils.isEmpty(criteria.getState())){
				RequestInfoMapper requestInfoMapper = RequestInfoMapper.builder().requestInfo(requestInfo).build();
				criteria.setBusinessName(criteria.getBranchType());
				criteria.setTenantId(PSConstants.TENANT_ID);
				List<String> states = getStates(requestInfoMapper, criteria);
				criteria.setState(states);
			}
			List<String> userRoleCode=requestInfo.getUserInfo().getRoles().stream().filter(role->role.getCode()!=PSConstants.ROLE_EMPLOYEE).map(role->role.getCode()).collect(Collectors.toList());
			if(userRoleCode.get(0).startsWith("ES_EB")) {
				criteria.setBranchType(PSConstants.APPLICATION_ESTATE_BRANCH);
			}
			else if(userRoleCode.get(0).startsWith("ES_BB")) {
				criteria.setBranchType(PSConstants.APPLICATION_BUILDING_BRANCH);
			}
			else if(userRoleCode.get(0).startsWith("ES_MM")) {
				criteria.setBranchType(PSConstants.APPLICATION_MANI_MAJRA);
			}
		}
		List<Application> applications = applicationRepository.getApplications(criteria);
		if (CollectionUtils.isEmpty(applications)) {
			if (requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_CITIZEN)
					&& criteria.getApplicationNumber() != null)
				throw new CustomException("INVALID ACCESS", "You can not access this application.");
			else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_EMPLOYEE)
					&& criteria.getApplicationNumber() != null)
				throw new CustomException("INVALID ACCESS", "You are not authorised to access this resource.");
			else
				return Collections.emptyList();
		}
		return applications;
	}

	public List<Application> updateApplicationRequest(ApplicationRequest applicationRequest) {
		validator.validateUpdateRequest(applicationRequest);
		applicationEnrichmentService.enrichUpdateApplication(applicationRequest);
		applicationRequest.getApplications().stream()
		.forEach(application -> updateApplication(applicationRequest.getRequestInfo(), application));
		producer.push(config.getUpdateApplicationTopic(), applicationRequest);

		applicationNotificationService.processNotifications(applicationRequest);
		return applicationRequest.getApplications();
	}

	private void updateApplication(RequestInfo requestInfo, Application application) {
		String action = application.getAction();
		String state = application.getState();

		if (state.contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {
			demandService.generateDemand(requestInfo, Collections.singletonList(application));
		}
		if (config.getIsWorkflowEnabled() && !action.contentEquals("")) {
			wfIntegrator.callApplicationWorkFlow(requestInfo, application);
		}
	}

	public List<String> getStates(RequestInfoMapper requestInfoWrapper, ApplicationCriteria applicationCriteria) {

		String tenantId = applicationCriteria.getTenantId();
		tenantId = tenantId.split("\\.")[0];

		List<State> states = wfService.getApplicationStatus(tenantId, applicationCriteria.getBusinessName(),
				requestInfoWrapper);
		return states.stream().map(State::getApplicationStatus).distinct().filter(state -> !state.equalsIgnoreCase(""))
				.collect(Collectors.toList());
	}

	public void collectPayment(ApplicationRequest applicationRequest) {
		applicationEnrichmentService.collectPayment(applicationRequest);
		demandService.generateFinanceDemand(applicationRequest);
	}

	public void updatePostPayment(ApplicationRequest applicationRequest, Map<String, Boolean> idToIsStateUpdatableMap) {
		RequestInfo requestInfo = applicationRequest.getRequestInfo();
		List<Application> applications = applicationRequest.getApplications();

		List<Application> applicationsForUpdate = new LinkedList<>();

		for (Application application : applications) {
			if (idToIsStateUpdatableMap.get(application.getId())) {
				applicationsForUpdate.add(application);
			}
		}

		if (!CollectionUtils.isEmpty(applicationsForUpdate)) {
			applicationsForUpdate.forEach(application -> {
				wfIntegrator.callApplicationWorkFlow(requestInfo, application);
			});
			producer.push(config.getUpdateApplicationTopic(),
					new ApplicationRequest(requestInfo, applicationsForUpdate));
		}
	}

}
