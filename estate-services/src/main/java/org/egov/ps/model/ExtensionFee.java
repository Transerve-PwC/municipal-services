package org.egov.ps.model;

import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PaymentStatusEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ExtensionFee implements Comparable<ExtensionFee> {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("property")
	private Property property;

	@JsonProperty("branchType")
	private String branchType;

	@JsonProperty("amount")
	private Double amount;

	@JsonProperty("remainingDue")
	private Double remainingDue;

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

	@JsonProperty("generationDate")
	private Long generationDate;

	@JsonProperty("businessService")
	private String businessService;

	@JsonProperty("calculation")
	Calculation calculation;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Override
	public int compareTo(ExtensionFee extensionFee) {
		return this.getGenerationDate().compareTo(extensionFee.getGenerationDate());
	}

}
