package org.egov.assets.service;

import java.util.HashMap;
import java.util.Map;

import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.model.Material;
import org.egov.assets.model.PurchaseOrderDetail;
import org.egov.assets.model.PurchaseOrderDetailSearch;
import org.egov.assets.repository.PurchaseOrderDetailJdbcRepository;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderDetailService extends DomainService {

	@Autowired
	private PurchaseOrderDetailJdbcRepository purchaseOrderDetailJdbcRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MdmsRepository mdmsRepository;

	public Pagination<PurchaseOrderDetail> search(PurchaseOrderDetailSearch purchaseOrderDetailSearch) {
		Pagination<PurchaseOrderDetail> detailPagination = purchaseOrderDetailJdbcRepository
				.search(purchaseOrderDetailSearch);

		if (!detailPagination.getPagedData().isEmpty()) {
			Map<String, Material> materialMap = getMaterials(purchaseOrderDetailSearch.getTenantId(), mapper,
					new RequestInfo());
			for (PurchaseOrderDetail details : detailPagination.getPagedData()) {
				details.setMaterial(materialMap.get(details.getMaterial().getCode()));
			}
		}
		return detailPagination;

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