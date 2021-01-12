package org.egov.waterconnection.controller;

import java.util.List;

import javax.validation.Valid;

import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.model.BillGenerationFile;
import org.egov.waterconnection.model.BillGenerationRequest;
import org.egov.waterconnection.model.BillGenerationResponse;
import org.egov.waterconnection.model.RequestInfoWrapper;
import org.egov.waterconnection.model.SearchCriteria;
import org.egov.waterconnection.service.BillGenerationService;
import org.egov.waterconnection.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billGeneration")
public class BillGenerationController {

	@Autowired
	private  BillGenerationService billingService;

	@Autowired
	private  ResponseInfoFactory responseInfoFactory;
	
	@RequestMapping(value = "/_saveBilling", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<BillGenerationResponse> saveBillingData(
			@Valid @RequestBody BillGenerationRequest billGenerationRequest) {
		List<BillGeneration> billGeneration = billingService.saveBillingData(billGenerationRequest);
		BillGenerationResponse response = BillGenerationResponse.builder().billGeneration(billGeneration)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(billGenerationRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@RequestMapping(value = "/_generateBillFile", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<BillGenerationResponse> generateBillFile(
			@Valid @RequestBody BillGenerationRequest billGenerationRequest) {
		List<BillGenerationFile> billGeneration = billingService.generateBillFile(billGenerationRequest);
		BillGenerationResponse response = BillGenerationResponse.builder().billGenerationFile(billGeneration)
				.responseInfo(responseInfoFactory
						.createResponseInfoFromRequestInfo(billGenerationRequest.getRequestInfo(), true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@RequestMapping(value = "/_getBillingFiles", method = RequestMethod.POST)
	public ResponseEntity<BillGenerationResponse> getBillingFiles(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		List<BillGenerationFile> billingFileList = billingService.getBillingFiles();
		BillGenerationResponse response = BillGenerationResponse.builder().billGenerationFile(billingFileList)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(),
						true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	

	@RequestMapping(value = "/_getBillData", method = RequestMethod.POST)
	public ResponseEntity<BillGenerationResponse> getBillData(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @RequestBody BillGenerationRequest billGenerationRequest) {
		List<BillGeneration> billingData = billingService.getBillData(billGenerationRequest);
		BillGenerationResponse response = BillGenerationResponse.builder().billGeneration(billingData)
				.responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(),
						true))
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
