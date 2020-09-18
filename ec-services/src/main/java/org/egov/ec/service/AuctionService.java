package org.egov.ec.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.repository.AuctionRepository;
import org.egov.ec.repository.ViolationRepository;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.Auction;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
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
public class AuctionService {

	private final ObjectMapper objectMapper;
	private WorkflowIntegrator wfIntegrator;
	private CustomBeanValidator validate;
	private AuctionRepository repository;
	private ViolationRepository violationRepository;
	private DeviceSourceService deviceSource;

	@Autowired
	public AuctionService(WorkflowIntegrator wfIntegrator, ObjectMapper objectMapper,
			ViolationRepository violationRepository, AuctionRepository repository, CustomBeanValidator validate,
			DeviceSourceService deviceSource) {
		this.objectMapper = objectMapper;
		this.wfIntegrator = wfIntegrator;
		this.repository = repository;
		this.validate = validate;
		this.deviceSource = deviceSource;
		this.violationRepository = violationRepository;
	}
	
	/**
	*This method will add entries in auction flow which will be eligible for approval by apellate authority.
	*
	* @param RequestInfoWrapper List of auction items
	* @param requestHeader for saving device source information
	* @return HTTP status on success
	* @throws CustomException AUCTION_ADD_EXCEPTION
	*/
	 /**
     * Adds entries in auction for approval
     *
     * @param RequestInfoWrapper List of auction items
     * @param requestHeader for saving device information 
     * @return HTTP status on success
     */
	
	public ResponseEntity<ResponseInfoWrapper> addAuction(RequestInfoWrapper requestInfoWrapper, String requestHeader) {
		log.info("Auction Service - add Auction Method");
		
		String responseValidate = "";
		try {
			Auction auction = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), Auction.class);

			Gson gson = new Gson();
			String payloadData = gson.toJson(auction, Auction.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONCREATE);
		
			if(responseValidate.equals("")) 
			{
				String auctionUuid = UUID.randomUUID().toString();
				auction.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
				auction.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
				auction.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
				auction.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
				auction.setAuctionUuid(auctionUuid);
				auction.setIsActive(true);
				auction.setStatus(EcConstants.STATUS_PENDING);
				String sourceUuid = deviceSource.saveDeviceDetails(requestHeader, "addAuctionEvent", auction.getTenantId(),
						requestInfoWrapper.getAuditDetails());
				auction.setSourceUuid(sourceUuid);
	
				auction.getAuctionList().stream().forEach((c) -> {
					c.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
					c.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
					c.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					c.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					c.setIsActive(true);
					c.setViolationUuid(auction.getViolationUuid());
					c.setChallanUuid(auction.getChallanUuid());
					c.setTenantId(auction.getTenantId());
					c.setAuctionUuid(auctionUuid);
					c.setAuctionDetailUuid(UUID.randomUUID().toString());
	
				});
				ProcessInstance processInstance = new ProcessInstance();
				processInstance.setBusinessId(auctionUuid);
				processInstance.setTenantId(auction.getTenantId());
				processInstance.setBusinessService(EcConstants.WORKFLOW_AUCTION);
				processInstance.setAction(EcConstants.STATUS_PENDING);
				processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
				List<ProcessInstance> processList = Arrays.asList(processInstance);
	
				ResponseInfo response = wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder()
						.processInstances(processList).requestInfo(requestInfoWrapper.getRequestInfo()).build());
	
				validate.validateFields(auction.getAuctionList());
	
				if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
					repository.saveAuction(auction);
				}
	
				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
						.responseBody(auction).build(), HttpStatus.OK);
			}
			else
			{
				throw new CustomException("AUCTION_ADD_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Auction Service - Add Auction Exception "+e.getMessage());
			throw new CustomException("AUCTION_ADD_EXCEPTION", e.getMessage());			
		}
	}

	/**
	*This method will update status of auction workflow and update store depending upon approval status
	*
	* @param RequestInfoWrapper List of auction items
	* @return HTTP status on success
	* @throws CustomException AUCTION_UPDATE_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> updateAuction(RequestInfoWrapper requestInfoWrapper) {
		log.info("Auction Service - Update Auction");
		try {
			
			String responseValidate = "";
			Auction auction = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), Auction.class);
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(auction, Auction.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONUPDATE);
		
			if(responseValidate.equals("")) 
			{
		
				auction.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
				auction.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
				ProcessInstance processInstance = new ProcessInstance();
				processInstance.setBusinessId(auction.getAuctionUuid());
				processInstance.setTenantId(auction.getTenantId());
				processInstance.setBusinessService(EcConstants.WORKFLOW_AUCTION);
				processInstance.setAction(auction.getStatus());
				processInstance.setModuleName(EcConstants.WORKFLOW_MODULE);
				List<ProcessInstance> processList = Arrays.asList(processInstance);
	
				ResponseInfo response = wfIntegrator.callWorkFlow(ProcessInstanceRequest.builder()
						.processInstances(processList).requestInfo(requestInfoWrapper.getRequestInfo()).build());
	
				if (response != null && response.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
					if (auction.getStatus().equalsIgnoreCase(EcConstants.ACTION_APPROVE)) {
	
						auction.setStatus(EcConstants.STATUS_APPROVED);
	
						repository.updateAuction(auction);
					}
					if (auction.getStatus().equalsIgnoreCase(EcConstants.ACTION_REJECT)) {
						log.info("Auction Rejected Case");
						auction.setStatus(EcConstants.STATUS_REJECTED);
						auction.getAuctionList().stream().forEach((c) -> {
							c.setCreatedBy(requestInfoWrapper.getAuditDetails().getCreatedBy());
							c.setCreatedTime(requestInfoWrapper.getAuditDetails().getCreatedTime());
							c.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
							c.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
							// c.setIsActive(false);
	
						});
						repository.updateAuctionRejection(auction);
					}
	
				}
	
				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
						.responseBody(auction).build(), HttpStatus.OK);
				}
			else
			{
				throw new CustomException("AUCTION_UPDATE_EXCEPTION", responseValidate);
			}

		} catch (Exception e) {
			log.error("Auction Service - Update Auction Exception "+e.getMessage());
			throw new CustomException("AUCTION_UPDATE_EXCEPTION", e.getMessage());
		}
	}

	/**
	*This method will fetch auction details against specific challan 
	*
	* @param RequestInfoWrapper SearchCriteria
	* @return List of Auction details
	* @throws CustomException AUCTION_GET_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> getAuction(RequestInfoWrapper requestInfoWrapper) {
		log.info("Auction Service - Get Auction");
		try {
			EcSearchCriteria searchCriteria = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),
					EcSearchCriteria.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(searchCriteria, EcSearchCriteria.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONGET);
		
			if(responseValidate.equals("")) 
			{
		
			
				List<Auction> auction = repository.getAuction(searchCriteria);
	
				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
						.responseBody(auction).build(), HttpStatus.OK);	
			}
			else
			{
				throw new CustomException("AUCTION_GET_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Auction Service - Get Auction Exception"+e.getMessage());
			throw new CustomException("AUCTION_GET_EXCEPTION", e.getMessage());
		}
	}

	/**
	*This method will fetch list of challans pending for approval by HOD 
	*
	* @param RequestInfoWrapper SearchCriteria
	* @return List of challans
	* @throws CustomException AUCTION_GET_EXCEPTION
	*/
	public ResponseEntity<ResponseInfoWrapper> getAuctionChallan(RequestInfoWrapper requestInfoWrapper) {
		log.info("Auction Service - Get Auction Challan");
		try {
			Auction auction = objectMapper.convertValue(requestInfoWrapper.getRequestBody(), Auction.class);
			
			String responseValidate = "";
			
			Gson gson = new Gson();
			String payloadData = gson.toJson(auction, Auction.class);
			
			responseValidate = wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.CHALLANGET);
		
			if(responseValidate.equals("")) 
			{
		

			List<Auction> auctionList = repository.getAuctionChallan(auction);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build())
					.responseBody(auctionList).build(), HttpStatus.OK);
			}
			else
			{
				throw new CustomException("AUCTION_GET_EXCEPTION", responseValidate);
			}
		} catch (Exception e) {
			log.error("Auction Service - Get Auction Challan Exception"+e.getMessage());
			throw new CustomException("AUCTION_GET_EXCEPTION", e.getMessage());
		}
	}
}
