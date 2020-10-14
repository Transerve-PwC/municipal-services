package org.egov.integration.service;

import java.util.UUID;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.model.PtMapping;
import org.egov.integration.model.PtMappingRequest;
import org.egov.integration.model.PtMappingRequestInfoWrapper;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.integration.repository.PtMappingRepository;
import org.egov.integration.util.AuditDetailsUtil;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PtMappingService {
	private final ObjectMapper objectMapper;
	private AuditDetailsUtil auditDetailsUtil;
	private PtMappingRepository repository;
	
	@Autowired
	public PtMappingService(ObjectMapper objectMapper, AuditDetailsUtil auditDetailsUtil, PtMappingRepository repository) {
		this.objectMapper = objectMapper;
		this.auditDetailsUtil = auditDetailsUtil;
		this.repository = repository;
	}
	
	public ResponseEntity<ResponseInfoWrapper> savePtMapping(PtMappingRequest request) {
		try {
			PtMapping ptMapping = objectMapper.convertValue(request.getPtMappingRequest(), PtMapping.class);

			String uuid = UUID.randomUUID().toString();
			ptMapping.setUuid(uuid);
			ptMapping.setIsActive(true);
			ptMapping.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
			
			repository.savePtMap(ptMapping);
			
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(ptMapping)
					.build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.PT_MAPPING_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	public ResponseEntity<ResponseInfoWrapper> getPropertyTaxList(PtMappingRequestInfoWrapper request) {
		try {
			JSONObject result = repository.getPropertyTaxList(request);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(result).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.PT_MAPPING_EXCEPTION_CODE, e.getMessage());
		}
	}
}
