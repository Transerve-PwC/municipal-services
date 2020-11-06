package org.egov.ps.controller;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.service.PropertyViolationService;
import org.egov.ps.util.ResponseInfoFactory;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.egov.ps.web.contracts.PropertyPenaltyResponse;
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
	public ResponseEntity<PropertyPenaltyResponse> penalty(
			 @RequestBody PropertyPenaltyRequest propertyPenaltyRequest) {
		List<PropertyPenalty> propertyPenalties = propertyViolationService.penalty(propertyPenaltyRequest);
		ResponseInfo resInfo = responseInfoFactory
				.createResponseInfoFromRequestInfo(propertyPenaltyRequest.getRequestInfo(), true);
		PropertyPenaltyResponse response = PropertyPenaltyResponse.builder().propertyPenalties(propertyPenalties)
				.responseInfo(resInfo).build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
