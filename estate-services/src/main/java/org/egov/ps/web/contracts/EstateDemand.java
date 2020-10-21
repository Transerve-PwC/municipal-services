package org.egov.ps.web.contracts;

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

public class EstateDemand implements Comparable<EstateDemand> {

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
	 * No of days of grace period before interest starts getting applied.
	 */
	@Builder.Default
	@JsonProperty("initialGracePeriod")
	private int initialGracePeriod = 10;

	/**
	 * Date of generation of this demand.
	 */
	@JsonProperty("generationDate")
	private Long generationDate;

	/**
	 * The principal rent amount that is to be collected
	 */
	@Size(max = 13)
	@JsonProperty("collectionPrincipal")
	private Double collectionPrincipal;

	/**
	 * The remaining principal that still has to be collected.
	 */
	@Size(max = 13)
	@Builder.Default
	@JsonProperty("remainingPrincipal")
	private Double remainingPrincipal = 0.0;

	/**
	 * Last date on which interest was made as 0.
	 */
	@JsonProperty("interestSince")
	private Long interestSince;

	/**
	 * Date of demand.
	 */
	@JsonProperty("demandDate")
	private Long demandDate;

	@JsonProperty("isPrevious")
	private Boolean isPrevious;

	/**
	 * Rent of demand.
	 */
	@JsonProperty("rent")
	private Double rent;

	/**
	 * Penalty Interest of demand.
	 */
	@JsonProperty("penaltyInterest")
	private Double penaltyInterest;

	/**
	 * Gst Interest of demand.
	 */
	@JsonProperty("gstInterest")
	private Double gstInterest;

	/**
	 * GST of demand.
	 */
	@JsonProperty("gst")
	private Integer gst;

	/**
	 * Collected Rent of demand.
	 */
	@JsonProperty("collectedRent")
	private Double collectedRent;

	/**
	 * Collected GST of demand.
	 */
	@JsonProperty("collectedGST")
	private Double collectedGST;

	/**
	 * Collected Rent Penalty of demand.
	 */
	@JsonProperty("collectedRentPenalty")
	private Double collectedRentPenalty;

	/**
	 * Collected STt Penalty of demand.
	 */
	@JsonProperty("collectedGSTPenalty")
	private Double collectedGSTPenalty;

	/**
	 * No of days of demand.
	 */
	@JsonProperty("noOfDays")
	private Double noOfDays;

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

	public void setRemainingPrincipalAndUpdatePaymentStatus(Double d) {
		this.setRemainingPrincipal(d);
		if (this.remainingPrincipal == 0) {
			this.status = PaymentStatusEnum.PAID;
		} else {
			this.status = PaymentStatusEnum.UNPAID;
		}
	}

	/**
	 * Last date on which payment made
	 */
	@JsonProperty("paymentSince")
	private Long paymentSince;

	/**
	 * The remaining rent that still has to be collected.
	 */
	@Builder.Default
	@JsonProperty("remainingRent")
	private Double remainingRent = 0.0;

	/**
	 * The remaining GST that still has to be collected.
	 */
	@Builder.Default
	@JsonProperty("remainingGST")
	private Double remainingGST = 0.0;

	/**
	 * The remaining Rent Penalty that still has to be collected.
	 */
	@Builder.Default
	@JsonProperty("remainingRentPenalty")
	private Double remainingRentPenalty = 0.0;

	/**
	 * The remaining GSTPenalty that still has to be collected.
	 */
	@Builder.Default
	@JsonProperty("remainingGSTPenalty")
	private Double remainingGSTPenalty = 0.0;

	@Override
	public int compareTo(EstateDemand other) {
		return this.getDemandDate().compareTo(other.getDemandDate());
	}

	@JsonProperty("auditDetails")
	@Builder.Default
	private AuditDetails auditDetails = null;

}
