package org.egov.ps.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.egov.ps.web.contracts.EstateRentSummary;
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
 * A Object holds the basic data for a Property
 */
@ApiModel(description = "A Object holds the basic data for a Property")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-31T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Property {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("fileNumber")
	private String fileNumber;

	/**
	 * One of the categories from `data/ch/EstateServices/Categories.json`
	 * CAT.RESIDENTIAL, CAT.COMMERCIAL, CAT.INDUSTRIAL, CAT.INSTITUTIONAL,
	 * CAT.GOVPROPERTY, CAT.RELIGIOUS, CAT.HOSPITAL,
	 * 
	 */
	@JsonProperty("category")
	private String category;

	@JsonProperty("subCategory")
	private String subCategory;

	@JsonProperty("siteNumber")
	private String siteNumber;

	@JsonProperty("sectorNumber")
	private String sectorNumber;

	@JsonProperty("propertyMasterOrAllotmentOfSite")
	private String propertyMasterOrAllotmentOfSite;

	@JsonProperty("isCancelationOfSite")
	private Boolean isCancelationOfSite;

	@JsonProperty("state")
	private String state;

	@JsonProperty("action")
	private String action;
	
	@JsonProperty("assignee")
	@Builder.Default
	private List<String> assignee = null;

	@JsonProperty("comments")
	private String comments;

	@JsonProperty("propertyDetails")
	private PropertyDetails propertyDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
	@Valid
	@JsonProperty("estateRentSummary")
	private EstateRentSummary estateRentSummary;
	
	/**
	 * Amount to be paid
	 */
	@JsonProperty("paymentAmount")
	private Double paymentAmount;

	/**
	 * Pending consumer code. This needs to be saved in the database for online
	 * payments.
	 */
	@JsonProperty("rentPaymentConsumerCode")
	@Size(max = 256, message = "Rent payment consumer code must be between 0 and 256 characters in length")
	private String rentPaymentConsumerCode;

	@JsonProperty("transactionId")
	@Size(max = 256, message = "Transaction id must be between 0 and 256 characters in length")
	private String transactionId;

	@JsonProperty("bankName")
	@Size(max = 256, message = "Bank name must be between 0 and 256 characters in length")
	private String bankName;
	
	@JsonProperty("calculation")
	Calculation calculation;
	
	@JsonProperty("billingBusinessService")
	@Size(max = 256, message = "Billing business service must be between 0 and 256 characters in length")
	private String billingBusinessService;
}
