package org.egov.ps.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.egov.ps.model.PropertyCriteria;
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
import com.fasterxml.jackson.databind.ObjectMapper;
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

		if (application.getBranchType().equalsIgnoreCase(PSConstants.APPLICATION_BUILDING_BRANCH)
				&& application.getApplicationType().equalsIgnoreCase(PSConstants.NOC)
				&& application.getProperty() == null) {
			final ObjectMapper mapper = new ObjectMapper();
			final ObjectNode transferorDetails = mapper.createObjectNode();

			transferorDetails.put("ownerName", applicationDetails.get("owner").get("name"));
			transferorDetails.put("mobileNumber", applicationDetails.get("owner").get("ownerDetails").get("mobileNumber"));
			((ObjectNode) transferor).set("transferorDetails", transferorDetails);
		}else {
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
		}
		application.setApplicationDetails(applicationDetails);
	}

	private void enrichPropertyDetails(Application application) {
		Property property=null;
		if (application.getBranchType().equalsIgnoreCase(PSConstants.APPLICATION_BUILDING_BRANCH)
				&& application.getApplicationType().equalsIgnoreCase(PSConstants.NOC)
				&& application.getProperty() == null) {
			property = propertyRepository.fetchDummyProperty(
					PropertyCriteria.builder().fileNumber(PSConstants.BB_NOC_DUMMY_FILENO).limit(1l).build());
		}else {

			property = propertyRepository.findPropertyById(application.getProperty().getId());
		}
		application.setProperty(property);
	}

	public void enrichUpdateApplication(ApplicationRequest request) {

		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), false);

		if (!CollectionUtils.isEmpty(request.getApplications())) {
			request.getApplications().forEach(application -> {
				if (!(application.getBranchType().equalsIgnoreCase(PSConstants.APPLICATION_BUILDING_BRANCH)
						&& application.getApplicationType().equalsIgnoreCase(PSConstants.NOC) && application
						.getProperty().getFileNumber().equalsIgnoreCase(PSConstants.BB_NOC_DUMMY_FILENO))) {
					enrichApplicationDetails(application);
				}
				AuditDetails modifyAuditDetails = application.getAuditDetails();
				modifyAuditDetails.setLastModifiedBy(auditDetails.getLastModifiedBy());
				modifyAuditDetails.setLastModifiedTime(auditDetails.getLastModifiedTime());
				application.setAuditDetails(modifyAuditDetails);

				if (application.getAction().equalsIgnoreCase(PSConstants.ACTION_SUBMIT)
						&& application.getState().equalsIgnoreCase(""))
					application.setApplicationSubmissionDate(auditDetails.getLastModifiedTime());

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

		if (application.getBranchType().contentEquals(PSConstants.APPLICATION_BUILDING_BRANCH)
				&& application.getApplicationType().contentEquals(PSConstants.NOC)) {

			JsonNode applicationDetails = application.getApplicationDetails();

			if (application.getState().contains(PSConstants.BB_NOC_PENDING_JE_VERIFICATION)) {

				BigDecimal developmentCharges = calculateDevelopmentCharges(applicationDetails);
				((ObjectNode) applicationDetails).put("developmentCharges", developmentCharges);

				BigDecimal conversionCharges = calculateConversionCharges(applicationDetails);
				((ObjectNode) applicationDetails).put("conversionCharges", conversionCharges);

			}

			if (application.getState().contains(PSConstants.BB_NOC_PENDING_AC_APPROVAL)) {

				// Development Charges
				BigDecimal developmentCharges = new BigDecimal(applicationDetails.get("developmentCharges").toString());
				TaxHeadEstimate developmentChargesEstimate = new TaxHeadEstimate();
				developmentChargesEstimate.setEstimateAmount(developmentCharges);
				developmentChargesEstimate.setCategory(Category.CHARGES);
				developmentChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "DEVELOPMENT", Category.CHARGES));
				estimates.add(developmentChargesEstimate);

				// Conversion charges
				BigDecimal conversionCharges = new BigDecimal(applicationDetails.get("conversionCharges").toString());
				TaxHeadEstimate conversionChargesEstimate = new TaxHeadEstimate();
				conversionChargesEstimate.setEstimateAmount(conversionCharges);
				conversionChargesEstimate.setCategory(Category.CHARGES);
				conversionChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "CONVERSION", Category.CHARGES));
				estimates.add(conversionChargesEstimate);

				// Scrutiny charges
				BigDecimal scrutinyCharges = BigDecimal.ZERO;
				if (null != applicationDetails.get("scrutinyCharges")) {
					scrutinyCharges = new BigDecimal(applicationDetails.get("scrutinyCharges").asText());
				}
				TaxHeadEstimate scrutinyChargesEstimate = new TaxHeadEstimate();
				scrutinyChargesEstimate.setEstimateAmount(scrutinyCharges.setScale(0, RoundingMode.HALF_UP));
				scrutinyChargesEstimate.setCategory(Category.CHARGES);
				scrutinyChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "SCRUTINY", Category.CHARGES));
				estimates.add(scrutinyChargesEstimate);

				// Transfer fees
				BigDecimal transferFee = BigDecimal.ZERO;
				if (null != applicationDetails.get("transferFee")) {
					transferFee = new BigDecimal(applicationDetails.get("transferFee").asText());
				}
				TaxHeadEstimate transferFeeEstimate = new TaxHeadEstimate();
				transferFeeEstimate.setEstimateAmount(transferFee);
				transferFeeEstimate.setCategory(Category.FEE);
				transferFeeEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "TRANSFER", Category.FEE));
				estimates.add(transferFeeEstimate);

				// Allotment number
				BigDecimal applicationNumberCharges = BigDecimal.ZERO;
				if (null != applicationDetails.get("applicationNumberCharges")) {
					applicationNumberCharges = new BigDecimal(
							applicationDetails.get("applicationNumberCharges").asText());
				}
				TaxHeadEstimate applicationNumberChargesEstimate = new TaxHeadEstimate();
				applicationNumberChargesEstimate.setEstimateAmount(applicationNumberCharges);
				applicationNumberChargesEstimate.setCategory(Category.CHARGES);
				applicationNumberChargesEstimate
				.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "ALLOTMENT_NUMBER", Category.CHARGES));
				estimates.add(applicationNumberChargesEstimate);
			}

		}
		else if (application.getBranchType().equalsIgnoreCase(PSConstants.APPLICATION_ESTATE_BRANCH) && application.getState().contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {

			JsonNode applicationDetails = application.getApplicationDetails();

			//Transfer Charges
			if(!applicationDetails.get("transferCharges").isNull() && Integer.valueOf(applicationDetails.get("transferCharges").asText())>0) {
				BigDecimal transferCharges = new BigDecimal(applicationDetails.get("transferCharges").toString());
				TaxHeadEstimate transferChargesEstimate = new TaxHeadEstimate();
				transferChargesEstimate.setEstimateAmount(transferCharges);
				transferChargesEstimate.setCategory(Category.CHARGES);
				transferChargesEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_TRANSFER, Category.CHARGES));
				estimates.add(transferChargesEstimate);
			}
			
			//GST
			if(!applicationDetails.get("GST").isNull() && Integer.valueOf(applicationDetails.get("GST").asText())>0) {
				BigDecimal gst = new BigDecimal(applicationDetails.get("GST").toString());
				TaxHeadEstimate gstEstimate = new TaxHeadEstimate();
				gstEstimate.setEstimateAmount(gst);
				gstEstimate.setCategory(Category.TAX);
				gstEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),PSConstants.TAX_HEAD_CODE_GST, Category.TAX));
				estimates.add(gstEstimate);
			}
			
			//Processing fees/Application Fee
			if(!applicationDetails.get("applicationFee").isNull() && Integer.valueOf(applicationDetails.get("applicationFee").asText())>0) {
				BigDecimal applciationFee = new BigDecimal(applicationDetails.get("applicationFee").toString());
				TaxHeadEstimate applciationFeeEstimate = new TaxHeadEstimate();
				applciationFeeEstimate.setEstimateAmount(applciationFee);
				applciationFeeEstimate.setCategory(Category.FEE);
				applciationFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
				estimates.add(applciationFeeEstimate);
			}
			
			//Inspection Fees
			if(!applicationDetails.get("inspectionFee").isNull() && Integer.valueOf(applicationDetails.get("inspectionFee").asText())>0) {
				BigDecimal inspectionFee = new BigDecimal(applicationDetails.get("inspectionFee").toString());
				TaxHeadEstimate inspectionFeeEstimate = new TaxHeadEstimate();
				inspectionFeeEstimate.setEstimateAmount(inspectionFee);
				inspectionFeeEstimate.setCategory(Category.FEE);
				inspectionFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_INSPECTION, Category.FEE));
				estimates.add(inspectionFeeEstimate);
			}
			
			//EMD/Security
			if(!applicationDetails.get("securityFee").isNull() && Integer.valueOf(applicationDetails.get("securityFee").asText())>0) {
				BigDecimal securityFee = new BigDecimal(applicationDetails.get("securityFee").toString());
				TaxHeadEstimate securityFeeEstimate = new TaxHeadEstimate();
				securityFeeEstimate.setEstimateAmount(securityFee);
				securityFeeEstimate.setCategory(Category.FEE);
				securityFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_SECURITY, Category.FEE));
				estimates.add(securityFeeEstimate);
			}
			
			//Extension fees
			if(!applicationDetails.get("extensionFee").isNull() && Integer.valueOf(applicationDetails.get("extensionFee").asText())>0) {
				BigDecimal extensionFee = new BigDecimal(applicationDetails.get("extensionFee").toString());
				TaxHeadEstimate extensionFeeEstimate = new TaxHeadEstimate();
				extensionFeeEstimate.setEstimateAmount(extensionFee);
				extensionFeeEstimate.setCategory(Category.FEE);
				extensionFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_EXTENSION, Category.FEE));
				estimates.add(extensionFeeEstimate);
			}
			
			//Certificate/Document Copying Fees
			if(!applicationDetails.get("DocumentCopyingFee").isNull() && Integer.valueOf(applicationDetails.get("DocumentCopyingFee").asText())>0) {
				BigDecimal documentCopyingFee = new BigDecimal(applicationDetails.get("DocumentCopyingFee").toString());
				TaxHeadEstimate documentCopyingFeeEstimate = new TaxHeadEstimate();
				documentCopyingFeeEstimate.setEstimateAmount(documentCopyingFee);
				documentCopyingFeeEstimate.setCategory(Category.FEE);
				documentCopyingFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_DOCUMENTCOPYING, Category.FEE));
				estimates.add(documentCopyingFeeEstimate);
			}
			
			//Allotment Fees
			if(!applicationDetails.get("allotmentFee").isNull() && Integer.valueOf(applicationDetails.get("allotmentFee").asText())>0) {
				BigDecimal allotmentFee = new BigDecimal(applicationDetails.get("allotmentFee").toString());
				TaxHeadEstimate allotmentFeeEstimate = new TaxHeadEstimate();
				allotmentFeeEstimate.setEstimateAmount(allotmentFee);
				allotmentFeeEstimate.setCategory(Category.FEE);
				allotmentFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_ALLOTMENT, Category.FEE));
				estimates.add(allotmentFeeEstimate);
			}
			
			//Conversion fees
			if(!applicationDetails.get("conversionFee").isNull() && Integer.valueOf(applicationDetails.get("conversionFee").asText())>0) {
				BigDecimal conversionFee = new BigDecimal(applicationDetails.get("conversionFee").toString());
				TaxHeadEstimate conversionFeeEstimate = new TaxHeadEstimate();
				conversionFeeEstimate.setEstimateAmount(conversionFee);
				conversionFeeEstimate.setCategory(Category.FEE);
				conversionFeeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_CONVERSION, Category.FEE));
				estimates.add(conversionFeeEstimate);
			}
			
			//Property Transfer charges
			if(!applicationDetails.get("propertyTransferCharge").isNull() && Integer.valueOf(applicationDetails.get("propertyTransferCharge").asText())>0) {
				BigDecimal propertyTransferCharge = new BigDecimal(applicationDetails.get("propertyTransferCharge").toString());
				TaxHeadEstimate propertyTransferChargeEstimate = new TaxHeadEstimate();
				propertyTransferChargeEstimate.setEstimateAmount(propertyTransferCharge);
				propertyTransferChargeEstimate.setCategory(Category.CHARGES);
				propertyTransferChargeEstimate.setTaxHeadCode(getTaxHeadCodeWithCharge(application.getBillingBusinessService(),
						PSConstants.TAX_HEAD_CODE_PROPERTYTRANSFER, Category.CHARGES));
				estimates.add(propertyTransferChargeEstimate);
			}

		}
		
		else if (application.getState().contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {

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

	public BigDecimal calculateDevelopmentCharges(JsonNode applicationDetails) {
		BigDecimal developmentCharges = BigDecimal.ZERO;
		double calculateDevelopmentCharges = 0.0;

		double frontElevationWidthFt = applicationDetails.get("frontElevationWidth").asDouble();
		double frontElevationWidthInch = applicationDetails.get("frontElevationWidthInch").asDouble();
		double frontElevationWidth = frontElevationWidthFt + (frontElevationWidthInch / 12);

		double streetWidthFt = applicationDetails.get("streetWidth").asDouble();
		double streetWidthInch = applicationDetails.get("streetWidthInch").asDouble();
		double streetWidth = streetWidthFt + (streetWidthInch / 12);

		boolean otherSideStreet = applicationDetails.get("otherSideStreet").asBoolean();
		if (otherSideStreet) {

			double sameWidthOfSideStreetFT = applicationDetails.get("sameWidthOfSideStreet").asDouble();
			double sameWidthOfSideStreetInch = applicationDetails.get("sameWidthOfSideStreetInch").asDouble();
			double sameWidthOfSideStreet = sameWidthOfSideStreetFT + (sameWidthOfSideStreetInch / 12);

			double sameHeightOfSideStreetFT = applicationDetails.get("sameHeightOfSideStreet").asDouble();
			double sameHeightOfSideStreetInch = applicationDetails.get("sameHeightOfSideStreetInch").asDouble();
			double sameHeightOfSideStreet = sameHeightOfSideStreetFT + (sameHeightOfSideStreetInch / 12);

			calculateDevelopmentCharges = ((frontElevationWidth * streetWidth)
					+ (sameWidthOfSideStreet * sameHeightOfSideStreet)) * (100 / 2);

		} else {
			calculateDevelopmentCharges = (frontElevationWidth * streetWidth) * (100 / 2);
		}

		developmentCharges = BigDecimal.valueOf(calculateDevelopmentCharges);
		developmentCharges = developmentCharges.setScale(0, RoundingMode.HALF_UP);
		return developmentCharges;
	}

	public BigDecimal calculateConversionCharges(JsonNode applicationDetails) {
		BigDecimal conversionCharges = BigDecimal.ZERO;
		double calculateconversionCharges = 0.0;

		boolean commercialActivity = applicationDetails.get("commercialActivity").asBoolean();

		if (commercialActivity) {

			double groundFloorcommercialActivityFt = applicationDetails.get("groundFloorcommercialActivity").asDouble();
			double groundFloorcommercialActivityInch = applicationDetails.get("groundFloorcommercialActivityInch")
					.asDouble();
			double groundFloorcommercialActivity = groundFloorcommercialActivityFt
					+ (groundFloorcommercialActivityInch / 12);

			double firstFloorcommercialActivityFt = applicationDetails.get("firstFloorcommercialActivity").asDouble();
			double firstFloorcommercialActivityInch = applicationDetails.get("firstFloorcommercialActivityInch")
					.asDouble();
			double firstFloorcommercialActivity = firstFloorcommercialActivityFt
					+ (firstFloorcommercialActivityInch / 12);

			double secondFloorcommercialActivityFt = applicationDetails.get("secondFloorcommercialActivity").asDouble();
			double secondFloorcommercialActivityInch = applicationDetails.get("secondFloorcommercialActivityInch")
					.asDouble();
			double secondFloorcommercialActivity = secondFloorcommercialActivityFt
					+ (secondFloorcommercialActivityInch / 12);

			calculateconversionCharges = ((groundFloorcommercialActivity + firstFloorcommercialActivity
					+ secondFloorcommercialActivity) / 9) * 2400;

		}

		conversionCharges = BigDecimal.valueOf(calculateconversionCharges);
		conversionCharges = conversionCharges.setScale(0, RoundingMode.HALF_UP);
		return conversionCharges;
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

	public String getBbNocTaxHeadCode(String billingBusService, String chargeFor, String chargeType,
			Category category) {
		return String.format("%s_%s_%s_%s", billingBusService, chargeFor, chargeType, category.toString());
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

		if (application.getBranchType().contentEquals(PSConstants.APPLICATION_BUILDING_BRANCH)
				&& application.getApplicationType().contentEquals(PSConstants.NOC)) {

			JsonNode applicationDetails = application.getApplicationDetails();

			BigDecimal developmentCharges = new BigDecimal(applicationDetails.get("developmentCharges").toString());
			TaxHeadEstimate developmentChargesEstimate = new TaxHeadEstimate();
			developmentChargesEstimate.setEstimateAmount(developmentCharges);
			developmentChargesEstimate.setCategory(Category.CHARGES);
			developmentChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "DEVELOPMENT", Category.CHARGES));
			estimates.add(developmentChargesEstimate);

			// Conversion charges
			BigDecimal conversionCharges = new BigDecimal(applicationDetails.get("conversionCharges").toString());
			TaxHeadEstimate conversionChargesEstimate = new TaxHeadEstimate();
			conversionChargesEstimate.setEstimateAmount(conversionCharges);
			conversionChargesEstimate.setCategory(Category.CHARGES);
			conversionChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "CONVERSION", Category.CHARGES));
			estimates.add(conversionChargesEstimate);

			// Scrutiny charges
			BigDecimal scrutinyCharges = BigDecimal.ZERO;
			if (null != applicationDetails.get("scrutinyCharges")) {
				scrutinyCharges = new BigDecimal(applicationDetails.get("scrutinyCharges").asText());
			}
			TaxHeadEstimate scrutinyChargesEstimate = new TaxHeadEstimate();
			scrutinyChargesEstimate.setEstimateAmount(scrutinyCharges);
			scrutinyChargesEstimate.setCategory(Category.CHARGES);
			scrutinyChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "SCRUTINY", Category.CHARGES));
			estimates.add(scrutinyChargesEstimate);

			// Transfer fees
			BigDecimal transferFee = BigDecimal.ZERO;
			if (null != applicationDetails.get("transferFee")) {
				transferFee = new BigDecimal(applicationDetails.get("transferFee").asText());
			}
			TaxHeadEstimate transferFeeEstimate = new TaxHeadEstimate();
			transferFeeEstimate.setEstimateAmount(transferFee);
			transferFeeEstimate.setCategory(Category.FEE);
			transferFeeEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "TRANSFER", Category.FEE));
			estimates.add(transferFeeEstimate);

			// Allotment number
			BigDecimal applicationNumberCharges = BigDecimal.ZERO;
			if (null != applicationDetails.get("applicationNumberCharges")) {
				applicationNumberCharges = new BigDecimal(applicationDetails.get("applicationNumberCharges").asText());
			}
			TaxHeadEstimate applicationNumberChargesEstimate = new TaxHeadEstimate();
			applicationNumberChargesEstimate.setEstimateAmount(applicationNumberCharges);
			applicationNumberChargesEstimate.setCategory(Category.CHARGES);
			applicationNumberChargesEstimate.setTaxHeadCode(getBbNocTaxHeadCode(application.getBillingBusinessService(),
					PSConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, "ALLOTMENT_NUMBER", Category.CHARGES));
			estimates.add(applicationNumberChargesEstimate);
		} else {

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
		}

		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);
	}

}
