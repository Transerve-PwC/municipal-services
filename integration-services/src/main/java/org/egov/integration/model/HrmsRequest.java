package org.egov.integration.model;

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
public class HrmsRequest {
	
	@JsonProperty("empCode")
	private String empCode;
	

	@JsonProperty("month")
	private String month;
	

	@JsonProperty("year")
	private String year;
}
