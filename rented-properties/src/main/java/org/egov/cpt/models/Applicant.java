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
	@Size(max = 256)
	private String id;

	@JsonProperty("applicationId")
	@Size(max = 256)
	private String applicationId;

	@JsonProperty("tenantId")
	@Size(max = 256)
	private String tenantId;

	@JsonProperty("name")
	@Size(max = 256)
	private String name;

	@JsonProperty("email")
	@Size(max = 256)
	private String email;

	@JsonProperty("phone")
	@Size(max = 10, min = 10)
	private String phone;

	@JsonProperty("guardian")
	@Size(min =10, max = 256)
	private String guardian;

	@JsonProperty("relationship")
	@Size(max = 256)
	private String relationship;

	@JsonProperty("adhaarNumber")
	@Size(min = 12, max = 12)
	private String adhaarNumber;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@JsonProperty("feeAmount")
	private BigDecimal feeAmount;

	@JsonProperty("aproCharge")
	private BigDecimal aproCharge;

}
