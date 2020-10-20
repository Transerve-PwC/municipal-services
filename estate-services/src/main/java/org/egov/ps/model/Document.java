package org.egov.ps.model;

import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object holds the basic data for a Documents
 */
@ApiModel(description = "A Object holds the basic data for a Documents")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-08-05T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Document {

	@JsonProperty("id")
	private String id;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("referenceId")
	private String referenceId;

	@JsonProperty("documentType")
	private String documentType;

	@JsonProperty("fileStoreId")
	private String fileStoreId;

	@JsonProperty("isActive")
	private Boolean isActive;

	@JsonProperty("propertyId")
	private String propertyId;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
