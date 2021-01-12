package org.egov.wscalculation.model;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class BillGenerationRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;

	@JsonProperty("billGeneration")
	private BillGeneration billGeneration = null;
}
