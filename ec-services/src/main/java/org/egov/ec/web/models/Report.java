package org.egov.ec.web.models;

import java.util.Date;

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
import lombok.ToString;
@Validated
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Report {
	@Size(max = 64)
	@JsonProperty("reportType")
	@NotNull(message = "reportType should not be empty or null")
	@NotBlank(message = "reportType should not be empty or null")
	private String reportType;
	
	@Size(max = 64)
	@JsonProperty("challanId")
	private String challanId;
	
	@JsonProperty("paymentAmount")
	private Integer paymentAmount;
	
	@JsonProperty("fromDate")
	@NotNull(message = "fromDate should not be empty or null")
	@NotBlank(message = "fromDate should not be empty or null")
	private Date fromDate;
	
	@JsonProperty("toDate")
	@NotNull(message = "toDate should not be empty or null")
	@NotBlank(message = "toDate should not be empty or null")
	private Date toDate;
	
	@Size(max = 64)
	@JsonProperty("violationDate")
	private String violationDate;
	
	@Size(max = 256)
	@JsonProperty("paymentStatus")
	private String paymentStatus;
	
	@Size(max = 256)
	@JsonProperty("sector")
	private String sector;
	
	@Size(max = 64)
	@JsonProperty("encroachmentType")
	private String encroachmentType;
	
	@JsonProperty("siName")
	private String siName;

	@JsonProperty("challanStatus")
	private String challanStatus;
	
	@JsonProperty("violatorName")
	private String violatorName;

	@JsonProperty("age")
	private Integer age;
	
	@Size(max = 256)
	@JsonProperty("violationUuid")
	private String violationUuid;
	
	@Size(max = 256)
	@JsonProperty("vehicleNumber")
	private String vehicleNumber;
	
	@Size(max = 256)
	@JsonProperty("paymentMode")
	private String paymentMode;
	
	@Size(max = 64)
	@JsonProperty("itemsAgeFrom")
	private String itemsAgeFrom;
	
	@Size(max = 64)
	@JsonProperty("itemsAgeTo")
	private String itemsAgeTo;
	
	@Size(max = 256)
	@JsonProperty("itemName")
	private String itemName;
	
	@JsonProperty("itemQuantity")
	private Integer itemQuantity;
	
	@JsonProperty("itemStoreDepositDate")
	private String itemStoreDepositDate;
	
	@Size(max = 256)
	@JsonProperty("createdBy")
	private String createdBy;
	
	@JsonProperty("createdTime")
	private Long createdTime;
	
	@Size(max = 256)
	@JsonProperty("lastModifiedBy")
	private String lastModifiedBy;
	
	@JsonProperty("lastModifiedTime")
	private Long lastModifiedTime;
	
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 256)
	@JsonProperty("transactionId")
	private String transactionId;

}
