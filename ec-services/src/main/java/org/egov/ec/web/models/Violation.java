package org.egov.ec.web.models;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Violation {
	  
	@Size(max = 64)
	@JsonProperty("violationUuid")
	private String violationUuid;
	
	
	
	@Size(max = 64,message="Size Cannot be more than 64 characters")
	@NotNull(message = "challanUuid  should not be empty or null")
	@NotBlank(message = "challanUuid  should not be empty or null")
	@JsonProperty("challanUuid")
	private String challanUuid;
	
	@NotNull(message = "tenantId  should not be empty or null")
	@NotBlank(message = "tenantId  should not be empty or null")
	@JsonProperty("tenantId")
	private String tenantId;
	
	@NotNull(message = "challanUuid  should not be empty or null")
	@NotBlank(message = "challanUuid  should not be empty or null")
	@JsonProperty("numberOfViolation")
	private String numberOfViolation;
	
	@Size(max = 64,message="Size Cannot be more than 64 characters")
	@NotNull(message = "challanId  should not be empty or null")
	@NotBlank(message = "challanId  should not be empty or null")
	@JsonProperty("challanId")
	private String challanId;
	
	
	@Size(max = 64)
	@JsonProperty("encroachmentType")
	@NotNull(message = "encroachmentType  should not be empty or null")
	@NotBlank(message = "encroachmentType  should not be empty or null")
	private String encroachmentType;
	
	@JsonProperty("violationDate")
	@NotNull(message = "violationDate  should not be empty or null")
	@NotBlank(message = "violationDate  should not be empty or null")
	private String violationDate;
	
	@JsonProperty("violationTime")
	@NotNull(message = "violationTime  should not be empty or null")
	@NotBlank(message = "violationTime  should not be empty or null")
	private String violationTime;
	
	@Size(max = 256)
	@NotNull(message = "violatorName  should not be empty or null")
	@NotBlank(message = "violatorName  should not be empty or null")
	@JsonProperty("violatorName")
	private String violatorName;
	
	@Size(max = 64)
	@NotNull(message = "sector  should not be empty or null")
	@NotBlank(message = "sector  should not be empty or null")
	@JsonProperty("sector")
	private String sector;
	
	@Size(max = 512)
	@JsonProperty("address")
	private String address;
	
	@Size(max = 64)
	@JsonProperty("emailId")
	private String emailId;
	
	@Size(max = 256)
	//@NotNull(message = "fatherName  should not be empty or null")
	//@NotBlank(message = "fatherName  should not be empty or null")
	@JsonProperty("fatherName")
	private String fatherName;

	@Size(max = 64)
	@NotNull(message = "contactNumber  should not be empty or null")
	@NotBlank(message = "contactNumber  should not be empty or null")
	@JsonProperty("contactNumber")
	private String contactNumber;
	
	@Size(max = 256)
	/*
	 * @NotNull(message = "licenseNoCov  should not be empty or null")
	 * 
	 * @NotBlank(message = "licenseNoCov  should not be empty or null")
	 */
	@JsonProperty("licenseNoCov")
	private String licenseNoCov;
	
	@Size(max = 256)
	@NotNull(message = "natureOfViolation  should not be empty or null")
	@NotBlank(message = "natureOfViolation  should not be empty or null")
	@JsonProperty("natureOfViolation")
	private String natureOfViolation;
	
	
	@Size(max = 256)
	@NotNull(message = "location  should not be empty or null")
	@NotBlank(message = "location  should not be empty or null")
	@JsonProperty("location")
	private String location;
	
	@Size(max = 256)
	@JsonProperty("paymentMode")
	private String paymentMode;
	
	@JsonProperty("challanAmount")
	private String challanAmount;
	
    @JsonProperty("fineAmount")
	private String fineAmount;
    
    @JsonProperty("penaltyAmount")
	private String penaltyAmount;
    
    @JsonProperty("gstAmount")
	private String gstAmount;
    
    @JsonProperty("totalChallanAmount")
	private String totalChallanAmount;
	
	@Size(max = 256)
	@NotNull(message = "siName  should not be empty or null")
	@NotBlank(message = "siName  should not be empty or null")
	@JsonProperty("siName")
	private String siName;
	
	@Size(max = 64)
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("isActive")
	private Boolean isActive;
	
	@Size(max = 256)
	@JsonProperty("createdBy")
	private String createdBy;
	
	@JsonProperty("createdTime")
	@NotNull(message = "createdTime  should not be empty or null")
	private Long createdTime;
	
	@Size(max = 256)
	@JsonProperty("lastModifiedBy")
	private String lastModifiedBy;
	
	@JsonProperty("lastModifiedTime")
	@NotNull(message = "lastModifiedTime  should not be empty or null")
	private Long lastModifiedTime;
	
	@JsonProperty("document")
	private List<Document> document;
	
	@JsonProperty("violationItem")
	private List<ViolationItem> violationItem;
	
	@JsonProperty("notificationTemplate")
	private NotificationTemplate notificationTemplate;
	
	@JsonProperty("challanDetails")
	private List<ChallanDetails> challanDetails;

	@JsonProperty("paymentDetails")
	private EcPayment paymentDetails;
	
	@JsonProperty("sourceUuid")
	private String sourceUuid;
	
	@JsonProperty("fromDate")
	private Date fromDate;
	
	@JsonProperty("toDate")
	private Date toDate;
	
}
