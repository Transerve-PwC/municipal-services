package org.egov.nulm.model;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.egov.nulm.model.SepApplication.StatusEnum;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.simple.JSONArray;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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

public class SuhLogMaintenance {

	private String logUuid;

	@JsonProperty("nameOfShelter")
	private String nameOfShelter;

	@JsonProperty("address")
	private String address;

	@JsonProperty("name")
	private String name;

	@JsonProperty("qualification")
	private String qualification;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("age")
	private String age;

	@JsonProperty("date")
	private String date;

	@JsonProperty("dob")
	private String dob;

	@Pattern(regexp = "^[0-9]{12}$", message = "AdharNumber should be 12 digit number")
	@JsonProperty("aadhaarNo")
	private String aadhaarNo;

	@JsonProperty("reasonForStaying")
	private String reasonForStaying;

	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("isActive")
	private Boolean isActive;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	

	
	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;

}
