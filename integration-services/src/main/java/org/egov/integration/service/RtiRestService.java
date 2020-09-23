package org.egov.integration.service;

import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.RtiConfiguration;
import org.egov.integration.model.RtiIndividualResponse;
import org.egov.integration.model.RtiRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RtiRestService {
	@Autowired
	private RequestFactory requestFactory;

	private RtiConfiguration config;

	@Autowired
	public RtiRestService(RtiConfiguration config) {
		this.config = config;
	}	
	
	public ResponseEntity<RtiIndividualResponse> getCpioResponse(RtiRequest rti, String token) {
		try {		
			RestTemplate restTemplate = requestFactory.getRestTemplate();
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			header.add("Authorization", "Bearer " + token);
			ResponseEntity<RtiIndividualResponse> totalRequest = restTemplate.exchange(config.getCpioHost() + config.getCpioPath(),
					HttpMethod.GET, new HttpEntity<>(rti, header), RtiIndividualResponse.class);
			return totalRequest;	
		} catch (HttpClientErrorException e) {
			throw new CustomException(CommonConstants.RTI_CPIO_EXCEPTION_CODE, e.getResponseBodyAsString());
		}
	}
	
	public ResponseEntity<RtiIndividualResponse> getNodalResponse(RtiRequest rti, String token) {
		try {		
			RestTemplate restTemplate = requestFactory.getRestTemplate();
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			header.add("Authorization", "Bearer " + token);
			ResponseEntity<RtiIndividualResponse> totalRequest = restTemplate.exchange(config.getNodalHost() + config.getNodalPath(),
					HttpMethod.GET, new HttpEntity<>(rti, header), RtiIndividualResponse.class);
			return totalRequest;
		} catch (HttpClientErrorException e) {
			throw new CustomException(CommonConstants.RTI_NODAL_EXCEPTION_CODE, e.getResponseBodyAsString());
		}
	}
	
	public ResponseEntity<RtiIndividualResponse> geAppellateResponse(RtiRequest rti, String token) {
		try {	
			RestTemplate restTemplate = requestFactory.getRestTemplate();
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			header.add("Authorization", "Bearer " + token);
			
			ResponseEntity<RtiIndividualResponse> totalRequest = restTemplate.exchange(config.getAppellateHost() + config.getAppellatePath(),
					HttpMethod.GET, new HttpEntity<>(rti, header), RtiIndividualResponse.class);
			return totalRequest;
		} catch (HttpClientErrorException e) {
			throw new CustomException(CommonConstants.RTI_APPELLATE_EXCEPTION_CODE, e.getResponseBodyAsString());
		}
	}
	
	public void confirmTransaction(RtiIndividualResponse res, String token) {
		try {		
			RestTemplate restTemplate = requestFactory.getRestTemplate();
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			header.add("Authorization", "Bearer " + token);
			ResponseEntity<RtiIndividualResponse> totalRequest = restTemplate.exchange(config.getTransConfirmHost() + config.getTransConfirmPath(),
					HttpMethod.GET, new HttpEntity<>(res, header), RtiIndividualResponse.class);
			log.info("integration-services logs :: Executing confirmTransaction() = {}", totalRequest.toString());
		} catch (HttpClientErrorException e) {
			log.error("integration-services logs :: cpiores :: " + e.getResponseBodyAsString());
		}
	}	
}
