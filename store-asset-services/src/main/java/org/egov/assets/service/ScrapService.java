package org.egov.assets.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialIssue;
import org.egov.assets.model.MaterialIssue.IssueTypeEnum;
import org.egov.assets.model.MaterialIssue.MaterialIssueStatusEnum;
import org.egov.assets.model.MaterialIssueDetail;
import org.egov.assets.model.MaterialIssueResponse;
import org.egov.assets.model.MaterialIssueSearchContract;
import org.egov.assets.model.MaterialIssuedFromReceipt;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptDetailSearch;
import org.egov.assets.model.Scrap;
import org.egov.assets.model.Scrap.ScrapStatusEnum;
import org.egov.assets.model.ScrapDetail;
import org.egov.assets.model.ScrapDetailSearch;
import org.egov.assets.model.ScrapRequest;
import org.egov.assets.model.ScrapResponse;
import org.egov.assets.model.ScrapSearch;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.Tenant;
import org.egov.assets.model.Uom;
import org.egov.assets.repository.MaterialIssueDetailJdbcRepository;
import org.egov.assets.repository.MaterialIssuedFromReceiptJdbcRepository;
import org.egov.assets.repository.ScrapDetailJdbcRepository;
import org.egov.assets.repository.ScrapJdbcRepository;
import org.egov.assets.repository.StoreJdbcRepository;
import org.egov.assets.repository.entity.MaterialIssueDetailEntity;
import org.egov.assets.repository.entity.MaterialIssueEntity;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.kafka.LogAwareKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class ScrapService extends DomainService {

	@Autowired
	private LogAwareKafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private NonIndentMaterialIssueService nonIndentMaterialIssueService;

	@Autowired
	private MaterialIssueDetailJdbcRepository materialIssueDetailsJdbcRepository;

	@Autowired
	private MaterialIssuedFromReceiptJdbcRepository materialIssuedFromReceiptsJdbcRepository;

	@Autowired
	private ScrapJdbcRepository scrapJdbcRepository;

	@Autowired
	private MaterialReceiptDetailService materialReceiptDetailService;

	@Autowired
	private MdmsRepository mdmsRepository;

	@Autowired
	private StoreJdbcRepository storeJdbcRepository;

	@Autowired
	private NumberGenerator numberGenerator;

	@Autowired
	private ScrapDetailJdbcRepository scrapDetailJdbcRepository;

	@Value("${inv.scrap.save.topic}")
	private String createTopic;

	@Value("${inv.scrap.update.topic}")
	private String updateTopic;

	public List<Scrap> create(ScrapRequest scrapReq, String tenantId) {
		try {
			fetchRelated(scrapReq, tenantId);
			List<Scrap> scrap = scrapReq.getScraps();
			validate(scrapReq.getScraps(), Constants.ACTION_CREATE, tenantId, scrapReq.getRequestInfo());

			scrap.forEach(scrapData -> {
				scrapData.setAuditDetails(getAuditDetails(scrapReq.getRequestInfo(), Constants.ACTION_CREATE));
				scrapData.setId(scrapJdbcRepository.getSequence("seq_scrap"));
				scrapData.setScrapNumber(getScrapNumber(scrapData, scrapReq.getRequestInfo(), tenantId));
				scrapData.setScrapStatus(ScrapStatusEnum.APPROVED);
				if (StringUtils.isEmpty(scrapData.getTenantId())) {
					scrapData.setTenantId(tenantId);
				}
				scrapData.getScrapDetails().forEach(scrapDetails -> {
					scrapDetails.setAuditDetails(getAuditDetails(scrapReq.getRequestInfo(), Constants.ACTION_CREATE));
					scrapDetails.setId(scrapJdbcRepository.getSequence("seq_scrapDetail"));
					scrapDetails.setTenantId(tenantId);
				});
			});
			materialIssueBackUpdate(scrapReq, tenantId);
			kafkaTemplate.send(createTopic, scrapReq);
			return scrapReq.getScraps();
		} catch (CustomBindException e) {
			throw e;
		}
	}

	public List<Scrap> update(ScrapRequest scrapReq, String tenantId) {
		try {
			fetchRelated(scrapReq, tenantId);
			List<Scrap> scrap = scrapReq.getScraps();
			validate(scrapReq.getScraps(), Constants.ACTION_UPDATE, tenantId, scrapReq.getRequestInfo());
			scrap.forEach(scrapData -> {
				scrapData.setAuditDetails(getAuditDetails(scrapReq.getRequestInfo(), Constants.ACTION_UPDATE));

				if (StringUtils.isEmpty(scrapData.getTenantId())) {
					scrapData.setTenantId(tenantId);
				}
				if (StringUtils.isEmpty(scrapData.getId())) {
					scrapData.setId(scrapJdbcRepository.getSequence("seq_scrap"));
				}

				scrapData.getScrapDetails().forEach(scrapDetails -> {
					if (StringUtils.isEmpty(scrapDetails.getId())) {
						scrapDetails.setId(scrapJdbcRepository.getSequence("seq_scrapDetail"));
					}
					scrapDetails.setUpdatedScrapedQty(scrapDetails.getScrapQuantity());
					List<ScrapDetail> scrapDetail = getScrapDetails(scrapDetails.getScrapNumber(),
							scrapData.getTenantId());
					if (!scrapDetail.isEmpty() && scrapDetail.size() == 1) {
						scrapDetails.setScrapQuantity(
								scrapDetails.getScrapQuantity().subtract(scrapDetail.get(0).getScrapQuantity()));
					}
					scrapDetails.setAuditDetails(getAuditDetails(scrapReq.getRequestInfo(), Constants.ACTION_UPDATE));

					if (StringUtils.isEmpty(scrapDetails.getTenantId())) {
						scrapDetails.setTenantId(tenantId);
					}
					scrapDetails.setScrapNumber(scrapData.getScrapNumber());
				});

			});
			materialIssueBackUpdate(scrapReq, tenantId);
			kafkaTemplate.send(updateTopic, scrapReq);
			return scrapReq.getScraps();
		} catch (CustomBindException e) {
			throw e;
		}
	}

	public ScrapResponse search(ScrapSearch scrapSearch) {
		Pagination<Scrap> scrapPagination = scrapJdbcRepository.search(scrapSearch);
		if (!scrapPagination.getPagedData().isEmpty()) {
			for (Scrap scrap : scrapPagination.getPagedData()) {
				scrap.setStore(getStore(scrapSearch.getTenantId(), scrap));
				List<ScrapDetail> scrapDetail = getScrapDetails(scrap.getScrapNumber(), scrapSearch.getTenantId());
				scrap.setScrapDetails(scrapDetail);
			}
		}
		ScrapResponse response = new ScrapResponse();
		return response.responseInfo(null).scraps(
				scrapPagination.getPagedData().size() > 0 ? scrapPagination.getPagedData() : Collections.EMPTY_LIST);
	}

	private void validate(List<Scrap> scrap, String method, String tenantId, RequestInfo info) {
		InvalidDataException errors = new InvalidDataException();

		try {
			switch (method) {
			case Constants.ACTION_CREATE: {
				if (scrap == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "scrap", null);
				}
			}
				break;

			case Constants.ACTION_UPDATE: {
				if (scrap == null) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "scrap", null);
				}
			}
				break;
			}
			for (Scrap scrapData : scrap) {
				if (null == scrapData.getScrapDate()) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "scrap Date", null);
				}
				if (null == scrapData.getStore().getCode() || scrapData.getStore().getCode() == null
						|| scrapData.getStore().getCode().isEmpty()) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "Store Code", null);
				} else {
					if (validateStore(tenantId, scrapData)) {
						errors.addDataError(ErrorCode.INVALID_ACTIVE_VALUE.getCode(),
								"Store " + scrapData.getStore().getCode());
					}
				}
/*				if (null == scrapData.getDescription() || scrapData.getDescription().isEmpty()) {
					errors.addDataError(ErrorCode.NOT_NULL.getCode(), "Description", null);
				}
*/
				for (ScrapDetail scrapDetail : scrapData.getScrapDetails()) {
					if (null == scrapDetail.getUserQuantity()) {
						errors.addDataError(ErrorCode.NOT_NULL.getCode(), "Scrap Quantity", null);
					} else {
						int scrapQty = scrapDetail.getUserQuantity().compareTo(BigDecimal.ZERO);
						if (scrapQty != 1) {
							errors.addDataError(ErrorCode.QUANTITY_GT_ZERO.getCode(),
									"Scrap Quantity " + scrapDetail.getUserQuantity());
						}
					}
					int scrapValue = scrapDetail.getScrapValue().compareTo(BigDecimal.ZERO);
					if (scrapValue != 1) {
						errors.addDataError(ErrorCode.UNIT_PRICE_GT_ZERO.getCode());

					}
					BigDecimal remainQuantity = scrapDetail.getQuantity()
							.subtract(scrapDetail.getIssueDetail().getScrapedQuantity());
					if ((scrapDetail.getScrapQuantity().compareTo(remainQuantity)) == 1) {
						errors.addDataError(ErrorCode.QTY1_LE_QTY2.getCode(),
								"Scrap Quantity  " + scrapDetail.getScrapQuantity(),
								"Available Issue Quantity " + remainQuantity);

					}
				}
			}
		} catch (IllegalArgumentException e) {
			throw e;
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;
	}

	private String getScrapNumber(Scrap scrap, RequestInfo info, String tenantId) {
		InvalidDataException errors = new InvalidDataException();
		ObjectMapper mapper = new ObjectMapper();
		JSONArray tenantStr = mdmsRepository.getByCriteria(tenantId, "tenant", "tenants", "code", scrap.getTenantId(),
				info);

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

		String seq = "SCRP-" + tenant.getCity().getCode() + "-" + finYearRange;
		return seq + "-" + numberGenerator.getNextNumber(seq, 5);
	}

	private void fetchRelated(ScrapRequest request, String tenantId) {
		InvalidDataException errors = new InvalidDataException();
		List<ScrapDetail> scrapDetailList = new ArrayList<>();
		for (Scrap scrap : request.getScraps()) {
			for (ScrapDetail scrapDetails : scrap.getScrapDetails()) {
				//search scrap by issue number
				MaterialIssueSearchContract searchContract = MaterialIssueSearchContract.builder()
						//.id(Arrays.asList(scrapDetails.getIssueDetail().getId()))
						.issueNoteNumber(scrap.getIssueNumber())
						.issuePurpose(MaterialIssue.IssuePurposeEnum.WRITEOFFORSCRAP.toString())
						.materialIssueStatus(MaterialIssueStatusEnum.APPROVED.toString()).scrapCreated(false)
						.tenantId(tenantId).build();

				MaterialIssueResponse response = nonIndentMaterialIssueService.search(searchContract);
				if (response.getMaterialIssues().size() <= 0) {
					errors.addDataError(ErrorCode.NO_DATA_FOUND.getCode(), "Scrap Process");
				}
				for (MaterialIssue issue : response.getMaterialIssues()) {
					for (MaterialIssueDetail issueDetail : issue.getMaterialIssueDetails()) {
						ScrapDetail scrapDetail = new ScrapDetail();
						MaterialIssueDetail matIssueDetail = new MaterialIssueDetail();
						matIssueDetail.setId(issueDetail.getId());
						matIssueDetail.setScrapedQuantity(issueDetail.getScrapedQuantity());
						if (issue == null)
							errors.addDataError(ErrorCode.NO_DATA_FOUND.getCode(), "Scrap Process", null);

						else
							scrapDetail.setId(scrapDetails.getId());
						scrapDetail.setIssueDetail(matIssueDetail);
						scrapDetail.setTenantId(tenantId);
						scrapDetail.setUom(issueDetail.getUom());
						scrapDetail.setMaterial(issueDetail.getMaterial());
						scrapDetail.setExistingValue(issueDetail.getValue());
						scrapDetail.setQuantity(issueDetail.getQuantityIssued());

						if (scrapDetails.getUserQuantity() != null) {
							setConvertedScrapQuantity(tenantId, scrapDetails, issueDetail, request.getRequestInfo());
						}

						if (scrapDetails.getScrapValue() != null) {
							setConvertedScrapRate(tenantId, scrapDetails, issueDetail, request.getRequestInfo());
						}
						scrapDetail.setScrapQuantity(scrapDetails.getScrapQuantity());
						scrapDetail.setUserQuantity(scrapDetails.getUserQuantity());
						scrapDetail.setScrapReason(scrapDetail.getScrapReason());
						scrapDetail.setScrapValue(scrapDetails.getScrapValue());
						scrapDetail.setScrapReason(scrapDetails.getScrapReason());
						scrapDetailList.add(scrapDetail);

					}
				}
				scrap.setScrapDetails(scrapDetailList);
			}
		}
		if (errors.getValidationErrors().size() > 0)
			throw errors;
	}

	private List<ScrapDetail> getScrapDetails(String scrapNumber, String tenantId) {
		ScrapDetailSearch scrapDetailSearch = ScrapDetailSearch.builder().ScrapNumber(scrapNumber).tenantId(tenantId)
				.build();
		Pagination<ScrapDetail> scrapDetails = scrapDetailJdbcRepository.search(scrapDetailSearch);

		if (!scrapDetails.getPagedData().isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Material> materialMap = getMaterials(tenantId, mapper, new RequestInfo());
			for (ScrapDetail detail : scrapDetails.getPagedData()) {
				Pagination<MaterialIssueDetail> materialIssueDetails = materialIssueDetailsJdbcRepository
						.searchById(detail.getIssueDetail().getId(), tenantId, IssueTypeEnum.NONINDENTISSUE.toString());

				if (!materialIssueDetails.getPagedData().isEmpty()) {
					Map<String, Uom> uoms = getUoms(tenantId, mapper, new RequestInfo());
					for (MaterialIssueDetail details : materialIssueDetails.getPagedData()) {
						Pagination<MaterialIssuedFromReceipt> materialIssuedFromReceipts = materialIssuedFromReceiptsJdbcRepository
								.search(details.getId(), tenantId);
						if (details.getUom() != null && details.getUom().getCode() != null) {
							for (MaterialIssuedFromReceipt mifr : materialIssuedFromReceipts.getPagedData()) {
								BigDecimal quantity = getSearchConvertedQuantity(mifr.getQuantity(),
										uoms.get(details.getUom().getCode()).getConversionFactor());
								mifr.setQuantity(quantity);

								List<MaterialReceiptDetail> materialReceiptDetail = getMaterialReceiptDetail(
										mifr.getMaterialReceiptDetail().getId(), tenantId);

								mifr.setMaterialReceiptDetail(
										materialReceiptDetail.isEmpty() ? mifr.getMaterialReceiptDetail()
												: materialReceiptDetail.get(0));
							}
						}
						details.setMaterialIssuedFromReceipts(materialIssuedFromReceipts.getPagedData());
					}
				}

				detail.issueDetail(materialIssueDetails.getPagedData().isEmpty() ? null
						: materialIssueDetails.getPagedData().get(0));
				detail.setMaterial(materialMap.get(detail.getMaterial().getCode()));
			}
		}
		return !scrapDetails.getPagedData().isEmpty() ? scrapDetails.getPagedData() : Collections.EMPTY_LIST;
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

	private void setConvertedScrapRate(String tenantId, ScrapDetail detail, MaterialIssueDetail issueDetail,
			RequestInfo requestInfo) {
		Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code",
				issueDetail.getUom().getCode(), Uom.class, requestInfo);
		issueDetail.setUom(uom);

		if (null != detail.getScrapValue() && null != uom.getConversionFactor()) {
			BigDecimal convertedRate = getSaveConvertedRate(detail.getScrapValue(), uom.getConversionFactor());
			detail.setScrapValue(convertedRate);
		}

	}

	private void setConvertedScrapQuantity(String tenantId, ScrapDetail detail, MaterialIssueDetail issueDetail,
			RequestInfo requestInfo) {
		InvalidDataException errors = new InvalidDataException();
		Uom uom = (Uom) mdmsRepository.fetchObject(tenantId, "common-masters", "UOM", "code",
				issueDetail.getUom().getCode(), Uom.class, requestInfo);
		issueDetail.setUom(uom);

		if (null != detail.getUserQuantity() && null != uom.getConversionFactor()) {
			BigDecimal convertedUserQuantity = getSaveConvertedQuantity(detail.getUserQuantity(),
					uom.getConversionFactor());
			detail.setScrapQuantity(convertedUserQuantity);
			int res = convertedUserQuantity.compareTo(issueDetail.getQuantityIssued());
			if (res == 1) {
				errors.addDataError(ErrorCode.QTY1_LE_QTY2.getCode(), "Scrap Quantity ", "Issued Quantity ", null);

			}
		}

		if (errors.getValidationErrors().size() > 0)
			throw errors;

	}

	private boolean validateStore(String tenantId, Scrap scrap) {
		StoreGetRequest storeEntity = StoreGetRequest.builder()
				.code(Collections.singletonList(scrap.getStore().getCode())).tenantId(tenantId).active(true).build();
		Pagination<Store> store = storeJdbcRepository.search(storeEntity);
		if (store.getPagedData().size() > 0) {
			return false;
		}
		return true;
	}

	private Store getStore(String tenantId, Scrap scrap) {
		StoreGetRequest storeEntity = StoreGetRequest.builder()
				.code(Collections.singletonList(scrap.getStore().getCode())).tenantId(tenantId).active(true).build();
		Pagination<Store> store = storeJdbcRepository.search(storeEntity);
		if (!store.getPagedData().isEmpty()) {
			return store.getPagedData().get(0);
		}
		return null;
	}

	private Long getCurrentDate() {
		return currentEpochWithoutTime() + (24 * 60 * 60) - 1;
	}

	private void materialIssueBackUpdate(ScrapRequest request, String tenantId) {

		for (Scrap scrap : request.getScraps()) {
			for (ScrapDetail detail : scrap.getScrapDetails()) {
				HashMap<String, String> hashMaps = new HashMap<>();

				hashMaps.put("scrapedquantity", "scrapedquantity + " + detail.getScrapQuantity());

				scrapJdbcRepository.updateColumn(new MaterialIssueDetailEntity(), "materialissuedetail", hashMaps,
						" WHERE id = '" + detail.getIssueDetail().getId() + "'  AND tenantid = '" + tenantId + "'");
				BigDecimal totalScrapedQuantity = detail.getIssueDetail().getScrapedQuantity()
						.add(detail.getScrapQuantity());
				int res = detail.getQuantity().compareTo(totalScrapedQuantity);
				if (res == 0) {
					HashMap<String, String> hashMap = new HashMap<>();
					hashMap.put("scrapcreated", "true");
					scrapJdbcRepository.updateColumn(new MaterialIssueEntity(), "materialissue", hashMap,
							" WHERE issuenumber = (select materialissuenumber from materialissuedetail where id = '"
									+ detail.getIssueDetail().getId() + "' AND tenantId = '" + tenantId + "') "
									+ " AND issuepurpose = 'WRITEOFFORSCRAP' AND tenantid = '" + tenantId + "'");
				}
			}
		}
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
}
