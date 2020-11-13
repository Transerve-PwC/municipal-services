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
public class PenaltyStatementSummary {

	@JsonProperty("totalPenalty")
	private Double totalPenalty;

	@JsonProperty("totalPenaltyDue")
	private Double totalPenaltyDue;

	@JsonProperty("totalPenaltyPaid")
	private Double totalPenaltyPaid;

}
