package org.egov.integration.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.PtConfiguration;
import org.egov.integration.model.PtRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PtService {
	@Autowired
	private RequestFactory requestFactory;

	private PtConfiguration config;

	@Autowired
	public PtService(PtConfiguration config) {
		this.config = config;
	}	
	
	public ResponseEntity<ResponseInfoWrapper> getPropertyTaxDetails(String uid) {
		try {		
			RestTemplate restTemplate = requestFactory.getRestTemplate();			
			ResponseEntity<String> ptDetails = restTemplate.exchange(config.getPtHost() + config.getPtPath().replace("<<uid>>", uid),
					HttpMethod.GET, null, String.class);			
			String ptStr = ptDetails.getBody().replaceAll(Pattern.quote("\\"), "");
			ptStr = ptStr.replaceAll(Pattern.quote("\"["), Matcher.quoteReplacement("["));
			ptStr = ptStr.replaceAll(Pattern.quote("]\""), Matcher.quoteReplacement("]"));
			JSONParser parser = new JSONParser();
			JSONArray json = null;
			try {
				json = (JSONArray) parser.parse(ptStr);
				System.out.println("json :: " + json.toJSONString());
			} catch (ParseException e) {
				log.error("integration-services logs :: getPropertyTaxDetails :: " + e.getMessage());
			}
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(json)
					.build(), HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			throw new CustomException(CommonConstants.PT_EXCEPTION_CODE, e.getResponseBodyAsString());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> getOTP(PtRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();	
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(config.getPtHost() + config.getPtSendotp())
				.queryParam(CommonConstants.PT_PARAM_UID, request.getUid())
				.queryParam(CommonConstants.PT_PARAM_MOBILE_NUMBER, request.getMobileNo());
		ResponseEntity<Object> response = restTemplate.exchange((new StringBuilder(builder.toUriString())).toString(),HttpMethod.GET, null,Object.class);
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(response.getBody())
				.build(), HttpStatus.OK);
	}

	public ResponseEntity<ResponseInfoWrapper> verifyOTP(PtRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();		
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(config.getPtHost() + config.getPtVerifyOtp())
				.queryParam(CommonConstants.PT_PARAM_UID, request.getUid())
				.queryParam(CommonConstants.PT_PARAM_MOBILE_NUMBER, request.getMobileNo())
				.queryParam(CommonConstants.PT_PARAM_OTP, request.getOtp())
				.queryParam(CommonConstants.PT_PARAM_TOKENID,  request.getTokenId());
		ResponseEntity<String> ptDetails = restTemplate.exchange((new StringBuilder(builder.toUriString())).toString(),
				HttpMethod.GET, null, String.class);			
		String ptStr = ptDetails.getBody().replaceAll(Pattern.quote("\\"), "");
		ptStr = ptStr.replaceAll(Pattern.quote("\"["), Matcher.quoteReplacement("["));
		ptStr = ptStr.replaceAll(Pattern.quote("]\""), Matcher.quoteReplacement("]"));
		JSONParser parser = new JSONParser();
		JSONArray json = null;
		try {
			json = (JSONArray) parser.parse(ptStr);
			System.out.println("json :: " + json.toJSONString());
		} catch (ParseException e) {
			log.error("integration-services logs :: getPropertyTaxDetails :: " + e.getMessage());
		}
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(json)
				.build(), HttpStatus.OK);
	}

	public ResponseEntity<ResponseInfoWrapper> getSectorList(PtRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();			
		ResponseEntity<Object> ptDetails = restTemplate.exchange(config.getPtDemoHost() + config.getPtSectorList(),
				HttpMethod.GET, null, Object.class);
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(ptDetails.getBody())
				.build(), HttpStatus.OK);
		
	}

	public ResponseEntity<ResponseInfoWrapper> getPropertyByHouseNo(PtRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();	
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(config.getPtDemoHost() + config.getPtSearchProperty())
				.queryParam(CommonConstants.PT_PARAM_HOUSENO, request.getHouseNo())
				.queryParam(CommonConstants.PT_PARAM_SECTORID, request.getSectorId());
		ResponseEntity<Object> ptDetails = restTemplate.exchange((new StringBuilder(builder.toUriString())).toString(),
				HttpMethod.GET, null, Object.class);
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(ptDetails.getBody())
				.build(), HttpStatus.OK);
		
	}
}
