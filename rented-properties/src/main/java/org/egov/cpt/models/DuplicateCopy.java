package org.egov.cpt.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.cpt.models.calculation.Calculation;
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
@ApiModel(description = "A Object holds the basic data for a Property")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DuplicateCopy {

	@JsonProperty("id")
	private String id;

	@JsonSerialize(using = PropertySerializer.class)
	private Property property;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("state")
	private String state;

	@JsonProperty("action")
	private String action;

	@JsonProperty("applicationNumber")
	private String applicationNumber;

	@JsonProperty("allotmentNumber")
	private String allotmentNumber;
	
	@JsonProperty("allotmentStartDate")
	private Long allotmentStartDate;
	
	@JsonProperty("allotmentEndDate")
	private Long allotmentEndDate;

	@JsonProperty("assignee")
	@Builder.Default
	private List<String> assignee = null;

	@Size(max = 128)
	@JsonProperty("comment")
	private String comment;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Valid
	@JsonProperty("applicationDocuments")
	private List<Document> applicationDocuments;

	@Valid
	@JsonProperty("applicant")
	private List<Applicant> applicant;

	@Valid
	@JsonProperty("wfDocuments")
	private List<Document> wfdocuments;

	@JsonProperty("calculation")
	Calculation calculation;

	public DuplicateCopy addApplicationDocumentsItem(Document newApplicationDocumentsItem) {
		if (this.applicationDocuments == null) {
			this.applicationDocuments = new ArrayList<>();
		}
		for (Document applicationDocument : applicationDocuments) {
			if (applicationDocument.getId().equalsIgnoreCase(newApplicationDocumentsItem.getId())) {
				return this;
			}
		}
		this.applicationDocuments.add(newApplicationDocumentsItem);
		return this;
	}

}
