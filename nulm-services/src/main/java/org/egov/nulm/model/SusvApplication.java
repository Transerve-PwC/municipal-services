package org.egov.nulm.model;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.egov.nulm.workflow.model.Document;
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

public class SusvApplication {

	private String applicationUuid;

	private String applicationId;

	private String nulmApplicationId;

	
	@JsonProperty("applicationStatus")
	private StatusEnum applicationStatus;

	public enum StatusEnum {
		DRAFTED("Drafted"), 
		CREATED("Created"), 
		FORWARDEDTOJA("Forwarded To JA"),
		FORWARDEDTOSDO("Forwarded To SDO"), 
		FORWARDEDTOACMC("Forwarded To ACMC"),
		REASSIGNTOJA("Reassign To JA"), 
		REASSIGNTOSDO("Reassign To SDO"), 
		REASSIGNTOCITIZEN("Reassign To Citizen"), 
		APPROVED("Approved"), 
		REJECTED("Rejected");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static StatusEnum fromValue(String text) {
			for (StatusEnum b : StatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("nameOfApplicant")
	private String nameOfApplicant;

	@JsonProperty("gender")
	private String gender;

	@JsonProperty("age")
	private Integer age;

	@JsonProperty("adharNo")
	private String adharNo;

	@JsonProperty("motherName")
	private String motherName;

	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName;

	@JsonProperty("presentAddress")
	private String presentAddress;

	@JsonProperty("permanentAddress")
	private String permanentAddress;

	@JsonProperty("category")
	private String category;

	@JsonProperty("mobileNo")
	private String mobileNo;

	@JsonProperty("isDisability")
	private Boolean isDisability;

	@JsonProperty("qualification")
	private String qualification;

	@JsonProperty("bloodGroup")
	private String bloodGroup;

	@JsonProperty("categoryOfVending")
	private String categoryOfVending;

	@JsonProperty("proposedLocationOfVending")
	private String proposedLocationOfVending;

	@JsonProperty("proposedTimeOfVending")
	private String proposedTimeOfVending;

	@JsonProperty("govermentScheme")
	private String govermentScheme;

	@JsonProperty("nameOfNominee")
	private String nameOfNominee;

	@JsonProperty("isActive")
	private Boolean isActive;


	@JsonProperty("applicationDocument")
	private List<SusvApplicationDocument> applicationDocument;
	
	@JsonProperty("susvApplicationFamilyDetails")
	private List<SusvApplicationFamilyDetails> susvApplicationFamilyDetails;

	@JsonProperty("dob")
	private String dob;

	@JsonProperty("fromDate")
	private String fromDate;
	

	@JsonProperty("toDate")
	private String toDate;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("remark")
	private String remark;

	@JsonProperty("isUndertaking")
	private Boolean isUndertaking;

	@JsonProperty("place")
	private String place;

	@JsonProperty("date")
	private String date;
	
	@JsonProperty("assignee")
	private List<String> assignee = null;

	@JsonProperty("action")
	private String action = null;



	@JsonProperty("wfDocuments")
    private List<Document> wfDocuments;
}
