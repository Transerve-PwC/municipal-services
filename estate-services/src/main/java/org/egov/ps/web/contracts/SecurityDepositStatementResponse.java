package org.egov.ps.web.contracts;

import java.util.List;

import javax.validation.Valid;

import org.egov.ps.model.OfflinePaymentDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityDepositStatementResponse {

	@JsonProperty("PaymentDetails")
	@Valid
	private List<OfflinePaymentDetails> offlinePaymentDetails;

	@JsonProperty("SecurityDepositStatementSummary")
	private SecurityDepositStatementSummary securityDepositStatementSummary;

}
