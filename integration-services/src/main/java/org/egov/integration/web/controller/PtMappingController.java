package org.egov.integration.web.controller;

import javax.validation.Valid;

import org.egov.integration.model.PtMappingRequest;
import org.egov.integration.model.PtMappingRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.service.PtMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pt-mapping/v1")
public class PtMappingController {
	private final PtMappingService service;
	
	@Autowired
	public PtMappingController(PtMappingService service) {
		this.service = service;
	}
	
	@PostMapping(value = "/_save")
	public ResponseEntity<ResponseInfoWrapper> savePtMapping(@Valid @RequestBody PtMappingRequest request) {
		return service.savePtMapping(request);
	}
	
	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> get(@Valid @RequestBody  PtMappingRequestInfoWrapper request) {		 
		return service.getPropertyTaxList(request);
	}
}
