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
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SecurityDepositService {

	@Autowired
	PropertyEnrichmentService propertyEnrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	PropertyRepository repository;

	@Autowired
	DemandService demandService;

	@Autowired
	private Util utils;

	@Autowired
	private UserService userService;

	@Autowired
	private DemandRepository demandRepository;

	public List<OfflinePaymentDetails> processSecurityDepositPaymentRequest(PropertyRequest propertyRequest) {
		/**
		 * Validate not empty
		 */
		if (CollectionUtils.isEmpty(propertyRequest.getProperties())) {
			// return Collections.emptyList();
			return Collections.emptyList();
		}

		return propertyRequest.getProperties().stream().map(property -> {
			List<OfflinePaymentDetails> offlinePaymentDetails = this
					.processSecurityDepositPayment(propertyRequest.getRequestInfo(), property);
			return offlinePaymentDetails.get(0);
		}).collect(Collectors.toList());
	}

	private List<OfflinePaymentDetails> processSecurityDepositPayment(RequestInfo requestInfo, Property property) {
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

		BigDecimal paymentAmount = offlinePaymentDetails.get(0).getAmount();

		/**
		 * Calculate remaining due.
		 */
		BigDecimal totalDue = propertyDb.getPropertyDetails().getPaymentConfig().getSecurityAmount();

		if (paymentAmount.compareTo(totalDue) == 1) {
			throw new CustomException("DUE OVERFLOW", String.format(
					"Total security fee due is only Rs%.2f. Please don't collect more amount than that.", totalDue));
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

		String consumerCode = utils.getSecurityDepositConsumerCode(propertyDb.getFileNumber());
		/**
		 * Enrich an actual finance demand
		 */
		Calculation calculation = propertyEnrichmentService.enrichGenerateDemand(requestInfo,
				paymentAmount.doubleValue(), consumerCode, propertyDb, PSConstants.SECURITY_DEPOSIT);

		/**
		 * Generate an actual finance demand
		 */
		demandService.createPenaltyExtensionFeeDemand(requestInfo, propertyDb, consumerCode, calculation,
				PSConstants.SECURITY_DEPOSIT);

		/**
		 * Get the bill generated.
		 */
		List<BillV2> bills = demandRepository.fetchBill(requestInfo, propertyDb.getTenantId(), consumerCode,
				propertyDb.getSecuritynDepositBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + propertyDb.getSecuritynDepositBusinessService());
		}

		demandService.createCashPaymentProperty(requestInfo, paymentAmount, bills.get(0).getId(), owner,
				propertyDb.getSecuritynDepositBusinessService());

		offlinePaymentDetails.forEach(ofpd -> {
			ofpd.setId(UUID.randomUUID().toString());
			ofpd.setDemandId(bills.get(0).getBillDetails().get(0).getDemandId());
			ofpd.setType(OfflinePaymentType.SECURITY);
			ofpd.setPropertyDetailsId(propertyDb.getPropertyDetails().getId());
		});

//		propertyDb.getPropertyDetails().getPaymentConfig().setSecurityAmount(totalDue.subtract(paymentAmount));
		List<Property> properties = new ArrayList<Property>();
		properties.add(propertyDb);

		producer.push(config.getUpdatePropertyTopic(), new PropertyRequest(requestInfo, properties));
		return offlinePaymentDetails;
	}

}
