package org.egov.ec.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.config.EchallanConfiguration;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.IdGenRepository;
import org.egov.ec.repository.ViolationRepository;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.ChallanDetails;
import org.egov.ec.web.models.EcPayment;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.NotificationTemplate;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
import org.egov.ec.web.models.Violation;
import org.egov.ec.web.models.Idgen.IdGenerationResponse;
import org.egov.ec.web.models.workflow.ProcessInstance;
import org.egov.ec.web.models.workflow.ProcessInstanceRequest;
import org.egov.ec.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ViolationService {
	private final ObjectMapper objectMapper;
	private WorkflowIntegrator wfIntegrator;
	private CustomBeanValidator validate;
	private ViolationRepository repository;
	private DeviceSourceService deviceSource;

	private Producer producer;
	private EchallanConfiguration config;

	public static final int DEFAULT_LIMIT = 10;

	public static final int MAX_LIMIT = 300;

	private IdGenRepository idgenrepository;
	

	@Autowired
	public ViolationService(WorkflowIntegrator wfIntegrator, ObjectMapper objectMapper, ViolationRepository repository,CustomBeanValidator validate,
			EchallanConfiguration config, IdGenRepository idgenrepository, Producer producer,DeviceSourceService deviceSource) {
		this.objectMapper = objectMapper;
		this.wfIntegrator = wfIntegrator;
		this.idgenrepository = idgenrepository;
		this.producer = producer;
		this.repository = repository;
		this.config = config;
		this.validate=validate;
		this.deviceSource=deviceSource;
	}
	
	/**
	*This method will be used to generate challan
	* @param RequestInfoWrapper containing objects of violation, violation details, documents
	* @param requestHeader for saving device source information
	* @return HTTP status on success
	* @throws CustomException VIOLATION_GENERATE_CHALLAN_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> generateChallan(RequestInfoWrapper requestInfoWrapper, String requestHeader) {
		log.info("Violation Service - Generate Challan");
		try {
			Violation violationMaster = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), Violation.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(violationMaster, Violation.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.CHALLANCREATE);
		
			if(responseValidate.equals("")) 
			{

					// Parsing the given String to Date object
					IdGenerationResponse id = idgenrepository.getId(requestInfoWrapper.getRequestInfo(),
							violationMaster.getTenantId(), config.getApplicationNumberIdgenName(),
							config.getApplicationNumberIdgenFormat(), 1);
					if (id.getIdResponses() != null && id.getIdResponses().get(0) != null) {
						violationMaster.setChallanId(id.getIdResponses().get(0).getId());
					} else {
						throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), EcConstants.FAILED_IDGEN_CHALLANID);
					}
					String violationUuid = UUID.randomUUID().toString();
					String challanUuid = UUID.randomUUID().toString();
					violationMaster.setChallanUuid(challanUuid);
					violationMaster.setViolationUuid(violationUuid);
		
					violationMaster.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					violationMaster.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					violationMaster.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					violationMaster.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
		
					String penaltyAmount = repository.getpenalty(violationMaster);
					if(penaltyAmount==null) {
						throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),  EcConstants.INVALID_FINE_DATA);
						}
					violationMaster.setTotalChallanAmount(penaltyAmount);
		
					violationMaster.setPenaltyAmount(penaltyAmount);
		
					violationMaster.getPaymentDetails().setPaymentStatus(EcConstants.STATUS_PENDING);
		
					violationMaster.getPaymentDetails().setIsActive(true);
					violationMaster.getPaymentDetails().setChallanId(violationMaster.getChallanId());
					violationMaster.getPaymentDetails().setPaymentAmount(penaltyAmount);
					violationMaster.getPaymentDetails().setChallanUuid(challanUuid);
					violationMaster.getPaymentDetails().setPaymentUuid(UUID.randomUUID().toString());
					;
					violationMaster.getPaymentDetails().setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					violationMaster.getPaymentDetails().setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					violationMaster.getPaymentDetails()
							.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					violationMaster.getPaymentDetails()
							.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					violationMaster.getPaymentDetails().setTenantId(violationMaster.getTenantId());
					violationMaster.getPaymentDetails().setViolationUuid(violationUuid);
		
					List<ChallanDetails> listdetails = new ArrayList<ChallanDetails>();
					ChallanDetails challanDetails = new ChallanDetails();
					challanDetails.setBudgetHead("FINE_AMOUNT");
					challanDetails.setHeadAmount(penaltyAmount);
					challanDetails.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					challanDetails.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					challanDetails.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					challanDetails.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					challanDetails.setTenantId(violationMaster.getTenantId());
					challanDetails.setChallanDetailUuid(UUID.randomUUID().toString());
					challanDetails.setChallanUuid(challanUuid);
					challanDetails.setIsActive(violationMaster.getIsActive());
		
					listdetails.add(challanDetails);
					ChallanDetails challanDetails1 = new ChallanDetails();
					challanDetails1.setBudgetHead("PENALTY_AMOUNT");
					challanDetails1.setHeadAmount("0");
					challanDetails1.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					challanDetails1.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					challanDetails1.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					challanDetails1.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					challanDetails1.setTenantId(violationMaster.getTenantId());
					challanDetails1.setIsActive(violationMaster.getIsActive());
					challanDetails1.setChallanDetailUuid(UUID.randomUUID().toString());
					challanDetails1.setChallanUuid(challanUuid);
					listdetails.add(challanDetails1);
					violationMaster.setChallanDetails(listdetails);
		
					violationMaster.getDocument().stream().forEach((c) -> {
						c.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
						c.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
						c.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
						c.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
						c.setViolationUuid(violationUuid);
						c.setDocumentUuid(UUID.randomUUID().toString());
						c.setTenantId(violationMaster.getTenantId());
						c.setChallanId(challanUuid);
					});
		
					violationMaster.getViolationItem().stream().forEach((c) -> {
						c.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
						c.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
						c.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
						c.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
						c.setViolationUuid(violationUuid);
						c.setViolationItemUuid(UUID.randomUUID().toString());
						c.setTenantId(violationMaster.getTenantId());
						c.setChallanId(challanUuid);
		
					});
		
					ProcessInstance processInstance = new ProcessInstance();
					processInstance.setBusinessId(violationMaster.getChallanId());
					processInstance.setTenantId(violationMaster.getTenantId());
					processInstance.setBusinessService(EcConstants.WORKFLOW_CHALLAN);
					processInstance.setAction(violationMaster.getStatus());
					processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList = Arrays.asList(processInstance);
					ResponseInfo response = wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder()
							.processInstances(processList).requestInfo(requestInfoWrapper.getRequestInfo()).build());
		
					ProcessInstance processInstance1 = new ProcessInstance();
					processInstance1.setBusinessId(violationMaster.getPaymentDetails().getChallanUuid());
					processInstance1.setTenantId(violationMaster.getTenantId());
					processInstance1.setBusinessService(EcConstants.WORKFLOW_PAYMENT);
					processInstance1.setAction(EcConstants.STATUS_PENDING);
					processInstance1.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList1 = Arrays.asList(processInstance1);
		
					ResponseInfo response1 = wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder()
							.processInstances(processList1).requestInfo(requestInfoWrapper.getRequestInfo()).build());
					String strOutput = violationMaster.getNotificationTemplate().getBody()
							.replace("<violator>",violationMaster.getViolatorName())
							.replace("<ChallanId>", violationMaster.getChallanId())
							.replace("<EnchroachmentType>", violationMaster.getEncroachmentType())
							.replace("<Date and Time>",
									violationMaster.getViolationDate() + " " + violationMaster.getViolationTime())
							.replace("<Link>", (config.getLoginUrl()+"?mobileno="+violationMaster.getContactNumber()+"&ecno="+violationMaster.getChallanId()))
							.replace("<br>","\r\n");
					System.out.println("dsvdjhvd"+strOutput);
					violationMaster.getNotificationTemplate().setBody(strOutput);
					violationMaster.getNotificationTemplate().setMessage(strOutput);
					
					validate.validateFields(violationMaster);
					
					String sourceUuid = deviceSource.saveDeviceDetails(requestHeader, "addViolationEvent",
							violationMaster.getTenantId(), requestInfoWrapper.getAuditDetails());
					violationMaster.setSourceUuid(sourceUuid);
					
					if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL) && response1 != null
							&& response1.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						repository.generateChallan(violationMaster);
						// email notification
						//producer.push(config.getEmailNotificationTopic(), violationMaster.getNotificationTemplate());
						// sms notification
						producer.push(config.getSmsNotificationTopic(), violationMaster.getNotificationTemplate());
		
					}
		
					return new ResponseEntity<>(
							ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
									.responseBody(violationMaster).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("VIOLATION_GENERATE_CHALLAN_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Violation Service - Generate Violation Exception"+e.getMessage());
			throw new CustomException("VIOLATION_GENERATE_CHALLAN_EXCEPTION", e.getMessage());
		}

	}

	/**
	*This method will be used to update challan status
	* @param RequestInfoWrapper containing violation object
	* @return HTTP status on success
	* @throws CustomException VIOLATION_UPDATE_CHALLAN_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> updateChallan(RequestInfoWrapper requestInfoWrapper) {
		log.info("Violation Service - Update Challan");
		try {
			Violation violationMaster = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), Violation.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(violationMaster, Violation.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.CHALLANUPDATE);
		
			if(responseValidate.equals("")) 
			{
				
					violationMaster.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					violationMaster.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					violationMaster.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					violationMaster.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					ProcessInstance processInstance = new ProcessInstance();
					processInstance.setBusinessId(violationMaster.getChallanId());
					processInstance.setTenantId(violationMaster.getTenantId());
					processInstance.setBusinessService(EcConstants.WORKFLOW_CHALLAN);
					processInstance.setAction(violationMaster.getStatus());
					processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList = Arrays.asList(processInstance);
		
					ResponseInfo response = wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder()
							.processInstances(processList).requestInfo(requestInfoWrapper.getRequestInfo()).build());
					if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						repository.updateChallan(violationMaster);
					}
		
					return new ResponseEntity<>(
							ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
									.responseBody(violationMaster).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("VIOLATION_UPDATE_CHALLAN_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Violation Service - Update Violation Exception"+e.getMessage());
			throw new CustomException("VIOLATION_UPDATE_CHALLAN_EXCEPTION", e.getMessage());
		}

	}

	/**
	*This method will fetch list of challans
	*
	* @param RequestInfoWrapper SearchCriteria
	* @return ResponseInfoWrapper containing list of challans
	* @throws CustomException VIOLATION_GET_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> getChallan(RequestInfoWrapper requestInfoWrapper) {
		log.info("Violation Service - Get Challan");
		try {

			EcSearchCriteria searchCriteria = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),
					EcSearchCriteria.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(searchCriteria, EcSearchCriteria.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.CHALLANGET);
		
			if(responseValidate.equals("")) 
			{

					List<Violation> violationPage = null;
		
					if (searchCriteria.getLimit() == null) {
						searchCriteria.setLimit(DEFAULT_LIMIT);
					}
					if (searchCriteria.getLimit() == -1) {
						searchCriteria.setLimit(1_000_000);
					}
					List<String> roleCodes = new LinkedList<>();
					requestInfoWrapper.getRequestInfo().getUserInfo().getRoles().forEach(role -> {
						roleCodes.add(role.getCode());
					});
					if (roleCodes.contains("challanHOD") && !roleCodes.contains("challanSM") && !roleCodes.contains("challanSI")) {
						if(!searchCriteria.getAction().equals("auctionChallan") && !searchCriteria.getAction().equals("challanSM") && searchCriteria.getSearchText().equals(""))
						{
							violationPage = repository.getChallanForHOD(searchCriteria);
							
						}
						else if(searchCriteria.getAction().equals("auctionChallan") && searchCriteria.getSearchText().equals(""))
						{
							violationPage = repository.getChallanForAuctionHOD(searchCriteria);
						}
						else
						{
							violationPage = repository.getChallan(searchCriteria);
						}
						
					} else {
		
						violationPage = repository.getChallan(searchCriteria);
					}
		
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(violationPage).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("VIOLATION_GET_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Violation Service - Get Violation Exception"+e.getMessage());
			throw new CustomException("VIOLATION_GET_EXCEPTION", e.getMessage());
		}
	}

	/**
	*This method will be used to add payment entries
	* @param RequestInfoWrapper containing payment object
	* @return HTTP status on success
	* @throws CustomException STOREITEM_ADDPAYMENT_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> addPayment(RequestInfoWrapper requestInfoWrapper) {
		log.info("Violation Service - Add Payment");
		try {
			EcPayment ecPayment = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), EcPayment.class);
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(ecPayment, EcPayment.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONCREATE);
		
			if(responseValidate.equals("")) 
			{
				
					ecPayment.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					ecPayment.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					ecPayment.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					ecPayment.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					ecPayment.setPaymentReceiptUuid(UUID.randomUUID().toString());
					ecPayment.setIsActive(true);
					repository.addPayment(ecPayment);
		
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(ecPayment).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("STOREITEM_ADDPAYMENT_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Violation Service - Add Payment Exception"+e.getMessage());
			throw new CustomException("STOREITEM_ADDPAYMENT_EXCEPTION", e.getMessage());

		}
	}
	
	/**
	*This method will be used to send email notification
	* @param RequestInfoWrapper containing NotificationTemplate object
	* @return HTTP status on success
	* @throws CustomException VIOLATION_NOTIFICATION_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> sendNotification(RequestInfoWrapper requestInfoWrapper) {
		log.info("Violation Service - Send Notification");
		try {
			NotificationTemplate email = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), NotificationTemplate.class)	;
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(email, NotificationTemplate.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONCREATE);
		
			if(responseValidate.equals("")) 
			{
			
					if(config.getEchallanNotificationFlag().equalsIgnoreCase("ON"))
					{			
						if(email.getSubject().equalsIgnoreCase("Challan Issued"))
						{
						String body=email.getBody().replace("<Link>", config.getLoginUrl());
						email.setBody(body);
						}
						producer.push(config.getEmailNotificationTopic(), email);			
					}			
					return new ResponseEntity<>(
							ResponseInfoWrapper.builder().responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
									.responseBody(email).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("VIOLATION_NOTIFICATION_EXCEPTION", responseValidate);
			}
			
		} catch (Exception e) {
			log.error("Violation Service - Send Notification Exception"+e.getMessage());
			throw new CustomException("VIOLATION_NOTIFICATION_EXCEPTION", e.getMessage());

		}
	}
	
	/**
	*This method will fetch list of challans
	*
	* @param RequestInfoWrapper SearchCriteria
	* @return ResponseInfoWrapper containing list of challans
	* @throws CustomException VIOLATION_SEARCH_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> getSearchChallan(RequestInfoWrapper requestInfoWrapper) {
		log.info("Violation Service - Get Search Challan");
		try {

			Violation violation = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),Violation.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(violation, Violation.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.VENDDORGET);
		
			if(responseValidate.equals("")) 
			{

					List<Violation> violationPage = null;
					
						violationPage = repository.getSearchChallan(violation);			
		
					return new ResponseEntity<>(ResponseInfoWrapper.builder()
							.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(violationPage).build(),
							HttpStatus.OK);
			}
			else
			{
				throw new CustomException("VIOLATION_SEARCH_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Violation Service - Search Violation Exception"+e.getMessage());
			throw new CustomException("VIOLATION_SEARCH_EXCEPTION", e.getMessage());
		}
	}

}
