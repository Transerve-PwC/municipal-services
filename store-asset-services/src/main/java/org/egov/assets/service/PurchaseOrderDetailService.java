package org.egov.assets.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.model.Indent;
import org.egov.assets.model.IndentDetail;
import org.egov.assets.model.IndentResponse;
import org.egov.assets.model.IndentSearch;
import org.egov.assets.model.Material;
import org.egov.assets.model.PriceListResponse;
import org.egov.assets.model.PriceListSearchRequest;
import org.egov.assets.model.PurchaseIndentDetail;
import org.egov.assets.model.PurchaseOrderDetail;
import org.egov.assets.model.PurchaseOrderDetailSearch;
import org.egov.assets.repository.IndentDetailJdbcRepository;
import org.egov.assets.repository.IndentJdbcRepository;
import org.egov.assets.repository.PurchaseIndentDetailJdbcRepository;
import org.egov.assets.repository.PurchaseOrderDetailJdbcRepository;
import org.egov.assets.repository.entity.PurchaseIndentDetailEntity;
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
	private IndentDetailJdbcRepository indentDetailJdbcRepository;

	@Autowired
	private PurchaseIndentDetailJdbcRepository purchaseIndentDetailJdbcRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MdmsRepository mdmsRepository;


	@Autowired
	private PriceListService priceListService;

	public Pagination<PurchaseOrderDetail> search(PurchaseOrderDetailSearch purchaseOrderDetailSearch) {
		Pagination<PurchaseOrderDetail> detailPagination = purchaseOrderDetailJdbcRepository
				.search(purchaseOrderDetailSearch);

		if (!detailPagination.getPagedData().isEmpty()) {
			Map<String, Material> materialMap = getMaterials(purchaseOrderDetailSearch.getTenantId(), mapper,
					new RequestInfo());
			for (PurchaseOrderDetail details : detailPagination.getPagedData()) {
				details.setMaterial(materialMap.get(details.getMaterial().getCode()));

				PurchaseIndentDetailEntity indentDetailEntity = new PurchaseIndentDetailEntity();
				indentDetailEntity.setPurchaseOrderDetail(details.getId());
				indentDetailEntity.setTenantId(purchaseOrderDetailSearch.getTenantId());
				PurchaseIndentDetail purchaseIndentDetail = purchaseIndentDetailJdbcRepository
						.findByPODetailId(indentDetailEntity);

				if (purchaseIndentDetail != null) {
					IndentDetail indentDetail = indentDetailJdbcRepository.findIndentDetailsId(
							Arrays.asList(purchaseIndentDetail.getIndentDetail().getId()),
							purchaseOrderDetailSearch.getTenantId());
					purchaseIndentDetail.setIndentDetail(indentDetail);
					details.setPurchaseIndentDetails(Arrays.asList(purchaseIndentDetail));
				}

				PriceListSearchRequest priceListSearchRequest = new PriceListSearchRequest();
				priceListSearchRequest.setId(details.getPriceList().getId());
				priceListSearchRequest.setTenantId(purchaseOrderDetailSearch.getTenantId());
				PriceListResponse listResponse = priceListService.search(priceListSearchRequest, new RequestInfo());
				if (!listResponse.getPriceLists().isEmpty()) {
					details.setPriceList(listResponse.getPriceLists().get(0));
				}
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