package org.egov.cpt.util;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.cpt.models.ExcelSearchCriteria;
import org.egov.cpt.models.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
		String responseMap = "";
		StringBuilder uri = new StringBuilder(fileStoreUrl);
		uri.append("?tenantId=" + searchCriteria.getTenantId() + "&fileStoreIds=" + searchCriteria.getFileStoreId());
		try {
			Map<String, Object> response = (Map<String, Object>) (restTemplate.getForObject(uri.toString(),
					HashMap.class));
			responseMap = String.valueOf(response.get(searchCriteria.getFileStoreId()));
		} catch (Exception e) {
			log.error("Exception while fetching file url: ", e);
		}
		return responseMap;
	}

	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> fetchFileStoreId(File file, Property property) {
		StringBuilder uri = new StringBuilder("http://localhost:8060//filestore/v1/files");

		FileSystemResource fileSystemResource = new FileSystemResource(file);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", fileSystemResource);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		uri.append("?tenantId=" + property.getTenantId() + "&module=" + "RentedProperties");
		try {
			Map<String, Map<String, String>> response = (Map<String, Map<String, String>>) restTemplate
					.postForObject(uri.toString(), requestEntity, HashMap.class);

			List<HashMap<String, String>> result = (List<HashMap<String, String>>) response.get("files");
			return result;
		} catch (Exception e) {
			log.error("Exception while fetching file store id: ", e);
		}
		return null;
	}
}
