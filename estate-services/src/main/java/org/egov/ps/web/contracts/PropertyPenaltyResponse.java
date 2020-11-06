package org.egov.ps.web.contracts;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
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
public class PropertyPenaltyResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo ResponseInfo;

	@JsonProperty("PropertyPenaltys")
	@Valid
	private List<PropertyPenalty> PropertyPenaltys;
}
