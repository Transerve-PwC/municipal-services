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
/**
 * Principal = 100, Interest = 50, Amount = 10, then say Principal = 100,
 * Interest = 40, Amount = 0
 */
public class ManiMajraRentSummary {

	@JsonProperty("balanceRent")
	@Builder.Default
	private double balanceRent = 0D;

	@JsonProperty("balanceGST")
	@Builder.Default
	private double balanceGST = 0D;

	// public double getBalancePrincipal() {
	// return this.balancePrincipal;
	// }

	@Builder.Default
	private double rent = 0D;

	@Builder.Default
	private double collectedRent = 0D;

	@Builder.Default
	private double gst = 0D;

	@Builder.Default
	private double collectedGST = 0D;

	@JsonProperty("balanceAmount")
	@Builder.Default
	private double balanceAmount = 0D;

	public double getBalanceAmount() {
		// if (this.balanceAmount == 0) {
		return this.balanceAmount;
		// }
		// return Math.max(0, this.balanceAmount - this.balanceInterest);
	}

	@Builder.Default
	private Boolean isPrevious = false;

	public String toString() {
		return String.format("Due Rent : %.2f, Due GST: %.2f, Due GST penalty: %.2f,Due Rent penalty: %.2f",
				this.getBalanceRent(), this.getBalanceGST());
	}
}
