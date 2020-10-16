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

	@Size(max = 64)
	@JsonProperty("id")
	private String id;

	@Size(max = 256)
	@JsonProperty("referenceId")
	private String referenceId;

	@Size(max = 64)
	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("active")
	private Boolean active;

	@Size(max = 64)
	@JsonProperty("documentType")
	private String documentType;

	@Size(max = 64)
	@JsonProperty("fileStoreId")
	private String fileStoreId;

	@Size(max = 64)
	@JsonProperty("documentUid")
	private String documentUid;

	@Size(max = 64)
	@JsonProperty("propertyId")
	private String propertyId;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}
