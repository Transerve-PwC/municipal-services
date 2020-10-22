package org.egov.cpt.models;

import java.math.BigDecimal;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Applicant {

	@JsonProperty("id")
	@Size(max = 256, message = "Id must be between 0 and 256 characters in length")
	private String id;

	@JsonProperty("applicationId")
	@Size(max = 256, message = "Application id must be between 0 and 256 characters in length")
	private String applicationId;

	@JsonProperty("tenantId")
	@Size(max = 256, message = "Tenant id must be between 0 and 256 characters in length")
	private String tenantId;

	@JsonProperty("name")
	@Size(max = 256, message = "Name must be between 0 and 256 characters in length")
	private String name;

	@JsonProperty("email")
	@Size(max = 256, message = "Email must be between 0 and 256 characters in length")
	private String email;

	@JsonProperty("phone")
	@Size(max = 10, min = 10, message = "Phone must be 10 digits in length")
	private String phone;

	@JsonProperty("guardian")
	@Size(max = 256, message = "Guardian must be between 0 and 256 characters in length")
	private String guardian;

	@JsonProperty("relationship")
	@Size(max = 256, message = "Relationship must be between 0 and 256 characters in length")
	private String relationship;

	@JsonProperty("adhaarNumber")
	@Size(min = 12, max = 12, message = "Aadhaar number must be between 12 characters in length")
	private String adhaarNumber;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@JsonProperty("feeAmount")
	private BigDecimal feeAmount;

	@JsonProperty("aproCharge")
	private BigDecimal aproCharge;

}
