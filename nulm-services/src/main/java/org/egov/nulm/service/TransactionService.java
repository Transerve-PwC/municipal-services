
package org.egov.nulm.service;

import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSusvTransactionRequest;
import org.egov.nulm.model.ResponseInfoWrapper;
import org.egov.nulm.model.Transaction;
import org.egov.nulm.repository.TransactionRepository;
import org.egov.nulm.util.AuditDetailsUtil;
import org.egov.nulm.util.IdGenRepository;
import org.egov.nulm.util.WorkFlowRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionService {

	private final ObjectMapper objectMapper;

	private TransactionRepository repository;

	private AuditDetailsUtil auditDetailsUtil;
	
	@Autowired
	public TransactionService(TransactionRepository repository, ObjectMapper objectMapper, IdGenRepository idgenrepository,
			NULMConfiguration config,AuditDetailsUtil auditDetailsUtil,WorkFlowRepository workFlowRepository) {
		this.objectMapper = objectMapper;
		this.repository = repository;
		this.auditDetailsUtil=auditDetailsUtil;

	}

	public ResponseEntity<ResponseInfoWrapper> createTransaction(NulmSusvTransactionRequest request) {
		try {
			Transaction trans = objectMapper.convertValue(request.getNulmTransactionRequest(),
					Transaction.class);
			
			String id=UUID.randomUUID().toString();
			trans.setUuid(id);
			trans.setIsActive(true);
			trans.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
		   
			trans.getSupportingDocument().stream().forEach(element -> {
				element.setUuid(id);
				element.setDocumnetUuid( UUID.randomUUID().toString());
				element.setIsActive(true);
				element.setTenantId(trans.getTenantId());
				element.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
			
			});
		
		    repository.createTransaction(trans);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(trans).build(), HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.TRANSACTION_EXCEPTION_CODE, e.getMessage().toString());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> updateTransaction(NulmSusvTransactionRequest request) {
		try {
			Transaction trans = objectMapper.convertValue(request.getNulmTransactionRequest(),
					Transaction.class);
			
			
			trans.setIsActive(true);
			trans.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_UPDATE));
		   
			trans.getSupportingDocument().stream().forEach(element -> {
				element.setDocumnetUuid( UUID.randomUUID().toString());
				element.setUuid(trans.getUuid());
				element.setIsActive(true);
				element.setTenantId(trans.getTenantId());
				element.setAuditDetails(auditDetailsUtil.getAuditDetails(request.getRequestInfo(), CommonConstants.ACTION_CREATE));
			
			});
		
		    repository.updateTransaction(trans);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(trans).build(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.TRANSACTION_EXCEPTION_CODE, e.getMessage().toString());
		}
	}
	public ResponseEntity<ResponseInfoWrapper> getTransaction(NulmSusvTransactionRequest request)  {
		try {
			Transaction trans = objectMapper.convertValue(request.getNulmTransactionRequest(),
					Transaction.class);
			List<Role> role=request.getRequestInfo().getUserInfo().getRoles();
			List<Transaction> result = repository.getTransaction(trans,role,request.getRequestInfo().getUserInfo().getId());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
					.responseBody(result).build(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.TRANSACTION_EXCEPTION_CODE, e.getMessage());
		}
	}
}