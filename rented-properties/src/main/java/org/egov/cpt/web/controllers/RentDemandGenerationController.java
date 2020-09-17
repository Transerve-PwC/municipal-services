package org.egov.cpt.web.controllers;

import javax.validation.Valid;

import org.egov.cpt.models.RentDemandCriteria;
import org.egov.cpt.models.RequestInfoWrapper;
import org.egov.cpt.service.RentDemandGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rent-demands")
public class RentDemandGenerationController {

	private RentDemandGenerationService demandGenerationService;

	@Autowired
	public RentDemandGenerationController(RentDemandGenerationService demandGenerationService) {
		this.demandGenerationService = demandGenerationService;
	}

	@PostMapping("/_create")
	public ResponseEntity<?> create(@Valid @ModelAttribute RentDemandCriteria demandCriteria,
			@Valid @RequestBody RequestInfoWrapper requestInfoWrapper) {
		demandGenerationService.createDemand(demandCriteria, requestInfoWrapper.getRequestInfo());
		log.info("Rend Demand generation request for year: " + demandCriteria.getDate());
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
