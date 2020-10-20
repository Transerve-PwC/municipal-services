package org.egov.ps.web.contracts;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.egov.common.contract.response.ResponseInfo;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstateCalculationResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Calculations")
	@Valid

	private EstateModuleResponse estateModuleResponse;
	

}
