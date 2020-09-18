package org.egov.ec.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.StoreItemRegisterRepository;
import org.egov.ec.service.DeviceSourceService;
import org.egov.ec.service.StoreItemRegisterService;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.Auction;
import org.egov.ec.web.models.AuditDetails;
import org.egov.ec.web.models.Document;
import org.egov.ec.web.models.EcPayment;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.StoreItemRegister;
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
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class StoreItemRegisterServiceTest {

	@Mock
	private StoreItemRegisterRepository repository;

	@InjectMocks
	private StoreItemRegisterService service;

	/*@Mock
	private ResponseInfoFactory responseInfoFactory;*/

	@InjectMocks
	private StoreItemRegister storeItemnMaster;

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
	
	@Mock
	Document document;

	@Test
	public void testGeStoreItemRegister() throws Exception {
		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		org.egov.common.contract.request.User userInfo = new org.egov.common.contract.request.User();
		userInfo.setTenantId("ch.chandigarh");
		userInfo.setRoles(Arrays.asList(Role.builder().code("challanHOD").name("challanHOD").build()));
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(storeItemnMaster)
				.requestInfo(RequestInfo.builder().userInfo(userInfo).build()).build();
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);
		
		EcSearchCriteria searchCriteria = EcSearchCriteria.builder().build();
		searchCriteria.setChallanId("dfghjvbnm");
		searchCriteria.setSearchText("dfghjcvb");
		
		RequestInfoWrapper getinfoWrapper = RequestInfoWrapper.builder().requestBody(searchCriteria)
				.requestInfo(RequestInfo.builder().userInfo(userInfo).build()).build();
		
		Mockito.when(objectMapper.convertValue(getinfoWrapper.getRequestBody(), EcSearchCriteria.class)).thenReturn(searchCriteria);
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(getinfoWrapper.getRequestBody(), EcSearchCriteria.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.REPORTAGEINGGET)).thenReturn("");
		
		Mockito.when(repository.getStoreItemRegister(searchCriteria)).thenReturn(new ArrayList<StoreItemRegister>());
		Assert.assertEquals(HttpStatus.OK, service.getStoreRegisterItem(getinfoWrapper).getStatusCode());
	}
	
	@Test
	public void testGeStoreItemRegister_1() throws Exception {
		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		org.egov.common.contract.request.User userInfo = new org.egov.common.contract.request.User();
		userInfo.setTenantId("ch.chandigarh");
		userInfo.setRoles(Arrays.asList(Role.builder().code("challanEAO").name("challanEAO").build()));
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(storeItemnMaster)
				.requestInfo(RequestInfo.builder().userInfo(userInfo).build()).build();
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);
		
		EcSearchCriteria searchCriteria = EcSearchCriteria.builder().build();
		searchCriteria.setChallanId("dfghjvbnm");
		searchCriteria.setSearchText("dfghjcvb");
		
		RequestInfoWrapper getinfoWrapper = RequestInfoWrapper.builder().requestBody(searchCriteria)
				.requestInfo(RequestInfo.builder().userInfo(userInfo).build()).build();
		
		Mockito.when(objectMapper.convertValue(getinfoWrapper.getRequestBody(), EcSearchCriteria.class)).thenReturn(searchCriteria);
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(getinfoWrapper.getRequestBody(), EcSearchCriteria.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.REPORTAGEINGGET)).thenReturn("");
		
		Mockito.when(repository.getStoreItemRegister(searchCriteria)).thenReturn(new ArrayList<StoreItemRegister>());
		Assert.assertEquals(HttpStatus.OK, service.getStoreRegisterItem(getinfoWrapper).getStatusCode());
	}

	@Test(expected = CustomException.class)
	public void testGetStoreItemRegisterException() throws CustomException {
		service.getStoreRegisterItem(null);
	}

	@Test
	public void testCreateStoreItemRegister() {
		
		StoreItemRegister StoreItemRegister2 = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8da9h").build();
		ArrayList<StoreItemRegister> StoreItemRegister2List=new ArrayList<>();
		StoreItemRegister2List.add(StoreItemRegister2);
		
		List<StoreItemRegister> storeItemRegister=new ArrayList<StoreItemRegister>();
		List<Document> document=Arrays.asList(Document.builder().documentUuid("hbhjbhjbjb").documentType("hbhbj").build());
		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").document(document).build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(storeItemnMaster)
				.build();
		storeItemRegister.add(storeItemnMaster); 
		storeItemnMaster.setStoreItemRegister(storeItemRegister);
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);
		
		StoreItemRegister storeItemnMasternew = StoreItemRegister.builder().document(document).storeItemRegister(StoreItemRegister2List).storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrappernew = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(storeItemnMasternew)
				.build();
		Mockito.when(objectMapper.convertValue(infoWrappernew.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMasternew);
		Gson gson = new Gson();
		String payloadData = gson.toJson(storeItemnMasternew, StoreItemRegister.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION)).thenReturn("");
		
		when(wfIntegrator.callWorkFlow(Matchers.any(ProcessInstanceRequest.class)))
		
		.thenReturn(ResponseInfo.builder().status("successful").build());
		Assert.assertEquals(HttpStatus.OK, service.createStoreRegisterItem(infoWrappernew,"vhbk").getStatusCode());

	}

	@Test(expected = CustomException.class)
	public void testCreateStoreItemRegisterException() {

		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(storeItemnMaster).build();
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);
		service.createStoreRegisterItem(infoWrapper,"");
	}

	@Test
	public void testUpdateStoreItemRegister() {
		
		StoreItemRegister StoreItemRegister2 = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8da9h").build();
		ArrayList<StoreItemRegister> StoreItemRegister2List=new ArrayList<>();
		StoreItemRegister2List.add(StoreItemRegister2);
		List<StoreItemRegister> storeItemRegister=new ArrayList<StoreItemRegister>();
		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(storeItemnMaster)
				.build();
		storeItemRegister.add(storeItemnMaster);
		storeItemnMaster.setStoreItemRegister(storeItemRegister);
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);

		StoreItemRegister storeItemnMasternew = StoreItemRegister.builder().storeItemRegister(StoreItemRegister2List).storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrappernew = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(storeItemnMasternew)
				.build();
		Mockito.when(objectMapper.convertValue(infoWrappernew.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMasternew);
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrappernew.getRequestBody(), StoreItemRegister.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION)).thenReturn("");
		when(wfIntegrator.callWorkFlow(Matchers.any(ProcessInstanceRequest.class)))
		.thenReturn(ResponseInfo.builder().status("successful").build());
		Assert.assertEquals(HttpStatus.OK, service.updateStoreRegisterItem(infoWrappernew).getStatusCode());
	}

	@Test(expected = CustomException.class)
	public void testUpdateStoreItemRegisterException() {

		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(storeItemnMaster).build();
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);
		service.updateStoreRegisterItem(infoWrapper);
	}
	
	//
	
	@Test
	public void testUpdateupdateStorePayment() {

		EcPayment paymentMaster = EcPayment.builder().paymentUuid("aasdjiasdu8ahs89asdy8a9h").build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(paymentMaster)
				.build();
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), EcPayment.class)).thenReturn(paymentMaster);
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), EcPayment.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION)).thenReturn("");
		when(wfIntegrator.callWorkFlow(Matchers.any(ProcessInstanceRequest.class)))
		.thenReturn(ResponseInfo.builder().status("successful").build());
		Assert.assertEquals(HttpStatus.OK, service.updateStorePayment(infoWrapper).getStatusCode());
	}

	@Test(expected = CustomException.class)
	public void testUpdateupdateStorePaymentException() {

		EcPayment paymentMaster = EcPayment.builder().paymentUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(paymentMaster).build();
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), EcPayment.class)).thenReturn(paymentMaster);
		service.updateStorePayment(infoWrapper);
	}

}
