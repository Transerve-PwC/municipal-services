package org.egov.assets.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.FinancialYear;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceipt.ReceiptTypeEnum;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptDetailAddnlinfo;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.OpeningBalanceRequest;
import org.egov.assets.model.OpeningBalanceResponse;
import org.egov.assets.model.PDFResponse;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.StoreResponse;
import org.egov.assets.model.Tenant;
import org.egov.assets.model.Uom;
import org.egov.assets.model.WorkFlowDetails;
import org.egov.assets.repository.MaterialReceiptJdbcRepository;
import org.egov.assets.repository.PDFServiceReposistory;
import org.egov.assets.repository.StoreJdbcRepository;
import org.egov.assets.wf.WorkflowIntegrator;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class OpeningBalanceService extends DomainService {

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Value("${inv.openbalance.save.topic}")
	private String createTopic;

	@Autowired
	private MdmsRepository mdmsRepository;

	@Autowired
	private PDFServiceReposistory pdfServiceReposistory;

	@Autowired
	private NumberGenerator numberGenerator;

	@Value("${inv.openbalance.update.topic}")
	private String updateTopic;

	@Value("${inv.openbal.idgen.name}")
	private String idGenNameForTargetNumPath;

	@Autowired
	private MaterialReceiptJdbcRepository jdbcRepository;

	@Autowired
	private MaterialService materialService;

	@Autowired
	private StoreJdbcRepository storeJdbcRepository;
	
	@Autowired
	private StoreService storeService;

	@Autowired
	private MaterialReceiptService materialReceiptService;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Value("${inv.openbalance.updatestatus.topic}")
	private String updatestatusTopic;

	public List<MaterialReceipt> create(OpeningBalanceRequest openBalReq, String tenantId) {
		try {
			validate(openBalReq.getMaterialReceipt(), Constants.ACTION_CREATE, tenantId, openBalReq.getRequestInfo());
			openBalReq.getMaterialReceipt().stream().forEach(materialReceipt -> {
				materialReceipt.setId(jdbcRepository.getSequence("seq_materialreceipt"));
				materialReceipt.setMrnStatus(MaterialReceipt.MrnStatusEnum.CREATED);
				if (isEmpty(materialReceipt.getTenantId())) {
					materialReceipt.setTenantId(tenantId);
				}
				materialReceipt.setReceiptType(ReceiptTypeEnum.valueOf("OPENING_BALANCE"));
				materialReceipt.setMrnNumber(getOpbNumber(materialReceipt, openBalReq.getRequestInfo()));
				if (null != materialReceipt.getReceiptDetails()) {
					materialReceipt.getReceiptDetails().stream().forEach(detail -> {
						detail.setId(jdbcRepository.getSequence("seq_materialreceiptdetail"));
						setQuantity(tenantId, detail, openBalReq.getRequestInfo());
						convertRate(tenantId, detail, openBalReq.getRequestInfo());
						if (isEmpty(detail.getTenantId())) {
							detail.setTenantId(tenantId);
						}
						if (null != detail.getReceiptDetailsAddnInfo()
								|| detail.getReceiptDetailsAddnInfo().size() > 0) {
							detail.getReceiptDetailsAddnInfo().stream().forEach(addinfo -> {
								if (!(isEmpty(addinfo.getLotNo()) && isEmpty(addinfo.getExpiryDate())
										&& isEmpty(addinfo.getOldReceiptNumber()) && isEmpty(addinfo.getReceivedDate())
										&& isEmpty(addinfo.getOldReceiptNumber()))) {
									addinfo.setId(jdbcRepository.getSequence("seq_materialreceiptdetailaddnlinfo"));
									addinfo.setUserQuantity(detail.getUserReceivedQty());
									addinfo.setQuantity(detail.getReceivedQty());
									if (isEmpty(addinfo.getTenantId())) {
										addinfo.setTenantId(tenantId);
									}
								} else {
									detail.setReceiptDetailsAddnInfo(Collections.EMPTY_LIST);
								}

							});
						} else {
							detail.setReceiptDetailsAddnInfo(Collections.EMPTY_LIST);
						}
					});
				}
				WorkFlowDetails workFlowDetails = openBalReq.getWorkFlowDetails();
				workFlowDetails.setBusinessId(materialReceipt.getMrnNumber());
				workflowIntegrator.callWorkFlow(openBalReq.getRequestInfo(), workFlowDetails,
						materialReceipt.getTenantId());

			});

			for (MaterialReceipt material : openBalReq.getMaterialReceipt()) {
				material.setAuditDetails(getAuditDetails(openBalReq.getRequestInfo(), "CREATE"));

				for (MaterialReceiptDetail recipt : material.getReceiptDetails()) {
					recipt.auditDetails(getAuditDetails(openBalReq.getRequestInfo(), "CREATE"));
				}
				material.setId(jdbcRepository.getSequence(material));
			}

			System.out.println("openBalReq :" + openBalReq);
			kafkaTemplate.send(createTopic, openBalReq);
			return openBalReq.getMaterialReceipt();
		} catch (CustomBindException e) {
			throw e;
		}
	}

	public List<MaterialReceipt> update(OpeningBalanceRequest openBalReq, String tenantId) {
		try {
			validate(openBalReq.getMaterialReceipt(), Constants.ACTION_UPDATE, tenantId, openBalReq.getRequestInfo());
			List<String> materialReceiptDetailIds = new ArrayList<>();
			List<String> materialReceiptDetailAddlnInfoIds = new ArrayList<>();
			openBalReq.getMaterialReceipt().stream().forEach(materialReceipt -> {
				if (isEmpty(materialReceipt.getTenantId())) {
					materialReceipt.setTenantId(tenantId);
				}
				materialReceipt.getReceiptDetails().stream().forEach(detail -> {
					if (isEmpty(detail.getTenantId())) {
						detail.setTenantId(tenantId);
					}
					setQuantity(tenantId, detail, openBalReq.getRequestInfo());
					if (isEmpty(detail.getId())) {
						setMaterialDetails(tenantId, detail);
					}
					materialReceiptDetailIds.add(detail.getId());
					if (null != detail.getReceiptDetailsAddnInfo()) {
						detail.getReceiptDetailsAddnInfo().stream().forEach(addinfo -> {
							if (!(isEmpty(addinfo.getLotNo()) && isEmpty(addinfo.getExpiryDate())
									&& isEmpty(addinfo.getOldReceiptNumber()) && isEmpty(addinfo.getReceivedDate())
									&& isEmpty(addinfo.getOldReceiptNumber()))) {
								if (isEmpty(addinfo.getTenantId())) {
									addinfo.setTenantId(tenantId);
									addinfo.setQuantity(detail.getReceivedQty());
									addinfo.setUserQuantity(detail.getUserReceivedQty());
								}
								materialReceiptDetailAddlnInfoIds.add(addinfo.getId());
							} else {
								detail.setReceiptDetailsAddnInfo(Collections.EMPTY_LIST);
							}
						});
					} else {
						detail.setReceiptDetailsAddnInfo(Collections.EMPTY_LIST);
					}
				});
				jdbcRepository.markDeletedPKeyIn(materialReceiptDetailAddlnInfoIds, tenantId,
						"materialreceiptdetailaddnlinfo", "receiptdetailid", materialReceiptDetailIds);
				jdbcRepository.markDeleted(materialReceiptDetailIds, tenantId, "materialreceiptdetail", "mrnNumber",
						materialReceipt.getMrnNumber());
			});

			for (MaterialReceipt material : openBalReq.getMaterialReceipt()) {
				material.setAuditDetails(getAuditDetails(openBalReq.getRequestInfo(), "UPDATE"));
			}

			System.out.println("openBalReq Update :" + openBalReq);

			kafkaTemplate.send(updateTopic, openBalReq);
			return openBalReq.getMaterialReceipt();
		} catch (CustomBindException e) {
			throw e;
		}
	}

	public OpeningBalanceResponse search(MaterialReceiptSearch materialReceiptSearch) {
		Pagination<MaterialReceipt> materialReceiptPagination = materialReceiptService.search(materialReceiptSearch);
		OpeningBalanceResponse response = new OpeningBalanceResponse();
		return response.responseInfo(null).materialReceipt(
				materialReceiptPagination.getPagedData().size() > 0 ? materialReceiptPagination.getPagedData()
						: Collections.EMPTY_LIST);
	}


	private String getOpbNumber(MaterialReceipt receipt, RequestInfo info) {
		InvalidDataException errors = new InvalidDataException();
		ObjectMapper mapper = new ObjectMapper();
		JSONArray tenantStr = mdmsRepository.getByCriteria(receipt.getTenantId(), "tenant", "tenants", "code",
				receipt.getTenantId(), info);

		Tenant tenant = mapper.convertValue(tenantStr.get(0), Tenant.class);
		if (tenant == null) {
			errors.addDataError(ErrorCode.CITY_CODE_NOT_AVAILABLE.getCode(), receipt.getTenantId());
		}
		String finYearRange = "";
		JSONArray finYears = mdmsRepository.getByCriteria(receipt.getTenantId(), "egf-master", "FinancialYear", null,
				null, info);
		outer: for (int i = 0; i < finYears.size(); i++) {
			FinancialYear fin = mapper.convertValue(finYears.get(i), FinancialYear.class);
			if (getCurrentDate() >= fin.getStartingDate().getTime()
					&& getCurrentDate() <= fin.getEndingDate().getTime()) {
				finYearRange = fin.getFinYearRange();
				break outer;
			}
		}

		if (finYearRange.isEmpty()) {
			errors.addDataError(ErrorCode.FIN_YEAR_NOT_EXIST.getCode(), receipt.getFinancialYear().toString());
		}
		if (errors.getValidationErrors().size() > 0) {
			throw errors;
		}

		String seq = "OPB-" + tenant.getCity().getCode() + "-" + finYearRange;
		return seq + "-" + numberGenerator.getNextNumber(seq, 5);
	}

	private void validate(List<MaterialReceipt> receipt, String method, String tenantId, RequestInfo info) {
		InvalidDataException errors = new InvalidDataException();

		try {
			switch (method) {
			case Constants.ACTION_CREATE: {
				if (receipt == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "materialReceipt", null);

				}
			}
				break;

			case Constants.ACTION_UPDATE: {
				if (receipt == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "materialReceipt", null);
				}
			}
				break;

			}

			for (MaterialReceipt rcpt : receipt) {
				int index = receipt.indexOf(rcpt) + 1;
				ObjectMapper mapper = new ObjectMapper();

				if (!isEmpty(rcpt.getFinancialYear())) {
					JSONArray finYears = mdmsRepository.getByCriteria(tenantId, "egf-master", "FinancialYear",
							"finYearRange", rcpt.getFinancialYear(), info);
					if (finYears != null && finYears.size() > 0) {
						for (int i = 0; i < finYears.size(); i++) {
							FinancialYear fin = mapper.convertValue(finYears.get(i), FinancialYear.class);
							if (getCurrentDate() >= fin.getStartingDate().getTime()
									&& getCurrentDate() <= fin.getEndingDate().getTime()) {
								rcpt.setFinancialYear(fin.getFinYearRange());
							} else
								errors.addDataError(ErrorCode.FIN_CUR_YEAR.getCode(),
										"Financial Year (" + rcpt.getFinancialYear() + ")");
						}
					} else
						errors.addDataError(ErrorCode.FIN_YEAR_NOT_EXIST.getCode(), rcpt.getFinancialYear());
				} else {
					errors.addDataError(ErrorCode.FIN_YEAR_NOT_EXIST.getCode(), rcpt.getFinancialYear());
				}
				if (isEmpty(rcpt.getReceivingStore().getCode())) {
					errors.addDataError(ErrorCode.RECEIVING_STORE_NOT_EXIST.getCode(),
							rcpt.getReceivingStore().getCode());
				} else {
					if (validateStore(tenantId, rcpt)) {
						errors.addDataError(ErrorCode.INVALID_ACTIVE_VALUE.getCode(),
								"Receiving Store " + rcpt.getReceivingStore().getCode());
					}
				}

				if (null != rcpt.getReceiptDetails()) {
					for (MaterialReceiptDetail detail : rcpt.getReceiptDetails()) {
						int detailIndex = rcpt.getReceiptDetails().indexOf(detail) + 1;

						if (isEmpty(detail.getUom().getCode())) {
							errors.addDataError(ErrorCode.UOM_CODE_NOT_EXIST.getCode(),
									detail.getUom().getCode() + " at serial no." + detailIndex);
						}

						if (isEmpty(detail.getMaterial().getCode())) {
							errors.addDataError(ErrorCode.MATERIAL_NAME_NOT_EXIST.getCode(),
									detail.getMaterial().getCode() + " at serial no." + detailIndex);
						}

						if (!validateUom(tenantId, detail, info)) {
							errors.addDataError(ErrorCode.CATGRY_MATCH.getCode(), detail.getMaterial().getCode(),
									detail.getUom().getCode(), "At Row " + detailIndex);
						}

						if (isEmpty(detail.getUserReceivedQty())) {
							errors.addDataError(ErrorCode.RCVED_QTY_NOT_EXIST.getCode(),
									detail.getUserReceivedQty() + " at serial no." + detailIndex);
						} else {
							int res1 = detail.getUserReceivedQty().compareTo(BigDecimal.ZERO);
							if (res1 != 1) {
								errors.addDataError(ErrorCode.RCVED_QTY_GT_ZERO.getCode(),
										detail.getUserReceivedQty() + " at serial no." + detailIndex);
							}
						}
						if (isEmpty(detail.getUnitRate())) {
							errors.addDataError(ErrorCode.UNIT_RATE_NOT_EXIST.getCode(),
									detail.getUnitRate() + " at serial no." + detailIndex);
						} else {
							int res = detail.getUnitRate().compareTo(BigDecimal.ZERO);
							if (res != 1) {
								errors.addDataError(ErrorCode.UNIT_RATE_GT_ZERO.getCode(),
										detail.getUnitRate() + " at serial no." + detailIndex);
							}
						}

						Material material = materialService.fetchMaterial(tenantId, detail.getMaterial().getCode(),
								new org.egov.common.contract.request.RequestInfo());

						if (null != detail.getReceiptDetailsAddnInfo()) {
							for (MaterialReceiptDetailAddnlinfo addInfo : detail.getReceiptDetailsAddnInfo()) {

								/*
								 * if (null != material && material.getLotControl() == true &&
								 * isEmpty(addInfo.getLotNo())) {
								 * errors.addDataError(ErrorCode.LOT_NO_NOT_EXIST.getCode(), addInfo.getLotNo()
								 * + " at serial no." + detailIndex); }
								 */
								if (null != material && material.getShelfLifeControl() == true
										&& isEmpty(addInfo.getExpiryDate())
										|| (!isEmpty(addInfo.getExpiryDate())
												&& !(addInfo.getExpiryDate().doubleValue() > 0))) {
									errors.addDataError(ErrorCode.EXP_DATE_NOT_EXIST.getCode(),
											addInfo.getExpiryDate() + " at serial no." + detailIndex);
								}

								if (null != addInfo.getReceivedDate()
										&& Long.valueOf(addInfo.getReceivedDate()) > getCurrentDate()) {
									String date = convertEpochtoDate(addInfo.getReceivedDate());
									errors.addDataError(ErrorCode.RCPT_DATE_LE_TODAY.getCode(),
											date + " at serial no." + detailIndex);

								}
								if (null != addInfo.getExpiryDate()
										&& Long.valueOf(addInfo.getExpiryDate()) < getCurrentDate()) {
									String date = convertEpochtoDate(addInfo.getExpiryDate());
									errors.addDataError(ErrorCode.EXP_DATE_GE_TODAY.getCode(),
											date + " at serial no." + detailIndex);
								}
							}
						}
					}
				} else
					errors.addDataError(ErrorCode.NULL_VALUE.getCode(), "receiptDetail");

			}

		} catch (IllegalArgumentException e) {

		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;
	}

	private void setMaterialDetails(String tenantId, MaterialReceiptDetail materialReceiptDetail) {
		materialReceiptDetail.setId(jdbcRepository.getSequence("seq_materialreceiptdetail"));
		if (isEmpty(materialReceiptDetail.getTenantId())) {
			materialReceiptDetail.setTenantId(tenantId);
		}

		materialReceiptDetail.getReceiptDetailsAddnInfo().forEach(materialReceiptDetailAddnlInfo -> {
			materialReceiptDetailAddnlInfo.setId(jdbcRepository.getSequence("seq_materialreceiptdetailaddnlinfo"));
			if (isEmpty(materialReceiptDetailAddnlInfo.getTenantId())) {
				materialReceiptDetailAddnlInfo.setTenantId(tenantId);
			}
		});
	}

	private void setQuantity(String tenantId, MaterialReceiptDetail detail, RequestInfo requestInfo) {
		Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code", detail.getUom().getCode(),
				Uom.class, requestInfo);
		detail.setUom(uom);

		if (null != detail.getUserReceivedQty() && null != uom.getConversionFactor()) {
			BigDecimal convertedReceivedQuantity = getSaveConvertedQuantity(detail.getUserReceivedQty(),
					uom.getConversionFactor());
			detail.setReceivedQty(convertedReceivedQuantity);
		}

	}

	private boolean validateUom(String tenantId, MaterialReceiptDetail detail, RequestInfo requestInfo) {
		Material material = materialService.fetchMaterial(tenantId, detail.getMaterial().getCode(),
				new org.egov.common.contract.request.RequestInfo());
		String uomCategory = material.getBaseUom().getUomCategory();
		List<String> uomList = new ArrayList<>();
		List<Object> objectList = mdmsRepository.fetchObjectList(tenantId, "common-masters", "UOM", "uomCategory",
				uomCategory, Uom.class, requestInfo);
		for (Object o : objectList) {
			Uom uom = (Uom) o;
			uomList.add(uom.getCode());
		}
		return uomList.stream().anyMatch(Collections.singletonList(detail.getUom().getCode())::contains);
	}

	private boolean validateStore(String tenantId, MaterialReceipt rcpt) {
		StoreGetRequest storeEntity = StoreGetRequest.builder()
				.code(Collections.singletonList(rcpt.getReceivingStore().getCode())).tenantId(tenantId).active(true)
				.build();
		Pagination<Store> store = storeJdbcRepository.search(storeEntity);
		if (store.getPagedData().size() > 0) {
			return false;
		}
		return true;
	}

	private void convertRate(String tenantId, MaterialReceiptDetail detail, RequestInfo requestInfo) {
		Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code", detail.getUom().getCode(),
				Uom.class, requestInfo);
		detail.setUom(uom);

		if (null != detail.getUnitRate() && null != uom.getConversionFactor()) {
			BigDecimal convertedRate = getSaveConvertedRate(detail.getUnitRate(), uom.getConversionFactor());
			detail.setUnitRate(convertedRate);
		}

	}

	private Long getCurrentDate() {
		return currentEpochWithoutTime() + (24 * 60 * 60) - 1;
	}

	private String convertEpochtoDate(Long date) {
		Date epoch = new Date(date);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		String s2 = format.format(epoch);
		return s2;
	}

	public PDFResponse printOpeningBalanceReportPdf(MaterialReceiptSearch materialReceiptSearch,
			RequestInfo requestInfo) {
		OpeningBalanceResponse balanceResponse = search(materialReceiptSearch);

		if (!balanceResponse.getMaterialReceipt().isEmpty()) {

			JSONObject mainRequest = new JSONObject();
			ObjectMapper mapper = new ObjectMapper();
			try {
				JSONObject reqInfo = (JSONObject) new JSONParser().parse(mapper.writeValueAsString(requestInfo));
				if (materialReceiptSearch.isForprint())
					mainRequest.put("RequestInfo", reqInfo);
				else
					mainRequest.put("ResponseInfo", null);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("storeName", balanceResponse.getMaterialReceipt().get(0).getReceivingStore().getName());
			jsonObject.put("financialYear", balanceResponse.getMaterialReceipt().get(0).getFinancialYear());

			org.json.simple.JSONArray materialDetails = new org.json.simple.JSONArray();
			org.json.simple.JSONArray mainMaterials = new org.json.simple.JSONArray();
			int i = 0;
			for (MaterialReceipt materialReceipt : balanceResponse.getMaterialReceipt()) {
				for (MaterialReceiptDetail detail : materialReceipt.getReceiptDetails()) {
					JSONObject materialDetail = new JSONObject();
					materialDetail.put("srNo", i++);
					materialDetail.put("materialCode", detail.getMaterial().getCode());
					materialDetail.put("materialName", detail.getMaterial().getName());
					materialDetail.put("materialType", detail.getMaterial().getMaterialType().getName());
					materialDetail.put("uomName", detail.getMaterial().getPurchaseUom().getCode());
					materialDetail.put("quantity", detail.getUserAcceptedQty());
					materialDetail.put("unitRate", detail.getUnitRate());
					materialDetail.put("totalAmount", detail.getUserAcceptedQty().multiply(detail.getUnitRate()));
					materialDetail.put("remarks", detail.getRemarks());
					materialDetails.add(materialDetail);
				}
			}
			jsonObject.put("balanceDetails", materialDetails);
			mainMaterials.add(jsonObject);
			mainRequest.put("OpeningBalanceReport", mainMaterials);

			if (materialReceiptSearch.isForprint())
				return pdfServiceReposistory.getPrint(mainRequest, "store-asset-report-opening-balance",
						materialReceiptSearch.getTenantId());
			else
				return PDFResponse.builder().responseInfo(ResponseInfo.builder().status("Success").build())
						.printData(mainRequest).build();
		}

		return PDFResponse.builder()
				.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();
	}
	
	public PDFResponse printClosingBalanceReportPdf(MaterialReceiptSearch materialReceiptSearch,
			RequestInfo requestInfo) {
		OpeningBalanceResponse balanceResponse = search(materialReceiptSearch);

		if (!balanceResponse.getMaterialReceipt().isEmpty()) {

			JSONObject mainRequest = new JSONObject();
			ObjectMapper mapper = new ObjectMapper();
			try {
				JSONObject reqInfo = (JSONObject) new JSONParser().parse(mapper.writeValueAsString(requestInfo));
				if (materialReceiptSearch.isForprint())
					mainRequest.put("RequestInfo", reqInfo);
				else
					mainRequest.put("ResponseInfo", null);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("storeName", balanceResponse.getMaterialReceipt().get(0).getReceivingStore().getName());
			jsonObject.put("asOnDate", materialReceiptSearch.getAsOnDate());

			org.json.simple.JSONArray materialDetails = new org.json.simple.JSONArray();
			org.json.simple.JSONArray mainMaterials = new org.json.simple.JSONArray();
			int i = 0;
			for (MaterialReceipt materialReceipt : balanceResponse.getMaterialReceipt()) {
				for (MaterialReceiptDetail detail : materialReceipt.getReceiptDetails()) {
					JSONObject materialDetail = new JSONObject();
					materialDetail.put("srNo", i++);
					materialDetail.put("materialCode", detail.getMaterial().getCode());
					materialDetail.put("materialName", detail.getMaterial().getName());
					materialDetail.put("materialType", detail.getMaterial().getMaterialType().getName());
					materialDetail.put("uomName", detail.getMaterial().getPurchaseUom().getCode());
					materialDetail.put("quantity", detail.getAcceptedQty());
					materialDetail.put("unitRate", detail.getUnitRate());
					if(detail.getAcceptedQty()!=null && detail.getUnitRate()!=null)
					{
					materialDetail.put("totalAmount", detail.getAcceptedQty().multiply(detail.getUnitRate()));
					}
					else {
						materialDetail.put("totalAmount", "");
					}
					materialDetail.put("remarks", detail.getRemarks());
					materialDetails.add(materialDetail);
				}
			}
			jsonObject.put("balanceDetails", materialDetails);
			mainMaterials.add(jsonObject);
			mainRequest.put("OpeningBalanceReport", mainMaterials);

			if (materialReceiptSearch.isForprint())
				return pdfServiceReposistory.getPrint(mainRequest, "store-asset-report-closing-balance",
						materialReceiptSearch.getTenantId());
				
				/*return pdfServiceReposistory.getPrint(mainRequest, "store-asset-stock-report",
						materialReceiptSearch.getTenantId());*/
			else
				return PDFResponse.builder().responseInfo(ResponseInfo.builder().status("Success").build())
						.printData(mainRequest).build();
		}

		return PDFResponse.builder()
				.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();
	}
	public PDFResponse printStockBalanceReportPdf(MaterialReceiptSearch materialReceiptSearch,
			RequestInfo requestInfo) {
		org.json.simple.JSONArray jsonArray = materialReceiptService.searchStock(materialReceiptSearch);
		
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
			requestMain.put("asOnDate",materialReceiptSearch.getAsOnDate());
			for(Object js:jsonArray)
			{
				JSONObject js1=(JSONObject)js;
				js1.put("materialName", material.getName());
				js1.put("materialCode", material.getCode());
				js1.put("materialType","");
				//(JSONObject) js.pu
				
			}
			requestMain.put("stockDetails", jsonArray);
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
			finalDta.put("stockDetails", arrayPrintData);

			return pdfServiceReposistory.getPrint(finalDta, "store-asset-stock-report",
					materialReceiptSearch.getTenantId());
		} else if (!arrayPrintData.isEmpty() && !materialReceiptSearch.isForprint()) {
			return PDFResponse.builder().responseInfo(ResponseInfo.builder().status("Success").build())
					.printData(arrayPrintData).build();
		} else {
			return PDFResponse.builder()
					.responseInfo(ResponseInfo.builder().status("Failed").resMsgId("No data found").build()).build();
		}
	}

	public OpeningBalanceResponse updateStatus(OpeningBalanceRequest openingBalance) {

		try {
			WorkFlowDetails workFlowDetails = workflowIntegrator.callWorkFlow(openingBalance.getRequestInfo(),
					openingBalance.getWorkFlowDetails(), openingBalance.getWorkFlowDetails().getTenantId());
			openingBalance.setWorkFlowDetails(workFlowDetails);
			kafkaQue.send(updatestatusTopic, openingBalance);
			OpeningBalanceResponse response = new OpeningBalanceResponse();
			response.setMaterialReceipt(openingBalance.getMaterialReceipt());
			response.setResponseInfo(getResponseInfo(openingBalance.getRequestInfo()));
			return response;
		} catch (CustomBindException e) {
			throw e;
		}
	}
}
