package org.egov.integration.model;

import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class PtMapping {
	private String uuid;
	
	@NotNull
	@JsonProperty("userId")
	private int userId;
	
	@NotNull
	@JsonProperty("propertyTaxId")
	private String propertyTaxId;
	
	@NotNull
	@JsonProperty("tenantId")
	private String tenantId;
	
	@JsonProperty("isActive")
	private Boolean isActive;
				
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
}
