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
import org.egov.ps.model.ExtensionFee;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.OfflinePaymentDetails.OfflinePaymentType;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.service.calculation.ExtensionFeeCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.AccountStatementRequest;
import org.egov.ps.web.contracts.ExtensionFeeRequest;
import org.egov.ps.web.contracts.ExtensionFeeStatementResponse;
import org.egov.ps.web.contracts.ExtensionFeeStatementSummary;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ExtensionFeeService {

	@Autowired
	PropertyEnrichmentService propertyEnrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	@Autowired
	PropertyRepository repository;

	@Autowired
	ExtensionFeeCollectionService extensionFeeCollectionService;

	@Autowired
	DemandService demandService;

	@Autowired
	private Util utils;

	@Autowired
	private UserService userService;

	@Autowired
	private DemandRepository demandRepository;

	public List<ExtensionFee> createExtensionFee(ExtensionFeeRequest extensionFeeRequest) {
		propertyEnrichmentService.enrichExtensionFeeRequest(extensionFeeRequest);
		producer.push(config.getSaveExtensionFeeTopic(), extensionFeeRequest);
		return extensionFeeRequest.getExtensionFees();
	}

	public ExtensionFeeStatementResponse createExtensionFeeStatement(AccountStatementRequest statementRequest) {

		/* Set current date in a toDate if it is null */
		statementRequest.getCriteria()
				.setToDate(statementRequest.getCriteria().getToDate() == null ? new Date().getTime()
						: statementRequest.getCriteria().getToDate());
		AccountStatementCriteria statementCriteria = statementRequest.getCriteria();
		if (null == statementCriteria.getFromDate()) {
			throw new CustomException(Collections.singletonMap("NO_FROM_DATE", "From date should not be null"));
		}
		if (null == statementCriteria.getPropertyid()) {
			throw new CustomException(Collections.singletonMap("NO_PROPERTY_ID", "Property id should not be null"));
		}
		if (statementCriteria.getToDate() <= statementCriteria.getFromDate()) {
			throw new CustomException(
					Collections.singletonMap("NO_PROPER_DATE", "Statement from date should be greater than to date"));
		}

		Property property = repository.findPropertyById(statementCriteria.getPropertyid());
		if (null == property) {
			throw new CustomException(Collections.singletonMap("NO_PROPERTY_FOUND",
					"Property not found for the given property id: " + statementCriteria.getPropertyid()));
		}

		List<ExtensionFee> extensionFees = repository.getExtensionFeesForPropertyId(property.getId());
		List<ExtensionFee> filteredExtensionFees = extensionFees.stream()
				.filter(extensionFee -> extensionFee.getGenerationDate() >= statementCriteria.getFromDate()
						&& extensionFee.getGenerationDate() <= statementCriteria.getToDate())
				.collect(Collectors.toList());

		List<String> propertyDetailsIds = new ArrayList<String>();
		propertyDetailsIds.add(property.getPropertyDetails().getId());
		List<OfflinePaymentDetails> offlinePaymentDetails = repository
				.getOfflinePaymentsForPropertyDetailsIds(propertyDetailsIds);
		List<OfflinePaymentDetails> filteredOfflinePaymentDetails = offlinePaymentDetails.stream()
				.filter(offlinePaymentDetail -> null != offlinePaymentDetail.getType()
						&& offlinePaymentDetail.getType().equals(OfflinePaymentType.EXTENSIONFEE))
				.collect(Collectors.toList());

		double totalExtensionFee = filteredExtensionFees.stream().mapToDouble(ExtensionFee::getAmount).sum();
		double totalExtensionFeeDue = filteredExtensionFees.stream().mapToDouble(ExtensionFee::getRemainingDue).sum();

		BigDecimal totalExtensionFeePaid = filteredOfflinePaymentDetails.stream().map(OfflinePaymentDetails::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		ExtensionFeeStatementSummary extensionFeeStatementSummary = ExtensionFeeStatementSummary.builder()
				.totalExtensionFee(totalExtensionFee).totalExtensionFeeDue(totalExtensionFeeDue)
				.totalExtensionFeePaid(totalExtensionFeePaid.doubleValue()).build();

		ExtensionFeeStatementResponse extensionFeeStatementResponse = ExtensionFeeStatementResponse.builder()
				.extensionFees(filteredExtensionFees).offlinePaymentDetails(filteredOfflinePaymentDetails)
				.extensionFeeStatementSummary(extensionFeeStatementSummary).build();

		return extensionFeeStatementResponse;
	}

	public List<OfflinePaymentDetails> processExtensionFeePaymentRequest(PropertyRequest propertyRequest) {
		/**
		 * Validate not empty
		 */
		if (CollectionUtils.isEmpty(propertyRequest.getProperties())) {
			// return Collections.emptyList();
			return Collections.emptyList();
		}

		return propertyRequest.getProperties().stream().map(property -> {
			List<OfflinePaymentDetails> offlinePaymentDetails = this
					.processExtensionFeePayment(propertyRequest.getRequestInfo(), property);
			return offlinePaymentDetails.get(0);
		}).collect(Collectors.toList());
	}

	private List<OfflinePaymentDetails> processExtensionFeePayment(RequestInfo requestInfo, Property property) {
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
		List<ExtensionFee> extensionFees = repository.getExtensionFeesForPropertyId(propertyDb.getId());
		double totalDue = extensionFees.stream().filter(ExtensionFee::isUnPaid)
				.mapToDouble(ExtensionFee::getRemainingDue).sum();

		if (totalDue < paymentAmount) {
			throw new CustomException("DUE OVERFLOW", String.format(
					"Total due for all extension fees is only Rs%.2f. Please don't collect more amount than that.",
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

		String consumerCode = utils.getExtensionFeeConsumerCode(propertyDb.getFileNumber());
		/**
		 * Enrich an actual finance demand
		 */
		Calculation calculation = propertyEnrichmentService.enrichGenerateDemand(requestInfo, paymentAmount,
				consumerCode, propertyDb, PSConstants.EXTENSION_FEE);

		/**
		 * Generate an actual finance demand
		 */
		demandService.createPenaltyExtensionFeeDemand(requestInfo, propertyDb, consumerCode, calculation,
				PSConstants.EXTENSION_FEE);

		/**
		 * Get the bill generated.
		 */
		List<BillV2> bills = demandRepository.fetchBill(requestInfo, propertyDb.getTenantId(), consumerCode,
				propertyDb.getExtensionFeeBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + propertyDb.getExtensionFeeBusinessService());
		}

		demandService.createCashPaymentProperty(requestInfo, new BigDecimal(paymentAmount), bills.get(0).getId(), owner,
				propertyDb.getExtensionFeeBusinessService());

		offlinePaymentDetails.forEach(ofpd -> {
			ofpd.setId(UUID.randomUUID().toString());
			ofpd.setDemandId(bills.get(0).getBillDetails().get(0).getDemandId());
			ofpd.setType(OfflinePaymentType.EXTENSIONFEE);
			ofpd.setPropertyDetailsId(propertyDb.getPropertyDetails().getId());
		});

		List<ExtensionFee> unpaidExtensionFees = extensionFeeCollectionService.settle(extensionFees, paymentAmount);
		List<Property> properties = new ArrayList<Property>();
		properties.add(propertyDb);

		producer.push(config.getUpdateExtensionFeeTopic(), new ExtensionFeeRequest(requestInfo, unpaidExtensionFees));
		producer.push(config.getUpdatePropertyTopic(), new PropertyRequest(requestInfo, properties));
		return offlinePaymentDetails;
	}

}
