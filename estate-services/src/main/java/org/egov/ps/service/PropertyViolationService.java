package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.BillV2;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.OfflinePaymentDetails.OfflinePaymentType;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.service.calculation.PenaltyCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.AccountStatementRequest;
import org.egov.ps.web.contracts.PenaltyStatementResponse;
import org.egov.ps.web.contracts.PenaltyStatementSummary;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyViolationService {

	@Autowired
	PropertyEnrichmentService propertyEnrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	PropertyRepository repository;

	@Autowired
	private Util utils;

	@Autowired
	private UserService userService;

	@Autowired
	private DemandRepository demandRepository;

	@Autowired
	PenaltyCollectionService penaltyCollectionService;

	@Autowired
	DemandService demandService;

	public List<PropertyPenalty> createPenalty(PropertyPenaltyRequest propertyPenaltyRequest) {
		propertyEnrichmentService.enrichPenaltyRequest(propertyPenaltyRequest);
		producer.push(config.getSavePenaltyTopic(), propertyPenaltyRequest);
		return propertyPenaltyRequest.getPropertyPenalties();
	}

	public List<OfflinePaymentDetails> processPropertyPenaltyPaymentRequest(PropertyRequest propertyRequest) {
		/**
		 * Validate not empty
		 */
		if (CollectionUtils.isEmpty(propertyRequest.getProperties())) {
			// return Collections.emptyList();
			return Collections.emptyList();
		}

		return propertyRequest.getProperties().stream().map(property -> {
			List<OfflinePaymentDetails> offlinePaymentDetails = this
					.processPropertyPenaltyPayment(propertyRequest.getRequestInfo(), property);
			return offlinePaymentDetails.get(0);
		}).collect(Collectors.toList());
	}

	private List<OfflinePaymentDetails> processPropertyPenaltyPayment(RequestInfo requestInfo, Property property) {
		List<OfflinePaymentDetails> offlinePaymentDetails = property.getPropertyDetails().getOfflinePaymentDetails();

		/**
		 * Get property from db to enrich property from request to send to
		 * update-property-topic.
		 */
		Property propertyDb = repository.findPropertyById(property.getId());
		propertyDb.getPropertyDetails().setOfflinePaymentDetails(offlinePaymentDetails);

		if (CollectionUtils.isEmpty(offlinePaymentDetails)) {
			throw new CustomException(
					Collections.singletonMap("NO_PAYMENT_AMOUNT_FOUND", "Payment amount should not be empty"));
		}

		if (offlinePaymentDetails.size() > 1) {
			throw new CustomException(Collections.singletonMap("ONLY_ONE_PAYMENT_ACCEPTED",
					"Only one payment can be accepted at a time"));
		}

		double paymentAmount = offlinePaymentDetails.get(0).getAmount().doubleValue();

		if (paymentAmount <= 0) {
			throw new CustomException("Invalid Amount", "Payable amount should not less than or equals 0");
		}

		/**
		 * Calculate remaining due.
		 */
		List<PropertyPenalty> penalties = repository.getPenaltyDemandsForPropertyId(propertyDb.getId());
		double totalDue = penalties.stream().filter(PropertyPenalty::isUnPaid)
				.mapToDouble(PropertyPenalty::getRemainingPenaltyDue).sum();

		if (totalDue < paymentAmount) {
			throw new CustomException("DUE OVERFLOW",
					String.format(
							"Total due for all penalties is only Rs%.2f. Please don't collect more amount than that.",
							totalDue));
		}

		/**
		 * Create egov user if not already present.
		 */
		Owner owner = utils.getCurrentOwnerFromProperty(propertyDb);
		userService.createUser(requestInfo, owner.getOwnerDetails().getMobileNumber(),
				owner.getOwnerDetails().getOwnerName(), owner.getTenantId());

		/**
		 * Generate Calculations for the property.
		 */

		String consumerCode = utils.getPropertyPenaltyConsumerCode(propertyDb.getFileNumber());
		/**
		 * Enrich an actual finance demand
		 */
		Calculation calculation = propertyEnrichmentService.enrichGenerateDemand(requestInfo, paymentAmount,
				consumerCode, propertyDb, PSConstants.PROPERTY_VIOLATION);

		/**
		 * Generate an actual finance demand
		 */
		demandService.createPenaltyExtensionFeeDemand(requestInfo, propertyDb, consumerCode, calculation,
				PSConstants.PROPERTY_VIOLATION);

		/**
		 * Get the bill generated.
		 */
		List<BillV2> bills = demandRepository.fetchBill(requestInfo, propertyDb.getTenantId(), consumerCode,
				propertyDb.getPenaltyBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + propertyDb.getPenaltyBusinessService());
		}

		demandService.createCashPaymentProperty(requestInfo, new BigDecimal(paymentAmount), bills.get(0).getId(), owner,
				propertyDb.getPenaltyBusinessService());

		offlinePaymentDetails.forEach(ofpd -> {
			ofpd.setId(UUID.randomUUID().toString());
			ofpd.setDemandId(bills.get(0).getBillDetails().get(0).getDemandId());
			ofpd.setType(OfflinePaymentType.PENALTY);
			ofpd.setPropertyDetailsId(propertyDb.getPropertyDetails().getId());
			ofpd.setTenantId(propertyDb.getTenantId());
			ofpd.setFileNumber(propertyDb.getFileNumber());
			ofpd.setConsumerCode(consumerCode);
			ofpd.setBillingBusinessService(propertyDb.getPenaltyBusinessService());
		});

		List<PropertyPenalty> updatedPenalties = penaltyCollectionService.settle(penalties, paymentAmount);
		List<Property> properties = new ArrayList<Property>();
		properties.add(propertyDb);

		producer.push(config.getUpdatePenaltyTopic(), new PropertyPenaltyRequest(requestInfo, updatedPenalties));
		producer.push(config.getUpdatePropertyTopic(), new PropertyRequest(requestInfo, properties));
		return offlinePaymentDetails;
	}

	public PenaltyStatementResponse createPenaltyStatement(AccountStatementRequest accountStatementRequest) {

		/* Set current date in a toDate if it is null */
		accountStatementRequest.getCriteria()
				.setToDate(accountStatementRequest.getCriteria().getToDate() == null ? new Date().getTime()
						: accountStatementRequest.getCriteria().getToDate());
		AccountStatementCriteria accountStatementCriteria = accountStatementRequest.getCriteria();
		if (null == accountStatementCriteria.getFromDate()) {
			throw new CustomException(Collections.singletonMap("NO_FROM_DATE", "From date should not be null"));
		}
		if (null == accountStatementCriteria.getPropertyid()) {
			throw new CustomException(Collections.singletonMap("NO_PROPERTY_ID", "Property id should not be null"));
		}
		if (accountStatementCriteria.getToDate() <= accountStatementCriteria.getFromDate()) {
			throw new CustomException(
					Collections.singletonMap("NO_PROPER_DATE", "Statement from date should be greater than to date"));
		}

		Property property = repository.findPropertyById(accountStatementCriteria.getPropertyid());
		if (null == property) {
			throw new CustomException(Collections.singletonMap("NO_PROPERTY_FOUND",
					"Property not found for the given property id: " + accountStatementCriteria.getPropertyid()));
		}

		List<PropertyPenalty> propertyPenalties = repository.getPenaltyDemandsForPropertyId(property.getId());
		List<PropertyPenalty> filteredPropertyPenalties = propertyPenalties.stream()
				.filter(propertyPenalty -> propertyPenalty.getGenerationDate() >= accountStatementCriteria.getFromDate()
						&& propertyPenalty.getGenerationDate() <= accountStatementCriteria.getToDate())
				.collect(Collectors.toList());

		List<String> propertyDetailsIds = new ArrayList<String>();
		propertyDetailsIds.add(property.getPropertyDetails().getId());
		List<String> relations = new ArrayList<String>();
		relations.add(PSConstants.RELATION_OPD);
		PropertyCriteria criteria = PropertyCriteria.builder().relations(relations).build();
		List<OfflinePaymentDetails> offlinePaymentDetails = repository
				.getOfflinePaymentsForPropertyDetailsIds(propertyDetailsIds, criteria);
		List<OfflinePaymentDetails> filteredOfflinePaymentDetails = offlinePaymentDetails.stream()
				.filter(offlinePaymentDetail -> null != offlinePaymentDetail.getType()
						&& offlinePaymentDetail.getType().equals(OfflinePaymentType.PENALTY))
				.collect(Collectors.toList());

		double totalPenalty = filteredPropertyPenalties.stream().mapToDouble(PropertyPenalty::getPenaltyAmount).sum();
		double totalPenaltyDue = filteredPropertyPenalties.stream().mapToDouble(PropertyPenalty::getRemainingPenaltyDue)
				.sum();
		BigDecimal totalPenaltyPaid = filteredOfflinePaymentDetails.stream().map(OfflinePaymentDetails::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		PenaltyStatementSummary penaltyStatementSummary = PenaltyStatementSummary.builder().totalPenalty(totalPenalty)
				.totalPenaltyDue(totalPenaltyDue).totalPenaltyPaid(totalPenaltyPaid.doubleValue()).build();

		PenaltyStatementResponse penaltyStatementResponse = PenaltyStatementResponse.builder()
				.propertyPenalties(filteredPropertyPenalties).offlinePaymentDetails(filteredOfflinePaymentDetails)
				.penaltyStatementSummary(penaltyStatementSummary).build();

		return penaltyStatementResponse;
	}

}
