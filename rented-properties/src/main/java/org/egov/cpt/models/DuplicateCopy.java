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

    @Size(max = 256, message = "Id must be between 0 to 256 characters in length")
	@JsonProperty("id")
	private String id;

	@JsonSerialize(using = PropertySerializer.class)
	private Property property;

	@Size(max = 256, message = "Tenant id must be between 0 to 256 characters in length")
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 256, message = "State must be between 0 to 256 characters in length")
	@JsonProperty("state")
	private String state;

	@Size(max = 256, message = "Action must be between 0 to 256 characters in length")
	@JsonProperty("action")
	private String action;

	@Size(max = 64, message = "Application number must be between 0 to 64 characters in length")
	@JsonProperty("applicationNumber")
	private String applicationNumber;

	@Size(max = 256, message = "Allotment number must be between 0 to 256 characters in length")
	@JsonProperty("allotmentNumber")
	private String allotmentNumber;
	
	@JsonProperty("allotmentStartDate")
	private Long allotmentStartDate;

	@JsonProperty("allotmentEndDate")
	private Long allotmentEndDate;

	@JsonProperty("assignee")
	@Builder.Default
	private List<String> assignee = null;

	@Size(max = 128, message = "Comment must be between 0 to 128 characters in length")
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

	@Size(max = 256, message = "Billing business service must be between 0 to 256 characters in length")
	@JsonProperty("billingBusinessService")
	private String billingBusinessService;

	/**
	 * RENTED_PROPERTIES_COLONY_MILK.DUPLICATE_ALLOTMENT_LETTER
	 * @return
	 */
	public String getBillingBusinessService() {
		if (this.property == null) {
			return "";
		}
		return String.format("RENTED_PROPERTIES_%s.DUPLICATE_ALLOTMENT_LETTER", this.property.getColony());
	}

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
