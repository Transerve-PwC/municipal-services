
package org.egov.nulm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.idgen.model.IdGenerationResponse;
import org.egov.nulm.model.NulmSepRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SepApplication;
import org.egov.nulm.model.SepApplicationDocument;
import org.egov.nulm.repository.SepRepository;
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
public class SepService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SepRepository repository;

	private IdGenRepository idgenrepository;
	
	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	public SepService(SepRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config,AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil=auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createSEPApplication(NulmSepRequest seprequest) {
		try {
			SepApplication sepapplication = objectMapper.convertValue(seprequest.getNulmSepRequest(),
					SepApplication.class);
			checkValidation(sepapplication);
			String sepid = UUID.randomUUID().toString();
			sepapplication.setApplicationUuid(sepid);
			sepapplication.setIsActive(true);
			sepapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_CREATE));
		   // idgen service call to genrate event id
			IdGenerationResponse id = idgenrepository.getId(seprequest.getRequestInfo(), sepapplication.getTenantId(),
					config.getSepapplicationNumberIdgenName(), config.getSepapplicationNumberIdgenFormat(), 1);
			if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
				sepapplication.setApplicationId(id.getIdResponses().get(0).getId());
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);

			// save document to nulm_sep_application_document table
			List<SepApplicationDocument> sepdoc = new ArrayList<>();
			for (SepApplicationDocument docobj : sepapplication.getApplicationDocument()) {
				SepApplicationDocument documnet = new SepApplicationDocument();
				documnet.setDocumnetUuid(UUID.randomUUID().toString());
				documnet.setApplicationUuid(sepid);
				documnet.setDocumentType(docobj.getDocumentType());
				documnet.setFilestoreId(docobj.getFilestoreId());
				documnet.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_CREATE));
				documnet.setIsActive(true);
				documnet.setTenantId(sepapplication.getTenantId());
				sepdoc.add(documnet);

			}
			sepapplication.setApplicationDocument(sepdoc);
			repository.createSEPApplication(sepapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(sepapplication).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SEP_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	private void checkValidation(SepApplication sepapplication) {
		Map<String, String> errorMap = new HashMap<>();
		if (sepapplication != null) {
			if (sepapplication.getIsMinority() == true
					&& (sepapplication.getMinority() == null || sepapplication.getMinority() == "")) {
				errorMap.put(CommonConstants.APPLICATION_MINORITY_NULL_CODE,
						CommonConstants.APPLICATION_MINORITY_NULL_CODE_MESSAGE);
			}
			if (sepapplication.getIsUrbanPoor() == true
					&& (sepapplication.getBplNo() == null || sepapplication.getBplNo() == "")) {
				errorMap.put(CommonConstants.APPLICATION_BPLNO_NULL_CODE,
						CommonConstants.APPLICATION_BPLNO_NULL_CODE_MESSAGE);
			}
		} else {
			errorMap.put(CommonConstants.MISSING_OR_INVALID_SEP_APPLICATION_OBJECT,
					CommonConstants.MISSING_OR_INVALID_SEP_APPLICATION_MESSAGE);
		}

		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}

	}

	public ResponseEntity<ResponseInfoWrapper> getSEPApplication(NulmSepRequest seprequest) {
		try {

			SepApplication SEPApplication = objectMapper.convertValue(seprequest.getNulmSepRequest(),
					SepApplication.class);
			List<Role> role=seprequest.getRequestInfo().getUserInfo().getRoles();
			List<SepApplication> SEPApplicationresult = repository.getSEPApplication(SEPApplication,role,seprequest.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SEPApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SEP_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	public ResponseEntity<ResponseInfoWrapper> updateSEPApplication(NulmSepRequest seprequest) {
		try {
			SepApplication sepapplication = objectMapper.convertValue(seprequest.getNulmSepRequest(),
					SepApplication.class);
			
			checkValidation(sepapplication);
			sepapplication.setIsActive(true);
			sepapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
		   // update document to nulm_sep_application_document table
			List<SepApplicationDocument> sepdoc = new ArrayList<>();
			for (SepApplicationDocument docobj : sepapplication.getApplicationDocument()) {
							
				SepApplicationDocument document = new SepApplicationDocument();
				document.setDocumnetUuid(UUID.randomUUID().toString());
				document.setApplicationUuid(sepapplication.getApplicationUuid());
				document.setDocumentType(docobj.getDocumentType());
				document.setFilestoreId(docobj.getFilestoreId());
				document.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_CREATE));
				document.setIsActive(true);
				document.setTenantId(sepapplication.getTenantId());
				sepdoc.add(document);
				

			}
			sepapplication.setApplicationDocument(sepdoc);
			repository.updateSEPApplication(sepapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(sepapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SEP_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	public ResponseEntity<ResponseInfoWrapper> updateSEPApplicationStatus(NulmSepRequest seprequest) {
		try {
			 SepApplication sepapplication = objectMapper.convertValue(seprequest.getNulmSepRequest(), SepApplication.class);			
			 if(sepapplication.getApplicationStatus() != null  && sepapplication.getApplicationStatus().toString().equalsIgnoreCase(config.getApproved())){
				 sepapplication.setApplicationStatus(SepApplication.StatusEnum.fromValue(sepapplication.getApplicationStatus().toString()));
				 sepapplication.setNulmApplicationId(UUID.randomUUID().toString());
				 sepapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			 }
			 else if(sepapplication.getApplicationStatus() != null  && sepapplication.getApplicationStatus().toString().equalsIgnoreCase(config.getRejected())){
				 sepapplication.setApplicationStatus(SepApplication.StatusEnum.fromValue(sepapplication.getApplicationStatus().toString()));
				 sepapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(seprequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
				 sepapplication.setNulmApplicationId(UUID.randomUUID().toString());
			 }
			 repository.updateSEPApplicationStatus(sepapplication);
			 return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
						.responseBody(sepapplication).build(), HttpStatus.OK);
		} catch (Exception e) {
			throw new CustomException(CommonConstants.SEP_APPLICATION_STATUS_EXCEPTION_CODE, e.getMessage());
		}
	}
	
}