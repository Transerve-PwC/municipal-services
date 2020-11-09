package org.egov.ps.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.repository.ServiceRequestRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service

public class LocalisationService {
	@Autowired
	private Configuration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * Populates the localized msg cache
	 * 
	 * @param requestInfo
	 * @param tenantId
	 * @param locale
	 * @param module
	 */
	@Cacheable(value = "allLocalisedMessages", key = "#tenantId")
	public Map<String, String> getAllLocalisedMessages(RequestInfo requestInfo, String tenantId, String locale,
			String module) {
		StringBuilder uri = new StringBuilder();
		uri.append(config.getLocalizationHost()).append(config.getLocalizationEndpoint())
				.append("?tenantId=" + tenantId).append("&module=" + module).append("&locale=" + locale);

		try {
			Map<String, String> mapOfCodesAndMessages = new HashMap<>();
			Object result = serviceRequestRepository.fetchResult(uri,
					RequestInfoWrapper.builder().requestInfo(requestInfo).build());
			List<Map<String, String>> items = JsonPath.read(result, PSConstants.LOCALIZATION_MSGS_JSONPATH);
			if (CollectionUtils.isEmpty(items)) {
				return Collections.emptyMap();
			}
			for (Map<String, String> map : items) {
				mapOfCodesAndMessages.put(map.get("code"), map.get("message"));
			}
			return mapOfCodesAndMessages;
		} catch (Exception e) {
			log.error("Exception while fetching from localization: " + e);
			throw new CustomException("LOCALIZATION_NOT_FOUND", "Could not fetch localizations");
		}
	}
}
