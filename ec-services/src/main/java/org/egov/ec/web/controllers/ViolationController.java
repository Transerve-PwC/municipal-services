package org.egov.ec.web.controllers;

import javax.validation.Valid;

import org.egov.ec.service.ViolationService;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/violation")
@Slf4j
public class ViolationController {

	private final ViolationService genearateChallanService;

	@Autowired
	public ViolationController(ViolationService genearateChallanService) {
		this.genearateChallanService = genearateChallanService;
	}

	/**
	* get  violationDetail  controller
	* 
	* @param RequestInfoWrapper Object of EcSearchCriteria
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> getChallan(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return genearateChallanService.getChallan(requestInfoWrapper);
	}

	/**
	* create  violation  controller
	* 
	* @param RequestInfoWrapper List of ViolationDetail
	* @param requestHeader for saving device information
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_create")
	public ResponseEntity<ResponseInfoWrapper> generateChallan(
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,@RequestHeader("User-Agent") String requestHeader) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return genearateChallanService.generateChallan(requestInfoWrapper,requestHeader);
	}

	/**
	* update  violation  controller
	* 
	* @param RequestInfoWrapper object of ViolationDetail
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_update")
	public ResponseEntity<ResponseInfoWrapper> updateChallan(
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return genearateChallanService.updateChallan(requestInfoWrapper);
	}

	/**
	* create  violationPayment  controller
	* 
	* @param RequestInfoWrapper List of ViolationDetail
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_addPayment")
	public ResponseEntity<ResponseInfoWrapper> addPayment(
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		return genearateChallanService.addPayment(requestInfoWrapper);
	}

	/**
	* Send Email Notification
	* 
	* @param RequestInfoWrapper NotificationDetail Object
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_notify")
	public ResponseEntity<ResponseInfoWrapper> sendNotification(
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
					return genearateChallanService.sendNotification(requestInfoWrapper);
			}
	
	/**
	* get  violationDetail  controller
	* 
	* @param RequestInfoWrapper Object of EcSearchCriteria
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_search")
	public ResponseEntity<ResponseInfoWrapper> getSearchChallan(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return genearateChallanService.getSearchChallan(requestInfoWrapper);
	}

	@PostMapping(value = "/_sendMessage")
	public ResponseEntity<ResponseInfoWrapper> sendMessage(
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,@RequestHeader("User-Agent") String requestHeader) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return genearateChallanService.sendMessage(requestInfoWrapper,requestHeader);
	}

}
