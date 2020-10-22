package org.egov.cpt.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.calculation.BusinessService;
import org.egov.cpt.models.calculation.State;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.service.notification.MortgageNotificationService;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.validator.PropertyValidator;
import org.egov.cpt.web.contracts.MortgageRequest;
import org.egov.cpt.workflow.WorkflowIntegrator;
import org.egov.cpt.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MortgageService {
	@Autowired
	private PropertyValidator propertyValidator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private PropertyRepository repository;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	MortgageNotificationService notificationService;

	@Autowired
	private WorkflowService workflowService;

	public List<Mortgage> createApplication(MortgageRequest mortgageRequest) {
		propertyValidator.isPropertyExist(mortgageRequest);
		propertyValidator.validateMortgageCreateRequest(mortgageRequest);
		enrichmentService.enrichMortgageCreateRequest(mortgageRequest);
		if (config.getIsWorkflowEnabled()) {
			wfIntegrator.callMortgageWorkFlow(mortgageRequest);
		}
		producer.push(config.getSaveMortgageTopic(), mortgageRequest);
		return mortgageRequest.getMortgageApplications();
	}

	public List<Mortgage> searchApplication(DuplicateCopySearchCriteria criteria, RequestInfo requestInfo) {
		propertyValidator.validateMortgageSearch(requestInfo, criteria);
		enrichmentService.enrichDuplicateCopySearchCriteria(requestInfo, criteria);
		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(PTConstants.ROLE_EMPLOYEE)
				&& CollectionUtils.isEmpty(criteria.getStatus())) {
			String wfbusinessServiceName = PTConstants.BUSINESS_SERVICE_MG;
			BusinessService otBusinessService = workflowService.getBusinessService(criteria.getTenantId(), requestInfo,
					wfbusinessServiceName);
			List<State> stateList = otBusinessService.getStates();
			List<String> states = new ArrayList<String>();

			for (State state : stateList) {
				states.add(state.getState());
			}
			states.remove("");
			states.remove(PTConstants.MG_DRAFTED);

			log.info("states:" + states);

			criteria.setStatus(states);
		}
		return getApplication(criteria, requestInfo);
	}

	private List<Mortgage> getApplication(DuplicateCopySearchCriteria criteria, RequestInfo requestInfo) {
		List<Mortgage> application = repository.getMortgageProperties(criteria);
		if (application.isEmpty()){
			if(requestInfo.getUserInfo().getType().equalsIgnoreCase(PTConstants.ROLE_CITIZEN)&& criteria.getApplicationNumber()!=null)
				throw new CustomException("INVALID ACCESS", "You can not access this application.");
			else
				return Collections.emptyList();
		}
		return application;
	}

	public List<Mortgage> updateApplication(MortgageRequest mortgageRequest) {
		List<Mortgage> searchedProperty = propertyValidator.validateMOrtgageUpdateRequest(mortgageRequest);
		enrichmentService.enrichMortgageUpdateRequest(mortgageRequest, searchedProperty);
		propertyValidator.validateMortgageUpdate(mortgageRequest);
		if (config.getIsWorkflowEnabled()) {
			wfIntegrator.callMortgageWorkFlow(mortgageRequest);
		}
		producer.push(config.getUpdateMortgageTopic(), mortgageRequest);
		notificationService.process(mortgageRequest);
		return mortgageRequest.getMortgageApplications();
	}
}
