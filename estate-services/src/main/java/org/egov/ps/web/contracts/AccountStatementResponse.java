package org.egov.ps.web.contracts;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountStatementResponse {

	@JsonProperty("EstateAccountStatement")
	@Valid
	private List<EstateAccountStatement> estateAccountStatements;

	@JsonProperty("ManiMajraAccountStatement")
	@Valid
	private List<ManiMajraAccountStatement> mmAccountStatements;

}
