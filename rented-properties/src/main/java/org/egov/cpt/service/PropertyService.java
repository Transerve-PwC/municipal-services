package org.egov.cpt.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.AccountStatementCriteria;
import org.egov.cpt.models.BillV2;
import org.egov.cpt.models.OfflinePaymentDetails;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.PropertyDueAmount;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.models.RentSummary;
import org.egov.cpt.models.calculation.BusinessService;
import org.egov.cpt.models.calculation.State;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.service.calculation.DemandRepository;
import org.egov.cpt.service.calculation.DemandService;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.util.PropertyUtil;
import org.egov.cpt.validator.PropertyValidator;
import org.egov.cpt.web.contracts.AccountStatementResponse;
import org.egov.cpt.web.contracts.PropertyDueRequest;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.egov.cpt.workflow.WorkflowIntegrator;
import org.egov.cpt.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PropertyService {

	@Autowired
	private PropertyValidator propertyValidator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private UserService userService;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private PropertyRepository repository;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private RentEnrichmentService rentEnrichmentService;

	@Autowired
	private IRentCollectionService rentCollectionService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private PropertyUtil utils;

	@Autowired
	private DemandRepository demandRepository;
	
	@Autowired
	private NotificationUtil notificationUtil;


	public List<Property> createProperty(PropertyRequest request) {

		propertyValidator.validateCreateRequest(request);
		enrichmentService.enrichCreateRequest(request);
		processRentHistory(request);
		// userService.createUser(request);

		producer.push(config.getSavePropertyTopic(), request);

		processRentSummary(request);
		return request.getProperties();
	}

	private void processRentSummary(PropertyRequest request) {
		request.getProperties().stream().filter(property -> property.getDemands() != null
				&& property.getPayments() != null && property.getRentAccount() != null).forEach(property -> {
					property.setRentSummary(rentCollectionService.calculateRentSummary(property.getDemands(),
							property.getRentAccount(), property.getPropertyDetails().getInterestRate()));
				});
	}

	/**
	 * Updates the property
	 *
	 * @param request PropertyRequest containing list of properties to be update
	 * @return List of updated properties
	 */
	public List<Property> updateProperty(PropertyRequest request) {
		List<Property> propertyFromSearch = propertyValidator.validateUpdateRequest(request);
		enrichmentService.enrichUpdateRequest(request, propertyFromSearch);
		processRentHistory(request);
		// userService.createUser(request);
		String action = request.getProperties().get(0).getMasterDataAction();
		String state = request.getProperties().get(0).getMasterDataState();
		if ((config.getIsWorkflowEnabled() && !action.equalsIgnoreCase(""))
				&& (!state.equalsIgnoreCase(PTConstants.PM_STATUS_APPROVED))) {
			wfIntegrator.callWorkFlow(request);
		}
		producer.push(config.getUpdatePropertyTopic(), request);

		processRentSummary(request);
		return request.getProperties();
	}

	private void processRentHistory(PropertyRequest request) {
		rentEnrichmentService.enrichRentdata(request);
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().stream().filter(property -> property.getDemands() != null
					&& property.getPayments() != null && property.getRentAccount() != null).forEach(property -> {
						property.setRentCollections(
								rentCollectionService.settle(property.getDemands(), property.getPayments(),
										property.getRentAccount(), property.getPropertyDetails().getInterestRate()));
					});
		}
		rentEnrichmentService.enrichCollection(request);
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
		List<RentDemand> demands = repository
				.getPropertyRentDemandDetails(PropertyCriteria.builder().propertyId(property.getId()).build());

		List<RentPayment> payments = repository
				.getPropertyRentPaymentDetails(PropertyCriteria.builder().propertyId(property.getId()).build());

		return AccountStatementResponse.builder()
				.rentAccountStatements(rentCollectionService.getAccountStatement(demands, payments,
						property.getPropertyDetails().getInterestRate(), accountStatementCriteria.getFromDate(),
						accountStatementCriteria.getToDate()))
				.build();
	}

	public List<Property> searchProperty(PropertyCriteria criteria, RequestInfo requestInfo) {
		if (criteria.isEmpty()) {
			String wfbusinessServiceName = PTConstants.BUSINESS_SERVICE_PM;
			BusinessService otBusinessService = workflowService.getBusinessService(criteria.getTenantId(), requestInfo,
					wfbusinessServiceName);
			List<State> stateList = otBusinessService.getStates();
			List<String> states = new ArrayList<String>();

			for (State state : stateList) {
				states.add(state.getState());
			}
			states.remove("");
			states.remove(PTConstants.PM_DRAFTED);
			log.info("states:" + states);
			criteria.setState(states);
			criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
		} else {
			if (!CollectionUtils.isEmpty(criteria.getState()) && criteria.getState().contains(PTConstants.PM_DRAFTED))
				criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
		}
		List<Property> properties = repository.getProperties(criteria);
		if (CollectionUtils.isEmpty(properties))
			return Collections.emptyList();

		if (properties.size() <= 1 || !CollectionUtils.isEmpty(criteria.getRelations())
				&& criteria.getRelations().contains(PTConstants.RELATION_FINANCE)) {
			properties.stream().forEach(property -> {
				List<RentDemand> demands = repository
						.getPropertyRentDemandDetails(PropertyCriteria.builder().propertyId(property.getId()).build());
				List<RentPayment> payments = repository
						.getPropertyRentPaymentDetails(PropertyCriteria.builder().propertyId(property.getId()).build());
				RentAccount rentAccount = repository
						.getPropertyRentAccountDetails(PropertyCriteria.builder().propertyId(property.getId()).build());
				if (!CollectionUtils.isEmpty(demands) && null != rentAccount) {
					property.setRentSummary(rentCollectionService.calculateRentSummary(demands, rentAccount,
							property.getPropertyDetails().getInterestRate()));
					property.setDemands(demands);
					property.setPayments(payments);
					property.setRentAccount(rentAccount);
				}
			});
		}

		if (properties.size() <= 1 && !CollectionUtils.isEmpty(criteria.getRelations())
				&& criteria.getRelations().contains(PTConstants.RELATION_OPD)) {
			properties.stream().forEach(property -> {
				List<OfflinePaymentDetails> offlinePaymentDetails = repository.getPropertyOfflinePaymentDetails(
						PropertyCriteria.builder().propertyId(property.getId()).build());
				if (!CollectionUtils.isEmpty(offlinePaymentDetails)) {
					property.setOfflinePaymentDetails(offlinePaymentDetails);
				}
			});
		}
		return properties;
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
		userService.createUser(propertyRequest.getRequestInfo(), owner.getOwnerDetails().getPhone()
				,owner.getOwnerDetails().getName(),property.getTenantId());

		/**
		 * Generate Calculations for the property.
		 */
		List<RentDemand> demands = repository.getPropertyRentDemandDetails(propertyCriteria);
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
				property.getRentPaymentConsumerCode(),property.getBillingBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + property.getRentPaymentConsumerCode());
		}

		if (propertyRequest.getRequestInfo().getUserInfo().getType().equalsIgnoreCase(PTConstants.ROLE_EMPLOYEE)) {
			/**
			 * if offline, create a payment.
			 */
			demandService.createCashPayment(propertyRequest.getRequestInfo(), property.getPaymentAmount(),
					bills.get(0).getId(), owner,property.getBillingBusinessService());

			OfflinePaymentDetails offlinePaymentDetails = OfflinePaymentDetails.builder()
					.id(UUID.randomUUID().toString()).propertyId(property.getId())
					.demandId(bills.get(0).getBillDetails().get(0).getDemandId())
					.amount(property.getPaymentAmount())
					.bankName(property.getBankName()).transactionNumber(property.getTransactionId()).build();
			property.setOfflinePaymentDetails(Collections.singletonList(offlinePaymentDetails));
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

	public void getDueAmount( RequestInfo requestInfo) {
		PropertyCriteria criteria = PropertyCriteria.builder()
				.state(Arrays.asList(PTConstants.PM_STATUS_APPROVED))
				.relations(Arrays.asList(PTConstants.RELATION_OWNER))
				.build();
		List<String> propertyIds = repository.getPropertyIds(criteria);
		if (CollectionUtils.isEmpty(propertyIds))
			throw new CustomException("NO_PROPERTY_FOUND","No approved property found");
			
		List<PropertyDueAmount> PropertyDueAmounts=new ArrayList<>();
		String localizationMessages = notificationUtil.getLocalizationMessages("ch.chandigarh", requestInfo);
		
		propertyIds.stream().forEach(property -> {
				criteria.setPropertyId(property); 
				criteria.setLimit(1L);
				List<Property> singleProperty = repository.getPropertyOwner(criteria);
				PropertyDueAmount propertyDueAmount = PropertyDueAmount.builder().propertyId(singleProperty.get(0).getId())
						.transitNumber(singleProperty.get(0).getTransitNumber())
						.tenantId(singleProperty.get(0).getTenantId())
						.colony(notificationUtil.getMessageTemplate(singleProperty.get(0).getColony(), localizationMessages))
						.ownerName(singleProperty.get(0).getOwners().get(0).getOwnerDetails().getName())
						.mobileNumber(singleProperty.get(0).getOwners().get(0).getOwnerDetails().getPhone())
						.build();
				List<RentDemand> demands = repository
						.getPropertyRentDemandDetails(PropertyCriteria.builder().propertyId(property).build());
				RentAccount rentAccount = repository
						.getPropertyRentAccountDetails(PropertyCriteria.builder().propertyId(property).build());
				if (!CollectionUtils.isEmpty(demands) && null != rentAccount) {
					propertyDueAmount.setRentSummary(rentCollectionService.calculateRentSummary(demands, rentAccount,
							singleProperty.get(0).getPropertyDetails().getInterestRate()));
				}
				PropertyDueAmounts.add(propertyDueAmount);
			});
			
			PropertyDueRequest propertyDueRequest =  PropertyDueRequest.builder().requestInfo(requestInfo)
					.propertyDueAmounts(PropertyDueAmounts).build();
			producer.push(config.getDueAmountTopic(), propertyDueRequest);	
	}
}
