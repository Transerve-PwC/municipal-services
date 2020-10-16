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

	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	@Size(max = 256)
	@JsonProperty("mortgageId")
	private String mortgageId;

	@Size(max = 256)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 256)
	@JsonProperty("name")
	private String name;

	@Email(message = "email is not valid")
	@Size(max = 256)
	@JsonProperty("email")
	private String email;

	@Size(max = 10, min = 10)
	@JsonProperty("phone")
	private String phone;

	@Size(max = 256)
	@JsonProperty("guardian")
	private String guardian;

	@Size(max = 64)
	@JsonProperty("relationship")
	private String relationship;

	@Size(max = 12, min = 12)
	@JsonProperty("adhaarNumber")
	private String adhaarNumber;
	
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;


}
