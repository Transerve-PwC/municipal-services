package org.egov.cpt.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.cpt.models.ExcelSearchCriteria;
import org.egov.cpt.models.Property;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
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

	@Value("${egov.filestore.host}${egov.file.url.path}")
	private String fileStoreUrl;

	private RestTemplate restTemplate;

	public FileStoreUtils(RestTemplate restTemplate) {
		super();
		this.restTemplate = restTemplate;
	}

	@Cacheable(value = "fileUrl", sync = true)
	@SuppressWarnings("unchecked")
	public String fetchFileStoreUrl(ExcelSearchCriteria searchCriteria) {
		StringBuilder uri = new StringBuilder(fileStoreUrl);
		String stateLevelTenantId = this.getStateLevelTenantId(searchCriteria.getTenantId());
		uri.append("?tenantId=" + stateLevelTenantId + "&fileStoreIds=" + searchCriteria.getFileStoreId());
		Map<String, Object> response = (Map<String, Object>) (restTemplate.getForObject(uri.toString(), HashMap.class));
		if (!response.containsKey(searchCriteria.getFileStoreId())) {
			throw new CustomException("FILE_NOT_FOUND", String.format("File store id %s not found with tenant id %s",
					searchCriteria.getFileStoreId(), stateLevelTenantId));
		}
		return String.valueOf(response.get(searchCriteria.getFileStoreId()));
	}

	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> fetchFileStoreId(File file, Property property) {
		StringBuilder uri = new StringBuilder(fileStoreUrl.substring(0, fileStoreUrl.length() - 4));

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

		uri.append("?tenantId=" + tenantId + "&module=" + "RentedProperties");
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

	private String getStateLevelTenantId(String tenantId) {
		String[] components = tenantId.split(".");
		if (components.length == 0) {
			return "ch";
		}
		return components[0];
	}

}
