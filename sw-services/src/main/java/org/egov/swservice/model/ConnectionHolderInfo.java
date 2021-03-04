package org.egov.swservice.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
public class ConnectionHolderInfo {

	@JsonProperty("ownerInfoUuid")
	private String ownerInfoUuid;

	@JsonProperty("tenantId")
	private String tenantId;

	@NotNull
	@JsonProperty("mobileNumber")
	private String mobileNumber;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName;

	@JsonProperty("correspondenceAddress")
	private String correspondenceAddress;

	@JsonProperty("isPrimaryOwner")
	private Boolean isPrimaryOwner;

	@JsonProperty("ownerShipPercentage")
	private Double ownerShipPercentage;

	@NotNull
	@JsonProperty("ownerType")
	private String ownerType;

	@JsonProperty("name")
	private String name;

	@JsonProperty("guardianName")
	private String guardianName;
	
	@JsonProperty("institutionId")
	private String institutionId;

	@JsonProperty("status")
	private Status status;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents;

	@JsonProperty("relationship")
	private Relationship relationship;

}
