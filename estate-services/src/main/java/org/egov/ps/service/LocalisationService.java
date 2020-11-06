package org.egov.ps.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.repository.ServiceRequestRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service

public class LocalisationService {
    @Autowired
	private Configuration config;

	@Autowired
    private ServiceRequestRepository serviceRequestRepository;
    
    public static final Map<String, Map<String, String>> localisedMessageMap = new HashMap<>();
/**
	 * Populates the localized msg cache
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param locale
	 * @param module
	 */
	public void getLocalisedMessages(RequestInfo requestInfo, String tenantId, String locale, String module) {
		Map<String, String> mapOfCodesAndMessages = new HashMap<>();
        StringBuilder uri = new StringBuilder();
        RequestInfoWrapper requestInfoWrapper = new RequestInfoWrapper();
		// RequestInfoWrapper requestInfoWrapper ;// = hCUtils.prepareRequestForLocalization(uri, requestInfo, locale,
				// tenantId, module);
		requestInfoWrapper.setRequestInfo(requestInfo);
        uri.append(config.getLocalizationHost()).append(config.getLocalizationEndpoint()).append("?tenantId=" + tenantId)
                .append("&module=" + module).append("&locale=" + locale);
        
        
		List<String> codes = null;
		List<String> messages = null;
		Object result = null;
		try {
			result = serviceRequestRepository.fetchResult(uri, requestInfoWrapper);
			codes = JsonPath.read(result, PSConstants.LOCALIZATION_CODES_JSONPATH);
			messages = JsonPath.read(result, PSConstants.LOCALIZATION_MSGS_JSONPATH);
		} catch (Exception e) {
			log.error("Exception while fetching from localization: " + e);
		}
		if (null != result && codes != null && messages != null) {
			for (int i = 0; i < codes.size(); i++) {
				mapOfCodesAndMessages.put(codes.get(i), messages.get(i));
			}
			localisedMessageMap.put(locale + "|" + tenantId, mapOfCodesAndMessages);
		}
	}
}
