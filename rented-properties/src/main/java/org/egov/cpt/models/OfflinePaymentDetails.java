package org.egov.cpt.models;

import javax.validation.constraints.Size;

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

	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	@Size(max = 256)
	@JsonProperty("propertyId")
	private String propertyId;

	@Size(max = 256)
	@JsonProperty("demandId")
	private String demandId;

	@JsonProperty("amount")
	private Double amount;

	@Size(max = 100)
	@JsonProperty("bankName")
	private String bankName;

	@Size(max = 100)
	@JsonProperty("transactionNumber")
	private String transactionNumber;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
