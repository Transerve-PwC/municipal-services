package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.ApplicationCriteria;
import org.egov.ps.model.Document;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.model.calculation.Category;
import org.egov.ps.model.calculation.TaxHeadEstimate;
import org.egov.ps.repository.ApplicationRepository;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ApplicationEnrichmentService {

	@Autowired
	private Util util;

	@Autowired
	private MDMSService mdmsService;

	@Autowired
	private IdGenService idGenService;

	@Autowired
	private PropertyRepository propertyRepository;

	@Autowired
	ApplicationRepository applicationRepository;

	@Autowired
	private Configuration config;

	public void enrichCreateApplicationRequest(ApplicationRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		List<Application> applications = request.getApplications();

		if (!CollectionUtils.isEmpty(applications)) {
			applications.forEach(application -> {
				enrichApplication(requestInfo, application);
			});
		}
	}

	public void enrichApplication(RequestInfo requestInfo, Application application) {
		AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		enrichApplicationDetails(application);
		enrichPropertyDetails(application);

		application.setId(UUID.randomUUID().toString());
		application.setAuditDetails(auditDetails);
		application.setApplicationNumber(
				idGenService.getId(requestInfo, application.getTenantId(), config.getApplicationNumberIdgenNamePS()));
	}

	private void enrichApplicationDetails(Application application) {
		JsonNode applicationDetails = application.getApplicationDetails();

		JsonNode transferor = (applicationDetails.get("transferor") != null) ? applicationDetails.get("transferor")
				: applicationDetails.get("owner");

		String propertyId = application.getProperty().getId();
		String transferorId = "";
		if (null != transferor && null != transferor.get("id")) {
			transferorId = transferor.get("id").asText();
		}

		Property property = propertyRepository.findPropertyById(propertyId);
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {
			for (Owner owner : property.getPropertyDetails().getOwners()) {
				if (owner.getId().equals(transferorId)) {
					((ObjectNode) transferor).put("name", owner.getOwnerDetails().getOwnerName());
					((ObjectNode) transferor).put("serialNumber", owner.getSerialNumber());
					((ObjectNode) transferor).put("share", owner.getShare());
					((ObjectNode) transferor).put("cpNumber", owner.getCpNumber());
					((ObjectNode) transferor).set("transferorDetails", owner.getOwnerDetails().copyAsJsonNode());
				}
			}
		}

		if (applicationDetails.get("transferee") != null && applicationDetails.get("transferee").get("id") != null) {
			JsonNode transferee = applicationDetails.get("transferee");
			String transfereeId = "";
			if (null != transferee && null != transferee.get("id")) {
				transferee.get("id").asText();
			}

			if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {
				property.getPropertyDetails().getOwners().forEach(owner -> {
					if (owner.getId().equals(transfereeId)) {
						((ObjectNode) transferee).put("name", owner.getOwnerDetails().getOwnerName());
						((ObjectNode) transferee).put("fatherOrHusbandName", owner.getOwnerDetails().getGuardianName());
						((ObjectNode) transferee).put("relation", owner.getOwnerDetails().getGuardianRelation());
						((ObjectNode) transferee).put("address", owner.getOwnerDetails().getAddress());
						((ObjectNode) transferee).put("relationWithDeceased",
								owner.getOwnerDetails().getGuardianRelation());
						((ObjectNode) transferee).put("mobileNo", owner.getOwnerDetails().getMobileNumber());
					}
				});
			}
		}
		application.setApplicationDetails(applicationDetails);
	}

	private void enrichPropertyDetails(Application application) {
		Property property = propertyRepository.findPropertyById(application.getProperty().getId());
		application.setProperty(property);
	}

	public void enrichUpdateApplication(ApplicationRequest request) {

		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), false);

		if (!CollectionUtils.isEmpty(request.getApplications())) {
			request.getApplications().forEach(application -> {
				AuditDetails modifyAuditDetails = application.getAuditDetails();
				modifyAuditDetails.setLastModifiedBy(auditDetails.getLastModifiedBy());
				modifyAuditDetails.setLastModifiedTime(auditDetails.getLastModifiedTime());
				application.setAuditDetails(modifyAuditDetails);

				List<Document> applicationDocuments = application.getAllDocuments();
				AuditDetails docAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				applicationDocuments.forEach(document -> {
					if (document.getId() == null) {
						document.setId(UUID.randomUUID().toString());
					}
					document.setTenantId(application.getTenantId());
					document.setReferenceId(application.getId());
					document.setPropertyId(application.getProperty().getId());
					document.setAuditDetails(docAuditDetails);
				});
				enrichGenerateDemand(application, request.getRequestInfo());
			});
		}
	}

	private void enrichGenerateDemand(Application application, RequestInfo requestInfo) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		if (application.getState().contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {

			try {
				List<Map<String, Object>> feesConfigurations = mdmsService
						.getApplicationFees(application.getMDMSModuleName(), requestInfo, application.getTenantId());

				BigDecimal estimateAmount = fetchEstimateAmountFromMDMSJson(feesConfigurations, application);
				TaxHeadEstimate estimateDue = new TaxHeadEstimate();
				estimateDue.setEstimateAmount(estimateAmount);
				estimateDue.setCategory(Category.FEE);
				estimateDue.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
				estimates.add(estimateDue);

				TaxHeadEstimate estimateGst = new TaxHeadEstimate();
				BigDecimal gstEstimatePercentage = feesGSTOfApplication(application, requestInfo);
				if (null != gstEstimatePercentage && null != estimateAmount) {
					BigDecimal gstEstimateAmount = (estimateAmount.multiply(gstEstimatePercentage))
							.divide(new BigDecimal(100));
					estimateGst.setEstimateAmount(gstEstimateAmount);
					estimateGst.setCategory(Category.TAX);
					estimateGst.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
							PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.TAX));
					estimates.add(estimateGst);
				}

			} catch (JSONException e) {
				log.error("Can not parse Json file", e);
			}
		}

		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);
	}

	// Used for get feePercentGST
	public BigDecimal feesGSTOfApplication(Application application, RequestInfo requestInfo) {
		BigDecimal responseGSTEstateAmount = new BigDecimal(0.0);
		List<Map<String, Object>> feeGsts;
		try {
			feeGsts = mdmsService.getApplicationGST(application.getMDMSModuleName(), requestInfo,
					application.getTenantId());

			if (!feeGsts.isEmpty()) {
				responseGSTEstateAmount = new BigDecimal(feeGsts.get(0).get("gst").toString());
			}
		} catch (JSONException e) {
			log.error("Can not parse Json fie", e);
		}
		return responseGSTEstateAmount;
	}

	// Used for filter fees by using category and sub-category
	public BigDecimal fetchEstimateAmountFromMDMSJson(List<Map<String, Object>> feesConfigurations,
			Application application) {
		BigDecimal responseEstimateAmount = new BigDecimal(0.0);
		Integer compareVarForEstimateAmount = 0;
		for (Map<String, Object> feesConfig : feesConfigurations) {
			if (application.getProperty().getCategory().equalsIgnoreCase(feesConfig.get("category").toString())) {
				/* Category And Sub-category both meet than directly return a amount for that */
				if (application.getProperty().getSubCategory()
						.equalsIgnoreCase(feesConfig.get("subCategory").toString())) {
					return new BigDecimal(feesConfig.get("amount").toString());
				}
				/*
				 * Main Category is available but no sub-category available than go with
				 * category default amount
				 */
				if ("*".equalsIgnoreCase(feesConfig.get("subCategory").toString())) {
					responseEstimateAmount = new BigDecimal(feesConfig.get("amount").toString());
					compareVarForEstimateAmount++;
				}
			}
		}
		/*
		 * If there is not any equal category and sub-category than estate amount could
		 * be default amount Where Category is : * And Sub-Category is :*
		 */
		if (compareVarForEstimateAmount == 0) {
			List<Map<String, Object>> feesConfigurationsForCommonCatandSubCat = feesConfigurations.stream()
					.filter(feesConfig -> "*".equalsIgnoreCase(feesConfig.get("category").toString()))
					.filter(feesConfig -> "*".equalsIgnoreCase(feesConfig.get("subCategory").toString()))
					.collect(Collectors.toList());
			responseEstimateAmount = !feesConfigurationsForCommonCatandSubCat.isEmpty()
					? new BigDecimal(feesConfigurationsForCommonCatandSubCat.get(0).get("amount").toString())
					: new BigDecimal("0");
		}
		return responseEstimateAmount;
	}

	public String getTaxHeadCodeWithCharge(String billingBusService, String chargeFor, Category category) {
		return String.format("%s_%s_%s", billingBusService, chargeFor, category.toString());
	}

	public void collectPayment(ApplicationRequest applicationRequest) {
		List<Application> applicationsFromDB = applicationRepository
				.getApplications(getApplicationCriteria(applicationRequest));

		Application applicationFromDb = applicationsFromDB.get(0);

		applicationRequest.getApplications().forEach(application -> {
			application.setTenantId(applicationFromDb.getTenantId());
			application.setBranchType(applicationFromDb.getBranchType());
			application.setModuleType(applicationFromDb.getModuleType());
			application.setApplicationType(applicationFromDb.getApplicationType());
			application.setApplicationDetails(applicationFromDb.getApplicationDetails());
			application.setAuditDetails(applicationFromDb.getAuditDetails());

			enrichCollectPaymentDemand(application, applicationRequest.getRequestInfo());
		});
	}

	public ApplicationCriteria getApplicationCriteria(ApplicationRequest request) {
		ApplicationCriteria applicationCriteria = new ApplicationCriteria();
		if (!CollectionUtils.isEmpty(request.getApplications())) {
			request.getApplications().forEach(application -> {
				if (application.getApplicationNumber() != null)
					applicationCriteria.setApplicationNumber(application.getApplicationNumber());
			});
		}
		return applicationCriteria;
	}

	private void enrichCollectPaymentDemand(Application application, RequestInfo requestInfo) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		TaxHeadEstimate estimateFee = new TaxHeadEstimate();
		estimateFee.setEstimateAmount(application.getPaymentAmount());
		estimateFee.setCategory(Category.FEE);
		estimateFee.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
				PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));

		TaxHeadEstimate estimateGst = new TaxHeadEstimate();
		estimateGst.setEstimateAmount(application.getGst());
		estimateGst.setCategory(Category.TAX);
		estimateGst.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
				PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.TAX));

		estimates.add(estimateFee);
		estimates.add(estimateGst);

		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);
	}

}
