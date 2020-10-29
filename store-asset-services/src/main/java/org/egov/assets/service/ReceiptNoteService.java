package org.egov.assets.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.SupplierRepository;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialBalanceRate;
import org.egov.assets.model.MaterialBalanceRateResponse;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceipt.MrnStatusEnum;
import org.egov.assets.model.MaterialReceipt.ReceiptTypeEnum;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptDetailAddnlinfo;
import org.egov.assets.model.MaterialReceiptRequest;
import org.egov.assets.model.MaterialReceiptResponse;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.PriceList.RateTypeEnum;
import org.egov.assets.model.PurchaseOrder;
import org.egov.assets.model.PurchaseOrder.PurchaseTypeEnum;
import org.egov.assets.model.PurchaseOrder.StatusEnum;
import org.egov.assets.model.PurchaseOrderDetail;
import org.egov.assets.model.PurchaseOrderDetailSearch;
import org.egov.assets.model.PurchaseOrderSearch;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.StoreResponse;
import org.egov.assets.model.Supplier;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.PurchaseOrderDetailJdbcRepository;
import org.egov.assets.repository.PurchaseOrderJdbcRepository;
import org.egov.assets.repository.ReceiptNoteRepository;
import org.egov.assets.repository.entity.PurchaseOrderDetailEntity;
import org.egov.assets.repository.entity.PurchaseOrderEntity;
import org.egov.assets.util.InventoryUtilities;
import org.egov.assets.wf.WorkflowIntegrator;
import org.egov.assets.wf.model.ProcessInstance;
import org.egov.assets.wf.model.ProcessInstanceResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReceiptNoteService extends DomainService {

	@Autowired
	private LogAwareKafkaTemplate<String, Object> logAwareKafkaTemplate;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private PurchaseOrderJdbcRepository purchaseOrderRepository;

	@Value("${inv.purchaseorders.nonindent.save.topic}")
	private String saveNonIndentTopic;

	@Value("${inv.purchaseorders.nonindent.save.key}")
	private String saveNonIndentKey;

	@Value("${inv.materialreceiptnote.save.topic}")
	private String createTopic;

	@Value("${inv.materialreceiptnote.update.topic}")
	private String updateTopic;

	@Value("${inv.materialreceiptnote.save.key}")
	private String createTopicKey;

	@Value("${inv.materialreceiptnote.update.topic}")
	private String updateTopicKey;

	@Value("${inv.purchaseorders.cancelreceipt.key}")
	private String cancelReceiptPOTopicKey;

	@Value("${inv.purchaseorders.cancelreceipt.topic}")
	private String cancelReceiptPOTopic;

	@Value("${inv.materialreceiptnote.updatestatus.topic}")
	private String updateStatusTopic;

	@Value("${inv.materialreceiptnote.updatestatus.key}")
	private String updateStatusTopicKey;

	@Autowired
	private MaterialReceiptService materialReceiptService;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private ReceiptNoteRepository receiptNoteRepository;

	@Autowired
	private PurchaseOrderService purchaseOrderService;

	@Autowired
	private PurchaseOrderDetailService purchaseOrderDetailService;

	@Autowired
	private MaterialService materialService;

	@Autowired
	private StoreService storeService;

	@Autowired
	private SupplierRepository supplierRepository;

	@Autowired
	private PurchaseOrderDetailJdbcRepository purchaseOrderDetailJdbcRepository;

	@Autowired
	private MdmsRepository mdmsRepository;

	@Autowired
	private PurchaseOrderJdbcRepository purchaseOrderJdbcRepository;

	private static final Logger LOG = LoggerFactory.getLogger(ReceiptNoteService.class);

	public MaterialReceiptResponse create(MaterialReceiptRequest materialReceiptRequest, String tenantId) {
		List<MaterialReceipt> materialReceipts = materialReceiptRequest.getMaterialReceipt();
		for (MaterialReceipt m : materialReceipts) {
			for (MaterialReceiptDetail receiptDetails : m.getReceiptDetails()) {
				if (receiptDetails.getUserReceivedQty() != null) {
					setUomAndQuantity(tenantId, receiptDetails);
				}
				for (MaterialReceiptDetailAddnlinfo receiptDetailsAddnInfo : receiptDetails
						.getReceiptDetailsAddnInfo()) {
					setUomAndQuantityForDetailInfo(tenantId, receiptDetails, receiptDetailsAddnInfo);
				}
			}
		}

		validate(materialReceipts, tenantId, Constants.ACTION_CREATE, materialReceiptRequest.getRequestInfo());

		materialReceipts.forEach(materialReceipt -> {
			materialReceipt.setId(receiptNoteRepository.getSequence("seq_materialreceipt"));
			if (StringUtils.isEmpty(materialReceipt.getTenantId())) {
				materialReceipt.setTenantId(tenantId);
			}
			materialReceipt.setMrnNumber(appendString(materialReceipt));
			materialReceipt.setMrnStatus(MaterialReceipt.MrnStatusEnum.CREATED);
			materialReceipt
					.setAuditDetails(getAuditDetails(materialReceiptRequest.getRequestInfo(), Constants.ACTION_CREATE));

			materialReceipt.getReceiptDetails().forEach(materialReceiptDetail -> {
				setMaterialDetails(tenantId, materialReceiptDetail);
				materialReceiptDetail.setAuditDetails(
						getAuditDetails(materialReceiptRequest.getRequestInfo(), Constants.ACTION_CREATE));
			});

			backUpdatePo(tenantId, materialReceipt);

			WorkFlowDetails workFlowDetails = materialReceiptRequest.getWorkFlowDetails();
			workFlowDetails.setBusinessId(materialReceipt.getMrnNumber());
			workflowIntegrator.callWorkFlow(materialReceiptRequest.getRequestInfo(), workFlowDetails, tenantId);
		});
		logAwareKafkaTemplate.send(createTopic, createTopicKey, materialReceiptRequest);
		MaterialReceiptResponse materialReceiptResponse = new MaterialReceiptResponse();

		return materialReceiptResponse.responseInfo(null).materialReceipt(materialReceipts);
	}

	public MaterialReceiptResponse update(MaterialReceiptRequest materialReceiptRequest, String tenantId) {
		List<MaterialReceipt> materialReceipts = materialReceiptRequest.getMaterialReceipt();

		for (MaterialReceipt m : materialReceipts) {
			for (MaterialReceiptDetail receiptDetails : m.getReceiptDetails()) {
				if (receiptDetails.getUserReceivedQty() != null) {
					setUomAndQuantity(tenantId, receiptDetails);
				}
				for (MaterialReceiptDetailAddnlinfo receiptDetailsAddnInfo : receiptDetails
						.getReceiptDetailsAddnInfo()) {
					setUomAndQuantityForDetailInfo(tenantId, receiptDetails, receiptDetailsAddnInfo);
				}
			}
		}

		validate(materialReceipts, tenantId, Constants.ACTION_UPDATE, materialReceiptRequest.getRequestInfo());

		List<String> materialReceiptDetailIds = new ArrayList<>();
		List<String> materialReceiptDetailAddlnInfoIds = new ArrayList<>();
		materialReceipts.forEach(materialReceipt -> {
			if (StringUtils.isEmpty(materialReceipt.getTenantId())) {
				materialReceipt.setTenantId(tenantId);
			}

			materialReceipt.getReceiptDetails().forEach(materialReceiptDetail -> {
				if (isEmpty(materialReceiptDetail.getTenantId())) {
					materialReceiptDetail.setTenantId(tenantId);
				}

				if (isEmpty(materialReceiptDetail.getId())) {
					setMaterialDetails(tenantId, materialReceiptDetail);
				}

				materialReceiptDetailIds.add(materialReceiptDetail.getId());

				materialReceiptDetail.getReceiptDetailsAddnInfo().forEach(materialReceiptDetailAddnlInfo -> {
					materialReceiptDetailAddlnInfoIds.add(materialReceiptDetailAddnlInfo.getId());

					if (isEmpty(materialReceiptDetailAddnlInfo.getTenantId())) {
						materialReceiptDetailAddnlInfo.setTenantId(tenantId);
					}
				});
				receiptNoteRepository.markDeleted(materialReceiptDetailAddlnInfoIds, tenantId,
						"materialreceiptdetailaddnlinfo", "receiptdetailid", materialReceiptDetail.getId());

				receiptNoteRepository.markDeleted(materialReceiptDetailIds, tenantId, "materialreceiptdetail",
						"mrnNumber", materialReceipt.getMrnNumber());

			});

			if (MaterialReceipt.MrnStatusEnum.CANCELED.toString()
					.equalsIgnoreCase(materialReceipt.getMrnStatus().toString())) {
				for (MaterialReceiptDetail materialReceiptDetail : materialReceipt.getReceiptDetails()) {
					HashMap<String, String> hashMap = new HashMap<>();
					hashMap.put("receivedquantity", "receivedquantity - " + materialReceiptDetail.getAcceptedQty());
					materialReceiptDetail.getPurchaseOrderDetail().setTenantId(tenantId);
					receiptNoteRepository.updateColumn(
							new PurchaseOrderDetailEntity().toEntity(materialReceiptDetail.getPurchaseOrderDetail()),
							"purchaseorderdetail", hashMap, null);

					receiptNoteRepository.updateColumn(new PurchaseOrderEntity(), "purchaseorder", new HashMap<>(),
							"status = (case when status = 'RECEIPTED' then 'Approved' ELSE status end)"
									+ " where purchaseordernumber = (select purchaseorder from purchaseorderdetail where id = '"
									+ materialReceiptDetail.getPurchaseOrderDetail().getId() + "') and tenantid = '"
									+ tenantId + "'");
				}
			}

			backUpdatePo(tenantId, materialReceipt);
		});

		logAwareKafkaTemplate.send(updateTopic, updateTopicKey, materialReceiptRequest);

		MaterialReceiptResponse materialReceiptResponse = new MaterialReceiptResponse();

		return materialReceiptResponse.responseInfo(null).materialReceipt(materialReceipts);
	}

	private void backUpdatePo(String tenantId, MaterialReceipt materialReceipt) {
		if (MaterialReceipt.ReceiptTypeEnum.PURCHASE_RECEIPT.toString()
				.equalsIgnoreCase(materialReceipt.getReceiptType().toString())) {
			for (MaterialReceiptDetail materialReceiptDetail : materialReceipt.getReceiptDetails()) {
				HashMap<String, String> hashMap = new HashMap<>();
				hashMap.put("receivedquantity", "receivedquantity + " + materialReceiptDetail.getAcceptedQty());
				materialReceiptDetail.getPurchaseOrderDetail().setTenantId(tenantId);
				receiptNoteRepository.updateColumn(
						new PurchaseOrderDetailEntity().toEntity(materialReceiptDetail.getPurchaseOrderDetail()),
						"purchaseorderdetail", hashMap, null);

				PurchaseOrderDetailEntity purchaseOrderDetailEntity = new PurchaseOrderDetailEntity();
				purchaseOrderDetailEntity.setId(materialReceiptDetail.getPurchaseOrderDetail().getId());
				purchaseOrderDetailEntity.setTenantId(tenantId);
				PurchaseOrderDetailEntity orderDetailEntity = purchaseOrderDetailJdbcRepository
						.findById(purchaseOrderDetailEntity);

				PurchaseOrderSearch purchaseOrderSearch = new PurchaseOrderSearch();
				purchaseOrderSearch.setPurchaseOrderNumber(orderDetailEntity.getPurchaseOrder());
				purchaseOrderSearch.setTenantId(tenantId);
				if (purchaseOrderService.checkAllItemsSuppliedForPo(purchaseOrderSearch))
					receiptNoteRepository.updateColumn(new PurchaseOrderEntity(), "purchaseorder", new HashMap<>(),
							"status = (case when status = 'RECEIPTED' then 'Approved' ELSE status end)"
									+ " where purchaseordernumber = ('" + purchaseOrderSearch.getPurchaseOrderNumber()
									+ "') and tenantid = '" + tenantId + "'");
			}
		}
	}

	public MaterialReceiptResponse search(MaterialReceiptSearch materialReceiptSearch) {
		Pagination<MaterialReceipt> materialReceiptPagination = materialReceiptService.search(materialReceiptSearch);
		MaterialReceiptResponse response = new MaterialReceiptResponse();
		return response.responseInfo(null).materialReceipt(
				materialReceiptPagination.getPagedData().size() > 0 ? materialReceiptPagination.getPagedData()
						: Collections.EMPTY_LIST);
	}

	public MaterialBalanceRateResponse searchBalanceAndRate(MaterialReceiptSearch materialReceiptSearch) {
		Pagination<MaterialBalanceRate> materialBalanceRate = materialReceiptService
				.searchBalanceRate(materialReceiptSearch);
		MaterialBalanceRateResponse response = new MaterialBalanceRateResponse();
		if (!materialBalanceRate.getPagedData().isEmpty()) {
			List<MaterialBalanceRate> materialBalanceRate2 = materialBalanceRate.getPagedData();
			setMaterials(materialBalanceRate2, materialReceiptSearch.getTenantId());
			response.setMaterialBalanceRate(materialBalanceRate2);
		} else {
			response.setMaterialBalanceRate(Collections.EMPTY_LIST);
		}
		return response;
	}

	private void setMaterials(List<MaterialBalanceRate> materialBalanceRate, String tenantId) {

		for (MaterialBalanceRate materialBalanceRate2 : materialBalanceRate) {
			Material material = materialService.fetchMaterial(tenantId, materialBalanceRate2.getMaterialCode(),
					new RequestInfo());
			BigDecimal rounded = new BigDecimal(materialBalanceRate2.getBalance().toString());
			rounded = rounded.setScale(2, RoundingMode.CEILING);
			materialBalanceRate2.setMaterialName(String.format("%s (Qty:%s, Rate:%s)", material.getName(), rounded,
					materialBalanceRate2.getUnitRate()));
		}
	}

	private void setMaterialDetails(String tenantId, MaterialReceiptDetail materialReceiptDetail) {
		materialReceiptDetail.setId(receiptNoteRepository.getSequence("seq_materialreceiptdetail"));
		if (isEmpty(materialReceiptDetail.getTenantId())) {
			materialReceiptDetail.setTenantId(tenantId);
		}

		// setUomAndQuantity(tenantId, materialReceiptDetail);
		convertRate(tenantId, materialReceiptDetail);

		Material material = materialService.fetchMaterial(tenantId, materialReceiptDetail.getMaterial().getCode(),
				new RequestInfo());
		if (false == material.getSerialNumber() && false == material.getShelfLifeControl()
				&& false == material.getLotControl()) {
			materialReceiptDetail.setReceiptDetailsAddnInfo(Collections.EMPTY_LIST);
		} else {
			materialReceiptDetail.getReceiptDetailsAddnInfo().forEach(materialReceiptDetailAddnlInfo -> {
				materialReceiptDetailAddnlInfo
						.setId(receiptNoteRepository.getSequence("seq_materialreceiptdetailaddnlinfo"));
				if (isEmpty(materialReceiptDetailAddnlInfo.getTenantId())) {
					materialReceiptDetailAddnlInfo.setTenantId(tenantId);
					Uom uom = getUom(tenantId, materialReceiptDetail.getUom().getCode(), new RequestInfo());
				}
			});
		}
	}

	private void setUomAndQuantity(String tenantId, MaterialReceiptDetail materialReceiptDetail) {
		Uom uom = getUom(tenantId, materialReceiptDetail.getUom().getCode(), new RequestInfo());
		materialReceiptDetail.setUom(uom);

		if (null != materialReceiptDetail.getUserReceivedQty() && null != uom.getConversionFactor()) {
			materialReceiptDetail.setReceivedQty(InventoryUtilities
					.getQuantityInBaseUom(materialReceiptDetail.getUserReceivedQty(), uom.getConversionFactor()));

		}

		if (null != materialReceiptDetail.getUserAcceptedQty() && null != uom.getConversionFactor()) {
			materialReceiptDetail.setAcceptedQty(InventoryUtilities
					.getQuantityInBaseUom(materialReceiptDetail.getUserAcceptedQty(), uom.getConversionFactor()));

		}
	}

	private void setUomAndQuantityForDetailInfo(String tenantId, MaterialReceiptDetail materialReceiptDetail,
			MaterialReceiptDetailAddnlinfo receiptDetailsAddnInfo) {
		Uom uom = materialReceiptDetail.getUom();
		if (null != receiptDetailsAddnInfo.getUserQuantity() && null != uom.getConversionFactor()) {
			receiptDetailsAddnInfo.setQuantity(InventoryUtilities
					.getQuantityInBaseUom(receiptDetailsAddnInfo.getUserQuantity(), uom.getConversionFactor()));
		}

	}

	private void convertRate(String tenantId, MaterialReceiptDetail detail) {
		Uom uom = getUom(tenantId, detail.getUom().getCode(), new RequestInfo());
		detail.setUom(uom);

		if (null != detail.getUnitRate() && null != uom.getConversionFactor()) {
			BigDecimal convertedRate = getSaveConvertedRate(detail.getUnitRate(), uom.getConversionFactor());
			detail.setUnitRate(convertedRate);
		}

	}

	private void validate(List<MaterialReceipt> materialReceipts, String tenantId, String method,
			RequestInfo requestInfo) {
		InvalidDataException errors = new InvalidDataException();

		try {
			switch (method) {

			case Constants.ACTION_CREATE: {
				if (materialReceipts == null) {
					throw new InvalidDataException("materialreceipt", ErrorCode.NOT_NULL.getCode(), null);
				} else {
					for (MaterialReceipt materialReceipt : materialReceipts) {
						validateMaterialReceipt(materialReceipt, tenantId, errors, requestInfo);
					}
				}
			}

				break;

			case Constants.ACTION_UPDATE: {
				if (materialReceipts == null) {
					throw new InvalidDataException("materialreceipt", ErrorCode.NOT_NULL.getCode(), null);
				} else {
					for (MaterialReceipt materialReceipt : materialReceipts) {
						validateMaterialReceipt(materialReceipt, tenantId, errors, requestInfo);
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

	private void validateMaterialReceipt(MaterialReceipt materialReceipt, String tenantId, InvalidDataException errors,
			RequestInfo requestInfo) {

		if (null != materialReceipt.getReceivingStore() && !isEmpty(materialReceipt.getReceivingStore().getCode())) {
			validateStore(materialReceipt.getReceivingStore().getCode(), tenantId, errors);
		}

		if (null != materialReceipt.getSupplierBillDate() && materialReceipt.getSupplierBillDate() > getCurrentDate()) {
			String date = convertEpochtoDate(materialReceipt.getSupplierBillDate());
			errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "Supplier bill date ", date);
		}

		if (null != materialReceipt.getChallanDate() && materialReceipt.getChallanDate() > getCurrentDate()) {
			String date = convertEpochtoDate(materialReceipt.getChallanDate());
			errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "Challan date ", date);
		}

		if (null != materialReceipt.getReceiptDate() && materialReceipt.getReceiptDate() > getCurrentDate()) {
			String date = convertEpochtoDate(materialReceipt.getReceiptDate());
			errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "Receipt date ", date);
		}

		if (null != materialReceipt.getSupplier() && !isEmpty(materialReceipt.getSupplier().getCode())) {
			validateSupplier(materialReceipt, tenantId, errors);
		}

		validateMaterialReceiptDetail(materialReceipt, tenantId, errors, requestInfo);

	}

	private Long getCurrentDate() {
		return currentEpochWithoutTime() + (24 * 60 * 60 * 1000) - 1;
	}

	private void validateMaterialReceiptDetail(MaterialReceipt materialReceipt, String tenantId,
			InvalidDataException errors, RequestInfo requestInfo) {
		int i = 0;
		validateDuplicateMaterialDetails(materialReceipt.getReceiptDetails(), errors);
		for (MaterialReceiptDetail materialReceiptDetail : materialReceipt.getReceiptDetails()) {
			i++;
			materialReceiptDetail.setUom(getUom(materialReceiptDetail, tenantId, requestInfo));
			if (materialReceipt.getReceiptType().toString()
					.equalsIgnoreCase(MaterialReceipt.ReceiptTypeEnum.PURCHASE_RECEIPT.toString())) {
				validatePurchaseOrder(materialReceiptDetail, materialReceipt.getReceivingStore().getCode(),
						materialReceipt.getReceiptDate(), materialReceipt.getSupplier().getCode(), tenantId, i, errors);
			}
			validateMaterial(materialReceiptDetail, tenantId, i, errors);
			validateUom(materialReceiptDetail, tenantId, i, errors, requestInfo);
			validateQuantity(materialReceiptDetail, i, errors);
			if (materialReceiptDetail.getReceiptDetailsAddnInfo().size() > 0) {
				validateDetailsAddnInfo(materialReceiptDetail.getReceiptDetailsAddnInfo(),
						materialReceiptDetail.getAcceptedQty(), tenantId, i, errors);
			}
		}
	}

	private void validateStore(String storeCode, String tenantId, InvalidDataException errors) {
		StoreGetRequest storeGetRequest = StoreGetRequest.builder().code(Collections.singletonList(storeCode))
				.tenantId(tenantId).build();

		StoreResponse storeResponse = storeService.search(storeGetRequest);
		if (storeResponse.getStores().size() == 0) {
			errors.addDataError(ErrorCode.INVALID_REF_VALUE.getCode(), "store", storeCode);
		}
	}

	private void validateSupplier(MaterialReceipt materialReceipt, String tenantId, InvalidDataException errors) {
		Supplier suppliers = supplierRepository.getByCode(materialReceipt.getSupplier().getCode());
		if (suppliers == null) {
			errors.addDataError(ErrorCode.INVALID_REF_VALUE.getCode(), "supplier",
					materialReceipt.getSupplier().getCode());

		}
	}

	private void validateUom(MaterialReceiptDetail materialReceiptDetail, String tenantId, int i,
			InvalidDataException errors, RequestInfo requestInfo) {
		if (null != materialReceiptDetail.getUom() && !isEmpty(materialReceiptDetail.getUom().getCode())) {
			Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code",
					materialReceiptDetail.getUom().getCode(), Uom.class, requestInfo);
			materialReceiptDetail.setUom(uom);
		} else
			errors.addDataError(ErrorCode.OBJECT_NOT_FOUND_ROW.getCode(), "UOM ",
					materialReceiptDetail.getUom().toString(), String.valueOf(i));

	}

	private Uom getUom(MaterialReceiptDetail materialReceiptDetail, String tenantId, RequestInfo requestInfo) {
		Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code",
				materialReceiptDetail.getUom().getCode(), Uom.class, requestInfo);
		return uom;
	}

	private void validateQuantity(MaterialReceiptDetail materialReceiptDetail, int i, InvalidDataException errors) {

		if (isEmpty(materialReceiptDetail.getReceivedQty())) {
			errors.addDataError(ErrorCode.MANDATORY_VALUE_MISSING.getCode(),
					"Received Quantity is Required at row " + i);
		}

		if (!isEmpty(materialReceiptDetail.getReceivedQty())
				&& (materialReceiptDetail.getReceivedQty().compareTo(BigDecimal.ZERO) == 0
						|| materialReceiptDetail.getReceivedQty().compareTo(BigDecimal.ZERO) == -1)) {
			errors.addDataError(ErrorCode.QTY_GTR_ROW.getCode(), "Received Quantity", String.valueOf(i));
		}

		if (isEmpty(materialReceiptDetail.getAcceptedQty())) {
			errors.addDataError(ErrorCode.MANDATORY_VALUE_MISSINGROW.getCode(), "Accepted Quantity ",
					String.valueOf(i));
		}

		if (!isEmpty(materialReceiptDetail.getAcceptedQty())
				&& (materialReceiptDetail.getAcceptedQty().compareTo(BigDecimal.ZERO) == 0
						|| materialReceiptDetail.getAcceptedQty().compareTo(BigDecimal.ZERO) == -1)) {
			errors.addDataError(ErrorCode.QTY_GTR_ROW.getCode(), "Accepted Quantity ", String.valueOf(i));
		}

		if (materialReceiptDetail.getAcceptedQty().compareTo(materialReceiptDetail.getReceivedQty()) == 1) {
			errors.addDataError(ErrorCode.QTY_LE_SCND_ROW.getCode(), "Accepted Quantity ", "Received Quantity",
					String.valueOf(i));
		}
	}

	private void validateMaterial(MaterialReceiptDetail receiptDetail, String tenantId, int i,
			InvalidDataException errors) {
		if (null != receiptDetail.getMaterial() && !isEmpty(receiptDetail.getMaterial().getCode())) {
			Material material = materialService.fetchMaterial(tenantId, receiptDetail.getMaterial().getCode(),
					new RequestInfo());

			for (MaterialReceiptDetailAddnlinfo addnlinfo : receiptDetail.getReceiptDetailsAddnInfo()) {
				if (true == (material.getLotControl() == null ? false : material.getLotControl())
						&& isEmpty(addnlinfo.getLotNo())) {
					errors.addDataError(ErrorCode.LOT_NO_NOT_EXIST.getCode(),
							addnlinfo.getLotNo() + " at serial no." + i);
				}

				if (true == material.getShelfLifeControl() && (isEmpty(addnlinfo.getExpiryDate())
						|| (!isEmpty(addnlinfo.getExpiryDate()) && !(addnlinfo.getExpiryDate().doubleValue() > 0)))) {
					errors.addDataError(ErrorCode.EXP_DATE_NOT_EXIST.getCode(),
							addnlinfo.getExpiryDate() + " at serial no." + i);
					if (true == (material.getSerialNumber() == null ? false : material.getSerialNumber())
							&& isEmpty(addnlinfo.getSerialNo())) {
						errors.addDataError(ErrorCode.MANDATORY_VALUE_MISSINGROW.getCode(), "Serial number ",
								String.valueOf(i));
					}
				}

			}
		} else
			errors.addDataError(ErrorCode.MANDATORY_VALUE_MISSINGROW.getCode(), "Material ", String.valueOf(i));

	}

	private void validateDetailsAddnInfo(List<MaterialReceiptDetailAddnlinfo> materialReceiptDetailAddnlinfos,
			BigDecimal acceptedQuantity, String tenantId, int i, InvalidDataException errors) {
		BigDecimal totalQuantity = BigDecimal.ZERO;
		for (MaterialReceiptDetailAddnlinfo addnlinfo : materialReceiptDetailAddnlinfos) {

			if (null != addnlinfo.getQuantity()) {
				totalQuantity = totalQuantity.add(addnlinfo.getQuantity());
			}

			if (null != addnlinfo.getExpiryDate() && addnlinfo.getExpiryDate() <= getCurrentDate()) {

				String date = convertEpochtoDate(addnlinfo.getExpiryDate());
				errors.addDataError(ErrorCode.DATE_GE_CURRENTDATE.getCode(), "Expiry date ", date);

			}
		}

		if (totalQuantity.compareTo(acceptedQuantity) != 0) {
			errors.addDataError(ErrorCode.FIELD_DOESNT_MATCH.getCode(), "Accepted Quantity",
					"Sum of quantity of additional details");
		}
	}

	private void validateDuplicateMaterialDetails(List<MaterialReceiptDetail> materialReceiptDetails,
			InvalidDataException errors) {
		HashSet<String> hashSet = new HashSet<>();
		int i = 0;
		for (MaterialReceiptDetail materialReceiptDetail : materialReceiptDetails) {
			i++;
			if (false == hashSet.add(materialReceiptDetail.getPurchaseOrderDetail().getId() + "-"
					+ materialReceiptDetail.getMaterial().getCode())) {
				errors.addDataError(ErrorCode.COMBINATION_EXISTS_ROW.getCode(), "Purchase order",
						materialReceiptDetail.getPurchaseOrderDetail().getId().toString(), "material",
						materialReceiptDetail.getMaterial().getCode().toString(), String.valueOf(i));
			}
		}
	}

	private void validatePurchaseOrder(MaterialReceiptDetail materialReceiptDetail, String store, Long receiptDate,
			String supplier, String tenantId, int i, InvalidDataException errors) {

		if (null != materialReceiptDetail.getPurchaseOrderDetail()) {
			PurchaseOrderDetailSearch purchaseOrderDetailSearch = new PurchaseOrderDetailSearch();
			purchaseOrderDetailSearch.setTenantId(tenantId);
			purchaseOrderDetailSearch
					.setIds(Collections.singletonList(materialReceiptDetail.getPurchaseOrderDetail().getId()));

			Pagination<PurchaseOrderDetail> purchaseOrderDetails = purchaseOrderDetailService
					.search(purchaseOrderDetailSearch);

			if (purchaseOrderDetails.getPagedData().size() > 0) {
				for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrderDetails.getPagedData()) {
					LOG.info("materialReceiptDetail.getReceivedQty()" + materialReceiptDetail.getReceivedQty());
					LOG.info("materialReceiptDetail.getUserReceivedQty()" + materialReceiptDetail.getUserReceivedQty());
					LOG.info("purchaseOrderDetail.getOrderQuantity()" + purchaseOrderDetail.getOrderQuantity());
					LOG.info("purchaseOrderDetail.getOrderQuantity()" + purchaseOrderDetail.getUserQuantity());

					LOG.info("materialReceiptDetail.getReceivedQty()" + materialReceiptDetail.getUom().getCode()

							+ " conversion" + materialReceiptDetail.getUom().getConversionFactor());

					LOG.info("materialReceiptDetail.getReceivedQty().compareTo(purchaseOrderDetail.getOrderQuantity())"
							+ materialReceiptDetail.getReceivedQty().compareTo(purchaseOrderDetail.getOrderQuantity()));

					if (materialReceiptDetail.getReceivedQty().compareTo(purchaseOrderDetail.getOrderQuantity()) > 0) {
						errors.addDataError(ErrorCode.RCVED_QTY_LS_ODRQTY.getCode(), String.valueOf(i));
					}

					BigDecimal remainingQuantity = purchaseOrderDetail.getOrderQuantity()
							.subtract(purchaseOrderDetail.getReceivedQuantity());
					BigDecimal conversionFactor = materialReceiptDetail.getUom().getConversionFactor();
					BigDecimal convertedRemainingQuantity = getSearchConvertedQuantity(remainingQuantity,
							conversionFactor);
					if (null != purchaseOrderDetail.getReceivedQuantity()
							&& materialReceiptDetail.getReceivedQty().compareTo(remainingQuantity) > 0) {
						errors.addDataError(ErrorCode.RCVED_QTY_LS_PORCVEDATY.getCode(),
								materialReceiptDetail.getUserReceivedQty().toString(),
								convertedRemainingQuantity.toString(), String.valueOf(i));
					}

					PurchaseOrderEntity po = new PurchaseOrderEntity();
					po.setPurchaseOrderNumber(purchaseOrderDetail.getPurchaseOrderNumber());
					po.setTenantId(tenantId);

					PurchaseOrderEntity poe = (PurchaseOrderEntity) purchaseOrderJdbcRepository.findById(po,
							"PurchaseOrderEntity");

					if (poe.getPurchaseOrderDate() > receiptDate) {
						errors.addDataError(ErrorCode.DATE1_GT_DATE2ROW.getCode(), "Receipt Date ",
								"purchase order date ", String.valueOf(i));
					}

					if (!isEmpty(supplier) && !supplier.equalsIgnoreCase(poe.getSupplier())) {

						errors.addDataError(ErrorCode.MATCH_TWO_FIELDS.getCode(), "Supplier ",
								"purchase order supplier ", String.valueOf(i));
					}

					if (!isEmpty(store) && !store.equalsIgnoreCase(poe.getStore())) {

						errors.addDataError(ErrorCode.MATCH_TWO_FIELDS.getCode(), "Store ", "purchase order Store ",
								String.valueOf(i));
					}

					PurchaseOrderSearch purchaseOrderSearch = new PurchaseOrderSearch();
					purchaseOrderSearch.setPurchaseOrderNumber(purchaseOrderDetail.getPurchaseOrderNumber());
					purchaseOrderSearch.setTenantId(tenantId);
					if (purchaseOrderService.checkAllItemsSuppliedForPo(purchaseOrderSearch)) {
						errors.addDataError(ErrorCode.PO_SUPPLIED.getCode(),
								purchaseOrderDetail.getPurchaseOrderNumber());
					}
				}
			}
		}
	}

	private String appendString(MaterialReceipt materialReceipt) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		String code = "MRN-";
		int id = Integer.valueOf(receiptNoteRepository.getSequence(materialReceipt));
		String idgen = String.format("%05d", id);
		String mrnNumber = code + idgen + "-" + year;
		return mrnNumber;
	}

	private String convertEpochtoDate(Long date) {
		Date epoch = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String s2 = format.format(epoch);
		return s2;
	}

	private MaterialReceiptRequest fetchRelated(MaterialReceiptRequest materialReceiptRequest, String tenantId) {
		List<MaterialReceipt> materialReceipts = materialReceiptRequest.getMaterialReceipt();

		for (MaterialReceipt materialReceipt : materialReceipts) {
			if (StringUtils.isEmpty(materialReceipt.getTenantId())) {
				materialReceipt.setTenantId(tenantId);
			}

			for (MaterialReceiptDetail materialReceiptDetail : materialReceipt.getReceiptDetails()) {
				setMaterialDetails(tenantId, materialReceiptDetail);
				setPoDetails(materialReceiptDetail);
			}
		}

		return materialReceiptRequest;
	}

	private void setPoDetails(MaterialReceiptDetail materialReceiptDetail) {
		PurchaseOrderDetailSearch purchaseOrderDetailSearch = new PurchaseOrderDetailSearch();
		purchaseOrderDetailSearch.setTenantId(materialReceiptDetail.getTenantId());
		purchaseOrderDetailSearch
				.setIds(Collections.singletonList(materialReceiptDetail.getPurchaseOrderDetail().getId()));

		Pagination<PurchaseOrderDetail> purchaseOrderDetails = purchaseOrderDetailService
				.search(purchaseOrderDetailSearch);

		if (purchaseOrderDetails.getPagedData().size() == 0) {
			throw new CustomException("Purchase Order Detail", "Purchase order not found");
		} else {
			materialReceiptDetail.setPurchaseOrderDetail(purchaseOrderDetails.getPagedData().get(0));
		}
	}

	public PDFResponse printPdf(MaterialReceiptSearch materialReceiptSearch, RequestInfo requestInfo) {
		MaterialReceiptResponse materialReceiptResponse = search(materialReceiptSearch);
		if (!materialReceiptResponse.getMaterialReceipt().isEmpty()
				&& materialReceiptResponse.getMaterialReceipt().size() == 1) {
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

			JSONArray materials = new JSONArray();
			for (MaterialReceipt in : materialReceiptResponse.getMaterialReceipt()) {
				JSONObject material = new JSONObject();
				Instant receiptDate = Instant.ofEpochMilli(in.getReceiptDate());
				material.put("mrnNumber", in.getMrnNumber());
				material.put("storeName", in.getReceivingStore().getName());
				material.put("receiptDate", fmt.format(receiptDate.atZone(ZoneId.systemDefault())));
				material.put("mrnStatus", in.getMrnStatus());
				material.put("receiptType", in.getReceiptType());
				material.put("supplierName", in.getSupplier().getName());
				material.put("supplierBillNo", in.getSupplierBillNo());
				if (in.getSupplierBillDate() != null) {
					Instant supplierBillDate = Instant.ofEpochMilli(in.getReceiptDate());
					material.put("supplierBillDate", fmt.format(supplierBillDate.atZone(ZoneId.systemDefault())));
				} else {
					material.put("supplierBillDate", in.getSupplierBillDate());
				}
				material.put("challanNo", in.getChallanNo());
				if (in.getSupplierBillDate() != null) {
					Instant supplierBillDate = Instant.ofEpochMilli(in.getChallanDate());
					material.put("challanDate", fmt.format(supplierBillDate.atZone(ZoneId.systemDefault())));
				} else {
					material.put("challanDate", in.getChallanDate());
				}

				material.put("remarks", in.getDescription());
				material.put("receivedBy", in.getReceivedBy());
				material.put("designation", in.getDesignation());
				material.put("inspectedBy", in.getInspectedBy());
				if (in.getInspectionDate() != null) {
					Instant inspectedDate = Instant.ofEpochMilli(in.getInspectionDate());
					material.put("inspectedDate", fmt.format(inspectedDate.atZone(ZoneId.systemDefault())));
				} else {
					material.put("inspectedDate", in.getInspectionDate());
				}
				material.put("inspectedRemarks", in.getInspectionRemarks());

				JSONArray matsDetails = new JSONArray();
				int i = 1;
				for (MaterialReceiptDetail detail : in.getReceiptDetails()) {
					JSONObject matsDetail = new JSONObject();
					matsDetail.put("srNo", i++);
					matsDetail.put("materialName", detail.getMaterial().getName());
					matsDetail.put("poNumber", detail.getPurchaseOrderDetail().getPurchaseOrderNumber());
					matsDetail.put("uomName", detail.getUom().getName());
					matsDetail.put("acceptedQty", detail.getAcceptedQty());
					matsDetail.put("orderedQty", detail.getPurchaseOrderDetail().getOrderQuantity());
					matsDetail.put("receivedQty", detail.getReceivedQty());
					matsDetail.put("totalValueOfAcceptedQty", detail.getAcceptedQty().multiply(detail.getUnitRate()));
					matsDetail.put("ratePerUnit", detail.getUnitRate());
					matsDetail.put("lotNo", detail.getReceiptDetailsAddnInfo().get(0).getLotNo());
					if (detail.getReceiptDetailsAddnInfo().get(0).getManufactureDate() != null) {
						Instant mfgDate = Instant
								.ofEpochMilli(detail.getReceiptDetailsAddnInfo().get(0).getManufactureDate());
						matsDetail.put("mfgDate", fmt.format(mfgDate.atZone(ZoneId.systemDefault())));
					} else {
						matsDetail.put("mfgDate", detail.getReceiptDetailsAddnInfo().get(0).getManufactureDate());
					}
					matsDetails.add(matsDetail);
				}
				material.put("materialDetails", matsDetails);

				// Need to integrate Workflow

				ProcessInstanceResponse workflowData = workflowIntegrator.getWorkflowDataByID(requestInfo,
						in.getMrnNumber(), in.getTenantId());
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
				material.put("workflowDetails", workflows);
				materials.add(material);
				requestMain.put("MeterialReceiptNote", materials);
			}

			return pdfServiceReposistory.getPrint(requestMain, "store-asset-mrn-receipt",
					materialReceiptSearch.getTenantId());
		}
		return PDFResponse.builder()
				.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();

	}

	@Transactional
	public MaterialReceiptResponse updateStatus(MaterialReceiptRequest materialReceiptRequest, String tenantId) {

		try {
			WorkFlowDetails workFlowDetails = workflowIntegrator.callWorkFlow(materialReceiptRequest.getRequestInfo(),
					materialReceiptRequest.getWorkFlowDetails(),
					materialReceiptRequest.getWorkFlowDetails().getTenantId());
			materialReceiptRequest.setWorkFlowDetails(workFlowDetails);
			kafkaQue.send(updateStatusTopic, updateStatusTopicKey, materialReceiptRequest);
			MaterialReceiptResponse materialReceiptResponse = new MaterialReceiptResponse();

			if (materialReceiptRequest.getWorkFlowDetails().getStatus().equals(MrnStatusEnum.APPROVED.toString())) {
				MaterialReceiptSearch materialReceiptSearch = new MaterialReceiptSearch();
				materialReceiptSearch
						.setMrnNumber(Arrays.asList(materialReceiptRequest.getWorkFlowDetails().getBusinessId()));
				materialReceiptSearch.setTenantId(materialReceiptRequest.getWorkFlowDetails().getTenantId());
				MaterialReceiptResponse receiptResponse = search(materialReceiptSearch);
				for (MaterialReceipt materialReceipt : receiptResponse.getMaterialReceipt()) {
					// generate PO
					if (materialReceipt.getReceiptType() != null && materialReceipt.getReceiptType().toString()
							.equals(ReceiptTypeEnum.NON_PURCHASE_RECEIPT.toString())) {
						CreateNonPurchaseOrder(materialReceipt);
					}
				}
			}

			return materialReceiptResponse.responseInfo(null)
					.materialReceipt(materialReceiptRequest.getMaterialReceipt());
		} catch (CustomBindException e) {
			throw e;
		}
	}

	public PDFResponse printInventoryReportPdf(MaterialReceiptSearch materialReceiptSearch, RequestInfo requestInfo) {
		JSONArray jsonArray = materialReceiptService.getInventoryReport(materialReceiptSearch);
		JSONArray arrayPrintData = new JSONArray();
		if (!jsonArray.isEmpty()) {
			JSONObject requestMain = new JSONObject();

			StoreGetRequest storeGetRequest = new StoreGetRequest();
			storeGetRequest.setCode(Arrays.asList(materialReceiptSearch.getReceivingStore()));
			storeGetRequest.setTenantId(materialReceiptSearch.getTenantId());
			StoreResponse store = storeService.search(storeGetRequest);

			Material material = materialService.fetchMaterial(materialReceiptSearch.getTenantId(),
					materialReceiptSearch.getMaterials().get(0), new RequestInfo());
			requestMain.put("storeName", store.getStores().isEmpty() ? materialReceiptSearch.getReceivingStore()
					: store.getStores().get(0).getName());
			requestMain.put("storeDepartment",
					store.getStores().isEmpty() ? "" : store.getStores().get(0).getDepartment().getName());
			requestMain.put("materialName", material.getName());
			requestMain.put("invetoryDetails", jsonArray);
			arrayPrintData.add(requestMain);
		}

		if (!arrayPrintData.isEmpty() && materialReceiptSearch.isForprint()) {
			JSONObject finalDta = new JSONObject();

			ObjectMapper mapper = new ObjectMapper();
			try {
				JSONObject reqInfo = (JSONObject) new JSONParser().parse(mapper.writeValueAsString(requestInfo));
				finalDta.put("RequestInfo", reqInfo);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			finalDta.put("InventoryReport", arrayPrintData);

			return pdfServiceReposistory.getPrint(finalDta, "store-asset-report-inventory-register",
					materialReceiptSearch.getTenantId());
		} else if (!arrayPrintData.isEmpty() && !materialReceiptSearch.isForprint()) {
			return PDFResponse.builder().responseInfo(ResponseInfo.builder().status("Success").build())
					.printData(arrayPrintData).build();
		} else {
			return PDFResponse.builder()
					.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();
		}
	}

	private void CreateNonPurchaseOrder(MaterialReceipt materialReceipt) {
		PurchaseOrder purchaseOrder = new PurchaseOrder();
		List<String> sequenceNos = purchaseOrderRepository.getSequence(PurchaseOrder.class.getSimpleName(), 1);
		if (!sequenceNos.isEmpty()) {
			purchaseOrder.setId(sequenceNos.get(0));
			purchaseOrder.setPurchaseOrderNumber(materialReceipt.getExternalPoNumber());
			purchaseOrder.setPurchaseOrderDate(Instant.EPOCH.getEpochSecond());
			purchaseOrder.setDesignation(materialReceipt.getDesignation());
			purchaseOrder.setTenantId(materialReceipt.getTenantId());
			purchaseOrder.setStore(Store.builder().code(materialReceipt.getReceivingStore().getCode()).build());
			purchaseOrder.setPurchaseType(PurchaseTypeEnum.NON_INDENT);
			purchaseOrder.setRateType(org.egov.assets.model.PurchaseOrder.RateTypeEnum.GEM);
			Supplier supplier = new Supplier();
			supplier.setCode(materialReceipt.getSupplier().getCode());
			purchaseOrder.setSupplier(supplier);
			purchaseOrder.setPoCreatedBy(materialReceipt.getReceivedBy());
			purchaseOrder.setStatus(StatusEnum.APPROVED);
			purchaseOrder.setRemarks(materialReceipt.getDescription());
			purchaseOrder.setAuditDetails(materialReceipt.getAuditDetails());

			List<PurchaseOrderDetail> purchaseOrderDetails = new ArrayList<>();
			List<String> detailSequenceNos = purchaseOrderRepository
					.getSequence(PurchaseOrderDetail.class.getSimpleName(), materialReceipt.getReceiptDetails().size());
			int i = 0;
			BigDecimal totalAmount = BigDecimal.ZERO;
			for (MaterialReceiptDetail detailReceipt : materialReceipt.getReceiptDetails()) {
				PurchaseOrderDetail purchaseOrderDetail = new PurchaseOrderDetail();
				purchaseOrderDetail.setId(detailSequenceNos.get(i));
				purchaseOrderDetail.setMaterial(detailReceipt.getMaterial());
				purchaseOrderDetail.setTenantId(materialReceipt.getTenantId());
				purchaseOrderDetail.setUom(detailReceipt.getUom());
				purchaseOrderDetail.setOrderQuantity(detailReceipt.getOrderQuantity());
				purchaseOrderDetail.setUserQuantity(detailReceipt.getOrderQuantity());
				purchaseOrderDetail.setUnitPrice(detailReceipt.getUnitRate());
				purchaseOrderDetail.setReceivedQuantity(detailReceipt.getReceivedQty());

				PurchaseOrderDetail detail = new PurchaseOrderDetail();
				detail.setId(detailSequenceNos.get(i));
				detailReceipt.setPurchaseOrderDetail(detail);

				purchaseOrderDetails.add(purchaseOrderDetail);

				totalAmount = totalAmount.add(detailReceipt.getOrderQuantity().multiply(detailReceipt.getUnitRate()));
				i++;
			}

			purchaseOrder.setTotalAmount(totalAmount);
			purchaseOrder.setPurchaseOrderDetails(purchaseOrderDetails);

			// 1. Add into PO
			// 2. Update PO Id against materials
			kafkaQue.send(saveNonIndentTopic, saveNonIndentKey, purchaseOrder);
			materialReceiptService.updatePOdetailIdAgainstMaterials(materialReceipt.getReceiptDetails(),
					materialReceipt.getTenantId());
		}
	}
}