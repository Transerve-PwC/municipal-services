package org.egov.ps.model;

import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.web.contracts.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("id")
	private Set<String> id;

	@JsonProperty("transitNumber")
	private String transitNumber;

	@JsonProperty("property_id")
	private String property_id;

	@JsonProperty("owner_id")
	private String owner_id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("email")
	private String email;

	@JsonProperty("phone")
	private String phone;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("dateOfBirth")
	private String dateOfBirth;

	@JsonProperty("aadhaarNumber")
	private String aadhaarNumber;

	@JsonProperty("allotmentStartdate")
	private String allotmentStartdate;

	@JsonProperty("allotmentEnddate")
	private String allotmentEnddate;

	@JsonProperty("posessionStartdate")
	private String posessionStartdate;

	@JsonProperty("posessionEnddate")
	private String posessionEnddate;

	@JsonProperty("allotmenNumber")
	private String allotmenNumber;

	@JsonProperty("applicationStatus")
	private String applicationStatus;

	@JsonProperty("activeState")
	private Boolean activeState;

	@JsonProperty("isPrimaryOwner")
	private String isPrimaryOwner;

	@JsonProperty("monthlyRent")
	private String monthlyRent;

	@JsonProperty("revisionPeriod")
	private String revisionPeriod;

	@JsonProperty("revisionPercentage")
	private String revisionPercentage;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("pageSize")
	private int pageSize;

	@JsonProperty("pageNumber")
	@Default
	private int pageNumber = 0;

	// @JsonProperty("sort")
	// @Default
	// private List<String> sort = Collections.singletonList("name");
	//
	 @JsonProperty("userType")
	 private String userType;
	 
	 @JsonProperty("userName")
	 private String userName;
	 
	 @JsonProperty("tenantId")
	 private String tenantId;
	 
	 
	
	// @JsonProperty("roleCodes")
	// private List<String> roleCodes;

}
