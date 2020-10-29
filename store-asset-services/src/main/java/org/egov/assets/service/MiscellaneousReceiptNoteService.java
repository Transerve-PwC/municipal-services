package org.egov.assets.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptRequest;
import org.egov.assets.model.MaterialReceiptResponse;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.Scrap;
import org.egov.assets.model.Scrap.ScrapStatusEnum;
import org.egov.assets.model.ScrapDetail;
import org.egov.assets.model.ScrapRequest;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.ReceiptNoteRepository;
import org.egov.assets.repository.entity.MaterialIssueEntity;
import org.egov.assets.repository.entity.MaterialReceiptEntity;
import org.egov.assets.wf.WorkflowIntegrator;
import org.egov.assets.wf.model.Action;
import org.egov.assets.wf.model.ProcessInstance;
import org.egov.assets.wf.model.ProcessInstanceResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MiscellaneousReceiptNoteService extends DomainService {

	@Autowired
	private LogAwareKafkaTemplate<String, Object> logAwareKafkaTemplate;

	@Value("${inv.miscellaneousreceiptnote.save.topic}")
	private String saveTopic;

	@Value("${inv.miscellaneousreceiptnote.save.key}")
	private String saveTopicKey;

	@Value("${inv.miscellaneousreceiptnote.updatestatus.topic}")
	private String updatestatusTopic;

	@Value("${inv.miscellaneousreceiptnote.updatestatus.key}")
	private String updatestatusTopicKey;

	@Autowired
	private MaterialReceiptService materialReceiptService;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private ReceiptNoteRepository receiptNoteRepository;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private MdmsRepository mdmsRepository;

	public MaterialReceiptResponse create(MaterialReceiptRequest materialReceiptRequest, String tenantId) {
		List<MaterialReceipt> materialReceipts = materialReceiptRequest.getMaterialReceipt();
		MaterialReceiptRequest fetchRelated = fetchRelated(materialReceiptRequest, tenantId);
		validate(fetchRelated.getMaterialReceipt(), tenantId, Constants.ACTION_CREATE);
		materialReceipts.forEach(materialReceipt -> {
			materialReceipt.setId(receiptNoteRepository.getSequence("seq_materialreceipt"));
			materialReceipt.setMrnNumber(appendString(materialReceipt));
			materialReceipt.setMrnStatus(MaterialReceipt.MrnStatusEnum.CREATED);
			materialReceipt.setReceiptType(MaterialReceipt.ReceiptTypeEnum.MISCELLANEOUS_RECEIPT);
			materialReceipt.setAuditDetails(getAuditDetails(materialReceiptRequest.getRequestInfo(), Constants.ACTION_CREATE));
			if (StringUtils.isEmpty(materialReceipt.getTenantId())) {
				materialReceipt.setTenantId(tenantId);
			}

			materialReceipt.getReceiptDetails().forEach(materialReceiptDetail -> {
				setMaterialDetails(tenantId, materialReceiptDetail);
			});
			WorkFlowDetails workFlowDetails = materialReceiptRequest.getWorkFlowDetails();
			workFlowDetails.setBusinessId(materialReceipt.getMrnNumber());
			workflowIntegrator.callWorkFlow(materialReceiptRequest.getRequestInfo(), workFlowDetails, tenantId);
		});

		logAwareKafkaTemplate.send(saveTopic, saveTopicKey, materialReceiptRequest);

		MaterialReceiptResponse materialReceiptResponse = new MaterialReceiptResponse();

		return materialReceiptResponse.responseInfo(null).materialReceipt(materialReceipts);
	}

	public MaterialReceiptResponse update(MaterialReceiptRequest materialReceiptRequest, String tenantId) {
		List<MaterialReceipt> materialReceipts = materialReceiptRequest.getMaterialReceipt();
		MaterialReceiptRequest fetchRelated = fetchRelated(materialReceiptRequest, tenantId);
		validate(fetchRelated.getMaterialReceipt(), tenantId, Constants.ACTION_UPDATE);
		List<String> materialReceiptDetailIds = new ArrayList<>();
		List<String> materialReceiptDetailAddlnInfoIds = new ArrayList<>();
		materialReceipts.forEach(materialReceipt -> {
			materialReceipt.setReceiptType(MaterialReceipt.ReceiptTypeEnum.MISCELLANEOUS_RECEIPT);

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
		});

		logAwareKafkaTemplate.send(saveTopic, saveTopicKey, materialReceiptRequest);

		MaterialReceiptResponse materialReceiptResponse = new MaterialReceiptResponse();

		return materialReceiptResponse.responseInfo(null).materialReceipt(materialReceipts);
	}

	public MaterialReceiptResponse search(MaterialReceiptSearch materialReceiptSearch) {
		Pagination<MaterialReceipt> materialReceiptPagination = materialReceiptService.search(materialReceiptSearch);
		MaterialReceiptResponse response = new MaterialReceiptResponse();
		return response.responseInfo(null).materialReceipt(
				materialReceiptPagination.getPagedData().size() > 0 ? materialReceiptPagination.getPagedData()
						: Collections.EMPTY_LIST);
	}

	private void setMaterialDetails(String tenantId, MaterialReceiptDetail materialReceiptDetail) {
		materialReceiptDetail.setId(receiptNoteRepository.getSequence("seq_materialreceiptdetail"));
		if (isEmpty(materialReceiptDetail.getTenantId())) {
			materialReceiptDetail.setTenantId(tenantId);
		}

		materialReceiptDetail.getReceiptDetailsAddnInfo().forEach(materialReceiptDetailAddnlInfo -> {
			materialReceiptDetailAddnlInfo
					.setId(receiptNoteRepository.getSequence("seq_materialreceiptdetailaddnlinfo"));
			if (isEmpty(materialReceiptDetailAddnlInfo.getTenantId())) {
				materialReceiptDetailAddnlInfo.setTenantId(tenantId);
			}
		});
	}

	private void validate(List<MaterialReceipt> materialReceipts, String tenantId, String method) {
		InvalidDataException errors = new InvalidDataException();

		try {
			switch (method) {

			case Constants.ACTION_CREATE: {
				if (materialReceipts == null) {
					throw new InvalidDataException("materialreceipt", ErrorCode.NOT_NULL.getCode(), null);
				}
			}

				break;

			case Constants.ACTION_UPDATE: {
				if (materialReceipts == null) {
					throw new InvalidDataException("materialreceipt", ErrorCode.NOT_NULL.getCode(), null);
				}
			}

				break;
			}
			for (MaterialReceipt materialReceipt : materialReceipts) {
				if (!isEmpty(materialReceipt.getReceiptPurpose())) {
					validateReceiptPurpose(materialReceipt.getMrnNumber(),
							materialReceipt.getReceiptPurpose().toString(), tenantId, errors);
				}
				if (!isEmpty(materialReceipt.getIssueNumber())) {
					validateIssue(tenantId, errors, materialReceipt.getIssueNumber());
				}
				validateReceiptdate(errors, materialReceipt);
				for (MaterialReceiptDetail materialReceiptDetail : materialReceipt.getReceiptDetails()) {
					int j = 0;
					validateNonIssueQuantity(materialReceiptDetail, errors, j);
					validateReceivedQuantity(errors, materialReceiptDetail, j);
					j++;
				}
			}
		} catch (IllegalArgumentException e) {
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;
	}

	private void validateReceiptdate(InvalidDataException errors, MaterialReceipt materialReceipt) {
		if (null != materialReceipt.getReceiptDate() && materialReceipt.getReceiptDate() > getCurrentDate()) {
			String date = convertEpochtoDate(materialReceipt.getReceiptDate());
			errors.addDataError(ErrorCode.DATE_LE_CURRENTDATE.getCode(), "Receipt date ", date);
		}
	}

	private void validateReceivedQuantity(InvalidDataException errors, MaterialReceiptDetail materialReceiptDetail,
			int j) {
		if (materialReceiptDetail.getReceivedQty().compareTo(BigDecimal.ZERO) <= 0) {
			errors.addDataError(ErrorCode.RCVED_QTY_GT_ZERO.getCode(), String.valueOf(j));
		}
	}

	private void validateIssue(String tenantId, InvalidDataException errors, String issueNumber) {
		MaterialIssueEntity materialIssueEntity = new MaterialIssueEntity();
		materialIssueEntity.setIssueNumber(issueNumber);
		materialIssueEntity.setTenantId(tenantId);
		Object materialIssue = receiptNoteRepository.findById(materialIssueEntity, "MaterialIssueEntity");
		if (null == materialIssue) {
			errors.addDataError(ErrorCode.OBJECT_NOT_FOUND.getCode(), "Issue", "", issueNumber);
		}
	}

	private void validateNonIssueQuantity(MaterialReceiptDetail materialReceiptDetail, InvalidDataException errors,
			int row) {
		if (materialReceiptDetail.getAcceptedQty().compareTo(materialReceiptDetail.getReceivedQty()) == -1) {
			errors.addDataError(ErrorCode.QTY_LE_SCND_ROW.getCode(), "Issued Quantity", "Received Quantity",
					String.valueOf(row));
		}
	}

	private void validateReceiptPurpose(String mrnNumber, String receiptPurpose, String tenantId,
			InvalidDataException errors) {
		if (!isEmpty(mrnNumber)) {
			MaterialReceiptEntity materialReceiptEntity = new MaterialReceiptEntity();
			materialReceiptEntity.setMrnNumber(mrnNumber);
			materialReceiptEntity.setTenantId(tenantId);
			Object receiptEntity = receiptNoteRepository.findById(materialReceiptEntity, "MaterialReceiptEntity");
			MaterialReceiptEntity receiptEntityfromDb = (MaterialReceiptEntity) receiptEntity;
			if (null != receiptEntityfromDb) {
				if (!(receiptPurpose.toString().equalsIgnoreCase(receiptEntityfromDb.getReceiptPurpose().toString()))) {
					errors.addDataError(ErrorCode.DOESNT_MATCH.getCode(), receiptPurpose, "receipt purpose",
							receiptEntityfromDb.getReceiptPurpose().toString());
				}
			}
		}
	}

	private String appendString(MaterialReceipt materialReceipt) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		String code = "MMRN-";
		int id = Integer.valueOf(receiptNoteRepository.getSequence(materialReceipt));
		String idgen = String.format("%05d", id);
		String mrnNumber = code + idgen + "-" + year;
		return mrnNumber;
	}

	private MaterialReceiptRequest fetchRelated(MaterialReceiptRequest materialReceiptRequest, String tenantId) {

		List<MaterialReceipt> materialReceipts = materialReceiptRequest.getMaterialReceipt();

		for (MaterialReceipt materialReceipt : materialReceipts) {
			for (MaterialReceiptDetail materialReceiptDetail : materialReceipt.getReceiptDetails()) {

				Material material = (Material) mdmsRepository.fetchObject(tenantId, "store-asset", "Material", "code",
						materialReceiptDetail.getMaterial().getCode(), Material.class,
						materialReceiptRequest.getRequestInfo());
				materialReceiptDetail.setMaterial(material);

				Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code",
						materialReceiptDetail.getUom().getCode(), Uom.class, materialReceiptRequest.getRequestInfo());
				materialReceiptDetail.setUom(uom);

				if (null != materialReceiptDetail.getAcceptedQty() && null != uom.getConversionFactor()) {
					BigDecimal convertedAcceptedQuantity = getSaveConvertedQuantity(
							materialReceiptDetail.getAcceptedQty(), uom.getConversionFactor());
					materialReceiptDetail.setAcceptedQty(convertedAcceptedQuantity);
				}

				if (null != materialReceiptDetail.getReceivedQty() && null != uom.getConversionFactor()) {
					BigDecimal convertedReceivedQuantity = getSaveConvertedQuantity(
							materialReceiptDetail.getReceivedQty(), uom.getConversionFactor());
					materialReceiptDetail.setReceivedQty(convertedReceivedQuantity);
				}

				if (null != materialReceiptDetail.getUnitRate() && null != uom.getConversionFactor()) {
					BigDecimal convertedRate = getSaveConvertedRate(materialReceiptDetail.getUnitRate(),
							uom.getConversionFactor());
					materialReceiptDetail.setUnitRate(convertedRate);
				}
			}
		}

		return materialReceiptRequest;
	}

	private Long getCurrentDate() {
		return currentEpochWithoutTime() + (24 * 60 * 60 * 1000) - 1;
	}

	private String convertEpochtoDate(Long date) {
		Date epoch = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String s2 = format.format(epoch);
		return s2;
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
				material.put("receiptNo", in.getMrnNumber());
				material.put("storeName", in.getReceivingStore().getName());
				material.put("receiptType", in.getReceiptType());
				material.put("status", in.getMrnStatus());
				material.put("department", in.getReceivingStore().getDepartment().getName());
				if (in.getReceiptDate() != null) {
					Instant receiptDate = Instant.ofEpochMilli(in.getReceiptDate());
					material.put("receiptDate", fmt.format(receiptDate.atZone(ZoneId.systemDefault())));
				} else {
					material.put("receiptDate", in.getSupplierBillDate());
				}
				material.put("remarks", in.getDescription());
				material.put("receivedBy", in.getReceivedBy());
				material.put("designation", in.getDesignation());

				JSONArray matsDetails = new JSONArray();
				int i = 1;
				for (MaterialReceiptDetail detail : in.getReceiptDetails()) {
					JSONObject matsDetail = new JSONObject();
					matsDetail.put("srNo", i++);
					matsDetail.put("materialCode", detail.getMaterial().getCode());
					matsDetail.put("materialName", detail.getMaterial().getName());
					matsDetail.put("uomName", detail.getUom().getName());
					matsDetail.put("quantityReceived", detail.getReceivedQty());
					matsDetail.put("unitRate", detail.getUnitRate());
					matsDetail.put("totalValue", detail.getReceivedQty().multiply(detail.getUnitRate()));
					matsDetails.add(matsDetail);
				}
				material.put("materialDetails", matsDetails);

				// Need to integrate Workflow

				ProcessInstanceResponse workflowData = workflowIntegrator.getWorkflowDataByID(requestInfo,
						in.getMrnNumber(), in.getTenantId());
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
				requestMain.put("MiscellaneousMaterialReceiptNote", materials);
			}

			return pdfServiceReposistory.getPrint(requestMain, "store-asset-misc-material-note",
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
			kafkaQue.send(updatestatusTopic, updatestatusTopicKey, materialReceiptRequest);
			MaterialReceiptResponse materialReceiptResponse = new MaterialReceiptResponse();
			return materialReceiptResponse.responseInfo(null)
					.materialReceipt(materialReceiptRequest.getMaterialReceipt());
		} catch (CustomBindException e) {
			throw e;
		}
	}

}
