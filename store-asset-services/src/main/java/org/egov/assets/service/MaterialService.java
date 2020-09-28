package org.egov.assets.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.assets.common.Constants;
import org.egov.assets.common.DomainService;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.common.exception.CustomBindException;
import org.egov.assets.common.exception.ErrorCode;
import org.egov.assets.common.exception.InvalidDataException;
import org.egov.assets.model.Material;
import org.egov.assets.model.MaterialRequest;
import org.egov.assets.model.MaterialResponse;
import org.egov.assets.model.MaterialSearchRequest;
import org.egov.assets.model.MaterialStoreMapping;
import org.egov.assets.model.MaterialStoreMappingRequest;
import org.egov.assets.model.MaterialStoreMappingSearch;
import org.egov.assets.model.MaterialType;
import org.egov.assets.model.StoreMapping;
import org.egov.assets.repository.MaterialJdbcRepository;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONArray;

@Service
public class MaterialService extends DomainService {

	@Autowired
	private MaterialJdbcRepository materialJdbcRepository;

	@Autowired
	private MaterialStoreMappingService materialStoreMappingService;

	@Value("${inv.materials.save.topic}")
	private String saveTopic;

	@Value("${inv.materials.save.key}")
	private String saveKey;

	@Value("${inv.materials.update.topic}")
	private String updateTopic;

	@Value("${inv.materials.update.key}")
	private String updateKey;

	@Autowired
	private MdmsRepository mdmsRepository;

	public static final String SEQ_SERIAL_NO = "seq_material_code_serial_no";

	public MaterialResponse save(MaterialRequest materialRequest, String tenantId) {

		try {
			List<MaterialStoreMapping> materialStoreMappings = new ArrayList<>();

			validate(materialRequest.getMaterials(), Constants.ACTION_CREATE, materialRequest.getRequestInfo());

			List<String> materialIdList = materialJdbcRepository.getSequence(Material.class.getSimpleName(),
					materialRequest.getMaterials().size());

			for (int i = 0; i <= materialIdList.size() - 1; i++) {
				materialRequest.getMaterials().get(i).setId(materialIdList.get(i).toString());
				if (isEmpty(materialRequest.getMaterials().get(i).getTenantId())) {
					materialRequest.getMaterials().get(i).setTenantId(tenantId);
				}
				materialRequest.getMaterials().get(i)
						.setAuditDetails(mapAuditDetails(materialRequest.getRequestInfo()));
				materialStoreMappings = buildMaterialStoreMapping(materialRequest.getMaterials().get(i).getCode(),
						materialRequest.getMaterials().get(i).getStoreMapping());
			}

			kafkaQue.send(saveTopic, saveKey, materialRequest);

			materialStoreMappingService.create(
					buildMaterialStoreRequest(materialRequest.getRequestInfo(), materialStoreMappings), tenantId);

			return MaterialResponse.builder().materials(materialRequest.getMaterials())
					.responseInfo(getResponseInfo(materialRequest.getRequestInfo())).build();

		} catch (CustomBindException e) {
			throw new CustomException("MATERIAL_MASTER_EXCEPTION", e.getMessage());
		}
	}

	public MaterialResponse update(MaterialRequest materialRequest, String tenantId) {
		try {
			List<MaterialStoreMapping> materialStoreMappings = new ArrayList<>();
			validate(materialRequest.getMaterials(), Constants.ACTION_UPDATE, materialRequest.getRequestInfo());
			for (Material material : materialRequest.getMaterials()) {
				if (isEmpty(material.getTenantId())) {
					material.setTenantId(tenantId);
				}
				material.setAuditDetails(mapAuditDetails(materialRequest.getRequestInfo()));
				materialStoreMappings = buildMaterialStoreMapping(material.getCode(), material.getStoreMapping());
			}

			kafkaQue.send(saveTopic, saveKey, materialRequest);

			materialStoreMappingService.update(
					buildMaterialStoreRequest(materialRequest.getRequestInfo(), materialStoreMappings), tenantId);

			MaterialResponse response = new MaterialResponse();
			response.setResponseInfo(getResponseInfo(materialRequest.getRequestInfo()));
			response.setMaterials(materialRequest.getMaterials());
			return response;

		} catch (CustomBindException e) {
			throw new CustomException("MATERIAL_MASTER_EXCEPTION", e.getMessage());
		}
	}

	public MaterialResponse search(MaterialSearchRequest materialSearchRequest, RequestInfo requestInfo) {

		MaterialResponse response = new MaterialResponse();

		List<Material> materials = new ArrayList<>();

		List<Material> materialFromMdms = getMaterialFromMdms(materialSearchRequest.getTenantId(),
				materialSearchRequest.getCode(), requestInfo);

		if (!materialFromMdms.isEmpty()) {
			for (Material material : materialFromMdms) {
				MaterialSearchRequest request = MaterialSearchRequest.builder().code(material.getCode())
						.materialType(materialSearchRequest.getMaterialType()).store(materialSearchRequest.getStore())
						.tenantId(materialSearchRequest.getTenantId()).build();

				Pagination<Material> materialFromDb = materialJdbcRepository.search(request);

				if (!materialFromDb.getPagedData().isEmpty()) {

					for (Material materialDb : materialFromDb.getPagedData()) {

						prepareMaterial(materialDb, material);

						if (materialDb.getMaterialType().getCode() != null) {
							MaterialType materialType = (MaterialType) mdmsRepository.fetchObject(
									materialSearchRequest.getTenantId(), "store-asset", "MaterialType", "code",
									materialDb.getMaterialType().getCode(), MaterialType.class, requestInfo);
							materialDb.setMaterialType(
									materialType == null ? materialDb.getMaterialType() : materialType);
						}

						List<StoreMapping> storeMappings = new ArrayList<>();
						MaterialStoreMappingSearch materialStoreMappingSearch = MaterialStoreMappingSearch.builder()
								.material(material.getCode()).tenantId(materialSearchRequest.getTenantId()).build();

						List<MaterialStoreMapping> materialStoreMappings = materialStoreMappingService
								.search(materialStoreMappingSearch, requestInfo).getMaterialStoreMappings();

						materialStoreMappings.forEach(materialStoreMapping -> {
							StoreMapping storeMapping = StoreMapping.builder().id(materialStoreMapping.getId())
									.chartofAccount(materialStoreMapping.getChartofAccount())
									.active(materialStoreMapping.getActive()).store(materialStoreMapping.getStore())
									.auditDetails(materialStoreMapping.getAuditDetails()).build();
							storeMappings.add(storeMapping);
						});
						materialDb.setStoreMapping(storeMappings);
						materials.add(materialDb);

					}
				} else {
					material.setStoreMapping(Collections.EMPTY_LIST);
					//materials.add(material);
				}
			}
		}
		response.setMaterials(materials);
		return response;
	}

	public Material fetchMaterial(final String tenantId, final String code, final RequestInfo requestInfo) {

		JSONArray responseJSONArray;
		final ObjectMapper mapper = new ObjectMapper();

		responseJSONArray = mdmsRepository.getByCriteria(tenantId, "store-asset", "Material", "code", code,
				requestInfo);

		if (responseJSONArray != null && responseJSONArray.size() > 0)
			return mapper.convertValue(responseJSONArray.get(0), Material.class);
		else
			throw new CustomException("Material", "Given Material is invalid: " + code);

	}

	private void validate(List<Material> materials, String method, RequestInfo requestInfo) {

		try {
			switch (method) {

			case Constants.ACTION_CREATE: {
				if (materials == null) {
					throw new InvalidDataException("materialstore", ErrorCode.NOT_NULL.getCode(), null);
				} else {
					materials.forEach(material -> {
						minmaxvalidate(material);
						validateMaterial(material.getCode(), material.getTenantId(), requestInfo);
					});
				}
			}

				break;

			case Constants.ACTION_UPDATE: {
				if (materials == null) {
					throw new InvalidDataException("materialstore", ErrorCode.NOT_NULL.getCode(), null);
				} else {
					materials.forEach(material -> {
						minmaxvalidate(material);
					});
				}
			}

				break;
			}
		} catch (IllegalArgumentException e) {

		}

	}

	private List<MaterialStoreMapping> buildMaterialStoreMapping(String materialCode,
			List<StoreMapping> storeMappings) {
		List<MaterialStoreMapping> materialStoreMappings = new ArrayList<>();

		storeMappings.stream().forEach(storeMapping -> {
			MaterialStoreMapping materialStoreMapping = MaterialStoreMapping.builder().id(storeMapping.getId())
					.material(buildMaterial(materialCode)).store(storeMapping.getStore())
					.chartofAccount(storeMapping.getChartofAccount()).active(storeMapping.getActive())
					.auditDetails(storeMapping.getAuditDetails()).delete(storeMapping.getDelete()).build();

			materialStoreMappings.add(materialStoreMapping);
		});
		return materialStoreMappings;
	}

	private Material buildMaterial(String materialCode) {
		return Material.builder().code(materialCode).build();
	}

	private MaterialStoreMappingRequest buildMaterialStoreRequest(RequestInfo requestInfo,
			List<MaterialStoreMapping> materialStoreMappings) {
		return MaterialStoreMappingRequest.builder().requestInfo(requestInfo)
				.materialStoreMappings(materialStoreMappings).build();
	}

	private void minmaxvalidate(Material material) {
		if (material.getMaxQuantity().compareTo(material.getMinQuantity()) < 0) {
			throw new CustomException("inv.0013", "maximum quantity should be greater than minimum quantity");
		}
	}

	private List<Material> getMaterialFromMdms(String tenantId, String code, RequestInfo requestInfo) {

		List<Object> objectList;

		if (!StringUtils.isEmpty(code)) {
			objectList = mdmsRepository.fetchObjectList(tenantId, "store-asset", "Material", "code", code,
					Material.class, requestInfo);
		} else {
			objectList = mdmsRepository.fetchObjectList(tenantId, "store-asset", "Material", null, null, Material.class,
					requestInfo);
		}

		List<Material> materials = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		if (objectList != null && objectList.size() > 0) {
			for (Object object : objectList) {
				Material material = mapper.convertValue(object, Material.class);
				materials.add(material);
			}
		}
		return materials;
	}

	private void prepareMaterial(Material material, Material mdmsMaterial) {
		material.baseUom(mdmsMaterial.getBaseUom()).purchaseUom(mdmsMaterial.getPurchaseUom())
				.stockingUom(mdmsMaterial.getStockingUom()).name(mdmsMaterial.getName())
				.oldCode(mdmsMaterial.getOldCode()).assetCategory(mdmsMaterial.getAssetCategory())
				.description(mdmsMaterial.getDescription()).expenseAccount(mdmsMaterial.getExpenseAccount())
				.inActiveDate(mdmsMaterial.getInActiveDate()).inventoryType(mdmsMaterial.getInventoryType())
				.lotControl(mdmsMaterial.getLotControl()).materialClass(mdmsMaterial.getMaterialClass())
				.materialType(mdmsMaterial.getMaterialType()).scrapable(mdmsMaterial.getScrapable())
				.serialNumber(mdmsMaterial.getSerialNumber()).shelfLifeControl(mdmsMaterial.getShelfLifeControl())
				.status(mdmsMaterial.getStatus()).tenantId(mdmsMaterial.getTenantId());

		/*
		 * material.baseUom(mdmsMaterial.getBaseUom()).purchaseUom(mdmsMaterial.
		 * getPurchaseUom())
		 * .stockingUom(mdmsMaterial.getStockingUom()).name(mdmsMaterial.getName())
		 * .oldCode(mdmsMaterial.getOldCode()).assetCategory(mdmsMaterial.
		 * getAssetCategory())
		 * .description(mdmsMaterial.getDescription()).expenseAccount(mdmsMaterial.
		 * getExpenseAccount())
		 * .inActiveDate(mdmsMaterial.getInActiveDate()).inventoryType(mdmsMaterial.
		 * getInventoryType())
		 * .lotControl(mdmsMaterial.getLotControl()).manufacturePartNo(mdmsMaterial.
		 * getManufacturePartNo())
		 * .materialClass(mdmsMaterial.getMaterialClass()).materialType(mdmsMaterial.
		 * getMaterialType())
		 * .scrapable(mdmsMaterial.getScrapable()).serialNumber(mdmsMaterial.
		 * getSerialNumber())
		 * .shelfLifeControl(mdmsMaterial.getShelfLifeControl()).model(mdmsMaterial.
		 * getModel()) .status(mdmsMaterial.getStatus()).techincalSpecs(mdmsMaterial.
		 * getTechincalSpecs())
		 * .termsOfDelivery(mdmsMaterial.getTermsOfDelivery()).tenantId(mdmsMaterial.
		 * getTenantId());
		 */
	}

	private void validateMaterial(String code, String tenantId, RequestInfo requestInfo) {
		Material material = (Material) mdmsRepository.fetchObject(tenantId, "store-asset", "Material", "code", code,
				Material.class, requestInfo);
		if (null == material) {
			throw new CustomException("inv.0014", "Material not found with Code" + code);

		}
	}

}
