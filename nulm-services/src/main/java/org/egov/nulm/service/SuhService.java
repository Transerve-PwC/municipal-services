
package org.egov.nulm.service;

import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.idgen.model.IdGenerationResponse;
import org.egov.nulm.model.NulmSuhRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SuhApplication;
import org.egov.nulm.repository.SuhRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SuhService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SuhRepository repository;

	private IdGenRepository idgenrepository;
	
	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	public SuhService(SuhRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config,AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.idgenrepository = idgenrepository;
		this.config = config;
		this.auditDetailsUtil=auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createSuhApplication(NulmSuhRequest request) {
		try {
			SuhApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhApplication.class);
			repository.checkShelterName(suhapplication);
			repository.checkOrganizationUuid(suhapplication);
			String suhid = UUID.randomUUID().toString();
			 // idgen service call to genrate event id
			IdGenerationResponse id = idgenrepository.getId(request.getRequestInfo(), suhapplication.getTenantId(),
					config.getSuhapplicationNumberIdgenName(), config.getSuhapplicationNumberIdgenFormat(), 1);
			if (id.getIdResponses() != null && id.getIdResponses().get(0) != null)
				suhapplication.setSuhId(id.getIdResponses().get(0).getId());
			else
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), CommonConstants.ID_GENERATION);
			suhapplication.setSuhUuid(suhid);
			suhapplication.setIsActive(true);
			suhapplication.setAddressPictureDoc(suhapplication.getAddressPicture()!=null?suhapplication.getAddressPicture().toJSONString():null);
			suhapplication.setProgramPictureDoc(suhapplication.getProgramPicture()!=null?suhapplication.getProgramPicture().toJSONString():null);
			suhapplication.setDocumentAttachmentDoc(suhapplication.getDocumentAttachment()!=null?suhapplication.getDocumentAttachment().toJSONString():null);
			suhapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
			//save facilities
			suhapplication.getSuhFacilitiesDetails().stream().forEach(element -> {
				element.setFacilityUuid( UUID.randomUUID().toString());
				element.setFacilityPictureDoc(element.getFacilityPicture()!=null?element.getFacilityPicture().toJSONString():null);
			
			});
			
			//save Record Maintenance
			suhapplication.getSuhRecordMaintenance().stream().forEach(element -> {
				element.setRecordUuid(UUID.randomUUID().toString());
			});
					
			//save staff Maintenance
			suhapplication.getSuhStaffMaintenance().stream().forEach(element -> {
				element.setStaffUuid(UUID.randomUUID().toString());
			});
			repository.createSuhApplication(suhapplication);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhapplication).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
	public ResponseEntity<ResponseInfoWrapper> updateSuhApplication(NulmSuhRequest request) {
		try {
			SuhApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhApplication.class);
			repository.checkOrganizationUuid(suhapplication);
			suhapplication.setIsActive(true);
			suhapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			suhapplication.setAddressPictureDoc(suhapplication.getAddressPicture()!=null?suhapplication.getAddressPicture().toJSONString():null);
			suhapplication.setProgramPictureDoc(suhapplication.getProgramPicture()!=null?suhapplication.getProgramPicture().toJSONString():null);
			suhapplication.setDocumentAttachmentDoc(suhapplication.getDocumentAttachment()!=null?suhapplication.getDocumentAttachment().toJSONString():null);
			suhapplication.getSuhFacilitiesDetails().stream().forEach(element -> {
				element.setFacilityPictureDoc(element.getFacilityPicture()!=null?element.getFacilityPicture().toJSONString():null);
			
			});
			repository.updateSuhApplication(suhapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> updateSuhApplicationStatus(NulmSuhRequest request) {
		try {
			SuhApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhApplication.class);
			suhapplication.setIsActive(true);
			suhapplication.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.updateSuhApplicationStatus(suhapplication);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhapplication).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> getSuhApplication(NulmSuhRequest request) {
		try {
			SuhApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhApplication.class);
			List<Role> role=request.getRequestInfo().getUserInfo().getRoles();
			List<SuhApplication> SuhApplicationresult = repository.getSuhApplication(suhapplication,role,request.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SuhApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> getShelterName(NulmSuhRequest request) {
		try {
			SuhApplication suhapplication = objectMapper.convertValue(request.getNulmSuhRequest(),
					SuhApplication.class);
			List<Role> role=request.getRequestInfo().getUserInfo().getRoles();
			List<SuhApplication> SuhApplicationresult = repository.getShelterName(suhapplication,role,request.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(SuhApplicationresult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
	}
	
}