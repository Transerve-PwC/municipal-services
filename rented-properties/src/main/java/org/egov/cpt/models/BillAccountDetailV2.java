package org.egov.cpt.models;

import java.math.BigDecimal;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BillAccountDetail
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillAccountDetailV2   {
	
  @JsonProperty("id")
  @Size(max = 64)
  private String id;

  @JsonProperty("tenantId")
  @Size(max = 250)
  private String tenantId;

  @JsonProperty("billDetailId")
  @Size(max = 64)
  private String billDetailId;

  @JsonProperty("demandDetailId")
  @Size(max = 64)
  private String demandDetailId;

  @JsonProperty("order")
  private Integer order;

  @JsonProperty("amount")
  private BigDecimal amount;
  
  @JsonProperty("adjustedAmount")
  private BigDecimal adjustedAmount;

  @JsonProperty("taxHeadCode")
  @Size(max = 256)
  private String taxHeadCode;

  @JsonProperty("additionalDetails")
  private Object additionalDetails;

  @JsonProperty("auditDetails")
  private AuditDetails auditDetails;
}

