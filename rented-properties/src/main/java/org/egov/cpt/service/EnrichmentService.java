package org.egov.cpt.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.Address;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.OwnerDetails;
import org.egov.cpt.models.OwnerDetails.ApplicationTypeEnum;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.PropertyDetails;
import org.egov.cpt.models.PropertyImages;
import org.egov.cpt.models.RentSummary;
import org.egov.cpt.models.Idgen.IdResponse;
import org.egov.cpt.models.calculation.Calculation;
import org.egov.cpt.models.calculation.Category;
import org.egov.cpt.models.calculation.TaxHeadEstimate;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.IdGenRepository;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.util.PropertyUtil;
import org.egov.cpt.web.contracts.DuplicateCopyRequest;
import org.egov.cpt.web.contracts.MortgageRequest;
import org.egov.cpt.web.contracts.NoticeGenerationRequest;
import org.egov.cpt.web.contracts.OwnershipTransferRequest;
import org.egov.cpt.web.contracts.PropertyImagesRequest;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EnrichmentService {

	@Autowired
	PropertyUtil propertyutil;

	@Autowired
	IdGenRepository idGenRepository;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private PropertyRepository propertyRepository;

	@Autowired
	private Producer producer;

	public void enrichCreateRequest(PropertyRequest request) {

		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {

				String gen_property_id = UUID.randomUUID().toString();
				PropertyDetails propertyDetail = getPropertyDetail(property, requestInfo, gen_property_id);

				property.setId(gen_property_id);
				property.setAuditDetails(propertyAuditDetails);
				property.setPropertyDetails(propertyDetail);
				property.setMasterDataState(PTConstants.PM_DRAFTED);

				// log.info("property id: " + gen_property_id);

				if (!CollectionUtils.isEmpty(property.getOwners())) {
					property.getOwners().forEach(owner -> {
						Property ownerProperty = property;
						ownerProperty.setId(gen_property_id);
						ownerProperty.setTransitNumber(property.getTransitNumber());
						String gen_owner_id = UUID.randomUUID().toString();
						owner.setId(gen_owner_id);
						owner.setProperty(ownerProperty);
						owner.setTenantId(property.getTenantId());
						owner.setAuditDetails(propertyAuditDetails);
						OwnerDetails ownerDetails = getOwnerShipDetails(owner, property, requestInfo, gen_property_id);
						owner.setOwnerDetails(ownerDetails);
						property.getPropertyDetails().setCurrentOwner(owner.getId());
						owner.setIsPrimaryOwner(true);
						owner.getOwnerDetails().setPermanent(true);

						// log.info("owner id: " + gen_owner_id);
					});
				}
			});
		}
	}

	private OwnerDetails getOwnerShipDetails(Owner owner, Property property, RequestInfo requestInfo,
			String gen_property_id) {
		OwnerDetails ownerDetails = owner.getOwnerDetails();
		String gen_owner_details_id = UUID.randomUUID().toString();
		AuditDetails ownerAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		ownerDetails.setId(gen_owner_details_id);
		ownerDetails.setPropertyId(property.getId());
		ownerDetails.setOwnerId(owner.getId());
		ownerDetails.setTenantId(property.getTenantId());
		ownerDetails.setPermanent(false);
		ownerDetails.setAuditDetails(ownerAuditDetails);
		ownerDetails.setApplicationType(ApplicationTypeEnum.MASTERRP);
		// log.info("owner detail id: " + gen_owner_details_id);
		return ownerDetails;
	}

	public void enrichUpdateRequest(PropertyRequest request, List<Property> propertyFromDb) {
		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails auditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), false);

		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {
				AuditDetails modifyAuditDetails = property.getAuditDetails();
				modifyAuditDetails.setLastModifiedBy(auditDetails.getLastModifiedBy());
				modifyAuditDetails.setLastModifiedTime(auditDetails.getLastModifiedTime());
				property.setAuditDetails(modifyAuditDetails);
				property.getPropertyDetails().setAuditDetails(modifyAuditDetails);

				PropertyDetails propertyDetail = updatePropertyDetail(property, requestInfo);
				property.setPropertyDetails(propertyDetail);

				if (!CollectionUtils.isEmpty(property.getOwners())) {
					property.getOwners().forEach(owner -> {
						owner.setAuditDetails(modifyAuditDetails);
						owner.getOwnerDetails().setAuditDetails(modifyAuditDetails);
					});
				}
			});
		}
	}

	public PropertyDetails updatePropertyDetail(Property property, RequestInfo requestInfo) {
		PropertyDetails propertyDetail = property.getPropertyDetails();
		enrichDocuments(propertyDetail.getApplicationDocuments(), requestInfo, property.getId(), property.getId(),
				property.getTenantId());
		return propertyDetail;
	}

	public PropertyDetails getPropertyDetail(Property property, RequestInfo requestInfo, String gen_property_id) {
		PropertyDetails propertyDetail = property.getPropertyDetails();
		String gen_property_details_id = UUID.randomUUID().toString();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		propertyDetail.setId(gen_property_details_id);
		propertyDetail.setPropertyId(gen_property_id);
		propertyDetail.setTransitNumber(property.getTransitNumber());
		propertyDetail.setTenantId(property.getTenantId());

		Address address = getAddress(property, requestInfo, gen_property_id);
		propertyDetail.setAddress(address);

		List<Document> applicationDocuments = getApplicationDocs(propertyDetail, property, requestInfo,
				gen_property_id);
		propertyDetail.setApplicationDocuments(applicationDocuments);

		/*
		 * if (!CollectionUtils.isEmpty(property.getOwners())) {
		 * property.getOwners().forEach(owner -> { if (owner.getActiveState()) {
		 * propertyDetail.setCurrentOwner(owner.getOwnerDetails().getName()); } }); }
		 */

		propertyDetail.setAuditDetails(propertyAuditDetails);
		return propertyDetail;
	}

	private List<Document> getApplicationDocs(PropertyDetails propertyDetails, Property property,
			RequestInfo requestInfo, String gen_property_id) {
		List<Document> applicationDocuments = propertyDetails.getApplicationDocuments();
		if (!CollectionUtils.isEmpty(applicationDocuments)) {
			AuditDetails docAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
			applicationDocuments.forEach(document -> {
				String gen_doc_id = UUID.randomUUID().toString();
				document.setId(gen_doc_id);
				document.setPropertyId(gen_property_id);
				document.setReferenceId(gen_property_id);
				document.setTenantId(property.getTenantId());
				document.setAuditDetails(docAuditDetails);
			});
		}
		return applicationDocuments;
	}

	public Address getAddress(Property property, RequestInfo requestInfo, String gen_property_id) {
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		Address address = property.getPropertyDetails().getAddress();
		if (address != null) {
			String gen_address_id = UUID.randomUUID().toString();
			address.setId(gen_address_id);
			address.setPropertyId(gen_property_id);
			address.setTransitNumber(property.getTransitNumber());
			address.setTenantId(property.getTenantId());
			address.setColony(property.getColony());
			address.setAuditDetails(propertyAuditDetails);
			return address;
		}
		return address;
	}

	/**
	 * Populates the owner fields inside of property objects from the response got
	 * from calling user api
	 * 
	 * @param userDetailResponse response from user api which contains list of user
	 *                           which are used to populate owners in properties
	 * @param properties         List of property whose owner's are to be populated
	 *                           from userDetailResponse
	 */
	/*
	 * public void enrichOwner(UserDetailResponse userDetailResponse, List<Property>
	 * properties) {
	 * 
	 * List<Owner> users = userDetailResponse.getUser(); Map<String, Owner>
	 * userIdToOwnerMap = new HashMap<>(); users.forEach(user ->
	 * userIdToOwnerMap.put(user.getId(), user));
	 * 
	 * properties.forEach(property -> {
	 * 
	 * property.getOwners().forEach(owner -> {
	 * 
	 * if (userIdToOwnerMap.get(owner.getId()) == null) throw new
	 * CustomException("OWNER SEARCH ERROR", "The owner of the propertyDetail " +
	 * property.getId() + " is not coming in user search"); }); }); }
	 */

	/*
	 * Ownership Transfer
	 */
	public void enrichCreateOwnershipTransfer(OwnershipTransferRequest request, List<Property> propertyFromDb) {

		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		Property foundProperty = propertyFromDb.get(0);
		if (!CollectionUtils.isEmpty(request.getOwners())) {
			request.getOwners().forEach(owner -> {
				String gen_owner_id = UUID.randomUUID().toString();
				owner.setId(gen_owner_id);
				owner.setTenantId(foundProperty.getTenantId());
				owner.setIsPrimaryOwner(false);
				owner.setActiveState(false);
				owner.setAuditDetails(propertyAuditDetails);
				OwnerDetails ownerDetails = updateOwnerShipDetails(owner, foundProperty, requestInfo, gen_owner_id);
				owner.setOwnerDetails(ownerDetails);

				// demand generation
				enrichGenerateDemand(owner);
			});
			setIdgenIds(request);
		}
	}

	public void enrichUpdateOwnershipTransfer(OwnershipTransferRequest request, List<Owner> ownerFromDb) {
		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails updateAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		if (!CollectionUtils.isEmpty(request.getOwners())) {
			request.getOwners().forEach(owner -> {
				updateOwnershipTransferDocs(owner, requestInfo);
				AuditDetails modifyAuditDetails = owner.getAuditDetails();
				modifyAuditDetails.setLastModifiedBy(updateAuditDetails.getLastModifiedBy());
				modifyAuditDetails.setLastModifiedTime(updateAuditDetails.getLastModifiedTime());

				owner.setIsPrimaryOwner(false);
				owner.setActiveState(false);
				owner.setAuditDetails(modifyAuditDetails);
				owner.getOwnerDetails().setAuditDetails(modifyAuditDetails);

				// demand generation
				enrichUpdateDemand(owner);
			});
		}
	}

	private void enrichGenerateDemand(Owner owner) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		TaxHeadEstimate estimateDue = new TaxHeadEstimate();
		estimateDue.setEstimateAmount(new BigDecimal(0.0));
		estimateDue.setCategory(Category.FEE);
		estimateDue.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_OT, PTConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
		estimates.add(estimateDue);

		TaxHeadEstimate estimateCharges = new TaxHeadEstimate();
		estimateCharges.setEstimateAmount(new BigDecimal(0.0));
		estimateCharges.setCategory(Category.FEE);
		estimateCharges.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_OT, PTConstants.TAX_HEAD_CODE_PUBLICATION_CHARGE, Category.FEE));
		estimates.add(estimateCharges);

		Calculation calculation = Calculation.builder()
				.applicationNumber(owner.getOwnerDetails().getApplicationNumber()).taxHeadEstimates(estimates)
				.tenantId(owner.getTenantId()).build();
		owner.setCalculation(calculation);
	}

	private void enrichUpdateDemand(Owner owner) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		// TaxHeadEstimate estimate = new TaxHeadEstimate();
		/*
		 * if (owner.getApplicationState().equalsIgnoreCase(PTConstants.
		 * OT_STATE_PENDING_SA_VERIFICATION)) {
		 * estimate.setEstimateAmount(owner.getOwnerDetails().getDueAmount());
		 * estimate.setCategory(Category.DUE);
		 * estimate.setTaxHeadCode(getTaxHeadCode(PTConstants.
		 * BILLING_BUSINESS_SERVICE_OT, Category.DUE)); }
		 */
		if (owner.getApplicationState().equalsIgnoreCase(PTConstants.OT_STATE_PENDING_APRO)) {
			TaxHeadEstimate estimate1 = new TaxHeadEstimate();
			estimate1.setEstimateAmount(owner.getOwnerDetails().getDueAmount());
			estimate1.setCategory(Category.FEE);
			estimate1.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_OT, PTConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
			estimates.add(estimate1);

			TaxHeadEstimate estimate2 = new TaxHeadEstimate();
			estimate2.setEstimateAmount(owner.getOwnerDetails().getAproCharge());
			estimate2.setCategory(Category.FEE);
			estimate2.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_OT, PTConstants.TAX_HEAD_CODE_PUBLICATION_CHARGE, Category.FEE));
			estimates.add(estimate2);
		}
		// estimates.add(estimate);
		Calculation calculation = Calculation.builder()
				.applicationNumber(owner.getOwnerDetails().getApplicationNumber()).taxHeadEstimates(estimates)
				.tenantId(owner.getTenantId()).build();
		owner.setCalculation(calculation);
	}

	private String getTaxHeadCode(String billingBusService, Category category) {
		return String.format("%s_%s", billingBusService, category.toString());
	}
	
	private String getTaxHeadCodeWithCharge(String billingBusService, String chargeFor, Category category) {
		return String.format("%s_%s_%s", billingBusService, chargeFor, category.toString());
	}

	private void updateOwnershipTransferDocs(Owner owner, RequestInfo requestInfo) {
		enrichDocuments(owner.getOwnerDetails().getOwnershipTransferDocuments(), requestInfo,
				owner.getProperty().getId(), owner.getId(), owner.getProperty().getTenantId());
	}

	private OwnerDetails updateOwnerShipDetails(Owner owner, Property foundProperty, RequestInfo requestInfo,
			String gen_owner_id) {
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		OwnerDetails ownerDetails = owner.getOwnerDetails();
		String gen_owner_details_id = UUID.randomUUID().toString();
		ownerDetails.setId(gen_owner_details_id);
		ownerDetails.setPropertyId(owner.getProperty().getId());
		ownerDetails.setOwnerId(gen_owner_id);
		ownerDetails.setAuditDetails(propertyAuditDetails);
		owner.setTenantId(foundProperty.getTenantId());
		return ownerDetails;
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
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * Sets the ApplicationNumber for given TradeLicenseRequest
	 *
	 * @param request TradeLicenseRequest which is to be created
	 */
	private void setIdgenIds(OwnershipTransferRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getOwners().get(0).getTenantId();
		List<Owner> owners = request.getOwners();
		int peopertiesSize = request.getOwners().size();

		List<String> applicationNumbers = setIdgenIds(requestInfo, tenantId, peopertiesSize,
				config.getApplicationNumberIdgenNameRP(), config.getApplicationNumberIdgenFormatRP());
		ListIterator<String> itr = applicationNumbers.listIterator();
		List<String> allotmentNumbers = setIdgenIds(requestInfo, tenantId, peopertiesSize,
				config.getAllotmentNumberIdgenNameRP(), config.getAllotmentNumberIdgenFormatRP());
		ListIterator<String> allotmentitr = allotmentNumbers.listIterator();
		if (!CollectionUtils.isEmpty(owners)) {
			owners.forEach(owner -> {
				owner.getOwnerDetails().setApplicationNumber(itr.next());
				owner.setAllotmenNumber(allotmentitr.next());
			});
		}
	}

	public void enrichDuplicateCopyCreateRequest(DuplicateCopyRequest duplicateCopyRequest) {
		RequestInfo requestInfo = duplicateCopyRequest.getRequestInfo();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		if (!CollectionUtils.isEmpty(duplicateCopyRequest.getDuplicateCopyApplications())) {
			duplicateCopyRequest.getDuplicateCopyApplications().forEach(application -> {
				String gen_application_id = UUID.randomUUID().toString();
				application.setId(gen_application_id);
				application.getProperty()
						.setId(duplicateCopyRequest.getDuplicateCopyApplications().get(0).getProperty().getId());
				application.setAuditDetails(propertyAuditDetails);

				if (!CollectionUtils.isEmpty(application.getApplicant())) {
					application.getApplicant().forEach(applicant -> {
						applicant.setId(UUID.randomUUID().toString());
						applicant.setApplicationId(gen_application_id);
						applicant.setTenantId(application.getTenantId());
						applicant.setAuditDetails(propertyAuditDetails);
					});
				}

				// demand generation
				enrichDuplicateCopyGenerateDemand(application);
			});
		}
		setDCIdgenIds(duplicateCopyRequest);
	}

	// PI Enrich
	public void enrichpropertyImageCreateRequest(PropertyImagesRequest propertyImagesRequest) {
		RequestInfo requestInfo = propertyImagesRequest.getRequestInfo();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		if (!CollectionUtils.isEmpty(propertyImagesRequest.getPropertyImagesApplications())) {
			propertyImagesRequest.getPropertyImagesApplications().forEach(application -> {
				String gen_application_id = UUID.randomUUID().toString();
				application.setId(gen_application_id);
				application.getProperty()
						.setId(propertyImagesRequest.getPropertyImagesApplications().get(0).getProperty().getId());
				application.setAuditDetails(propertyAuditDetails);

				application.setCapturedBy(requestInfo.getUserInfo().getName());

				enrichDocuments(application.getApplicationDocuments(), requestInfo, application.getProperty().getId(),
						application.getId(), application.getTenantId());
			});
		}
		setPIIdgenIds(propertyImagesRequest);
	}

	private void enrichDuplicateCopyGenerateDemand(DuplicateCopy application) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		TaxHeadEstimate estimateFee = new TaxHeadEstimate();
		estimateFee.setEstimateAmount(new BigDecimal(0.0));
		estimateFee.setCategory(Category.FEE);
		estimateFee.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_DC, PTConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
		estimates.add(estimateFee);

		TaxHeadEstimate estimateCharges = new TaxHeadEstimate();
		estimateCharges.setEstimateAmount(new BigDecimal(0.0));
		estimateCharges.setCategory(Category.FEE);
		estimateCharges.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_DC, PTConstants.TAX_HEAD_CODE_PUBLICATION_CHARGE, Category.FEE));
		estimates.add(estimateCharges);

		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);

	}

	private void enrichDuplicateCopyUpdateDemand(DuplicateCopy application) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		/*
		 * if (application.getState().equalsIgnoreCase(PTConstants.
		 * DC_STATE_PENDING_SA_VERIFICATION)) {
		 * estimate.setEstimateAmount(application.getApplicant().get(0).getFeeAmount());
		 * estimate.setCategory(Category.FEE);
		 * estimate.setTaxHeadCode(getTaxHeadCode(PTConstants.
		 * BILLING_BUSINESS_SERVICE_DC, Category.FEE)); }
		 */
		if (application.getState().equalsIgnoreCase(PTConstants.DC_STATE_PENDING_APRO)) {
			TaxHeadEstimate estimate1 = new TaxHeadEstimate();
			estimate1.setEstimateAmount(application.getApplicant().get(0).getFeeAmount());
			estimate1.setCategory(Category.FEE);
			estimate1.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_DC, PTConstants.TAX_HEAD_CODE_APPLICATION_CHARGE, Category.FEE));
			estimates.add(estimate1);

			TaxHeadEstimate estimate2 = new TaxHeadEstimate();
			estimate2.setEstimateAmount(application.getApplicant().get(0).getAproCharge());
			estimate2.setCategory(Category.FEE);
			estimate2.setTaxHeadCode(getTaxHeadCodeWithCharge(PTConstants.BILLING_BUSINESS_SERVICE_DC, PTConstants.TAX_HEAD_CODE_PUBLICATION_CHARGE, Category.FEE));
			estimates.add(estimate2);
		}
		Calculation calculation = Calculation.builder().applicationNumber(application.getApplicationNumber())
				.taxHeadEstimates(estimates).tenantId(application.getTenantId()).build();
		application.setCalculation(calculation);
	}

	private void setDCIdgenIds(DuplicateCopyRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getDuplicateCopyApplications().get(0).getTenantId();
		List<DuplicateCopy> applications = request.getDuplicateCopyApplications();

		List<String> applicationNumbers = setIdgenIds(requestInfo, tenantId, applications.size(),
				config.getApplicationNumberIdgenNameDC(), config.getApplicationNumberIdgenFormatDC());
		ListIterator<String> itr = applicationNumbers.listIterator();
		applications.forEach(application -> {
			application.setApplicationNumber(itr.next());
		});
	}

	// PI IDGen
	private void setPIIdgenIds(PropertyImagesRequest propertyImagesRequest) {
		RequestInfo requestInfo = propertyImagesRequest.getRequestInfo();
		String tenantId = propertyImagesRequest.getPropertyImagesApplications().get(0).getTenantId();
		List<PropertyImages> applications = propertyImagesRequest.getPropertyImagesApplications();

		List<String> applicationNumbers = setIdgenIds(requestInfo, tenantId, applications.size(),
				config.getApplicationNumberIdgenNamePI(), config.getApplicationNumberIdgenFormatPI());
		ListIterator<String> itr = applicationNumbers.listIterator();
		applications.forEach(application -> {
			application.setApplicationNumber(itr.next());
		});

	}

	public void enrichDuplicateCopyUpdateRequest(DuplicateCopyRequest duplicateCopyRequest,
			List<DuplicateCopy> searchedProperty) {
		RequestInfo requestInfo = duplicateCopyRequest.getRequestInfo();
		AuditDetails updateAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		// String propertyDtlId = searchedProperty.get(0).getPropertyDetails().getId();
		if (!CollectionUtils.isEmpty(duplicateCopyRequest.getDuplicateCopyApplications())) {
			duplicateCopyRequest.getDuplicateCopyApplications().forEach(application -> {
				AuditDetails modifyAuditDetails = application.getAuditDetails();
				modifyAuditDetails.setLastModifiedBy(updateAuditDetails.getLastModifiedBy());
				modifyAuditDetails.setLastModifiedTime(updateAuditDetails.getLastModifiedTime());
				application.setAuditDetails(modifyAuditDetails);
				enrichDocuments(application.getApplicationDocuments(), requestInfo, application.getProperty().getId(),
						application.getId(), application.getTenantId());
				enrichDuplicateCopyUpdateDemand(application);

				if (!CollectionUtils.isEmpty(application.getApplicant())) {
					application.getApplicant().forEach(applicant -> {
						applicant.setAuditDetails(modifyAuditDetails);
					});
				}
			});
		}

	}

	// PI Update Enrich
	public void enrichpropertyImagesUpdateRequest(PropertyImagesRequest propertyImagesRequest,
			List<PropertyImages> searchedProperty) {
		RequestInfo requestInfo = propertyImagesRequest.getRequestInfo();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		// String propertyDtlId = searchedProperty.get(0).getPropertyDetails().getId();
		if (!CollectionUtils.isEmpty(propertyImagesRequest.getPropertyImagesApplications())) {
			propertyImagesRequest.getPropertyImagesApplications().forEach(application -> {
				application.setAuditDetails(propertyAuditDetails);

				application.setCapturedBy(requestInfo.getUserInfo().getName());

				enrichDocuments(application.getApplicationDocuments(), requestInfo, application.getProperty().getId(),
						application.getId(), application.getTenantId());
			});
		}

	}

	public void enrichMortgageCreateRequest(MortgageRequest mortgageRequest) {
		RequestInfo requestInfo = mortgageRequest.getRequestInfo();
		AuditDetails mortgageAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		if (!CollectionUtils.isEmpty(mortgageRequest.getMortgageApplications())) {
			mortgageRequest.getMortgageApplications().forEach(application -> {
				String gen_mortgage_id = UUID.randomUUID().toString();
				application.setId(gen_mortgage_id);
				application.getProperty().setId(mortgageRequest.getMortgageApplications().get(0).getProperty().getId());
				application.setAuditDetails(mortgageAuditDetails);

				if (!CollectionUtils.isEmpty(application.getApplicant())) {
					application.getApplicant().forEach(applicant -> {
						applicant.setId(UUID.randomUUID().toString());
						applicant.setMortgageId(gen_mortgage_id);
						applicant.setTenantId(application.getTenantId());
						applicant.setAuditDetails(mortgageAuditDetails);
					});
				}

				if (!CollectionUtils.isEmpty(application.getMortgageApprovedGrantDetails())) {
					application.getMortgageApprovedGrantDetails().forEach(grantDetails -> {
						AuditDetails auditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(),
								true);
						grantDetails.setId(UUID.randomUUID().toString());
						grantDetails.setPropertyDetailId(application.getProperty().getId());
						grantDetails.setOwnerId(requestInfo.getUserInfo().getUuid());
						grantDetails.setTenentId(application.getTenantId());
						grantDetails.setAuditDetails(auditDetails);

					});
				}
			});
		}
		setMortgageIdgenIds(mortgageRequest);

	}

	private void setMortgageIdgenIds(MortgageRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getMortgageApplications().get(0).getTenantId();
		List<Mortgage> applications = request.getMortgageApplications();
		List<String> applicationNumbers = setIdgenIds(requestInfo, tenantId, applications.size(),
				config.getApplicationNumberIdgenNameMG(), config.getApplicationNumberIdgenFormatMG());
		ListIterator<String> itr = applicationNumbers.listIterator();
		applications.forEach(application -> {
			application.setApplicationNumber(itr.next());
		});

	}

	private List<String> setIdgenIds(RequestInfo requestInfo, String tenantId, int size, String idGenName,
			String idGenFormate) {
		List<String> applicationNumbers = null;

		applicationNumbers = getIdList(requestInfo, tenantId, idGenName, idGenFormate, size);

		Map<String, String> errorMap = new HashMap<>();
		if (applicationNumbers.size() != size) {
			errorMap.put("IDGEN ERROR ",
					"The number of application number returned by idgen is not equal to number of Applications");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return applicationNumbers;

	}

	/**
	 * Enriches the object after status is approved
	 * 
	 * @param ownershipTransferRequest The update request
	 */

	/**
	 * 1. property.propertyDetails.currentOwner
	 * 
	 * For current owner
	 * 
	 * 1. owner.activeState (true) 2. owner.ownerDetails.allotmentStartdate
	 * (current) 3. owner.ownerDetails.permanent (true)
	 * 
	 * For previous owner. 1. Make sure allotmentEndDate is set to current date. 2.
	 * active is false.
	 * 
	 * meu or applications separate allotment date
	 */
	public void postStatusEnrichment(OwnershipTransferRequest ownershipTransferRequest) {
		ownershipTransferRequest.getOwners().forEach(latestOwner -> {

			/**
			 * Get the property for the current ownership transfer request.
			 */
			PropertyCriteria criteria = getPropertyCriteriaForOT(ownershipTransferRequest);
			criteria.setLimit(1L);
			List<Property> properties = propertyRepository.getProperties(criteria);
			if (CollectionUtils.isEmpty(properties)) {
				throw new CustomException("OWNERSHIP TRANSFER INCOMPLETE",
						"Could not find property for this ownership transfer enrichment");
			}
			Property property = properties.get(0);
			property.getPropertyDetails().setCurrentOwner(latestOwner.getId());

			/*
			 * Modify the previous owner. 1. Make sure allotmentEndDate is set to current
			 * date. 2. active is false.
			 */
			Optional<Owner> previousOwner = property.getOwners().stream().filter(Owner::getActiveState).findAny();
			if (!previousOwner.isPresent()) {
				log.error("NO EXISTING OWNER FOUND",
						"No existing owner with isActive = true found in property " + property.getId());
			} else {
				previousOwner.get().setActiveState(false);
				previousOwner.get().getOwnerDetails().setAllotmentEnddate(getCurrentTimeEpoch());
			}

			/**
			 * Get the current owner. 1. owner.activeState (true) 2.
			 * owner.ownerDetails.allotmentStartdate (current) 3.
			 * owner.ownerDetails.permanent (true)
			 */

			latestOwner.setActiveState(true);
			latestOwner.getOwnerDetails().setPermanent(true);
			latestOwner.getOwnerDetails().setAllotmentStartdate(getCurrentTimeEpoch());

			properties.get(0).getOwners().add(latestOwner);

			/**
			 * Update the property by sending to the persistor.
			 */
			PropertyRequest propertyRequest = new PropertyRequest();
			propertyRequest.setRequestInfo(ownershipTransferRequest.getRequestInfo());
			propertyRequest.setProperties(properties);
			producer.push(config.getUpdatePropertyTopic(), propertyRequest);
		});
	}

	public Long getCurrentTimeEpoch() {
		long epochTime = 0;
		Date today = Calendar.getInstance().getTime();
		SimpleDateFormat crunchifyFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
		String currentTime = crunchifyFormat.format(today);
		Date date;
		try {
			date = crunchifyFormat.parse(currentTime);
			epochTime = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return epochTime;
	}

	public PropertyCriteria getPropertyCriteriaForOT(OwnershipTransferRequest request) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!CollectionUtils.isEmpty(request.getOwners())) {
			request.getOwners().forEach(owner -> {
				if (owner.getProperty().getId() != null)
					propertyCriteria.setPropertyId(owner.getProperty().getId());
				ArrayList<String> relations = new ArrayList<String>();
				relations.add(PTConstants.RELATION_OWNER);
				propertyCriteria.setRelations(relations);
			});
		}
		return propertyCriteria;
	}

	public void enrichMortgageUpdateRequest(MortgageRequest mortgageRequest, List<Mortgage> searchedProperty) {
		RequestInfo requestInfo = mortgageRequest.getRequestInfo();
		AuditDetails propertyAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		// String propertyDtlId = searchedProperty.get(0).getPropertyDetails().getId();
		if (!CollectionUtils.isEmpty(mortgageRequest.getMortgageApplications())) {
			mortgageRequest.getMortgageApplications().forEach(application -> {
				application.setAuditDetails(propertyAuditDetails);
				enrichDocuments(application.getApplicationDocuments(), requestInfo, application.getProperty().getId(),
						application.getId(), application.getTenantId());

				if (!CollectionUtils.isEmpty(application.getApplicant())) {
					application.getApplicant().forEach(applicant -> {
						applicant.setAuditDetails(propertyAuditDetails);
					});
				}

				if (!CollectionUtils.isEmpty(application.getMortgageApprovedGrantDetails())) {
					application.getMortgageApprovedGrantDetails().forEach(grantDetails -> {
						if (grantDetails.getId() == null || grantDetails.getId() == ""
								|| (!grantDetails.getId().isEmpty())) {
							AuditDetails auditDetails = propertyutil
									.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
							grantDetails.setId(UUID.randomUUID().toString());
							grantDetails.setPropertyDetailId(application.getProperty().getId());
							grantDetails.setOwnerId(requestInfo.getUserInfo().getUuid());
							grantDetails.setTenentId(application.getTenantId());
							grantDetails.setAuditDetails(auditDetails);
						}
					});
				}

			});
		}

	}

	public void postStatusEnrichmentDC(DuplicateCopyRequest duplicateCopyRequest, List<String> endstates) {
		duplicateCopyRequest.getDuplicateCopyApplications().forEach(dcApplication -> {
			// No enrichment for duplicate copy applications after final approval.
		});
	}

	public void enrichDuplicateCopySearchCriteria(RequestInfo requestInfo, DuplicateCopySearchCriteria criteria) {
		if (criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")) {
			criteria.setApplicantMobNo(requestInfo.getUserInfo().getUserName());
		}

	}

	public void enrichNoticeCreateRequest(NoticeGenerationRequest noticeGenerationRequest) {
		RequestInfo requestInfo = noticeGenerationRequest.getRequestInfo();
		AuditDetails noticeAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);

		if (!CollectionUtils.isEmpty(noticeGenerationRequest.getNoticeApplications())) {
			noticeGenerationRequest.getNoticeApplications().forEach(notice -> {
				String gen_notice_id = UUID.randomUUID().toString();
				notice.setId(gen_notice_id);
				notice.getProperty()
						.setId(noticeGenerationRequest.getNoticeApplications().get(0).getProperty().getId());
				notice.setAuditDetails(noticeAuditDetails);

				enrichDocuments(notice.getApplicationDocuments(), requestInfo, notice.getProperty().getId(),
						notice.getId(), notice.getTenantId());
			});
		}
		setNoticeIdgenIds(noticeGenerationRequest);
	}

	private void setNoticeIdgenIds(NoticeGenerationRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getNoticeApplications().get(0).getTenantId();
		List<NoticeGeneration> notice = request.getNoticeApplications();
		List<String> memoNumbers = setIdgenIds(requestInfo, tenantId, notice.size(), config.getMemoNumberIdgenNameNG(),
				config.getMemoNumbeIdgenFormatNG());
		ListIterator<String> itr = memoNumbers.listIterator();
		notice.forEach(application -> {
			application.setMemoNumber(itr.next());
		});

	}

	public void enrichNoticeUpdateRequest(NoticeGenerationRequest noticeGenerationRequest,
			List<NoticeGeneration> searchedProperty) {
		RequestInfo requestInfo = noticeGenerationRequest.getRequestInfo();
		AuditDetails updateAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		// String propertyDtlId = searchedProperty.get(0).getPropertyDetails().getId();
		if (!CollectionUtils.isEmpty(noticeGenerationRequest.getNoticeApplications())) {
			noticeGenerationRequest.getNoticeApplications().forEach(application -> {
				AuditDetails modifyAuditDetails = application.getAuditDetails();
				modifyAuditDetails.setLastModifiedBy(updateAuditDetails.getLastModifiedBy());
				modifyAuditDetails.setLastModifiedTime(updateAuditDetails.getLastModifiedTime());
				application.setAuditDetails(modifyAuditDetails);
				enrichDocuments(application.getApplicationDocuments(), requestInfo, application.getProperty().getId(),
						application.getId(), application.getTenantId());
			});
		}
	}

	private void enrichDocuments(List<Document> documents, RequestInfo requestInfo, String propertyId,
			String referenceId, String tenantId) {
		if (CollectionUtils.isEmpty(documents)) {
			return;
		}
		AuditDetails documentAuditDetails = propertyutil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		documents.forEach(document -> {
			if (document.getId() == null) {
				document.setId(UUID.randomUUID().toString());
				document.setActive(true);
			}
			document.setReferenceId(referenceId);
			document.setPropertyId(propertyId);
			document.setAuditDetails(documentAuditDetails);
			document.setTenantId(tenantId);
		});
	}

	public void enrichRentDemand(Property property, RentSummary rentSummary) {
		if (rentSummary == null)
			return;
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		double amount = property.getPaymentAmount();
		double balPrincipal = rentSummary.getBalancePrincipal();
		double balInterest = rentSummary.getBalanceInterest();

		if (amount >= balInterest) {
			TaxHeadEstimate estimate1 = new TaxHeadEstimate();
			estimate1.setEstimateAmount(new BigDecimal(balInterest));
			estimate1.setCategory(Category.INTEREST);
			estimate1.setTaxHeadCode(getTaxHeadCode(PTConstants.BILLING_BUSINESS_SERVICE_RENT, Category.INTEREST));
			estimates.add(estimate1);
			double remainingAmmount = amount - balInterest;
			if (remainingAmmount >= balPrincipal) {
				TaxHeadEstimate estimate2 = new TaxHeadEstimate();
				estimate2.setEstimateAmount(new BigDecimal(balPrincipal));
				estimate2.setCategory(Category.PRINCIPAL);
				estimate2.setTaxHeadCode(getTaxHeadCode(PTConstants.BILLING_BUSINESS_SERVICE_RENT, Category.PRINCIPAL));
				estimates.add(estimate2);
			} else {
				TaxHeadEstimate estimate2 = new TaxHeadEstimate();
				estimate2.setEstimateAmount(new BigDecimal(remainingAmmount));
				estimate2.setCategory(Category.PRINCIPAL);
				estimate2.setTaxHeadCode(getTaxHeadCode(PTConstants.BILLING_BUSINESS_SERVICE_RENT, Category.PRINCIPAL));
				estimates.add(estimate2);
			}
			remainingAmmount = amount - balInterest - balPrincipal;
			if (remainingAmmount > 0) {
				TaxHeadEstimate estimate3 = new TaxHeadEstimate();
				estimate3.setEstimateAmount(new BigDecimal(remainingAmmount));
				estimate3.setCategory(Category.ADVANCE_COLLECTION);
				estimate3.setTaxHeadCode(
						getTaxHeadCode(PTConstants.BILLING_BUSINESS_SERVICE_RENT, Category.ADVANCE_COLLECTION));
				estimates.add(estimate3);
			}
		} else {
			TaxHeadEstimate estimate2 = new TaxHeadEstimate();
			estimate2.setEstimateAmount(new BigDecimal(amount));
			estimate2.setCategory(Category.ADVANCE_COLLECTION);
			estimate2.setTaxHeadCode(
					getTaxHeadCode(PTConstants.BILLING_BUSINESS_SERVICE_RENT, Category.ADVANCE_COLLECTION));
			estimates.add(estimate2);
		}

		// estimates.add(estimate);
		Calculation calculation = Calculation.builder()
				.applicationNumber(propertyutil.getPropertyRentConsumerCode(property.getTransitNumber()))
				.taxHeadEstimates(estimates).tenantId(property.getTenantId()).build();
		log.info("calculation:" + calculation);
		property.setCalculation(calculation);

	}

}
