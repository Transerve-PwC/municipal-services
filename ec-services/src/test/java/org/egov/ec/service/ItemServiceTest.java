package org.egov.ec.service;

import java.util.ArrayList;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.ec.config.EcConstants;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.ItemRepository;
import org.egov.ec.service.DeviceSourceService;
import org.egov.ec.service.ItemService;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.AuditDetails;
import org.egov.ec.web.models.FineMaster;
import org.egov.ec.web.models.ItemMaster;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.workflow.WorkflowIntegrator;
import org.egov.tracer.model.CustomException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class ItemServiceTest {
	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	@InjectMocks
	private ItemMaster itemMaster;

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
	public void testGetItem() throws Exception {
		ItemMaster itemMaster = ItemMaster.builder().itemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(itemMaster)
				.requestInfo(RequestInfo.builder().userInfo(User.builder().tenantId("ch").build()).build()).build();
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), ItemMaster.class)).thenReturn(itemMaster);
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), ItemMaster.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.ITEMMASTERGET)).thenReturn("");
		Mockito.when(itemRepository.getItem(infoWrapper)).thenReturn(new ArrayList<ItemMaster>());
		Assert.assertEquals(HttpStatus.OK, itemService.getItem(infoWrapper).getStatusCode());
	} 
	
	
	  @Test public void testCreateItem() {
	  
		  ItemMaster itemMaster =ItemMaster.builder().itemUuid("aasdjiasdu8ahs89asdy8a9h").build(); 
		  AuditDetails auditDetails =AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy
				  ("1") .lastModifiedTime(15645455L).build();
		  RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(itemMaster).build();
		  Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(),ItemMaster.class)) .thenReturn(itemMaster);
		  
		  Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), ItemMaster.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.ITEMMASTERCREATE)).thenReturn("");
	  
	  Assert.assertEquals(HttpStatus.OK,itemService.addItem(infoWrapper,"dfwdv").getStatusCode());
	  
	  }
	  
	  @Test(expected = CustomException.class)
	   public void testCreateItemException() {
		  
		  ItemMaster itemMaster =ItemMaster.builder().itemUuid("aasdjiasdu8ahs89asdy8a9h").build(); 
		  RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(itemMaster).build();
	  Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(),ItemMaster.class)) .thenReturn(itemMaster);
	  itemService.addItem(infoWrapper,"");
	  }
	  
	  @Test public void testUpdateItem() {
	  
		  ItemMaster itemMaster =ItemMaster.builder().itemUuid("aasdjiasdu8ahs89asdy8a9h").build(); 
		  AuditDetails auditDetails =AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy
		  ("1") .lastModifiedTime(15645455L).build(); 
		  RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(itemMaster).build();
		  Gson gson = new Gson();
			String payloadData = gson.toJson(infoWrapper.getRequestBody(), ItemMaster.class);
			Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.ITEMMASTERCREATE)).thenReturn("");
		  Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(),ItemMaster.class)) .thenReturn(itemMaster);
		  
		  Assert.assertEquals(HttpStatus.OK,itemService.updateItem(infoWrapper).getStatusCode());
		  }
	  
	  @Test(expected = CustomException.class)
	   public void testUpdateItemException() {
		  
		  ItemMaster itemMaster =ItemMaster.builder().itemUuid("aasdjiasdu8ahs89asdy8a9h").build(); 
		  RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(itemMaster).build();
	  Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(),ItemMaster.class)) .thenReturn(itemMaster);
	  itemService.updateItem(infoWrapper);
	  }
	 
}
