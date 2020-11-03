package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.AuctionBidder;
import org.egov.ps.model.Document;
import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.model.Payment;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.model.RentSummary;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.model.calculation.Category;
import org.egov.ps.model.calculation.TaxHeadEstimate;
import org.egov.ps.repository.IdGenRepository;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.AuctionSaveRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentSummary;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyEnrichmentService {

	@Autowired
	Util util;

	@Autowired
	IdGenRepository idGenRepository;

	@Autowired
	private PropertyRepository propertyRepository;

	public void enrichPropertyRequest(PropertyRequest request) {

		RequestInfo requestInfo = request.getRequestInfo();

		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {
				AuditDetails propertyAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
						property.getId() == null);

				if (property.getId() == null) {
					property.setId(UUID.randomUUID().toString());
					property.setState(PSConstants.PM_DRAFTED);
					property.setFileNumber(property.getFileNumber().toUpperCase());
				}

				property.setAuditDetails(propertyAuditDetails);

				enrichPropertyDetail(property, requestInfo);

			});
		}
	}

	private void enrichPropertyDetail(Property property, RequestInfo requestInfo) {

		PropertyDetails propertyDetail = property.getPropertyDetails();

		AuditDetails propertyDetailsAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
				propertyDetail.getId() == null);

		if (propertyDetail.getId() == null) {
			propertyDetail.setId(UUID.randomUUID().toString());
			propertyDetail.setTenantId(property.getTenantId());
			propertyDetail.setPropertyId(property.getId());
		}
		propertyDetail.setAuditDetails(propertyDetailsAuditDetails);

		enrichOwners(property, requestInfo);
		enrichCourtCases(property, requestInfo);
		enrichPaymentDetails(property, requestInfo);
		enrichBidders(property, requestInfo);
		enrichEstateDemand(property, requestInfo);
		enrichEstatePayment(property, requestInfo);
		enrichEstateAccount(property, requestInfo);

	}

	private void enrichOwners(Property property, RequestInfo requestInfo) {

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {

			property.getPropertyDetails().getOwners().forEach(owner -> {

				if (owner.getId() == null) {

					owner.setId(UUID.randomUUID().toString());
					owner.setTenantId(property.getTenantId());
					owner.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails ownerAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				owner.setAuditDetails(ownerAuditDetails);
				enrichOwnerDetail(property, owner, requestInfo);

			});
		}
	}

	private void enrichOwnerDetail(Property property, Owner owner, RequestInfo requestInfo) {

		OwnerDetails ownerDetails = owner.getOwnerDetails();

		if (ownerDetails.getId() == null || ownerDetails.getId().isEmpty()) {

			ownerDetails.setId(UUID.randomUUID().toString());
			ownerDetails.setTenantId(property.getTenantId());
			ownerDetails.setOwnerId(owner.getId());

		}

		AuditDetails ownerDetailsAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		ownerDetails.setAuditDetails(ownerDetailsAuditDetails);
		enrichOwnerDocs(property, requestInfo);

	}

	private void enrichOwnerDocs(Property property, RequestInfo requestInfo) {
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {

			property.getPropertyDetails().getOwners().forEach(owner -> {
				List<Document> ownerDocuments = owner.getOwnerDetails().getOwnerDocuments();

				if (!CollectionUtils.isEmpty(ownerDocuments)) {
					ownerDocuments.forEach(document -> {
						if (document.getId() == null || document.getId().isEmpty()) {
							document.setId(UUID.randomUUID().toString());
						}
						document.setTenantId(property.getTenantId());
						document.setReferenceId(owner.getOwnerDetails().getId());
						document.setPropertyId(property.getId());
						AuditDetails docAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
						document.setAuditDetails(docAuditDetails);
					});
				}
			});
		}
	}

	private void enrichCourtCases(Property property, RequestInfo requestInfo) {

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getCourtCases())) {

			property.getPropertyDetails().getCourtCases().forEach(courtCase -> {

				if (courtCase.getId() == null || courtCase.getId().isEmpty()) {

					courtCase.setId(UUID.randomUUID().toString());
					courtCase.setTenantId(property.getTenantId());
					courtCase.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails courtCaseAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				courtCase.setAuditDetails(courtCaseAuditDetails);

			});
		}
	}

	private void enrichPaymentDetails(Property property, RequestInfo requestInfo) {

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {
			property.getPropertyDetails().getOwners().forEach(owner -> {

				List<Payment> payments = property.getPropertyDetails().getPaymentDetails();
				if (!CollectionUtils.isEmpty(payments)) {

					payments.forEach(payment -> {
						if (payment.getId() == null || payment.getId().isEmpty()) {
							AuditDetails paymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
									true);
							String gen_payment_detail_id = UUID.randomUUID().toString();
							payment.setId(gen_payment_detail_id);
							payment.setTenantId(property.getTenantId());
							payment.setOwnerDetailsId(owner.getOwnerDetails().getId());
							payment.setAuditDetails(paymentAuditDetails);
						} else {
							AuditDetails paymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
									true);
							payment.setAuditDetails(paymentAuditDetails);
						}
					});
				}
			});
		}
	}

	private void enrichBidders(Property property, RequestInfo requestInfo) {

		/**
		 * Delete existing data as new data is coming in.
		 */
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getBidders())) {

			boolean hasAnyNewBidder = property.getPropertyDetails().getBidders().stream()
					.filter(bidder -> bidder.getId() == null || bidder.getId().isEmpty()).findAny().isPresent();

			if (hasAnyNewBidder) {
				List<AuctionBidder> existingBidders = propertyRepository.getBiddersForPropertyDetailsIds(
						Collections.singletonList(property.getPropertyDetails().getId()));
				property.getPropertyDetails().setInActiveBidders(existingBidders);
			} else {
				property.getPropertyDetails().setInActiveBidders(Collections.emptyList());
			}
		}

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getBidders())) {

			property.getPropertyDetails().getBidders().forEach(bidder -> {

				if (bidder.getId() == null) {

					bidder.setId(UUID.randomUUID().toString());
					bidder.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails buidderAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				bidder.setAuditDetails(buidderAuditDetails);

			});
		}

	}

	private void enrichEstateAccount(Property property, RequestInfo requestInfo) {

		if (property.getPropertyDetails().getEstateAccount().getId() == null) {
			String gen_estate_account_id = UUID.randomUUID().toString();

			property.getPropertyDetails().getEstateAccount().setId(gen_estate_account_id);
			property.getPropertyDetails().getEstateAccount()
					.setPropertyDetailsId(property.getPropertyDetails().getId());
		}
		AuditDetails estateAccountAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		property.getPropertyDetails().getEstateAccount().setAuditDetails(estateAccountAuditDetails);
	}

	private void enrichEstateDemand(Property property, RequestInfo requestInfo) {

		/**
		 * Delete existing data as new data is coming in.
		 */
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateDemands())) {

			boolean hasAnyNewEstateDemands = property.getPropertyDetails().getEstateDemands().stream()
					.filter(estateDemand -> estateDemand.getId() == null || estateDemand.getId().isEmpty()).findAny()
					.isPresent();

			if (hasAnyNewEstateDemands) {
				List<EstateDemand> existingEstateDemands = propertyRepository.getDemandDetailsForPropertyDetailsIds(
						Collections.singletonList(property.getPropertyDetails().getId()));
				property.getPropertyDetails().setInActiveEstateDemands(existingEstateDemands);
			} else {
				property.getPropertyDetails().setInActiveEstateDemands(Collections.emptyList());
			}
		}

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateDemands())) {

			property.getPropertyDetails().getEstateDemands().forEach(estateDemand -> {

				if (estateDemand.getId() == null) {

					estateDemand.setId(UUID.randomUUID().toString());
					estateDemand.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				estateDemand.setRemainingRentPenalty(estateDemand.getPenaltyInterest());
				estateDemand.setRemainingGST(estateDemand.getGstInterest());
				estateDemand.setRemainingRent(estateDemand.getRent());
				estateDemand.setInterestSince(estateDemand.getGenerationDate());
				estateDemand.setIsPrevious(false);
				AuditDetails estateDemandAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				estateDemand.setAuditDetails(estateDemandAuditDetails);

			});
		}

	}

	private void enrichEstatePayment(Property property, RequestInfo requestInfo) {

		/**
		 * Delete existing data as new data is coming in.
		 */
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstatePayments())) {

			boolean hasAnyNewEstatePayments = property.getPropertyDetails().getEstatePayments().stream()
					.filter(estatePayment -> estatePayment.getId() == null || estatePayment.getId().isEmpty()).findAny()
					.isPresent();

			if (hasAnyNewEstatePayments) {
				List<EstatePayment> existingEstatePayments = propertyRepository.getEstatePaymentsForPropertyDetailsIds(
						Collections.singletonList(property.getPropertyDetails().getId()));
				property.getPropertyDetails().setInActiveEstatePayments(existingEstatePayments);
			} else {
				property.getPropertyDetails().setInActiveEstatePayments(Collections.emptyList());
			}
		}

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstatePayments())) {

			property.getPropertyDetails().getEstatePayments().forEach(estatePayment -> {

				if (estatePayment.getId() == null) {

					estatePayment.setId(UUID.randomUUID().toString());
					estatePayment.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails estatePaymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
						true);
				estatePayment.setAuditDetails(estatePaymentAuditDetails);

			});
		}

	}

	public void enrichUpdateAuctionRequest(AuctionSaveRequest request, List<AuctionBidder> auctionFromSearch) {
		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), false);

		if (!CollectionUtils.isEmpty(request.getAuctions())) {
			request.getAuctions().forEach(auction -> {
				auction.getAuditDetails().setLastModifiedBy(auditDetails.getLastModifiedBy());
				auction.getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
			});
		}
	}

	public void enrichRentDemand(Property property, EstateRentSummary rentSummary) {
		if (rentSummary == null)
			return;
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		double amount = property.getPropertyDetails().getOfflinePaymentDetails().get(0).getAmount().doubleValue();
		double balPrincipal = rentSummary.getBalanceRent();
		double balInterest = rentSummary.getBalanceInterest();

		if (amount >= balInterest) {
			TaxHeadEstimate estimate1 = new TaxHeadEstimate();
			estimate1.setEstimateAmount(new BigDecimal(balInterest));
			estimate1.setCategory(Category.INTEREST);
			estimate1.setTaxHeadCode(
					getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(), Category.INTEREST));
			estimates.add(estimate1);
			double remainingAmmount = amount - balInterest;
			if (remainingAmmount >= balPrincipal) {
				TaxHeadEstimate estimate2 = new TaxHeadEstimate();
				estimate2.setEstimateAmount(new BigDecimal(balPrincipal));
				estimate2.setCategory(Category.PRINCIPAL);
				estimate2.setTaxHeadCode(
						getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(), Category.PRINCIPAL));
				estimates.add(estimate2);
			} else {
				TaxHeadEstimate estimate2 = new TaxHeadEstimate();
				estimate2.setEstimateAmount(new BigDecimal(remainingAmmount));
				estimate2.setCategory(Category.PRINCIPAL);
				estimate2.setTaxHeadCode(
						getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(), Category.PRINCIPAL));
				estimates.add(estimate2);
			}
			remainingAmmount = amount - balInterest - balPrincipal;
			if (remainingAmmount > 0) {
				TaxHeadEstimate estimate3 = new TaxHeadEstimate();
				estimate3.setEstimateAmount(new BigDecimal(remainingAmmount));
				estimate3.setCategory(Category.ADVANCE_COLLECTION);
				estimate3.setTaxHeadCode(getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(),
						Category.ADVANCE_COLLECTION));
				estimates.add(estimate3);
			}
		} else {
			TaxHeadEstimate estimate2 = new TaxHeadEstimate();
			estimate2.setEstimateAmount(new BigDecimal(amount));
			estimate2.setCategory(Category.ADVANCE_COLLECTION);
			estimate2.setTaxHeadCode(getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(),
					Category.ADVANCE_COLLECTION));
			estimates.add(estimate2);
		}

		// estimates.add(estimate);
		Calculation calculation = Calculation.builder()
				.applicationNumber(util.getPropertyRentConsumerCode(property.getFileNumber()))
				.taxHeadEstimates(estimates).tenantId(property.getTenantId()).build();
		property.setCalculation(calculation);

	}

	private String getTaxHeadCode(String billingBusService, Category category) {
		return String.format("%s_%s", billingBusService, category.toString());
	}

	public void enrichCollection(PropertyRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {

				if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateRentCollections())) {
					property.getPropertyDetails().getEstateRentCollections().forEach(collection -> {
						if (collection.getId() == null) {
							AuditDetails estateCollectionAuditDetails = util
									.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
							collection.setId(UUID.randomUUID().toString());
							collection.setAuditDetails(estateCollectionAuditDetails);
						}

					});
				}
			});
		}
	}
}
