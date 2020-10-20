package org.egov.cpt.models.calculation;

import java.math.BigDecimal;

import javax.validation.constraints.Size;

import org.egov.cpt.models.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A object holds a demand and collection values for a tax head and period.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemandDetail {

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 64)
	@JsonProperty("demandId")
	private String demandId;

	@Size(max = 250)
	@JsonProperty("taxHeadMasterCode")
	private String taxHeadMasterCode;

	@Size(max = 12)
	@JsonProperty("taxAmount")
	private BigDecimal taxAmount;

	@Size(max = 12)
	@Default
	@JsonProperty("collectionAmount")
	private BigDecimal collectionAmount = BigDecimal.ZERO;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Size(max = 250)
	@JsonProperty("tenantId")
	private String tenantId;

}
