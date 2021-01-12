package org.egov.wscalculation.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.wscalculation.model.BillGeneration;
import org.egov.wscalculation.model.BillGenerationRequest;
import org.egov.wscalculation.model.BillGenerationResponse;
import org.egov.wscalculation.model.Calculation;
import org.egov.wscalculation.service.BillingService;
import org.egov.wscalculation.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/billing")
public class BillingController {

	@Autowired
	private BillingService service;

	@Autowired
	private  ResponseInfoFactory responseInfoFactory;
	
	
	@RequestMapping(value = "/_getBillingEstimation", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<BillGenerationResponse> getBillingEstimation(
			@Valid @RequestBody BillGenerationRequest billGenerationRequest) {
		List<BillGeneration> billGeneration = service.getBillingEstimation(billGenerationRequest);
		BillGenerationResponse response = BillGenerationResponse.builder().billGeneration(billGeneration)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(billGenerationRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/*
	 * @RequestMapping(value = "/_scheduleDemandForSurcharge", method =
	 * RequestMethod.POST, produces = "application/json") public
	 * ResponseEntity<BillGenerationResponse> scheduleDemandForSurcharge(
	 * 
	 * @Valid @RequestBody BillGenerationRequest billGenerationRequest) {
	 * List<BillGeneration> billGeneration =
	 * service.scheduleDemandForSurcharge(billGenerationRequest);
	 * BillGenerationResponse response =
	 * BillGenerationResponse.builder().billGeneration(billGeneration)
	 * .responseInfo(responseInfoFactory
	 * .createResponseInfoFromRequestInfo(billGenerationRequest.getRequestInfo(),
	 * true)) .build(); return new ResponseEntity<>(response, HttpStatus.OK); }
	 */
	
	
}
