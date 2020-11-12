package org.egov.nulm.model;

import java.util.List;

import javax.validation.constraints.NotNull;

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

public class SmidShgMemberApplication {
	private String applicationUuid ;
	
	private String applicationId ;
	
	
	@JsonProperty("shgUuid")
	private String shgUuid ;
	
	private String nulmApplicationId ;
	
	
	@JsonProperty("applicationStatus")
	private StatusEnum applicationStatus ;

	public enum StatusEnum {
		DRAFTED("Drafted"),
	    CREATED("Created"),
	    APPROVED("Approved"),
	    AWAITINGFORAPPROVAL("Awaiting for Approval"),
	    UPDATED("Updated"),
	    DELETED("Deleted"),
		REJECTED("Rejected"),
		DELETIONINPROGRESS("Deletion in Progress"),
		AWAITINGFORDELETION("Awaiting for Deletion");

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
	@NotNull
	@JsonProperty("tenantId")
	private String tenantId ;
		
	@JsonProperty("name")
	private String name ;
	
	
	@JsonProperty("positionLevel")
	private String positionLevel ;
		
	@JsonProperty("gender")
	private String gender ;
	
	@JsonProperty("dob")
	private String dob ;
	
	@JsonProperty("dateOfOpeningAccount")
	private String dateOfOpeningAccount ;
	
	@JsonProperty("adharNo")
	private String adharNo ;
	
	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName ;
	
	@JsonProperty("motherName")
	private String motherName ;
	
	@JsonProperty("address")
	private String address ;
	
	@JsonProperty("mobileNo")
	private String mobileNo ;
	
	@JsonProperty("phoneNo")
	private String phoneNo ;

	@JsonProperty("emailId")
	private String emailId ;
	
	@JsonProperty("isUrbanPoor")
	private Boolean isUrbanPoor ;
	
	@JsonProperty("isMinority")
	private Boolean isMinority ;
	
	@JsonProperty("isPwd")
	private Boolean isPwd ;
	
	@JsonProperty("isStreetVendor")
	private Boolean isStreetVendor ;
	
	@JsonProperty("isInsurance")
	private Boolean isInsurance ;
	
	@JsonProperty("isHomeless")
	private Boolean isHomeless ;
	
	
	@JsonProperty("bplNo")
	private String bplNo ;	
	
	@JsonProperty("caste")
	private String caste ;
	
	@JsonProperty("minority")
	private String minority ;
	
	@JsonProperty("wardNo")
	private String wardNo ;
	
	@JsonProperty("nameAsPerAdhar")
	private String nameAsPerAdhar ;
	
	@JsonProperty("adharAcknowledgementNo")
	private String adharAcknowledgementNo ;
	
	@JsonProperty("insuranceThrough")
	private String insuranceThrough ;

	
	@JsonProperty("qualification")
	private String qualification ;	 
	
	@JsonProperty("documentAttachemnt")
	private String documentAttachemnt ;
	
	@JsonProperty("accountNo")
	private String accountNo ;
	
	@JsonProperty("bankName")
	private String bankName;
	
	@JsonProperty("branchName")
	private String branchName;
	
	@JsonProperty("remark")
	private String remark ;
	
	@JsonProperty("isActive")
	private Boolean isActive ;
	
	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;

	private List<SmidShgGroup> smidShgGroup;
			
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails ;
	
	private String groupName;
	private String shgId;
}
