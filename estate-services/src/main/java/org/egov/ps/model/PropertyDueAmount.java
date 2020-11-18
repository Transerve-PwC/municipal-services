package org.egov.ps.model;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.ps.web.contracts.EstateRentSummary;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Property
 */
@ApiModel(description = "A Object holds the basic data for a Property")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PropertyDueAmount {

	@Size(max = 256)
	@JsonProperty("propertyId")
	private String propertyId;

	@Size(max = 256)
	@JsonProperty("fileNumber")
	private String fileNumber;

	@Size(max = 256)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 256)
	@JsonProperty("propertyType")
	private String propertyType;
	
	@Size(max = 256)
	@JsonProperty("sectorNumber")
	private String sectorNumber;
	
	@Size(max = 256)
	@JsonProperty("ownerName")
	private String ownerName;
	
	@Size(max = 10, min = 10)
	@JsonProperty("mobileNumber")
	private String mobileNumber;

	@Valid
	@JsonProperty("estateRentSummary")
	private EstateRentSummary estateRentSummary;

}