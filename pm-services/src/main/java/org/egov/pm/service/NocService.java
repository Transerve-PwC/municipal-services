package org.egov.pm.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pm.model.EmailRequest;
import org.egov.pm.model.EmailTemplateModel;
import org.egov.pm.model.RequestData;
import org.egov.pm.producer.Producer;
import org.egov.pm.repository.NocRepository;
import org.egov.pm.util.CommonConstants;
import org.egov.pm.util.UserUtil;
import org.egov.pm.web.contract.NocResponse;
import org.egov.pm.web.contract.ResponseData;
import org.egov.pm.web.contract.factory.ResponseFactory;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NocService {

	@Autowired
	private NocRepository nocRepository;

	@Autowired
	private ResponseFactory responseInfoFactory;

	@Autowired
	private UserUtil userUtil;

	@Autowired
	Producer producer;

	@Value("${kafka.topics.notification.mail.name}")
	private String emailtopic;

	@Value("${kafka.topics.notification.sms}")
	private String smstopic;

	@Autowired
	@Qualifier("validatorAddUpdateJSON")
	private JSONObject jsonAddObject;

	@Autowired
	@Qualifier("validatorApproveRejectJSON")
	private JSONObject jsonApproveRejectObject;

	/**
	 * Get the Application Data list for NOC
	 * 
	 * @param RequestData
	 *            for applicationType,tenantId,applicationId,requestinfo
	 * @return The data list based on role,applicationType and tenantId
	 */
	public ResponseEntity<NocResponse> getNoc(RequestData requestInfo) {
		JSONArray nocs = nocRepository.getNoc(requestInfo);
		if (nocs == null || nocs.isEmpty()) {
			return new ResponseEntity(NocResponse.builder()
					.resposneInfo(
							ResponseInfo.builder().msgId("Invalid role or application type or blank data").build())
					.nocApplicationDetail(nocs).build(), HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity(
					NocResponse.builder().resposneInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.nocApplicationDetail(nocs).build(),
					HttpStatus.OK);
		}
	}

	/**
	 * Get the Application Data requested ID
	 * 
	 * @param RequestData
	 *            for tenantId,applicationId,requestinfo
	 * @return The data for requested application Id
	 */
	public ResponseEntity<NocResponse> viewNoc(RequestData requestInfo) {

		JSONArray nocs = nocRepository.viewNoc(requestInfo);
		if (nocs == null || nocs.isEmpty()) {
			return new ResponseEntity<>(NocResponse.builder()
					.resposneInfo(ResponseInfo.builder().msgId(CommonConstants.INVALIDAPPID).build())
					.nocApplicationDetail(nocs).build(), HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(
					NocResponse.builder().resposneInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.nocApplicationDetail(nocs).build(),
					HttpStatus.OK);
		}
	}

	/**
	 * Get the Application Data required to generate certificate for NOC
	 * 
	 * @param RequestData
	 *            for applicationType,tenantId,applicationId,requestinfo
	 * @return The certificate data for requested application Id
	 */

	public ResponseEntity<NocResponse> getCertificateData(RequestData requestInfo) {

		JSONArray nocs = nocRepository.getCertificateData(requestInfo);
		if (nocs == null || nocs.isEmpty()) {
			return new ResponseEntity<>(NocResponse.builder()
					.resposneInfo(ResponseInfo.builder().msgId(CommonConstants.INVALIDAPPID).build())
					.nocApplicationDetail(nocs).build(), HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(
					NocResponse.builder().resposneInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.nocApplicationDetail(nocs).build(),
					HttpStatus.OK);
		}
	}

	/**
	 * Adding a new Noc data
	 * 
	 * @param RequestData
	 *            for applicationType,tenantId,applicationId,requestinfo
	 */
	public ResponseEntity<ResponseData> saveNoc(RequestData requestData) {

		String responseValidate = "";
		try {
			responseValidate = validateJsonAddUpdateData(requestData, CommonConstants.INSERT);
			if (responseValidate.equals("")) {
				String applicationId = nocRepository.saveValidateStatus(requestData,
						requestData.getApplicationStatus());
				if (applicationId != null) {
					return new ResponseEntity<>(ResponseData.builder()
							.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.applicationId(applicationId).build(), HttpStatus.CREATED);

				} else {
					return new ResponseEntity<>(ResponseData.builder()
							.responseInfo(ResponseInfo.builder().status("FAIL").msgId("Record Creation Failed").build())
							.applicationId(applicationId).build(), HttpStatus.BAD_REQUEST);
				}
			} else {
				return new ResponseEntity<>(ResponseData.builder()
						.responseInfo(ResponseInfo.builder().status("FAIL").msgId(responseValidate).build()).build(),
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			throw new CustomException("EGBS_NOCS_SAVE_ERROR", e.getMessage());
		}
	}

	/**
	 * Getting RemarksData for application
	 * 
	 * @param RequestData,applicationType,tenantId,applicationId,requestinfo
	 */
	public ResponseEntity<NocResponse> getRemarksForNoc(RequestData requestInfo) {

		JSONArray nocs = nocRepository.getRemarksForNoc(requestInfo.getTenantId(), requestInfo.getApplicationId());
		if (nocs == null || nocs.isEmpty()) {
			return new ResponseEntity<>(NocResponse.builder()
					.resposneInfo(ResponseInfo.builder().msgId(CommonConstants.INVALIDAPPID).build())
					.nocApplicationDetail(nocs).build(), HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(
					NocResponse.builder().resposneInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.nocApplicationDetail(nocs).build(),
					HttpStatus.OK);
		}
	}

	/**
	 * Updates data for application
	 * 
	 * @param RequestData,applicationType,tenantId,applicationId,requestinfo
	 */
	public ResponseEntity<ResponseData> updateNoc(RequestData requestData) {
		String responseValidate = "";
		try {
			responseValidate = validateJsonAddUpdateData(requestData, CommonConstants.UPDATE);

			if (responseValidate.equals("")) {
				int applicationcount = nocRepository.validateApplicationId(requestData.getApplicationId());
				if (applicationcount > 0) {
					nocRepository.updateNOC(requestData, requestData.getApplicationId());

					return new ResponseEntity<>(ResponseData.builder()
							.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.applicationId(requestData.getApplicationId()).build(), HttpStatus.CREATED);
				} else {
					return new ResponseEntity<>(
							ResponseData.builder()
									.responseInfo(ResponseInfo.builder()
											.msgId("Invalid Application Id - No Application Id Found: ["
													+ requestData.getApplicationId() + "]")
											.status(CommonConstants.FAIL).build())
									.applicationId(requestData.getApplicationId()).build(),
							HttpStatus.BAD_REQUEST);
				}

			} else {
				return new ResponseEntity<>(ResponseData.builder()
						.responseInfo(
								ResponseInfo.builder().msgId(responseValidate).status(CommonConstants.FAIL).build())
						.applicationId(requestData.getApplicationId()).build(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			log.info("update noc exception ", e.getMessage());
		}
		return null;
	}

	/**
	 * Validating Json data for all status validations
	 * 
	 * @param RequestData,applicationType,tenantId,applicationId,requestinfo
	 */
	String validateJsonUpdateStatusData(RequestData requestData) throws ParseException {
		String responseText = "";
		if (requestData.getApplicationStatus() == null || requestData.getApplicationStatus().isEmpty()
				|| requestData.getTenantId() == null || requestData.getTenantId().isEmpty()
				|| requestData.getApplicationId() == null || requestData.getApplicationId().isEmpty()) {
			return "Invalid or blank Application Id or Application Status or datapayload or tenant Id ";
		}
		try {
			String roleCode = null;
			JSONParser jsonParser = new JSONParser();

			List<Role> roleList = requestData.getRequestInfo().getUserInfo().getRoles();
			boolean roleFlag = false;
			JSONObject jsonValidator = (JSONObject) jsonParser.parse(jsonApproveRejectObject.toJSONString());
			jsonValidator = (JSONObject) jsonValidator.get(requestData.getApplicationType());
			for (Role role : roleList) {
				if (role.getCode() != null && role.getCode().isEmpty()) {
					return "Invalid Role";
				}
				if (jsonValidator.get(role.getCode() + "-" + requestData.getCurrentState()) != null) {
					jsonValidator = (JSONObject) jsonValidator
							.get(role.getCode() + "-" + requestData.getCurrentState());
					roleFlag = true;
				}
			}

			if (!roleFlag)
				return "Invalid Role";

			jsonValidator = (JSONObject) jsonValidator.get(requestData.getApplicationStatus());
			JSONObject jsonRequested = (JSONObject) jsonParser.parse(requestData.getDataPayload().toString());

			if (jsonValidator == null || jsonRequested == null) {
				return "Invalid data to load the JSON file or requested data.";
			}
			responseText = commonValidation(jsonValidator, jsonRequested);
		} catch (Exception e) {
			throw new CustomException("NOC_UPDATE_STATUS",
					"Invalid Application Type or Role or application status or datapayload data");
		}
		return responseText;
	}

	/**
	 * Validating Json data for create and update validations
	 * 
	 * @param RequestData,applicationType,tenantId,applicationId,requestinfo
	 */
	private String validateJsonAddUpdateData(RequestData requestData, String calledfor) throws ParseException {
		String responseText = "";
		if (requestData.getApplicationStatus() == null || requestData.getApplicationStatus().isEmpty()
				|| requestData.getDataPayload().isEmpty() || requestData.getTenantId().isEmpty()) {
			if (calledfor.equals("U")
					&& (requestData.getApplicationId() == null || requestData.getApplicationId().isEmpty()))
				return "Invalid or blank - Application Id or Application Status or datapayload or tenant Id ";
			else
				return "Invalid or blank - Application Status or datapayload or tenant Id";
		}

		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonValidator = (JSONObject) jsonParser.parse(jsonAddObject.toJSONString());
			jsonValidator = (JSONObject) jsonValidator.get(requestData.getApplicationType());
			JSONObject jsonRequested = (JSONObject) jsonParser.parse(requestData.getDataPayload().toString());

			if (jsonValidator == null || jsonRequested == null) {
				return "Unable to load the JSON file or requested data.";
			}
			responseText = commonValidation(jsonValidator, jsonRequested);

		} catch (Exception e) {
			throw new CustomException("NOC_SAVE_UPDATE", "Invalid Application Type or Role or datapayload data");
		}
		return responseText;
	}

	/**
	 * Common Validation Method
	 * 
	 * @param requested
	 *            json and validator json
	 */
	private String commonValidation(JSONObject jsonValidator, JSONObject jsonRequested) {

		Set<String> keyValidateList = jsonValidator.keySet();
		Set<String> keyRequestedList = jsonRequested.keySet();
		StringBuilder responseText = new StringBuilder();
		try {
			if (keyValidateList.equals(keyRequestedList)) {

				for (String key : keyValidateList) {
					JSONObject actualValidate = (JSONObject) jsonValidator.get(key);
					String isMandatory = actualValidate.get("mandatory").toString();
					String isRegExpression = actualValidate.get("validateRegularExp").toString();
					String dataReq = jsonRequested.get(key).toString();
					if (isMandatory.equals("true") && dataReq.equals("")) {
						responseText.append(key + " : [Mandatory field]");
						responseText.append(",");
					} else {
						if (!dataReq.equals("")) {
							Pattern validatePattern = Pattern.compile(isRegExpression);
							if (!validatePattern.matcher(dataReq).matches()) {
								responseText.append(key + ":[Invalid data]");
								responseText.append(",");
							}
						}
					}
				}
				if (!responseText.toString().equals("")) {
					responseText = new StringBuilder(
							"Error at =>  " + responseText.substring(0, responseText.length() - 1));
				}
			} else {
				responseText = new StringBuilder("Invalid Requested Colunms");
			}

		} catch (Exception e) {
			responseText.append("Unable to Process request => ");
			responseText.append("Exceptions => " + e.getMessage());
		}

		return responseText.toString();
	}

	/**
	 * Update status of the application
	 * 
	 * @param RequestData,applicationType,tenantId,applicationId,requestinfo
	 */
	public ResponseEntity<ResponseData> updateNocApplicationStatus(RequestData requestData) {

		String responseValidate = "";
		ResponseData reponseData = null;
		try {
			responseValidate = validateJsonUpdateStatusData(requestData);

			if (responseValidate.equals("")) {
				reponseData = nocRepository.updateAppStatus(requestData);
				if (reponseData.getResponseInfo().getStatus().equals(CommonConstants.SUCCESS)) {
					return new ResponseEntity<>(ResponseData.builder()
							.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
							.applicationId(requestData.getApplicationId()).build(), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(reponseData, HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<>(ResponseData.builder()
						.responseInfo(
								ResponseInfo.builder().msgId(responseValidate).status(CommonConstants.FAIL).build())
						.applicationId(requestData.getApplicationId()).build(), HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			log.debug("NOCUPDATESTAUS ERROR:" + e);
			e.printStackTrace();
			throw new CustomException("NOCUPDATESTAUS ERROR", e.getMessage());
		}
	}

	/**
	 * Getting pricebook data
	 * 
	 * @param required
	 *            tenantId,applicationType
	 */
	public NocResponse viewPriceBook(RequestData requestInfo) {
		Object nocs = nocRepository.viewPriceBook(requestInfo);
		NocResponse res = new NocResponse();
		res.setResposneInfo(responseInfoFactory.getResponseInfo(requestInfo.getRequestInfo(), HttpStatus.OK));
		res.setNocApplicationDetail(nocs);
		return res;
	}

	/**
	 * Getting pricebook data for requested pricebook id
	 * 
	 * @param required
	 *            pricebook id
	 */
	public NocResponse viewPriceBookById(RequestData requestInfo) {
		Object nocs = nocRepository.viewPriceBookById(requestInfo);
		NocResponse res = new NocResponse();
		res.setResposneInfo(responseInfoFactory.getResponseInfo(requestInfo.getRequestInfo(), HttpStatus.OK));
		res.setNocApplicationDetail(nocs);
		return res;
	}

	/**
	 * Updating pricebook data
	 * 
	 * @param required
	 *            RequestInfo,datapayload and tenantId datapayload includes
	 *            categoryid,sub category id,annual,perday,per month,fixed price
	 */
	public NocResponse updatepricebook(RequestData requestData) throws java.text.ParseException {
		ResponseInfo res = new ResponseInfo();
		NocResponse rs = null;
		int flag = 0;
		int isExist = nocRepository.getpricebook(requestData, flag);
		if (isExist > 0) {
			rs = nocRepository.updatepricebook(requestData);
		} else {
			flag = 1;
			nocRepository.getpricebook(requestData, flag);
			rs = nocRepository.updatepricebookdate(requestData);
		}
		Map<String, EmailTemplateModel> map = null;
		map = nocRepository.findTemplate(requestData.getApplicationStatus(), requestData.getTenantId(),
				requestData.getApplicationType());
		if (map != null && !map.isEmpty()) {
			for (String role : map.keySet()) {
				JsonNode userInfo = userUtil.getUserByRole(requestData.getRequestInfo(), role,
						requestData.getTenantId());
				if (userInfo != null) {
					for (JsonNode employee : userInfo.get("Employees")) {
						JsonNode user = employee.get("user");
						String emailTemplate = map.get(role).getTemplate();
						EmailRequest email = EmailRequest.builder().email(user.get("emailId").toString())
								.subject(map.get(role).getEmailSubject()).isHTML(true).body(emailTemplate).build();
						producer.push(emailtopic, email);
						res.setStatus(CommonConstants.SUCCESS);
					}
				} else {
					res.setStatus(CommonConstants.FAIL);
					res.setResMsgId("Error while connecting HRMS sevice");

				}
			}
		} else {
			res.setResMsgId("No Template fOUND");
		}
		res.setStatus("SUCCESS");
		rs.setResposneInfo(res);
		return rs;
	}
}
