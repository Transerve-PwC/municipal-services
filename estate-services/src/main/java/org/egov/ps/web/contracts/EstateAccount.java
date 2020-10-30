package org.egov.ps.web.contracts;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class EstateAccount {

	@JsonProperty("id")
	private String id;

	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

	@JsonProperty("remainingAmount")
	@Builder.Default
	private Double remainingAmount = 0D;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Builder.Default
	private Long remainingSince = 0L;
}

