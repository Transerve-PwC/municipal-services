package org.egov.ps.model;

import java.math.BigDecimal;

import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class PropertyPenalty {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("propertyId")
	private String propertyId;

	@JsonProperty("branchType")
	private String branchType;

	@JsonProperty("penaltyAmount")
	private BigDecimal penaltyAmount;

	@JsonProperty("violationType")
	private String violationType;

	@JsonProperty("isPaid")
	private Boolean isPaid;

	@JsonProperty("totalPenaltyDue")
	private BigDecimal totalPenaltyDue;

	@JsonProperty("penaltyNumber")
	private String penaltyNumber;

	@JsonProperty("penaltyBusinessService")
	private String penaltyBusinessService;

	@JsonProperty("calculation")
	Calculation calculation;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	public String getPenaltyBusinessService() {
		return String.format("%s_%s.%s", PSConstants.ESTATE_SERVICE, camelToSnake(this.getBranchType()),
				PSConstants.PROPERTY_VIOLATION);
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
}
