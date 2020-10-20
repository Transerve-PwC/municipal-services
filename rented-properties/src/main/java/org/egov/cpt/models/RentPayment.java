package org.egov.cpt.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
public class RentPayment implements Comparable<RentPayment> {
	/**
	 * Unique id of the demand
	 */
	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	/**
	 * Amount payed by the renter
	 */
	@JsonProperty("amountPaid")
	private Double amountPaid;

	/**
	 * Receipt no of the payment
	 */
	@Size(max = 64)
	@JsonProperty("receiptNo")
	private String receiptNo;

	/**
	 * Property for which the rent is paid for.
	 */
	@Size(max = 256)
	@JsonProperty("propertyId")
	private String propertyId;

	/**
	 * Date of payment
	 */
	@JsonProperty("dateOfPayment")
	private Long dateOfPayment;

	@Size(max = 64)
	@JsonProperty("mode")
	@Builder.Default
	private ModeEnum mode = ModeEnum.UPLOAD;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	/**
	 * boolean indicates whether payment is processed or not
	 */
	@Builder.Default
	private boolean processed = false;

	@Override
	public int compareTo(RentPayment other) {
		return this.getDateOfPayment().compareTo(other.getDateOfPayment());
	}

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yy");

	public String toString() {
		return String.format("Amount: %.2f, paidOn: %s", this.amountPaid, DATE_FORMAT.format(this.dateOfPayment));

	}
}
