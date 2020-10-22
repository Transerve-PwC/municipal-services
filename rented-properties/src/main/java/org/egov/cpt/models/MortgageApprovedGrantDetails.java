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
public class MortgageApprovedGrantDetails {

	@Size(max = 64, message = "Id must be between 0 and 64 characters in length")
	@JsonProperty("id")
	private String id;

	@Size(max = 64, message = "Property detail id must be between 0 and 64 characters in length")
	@JsonProperty("propertyDetailId")
	private String propertyDetailId;

	@Size(max = 64, message = "Owner id must be between 0 and 64 characters in length")
	@JsonProperty("ownerId")
	private String ownerId;

	@Size(max = 64, message = "Tenant id must be between 0 and 64 characters in length")
	@JsonProperty("tenentId")
	private String tenentId;

	@Size(max = 64, message = "Bank name must be between 0 and 64 characters in length")
	@JsonProperty("bankName")
	private String bankName;

	@JsonProperty("mortgageAmount")
	private BigDecimal mortgageAmount;

	@Size(max = 64, message = "Sanction letter number must be between 0 and 64 characters in length")
	@JsonProperty("sanctionLetterNumber")
	private String sanctionLetterNumber;

	@JsonProperty("sanctionDate")
	private Long sanctionDate;

	@JsonProperty("mortgageEndDate")
	private Long mortgageEndDate;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

}
