package org.egov.integration.model;

import org.egov.common.contract.request.RequestInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PtMappingRequest {
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;
	
	@JsonProperty("PtMappingRequest")
	private PtMapping ptMappingRequest;
}
