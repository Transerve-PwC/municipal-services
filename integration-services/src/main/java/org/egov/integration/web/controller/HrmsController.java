package org.egov.integration.web.controller;

import javax.validation.Valid;

import org.egov.integration.model.HrmsRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.model.RtiRequestInfoWrapper;
import org.egov.integration.service.HrmsService;
import org.egov.integration.service.PayslipClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hrms/v1")
public class HrmsController {

	@Autowired
	private HrmsService service;


	@Autowired
	private PayslipClient client;
	/**
	 * Get statelist from ehrms
	 * 
	 * @param requestInfoWrapper
	 * @return ResponseInfoWrapper
	 */
	@PostMapping(value = "/_getStatelist")
	public ResponseEntity<ResponseInfoWrapper> getstateList(@Valid @RequestBody HrmsRequestInfoWrapper request) {
		return service.getstateList(request);
	}

	/**
	 * Get employee backlogs from ehrms
	 * 
	 * @param requestInfoWrapper
	 * @return ResponseInfoWrapper
	 */
	@PostMapping(value = "/_getBacklogs")
	public ResponseEntity<ResponseInfoWrapper> getBacklogs(@Valid @RequestBody HrmsRequestInfoWrapper request) {
		return service.getBacklogs(request);
	}

	/**
	 * Get employee notification details from ehrms portal
	 * 
	 * @param requestInfoWrapper
	 * @return ResponseInfoWrapper
	 */
	@PostMapping(value = "/_getNotification")
	public ResponseEntity<ResponseInfoWrapper> getNotification(@Valid @RequestBody HrmsRequestInfoWrapper request) {
		return service.getNotification(request);
	}
	

	/**
	 * Get employee payslip details from ehrms portal
	 * 
	 * @param requestInfoWrapper
	 * @return ResponseInfoWrapper
	 */
	@PostMapping(value = "/_getpayslip")
	public ResponseEntity<ResponseInfoWrapper> getpayslip(@Valid @RequestBody HrmsRequestInfoWrapper request) {
		return client.fetchPayslip(request);
	}

}
