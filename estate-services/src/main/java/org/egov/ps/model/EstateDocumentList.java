package org.egov.ps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EstateDocumentList {
	
	@EqualsAndHashCode.Include
	@JsonProperty("code")
	private String code;

	@JsonProperty("required")
	private String required;

	@JsonProperty("accept")
	private String accept;

	@JsonProperty("fileType")
	private String fileType;

	@JsonProperty("description")
	private String description;
}
