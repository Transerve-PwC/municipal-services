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
	@Size(max = 256, message = "Id must be between 0 and 256 characters in length")
	private String id;

	@JsonProperty("propertyId")
	@Size(max = 256, message = "Property id must be between 0 and 256 characters in length")
	private String propertyId;

	@JsonProperty("transitNumber")
	@Size(max = 4, message = "Transit number must be between 0 and 4 characters in length")
	private String transitNumber;

	@JsonProperty("tenantId")
	@Size(max = 256, message = "Tenent id must be between 0 and 256 characters in length")
	private String tenantId;

	@JsonProperty("colony")
	@Size(max = 256, message = "Colony must be between 0 and 256 characters in length")
	private String colony;

	@NotNull
	@JsonProperty("area")
	@Size(min = 3, max = 100, message = "Locality must be between 3 and 100 characters in length")
	private String area;

	@JsonProperty("district")
	@Size(max = 256, message = "District must be between 0 and 256 characters in length")
	private String district;

	@JsonProperty("state")
	@Size(max = 256, message = "State must be between 0 and 256 characters in length")
	private String state;

	@JsonProperty("country")
	@Size(max = 256, message = "Country must be between 0 and 256 characters in length")
	private String country;

	@NotNull
	@JsonProperty("pincode")
	@Size(min = 6, max = 6, message = "Pincode must be 6 digits in length")
	private String pincode;

	@JsonProperty("landmark")
	@Size(max = 256, message = "Landmark must be between 0 and 256 characters in length")
	private String landmark;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;
}
