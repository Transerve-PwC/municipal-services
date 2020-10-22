package org.egov.cpt.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

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
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnerDetails {

	@Size(max = 256, message = "Id must be between 0 and 256 characters in length")
	@JsonProperty("id")
	private String id;

	@Size(max = 256, message = "Property id must be between 0 and 256 characters in length")
	@JsonProperty("propertyId")
	private String propertyId;

	@Size(max = 256, message = "Owner id must be between 0 and 256 characters in length")
	@JsonProperty("ownerId")
	private String ownerId;

	@Size(max = 256, message = "Tenant id must be between 0 and 256 characters in length")
	@JsonProperty("tenantId")
	private String tenantId;

	@NotNull
	@JsonProperty("name")
	@Size(max = 256, message = "Name must be between 0 and 256 characters in length")
	private String name;

	@Email(message = "Email is not valid")
	@Size(max = 256, message = "Email must be between 0 and 256 characters in length")
	@JsonProperty("email")
	private String email;

	@Size(max = 10, min = 10, message = "Phone must be 10 digits in length")
	@JsonProperty("phone")
	private String phone;

	@Size(min = 4, max = 6, message = "Gender must be between 4 and 6 characters in length")
	@JsonProperty("gender")
	private String gender;

	@JsonProperty("dateOfBirth")
	private Long dateOfBirth;

	@JsonProperty("aadhaarNumber")
	@Size(max = 12, min = 12, message = "Aadhaar number must be 12 characters in length")
	private String aadhaarNumber;

	@JsonProperty("allotmentStartdate")
	private Long allotmentStartdate;

	@JsonProperty("allotmentEnddate")
	private Long allotmentEnddate;

	@JsonProperty("posessionStartdate")
	private Long posessionStartdate;

	@JsonProperty("posessionEnddate")
	private Long posessionEnddate;

	@Size(max = 256, message = "Monthly rent must be between 0 and 256 characters in length")
	@JsonProperty("monthlyRent")
	private String monthlyRent;

	@Size(max = 256, message = "Revision period must be between 0 and 256 characters in length")
	@JsonProperty("revisionPeriod")
	private String revisionPeriod;

	@Size(max = 256, message = "Revision percentage must be between 0 and 256 characters in length")
	@JsonProperty("revisionPercentage")
	private String revisionPercentage;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Size(min = 4, max = 256, message = "Father or husband must be between 4 and 256 characters in length")
	@JsonProperty("fatherOrHusband")
	private String fatherOrHusband;

	@Size(max = 256)
	@JsonProperty("relation")
	private String relation;

	public enum ApplicationTypeEnum {
		MASTERRP("MasterEntry"),

		CITIZENRP("CitizenApplication");

		private String value;

		ApplicationTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static ApplicationTypeEnum fromValue(String text) {
			for (ApplicationTypeEnum b : ApplicationTypeEnum.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}

	/**
	 * This value will tell us if this got added as part of property masters or via
	 * ownership transfer application. This should be either MasterEntry or
	 * CitizenApplication. This
	 */
	@Builder.Default
	@JsonProperty("applicationType")
	private ApplicationTypeEnum applicationType = ApplicationTypeEnum.MASTERRP;

	/**
	 * After approval of application this owner becomes permanent.
	 */
	@Builder.Default
	@JsonProperty("permanent")
	private Boolean permanent = false;

	@Size(max = 256, message = "Relation with deceased allottee must be between 0 and 256 characters in length")
	@JsonProperty("relationWithDeceasedAllottee")
	private String relationWithDeceasedAllottee;

	@JsonProperty("dateOfDeathAllottee")
	private Long dateOfDeathAllottee;

	@Size(max = 256, message = "Application number must be between 0 and 256 characters in length")
	@JsonProperty("applicationNumber")
	private String applicationNumber;

	@Valid
	@JsonProperty("ownershipTransferDocuments")
	private List<Document> ownershipTransferDocuments;

	public OwnerDetails addownershipTransferDocumentsItem(Document ownershipTransferDocumentsItem) {
		if (this.ownershipTransferDocuments == null) {
			this.ownershipTransferDocuments = new ArrayList<>();
		}
		if (!this.ownershipTransferDocuments.contains(ownershipTransferDocumentsItem))
			this.ownershipTransferDocuments.add(ownershipTransferDocumentsItem);
		return this;
	}

	@JsonProperty("dueAmount")
	private BigDecimal dueAmount;

	@JsonProperty("aproCharge")
	private BigDecimal aproCharge;

}
