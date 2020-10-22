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
 * A Object holds the basic data for a Notice Generation
 */
@ApiModel(description = "A Object holds the basic data for a Notice Generation")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class PropertyImagesDocument {

	@Size(max = 64, message = "Id must be between 0 to 64 characters in length")
	@JsonProperty("id")
	private String id;

	@JsonProperty("applicationId")
	private String applicationId;

	@Size(max = 64, message = "Tenant id must be between 0 to 64 characters in length")
	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("active")
	private Boolean active;

	@Size(max = 64, message = "Document type must be between 0 to 64 characters in length")
	@JsonProperty("documentType")
	private String documentType = null;

	@Size(max = 64, message = "File store id must be between 0 to 64 characters in length")
	@JsonProperty("fileStoreId")
	private String fileStoreId = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

}
