package org.egov.integration.model;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PtRequestInfoWrapper {
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("uid")
	private String uid;

	@JsonProperty("otp")
	private String otp;

	@JsonProperty("mobileNo")
	private String mobileNo;

	@JsonProperty("tokenId")
	private String tokenId;

	@JsonProperty("houseNo")
	private String houseNo;

	@JsonProperty("sectorId")
	private String sectorId;

}
