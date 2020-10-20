package org.egov.ps.web.contracts;

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
public class EstatePayment {

	/**
	 * Unique id of the payment
	 */
	@JsonProperty("id")
	private String id;

	/**
	 * Property details that this payment is generated for.
	 */
	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

	/**
	 * Receipt Date of demand.
	 */
	@JsonProperty("receiptDate")
	private Long receiptDate;

	/**
	 * Rent Received of demand.
	 */
	@JsonProperty("rentReceived")
	private Double rentReceived;

	/**
	 * Rent Received of demand.
	 */
	@JsonProperty("receiptNo")
	private String receiptNo;

	@JsonProperty("auditDetails")
	@Builder.Default
	private AuditDetails auditDetails = null;
}
