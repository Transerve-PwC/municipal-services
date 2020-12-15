package org.egov.ps.web.contracts;

import javax.validation.constraints.Size;

import org.egov.ps.model.ModeEnum;

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
public class ManiMajraPayment implements Comparable<ManiMajraPayment> {

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
	 * Payment Date of demand.
	 */
	@JsonProperty("paymentDate")
	private Long paymentDate;

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

	/**
	 * Amount payed by the renter
	 */
	@Size(max = 13)
	@JsonProperty("amountPaid")
	private Double amountPaid;

	/**
	 * Date of payment
	 */
	@JsonProperty("dateOfPayment")
	private Long dateOfPayment;

	@Size(max = 64)
	@JsonProperty("mode")
	@Builder.Default
	private ModeEnum mode = ModeEnum.UPLOAD;

	/**
	 * boolean indicates whether payment is processed or not
	 */
	@Builder.Default
	private boolean processed = false;

	// DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yy");

	public String toString() {
		return String.format("Amount: %10.2f, isProcessed: %.5s", this.rentReceived, this.isProcessed());
	}

	@Override
	public int compareTo(ManiMajraPayment other) {
		return this.getReceiptDate().compareTo(other.getReceiptDate());
	}
}
