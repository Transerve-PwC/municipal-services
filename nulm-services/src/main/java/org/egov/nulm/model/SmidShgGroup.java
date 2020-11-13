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

public class SmidShgGroup {
	
	private String shgUuid ;
	private String shgId ;	
	
	@NotNull
	@JsonProperty("tenantId")
	private String tenantId ;
		
	@JsonProperty("name")
	private String name ;
	
	@JsonProperty("type")
	private String type ;
		
	@JsonProperty("formendThrough")
	private String formendThrough ;
	
	@JsonProperty("address")
	private String address ;	
	
	
	@JsonProperty("status")
	private StatusEnum status ;

	public enum StatusEnum {
		DRAFTED("Drafted"),
	    CREATED("Created"),
	    APPROVED("Approved"),
	    REJECTED("Rejected"),
	    DELETED("Deleted"),
	    UPDATED("Updated"),
	    AWAITINGFORAPPROVAL("Awaiting for Approval"),
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
	
	
	@JsonProperty("remark")
	private String remark ;
	
	@JsonProperty("contactNo")
	private String contactNo ;
	
	@JsonProperty("mainAcitivity")
	private String mainAcitivity ;
	
	@JsonProperty("groupNominatedBy")
	private String groupNominatedBy ;
	
	@JsonProperty("dateOfFormation")
	private String dateOfFormation ;
	
	@JsonProperty("accountNo")
	private String accountNo ;
	
	@JsonProperty("dateOfOpeningAccount")
	private String dateOfOpeningAccount ;
	
	@JsonProperty("bankName")
	private String bankName ;
	
	@JsonProperty("branchName")
	private String branchName ;
		
	@JsonProperty("isActive")
	private Boolean isActive ;
				
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails ;
	
	@JsonProperty("smidShgMemberApplication")
	private List<SmidShgMemberApplication> smidShgMemberApplication;
	
	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;


}
