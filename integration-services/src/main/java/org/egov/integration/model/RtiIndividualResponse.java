package org.egov.integration.model;

import org.json.simple.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RtiIndividualResponse {
	private String transactionNo;
	private String transactionNumber;
	private JSONArray records;
	private Boolean isConfirm;
}
