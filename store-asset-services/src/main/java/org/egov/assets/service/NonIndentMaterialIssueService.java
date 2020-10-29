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
import org.egov.assets.common.SupplierRepository;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.Fifo;
import org.egov.assets.model.FifoRequest;
import org.egov.assets.model.FifoResponse;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialIssue;
import org.egov.assets.model.MaterialIssue.IssuePurposeEnum;
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
import org.egov.assets.model.Scrap;
import org.egov.assets.model.Scrap.ScrapStatusEnum;
import org.egov.assets.model.ScrapDetail;
import org.egov.assets.model.ScrapRequest;
import org.egov.assets.model.ScrapResponse;
import org.egov.assets.model.ScrapSearch;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.Supplier;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.MaterialIssueDetailJdbcRepository;
import org.egov.assets.repository.MaterialIssueJdbcRepository;
import org.egov.assets.repository.MaterialIssuedFromReceiptJdbcRepository;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.entity.FifoEntity;
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
public class NonIndentMaterialIssueService extends DomainService {

	private static final Logger LOG = LoggerFactory.getLogger(NonIndentMaterialIssueService.class);

	@Autowired
	private MaterialIssueJdbcRepository materialIssueJdbcRepository;

	@Autowired
	private MaterialIssueDetailJdbcRepository materialIssueDetailsJdbcRepository;

	@Autowired
	private MaterialIssuedFromReceiptJdbcRepository materialIssuedFromReceiptsJdbcRepository;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private RejectedMaterialService rejectedMaterialService;

	@Autowired
	private ScrapService scrapService;

	@Autowired
	private MdmsRepository mdmsRepository;

	@Autowired
	private SupplierRepository supplierRepository;

	@Autowired
	private MaterialReceiptDetailService materialReceiptDetailService;

	@Autowired
	private MaterialIssueReceiptFifoLogic materialIssueReceiptFifoLogic;

	@Autowired
	private StoreService storeService;

	@Value("${inv.issues.save.topic}")
	private String createTopic;

	@Value("${inv.issues.save.key}")
	private String createKey;

	@Value("${inv.issues.update.topic}")
	private String updateTopic;

	@Value("${inv.issues.update.key}")
	private String updateKey;

	@Value("${inv.issues.updatestatus.topic}")
	private String updateStatusTopic;

	@Value("${inv.issues.updatestatus.key}")
	private String updateStatusKey;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	public MaterialIssueResponse create(MaterialIssueRequest nonIndentIssueRequest, String tenantId) {
		try {

			fetchRelated(nonIndentIssueRequest);
			validate(nonIndentIssueRequest.getMaterialIssues(), Constants.ACTION_CREATE);
			List<String> sequenceNos = materialIssueJdbcRepository.getSequence(MaterialIssue.class.getSimpleName(),
					nonIndentIssueRequest.getMaterialIssues().size());
			int i = 0;
			String issuePurpose = "";
			for (MaterialIssue materialIssue : nonIndentIssueRequest.getMaterialIssues()) {
				if (materialIssue.getIssuePurpose() != null)
					issuePurpose = materialIssue.getIssuePurpose().toString();

				String seqNo = sequenceNos.get(i);
				materialIssue.setId(seqNo);
				setMaterialIssueValues(materialIssue, seqNo, Constants.ACTION_CREATE);
				materialIssue.setAuditDetails(mapAuditDetails(nonIndentIssueRequest.getRequestInfo()));
				i++;
				int j = 0;
				BigDecimal totalIssueValue = BigDecimal.ZERO;
				if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
					List<String> detailSequenceNos = materialIssueDetailsJdbcRepository.getSequence(
							MaterialIssueDetail.class.getSimpleName(), materialIssue.getMaterialIssueDetails().size());
					for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
						materialIssueDetail.setId(detailSequenceNos.get(j));
						materialIssueDetail.setTenantId(materialIssue.getTenantId());
						BigDecimal value = getMaterialIssuedFromReceiptData(materialIssue.getFromStore(),
								materialIssueDetail.getMaterial(), materialIssue.getIssueDate(),
								materialIssue.getTenantId(), materialIssueDetail, materialIssue);
						totalIssueValue = totalIssueValue.add(value);
						materialIssueDetail.setValue(value);
						j++;
					}
				}
				materialIssue.setTotalIssueValue(totalIssueValue);
				WorkFlowDetails workFlowDetails = nonIndentIssueRequest.getWorkFlowDetails();
				workFlowDetails.setBusinessId(materialIssue.getIssueNumber());
				workflowIntegrator.callWorkFlow(nonIndentIssueRequest.getRequestInfo(), workFlowDetails,
						materialIssue.getTenantId());
			}

			kafkaTemplate.send(createTopic, createKey, nonIndentIssueRequest);
			MaterialIssueResponse response = new MaterialIssueResponse();
			response.setMaterialIssues(nonIndentIssueRequest.getMaterialIssues());
			response.setResponseInfo(getResponseInfo(nonIndentIssueRequest.getRequestInfo()));

			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}

	// public void addScrapDetails(MaterialIssueRequest nonIndentIssueRequest,
	// String tenantId) {
	// ScrapRequest scrapRequest = new ScrapRequest();
	// List<Scrap> scrapList = new ArrayList<>();
	// for (MaterialIssue materialIssue : nonIndentIssueRequest.getMaterialIssues())
	// {
	// Scrap scrap = new Scrap();
	// scrap.setIssueNumber(materialIssue.getIssueNumber());
	// scrap.setScrapStatus(ScrapStatusEnum.fromValue(materialIssue.getMaterialIssueStatus().toString()));
	// scrap.setStore(materialIssue.getFromStore());
	// scrap.description(materialIssue.getDescription());
	// scrap.setDesignation(materialIssue.getDesignation());
	// scrap.setScrapDate(materialIssue.getIssueDate());
	//
	// List<ScrapDetail> listScrapDetail = new ArrayList<>();
	// for (MaterialIssueDetail materialIssueDetail :
	// materialIssue.getMaterialIssueDetails()) {
	// materialIssueDetail.setScrapedQuantity(
	// materialIssueDetail.getScrapedQuantity() != null ?
	// materialIssueDetail.getScrapedQuantity()
	// : BigDecimal.ZERO);
	//
	// ScrapDetail scrapDetail = new ScrapDetail();
	// scrapDetail.setTenantId(tenantId);
	// scrapDetail.setIssueDetail(materialIssueDetail);
	// scrapDetail.setMaterial(materialIssueDetail.getMaterial());
	// scrapDetail.setUom(materialIssueDetail.getUom());
	// scrapDetail.setLotNumber("0");
	// scrapDetail.setUserQuantity(materialIssueDetail.getUserQuantityIssued());
	// scrapDetail.setScrapQuantity(materialIssueDetail.getUserQuantityIssued());
	// scrapDetail.setScrapValue(materialIssueDetail.getScrapValue());
	// scrapDetail.setExistingValue(materialIssueDetail.getValue());
	// scrapDetail.setQuantity(
	// materialIssueDetail.getQuantityIssued() != null ?
	// materialIssueDetail.getQuantityIssued()
	// : BigDecimal.ZERO);
	//
	// listScrapDetail.add(scrapDetail);
	// }
	// scrap.setScrapDetails(listScrapDetail);
	// scrapList.add(scrap);
	// }
	// scrapRequest.setRequestInfo(nonIndentIssueRequest.getRequestInfo());
	// scrapRequest.setScraps(scrapList);
	// scrapService.create(scrapRequest, tenantId);
	// }

	public void addScrapDetails(MaterialIssueRequest nonIndentIssueRequest, String tenantId) {
		ScrapRequest scrapRequest = new ScrapRequest();
		List<Scrap> scrapList = new ArrayList<>();
		boolean isUpdate = false;
		for (MaterialIssue materialIssue : nonIndentIssueRequest.getMaterialIssues()) {
			ScrapSearch scrapSearch = new ScrapSearch();
			scrapSearch.setTenantId(tenantId);
			scrapSearch.setIssueNumber(materialIssue.getIssueNumber());
			ScrapResponse scrapResponse = scrapService.search(scrapSearch);
			if (!scrapResponse.getScraps().isEmpty()) {
				isUpdate = true;
				for (Scrap d : scrapResponse.getScraps()) {
					Scrap scrap = new Scrap();
					scrap.setId(d.getId());
					scrap.setScrapNumber(d.getScrapNumber());
					scrap.setIssueNumber(materialIssue.getIssueNumber());
					scrap.setScrapStatus(ScrapStatusEnum.fromValue(materialIssue.getMaterialIssueStatus().toString()));
					scrap.setStore(materialIssue.getFromStore());
					scrap.description(materialIssue.getDescription());
					scrap.setDesignation(materialIssue.getDesignation());
					scrap.setScrapDate(materialIssue.getIssueDate());

					List<ScrapDetail> dd = d.getScrapDetails();
					List<ScrapDetail> listScrapDetail = new ArrayList<>();
					for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
						ScrapDetail scrapDetail = new ScrapDetail();

						ScrapDetail e = dd.stream().filter(predicate -> predicate.getMaterial().getCode()
								.equals(materialIssueDetail.getMaterial().getCode())).findAny().orElse(null);

						if (e != null) {
							scrapDetail.setId(e.getId());
						}

						scrapDetail.setScrapNumber(d.getScrapNumber());
						materialIssueDetail.setScrapedQuantity(materialIssueDetail.getScrapedQuantity() != null
								? materialIssueDetail.getScrapedQuantity()
								: BigDecimal.ZERO);

						scrapDetail.setTenantId(tenantId);
						scrapDetail.setIssueDetail(materialIssueDetail);
						scrapDetail.setMaterial(materialIssueDetail.getMaterial());
						scrapDetail.setUom(materialIssueDetail.getUom());
						scrapDetail.setLotNumber("0");
						scrapDetail.setUserQuantity(
								e != null ? e.getUserQuantity().add(materialIssueDetail.getUserQuantityIssued())
										: materialIssueDetail.getUserQuantityIssued());
						scrapDetail.setScrapQuantity(
								e != null ? e.getScrapQuantity().add(materialIssueDetail.getUserQuantityIssued())
										: materialIssueDetail.getUserQuantityIssued());
						scrapDetail.setScrapValue(e != null ? e.getScrapValue().add(materialIssueDetail.getScrapValue())
								: materialIssueDetail.getScrapValue());
						scrapDetail
								.setExistingValue(e != null ? e.getExistingValue().add(materialIssueDetail.getValue())
										: materialIssueDetail.getValue());
						scrapDetail.setQuantity(e != null ? e.getQuantity().add(materialIssueDetail.getQuantityIssued())
								: materialIssueDetail.getQuantityIssued());

						listScrapDetail.add(scrapDetail);
					}
					scrap.setScrapDetails(listScrapDetail);
					scrapList.add(scrap);
				}
			} else {
				Scrap scrap = new Scrap();
				scrap.setIssueNumber(materialIssue.getIssueNumber());
				scrap.setScrapStatus(ScrapStatusEnum.fromValue(materialIssue.getMaterialIssueStatus().toString()));
				scrap.setStore(materialIssue.getFromStore());
				scrap.description(materialIssue.getDescription());
				scrap.setDesignation(materialIssue.getDesignation());
				scrap.setScrapDate(materialIssue.getIssueDate());
				List<ScrapDetail> listScrapDetail = new ArrayList<>();
				for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
					ScrapDetail scrapDetail = new ScrapDetail();

					materialIssueDetail.setScrapedQuantity(
							materialIssueDetail.getScrapedQuantity() != null ? materialIssueDetail.getScrapedQuantity()
									: BigDecimal.ZERO);

					scrapDetail.setTenantId(tenantId);
					scrapDetail.setIssueDetail(materialIssueDetail);
					scrapDetail.setMaterial(materialIssueDetail.getMaterial());
					scrapDetail.setUom(materialIssueDetail.getUom());
					scrapDetail.setLotNumber("0");
					scrapDetail.setUserQuantity(materialIssueDetail.getUserQuantityIssued());
					scrapDetail.setScrapQuantity(materialIssueDetail.getUserQuantityIssued());
					// scrapDetail.setScrapValue(materialIssueDetail.getScrapValue());
					scrapDetail.setScrapValue(materialIssueDetail.getValue());
					scrapDetail.setExistingValue(materialIssueDetail.getValue());
					scrapDetail.setQuantity(
							materialIssueDetail.getQuantityIssued() != null ? materialIssueDetail.getQuantityIssued()
									: BigDecimal.ZERO);

					listScrapDetail.add(scrapDetail);
				}
				scrap.setScrapDetails(listScrapDetail);
				scrapList.add(scrap);
			}
		}
		scrapRequest.setRequestInfo(nonIndentIssueRequest.getRequestInfo());
		scrapRequest.setScraps(scrapList);
		if (isUpdate)
			scrapService.update(scrapRequest, tenantId);
		else
			scrapService.create(scrapRequest, tenantId);
	}

	public void updateScrapDetails(MaterialIssueRequest nonIndentIssueRequest, String tenantId,
			List<MaterialIssue> existingMaterialIssue) {
		ScrapRequest scrapRequest = new ScrapRequest();
		List<Scrap> scrapList = new ArrayList<>();
		for (MaterialIssue materialIssue : nonIndentIssueRequest.getMaterialIssues()) {
			MaterialIssue materialIssueExisting = existingMaterialIssue.stream()
					.filter(predicate -> predicate.getIssueNumber().equals(materialIssue.getIssueNumber())).findAny()
					.orElse(null);

			ScrapSearch scrapSearch = new ScrapSearch();
			scrapSearch.setTenantId(tenantId);
			scrapSearch.setIssueNumber(materialIssue.getIssueNumber());
			ScrapResponse scrapResponse = scrapService.search(scrapSearch);
			if (!scrapResponse.getScraps().isEmpty()) {
				for (Scrap d : scrapResponse.getScraps()) {
					Scrap scrap = new Scrap();
					scrap.setId(d.getId());
					scrap.setScrapNumber(d.getScrapNumber());
					scrap.setIssueNumber(materialIssue.getIssueNumber());
					scrap.setScrapStatus(ScrapStatusEnum.fromValue(materialIssue.getMaterialIssueStatus().toString()));
					scrap.setStore(materialIssue.getFromStore());
					scrap.description(materialIssue.getDescription());
					scrap.setDesignation(materialIssue.getDesignation());
					scrap.setScrapDate(materialIssue.getIssueDate());

					List<ScrapDetail> dd = d.getScrapDetails();
					List<ScrapDetail> listScrapDetail = new ArrayList<>();

					List<MaterialIssueDetail> listOfExistingIssue = materialIssueExisting != null
							? materialIssueExisting.getMaterialIssueDetails()
							: new ArrayList();

					for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
						ScrapDetail scrapDetail = new ScrapDetail();

						MaterialIssueDetail materialIssueDetailExisting = listOfExistingIssue.stream()
								.filter(predicate -> predicate.getId().equalsIgnoreCase(materialIssueDetail.getId())
										&& predicate.getMaterial().getCode()
												.equals(materialIssueDetail.getMaterial().getCode()))
								.findAny().orElse(null);

						ScrapDetail e = dd.stream().filter(predicate -> predicate.getMaterial().getCode()
								.equals(materialIssueDetail.getMaterial().getCode())).findAny().orElse(null);

						if (e != null) {
							scrapDetail.setId(e.getId());
						}

						scrapDetail.setScrapNumber(d.getScrapNumber());
						materialIssueDetail.setScrapedQuantity(materialIssueDetail.getScrapedQuantity() != null
								? materialIssueDetail.getScrapedQuantity()
								: BigDecimal.ZERO);

						scrapDetail.setTenantId(tenantId);
						scrapDetail.setIssueDetail(materialIssueDetail);
						scrapDetail.setMaterial(materialIssueDetail.getMaterial());
						scrapDetail.setUom(materialIssueDetail.getUom());
						scrapDetail.setLotNumber("0");

						BigDecimal userQty = (materialIssueDetailExisting != null && e != null
								? e.getScrapQuantity().subtract(materialIssueDetailExisting.getUserQuantityIssued())
										.add(materialIssueDetail.getUserQuantityIssued())
								: materialIssueDetail.getUserQuantityIssued());

						scrapDetail.setUserQuantity(userQty);
						scrapDetail.setScrapQuantity(userQty);

						BigDecimal scrapValue = (materialIssueDetailExisting != null && e != null
								? e.getScrapValue().subtract(materialIssueDetailExisting.getScrapValue()).add(
										materialIssueDetail.getScrapValue())
								: materialIssueDetail.getScrapValue());

						scrapDetail.setScrapValue(scrapValue);

						BigDecimal scrapValueExisting = (materialIssueDetailExisting != null && e != null
								? e.getExistingValue().subtract(materialIssueDetailExisting.getValue()).add(
										materialIssueDetail.getValue())
								: materialIssueDetail.getValue());

						scrapDetail.setExistingValue(scrapValueExisting);

						BigDecimal quanity = (materialIssueDetailExisting != null && e != null
								? e.getQuantity().subtract(materialIssueDetailExisting.getQuantityIssued())
										.add(materialIssueDetail.getQuantityIssued())
								: materialIssueDetail.getQuantityIssued());

						scrapDetail.setQuantity(quanity);
						listScrapDetail.add(scrapDetail);
					}
					scrap.setScrapDetails(listScrapDetail);
					scrapList.add(scrap);
				}
			}
		}
		scrapRequest.setRequestInfo(nonIndentIssueRequest.getRequestInfo());
		scrapRequest.setScraps(scrapList);
		scrapService.update(scrapRequest, tenantId);
	}

	private BigDecimal getMaterialIssuedFromReceiptData(Store store, Material material, Long issueDate, String tenantId,
			MaterialIssueDetail materialIssueDetail, MaterialIssue materialIssue) {
		List<MaterialIssuedFromReceipt> materialIssuedFromReceipts = new ArrayList<>();
		List<FifoEntity> listOfFifoEntity = new ArrayList<>();
		if (materialIssue.getIssuePurpose().equals(IssuePurposeEnum.WRITEOFFORSCRAP)) {
			listOfFifoEntity = materialIssueReceiptFifoLogic.implementFifoLogic(store, material, issueDate, tenantId);
		} else {
			listOfFifoEntity = materialIssueReceiptFifoLogic.implementFifoLogicForReturnMaterial(store, material,
					issueDate, tenantId, materialIssueDetail.getMrnNumber());
		}
		BigDecimal unitRate = BigDecimal.ZERO;
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
				unitRate = BigDecimal.valueOf(fifoEntity.getUnitRate())
						.multiply(BigDecimal.valueOf(fifoEntity.getBalance())).add(unitRate);
				quantityIssued = quantityIssued.subtract(BigDecimal.valueOf(fifoEntity.getBalance()));
			} else {
				materialIssuedFromReceipt.setQuantity(quantityIssued);
				unitRate = quantityIssued.multiply(BigDecimal.valueOf(fifoEntity.getUnitRate())).add(unitRate);
				quantityIssued = BigDecimal.ZERO;
			}

			materialIssuedFromReceipts.add(materialIssuedFromReceipt);
			if (quantityIssued.compareTo(BigDecimal.ZERO) == 0)
				break;
		}
		materialIssueDetail.setMaterialIssuedFromReceipts(materialIssuedFromReceipts);
		return unitRate;

	}

	private void fetchRelated(MaterialIssueRequest nonIndentIssueRequest) {
		for (MaterialIssue materialIssue : nonIndentIssueRequest.getMaterialIssues()) {
			if (materialIssue.getFromStore() != null
					&& StringUtils.isNotBlank(materialIssue.getFromStore().getCode())) {
				StoreGetRequest storeGetRequest = new StoreGetRequest();
				storeGetRequest.setCode(Arrays.asList(materialIssue.getFromStore().getCode()));
				storeGetRequest.setTenantId(materialIssue.getTenantId());
				List<Store> stores = storeService.search(storeGetRequest).getStores();
				if (stores.isEmpty())
					throw new CustomException("invalid.ref.value",
							"the field fromstore should have a valid value which exists in the system.");
				else
					materialIssue.setFromStore(stores.get(0));

			}
			if (materialIssue.getToStore() != null && StringUtils.isNotBlank(materialIssue.getToStore().getCode())) {
				StoreGetRequest storeGetRequest = new StoreGetRequest();
				storeGetRequest.setCode(Arrays.asList(materialIssue.getToStore().getCode()));
				storeGetRequest.setTenantId(materialIssue.getTenantId());
				List<Store> stores = storeService.search(storeGetRequest).getStores();
				if (stores.isEmpty())
					throw new CustomException("invalid.ref.value",
							"the field tostore should have a valid value which exists in the system.");
				else
					materialIssue.setToStore(stores.get(0));
			}
			if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Material> materialMap = getMaterials(materialIssue.getTenantId(), mapper,
						new RequestInfo());
				Map<String, Uom> uomMap = getUoms(materialIssue.getTenantId(), mapper, new RequestInfo());
				for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
					if (materialIssueDetail.getMaterial() != null
							&& StringUtils.isNotBlank(materialIssueDetail.getMaterial().getCode())) {
						if (materialMap.get(materialIssueDetail.getMaterial().getCode()) == null)
							throw new CustomException("invalid.ref.value",
									"the field material should have a valid value which exists in the system.");
						else
							materialIssueDetail
									.setMaterial(materialMap.get(materialIssueDetail.getMaterial().getCode()));
					}
					if (materialIssueDetail.getUom() != null
							&& StringUtils.isNotBlank(materialIssueDetail.getUom().getCode())) {
						if (uomMap.get(materialIssueDetail.getUom().getCode()) == null)
							throw new CustomException("invalid.ref.value",
									"the field uom should have a valid value which exists in the system.");
						else
							materialIssueDetail.setUom(uomMap.get(materialIssueDetail.getUom().getCode()));
					}
				}
			}
		}
	}

	private void validate(List<MaterialIssue> materialIssues, String method) {
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
						String date = convertEpochtoDate(materialIssue.getIssueDate());
						errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "issueDate", date);
					}
					if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
						int i = 0;
						for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
							materialIssueDetail.setQuantityIssued(
									InventoryUtilities.getQuantityInBaseUom(materialIssueDetail.getUserQuantityIssued(),
											materialIssueDetail.getUom().getConversionFactor()));
							if (materialIssue.getIssuePurpose().equals(IssuePurposeEnum.RETURNTOSUPPLIER)) {
								if (StringUtils.isBlank(materialIssueDetail.getMrnNumber()))
									errors.addDataError(ErrorCode.FIELD_MANDATORY_IN_CASE.getCode(), "mrnNumber",
											String.valueOf(i));
							}
							if (materialIssueDetail.getQuantityIssued().compareTo(BigDecimal.ZERO) <= 0)
								errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "quantityIssued",
										String.valueOf(i),
										InventoryUtilities
												.getQuantityInSelectedUom(materialIssueDetail.getQuantityIssued(),
														materialIssueDetail.getUom().getConversionFactor())
												.toString());
							FifoRequest fifoRequest = new FifoRequest();
							Fifo fifo = new Fifo();
							fifo.setStore(materialIssue.getFromStore());
							fifo.setMaterial(materialIssueDetail.getMaterial());
							fifo.setIssueDate(materialIssue.getIssueDate());
							fifo.setTenantId(materialIssue.getTenantId());
							fifo.setMrnNumber(materialIssueDetail.getMrnNumber());
							fifoRequest.setFifo(fifo);
							FifoResponse fifoResponse = materialIssueReceiptFifoLogic
									.getTotalStockAsPerMaterial(fifoRequest);
							BigDecimal balanceQuantity = BigDecimal.ZERO;
							if (fifoResponse != null)
								balanceQuantity = fifoResponse.getStock();
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
							if (materialIssue.getIssuePurpose() != null)
								if (materialIssue.getIssuePurpose().equals(IssuePurposeEnum.WRITEOFFORSCRAP)) {
									if (materialIssueDetail.getMaterial() != null
											&& materialIssueDetail.getMaterial().getScrapable() != null) {
										if (!materialIssueDetail.getMaterial().getScrapable())
											errors.addDataError(ErrorCode.ALLOW_SCRAP_MATERIALS.getCode(),
													IssuePurposeEnum.WRITEOFFORSCRAP.toString());
									}
								}
							i++;
						}
						validateDuplicateMaterials(materialIssue.getMaterialIssueDetails(), materialIssue.getTenantId(),
								errors);
					}
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
					if (materialIssue.getIssueNumber() != null) {
						MaterialIssueEntity materialIssueEntity = new MaterialIssueEntity();
						materialIssueEntity.setIssueNumber(materialIssue.getIssueNumber());
						materialIssueEntity.setTenantId(materialIssue.getTenantId());
						Object issueEntity = materialIssueJdbcRepository.findById(materialIssueEntity,
								"MaterialIssueEntity");
						MaterialIssueEntity issueEntityfromDb = (MaterialIssueEntity) issueEntity;
						if (issueEntityfromDb != null) {
							if (materialIssue.getIssueType() != null) {
								if (!issueEntityfromDb.getIssueType().equals(materialIssue.getIssueType().name()))
									errors.addDataError(ErrorCode.NOT_ALLOWED_TO_UPDATE.getCode(), "issueType",
											"MaterialIssue");
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
						if (materialIssue.getIssuePurpose() != null) {
							if (!issueEntityfromDb.getIssuePurpose().toString()
									.equals(materialIssue.getIssuePurpose().toString()))
								errors.addDataError(ErrorCode.NOT_ALLOWED_TO_UPDATE.getCode(), "issuePurpose",
										"MaterialIssue");
						}

					}

					if (!materialIssue.getMaterialIssueDetails().isEmpty()) {
						int i = 0;
						for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
							materialIssueDetail.setQuantityIssued(
									InventoryUtilities.getQuantityInBaseUom(materialIssueDetail.getUserQuantityIssued(),
											materialIssueDetail.getUom().getConversionFactor()));
							if (materialIssueDetail.getQuantityIssued().compareTo(BigDecimal.ZERO) <= 0)
								errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(), "quantityIssued",
										String.valueOf(i),
										InventoryUtilities
												.getQuantityInSelectedUom(materialIssueDetail.getQuantityIssued(),
														materialIssueDetail.getUom().getConversionFactor())
												.toString());
							FifoRequest fifoRequest = new FifoRequest();
							Fifo fifo = new Fifo();
							fifo.setStore(materialIssue.getFromStore());
							fifo.setMaterial(materialIssueDetail.getMaterial());
							fifo.setIssueDate(materialIssue.getIssueDate());
							fifo.setTenantId(materialIssue.getTenantId());
							fifoRequest.setFifo(fifo);
							MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract();
							searchContract.setIssueNoteNumber(materialIssue.getIssueNumber());
							searchContract.setTenantId(materialIssue.getTenantId());
							Pagination<MaterialIssueDetail> listOfPagedMaterialIssueDetails = materialIssueDetailsJdbcRepository
									.search(materialIssue.getIssueNumber(), materialIssue.getTenantId(), null);
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
							FifoResponse fifoResponse = materialIssueReceiptFifoLogic
									.getTotalStockAsPerMaterial(fifoRequest);
							BigDecimal balQuantity = BigDecimal.ZERO;
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
													.toString().toString(),
											InventoryUtilities
													.getQuantityInSelectedUom(balanceQuantity,
															materialIssueDetail.getUom().getConversionFactor())
													.toString(),
											String.valueOf(i));
								}
							}
							if (materialIssue.getIssuePurpose() != null)
								if (materialIssue.getIssuePurpose().equals(IssuePurposeEnum.WRITEOFFORSCRAP)) {
									if (materialIssueDetail.getMaterial() != null
											&& materialIssueDetail.getMaterial().getScrapable() != null) {
										if (!materialIssueDetail.getMaterial().getScrapable())
											errors.addDataError(ErrorCode.ALLOW_SCRAP_MATERIALS.getCode(),
													IssuePurposeEnum.WRITEOFFORSCRAP.toString());
									}
								}
							i++;
						}
						validateDuplicateMaterials(materialIssue.getMaterialIssueDetails(), materialIssue.getTenantId(),
								errors);
					}
					break;
				}
			}
		} catch (IllegalArgumentException e) {
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;
	}

	private void setMaterialIssueValues(MaterialIssue materialIssue, String seqNo, String action) {
		materialIssue.setIssueType(IssueTypeEnum.NONINDENTISSUE);
		if (action.equals(Constants.ACTION_CREATE)) {
			int year = Calendar.getInstance().get(Calendar.YEAR);
			materialIssue.setIssueNumber("MRNIN-" + String.valueOf(year) + "-" + seqNo);
			materialIssue.setMaterialIssueStatus(MaterialIssueStatusEnum.CREATED);
		}

	}

	public MaterialIssueResponse update(final MaterialIssueRequest materialIssueRequest, String tenantId) {
		fetchRelated(materialIssueRequest);
		validate(materialIssueRequest.getMaterialIssues(), Constants.ACTION_UPDATE);
		List<MaterialIssue> materialIssues = materialIssueRequest.getMaterialIssues();
		int i = 0;
		String issuePurpose = "";

		List<MaterialIssue> existingMaterialIssue = new ArrayList<>();
		for (MaterialIssue materialIssue : materialIssues) {
			if (materialIssue.getIssuePurpose() != null)
				issuePurpose = materialIssue.getIssuePurpose().toString();

			if (StringUtils.isEmpty(materialIssue.getTenantId()))
				materialIssue.setTenantId(tenantId);
			MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract();
			searchContract.setIssueNoteNumber(materialIssue.getIssueNumber());
			searchContract.setTenantId(materialIssue.getTenantId());
			MaterialIssueResponse issueResponse = search(searchContract);
			List<MaterialIssueDetail> materialIssueDetails = issueResponse.getMaterialIssues().get(0)
					.getMaterialIssueDetails();

			existingMaterialIssue.addAll(issueResponse.getMaterialIssues());
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
			materialIssuedFromReceiptsJdbcRepository.updateStatus(materialIssuedFromReceiptsIds,
					materialIssue.getTenantId());
			if (materialIssue.getMaterialIssueStatus() != null)
				if (materialIssue.getMaterialIssueStatus().toString()
						.equals(MaterialIssueStatusEnum.CANCELED.toString())) {
					issueResponse.getMaterialIssues().get(0).setMaterialIssueStatus(MaterialIssueStatusEnum.CANCELED);
					updateStatusAsCancelled(tenantId, materialIssue);
					materialIssues.set(i, issueResponse.getMaterialIssues().get(0));
					i++;
					continue;
				}
			setMaterialIssueValues(materialIssue, null, Constants.ACTION_UPDATE);
			materialIssue
					.setAuditDetails(getAuditDetails(materialIssueRequest.getRequestInfo(), Constants.ACTION_UPDATE));
			BigDecimal totalIssueValue = BigDecimal.ZERO;
			List<String> materialIssueDetailsIds = new ArrayList<>();
			for (MaterialIssueDetail materialIssueDetail : materialIssue.getMaterialIssueDetails()) {
				if (StringUtils.isEmpty(materialIssueDetail.getTenantId()))
					materialIssueDetail.setTenantId(tenantId);
				if (StringUtils.isEmpty(materialIssueDetail.getId()))
					materialIssueDetail.setId(materialIssueDetailsJdbcRepository
							.getSequence(MaterialIssueDetail.class.getSimpleName(), 1).get(0));
				materialIssueDetailsIds.add(materialIssueDetail.getId());
				BigDecimal value = getMaterialIssuedFromReceiptData(materialIssue.getFromStore(),
						materialIssueDetail.getMaterial(), materialIssue.getIssueDate(), materialIssue.getTenantId(),
						materialIssueDetail, materialIssue);
				totalIssueValue = totalIssueValue.add(value);
				materialIssueDetail.setValue(value);
			}
			materialIssueDetailsJdbcRepository.markDeleted(materialIssueDetailsIds, tenantId, "materialissuedetail",
					"materialissuenumber", materialIssue.getIssueNumber());
			materialIssue.setTotalIssueValue(totalIssueValue);
			i++;
		}

		kafkaTemplate.send(updateTopic, updateKey, materialIssueRequest);
		MaterialIssueResponse response = new MaterialIssueResponse();
		response.setMaterialIssues(materialIssueRequest.getMaterialIssues());
		response.setResponseInfo(getResponseInfo(materialIssueRequest.getRequestInfo()));

		if ((issuePurpose != null && !issuePurpose.isEmpty())
				&& issuePurpose.equals(IssuePurposeEnum.WRITEOFFORSCRAP.toString())) {
			updateScrapDetails(materialIssueRequest, tenantId, existingMaterialIssue);
		}

		return response;
	}

	private void updateStatusAsCancelled(String tenantId, MaterialIssue materialIssue) {

		materialIssueJdbcRepository.updateStatus(materialIssue.getIssueNumber(), "CANCELED",
				materialIssue.getTenantId());
	}

	public MaterialIssueResponse search(MaterialIssueSearchContract searchContract) {
		Pagination<MaterialIssue> materialIssues = materialIssueJdbcRepository.search(searchContract,
				IssueTypeEnum.NONINDENTISSUE.toString());
		if (!materialIssues.getPagedData().isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Material> materialMap = getMaterials(searchContract.getTenantId(), mapper, new RequestInfo());
			Map<String, Uom> uoms = getUoms(searchContract.getTenantId(), mapper, new RequestInfo());

			for (MaterialIssue materialIssue : materialIssues.getPagedData()) {

				if (materialIssue.getFromStore() != null && materialIssue.getFromStore().getCode() != null) {
					materialIssue.setFromStore(
							getStore(materialIssue.getFromStore().getCode(), searchContract.getTenantId()));
				}
				if (materialIssue.getToStore() != null && materialIssue.getToStore().getCode() != null) {
					materialIssue.toStore(getStore(materialIssue.getToStore().getCode(), searchContract.getTenantId()));
				}

				if (materialIssue.getSupplier() != null && materialIssue.getSupplier().getCode() != null) {
					Supplier response = supplierRepository.getByCode(materialIssue.getSupplier().getCode());
					materialIssue.setSupplier(response == null ? materialIssue.getSupplier() : response);
				}
				Pagination<MaterialIssueDetail> materialIssueDetails = materialIssueDetailsJdbcRepository.search(
						materialIssue.getIssueNumber(), materialIssue.getTenantId(),
						IssueTypeEnum.NONINDENTISSUE.toString());
				if (materialIssueDetails.getPagedData().size() > 0) {
					for (MaterialIssueDetail materialIssueDetail : materialIssueDetails.getPagedData()) {
						materialIssueDetail.setUom(uoms.get(materialIssueDetail.getUom().getCode()));
						if (searchContract.getSearchPurpose() != null) {
							if (searchContract.getSearchPurpose().equals("update")) {
								materialIssueDetail.setBalanceQuantity(InventoryUtilities.getQuantityInSelectedUom(
										getBalanceQuantityByStoreByMaterialAndIssueDate(materialIssue.getFromStore(),
												materialIssueDetail.getMaterial(), materialIssue.getIssueDate(),
												materialIssue.getTenantId())
														.add(materialIssueDetail.getQuantityIssued()),
										uoms.get(materialIssueDetail.getUom().getCode()).getConversionFactor()));
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
						if (materialIssueDetail.getMaterial() != null
								&& materialIssueDetail.getMaterial().getCode() != null) {
							materialIssueDetail
									.setMaterial(materialMap.get(materialIssueDetail.getMaterial().getCode()));
						}

						materialIssueDetail.setMaterialIssuedFromReceipts(materialIssuedFromReceipts.getPagedData());
					}
					materialIssue.setMaterialIssueDetails(materialIssueDetails.getPagedData());
				}
			}
		}
		MaterialIssueResponse materialIssueResponse = new MaterialIssueResponse();
		materialIssueResponse.setMaterialIssues(materialIssues.getPagedData());
		return materialIssueResponse;
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

	private List<MaterialReceiptDetail> getMaterialReceiptDetail(String ids, String tenantId) {
		MaterialReceiptDetailSearch materialReceiptDetailSearch = MaterialReceiptDetailSearch.builder()
				.ids(Arrays.asList(ids)).tenantId(tenantId).build();
		Pagination<MaterialReceiptDetail> materialReceiptDetails = materialReceiptDetailService
				.search(materialReceiptDetailSearch);

		if (!materialReceiptDetails.getPagedData().isEmpty()) {
			Map<String, Material> materialMap = getMaterials(tenantId, new ObjectMapper(), new RequestInfo());
			for (MaterialReceiptDetail details : materialReceiptDetails.getPagedData()) {
				details.setMaterial(materialMap.get(details.getMaterial().getCode()));
			}
		}
		return materialReceiptDetails.getPagedData().size() > 0 ? materialReceiptDetails.getPagedData()
				: Collections.EMPTY_LIST;
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

	private BigDecimal getBalanceQuantityByStoreByMaterialAndIssueDate(Store store, Material material, Long issueDate,
			String tenantId) {
		BigDecimal balanceQuantity = BigDecimal.ZERO;
		LOG.info("store :" + store + "material :" + material + "issueDate :" + issueDate + "tenantId :" + tenantId);
		FifoRequest fifoRequest = new FifoRequest();
		Fifo fifo = new Fifo();
		fifo.setStore(store);
		fifo.setMaterial(material);
		fifo.setIssueDate(issueDate);
		fifo.setTenantId(tenantId);
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

	private String convertEpochtoDate(Long date) {
		Date epoch = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String s2 = format.format(epoch);
		return s2;
	}

	public PDFResponse printPdf(MaterialIssueSearchContract searchContract, RequestInfo requestInfo) {
		MaterialIssueResponse materialIssueResponse = search(searchContract);
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
				indent.put("issueNumber", in.getIssueNumber());
				if (in.getIssueDate() != null) {
					Instant issueDate = Instant.ofEpochMilli(in.getIssueDate());
					indent.put("issueDate", fmt.format(issueDate.atZone(ZoneId.systemDefault())));
				} else {
					indent.put("issueDate", in.getIssueDate());
				}

				indent.put("issuingStoreName", in.getFromStore().getName());
				indent.put("issuingStoreDept", in.getFromStore().getDepartment().getName());
				indent.put("issueStatus", in.getMaterialIssueStatus());
				indent.put("indentPurpose", in.getIssuePurpose());
				indent.put("supplierName", in.getSupplier().getName());
				indent.put("remark", in.getDescription());
				indent.put("createdBy", in.getCreatedByName());
				indent.put("designation", in.getDesignation());

				JSONArray indentDetails = new JSONArray();
				int i = 1;
				BigDecimal totalIssueAmount = BigDecimal.ZERO;
				for (MaterialIssueDetail detail : in.getMaterialIssueDetails()) {
					JSONObject indentDetail = new JSONObject();
					indentDetail.put("srNo", i++);
					indentDetail.put("materialCode", detail.getMaterial().getCode());
					indentDetail.put("materialName", detail.getMaterial().getName());
					indentDetail.put("materialDescription", detail.getMaterial().getDescription());
					indentDetail.put("uomName", detail.getUom().getName());
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

				requestMain.put("NonIndentIssueNote", indents);
			}

			return pdfServiceReposistory.getPrint(requestMain, "store-asset-non-indent-issue-note",
					searchContract.getTenantId());
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
			kafkaQue.send(updateStatusTopic, updateStatusKey, indentIssueRequest);
			MaterialIssueResponse response = new MaterialIssueResponse();
			response.setMaterialIssues(indentIssueRequest.getMaterialIssues());
			response.setResponseInfo(getResponseInfo(indentIssueRequest.getRequestInfo()));
			// indentIssueRequest.getWorkFlowDetails().setStatus("Approved");
			if (indentIssueRequest.getWorkFlowDetails().getStatus()
					.equals(MaterialIssueStatusEnum.REJECTED.toString())) {
				MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(
						indentIssueRequest.getWorkFlowDetails().getTenantId(), null, null, null,
						indentIssueRequest.getWorkFlowDetails().getBusinessId(), null, null, null, null, null, null,
						null, null, null, null, null, null, null);
				MaterialIssueResponse issueResponse = search(searchContract);

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

			} else if (indentIssueRequest.getWorkFlowDetails().getStatus()
					.equals(MaterialIssueStatusEnum.APPROVED.toString())) {
				MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(
						indentIssueRequest.getWorkFlowDetails().getTenantId(), null, null, null,
						indentIssueRequest.getWorkFlowDetails().getBusinessId(), null, null, null, null, null, null,
						null, null, null, null, null, null, null);
				MaterialIssueResponse issueResponse = search(searchContract);

				if ((issueResponse.getMaterialIssues().get(0).getIssuePurpose() != null)
						&& issueResponse.getMaterialIssues().get(0).getIssuePurpose().toString()
								.equals(IssuePurposeEnum.WRITEOFFORSCRAP.toString())) {
					indentIssueRequest.setMaterialIssues(issueResponse.getMaterialIssues());
					addScrapDetails(indentIssueRequest, indentIssueRequest.getWorkFlowDetails().getTenantId());
				}
			}
			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}
}