package org.egov.ec.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.ec.config.EcConstants;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.StoreItemRegisterRepository;
import org.egov.ec.repository.VendorRegistrationRepository;
import org.egov.ec.service.DeviceSourceService;
import org.egov.ec.service.VendorRegistrationService;
import org.egov.ec.service.validator.CustomBeanValidator;
import org.egov.ec.web.models.Auction;
import org.egov.ec.web.models.AuditDetails;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.StoreItemRegister;
import org.egov.ec.web.models.VendorRegistration;
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
public class VendorRegistrationServiceTest {

	@Mock
	private VendorRegistrationRepository repository;

	@InjectMocks
	private VendorRegistrationService service;

	/*@Mock
	private ResponseInfoFactory responseInfoFactory;*/

	@InjectMocks
	private VendorRegistration vendorMaster;

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
	public void testGeVendorMaster() throws Exception {
		VendorRegistration vendorMaster = VendorRegistration.builder().vendorUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(vendorMaster)
				.requestInfo(RequestInfo.builder().userInfo(User.builder().tenantId("ch").build()).build()).build();
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), VendorRegistration.class)).thenReturn(vendorMaster);
		
		EcSearchCriteria searchCriteria = EcSearchCriteria.builder().build();
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), EcSearchCriteria.class)).thenReturn(searchCriteria);
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(searchCriteria, EcSearchCriteria.class);

		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.VENDDORGET)).thenReturn("");
		
		Mockito.when(repository.getVendor(searchCriteria)).thenReturn(new ArrayList<VendorRegistration>());
		Assert.assertEquals(HttpStatus.OK, service.getVendor(infoWrapper).getStatusCode());
	}

	@Test(expected = CustomException.class)
	public void testGetVendorException() throws CustomException {
		service.getVendor(null);
	}

	@Test
	public void testCreateVendorRegistration() {
		List<VendorRegistration> vendorRegistrationList=new ArrayList<VendorRegistration>();
		VendorRegistration vendorMaster1 = VendorRegistration.builder().name("aasdjiasdu8ahs89asdy8da9h").build();
		ArrayList<VendorRegistration> vendorMasterlist=new ArrayList<>();
		vendorMasterlist.add(vendorMaster1);
		
		VendorRegistration vendorMaster = VendorRegistration.builder().vendorUuid("aasdjiasdu8ahs89asdy8a9h").vendorRegistrationList(vendorRegistrationList).build();
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(vendorMaster).build();
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), VendorRegistration.class)).thenReturn(vendorMaster);
		Gson gson = new Gson();
		String payloadData = gson.toJson(infoWrapper.getRequestBody(), VendorRegistration.class);
//		vendorRegistrationList.add(vendorMaster);
//		vendorMaster.setVendorRegistrationList(vendorMasterlist);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.VENDDORCREATE)).thenReturn("");
		Assert.assertEquals(HttpStatus.OK, service.createVendor(infoWrapper,"dgdsd").getStatusCode());

	}

	@Test(expected = CustomException.class)
	public void testCreateVendorRegistrationException() {

		StoreItemRegister storeItemnMaster = StoreItemRegister.builder().storeItemUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(storeItemnMaster).build();
		
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), StoreItemRegister.class)).thenReturn(storeItemnMaster);
		VendorRegistration vendorRegistration = new VendorRegistration();
		RequestInfoWrapper infoWrappernew = RequestInfoWrapper.builder().requestBody(storeItemnMaster).build();
		Mockito.when(objectMapper.convertValue(infoWrappernew.getRequestBody(), VendorRegistration.class)).thenReturn(vendorRegistration);
		Gson gson = new Gson();
		String payloadData = gson.toJson(vendorRegistration, VendorRegistration.class);
		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.STOREREGISTRATION)).thenReturn("");
		service.createVendor(infoWrappernew,"");
	}

	@Test
	public void testUpdateVendorRegistration() {
//		List<VendorRegistration> vendorRegistrationList=new ArrayList<VendorRegistration>();
//		VendorRegistration vendorMaster = VendorRegistration.builder().vendorUuid("aasdjiasdu8ahs89asdy8a9h").vendorRegistrationList(null).build();
		List<VendorRegistration> vendorRegistrationList=new ArrayList<VendorRegistration>();
		VendorRegistration vendorMaster1 = VendorRegistration.builder().name("aasdjiasdu8ahs89asdy8da9h").build();
//		ArrayList<VendorRegistration> vendorMasterlist=new ArrayList<>();
//		vendorMasterlist.add(vendorMaster1);
		
		VendorRegistration vendorMaster = VendorRegistration.builder().vendorUuid("aasdjiasdu8ahs89asdy8a9h").vendorRegistrationList(vendorRegistrationList).build();
		
		AuditDetails auditDetails = AuditDetails.builder().createdBy("1").createdTime(1546515646L).lastModifiedBy("1")
				.lastModifiedTime(15645455L).build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().auditDetails(auditDetails).requestBody(vendorMaster)
				.build();
//		vendorRegistrationList.add(vendorMaster);
//		vendorMaster.setVendorRegistrationList(vendorRegistrationList);
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), VendorRegistration.class)).thenReturn(vendorMaster);
		
		Gson gson = new Gson();
		String payloadData = gson.toJson(vendorMaster, VendorRegistration.class);

		Mockito.when(wfIntegrator.validateJsonAddUpdateData(payloadData,EcConstants.VENDDORUPDATE)).thenReturn("");

		Assert.assertEquals(HttpStatus.OK, service.updateVendor(infoWrapper).getStatusCode());
	}

	@Test(expected = CustomException.class)
	public void testUpdateVendorRegistrationException() {

		VendorRegistration vendorMaster = VendorRegistration.builder().vendorUuid("aasdjiasdu8ahs89asdy8a9h").build();
		RequestInfoWrapper infoWrapper = RequestInfoWrapper.builder().requestBody(vendorMaster).build();
		Mockito.when(objectMapper.convertValue(infoWrapper.getRequestBody(), VendorRegistration.class)).thenReturn(vendorMaster);
		service.updateVendor(infoWrapper);
	}
	

}
