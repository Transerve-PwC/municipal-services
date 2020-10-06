package org.egov.cpt.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.cpt.CSPropertyApplication;
import org.egov.cpt.config.TestConfiguration;
import org.egov.cpt.models.AccountStatementCriteria;
import org.egov.cpt.models.Property;
import org.egov.cpt.service.AccountStatementExcelGenerationService;
import org.egov.cpt.service.PropertyService;
import org.egov.cpt.util.PropertyUtil;
import org.egov.cpt.util.ResponseInfoFactory;
import org.egov.cpt.web.contracts.AccountStatementRequest;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.egov.cpt.web.contracts.PropertyResponse;
import org.egov.cpt.web.controllers.PropertyController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CSPropertyApplication.class)
@AutoConfigureMockMvc
@Import(TestConfiguration.class)
public class PropertyControllerTest {

	private String timeZone = "UTC";

	@InjectMocks
	PropertyController propertyController;

	@Mock
	PropertyService propertyService;

	@Mock
	PropertyUtil propertyutil;

	@Mock
	private AccountStatementExcelGenerationService accountStatementExcelGeneration;

	@Mock
	private ResponseInfoFactory responseInfoFactory;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	}

	@Test
	public void CreateRequestTest() throws Exception {
		List<Property> properties = new ArrayList<Property>();
		Property property = Property.builder().id("1").tenantId("ch.chandigarh").transitNumber("trns001")
				.colony("colony").masterDataAction("masterDataAction").masterDataState("masterDataState").build();

		properties.add(property);

		PropertyRequest propertyRequest = new PropertyRequest();
		propertyRequest.setProperties(properties);
		Mockito.when(responseInfoFactory.createResponseInfoFromRequestInfo(Mockito.any(), Mockito.anyBoolean()))
		.thenReturn(buildResponseInfo());
		Mockito.when(propertyService.createProperty(Mockito.any())).thenReturn(properties);
		ResponseEntity<PropertyResponse> response = propertyController.create(propertyRequest);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("1", response.getBody().getProperties().get(0).getId());
		assertEquals("trns001", response.getBody().getProperties().get(0).getTransitNumber());
		assertEquals("ch.chandigarh", response.getBody().getProperties().get(0).getTenantId());
		assertEquals("colony", response.getBody().getProperties().get(0).getColony());
		assertEquals("masterDataState", response.getBody().getProperties().get(0).getMasterDataState());
		assertEquals("masterDataAction", response.getBody().getProperties().get(0).getMasterDataAction());
		assertEquals("Rainmaker", response.getBody().getResponseInfo().getApiId());
	}

	@Test
	public void generateAccountStatementExcelTestWithToDate() {
		List<HashMap<String, String>> list = new ArrayList<>();
		HashMap<String, String> hashmap = new HashMap<>();
		hashmap.put("fileStoreId", "1b53c");
		hashmap.put("tenantId", "ch.chandigarh");
		list.add(hashmap);
		Mockito.when(accountStatementExcelGeneration.generateAccountStatementExcel(Mockito.any(), Mockito.any()))
				.thenReturn(list);
		ResponseEntity<List<HashMap<String, String>>> response = propertyController
				.generateAccountStatementExcel(buildAccountStatementRequest());
		assertEquals(response.getBody(), list);
	}

	@Test
	public void generateAccountStatementExcelTestWithoutToDate() {
		List<HashMap<String, String>> list = new ArrayList<>();
		HashMap<String, String> hashmap = new HashMap<>();
		hashmap.put("fileStoreId", "1b53c");
		hashmap.put("tenantId", "ch.chandigarh");
		list.add(hashmap);
		Mockito.when(accountStatementExcelGeneration.generateAccountStatementExcel(Mockito.any(), Mockito.any()))
				.thenReturn(list);
		ResponseEntity<List<HashMap<String, String>>> response = propertyController
				.generateAccountStatementExcel(buildAccountStatementRequestWithutToDate());
		assertEquals(response.getBody(), list);
	}

	private AccountStatementRequest buildAccountStatementRequest() {
		AccountStatementRequest accountStatementRequest = new AccountStatementRequest();
		AccountStatementCriteria criteria = new AccountStatementCriteria();
		criteria.setPropertyid("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		criteria.setFromDate(1567775475000L);
		criteria.setToDate(1667775475000L);
		accountStatementRequest.setCriteria(criteria);
		return accountStatementRequest;
	}

	private AccountStatementRequest buildAccountStatementRequestWithutToDate() {
		AccountStatementRequest accountStatementRequest = new AccountStatementRequest();
		AccountStatementCriteria criteria = new AccountStatementCriteria();
		criteria.setPropertyid("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		criteria.setFromDate(1567775475000L);
		accountStatementRequest.setCriteria(criteria);
		return accountStatementRequest;
	}

	private ResponseInfo buildResponseInfo() {
		return ResponseInfo.builder().apiId("Rainmaker").build();
	}
}
