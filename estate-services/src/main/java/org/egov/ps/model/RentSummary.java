package org.egov.ps.model;

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
public class RentSummary {

	@Builder.Default
	private double balancePrincipal = 0D;

	// public double getBalancePrincipal() {
	// return this.balancePrincipal;
	// }

	@Builder.Default
	private double balanceInterest = 0D;

	public double getBalanceInterest() {
		if (this.balanceAmount > 0) {
			return Math.max(0, this.balanceInterest - this.balanceAmount);
		}
		return this.balanceInterest;
	}

	@Builder.Default
	private double balanceAmount = 0D;

	public double getBalanceAmount() {
		if (this.balanceAmount == 0) {
			return this.balanceAmount;
		}
		return Math.max(0, this.balanceAmount - this.balanceInterest);
	}

	public String toString() {
		return String.format("Principal : %.2f, Interest: %.2f, Amount: %.2f", this.getBalancePrincipal(),
				this.getBalanceInterest(), this.getBalanceAmount());
	}
}
