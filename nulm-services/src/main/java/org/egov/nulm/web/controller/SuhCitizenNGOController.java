package org.egov.nulm.web.controller;

import javax.validation.Valid;

import org.egov.nulm.model.NulmSuhCitizenNGORequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.service.SuhCitizenNGOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/suh/citizen")
public class SuhCitizenNGOController {

	private final SuhCitizenNGOService service;

	@Autowired
	public SuhCitizenNGOController(SuhCitizenNGOService service) {
		this.service = service;
	}

	@PostMapping(value = "/_create")
	public ResponseEntity<ResponseInfoWrapper> createSuhApplication(
			@Valid @RequestBody NulmSuhCitizenNGORequest request) {
		return service.createSuhApplication(request);
	}

	@PostMapping(value = "/_update")
	public ResponseEntity<ResponseInfoWrapper> updateSuhApplication(
			@Valid @RequestBody NulmSuhCitizenNGORequest request) {
		return service.updateSuhApplication(request);
	}

	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> getSuhApplication(@RequestBody NulmSuhCitizenNGORequest request) {
		return service.getSuhApplication(request);
	}
}
