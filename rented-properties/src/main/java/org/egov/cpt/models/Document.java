package org.egov.cpt.models;

import javax.validation.constraints.Size;

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
 * A Object holds the basic data for a Trade License
 */
@ApiModel(description = "A Object holds the basic data for a Trade License")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Document {

	@Size(max = 64, message = "Id must be between 0 and 64 characters in length")
	@JsonProperty("id")
	private String id;

	@Size(max = 256, message = "Reference id must be between 0 and 256 characters in length")
	@JsonProperty("referenceId")
	private String referenceId;

	@Size(max = 64, message = "Tenant id must be between 0 and 64 characters in length")
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("active")
	private Boolean active;

	@Size(max = 64, message = "Documant type must be between 0 and 64 characters in length")
	@JsonProperty("documentType")
	private String documentType;

	@Size(max = 64, message = "File store id must be between 0 and 64 characters in length")
	@JsonProperty("fileStoreId")
	private String fileStoreId;

	@Size(max = 64, message = "Document uid must be between 0 and 64 characters in length")
	@JsonProperty("documentUid")
	private String documentUid;

	@Size(max = 64, message = "Property id must be between 0 and 64 characters in length")
	@JsonProperty("propertyId")
	private String propertyId;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
