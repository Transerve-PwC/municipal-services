package org.egov.assets.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.Department;
import org.egov.assets.model.Fifo;
import org.egov.assets.model.FifoRequest;
import org.egov.assets.model.FifoResponse;
import org.egov.assets.model.Indent.IndentStatusEnum;
import org.egov.assets.model.IndentDetail;
import org.egov.assets.model.IndentResponse;
import org.egov.assets.model.IndentSearch;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialIssue;
import org.egov.assets.model.MaterialIssue.IssueTypeEnum;
import org.egov.assets.model.MaterialIssue.MaterialIssueStatusEnum;
import org.egov.assets.model.MaterialIssueDetail;
import org.egov.assets.model.MaterialIssueRequest;
import org.egov.assets.model.MaterialIssueResponse;
import org.egov.assets.model.MaterialIssueSearchContract;
import org.egov.assets.model.MaterialIssuedFromReceipt;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptDetailSearch;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.IndentDetailJdbcRepository;
import org.egov.assets.repository.MaterialIssueDetailJdbcRepository;
import org.egov.assets.repository.MaterialIssueJdbcRepository;
import org.egov.assets.repository.MaterialIssuedFromReceiptJdbcRepository;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.entity.FifoEntity;
import org.egov.assets.repository.entity.IndentDetailEntity;
import org.egov.assets.repository.entity.IndentEntity;
import org.egov.assets.repository.entity.MaterialIssueDetailEntity;
import org.egov.assets.repository.entity.MaterialIssueEntity;
import org.egov.assets.util.InventoryUtilities;
import org.egov.assets.wf.WorkflowIntegrator;
import org.egov.assets.wf.model.ProcessInstance;
import org.egov.assets.wf.model.ProcessInstanceResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class MaterialIssueService extends DomainService {

	private static final Logger LOG = LoggerFactory.getLogger(MaterialIssueService.class);

	@Autowired
	private MaterialIssueJdbcRepository materialIssueJdbcRepository;

	@Autowired
	private IndentService indentService;

	@Autowired
	private RejectedMaterialService rejectedMaterialService;

	@Autowired
	private MdmsRepository mdmsRepository;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private MaterialReceiptDetailService materialReceiptDetailService;

	@Autowired
	private StoreService storeService;

	@Autowired
	private MaterialIssueDetailJdbcRepository materialIssueDetailsJdbcRepository;

	@Autowired
	private MaterialIssuedFromReceiptJdbcRepository materialIssuedFromReceiptsJdbcRepository;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private MaterialIssueReceiptFifoLogic materialIssueReceiptFifoLogic;

	@Autowired
	private IndentDetailJdbcRepository indentDetailJdbcRepository;

	@Value("${inv.issues.save.topic}")
	private String createTopic;

	@Value("${inv.issues.save.key}")
	private String createKey;

	@Value("${inv.issues.update.topic}")
	private String updateTopic;

	@Value("${inv.issues.update.key}")
	private String updateKey;

	@Value("${inv.issues.updatestatus.topic}")
	private String updatestatusTopic;

	@Value("${inv.issues.updatestatus.key}")
	private String updatestatusKey;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	public MaterialIssueResponse create(final MaterialIssueRequest materialIssueRequest, String type) {
		try {
			fetchRelated(materialIssueRequest);
			validate(materialIssueRequest.getMaterialIssues(), Constants.ACTION_CREATE, type);
			List<String> sequenceNos = materialIssueJdbcRepository.getSequence(MaterialIssue.class.getSimpleName(),
					materialIssueRequest.getMaterialIssues().size());
			int i = 0;
			for (MaterialIssue materialIssue : materialIssueRequest.getMaterialIssues()) {
				materialIssue.setToStore(getStore(materialIssue.getToStore().getCode(), materialIssue.getTenantId()));
				String seqNo = sequenceNos.get(i);
				materialIssue.setId(seqNo);
				setMaterialIssueValues(materialIssue, seqNo, Constants.ACTION_CREATE, type);
				materialIssue.setAuditDetails(mapAuditDetails(materialIssueRequest.getRequestInfo()));
				i++;
				int j = 0;
				BigDecimal totalIssueValue = BigDecimal.ZERO;
				if (!materialIssue.getMaterialIssueDetails().isEmpty()) {

					List<String> detailSequenceNos = materialIssueDetailsJdbcRepository.getSequence(
							MaterialIssueDetail.class.getSimpleName(), materialIssue.getMaterialIssueDetails().size());

					for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
						materialIssueDetail.setId(detailSequenceNos.get(j));
						materialIssueDetail.setTenantId(materialIssue.getTenantId());
						//fetch amount from receipt table --aniket review
						BigDecimal value = getMaterialIssuedFromReceiptData(materialIssue.getFromStore(),
								materialIssueDetail.getMaterial(), materialIssue.getIssueDate(),
								materialIssue.getTenantId(), materialIssueDetail);
						totalIssueValue = totalIssueValue.add(value);
						materialIssueDetail.setValue(value);
						//update indent issue quantity in indentdetail table for that material
						backUpdateIndentAdd(materialIssueDetail, materialIssue.getTenantId());
						j++;
					}
				}
				materialIssue.setTotalIssueValue(totalIssueValue);
				BigDecimal deductionPercentage = materialIssue.getToStore().getDepartment().getDeductionPercentage();
				if (deductionPercentage != null && deductionPercentage.compareTo(BigDecimal.ZERO) > 0)
					materialIssue.setTotalDeductionValue(
							deductionPercentage.divide(new BigDecimal(100)).multiply(totalIssueValue));
				else
					materialIssue.setTotalDeductionValue(BigDecimal.ZERO);

				WorkFlowDetails workFlowDetails = materialIssueRequest.getWorkFlowDetails();
				workFlowDetails.setBusinessId(materialIssue.getIssueNumber());
				workflowIntegrator.callWorkFlow(materialIssueRequest.getRequestInfo(), workFlowDetails,
						materialIssue.getTenantId());

			}
			kafkaTemplate.send(createTopic, createKey, materialIssueRequest);
			MaterialIssueResponse response = new MaterialIssueResponse();
			response.setMaterialIssues(materialIssueRequest.getMaterialIssues());
			response.setResponseInfo(getResponseInfo(materialIssueRequest.getRequestInfo()));
			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}

	private void backUpdateIndentAdd(MaterialIssueDetail materialIssueDetail, String tenantId) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("indentissuedquantity",
				"indentissuedquantity  + " + materialIssueDetail.getQuantityIssued().toString());
		materialIssueDetail.getIndentDetail().setTenantId(tenantId);
		materialIssueJdbcRepository.updateColumn(
				new IndentDetailEntity().toEntity(materialIssueDetail.getIndentDetail()), "indentdetail", hashMap,
				null);
	}

	private BigDecimal getMaterialIssuedFromReceiptData(Store store, Material material, Long issueDate, String tenantId,
			MaterialIssueDetail materialIssueDetail) {
		List<MaterialIssuedFromReceipt> materialIssuedFromReceipts = new ArrayList<>();
		List<FifoEntity> listOfFifoEntity = materialIssueReceiptFifoLogic.implementFifoLogicBalanceRate(store, material,
				issueDate, tenantId, materialIssueDetail.getMrnNumber());
		BigDecimal value = BigDecimal.ZERO;
		BigDecimal quantityIssued = materialIssueDetail.getQuantityIssued();
		for (FifoEntity fifoEntity : listOfFifoEntity) {
			MaterialIssuedFromReceipt materialIssuedFromReceipt = new MaterialIssuedFromReceipt();
			MaterialReceiptDetail materialReceiptDetail = new MaterialReceiptDetail();
			materialReceiptDetail.setId(fifoEntity.getReceiptDetailId());
			materialIssuedFromReceipt.setMaterialReceiptDetail(materialReceiptDetail);
			materialIssuedFromReceipt.setMaterialReceiptDetailAddnlinfoId(fifoEntity.getReceiptDetailAddnInfoId());
			materialIssuedFromReceipt.setId(materialIssuedFromReceiptsJdbcRepository
					.getSequence(MaterialIssuedFromReceipt.class.getSimpleName(), 1).get(0));
			materialIssuedFromReceipt.setTenantId(materialIssueDetail.getTenantId());
			materialIssuedFromReceipt.setStatus(true);
			materialIssuedFromReceipt.setMaterialReceiptId(fifoEntity.getReceiptId());
			if (quantityIssued.compareTo(BigDecimal.valueOf(fifoEntity.getBalance())) >= 0) {
				materialIssuedFromReceipt.setQuantity(BigDecimal.valueOf(fifoEntity.getBalance()));
				value = BigDecimal.valueOf(fifoEntity.getUnitRate())
						.multiply(BigDecimal.valueOf(fifoEntity.getBalance())).add(value);
				quantityIssued = quantityIssued.subtract(BigDecimal.valueOf(fifoEntity.getBalance()));
			} else {
				materialIssuedFromReceipt.setQuantity(quantityIssued);
				value = quantityIssued.multiply(BigDecimal.valueOf(fifoEntity.getUnitRate())).add(value);
				quantityIssued = BigDecimal.ZERO;
			}
			materialIssuedFromReceipts.add(materialIssuedFromReceipt);
			if (quantityIssued.compareTo(BigDecimal.ZERO) == 0)
				break;
		}
		materialIssueDetail.setMaterialIssuedFromReceipts(materialIssuedFromReceipts);
		return value;
	}

	private void fetchRelated(MaterialIssueRequest materialIssueRequest) {
		for (MaterialIssue materialIssue : materialIssueRequest.getMaterialIssues()) {
			validateAndBuildMaterialIssueHeader(materialIssue);
			validateAndBuildMaterialIssueDetails(materialIssue);
		}
	}

	private void validateAndBuildMaterialIssueHeader(MaterialIssue materialIssue) {
		if (materialIssue.getFromStore() != null && StringUtils.isNotBlank(materialIssue.getFromStore().getCode())) {
			List<Store> stores = searchStoreByParameters(materialIssue.getFromStore().getCode(),
					materialIssue.getTenantId());
			if (stores.isEmpty())
				throw new CustomException("invalid.ref.value",
						"the field issuestore should have a valid value which exists in the system.");
			else
				materialIssue.setFromStore(stores.get(0));

		}
		if (materialIssue.getToStore() != null && StringUtils.isNotBlank(materialIssue.getToStore().getCode())) {
			List<Store> stores = searchStoreByParameters(materialIssue.getToStore().getCode(),
					materialIssue.getTenantId());
			if (stores.isEmpty())
				throw new CustomException("invalid.ref.value",
						"the field indentstore should have a valid value which exists in the system.");
			else
				materialIssue.setToStore(stores.get(0));
		}
	}

	private void validateAndBuildMaterialIssueDetails(MaterialIssue materialIssue) {
		if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Material> materialMap = getMaterials(materialIssue.getTenantId(), mapper, new RequestInfo());
			Map<String, Uom> uomMap = getUoms(materialIssue.getTenantId(), mapper, new RequestInfo());

			for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
				if (materialIssueDetail.getMaterial() != null
						&& StringUtils.isNotBlank(materialIssueDetail.getMaterial().getCode())) {
					if (materialMap.get(materialIssueDetail.getMaterial().getCode()) == null)
						throw new CustomException("invalid.ref.value",
								"the field material should have a valid value which exists in the system.");

					else
						materialIssueDetail.setMaterial(materialMap.get(materialIssueDetail.getMaterial().getCode()));
				}
				if (materialIssueDetail.getUom() != null
						&& StringUtils.isNotBlank(materialIssueDetail.getUom().getCode())) {
					if (uomMap.get(materialIssueDetail.getUom().getCode()) == null)
						throw new CustomException("invalid.ref.value",
								"the field uom should have a valid value which exists in the system.");

					else
						materialIssueDetail.setUom(uomMap.get(materialIssueDetail.getUom().getCode()));
				}
				if (materialIssueDetail.getIndentDetail() != null
						&& materialIssueDetail.getIndentDetail().getId() != null) {
					IndentDetailEntity entity = new IndentDetailEntity();
					entity.setId(materialIssueDetail.getIndentDetail().getId());
					entity.setTenantId(materialIssue.getTenantId());
					materialIssueDetail.setIndentDetail(indentDetailJdbcRepository.findById(entity) != null
							? indentDetailJdbcRepository.findById(entity).toDomain()
							: null);
				}
			}
		}
	}

	private List<Store> searchStoreByParameters(String storeCode, String tenantId) {
		StoreGetRequest storeGetRequest = new StoreGetRequest();
		storeGetRequest.setCode(Arrays.asList(storeCode));
		storeGetRequest.setTenantId(tenantId);
		return storeService.search(storeGetRequest).getStores();
	}

	private void setMaterialIssueValues(MaterialIssue materialIssue, String seqNo, String action, String type) {
		if (type.equals(IssueTypeEnum.INDENTISSUE.toString()))
			materialIssue.setIssueType(IssueTypeEnum.INDENTISSUE);
		else
			materialIssue.setIssueType(IssueTypeEnum.MATERIALOUTWARD);
/*if (StringUtils.isNotBlank(materialIssue.getIndent().getIndentCreatedBy()))
			materialIssue.setIssuedToEmployee(materialIssue.getIndent().getIndentCreatedBy());
		if (StringUtils.isNotBlank(materialIssue.getIndent().getDesignation()))
			materialIssue.setIssuedToDesignation(materialIssue.getIndent().getDesignation());
*/
		if (action.equals(Constants.ACTION_CREATE)) {
			int year = Calendar.getInstance().get(Calendar.YEAR);

			materialIssue.setMaterialIssueStatus(MaterialIssueStatusEnum.CREATED);

			if (type.equals(IssueTypeEnum.INDENTISSUE.toString())) {
				materialIssue.setIssueNumber("MRIN-" + String.valueOf(year) + "-" + seqNo);
			} else {
				materialIssue.setIssueNumber("MROW-" + String.valueOf(year) + "-" + seqNo);
			}
		}
	}

	private void validate(List<MaterialIssue> materialIssues, String method, String type) {
		InvalidDataException errors = new InvalidDataException();
		try {
			Long currentDate = currentEpochWithoutTime();
			currentDate = currentDate + (24 * 60 * 60 * 1000) - 1;
			LOG.info("CurrentDate is " + toDateStr(currentDate));
			switch (method) {
			case "create":
				if (materialIssues == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "materialIssues", "null");
				}
				for (MaterialIssue materialIssue : materialIssues) {
					if (materialIssue.getIssueDate().compareTo(currentDate) > 0) {
						errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "issueDate",
								convertEpochtoDate(materialIssue.getIssueDate()));
					}
					if (materialIssue.getIndent() != null
							&& StringUtils.isNotBlank(materialIssue.getIndent().getIndentNumber())) {
						BigDecimal totalIndentQuantity = BigDecimal.ZERO;

						IndentEntity indentEntity = new IndentEntity();
						indentEntity.setIndentNumber(materialIssue.getIndent().getIndentNumber());
						indentEntity.setTenantId(materialIssue.getTenantId());
						Object indenttEntity = materialIssueJdbcRepository.findById(indentEntity, "IndentEntity");

						if (indenttEntity != null) {
							IndentEntity indentEntityfromDb = (IndentEntity) indenttEntity;
							if (indentEntityfromDb != null) {
								if (!indentEntityfromDb.getIndentStatus().equals(IndentStatusEnum.APPROVED.toString()))
									errors.addDataError(ErrorCode.INDENT_NOT_APPROVED.getCode(),
											materialIssue.getIndent().getIndentNumber());
							}
							List<IndentDetailEntity> indentDetailEntity = indentDetailJdbcRepository.find(
									Arrays.asList(materialIssue.getIndent().getIndentNumber()),
									materialIssue.getTenantId(), null);
							if (!indentDetailEntity.isEmpty()) {
								for (IndentDetailEntity indentDetailsEntity : indentDetailEntity) {
									BigDecimal quantity = BigDecimal.ZERO;
									IndentDetail indentDetail = indentDetailsEntity.toDomain();
									if (indentDetail.getIndentQuantity() != null
											&& indentDetail.getIndentIssuedQuantity() != null)
										quantity = indentDetail.getIndentQuantity()
												.subtract(indentDetail.getIndentIssuedQuantity());
									totalIndentQuantity = totalIndentQuantity.add(quantity);
								}
							}
						}
						// todo: if all items already issued?
						if (totalIndentQuantity.compareTo(BigDecimal.ZERO) == 0)
							errors.addDataError(ErrorCode.NO_ITEMS_TO_ISSUE.getCode());
					}
					if (type.equals(IssueTypeEnum.MATERIALOUTWARD.toString())) {
						if (materialIssue.getToStore() == null)
							errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "toStore",
									"issueType - Transfer Outward", "");
						else {
							if (StringUtils.isBlank(materialIssue.getToStore().getCode()))
								errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "toStore",
										"issueType - Transfer Outward", "");
						}
						if (materialIssue.getFromStore() == null)
							errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "fromStore",
									"issueType - Transfer Outward", "");
						else {
							if (StringUtils.isBlank(materialIssue.getFromStore().getCode()))
								errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "fromStore",
										"issueType - Transfer Outward", "");
						}
						if (materialIssue.getFromStore() != null && materialIssue.getFromStore().getActive() != null) {
							if (!materialIssue.getFromStore().getActive())
								errors.addDataError(ErrorCode.ACTIVE_STORES_ALLOWED.getCode(), "fromStore");
						}
						if (materialIssue.getToStore() != null && materialIssue.getToStore().getActive() != null) {
							if (!materialIssue.getToStore().getActive())
								errors.addDataError(ErrorCode.ACTIVE_STORES_ALLOWED.getCode(), "toStore");
						}
					}

					if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
						int i = 1;
						for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {

							// user entered value copied into quantity issued
							// column. Use the same for validation, create and
							// update.

							materialIssueDetail.setQuantityIssued(
									InventoryUtilities.getQuantityInBaseUom(materialIssueDetail.getUserQuantityIssued(),
											materialIssueDetail.getUom().getConversionFactor()));
							if (materialIssueDetail.getQuantityIssued().compareTo(BigDecimal.ZERO) <= 0)
								errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "quantityIssued",
										String.valueOf(i), materialIssueDetail.getQuantityIssued().toString());
							if (materialIssueDetail.getMaterial() != null
									&& materialIssueDetail.getMaterial().getScrapable() != null)
								if (materialIssueDetail.getMaterial().getScrapable())
									errors.addDataError(ErrorCode.DONT_ALLOW_SCRAP_MATERIALS.getCode(),
											String.valueOf(i));

							BigDecimal balanceQuantity;
							LOG.info("calculating balance quantity");
							balanceQuantity = getBalanceQuantityByStoreByMaterialAndIssueDate(
									materialIssue.getFromStore(), materialIssueDetail.getMaterial(),
									materialIssue.getIssueDate(), materialIssue.getTenantId(),
									materialIssueDetail.getMrnNumber());

							if (StringUtils.isNotBlank(balanceQuantity.toString())) {
								if (balanceQuantity.compareTo(BigDecimal.ZERO) <= 0)
									errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "balanceQuantity",
											String.valueOf(i), balanceQuantity.toString());
								if (materialIssueDetail.getQuantityIssued().compareTo(balanceQuantity) > 0) {
									errors.addDataError(ErrorCode.QUANTITY1_LTE_QUANTITY2.getCode(), "quantityIssued",
											"balanceQuantity",
											InventoryUtilities
													.getQuantityInSelectedUom(materialIssueDetail.getQuantityIssued(),
															materialIssueDetail.getUom().getConversionFactor())
													.toString(),
											InventoryUtilities
													.getQuantityInSelectedUom(balanceQuantity,
															materialIssueDetail.getUom().getConversionFactor())
													.toString(),
											String.valueOf(i));
								}
							}
							if (materialIssueDetail.getIndentDetail() != null) {
								if (materialIssueDetail.getIndentDetail().getIndentQuantity() != null
										&& materialIssueDetail.getIndentDetail().getIndentIssuedQuantity() != null)

									materialIssueDetail.setPendingIndentQuantity(
											materialIssueDetail.getIndentDetail().getIndentQuantity().subtract(
													materialIssueDetail.getIndentDetail().getIndentIssuedQuantity()));
							}
							if (materialIssueDetail.getQuantityIssued()
									.compareTo(materialIssueDetail.getPendingIndentQuantity()) > 0)
								errors.addDataError(ErrorCode.QUANTITY1_LTE_QUANTITY2.getCode(), "quantityIssued",
										"indentQuantity",
										InventoryUtilities
												.getQuantityInSelectedUom(materialIssueDetail.getQuantityIssued(),
														materialIssueDetail.getUom().getConversionFactor())
												.toString(),
										InventoryUtilities.getQuantityInSelectedUom(
												materialIssueDetail.getPendingIndentQuantity(),
												materialIssueDetail.getUom().getConversionFactor()).toString(),
										String.valueOf(i));
							if (materialIssueDetail.getPendingIndentQuantity().compareTo(BigDecimal.ZERO) <= 0)
								errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "indentQuantity",
										String.valueOf(i),
										InventoryUtilities.getQuantityInSelectedUom(
												materialIssueDetail.getPendingIndentQuantity(),
												materialIssueDetail.getUom().getConversionFactor()).toString());
							i++;
						}

					}
					validateDuplicateMaterials(materialIssue.getMaterialIssueDetails(), materialIssue.getTenantId(),
							errors);
				}
				break;
			case "update":
				for (MaterialIssue materialIssue : materialIssues) {
					if (StringUtils.isEmpty(materialIssue.getIssueNumber()))
						errors.addDataError(ErrorCode.NOT_NULL.getCode(), "issueNumber", "null");
					if (materialIssue.getIssueDate().compareTo(currentDate) > 0) {
						String date = convertEpochtoDate(materialIssue.getIssueDate());
						errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "issueDate", date);
					}
					if (type.equals(IssueTypeEnum.MATERIALOUTWARD.toString())) {
						if (materialIssue.getToStore() == null)
							errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "toStore",
									"issueType - Transfer Outward", "");
						else {
							if (StringUtils.isBlank(materialIssue.getToStore().getCode()))
								errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "toStore",
										"issueType - Transfer Outward", "");
						}
						if (materialIssue.getFromStore() == null)
							errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "fromStore",
									"issueType - Transfer Outward", "");
						else {
							if (StringUtils.isBlank(materialIssue.getFromStore().getCode()))
								errors.addDataError(ErrorCode.MANDATORY_BASED_ON.getCode(), "fromStore",
										"issueType - Transfer Outward", "");
						}
						if (materialIssue.getFromStore() != null && materialIssue.getFromStore().getActive() != null) {
							if (!materialIssue.getFromStore().getActive())
								errors.addDataError(ErrorCode.ACTIVE_STORES_ALLOWED.getCode(), "fromStore");
						}
						if (materialIssue.getToStore() != null && materialIssue.getToStore().getActive() != null) {
							if (!materialIssue.getToStore().getActive())
								errors.addDataError(ErrorCode.ACTIVE_STORES_ALLOWED.getCode(), "toStore");
						}
					}
					if (materialIssue.getIssueNumber() != null) {
						MaterialIssueEntity materialIssueEntity = new MaterialIssueEntity();
						materialIssueEntity.setIssueNumber(materialIssue.getIssueNumber());
						materialIssueEntity.setTenantId(materialIssue.getTenantId());
						Object issueEntity = materialIssueJdbcRepository.findById(materialIssueEntity,
								"MaterialIssueEntity");
						MaterialIssueEntity issueEntityfromDb = (MaterialIssueEntity) issueEntity;
						if (issueEntityfromDb != null) {
							if (materialIssue.getIssueType() != null) {
								if (!issueEntityfromDb.getIssueType().equals(materialIssue.getIssueType().name())) {
									System.out.println("DBValue" + issueEntityfromDb.getIssueType());
									System.out.println("UIValue" + materialIssue.getIssueType());
									errors.addDataError(ErrorCode.NOT_ALLOWED_TO_UPDATE.getCode(), "issueType",
											"MaterialIssue");
								}
							}
							if (materialIssue.getSupplier() != null && materialIssue.getSupplier().getCode() != null) {
								if (!issueEntityfromDb.getSupplier().equals(materialIssue.getSupplier().getCode()))
									errors.addDataError(ErrorCode.NOT_ALLOWED_TO_UPDATE.getCode(), "supplier",
											"MaterialIssue");
							}
						}
						if (materialIssue.getIssueDate() != null) {
							if (!issueEntityfromDb.getIssueDate().equals(materialIssue.getIssueDate()))
								errors.addDataError(ErrorCode.NOT_ALLOWED_TO_UPDATE.getCode(), "issueDate",
										"MaterialIssue");
						}
					}
					if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
						int i = 1;
						for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {

							// user entered value copied into quantity issued
							// column. Use the same for validation, create and
							// update.

							materialIssueDetail.setQuantityIssued(
									InventoryUtilities.getQuantityInBaseUom(materialIssueDetail.getUserQuantityIssued(),
											materialIssueDetail.getUom().getConversionFactor()));

							if (materialIssueDetail.getQuantityIssued().compareTo(BigDecimal.ZERO) <= 0)
								errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "quantityIssued",
										String.valueOf(i), materialIssueDetail.getQuantityIssued().toString());
							if (materialIssueDetail.getMaterial() != null
									&& materialIssueDetail.getMaterial().getScrapable() != null)
								if (materialIssueDetail.getMaterial().getScrapable())
									errors.addDataError(ErrorCode.DONT_ALLOW_SCRAP_MATERIALS.getCode(),
											String.valueOf(i));
							FifoRequest fifoRequest = new FifoRequest();
							Fifo fifo = new Fifo();
							fifo.setStore(materialIssue.getFromStore());
							fifo.setMaterial(materialIssueDetail.getMaterial());
							fifo.setIssueDate(materialIssue.getIssueDate());
							fifo.setTenantId(materialIssue.getTenantId());
							fifo.setMrnNumber(materialIssueDetail.getMrnNumber());
							fifoRequest.setFifo(fifo);

							MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract();
							searchContract.setIssueNoteNumber(materialIssue.getIssueNumber());
							searchContract.setTenantId(materialIssue.getTenantId());
							Pagination<MaterialIssueDetail> listOfPagedMaterialIssueDetails = materialIssueDetailsJdbcRepository
									.search(materialIssue.getIssueNumber(), materialIssue.getTenantId(),
											materialIssue.getIssueType().toString());
							List<MaterialIssueDetail> listOfMaterialIssueDetails = new ArrayList<>();
							BigDecimal quantityIssued = BigDecimal.ZERO;
							if (listOfPagedMaterialIssueDetails != null)
								listOfMaterialIssueDetails = listOfPagedMaterialIssueDetails.getPagedData();
							for (MaterialIssueDetail materialIssDetail : listOfMaterialIssueDetails) {
								if (materialIssDetail.getId().equals(materialIssueDetail.getId())) {
									quantityIssued = materialIssDetail.getQuantityIssued();
									break;
								}

							}
							BigDecimal balQuantity = BigDecimal.ZERO;
							FifoResponse fifoResponse = materialIssueReceiptFifoLogic
									.getTotalStockAsPerMaterial(fifoRequest);
							if (fifoResponse != null)
								balQuantity = fifoResponse.getStock();

							BigDecimal balanceQuantity = balQuantity.add(quantityIssued);
							if (StringUtils.isNotBlank(balanceQuantity.toString())) {
								if (balanceQuantity.compareTo(BigDecimal.ZERO) <= 0)
									errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "balanceQuantity",
											String.valueOf(i),
											InventoryUtilities
													.getQuantityInSelectedUom(balanceQuantity,
															materialIssueDetail.getUom().getConversionFactor())
													.toString());
								if (materialIssueDetail.getQuantityIssued().compareTo(balanceQuantity) > 0) {
									errors.addDataError(ErrorCode.QUANTITY1_LTE_QUANTITY2.getCode(), "quantityIssued",
											"balanceQuantity",
											InventoryUtilities
													.getQuantityInSelectedUom(materialIssueDetail.getQuantityIssued(),
															materialIssueDetail.getUom().getConversionFactor())
													.toString(),
											InventoryUtilities
													.getQuantityInSelectedUom(balanceQuantity,
															materialIssueDetail.getUom().getConversionFactor())
													.toString(),
											String.valueOf(i));
								}
							}
							if (materialIssueDetail.getIndentDetail() != null) {
								if (materialIssueDetail.getIndentDetail().getIndentQuantity() != null
										&& materialIssueDetail.getIndentDetail().getIndentIssuedQuantity() != null)
									materialIssueDetail.setPendingIndentQuantity(
											materialIssueDetail.getIndentDetail().getIndentQuantity().subtract(
													materialIssueDetail.getIndentDetail().getIndentIssuedQuantity()));
							}
							BigDecimal actualIndentQuantity = quantityIssued
									.add(materialIssueDetail.getPendingIndentQuantity());
							if (materialIssueDetail.getQuantityIssued().compareTo(actualIndentQuantity) > 0)
								errors.addDataError(ErrorCode.QUANTITY1_LTE_QUANTITY2.getCode(), "quantityIssued",
										"indentQuantity",
										InventoryUtilities
												.getQuantityInSelectedUom(materialIssueDetail.getQuantityIssued(),
														materialIssueDetail.getUom().getConversionFactor())
												.toString(),
										InventoryUtilities.getQuantityInSelectedUom(actualIndentQuantity,
												materialIssueDetail.getUom().getConversionFactor()).toString(),
										String.valueOf(i));
							if (actualIndentQuantity.compareTo(BigDecimal.ZERO) <= 0)
								errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "indentQuantity",
										String.valueOf(i),
										InventoryUtilities.getQuantityInSelectedUom(actualIndentQuantity,
												materialIssueDetail.getUom().getConversionFactor()).toString());
							i++;
						}
						validateDuplicateMaterials(materialIssue.getMaterialIssueDetails(), materialIssue.getTenantId(),
								errors);
					}
					break;
				}
			}
		} catch (

		IllegalArgumentException e) {
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;

	}

	private BigDecimal getBalanceQuantityByStoreByMaterialAndIssueDate(Store store, Material material, Long issueDate,
			String tenantId, String mrnNumber) {
		BigDecimal balanceQuantity = BigDecimal.ZERO;
		LOG.info("store :" + store + "material :" + material + "issueDate :" + issueDate + "tenantId :" + tenantId);
		FifoRequest fifoRequest = new FifoRequest();
		Fifo fifo = new Fifo();
		fifo.setStore(store);
		fifo.setMaterial(material);
		fifo.setIssueDate(issueDate);
		fifo.setTenantId(tenantId);
		fifo.setMrnNumber(mrnNumber);
		fifoRequest.setFifo(fifo);
		FifoResponse fifoResponse = materialIssueReceiptFifoLogic.getTotalStockAsPerMaterial(fifoRequest);
		if (fifoResponse != null)
			balanceQuantity = fifoResponse.getStock();
		return balanceQuantity;
	}

	private void validateDuplicateMaterials(List<MaterialIssueDetail> materialIssueDetails, String tenantId,
			InvalidDataException errors) {
		HashSet<Material> setMaterial = new HashSet<Material>();

		for (MaterialIssueDetail issueDetail : materialIssueDetails) {
			Material material = new Material();
			material.setCode(issueDetail.getMaterial().getCode());
			material.setTenantId(tenantId);
			if (!setMaterial.add(material)) {
				errors.addDataError(ErrorCode.REPEATED_VALUE.getCode(), "material", material.getCode(), "");
			}
		}
	}

	public MaterialIssueResponse update(final MaterialIssueRequest materialIssueRequest, String tenantId, String type) {
		fetchRelated(materialIssueRequest);
		validate(materialIssueRequest.getMaterialIssues(), Constants.ACTION_UPDATE, type);
		List<MaterialIssue> materialIssues = materialIssueRequest.getMaterialIssues();
		int i = 0;
		for (MaterialIssue materialIssue : materialIssues) {
			materialIssue.setToStore(getStore(materialIssue.getToStore().getCode(), materialIssue.getTenantId()));

			if (StringUtils.isEmpty(materialIssue.getTenantId()))
				materialIssue.setTenantId(tenantId);
			// Search old issue object.
			MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract();
			searchContract.setIssueNoteNumber(materialIssue.getIssueNumber());
			searchContract.setTenantId(materialIssue.getTenantId());
			searchContract.setSearchPurpose("update");
			MaterialIssueResponse issueResponse = search(searchContract, type);
			// legacy mifr updation
			List<MaterialIssueDetail> materialIssueDetails = issueResponse.getMaterialIssues().get(0)
					.getMaterialIssueDetails();
			List<String> materialIssuedFromReceiptsIds = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Uom> uoms = getUoms(tenantId, objectMapper, new RequestInfo());
			for (MaterialIssueDetail materialIssueDetail : materialIssueDetails) {
				for (MaterialIssuedFromReceipt mifr : materialIssueDetail.getMaterialIssuedFromReceipts()) {
					BigDecimal quantity = InventoryUtilities.getQuantityInBaseUom(mifr.getQuantity(),
							uoms.get(materialIssueDetail.getUom().getCode()).getConversionFactor());
					mifr.setQuantity(quantity);
					materialIssuedFromReceiptsIds.add(mifr.getId());
					mifr.setStatus(false);
				}
			}
			// Update Material issue and receipt status as false.
			materialIssuedFromReceiptsJdbcRepository.updateStatus(materialIssuedFromReceiptsIds,
					materialIssue.getTenantId());
			// Cancel record status as cancelled
			if (materialIssue.getMaterialIssueStatus() != null) {
				if (materialIssue.getMaterialIssueStatus().toString()
						.equals(MaterialIssueStatusEnum.CANCELED.toString())) {
					issueResponse.getMaterialIssues().get(0).setMaterialIssueStatus(MaterialIssueStatusEnum.CANCELED);
					updateStatusAsCancelled(tenantId, materialIssue);
					materialIssues.set(i, issueResponse.getMaterialIssues().get(0));
					backUpdateIndentInCaseOfCancellation(materialIssueDetails, materialIssue.getTenantId());
					i++;
					continue;
				}
			}
			setMaterialIssueValues(materialIssue, null, Constants.ACTION_UPDATE, type);
			materialIssue
					.setAuditDetails(getAuditDetails(materialIssueRequest.getRequestInfo(), Constants.ACTION_UPDATE));
			BigDecimal totalIssueValue = BigDecimal.ZERO;
			List<String> materialIssueDetailsIds = new ArrayList<>();
			for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
				backUpdatingIndentForInsertionAndUpdation(materialIssueDetail, materialIssue.getTenantId(), type);
				if (StringUtils.isEmpty(materialIssueDetail.getTenantId()))
					materialIssueDetail.setTenantId(tenantId);
				if (StringUtils.isEmpty(materialIssueDetail.getId()))
					materialIssueDetail.setId(materialIssueDetailsJdbcRepository
							.getSequence(MaterialIssueDetail.class.getSimpleName(), 1).get(0));
				materialIssueDetailsIds.add(materialIssueDetail.getId());
				BigDecimal value = getMaterialIssuedFromReceiptData(materialIssue.getFromStore(),
						materialIssueDetail.getMaterial(), materialIssue.getIssueDate(), materialIssue.getTenantId(),
						materialIssueDetail);
				totalIssueValue = totalIssueValue.add(value);
				materialIssueDetail.setValue(value);
			}
			backUpdatingIndentForDeletionCase(materialIssueDetailsIds, materialIssue.getIssueNumber(),
					materialIssue.getTenantId(), type);
			materialIssueDetailsJdbcRepository.markDeleted(materialIssueDetailsIds, tenantId, "materialissuedetail",
					"materialissuenumber", materialIssue.getIssueNumber());
			materialIssue.setTotalIssueValue(totalIssueValue);

			BigDecimal deductionPercentage = materialIssue.getToStore().getDepartment().getDeductionPercentage();
			if (deductionPercentage != null && deductionPercentage.compareTo(BigDecimal.ZERO) > 0)
				materialIssue.setTotalDeductionValue(
						deductionPercentage.divide(new BigDecimal(100)).multiply(totalIssueValue));
			else
				materialIssue.setTotalDeductionValue(BigDecimal.ZERO);

			i++;
		}
		kafkaTemplate.send(updateTopic, updateKey, materialIssueRequest);
		MaterialIssueResponse response = new MaterialIssueResponse();
		response.setMaterialIssues(materialIssueRequest.getMaterialIssues());
		response.setResponseInfo(getResponseInfo(materialIssueRequest.getRequestInfo()));
		return response;
	}

	private void backUpdatingIndentForDeletionCase(List<String> materialIssueDetailsIds, String issueNumber,
			String tenantId, String type) {
		Pagination<MaterialIssueDetail> materialIssueDetails = materialIssueDetailsJdbcRepository.search(issueNumber,
				tenantId, type);
		List<MaterialIssueDetail> materialIssueDls = new ArrayList<>();
		if (materialIssueDetails != null)
			materialIssueDls = materialIssueDetails.getPagedData();

		List<MaterialIssueDetail> midetails = new ArrayList<>();
		for (MaterialIssueDetail mids : materialIssueDls) {
			int flag = 0;
			for (String ids : materialIssueDetailsIds) {
				if (mids.getId().equals(ids))
					flag = 1;
				if (flag == 1)
					break;
			}
			if (flag == 0)
				midetails.add(mids);
		}
		for (MaterialIssueDetail mid : midetails) {
			backUpdateIndentMinus(mid, tenantId);
		}

	}

	private void backUpdatingIndentForInsertionAndUpdation(MaterialIssueDetail materialIssueDetail, String tenantId,
			String type) {
		if (StringUtils.isEmpty(materialIssueDetail.getId()))
			backUpdateIndentAdd(materialIssueDetail, tenantId);
		else if (StringUtils.isNotEmpty(materialIssueDetail.getId())) {
			MaterialIssueDetailEntity materialIssueDetailEntity = new MaterialIssueDetailEntity();
			MaterialIssueDetail materialIssueDet = new MaterialIssueDetail();
			materialIssueDetailEntity.setId(materialIssueDetail.getId());
			materialIssueDetailEntity.setTenantId(tenantId);
			MaterialIssueDetailEntity materialIssueDetEntity = materialIssueDetailsJdbcRepository
					.findById(materialIssueDetailEntity, "MaterialIssueDetailEntity");
			if (materialIssueDetEntity != null)
				materialIssueDet = materialIssueDetEntity.toDomain(type);
			backUpdateIndentMinus(materialIssueDet, tenantId);
			backUpdateIndentAdd(materialIssueDetail, tenantId);
		}
	}

	private void backUpdateIndentMinus(MaterialIssueDetail materialIssueDet, String tenantId) {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("indentissuedquantity",
				"indentissuedquantity  - " + materialIssueDet.getQuantityIssued().toString());
		materialIssueDet.getIndentDetail().setTenantId(tenantId);
		materialIssueJdbcRepository.updateColumn(new IndentDetailEntity().toEntity(materialIssueDet.getIndentDetail()),
				"indentdetail", hashMap, null);

	}

	private void updateStatusAsCancelled(String tenantId, MaterialIssue materialIssue) {
		materialIssueJdbcRepository.updateStatus(materialIssue.getIssueNumber(), "CANCELED",
				materialIssue.getTenantId());
	}

	private void backUpdateIndentInCaseOfCancellation(List<MaterialIssueDetail> materialIssueDetails, String tenantId) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Uom> uoms = getUoms(tenantId, mapper, new RequestInfo());
		for (MaterialIssueDetail mid : materialIssueDetails) {
			mid.setUom(uoms.get(mid.getUom().getCode()));
			HashMap<String, String> hashMap = new HashMap<>();
			hashMap.put("indentissuedquantity", "indentissuedquantity - " + mid.getQuantityIssued());
			mid.getIndentDetail().setTenantId(tenantId);
			materialIssueJdbcRepository.updateColumn(new IndentDetailEntity().toEntity(mid.getIndentDetail()),
					"indentdetail", hashMap, null);
		}
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

	public MaterialIssueResponse search(final MaterialIssueSearchContract searchContract, String type) {
		Pagination<MaterialIssue> materialIssues = null;

			materialIssues = materialIssueJdbcRepository.search(searchContract, type);

		if (materialIssues.getPagedData().size() > 0)
			for (MaterialIssue materialIssue : materialIssues.getPagedData()) {

				if (materialIssue.getFromStore() != null) {
					materialIssue.setFromStore(
							getStore(materialIssue.getFromStore().getCode(), searchContract.getTenantId()));
				}
				if (materialIssue.getToStore() != null && materialIssue.getToStore().getCode() != null) {
					materialIssue.toStore(getStore(materialIssue.getToStore().getCode(), searchContract.getTenantId()));
				}

				if (materialIssue.getIndent() != null && materialIssue.getIndent().getIndentNumber() != null) {
					IndentSearch indentSearch = new IndentSearch();
					indentSearch.setIndentNumber(materialIssue.getIndent().getIndentNumber());
					indentSearch.setTenantId(searchContract.getTenantId());
					IndentResponse indentResponse = indentService.search(indentSearch, new RequestInfo());

					if (!indentResponse.getIndents().isEmpty())
						materialIssue.setIndent(indentResponse.getIndents().get(0));

				}
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Uom> uoms = getUoms(materialIssue.getTenantId(), mapper, new RequestInfo());
				Map<String, Material> materialMap = getMaterials(materialIssue.getTenantId(), mapper,
						new RequestInfo());
				Pagination<MaterialIssueDetail> materialIssueDetails = materialIssueDetailsJdbcRepository
						.search(materialIssue.getIssueNumber(), materialIssue.getTenantId(), type);
				if (materialIssueDetails.getPagedData().size() > 0) {
					for (MaterialIssueDetail materialIssueDetail : materialIssueDetails.getPagedData()) {
						materialIssueDetail.setMaterial(materialMap.get(materialIssueDetail.getMaterial().getCode()));
						if (searchContract.getSearchPurpose() != null) {
							if (searchContract.getSearchPurpose().equals("update")) {
								materialIssueDetail.setBalanceQuantity(InventoryUtilities.getQuantityInSelectedUom(
										getBalanceQuantityByStoreByMaterialAndIssueDate(materialIssue.getFromStore(),
												materialIssueDetail.getMaterial(), materialIssue.getIssueDate(),
												materialIssue.getTenantId(), materialIssueDetail.getMrnNumber())
														.add(materialIssueDetail.getQuantityIssued()),
										uoms.get(materialIssueDetail.getUom().getCode()).getConversionFactor()));
								if (materialIssueDetail.getIndentDetail() != null
										&& materialIssueDetail.getIndentDetail().getId() != null) {
									IndentDetailEntity entity = new IndentDetailEntity();
									entity.setId(materialIssueDetail.getIndentDetail().getId());
									entity.setTenantId(materialIssue.getTenantId());
									materialIssueDetail
											.setIndentDetail(indentDetailJdbcRepository.findById(entity) != null
													? indentDetailJdbcRepository.findById(entity).toDomain()
													: null);
								}
								materialIssueDetail
										.setPendingIndentQuantity(InventoryUtilities.getQuantityInSelectedUom(
												materialIssueDetail.getIndentDetail().getIndentQuantity()
														.subtract(materialIssueDetail.getIndentDetail()
																.getIndentIssuedQuantity())
														.add(materialIssueDetail.getQuantityIssued()),
												uoms.get(materialIssueDetail.getUom().getCode())
														.getConversionFactor()));
							}
						}
						Pagination<MaterialIssuedFromReceipt> materialIssuedFromReceipts = materialIssuedFromReceiptsJdbcRepository
								.search(materialIssueDetail.getId(), materialIssueDetail.getTenantId());
						if (materialIssueDetail.getUom() != null && materialIssueDetail.getUom().getCode() != null) {
							for (MaterialIssuedFromReceipt mifr : materialIssuedFromReceipts.getPagedData()) {
								BigDecimal quantity = getSearchConvertedQuantity(mifr.getQuantity(),
										uoms.get(materialIssueDetail.getUom().getCode()).getConversionFactor());
								mifr.setQuantity(quantity);

								List<MaterialReceiptDetail> materialReceiptDetail = getMaterialReceiptDetail(
										mifr.getMaterialReceiptDetail().getId(), materialIssueDetail.getTenantId());

								mifr.setMaterialReceiptDetail(
										materialReceiptDetail.isEmpty() ? mifr.getMaterialReceiptDetail()
												: materialReceiptDetail.get(0));
							}
						}
						materialIssueDetail.setMaterialIssuedFromReceipts(materialIssuedFromReceipts.getPagedData());
					}
					materialIssue.setMaterialIssueDetails(materialIssueDetails.getPagedData());
				}
			}
		MaterialIssueResponse materialIssueResponse = new MaterialIssueResponse();
		materialIssueResponse.setMaterialIssues(materialIssues.getPagedData());
		return materialIssueResponse;
	}

	private List<MaterialReceiptDetail> getMaterialReceiptDetail(String ids, String tenantId) {
		MaterialReceiptDetailSearch materialReceiptDetailSearch = MaterialReceiptDetailSearch.builder()
				.ids(Arrays.asList(ids)).tenantId(tenantId).build();
		Pagination<MaterialReceiptDetail> materialReceiptDetails = materialReceiptDetailService
				.search(materialReceiptDetailSearch);
		return materialReceiptDetails.getPagedData().size() > 0 ? materialReceiptDetails.getPagedData()
				: Collections.EMPTY_LIST;
	}

	public MaterialIssueResponse prepareMIFromIndents(MaterialIssueRequest materialIssueRequest, String tenantId) {
		for (MaterialIssue materialIssue : materialIssueRequest.getMaterialIssues()) {
			if (materialIssue.getIndent() != null
					&& StringUtils.isNotBlank(materialIssue.getIndent().getIndentNumber())) {
				// Show error if indent not found
				IndentSearch indentSearch = new IndentSearch();
				indentSearch.setIndentNumber(materialIssue.getIndent().getIndentNumber());
				indentSearch.setTenantId(tenantId);
				IndentResponse indentResponse = indentService.search(indentSearch, new RequestInfo());
				if (indentResponse != null && indentResponse.getIndents() != null
						&& indentResponse.getIndents().isEmpty())
					throw new CustomException(ErrorCode.INVALID_INDENTNUMBER_FOR_ISSUE.getCode(),
							ErrorCode.INVALID_INDENTNUMBER_FOR_ISSUE.getMessage());

				materialIssue.setIndent(indentResponse.getIndents().get(0));
				if (materialIssue.getIndent().getIssueStore() != null
						&& StringUtils.isNotEmpty(materialIssue.getIndent().getIssueStore().getCode())) {
					List<Store> stores = searchStoreByParameters(materialIssue.getIndent().getIssueStore().getCode(),
							materialIssue.getTenantId());

					if (!stores.isEmpty()) {
						Store store = stores.get(0);
						if (stores != null && stores.get(0) != null && store.getDepartment() != null
								&& StringUtils.isNotBlank(store.getDepartment().getCode())) {
							Department department = departmentService.getDepartment(tenantId,
									store.getDepartment().getCode(), new RequestInfo());
							store.setDepartment(department);
						}
						materialIssue.getIndent().setIssueStore(store);
						materialIssue.setFromStore(store); // Adding indent issue store as material issue -> from store
															// (Store issues material)
					}
				}
				if (materialIssue.getIndent().getIndentStore() != null
						&& StringUtils.isNotBlank(materialIssue.getIndent().getIndentStore().getCode())) {
					List<Store> stores = searchStoreByParameters(materialIssue.getIndent().getIndentStore().getCode(),
							materialIssue.getTenantId());
					if (!stores.isEmpty()) {
						Store store = stores.get(0);
						if (stores != null && stores.get(0) != null && store.getDepartment() != null
								&& StringUtils.isNotBlank(store.getDepartment().getCode())) {
							Department department = departmentService.getDepartment(tenantId,
									store.getDepartment().getCode(), new RequestInfo());
							store.setDepartment(department);
						}
						materialIssue.getIndent().setIndentStore(store);
						materialIssue.setToStore(store);
					}
				}
				ObjectMapper mapper = new ObjectMapper();
				if (!materialIssue.getIndent().getIndentDetails().isEmpty()) {
					Map<String, Uom> uomMap = getUoms(tenantId, mapper, new RequestInfo());
					Map<String, Material> materialMap = getMaterials(tenantId, mapper, new RequestInfo());
					List<MaterialIssueDetail> materialIssueDetail = new ArrayList<>();

					// Fetch indent details where quantity issue is pending.
					for (IndentDetail indentDetail : materialIssue.getIndent().getIndentDetails()) {

						// Show total indent required quantity.
						BigDecimal indentBalanceQuantity = InventoryUtilities.getQuantityInSelectedUom(
								indentDetail.getIndentQuantity()
										.subtract(indentDetail.getIndentIssuedQuantity() != null
												? indentDetail.getIndentIssuedQuantity()
												: BigDecimal.ZERO),
								uomMap.get(indentDetail.getUom().getCode()).getConversionFactor());

						if (indentBalanceQuantity.compareTo(BigDecimal.ZERO) > 0) {

							MaterialIssueDetail materialIssueDet = new MaterialIssueDetail();

							materialIssueDet.setPendingIndentQuantity(InventoryUtilities.getQuantityInSelectedUom(
									indentDetail.getIndentQuantity().subtract(indentDetail.getIndentIssuedQuantity()),
									uomMap.get(indentDetail.getUom().getCode()).getConversionFactor()));

							if (indentDetail.getMaterial() != null
									&& StringUtils.isNotBlank(indentDetail.getMaterial().getCode())) {
								indentDetail.setMaterial(materialMap.get(indentDetail.getMaterial().getCode()));
								materialIssueDet.setMaterial(materialMap.get(indentDetail.getMaterial().getCode()));
							}
							if (indentDetail.getUom() != null
									&& StringUtils.isNotBlank(indentDetail.getUom().getCode())) {
								indentDetail.setUom(uomMap.get(indentDetail.getUom().getCode()));
								materialIssueDet.setUom(uomMap.get(indentDetail.getUom().getCode()));

								// Show available quantity in UI. Converted to
								// Unit of measurement selected.
								materialIssueDet.setBalanceQuantity(InventoryUtilities.getQuantityInSelectedUom(
										getBalanceQuantityByStoreByMaterialAndIssueDate(
												materialIssue.getIndent().getIssueStore(),
												materialIssueDet.getMaterial(),
												(materialIssue.getIssueDate() != null ? materialIssue.getIssueDate()
														: currentEpochWithoutTime()),
												materialIssue.getTenantId(), materialIssueDet.getMrnNumber()),
										materialIssueDet.getUom().getConversionFactor()));

							}
							materialIssueDet.setIndentDetail(indentDetail);
							materialIssueDetail.add(materialIssueDet);
						}
					}

					if (materialIssueDetail.isEmpty())
						throw new CustomException(ErrorCode.NO_ITEMS_TO_ISSUE.getCode(),
								ErrorCode.NO_ITEMS_TO_ISSUE.getMessage());

					materialIssue.setMaterialIssueDetails(materialIssueDetail);
				}
			} else
				throw new CustomException(ErrorCode.ATLEAST_ONEINDENT_REQUIRE_ISSUE.getCode(),
						ErrorCode.ATLEAST_ONEINDENT_REQUIRE_ISSUE.getMessage());
		}
		MaterialIssueResponse materialIssueResponse = new MaterialIssueResponse();
		materialIssueResponse.setMaterialIssues(materialIssueRequest.getMaterialIssues());
		return materialIssueResponse;
	}

	private Map<String, Uom> getUoms(String tenantId, final ObjectMapper mapper, RequestInfo requestInfo) {
		JSONArray responseJSONArray = mdmsRepository.getByCriteria(tenantId, "common-masters", "UOM", null, null,
				requestInfo);
		Map<String, Uom> uomMap = new HashMap<>();

		if (responseJSONArray != null && responseJSONArray.size() > 0) {
			for (int i = 0; i < responseJSONArray.size(); i++) {
				Uom uom = mapper.convertValue(responseJSONArray.get(i), Uom.class);
				uomMap.put(uom.getCode(), uom);
			}

		}
		return uomMap;
	}

	private Map<String, Material> getMaterials(String tenantId, final ObjectMapper mapper, RequestInfo requestInfo) {
		JSONArray responseJSONArray = mdmsRepository.getByCriteria(tenantId, "store-asset", "Material", null, null,
				requestInfo);
		Map<String, Material> materialMap = new HashMap<>();

		if (responseJSONArray != null && responseJSONArray.size() > 0) {
			for (int i = 0; i < responseJSONArray.size(); i++) {
				Material material = mapper.convertValue(responseJSONArray.get(i), Material.class);
				materialMap.put(material.getCode(), material);
			}

		}
		return materialMap;
	}

	private String convertEpochtoDate(Long date) {
		Date epoch = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String s2 = format.format(epoch);
		return s2;
	}

	public PDFResponse printPdf(MaterialIssueSearchContract searchContract, String type, RequestInfo requestInfo) {
		MaterialIssueResponse materialIssueResponse = search(searchContract, type);
		if (!materialIssueResponse.getMaterialIssues().isEmpty()
				&& materialIssueResponse.getMaterialIssues().size() == 1) {
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
			for (MaterialIssue in : materialIssueResponse.getMaterialIssues()) {
				JSONObject indent = new JSONObject();

				indent.put("outwardNumber", in.getIssueNumber());
				if (in.getIssueDate() != null) {
					Instant issueDate = Instant.ofEpochMilli(in.getIssueDate());
					indent.put("outwardDate", fmt.format(issueDate.atZone(ZoneId.systemDefault())));
				} else {
					indent.put("outwardDate", in.getIssueDate());
				}

				indent.put("issueNumber", in.getIssueNumber());
				if (in.getIssueDate() != null) {
					Instant issueDate = Instant.ofEpochMilli(in.getIssueDate());
					indent.put("issueDate", fmt.format(issueDate.atZone(ZoneId.systemDefault())));
				} else {
					indent.put("issueDate", in.getIssueDate());
				}
				indent.put("issuingStoreName", in.getFromStore().getName());
				indent.put("issuingStoreDept", in.getFromStore().getDepartment().getName());

				indent.put("indentNumber", in.getIndent().getIndentNumber());
				indent.put("indentingStoreName", in.getToStore().getName());
				indent.put("indentingStoreDept", in.getToStore().getDepartment().getName());
				if (in.getIndent().getIndentDate() != null) {
					Instant indentDate = Instant.ofEpochMilli(in.getIndent().getIndentDate());
					indent.put("indentDate", fmt.format(indentDate.atZone(ZoneId.systemDefault())));
				} else {
					indent.put("indentDate", in.getIndent().getIndentDate());
				}
				indent.put("issueStatus", in.getMaterialIssueStatus());
				indent.put("outwardStatus", in.getMaterialIssueStatus());
				indent.put("indentPurpose", in.getIndent().getIndentPurpose());
				indent.put("issuedToEmployee", in.getIssuedToEmployee());
				indent.put("issuedToEmployeeDesignation", in.getIssuedToDesignation());
				indent.put("remark", in.getDescription());
				indent.put("createdBy", in.getCreatedByName());
				indent.put("designation", in.getDesignation());

				List<IndentDetail> indList = in.getIndent().getIndentDetails();

				JSONArray indentDetails = new JSONArray();
				int i = 1;
				BigDecimal totalIssueAmount = BigDecimal.ZERO;
				for (MaterialIssueDetail detail : in.getMaterialIssueDetails()) {
					JSONObject indentDetail = new JSONObject();
					indentDetail.put("srNo", i++);
					indentDetail.put("materialCode", detail.getMaterial().getCode());
					indentDetail.put("materialName", detail.getMaterial().getName());
					indentDetail.put("materialDescription", detail.getMaterial().getDescription());
					indentDetail.put("uomName", detail.getUom().getCode());
					IndentDetail d = indList.stream()
							.filter(predicate -> predicate.getId().equals(detail.getIndentDetail().getId())).findFirst()
							.orElse(null);
					indentDetail.put("requiredQuantity", d.getIndentQuantity());
					indentDetail.put("quantityIssued", detail.getQuantityIssued());

					BigDecimal totalUnitRate = BigDecimal.ZERO;
					BigDecimal total = BigDecimal.ZERO;
					for (MaterialIssuedFromReceipt rec : detail.getMaterialIssuedFromReceipts()) {
						total = total.add(rec.getQuantity().multiply(rec.getMaterialReceiptDetail().getUnitRate()));
						totalUnitRate = totalUnitRate.add(rec.getMaterialReceiptDetail().getUnitRate());
					}
					indentDetail.put("totalValue", total);
					indentDetail.put("unitRate", totalUnitRate);
					indentDetail.put("remark", detail.getDescription());
					indentDetails.add(indentDetail);
					totalIssueAmount = totalIssueAmount.add(total);
				}
				indent.put("issueTotalAmount", totalIssueAmount);
				indent.put("materialDetails", indentDetails);

				// Need to integrate Workflow
				ProcessInstanceResponse workflowData = workflowIntegrator.getWorkflowDataByID(requestInfo,
						in.getIssueNumber(), in.getTenantId());
				LOG.info(workflowData.toString());
				JSONArray workflows = new JSONArray();
				for (int j = 0; j < workflowData.getProcessInstances().size(); j++) {
					ProcessInstance processData = workflowData.getProcessInstances().get(j);
					Instant wfDate = Instant.ofEpochMilli(processData.getAuditDetails().getCreatedTime());
					// Need to integrate Workflow
					JSONObject jsonWork = new JSONObject();
					jsonWork.put("srNo", j+1);
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
				}
				indent.put("workflowDetails", workflows);
				indents.add(indent);
				if (type.equals(IssueTypeEnum.MATERIALOUTWARD.toString())) {
					requestMain.put("IndentOutwardTransfer", indents);
				} else if (type.equals(IssueTypeEnum.INDENTISSUE.toString())) {
					requestMain.put("IndentIssueNote", indents);
				}
			}

			if (type.equals(IssueTypeEnum.MATERIALOUTWARD.toString())) {
				return pdfServiceReposistory.getPrint(requestMain, "store-asset-indent-outward",
						searchContract.getTenantId());
			} else if (type.equals(IssueTypeEnum.INDENTISSUE.toString())) {
				return pdfServiceReposistory.getPrint(requestMain, "store-asset-indent-issue-note",
						searchContract.getTenantId());
			}
		}
		return PDFResponse.builder()
				.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();

	}

	@Transactional
	public MaterialIssueResponse updateStatus(MaterialIssueRequest indentIssueRequest) {

		try {
			WorkFlowDetails workFlowDetails = workflowIntegrator.callWorkFlow(indentIssueRequest.getRequestInfo(),
					indentIssueRequest.getWorkFlowDetails(), indentIssueRequest.getWorkFlowDetails().getTenantId());
			indentIssueRequest.setWorkFlowDetails(workFlowDetails);
			kafkaQue.send(updatestatusTopic, updatestatusKey, indentIssueRequest);
			MaterialIssueResponse response = new MaterialIssueResponse();
			response.setMaterialIssues(indentIssueRequest.getMaterialIssues());
			response.setResponseInfo(getResponseInfo(indentIssueRequest.getRequestInfo()));

			if (indentIssueRequest.getWorkFlowDetails().getStatus()
					.equals(MaterialIssueStatusEnum.REJECTED.toString())) {
				MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(
						indentIssueRequest.getWorkFlowDetails().getTenantId(), null, null, null,
						indentIssueRequest.getWorkFlowDetails().getBusinessId(), null, null, null, null, null, null,
						null, null, null, null, null, null, null);
				MaterialIssueResponse issueResponse = search(searchContract, IssueTypeEnum.INDENTISSUE.toString());
				for (MaterialIssue materialIssues : issueResponse.getMaterialIssues()) {
					for (MaterialIssueDetail issueDetail : materialIssues.getMaterialIssueDetails()) {
						issueDetail.rejectedIssuedQuantity(issueDetail.getQuantityIssued());
						issueDetail.setQuantityIssued(BigDecimal.ZERO);
						issueDetail.setUserQuantityIssued(BigDecimal.ZERO);

						for (MaterialIssuedFromReceipt fromReceipt : issueDetail.getMaterialIssuedFromReceipts()) {
							fromReceipt.setRejectedIssuedQuantity(fromReceipt.getQuantity());
							fromReceipt.setQuantity(BigDecimal.ZERO);
						}
					}
					rejectedMaterialService.minusRejectedMaterial(materialIssues.getMaterialIssueDetails(),
							indentIssueRequest.getWorkFlowDetails().getTenantId());
				}
			}

			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}

}
