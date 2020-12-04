package org.egov.integration.service;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.ApiConfiguration;
import org.egov.integration.model.HrmsRequest;
import org.egov.integration.model.HrmsRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HrmsService {
	@Autowired
	private RequestFactory requestFactory;

	@Autowired
	private ApiConfiguration config;

	@Autowired
	private ObjectMapper objectMapper;

	public ResponseEntity<ResponseInfoWrapper> getstateList(HrmsRequestInfoWrapper request) {
		// TODO Auto-generated method stub
		RestTemplate restTemplate = requestFactory.getRestTemplate();

		HttpHeaders headers = new HttpHeaders();
		String authStr = config.getHrmsAuthHeader() + ":"
				+ config.getHrmsAuthKey();
		String authEncoded = Base64Utils.encodeToString(authStr.getBytes());
		headers.add("Authorization", "Basic " + authEncoded);
		HttpEntity<String> reqEntity = new HttpEntity<String>(headers);
		ResponseEntity<Object> stateDetails = restTemplate.exchange(config.getEhrmsHost() + config.getHrmsStatelist(),
				HttpMethod.GET, reqEntity, Object.class);

		System.out.println(stateDetails);
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(stateDetails.getBody()).build(), HttpStatus.OK);
	}

	public ResponseEntity<ResponseInfoWrapper> getBacklogs(HrmsRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();
		HrmsRequest hrmsReq = objectMapper.convertValue(request.getHrmsRequest(), HrmsRequest.class);

		HttpHeaders headers = new HttpHeaders();
		String authStr = config.getHrmsAuthHeader() + ":"
				+ config.getHrmsAuthKey();
		String authEncoded = Base64Utils.encodeToString(authStr.getBytes());
		headers.add("Authorization", "Basic " + authEncoded);
		HttpEntity<String> reqEntity = new HttpEntity<String>(headers);
		ResponseEntity<Object> backlogDetails = restTemplate.exchange(
				config.getEhrmsHost() + config.getHrmsBacklog().replace("<<empcode>>", hrmsReq.getEmpCode()),
				HttpMethod.GET, reqEntity, Object.class);

		System.out.println(backlogDetails);
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(backlogDetails.getBody()).build(), HttpStatus.OK);
	}

	public ResponseEntity<ResponseInfoWrapper> getNotification(HrmsRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();
		HrmsRequest hrmsReq = objectMapper.convertValue(request.getHrmsRequest(), HrmsRequest.class);

		HttpHeaders headers = new HttpHeaders();
		String authStr = config.getHrmsAuthHeader() + ":"
				+ config.getHrmsAuthKey();
		String authEncoded = Base64Utils.encodeToString(authStr.getBytes());
		headers.add("Authorization", "Basic " + authEncoded);
		HttpEntity<String> reqEntity = new HttpEntity<String>(headers);
		ResponseEntity<Object> notificationDetails = restTemplate.exchange(
				config.getEhrmsHost() + config.getHrmsNotification().replace("<<empcode>>", hrmsReq.getEmpCode()),
				HttpMethod.GET, reqEntity, Object.class);

		System.out.println(notificationDetails);
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(notificationDetails.getBody()).build(), HttpStatus.OK);
	}

}
