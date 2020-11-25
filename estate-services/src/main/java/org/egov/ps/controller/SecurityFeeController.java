package org.egov.ps.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.service.SecurityFeeService;
import org.egov.ps.util.ResponseInfoFactory;
import org.egov.ps.web.contracts.PropertyOfflinePaymentResponse;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security-fee")
public class SecurityFeeController {
	
	@Autowired
	SecurityFeeService securityFeeService;

	@Autowired
	ResponseInfoFactory responseInfoFactory;

	@PostMapping("/_payment")
	public ResponseEntity<PropertyOfflinePaymentResponse> extensionFeePayment(
			@Valid @RequestBody PropertyRequest propertyRequest) {
		List<OfflinePaymentDetails> offlinePaymentDetails = securityFeeService
				.processSecurityFeePaymentRequest(propertyRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(propertyRequest.getRequestInfo(),
				true);
		PropertyOfflinePaymentResponse response = PropertyOfflinePaymentResponse.builder()
				.offlinePaymentDetails(offlinePaymentDetails).responseInfo(resInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
