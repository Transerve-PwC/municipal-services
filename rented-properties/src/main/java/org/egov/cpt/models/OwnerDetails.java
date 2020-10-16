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

	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	@Size(max = 256)
	@JsonProperty("propertyId")
	private String propertyId;

	@Size(max = 256)
	@JsonProperty("ownerId")
	private String ownerId;

	@Size(max = 256)
	@JsonProperty("tenantId")
	private String tenantId;

	@NotNull
	@JsonProperty("name")
	@Size(max = 256)
	private String name;

	@NotNull
	@Email(message = "email is not valid")
	@Size(max = 256)
	@JsonProperty("email")
	private String email;

	@Size(max = 10, min = 10)
	@JsonProperty("phone")
	private String phone;

	@Size(min = 4, max = 6)
	@JsonProperty("gender")
	private String gender;

	@JsonProperty("dateOfBirth")
	private Long dateOfBirth;

	@JsonProperty("aadhaarNumber")
	@Size(max = 12, min = 12)
	private String aadhaarNumber;

	@JsonProperty("allotmentStartdate")
	private Long allotmentStartdate;

	@JsonProperty("allotmentEnddate")
	private Long allotmentEnddate;

	@JsonProperty("posessionStartdate")
	private Long posessionStartdate;

	@JsonProperty("posessionEnddate")
	private Long posessionEnddate;

	@Size(max = 256)
	@JsonProperty("monthlyRent")
	private String monthlyRent;

	@Size(max = 256)
	@JsonProperty("revisionPeriod")
	private String revisionPeriod;

	@Size(max = 256)
	@JsonProperty("revisionPercentage")
	private String revisionPercentage;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@NotNull
	@Size(min = 4, max = 256)
	@JsonProperty("fatherOrHusband")
	private String fatherOrHusband;

	@NotNull
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

	@Size(max = 256)
	@JsonProperty("relationWithDeceasedAllottee")
	private String relationWithDeceasedAllottee;

	@JsonProperty("dateOfDeathAllottee")
	private Long dateOfDeathAllottee;

	@Size(max = 256)
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
