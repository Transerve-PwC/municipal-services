package org.egov.ec.web.controllers;

import javax.validation.Valid;

import org.egov.ec.service.ReportService;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/report")
@Slf4j
public class ReportController {

	private final ReportService reportService;

	@Autowired
	public ReportController(ReportService reportService) {
		this.reportService = reportService;

	}

	/**
	* Get  Item Report  controller
	* @param RequestInfoWrapper object of reportData
	* @return HTTP status 200 on success
	*/
	
	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> getItem(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return reportService.getReport(requestInfoWrapper);
	}


	/**
	* Get  Dashboard  controller
	* @param RequestInfoWrapper object of DashboardDetails
	* @return HTTP status 200 on success
	*/
	@PostMapping(value = "/_getDashboard")
	public ResponseEntity<ResponseInfoWrapper> getDashboard(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		log.info("Entering: " + Thread.currentThread().getStackTrace()[1].getClassName()
				+ "."+ Thread.currentThread().getStackTrace()[1].getMethodName());
		return reportService.getDashboard(requestInfoWrapper);
	}
	
}
