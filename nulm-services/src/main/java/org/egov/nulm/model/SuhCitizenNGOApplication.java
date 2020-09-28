package org.egov.nulm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class SuhCitizenNGOApplication {

	@JsonProperty("suhCitizenNGOUuid")
	private String suhCitizenNGOUuid;

	@JsonProperty("shelterRequestedForPerson")
	private String shelterRequestedForPerson;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("age")
	private Integer age;

	@JsonProperty("address")
	private String address;

	@JsonProperty("reasonForStaying")
	private String reasonForStaying;

	@JsonProperty("isDisabled")
	private Boolean isDisabled;

	@JsonProperty("nominatedBy")
	private String nominatedBy;
	
	@JsonProperty("nameOfNominatedPerson")
	private String nameOfNominatedPerson;

	@JsonProperty("contactNo")
	private String contactNo;

	@JsonProperty("isActive")
	private Boolean isActive;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;
}
