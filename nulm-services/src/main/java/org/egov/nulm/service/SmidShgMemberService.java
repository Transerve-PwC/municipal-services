
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
import org.egov.nulm.model.NulmShgMemberRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SmidShgMemberApplication;
import org.egov.nulm.repository.SmidShgMemberRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SmidShgMemberService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SmidShgMemberRepository repository;

	private IdGenRepository idgenrepository;

	private AuditDetailsUtil auditDetailsUtil;

	@Autowired
	public SmidShgMemberService(SmidShgMemberRepository repository, ObjectMapper objectMapper,
			IdGenRepository idgenrepository, NULMConfiguration config, AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil = auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createMembers(NulmShgMemberRequest memberrequest) {
		try {
			SmidShgMemberApplication smidapplication = objectMapper.convertValue(memberrequest.getSmidShgMemberApplication(),
					SmidShgMemberApplication.class);
			   checkValidation(smidapplication);
			   repository.checkShgUuid(smidapplication);
				String smidid = UUID.randomUUID().toString();
				smidapplication.setApplicationUuid(smidid);
				smidapplication.setIsActive(true);
				smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(memberrequest.getRequestInfo(), CommonConstants.ACTION_CREATE));
			   // idgen service call to genrate event id
				IdGenerationResponse id = idgenrepository.getId(memberrequest.getRequestInfo(), smidapplication.getTenantId(),
						config.getSmidapplicationNumberIdgenName(), config.getSmidapplicationNumberIdgenFormat(), 1);
				if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
					smidapplication.setApplicationId(id.getIdResponses().get(0).getId());
				else
					throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);

				repository.createMembers(smidapplication);

				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
						.responseBody(smidapplication).build(), HttpStatus.CREATED);

			} catch (Exception e) {
				throw new CustomException(CommonConstants.SMID_SHG_MEMBER_APPLICATION_EXCEPTION_CODE, e.getMessage());
			}
	}
	
	public ResponseEntity<ResponseInfoWrapper> getMembers(NulmShgMemberRequest memberrequest) {
		try {

			SmidShgMemberApplication shg = objectMapper.convertValue(memberrequest.getSmidShgMemberApplication(),
					SmidShgMemberApplication.class);
			List<Role> role=memberrequest.getRequestInfo().getUserInfo().getRoles();
			List<SmidShgMemberApplication> groupresult = repository.getMembers(shg,role,memberrequest.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(groupresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SMID_SHG_MEMBER_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> updateMembers(NulmShgMemberRequest memberrequest) {
		try {
			SmidShgMemberApplication smidapplication = objectMapper.convertValue(memberrequest.getSmidShgMemberApplication(),
					SmidShgMemberApplication.class);
			checkValidation(smidapplication);
			String status = "";
			repository.checkMemberUuid(smidapplication);
			List<Role> role = memberrequest.getRequestInfo().getUserInfo().getRoles();
			JSONArray groupresult = repository.getMemmberStatus(smidapplication);
			JSONObject applicationData = (JSONObject) groupresult.get(0);
			status = applicationData.get("application_status").toString();
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleNgoUser())) {
					
					if (status.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.CREATED.toString())) {
						smidapplication.setApplicationStatus(SmidShgMemberApplication.StatusEnum.CREATED);
					}
					if (status.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.APPROVED.toString())
							|| status.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.REJECTED.toString())) {
						smidapplication.setApplicationStatus(SmidShgMemberApplication.StatusEnum.UPDATED);
					}
				}
				else {
					smidapplication.setApplicationStatus(SmidShgMemberApplication.StatusEnum.fromValue(status));
				}
			}
			smidapplication.setIsActive(true);
			smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(memberrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
		 	repository.updateMembers(smidapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(smidapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_MEMBER_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> deleteMembers(NulmShgMemberRequest memberrequest) {
		try {
			SmidShgMemberApplication smidapplication = objectMapper.convertValue(memberrequest.getSmidShgMemberApplication(),
					SmidShgMemberApplication.class);
			String status = "";
			repository.checkMemberUuid(smidapplication);
			List<Role> role = memberrequest.getRequestInfo().getUserInfo().getRoles();
			JSONArray groupresult = repository.getMemmberStatus(smidapplication);
			JSONObject applicationData = (JSONObject) groupresult.get(0);
			status = applicationData.get("application_status").toString();
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleNgoUser()) ||
						(roleobj.getCode()).equalsIgnoreCase(config.getRoleCitizenUser())) {
					
					if (status.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.CREATED.toString())) {
						repository.hardDeleteMembers(smidapplication);
					}
					if (status.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.APPROVED.toString())
							|| status.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.REJECTED.toString())) {
						smidapplication.setApplicationStatus(SmidShgMemberApplication.StatusEnum.DELETIONINPROGRESS);
						smidapplication.setIsActive(true);
						smidapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(memberrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
					 	repository.deleteMembers(smidapplication);
					}
				}
			}
			
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(smidapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_MEMBER_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	private void checkValidation(SmidShgMemberApplication smidapplication) {
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
	
	public ResponseEntity<ResponseInfoWrapper> memberCount(NulmShgMemberRequest   request) {
		try {

			SmidShgMemberApplication smidapplication = objectMapper.convertValue(request.getSmidShgMemberApplication(),
					SmidShgMemberApplication.class);
			List<SmidShgMemberApplication> groupresult = repository.getMemberCount(smidapplication);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(groupresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
}