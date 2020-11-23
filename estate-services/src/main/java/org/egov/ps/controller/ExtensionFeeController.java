package org.egov.ps.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.ExtensionFee;
import org.egov.ps.service.ExtensionFeeService;
import org.egov.ps.util.ResponseInfoFactory;
import org.egov.ps.web.contracts.AccountStatementRequest;
import org.egov.ps.web.contracts.ExtensionFeeRequest;
import org.egov.ps.web.contracts.ExtensionFeeResponse;
import org.egov.ps.web.contracts.ExtensionFeeStatementResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/extension-fee")
public class ExtensionFeeController {

	@Autowired
	ExtensionFeeService extensionFeeService;

	@Autowired
	ResponseInfoFactory responseInfoFactory;

	@PostMapping("/_create")
	public ResponseEntity<ExtensionFeeResponse> createExtensionFee(
			@RequestBody ExtensionFeeRequest extensionFeeRequest) {
		List<ExtensionFee> extensionFees = extensionFeeService.createExtensionFee(extensionFeeRequest);
		ResponseInfo resInfo = responseInfoFactory
				.createResponseInfoFromRequestInfo(extensionFeeRequest.getRequestInfo(), true);
		ExtensionFeeResponse response = ExtensionFeeResponse.builder().extensionFees(extensionFees)
				.responseInfo(resInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_statement")
	public ResponseEntity<ExtensionFeeStatementResponse> penaltyStatement(
			@Valid @RequestBody AccountStatementRequest statementRequest) {
		ExtensionFeeStatementResponse extensionFeeStatementResponse = extensionFeeService
				.createExtensionFeeStatement(statementRequest);
		return new ResponseEntity<>(extensionFeeStatementResponse, HttpStatus.OK);
	}

}
