package org.egov.ps.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.ManiMajraPayment;
import org.egov.ps.web.contracts.ManiMajraRentCollection;
import org.springframework.validation.annotation.Validated;

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
	 * Demand type is added for ManiMajra, * If Property Type is Janatha Reddy
	 * Market the enable this and * Demand will be generated monthly or annually
	 * based on selection
	 */
	@JsonProperty("demandType")
	private String demandType;

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

	@JsonProperty("houseNumber")
	private String houseNumber;

	@JsonProperty("village")
	private String village;

	@JsonProperty("mohalla")
	private String mohalla;

	@JsonProperty("street")
	private String street;

	@JsonProperty("owners")
	@Builder.Default
	private List<Owner> owners = new ArrayList<Owner>();

	@JsonProperty("accountStatementDocument")
	private List<Document> accountStatementDocument;

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

	@JsonProperty("paymentConfig")
	private PaymentConfig paymentConfig;

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

	@Valid
	@JsonProperty
	private List<OfflinePaymentDetails> offlinePaymentDetails;

	@JsonProperty("maniMajraDemands")
	@Builder.Default
	private List<ManiMajraDemand> maniMajraDemands = new ArrayList<ManiMajraDemand>();

	public PropertyDetails addManiMajraDemandItem(ManiMajraDemand maniMajraDemandItem) {
		if (this.maniMajraDemands == null) {
			this.maniMajraDemands = new ArrayList<>();
		}
		for (ManiMajraDemand maniMajraDemand : maniMajraDemands) {
			if (maniMajraDemand.getId().equalsIgnoreCase(maniMajraDemandItem.getId())) {
				return this;
			}
		}
		this.maniMajraDemands.add(maniMajraDemandItem);
		return this;
	}

	@Valid
	@JsonProperty
	private List<ManiMajraDemand> inActiveManiMajraDemands;

	@JsonProperty("maniMajraPayments")
	@Builder.Default
	private List<ManiMajraPayment> maniMajraPayments = new ArrayList<ManiMajraPayment>();

	public PropertyDetails addManiMajraPaymentItem(ManiMajraPayment maniMajraPaymentItem) {
		if (this.maniMajraPayments == null) {
			this.maniMajraPayments = new ArrayList<>();
		}
		for (ManiMajraPayment maniMajraPayment : maniMajraPayments) {
			if (maniMajraPayment.getId().equalsIgnoreCase(maniMajraPaymentItem.getId())) {
				return this;
			}
		}
		this.maniMajraPayments.add(maniMajraPaymentItem);
		return this;
	}

	@Valid
	@JsonProperty
	private List<ManiMajraPayment> inActiveManiMajraPayments;

	@Valid
	@JsonProperty("maniMajraRentCollections")
	private List<ManiMajraRentCollection> maniMajraRentCollections;

	public PropertyDetails addManiMajraCollectionItem(ManiMajraRentCollection newCollectionItem) {
		if (this.maniMajraRentCollections == null) {
			this.maniMajraRentCollections = new ArrayList<>();
		}
		for (ManiMajraRentCollection maniMajraRentCollections : maniMajraRentCollections) {
			if (maniMajraRentCollections.getId().equalsIgnoreCase(newCollectionItem.getId())) {
				return this;
			}
		}
		this.maniMajraRentCollections.add(newCollectionItem);
		return this;
	}

	@JsonProperty("billingBusinessService")
	@Size(max = 256, message = "Billing business service must be between 0 and 256 characters in length")
	private String billingBusinessService;

	public String getWorkFlowBusinessService() {
		return String.format("ES-%s", extractPrefix(this.getBranchType()));
	}

	public String getBillingBusinessService() {
		return String.format("%s_%s.%s", PSConstants.ESTATE_SERVICE, camelToSnake(this.getBranchType()),
				PSConstants.PROPERTY_MASTER);
	}

	private String extractPrefix(String inputString) {
		String outputString = "";

		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);
			outputString += Character.isUpperCase(c) ? c : "";
		}
		return outputString;
	}

	public static String camelToSnake(String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		str = str.replaceAll(regex, replacement).toUpperCase();
		return str;
	}

	@JsonProperty("mmDemandStartYear")
	private int mmDemandStartYear;
	
	@JsonProperty("mmDemandStartMonth")
	private int mmDemandStartMonth;
}
