package org.egov.hc.workflow;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import lombok.*;

import org.egov.hc.model.AuditDetails;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

/**
 * A Object holds the basic data for a Horticulture
 */
@ApiModel(description = "A Object holds the basic data for a Horticulture")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Documenttest   {

        @Size(max=64)
        @JsonProperty("id")
        private String id;

        @JsonProperty("active")
        private Boolean active;

        @Size(max=64)
        @JsonProperty("tenantId")
        private String tenantId = null;

        @Size(max=64)
        @JsonProperty("documentType")
        private String documentType = null;

        @Size(max=64)
        @JsonProperty("fileStoreId")
        private String fileStoreId = null;

        @Size(max=64)
        @JsonProperty("documentUid")
        private String documentUid;

        
  	  @JsonProperty("auditDetails")
  	  private AuditDetails auditDetails;


}

