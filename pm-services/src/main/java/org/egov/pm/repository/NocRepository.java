package org.egov.pm.repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.pm.PreApplicationRunnerImpl;
import org.egov.pm.model.EmailTemplateModel;
import org.egov.pm.model.NOCApplication;
import org.egov.pm.model.NOCApplicationDetail;
import org.egov.pm.model.NOCApplicationRemark;
import org.egov.pm.model.NOCDetailsRequestData;
import org.egov.pm.model.NOCPriceBook;
import org.egov.pm.model.NOCRemarksRequestData;
import org.egov.pm.model.NOCRequestData;
import org.egov.pm.model.PriceBookRequestData;
import org.egov.pm.model.ReportModel;
import org.egov.pm.model.RequestData;
import org.egov.pm.producer.Producer;
import org.egov.pm.repository.querybuilder.QueryBuilder;
import org.egov.pm.repository.rowmapper.ColumnsNocRowMapper;
import org.egov.pm.repository.rowmapper.CounterRowMapper;
import org.egov.pm.repository.rowmapper.NocRowMapper;
import org.egov.pm.repository.rowmapper.PriceRowMapper;
import org.egov.pm.service.CommonService;
import org.egov.pm.util.CommonConstants;
import org.egov.pm.web.contract.NocResponse;
import org.egov.pm.web.contract.ResponseData;
import org.egov.pm.wf.model.Document;
import org.egov.pm.wf.model.ProcessInstance;
import org.egov.pm.wf.model.ProcessInstanceRequest;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class NocRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NocRowMapper nocRowMapper;

	@Autowired
	private CounterRowMapper counterRowMapper;

	@Autowired
	private ColumnsNocRowMapper columnsNocRowMapper;

	@Autowired
	private CommonService commonService;

	@Autowired
	private NOCApplication nocApplication;

	@Autowired
	private Producer producer;

	@Value("${persister.save.transition.noc.topic}")
	private String saveNOCTopic;

	@Value("${persister.save.transition.noc.details.topic}")
	private String saveNOCDetailsTopic;

	@Value("${persister.save.transition.nocapprovereject.topic}")
	private String saveNOCApproveRejectTopic;

	@Value("${persister.update.transition.noc.topic}")
	private String updateNOCTopic;

	@Value("${persister.update.transition.noc.status.topic}")
	private String updateStatusNOCTopic;

	@Value("${persister.update.transition.nocapprovereject.topic}")
	private String updateNOCApproveRejectTopic;

	@Value("${persister.update.transition.noc.details.topic}")
	private String updateNOCDetailsTopic;

	@Value("${persister.update.noc.applicationDetails.topic}")
	private String updateNOCApplicationDetailsTopic;

	@Value("${persister.notification.noc.topic}")
	private String notificationTopic;

	@Value("${persister.delete.transition.noc.details.topic}")
	private String deleteNOCDetailsTopic;

	@Value("${persister.update.pricebook.noc.topic}")
	private String updatePriceBook;

	@Value("${persister.update.pricebookdate.noc.topic}")
	private String updatePriceBookdate;

	@Value("${persister.insert.pricebook.noc.topic}")
	private String insertPriceBook;

	@Value("${persister.wf.fallback.noc.topic}")
	private String fallbackTopic;

	@Autowired
	private PriceRowMapper priceRowMapper;

	/**
	 * Updates the noc application and pushed data to kafka It will update the main
	 * noc and noc details data
	 * 
	 * @param RequestData
	 *            with datapayload,applicationType,applicationId,tenantId
	 */
	public void updateNOC(RequestData requestData, String applicationId) {
		RequestInfo requestInfo = requestData.getRequestInfo();
		JSONObject dataPayLoad = requestData.getDataPayload();
		NOCApplication app = new NOCApplication();
		app.setApplicantName((dataPayLoad.get(CommonConstants.APPLICANTNAME) == null ? ""
				: dataPayLoad.get(CommonConstants.APPLICANTNAME).toString()));
		app.setHouseNo((dataPayLoad.get(CommonConstants.HOUSENO) == null ? ""
				: dataPayLoad.get(CommonConstants.HOUSENO).toString()));
		app.setSector((dataPayLoad.get(CommonConstants.SECTOR) == null ? ""
				: dataPayLoad.get(CommonConstants.SECTOR).toString()));
		app.setNocNumber(applicationId);
		app.setApplicationStatus(requestData.getApplicationStatus());
		app.setLastModifiedTime(System.currentTimeMillis());
		List<NOCApplication> applist = Arrays.asList(app);
		NOCRequestData data = new NOCRequestData();
		data.setRequestInfo(requestInfo);
		data.setNocApplication(applist);

		producer.push(updateNOCTopic, data);
		// update set is active false

		// update detail table
		Long time = System.currentTimeMillis();
		String applicationDetailsId = UUID.randomUUID().toString();

		List<NOCApplicationDetail> preparedStatementValues = jdbcTemplate
				.query(QueryBuilder.SELECT_APPLICATION_DETAIL_QUERY, new Object[] { applicationId }, nocRowMapper);

		JSONObject dataPayload = requestData.getDataPayload();
		dataPayload.remove(CommonConstants.APPLICANTNAME);
		dataPayload.remove(CommonConstants.HOUSENO);
		dataPayload.remove(CommonConstants.SECTOR);
		NOCApplicationDetail nocappdetails = new NOCApplicationDetail();
		for (NOCApplicationDetail ps : preparedStatementValues) {
			nocappdetails.setApplicationDetailUuid(applicationDetailsId);
			nocappdetails.setApplicationUuid(ps.getApplicationUuid());
			nocappdetails.setApplicationDetail(requestData.getDataPayload().toJSONString());
			nocappdetails.setIsActive(true);
			nocappdetails.setCreatedBy(ps.getCreatedBy());
			nocappdetails.setCreatedTime(ps.getCreatedTime());
			nocappdetails.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
			nocappdetails.setLastModifiedTime(time);
			nocappdetails.setTenantId(requestData.getTenantId());
			List<NOCApplicationDetail> applist1 = Arrays.asList(nocappdetails);
			NOCDetailsRequestData data1 = new NOCDetailsRequestData();
			data1.setRequestInfo(requestInfo);
			data1.setNocApplicationDetails(applist1);
			producer.push(updateNOCDetailsTopic, data1);

			NOCApplicationDetail nocappdetails1 = new NOCApplicationDetail();
			nocappdetails1.setApplicationDetailUuid(ps.getApplicationDetailUuid());
			nocappdetails1.setLastModifiedTime(time);
			List<NOCApplicationDetail> applisttodelete = Arrays.asList(nocappdetails1);
			NOCDetailsRequestData data2 = new NOCDetailsRequestData();
			data2.setRequestInfo(requestInfo);
			data2.setNocApplicationDetails(applisttodelete);
			producer.push(deleteNOCDetailsTopic, data2);
		}
	}

	/**
	 * Getting remarks data for requested applicationId
	 * 
	 * @param required
	 *            tenantId and applicationId
	 */
	public JSONArray getRemarksForNoc(String tenantId, String appId) {
		return jdbcTemplate.query(QueryBuilder.GET_NOC_REMARKS_QUERY, new Object[] { tenantId, appId },
				columnsNocRowMapper);
	}

	/**
	 * Getting data for Templates
	 * 
	 * @param required
	 *            tenantId and applicationType and status
	 */
	public Map<String, EmailTemplateModel> findTemplate(String status, String tenantId, String applicationType) {

		Map<String, EmailTemplateModel> map = null;
		if (status != null && tenantId != null && applicationType != null) {
			map = PreApplicationRunnerImpl.getTemplate(tenantId, status, applicationType);
		}
		return map;
	}

	/**
	 * Getting application list data
	 * 
	 * @param required
	 *            applicationType,RequestInfo and tenantId
	 */
	public JSONArray getNoc(RequestData requestInfo) {
		List<Role> roles = requestInfo.getRequestInfo().getUserInfo().getRoles();
		String tenantId = requestInfo.getTenantId();
		List<String> queries = new ArrayList<>();
		String requestType = requestInfo.getApplicationType();
		String queryString = "";

		// this is for citizen
		if (requestInfo.getRequestInfo().getUserInfo().getType().equals("CITIZEN")) {
			log.info("Citizen Login :{} " + queryString);
			queryString = PreApplicationRunnerImpl.getSqlQuery(tenantId, "CITIZEN", requestType);
			log.info("queryString :{} " + queryString);
			JSONObject dataPayload = requestInfo.getDataPayload();
			Set<String> keyList = dataPayload.keySet();
			for (String keyName : keyList) {
				StringBuilder values = new StringBuilder();
				String[] strParameters = dataPayload.get(keyName).toString().split(",");
				for (int i = 0; i < strParameters.length; i++) {
					values.append("'" + strParameters[i] + "',");
				}
				queryString = queryString.replace("[:" + keyName + ":]", values.substring(0, values.length() - 1));
			}
			if (!queryString.equals("")) {
				return jdbcTemplate.query(queryString, new Object[] {}, columnsNocRowMapper);
			} else {
				return new JSONArray();
			}

		} else {
			List<Object> preparedStmtList = new ArrayList<>();

			String query = QueryBuilder.getSearchQuery(requestInfo, preparedStmtList);
			log.info("Query: " + query);
			return jdbcTemplate.query(query, preparedStmtList.toArray(), columnsNocRowMapper);
		}
	}

	/**
	 * Getting certificate data for requested applicationId
	 * 
	 * @param required
	 *            applicationType,RequestInfo and tenantId,requestDocumentType
	 */
	public JSONArray getCertificateData(RequestData requestInfo) {
		try {
			if (!requestInfo.getApplicationId().isEmpty()) {
				String tenantId = requestInfo.getTenantId();
				String certificateSqlQuery = PreApplicationRunnerImpl.getCertificateSqlQuery(tenantId,
						requestInfo.getApplicationType(),
						requestInfo.getDataPayload().get("requestDocumentType") != null
								? requestInfo.getDataPayload().get("requestDocumentType").toString()
								: "");
				JSONArray actualResult = jdbcTemplate.query(certificateSqlQuery,
						new Object[] { requestInfo.getApplicationId(), requestInfo.getTenantId() },
						columnsNocRowMapper);
				if (!actualResult.isEmpty() && actualResult.size() > 0) {
					JSONArray remarksDataList = new JSONArray();
					String uUid = "";
					for (int i = 0; i < actualResult.size(); i++) {
						JSONObject remarksData = (JSONObject) actualResult.get(i);
						remarksData.put("applicationType", requestInfo.getApplicationType());
						remarksData.put("tenantId", requestInfo.getTenantId());
						remarksDataList.add(remarksData);
					}
					return remarksDataList;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Getting data for application based on applicationId
	 * 
	 * @param required
	 *            RequestInfo,applicationId
	 */
	public JSONArray viewNoc(RequestData requestInfo) {
		try {
			if (!requestInfo.getApplicationId().isEmpty()) {

				JSONArray actualResult = jdbcTemplate.query(QueryBuilder.SELECT_VIEW_QUERY,
						new Object[] { requestInfo.getApplicationId() }, columnsNocRowMapper);

				JSONObject applicationData = (JSONObject) actualResult.get(0);

				if (requestInfo.getRequestInfo().getUserInfo().getType().equals("CITIZEN") && !applicationData
						.get("createdby").toString().equals(requestInfo.getRequestInfo().getUserInfo().getUuid())) {
					return null;
				}

				JSONArray applicationList = new JSONArray();
				String uUid = "";

				JSONArray remarksDataList = new JSONArray();

				uUid = applicationData.get("applicationuuid").toString();
				if (uUid != null && !uUid.isEmpty()) {
					JSONArray actualRemarksResult = jdbcTemplate.query(QueryBuilder.ALL_REMARKS_QUERY,
							new Object[] { uUid }, columnsNocRowMapper);
					JSONObject remarkData = null;
					for (int n = 0; n < actualRemarksResult.size(); n++) {
						JSONObject remarkList = (JSONObject) actualRemarksResult.get(n);
						remarkData = new JSONObject();
						Set<String> keyss = remarkList.keySet();
						for (String key : keyss) {
							remarkData.put(key, remarkList.get(key));
						}
						remarksDataList.add(remarkData);
					}
				}
				applicationData.put(CommonConstants.REMARKS, remarksDataList);
				applicationList.add(applicationData);

				return applicationList;
			} else {
				return new JSONArray();
			}
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Generating Id and calling to save noc
	 * 
	 * @param required
	 *            RequestData and status
	 */

	public String saveValidateStatus(RequestData requestData, String status)
			throws JsonParseException, JsonMappingException, ParseException, IOException {
		String nocId = null;
		String applicationId = commonService.generateApplicationId(requestData.getTenantId());
		if (applicationId != null) {
			requestData.setApplicationId(applicationId);
			nocId = saveNoc(requestData, status, applicationId);
		} else {
			nocId = null;
		}
		return nocId;
	}

	/**
	 * Adding new record for NOC using kafka Setting data to Noc Application,details
	 * and remarks for NOC
	 * 
	 * @param required
	 *            Requestdata,applicationId and status
	 */
	// add
	@SuppressWarnings("deprecation")
	public String saveNoc(RequestData requestData, String status, String nocId) {

		RequestInfo requestInfo = requestData.getRequestInfo();

		int isApplicationIdExists = validateApplicationId(nocId);
		if (isApplicationIdExists == 0) {
			String applicationId = null;
			if (nocId != null) {
				JSONObject dataPayLoad = requestData.getDataPayload();
				Long time = System.currentTimeMillis();
				applicationId = UUID.randomUUID().toString();
				NOCApplication app = new NOCApplication();
				app.setApplicationUuid(applicationId);
				app.setTenantId(requestData.getTenantId());
				app.setNocNumber(nocId);
				app.setApplicantName((dataPayLoad.get(CommonConstants.APPLICANTNAME) == null ? ""
						: dataPayLoad.get(CommonConstants.APPLICANTNAME).toString()));
				app.setHouseNo((dataPayLoad.get(CommonConstants.HOUSENO) == null ? ""
						: dataPayLoad.get(CommonConstants.HOUSENO).toString()));
				app.setSector((dataPayLoad.get(CommonConstants.SECTOR) == null ? ""
						: dataPayLoad.get(CommonConstants.SECTOR).toString()));
				app.setAppliedDate(new Date().toLocaleString());
				app.setApplicationType(requestData.getApplicationType());
				app.setApplicationStatus(status);
				app.setIsActive(true);
				app.setCreatedBy(requestInfo.getUserInfo().getUuid());
				app.setCreatedTime(time);
				app.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
				app.setLastModifiedTime(time);

				NOCApplicationDetail saveNOCDetails = saveNOCDetails(requestData, applicationId, nocId);
				app.setNocApplicationDetails(saveNOCDetails);
				String roleCode = requestData.getRequestInfo().getUserInfo().getType();
				String remarkID = UUID.randomUUID().toString();

				NOCApplicationRemark remarkDetails = new NOCApplicationRemark();
				remarkDetails.setRemarkId(remarkID);
				remarkDetails.setApplicationUuid(applicationId);
				remarkDetails.setApplicationStatus(requestData.getApplicationStatus());
				remarkDetails.setRemark((dataPayLoad.get(CommonConstants.REMARKS) == null ? ""
						: dataPayLoad.get(CommonConstants.REMARKS).toString()));
				remarkDetails.setRemarkBy(roleCode);
				remarkDetails.setIsActive(true);
				remarkDetails.setCreatedBy(requestInfo.getUserInfo().getUuid());
				remarkDetails.setCreatedTime(time);
				remarkDetails.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
				remarkDetails.setLastModifiedTime(time);
				remarkDetails.setTenantId(requestData.getTenantId());

				app.setNocApplicationRemark(remarkDetails);

				List<NOCApplication> applist = Arrays.asList(app);
				NOCRequestData data = new NOCRequestData();
				data.setRequestInfo(requestInfo);
				data.setNocApplication(applist);
				producer.push(saveNOCTopic, data);
				return nocId;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Validating application Id for requested Id
	 * 
	 * @param required
	 *            applicationId
	 */
	public int validateApplicationId(String applicationId) {
		List<Object> preparedStatementValues = new ArrayList<>();
		String queryStr = QueryBuilder.getApplicationQuery();
		log.debug("query:::" + queryStr + " preparedStatementValues::" + preparedStatementValues);

		Map<String, Object> params = new HashMap<>();
		params.put(CommonConstants.APPLICATIONUUID, applicationId);

		return jdbcTemplate.query(QueryBuilder.SELECT_APPLICATION_ID_QUERY, new Object[] { applicationId },
				counterRowMapper);

	}

	/**
	 * Getting user of requested applicationId
	 * 
	 * @param required
	 *            applicationId
	 */

	public List<Map<String, Object>> getUserByNocNumber(String applicationId) {
		List<Object> preparedStatementValues = new ArrayList<>();
		String queryStr = QueryBuilder.getApplicationQuery();
		log.debug("query:::" + queryStr + " preparedStatementValues::" + preparedStatementValues);

		Map<String, Object> params = new HashMap<>();
		params.put(CommonConstants.APPLICATIONUUID, applicationId);

		List<Map<String, Object>> rsMapList = jdbcTemplate
				.queryForList(QueryBuilder.SELECT_USER_BY_APPLICATION_ID_QUERY, new Object[] { applicationId });
		log.info("User for requested noc", rsMapList);

		return rsMapList;

	}

	/**
	 * Workflow Integration Method
	 * 
	 * @param required
	 *            applicationId,requestData and status
	 */
	public ResponseInfo workflowIntegration(String applicationId, RequestData requestData, String status)
			throws IOException {

		String remarks = requestData.getDataPayload().get("remarks") != null
				? requestData.getDataPayload().get("remarks").toString()
				: "";

		ArrayList<LinkedHashMap<Object, Object>> documentList = requestData.getDataPayload()
				.get("uploadDocuments") != null
						? (ArrayList<LinkedHashMap<Object, Object>>) requestData.getDataPayload().get("uploadDocuments")
						: null;

		List<Document> workflowDocumentList = new ArrayList<>();
		ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest();
		workflowRequest.setRequestInfo(requestData.getRequestInfo());
		ProcessInstance processInstances = new ProcessInstance();
		processInstances.setTenantId(requestData.getTenantId());
		processInstances.setAction(status);
		processInstances.setBusinessId(applicationId);
		processInstances.setComment(remarks);
		processInstances.setModuleName(requestData.getApplicationType());
		processInstances.setBusinessService(requestData.getApplicationType());
		// requestData.getRequestInfo().getUserInfo().getRoles().get(0).getCode().equals("CITIZEN")
		if (!requestData.getRequestInfo().getUserInfo().getType().equals("CITIZEN")) {
			if (documentList != null && documentList.get(0).get(CommonConstants.FILESTOREID) != null
					&& documentList.get(0).get(CommonConstants.FILESTOREID) != "") {
				for (LinkedHashMap<Object, Object> document : documentList) {
					log.info("Inside document in workflowintegration : {}", document);
					if (document.get(CommonConstants.FILESTOREID) != null) {
						Document workflowDocument = new Document();
						workflowDocument.setFileStoreId(document.get(CommonConstants.FILESTOREID).toString());
						workflowDocument.setTenantId(requestData.getTenantId());
						workflowDocument.setDocumentType(status + "_DOCUMENT");
						workflowDocumentList.add(workflowDocument);

					}
				}
			}
		}

		processInstances.setDocuments(workflowDocumentList);

		List<ProcessInstance> processList = Arrays.asList(processInstances);
		workflowRequest.setProcessInstances(processList);
		try {
			return commonService.createWorkflowRequest(workflowRequest);

		} catch (Exception e) {
			ResponseInfo responseInfo = new ResponseInfo();
			responseInfo.setResMsgId(HttpStatus.BAD_REQUEST.toString());
			responseInfo.setStatus("Fail");
			responseInfo.setMsgId(e.getMessage());
			return responseInfo;
		}

	}

	/**
	 * Saving Noc Details data
	 * 
	 * @param required
	 *            applicationId,RequestInfo and RequestData
	 */
	public NOCApplicationDetail saveNOCDetails(RequestData requestData, String applicationId, String nocId) {

		RequestInfo requestInfo = requestData.getRequestInfo();
		log.info("Application Id is :{}", applicationId);
		JSONObject dataPayload = requestData.getDataPayload();
		dataPayload.remove(CommonConstants.APPLICANTNAME);
		dataPayload.remove(CommonConstants.HOUSENO);
		dataPayload.remove(CommonConstants.SECTOR);

		Long time = System.currentTimeMillis();
		String applicationDetailsId = UUID.randomUUID().toString();
		requestData.getDataPayload();
		NOCApplicationDetail nocappdetails = new NOCApplicationDetail();
		nocappdetails.setApplicationDetailUuid(applicationDetailsId);
		nocappdetails.setApplicationUuid(applicationId);
		nocappdetails.setApplicationDetail(dataPayload.toJSONString());
		nocappdetails.setIsActive(true);
		nocappdetails.setCreatedBy(requestInfo.getUserInfo().getUuid());
		nocappdetails.setCreatedTime(time);
		nocappdetails.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
		nocappdetails.setLastModifiedTime(time);
		nocappdetails.setTenantId(requestData.getTenantId());
		List<NOCApplicationDetail> applist = Arrays.asList(nocappdetails);
		NOCDetailsRequestData data = new NOCDetailsRequestData();
		data.setRequestInfo(requestInfo);
		data.setNocApplicationDetails(applist);
		return nocappdetails;
	}

	/**
	 * Updating status for application This method updates th status in main noc and
	 * noc details and remarks data
	 * 
	 * @param required
	 *            applicationType,RequestInfo,tenantId and workflowresponse
	 */
	public ResponseData updateAppStatus(RequestData requestData)
			throws ParseException, JsonParseException, JsonMappingException, IOException {
		log.info("Started approveRejectNocTable() : " + requestData);
		RequestInfo requestInfo = requestData.getRequestInfo();
		String applicationId = null;
		try {
			setFallBack(requestData.getApplicationId());

			JSONObject dataPayLoad = requestData.getDataPayload();
			String roleCode = "";
			roleCode = requestData.getRequestInfo().getUserInfo().getType();
			// getApplication Details by noc_number
			JSONArray actualResult = jdbcTemplate.query(QueryBuilder.SELECT_NOC_BY_NOCNUMBER,
					new Object[] { requestData.getApplicationId() }, columnsNocRowMapper);
			JSONObject jsonObject1 = (JSONObject) actualResult.get(0);

			String appId = jsonObject1.get(CommonConstants.STATUSAPPUUID).toString();
			String tenantId = jsonObject1.get(CommonConstants.TENANTID).toString();

			Long time = System.currentTimeMillis();
			applicationId = UUID.randomUUID().toString();
			NOCApplicationRemark app = new NOCApplicationRemark();
			app.setRemarkId(applicationId);
			app.setApplicationUuid(appId);
			app.setApplicationStatus(requestData.getApplicationStatus());
			app.setRemark((dataPayLoad.get(CommonConstants.REMARKS) == null ? ""
					: dataPayLoad.get(CommonConstants.REMARKS).toString()));
			app.setRemarkBy(roleCode);
			app.setIsActive(true);
			app.setCreatedBy(requestInfo.getUserInfo().getUuid());
			app.setCreatedTime(time);
			app.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
			app.setLastModifiedTime(time);
			app.setTenantId(tenantId);
			app.setDocumentId((dataPayLoad.get(CommonConstants.DOCUMENTDETAIL) == null ? "{ \"fileStoreId\":\"\" }"
					: dataPayLoad.get(CommonConstants.DOCUMENTDETAIL).toString()));

			List<NOCApplicationRemark> applist = Arrays.asList(app);

			NOCRemarksRequestData data = new NOCRemarksRequestData();
			data.setRequestInfo(requestInfo);
			data.setNocApplicationRamarks(applist);

			// then Save new entry
			producer.push(saveNOCApproveRejectTopic, data);

			// setting remark id to fallback object
			if (nocApplication.getNocApplicationRemark() != null)
				nocApplication.getNocApplicationRemark().setRemarkId(applicationId);

			nocApplication.setLastModifiedTime(time);

			// then Update the main table application status

			BigDecimal gstAmount = jsonObject1.get(CommonConstants.GSTAMOUNTDB) != null
					&& !jsonObject1.get(CommonConstants.GSTAMOUNTDB).toString().isEmpty()
							? new BigDecimal(jsonObject1.get(CommonConstants.GSTAMOUNTDB).toString())
							: null;
			BigDecimal performanceCharges = jsonObject1.get(CommonConstants.PERFORMANCEBANKGUARANTEECHARGESDB) != null
					&& !jsonObject1.get(CommonConstants.PERFORMANCEBANKGUARANTEECHARGESDB).toString().isEmpty()
							? new BigDecimal(
									jsonObject1.get(CommonConstants.PERFORMANCEBANKGUARANTEECHARGESDB).toString())
							: null;
			BigDecimal amount = jsonObject1.get(CommonConstants.AMOUNT) != null
					&& !jsonObject1.get(CommonConstants.AMOUNT).toString().isEmpty()
							? new BigDecimal(jsonObject1.get(CommonConstants.AMOUNT).toString())
							: null;

			BigDecimal totalamount = jsonObject1.get(CommonConstants.TOTALAMOUNT) != null
					&& !jsonObject1.get(CommonConstants.TOTALAMOUNT).toString().isEmpty()
							? new BigDecimal(jsonObject1.get(CommonConstants.TOTALAMOUNT).toString())
							: null;

			NOCApplication apps = new NOCApplication();
			apps.setTenantId(tenantId);
			apps.setApplicationUuid(appId);
			apps.setNocNumber(requestData.getApplicationId());
			apps.setApplicationType(requestData.getApplicationType());
			apps.setApplicationStatus(requestData.getApplicationStatus());
			apps.setCreatedBy(jsonObject1.get(CommonConstants.CREATEDBYDB).toString());
			apps.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
			apps.setLastModifiedTime(time);
			apps.setAmount((dataPayLoad.get(CommonConstants.AMOUNT) != null
					? new BigDecimal(dataPayLoad.get(CommonConstants.AMOUNT).toString())
					: amount));
			apps.setGstAmount((dataPayLoad.get(CommonConstants.GSTAMOUNT) != null
					? new BigDecimal(dataPayLoad.get(CommonConstants.GSTAMOUNT).toString())
					: gstAmount));
			apps.setPerformanceBankGuarantee((dataPayLoad.get(CommonConstants.PERFORMANCEBANKGUARANTEECHARGES) != null
					? new BigDecimal(dataPayLoad.get(CommonConstants.PERFORMANCEBANKGUARANTEECHARGES).toString())
					: performanceCharges));

			apps.setAmount(apps.getAmount() != null ? apps.getAmount().setScale(0, BigDecimal.ROUND_UP) : null);
			apps.setPerformanceBankGuarantee(apps.getPerformanceBankGuarantee() != null
					? apps.getPerformanceBankGuarantee().setScale(0, BigDecimal.ROUND_UP)
					: null);
			apps.setGstAmount(
					apps.getGstAmount() != null ? apps.getGstAmount().setScale(0, BigDecimal.ROUND_UP) : null);
			if (dataPayLoad.get(CommonConstants.GSTAMOUNT) != null && dataPayLoad.get(CommonConstants.AMOUNT) != null) {
				totalamount = apps.getGstAmount().add(apps.getAmount());
				apps.setTotalamount(totalamount.setScale(0, BigDecimal.ROUND_UP));
			} else {
				apps.setTotalamount(totalamount != null ? totalamount.setScale(0, BigDecimal.ROUND_UP) : null);
			}

			if (dataPayLoad.get(CommonConstants.BADGENUMBER) != null) {
				NOCApplicationDetail applicationDetailModel = jdbcTemplate.queryForObject(
						QueryBuilder.SELECT_APPLICATION_DETAIL_QUERY, new Object[] { requestData.getApplicationId() },
						BeanPropertyRowMapper.newInstance(NOCApplicationDetail.class));

				if (applicationDetailModel != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					JSONObject applicationDetailData = objectMapper
							.readValue(applicationDetailModel.getApplicationDetail(), JSONObject.class);
					applicationDetailData.put(CommonConstants.BADGENUMBER,
							dataPayLoad.get(CommonConstants.BADGENUMBER));
					NOCApplicationDetail details = new NOCApplicationDetail();
					details.setApplicationDetail(applicationDetailData.toJSONString());
					details.setApplicationUuid(getAppIdUuid(requestData.getApplicationId()));
					JSONObject object = new JSONObject();
					object.put("NOCApplicationDetail", details);
					producer.push(updateNOCApplicationDetailsTopic, object);
				}
			} else if (dataPayLoad.get(CommonConstants.WITHDRAWAPPROVALAMOUNT) != null
					&& dataPayLoad.get(CommonConstants.WITHDRAWAPPROVALTAXAMOUNT) != null) {
				NOCApplicationDetail applicationDetailModel = jdbcTemplate.queryForObject(
						QueryBuilder.SELECT_APPLICATION_DETAIL_QUERY, new Object[] { requestData.getApplicationId() },
						BeanPropertyRowMapper.newInstance(NOCApplicationDetail.class));

				if (applicationDetailModel != null) {
					ObjectMapper objectMapper = new ObjectMapper();
					JSONObject applicationDetailData = objectMapper
							.readValue(applicationDetailModel.getApplicationDetail(), JSONObject.class);
					BigDecimal amountFromPayload = new BigDecimal(
							dataPayLoad.get(CommonConstants.WITHDRAWAPPROVALAMOUNT).toString());
					BigDecimal taxFromPayload = new BigDecimal(
							dataPayLoad.get(CommonConstants.WITHDRAWAPPROVALTAXAMOUNT).toString());
					if (amountFromPayload.compareTo(apps.getAmount()) > 0)
						throw new CustomException(CommonConstants.WITHDRAWAPPROVALAMOUNTERRORCODE,
								CommonConstants.WITHDRAWAPPROVALAMOUNTERROR);

					if (taxFromPayload.compareTo(apps.getGstAmount()) > 0)
						throw new CustomException(CommonConstants.WITHDRAWAPPROVALTAXAMOUNTERRORCODE,
								CommonConstants.WITHDRAWAPPROVALTAXAMOUNTERROR);

					applicationDetailData.put(CommonConstants.WITHDRAWAPPROVALAMOUNT,
							dataPayLoad.get(CommonConstants.WITHDRAWAPPROVALAMOUNT));
					applicationDetailData.put(CommonConstants.WITHDRAWAPPROVALTAXAMOUNT,
							dataPayLoad.get(CommonConstants.WITHDRAWAPPROVALTAXAMOUNT));

					NOCApplicationDetail details = new NOCApplicationDetail();
					details.setApplicationDetail(applicationDetailData.toJSONString());
					details.setApplicationUuid(getAppIdUuid(requestData.getApplicationId()));
					JSONObject object = new JSONObject();
					object.put("NOCApplicationDetail", details);
					producer.push(updateNOCApplicationDetailsTopic, object);
				}
			}

			List<NOCApplication> applists = Arrays.asList(apps);
			NOCRequestData dataApp = new NOCRequestData();
			dataApp.setRequestInfo(requestInfo);
			dataApp.setNocApplication(applists);
			producer.push(updateStatusNOCTopic, dataApp);

			return callWorkflow(requestData, applicationId, dataApp);
		} catch (Exception e) {
			return ResponseData.builder().responseInfo(
					ResponseInfo.builder().status(CommonConstants.FAIL).msgId("Exception : " + e.getMessage()).build())
					.build();
		}
	}

	public ResponseData callWorkflow(RequestData requestData, String applicationId, NOCRequestData dataApp) {
		try {
			log.info("NocRepository.callWorkflow :: Calling Workflow Service");
			ResponseInfo workflowResponse = workflowIntegration(requestData.getApplicationId(), requestData,
					requestData.getApplicationStatus());

			if (workflowResponse != null && workflowResponse.getStatus().equals(CommonConstants.SUCCESSFUL)) {
				log.info("NocRepository.callWorkflow :: Received Workflow Response Successful for id:{}",
						nocApplication.getNocNumber());

				producer.push(notificationTopic, dataApp);
				log.info("NocRepository.callWorkflow :: Notification Data Sent to Topic for id:{}",
						nocApplication.getNocNumber());

				return ResponseData.builder().responseInfo(ResponseInfo.builder().msgId(workflowResponse.getMsgId())
						.status(CommonConstants.SUCCESS).build()).applicationId(applicationId).build();
			} else {
				log.info(
						"NocRepository.callWorkflow :: Received Workflow Response Failed.Pushing Data to Fallback Topic for id:{}",
						nocApplication.getNocNumber());

				JSONObject fallbackObject = new JSONObject();
				fallbackObject.put("NOCApplication", nocApplication);
				producer.push(fallbackTopic, fallbackObject);
				log.info("NocRepository.callWorkflow :: Data pushed To fallback topic for id:{}",
						nocApplication.getNocNumber());

				return ResponseData.builder().responseInfo(
						ResponseInfo.builder().status(CommonConstants.FAIL).msgId(workflowResponse.getMsgId()).build())
						.build();
			}
		} catch (Exception e) {
			log.error("EXCEPTION -> Inside NocRepository.callWorkflow:: Id:{}", nocApplication.getNocNumber());
			log.info(
					"NocRepository.callWorkflow :: Received Workflow Response Failed.Pushing Data to Fallback Topic for id:{}",
					nocApplication.getNocNumber());

			JSONObject fallbackObject = new JSONObject();
			fallbackObject.put("NOCApplication", nocApplication);
			producer.push(fallbackTopic, fallbackObject);
			log.info("NocRepository.callWorkflow :: Data pushed To fallback topic for id:{}",
					nocApplication.getNocNumber());
			return ResponseData.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.FAIL).msgId("WORKFLOW ERROR").build())
					.build();
		}
	}

	public void setFallBack(String applicationId) {

		JSONArray actualResult = jdbcTemplate.query(QueryBuilder.SELECT_FALLBACK_QUERY, new Object[] { applicationId },
				columnsNocRowMapper);
		if (actualResult.isEmpty() || actualResult.get(0) == null || actualResult == null)
			throw new CustomException("FALLBACK DATA ERROR", "No data found");

		JSONObject jsonObject1 = (JSONObject) actualResult.get(0);
		nocApplication.setNocNumber(jsonObject1.get("nocnumber").toString());
		nocApplication.setAmount(
				jsonObject1.get("amount") != "" ? new BigDecimal(jsonObject1.get("amount").toString()) : null);
		nocApplication.setPerformanceBankGuarantee(jsonObject1.get("performancebankguaranteecharges") != ""
				? new BigDecimal(jsonObject1.get("performancebankguaranteecharges").toString())
				: null);
		nocApplication.setGstAmount(
				jsonObject1.get("gstamount") != "" ? new BigDecimal(jsonObject1.get("gstamount").toString()) : null);
		nocApplication.setTotalamount(
				jsonObject1.get("totalamount") != "" ? new BigDecimal(jsonObject1.get("totalamount").toString())
						: null);

		nocApplication.setApplicationStatus(jsonObject1.get("applicationstatus").toString());
		// nocApplication.setApplicantName(jsonObject1.get("applicantname").toString());
		nocApplication.setHouseNo(jsonObject1.get("housenumber").toString());
		nocApplication.setSector(jsonObject1.get("sector").toString());
		nocApplication.setApplicantName(jsonObject1.get("applicantname").toString());
		nocApplication.setApplicationUuid(jsonObject1.get("applicationuuid").toString());
		nocApplication.setNocApplicationRemark(
				NOCApplicationRemark.builder().previousRemarkId(jsonObject1.get("remarkid").toString()).build());
		nocApplication.setNocApplicationDetails(NOCApplicationDetail.builder()
				.applicationDetail(jsonObject1.get("applicationdetail").toString()).build());
		System.out.println(nocApplication);

	}

	/**
	 * Getting application uuid
	 * 
	 * @param required
	 *            applicationId
	 */
	public String getAppIdUuid(String applicationId) {
		String appId = "";
		JSONArray jsonArray = jdbcTemplate.query(QueryBuilder.SELECT_APPID_QUERY, new Object[] { applicationId },
				columnsNocRowMapper);
		if (!jsonArray.isEmpty()) {
			JSONObject obj = (JSONObject) jsonArray.get(0);
			appId = obj.get("application_uuid").toString();
		}
		return appId;
	}

	/**
	 * Getting remark data
	 * 
	 * @param required
	 *            applicationId
	 */
	public Integer findRemarks(String appId) {
		return jdbcTemplate.query(QueryBuilder.SELECT_REMARKS_QUERY, new Object[] { appId }, counterRowMapper);
	}

	/**
	 * Getting pricebook data
	 * 
	 * @param required
	 *            from date,categoryid,subcategory id,applicationType
	 */
	public Integer getpricebook(RequestData requestData, int flag) throws java.text.ParseException {
		Integer i = null;
		LocalDate date = LocalDate.parse(requestData.getDataPayload().get(CommonConstants.FROMDATE).toString());
		if (flag == 0) {
			i = jdbcTemplate.query(QueryBuilder.SELECT_PRICE_BOOK_QUERY,
					new Object[] { date.toString(), requestData.getDataPayload().get(CommonConstants.CATEGORYID),
							requestData.getDataPayload().get(CommonConstants.SUBCATEGORYID),
							requestData.getApplicationType() },
					counterRowMapper);
		} else if (flag == 1) {
			i = jdbcTemplate.query(QueryBuilder.SELECT_PRICE_BOOK_INBETWEEN_QUERY,
					new Object[] { requestData.getDataPayload().get(CommonConstants.CATEGORYID),
							requestData.getDataPayload().get(CommonConstants.SUBCATEGORYID), date.toString(),
							requestData.getApplicationType() },
					counterRowMapper);
		}
		return i;
	}

	/**
	 * Updating pricebook data
	 * 
	 * @param required
	 *            applicationType,RequestInfo, tenantId and category id and
	 *            subcategory id
	 */
	public NocResponse updatepricebookdate(RequestData requestData) {
		ResponseInfo res = new ResponseInfo();
		NOCPriceBook nb = new NOCPriceBook();

		NocResponse resinfo = new NocResponse();
		JSONObject dataPayLoad = requestData.getDataPayload();
		LocalDate date = LocalDate.parse(requestData.getDataPayload().get(CommonConstants.FROMDATE).toString());
		LocalDate yesterday = date.minusDays(1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
		Long time = System.currentTimeMillis();

		JSONArray oldData = jdbcTemplate.query(QueryBuilder.SELECT_PRICE_BOOK_INBETWEEN_QUERY,
				new Object[] { requestData.getDataPayload().get(CommonConstants.CATEGORYID),
						requestData.getDataPayload().get(CommonConstants.SUBCATEGORYID), date.toString(),
						requestData.getApplicationType() },
				columnsNocRowMapper);

		if (!oldData.isEmpty() && oldData.size() == 1) {
			JSONObject jsonObject = (JSONObject) oldData.get(0);

			PriceBookRequestData data = new PriceBookRequestData();
			nb.setPriceBookId(
					(jsonObject.get("price_book_id") == null ? "" : jsonObject.get("price_book_id").toString()));
			nb.setEffectiveToDate(formatter.format(yesterday));
			nb.setLastModifiedBy(requestData.getRequestInfo().getUserInfo().getUuid());
			nb.setCreatedTime(time);
			nb.setApplicationType(requestData.getApplicationType());
			nb.setTenantId(requestData.getTenantId());
			List<NOCPriceBook> applist1 = Arrays.asList(nb);
			data.setRequestInfo(requestData.getRequestInfo());
			data.setNocPriceBook(applist1);

			producer.push(updatePriceBookdate, data);
			NOCPriceBook app = new NOCPriceBook();
			String applicationId = UUID.randomUUID().toString();
			app.setPriceBookId(applicationId);

			app.setCategoryId((dataPayLoad.get(CommonConstants.CATEGORYID) == null ? ""
					: dataPayLoad.get(CommonConstants.CATEGORYID).toString()));

			app.setApplicationType(requestData.getApplicationType() == null ? "" : requestData.getApplicationType());

			app.setTenantId(requestData.getTenantId() == null ? "" : requestData.getTenantId());

			app.setSubCategoryId((dataPayLoad.get(CommonConstants.MINSQFT) == null ? ""
					: dataPayLoad.get(CommonConstants.MINSQFT).toString()));

			app.setSubCategoryId((dataPayLoad.get(CommonConstants.MAXSQFT) == null ? ""
					: dataPayLoad.get(CommonConstants.MAXSQFT).toString()));

			app.setSubCategoryId((dataPayLoad.get(CommonConstants.SUBCATEGORYID) == null ? ""
					: dataPayLoad.get(CommonConstants.SUBCATEGORYID).toString()));

			app.setAnnualPrice(dataPayLoad.get(CommonConstants.ANNUAL_PRICE) == null ? 0L
					: Long.parseLong(dataPayLoad.get(CommonConstants.ANNUAL_PRICE).toString()));

			app.setPerWeekPrice(dataPayLoad.get(CommonConstants.PER_WEEK_PRICE) == null ? 0L
					: Long.parseLong(dataPayLoad.get(CommonConstants.PER_WEEK_PRICE).toString()));

			app.setPerMonthPrice(dataPayLoad.get(CommonConstants.PER_MONTH_PRICE) == null ? 0L
					: Long.parseLong(dataPayLoad.get(CommonConstants.PER_MONTH_PRICE).toString()));

			app.setFixedPrice(dataPayLoad.get(CommonConstants.FIXED_PRICE) == null ? 0L
					: Long.parseLong(dataPayLoad.get(CommonConstants.FIXED_PRICE).toString()));

			app.setPerDayPrice(dataPayLoad.get(CommonConstants.PER_DAY_PRICE) == null ? 0L
					: Long.parseLong(dataPayLoad.get(CommonConstants.PER_DAY_PRICE).toString()));

			app.setCalculationSequence(jsonObject.get(CommonConstants.CALCULATION_SEQUENCE) == null ? 0
					: Integer.parseInt(jsonObject.get(CommonConstants.CALCULATION_SEQUENCE).toString()));

			app.setCalculationType(jsonObject.get(CommonConstants.CALCULATION_TYPE) == null ? ""
					: jsonObject.get(CommonConstants.CALCULATION_TYPE).toString());

			app.setMinSqft(jsonObject.get(CommonConstants.MINVALUESQFT) == null ? 0L
					: Long.parseLong(jsonObject.get(CommonConstants.MINVALUESQFT).toString()));
			app.setMaxSqft(jsonObject.get(CommonConstants.MAXVALUESQFT) == null ? 0L
					: Long.parseLong(jsonObject.get(CommonConstants.MAXVALUESQFT).toString()));

			app.setEffectiveFromDate(formatter.format(date));
			app.setCreatedBy(requestData.getRequestInfo().getUserInfo().getUuid());
			app.setCreatedTime(time);
			app.setTenantId(requestData.getTenantId());
			app.setApplicationType(requestData.getApplicationType());
			List<NOCPriceBook> nocPriceBook = Arrays.asList(app);

			PriceBookRequestData data1 = new PriceBookRequestData();
			data1.setRequestInfo(requestData.getRequestInfo());
			data1.setNocPriceBook(nocPriceBook);
			producer.push(insertPriceBook, data1);

			res.setStatus(CommonConstants.SUCCESS);
			resinfo.setResposneInfo(res);
			return resinfo;

		} else {
			res.setStatus(CommonConstants.FAIL);
			res.setMsgId("Unable to update the master data. / Zero or More than records found.");
			resinfo.setResposneInfo(res);
			return resinfo;
		}
	}

	/**
	 * Getting pricebook data
	 * 
	 * @param required
	 *            tenantId,applicationType
	 */
	public List<NOCPriceBook> viewPriceBook(RequestData requestdata) {

		List<NOCPriceBook> preparedStatementValues1 = jdbcTemplate.query(QueryBuilder.SELECT_PRICE_BOOK_GETALL_QUERY,
				new Object[] { requestdata.getTenantId(), requestdata.getApplicationType() }, priceRowMapper);

		return preparedStatementValues1;

	}

	/**
	 * Getting pricebook data for requested pricebook id
	 * 
	 * @param required
	 *            pricebook id
	 */
	public List<NOCPriceBook> viewPriceBookById(RequestData requestdata) {
		JSONObject dataPayLoad = requestdata.getDataPayload();

		List<NOCPriceBook> preparedStatementValues1 = jdbcTemplate.query(QueryBuilder.SELECT_PRICE_BOOK_ID_QUERY,
				new Object[] { dataPayLoad.get(CommonConstants.PRICE_BOOK_ID).toString(), requestdata.getTenantId(),
						requestdata.getApplicationType() },
				priceRowMapper);

		return preparedStatementValues1;

	}

	/**
	 * Updating pricebook data
	 * 
	 * @param required
	 *            RequestInfo,datapayload and tenantId datapayload includes
	 *            categoryid,sub category id,annual,perday,per month,fixed price
	 */
	public NocResponse updatepricebook(RequestData requestData) {
		ResponseInfo res = new ResponseInfo();
		NocResponse resinfo = new NocResponse();
		JSONObject dataPayLoad = requestData.getDataPayload();

		NOCPriceBook app = new NOCPriceBook();
		app.setCategoryId((dataPayLoad.get(CommonConstants.CATEGORYID) == null ? ""
				: dataPayLoad.get(CommonConstants.CATEGORYID).toString()));

		app.setApplicationType(requestData.getApplicationType() == null ? "" : requestData.getApplicationType());

		app.setTenantId(requestData.getTenantId() == null ? "" : requestData.getTenantId());

		app.setSubCategoryId((dataPayLoad.get(CommonConstants.SUBCATEGORYID) == null ? ""
				: dataPayLoad.get(CommonConstants.SUBCATEGORYID).toString()));

		app.setAnnualPrice(Long.parseLong(dataPayLoad.get(CommonConstants.ANNUAL_PRICE).toString()) == 0 ? 0L
				: Long.parseLong(dataPayLoad.get(CommonConstants.ANNUAL_PRICE).toString()));

		app.setPerWeekPrice(Long.parseLong(dataPayLoad.get(CommonConstants.PER_WEEK_PRICE).toString()) == 0 ? 0L
				: Long.parseLong(dataPayLoad.get(CommonConstants.PER_WEEK_PRICE).toString()));

		app.setPerMonthPrice(Long.parseLong(dataPayLoad.get(CommonConstants.PER_MONTH_PRICE).toString()) == 0 ? 0L
				: Long.parseLong(dataPayLoad.get(CommonConstants.PER_MONTH_PRICE).toString()));

		app.setFixedPrice(Long.parseLong(dataPayLoad.get(CommonConstants.FIXED_PRICE).toString()) == 0 ? 0L
				: Long.parseLong(dataPayLoad.get(CommonConstants.FIXED_PRICE).toString()));

		app.setPerDayPrice(Long.parseLong(dataPayLoad.get(CommonConstants.PER_DAY_PRICE).toString()) == 0 ? 0L
				: Long.parseLong(dataPayLoad.get(CommonConstants.PER_DAY_PRICE).toString()));

		app.setCreatedTime(System.currentTimeMillis());
		List<NOCPriceBook> nocPriceBook = Arrays.asList(app);

		PriceBookRequestData data = new PriceBookRequestData();
		data.setRequestInfo(requestData.getRequestInfo());
		data.setNocPriceBook(nocPriceBook);
		producer.push(updatePriceBook, data);
		res.setStatus(CommonConstants.SUCCESS);
		resinfo.setResposneInfo(res);
		return resinfo;
	}

	/**
	 * Generating Report Data
	 * 
	 * @param required
	 *            list of ReportModel
	 */
	public void generateReport(List<ReportModel> listOfResult) {
		String proccessingInsertSql = QueryBuilder.INSERT_NOC_AVERAGE_TIME_QUERY;
		listOfResult.stream().forEach(reportModel -> {
			PreparedStatementCreator psc = new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
					final PreparedStatement ps = connection.prepareStatement(proccessingInsertSql, new String[] {});

					ps.setString(1, (reportModel.getTenantId() == null ? "" : reportModel.getTenantId()));
					ps.setString(2, reportModel.getApplicationType());
					ps.setDouble(3, Double.parseDouble(reportModel.getTotalAverage()));
					ps.setDouble(4, Double.parseDouble(reportModel.getTotalAveragePending10DayasTo30Days()));
					ps.setDouble(5, Double.parseDouble(reportModel.getTotalAveragePendingGreaterThan30Days()));
					ps.setTimestamp(6, getCurrentDate());
					ps.setInt(7, 1);
					return ps;
				}
			};
			jdbcTemplate.update(psc);
		});
	}

	public static Timestamp getCurrentDate() {
		return new Timestamp(System.currentTimeMillis());
	}

	public JSONArray getRefundApplications() {
		return jdbcTemplate.query(QueryBuilder.GET_REFUND_APPLICATIONS_QUERY, columnsNocRowMapper);

	}

}
