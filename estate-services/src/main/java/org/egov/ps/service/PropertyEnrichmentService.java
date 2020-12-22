package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.AuctionBidder;
import org.egov.ps.model.Document;
import org.egov.ps.model.ExtensionFee;
import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.model.PaymentConfig;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.model.calculation.Category;
import org.egov.ps.model.calculation.TaxHeadEstimate;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.AuctionSaveRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentSummary;
import org.egov.ps.web.contracts.ExtensionFeeRequest;
import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PropertyEnrichmentService {

	@Autowired
	Util util;

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
					property.setFileNumber(property.getFileNumber().trim().toUpperCase());
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
		enrichPaymentConfig(property, requestInfo);
		enrichBidders(property, requestInfo);
		enrichEstateDemand(property, requestInfo);
		enrichEstatePayment(property, requestInfo);
		enrichEstateAccount(property, requestInfo);
		enrichAccountStatementDoc(property, requestInfo);

		enrichManiMajraDemand(property, requestInfo);
		enrichManiMajraPayment(property, requestInfo);
	}

	private void enrichAccountStatementDoc(Property property, RequestInfo requestInfo) {
		if (property.getPropertyDetails().getAccountStatementDocument() != null) {
			List<Document> accountStatementDoc = property.getPropertyDetails().getAccountStatementDocument();
			if (!CollectionUtils.isEmpty(accountStatementDoc)) {
				accountStatementDoc.forEach(document -> {
					if (document.getId() == null || document.getId().isEmpty()) {
						document.setId(UUID.randomUUID().toString());
					}
					document.setTenantId(property.getTenantId());
					document.setReferenceId(property.getPropertyDetails().getId());
					document.setPropertyId(property.getId());
					AuditDetails docAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
					document.setAuditDetails(docAuditDetails);
				});
			}
		}
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

	private void enrichPaymentConfig(Property property, RequestInfo requestInfo) {

		PaymentConfig paymentConfig = property.getPropertyDetails().getPaymentConfig();
		if (paymentConfig != null) {
			AuditDetails paymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
			if (paymentConfig.getId() == null || paymentConfig.getId().isEmpty()) {
				paymentConfig.setId(UUID.randomUUID().toString());
				paymentConfig.setTenantId(property.getTenantId());
				paymentConfig.setPropertyDetailsId(property.getPropertyDetails().getId());
			}
			paymentConfig.setAuditDetails(paymentAuditDetails);

			if (!CollectionUtils.isEmpty(paymentConfig.getPaymentConfigItems())) {
				paymentConfig.getPaymentConfigItems().forEach(paymentConfigItem -> {
					if (paymentConfigItem.getId() == null || paymentConfigItem.getId().isEmpty()) {
						paymentConfigItem.setId(UUID.randomUUID().toString());
						paymentConfigItem.setTenantId(property.getTenantId());
						paymentConfigItem.setPaymentConfigId(paymentConfig.getId());
					}
				});
			}

			if (!CollectionUtils.isEmpty(paymentConfig.getPremiumAmountConfigItems())) {
				paymentConfig.getPremiumAmountConfigItems().forEach(premiumAmountConfigItem -> {
					if (premiumAmountConfigItem.getId() == null || premiumAmountConfigItem.getId().isEmpty()) {
						premiumAmountConfigItem.setId(UUID.randomUUID().toString());
						premiumAmountConfigItem.setTenantId(property.getTenantId());
						premiumAmountConfigItem.setPaymentConfigId(paymentConfig.getId());
					}
				});
			}

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

		if (property.getPropertyDetails().getEstateAccount() == null) {

			EstateAccount existingEstateAccount = EstateAccount.builder().remainingAmount(0D).build();

			property.getPropertyDetails().setEstateAccount(existingEstateAccount);

			if (property.getPropertyDetails().getEstateAccount().getId() == null) {
				String gen_estate_account_id = UUID.randomUUID().toString();
				property.getPropertyDetails().getEstateAccount().setId(gen_estate_account_id);
				property.getPropertyDetails().getEstateAccount()
						.setPropertyDetailsId(property.getPropertyDetails().getId());
			}
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
				estateDemand.setRemainingGST(estateDemand.getGst());
				estateDemand.setRemainingGSTPenalty(estateDemand.getGstInterest());
				estateDemand.setRemainingRent(estateDemand.getRent());
				if (estateDemand.getInterestSince() != null && estateDemand.getInterestSince() != 0.0)
					estateDemand.setInterestSince(estateDemand.getInterestSince());
				else
					estateDemand.setInterestSince(estateDemand.getGenerationDate());
				estateDemand.setIsPrevious(estateDemand.getIsPrevious());
				AuditDetails estateDemandAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				estateDemand.setAuditDetails(estateDemandAuditDetails);
				estateDemand.setAdjustmentDate(estateDemand.getAdjustmentDate());
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

	public void enrichPenaltyRequest(PropertyPenaltyRequest propertyPenaltyRequest) {
		propertyPenaltyRequest.getPropertyPenalties().forEach(propertyPenalty -> {
			enrichPenalty(propertyPenaltyRequest.getRequestInfo(), propertyPenalty);
		});
	}

	private void enrichPenalty(RequestInfo requestInfo, PropertyPenalty penalty) {
		Property property = propertyRepository.findPropertyById(penalty.getProperty().getId());
		AuditDetails penaltyAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
				penalty.getId() == null);
		if (null == penalty.getId()) {
			penalty.setId(UUID.randomUUID().toString());
			penalty.setGenerationDate(new Date().getTime());
			penalty.setStatus(PaymentStatusEnum.UNPAID);
			penalty.setIsPaid(false);
			penalty.setRemainingPenaltyDue(penalty.getPenaltyAmount());
		}
		penalty.setTenantId(property.getTenantId());
		penalty.setBranchType(property.getPropertyDetails().getBranchType());
		penalty.setAuditDetails(penaltyAuditDetails);
	}

	public Calculation enrichGenerateDemand(RequestInfo requestInfo, double paymentAmount, String consumerCode,
			Property property, String demandFor) {

		List<TaxHeadEstimate> estimates = new LinkedList<>();

		TaxHeadEstimate estimateDue = new TaxHeadEstimate();
		estimateDue.setEstimateAmount(new BigDecimal(paymentAmount));
		if (demandFor.equals(PSConstants.EXTENSION_FEE)) {
			estimateDue.setCategory(Category.FEE);
			estimateDue.setTaxHeadCode(getTaxHeadCode(property.getExtensionFeeBusinessService(), Category.FEE));
		} else if (demandFor.equals(PSConstants.PROPERTY_VIOLATION)) {
			estimateDue.setCategory(Category.PENALTY);
			estimateDue.setTaxHeadCode(getTaxHeadCode(property.getPenaltyBusinessService(), Category.PENALTY));
		} else if (demandFor.equals(PSConstants.SECURITY_DEPOSIT)) {
			estimateDue.setCategory(Category.FEE);
			estimateDue.setTaxHeadCode(getTaxHeadCode(property.getSecurityDepositBusinessService(), Category.FEE));
		}
		estimates.add(estimateDue);

		Calculation calculation = Calculation.builder().applicationNumber(consumerCode).taxHeadEstimates(estimates)
				.tenantId(property.getTenantId()).build();
		return calculation;
	}

	public void enrichExtensionFeeRequest(ExtensionFeeRequest extensionFeeRequest) {
		extensionFeeRequest.getExtensionFees().forEach(extensionFee -> {
			enrichExtensionFee(extensionFeeRequest.getRequestInfo(), extensionFee);
		});
	}

	private void enrichExtensionFee(RequestInfo requestInfo, ExtensionFee extensionFee) {
		Property property = propertyRepository.findPropertyById(extensionFee.getProperty().getId());
		AuditDetails extensionFeeAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
				extensionFee.getId() == null);
		if (null == extensionFee.getId()) {
			extensionFee.setId(UUID.randomUUID().toString());
			extensionFee.setRemainingDue(extensionFee.getAmount());
			extensionFee.setIsPaid(false);
			extensionFee.setStatus(PaymentStatusEnum.UNPAID);
			extensionFee.setGenerationDate(new Date().getTime());
		}
		extensionFee.setTenantId(property.getTenantId());
		extensionFee.setBranchType(property.getPropertyDetails().getBranchType());
		extensionFee.setAuditDetails(extensionFeeAuditDetails);
	}

	private void enrichManiMajraDemand(Property property, RequestInfo requestInfo) {
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getManiMajraDemands())) {
			AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

			property.getPropertyDetails().getManiMajraDemands().forEach(mmDemand -> {
				if (null != mmDemand.getId()) {
					mmDemand.setId(UUID.randomUUID().toString());
					mmDemand.setPropertyDetailsId(property.getPropertyDetails().getId());
				}
				mmDemand.setAuditDetails(auditDetails);
			});
		}

	}

	private void enrichManiMajraPayment(Property property, RequestInfo requestInfo) {
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getManiMajraPayments())) {
			AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

			property.getPropertyDetails().getManiMajraPayments().forEach(mmPayments -> {
				if (null != mmPayments.getId()) {
					mmPayments.setId(UUID.randomUUID().toString());
					mmPayments.setPropertyDetailsId(property.getPropertyDetails().getId());
				}
				mmPayments.setAuditDetails(auditDetails);
			});
		}
	}

	public void enrichMmRentDemand(Property property) {

		List<TaxHeadEstimate> estimates = new LinkedList<>();
		double amount = property.getPropertyDetails().getOfflinePaymentDetails().get(0).getAmount().doubleValue();

		/**
		 * rent due from mar 2018
		 * 
		 * 1st payment apr 2018
		 * 
		 * unpaid is starting from may 2018
		 * 
		 * 2nd payment jun 2018
		 * 
		 * selected demands data mar 2018 rent = 1200, gst = 216, and apr 2018 rent =
		 * 1200, gst = 216
		 * 
		 * collection amount 1200+216+1200+216=2832
		 * 
		 * divide 2832 into principal=2400 and gst=432
		 */

		List<String> propertyDetailsIds = new ArrayList<String>();
		propertyDetailsIds.add(property.getPropertyDetails().getId());

		/**
		 * unpaid & oldest first demands
		 */
		List<ManiMajraDemand> demands = propertyRepository.getManiMajraDemandDetails(propertyDetailsIds);
		Collections.sort(demands);
		List<ManiMajraDemand> demandsToBeSettled = demands.stream().filter(demand -> demand.isUnPaid())
				.collect(Collectors.toList());

		Double gstAmount = 0D;
		Double rentAmount = 0D;

		for (ManiMajraDemand demand : demandsToBeSettled) {
			Double currentDue = demand.getRent() + demand.getGst();
			if (amount >= currentDue) {
				gstAmount = gstAmount + demand.getGst();
				rentAmount = rentAmount + demand.getRent();
				amount = amount - currentDue;
			}
		}

		TaxHeadEstimate estimate1 = new TaxHeadEstimate();
		estimate1.setEstimateAmount(new BigDecimal(rentAmount));
		estimate1.setCategory(Category.PRINCIPAL);
		estimate1.setTaxHeadCode(
				getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(), Category.PRINCIPAL));
		estimates.add(estimate1);

		TaxHeadEstimate estimate2 = new TaxHeadEstimate();
		estimate2.setEstimateAmount(new BigDecimal(gstAmount));
		estimate2.setCategory(Category.TAX);
		estimate2.setTaxHeadCode(
				getTaxHeadCode(property.getPropertyDetails().getBillingBusinessService(), Category.TAX));
		estimates.add(estimate2);

		Calculation calculation = Calculation.builder()
				.applicationNumber(util.getPropertyRentConsumerCode(property.getFileNumber()))
				.taxHeadEstimates(estimates).tenantId(property.getTenantId()).build();
		property.setCalculation(calculation);

	}

}
