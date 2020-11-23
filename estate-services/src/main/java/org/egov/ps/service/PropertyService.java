package org.egov.ps.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.BillV2;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
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
import org.egov.ps.web.contracts.EstateRentSummary;
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
		processRentHistory(request);
		producer.push(config.getSavePropertyTopic(), request);
		processRentSummary(request);
		return request.getProperties();
	}

	private void processRentSummary(PropertyRequest request) {
		request.getProperties().stream()
				.filter(property -> property.getPropertyDetails().getEstateDemands() != null
						&& property.getPropertyDetails().getEstatePayments() != null
						&& property.getPropertyDetails().getEstateAccount() != null
						&& property.getPropertyDetails().getPaymentConfig() != null)
				.forEach(property -> {
					estateRentCollectionService.settle(property.getPropertyDetails().getEstateDemands(),
							property.getPropertyDetails().getEstatePayments(),
							property.getPropertyDetails().getEstateAccount(), 18,
							property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
							property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue());
					property.setEstateRentSummary(

							estateRentCollectionService.calculateRentSummary(
									property.getPropertyDetails().getEstateDemands(),
									property.getPropertyDetails().getEstateAccount(),
									property.getPropertyDetails().getInterestRate(),
									property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
									property.getPropertyDetails().getPaymentConfig().getRateOfInterest()
											.doubleValue()));
				});
	}

	private void processRentHistory(PropertyRequest request) {
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().stream()
					.filter(property -> property.getPropertyDetails().getEstateDemands() != null
							&& property.getPropertyDetails().getEstatePayments() != null
							&& property.getPropertyDetails().getEstateAccount() != null
							&& property.getPropertyDetails().getPaymentConfig() != null)
					.forEach(property -> {
						property.getPropertyDetails().setEstateRentCollections(estateRentCollectionService.settle(
								property.getPropertyDetails().getEstateDemands(),
								property.getPropertyDetails().getEstatePayments(),
								property.getPropertyDetails().getEstateAccount(), 18,
								property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
								property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue()));
					});
		}
		enrichmentService.enrichCollection(request);

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
		String state = request.getProperties().get(0).getState();
		if (config.getIsWorkflowEnabled() && !action.contentEquals("") && !action.contentEquals(PSConstants.ES_DRAFT)
				&& !state.contentEquals(PSConstants.PM_APPROVED)) {
			wfIntegrator.callWorkFlow(request);
		}
		if (!CollectionUtils.isEmpty(request.getProperties().get(0).getPropertyDetails().getBidders())) {
			String roeAction = request.getProperties().get(0).getPropertyDetails().getBidders().get(0).getAction();
			if (config.getIsWorkflowEnabled() && !roeAction.contentEquals("")
					&& state.contentEquals(PSConstants.PM_APPROVED)) {
				wfIntegrator.callWorkFlow(request);
			}
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
			criteria.setFileNumber(criteria.getFileNumber().trim().toUpperCase());
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
				propertyDetailsIds.add(property.getPropertyDetails().getId());
				List<EstateDemand> demands = repository.getDemandDetailsForPropertyDetailsIds(propertyDetailsIds);
				List<EstatePayment> payments = repository.getEstatePaymentsForPropertyDetailsIds(propertyDetailsIds);

				EstateAccount estateAccount = repository.getPropertyEstateAccountDetails(propertyDetailsIds);

				if (!CollectionUtils.isEmpty(demands)
					&& property.getPropertyDetails().getPaymentConfig() != null) {
					estateRentCollectionService.settle(demands, payments, estateAccount, 18,
							property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
							property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue());
					property.setEstateRentSummary(estateRentCollectionService.calculateRentSummary(demands,
							estateAccount, property.getPropertyDetails().getInterestRate(),
							property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
							property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue()));
					property.getPropertyDetails().setEstateDemands(demands);
					property.getPropertyDetails().setEstatePayments(payments);

				}
				if (estateAccount != null) {
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
			return AccountStatementResponse.builder().estateAccountStatements(Collections.emptyList()).build();
		}

		Property property = properties.get(0);
		List<EstateDemand> demands = repository.getDemandDetailsForPropertyDetailsIds(
				Collections.singletonList(property.getPropertyDetails().getId()));

		List<EstatePayment> payments = repository.getEstatePaymentsForPropertyDetailsIds(
				Collections.singletonList(property.getPropertyDetails().getId()));

		return AccountStatementResponse.builder()
				.estateAccountStatements(estateRentCollectionService.getAccountStatement(demands, payments, 18.00,
						accountStatementCriteria.getFromDate(), accountStatementCriteria.getToDate(),
						property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
						property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue()))
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
		if (propertyFromRequest.getPropertyDetails().getOfflinePaymentDetails().get(0).getAmount() == null) {
			throw new CustomException(
					Collections.singletonMap("NO_PAYMENT_AMOUNT_FOUND", "Payment amount should not be empty"));
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
		EstateAccount account = repository.getAccountDetailsForPropertyDetailsIds(propertyDetailsIds);

		if (!CollectionUtils.isEmpty(demands) && null != account) {
			List<EstatePayment> payments = repository.getEstatePaymentsForPropertyDetailsIds(propertyDetailsIds);
			estateRentCollectionService.settle(demands, payments, account, 18,
					property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
					property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue());
			EstateRentSummary rentSummary = estateRentCollectionService.calculateRentSummary(demands, account,
					property.getPropertyDetails().getInterestRate(),
					property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
					property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue());
			property.getPropertyDetails()
					.setOfflinePaymentDetails(propertyFromRequest.getPropertyDetails().getOfflinePaymentDetails());
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
				property.getRentPaymentConsumerCode(), property.getPropertyDetails().getBillingBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + property.getRentPaymentConsumerCode());
		}

		if (propertyRequest.getRequestInfo().getUserInfo().getType().equalsIgnoreCase(PSConstants.ROLE_EMPLOYEE)) {
			/**
			 * if offline, create a payment.
			 */
			demandService.createCashPaymentProperty(propertyRequest.getRequestInfo(),
					property.getPropertyDetails().getOfflinePaymentDetails().get(0).getAmount(), bills.get(0).getId(),
					owner, config.getAosBusinessServiceValue());

			OfflinePaymentDetails offlinePaymentDetails = OfflinePaymentDetails.builder()
					.id(UUID.randomUUID().toString()).propertyDetailsId(property.getPropertyDetails().getId())
					.demandId(bills.get(0).getBillDetails().get(0).getDemandId())
					.amount(property.getPropertyDetails().getOfflinePaymentDetails().get(0).getAmount())
					.bankName(property.getPropertyDetails().getOfflinePaymentDetails().get(0).getBankName())
					.transactionNumber(
							property.getPropertyDetails().getOfflinePaymentDetails().get(0).getTransactionNumber())
					.build();
			property.getPropertyDetails().setOfflinePaymentDetails(Collections.singletonList(offlinePaymentDetails));

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
