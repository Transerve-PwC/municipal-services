package org.egov.ps.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Size;

import org.egov.common.contract.request.User;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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
		return String.format("%s_%s.%s", PSConstants.ESTATE_SERVICE, camelToSnake(this.getBranchType()),
				camelToSnake(this.getApplicationType()));
	}

	/**
	 * Convert camel case string to snake case string and capitalise string.
	 */
	public static String camelToSnake(String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		str = str.replaceAll(regex, replacement).toUpperCase();
		return str;
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
	@Builder.Default
	private List<Document> applicationDocuments = new ArrayList<Document>();

	public List<Document> getApplicationDocuments() {
		if (CollectionUtils.isEmpty(this.applicationDocuments)) {
			return Collections.emptyList();
		}
		return this.applicationDocuments.stream()
				.filter(doc -> !doc.getDocumentType().startsWith(PSConstants.ES_WF_DOCS)).collect(Collectors.toList());
	}

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

	@JsonProperty(value = "wfDocuments", access = JsonProperty.Access.READ_ONLY)
	private List<Document> wfDocuments;

	public List<Document> getWfDocuments() {
		if (CollectionUtils.isEmpty(this.applicationDocuments)) {
			return Collections.emptyList();
		}
		return this.applicationDocuments.stream()
				.filter(doc -> doc.getDocumentType().startsWith(PSConstants.ES_WF_DOCS)).collect(Collectors.toList());
	}

	public List<Document> getAllDocuments() {

		return this.applicationDocuments;
	}

	@JsonProperty("calculation")
	Calculation calculation;

	@JsonProperty("createdBy")
	private User createdBy;
	
	@JsonProperty("totalDue")
	private BigDecimal totalDue;
	
	@JsonProperty("payer")
	private User payer;
	
	@JsonProperty("recieptNumber")
	private String recieptNumber;
	
	@JsonIgnore
	public String getMDMSModuleName() {
		return String.format("%s_%s_%s", this.getBranchType(), this.getModuleType(), this.getApplicationType());
	}
	
	/**
	 * Amount to be paid
	 */
	@JsonProperty("paymentAmount")
	private BigDecimal paymentAmount;

	@JsonProperty("gst")
	private BigDecimal gst;
	
	@JsonProperty("bankName")
	@Size(max = 256, message = "bank name must be between 0 and 256 characters in length")
	private String bankName;
	
	@JsonProperty("transactionId")
	@Size(max = 256, message = "transaction id must be between 0 and 256 characters in length")
	private String transactionId;
	
	@JsonProperty("paymentType")
	private String paymentType;
	
	@JsonProperty("dateOfPayment")
	private Long dateOfPayment;
	
	@JsonProperty("newsPaperAdvertisementDate")
	private Long newsPaperAdvertisementDate;
	
	@JsonProperty("applicationSubmissionDate")
	private Long applicationSubmissionDate;
	
	@JsonProperty("bookNumber")
	private String bookNumber;
	
	@JsonProperty("pageNumber")
	private String pageNumber;
	
	@JsonProperty("volumeNumber")
	private String volumeNumber;
	
}
