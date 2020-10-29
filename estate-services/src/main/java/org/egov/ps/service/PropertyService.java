package org.egov.ps.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.IEstateRentCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.validator.PropertyValidator;
import org.egov.ps.web.contracts.AccountStatementResponse;
import org.egov.ps.web.contracts.BusinessService;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.ps.web.contracts.State;
import org.egov.ps.workflow.WorkflowIntegrator;
import org.egov.ps.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyService {

	@Autowired
	private PropertyEnrichmentService enrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	PropertyValidator propertyValidator;

	@Autowired
	PropertyRepository repository;

	@Autowired
	WorkflowIntegrator wfIntegrator;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private IEstateRentCollectionService estateRentCollectionService;

	
	public List<Property> createProperty(PropertyRequest request) {
		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichPropertyRequest(request);
		processRentHistory(request);
		producer.push(config.getSavePropertyTopic(), request);
		processRentSummary(request);
		return request.getProperties();
	}
	
	private void processRentHistory(PropertyRequest request) {
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().stream().filter(property -> property.getPropertyDetails().getEstateDemands() != null
					&& property.getPropertyDetails().getEstatePayments() != null && property.getPropertyDetails().getEstateAccount() != null).forEach(property -> {
						property.getPropertyDetails().setEstateRentCollections(
								estateRentCollectionService.settle(property.getPropertyDetails().getEstateDemands(), property.getPropertyDetails().getEstatePayments(),
										property.getPropertyDetails().getEstateAccount(), property.getPropertyDetails().getInterestRate(),true));
					});
		}
		enrichmentService.enrichCollection(request);
		
	}

	private void processRentSummary(PropertyRequest request) {
		request.getProperties().stream().filter(property -> property.getPropertyDetails().getEstateDemands() != null
				&& property.getPropertyDetails().getEstatePayments() != null && property.getPropertyDetails().getEstateAccount() != null).forEach(property -> {
					property.setEstateRentSummary(estateRentCollectionService.calculateRentSummary(property.getPropertyDetails().getEstateDemands(),
							property.getPropertyDetails().getEstateAccount(), property.getPropertyDetails().getInterestRate()));
				});
	}

	/**
	 * Updates the property
	 *
	 * @param request PropertyRequest containing list of properties to be update
	 * @return List of updated properties
	 */
	public List<Property> updateProperty(PropertyRequest request) {
		propertyValidator.validateUpdateRequest(request);
		enrichmentService.enrichPropertyRequest(request);
		processRentHistory(request);
		String action = request.getProperties().get(0).getAction();
		if (config.getIsWorkflowEnabled() && !action.contentEquals("") && !action.contentEquals(PSConstants.ES_DRAFT)) {
			wfIntegrator.callWorkFlow(request);
		}
		producer.push(config.getUpdatePropertyTopic(), request);
		processRentSummary(request);
		return request.getProperties();
	}

	public List<Property> searchProperty(PropertyCriteria criteria, RequestInfo requestInfo) {
		/**
		 * Convert file number to upper case if provided.
		 */
		if (criteria.getFileNumber() != null) {
			criteria.setFileNumber(criteria.getFileNumber().toUpperCase());
		}

		if (criteria.isEmpty()) {
			/**
			 * Set the list of states to exclude draft states. Allow criteria to have
			 * creator as current user.
			 */
			BusinessService businessService = workflowService.getBusinessService(PSConstants.TENANT_ID, requestInfo,
					config.getAosBusinessServiceValue());
			List<String> states = businessService.getStates().stream().map(State::getState)
					.filter(s -> s != null && s.length() != 0).collect(Collectors.toList());
			criteria.setState(states);
			criteria.setUserId(requestInfo.getUserInfo().getUuid());
		} else if (criteria.getState() != null && criteria.getState().contains(PSConstants.PM_DRAFTED)) {
			/**
			 * If only drafted state is asked for, fetch currently logged in user's
			 * properties.
			 */
			criteria.setUserId(requestInfo.getUserInfo().getUuid());
		}

		List<Property> properties = repository.getProperties(criteria);
		
		if (CollectionUtils.isEmpty(properties))
			return Collections.emptyList();
		
		// Note : criteria.getRelations().contains(PSConstants.RELATION_FINANCE) filter is in rented-properties do we need to put here?
		if (properties.size() <= 1 || !CollectionUtils.isEmpty(criteria.getRelations())) {
			properties.stream().forEach(property -> {
				List<String> propertyDetailsIds = new ArrayList<>();
				propertyDetailsIds.add(property.getId());
				List<EstateDemand> demands = repository.getDemandDetailsForPropertyDetailsIds(propertyDetailsIds);
				List<EstatePayment> payments = repository.getEstatePaymentsForPropertyDetailsIds(propertyDetailsIds);
				
				EstateAccount estateAccount = repository
						.getPropertyRentAccountDetails(PropertyCriteria.builder().propertyId(property.getId()).build());
				
				if (!CollectionUtils.isEmpty(demands) && null != estateAccount) {
					property.setEstateRentSummary(estateRentCollectionService.calculateRentSummary(demands, estateAccount,
							property.getPropertyDetails().getInterestRate()));
					property.getPropertyDetails().setEstateDemands(demands);
					property.getPropertyDetails().setEstatePayments(payments);
					property.getPropertyDetails().setEstateAccount(estateAccount);
				}
			});
		}

		return properties;
	}
	
	
	public AccountStatementResponse searchPayments(AccountStatementCriteria accountStatementCriteria,
			RequestInfo requestInfo) {

		List<Property> properties = repository
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid())
						.relations(Collections.singletonList("finance")).build());
		if (CollectionUtils.isEmpty(properties)) {
			return AccountStatementResponse.builder().estateAccountStatements(Collections.emptyList())
					.build();
		}

		Property property = properties.get(0);
		List<EstateDemand> demands = repository
				.getDemandDetailsForPropertyDetailsIds(Collections.singletonList(property.getPropertyDetails().getId()));

		List<EstatePayment> payments = repository
				.getEstatePaymentsForPropertyDetailsIds(Collections.singletonList(property.getPropertyDetails().getId()));

		return AccountStatementResponse.builder()
				.estateAccountStatements(estateRentCollectionService.getAccountStatement(demands, payments,
						18.00,
						accountStatementCriteria.getFromDate(), accountStatementCriteria.getToDate()))
				.build();
	}

}
