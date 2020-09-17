package org.egov.hc.model;

import javax.validation.constraints.Size;

import org.egov.common.contract.request.RequestInfo;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestData {

	@JsonProperty("houseNoAndStreetName")
	public String houseNoAndStreetName;
	
	@Size(max=257)
	@JsonProperty("latitude")
	private String latitude;
	
	@Size(max=257)
	@JsonProperty("longitude")
	private String longitude;
	
	@Size(max=257)
	@JsonProperty("locality")
	private String locality;
	
	@Size(max=256)
	@JsonProperty("mohalla")
	private String mohalla;
	
	@Size(max=259)
	@JsonProperty("landmark")
	private String landmark;
	
	@Size(max=257)
	@JsonProperty("address")
	private String address;
	
	@Size(max=255)
	@JsonProperty("ownerName")
	private String ownerName = null;
	
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("serviceType")
	private String serviceType;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("serviceRequestStatus")
	private String serviceRequestStatus;

	@JsonProperty("service_request_id")
	private String service_request_id;
	
	@JsonProperty("fromDate")
	private Long fromDate = null;

	@JsonProperty("toDate")
	private Long toDate = null;
	
	@JsonProperty("ownerContactNumber")
	private String ownerContactNumber;

	@JsonProperty("streetName")
	private String streetName;

	
	@JsonProperty("actions")
	private String actions;
	
	@Size(max=128)
	@JsonProperty("serviceRequestSubtype")
	private String serviceRequestSubtype;
	
	@Size(max=255)
	@JsonProperty("email")
	private String email = null;
	
	@JsonProperty("treeCount")
	private Long treeCount;
	
	
	@JsonProperty("dataPayload")
	private JSONObject dataPayload;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
	 @JsonProperty("offset")
	    private Integer offset;

	    @JsonProperty("limit")
	    private Integer limit;

}
