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

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 64)
	@JsonProperty("propertyDetailId")
	private String propertyDetailId;

	@Size(max = 64)
	@JsonProperty("ownerId")
	private String ownerId;

	@Size(max = 64)
	@JsonProperty("tenentId")
	private String tenentId;

	@Size(max = 64)
	@JsonProperty("bankName")
	private String bankName;

	@Size(max = 12)
	@JsonProperty("mortgageAmount")
	private BigDecimal mortgageAmount;

	@Size(max = 64)
	@JsonProperty("sanctionLetterNumber")
	private String sanctionLetterNumber;

	@JsonProperty("sanctionDate")
	private Long sanctionDate;

	@JsonProperty("mortgageEndDate")
	private Long mortgageEndDate;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

}
