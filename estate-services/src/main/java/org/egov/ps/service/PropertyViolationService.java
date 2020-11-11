package org.egov.ps.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.BillV2;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.service.calculation.PenaltyCollectionService;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyViolationService {

	@Autowired
	PropertyEnrichmentService propertyEnrichmentService;

	@Autowired
	private DemandService demandService;

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

	public List<PropertyPenalty> penalty(PropertyPenaltyRequest propertyPenaltyRequest) {
		propertyEnrichmentService.enrichPenalty(propertyPenaltyRequest);
//		demandService.createPenaltyDemand(propertyPenaltyRequest.getRequestInfo(),
//				propertyPenaltyRequest.getPropertyPenalties());
		producer.push(config.getSavePenaltyTopic(), propertyPenaltyRequest);
		return propertyPenaltyRequest.getPropertyPenalties();
	}

	public List<PropertyPenalty> generatePenaltyDemand(PropertyPenaltyRequest propertyPenaltyRequest) {
		/**
		 * Validate not empty
		 */
		if (CollectionUtils.isEmpty(propertyPenaltyRequest.getPropertyPenalties())) {
			return Collections.emptyList();
		}
		PropertyPenalty propertyPenaltyFromRequest = propertyPenaltyRequest.getPropertyPenalties().get(0);

		if (null == propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getAmount()) {
			throw new CustomException(
					Collections.singletonMap("NO_PAYMENT_AMOUNT_FOUND", "Payment amount should not be empty"));
		}

		Property property = repository.findPropertyById(propertyPenaltyFromRequest.getProperty().getId());
		Owner owner = utils.getCurrentOwnerFromProperty(property);

		/**
		 * Create egov user if not already present.
		 */
		userService.createUser(propertyPenaltyRequest.getRequestInfo(), owner.getOwnerDetails().getMobileNumber(),
				owner.getOwnerDetails().getOwnerName(), owner.getTenantId());

		/**
		 * Generate Calculations for the property.
		 */
		List<PropertyPenalty> demands = repository
				.getPenaltyDemandsForPropertyId(propertyPenaltyFromRequest.getProperty().getId());
		PropertyPenalty propertyPenaltyFromDb = null;
		if (!CollectionUtils.isEmpty(demands)) {
			propertyPenaltyFromDb = demands.get(0);
//			demands.get(0).setPenaltyCollection(propertyPenaltyFromRequest.getPenaltyCollection());
		} else {
			throw new CustomException(Collections.singletonMap("NO_PENALTIES_FOUND",
					"THERE SHOULD BE PENALTY TO PAY THE PENALTY AMOUNT"));
		}

		String consumerCode = utils.getPropertyPenaltyConsumerCode(property.getFileNumber());
		if (!CollectionUtils.isEmpty(demands)) {
			propertyPenaltyFromRequest.setTenantId(propertyPenaltyFromDb.getTenantId());
			propertyPenaltyFromRequest.setPenaltyNumber(propertyPenaltyFromDb.getPenaltyNumber());
			propertyPenaltyFromRequest.setBranchType(propertyPenaltyFromDb.getBranchType());
			propertyPenaltyFromRequest.setId(propertyPenaltyFromDb.getId());
//			propertyPenaltyFromRequest.setAuditDetails(propertyPenaltyFromDb.getAuditDetails());

			/**
			 * Enrich an actual finance demand
			 */
			propertyEnrichmentService.enrichGenerateDemand(propertyPenaltyFromRequest,
					propertyPenaltyRequest.getRequestInfo(), consumerCode);

		}

		/**
		 * Generate an actual finance demand
		 */
		demandService.createPenaltyDemand(propertyPenaltyRequest.getRequestInfo(),
				propertyPenaltyRequest.getPropertyPenalties(), consumerCode);

		/**
		 * Get the bill generated.
		 */
		List<BillV2> bills = demandRepository.fetchBill(propertyPenaltyRequest.getRequestInfo(),
				propertyPenaltyFromRequest.getTenantId(), consumerCode,
				propertyPenaltyFromRequest.getPenaltyBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED", "No bills were found for the consumer code "
					+ propertyPenaltyFromRequest.getPenaltyBusinessService());
		}

		demandService.createCashPaymentProperty(propertyPenaltyRequest.getRequestInfo(),
				propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getAmount(), bills.get(0).getId(), owner,
				propertyPenaltyFromRequest.getPenaltyBusinessService());

		OfflinePaymentDetails offlinePenaltyPaymentDetails = OfflinePaymentDetails.builder()
				.id(UUID.randomUUID().toString()).propertyDetailsId(property.getPropertyDetails().getId())
				.demandId(bills.get(0).getBillDetails().get(0).getDemandId())
				.amount(propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getAmount())
				.bankName(propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getBankName())
				.transactionNumber(propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getTransactionNumber())
				.dateOfPayment(propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getDateOfPayment()).build();

		List<PropertyPenalty> updatedPenaltyDetailsSettle = penaltyCollectionService.settle(demands,
				propertyPenaltyFromRequest.getOfflinePaymentDetails().get(0).getAmount().doubleValue());

		updatedPenaltyDetailsSettle.get(0)
				.setOfflinePaymentDetails(Collections.singletonList(offlinePenaltyPaymentDetails));

//		propertyPenaltyRequest.setPropertyPenalties(updatedPenaltyDetailsSettle);

		PropertyPenaltyRequest ppr = new PropertyPenaltyRequest(propertyPenaltyRequest.getRequestInfo(),
				updatedPenaltyDetailsSettle);
		producer.push(config.getUpdatePenaltyTopic(), ppr);

		return Collections.singletonList(propertyPenaltyFromRequest);
	}

}
