
package org.egov.nulm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.idgen.model.IdGenerationResponse;
import org.egov.nulm.model.NulmSmidRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SmidApplication;
import org.egov.nulm.repository.SmidRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SmidService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SmidRepository repository;

	private IdGenRepository idgenrepository;
	
	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	public SmidService(SmidRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config,AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil=auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createSMIDApplication(NulmSmidRequest smidrequest) {
		try {
			SmidApplication smidapplication = objectMapper.convertValue(smidrequest.getNulmSmidRequest(),
					SmidApplication.class);
		   checkValidation(smidapplication);
			String smidid = UUID.randomUUID().toString();
			smidapplication.setApplicationUuid(smidid);
			smidapplication.setIsActive(true);
			smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(smidrequest.getRequestInfo(), CommonConstants.ACTION_CREATE));
		   // idgen service call to genrate event id
			IdGenerationResponse id = idgenrepository.getId(smidrequest.getRequestInfo(), smidapplication.getTenantId(),
					config.getSmidapplicationNumberIdgenName(), config.getSmidapplicationNumberIdgenFormat(), 1);
			if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
				smidapplication.setApplicationId(id.getIdResponses().get(0).getId());
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);

			repository.createSMIDApplication(smidapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(smidapplication).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SMID_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	public ResponseEntity<ResponseInfoWrapper> updateSMIDApplication(NulmSmidRequest smidrequest) {
		try {
			SmidApplication smidapplication = objectMapper.convertValue(smidrequest.getNulmSmidRequest(),
					SmidApplication.class);
			checkValidation(smidapplication);
			smidapplication.setIsActive(true);
			smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(smidrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
		 	repository.updateSMIDApplication(smidapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(smidapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> updateSMIDApplicationStatus(NulmSmidRequest seprequest) {
		try {
			SmidApplication smidapplication = objectMapper.convertValue(seprequest.getNulmSmidRequest(),
					SmidApplication.class);
			
		 if(smidapplication.getApplicationStatus() != null  && smidapplication.getApplicationStatus().toString().equalsIgnoreCase(config.getApproved()))
		 {
			 smidapplication.setApplicationStatus(SmidApplication.StatusEnum.fromValue(smidapplication.getApplicationStatus().toString()));
			 smidapplication.setNulmApplicationId(UUID.randomUUID().toString());
			 smidapplication.setIsActive(true);
			 smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
		 }
		 else if(smidapplication.getApplicationStatus() != null  && smidapplication.getApplicationStatus().toString().equalsIgnoreCase(config.getRejected()))
			 
		 {
			 smidapplication.setApplicationStatus(SmidApplication.StatusEnum.fromValue(smidapplication.getApplicationStatus().toString()));
			 smidapplication.setIsActive(true);
			 smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			
		 }
		 repository.updateSMIDApplicationStatus(smidapplication);
		 return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(smidapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_APPLICATION_STATUS_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> getSMIDApplication(NulmSmidRequest smidrequest) {
		try {

			SmidApplication SmidApplication = objectMapper.convertValue(smidrequest.getNulmSmidRequest(),
					SmidApplication.class);
			List<Role> role=smidrequest.getRequestInfo().getUserInfo().getRoles();
			List<SmidApplication> SmidApplicationresult = repository.getSMIDApplication(SmidApplication,role,smidrequest.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SmidApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	private void checkValidation(SmidApplication smidapplication) {
		Map<String, String> errorMap = new HashMap<>();
		if (smidapplication != null) {
			if (smidapplication.getIsMinority() == true
					&& (smidapplication.getMinority() == null || smidapplication.getMinority() == "")) {
				errorMap.put(CommonConstants.APPLICATION_MINORITY_NULL_CODE,
						CommonConstants.APPLICATION_MINORITY_NULL_CODE_MESSAGE);
			}
			if (smidapplication.getIsUrbanPoor() == true
					&& (smidapplication.getBplNo() == null || smidapplication.getBplNo() == "")) {
				errorMap.put(CommonConstants.APPLICATION_BPLNO_NULL_CODE,
						CommonConstants.APPLICATION_BPLNO_NULL_CODE_MESSAGE);
			}
			if (smidapplication.getIsInsurance() == true
					&& (smidapplication.getInsuranceThrough() == null || smidapplication.getInsuranceThrough() == "")) {
				errorMap.put(CommonConstants.APPLICATION_INSURANCE_NULL_CODE,
						CommonConstants.APPLICATION_INSURANCE_NULL_CODE_MESSAGE);
			}
		} else {
			errorMap.put(CommonConstants.MISSING_OR_INVALID_SMID_APPLICATION_OBJECT,
					CommonConstants.MISSING_OR_INVALID_SMID_APPLICATION_MESSAGE);
		}

		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}

	}
}