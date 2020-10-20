package org.egov.ps.util;

import java.util.HashMap;
import java.util.Map;

import org.egov.ps.model.ExcelSearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class FileStoreUtils {

	@Value("${egov.filestore-service-host}${egov.file.url.path}")
	private String fileStoreUrl;

	private RestTemplate restTemplate;

	public FileStoreUtils(RestTemplate restTemplate) {
		super();
		this.restTemplate = restTemplate;
	}

	@Cacheable(value = "fileUrl", sync = true)
	@SuppressWarnings("unchecked")
	public String fetchFileStoreUrl(ExcelSearchCriteria searchCriteria) {
		log.info("Start - fetchFileStoreUrl, request : ", searchCriteria);
		String responseMap = "";
		StringBuilder uri = new StringBuilder(fileStoreUrl);
		String tenantId = searchCriteria.getTenantId();
		if (tenantId.contains(".")) {
			String array[] = tenantId.split("\\.");
			tenantId = array[0];
		}
		uri.append("?tenantId=" + tenantId + "&fileStoreIds=" + searchCriteria.getFileStoreId());
		try {
			Map<String, Object> response = (Map<String, Object>) (restTemplate.getForObject(uri.toString(),
					HashMap.class));
		    responseMap = String.valueOf(response.get(searchCriteria.getFileStoreId())).equalsIgnoreCase("null") ? "" : 
				response.get(searchCriteria.getFileStoreId()).toString();			
		} catch (Exception e) {
			log.error("Exception while fetching file url: ", e);
		}
		if(responseMap.isEmpty())
			log.error("No data could found by using this request: ", searchCriteria);
		log.info("End - fetchFileStoreUrl, resposne : ", responseMap);
		return responseMap;
	}

}
