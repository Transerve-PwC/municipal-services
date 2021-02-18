package org.egov.assets.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.SupplierRepository;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialBalanceRate;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceiptDetail;
import org.egov.assets.model.MaterialReceiptDetailSearch;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.model.PurchaseOrderDetail;
import org.egov.assets.model.PurchaseOrderDetailSearch;
import org.egov.assets.model.Store;
import org.egov.assets.model.StoreGetRequest;
import org.egov.assets.model.StoreResponse;
import org.egov.assets.model.Supplier;
import org.egov.assets.model.Uom;
import org.egov.assets.repository.MaterialReceiptJdbcRepository;
import org.egov.assets.repository.StoreJdbcRepository;
import org.egov.common.contract.request.RequestInfo;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MaterialReceiptService extends DomainService {

	@Autowired
	private MaterialReceiptJdbcRepository materialReceiptJdbcRepository;

	@Autowired
	private MaterialReceiptDetailService materialReceiptDetailService;

	@Autowired
	private StoreJdbcRepository storeJdbcRepository;

	@Autowired
	private MdmsRepository mdmsRepository;

	@Autowired
	private PurchaseOrderDetailService purchaseOrderDetailService;

	@Autowired
	private MaterialService materialService;

	@Autowired
	private SupplierRepository supplierRepository;

	@Autowired
	private StoreService storeService;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public Pagination<MaterialReceipt> search(MaterialReceiptSearch materialReceiptSearch) {
		Pagination<MaterialReceipt> materialReceiptPagination = materialReceiptJdbcRepository
				.search(materialReceiptSearch);

		if (!materialReceiptPagination.getPagedData().isEmpty()) {
			for (MaterialReceipt materialReceipt : materialReceiptPagination.getPagedData()) {
				materialReceipt.receivingStore(
						getStore(materialReceiptSearch.getTenantId(), materialReceipt.getReceivingStore().getCode()));

				if (materialReceipt.getIssueingStore() != null)
					materialReceipt.issueingStore(getStore(materialReceiptSearch.getTenantId(),
							materialReceipt.getIssueingStore().getCode()));

				
				if (materialReceipt.getSupplier() != null) {
					Supplier supplier = getSupplier(materialReceipt.getSupplier().getCode(), materialReceiptSearch.getTenantId());
					if(supplier!=null)
						materialReceipt.setSupplier(supplier);
				}
				
				List<MaterialReceiptDetail> materialReceiptDetail = getMaterialReceiptDetail(
						materialReceipt.getMrnNumber(), materialReceiptSearch.getTenantId());
				materialReceipt.setReceiptDetails(materialReceiptDetail);
			}
		}
		return materialReceiptPagination;
	}
	public JSONArray searchStock(MaterialReceiptSearch materialReceiptSearch) {
		JSONArray materialReceiptPagination = materialReceiptJdbcRepository
				.searchStock(materialReceiptSearch);

		
		return materialReceiptPagination;
	}
	
	
	
	public JSONArray getInventoryReport(MaterialReceiptSearch materialReceiptSearch) {
		return materialReceiptJdbcRepository.getInventoryReport(materialReceiptSearch);
	}

	private Supplier getSupplier(String code, String tenantId) {
		return supplierRepository.getByCode(code);
	}

	private Store getStore(String tenantId, String code) {
		StoreGetRequest storeGetRequest = StoreGetRequest.builder().code(Collections.singletonList(code))
				.tenantId(tenantId).active(true).build();
		StoreResponse search = storeService.search(storeGetRequest);
		if (!search.getStores().isEmpty()) {
			return search.getStores().get(0);
		}
		return null;
	}

	public Pagination<MaterialBalanceRate> searchBalanceRate(MaterialReceiptSearch materialReceiptSearch) {
		return materialReceiptJdbcRepository.searchBalanceRate(materialReceiptSearch);
	}

	private List<MaterialReceiptDetail> getMaterialReceiptDetail(String mrnNumber, String tenantId) {
		MaterialReceiptDetailSearch materialReceiptDetailSearch = MaterialReceiptDetailSearch.builder()
				.mrnNumber(Arrays.asList(mrnNumber)).tenantId(tenantId).build();
		Pagination<MaterialReceiptDetail> materialReceiptDetails = materialReceiptDetailService
				.search(materialReceiptDetailSearch);

		if (!materialReceiptDetails.getPagedData().isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Material> materialMap = getMaterials(tenantId, mapper, new RequestInfo());
			Map<String, Uom> uoms = getUoms(tenantId, mapper, new RequestInfo());

			for (MaterialReceiptDetail detail : materialReceiptDetails.getPagedData()) {

				if (detail.getPurchaseOrderDetail() != null && detail.getPurchaseOrderDetail().getId() != null) {
					PurchaseOrderDetailSearch purchaseOrderDetailSearch = new PurchaseOrderDetailSearch();
					purchaseOrderDetailSearch.setIds(Arrays.asList(detail.getPurchaseOrderDetail().getId()));
					purchaseOrderDetailSearch.setTenantId(tenantId);
					Pagination<PurchaseOrderDetail> detailPagination = purchaseOrderDetailService
							.search(purchaseOrderDetailSearch);

					detail.setPurchaseOrderDetail(
							!detailPagination.getPagedData().isEmpty() ? detailPagination.getPagedData().get(0) : null);

				}

				detail.setMaterial(materialMap.get(detail.getMaterial().getCode()));
				detail.setUom(uoms.get(detail.getUom().getCode()));
			}
		}

		return !materialReceiptDetails.getPagedData().isEmpty() ? materialReceiptDetails.getPagedData()
				: Collections.EMPTY_LIST;
	}

	private Map<String, Material> getMaterials(String tenantId, final ObjectMapper mapper, RequestInfo requestInfo) {
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

	public void updatePOdetailIdAgainstMaterials(List<MaterialReceiptDetail> materialReceiptDetails, String tenantId) {
		for (MaterialReceiptDetail detail : materialReceiptDetails) {
			Map<String, Object> paramValuesqueryMat = new HashMap<>();
			String queryMat = "update materialreceiptdetail set podetailid =:podetailid where id = :id and tenantid = :tenantId";
			paramValuesqueryMat.put("podetailid", detail.getPurchaseOrderDetail().getId());
			paramValuesqueryMat.put("id", detail.getId());
			paramValuesqueryMat.put("tenantId", tenantId);
			namedParameterJdbcTemplate.update(queryMat, paramValuesqueryMat);
		}
	}
}