package org.egov.ps.service;

import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.validator.ApplicationValidatorService;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.RequestInfoMapper;
import org.egov.ps.web.contracts.State;
import org.egov.ps.workflow.WorkflowIntegrator;
import org.egov.ps.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ApplicationService {

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	ApplicationValidatorService validator;

	@Autowired
	PropertyRepository repository;

	@Autowired
	WorkflowIntegrator wfIntegrator;

	@Autowired
	private DemandService demandService;

	@Autowired
	private WorkflowService wfService;

	public List<Application> createApplication(ApplicationRequest request) {
		validator.validateCreateRequest(request);
		enrichmentService.enrichCreateApplication(request);
		producer.push(config.getSaveApplicationTopic(), request);
		return request.getApplications();
	}

	public List<Application> searchApplication(ApplicationCriteria criteria, RequestInfo requestInfo) {
		if (criteria.getFileNumber() != null) {
			criteria.setFileNumber(criteria.getFileNumber().toUpperCase());
		}
		List<Application> applications = repository.getApplications(criteria);

		if (CollectionUtils.isEmpty(applications))
			return Collections.emptyList();
		return applications;
	}

	public List<Application> updateApplication(ApplicationRequest applicationRequest) {
		validator.getApplications(applicationRequest);
		enrichmentService.enrichUpdateApplication(applicationRequest);
		String action = applicationRequest.getApplications().get(0).getAction();
		String state = applicationRequest.getApplications().get(0).getState();

		if (state.contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {
			demandService.createDemand(applicationRequest.getRequestInfo(), applicationRequest.getApplications());
		}
		if (config.getIsWorkflowEnabled() && !action.contentEquals("")) {
			wfIntegrator.callApplicationWorkFlow(applicationRequest);
		}
		producer.push(config.getUpdateApplicationTopic(), applicationRequest);

		return applicationRequest.getApplications();
	}

	public List<State> getStates(RequestInfoMapper requestInfoWrapper) {

		List<State> status = wfService.getApplicationStatus("ch", requestInfoWrapper);

		return status;
	}
}
