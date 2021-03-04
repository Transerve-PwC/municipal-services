package org.egov.integration.web.controller;

import javax.validation.Valid;

import org.egov.integration.model.EOfficeMapRequestInfoWrapper;
import org.egov.integration.model.EOfficeRequestInfoWrapper;
import org.egov.integration.model.RequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.service.EOfficeService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/eoffice/v1")
public class EOfficeController {


	private final EOfficeService service;
	
	@Autowired
	public EOfficeController(EOfficeService service) {
		this.service = service;
	} 
	
	
	@PostMapping(value = "/_get")
	public ResponseEntity<ResponseInfoWrapper> get( @RequestBody  EOfficeRequestInfoWrapper request) throws JSONException {		 
		return service.get(request);		
	}
	
	@PostMapping(value = "/_jobscheduler")
	public ResponseEntity<ResponseInfoWrapper> process( @Valid @RequestBody RequestInfoWrapper request) throws JSONException {		
		return service.process(request);		
	}
	
	@PostMapping(value = "/_getPostDetailsId")
	public ResponseEntity<ResponseInfoWrapper> getPostDetailsId( @RequestBody  EOfficeMapRequestInfoWrapper request) throws JSONException {				
		return service.getPostDetailsId(request);		
	}
}
