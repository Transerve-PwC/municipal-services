package org.egov.ps.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.ApplicationRepository;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.validator.ApplicationValidatorService;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.ps.web.contracts.RequestInfoMapper;
import org.egov.ps.web.contracts.State;
import org.egov.ps.workflow.WorkflowIntegrator;
import org.egov.ps.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;

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
	private PropertyRepository propertyRepository;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private DemandService demandService;

	@Autowired
	private WorkflowService wfService;

	@Autowired
	private Util util;

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
		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_EMPLOYEE)) {
			if (!CollectionUtils.isEmpty(criteria.getBranchType()) && CollectionUtils.isEmpty(criteria.getState())) {
				RequestInfoMapper requestInfoMapper = RequestInfoMapper.builder().requestInfo(requestInfo).build();
				criteria.setBusinessName(criteria.getBranchType().get(0));
				criteria.setTenantId(PSConstants.TENANT_ID);
				List<String> states = getStates(requestInfoMapper, criteria);
				criteria.setState(states);
			}
			Set<String> employeeBranches = new HashSet<>();
			requestInfo.getUserInfo().getRoles().stream().filter(role -> role.getCode() != PSConstants.ROLE_EMPLOYEE)
					.map(role -> role.getCode()).forEach(rolecode -> {
						if (rolecode.startsWith("ES_EB")) {
							employeeBranches.add(PSConstants.APPLICATION_ESTATE_BRANCH);
						}
						if (rolecode.startsWith("ES_BB")) {
							employeeBranches.add(PSConstants.APPLICATION_BUILDING_BRANCH);
						}
						if (rolecode.startsWith("ES_MM")) {
							employeeBranches.add(PSConstants.APPLICATION_MANI_MAJRA);
						}
						if (rolecode.equalsIgnoreCase("ES_ADDITIONAL_COMMISSIONER")) {
							employeeBranches.add(PSConstants.APPLICATION_ESTATE_BRANCH);
							employeeBranches.add(PSConstants.APPLICATION_BUILDING_BRANCH);
							employeeBranches.add(PSConstants.APPLICATION_MANI_MAJRA);
						}
					});
			if ((criteria.getBranchType() != null && !criteria.getBranchType().isEmpty())) {
				if (!criteria.getBranchType().stream().filter(branch -> employeeBranches.contains(branch)).findAny()
						.isPresent())
					throw new CustomException("INVALID ACCESS", "You are not able to access this resource.");
			} else {
				criteria.setBranchType(new ArrayList<>(employeeBranches));
			}
		}
		List<Application> applications = applicationRepository.getApplications(criteria);
		if (CollectionUtils.isEmpty(applications)) {
			if ((requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_CITIZEN)
					|| requestInfo.getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_EMPLOYEE))
					&& criteria.getApplicationNumber() != null)
				throw new CustomException("INVALID ACCESS", "You are not able to access this resource.");
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

		applicationRequest.getApplications().forEach(application -> {
			if (application.getState().equalsIgnoreCase(PSConstants.PENDING_SO_APPROVAL)
					|| application.getState().equalsIgnoreCase(PSConstants.PENDING_MM_SO_APPROVAL)) {
				postApprovalChangeOwnerShare(application, applicationRequest.getRequestInfo());
			}
		});

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

	private void postApprovalChangeOwnerShare(Application application, RequestInfo requestInfo) {
		/**
		 * Change share % after OWNERSHIP_TRANSFER and update property topic
		 */
		if (application.getModuleType().equalsIgnoreCase(PSConstants.OWNERSHIP_TRANSFER)) {

			PropertyCriteria propertySearchCriteria = PropertyCriteria.builder()
					.propertyId(application.getProperty().getId()).build();
			List<Property> properties = propertyRepository.getProperties(propertySearchCriteria);

			properties.forEach(property -> {

				Owner newOwner = new Owner();
				for (Owner ownerFromDb : property.getPropertyDetails().getOwners()) {

					/**
					 * Decrease owner share and if share is equals 0 then make current owner as
					 * false
					 */
					JsonNode transferor = (application.getApplicationDetails().get("transferor") != null)
							? application.getApplicationDetails().get("transferor")
							: application.getApplicationDetails().get("owner");

					JsonNode transferee = application.getApplicationDetails().get("transferee");

					double percentageTransfered = transferee.get("percentageOfShareTransferred").asDouble();
					if (ownerFromDb.getId().equals(transferor.get("id").asText())) {
						double transferedShare = ownerFromDb.getShare() - percentageTransfered;
						ownerFromDb.setShare(transferedShare);
						if (ownerFromDb.getShare() <= 0) {
							ownerFromDb.getOwnerDetails().setIsCurrentOwner(false);
							ownerFromDb.setShare(0);
						}
					}

					/**
					 * Increase owner share if owner already exists else create new owner with the
					 * current share
					 */
					if (null != transferee.get("id") && ownerFromDb.getId().equals(transferee.get("id").asText())) {
						double transferedShare = ownerFromDb.getShare() + percentageTransfered;
						ownerFromDb.setShare(transferedShare);
						ownerFromDb.getOwnerDetails().setIsCurrentOwner(true);
					}
					if (null == transferee.get("id") && ownerFromDb.getId().equals(transferor.get("id").asText())) {
						AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
						String ownerId = UUID.randomUUID().toString();

						final OwnerDetails newOwnerDetails = OwnerDetails.builder().id(UUID.randomUUID().toString())
								.ownerId(ownerId).ownerName(transferee.get("name").asText())
								.tenantId(application.getTenantId())
								.guardianName(transferee.get("fatherOrHusbandName").asText())
								.guardianRelation(transferee.get("relation").asText())
								.mobileNumber(transferee.get("mobileNo").asText()).allotmentNumber(null)
								.dateOfAllotment(System.currentTimeMillis()).possesionDate(System.currentTimeMillis())
								.isApproved(true).isCurrentOwner(true).isMasterEntry(false)
								.address(transferee.get("address").asText()).isDirector(null)
								.sellerName(transferor.get("name").asText()).sellerGuardianName(null)
								.sellerRelation(null)
								.modeOfTransfer(getModeOfTransfer(application.getApplicationType()))
								.dob(transferee.get("dob").asLong()).isPreviousOwnerRequired(false)
								.auditDetails(auditDetails).build();

						newOwner = Owner.builder().id(ownerId).tenantId(application.getTenantId())
								.propertyDetailsId(property.getPropertyDetails().getId()).serialNumber(null)
								.share(percentageTransfered).cpNumber(null).state(null).action(null).ownershipType(null)
								.ownerDetails(newOwnerDetails).auditDetails(auditDetails).build();

					}
				}

				if (null != newOwner.getId()) {
					property.getPropertyDetails().addOwnerItem(newOwner);
				}
			});
			producer.push(config.getUpdatePropertyTopic(), new PropertyRequest(requestInfo, properties));
		}

	}

	private String getModeOfTransfer(String applicationType) {
		return String.format("%s.%s", PSConstants.MODE_OF_TRANSFER, applicationType.toUpperCase());
	}
}
