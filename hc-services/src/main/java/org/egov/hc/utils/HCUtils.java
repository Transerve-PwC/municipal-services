package org.egov.hc.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.egov.common.contract.request.RequestInfo;
import org.egov.hc.contract.AuditDetails;
import org.egov.hc.contract.RequestInfoWrapper;


import org.egov.hc.producer.HCConfiguration;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component

public class HCUtils {


	@Autowired
	private HCConfiguration hcConfiguration;

	@Autowired
	@Qualifier("validatorAddUpdateJSON")
	private JSONObject jsonAddObject;



	/**
	 * Util method to return Auditdetails for create and update processes
	 * 
	 * @param by
	 * @param isCreate
	 * @return
	 */
	public AuditDetails getAuditDetails(String by, Boolean isCreate) {

		Long dt = new Date().getTime();
		if (isCreate) 
			return AuditDetails.builder().createdBy(by).createdTime(dt).lastModifiedBy(by).lastModifiedTime(dt).build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(dt).build();
	}

	public AuditDetails getAuditDetail(String by, Boolean isCreate) {

		Long dt = new Date().getTime();

		return AuditDetails.builder().createdBy(by).createdTime(dt).lastModifiedBy(by).lastModifiedTime(dt).build();

	}



	public RequestInfoWrapper prepareRequestForLocalization(StringBuilder uri, RequestInfo requestInfo, String locale,
			String tenantId, String module) {
		RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
		requestInfoWrapper.setRequestInfo(requestInfo);
		uri.append(hcConfiguration.getLocalizationHost()).append(hcConfiguration.getLocalizationSearchEndpoint()).append("?tenantId=" + tenantId)
		.append("&module=" + module).append("&locale=" + locale);

		return requestInfoWrapper;
	}



	public Map<String, Object> prepareRequestForUserSearch(StringBuilder uri, RequestInfo requestInfo, String userId,
			String tenantId) {
		Map<String, Object> userServiceRequest = new HashMap();
		String[] userIds = { userId };
		userServiceRequest.put("RequestInfo", requestInfo);
		userServiceRequest.put("tenantId", tenantId);
		userServiceRequest.put("id", Arrays.asList(userIds));
		userServiceRequest.put("userType", HCConstants.ROLE_CITIZEN);

		uri.append(hcConfiguration.getEgovUserHost()).append(hcConfiguration.getEgovUserSearchEndpoint());

		return userServiceRequest;
	}

	/**
	 * Returns mapper with all the appropriate properties reqd in our
	 * functionalities.
	 * 
	 * @return ObjectMapper
	 */
	public ObjectMapper getObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		return mapper;
	}

	public String validateJsonAddUpdateData(String requestData, String applicationType)  {
		String responseText = "";
		try {

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonValidator = (JSONObject) jsonParser.parse(jsonAddObject.toJSONString());
			jsonValidator = (JSONObject) jsonValidator.get(applicationType);

			JSONObject jsonRequested = (JSONObject) jsonParser.parse(requestData.toString());

			if (jsonValidator == null || jsonRequested == null) {
				return "Unable to load the JSON file or requested data.";
			}
			responseText = commonValidation(jsonValidator, jsonRequested);

		} catch (Exception e) {
			throw new CustomException("HC_SAVE_UPDATE_GET", "Invalid Application Type or Role or datapayload data");
		}
		return responseText;
	}

	private String commonValidation(JSONObject jsonValidator, JSONObject jsonRequested) {
		Set<String> keyValidateList = jsonValidator.keySet();
		//Set<String> keyRequestedList = jsonRequested.keySet();
		StringBuilder responseText = new StringBuilder();

		try {
			for (String key : keyValidateList) {
				//JSONObject actualValidate = (JSONObject) jsonValidator.get(key);
				//String isMandatory = actualValidate.get("mandatory").toString();
				//String isRegExpression = actualValidate.get("validateRegularExp").toString();
				if (key.equals("is_Active") || key.equals("media") || key.equals("wfDocuments") || key.equals("isRoleSpecific"))
					continue;
				else {
					if (jsonRequested.get(key) != null) {
						if (jsonRequested.get(key) instanceof JSONArray) {
							JSONArray jsonarray = (JSONArray) jsonRequested.get(key);
							for (Object json : jsonarray)
							{
								if (jsonRequested.get(key) instanceof JSONObject) 
								{
									String jsonInString = new Gson().toJson(json);
									JSONObject mJSONObject = (JSONObject) new JSONParser().parse(jsonInString);
									HashMap<String, Object> yourHashMap = new Gson().fromJson(mJSONObject.toString(), HashMap.class);

									for (Map.Entry<String,Object> entry : yourHashMap.entrySet()) 
									{
										if(entry.getValue().toString().contains("</script>")||entry.getValue().toString().contains("&lt;/script&gt;")) {
											responseText.append(entry.getKey().toString() + ":[Invalid data]");
											responseText.append(",");
										}
										else
											responseText =  xxsFilterPatern(entry.getValue().toString(),entry.getKey().toString(),responseText);
									}
								}
								else
								{
									if(json.toString().contains("</script>")||json.toString().contains("&lt;/script&gt;")) {
										responseText.append(key + ":[Invalid data]");
										responseText.append(",");
										break;
									}
									else 
										responseText =  xxsFilterPatern(json.toString().toString(),key,responseText);
								}
							}
						}
						else
						{
							String dataReq = jsonRequested.get(key).toString();
							if(dataReq.contains("</script>")||dataReq.contains("&lt;/script&gt;")) {
								responseText.append(key + ":[Invalid data]");
								responseText.append(",");
							}
							else
								responseText =  xxsFilterPatern(dataReq,key,responseText);
						}
					}
				}
			}

			if (!responseText.toString().equals("")) {
				responseText = new StringBuilder("Error at =>  " + responseText.substring(0, responseText.length() - 1));
			}
		} catch (Exception e) {
			responseText.append("Unable to Process request => ");
			responseText.append("Exceptions => " + e.getMessage());
		}

		return responseText.toString();
	}

	private StringBuilder xxsFilterPatern(String dataReq,String key,StringBuilder responseText)
	{

		Pattern validatePattern = null;
		List<String> validatePatternList =new ArrayList<String>();
		validatePatternList.add(HCConstants.SCRIPTTAGXSS);
		validatePatternList.add(HCConstants.SRC1TAGXSS);
		validatePatternList.add(HCConstants.SRC2TAGXSS);
		validatePatternList.add(HCConstants.SCRIPTENDTAGXSS);
		validatePatternList.add(HCConstants.SCRIPTSTARTTAGXSS);
		validatePatternList.add(HCConstants.JSTAGXSS);
		validatePatternList.add(HCConstants.ONLOADTAGXSS);

		for(String validateStrPattern : validatePatternList)
		{
			if (!dataReq.equals("") && !dataReq.equals(null)) {
				validatePattern = Pattern.compile(validateStrPattern,
						Pattern.CASE_INSENSITIVE);
				if (validatePattern.matcher(dataReq).matches()) {
					responseText.append(key + ":[Invalid data]");
					responseText.append(",");
					break;
				}
			}
		}
		return responseText;

	}

}
