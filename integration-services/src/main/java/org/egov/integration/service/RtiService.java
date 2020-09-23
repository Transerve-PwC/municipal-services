
package org.egov.integration.service;

import java.util.List;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.RtiConfiguration;
import org.egov.integration.model.AccountResponse;
import org.egov.integration.model.MinistryMaster;
import org.egov.integration.model.RtiRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.model.RtiRequest;
import org.egov.integration.model.RtiResponse;
import org.egov.integration.repository.RtiRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RtiService {

	@Autowired
	private RequestFactory requestFactory;
	private RtiAsyncService rtiAsyncService;
	private RtiRepository repository;
	private RtiConfiguration config;
	private final ObjectMapper objectMapper;

	@Autowired
	public RtiService(RtiConfiguration config, ObjectMapper objectMapper,RtiAsyncService rtiAsyncService,RtiRepository repository) {
		this.config = config;
		this.objectMapper = objectMapper;
		this.rtiAsyncService=rtiAsyncService;
		this.repository=repository;
	}

	public ResponseEntity<ResponseInfoWrapper> get(RtiRequestInfoWrapper request){
		RestTemplate restTemplate = requestFactory.getRestTemplate();

		try {
			RtiRequest rti = objectMapper.convertValue(request.getRtiRequest(), RtiRequest.class);

			RtiResponse response = new RtiResponse();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			ResponseEntity<AccountResponse> responseEntity = restTemplate.exchange(
					config.getAccountHost() + config.getAccountPath(), HttpMethod.GET,
					new HttpEntity<>("{\"password\": \"" + config.getPassword() + "\",\"username\": \""
							+ config.getUsername() + "\"}", headers),
					AccountResponse.class);

			if (!StringUtils.isEmpty(responseEntity.getBody().getToken())) {
				response = rtiAsyncService.get(rti, responseEntity.getBody().getToken());			
			}
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(response)
					.build(), HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			throw new CustomException(CommonConstants.RTI_EXCEPTION_CODE, e.getResponseBodyAsString());
		}
	}	
	
	public ResponseEntity<ResponseInfoWrapper> getDepartment(RtiRequestInfoWrapper request) {
		try {
			List<MinistryMaster> SEPApplicationresult = repository.getDepartment(request);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SEPApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.RTI_EXCEPTION_CODE, e.getMessage());
		}
	}
}