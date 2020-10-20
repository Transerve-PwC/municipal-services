package org.egov.ps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Data
@Builder
@NoArgsConstructor
public class EstateCalculationModel {
	
	@JsonProperty("month")
	private String month;
	
	@JsonProperty("rentDue")
	private Double rentDue;
	
	@JsonProperty("rentReceiptNo")
	private String rentReceiptNo;
	
	@JsonProperty("date")
	private Long date;
	
	@JsonProperty("penaltyInterest")
	private Double penaltyInterest;
	
	@JsonProperty("stGstRate")
	private Double stGstRate;
	
	@JsonProperty("stGstDue")
	private Double stGstDue;
	
	@JsonProperty("paid")
	private Double paid;
	
	@JsonProperty("dateOfReceipt")
	private String dateOfReceipt;
	
	@JsonProperty("stGstReceiptNo")
	private String stGstReceiptNo;
	
	@JsonProperty("noOfDays")
	private Integer noOfDays;
	
	@JsonProperty("delayedPaymentOfGST")
	private Double delayedPaymentOfGST;
	
	@JsonProperty("rentReceived")
	private String rentReceived;
	
	@JsonProperty("dueDateOfRent")
	private Long dueDateOfRent;
	
	@JsonProperty("rentDateOfReceipt")
	private Long rentDateOfReceipt;	
		
}
