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
	 * For comment demand
	 */
	@JsonProperty("comment")
	private String comment;

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

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Override
	public int compareTo(ManiMajraDemand other) {
		return this.getGenerationDate().compareTo(other.getGenerationDate());
	}

	/**
	 * The principal rent amount that is to be collected
	 */
	@JsonProperty("collectionPrincipal")
	private Double collectionPrincipal;

	/**
	 * Demand Type Monthly or Annually
	 */
	@JsonProperty("typeOfDemand")
	private String typeOfDemand;
}
