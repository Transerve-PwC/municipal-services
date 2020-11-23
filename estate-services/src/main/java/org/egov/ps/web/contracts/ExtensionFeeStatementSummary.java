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
public class ExtensionFeeStatementSummary {

	@JsonProperty("totalExtensionFee")
	private Double totalExtensionFee;

	@JsonProperty("totalExtensionFeeDue")
	private Double totalExtensionFeeDue;

	@JsonProperty("totalExtensionFeePaid")
	private Double totalExtensionFeePaid;

}
