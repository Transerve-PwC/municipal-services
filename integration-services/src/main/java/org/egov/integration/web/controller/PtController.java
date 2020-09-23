package org.egov.integration.web.controller;

import javax.validation.Valid;

import org.egov.integration.model.PtRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.service.PtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pt/v1")
public class PtController {
	private final PtService service;
	
	@Autowired
	public PtController(PtService service) {
		this.service = service;
	}
	
	/**
	 * Get and confirms requet for Property Tax
	 * @param requestInfoWrapper 
	 * @return PT Response
	 */
	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> get(@Valid @RequestBody  PtRequestInfoWrapper request) {		 
		return service.getPropertyTaxDetails(request.getUid());	
	}
}
