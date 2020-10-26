package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.Document;
import org.egov.ps.model.MortgageDetails;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.model.calculation.Calculation;
import org.egov.ps.model.calculation.Category;
import org.egov.ps.model.calculation.TaxHeadEstimate;
import org.egov.ps.model.idgen.IdResponse;
import org.egov.ps.repository.IdGenRepository;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.ApplicationRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ApplicationEnrichmentService {

	@Autowired
	Util util;

	@Autowired
	IdGenRepository idGenRepository;

	@Autowired
	private Configuration config;

	@Autowired
	private MDMSService mdmsservice;

	@Autowired
	private PropertyRepository propertyRepository;

	@Autowired
	private ObjectMapper objectMapper;

	MDMSService mdmsService;

	/**
	 * Application Related Enrich
	 */

	public void enrichCreateApplication(ApplicationRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		List<Application> applications = request.getApplications();

		if (!CollectionUtils.isEmpty(applications)) {
			applications.forEach(application -> {
				String gen_application_id = UUID.randomUUID().toString();
				AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				JsonNode applicationDetails = enrichApplicationDetails(application);
				enrichPropertyDetails(application);

				application.setId(gen_application_id);
				application.setAuditDetails(auditDetails);
				application.setApplicationDetails(applicationDetails);
			});
			setIdgenIds(request);
		}
	}

	private JsonNode enrichApplicationDetails(Application application) {
		JsonNode applicationDetails = application.getApplicationDetails();

		JsonNode transferor = (applicationDetails.get("transferor") != null) ? applicationDetails.get("transferor")
				: applicationDetails.get("owner");

		String propertyId = application.getProperty().getId();
		String transferorId = transferor.get("id").asText();

		Property property = propertyRepository.findPropertyById(propertyId);
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {
			property.getPropertyDetails().getOwners().forEach(owner -> {
				if (owner.getId().equals(transferorId)) {
					((ObjectNode) transferor).put("serialNumber", owner.getSerialNumber());
					((ObjectNode) transferor).put("share", owner.getShare());
					((ObjectNode) transferor).put("cpNumber", owner.getCpNumber());

					ObjectNode ownerDetails = objectMapper.createObjectNode();
					ownerDetails.put("ownerName", owner.getOwnerDetails().getOwnerName());
					ownerDetails.put("guardianName", owner.getOwnerDetails().getGuardianName());
					ownerDetails.put("guardianRelation", owner.getOwnerDetails().getGuardianRelation());
					ownerDetails.put("mobileNumber", owner.getOwnerDetails().getMobileNumber());
					ownerDetails.put("allotmentNumber", owner.getOwnerDetails().getAllotmentNumber());
					ownerDetails.put("dateOfAllotment", owner.getOwnerDetails().getDateOfAllotment());
					ownerDetails.put("possesionDate", owner.getOwnerDetails().getPossesionDate());
					ownerDetails.put("isApproved", owner.getOwnerDetails().getIsApproved());
					ownerDetails.put("isCurrentOwner", owner.getOwnerDetails().getIsCurrentOwner());
					ownerDetails.put("isMasterEntry", owner.getOwnerDetails().getIsMasterEntry());
					ownerDetails.put("address", owner.getOwnerDetails().getAddress());

					((ObjectNode) transferor).set("transferorDetails", ownerDetails);
				}
			});
		}

		if (applicationDetails.get("transferee") != null && applicationDetails.get("transferee").get("id") != null) {
			JsonNode transferee = applicationDetails.get("transferee");
			String transfereeId = transferee.get("id").asText();

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
		return applicationDetails;
	}

	private void enrichPropertyDetails(Application application) {
		Property property = propertyRepository.findPropertyById(application.getProperty().getId());
		Property propertyToEnrich = application.getProperty();

		propertyToEnrich.setFileNumber(property.getFileNumber());
		propertyToEnrich.setTenantId(property.getTenantId());
		propertyToEnrich.setCategory(property.getCategory());
		propertyToEnrich.setSubCategory(property.getSubCategory());
		propertyToEnrich.setSiteNumber(property.getSiteNumber());
		propertyToEnrich.setSectorNumber(property.getSectorNumber());

		PropertyDetails propertyDetails = new PropertyDetails();
		propertyDetails.setBranchType(property.getPropertyDetails().getBranchType());
		propertyDetails.setPropertyType(property.getPropertyDetails().getBranchType());
		propertyDetails.setTypeOfAllocation(property.getPropertyDetails().getBranchType());
		propertyDetails.setEmdAmount(property.getPropertyDetails().getEmdAmount());
		propertyDetails.setEmdDate(property.getPropertyDetails().getEmdDate());
		propertyDetails.setModeOfAuction(property.getPropertyDetails().getModeOfAuction());
		propertyDetails.setSchemeName(property.getPropertyDetails().getSchemeName());
		propertyDetails.setDateOfAuction(property.getPropertyDetails().getDateOfAuction());
		propertyDetails.setAreaSqft(property.getPropertyDetails().getAreaSqft());
		propertyDetails.setRatePerSqft(property.getPropertyDetails().getRatePerSqft());
		propertyDetails.setLastNocDate(property.getPropertyDetails().getLastNocDate());
		propertyDetails.setServiceCategory(property.getPropertyDetails().getServiceCategory());
		propertyDetails.setIsPropertyActive(property.getPropertyDetails().getIsPropertyActive());
		propertyDetails.setTradeType(property.getPropertyDetails().getTradeType());
		propertyDetails.setCompanyName(property.getPropertyDetails().getCompanyName());
		propertyDetails.setCompanyAddress(property.getPropertyDetails().getCompanyAddress());
		propertyDetails.setCompanyRegistrationNumber(property.getPropertyDetails().getCompanyRegistrationNumber());
		propertyDetails.setCompanyType(property.getPropertyDetails().getCompanyType());

		propertyToEnrich.setPropertyDetails(propertyDetails);
	}

	/**
	 * Returns a list of numbers generated from idgen
	 *
	 * @param requestInfo RequestInfo from the request
	 * @param tenantId    tenantId of the city
	 * @param idKey       code of the field defined in application properties for
	 *                    which ids are generated for
	 * @param idformat    format in which ids are to be generated
	 * @param count       Number of ids to be generated
	 * @return List of ids generated using idGen service
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, count).getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * Sets the ApplicationNumber for given EstateServiceApplicationRequest
	 *
	 * @param request EstateServiceApplicationRequest which is to be created
	 */
	private void setIdgenIds(ApplicationRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getApplications().get(0).getTenantId();
		List<Application> applications = request.getApplications();
		int size = request.getApplications().size();

		List<String> applicationNumbers = setIdgenIds(requestInfo, tenantId, size,
				config.getApplicationNumberIdgenNamePS());
		ListIterator<String> itr = applicationNumbers.listIterator();

		if (!CollectionUtils.isEmpty(applications)) {
			applications.forEach(application -> {
				application.setApplicationNumber(itr.next());
			});
		}
	}

	private List<String> setIdgenIds(RequestInfo requestInfo, String tenantId, int size, String idGenName) {
		List<String> applicationNumbers = null;

		applicationNumbers = getIdList(requestInfo, tenantId, idGenName, size);

		Map<String, String> errorMap = new HashMap<>();
		if (applicationNumbers.size() != size) {
			errorMap.put("IDGEN ERROR ",
					"The number of application number returned by idgen is not equal to number of Applications");
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return applicationNumbers;
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
						document.setTenantId(application.getTenantId());
						document.setReferenceId(application.getId());
						document.setPropertyId(application.getProperty().getId());
						document.setAuditDetails(docAuditDetails);
					}
				});
				enrichGenerateDemand(application, request.getRequestInfo());
			});
		}
	}

	public void enrichMortgageDetailsRequest(PropertyRequest request) {
		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {
				if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {
					property.getPropertyDetails().getOwners().forEach(owner -> {
						// checking - owner is existing and mortgage details bound with user.
						if (null != owner.getId() && !owner.getId().isEmpty() && null != owner.getMortgageDetails()) {
							// validate mortgage details - documents
							validateMortgageDetails(property, owner, request.getRequestInfo(), owner.getId());
							owner.setMortgageDetails(
									getMortgage(property, owner, request.getRequestInfo(), owner.getId()));
						}
					});
				}
			});
		}
	}

	public void validateMortgageDetails(Property property, Owner owner, RequestInfo requestInfo, String id) {
		MortgageDetails mortgage = owner.getMortgageDetails();
		List<Map<String, Object>> fieldConfigurations = mdmsservice
				.getMortgageDocumentConfig(mortgage.getMortgageType(), requestInfo, property.getTenantId());

		// TODO :: write code to validate documents base on master json template.
	}

	private MortgageDetails getMortgage(Property property, Owner owner, RequestInfo requestInfo, String gen_owner_id) {
		String gen_mortgage_id = UUID.randomUUID().toString();

		MortgageDetails mortgage = owner.getMortgageDetails();
		mortgage.setId(gen_mortgage_id);
		mortgage.setTenantId(property.getTenantId());
		mortgage.setOwnerId(gen_owner_id);

		return mortgage;
	}

	private void enrichGenerateDemand(Application application, RequestInfo requestInfo) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		
		if (application.getState().contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {
			
			// TODO : We have to fetch the data from MDMS
			// Master name -> module name
			List<Map<String, Object>> feesConfigurations = mdmsservice.getApplicationFees(application.getMDMSModuleName(),
					requestInfo, application.getTenantId());	
		

			TaxHeadEstimate estimateDue = new TaxHeadEstimate();
			
			// TODO : replace 500 from the MDMS data , get it dynamic based on application
			// cat and sub cat provided by FE
			BigDecimal estimateAmount = fetchEstimateAmountFromMDMSJson(feesConfigurations,application);
			BigDecimal gstEstimateAmount = estimateAmount.multiply( 
					feesGSTOfApplication(application,requestInfo)).divide(new BigDecimal(100.0));
			estimateDue.setEstimateAmount(estimateAmount.add(gstEstimateAmount));
			estimateDue.setCategory(Category.FEE);
			estimateDue.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
			estimates.add(estimateDue);
		}
				
		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);
	}
	
	// Used for get feePercentGST 
	public BigDecimal feesGSTOfApplication(Application application, RequestInfo requestInfo) {
		BigDecimal responseGSTEstateAmount = new BigDecimal(0.0);
		List<Map<String, Object>> feeGsts = mdmsservice.getApplicationGST(application.getMDMSModuleName(),
				requestInfo, application.getTenantId());
		if(!feeGsts.isEmpty()) {
			responseGSTEstateAmount = new BigDecimal(feeGsts.get(0).get("gst").toString());
		}
		return responseGSTEstateAmount;
	}
	
	// Used for filter fees by using category and sub-category  
	public BigDecimal fetchEstimateAmountFromMDMSJson(List<Map<String, Object>> feesConfigurations, Application application) {
		BigDecimal responseEstimateAmount = new BigDecimal(0.0);
		Integer compareVarForEstimateAmount = 0;
		for(Map<String, Object> feesConfig : feesConfigurations) {
			if(application.getProperty().getCategory().equalsIgnoreCase(feesConfig.get("category").toString())) {
				/* Category And Sub-category both meet than directly return a amount for that */
				if(application.getProperty().getSubCategory().equalsIgnoreCase(feesConfig.get("subCategory").toString())) {
					return new BigDecimal(feesConfig.get("amount").toString());
				}
				/* Main Category is available but no sub-category available than go with category default amount*/
				if("*".equalsIgnoreCase(feesConfig.get("subCategory").toString())) {					
					responseEstimateAmount = new BigDecimal(feesConfig.get("amount").toString());
					compareVarForEstimateAmount++;
				}				
			}
		}
		/* If there is not any equal category and sub-category than estate amount could be default amount 
		 * Where Category is : * 
		 * And Sub-Category is :* */
		if(compareVarForEstimateAmount == 0) {		
			List<Map<String, Object>> feesConfigurationsForCommonCatandSubCat = feesConfigurations.stream()
					  .filter(feesConfig -> "*".equalsIgnoreCase(feesConfig.get("category").toString()))
					  .filter(feesConfig -> "*".equalsIgnoreCase(feesConfig.get("subCategory").toString()))
					  .collect(Collectors.toList());
			responseEstimateAmount = !feesConfigurationsForCommonCatandSubCat.isEmpty() ? 
					new BigDecimal(feesConfigurationsForCommonCatandSubCat.get(0).get("amount").toString()) : new BigDecimal("0") ;			
		}
		return responseEstimateAmount;
	}

	// To be used in future
	private void enrichUpdateDemand(Application application) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		if (application.getAction().equalsIgnoreCase(PSConstants.EM_ACTION_APPROVE)) {

			TaxHeadEstimate estimateDue = new TaxHeadEstimate();
			estimateDue.setEstimateAmount(new BigDecimal(500.00));
			estimateDue.setCategory(Category.FEE);
			estimateDue.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
			estimates.add(estimateDue);
		}
		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);
	}

	private String getTaxHeadCodeWithCharge(String billingBusService, String chargeFor, Category category) {
		return String.format("%s_%s_%s", billingBusService, chargeFor, category.toString());
	}

}
