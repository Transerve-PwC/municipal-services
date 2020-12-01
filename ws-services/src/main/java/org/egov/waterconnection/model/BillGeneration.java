package org.egov.waterconnection.model;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class BillGeneration {

	@JsonProperty("billGenerationId")
	private String billGenerationId = null;

	@JsonProperty("document")
	@Valid
	private Document document = null;
	
	@JsonProperty("ccCode")
	private String ccCode = null;

	@JsonProperty("divSdiv")
	private String divSdiv = null;

	@JsonProperty("consumerCode")
	private String consumerCode = null;
	
	@JsonProperty("billCycle")
	private String billCycle = null;

	@JsonProperty("billGroup")
	private String billGroup = null;

	@JsonProperty("subGroup")
	private String subGroup = null;

	@JsonProperty("billType")
	private String billType = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("address")
	private String address = null;

	@JsonProperty("add1")
	private String add1 = null;

	@JsonProperty("add2")
	private String add2 = null;
	
	@JsonProperty("add3")
	private String add3 = null;
	
	@JsonProperty("add4")
	private String add4 = null;
	
	@JsonProperty("add5")
	private String add5 = null;
	
	@JsonProperty("cessCharge")
	private String cessCharge = null;
	
	@JsonProperty("netAmount")
	private String netAmount = null;
	
	@JsonProperty("grossAmount")
	private String grossAmount = null;

	@JsonProperty("surcharge")
	private String surcharge = null;
	
	@JsonProperty("totalNetAmount")
	private String totalNetAmount = null;
	
	@JsonProperty("totalSurcharge")
	private String totalSurcharge = null;
	
	@JsonProperty("totalGrossAmount")
	private String totalGrossAmount = null;
	
	@JsonProperty("fixChargeCode")
	private String fixChargeCode = null;
	
	@JsonProperty("fixCharge")
	private String fixCharge = null;
	
	@JsonProperty("dueDateCash")
	private String dueDateCash = null;
	

	@JsonProperty("dueDateCheque")
	private String dueDateCheque = null;

	@JsonProperty("status")
	private String status = null;

	@JsonProperty("billId")
	private String billId = null;

	@JsonProperty("paymentId")
	private String paymentId = null;

	@JsonProperty("paymentStatus")
	private String paymentStatus = null;
	

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
}