package org.egov.ps.controller;

import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.Valid;

import org.egov.ps.model.EstateDemandCriteria;
import org.egov.ps.service.EstateDemandGenerationService;
import org.egov.ps.web.contracts.DemandGenerationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/estate-demands")
public class EstateDemandGenerationController {

	private EstateDemandGenerationService demandGenerationService;

	@Autowired
	public EstateDemandGenerationController(EstateDemandGenerationService demandGenerationService) {
		this.demandGenerationService = demandGenerationService;
	}
	
	@PostMapping("/_create")
	public ResponseEntity<?> create(@Valid @ModelAttribute EstateDemandCriteria demandCriteria) {
		AtomicInteger count=demandGenerationService.createDemand(demandCriteria);
		log.info(String.format("%s demands generated",count));
		DemandGenerationResponse response = DemandGenerationResponse.builder().generatedCount(count).build();
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
}
