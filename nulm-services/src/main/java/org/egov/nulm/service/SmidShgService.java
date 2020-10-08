
package org.egov.nulm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.idgen.model.IdGenerationResponse;
import org.egov.nulm.model.NulmShgRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SmidShgGroup;
import org.egov.nulm.model.SmidShgMemberApplication;
import org.egov.nulm.repository.SmidShgRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SmidShgService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SmidShgRepository repository;

	private IdGenRepository idgenrepository;

	private AuditDetailsUtil auditDetailsUtil;

	@Autowired
	public SmidShgService(SmidShgRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config, AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil = auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createGroup(NulmShgRequest shgrequest) {
		try {
			SmidShgGroup shg = objectMapper.convertValue(shgrequest.getNulmShgRequest(), SmidShgGroup.class);

			String uuid = UUID.randomUUID().toString();
			shg.setShgUuid(uuid);
			shg.setIsActive(true);
			shg.setAuditDetails(
					auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_CREATE));
			// idgen service call to genrate event id
			IdGenerationResponse id = idgenrepository.getId(shgrequest.getRequestInfo(), shg.getTenantId(),
					config.getSmidShgIdgenName(), config.getSmidShgIdgenFormat(), 1);
			if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
				shg.setShgId(id.getIdResponses().get(0).getId());
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);

			repository.createGroup(shg);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(shg)
					.build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> getGroup(NulmShgRequest shgrequest) {
		try {

			SmidShgGroup shg = objectMapper.convertValue(shgrequest.getNulmShgRequest(), SmidShgGroup.class);
			List<Role> role = shgrequest.getRequestInfo().getUserInfo().getRoles();
			List<SmidShgGroup> groupresult = repository.getGroup(shg, role,
					shgrequest.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(groupresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	
	
	public ResponseEntity<ResponseInfoWrapper> updateGroup(NulmShgRequest shgrequest) {
		try {
			SmidShgGroup shg = objectMapper.convertValue(shgrequest.getNulmShgRequest(), SmidShgGroup.class);
			String status = "";
			repository.checkShgUuid(shg);
			List<Role> role = shgrequest.getRequestInfo().getUserInfo().getRoles();
			JSONArray groupresult = repository.getGroupStatus(shg);
			JSONObject applicationData = (JSONObject) groupresult.get(0);
			status = applicationData.get("status").toString();
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleNgoUser())) {

					if (status.equalsIgnoreCase(SmidShgGroup.StatusEnum.CREATED.toString())) {
						shg.setStatus(SmidShgGroup.StatusEnum.CREATED);
					}
					if (status.equalsIgnoreCase(SmidShgGroup.StatusEnum.APPROVED.toString())
							|| status.equalsIgnoreCase(SmidShgGroup.StatusEnum.REJECTED.toString())) {
						shg.setStatus(SmidShgGroup.StatusEnum.UPDATED);
					}
				} else {
					shg.setStatus(SmidShgGroup.StatusEnum.fromValue(status));
				}
			}
			shg.setIsActive(true);
			shg.setAuditDetails(
					auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.updateGroup(shg);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(shg)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> deleteGroup(NulmShgRequest shgrequest) {
		try {
			SmidShgGroup shg = objectMapper.convertValue(shgrequest.getNulmShgRequest(), SmidShgGroup.class);
			List<SmidShgMemberApplication> deletemember = new ArrayList<>();
			for (SmidShgMemberApplication memberobj : shg.getSmidShgMemberApplication()) {
				SmidShgMemberApplication member = new SmidShgMemberApplication();
				member.setApplicationStatus(
						SmidShgMemberApplication.StatusEnum.fromValue(memberobj.getApplicationStatus().toString()));
				member.setTenantId(shg.getTenantId());
				member.setApplicationUuid(memberobj.getApplicationUuid());
				member.setShgUuid(shg.getShgUuid());
				member.setIsActive(false);
				member.setAuditDetails(
						auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
				deletemember.add(member);
			}
			shg.setSmidShgMemberApplication(deletemember);
			shg.setStatus(SmidShgGroup.StatusEnum.fromValue(shg.getStatus().toString()));
			shg.setIsActive(false);
			shg.setAuditDetails(
					auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.deleteGroup(shg);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(shg)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	public ResponseEntity<ResponseInfoWrapper> updateGroupStatus(NulmShgRequest shgrequest) {
		try {
			SmidShgGroup shg = objectMapper.convertValue(shgrequest.getNulmShgRequest(), SmidShgGroup.class);
			String status = "";
			String reqStatus = shg.getStatus().toString();
			repository.checkShgUuid(shg);
			List<Role> role = shgrequest.getRequestInfo().getUserInfo().getRoles();
			JSONArray groupresult = repository.getGroupStatus(shg);
			JSONObject applicationData = (JSONObject) groupresult.get(0);
			status = applicationData.get("status").toString();
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleEmployee())) {

					if (reqStatus.equalsIgnoreCase(SmidShgGroup.StatusEnum.APPROVED.toString())) {
						if (status.equalsIgnoreCase(SmidShgGroup.StatusEnum.AWAITINGFORAPPROVAL.toString())) {
							shg.setStatus(SmidShgGroup.StatusEnum.APPROVED);
						}
						if (status.equalsIgnoreCase(SmidShgGroup.StatusEnum.AWAITINGFORDELETION.toString())) {
							shg.setStatus(SmidShgGroup.StatusEnum.DELETED);
						}
					}
					if (reqStatus.equalsIgnoreCase(SmidShgGroup.StatusEnum.REJECTED.toString())) {
						if (status.equalsIgnoreCase(SmidShgGroup.StatusEnum.AWAITINGFORAPPROVAL.toString())) {
							shg.setStatus(SmidShgGroup.StatusEnum.REJECTED);
						}
						if (status.equalsIgnoreCase(SmidShgGroup.StatusEnum.AWAITINGFORDELETION.toString())) {
							shg.setStatus(SmidShgGroup.StatusEnum.DELETED);
						}
					}

				}
			}			
			shg.setStatus(SmidShgGroup.StatusEnum.fromValue(shg.getStatus().toString()));
			shg.setIsActive(true);
			shg.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.updateGroupStatus(shg);
		   List<SmidShgMemberApplication> memberlist=repository.getMemmberList(shg);
		   for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleEmployee())) {
	
					memberlist.stream().forEach(element -> {
						String elementStatus=element.getApplicationStatus().toString();
						if (reqStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.APPROVED.toString())) {
							if (elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.AWAITINGFORAPPROVAL.toString())) {
								element.setApplicationStatus(SmidShgMemberApplication.StatusEnum.APPROVED);
								element.setIsActive(true);
								element.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
								repository.updateMemberStatus(element);
							}
							if (elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.AWAITINGFORDELETION.toString())) {
								element.setApplicationStatus(SmidShgMemberApplication.StatusEnum.DELETED);
								element.setIsActive(true);
								element.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
								repository.updateMemberStatus(element);
							}
						}
						if (reqStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.REJECTED.toString())) {
							if (elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.AWAITINGFORAPPROVAL.toString())) {
								element.setApplicationStatus(SmidShgMemberApplication.StatusEnum.REJECTED);
								element.setIsActive(true);
								element.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
								repository.updateMemberStatus(element);
							}
							if (elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.AWAITINGFORDELETION.toString())) {
								element.setApplicationStatus(SmidShgMemberApplication.StatusEnum.DELETED);
								element.setIsActive(true);
								element.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
								repository.updateMemberStatus(element);
							}
						}
						
					});			
					
				}
			}	
			
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(shg)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> forwardToApproval(NulmShgRequest shgrequest) {
		try {
			SmidShgGroup shg = objectMapper.convertValue(shgrequest.getNulmShgRequest(), SmidShgGroup.class);
			repository.checkShgUuid(shg);
			shg.setStatus(SmidShgGroup.StatusEnum.AWAITINGFORAPPROVAL);									
			shg.setIsActive(true);
			shg.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.updateGroupStatus(shg);
		   List<SmidShgMemberApplication> memberlist=repository.getMemmberList(shg);
			memberlist.stream().forEach(element -> {
						String elementStatus=element.getApplicationStatus().toString();
					
							if (elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.DELETIONINPROGRESS.toString())) {
								element.setApplicationStatus(SmidShgMemberApplication.StatusEnum.AWAITINGFORDELETION);
								element.setIsActive(true);
								element.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
								repository.updateMemberStatus(element);
							}
							else if(!elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.APPROVED.toString()) &&
									!elementStatus.equalsIgnoreCase(SmidShgMemberApplication.StatusEnum.DELETED.toString())){
								element.setApplicationStatus(SmidShgMemberApplication.StatusEnum.AWAITINGFORAPPROVAL);
								element.setIsActive(true);
								element.setAuditDetails(auditDetailsUtil.getAuditDetails(shgrequest.getRequestInfo(), CommonConstants.ACTION_UPDATE));
								repository.updateMemberStatus(element);
							}
							
						
					});			
				
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(shg)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_SHG_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
}