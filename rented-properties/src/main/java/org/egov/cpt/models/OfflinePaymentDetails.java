package org.egov.cpt.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class OfflinePaymentDetails {

	@JsonProperty("id")
	private String id;

	@JsonProperty("propertyId")
	  private String propertyId;

	@JsonProperty("demandId")
	private String demandId;

	@JsonProperty("amount")
	private Double amount;

	@JsonProperty("bankName")
	private String bankName;

	@JsonProperty("transactionNumber")
	private String transactionNumber;
}
