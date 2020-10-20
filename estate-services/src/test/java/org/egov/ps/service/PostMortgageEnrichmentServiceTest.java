package org.egov.ps.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.Application;
import org.egov.ps.model.EstateDocumentList;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PostMortgageEnrichmentServiceTest {

	@InjectMocks
	PostMortgageEnrichmentService postMortgageEnrichmentService;

	@Mock
	PropertyRepository propertyRepository;

	@Mock
	Util util;

	@Mock
	MDMSService mdmsservice;

	@Test
	public void postEnrichMortgageDetailsPositive() {
		try {
			// Step 1 prepare RequestInfo
			String requestInfoJson = "{\"apiId\":\"Rainmaker\",\"ver\":\"01\",\"action\":\"_create\",\"key\":\"\",\"msgId\":\"20170310130900|en_IN\",\"authToken\":\"833b0a57-bbc5-4194-a961-bdb3794fa284\",\"userInfo\":{\"tenantId\":\"ch\",\"id\":8,\"username\":\"any\",\"mobile\":\"8866581197\",\"email\":\"mineshbhadeshia@gmail.com\" }}";
			RequestInfo requestInfo = new Gson().fromJson(requestInfoJson, RequestInfo.class);
			requestInfo.getUserInfo().setUuid(UUID.randomUUID().toString());
			Mockito.when(util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true))
					.thenReturn(AuditDetails.builder().createdBy("testCreatedBy").createdTime(new Date().getTime())
							.lastModifiedBy("testModifyBy").lastModifiedTime(new Date().getTime()).build());

			// Step 2 mock Application
			String json_ = getFileContents("MortgageDetails_application.json");
			Type listType_ = new TypeToken<List<org.egov.ps.model.Application>>() {
			}.getType();
			List<Application> applications = new Gson().fromJson(json_, listType_);

			// Step 3 - Mortgage Document.json file master data
			String documentJson = "[{\"code\":\"SELF_ATTESTED_PHOTO_IDENTITY_PROOF\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"SELF_ATTESTED_PHOTO_IDENTITY_PROOF_DESCRIPTION\"},{\"code\":\"AFFIDAVIT_WITH_PHOTOGRAPH\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"AFFIDAVIT_WITH_PHOTOGRAPH_DESCRIPTION\"},{\"code\":\"INDEMNITY_BOND_OWNER\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"INDEMNITY_BOND_OWNER_DESCRIPTION\"},{\"code\":\"INDEMNITY_BOND_TRANSFEREE\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"INDEMNITY_BOND_TRANSFEREE_DESCRIPTION\"},{\"code\":\"REDEMPTION_DEED_OF_THE_PREVIOUS_LOAN_OF_BANK\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"REDEMPTION_DEED_OF_THE_PREVIOUS_LOAN_OF_BANK_DESCRIPTION\"},{\"code\":\"SEWERAGE_CONNECTION\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"SEWERAGE_CONNECTION_DESCRIPTION\"},{\"code\":\"ELECTRICITY_BILL\",\"required\":false,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"ELECTRICITY_BILL_DESCRIPTION\"},{\"code\":\"CONSENT_LETTER_OF_BANK\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"CONSENT_LETTER_OF_BANK\"},{\"code\":\"ATTESTED_COPY_PARTNERSHIP_DEED\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"ATTESTED_COPY_PARTNERSHIP_DEED_DESCRIPTION\"},{\"code\":\"COPY_OF_MEMORANDUM\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"COPY_OF_MEMORANDUM_DESCRIPTION\"},{\"code\":\"CLEARANCE_OF_PROPERTY_TAX_TILL_CURRENT_FINANCIAL_YEAR\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"CLEARANCE_OF_PROPERTY_TAX_TILL_CURRENT_FINANCIAL_YEAR_DESCRIPTION\"}]";
			Type listTypeDocument = new TypeToken<List<EstateDocumentList>>() {
			}.getType();
			List<EstateDocumentList> propertyList = new Gson().fromJson(documentJson, listTypeDocument);

			List<Map<String, Object>> fieldConfigurations = new ArrayList<Map<String, Object>>(0);
			for (EstateDocumentList estateDocumentListObj : propertyList) {
				Map<String, Object> tempMap = new HashMap<String, Object>(0);
				tempMap.put("code", estateDocumentListObj.getCode());
				tempMap.put("required", estateDocumentListObj.getRequired());
				tempMap.put("accept", estateDocumentListObj.getAccept());
				tempMap.put("fileType", estateDocumentListObj.getFileType());
				tempMap.put("description", estateDocumentListObj.getDescription());
				fieldConfigurations.add(tempMap);
			}

			// Step 3 - Mock mdmservice.
			Mockito.when(mdmsservice.getMortgageDocumentConfig("mortgage", requestInfo, "ch"))
					.thenReturn(fieldConfigurations);

			ApplicationRequest request = ApplicationRequest.builder().requestInfo(requestInfo)
					.applications(applications).build();
			postMortgageEnrichmentService.postEnrichMortgageDetails(request);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void postEnrichMortgageDetailsNegative() {
		try {
			// Step 1 prepare RequestInfo
			String requestInfoJson = "{\"apiId\":\"Rainmaker\",\"ver\":\"01\",\"action\":\"_create\",\"key\":\"\",\"msgId\":\"20170310130900|en_IN\",\"authToken\":\"833b0a57-bbc5-4194-a961-bdb3794fa284\",\"userInfo\":{\"tenantId\":\"ch\",\"id\":8,\"username\":\"any\",\"mobile\":\"8866581197\",\"email\":\"mineshbhadeshia@gmail.com\" }}";
			RequestInfo requestInfo = new Gson().fromJson(requestInfoJson, RequestInfo.class);
			requestInfo.getUserInfo().setUuid(UUID.randomUUID().toString());
			Mockito.when(util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true))
					.thenReturn(AuditDetails.builder().createdBy("testCreatedBy").createdTime(new Date().getTime())
							.lastModifiedBy("testModifyBy").lastModifiedTime(new Date().getTime()).build());

			// Step 2 mock Application
			String json_ = getFileContents("MortgageDetails_application.json");
			Type listType_ = new TypeToken<List<org.egov.ps.model.Application>>() {
			}.getType();
			List<Application> applications = new Gson().fromJson(json_, listType_);

			// Step 3 - Mortgage Document.json file master data
			String documentJson = "[{\"code\":\"SELF_ATTESTED_PHOTO_IDENTITY_PROOF_\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"SELF_ATTESTED_PHOTO_IDENTITY_PROOF_DESCRIPTION\"},{\"code\":\"AFFIDAVIT_WITH_PHOTOGRAPH\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"AFFIDAVIT_WITH_PHOTOGRAPH_DESCRIPTION\"},{\"code\":\"INDEMNITY_BOND_OWNER\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"INDEMNITY_BOND_OWNER_DESCRIPTION\"},{\"code\":\"INDEMNITY_BOND_TRANSFEREE\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"INDEMNITY_BOND_TRANSFEREE_DESCRIPTION\"},{\"code\":\"REDEMPTION_DEED_OF_THE_PREVIOUS_LOAN_OF_BANK\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"REDEMPTION_DEED_OF_THE_PREVIOUS_LOAN_OF_BANK_DESCRIPTION\"},{\"code\":\"SEWERAGE_CONNECTION\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"SEWERAGE_CONNECTION_DESCRIPTION\"},{\"code\":\"ELECTRICITY_BILL\",\"required\":false,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"ELECTRICITY_BILL_DESCRIPTION\"},{\"code\":\"CONSENT_LETTER_OF_BANK\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"CONSENT_LETTER_OF_BANK\"},{\"code\":\"ATTESTED_COPY_PARTNERSHIP_DEED\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"ATTESTED_COPY_PARTNERSHIP_DEED_DESCRIPTION\"},{\"code\":\"COPY_OF_MEMORANDUM\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"COPY_OF_MEMORANDUM_DESCRIPTION\"},{\"code\":\"CLEARANCE_OF_PROPERTY_TAX_TILL_CURRENT_FINANCIAL_YEAR\",\"required\":true,\"accept\":\"application\\/msword,application\\/pdf,application\\/vnd.openxmlformats-officedocument.wordprocessingml.document,image\\/*\",\"fileType\":\"ALLTYPES\",\"description\":\"CLEARANCE_OF_PROPERTY_TAX_TILL_CURRENT_FINANCIAL_YEAR_DESCRIPTION\"}]";
			Type listTypeDocument = new TypeToken<List<EstateDocumentList>>() {
			}.getType();
			List<EstateDocumentList> mortgageDocumentlist = new Gson().fromJson(documentJson, listTypeDocument);

			List<Map<String, Object>> fieldConfigurations = new ArrayList<Map<String, Object>>(0);
			for (EstateDocumentList estateDocumentListObj : mortgageDocumentlist) {
				Map<String, Object> tempMap = new HashMap<String, Object>(0);
				tempMap.put("code", estateDocumentListObj.getCode());
				tempMap.put("required", estateDocumentListObj.getRequired());
				tempMap.put("fileType", estateDocumentListObj.getFileType());
				tempMap.put("description", estateDocumentListObj.getDescription());
				fieldConfigurations.add(tempMap);
			}

			// Step 4 mock mortgagte document master json data
			Mockito.when(mdmsservice.getMortgageDocumentConfig("mortgage", requestInfo, "ch"))
					.thenReturn(fieldConfigurations);

			ApplicationRequest request = ApplicationRequest.builder().requestInfo(requestInfo)
					.applications(applications).build();
			postMortgageEnrichmentService.postEnrichMortgageDetails(request);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String getFileContents(String fileName) {
		try {
			return IOUtils.toString(WorkflowCreationService.class.getClassLoader().getResourceAsStream(fileName),
					"UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}