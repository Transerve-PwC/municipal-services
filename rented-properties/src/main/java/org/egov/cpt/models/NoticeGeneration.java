package org.egov.cpt.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.cpt.util.PropertySerializer;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Property
 */
@ApiModel(description = "A Object holds the basic data for a Notice Generation")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class NoticeGeneration {

	@Size(max = 256, message = "id must be between 0 and 256 characters in length")
	@JsonProperty("id")
	private String id;

	@JsonSerialize(using = PropertySerializer.class)
	private Property property;

	@Size(max = 256, message = "tenant id must be between 0 and 256 characters in length")
	@JsonProperty("tenantId")
	private String tenantId;
	
	@Size(max = 64, message = "memo number must be between 0 and 64 characters in length")
	@JsonProperty("memoNumber")
	private String memoNumber;
	
	@Size(max = 256, message = "allotment number must be between 0 and 256 characters in length")
	@JsonProperty("allotmentNumber")
	private String allotmentNumber;
	
	@JsonProperty("memoDate")
	private Long memoDate;
	
	@Size(max = 64, message = "notice type must be between 0 and 64 characters in length")
	@JsonProperty("noticeType")
	private String noticeType;
	
	@Size(max = 256, message = "guardian must be between 0 and 256 characters in length")
	@JsonProperty("guardian")
	private String guardian;
	
	@Size(max = 256, message = "relationship must be between 0 and 256 characters in length")
	@JsonProperty("relationship")
	private String relationship;
	
	@Size(max = 500, message = "violations must be between 0 and 500 characters in length")
	@JsonProperty("violations")
	private String violations;
	
	@Size(max = 256, message = "description must be between 0 and 256 characters in length")
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("demandNoticeFrom")
	private Long demandNoticeFrom;
	
	@JsonProperty("demandNoticeTo")
	private Long demandNoticeTo;
	
	@Size(max = 256, message = "recovert type must be between 0 and 256 characters in length")
	@JsonProperty("recoveryType")
	private String recoveryType;
	
	@JsonProperty("amount")
	private Double amount;
	
	@Size(max = 256, message = "property image id must be between 0 and 256 characters in length")
	@JsonProperty("propertyImageId")
	private String propertyImageId;
	
	@Valid
	@JsonProperty("applicationDocuments")
	private List<Document> applicationDocuments = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;
	
	public NoticeGeneration addApplicationDocumentsItem(Document applicationDocumentsItem) {
		if (this.applicationDocuments == null) {
			this.applicationDocuments = new ArrayList<>();
		}
		for (Document applicationDocument : applicationDocuments) {
			if (applicationDocument.getId().equalsIgnoreCase(applicationDocumentsItem.getId())) {
				return this;
			}
		}
		this.applicationDocuments.add(applicationDocumentsItem);
		return this;
	}
	


}
