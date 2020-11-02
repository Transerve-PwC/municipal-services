package org.egov.ps.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.ps.model.ExcelSearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
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

	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> uploadStreamToFileStore(ByteArrayOutputStream outputStream, String tenantId,
			String fileName, String contentType) throws UnsupportedEncodingException {
		StringBuilder uri = new StringBuilder(fileStoreUrl.substring(0, fileStoreUrl.length() - 4));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

		body.add("filename", fileName);
		body.add("contentType", contentType);

		ByteArrayResource contentsAsResource = new ByteArrayResource(outputStream.toByteArray()) {
			@Override
			public String getFilename() {
				return fileName; // Filename has to be returned in order to be able to post.
			}
		};
		body.add("file", contentsAsResource);

		uri.append("?tenantId=" + tenantId + "&module=" + "EstateServices");
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
