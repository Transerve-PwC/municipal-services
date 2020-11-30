package org.egov.ps.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.ModeEnum;
import org.egov.ps.model.Property;
import org.egov.ps.model.calculation.PaymentDetail;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.IEstateRentCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class RentEnrichmentService {

	@Autowired
	Util propertyutil;

	@Autowired
	PropertyRepository propertyRepository;

	@Autowired
	private IEstateRentCollectionService rentCollectionService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	public void enrichCollection(PropertyRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {

				if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateRentCollections())) {
					property.getPropertyDetails().getEstateRentCollections().forEach(collection -> {
						if (collection.getId() == null) {
							AuditDetails rentAuditDetails = propertyutil
									.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
							collection.setId(UUID.randomUUID().toString());
							collection.setAuditDetails(rentAuditDetails);
						}

					});
				}
			});
		}

	}

	/**
	 * Accept payment and process it to settle pending demands.
	 * 
	 * @param requestInfo
	 * @param property
	 * @param paymentDetail
	 */
	public void postEnrichmentForRentPayment(RequestInfo requestInfo, Property property, PaymentDetail paymentDetail) {
		AuditDetails paymentAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		// Construct a new rent payment object.
		List<EstatePayment> rentPayments = Collections.singletonList(EstatePayment.builder()
				.id(UUID.randomUUID().toString()).amountPaid(paymentDetail.getTotalAmountPaid().doubleValue())
				.propertyDetailsId(property.getPropertyDetails().getId()).dateOfPayment(System.currentTimeMillis())
				.mode(ModeEnum.fromValue(PSConstants.MODE_GENERATED)).processed(false)
				.paymentDate(System.currentTimeMillis()).receiptDate(System.currentTimeMillis())
				.rentReceived(paymentDetail.getTotalAmountPaid().doubleValue())
				.receiptNo(paymentDetail.getReceiptNumber()).auditDetails(paymentAuditDetails).build());

		if (CollectionUtils.isEmpty(property.getPropertyDetails().getEstatePayments())) {
			property.getPropertyDetails().setEstatePayments(rentPayments);
		} else {
			property.getPropertyDetails().getEstatePayments().addAll(rentPayments);
		}
		// Settle the payment
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateDemands())
				&& null != property.getPropertyDetails().getEstateAccount()
				&& property.getPropertyDetails().getPaymentConfig() != null
				&& property.getPropertyDetails().getPropertyType().equalsIgnoreCase(PSConstants.ES_PM_LEASEHOLD)) {
			property.getPropertyDetails().setEstateRentCollections(rentCollectionService.settle(
					property.getPropertyDetails().getEstateDemands(), property.getPropertyDetails().getEstatePayments(),
					property.getPropertyDetails().getEstateAccount(), 18,
					property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
					property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue()));
		}

		// Save everything back to database
		PropertyRequest propertyRequest = PropertyRequest.builder().requestInfo(requestInfo)
				.properties(Collections.singletonList(property)).build();
		enrichCollection(propertyRequest);
		producer.push(config.getUpdatePropertyTopic(), propertyRequest);
	}
}
