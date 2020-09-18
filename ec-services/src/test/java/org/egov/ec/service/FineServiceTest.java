package org.egov.ec.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.FineMasterRepository;
import org.egov.ec.service.DeviceSourceService;
import org.egov.ec.service.FineService;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.Auction;
import org.egov.ec.web.models.AuditDetails;
import org.egov.ec.web.models.FineMaster;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.workflow.ProcessInstanceRequest;
import org.egov.ec.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class FineServiceTest {
	@Mock
	private FineMasterRepository fineRepository;

	@InjectMocks
	private FineService fineService;

	@InjectMocks
	private FineMaster fineMaster;

	@Mock
	private Producer producer;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	WorkflowIntegrator wfIntegrator;

	@Mock
	CustomBeanValidator validate;
	
	@Mock
	DeviceSourceService deviceSourceService;

	@Test
	public void testGetFine() throws Exception {
		FineMaster fineMaster = FineMaster.builder().fineUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(fineMaster)
				.requestInfo(RequestInfo.builder().userInfo(User.builder().tenantId("ch").build()).build()).build();
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), FineMaster.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONCREATE)).thenReturn("");
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), FineMaster.class)).thenReturn(fineMaster);
		Mockito.when(fineRepository.getFine(infoWrapper)).thenReturn(new ArrayList<FineMaster>());
		Assert.assertEquals(HttpStatus.OK, fineService.getFine(infoWrapper).getStatusCode());
	}

	@Test
	public void testCreateFine() {

		FineMaster fineMaster = FineMaster.builder().fineUuid("aasdjiasdu8ahs89asdy8a9h").encroachmentType("jnjknjkn")
				.numberOfViolation("1").penaltyAmount("100").approvalStatus("xyz").isActive(false).tenantId("cjbkc")
				.storageCharges("50").build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(fineMaster)
				.build(); 
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), FineMaster.class);
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), FineMaster.class)).thenReturn(fineMaster);
		when(wfIntegrator.callWorkFlow(Matchers.any(ProcessInstanceRequest.class)))
				.thenReturn(ResponseInfo.builder().status("successful").build());
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.FINEMASTERCREATE)).thenReturn("");
		Assert.assertEquals(HttpStatus.OK, fineService.saveFine(infoWrapper,"hvhvhh").getStatusCode());

	}

	@Test(expected = CustomException.class)
	public void testCreateFineException() {

		FineMaster fineMaster = FineMaster.builder().fineUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(fineMaster).build();
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), FineMaster.class);
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), FineMaster.class)).thenReturn(fineMaster);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONCREATE)).thenReturn("");
		fineService.saveFine(infoWrapper,"");
	}

	@Test
	public void testUpdateFine() {

		FineMaster fineMaster = FineMaster.builder().fineUuid("aasdjiasdu8ahs89asdy8a9h").approvalStatus("APPROVE")
				.build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(fineMaster)
				.build();
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), FineMaster.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.FINEMASTERUPDATE)).thenReturn("");
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), FineMaster.class)).thenReturn(fineMaster);
		when(wfIntegrator.callWorkFlow(Matchers.any(ProcessInstanceRequest.class)))
				.thenReturn(ResponseInfo.builder().status("successful").build());

		Assert.assertEquals(HttpStatus.OK, fineService.updateFine(infoWrapper).getStatusCode());
	}

	@Test
	public void testUpdateFine_1() {

		FineMaster fineMaster = FineMaster.builder().fineUuid("aasdjiasdu8ahs89asdy8a9h").approvalStatus("REJECT")
				.build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(fineMaster)
				.build();
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), FineMaster.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.FINEMASTERUPDATE)).thenReturn("");
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), FineMaster.class)).thenReturn(fineMaster);
		when(wfIntegrator.callWorkFlow(Matchers.any(ProcessInstanceRequest.class)))
				.thenReturn(ResponseInfo.builder().status("successful").build());

		Assert.assertEquals(HttpStatus.OK, fineService.updateFine(infoWrapper).getStatusCode());
	}

	@Test(expected = CustomException.class)
	public void testUpdateFineException() {

		FineMaster fineMaster = FineMaster.builder().fineUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(fineMaster).build();
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), FineMaster.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.AUCTIONCREATE)).thenReturn("");
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), FineMaster.class)).thenReturn(fineMaster);
		fineService.updateFine(infoWrapper);
	}

}
