package org.egov.ps.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.validation.annotation.Validated;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Payment
 */
@ApiModel(description = "A Object holds the basic data for a Payment")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-08-10T13:06:11.263+05:30")

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

	/**
	 * Unique id of the payment
	 */
	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	/**
	 * Property details that this payment is generated for.
	 */
	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

	@JsonProperty("isIntrestApplicable")
	private Boolean isIntrestApplicable;

	@JsonProperty("dueDateOfPayment")
	private Long dueDateOfPayment;

	@JsonProperty("noOfMonths")
	private Long noOfMonths;

	@JsonProperty("rateOfInterest")
	private BigDecimal rateOfInterest;

	@JsonProperty("securityAmount")
	private BigDecimal securityAmount;

	@JsonProperty("totalAmount")
	private BigDecimal totalAmount;

	@JsonProperty("isGroundRent")
	private Boolean isGroundRent;

	@JsonProperty("groundRentGenerationType")
	private String groundRentGenerationType;

	@JsonProperty("groundRentGenerateDemand")
	private String groundRentGenerateDemand;

	@JsonProperty("groundRentAdvanceRent")
	private BigDecimal groundRentAdvanceRent;

	@JsonProperty("groundRentBillStartDate")
	private Long groundRentBillStartDate;

	@JsonProperty("groundRentAdvanceRentDate")
	private Long groundRentAdvanceRentDate;

	@JsonProperty("premiumAmountConfigItems")
	private List<PremiumAmountConfigItems> premiumAmountConfigItems;

	@JsonProperty("paymentConfigItems")
	private List<PaymentConfigItems> paymentConfigItems;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
