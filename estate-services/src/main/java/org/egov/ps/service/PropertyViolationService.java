package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.BillV2;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.OfflinePaymentDetails.OfflinePaymentType;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.service.calculation.PenaltyCollectionService;
import org.egov.ps.util.Util;
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
				consumerCode, propertyDb);

		/**
		 * Generate an actual finance demand
		 */
		demandService.createPenaltyDemand(requestInfo, propertyDb, consumerCode, calculation);

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
		});

		List<PropertyPenalty> updatedPenalties = penaltyCollectionService.settle(penalties, paymentAmount);
		List<Property> properties = new ArrayList<Property>();
		properties.add(propertyDb);

		producer.push(config.getUpdatePenaltyTopic(), new PropertyPenaltyRequest(requestInfo, updatedPenalties));
		producer.push(config.getUpdatePropertyTopic(), new PropertyRequest(requestInfo, properties));
		return offlinePaymentDetails;
	}

}
