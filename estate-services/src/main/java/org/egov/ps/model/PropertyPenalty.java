package org.egov.ps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PaymentStatusEnum;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Property Penalty
 */
@ApiModel(description = "A Object holds the basic data for a Property Penalty")

@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-11-05T17:05:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PropertyPenalty implements Comparable<PropertyPenalty> {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("property")
	private Property property;

	@JsonProperty("branchType")
	private String branchType;

	@JsonProperty("generationDate")
	private Long generationDate;

	@JsonProperty("violationType")
	private String violationType;

	@JsonProperty("penaltyAmount")
	private Double penaltyAmount;

	@JsonProperty("remainingPenaltyDue")
	private Double remainingPenaltyDue;

	@JsonProperty("isPaid")
	private Boolean isPaid;

	@JsonProperty("status")
	@Builder.Default
	private PaymentStatusEnum status = PaymentStatusEnum.UNPAID;

	public boolean isPaid() {
		return this.status == PaymentStatusEnum.PAID;
	}

	public boolean isUnPaid() {
		return !this.isPaid();
	}

	@JsonProperty("penaltyBusinessService")
	private String penaltyBusinessService;

	@JsonProperty("calculation")
	Calculation calculation;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Override
	public int compareTo(PropertyPenalty other) {
		return this.getGenerationDate().compareTo(other.getGenerationDate());
	}

}
