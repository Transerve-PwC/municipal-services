package org.egov.hc.contract;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseInfoWrapper {

	public static final String HttpStatus = null;

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("ResponseBody")
	private Object responseBody;
}