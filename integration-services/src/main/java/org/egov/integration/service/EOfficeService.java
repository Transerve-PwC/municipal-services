package org.egov.integration.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.EOfficeConfiguration;
import org.egov.integration.model.EOfficeRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.util.AES128Bit;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EOfficeService {

	@Autowired
	private RequestFactory requestFactory;

	@Autowired
	private EOfficeConfiguration config;

	public ResponseEntity<ResponseInfoWrapper> get(EOfficeRequestInfoWrapper request) {
		RestTemplate restTemplate = requestFactory.getRestTemplate();
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, Object> mmap = new LinkedMultiValueMap<String, Object>();
		mmap.add("orgid", request.getEOfficeRequest().getOrgid());
		mmap.add("postdetailid", request.getEOfficeRequest().getPostdetailid());
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity(mmap, http);

		CompletableFuture<String> filePending = CompletableFuture.supplyAsync(() -> {
			return restTemplate.postForObject(config.getEofficeHost() + config.getEofficeuserfilepending(), entity,
					String.class);
		}).handle((res, ex) -> {
			if (ex != null) {
				log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
				return null;
			}
			return res;
		});
		CompletableFuture<String> fileClosed = CompletableFuture.supplyAsync(() -> {
			return restTemplate.postForObject(config.getEofficeHost() + config.getEofficeuserfileclosed(), entity,
					String.class);
		}).handle((res, ex) -> {
			if (ex != null) {
				log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
				return null;
			}
			return res;
		});

		CompletableFuture<String> receiptClosed = CompletableFuture.supplyAsync(() -> {
			return restTemplate.postForObject(config.getEofficeHost() + config.getEofficeuserreceiptclosed(), entity,
					String.class);
		}).handle((res, ex) -> {
			if (ex != null) {
				log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
				return null;
			}
			return res;
		});

		CompletableFuture<String> receiptPending = CompletableFuture.supplyAsync(() -> {
			return restTemplate.postForObject(config.getEofficeHost() + config.getEofficeuserreceiptpending(), entity,
					String.class);
		}).handle((res, ex) -> {
			if (ex != null) {
				log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
				return null;
			}
			return res;
		});

		CompletableFuture<String> vipdependency = CompletableFuture.supplyAsync(() -> {
			return restTemplate.postForObject(config.getEofficeHost() + config.getEofficevipreceiptpending(), entity,
					String.class);
		}).handle((res, ex) -> {
			if (ex != null) {
				log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
				return null;
			}
			return res;
		});

		CompletableFuture<String> filehierarchy = CompletableFuture.supplyAsync(() -> {
			return restTemplate.postForObject(config.getEofficeHost() + config.getEofficefilependinghierarchy(), entity,
					String.class);
		}).handle((res, ex) -> {
			if (ex != null) {
				log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
				return null;
			}
			return res;
		});

		CompletableFuture.allOf(filePending, fileClosed, receiptPending, receiptClosed, vipdependency, filehierarchy)
				.join();
		JSONObject response = new JSONObject();
		try {

			if (null != filePending) {
				JSONObject xmlJSONObj = XML.toJSONObject(filePending.get());
				response.accumulate(CommonConstants.EOFFICE_FILEPENDING,
						xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
			}
			if (null != fileClosed) {
				JSONObject xmlJSONObj = XML.toJSONObject(fileClosed.get());
				response.accumulate(CommonConstants.EOFFICE_FILECLOSED,
						xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
			}
			if (null != receiptPending) {
				JSONObject xmlJSONObj = XML.toJSONObject(receiptPending.get());
				response.accumulate(CommonConstants.EOFFICE_RECEIPTPENDING,
						xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
			}
			if (null != receiptClosed) {
				JSONObject xmlJSONObj = XML.toJSONObject(receiptClosed.get());
				response.accumulate(CommonConstants.EOFFICE_RECEIPTCLOSED,
						xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
			}
			if (null != vipdependency) {
				JSONObject xmlJSONObj = XML.toJSONObject(vipdependency.get());
				response.accumulate(CommonConstants.EOFFICE_VIP, xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
			}
			if (null != filehierarchy) {
				JSONObject xmlJSONObj = XML.toJSONObject(filehierarchy.get());
				response.accumulate(CommonConstants.EOFFICE_HIERARCHY,
						xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
			}

		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(response.toMap())
				.build(), HttpStatus.OK);
	}

}
