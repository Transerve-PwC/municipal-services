package org.egov.cpt.models;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.cpt.models.calculation.Calculation;
import org.egov.cpt.util.PropertySerializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Owner {

	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	@JsonSerialize(using = PropertySerializer.class)
	private Property property;

	@Size(max = 256)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(min = 3, max = 256)
	@JsonProperty("allotmenNumber")
	private String allotmenNumber;

	@Valid
	@JsonProperty("ownerDetails")
	private OwnerDetails ownerDetails;

	@Builder.Default
	@JsonProperty("assignee")
	private List<String> assignee = null;

	@Size(max = 128)
	@JsonProperty("comment")
	private String comment;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	/**
	 * This will be the first allotee. During property master, this becomes true.
	 */
	@Builder.Default
	@JsonProperty("isPrimaryOwner")
	private Boolean isPrimaryOwner = false;

	/**
	 * This represents currently active owner. During property master, this should
	 * be true.
	 */
	@JsonProperty("activeState")
	private Boolean activeState;

	/**
	 * This will indicate the application status.
	 */
	@Size(max = 256)
	@JsonProperty("applicationState")
	private String applicationState;

	@Size(max = 256)
	@JsonProperty("applicationAction")
	private String applicationAction;

	@Valid
	@JsonProperty("calculation")
	Calculation calculation;

	@Size(max = 256)
	@JsonProperty("billingBusinessService")
	private String billingBusinessService;

	/**
	 * 
	 * @return
	 */
	public String getBillingBusinessService() {
		if (this.property == null) {
			return "";
		}
		return String.format("RENTED_PROPERTIES_%s.OWNERSHIP_TRANSFER", this.property.getColony());
	}

	@Valid
	@JsonProperty("wfDocuments")
	private List<Document> wfdocuments;
}
