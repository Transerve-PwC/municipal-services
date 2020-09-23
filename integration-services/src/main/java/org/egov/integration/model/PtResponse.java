package org.egov.integration.model;

import org.json.simple.JSONArray;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PtResponse {
	@JsonProperty("Status")
	private String status;
	
	@JsonProperty("Msg")
	private String msg;
	
	@JsonProperty("TokenId")
	private String tokenId;
	
	@JsonProperty("OTP")
	private String otp;
	
	@JsonProperty("PropertyDetail")
	private JSONArray propertyDetail;
	
	@JsonProperty("PropertyTaxCalculation")
	private JSONArray propertyTaxCalculation;
	
	@JsonProperty("PayableAmount")
	private String payableAmount;
}
