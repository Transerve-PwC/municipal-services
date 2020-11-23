package org.egov.ps.web.contracts;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.ExtensionFee;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtensionFeeRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("ExtensionFees")
	private List<ExtensionFee> extensionFees;
}
