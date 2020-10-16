package org.egov.cpt.models;

import javax.validation.constraints.Size;

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
public class RentAccount {

	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	@Size(max = 256)
	@JsonProperty("propertyId")
	private String propertyId;

	@Size(max = 13)
	@JsonProperty("remainingAmount")
	@Builder.Default
	private Double remainingAmount = 0D;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	@Builder.Default
	private Long remainingSince = 0L;
}
