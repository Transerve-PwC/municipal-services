package org.egov.ps.model;

import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object holds the basic data for a Court Case
 */
@ApiModel(description = "A Object holds the basic data for a Court Case")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-31T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class CourtCase {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

	@JsonProperty("estateOfficerCourt")
	private String estateOfficerCourt;

	@JsonProperty("commissionersCourt")
	private String commissionersCourt;

	@JsonProperty("chiefAdministartorsCourt")
	private String chiefAdministartorsCourt;

	@JsonProperty("advisorToAdminCourt")
	private String advisorToAdminCourt;

	@JsonProperty("honorableDistrictCourt")
	private String honorableDistrictCourt;

	@JsonProperty("honorableHighCourt")
	private String honorableHighCourt;

	@JsonProperty("honorableSupremeCourt")
	private String honorableSupremeCourt;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
