
package org.egov.nulm.service;

import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.Organization;
import org.egov.nulm.model.OrganizationRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.repository.OrganizationRepository;
import org.egov.nulm.user.model.UserDetailResponse;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.UserUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrganizationService {
	private final ObjectMapper objectMapper;
	private UserUtil userUtil;	
	private OrganizationRepository repository;
	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	public OrganizationService(OrganizationRepository repository, ObjectMapper objectMapper,
			NULMConfiguration config,AuditDetailsUtil auditDetailsUtil, UserUtil userUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.auditDetailsUtil=auditDetailsUtil;
		this.userUtil=userUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createOrganization(OrganizationRequest request) {
		try {
			Organization organization = objectMapper.convertValue(request.getOrganizationRequest(),
					Organization.class);
			repository.checkMobileNo(organization);
			repository.checkOrganizationName(organization);
		    UserDetailResponse userDetailResponse =userUtil.createUser(request);
			
			if (userDetailResponse.getUser().get(0) != null && userDetailResponse.getUser().get(0).getId() != null)
				organization.setUserId(userDetailResponse.getUser().get(0).getId() );
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.USER_CREATION);
			 
			String sepid = UUID.randomUUID().toString();
			organization.setOrganizationUuid(sepid);
			organization.setIsActive(true);
			organization.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
		   	repository.createOrganization(organization);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(organization).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.ORGANIZATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	public ResponseEntity<ResponseInfoWrapper> getOrganization(OrganizationRequest request) {
		try {
			Organization organization = objectMapper.convertValue(request.getOrganizationRequest(),
					Organization.class);
			List<Role> role=request.getRequestInfo().getUserInfo().getRoles();
			List<Organization> orgresult = repository.getOrganization(organization,role,request.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(orgresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ORGANIZATION_EXCEPTION_CODE, e.getMessage());
		}
	}

	
}