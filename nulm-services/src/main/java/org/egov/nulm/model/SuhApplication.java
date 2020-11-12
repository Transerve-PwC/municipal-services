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

public class SuhApplication {

	private String suhUuid;
	
	private String suhId;
	private String nulmApplicationId ;
	
	@NotNull
	@JsonProperty("applicationStatus")
	private StatusEnum applicationStatus ;

	public enum StatusEnum {
	    DRAFTED("Drafted"),
	    CREATED("Created"),
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
	        if (String.valueOf(b.value).equalsIgnoreCase(text)) {
	          return b;
	        }
	      }
	      return null;
	    }
	  }
	
	@JsonProperty("nameOfShelter")
	private String nameOfShelter;

	@JsonProperty("address")
	private String address;

	@JsonProperty("shelterBackground")
	private String shelterBackground;

	@JsonProperty("category")
	private String category;

	@JsonProperty("weatherCondition")
	private String weatherCondition;

	@JsonProperty("shelterClassification")
	private String shelterClassification;

	@JsonProperty("otherClassification")
	private String otherClassification;
	
	@JsonProperty("ownership")
	private String ownership;

	@JsonProperty("operationAndManagementOfShelters")
	private String operationAndManagementOfShelters;

	@JsonProperty("area")
	private String area;

	@JsonProperty("capacity")
	private String capacity;

	@JsonProperty("isConstitutionOfShelterManagementCommittee")
	private Boolean isConstitutionOfShelterManagementCommittee;

	@JsonProperty("constitutionOfShelterManagementCommitteeRemark")
	private String constitutionOfShelterManagementCommitteeRemark;

	@JsonProperty("isSocialAudit")
	private Boolean isSocialAudit;
	
	@JsonProperty("socialAuditRemark")
	private String socialAuditRemark;
	
	@JsonProperty("isLinkageToCentralGovtWelfareSchemes")
	private Boolean isLinkageToCentralGovtWelfareSchemes;
	
	@JsonProperty("linkageToCentralGovtWelfareSchemesRemark")
	private String linkageToCentralGovtWelfareSchemesRemark;
	
	@JsonProperty("isLinkageToPublicHealthInitiatives")
	private Boolean isLinkageToPublicHealthInitiatives;
	
	@JsonProperty("linkageToPublicHealthInitiativesRemark")
	private String linkageToPublicHealthInitiativesRemark;
	
	@JsonProperty("isLinkageToOtherGovtSchemes")
	private Boolean isLinkageToOtherGovtSchemes;

	@JsonProperty("linkageToOtherGovtSchemesRemark")
	private String linkageToOtherGovtSchemesRemark;
	
	@JsonProperty("isLinkageToLocalCommunity")
	private Boolean isLinkageToLocalCommunity;
	
	@JsonProperty("linkageToLocalCommunityRemark")
	private String linkageToLocalCommunityRemark;
	
	@JsonProperty("isLinkageToSocialWorkersAndPhilanthropists")
	private Boolean isLinkageToSocialWorkersAndPhilanthropists;
	
	@JsonProperty("linkageToSocialWorkersAndPhilanthropistsRemark")
	private String linkageToSocialWorkersAndPhilanthropistsRemark;

	@JsonProperty("isUserCharges")
	private Boolean isUserCharges;
	
	@JsonProperty("userChargesRemark")
	private String userChargesRemark;

	@JsonProperty("isIECAndPromotionalInitiatives")
	private Boolean isIECAndPromotionalInitiatives;
	
	@JsonProperty("iecAndPromotionalInitiativesRemark")
	private String iecAndPromotionalInitiativesRemark;

	@JsonProperty("isQuarterlyReporting")
	private Boolean isQuarterlyReporting;
	
	@JsonProperty("quarterlyReportingRemark")
	private String quarterlyReportingRemark;

	@JsonProperty("isVisits")
	private Boolean isVisits;
	
	@JsonProperty("visitsRemark")
	private String visitsRemark;
	
	@JsonProperty("addressPicture")
	private JSONArray addressPicture;
	
	@JsonProperty("programPicture")
	private JSONArray programPicture;
	
	@JsonProperty("documentAttachment")
	private JSONArray documentAttachment;
	
	private String addressPictureDoc;
	
	private String programPictureDoc;
	private String documentAttachmentDoc;
	
	@JsonProperty("remark")
	private String remark;

	@JsonProperty("suhFacilitiesDetails")
	private List<SuhFacilitiesDetails> suhFacilitiesDetails;
	
	@JsonProperty("suhRecordMaintenance")
	private List<SuhRecordMaintenance> suhRecordMaintenance;
	
	@JsonProperty("suhStaffMaintenance")
	private List<SuhStaffMaintenance> suhStaffMaintenance;
	
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
	
	@JsonProperty("assignedTo")
	private String assignedTo;
	
	@JsonProperty("assignedToName")
	private String assignedToName;
}
