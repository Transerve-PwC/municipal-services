package org.egov.ps.repository;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ServiceRequestRepository {

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Fetches results from a REST service using the uri and object
	 * 
	 * @param requestInfo
	 * @param serviceReqSearchCriteria
	 * @return Object
	 * @author vishal
	 */
	public Object fetchResult(StringBuilder uri, Object request) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		Object response = null;
		try {
			response = restTemplate.postForObject(uri.toString(), request, Map.class);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from external service: ", e);
		}

		return response;

	}

	public Object fetchResult(String uri, Object requestObj) {
		return this.fetchResult(uri, requestObj, Map.class);
	}

	public <T> T fetchResult(String uri, Object requestObj, Class<T> c) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		try {
			return restTemplate.postForObject(uri, requestObj, c);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from external service: ", e);
			throw new CustomException("SERVICE CALL FAILED", e.getMessage());
		}
	}
}
