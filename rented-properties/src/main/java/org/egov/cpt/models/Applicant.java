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
	@Size(max = 256, message = "id must be between 0 and 256 characters in length")
	private String id;

	@JsonProperty("applicationId")
	@Size(max = 256, message = "application id must be between 0 and 256 characters in length")
	private String applicationId;

	@JsonProperty("tenantId")
	@Size(max = 256, message = "tenant id must be between 0 and 256 characters in length")
	private String tenantId;

	@JsonProperty("name")
	@Size(max = 256, message = "name must be between 0 and 256 characters in length")
	private String name;

	@JsonProperty("email")
	@Size(max = 256, message = "email must be between 0 and 256 characters in length")
	private String email;

	@JsonProperty("phone")
	@Size(max = 10, min = 10, message = "phone must be 10 characters in length")
	private String phone;

	@JsonProperty("guardian")
	@Size(max = 256, message = "guardian must be between 0 and 256 characters in length")
	private String guardian;

	@JsonProperty("relationship")
	@Size(max = 256, message = "relationship must be between 0 and 256 characters in length")
	private String relationship;

	@JsonProperty("adhaarNumber")
	@Size(min = 12, max = 12, message = "adhaar number must be between 12 characters in length")
	private String adhaarNumber;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@JsonProperty("feeAmount")
	private BigDecimal feeAmount;

	@JsonProperty("aproCharge")
	private BigDecimal aproCharge;

}
