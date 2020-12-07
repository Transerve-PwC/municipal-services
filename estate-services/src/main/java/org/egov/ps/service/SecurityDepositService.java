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
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.ps.web.contracts.SecurityDepositStatementResponse;
import org.egov.ps.web.contracts.SecurityDepositStatementSummary;
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
		if (paymentAmount.doubleValue() <= 0) {
			throw new CustomException("Invalid Amount", "Payable amount should not less than or equals 0");
		}

		/**
		 * Calculate remaining due from statement.
		 */
		SecurityDepositStatementResponse securityDepositStatementResponse = createSecurityDepositStatement(
				property.getId());
		double totalSecurityDepositDue = securityDepositStatementResponse.getSecurityDepositStatementSummary()
				.getTotalSecurityDepositDue();
		if (paymentAmount.doubleValue() > totalSecurityDepositDue) {
			throw new CustomException("DUE OVERFLOW",
					String.format(
							"Total security deposit due is only Rs%.2f. Please don't collect more amount than that.",
							totalSecurityDepositDue));
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
				propertyDb.getSecurityDepositBusinessService());
		if (CollectionUtils.isEmpty(bills)) {
			throw new CustomException("BILL_NOT_GENERATED",
					"No bills were found for the consumer code " + propertyDb.getSecurityDepositBusinessService());
		}

		demandService.createCashPaymentProperty(requestInfo, paymentAmount, bills.get(0).getId(), owner,
				propertyDb.getSecurityDepositBusinessService());

		AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		offlinePaymentDetails.forEach(ofpd -> {
			ofpd.setId(UUID.randomUUID().toString());
			ofpd.setDemandId(bills.get(0).getBillDetails().get(0).getDemandId());
			ofpd.setType(OfflinePaymentType.SECURITY);
			ofpd.setPropertyDetailsId(propertyDb.getPropertyDetails().getId());
			ofpd.setTenantId(propertyDb.getTenantId());
			ofpd.setFileNumber(propertyDb.getFileNumber());
			ofpd.setConsumerCode(consumerCode);
			ofpd.setBillingBusinessService(propertyDb.getSecurityDepositBusinessService());
			ofpd.setAuditDetails(auditDetails);
		});

//		propertyDb.getPropertyDetails().getPaymentConfig().setSecurityAmount(totalDue.subtract(paymentAmount));
		List<Property> properties = new ArrayList<Property>();
		properties.add(propertyDb);

		producer.push(config.getUpdatePropertyTopic(), new PropertyRequest(requestInfo, properties));
		return offlinePaymentDetails;
	}

	public SecurityDepositStatementResponse createSecurityDepositStatement(String propertyId) {

		Property property = repository.findPropertyById(propertyId);
		if (null == property) {
			throw new CustomException(Collections.singletonMap("NO_PROPERTY_FOUND",
					"Property not found for the given property id: " + propertyId));
		}

		List<String> propertyDetailsIds = new ArrayList<String>();
		propertyDetailsIds.add(property.getPropertyDetails().getId());
		List<String> relations = new ArrayList<String>();
		relations.add(PSConstants.RELATION_OPD);
		PropertyCriteria criteria = PropertyCriteria.builder().relations(relations).build();
		List<OfflinePaymentDetails> offlinePaymentDetails = repository
				.getOfflinePaymentsForPropertyDetailsIds(propertyDetailsIds, criteria);
		List<OfflinePaymentDetails> filteredOfflinePaymentDetails = offlinePaymentDetails.stream()
				.filter(offlinePaymentDetail -> null != offlinePaymentDetail.getType()
						&& offlinePaymentDetail.getType().equals(OfflinePaymentType.SECURITY))
				.collect(Collectors.toList());

		double totalSecurityDeposit = property.getPropertyDetails().getPaymentConfig().getSecurityAmount()
				.doubleValue();

		BigDecimal securityDepositPaid = filteredOfflinePaymentDetails.stream().map(OfflinePaymentDetails::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		double totalSecurityDepositPaid = securityDepositPaid.doubleValue();

		double totalSecurityDepositDue = totalSecurityDeposit - totalSecurityDepositPaid;

		if (totalSecurityDepositDue < 0) {
			totalSecurityDepositDue = 0;
		}

		SecurityDepositStatementSummary securityDepositStatementSummary = SecurityDepositStatementSummary.builder()
				.totalSecurityDeposit(totalSecurityDeposit).totalSecurityDepositDue(totalSecurityDepositDue)
				.totalSecurityDepositPaid(totalSecurityDepositPaid).build();

		SecurityDepositStatementResponse securityDepositStatementResponse = SecurityDepositStatementResponse.builder()
				.offlinePaymentDetails(filteredOfflinePaymentDetails)
				.securityDepositStatementSummary(securityDepositStatementSummary).build();

		return securityDepositStatementResponse;
	}

}
