package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.ExtensionFee;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.OfflinePaymentDetails.OfflinePaymentType;
import org.egov.ps.model.Property;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.DemandService;
import org.egov.ps.service.calculation.PenaltyCollectionService;
import org.egov.ps.web.contracts.AccountStatementRequest;
import org.egov.ps.web.contracts.ExtensionFeeRequest;
import org.egov.ps.web.contracts.ExtensionFeeStatementResponse;
import org.egov.ps.web.contracts.ExtensionFeeStatementSummary;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	PenaltyCollectionService penaltyCollectionService;

	@Autowired
	DemandService demandService;

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
						&& offlinePaymentDetail.getType().equals(OfflinePaymentType.EXTENSION))
				.collect(Collectors.toList());

		double totalExtensionFee = filteredExtensionFees.stream().mapToDouble(ExtensionFee::getAmount).sum();
		double totalExtensionFeeDue = filteredExtensionFees.stream().mapToDouble(ExtensionFee::getRemainingDue).sum();

		BigDecimal totalExtensionFeePaid = filteredOfflinePaymentDetails.stream().map(OfflinePaymentDetails::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		ExtensionFeeStatementSummary penaltyStatementSummary = ExtensionFeeStatementSummary.builder()
				.totalExtensionFee(totalExtensionFee).totalExtensionFeeDue(totalExtensionFeeDue)
				.totalExtensionFeePaid(totalExtensionFeePaid.doubleValue()).build();

		ExtensionFeeStatementResponse extensionFeeStatementResponse = ExtensionFeeStatementResponse.builder()
				.extensionFees(filteredExtensionFees).offlinePaymentDetails(filteredOfflinePaymentDetails)
				.extensionFeeStatementSummary(penaltyStatementSummary).build();

		return extensionFeeStatementResponse;
	}

}
