package org.egov.ps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Premium Amount
 */
@ApiModel(description = "A Object holds the basic data for a Premium Amount")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-08-10T13:06:11.263+05:30")

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PremiumAmountConfigItems {

	/**
	 * Unique id of the payment
	 */
	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("paymentConfigId")
	private String paymentConfigId;

	@JsonProperty("premiumAmount")
	private String premiumAmount;

	@JsonProperty("premiumAmountDate")
	private String premiumAmountDate;
}
