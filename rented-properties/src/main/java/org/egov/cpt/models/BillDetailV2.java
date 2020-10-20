package org.egov.cpt.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BillDetail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillDetailV2 {

	@JsonProperty("id")
	@Size(max = 64)
	private String id;

	@JsonProperty("tenantId")
	@Size(max = 250)
	private String tenantId;

	@JsonProperty("demandId")
	@Size(max = 64)
	private String demandId;

	@JsonProperty("billId")
	@Size(max = 64)
	private String billId;

	@JsonProperty("expiryDate")
	private Long expiryDate;

	@JsonProperty("amount")
	private BigDecimal amount;

	@JsonProperty("fromPeriod")
	private Long fromPeriod;

	@JsonProperty("toPeriod")
	private Long toPeriod;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("billAccountDetails")
	@Valid
	private List<BillAccountDetailV2> billAccountDetails;

	public BillDetailV2 addBillAccountDetailsItem(BillAccountDetailV2 billAccountDetailsItem) {
		if (this.billAccountDetails == null) {
			this.billAccountDetails = new ArrayList<>();
		}
		if (!this.billAccountDetails.contains(billAccountDetailsItem))
			this.billAccountDetails.add(billAccountDetailsItem);
		return this;
	}
}
