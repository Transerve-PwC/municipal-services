package org.egov.ec.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.repository.StoreItemRegisterRepository;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.EcPayment;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.Report;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
import org.egov.ec.web.models.StoreItemRegister;
import org.egov.ec.web.models.workflow.ProcessInstance;
import org.egov.ec.web.models.workflow.ProcessInstanceRequest;
import org.egov.ec.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StoreItemRegisterService {

	private final ObjectMapper objectMapper;
	private WorkflowIntegrator wfIntegrator;
	private CustomBeanValidator validate;
	private StoreItemRegisterRepository repository;
	private DeviceSourceService deviceSource;

	@Autowired
	public StoreItemRegisterService(WorkflowIntegrator wfIntegrator, ObjectMapper objectMapper,CustomBeanValidator validate,
			StoreItemRegisterRepository repository,DeviceSourceService deviceSource) {
		this.objectMapper = objectMapper;
		this.wfIntegrator = wfIntegrator;
		this.repository = repository;
		this.validate=validate;
		this.deviceSource=deviceSource;
	}

	/**
	*This method will fetch store item entires
	*
	* @param RequestInfoWrapper SearchCriteria
	* @return ResponseInfoWrapper containing list of items added to store against particular challan
	* @throws CustomException STOREITEM_GET_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> getStoreRegisterItem(RequestInfoWrapper requestInfoWrapper) {
		log.info("Store Item Service - Get Store Item");
		try {
			List<StoreItemRegister> storeItemRegister = null;
			EcSearchCriteria searchCriteria = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),EcSearchCriteria.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(searchCriteria, EcSearchCriteria.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.REPORTAGEINGGET);
		
			if(responseValidate.equals("")) 
			{
			
					 storeItemRegister = repository.getStoreItemRegister(searchCriteria);
				
				return new ResponseEntity<>(
						ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
								.responseBody(storeItemRegister).build(),
						HttpStatus.OK);
			}
			else
			{
				throw new CustomException("STOREITEM_GET_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Store Item Service - Get Store Item Exception"+e.getMessage());
			throw new CustomException("STOREITEM_GET_EXCEPTION", e.getMessage());
		}

	}

	/**
	* This method will add entries into store
	*
	* @param RequestInfoWrapper containing list of records which needs to be added in store
	* @param requestHeader for saving device source information
	* @return HTTP status on success
	* @throws CustomException STOREITEM_ADDSTORE_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> createStoreRegisterItem(RequestInfoWrapper requestInfoWrapper, String requestHeader) {
		log.info("Store Item Service - Create Store Item");
		try {
			StoreItemRegister storeItemRegister = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),
					StoreItemRegister.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(storeItemRegister, StoreItemRegister.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION);
		
			if(responseValidate.equals("")) 
			{
					storeItemRegister.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					storeItemRegister.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					storeItemRegister.setCreatedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					storeItemRegister.setCreatedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
		
					String sourceUuid = deviceSource.saveDeviceDetails(requestHeader, "addStoreItemEvent",
							storeItemRegister.getTenantId(), requestInfoWrapper.getAuditDetails());
					storeItemRegister.getDocument().stream().forEach((c) -> {
						c.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
						c.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
						c.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
						c.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
						c.setViolationUuid(storeItemRegister.getViolationUuid());
						c.setIsActive(true);
						c.setDocumentUuid(UUID.randomUUID().toString());
						c.setTenantId(storeItemRegister.getTenantId());
						c.setChallanId(storeItemRegister.getChallanUuid());
					});
					storeItemRegister.getStoreItemRegister().stream().forEach((c) -> {
						c.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
						c.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
						c.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
						c.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
						c.setAuctionedQuantity((long) 0);
						c.setIsReturned(false);
						c.setStoreItemUuid(UUID.randomUUID().toString());
						c.setChallanUuid(storeItemRegister.getChallanUuid());
						c.setSourceUuid(sourceUuid);
					});
					ProcessInstance processInstance = new ProcessInstance();
					processInstance.setBusinessId(storeItemRegister.getChallanId());
					processInstance.setTenantId(storeItemRegister.getTenantId());
					processInstance.setBusinessService(EcConstants.WORKFLOW_CHALLAN);
					processInstance.setAction(storeItemRegister.getStatus());
					processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList = Arrays.asList(processInstance);
		
					ResponseInfo response= wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList)
							.requestInfo(requestInfoWrapper.getRequestInfo()).build());
					
					validate.validateFields(storeItemRegister.getStoreItemRegister());
					
				
					if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						repository.saveStoreItem(storeItemRegister);
					}
				
					return new ResponseEntity<>(
							ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
									.responseBody(storeItemRegister).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("STOREITEM_ADDSTORE_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Store Item Service - Add Store Item Exception"+e.getMessage());
			throw new CustomException("STOREITEM_ADDSTORE_EXCEPTION", e.getMessage());
		}
	}

	/**
	* This method will update offline payment details
	*
	* @param RequestInfoWrapper containing store object
	* @return HTTP status on success
	* @throws CustomException STOREITEM_UPDATE_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> updateStoreRegisterItem(RequestInfoWrapper requestInfoWrapper) {
		log.info("Store Item Service - Update Store Item");
		try {
			StoreItemRegister storeItemRegister = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),
					StoreItemRegister.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(storeItemRegister, StoreItemRegister.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION);
		
			if(responseValidate.equals("")) 
			{
					storeItemRegister.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					storeItemRegister.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					storeItemRegister.setCreatedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					storeItemRegister.setCreatedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
		
				
					
					ProcessInstance processInstance = new ProcessInstance();
					processInstance.setBusinessId(storeItemRegister.getChallanId());
					processInstance.setTenantId(storeItemRegister.getTenantId());
					processInstance.setBusinessService(EcConstants.WORKFLOW_CHALLAN);
					processInstance.setAction(storeItemRegister.getStatus());
					processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList = Arrays.asList(processInstance);
		
					ResponseInfo response= wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList)
							.requestInfo(requestInfoWrapper.getRequestInfo()).build());
					if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						repository.updateStoreItem(storeItemRegister);
						}
					
					
		
					return new ResponseEntity<>(
							ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
									.responseBody(storeItemRegister).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("STOREITEM_UPDATE_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Store Item Service - Update Store Item Exception"+e.getMessage());
			throw new CustomException("STOREITEM_UPDATE_EXCEPTION", e.getMessage());
		}
	}

	/**
	* This method will update payment details in system
	* @param RequestInfoWrapper containing store object
	* @return HTTP status on success
	* @throws CustomException STOREITEM_ADDPAYMENT_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> updateStorePayment(RequestInfoWrapper requestInfoWrapper) {
		log.info("Store Item Service - Update Store Item");
		try {
			EcPayment ecPayment = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), EcPayment.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(ecPayment, EcPayment.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION);
		
			if(responseValidate.equals("")) 
			{
					ecPayment.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					ecPayment.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					ecPayment.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					ecPayment.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					ProcessInstance processInstance = new ProcessInstance();
					processInstance.setBusinessId(ecPayment.getChallanUuid());
					processInstance.setTenantId(ecPayment.getTenantId());
					processInstance.setBusinessService(EcConstants.WORKFLOW_PAYMENT);
					processInstance.setAction(ecPayment.getPaymentStatus());
					processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList = Arrays.asList(processInstance);
		
					ResponseInfo response= wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList)
							.requestInfo(requestInfoWrapper.getRequestInfo()).build());
		
					if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						repository.updateStorePayment(ecPayment);
					}
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(ecPayment).build(),
							HttpStatus.OK);
					}
			else
				{
						throw new CustomException("STOREITEM_ADDPAYMENT_EXCEPTION", responseValidate);
				}
		} catch (Exception e) {
			log.error("Store Item Service - Add Payment Exception"+e.getMessage());
			throw new CustomException("STOREITEM_ADDPAYMENT_EXCEPTION", e.getMessage());

		}
	}
}