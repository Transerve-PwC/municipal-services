package org.egov.cpt.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.egov.cpt.util.PropertySerializer;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Property
 */
@ApiModel(description = "A Object holds the basic data for a Notice Generation")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PropertyImages {

	@Size(max = 256, message = "id must be between 0 and 256 characters in length")
	@JsonProperty("id")
	private String id;

	@JsonSerialize(using = PropertySerializer.class)
	private Property property;

	@Size(max = 256, message = "tenant id must be between 0 and 256 characters in length")
	@JsonProperty("tenantId")
	private String tenantId;
	
	@Size(max = 64, message = "application number must be between 0 and 64 characters in length")
	@JsonProperty("applicationNumber")
	private String applicationNumber;
	
	@Size(max = 1000, message = "description must be between 0 and 1000 characters in length")
	@JsonProperty("description")
	private String description;
	
	@Size(max = 256, message = "captured by must be between 0 and 256 characters in length")
	@JsonProperty("capturedBy")
	private String capturedBy;
	
	@Valid
	@JsonProperty("applicationDocuments")
	private List<Document> applicationDocuments = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;


	public PropertyImages addApplicationDocumentsItem(Document applicationDocumentsItem) {
		if (this.applicationDocuments == null) {
			this.applicationDocuments = new ArrayList<>();
		}
		for (Document applicationDocument : applicationDocuments) {
			if (applicationDocument.getId().equalsIgnoreCase(applicationDocumentsItem.getId())) {
				return this;
			}
		}
		this.applicationDocuments.add(applicationDocumentsItem);
		return this;
		
	}

}
