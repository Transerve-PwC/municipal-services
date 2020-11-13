package org.egov.nulm.model;

import java.util.List;

import org.egov.nulm.model.SusvApplication.StatusEnum;
import org.egov.nulm.workflow.model.Document;

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

public class SusvRenewApplication {

	@JsonProperty("applicationUuId")
	private String applicationUuId;

	@JsonProperty("applicationId")
	private String applicationId;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("lookingFor")
	private String lookingFor;

	@JsonProperty("nameOfStreetVendor")
	private String nameOfStreetVendor;

	@JsonProperty("covNo")
	private String covNo;

	@JsonProperty("susvApplicationFamilyDetails")
	private List<SusvApplicationFamilyDetails> susvApplicationFamilyDetails;
	
	@JsonProperty("residentialAddress")
	private String residentialAddress;

	@JsonProperty("changeOfLocation")
	private Boolean changeOfLocation;

	@JsonProperty("proposedAddress")
	private String proposedAddress;

	@JsonProperty("assignee")
	private List<String> assignee;

	@JsonProperty("action")
	private String action;

	@JsonProperty("applicationStatus")
	private StatusEnum applicationStatus;

	@JsonProperty("applicationDocument")
	private List<SusvApplicationDocument> applicationDocument;

	@JsonProperty("wfDocuments")
    private List<Document> wfDocuments;

	@JsonProperty("nameOfProposedNewStreetVendor")
	private String nameOfProposedNewStreetVendor;

	@JsonProperty("isActive")
	private Boolean isActive;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;

	@JsonProperty("remark")
	private String remark;

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

}
