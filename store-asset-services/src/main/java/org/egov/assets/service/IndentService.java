package org.egov.assets.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.FinancialYear;
import org.egov.assets.model.IndenUserListResponse;
import org.egov.assets.model.Indent;
import org.egov.assets.model.Indent.IndentStatusEnum;
import org.egov.assets.model.Indent.IndentTypeEnum;
import org.egov.assets.model.IndentDetail;
import org.egov.assets.model.IndentRequest;
import org.egov.assets.model.IndentResponse;
import org.egov.assets.model.IndentSearch;
import org.egov.assets.model.Material;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.Tenant;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.IndentDetailJdbcRepository;
import org.egov.assets.repository.IndentJdbcRepository;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.StoreJdbcRepository;
import org.egov.assets.repository.entity.IndentDetailEntity;
import org.egov.assets.repository.entity.IndentEntity;
import org.egov.assets.util.InventoryUtilities;
import org.egov.assets.web.controller.AssetRepository;
import org.egov.assets.wf.WorkflowIntegrator;
import org.egov.assets.wf.model.ProcessInstance;
import org.egov.assets.wf.model.ProcessInstanceResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author mani In search for PO show only records which 1. If appconfig is to
 *         consider whole indentquantiy return podetails where
 *         qty-(poordered+issued) >0 2. If not return podetails qty-poordered 3.
 *         Maintain seperate field for ui quantity 4. all qty in base uom 5.
 *         give api for status change 6. give api for force close 7. Consider
 *         the status closed for not to show in issue or po
 * 
 */
@Service
@Transactional(readOnly = true)
public class IndentService extends DomainService {
	/*
	 * @Autowired private StoreRepository storeRepository;
	 * 
	 * @Autowired private DepartmentRepository departmentRepository;
	 */

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private IndentJdbcRepository indentRepository;

	@Value("${inv.indents.save.topic}")
	private String saveTopic;

	@Value("${inv.indents.save.key}")
	private String saveKey;

	@Value("${inv.indents.update.topic}")
	private String updateTopic;

	@Value("${inv.indents.update.key}")
	private String updateKey;

	@Value("${inv.indents.updatestatus.topic}")
	private String updatestatusTopic;

	@Value("${inv.indents.updatestatus.key}")
	private String updatestatusKey;

	@Autowired
	private StoreService storeService;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private IndentDetailJdbcRepository indentDetailJdbcRepository;

	@Autowired
	private StoreJdbcRepository storeJdbcRepository;
	@Autowired
	private MdmsRepository mdmsRepository;

	@Value("${inv.indents.addquantity.topic}")
	private String addQuantityTopic;

	@Value("${inv.indents.subtractquantity.topic}")
	private String subtractQuantityTopic;

	@Value("${inv.indents.addquantity.key}")
	private String addQuantitykey;

	@Value("${inv.indents.subtractquantity.key}")
	private String subtractQuantityKey;

	@Autowired
	private NumberGenerator numberGenerator;
	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	private static final Logger LOG = LoggerFactory.getLogger(IndentService.class);

	@Transactional
	public IndentResponse create(IndentRequest indentRequest) {

		try {
			indentRequest = fetchRelated(indentRequest);
			validate(indentRequest.getIndents(), Constants.ACTION_CREATE);
			List<String> sequenceNos = indentRepository.getSequence(Indent.class.getSimpleName(),
					indentRequest.getIndents().size());
			int i = 0;
			for (Indent b : indentRequest.getIndents()) {
				b.setId(sequenceNos.get(i));
				// move to id-gen with format <ULB short code>/<Store
				// Code>/<fin. Year>/<serial No.>
				b.setIndentNumber(getIndentNumber(b, indentRequest.getRequestInfo()));
				i++;
				int j = 0;
				// TO-DO : when workflow implemented change this to created
				WorkFlowDetails workFlowDetails = indentRequest.getWorkFlowDetails();
				workFlowDetails.setBusinessId(b.getIndentNumber());
				workflowIntegrator.callWorkFlow(indentRequest.getRequestInfo(), workFlowDetails, b.getTenantId());

				b.setIndentStatus(IndentStatusEnum.CREATED);
				b.setAuditDetails(getAuditDetails(indentRequest.getRequestInfo(), Constants.ACTION_CREATE));

				List<String> detailSequenceNos = indentRepository.getSequence(IndentDetail.class.getSimpleName(),
						b.getIndentDetails().size());
				setUpdatedQuantities(b);
				for (IndentDetail d : b.getIndentDetails()) {
					// save quantity in base uom
					BigDecimal quantityInBaseUom = InventoryUtilities.getQuantityInBaseUom(d.getUserQuantity(),
							d.getUom().getConversionFactor());
					d.setIndentQuantity(quantityInBaseUom);
					d.setId(detailSequenceNos.get(j));
					d.setTenantId(b.getTenantId());
					j++;
				}
			}
			kafkaQue.send(saveTopic, saveKey, indentRequest);
			IndentResponse response = new IndentResponse();
			response.setIndents(indentRequest.getIndents());
			response.setResponseInfo(getResponseInfo(indentRequest.getRequestInfo()));
			return response;
		} catch (Exception e) {
			throw e;
		}

	}

	private String getIndentNumber(Indent b, RequestInfo info) {
		InvalidDataException errors = new InvalidDataException();
		ObjectMapper mapper = new ObjectMapper();
		net.minidev.json.JSONArray tenantStr = mdmsRepository.getByCriteria(b.getTenantId(), "tenant", "tenants",
				"code", b.getTenantId(), info);

		Tenant tenant = mapper.convertValue(tenantStr.get(0), Tenant.class);
		if (tenant == null) {
			errors.addDataError(ErrorCode.CITY_CODE_NOT_AVAILABLE.getCode(), b.getTenantId());
		}
		String finYearRange = "";
		net.minidev.json.JSONArray finYears = mdmsRepository.getByCriteria(b.getTenantId(), "egf-master",
				"FinancialYear", null, null, info);
		outer: for (int i = 0; i < finYears.size(); i++) {
			FinancialYear fin = mapper.convertValue(finYears.get(i), FinancialYear.class);
			LOG.info("Indentdate" + b.getIndentDate());
			LOG.info("FinYear start date:" + fin.getStartingDate());
			if (b.getIndentDate() >= fin.getStartingDate().getTime()
					&& b.getIndentDate() <= fin.getEndingDate().getTime()) {
				finYearRange = fin.getFinYearRange();
				break outer;
			}
		}

		if (finYearRange.isEmpty()) {
			errors.addDataError(ErrorCode.FIN_YEAR_NOT_EXIST.getCode(), b.getIndentDate().toString());
		}
		if (errors.getValidationErrors().size() > 0) {
			throw errors;
		}
		String seq = "";
		if (b.getIndentType().equals(IndentTypeEnum.INDENTNOTE))
			seq = "IND-" + tenant.getCity().getCode() + "-" + b.getIndentStore().getCode() + "-" + finYearRange;
		else
			seq = "TRIN-" + tenant.getCity().getCode() + "-" + b.getIndentStore().getCode() + "-" + finYearRange;
		return seq + "-" + numberGenerator.getNextNumber(seq, 5);
	}

	@Transactional
	public IndentResponse update(IndentRequest indentRequest) {

		try {
			String tenantId = "";
			indentRequest = fetchRelated(indentRequest);

			String indentNumber = "";
			List<String> ids = new ArrayList<>();
			validate(indentRequest.getIndents(), Constants.ACTION_CREATE);
			for (Indent b : indentRequest.getIndents()) {
				// b.setIndentType(IndentTypeEnum.INDENTNOTE);
				if (!isEmpty(b.getAction())) {
					updateIndentQuantity(b);
				}
				int j = 0;
				if (indentNumber.isEmpty()) {
					indentNumber = b.getIndentNumber();
				}
				b.setAuditDetails(getAuditDetails(indentRequest.getRequestInfo(), Constants.ACTION_UPDATE));
				for (IndentDetail d : b.getIndentDetails()) {
					if (d.getId() == null)
						d.setId(indentRepository.getSequence(IndentDetail.class.getSimpleName(), 1).get(0));
					ids.add(d.getId());
					BigDecimal quantityInBaseUom = InventoryUtilities.getQuantityInBaseUom(d.getUserQuantity(),
							d.getUom().getConversionFactor());
					d.setIndentQuantity(quantityInBaseUom);
					d.setTenantId(b.getTenantId());
					if (tenantId.isEmpty())
						tenantId = b.getTenantId();
					j++;
				}
			}

			kafkaQue.send(saveTopic, saveKey, indentRequest);
			indentDetailJdbcRepository.markDeleted(ids, tenantId, "indentdetail", "indentNumber", indentNumber);
			IndentResponse response = new IndentResponse();
			response.setIndents(indentRequest.getIndents());
			response.setResponseInfo(getResponseInfo(indentRequest.getRequestInfo()));
			return response;
		} catch (CustomBindException e) {
			throw e;
		}

	}

	public IndentResponse search(IndentSearch is, RequestInfo info) {

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Uom> uomMap = getUoms(is.getTenantId(), mapper, info);
		Map<String, Material> materialMap = getMaterial(is.getTenantId(), mapper, info);

		IndentResponse response = new IndentResponse();
		Pagination<Indent> search = indentRepository.search(is);
		if (!search.getPagedData().isEmpty()) {
			List<String> indentNumbers = new ArrayList<>();
			for (Indent indent : search.getPagedData()) {
				indentNumbers.add(indent.getIndentNumber());
			}

			List<IndentDetailEntity> indentDetails = indentDetailJdbcRepository.find(indentNumbers, is.getTenantId(),
					is.getSearchPurpose());

			if (indentDetails != null) {
				IndentDetail detail = null;
				for (Indent indent : search.getPagedData()) {

					if (indent.getIssueStore() != null && indent.getIssueStore().getCode() != null) {
						indent.setIssueStore(getStore(indent.getIssueStore().getCode(), is.getTenantId()));
					}
					if (indent.getIndentStore() != null && indent.getIndentStore().getCode() != null) {
						indent.setIndentStore(getStore(indent.getIndentStore().getCode(), is.getTenantId()));
					}

					for (IndentDetailEntity detailEntity : indentDetails) {
						if (indent.getIndentNumber().equalsIgnoreCase(detailEntity.getIndentNumber())) {
							detail = detailEntity.toDomain();
							Material material = (materialMap.get(detail.getMaterial().getCode()));
							/*
							 * LOG.debug(material.getCode());
							 * LOG.debug(material.getPurchaseUom().getCode());
							 * LOG.debug(uomMap.get(material.getPurchaseUom().getCode()).getCode());
							 */
							material.setPurchaseUom(uomMap.get(
									(material.getPurchaseUom() != null ? material.getPurchaseUom().getCode() : "")));
							material.setStockingUom(uomMap.get(
									(material.getStockingUom() != null ? material.getStockingUom().getCode() : "")));
							material.setBaseUom(
									uomMap.get((material.getBaseUom() != null ? material.getBaseUom().getCode() : "")));
							detail.setMaterial(material);
							indent.addIndentDetailsItem(detail);

							detail.setUom(
									uomMap.get((detail.getUom().getCode() != null ? detail.getUom().getCode() : "")));

						}
					}
				}
			}
		}
		response.setIndents(search.getPagedData());
		response.setPage(getPage(search));
		return response;

	}

	private void updateIndentQuantity(Indent indent) {
		setUpdatedQuantities(indent);
		if (!isEmpty(indent.getAction()) && "Add".equalsIgnoreCase(indent.getAction())) {
			kafkaQue.send(addQuantityTopic, addQuantitykey, indent);
		}
		if (!isEmpty(indent.getAction()) && "Subtract".equalsIgnoreCase(indent.getAction())) {
			kafkaQue.send(subtractQuantityTopic, subtractQuantityKey, indent);
		}
	}

	private Indent setUpdatedQuantities(Indent indent) {
		for (IndentDetail indentDetail : indent.getIndentDetails()) {
			if (isEmpty(indentDetail.getIndentIssuedQuantity())) {
				indentDetail.setIndentIssuedQuantity(BigDecimal.valueOf(0L));
			}
			if (isEmpty(indentDetail.getInterstoreRequestQuantity())) {
				indentDetail.setInterstoreRequestQuantity(BigDecimal.valueOf(0L));
			}
			if (isEmpty(indentDetail.getPoOrderedQuantity())) {
				indentDetail.setPoOrderedQuantity(BigDecimal.valueOf(0L));
			}
			if (isEmpty(indentDetail.getTotalProcessedQuantity())) {
				indentDetail.setTotalProcessedQuantity(BigDecimal.valueOf(0L));
			}
		}
		return indent;
	}

	private void validate(List<Indent> indents, String method) {
		InvalidDataException errors = new InvalidDataException();
		try {
			Long currentDate = currentEpochWithoutTime();
			currentDate = currentDate + (24 * 60 * 60 * 1000) - 1;
			// LOG.info("CurrentDate is " + toDateStr(currentDate));

			Long ll = new Date().getTime();
			switch (method) {
			case Constants.ACTION_UPDATE: {
				if (indents == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "indents", "null");
				}

				for (Indent indent : indents) {
					IndentEntity entity = indentRepository.findById(new IndentEntity().toEntity(indent));
					if (entity.getIndentStatus().equalsIgnoreCase(IndentStatusEnum.CREATED.name())) {
						continue;
					} else {
						errors.addDataError(ErrorCode.UPDATE_NOT_ALLOWED.getCode(), "indent", entity.getIndentStatus(),
								indent.getIndentNumber());
					}
				}
				if (errors.getValidationErrors().size() > 0)
					break;

			}

			case Constants.ACTION_CREATE: {
				if (indents == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "indents", "null");
				}
				for (Indent indent : indents) {
					LOG.info("-----------------------------------------------------------");
					LOG.info("CurrentDate is " + toDateStr(currentDate));
					LOG.info("indentDate is " + toDateStr(indent.getIndentDate()));
					LOG.info("now is " + toDateStr(ll));
					LOG.info("compare  " + ll.compareTo(currentDate));
					LOG.info("compare  " + ll.compareTo(currentDate));
					LOG.info("-----------------------------------------------------------");

					if (indent.getIndentDate().compareTo(currentDate) >= 0) {
						String indentDate = convertEpochtoDate(indent.getIndentDate());
						errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "indentDate", indentDate);
					}

					if (indent.getIndentType().equals(IndentTypeEnum.TRANSFERINDENT)) {

						if (indent.getIndentStore() == null || indent.getIndentStore().getCode() == null)
							errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "Indenting Store",
									"indentType=Transfer Indent", "");
						else if (indent.getIndentStore().getCode().equalsIgnoreCase(indent.getIssueStore().getCode()))
							errors.addDataError(ErrorCode.SHOULD_BE_DIFFERENT.getCode(), "Indenting Store",
									"Issuing store");
					}
					// commeneted as of now to support the past dated entries
					/*
					 * if(indent.getExpectedDeliveryDate().compareTo( currentDate) < 0) {
					 * errors.addDataError(ErrorCode.DATE_GE_CURRENTDATE.getCode (),
					 * "expectedDeliveryDate",indent.getExpectedDeliveryDate(). toString()); }
					 */
					if (indent.getExpectedDeliveryDate() != null
							&& indent.getIndentDate().compareTo(indent.getExpectedDeliveryDate()) > 0) {
						LOG.info("expectedDeliveryDate=" + toDateStr(indent.getExpectedDeliveryDate()));
						LOG.info("indentDate=" + toDateStr(indent.getIndentDate()));

						String expectedDeliveryDate = convertEpochtoDate(indent.getExpectedDeliveryDate());
						String indentDate = convertEpochtoDate(indent.getIndentDate());
						errors.addDataError(ErrorCode.DATE1_GE_DATE2.getCode(), "expectedDeliveryDate", "indentDate",
								expectedDeliveryDate, indentDate);
					}
					List<String> materialCodes = new ArrayList<>();
					int i = 0;
					for (IndentDetail detail : indent.getIndentDetails()) {
						++i;
						if (materialCodes.isEmpty()) {
							materialCodes.add(detail.getMaterial().getCode());
						} else {
							if (materialCodes.indexOf(detail.getMaterial().getCode()) == -1) {
								materialCodes.add(detail.getMaterial().getCode());
							} else {
								errors.addDataError(ErrorCode.REPEATED_VALUE.getCode(), "material",
										detail.getMaterial().getCode(), " at serial no. " + i + " and "
												+ (materialCodes.indexOf(detail.getMaterial().getCode()) + 1));
							}
						}

						/*
						 * if (indent.getIndentPurpose().equals(IndentPurposeEnum.CAPITAL)) { if
						 * (detail.getProjectCode() == null || detail.getProjectCode().getCode() ==
						 * null) errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(),
						 * "projectCode", "indentPurpose=Capital", "at serail no. " + i); }
						 */
						/*
						 * if
						 * (indent.getIndentPurpose().equals(IndentPurposeEnum.REPAIRSANDMAINTENANCE)) {
						 * if (detail.getAsset() == null || detail.getAsset().getCode() == null)
						 * errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "assetCode",
						 * "indentPurpose=Repairs and Maintenance", "at seraill no. " + i); }
						 */

					}
				}

			}
				break;

			}
		} catch (IllegalArgumentException e) {

		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;

	}

	public IndentRequest fetchRelated(IndentRequest indentRequest) {
		String tenantId = indentRequest.getIndents().get(0).getTenantId();
		ObjectMapper mapper = new ObjectMapper();

		RequestInfo requestInfo = indentRequest.getRequestInfo();

		Map<String, Uom> uomMap = getUoms(tenantId, mapper, requestInfo);
		Map<String, Material> materialMap = getMaterial(tenantId, mapper, requestInfo);
		for (Indent indent : indentRequest.getIndents()) {
			// fetch related items
			if (indent.getIssueStore() != null && indent.getIssueStore().getCode() != null) {

				indent.getIssueStore().setTenantId(tenantId);
				Store issueStore = getStore(indent.getIssueStore().getCode(), tenantId);
				if (issueStore == null) {
					throw new InvalidDataException("issueStore", "issueStore.invalid", " Invalid issueStore");
				}
				indent.setIssueStore(issueStore);
			}

			if (indent.getIndentStore() != null) {
				indent.getIndentStore().setTenantId(tenantId);
				Store indentStore = getStore(indent.getIndentStore().getCode(), tenantId);
				if (indentStore == null) {
					throw new InvalidDataException("indentStore", "indentStore.invalid", " Invalid indentStore");
				}
				indent.setIndentStore(indentStore);
			}

			for (IndentDetail detail : indent.getIndentDetails()) {

				detail.setUom(uomMap.get(detail.getUom().getCode()));
				detail.setMaterial(materialMap.get(detail.getMaterial().getCode()));
				/*
				 * if(detail.getAsset().getCode()!=null) { Asset
				 * a=assetRepository.findByCode(detail.getAsset(),indentRequest.getRequestInfo()
				 * ); detail.setAsset(a); }
				 */

			}

			/*
			 * if (indent.getDepartment() != null) { Department department =
			 * restTemplate.postForEntity("", indentRequest.getRequestInfo(),
			 * Department.class); if (department == null) { throw new
			 * InvalidDataException("department", "department.invalid",
			 * " Invalid department"); } indent.setDepartment(department); }
			 */

		}
		return indentRequest;
	}

	private Map<String, Material> getMaterial(String tenantId, final ObjectMapper mapper, RequestInfo requestInfo) {
		net.minidev.json.JSONArray responseJSONArray = mdmsRepository.getByCriteria(tenantId, "store-asset", "Material",
				null, null, requestInfo);
		Map<String, Material> materialMap = new HashMap<>();

		if (responseJSONArray != null && responseJSONArray.size() > 0) {
			for (int i = 0; i < responseJSONArray.size(); i++) {
				Material material = mapper.convertValue(responseJSONArray.get(i), Material.class);
				materialMap.put(material.getCode(), material);
			}

		}
		return materialMap;
	}

	private Store getStore(String storeCode, String tenantId) {
		StoreGetRequest storeGetRequest = getStoreGetRequest(storeCode, tenantId);
		List<Store> storeList = storeService.search(storeGetRequest).getStores();
		if (storeList.size() == 1) {
			return storeList.get(0);
		} else {
			return null;
		}
	}

	private StoreGetRequest getStoreGetRequest(String storeCode, String tenantId) {
		return StoreGetRequest.builder().code(Arrays.asList(storeCode)).tenantId(tenantId).active(true).build();
	}

	private Map<String, Uom> getUoms(String tenantId, final ObjectMapper mapper, RequestInfo requestInfo) {
		net.minidev.json.JSONArray responseJSONArray = mdmsRepository.getByCriteria(tenantId, "common-masters", "UOM",
				null, null, requestInfo);
		Map<String, Uom> uomMap = new HashMap<>();

		if (responseJSONArray != null && responseJSONArray.size() > 0) {
			for (int i = 0; i < responseJSONArray.size(); i++) {
				Uom uom = mapper.convertValue(responseJSONArray.get(i), Uom.class);
				uomMap.put(uom.getCode(), uom);
			}

		}
		return uomMap;
	}

	private String convertEpochtoDate(Long date) {
		Date epoch = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String s2 = format.format(epoch);
		return s2;
	}

	public PDFResponse printPdf(IndentSearch is, RequestInfo requestInfo) {
		IndentResponse indentResponse = search(is, requestInfo);
		if (!indentResponse.getIndents().isEmpty() && indentResponse.getIndents().size() == 1) {
			JSONObject requestMain = new JSONObject();
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			DateTimeFormatter wfdateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			ObjectMapper mapper = new ObjectMapper();
			try {
				JSONObject reqInfo = (JSONObject) new JSONParser().parse(mapper.writeValueAsString(requestInfo));
				requestMain.put("RequestInfo", reqInfo);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			JSONArray indents = new JSONArray();
			for (Indent in : indentResponse.getIndents()) {
				JSONObject indent = new JSONObject();
				indent.put("indentNumber", in.getIndentNumber());
				indent.put("storeName", in.getIndentStore().getName());
				indent.put("indentingStoreName", in.getIndentStore().getName());
				indent.put("indentingStoreDept", in.getIndentStore().getDepartment().getName());
				if (in.getIndentDate() != null) {
					Instant indentDate = Instant.ofEpochMilli(in.getIndentDate());
					indent.put("indentDate", fmt.format(indentDate.atZone(ZoneId.systemDefault())));
				} else {
					indent.put("indentDate", in.getIndentDate());
				}

				if (in.getIssueStore() != null && in.getIssueStore().getCode() != null) {
					indent.put("issuingStoreName", in.getIssueStore().getName());
					indent.put("issuingStoreDept", in.getIssueStore().getDepartment().getName());
				}
				indent.put("indentStatus", in.getIndentStatus());
				indent.put("indentPurpose", in.getIndentPurpose());
				indent.put("narration", in.getNarration());
				indent.put("inventoryType", in.getInventoryType());
				indent.put("deliveryDate", in.getExpectedDeliveryDate());
				indent.put("createdBy", in.getIndentCreatedBy());
				indent.put("designation", in.getDesignation());

				JSONArray indentDetails = new JSONArray();
				int i = 1;
				for (IndentDetail detail : in.getIndentDetails()) {
					JSONObject indentDetail = new JSONObject();
					indentDetail.put("srNo", i++);
					indentDetail.put("materialCode", detail.getMaterial().getCode());
					indentDetail.put("materialName", detail.getMaterial().getName());
					indentDetail.put("materialDescription", detail.getMaterial().getDescription());
					indentDetail.put("uomName", detail.getUom().getName());
					indentDetail.put("indentPurpose", in.getIndentPurpose());
					indentDetail.put("workDetailsRemark", detail.getRemarks());
					indentDetail.put("requiredQuantity", detail.getIndentQuantity());
					indentDetails.add(indentDetail);
				}
				indent.put("materialDetails", indentDetails);

				ProcessInstanceResponse workflowData = workflowIntegrator.getWorkflowDataByID(requestInfo,
						in.getIndentNumber(), in.getTenantId());
				JSONArray workflows = new JSONArray();
				int wfSrNo=1;
				for (int j = 0; j < workflowData.getProcessInstances().size(); j++) {
					ProcessInstance processData = workflowData.getProcessInstances().get(j);
					Instant wfDate = Instant.ofEpochMilli(processData.getAuditDetails().getCreatedTime());
					// Need to integrate Workflow
					JSONObject jsonWork = new JSONObject();
					// fmt.format()
					jsonWork.put("srNo", wfSrNo);
					jsonWork.put("date", wfdateFormat.format(wfDate.atZone(ZoneId.systemDefault())));
					jsonWork.put("updatedBy", processData.getAssigner().getName());
					jsonWork.put("comments", processData.getComment());
					if (processData.getAssignee() == null) {
						if (!processData.getState().getIsTerminateState()) {
							jsonWork.put("currentAssignee",
									processData.getState().getActions().get(0).getRoles().get(0));
						} else
							jsonWork.put("currentAssignee", "NA");
					} else
						jsonWork.put("currentAssignee", processData.getAssignee().getName());

					jsonWork.put("status", processData.getState().getApplicationStatus());
					workflows.add(jsonWork);
					wfSrNo++;
				}
				indent.put("workflowDetails", workflows);
				indents.add(indent);
				if (is.getIndentType().equals(IndentTypeEnum.INDENTNOTE.toString()))
					requestMain.put("IndentNote", indents);
				if (is.getIndentType().equals(IndentTypeEnum.TRANSFERINDENT.toString()))
					requestMain.put("IndentNoteTransfer", indents);
			}
			if (is.getIndentType().equals(IndentTypeEnum.INDENTNOTE.toString()))
				return pdfServiceReposistory.getPrint(requestMain, "store-asset-indent-note", is.getTenantId());
			if (is.getIndentType().equals(IndentTypeEnum.TRANSFERINDENT.toString()))
				return pdfServiceReposistory.getPrint(requestMain, "store-asset-indent-transfer", is.getTenantId());

		}
		return PDFResponse.builder()
				.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();
	}

	@Transactional
	public IndentResponse updateStatus(IndentRequest indentRequest) {

		try {
			WorkFlowDetails workFlowDetails = workflowIntegrator.callWorkFlow(indentRequest.getRequestInfo(), indentRequest.getWorkFlowDetails(),
					indentRequest.getWorkFlowDetails().getTenantId());
			indentRequest.setWorkFlowDetails(workFlowDetails);
			kafkaQue.send(updatestatusTopic, updatestatusKey, indentRequest);
			IndentResponse response = new IndentResponse();
			response.setIndents(indentRequest.getIndents());
			response.setResponseInfo(getResponseInfo(indentRequest.getRequestInfo()));
			return response;
		} catch (CustomBindException e) {
			throw e;
		}

	}

	@Transactional
	public IndenUserListResponse getCreatorList(RequestInfo requestInfo,String tenantId) {

		try {
			IndenUserListResponse response = new IndenUserListResponse();
			List<String> usernameList = indentRepository.searchcreatorlist(tenantId);
			response.setUsers(usernameList);
			response.setResponseInfo(getResponseInfo(requestInfo));
			return response;
		} catch (Exception e) {
			throw e;
		}

	}

}