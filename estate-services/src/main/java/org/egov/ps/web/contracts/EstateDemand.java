package org.egov.ps.web.contracts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.validation.constraints.Size;

import org.egov.ps.model.PaymentStatusEnum;

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
	 * Property details that this demand is generated for.
	 */
	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

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
	 * No of days of demand.
	 */
	@JsonProperty("noOfDays")
	private Double noOfDays;

	/**
	 * paid of demand.
	 */
	@JsonProperty("paid")
	private Double paid;

	@JsonProperty("auditDetails")
	@Builder.Default
	private AuditDetails auditDetails = null;

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

	@Size(max = 64)
	@JsonProperty("status")
	@Builder.Default
	private PaymentStatusEnum status = PaymentStatusEnum.UNPAID;

	@Override
	public int compareTo(EstateDemand other) {
		return this.getGenerationDate().compareTo(other.getGenerationDate());
	}

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

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yy");

	public String toString() {
		return String.format("Collection: %.2f, remaining: %.2f, remainingSince: %s, generatedOn: %s",
				this.collectionPrincipal, this.remainingPrincipal, DATE_FORMAT.format(this.interestSince),
				DATE_FORMAT.format(this.generationDate));
	}

}
