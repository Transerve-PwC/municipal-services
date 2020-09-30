package org.egov.nulm.model;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.json.simple.JSONObject;

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

public class SepApplication {
	
	private String applicationUuid ;
	
	private String applicationId ;
	
	private String nulmApplicationId ;
	
	@NotNull
	@JsonProperty("applicationStatus")
	private StatusEnum applicationStatus ;

	public enum StatusEnum {
	    DRAFTED("DRAFTED"),
	    CREATED("CREATED"),
	    APPROVED("APPROVED"),
		REJECTED("REJECTED"),
	    FORWARDEDTOTASKFORCECOMMITTEE("Forwarded to Task force Committee"),
	    APPROVEDBYTASKFORCECOMMITTEE("Approved by Task force committee"),
	    REJECTEDBYTASKFORCECOMMITTEE("Rejected by Task force committee"),
		SENDTOBANKFORPROCESSING("Sent to bank for processing"),
		SANCTIONEDBYBANKBank("Sanctioned by Bank");
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
	private String tenantId ;
	
	
	@JsonProperty("name")
	private String name ;
	
	
	@JsonProperty("gender")
	private String gender ;
	
	
	@JsonProperty("age")
	private Integer age ;
	
	
	@JsonProperty("dob")
	private String dob ;
	
	 @Pattern(regexp = "^[0-9]{4,4}$", message = "AdharNumber should be last 4 digit number")
	@JsonProperty("adharNo")
	private String adharNo ;
	
	
	@JsonProperty("motherName")
	private String motherName ;
	
	
	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName ;
	
	
	@JsonProperty("occupation")
	private String occupation ;
	
	
	@JsonProperty("address")
	private String address ;
	
	 @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid contact number")
	@JsonProperty("contact")
	private String contact ;
	
	
	@JsonProperty("sinceHowLongInChandigarh")
	private String sinceHowLongInChandigarh ;
	
	
	@JsonProperty("qualification")
	private String qualification ;	
	
	
	@JsonProperty("category")
	private String category ;
	
	
	@JsonProperty("isUrbanPoor")
	private Boolean isUrbanPoor ;
	
	@JsonProperty("bplNo")
	private String bplNo ;	
	
	
	@JsonProperty("isMinority")
	private Boolean isMinority ;
	
	@JsonProperty("minority")
	private String minority ;
	
	
	@JsonProperty("isHandicapped")
	private Boolean isHandicapped ;
	
	@JsonProperty("isDisabilityCertificateAvailable")
	private Boolean isDisabilityCertificateAvailable ;
	
	@JsonProperty("typeOfBusinessToBeStarted")
	private String typeOfBusinessToBeStarted ;
	
	
	@JsonProperty("previousExperience")
	private String previousExperience ;
	
	
	@JsonProperty("placeOfWork")
	private String placeOfWork ;
		
	@JsonProperty("noOfFamilyMembers")
	private String noOfFamilyMembers ;		
	
	@DecimalMax(value = "200000", message = "The loan amount can not be more than 2L")
	@JsonProperty("loanAmount")
	private BigDecimal loanAmount ;
	
	@JsonProperty("isLoanFromBankinginstitute")
	private Boolean isLoanFromBankinginstitute ;
	
	
	@JsonProperty("isRepaymentMade")
	private Boolean isRepaymentMade ;
	
	@JsonProperty("recommendedBy")
	private String recommendedBy ;
	
	@JsonProperty("representativeName")
	private String representativeName ;
	
	@JsonProperty("representativeAddress")
	private String representativeAddress ;
	
	@JsonProperty("isActive")
	private Boolean isActive ;
		
	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;
	
	@JsonProperty("remark")
	private String remark ;
	
	@JsonProperty("accountName")
	private String accountName ;
	
	@JsonProperty("bankName")
	private String bankName;
	
	@JsonProperty("branchName")
	private String branchName;
	
	@JsonProperty("taskCommitteeApprovedAmount")
	private String taskCommitteeApprovedAmount;
	
	@JsonProperty("taskCommitteeRemark")
	private String taskCommitteeRemark;
	
	@JsonProperty("taskCommitteeActionDate")
	private String taskCommitteeActionDate;
	
	@JsonProperty("taskCommitteeStatus")
	private String taskCommitteeStatus;
	
	@JsonProperty("committeeBankName")
	private String committeeBankName;
	
	@JsonProperty("committeeBranchName")
	private String committeeBranchName;
	
	@JsonProperty("applicationForwardedOnDate")
	private String applicationForwardedOnDate;
	
	@JsonProperty("sanctionDate")
	private String sanctionDate;
	
	@JsonProperty("sanctionRemarks")
	private String sanctionRemarks;
	
	@JsonProperty("applicationDocument")
	private List<SepApplicationDocument> applicationDocument;
	
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails ;
	
}
