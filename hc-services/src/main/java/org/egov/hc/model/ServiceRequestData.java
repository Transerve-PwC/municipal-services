package org.egov.hc.model;


import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.hc.contract.AuditDetails;
import org.egov.hc.model.user.Citizen;
import org.egov.hc.workflow.Document;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequestData {
	
	@Size(max = 64)
	@NotNull
	@JsonProperty("service_request_uuid")
	private String service_request_uuid;
	
	@Size(max=255)
	@JsonProperty("ownerName")
	private String ownerName = null;
	
	@Size(max=128)
	@JsonProperty("tenantId")
	private String tenantId;
	
	@Size(max=128)
	@JsonProperty("current_assignee")
	private String current_assignee;

	@Size(max=257)
	@JsonProperty("address")
	private String address;
	
    @JsonProperty("currentState")
	String currentState;
	
	@Size(max=257)
	@JsonProperty("latitude")
	private String latitude;
	
	@Size(max=257)
	@JsonProperty("longitude")
	private String longitude;
	
	@Size(max=257)
	@JsonProperty("locality")
	private String locality;
	
	@Size(max=256)
	@JsonProperty("mohalla")
	private String mohalla;
	
	@Size(max=259)
	@JsonProperty("landmark")
	private String landmark;
	
	
	@JsonProperty("houseNoAndStreetName")
	public String houseNoAndStreetName;

	
	@Size(max=50)
	@JsonProperty("contactNumber")
	private String contactNumber = null;
	
	@Size(max=255)
	@JsonProperty("email")
	private String email = null;
	
	@JsonProperty("treeCount")
	private Long treeCount;
	
	@Size(max=255)
	@JsonProperty("service_request_date")
	private String service_request_date;
	
	@Size(max=255)
	@JsonProperty("city")
	private String city;
	
	@Size(max=255)
	@JsonProperty("service_request_status")
	private String service_request_status;
	
	@Size(max=255)
	@JsonProperty("service_request_id")
	private String service_request_id;
	
	@Size(max=255)
	@JsonProperty("service_request_id_old")
	private String service_request_id_old;
	
	@Size(max=255)
	@JsonProperty("history_service_request_id")
	private String history_service_request_id;
	
	@Size(max=64)
	@JsonProperty("serviceType")
	private String serviceType;
	
	@Size(max=64)
	@JsonProperty("description")
	private String description;
	

	@Size(max=64)
	@JsonProperty("createdBy")
	private String createdBy;
	
	@JsonProperty("createdTime")
	private Long createdTime;
	
	@Size(max=256)
	@JsonProperty("state")
	private String state;
	
	@Size(max = 64)
	@JsonProperty("lastModifiedBy")
	private String lastModifiedBy;
	
	
	@JsonProperty("serviceRequestSubtype")
	private String serviceRequestSubtype;
	
	@Size(max = 64)
	@JsonProperty("subType")
	private String subType;
	
	@JsonProperty("lastModifiedTime")
	private Long lastModifiedTime;
	

	@Size(max=250)
	@JsonProperty("eg_filestoremap_id_list")
	private org.json.simple.JSONArray eg_filestoremap_id_list;

	  @JsonProperty("accountId")
	  private String accountId;
	  
	  
	  @JsonProperty("citizen")
	  private Citizen citizen;
	  
	  
	  @JsonProperty("auditDetails")
	  private AuditDetails auditDetails;

	  
	  @JsonProperty("addressDetail")
	  private AddressDetail addressDetail;
	
	
	  @JsonProperty("active")
	  private Boolean active;
	  
	  @Size(max=250)
	  @JsonProperty("media")
	  private org.json.simple.JSONArray media;
	  
	  @Size(max=250)
	@JsonProperty("service_request_document")
	private org.json.simple.JSONObject service_request_document;
		
	private String serviceMedia;
	 
	@JsonProperty("isRoleSpecific")
	private Boolean isRoleSpecific;
		 
				
	 private String serviceRequestDate;
	  

	  /**
	   * The current status of the service request.
	   */
	  public enum StatusEnum {
		  
		  Pending("Pending"),
			
			ASSIGNED("Asigned"),
			        
		    COMPLETED("Completed"),
		    
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

	  @JsonProperty("status")
	  private StatusEnum status;

	  /**
	   * source of the complaint - Text, Mobile app, Phone, CSC, WhatsApp
	   */
	  public enum SourceEnum {
	    SMS("sms"),
	    
	    EMAIL("email"),
	    
	    IVR("ivr"),
	    
	    MOBILEAPP("mobileapp"),
	    
	    WHATSAPP("whatsapp"),
	    
	    CSC("csc"),
	    
	    WEB("web");

	    private String value;

	    SourceEnum(String value) {
	      this.value = value;
	    }

	    @Override
	    @JsonValue
	    public String toString() {
	      return String.valueOf(value);
	    }

	    @JsonCreator
	    public static SourceEnum fromValue(String text) {
	      for (SourceEnum b : SourceEnum.values()) {
	        if (String.valueOf(b.value).equals(text)) {
	          return b;
	        }
	      }
	      return null;
	    }
	  }

	

	

	  @JsonProperty("rating")
	  @Max(5)
	  @Min(0)
	  private String rating;
	  
	  @NotNull
      @Size(max=64)
      @JsonProperty("action")
      private String action = null;

      @JsonProperty("assignee")
      private List<String> assignee = null;
      
      @JsonProperty("businessService")
      private String businessService = null;
      
      @JsonProperty("documentList")
      private List<Document> documentList;
      
      @JsonProperty("wfDocuments")
      private List<Document> wfDocuments;
      
      @JsonProperty("comment")
      private String comment = null;
      
      @JsonProperty("documentType")
      private String documentType = null;
      
      @Size(max=255)
  	@JsonProperty("role")
  	private String role = null;
      
      @Size(max=255)
    	@JsonProperty("roleList")
    	private List<String> roleList = null;
  	
  	
  	@JsonProperty("businessservicesla")
  	private Long businessservicesla = null;
  	
  	@JsonProperty("mediaList")
    private List<String> mediaList;
  	
  	@JsonProperty("isEditState")
	private int isEditState;
  	

  	@Size(max=255)
  	@JsonProperty("servicerequest_lang")
  	private String servicerequest_lang = null;
  	
}
