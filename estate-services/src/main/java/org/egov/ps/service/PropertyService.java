package org.egov.ps.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.BillV2;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.model.RentAccount;
import org.egov.ps.model.RentSummary;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.service.calculation.IEstateRentCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
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
import org.egov.tracer.model.CustomException;
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
	private IRentCollectionService rentCollectionService;

	@Autowired
	private IEstateRentCollectionService estateRentCollectionService;

	@Autowired
	private UserService userService;

	@Autowired
	private Util utils;

	@Autowired
	private DemandService demandService;

	@Autowired
	private DemandRepository demandRepository;

	public List<Property> createProperty(PropertyRequest request) {
		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichPropertyRequest(request);
		producer.push(config.getSavePropertyTopic(), request);
		processRentSummary(request);
		return request.getProperties();
	}

	private void processRentSummary(PropertyRequest request) {
		request.getProperties().stream().filter(property -> property.getDemands() != null
				&& property.getPayments() != null && property.getEstateAccount() != null).forEach(property -> {
					property.setEstateRentSummary(
							estateRentCollectionService.calculateRentSummary(property.getDemands(),
									property.getEstateAccount(), property.getPropertyDetails().getInterestRate()));
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
		String action = request.getProperties().get(0).getAction();
		if (config.getIsWorkflowEnabled() && !action.contentEquals("") && !action.contentEquals(PSConstants.ES_DRAFT)) {
			wfIntegrator.callWorkFlow(request);
		}
		producer.push(config.getUpdatePropertyTopic(), request);

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

		// Note : criteria.getRelations().contains(PSConstants.RELATION_FINANCE) filter
		// is in rented-properties do we need to put here?
		if (properties.size() <= 1 || !CollectionUtils.isEmpty(criteria.getRelations())) {
			properties.stream().forEach(property -> {
				List<String> propertyDetailsIds = new ArrayList<>();
				propertyDetailsIds.add(property.getId());
				List<EstateDemand> demands = repository.getDemandDetailsForPropertyDetailsIds(propertyDetailsIds);
				List<EstatePayment> payments = repository.getEstatePaymentsForPropertyDetailsIds(propertyDetailsIds);

				EstateAccount estateAccount = repository.getPropertyEstateAccountDetails(
						PropertyCriteria.builder().propertyId(property.getId()).build());

				if (!CollectionUtils.isEmpty(demands) && null != estateAccount) {
					property.setEstateRentSummary(estateRentCollectionService.calculateRentSummary(demands,
							estateAccount, property.getPropertyDetails().getInterestRate()));
					property.setDemands(demands);
					property.setPayments(payments);
					property.setEstateAccount(estateAccount);
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
			return AccountStatementResponse.builder().rentAccountStatements(Collections.emptyList()).build();
		}

		Property property = properties.get(0);
		List<EstateDemand> demands = repository.getDemandDetailsForPropertyDetailsIds(
				Collections.singletonList(property.getPropertyDetails().getId()));

		List<EstatePayment> payments = repository.getEstatePaymentsForPropertyDetailsIds(
				Collections.singletonList(property.getPropertyDetails().getId()));

		return AccountStatementResponse.builder()
				.rentAccountStatements(rentCollectionService.getAccountStatement(demands, payments,
						property.getPropertyDetails().getInterestRate(), accountStatementCriteria.getFromDate(),
						accountStatementCriteria.getToDate()))
				.build();
	}

	public List<Property> generateFinanceDemand(PropertyRequest propertyRequest) {
		/**
		 * Validate not empty
		 */
		if (CollectionUtils.isEmpty(propertyRequest.getProperties())) {
			return Collections.emptyList();
		}
		Property propertyFromRequest = propertyRequest.getProperties().get(0);
		/**
		 * Validate that this is a valid property id.
		 */
		if (propertyFromRequest.getId() == null) {
			throw new CustomException(
					Collections.singletonMap("NO_PROPERTY_ID_FOUND", "No Property found to process rent"));
		}
		if (propertyFromRequest.getPaymentAmount() == null) {
			throw new CustomException(
					Collections.singletonMap("NO_PAYMENT_AMOUNT_FOUND", "No Property tenantId found to process rent"));
		}
		PropertyCriteria propertyCriteria = PropertyCriteria.builder().relations(Arrays.asList("owner"))
				.propertyId(propertyFromRequest.getId()).build();

		/**
		 * Retrieve properties from db with the given ids.
		 */
		List<Property> propertiesFromDB = repository.getProperties(propertyCriteria);
		if (CollectionUtils.isEmpty(propertiesFromDB)) {
			throw new CustomException(Collections.singletonMap("PROPERTIES_NOT_FOUND",
					"Could not find any valid properties with id " + propertyFromRequest.getId()));
		}

		Property property = propertiesFromDB.get(0);
		property.setPaymentAmount(propertyFromRequest.getPaymentAmount());
		property.setTransactionId(propertyFromRequest.getTransactionId());
		property.setBankName(propertyFromRequest.getBankName());
		Owner owner = utils.getCurrentOwnerFromProperty(property);

		/**
		 * Create egov user if not already present.
		 */
		userService.createUser(propertyRequest.getRequestInfo(), owner.getOwnerDetails().getMobileNumber(),
				owner.getOwnerDetails().getOwnerName(), property.getTenantId());

		/**
		 * Extract property detail ids.
		 */
		List<String> propertyDetailsIds = propertiesFromDB.stream()
				.map(propertyFromDb -> propertyFromDb.getPropertyDetails().getId()).collect(Collectors.toList());

		/**
		 * Generate Calculations for the property.
		 */
		List<EstateDemand> demands = repository.getDemandDetailsForPropertyDetailsIds(propertyDetailsIds);
		RentAccount account = repository.getPropertyRentAccountDetails(propertyCriteria);
		if (!CollectionUtils.isEmpty(demands) && null != account) {
			RentSummary rentSummary = rentCollectionService.calculateRentSummary(demands, account,
					property.getPropertyDetails().getInterestRate());
			enrichmentService.enrichRentDemand(property, rentSummary);
		}

		/**
		 * Generate an actual finance demand
		 */
		demandService.generateFinanceRentDemand(propertyRequest.getRequestInfo(), property);

		/**
		 * Get the bill generated.
		 */
		List<BillV2> bills = demandRepository.fetchBill(propertyRequest.getRequestInfo(), property.getTenantId(),
				property.getRentPaymentConsumerCode(), config.getAosBusinessServiceValue());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + property.getRentPaymentConsumerCode());
		}

		if (propertyRequest.getRequestInfo().getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_EMPLOYEE)) {
			/**
			 * if offline, create a payment.
			 */
			demandService.createCashPaymentProperty(propertyRequest.getRequestInfo(), property.getPaymentAmount(),
					bills.get(0).getId(), owner, config.getAosBusinessServiceValue());

			propertyRequest.setProperties(Collections.singletonList(property));
			producer.push(config.getUpdatePropertyTopic(), propertyRequest);

		} else {
			/**
			 * We return the property along with the consumerCode that we set earlier. Also
			 * save it so the consumer code gets persisted.
			 */
			propertyRequest.setProperties(Collections.singletonList(property));
			producer.push(config.getUpdatePropertyTopic(), propertyRequest);
		}
		return Collections.singletonList(property);
	}

}
