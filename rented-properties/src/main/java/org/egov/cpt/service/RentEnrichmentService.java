package org.egov.cpt.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.ModeEnum;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.models.calculation.PaymentDetail;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.util.PropertyUtil;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class RentEnrichmentService {

	@Autowired
	PropertyUtil propertyutil;

	@Autowired
	PropertyRepository propertyRepository;

	@Autowired
	private IRentCollectionService rentCollectionService;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private Producer producer;
	
	@Autowired
	private PropertyUtil propertyUtil;
	
	public void enrichRentdata(PropertyRequest request) {
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {
				enrichPropertyRentdata(property, request.getRequestInfo());
			});
		}
	}

	private void enrichPropertyRentdata(Property property, RequestInfo requestInfo) {

		if (CollectionUtils.isEmpty(property.getDemands()) && CollectionUtils.isEmpty(property.getPayments())) {
			return;
		}

		final PropertyCriteria criteria = PropertyCriteria.builder().propertyId(property.getId()).build();
		RentAccount account = propertyRepository.getPropertyRentAccountDetails(criteria);
		if (account == null) {
			account = RentAccount.builder().remainingAmount(0D).id(UUID.randomUUID().toString())
					.propertyId(property.getId())
					.auditDetails(propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true)).build();
		}
		property.setRentAccount(account);

		/**
		 * Delete existing data as new data is coming in.
		 */
		boolean hasAnyNewDemandOrPayment = property.getDemands().stream()
				.filter(demand -> demand.getId() == null || demand.getId().isEmpty()).findAny().isPresent()
				|| property.getPayments().stream()
						.filter(payment -> payment.getId() == null || payment.getId().isEmpty()).findAny().isPresent();
		if (hasAnyNewDemandOrPayment) {
			List<RentDemand> existingDemands = propertyRepository.getPropertyRentDemandDetails(criteria);
			List<RentPayment> existingPayments = propertyRepository.getPropertyRentPaymentDetails(criteria);
			property.setInActiveDemands(existingDemands);
			property.setInActivePayments(existingPayments);
			account.setRemainingAmount(0D);
			account.setRemainingSince(0L);
		} else {
			property.setInActiveDemands(Collections.emptyList());
			property.setInActivePayments(Collections.emptyList());
		}

		property.getDemands().forEach(demand -> {
			if (demand.getId() == null) {
				AuditDetails demandAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(),
						true);
				demand.setId(UUID.randomUUID().toString());
				demand.setPropertyId(property.getId());
				demand.setRemainingPrincipal(demand.getCollectionPrincipal());
				demand.setInterestSince(demand.getGenerationDate());
				demand.setMode(ModeEnum.fromValue(PTConstants.MODE_UPLOADED));
				demand.setAuditDetails(demandAuditDetails);
			} else {
				demand.getId();
			}
		});

		property.getPayments().forEach(payment -> {
			if (payment.getId() == null) {
				AuditDetails paymentAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(),
						true);
				payment.setId(UUID.randomUUID().toString());
				payment.setPropertyId(property.getId());
				payment.setMode(ModeEnum.fromValue(PTConstants.MODE_UPLOADED));
				payment.setAuditDetails(paymentAuditDetails);
			} else {
				payment.getId();
			}
		});
	}

	public void enrichCollection(PropertyRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {

				if (!CollectionUtils.isEmpty(property.getRentCollections())) {
					property.getRentCollections().forEach(collection -> {
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
		List<RentPayment> rentPayments = Collections.singletonList(RentPayment.builder()
				.id(UUID.randomUUID().toString()).amountPaid(paymentDetail.getTotalAmountPaid().doubleValue())
				.propertyId(property.getId()).dateOfPayment(System.currentTimeMillis())
				.mode(ModeEnum.fromValue(PTConstants.MODE_GENERATED)).processed(false)
				.receiptNo(paymentDetail.getReceiptNumber()).auditDetails(paymentAuditDetails).build());

		if (CollectionUtils.isEmpty(property.getPayments())) {
			property.setPayments(rentPayments);
		} else {
			property.getPayments().addAll(rentPayments);
		}
		// Settle the payment
		if (!CollectionUtils.isEmpty(property.getDemands()) && null != property.getRentAccount()) {
			long interestStartDate = propertyUtil.getInterstStartFromMDMS(property,requestInfo);
			property.setRentCollections(rentCollectionService.settle(property.getDemands(), property.getPayments(),
					property.getRentAccount(), property.getPropertyDetails().getInterestRate(),interestStartDate));
		}

		// Save everything back to database
		PropertyRequest propertyRequest = PropertyRequest.builder().requestInfo(requestInfo)
				.properties(Collections.singletonList(property)).build();
		enrichCollection(propertyRequest);
		producer.push(config.getUpdatePropertyTopic(), propertyRequest);
	}
}
