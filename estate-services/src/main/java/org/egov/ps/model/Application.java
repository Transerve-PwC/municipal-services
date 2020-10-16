package org.egov.ps.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.validation.annotation.Validated;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object holds the basic data for a Application
 */
@ApiModel(description = "A Object holds the basic data for a Application")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-08-12T10:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Application {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	/**
	 * Property for which we are trying to create this application for.
	 */
	// @JsonSerialize(using = PropertySerializer.class)
	@JsonProperty("property")
	private Property property;

	/**
	 * Generated number that will be visible to employees and to the user.
	 */
	@JsonProperty("applicationNumber")
	private String applicationNumber;

	/**
	 * This will be one of "Estate Branch", "Building Branch", "Manimajra Branch"
	 */
	@JsonProperty("branchType")
	private String branchType;

	/**
	 * This will be one of "TransferOfOwnership", "OtherCitizenServices",
	 * "InternalServices" "CitizenServices"
	 */
	@JsonProperty("moduleType")
	private String moduleType;

	/**
	 * This should be an enum.
	 */
	@JsonProperty("applicationType")
	private String applicationType;

	@JsonProperty("assignee")
	@Builder.Default
	private List<String> assignee = null;

	/**
	 * Latest comments entered by an employee during workflow.
	 */
	@JsonProperty("comments")
	private String comments;

	/**
	 * Date of documents hard copies received at MCC by DispatchSectionOfficer.
	 */
	@JsonProperty("hardcopyReceivedDate")
	private Long hardcopyReceivedDate;

	/**
	 * A JSON string that contains all the application details.
	 */
	@JsonProperty("applicationDetails")
	private JsonNode applicationDetails;

	/**
	 * The current workflow status of application.
	 */
	@JsonProperty("state")
	private String state;

	/**
	 * The latest workflow action performed on this application.
	 */
	@JsonProperty("action")
	private String action;

	/**
	 * History of changes for this application.
	 */
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("workFlowBusinessService")
	private String workFlowBusinessService;

	@JsonProperty("billingBusinessService")
	private String billingBusinessService;

	public String getWorkFlowBusinessService() {
		return String.format("ES-%s-%s-%s", extractPrefix(this.getBranchType()), extractPrefix(this.getModuleType()),
				this.getApplicationType());
	}

	public String getBillingBusinessService() {
		return String.format("%s.%s.%s", PSConstants.ESTATE_SERVICE, this.getBranchType(), this.getApplicationType());
	}

	private String extractPrefix(String inputString) {
		String outputString = "";

		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);
			outputString += Character.isUpperCase(c) ? c : "";
		}
		return outputString;
	}

	/**
	 * Documents uploaded for this application.
	 */
	@JsonProperty("applicationDocuments")
	private List<Document> applicationDocuments;

	public Application addApplicationDocumentsItem(Document applicationDocumentItem) {
		if (this.applicationDocuments == null) {
			this.applicationDocuments = new ArrayList<>();
		}
		for (Document ownerDocument : applicationDocuments) {
			if (ownerDocument.getId().equalsIgnoreCase(applicationDocumentItem.getId())) {
				return this;
			}
		}
		this.applicationDocuments.add(applicationDocumentItem);
		return this;
	}

	@JsonProperty("calculation")
	Calculation calculation;

	@JsonProperty("createdBy")
	private User createdBy;
}
