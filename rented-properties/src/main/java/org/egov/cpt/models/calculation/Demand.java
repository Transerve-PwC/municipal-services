package org.egov.cpt.models.calculation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.common.contract.request.User;
import org.egov.cpt.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A Object which holds the basic info about the revenue assessment for which
 * the demand is generated like module name, consumercode, owner, etc.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Demand {

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 250)
	@JsonProperty("tenantId")
	private String tenantId;

	@Size(max = 250)
	@JsonProperty("consumerCode")
	private String consumerCode;

	@Size(max = 250)
	@JsonProperty("consumerType")
	private String consumerType;

	@Size(max = 250)
	@JsonProperty("businessService")
	private String businessService;

	@Valid
	@JsonProperty("payer")
	private User payer;

	@JsonProperty("taxPeriodFrom")
	private Long taxPeriodFrom;

	@JsonProperty("taxPeriodTo")
	private Long taxPeriodTo;

	@Builder.Default
	@JsonProperty("demandDetails")
	@Valid
	private List<DemandDetail> demandDetails = new ArrayList<>();

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@Size(max = 12)
	@Builder.Default
	@JsonProperty("minimumAmountPayable")
	private BigDecimal minimumAmountPayable = BigDecimal.ZERO;

	/**
	 * Gets or Sets status
	 */
	public enum StatusEnum {

		ACTIVE("ACTIVE"),

		CANCELLED("CANCELLED"),

		ADJUSTED("ADJUSTED");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static StatusEnum fromValue(String text) {
			for (StatusEnum b : StatusEnum.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@Size(max = 64)
	@JsonProperty("status")
	private StatusEnum status;

	public Demand addDemandDetailsItem(DemandDetail demandDetailsItem) {
		this.demandDetails.add(demandDetailsItem);
		return this;
	}

}