package org.egov.ps.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.service.PropertyViolationService;
import org.egov.ps.util.ResponseInfoFactory;
import org.egov.ps.web.contracts.AccountStatementRequest;
import org.egov.ps.web.contracts.PenaltyStatementResponse;
import org.egov.ps.web.contracts.PropertyOfflinePaymentResponse;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.egov.ps.web.contracts.PropertyPenaltyResponse;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/violation")
public class PropertyViolationController {

	@Autowired
	PropertyViolationService propertyViolationService;

	@Autowired
	ResponseInfoFactory responseInfoFactory;

	@PostMapping("/_penalty")
	public ResponseEntity<PropertyPenaltyResponse> addPenalty(
			@RequestBody PropertyPenaltyRequest propertyPenaltyRequest) {
		List<PropertyPenalty> propertyPenalties = propertyViolationService.createPenalty(propertyPenaltyRequest);
		ResponseInfo resInfo = responseInfoFactory
				.createResponseInfoFromRequestInfo(propertyPenaltyRequest.getRequestInfo(), true);
		PropertyPenaltyResponse response = PropertyPenaltyResponse.builder().propertyPenalties(propertyPenalties)
				.responseInfo(resInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_penalty_payment")
	public ResponseEntity<PropertyOfflinePaymentResponse> penaltyPayment(
			@Valid @RequestBody PropertyRequest propertyRequest) {
		List<OfflinePaymentDetails> offlinePaymentDetails = propertyViolationService
				.processPropertyPenaltyPaymentRequest(propertyRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(),
				true);
		PropertyOfflinePaymentResponse response = PropertyOfflinePaymentResponse.builder()
				.offlinePaymentDetails(offlinePaymentDetails).responseInfo(resInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_penalty_statement")
	public ResponseEntity<PenaltyStatementResponse> penaltyStatement(
			@Valid @RequestBody AccountStatementRequest accountStatementRequest) {
		PenaltyStatementResponse penaltyStatementResponse = propertyViolationService
				.createPenaltyStatement(accountStatementRequest);
		return new ResponseEntity<>(penaltyStatementResponse, HttpStatus.OK);
	}

}
