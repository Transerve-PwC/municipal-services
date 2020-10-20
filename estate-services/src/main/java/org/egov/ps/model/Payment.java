package org.egov.ps.model;

import java.math.BigDecimal;

import org.egov.ps.web.contracts.AuditDetails;
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

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("ownerDetailsId")
	private String ownerDetailsId;

//	main
	
	@JsonProperty("paymentType")
	private String paymentType;

	@JsonProperty("dueDateOfPayment")
	private Long dueDateOfPayment;

	@JsonProperty("payable")
	private BigDecimal payable;

	@JsonProperty("amount")
	private BigDecimal amount;

	@JsonProperty("total")
	private BigDecimal total;

	@JsonProperty("dateOfDeposit")
	private Long dateOfDeposit;

	@JsonProperty("delayInPayment")
	private BigDecimal delayInPayment;

	@JsonProperty("interestForDelay")
	private BigDecimal interestForDelay;

	@JsonProperty("totalAmountDueWithInterest")
	private BigDecimal totalAmountDueWithInterest;

	@JsonProperty("amountDeposited")
	private BigDecimal amountDeposited;

	@JsonProperty("amountDepositedIntt")
	private BigDecimal amountDepositedIntt;

	@JsonProperty("balance")
	private BigDecimal balance;

	@JsonProperty("balanceIntt")
	private BigDecimal balanceIntt;

	@JsonProperty("totalDue")
	private BigDecimal totalDue;

	@JsonProperty("receiptNumber")
	private String receiptNumber;

	@JsonProperty("receiptDate")
	private Long receiptDate;

	@JsonProperty("stRateOfStGst")
	private BigDecimal stRateOfStGst;

	@JsonProperty("stAmountOfGst")
	private BigDecimal stAmountOfGst;

	@JsonProperty("stPaymentMadeBy")
	private String stPaymentMadeBy;

	@JsonProperty("bankName")
	private String bankName;

	@JsonProperty("chequeNumber")
	private String chequeNumber;
	
//  other
	
	@JsonProperty("installmentOne")
	private BigDecimal installmentOne;

	@JsonProperty("installmentTwo")
	private BigDecimal installmentTwo;

	@JsonProperty("installmentTwoDueDate")
	private Long installmentTwoDueDate;

	@JsonProperty("installmentThree")
	private BigDecimal installmentThree;

	@JsonProperty("installmentThreeDueDate")
	private Long installmentThreeDueDate;

	@JsonProperty("monthlyOrAnnually")
	private String monthlyOrAnnually;

	@JsonProperty("groundRentStartDate")
	private Long groundRentStartDate;

	@JsonProperty("rentRevision")
	private int rentRevision;

	@JsonProperty("leasePeriod")
	private int leasePeriod;

	@JsonProperty("licenseFeeOfYear")
	private int licenseFeeOfYear;

	@JsonProperty("licenseFee")
	private BigDecimal licenseFee;
	
	@JsonProperty("securityAmount")
	private BigDecimal securityAmount;

	@JsonProperty("securityDate")
	private Long securityDate;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
