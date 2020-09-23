package org.egov.integration.web.controller;

import javax.validation.Valid;

import org.egov.integration.model.RtiRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.service.RtiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rti/v1")
public class RtiController {

	private final RtiService service;
	
	@Autowired
	public RtiController(RtiService service) {
		this.service = service;
	}
	
	/**
	 * Get and confirms total requet for nodal,appealate,cpio
	 * @param requestInfoWrapper 
	 * @return Rti Response
	 */
	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> get(@Valid @RequestBody  RtiRequestInfoWrapper request) {		 
		return service.get(request);		
	}
	
	/**
	 * Get ministry mapping with user id
	 * @param requestInfoWrapper 
	 * @return Ministry Master Response
	 */
	@PostMapping(value = "/_getDepartment")
	public ResponseEntity<ResponseInfoWrapper> getDepartment(@Valid @RequestBody  RtiRequestInfoWrapper request)  {		 
		return service.getDepartment(request);		
	}
}
