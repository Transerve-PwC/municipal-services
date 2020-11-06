package org.egov.ps.web.contracts;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.PropertyPenalty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyPenaltyRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("PropertyPenalties")
	private List<PropertyPenalty> propertyPenalties;
}
