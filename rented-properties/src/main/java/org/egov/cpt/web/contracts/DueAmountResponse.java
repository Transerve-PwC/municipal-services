package org.egov.cpt.web.contracts;

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
public class DueAmountResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

}
