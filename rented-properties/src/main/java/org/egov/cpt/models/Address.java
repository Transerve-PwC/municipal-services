package org.egov.cpt.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
public class Address {

	@JsonProperty("id")
	@Size(max = 256)
	private String id;

	@JsonProperty("propertyId")
	@Size(max = 256)
	private String propertyId;

	@JsonProperty("transitNumber")
	@Size(min = 4, max = 256)
	private String transitNumber;

	@JsonProperty("tenantId")
	@Size(max = 256)
	private String tenantId;

	@JsonProperty("colony")
	@Size(max = 256)
	private String colony;

	@NotNull
	@JsonProperty("area")
	@Size(max = 256)
	private String area;

	@JsonProperty("district")
	@Size(max = 256)
	private String district;

	@JsonProperty("state")
	@Size(max = 256)
	private String state;

	@JsonProperty("country")
	@Size(max = 256)
	private String country;

	@NotNull
	@JsonProperty("pincode")
	@Size(min = 6, max = 256)
	private String pincode;

	@JsonProperty("landmark")
	@Size(max = 256)
	private String landmark;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;
}
