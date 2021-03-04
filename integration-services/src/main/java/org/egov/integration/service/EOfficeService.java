package org.egov.integration.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.EOfficeConfiguration;
import org.egov.integration.model.EOfficeMapRequestInfoWrapper;
import org.egov.integration.model.EOfficeRequestInfoWrapper;
import org.egov.integration.model.EmployeePostDetailMap;
import org.egov.integration.model.RequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.repository.EOfficeRepository;
import org.egov.integration.util.AuditDetailsUtil;
import org.egov.tracer.model.CustomException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EOfficeService {

	@Autowired
	private RequestFactory requestFactory;

	@Autowired
	private EOfficeConfiguration config;

	@Autowired
	EOfficeRepository repository;

	@Autowired
	private AuditDetailsUtil auditDetailsUtil;

	public ResponseEntity<ResponseInfoWrapper> get(EOfficeRequestInfoWrapper request) throws JSONException {
		RestTemplate restTemplate = requestFactory.getRestTemplate();
		JSONArray resData = new JSONArray();
		for (String postdetailId : request.getEOfficeRequest().getPostdetailid()) {
			System.out.println(postdetailId);
			JSONObject resData1 = new JSONObject();
			resData1.put("postdetailid", postdetailId);
			HttpHeaders http = new HttpHeaders();
			http.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, Object> mmap = new LinkedMultiValueMap<String, Object>();
			mmap.add("orgid", request.getEOfficeRequest().getOrgid());
			mmap.add("postdetailid", postdetailId);
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
				return restTemplate.postForObject(config.getEofficeHost() + config.getEofficeuserreceiptclosed(),
						entity, String.class);
			}).handle((res, ex) -> {
				if (ex != null) {
					log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
					return null;
				}
				return res;
			});

			CompletableFuture<String> receiptPending = CompletableFuture.supplyAsync(() -> {
				return restTemplate.postForObject(config.getEofficeHost() + config.getEofficeuserreceiptpending(),
						entity, String.class);
			}).handle((res, ex) -> {
				if (ex != null) {
					log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
					return null;
				}
				return res;
			});

			CompletableFuture<String> vipdependency = CompletableFuture.supplyAsync(() -> {
				return restTemplate.postForObject(config.getEofficeHost() + config.getEofficevipreceiptpending(),
						entity, String.class);
			}).handle((res, ex) -> {
				if (ex != null) {
					log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
					return null;
				}
				return res;
			});

			CompletableFuture<String> filehierarchy = CompletableFuture.supplyAsync(() -> {
				return restTemplate.postForObject(config.getEofficeHost() + config.getEofficefilependinghierarchy(),
						entity, String.class);
			}).handle((res, ex) -> {
				if (ex != null) {
					log.error("integration-services logs :: receipt pending :: " + ex.getMessage());
					return null;
				}
				return res;
			});

			CompletableFuture
					.allOf(filePending, fileClosed, receiptPending, receiptClosed, vipdependency, filehierarchy).join();
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
					response.accumulate(CommonConstants.EOFFICE_VIP,
							xmlJSONObj.get(CommonConstants.EOFFICE_WS_RESPONSE));
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
			Gson gson = new Gson();
			resData1.put("details", gson.fromJson(response.toString(), Object.class));
			resData.add(resData1);
		}
		Gson gson = new Gson();
		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(gson.fromJson(resData.toString(), Object.class)).build(), HttpStatus.OK);
	}

	public ResponseEntity<ResponseInfoWrapper> process(RequestInfoWrapper request) throws JSONException {
		RestTemplate restTemplate = requestFactory.getRestTemplate();
		HttpHeaders http = new HttpHeaders();
		http.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		http.add("Authorization", "Basic " + config.getEofficeAuthEncoded());
		MultiValueMap<String, Object> mmap = new LinkedMultiValueMap<String, Object>();
		mmap.add("departmentId", config.getEofficeDepartmentId());
		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity(mmap, http);
		log.info("integration-services logs :: process() :: Before calling eOffice APi");
		System.out.println(config.getEofficeHost() + config.getEofficeEmplyoeeDetials());
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(config.getEofficeHost() + config.getEofficeEmplyoeeDetials())
				.queryParam(CommonConstants.DEPARTMENT_ID, config.getEofficeDepartmentId());
		log.info("integration-services logs :: process() ::  calling eOffice APi-url "
				+ builder.toUriString().toString());

	
		  ResponseEntity<String> xml = restTemplate.exchange((new
		  StringBuilder(builder.toUriString())).toString(),HttpMethod.GET, entity,
		  String.class);
		  
		 
		 log. info("integration-services logs :: process() :: after calling eOffice APi-response,{}",xml );
		 
		
		
		
		JSONObject response = new JSONObject();
		
		JSONObject xmlJSONObj = new JSONObject();

		String responseStr = StringUtils.substringBetween(xml.getBody(), "<DETAIL>", "</DETAIL>");
		System.out.println("responseStr" + responseStr);
		List<EmployeePostDetailMap> map = new ArrayList<>();
		if (responseStr != null) {
			xmlJSONObj = XML.toJSONObject(responseStr);
			

			if (xmlJSONObj != null && xmlJSONObj.has("EMP_DETAIL")) {
				org.json.JSONArray jsonArray = (org.json.JSONArray) xmlJSONObj.get("EMP_DETAIL");

				for (int i = 0; i < jsonArray.length(); i++) {
					EmployeePostDetailMap obj = new EmployeePostDetailMap();
					JSONObject jsonData = jsonArray.getJSONObject(i);

					obj.setEmployeeId(jsonData.get("employeeId").toString());
					obj.setEmployeeCode(jsonData.get("employeeCode").toString());
					obj.setEmployeeName(jsonData.get("fullnameEn").toString());
					obj.setEmployeeDesignation(jsonData.get("designationName").toString());
					obj.setEmployeeEmail(jsonData.get("email").toString());
					JSONObject postdetail = jsonData.getJSONObject("post");

					if (postdetail != null && postdetail.get("postDetail") != null) {

						Object json = new JSONTokener(postdetail.get("postDetail").toString()).nextValue();
						if (json instanceof JSONObject) {
							JSONObject postdetailData = postdetail.getJSONObject("postDetail");
							System.out.println(postdetailData);
							obj.setPostDetailId(postdetailData.get("postDetailId").toString());
							obj.setPost(postdetailData.get("repPost").toString());
							String uuid = UUID.randomUUID().toString();
							obj.setUuid(uuid);
							obj.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(),
									CommonConstants.ACTION_CREATE));
							obj.setIsActive(true);
							repository.saveData(obj);
						} else 

						{
							org.json.JSONArray postdetailData = (org.json.JSONArray) (postdetail.get("postDetail"));
							for (int j = 0; j < postdetailData.length(); j++) {
								JSONObject jsonData1 = postdetailData.getJSONObject(j);
								obj.setPostDetailId(jsonData1.get("postDetailId").toString());
								obj.setPost(jsonData1.get("repPost").toString());
								String uuid = UUID.randomUUID().toString();
								obj.setUuid(uuid);
								obj.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(),
										CommonConstants.ACTION_CREATE));
								obj.setIsActive(true);
								repository.saveData(obj);
							}
						}

					}

				}

			}
		}

		System.out.println(map);
		response.accumulate(CommonConstants.EOFFICE_EMPLOYEE_DETAILS,
				xmlJSONObj.get(CommonConstants.EOFFICE_EMP_RESPONSE));
		Gson gson = new Gson();

		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(gson.fromJson(response.toString(), Object.class)).build(), HttpStatus.OK);

	}

	public ResponseEntity<ResponseInfoWrapper> getPostDetailsId(EOfficeMapRequestInfoWrapper request) {
		try {
			List<EmployeePostDetailMap> result = repository.getPostDetailId(request);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(result)
					.build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.EOFFICE_EXCEPTION_CODE, e.getMessage());
		}
	}
}
