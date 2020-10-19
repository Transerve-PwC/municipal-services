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
public class EstatePayment implements Comparable<EstatePayment>{

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
	
	/**
	 * boolean indicates whether payment is processed or not
	 */
	@Builder.Default
	private boolean processed = false;

	
	 @Override
	  public int compareTo(EstatePayment other) {
	    return this.getReceiptDate().compareTo(other.getReceiptDate());
	  }
}
