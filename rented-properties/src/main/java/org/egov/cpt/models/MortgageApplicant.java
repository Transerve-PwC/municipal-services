package org.egov.cpt.models;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

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
public class MortgageApplicant {

	@Size(max = 256, message = "Id must be between 0 and 256 characters in length")
	@JsonProperty("id")
	private String id;

	@Size(max = 256, message = "Mortgage id must be between 0 and 256 characters in length")
	@JsonProperty("mortgageId")
	private String mortgageId;

	@Size(max = 256, message = "Tenant id must be between 0 and 256 characters in length")
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 256, message = "Name must be between 0 and 256 characters in length")
	@JsonProperty("name")
	private String name;

	@Email(message = "Email is not valid")
	@Size(max = 256, message = "Email must be between 0 and 256 characters in length")
	@JsonProperty("email")
	private String email;

	@Size(max = 10, min = 10, message = "Phone must be 10 digits in length")
	@JsonProperty("phone")
	private String phone;

	@Size(max = 256, message = "Guardian must be between 0 and 256 characters in length")
	@JsonProperty("guardian")
	private String guardian;

	@Size(max = 64, message = "Relationship must be between 0 and 64 characters in length")
	@JsonProperty("relationship")
	private String relationship;

	@Size(max = 12, min = 12, message = "Aadhaar number must be 12 characters in length")
	@JsonProperty("adhaarNumber")
	private String adhaarNumber;
	
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;


}
