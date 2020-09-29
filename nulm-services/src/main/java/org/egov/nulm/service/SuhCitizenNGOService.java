
package org.egov.nulm.service;

import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSuhCitizenNGORequest;
import org.egov.nulm.model.NulmSuhRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SuhApplication;
import org.egov.nulm.model.SuhCitizenNGOApplication;
import org.egov.nulm.repository.SuhCitizenNGORepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SuhCitizenNGOService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SuhCitizenNGORepository repository;

	private IdGenRepository idgenrepository;

	private AuditDetailsUtil auditDetailsUtil;

	@Autowired
	public SuhCitizenNGOService(SuhCitizenNGORepository repository, ObjectMapper objectMapper,
			IdGenRepository idgenrepository, NULMConfiguration config, AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil = auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createSuhApplication(NulmSuhCitizenNGORequest request) {
		try {
			SuhCitizenNGOApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhCitizenNGOApplication.class);
			String suhid = UUID.randomUUID().toString();
			suhapplication.setSuhCitizenNGOUuid(suhid);
			suhapplication.setIsActive(true);
			suhapplication.setAuditDetails(
					auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
			repository.createSuhApplication(suhapplication);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhapplication).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> updateSuhApplication(NulmSuhCitizenNGORequest request) {
		try {
			SuhCitizenNGOApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhCitizenNGOApplication.class);
			suhapplication.setIsActive(true);
			suhapplication.setAuditDetails(
					auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.updateSuhApplication(suhapplication);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> getSuhApplication(NulmSuhCitizenNGORequest request) {
		try {

			SuhCitizenNGOApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhCitizenNGOApplication.class);
			List<Role> role = request.getRequestInfo().getUserInfo().getRoles();
			List<SuhCitizenNGOApplication> SuhApplicationresult = repository.getSuhApplication(suhapplication);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SuhApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
}