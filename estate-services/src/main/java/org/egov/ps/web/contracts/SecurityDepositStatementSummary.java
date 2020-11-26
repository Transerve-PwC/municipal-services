package org.egov.ps.web.contracts;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityDepositStatementSummary {

	@JsonProperty("totalSecurityDeposit")
	private Double totalSecurityDeposit;

	@JsonProperty("totalSecurityDepositDue")
	private Double totalSecurityDepositDue;

	@JsonProperty("totalSecurityDepositPaid")
	private Double totalSecurityDepositPaid;

}
