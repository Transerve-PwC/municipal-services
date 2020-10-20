package org.egov.ps.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object holds the basic data for a Mortgage
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class MortgageDetails {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("ownerId")
	private String ownerId;
	
	@JsonProperty("mortgageType")
	private String mortgageType;

	@JsonProperty("bankName")
	private String bankName;

	@JsonProperty("reasonForMortgage")
	private String reasonForMortgage;

	@JsonProperty("loanAmount")
	private String loanAmount;
	
	@JsonProperty("mortgageDocuments")
	private List<Document> mortgageDocuments;
	
}