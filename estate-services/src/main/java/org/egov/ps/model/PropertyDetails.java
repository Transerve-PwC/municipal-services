package org.egov.ps.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Property Details
 */
@ApiModel(description = "A Object holds the basic data for a Property Details")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-31T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PropertyDetails {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("propertyId")
	private String propertyId;

	/**
	 * One of BRANCH_ESTATE, BRANCH_BUILDING, BRANCH_MANIMAJRA
	 */
	@JsonProperty("branchType")
	private String branchType;

	/**
	 * One of PROPERTY_TYPE.LEASEHOLD or PROPERTY_TYPE.FREEHOLD defined in mdms at
	 * `data/ch/EstateServices/PropertyType.json`
	 */
	@JsonProperty("propertyType")
	private String propertyType;

	/**
	 * One of ALLOCATION_TYPE.AUCTION or ALLOCATION_TYPE.ALLOTMENT
	 */
	@JsonProperty("typeOfAllocation")
	private String typeOfAllocation;

	@JsonProperty("emdAmount")
	private BigDecimal emdAmount;

	@JsonProperty("emdDate")
	private Long emdDate;

	@JsonProperty("modeOfAuction")
	private String modeOfAuction;

	@JsonProperty("schemeName")
	private String schemeName;

	@JsonProperty("dateOfAuction")
	private Long dateOfAuction;

	@JsonProperty("areaSqft")
	private int areaSqft;

	@JsonProperty("ratePerSqft")
	private BigDecimal ratePerSqft;

	@JsonProperty("lastNocDate")
	private Long lastNocDate;

	@JsonProperty("serviceCategory")
	private String serviceCategory;

	@JsonProperty("isPropertyActive")
	private Boolean isPropertyActive;

	@JsonProperty("tradeType")
	private String tradeType;

	@JsonProperty("companyName")
	private String companyName;

	@JsonProperty("companyAddress")
	private String companyAddress;

	@JsonProperty("companyRegistrationNumber")
	private String companyRegistrationNumber;

	@JsonProperty("companyType")
	private String companyType;

	@JsonProperty("decreeDate")
	private Long decreeDate;

	@JsonProperty("courtDetails")
	private String courtDetails;

	@JsonProperty("civilTitledAs")
	private String civilTitledAs;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("companyRegistrationDate")
	private Long companyRegistrationDate;

	@JsonProperty("companyOrFirm")
	private String companyOrFirm;

	@JsonProperty("propertyRegisteredTo")
	private String propertyRegisteredTo;

	@JsonProperty("entityType")
	private String entityType;

	@JsonProperty("owners")
	@Builder.Default
	private List<Owner> owners = new ArrayList<Owner>();

	public PropertyDetails addOwnerItem(Owner newOwnerItem) {
		if (this.owners == null) {
			this.owners = new ArrayList<>();
		}
		for (Owner owner : owners) {
			if (owner.getId().equalsIgnoreCase(newOwnerItem.getId())) {
				return this;
			}
		}
		this.owners.add(newOwnerItem);
		return this;

	}

	@JsonProperty("courtCases")
	@Builder.Default
	private List<CourtCase> courtCases = new ArrayList<CourtCase>();

	public PropertyDetails addCourtCaseItem(CourtCase courtCaseItem) {
		if (this.courtCases == null) {
			this.courtCases = new ArrayList<>();
		}
		for (CourtCase courtCase : courtCases) {
			if (courtCase.getId().equalsIgnoreCase(courtCaseItem.getId())) {
				return this;
			}
		}
		this.courtCases.add(courtCaseItem);
		return this;

	}

	@JsonProperty("paymentDetails")
	@Builder.Default
	private List<Payment> paymentDetails = new ArrayList<Payment>();

	public PropertyDetails addPaymentItem(Payment paymentItem) {
		if (this.paymentDetails == null) {
			this.paymentDetails = new ArrayList<>();
		}
		for (Payment paymentDetail : paymentDetails) {
			if (paymentDetail.getId().equalsIgnoreCase(paymentItem.getId())) {
				return this;
			}
		}
		this.paymentDetails.add(paymentItem);
		return this;
	}

	@JsonProperty("bidders")
	@Builder.Default
	private List<AuctionBidder> bidders = new ArrayList<AuctionBidder>();

	public PropertyDetails addBidderItem(AuctionBidder newBidderItem) {
		if (this.bidders == null) {
			this.bidders = new ArrayList<>();
		}
		for (AuctionBidder bidder : bidders) {
			if (bidder.getId().equalsIgnoreCase(newBidderItem.getId())) {
				return this;
			}
		}
		this.bidders.add(newBidderItem);
		return this;
	}

	@Valid
	@JsonProperty
	private List<AuctionBidder> inActiveBidders;
	
	@JsonProperty("estateDemands")
	@Builder.Default
	private List<EstateDemand> estateDemands = new ArrayList<EstateDemand>();

	public PropertyDetails addEstatePaymentItem(EstateDemand estateDemandItem) {
		if (this.estateDemands == null) {
			this.estateDemands = new ArrayList<>();
		}
		for (EstateDemand estateDemand : estateDemands) {
			if (estateDemand.getId().equalsIgnoreCase(estateDemandItem.getId())) {
				return this;
			}
		}
		this.estateDemands.add(estateDemandItem);
		return this;
	}
	
	@Valid
	@JsonProperty
	private List<EstateDemand> inActiveEstateDemands;

	@JsonProperty("estatePayments")
	@Builder.Default
	private List<EstatePayment> estatePayments = new ArrayList<EstatePayment>();

	public PropertyDetails addEstatePaymentItem(EstatePayment estatePaymentItem) {
		if (this.estatePayments == null) {
			this.estatePayments = new ArrayList<>();
		}
		for (EstatePayment estatePayment : estatePayments) {
			if (estatePayment.getId().equalsIgnoreCase(estatePaymentItem.getId())) {
				return this;
			}
		}
		this.estatePayments.add(estatePaymentItem);
		return this;
	}
	
	@Valid
	@JsonProperty
	private List<EstatePayment> inActiveEstatePayments;
	
	@Valid
	@JsonProperty("estateAccount")
	private EstateAccount estateAccount;
	
	@Valid
	@JsonProperty("estateRentCollections")
	private List<EstateRentCollection> estateRentCollections;
	
	public PropertyDetails addCollectionItem(EstateRentCollection newCollectionItem) {
		if (this.estateRentCollections == null) {
			this.estateRentCollections = new ArrayList<>();
		}
		for (EstateRentCollection estateRentCollections : estateRentCollections) {
			if (estateRentCollections.getId().equalsIgnoreCase(newCollectionItem.getId())) {
				return this;
			}
		}
		this.estateRentCollections.add(newCollectionItem);
		return this;
	}
	
	@NotNull
	@Builder.Default
	@JsonProperty("interestRate")
	private Double interestRate = 0.0;
			
}
