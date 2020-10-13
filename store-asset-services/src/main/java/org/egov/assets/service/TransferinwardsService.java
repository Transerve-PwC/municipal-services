package org.egov.assets.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.FinancialYear;
import org.egov.assets.model.IndentDetail;
import org.egov.assets.model.MaterialIssue;
import org.egov.assets.model.MaterialIssue.IssueTypeEnum;
import org.egov.assets.model.MaterialIssue.MaterialIssueStatusEnum;
import org.egov.assets.model.MaterialIssueDetail;
import org.egov.assets.model.MaterialIssueRequest;
import org.egov.assets.model.MaterialIssueResponse;
import org.egov.assets.model.MaterialIssueSearchContract;
import org.egov.assets.model.MaterialIssuedFromReceipt;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceipt.ReceiptTypeEnum;
import org.egov.assets.model.MaterialReceiptAddInfoSearch;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptDetailAddnlinfo;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.Tenant;
import org.egov.assets.model.TransferInwardRequest;
import org.egov.assets.model.TransferInwardResponse;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.MaterialIssueJdbcRepository;
import org.egov.assets.repository.MaterialReceiptDetailAddInfoJdbcRepository;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.TransferInwardRepository;
import org.egov.assets.wf.WorkflowIntegrator;
import org.egov.assets.wf.model.Action;
import org.egov.assets.wf.model.ProcessInstance;
import org.egov.assets.wf.model.ProcessInstanceResponse;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class TransferinwardsService extends DomainService {

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private TransferInwardRepository transferInwardRepository;

	@Autowired
	private MaterialIssueService materialIssuesService;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private MaterialReceiptService materialReceiptService;

	@Autowired
	private NumberGenerator numberGenerator;

	@Autowired
	private MaterialIssueJdbcRepository materialIssueJdbcRepository;

	@Autowired
	private MdmsRepository mdmsRepository;
	@Autowired
	private MaterialReceiptDetailAddInfoJdbcRepository materialReceiptDetailAddInfoJdbcRepository;

	@Value("${inv.transfer.inward.save.topic}")
	private String createTopic;

	@Value("${inv.transfer.inward.update.topic}")
	private String updateTopic;

	@Value("${inv.transfer.inward.save.key}")
	private String createTopicKey;

	@Value("${inv.transfer.inward.update.key}")
	private String updateTopicKey;

	@Value("${inv.transfer.inward.updatestatus.topic}")
	private String updateStatusTopic;

	@Value("${inv.transfer.inward.updatestatus.key}")
	private String updateStatusTopicKey;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	// creating inter-store transfer inwards
	public TransferInwardResponse create(TransferInwardRequest inwardRequest, String tenantId) {
		try {
			fetchRelated(inwardRequest, tenantId);
			List<MaterialReceipt> inwards = inwardRequest.getTransferInwards();
			validate(inwards, tenantId, Constants.ACTION_CREATE);
			inwards.forEach(materialReceipt -> {
				materialReceipt.setId(transferInwardRepository.getSequence("seq_materialreceipt"));
				materialReceipt.setMrnStatus(MaterialReceipt.MrnStatusEnum.CREATED);
				materialReceipt
						.setMrnNumber(getInwardNumber(materialReceipt, inwardRequest.getRequestInfo(), tenantId));
				materialReceipt.setReceiptType(ReceiptTypeEnum.INWARD_RECEIPT);
				materialReceipt
						.setAuditDetails(getAuditDetails(inwardRequest.getRequestInfo(), Constants.ACTION_CREATE));
				if (StringUtils.isEmpty(materialReceipt.getTenantId())) {
					materialReceipt.setTenantId(tenantId);
				}
				materialReceipt.getReceiptDetails().forEach(materialReceiptDetail -> {
					materialReceiptDetail
							.setAuditDetails(getAuditDetails(inwardRequest.getRequestInfo(), Constants.ACTION_CREATE));
					materialReceiptDetail.setId(transferInwardRepository.getSequence("seq_materialreceiptdetail"));
					materialReceiptDetail.setTenantId(tenantId);
					materialReceiptDetail.getReceiptDetailsAddnInfo().stream().forEach(addinfo -> {
						addinfo.setId(transferInwardRepository.getSequence("seq_materialreceiptdetailaddnlinfo"));
						if (isEmpty(addinfo.getTenantId())) {
							addinfo.setTenantId(tenantId);
						}
					});
				});
				// Consider for workflow
				// updateStatusAsReceipted(materialReceipt.getIssueNumber(), tenantId);
				WorkFlowDetails workFlowDetails = inwardRequest.getWorkFlowDetails();
				workFlowDetails.setBusinessId(materialReceipt.getMrnNumber());
				workflowIntegrator.callWorkFlow(inwardRequest.getRequestInfo(), workFlowDetails,
						materialReceipt.getTenantId());
			});
			kafkaTemplate.send(createTopic, createTopicKey, inwardRequest);
			TransferInwardResponse response = new TransferInwardResponse();
			response.setResponseInfo(null);
			response.setTransferInwards(inwardRequest.getTransferInwards());
			return response;

		} catch (CustomBindException e) {
			throw e;
		}
	}

	// updating inter-store transfer inwards
	public TransferInwardResponse update(TransferInwardRequest inwardsRequest, String tenantId) {
		try {
			fetchRelated(inwardsRequest, tenantId);
			List<MaterialReceipt> inwards = inwardsRequest.getTransferInwards();
			validate(inwards, tenantId, Constants.ACTION_UPDATE);
			List<String> materialReceiptDetailIds = new ArrayList<>();
			List<String> materialReceiptDetailAddlnInfoIds = new ArrayList<>();
			inwards.forEach(materialReceipt -> {
				if (StringUtils.isEmpty(materialReceipt.getTenantId())) {
					materialReceipt.setTenantId(tenantId);
				}
				materialReceipt
						.setAuditDetails(getAuditDetails(inwardsRequest.getRequestInfo(), Constants.ACTION_UPDATE));

				materialReceipt.getReceiptDetails().forEach(materialReceiptDetail -> {
					materialReceiptDetail
							.setAuditDetails(getAuditDetails(inwardsRequest.getRequestInfo(), Constants.ACTION_UPDATE));
					if (isEmpty(materialReceiptDetail.getTenantId())) {
						materialReceiptDetail.setTenantId(tenantId);
					}

					materialReceiptDetailIds.add(materialReceiptDetail.getId());
					materialReceiptDetail.getReceiptDetailsAddnInfo().forEach(materialReceiptDetailAddnlInfo -> {
						materialReceiptDetailAddlnInfoIds.add(materialReceiptDetailAddnlInfo.getId());
						if (isEmpty(materialReceiptDetailAddnlInfo.getTenantId())) {
							materialReceiptDetailAddnlInfo.setTenantId(tenantId);
						}
					});
					transferInwardRepository.markDeleted(materialReceiptDetailAddlnInfoIds, tenantId,
							"materialreceiptdetailaddnlinfo", "receiptdetailid", materialReceiptDetail.getId());
					transferInwardRepository.markDeleted(materialReceiptDetailIds, tenantId, "materialreceiptdetail",
							"mrnNumber", materialReceipt.getMrnNumber());
				});
				// Consider for workflow
				// updateStatusAsReceipted(materialReceipt.getIssueNumber(), tenantId);
			});
			kafkaTemplate.send(updateTopic, updateTopicKey, inwardsRequest);
			TransferInwardResponse response = new TransferInwardResponse();
			response.setResponseInfo(null);
			response.setTransferInwards(inwardsRequest.getTransferInwards());
			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}

	// search inward data
	public TransferInwardResponse search(MaterialReceiptSearch transferinwardsSearch, String tenantId) {
		Pagination<MaterialReceipt> materialReceiptPagination = materialReceiptService.search(transferinwardsSearch);
		TransferInwardResponse response = new TransferInwardResponse();
		return response.responseInfo(null).transferInwards(
				materialReceiptPagination.getPagedData().size() > 0 ? materialReceiptPagination.getPagedData()
						: Collections.EMPTY_LIST);
	}

	// validating data inward data
	private void validate(List<MaterialReceipt> materialReceipts, String tenantId, String method) {
		InvalidDataException errors = new InvalidDataException();
		try {
			switch (method) {
			case Constants.ACTION_CREATE: {
				if (materialReceipts == null) {
					errors.addDataError("materialreceipt", ErrorCode.NOT_NULL.getCode(), null);
				}
			}
				break;

			case Constants.ACTION_UPDATE: {
				if (materialReceipts == null) {
					errors.addDataError("materialreceipt", ErrorCode.NOT_NULL.getCode(), null);
				}
			}
				break;
			}
			for (MaterialReceipt rcpt : materialReceipts) {
				if (null == rcpt.getIssueingStore() && isEmpty(rcpt.getIssueingStore())) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "Issuing Store", null);
				}
				if (null == rcpt.getReceivingStore() && isEmpty(rcpt.getReceivingStore())) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "Receiving Store", null);
				}
			}

		} catch (IllegalArgumentException e) {
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;

	}

	// generating inward number
	private String getInwardNumber(MaterialReceipt receipt, RequestInfo info, String tenantId) {
		InvalidDataException errors = new InvalidDataException();
		ObjectMapper mapper = new ObjectMapper();
		JSONArray tenantStr = mdmsRepository.getByCriteria(tenantId, "tenant", "tenants", "code", tenantId, info);

		Tenant tenant = mapper.convertValue(tenantStr.get(0), Tenant.class);
		if (tenant == null) {
			errors.addDataError(ErrorCode.CITY_CODE_NOT_AVAILABLE.getCode(), tenantId);
		}
		String finYearRange = "";
		JSONArray finYears = mdmsRepository.getByCriteria(tenantId, "egf-master", "FinancialYear", null, null, info);
		outer: for (int i = 0; i < finYears.size(); i++) {
			FinancialYear fin = mapper.convertValue(finYears.get(i), FinancialYear.class);
			if (getCurrentDate() >= fin.getStartingDate().getTime()
					&& getCurrentDate() <= fin.getEndingDate().getTime()) {
				finYearRange = fin.getFinYearRange();
				break outer;
			}
		}

		if (errors.getValidationErrors().size() > 0) {
			throw errors;
		}

		String seq = "MRNIW-" + tenant.getCity().getCode() + "-" + finYearRange;
		return seq + "-" + numberGenerator.getNextNumber(seq, 5);
	}

	// fetching material issue and building with receipt data
	private void fetchRelated(TransferInwardRequest request, String tenantId) {
		InvalidDataException errors = new InvalidDataException();

		for (MaterialReceipt receipt : request.getTransferInwards()) {
			if (receipt.getIssueNumber() != null && !StringUtils.isEmpty(receipt.getIssueNumber())) {
				MaterialIssueSearchContract issue = new MaterialIssueSearchContract();
				issue.setIssueNoteNumber(receipt.getIssueNumber());
				issue.setTenantId(receipt.getTenantId());
				issue.setMaterialIssueStatus(MaterialIssueStatusEnum.APPROVED.toString());
				// searching material issue on basis of issue note number
				List<MaterialIssue> matIssues = materialIssuesService.search(issue, null).getMaterialIssues();
				if (matIssues.isEmpty()) {
					errors.addDataError(ErrorCode.NO_DATA_FOUND.getCode(), "issueNumber " + receipt.getIssueNumber(),
							null);
				} else {
					Long issueDate = matIssues.get(0).getIssueDate();
					BigDecimal issuedQuantity = matIssues.get(0).getMaterialIssueDetails().get(0).getQuantityIssued();
					receipt.setIssueNumber(matIssues.get(0).getIssueNumber());

					for (MaterialReceiptDetail detail : receipt.getReceiptDetails()) {
						detail.getMaterial()
								.setCode(matIssues.get(0).getMaterialIssueDetails().get(0).getMaterial().getCode());
						detail.getUom().setCode(matIssues.get(0).getMaterialIssueDetails().get(0).getUom().getCode());
						// converting and validating userReceivedqty with issuedqty
						if (setConvertedQuantity(tenantId, detail, issuedQuantity, request.getRequestInfo())) {
							errors.addDataError(ErrorCode.QTY1_EQ_QTY2.getCode(), "Received Qty", "Issued Qty", null);
						}
						/*
						 * else // updating material issue table status
						 * updateStatusAsReceipted(receipt.getIssueNumber(), tenantId);
						 */

						for (MaterialReceiptDetailAddnlinfo info : detail.getReceiptDetailsAddnInfo()) {
							if (info.getReceivedDate() <= issueDate) {
								errors.addDataError(ErrorCode.DATE1_GT_DATE2.getCode(), "Receive Date", "Issue ", null);
							}
							info.setSerialNo(setDetailAddinfoFromIssueDetail(matIssues).getSerialNo());
							info.setLotNo(setDetailAddinfoFromIssueDetail(matIssues).getLotNo());
						}
					}
				}
			} else {
				errors.addDataError(ErrorCode.MANDATORY_VALUE_MISSING.getCode(), "IssueNumber", null);
			}
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;
	}

	// used to set or build receipt-detail-add-info using material issue detail
	private MaterialReceiptDetailAddnlinfo setDetailAddinfoFromIssueDetail(List<MaterialIssue> issue) {
		MaterialReceiptDetailAddnlinfo detailInfo = new MaterialReceiptDetailAddnlinfo();
		MaterialReceiptDetail details = new MaterialReceiptDetail();
		for (MaterialIssue iss : issue) {
			for (MaterialIssueDetail issDetail : iss.getMaterialIssueDetails()) {
				for (MaterialIssuedFromReceipt fromReceipt : issDetail.getMaterialIssuedFromReceipts()) {
					MaterialReceiptAddInfoSearch materialReceiptAddInfoSearch = MaterialReceiptAddInfoSearch.builder()
							.id(Arrays.asList(fromReceipt.getMaterialReceiptDetailAddnlinfoId()))
							.tenantId(fromReceipt.getTenantId()).build();
					Pagination<MaterialReceiptDetailAddnlinfo> info = materialReceiptDetailAddInfoJdbcRepository
							.search(materialReceiptAddInfoSearch);
					details.setReceiptDetailsAddnInfo(info.getPagedData());
					if (!isEmpty(details)) {
						for (MaterialReceiptDetailAddnlinfo addInfo : details.getReceiptDetailsAddnInfo()) {
							detailInfo.setSerialNo(addInfo.getSerialNo());
							detailInfo.setLotNo(addInfo.getLotNo());
							detailInfo.setManufactureDate(addInfo.getManufactureDate());
							detailInfo.setExpiryDate(addInfo.getExpiryDate());
							detailInfo.setTenantId(addInfo.getTenantId());
						}
					}
				}
			}
		}
		return detailInfo;
	}

	// used this api for updating status as receipted
	private void updateStatusAsReceipted(String issueNumber, String tenantId) {
		materialIssueJdbcRepository.updateStatus(issueNumber, "RECEIPTED", tenantId);
	}

	private Long getCurrentDate() {
		return currentEpochWithoutTime() + (24 * 60 * 60) - 1;
	}

	// converting user received qty on basis of conversion factor and setting it
	// into receipt received qty
	private Boolean setConvertedQuantity(String tenantId, MaterialReceiptDetail detail, BigDecimal issuedQty,
			RequestInfo requestInfo) {
		Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code", detail.getUom().getCode(),
				Uom.class, requestInfo);
		detail.setUom(uom);

		if (null != detail.getUserReceivedQty() && null != uom.getConversionFactor()) {
			BigDecimal convertedUserQuantity = getSaveConvertedQuantity(detail.getUserReceivedQty(),
					uom.getConversionFactor());
			detail.setReceivedQty(convertedUserQuantity);
			int res = convertedUserQuantity.compareTo(issuedQty);
			if (res != 0) {
				return true;
			}
		}
		return false;

	}

	public PDFResponse printPdf(MaterialReceiptSearch materialReceiptSearch, String tenantId, RequestInfo requestInfo) {
		TransferInwardResponse transferInwardResponse = search(materialReceiptSearch, tenantId);
		if (!transferInwardResponse.getTransferInwards().isEmpty()
				&& transferInwardResponse.getTransferInwards().size() == 1) {
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
			for (MaterialReceipt in : transferInwardResponse.getTransferInwards()) {
				JSONObject indent = new JSONObject();

				MaterialIssueSearchContract searchContract = new MaterialIssueSearchContract(tenantId, null, null, null,
						in.getIssueNumber(), null, null, null, null, null, null, null, null, null, null, null, null, null);
				MaterialIssueResponse materialIssueResponse = materialIssuesService.search(searchContract,
						IssueTypeEnum.MATERIALOUTWARD.toString());
				List<MaterialIssueDetail> materialIssueDetail = null;

				indent.put("inwardNumber", in.getMrnNumber());
				if (in.getReceiptDate() != null) {
					Instant receiptDate = Instant.ofEpochMilli(in.getReceiptDate());
					indent.put("inwardDate", fmt.format(receiptDate.atZone(ZoneId.systemDefault())));
				} else {
					indent.put("inwardDate", in.getReceiptDate());
				}
				indent.put("outwardNumber", in.getIssueNumber());

				if (!materialIssueResponse.getMaterialIssues().isEmpty()
						&& materialIssueResponse.getMaterialIssues().size() == 1) {
					materialIssueDetail = materialIssueResponse.getMaterialIssues().get(0).getMaterialIssueDetails();
					indent.put("indentPurpose",
							materialIssueResponse.getMaterialIssues().get(0).getIndent().getIndentPurpose());
					if (materialIssueResponse.getMaterialIssues().get(0).getIssueDate() != null) {
						Instant issueDate = Instant
								.ofEpochMilli(materialIssueResponse.getMaterialIssues().get(0).getIssueDate());
						indent.put("outwardDate", fmt.format(issueDate.atZone(ZoneId.systemDefault())));
					} else {
						indent.put("outwardDate", materialIssueResponse.getMaterialIssues().get(0).getIssueDate());
					}
				} else {
					indent.put("outwardDate", "");
					indent.put("indentPurpose", "");
				}

				indent.put("issuingStoreName", in.getIssueingStore().getName());
				indent.put("issuingStoreDept", in.getIssueingStore().getDepartment().getName());
				indent.put("indentingStoreName", in.getReceivingStore().getName());
				indent.put("indentingStoreDept", in.getReceivingStore().getDepartment().getName());
				indent.put("inwardStatus", in.getMrnStatus());

				indent.put("remark", in.getDescription());
				indent.put("createdBy", in.getReceivedBy());
				indent.put("designation", in.getDesignation());

				JSONArray indentDetails = new JSONArray();
				int i = 1;
				BigDecimal receiptTotalValue = BigDecimal.ZERO;
				for (MaterialReceiptDetail detail : in.getReceiptDetails()) {
					JSONObject indentDetail = new JSONObject();
					indentDetail.put("srNo", i++);
					indentDetail.put("materialCode", detail.getMaterial().getCode());
					indentDetail.put("materialName", detail.getMaterial().getName());
					indentDetail.put("materialDescription", detail.getMaterial().getDescription());
					indentDetail.put("uomName", detail.getUom().getName());
					indentDetail.put("receivedQuantity", detail.getReceivedQty());

					if (materialIssueDetail != null) {
						MaterialIssueDetail materialIssue = materialIssueDetail.stream().filter(
								predicate -> predicate.getMaterial().getCode().equals(detail.getMaterial().getCode()))
								.findAny().orElse(null);
						if (materialIssue != null)
							indentDetail.put("quantityIssued", materialIssue.getQuantityIssued());
						else
							indentDetail.put("quantityIssued", "");
					} else {
						indentDetail.put("quantityIssued", "");
					}
					indentDetail.put("totalValue", detail.getUnitRate().multiply(detail.getReceivedQty()));
					indentDetail.put("unitRate", detail.getUnitRate());
					indentDetail.put("remark", detail.getRemarks());
					indentDetails.add(indentDetail);
					receiptTotalValue = receiptTotalValue.add(detail.getUnitRate().multiply(detail.getReceivedQty()));
				}
				indent.put("receiptTotalValue", receiptTotalValue);
				indent.put("materialDetails", indentDetails);

				// Need to integrate Workflow

				ProcessInstanceResponse workflowData = workflowIntegrator.getWorkflowDataByID(requestInfo,
						in.getMrnNumber(), in.getTenantId());
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
				requestMain.put("IndentInwardTransfer", indents);
			}

			return pdfServiceReposistory.getPrint(requestMain, "store-asset-indent-inward",
					materialReceiptSearch.getTenantId());

		}
		return PDFResponse.builder()
				.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();

	}

	@Transactional
	public TransferInwardResponse updateStatus(TransferInwardRequest transferInwardRequest) {

		try {
			WorkFlowDetails workFlowDetails = workflowIntegrator.callWorkFlow(transferInwardRequest.getRequestInfo(),
					transferInwardRequest.getWorkFlowDetails(),
					transferInwardRequest.getWorkFlowDetails().getTenantId());
			transferInwardRequest.setWorkFlowDetails(workFlowDetails);
			kafkaQue.send(updateStatusTopic, updateStatusTopicKey, transferInwardRequest);
			TransferInwardResponse response = new TransferInwardResponse();
			response.setResponseInfo(null);
			response.setTransferInwards(transferInwardRequest.getTransferInwards());
			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}

}