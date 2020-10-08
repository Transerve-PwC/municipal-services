package org.egov.cpt.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.models.AccountStatementCriteria;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.OwnerDetails;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyDetails;
import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.models.RentAccountStatement.Type;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.util.FileStoreUtils;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.web.contracts.AccountStatementResponse;
import org.egov.tracer.model.CustomException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AccountStatementExcelGenerationServiceTest {

	@InjectMocks
	AccountStatementExcelGenerationService accountStatementExcelGenerationService;

	@Mock
	private PropertyRepository propertyRepository;

	@Mock
	private PropertyService propertyService;

	@Mock
	private FileStoreUtils fileStoreUtils;

	@Mock
	private NotificationUtil notificationUtil;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		accountStatementExcelGenerationService = new AccountStatementExcelGenerationService(propertyRepository,
				propertyService, fileStoreUtils, notificationUtil);
	}

	@Test
	public void generateAccountStatementExcelTest() {
		List<HashMap<String, String>> list = new ArrayList<>();
		HashMap<String, String> hashmap = new HashMap<>();
		hashmap.put("fileStoreId", "1b53c");
		hashmap.put("tenantId", "ch.chandigarh");
		list.add(hashmap);
		Mockito.when(fileStoreUtils.fetchFileStoreId(Mockito.any(), Mockito.any())).thenReturn(list);
		Mockito.when(propertyService.searchPayments(Mockito.any(), Mockito.any()))
				.thenReturn(buildAccountStatementResponse());
		Mockito.when(propertyRepository.getProperties(Mockito.any())).thenReturn(buildPropertyList());
		accountStatementExcelGenerationService.generateAccountStatementExcel(buildAccountStatementCriteria(),
				buildRequestInfo());
	}

	@Test
	public void generateAccountStatementExcelWithExceptionTest() {
		exception.expect(CustomException.class);
		Mockito.when(propertyRepository.getProperties(Mockito.any())).thenReturn(buildPropertyList());
		accountStatementExcelGenerationService.generateAccountStatementExcel(buildAccountStatementCriteria(),
				buildRequestInfo());
	}

	private AccountStatementCriteria buildAccountStatementCriteria() {
		AccountStatementCriteria accountStatementCriteria = new AccountStatementCriteria();
		accountStatementCriteria.setPropertyid("w2fed7b6-eb32-4b56-99d5-0361285e43ut");
		accountStatementCriteria.setFromDate(1567775476000L);
		accountStatementCriteria.setToDate(1599397876000L);
		return accountStatementCriteria;
	}

	private RequestInfo buildRequestInfo() {
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setApiId("Rainmaker");
		requestInfo.setVer(".01");
		requestInfo.setAction("search");
		requestInfo.setAuthToken("ea108480-b4d0-4c2f-909f-acce3fbf407d");
		requestInfo.setMsgId("20170310130900|en_IN");
		return requestInfo;
	}

	private AccountStatementResponse buildAccountStatementResponse() {
		AccountStatementResponse accountStatementResponse = new AccountStatementResponse();
		List<RentAccountStatement> rentAccountStatementList = new ArrayList<>();
		RentAccountStatement rentAccountStatement1 = new RentAccountStatement();
		rentAccountStatement1.setAmount(100D);
		rentAccountStatement1.setDate(1567775475000L);
		rentAccountStatement1.setRemainingBalance(50D);
		rentAccountStatement1.setRemainingInterest(2D);
		rentAccountStatement1.setRemainingPrincipal(50D);
		rentAccountStatement1.setType(Type.D);
		RentAccountStatement rentAccountStatement2 = new RentAccountStatement();
		rentAccountStatement2.setAmount(200D);
		rentAccountStatement2.setDate(1567775475100L);
		rentAccountStatement2.setRemainingBalance(50D);
		rentAccountStatement2.setRemainingInterest(2D);
		rentAccountStatement2.setRemainingPrincipal(50D);
		rentAccountStatement2.setType(Type.C);
		rentAccountStatementList.add(rentAccountStatement1);
		rentAccountStatementList.add(rentAccountStatement2);
		accountStatementResponse.setRentAccountStatements(rentAccountStatementList);
		return accountStatementResponse;
	}

	private List<Property> buildPropertyList() {
		Property property = new Property();
		property.setId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		property.setTransitNumber("1234");
		property.setTenantId("ch.chandigarh");
		property.setArea("pato plaza");
		PropertyDetails propertyDetails = new PropertyDetails();
		propertyDetails.setInterestRate(24D);
		propertyDetails.setRentIncrementPercentage(5D);
		propertyDetails.setRentIncrementPeriod(1);
		property.setPropertyDetails(propertyDetails);
		OwnerDetails ownerDetails = new OwnerDetails();
		ownerDetails.setName("John");
		ownerDetails.setAllotmentStartdate(1567775475000L);
		Owner owner = new Owner();
		owner.setId("d1fed7b6-eb22-4b56-99d4-0361285e42dr");
		owner.setOwnerDetails(ownerDetails);
		property.setOwners(Collections.singletonList(owner));
		return Collections.singletonList(property);
	}
}
