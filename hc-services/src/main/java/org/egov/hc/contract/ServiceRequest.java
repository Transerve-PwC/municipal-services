package org.egov.hc.contract;

import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.hc.model.ActionInfo;
import org.egov.hc.model.ServiceRequestData;



import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object to fetch the report data
 */
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRequest  implements Cloneable  {
	

	public ServiceRequest clone() throws
    CloneNotSupportedException 
{ 
return (ServiceRequest) super.clone();
} 

  @JsonProperty("RequestInfo")
  private RequestInfo requestInfo = null;

  @JsonProperty("services")
  @Valid
  private List<ServiceRequestData> services = new LinkedList<ServiceRequestData>();

  @JsonProperty("actionInfo")
  @Valid
  private List<ActionInfo> actionInfo = new LinkedList<ActionInfo>();
  
  @JsonProperty("RequestBody")
  //@Valid
  private Object requestBody;
  
  @JsonProperty("auditDetails")
	AuditDetails auditDetails;
  
  @JsonProperty("status")
	String status;
  

  
  @JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("ResponseBody")
	private Object responseBody;

	@JsonProperty("isEditState")
	private int isEditState;
	

}

