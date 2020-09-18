package org.egov.ec.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.ec.config.EcConstants;
import org.egov.ec.config.EchallanConfiguration;
import org.egov.ec.producer.Producer;
import org.egov.ec.repository.AuctionRepository;
import org.egov.ec.repository.FineMasterRepository;
import org.egov.ec.repository.StoreItemRegisterRepository;
import org.egov.ec.repository.ViolationRepository;
import org.egov.ec.web.models.Auction;
import org.egov.ec.web.models.AuditDetails;
import org.egov.ec.web.models.EcSearchCriteria;
import org.egov.ec.web.models.EmailAttachment;
import org.egov.ec.web.models.FileStore;
import org.egov.ec.web.models.NotificationTemplate;
import org.egov.ec.web.models.Report;
import org.egov.ec.web.models.RequestInfoWrapper;
import org.egov.ec.web.models.ResponseInfoWrapper;
import org.egov.ec.web.models.User;
import org.egov.ec.web.models.Violation;
import org.egov.ec.web.models.workflow.ProcessInstance;
import org.egov.ec.web.models.workflow.ProcessInstanceRequest;
import org.egov.ec.workflow.WorkflowIntegrator;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Component
public class EcSchedulerService {
	@Autowired
	private ViolationRepository violationRepository;
	@Autowired
	private FineMasterRepository fineRepository;
	@Autowired
	private AuctionRepository auctionRepository;
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private StoreItemRegisterRepository storeItemRepository;


	@Autowired
	private  ObjectMapper objectMapper;
	

	@Autowired
	private WorkflowIntegrator wfIntegrator;
	@Autowired
	private Producer producer;
	@Autowired
	private EchallanConfiguration config;

	/**
	 * Description : Scheduler which runs daily to update storage charges for
	 * challns residing at store for more than 7 days
	 * @param ecSearchCriteria 
	 * 
	 * @return
	 * @throws SCHEDULER_UPDATE_FINE_EXCEPTION
	 *
	 */

	public ResponseEntity<ResponseInfoWrapper> updateFineAmount(RequestInfoWrapper requestInfoWrapper, EcSearchCriteria ecSearchCriteria) {
		try {
			fineRepository.updatePenaltyAmount(ecSearchCriteria);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(null)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			log.error("EcScheduler Service - Update Fine Amount Exception " + e.getMessage());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_FAILED).build())
					.responseInfo(ResponseInfo.builder().msgId(e.getMessage()).build()).responseBody(null).build(),
					HttpStatus.BAD_REQUEST);

		}

	}

	/**
	 * Description : Closes challan when all seized items auctioned
	 * @param ecSearchCriteria 
	 * 
	 * @return httpstatus
	 *
	 */

	public ResponseEntity<ResponseInfoWrapper> updateChallanStatus(RequestInfoWrapper requestInfoWrapper, EcSearchCriteria ecSearchCriteria) {
		try {
			requestInfoWrapper.setAuditDetails(getAuditDetails(requestInfoWrapper.getRequestInfo().getUserInfo().getUuid(), false));
		List	<Auction> auctionList = new ArrayList<Auction>();
		Auction auction = new Auction();
		Violation violation = new  Violation();
		requestInfoWrapper.setRequestBody(violation);
			 auctionList = auctionRepository.getQuantity(ecSearchCriteria.getTenantId());
			if (!auctionList.isEmpty()) {

				auctionList.stream().forEach((c) -> {
				ProcessInstance processInstance1 = new ProcessInstance();
				processInstance1.setBusinessId(c.getChallanId());
				processInstance1.setTenantId(ecSearchCriteria.getTenantId());
				processInstance1.setBusinessService(EcConstants.WORKFLOW_CHALLAN);
				processInstance1.setAction(EcConstants.STATUS_CLOSED);
				processInstance1.setModuleName(EcConstants.WORKFLOW_MODULE);
				List<ProcessInstance> processList1 = Arrays.asList(processInstance1);

				ResponseInfo response1 = wfIntegrator
						.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList1)
								.requestInfo(requestInfoWrapper.getRequestInfo()).build());

				if (response1 != null
						&& response1.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
					Violation violationMaster = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),
							Violation.class);
				
					violationMaster.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
					violationMaster
							.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
					violationMaster.setStatus(EcConstants.STATUS_CLOSED);
					violationMaster.setChallanUuid(c.getChallanUuid());
					violationRepository.updateChallan(violationMaster);
				}
				});
			}
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(auctionList)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			log.error("EcScheduler Service - Update challan Exception " + e.getMessage());
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_FAILED).build())
					.responseInfo(ResponseInfo.builder().msgId(e.getMessage()).build()).responseBody(null).build(),
					HttpStatus.BAD_REQUEST);

		}

	}

	/**
	 * @author Swapnil.Kolapkar Description : Scheduler which run once in week and
	 *         fetch eligible challans for Auction
	 * @param ecSearchCriteria 
	 * @param requestInfoWrapper 
	 * @return
	 * @throws FileNotFoundException, JSONException
	 *
	 */

	public ResponseEntity<ResponseInfoWrapper> fetchAuctionReport(RequestInfoWrapper requestInfoWrapper, EcSearchCriteria ecSearchCriteria) {
		log.info("Scheduler started " + new Date().toString());

		List<Report> auctionChallan = storeItemRepository.getStoreAuctionItem(ecSearchCriteria.getTenantId());

		try {

			JSONArray objSearchOrdersDto = new JSONArray(auctionChallan);

			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Report");

			int rowCount = 0;
			Row row1 = sheet.createRow(0);
			Cell Row_cell1 = row1.createCell(0);
			Row_cell1.setCellValue("Challan No");
			Cell Row_cell2 = row1.createCell(1);
			Row_cell2.setCellValue("Item Name");
			Cell Row_cell3 = row1.createCell(2);
			Row_cell3.setCellValue("Item Quantity");
			Cell Row_cell4 = row1.createCell(3);
			Row_cell4.setCellValue("Store Deposit Date");
			Cell Row_cell5 = row1.createCell(4);
			Row_cell5.setCellValue("Age");

			for (int i = 0; i < objSearchOrdersDto.length(); ++i) {
				JSONObject rec = objSearchOrdersDto.getJSONObject(i);

				Row row = sheet.createRow(++rowCount);
				Cell cell1 = row.createCell(0);
				cell1.setCellValue(rec.getString("challanId"));
				Cell cell2 = row.createCell(1);
				cell2.setCellValue(rec.getString("itemName"));
				Cell cell3 = row.createCell(2);
				cell3.setCellValue(rec.getInt("itemQuantity"));
				Cell cell4 = row.createCell(3);
				cell4.setCellValue(rec.getString("itemStoreDepositDate"));
				Cell cell5 = row.createCell(4);
				cell5.setCellValue(rec.getInt("age"));

			}

			try (FileOutputStream outputStream = new FileOutputStream("Report.xlsx")) {
				workbook.write(outputStream);
				outputStream.toString();
			} catch (Exception e) {
				log.error("EcScheduler Service - Auction Reminder Exception" + e.getMessage());
			}

			HttpHeaders http = new HttpHeaders();
			http.setContentType(MediaType.MULTIPART_FORM_DATA);
			MultiValueMap<String, Object> mmap = new LinkedMultiValueMap<String, Object>();
			mmap.add("file", getFile());
			mmap.add("tenantId", "ch");
			mmap.add("module", EcConstants.MDM_MODULE);
			HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity(mmap, http);

			FileStore response = restTemplate.postForObject(config.getFileStoreHost() + config.getUploadFile(), entity,
					FileStore.class);

			UriComponentsBuilder builder = UriComponentsBuilder
					.fromUriString(config.getFileStoreHost() + config.getFileStoreUrl())
					.queryParam("fileStoreIds", response.getFiles().get(0).getFileStoreId())
					.queryParam("tenantId", (response.getFiles().get(0).getTenantId()));

			FileStore urlResponse = restTemplate.getForObject(builder.toUriString(), FileStore.class);
			sendMail(urlResponse,ecSearchCriteria,requestInfoWrapper);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(null)
					.build(), HttpStatus.OK);

		} catch (Exception e) {
			log.error("EcScheduler Service - Auction Reminder Exception" + e);

			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_FAILED).build())
					.responseInfo(ResponseInfo.builder().msgId(e.getMessage()).build()).responseBody(null).build(),
					HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * Description : To push email topic to producer for sending email notification
	 * 
	 * @param urlResponse
	 * @param ecSearchCriteria 
	 * @param req 
	 * @throws JSONException 
	 *
	 */
	private void sendMail(FileStore urlResponse, EcSearchCriteria ecSearchCriteria, RequestInfoWrapper req) throws JSONException {
		String subject = "";
		String body = "";
System.out.println(urlResponse);
		User user = new User();
	//	RequestInfo req = new RequestInfo();

		UriComponentsBuilder userUrlBuilder = UriComponentsBuilder
				.fromUriString(config.getHrmsHost() + config.getHrmsSearchEndpoint()).queryParam("roles", EcConstants.ROLE_STORE_MANAGER)
				.queryParam("tenantId", ecSearchCriteria.getTenantId());
		JsonNode userResponse = restTemplate.postForObject(userUrlBuilder.toUriString(),
				RequestInfoWrapper.builder().requestInfo(req.getRequestInfo()).build(), JsonNode.class);
		String json = userResponse.toString();
		JSONObject obj = new JSONObject(json);
		JSONArray arr = obj.getJSONArray("Employees");
		List<String> emailList = new ArrayList<String>();
		for (int i = 0; i < arr.length(); i++) {
			JSONObject userObj = arr.getJSONObject(i).getJSONObject("user");
			if (!userObj.get("emailId").toString().equals(null)) {
				emailList.add(userObj.get("emailId").toString());
			}
		}

		MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
    	mdmsCriteriaReq.setRequestInfo(req.getRequestInfo());
		MdmsCriteria criteria = new MdmsCriteria();
		criteria.setTenantId("ch");

		ModuleDetail detail = new ModuleDetail();
		detail.setModuleName("egec");
		MasterDetail masterDetail = new MasterDetail();
		masterDetail.setName("AuctionNotificationTemplate");
		//masterDetail.setFilter(filter);
		ArrayList<MasterDetail> masterList = new ArrayList<>();
		masterList.add(masterDetail);
		detail.setMasterDetails(masterList);

		ArrayList<ModuleDetail> moduleList = new ArrayList<>();
		moduleList.add(detail);

		criteria.setModuleDetails(moduleList);
		mdmsCriteriaReq.setMdmsCriteria(criteria);

		Object response = restTemplate.postForObject((config.getMdmsHost() + config.getMdmsEndPoint()), mdmsCriteriaReq, Object.class);

		Map<String, List<String>> mdmResponse = getAttributeValues(response);
		String value = mdmResponse.get(EcConstants.MDM_TEMPLATE_AUCTION_NOTIFICATION).toString();

		value = value.substring(2, value.length() - 2);
		String[] keyValuePairs = value.split(",");
		Map<String, String> map = new HashMap<>();

		for (String pair : keyValuePairs) {
			String[] entry = pair.split("=");
			if (entry[0].trim().equalsIgnoreCase("subject") || entry[0].trim().equalsIgnoreCase("body")) {
				map.put(entry[0].trim(), entry[1].trim());}
		}

		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("subject")) {
				subject = entry.getValue();
			}
			if (entry.getKey().equalsIgnoreCase("body")) {
				body = entry.getValue();
			}

		}

		NotificationTemplate email = new NotificationTemplate();
		
	//	email.setAttachmentUrls(attachment);
		
		
		EmailAttachment emailattachment =  new EmailAttachment();
		emailattachment.setName("AuctionReminderReport.xlsx");
		emailattachment.setUrl(urlResponse.getFileStoreIds().get(0).getUrl());;
		emailattachment.setMimeType("application/octet-stream");
		List<EmailAttachment> attachment = new ArrayList<EmailAttachment>();
		
		attachment.add(emailattachment);
		email.setIsHTML(true);
		email.setBody(body);
		email.setSubject(subject);
		email.setAttachments(attachment);
		emailList.stream().forEach((c) -> {
			email.setEmail(c);
			if(config.getEchallanNotificationFlag().equalsIgnoreCase("ON"))
			{
				producer.push(config.getEmailNotificationTopic(), email);
			}
		});

	}

	/**
	 * Description : To fetch data from MDM against specified object
	 * 
	 * @param mdmsData
	 * @returns mdmsRespMap
	 *
	 */
	private Map<String, List<String>> getAttributeValues(Object mdmsData) {
		List<String> modulepaths = Arrays.asList(EcConstants.MDMS_TEMPLATE_PATH);
		final Map<String, List<String>> mdmsResMap = new HashMap<>();
		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				log.error("Error while fetvhing MDMS data", e.getMessage());
				throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "ChallanID Generation Failed");
			}
		});
		return mdmsResMap;
	}

	/**
	 * Description : TO read file on specified path
	 *
	 */
	public static Resource getFile() throws IOException {
		File report = new File("Report.xlsx");
		Path filePath = report.toPath();
		Files.write(report.toPath(), Files.readAllBytes(report.toPath()));
		return new FileSystemResource(filePath.toFile());
	}
	
	  public AuditDetails getAuditDetails(String by, Boolean isCreate) {
	        Long time = System.currentTimeMillis();
	        if(isCreate)
	            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
	        else
	            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	    }
	  
	  /**
		 * Description : Closes Pending for Auction when items are lying for more than 30 days at Store
		 * @param ecSearchCriteria 
		 * 
		 * @return httpstatus
		 *
		 */

		public ResponseEntity<ResponseInfoWrapper> updateAuctionStatus(RequestInfoWrapper requestInfoWrapper, EcSearchCriteria ecSearchCriteria) {
			try {
				requestInfoWrapper.setAuditDetails(getAuditDetails(requestInfoWrapper.getRequestInfo().getUserInfo().getUuid(), false));
			List	<Auction> auctionList = new ArrayList<Auction>();
			Auction auction = new Auction();
			Violation violation = new  Violation();
			requestInfoWrapper.setRequestBody(violation);
				 auctionList = auctionRepository.getPendingForAuctionChallans(ecSearchCriteria.getTenantId());
				if (!auctionList.isEmpty()) {

					auctionList.stream().forEach((c) -> {
					ProcessInstance processInstance1 = new ProcessInstance();
					processInstance1.setBusinessId(c.getChallanId());
					processInstance1.setTenantId(ecSearchCriteria.getTenantId());
					processInstance1.setBusinessService(EcConstants.WORKFLOW_CHALLAN);
					processInstance1.setAction(EcConstants.STATUS_AUCTION_PENDING);
					processInstance1.setModuleName(EcConstants.WORKFLOW_MODULE);
					List<ProcessInstance> processList1 = Arrays.asList(processInstance1);

					ResponseInfo response1 = wfIntegrator
							.callWorkFlow(ProcessInstanceRequest.builder().processInstances(processList1)
									.requestInfo(requestInfoWrapper.getRequestInfo()).build());

					if (response1 != null
							&& response1.getStatus().equalsIgnoreCase(EcConstants.STATUS_SUCCESSFULL)) {
						Violation violationMaster = objectMapper.convertValue(requestInfoWrapper.getRequestBody(),
								Violation.class);
					
						violationMaster.setLastModifiedBy(requestInfoWrapper.getAuditDetails().getLastModifiedBy());
						violationMaster
								.setLastModifiedTime(requestInfoWrapper.getAuditDetails().getLastModifiedTime());
						violationMaster.setStatus(EcConstants.STATUS_AUCTION_PENDING);
						violationMaster.setChallanUuid(c.getChallanUuid());
						violationRepository.updateChallan(violationMaster);
					}
					});
				}
				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_SUCCESS).build()).responseBody(auctionList)
						.build(), HttpStatus.OK);

			} catch (Exception e) {
				log.error("EcScheduler Service - Update Auction Exception " + e.getMessage());
				return new ResponseEntity<>(ResponseInfoWrapper.builder()
						.responseInfo(ResponseInfo.builder().status(EcConstants.STATUS_FAILED).build())
						.responseInfo(ResponseInfo.builder().msgId(e.getMessage()).build()).responseBody(null).build(),
						HttpStatus.BAD_REQUEST);

			}

		}

}
