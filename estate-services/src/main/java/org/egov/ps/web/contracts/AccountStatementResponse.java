package org.egov.ps.web.contracts;

import java.util.List;

import javax.validation.Valid;

import org.egov.ps.model.RentAccountStatement;

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

	@JsonProperty("RentAccountStatements")
	@Valid
	private List<RentAccountStatement> rentAccountStatements;

}
