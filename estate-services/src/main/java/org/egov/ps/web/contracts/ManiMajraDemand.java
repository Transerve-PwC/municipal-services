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
public class ManiMajraDemand implements Comparable<ManiMajraDemand> {

	/**
	 * Unique id of the demand
	 */
	@JsonProperty("id")
	private String id;

	/**
	 * Property that this rent is generated for.
	 */
	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

	/**
	 * Date of generation of this demand.
	 */
	@JsonProperty("generationDate")
	private Long generationDate;

	/**
	 * The principal rent amount that is to be collected
	 */
	@JsonProperty("collectionPrincipal")
	private Double collectionPrincipal;

	/**
	 * Rent of demand.
	 */
	@JsonProperty("rent")
	private Double rent;

	/**
	 * GST of demand.
	 */
	@JsonProperty("gst")
	private Double gst;

	/**
	 * Collected Rent of demand.
	 */
	@Builder.Default
	@JsonProperty("collectedRent")
	private Double collectedRent = 0.0;

	/**
	 * Collected GST of demand.
	 */
	@Builder.Default
	@JsonProperty("collectedGST")
	private Double collectedGST = 0.0;

	/**
	 * For comment demand
	 */
	@JsonProperty("comment")
	private String comment;

	/**
	 * paid of demand.
	 */
	@JsonProperty("paid")
	private Double paid;

	@JsonProperty("status")
	@Builder.Default
	private PaymentStatusEnum status = PaymentStatusEnum.UNPAID;

	public boolean isPaid() {
		return this.status == PaymentStatusEnum.PAID;
	}

	public boolean isUnPaid() {
		return !this.isPaid();
	}

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Override
	public int compareTo(ManiMajraDemand other) {
		return this.getGenerationDate().compareTo(other.getGenerationDate());
	}
}
