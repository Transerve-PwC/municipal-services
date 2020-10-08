
package org.egov.nulm.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSuhLogRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.SuhLogMaintenance;
import org.egov.nulm.repository.SuhLogRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SuhLogService {

	private final ObjectMapper objectMapper;

	private NULMConfiguration config;

	private SuhLogRepository repository;

	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	public SuhLogService(SuhLogRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config,AuditDetailsUtil auditDetailsUtil) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.config = config;
		this.auditDetailsUtil=auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createSuhLog(NulmSuhLogRequest request) {
		
		try {
			SuhLogMaintenance suhLog = objectMapper.convertValue(request.getNulmSuhLogRequest(),
					SuhLogMaintenance.class);
			suhLog.setLogUuid(UUID.randomUUID().toString());
			suhLog.setIsActive(true);
			suhLog.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
			repository.createSuhLog(suhLog);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhLog).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_LOG_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> getSuhLog(NulmSuhLogRequest request) {
		try {
			SuhLogMaintenance suhLog = objectMapper.convertValue(request.getNulmSuhLogRequest(),
					SuhLogMaintenance.class);
			List<Role> role=request.getRequestInfo().getUserInfo().getRoles();
			List<SuhLogMaintenance> suhLogResult = repository.getSuhLog(suhLog,role,request.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhLogResult).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.SUH_LOG_EXCEPTION_CODE, e.getMessage());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> deleteSuhLog(NulmSuhLogRequest request) {
		try {
			SuhLogMaintenance suhLog = objectMapper.convertValue(request.getNulmSuhLogRequest(),
					SuhLogMaintenance.class);
			checkDate(suhLog,request);
			suhLog.setIsActive(false);
			suhLog.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));
			repository.deleteSuhLog(suhLog);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(suhLog).build(), HttpStatus.OK);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_LOG_EXCEPTION_CODE, e.getMessage());
		}
	}
	private void checkDate(SuhLogMaintenance suhLog,NulmSuhLogRequest request)
	{Map<String, String> errorMap = new HashMap<>();
		
		long cuurentDateTime=new Date().getTime();
		List<Role> role=request.getRequestInfo().getUserInfo().getRoles();
		List<SuhLogMaintenance> suhLogResult = repository.getSuhLog(suhLog,role,request.getRequestInfo().getUserInfo().getId());
		if(suhLogResult!=null && !suhLogResult.isEmpty()){
			long createdTime=suhLogResult.get(0).getAuditDetails().getCreatedTime();
			long diff = cuurentDateTime - createdTime;
			int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
			log.info("nulm-services logs :: Executing checkDate() = {}",diffDays);
			System.err.println("dif................"+diffDays);
			if(diffDays>Integer.parseInt(config.getSuhDeleteDatePeriod())){
				errorMap.put(CommonConstants.INVALID_SUH_LOG_REQUEST, CommonConstants.INVALID_SUH_LOG_REQUEST_MESSAGE);
				throw new CustomException(errorMap);
			}
		}
		else {
			errorMap.put(CommonConstants.INVALID_SUH_LOG_REQUEST, CommonConstants.INVALID_SUH_LOG_UUID_REQUEST_MESSAGE);
			throw new CustomException(errorMap);
		}
	}
}