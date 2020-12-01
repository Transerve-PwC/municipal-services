package org.egov.cpt.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.NoticeSearchCriteria;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.PropertyImages;
import org.egov.cpt.models.calculation.BusinessService;
import org.egov.cpt.models.calculation.State;
import org.egov.cpt.repository.OwnershipTransferRepository;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.service.MDMSService;
import org.egov.cpt.util.DuplicateCopyConstants;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.web.contracts.DuplicateCopyRequest;
import org.egov.cpt.web.contracts.MortgageRequest;
import org.egov.cpt.web.contracts.NoticeGenerationRequest;
import org.egov.cpt.web.contracts.OwnershipTransferRequest;
import org.egov.cpt.web.contracts.PropertyImagesRequest;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.egov.cpt.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PropertyValidator {

	@Autowired
	private PropertyRepository repository;

	@Autowired
	private OwnershipTransferRepository OTRepository;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private MDMSService mdmsService;
	
	@Autowired
	private NotificationUtil notificationUtil;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;

	public void validateCreateRequest(PropertyRequest request) {

		Map<String, String> errorMap = new HashMap<>();
		if (CollectionUtils.isEmpty(request.getProperties())) {
			throw new CustomException(Collections.singletonMap("NO PROPERTIES FOUND", "No properties to create"));
		}

		validateTransitNumber(request, errorMap);
		validateOwner(request, errorMap);

		validateColony(request, errorMap);
		validateArea(request, errorMap);
		validateRentDetails(request, errorMap);
		validateNumericValueForProperty(request, errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private void validateRentDetails(PropertyRequest request, Map<String, String> errorMap) {
		List<Property> property = request.getProperties();
		property.forEach(properties -> {
			if (properties.getPropertyDetails().getRentIncrementPercentage() < 0
					|| properties.getPropertyDetails().getRentIncrementPercentage() >= 100) {
				errorMap.put("INVALID INCREMENT PERCENTAGE", "Increment precentage is not valid");
			}

			if (properties.getPropertyDetails().getInterestRate() < 0
					|| properties.getPropertyDetails().getInterestRate() >= 100) {
				errorMap.put("INVALID INTEREST RATE", "Interest rate is not valid");
			}
		});
	}

	private void validateOwner(PropertyRequest request, Map<String, String> errorMap) {

		List<Property> property = request.getProperties();
		property.forEach(properties -> {
			properties.getOwners().forEach(owner -> {

				if (!isValid(owner.getOwnerDetails().getName(), 4, 40)) {
					errorMap.put("INVALID OWNER NAME", "Owner Name is not valid");
				}

				if (!isNotNullValid(owner.getOwnerDetails().getPosessionStartdate())) {
					errorMap.put("INVALID POSESSION START DATE",
							"Posession Start date is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (!isNotNullValid(owner.getOwnerDetails().getAllotmentStartdate())) {
					errorMap.put("INVALID ALLOTMENT START DATE",
							"Allotment Start date is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (!isValid(owner.getAllotmenNumber(), 3, 20)) {
					errorMap.put("INVALID ALLOTMENT NUMBER",
							"Allotment Number is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (!isEmailValid(owner.getOwnerDetails().getEmail())) {
					errorMap.put("INVALID EMAIL", "Email is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (!isAadharNumberValid(owner.getOwnerDetails().getAadhaarNumber())) {
					errorMap.put("INVALID AADHARNUMBER",
							"Aadhar Number is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (!isMobileNumberValid(owner.getOwnerDetails().getPhone())) {
					errorMap.put("INVALID MOBILE NUMBER",
							"MobileNumber is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (!isValid(owner.getOwnerDetails().getFatherOrHusband(), 4, 40)) {
					errorMap.put("INVALID FATHER/HUSBAND NAME",
							"Father/Husband Name is not valid for user : " + owner.getOwnerDetails().getName());
				}

				if (owner.getOwnerDetails().getRelation() == null) {
					errorMap.put("INVALID FATHER/HUSBAND RELATION", "Father/Husband Relation is not valid ");
				}

			});
		});

	}

	private void validateArea(PropertyRequest request, Map<String, String> errorMap) {

		List<Property> property = request.getProperties();
		property.forEach(properties -> {
			if (!isValid(properties.getPropertyDetails().getArea(), 2, 20)) {
				errorMap.put("INVALID AREA", "Area is not valid ");
			}

			if (properties.getPropertyDetails().getAddress().getPincode() == null
					|| properties.getPropertyDetails().getAddress().getPincode().length() != 6) {
				errorMap.put("INVALID PINCODE", "Pincode is not valid");
			}
		});

	}

	@SuppressWarnings("unchecked")
	private void validateColony(PropertyRequest request, Map<String, String> errorMap) {

		String tenantId = request.getProperties().get(0).getTenantId();
		RequestInfo requestInfo = request.getRequestInfo();

		request.getProperties().forEach(property -> {

			String filter = "$.*.code";
			Map<String, List<String>> colonies = (Map<String, List<String>>) mdmsService.getMDMSResponse(requestInfo,
					tenantId.split("\\.")[0], PTConstants.MDMS_PT_MOD_NAME, "colonies", filter,
					PTConstants.JSONPATH_CODES);

			if (!colonies.get(PTConstants.MDMS_PT_COLONY).contains(property.getColony())
					|| (property.getColony().length() < 5 || property.getColony().length() > 45)) {
				errorMap.put("INVALID COLONY", "The Colony '" + property.getColony() + "' is not valid");
			}

		});
	}

	private void validateTransitNumber(PropertyRequest request, Map<String, String> errorMap) {

		/**
		 * Make sure we have a valid transit number. Valid transit site numbers are
		 * numeric and between 1 and 10000
		 */
		List<Property> properties = request.getProperties();
		Optional<Property> propertyWithInvalidTransitNumber = properties.stream().filter(
				property -> property.getTransitNumber() == null || !property.getTransitNumber().matches("\\d{1,4}"))
				.findAny();
		if (propertyWithInvalidTransitNumber.isPresent()) {
			throw new CustomException(Collections.singletonMap("INVALID TRANSIT NUMBER", String.format(
					"Invalid transit number '%s' found", propertyWithInvalidTransitNumber.get().getTransitNumber())));
		}

		/**
		 * Search for existing properties with the same transit number.
		 */
		Optional<String> existingTransitNumberOptional = properties.stream().map(Property::getTransitNumber)
				.filter(transitNumber -> !repository
						.getProperties(PropertyCriteria.builder().transitNumber(transitNumber).build()).isEmpty())
				.findAny();

		if (existingTransitNumberOptional.isPresent()) {
			throw new CustomException(Collections.singletonMap("INVALID TRANSIT NUMBER",
					String.format("Transit number '%s' already exists", existingTransitNumberOptional.get())));
		}
	}

	/**
	 * Validates the masterData,CitizenInfo and the authorization of the assessee
	 * for update
	 * 
	 * @param request PropertyRequest for update
	 */
	public List<Property> validateUpdateRequest(PropertyRequest request) {

		Map<String, String> errorMap = new HashMap<>();

		if (CollectionUtils.isEmpty(request.getProperties())) {
			throw new CustomException(Collections.singletonMap("NO PROPERTIES FOUND", "No properties to update"));
		}
		validateIds(request, errorMap);
		/**
		 * TO validate Owner Details
		 */
		validateOwner(request, errorMap);

		validateColony(request, errorMap);
		validateArea(request, errorMap);
		validateRentDetails(request, errorMap);
		validateNumericValueForProperty(request, errorMap);
		/**
		 * TO validate Documents
		 */
		if (!request.getProperties().get(0).getMasterDataAction().equals(""))
			validatePropertyDocuments(request, errorMap);

		// validatePayment(request, errorMap);

		List<Property> properties = request.getProperties();
		properties.forEach(property -> {
			if (!property.getTransitNumber().matches("\\d{1,4}")) {
				errorMap.put("INVALID TRANSIT NUMBER", "Transit number is not valid ");
			}
		});

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		PropertyCriteria criteria = getPropertyCriteriaForSearch(request);
		List<Property> propertiesFromSearchResponse = repository.getProperties(criteria);
		boolean ifPropertyExists = PropertyExists(propertiesFromSearchResponse);
		if (!ifPropertyExists) {
			throw new CustomException("PROPERTY NOT FOUND", "The property to be updated does not exist");
		}

		propertiesFromSearchResponse.forEach(propertySearch -> {
			request.getProperties().forEach(property -> {

				compareIds(propertySearch.getId(), property.getId(), errorMap);
				compareIds(propertySearch.getPropertyDetails().getId(), property.getPropertyDetails().getId(),
						errorMap);
				compareIds(propertySearch.getPropertyDetails().getAddress().getId(),
						property.getPropertyDetails().getAddress().getId(), errorMap);
				compareIds(propertySearch.getId(), property.getPropertyDetails().getPropertyId(), errorMap);
				compareIds(propertySearch.getId(), property.getPropertyDetails().getAddress().getPropertyId(),
						errorMap);
				compareIds(propertySearch.getTransitNumber(),
						property.getPropertyDetails().getAddress().getTransitNumber(), errorMap);
				List<String> oIdList = propertySearch.getOwners().stream().map(Owner::getId)
						.collect(Collectors.toList());
				property.getOwners().forEach(owner -> {
					if (!oIdList.contains(owner.getId())) {
						errorMap.put("INVALID ID", String.format(
								"Existing user with id '%s' is missing during update. All the owners should be present during the update API",
								owner.getId()));
					}
				});

			});
		});

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return propertiesFromSearchResponse;
	}

	/*
	 * ownership transfer get properties
	 */
	public List<Property> getPropertyForOT(OwnershipTransferRequest request) {

		Map<String, String> errorMap = new HashMap<>();

		PropertyCriteria criteria = getPropertyCriteriaForOT(request);
		List<Property> propertiesFromSearchResponse = repository.getProperties(criteria);
		boolean ifPropertyExists = PropertyExists(propertiesFromSearchResponse);
		if (!ifPropertyExists) {
			throw new CustomException("PROPERTY NOT FOUND", "The property to be updated does not exist");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return propertiesFromSearchResponse;
	}

	public List<Owner> validateUpdateRequest(OwnershipTransferRequest request) {

		Map<String, String> errorMap = new HashMap<>();

		DuplicateCopySearchCriteria criteria = getOTSearchCriteria(request);
		List<Owner> ownersFromSearchResponse = OTRepository.searchOwnershipTransfer(criteria);
		boolean ifOwnerExists = OwnerExists(ownersFromSearchResponse);
		if (!ifOwnerExists) {
			throw new CustomException("OWNER NOT FOUND", "The owner to be updated does not exist");
		}
		String action = request.getOwners().get(0).getApplicationAction();
		/**
		 * MDMS Document validation
		 */
		if (!action.equalsIgnoreCase(PTConstants.ACTION_REINITIATE)
				|| !action.equalsIgnoreCase(PTConstants.ACTION_DRAFT))
			validateOwnershipTransferDocuments(request, errorMap);
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return ownersFromSearchResponse;
	}

	public DuplicateCopySearchCriteria getOTSearchCriteria(OwnershipTransferRequest request) {
		DuplicateCopySearchCriteria searchCriteria = new DuplicateCopySearchCriteria();
		if (!CollectionUtils.isEmpty(request.getOwners())) {
			request.getOwners().forEach(owner -> {
				if (owner.getOwnerDetails().getApplicationNumber() != null)
					searchCriteria.setApplicationNumber(owner.getOwnerDetails().getApplicationNumber());
			});
		}
		return searchCriteria;
	}

	private void compareIds(String searchId, String updateId, Map<String, String> errorMap) {

		if (!(searchId.equals(updateId))) {
			errorMap.put("INVALID ID", "ID is not valid");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}

	private void validateIds(PropertyRequest request, Map<String, String> errorMap) {
		if (request.getProperties().stream().filter(property -> property.getId() == null).findAny().isPresent()) {
			throw new CustomException(
					Collections.singletonMap("INVALID PROPERTY", "Property cannot be updated without propertyId"));
		}
	}

	public PropertyCriteria getPropertyCriteriaForSearch(PropertyRequest request) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setPropertyId(request.getProperties().stream().map(Property::getId).findAny().get());
		return propertyCriteria;
	}

	public boolean PropertyExists(List<Property> responseProperties) {
		return (!CollectionUtils.isEmpty(responseProperties) && responseProperties.size() == 1);
	}

	public boolean OwnerExists(List<Owner> responseOwners) {
		return (!CollectionUtils.isEmpty(responseOwners) && responseOwners.size() == 1);
	}

	/**
	 * Validates if the mobileNumber is 10 digit and starts with 5 or greater
	 * 
	 * @param mobileNumber The mobileNumber to be validated
	 * @return True if valid mobileNumber else false
	 */
	private Boolean isMobileNumberValid(String mobileNumber) {

		if (mobileNumber == null || mobileNumber == "")
			return false;
		else if (mobileNumber.length() != 10)
			return false;
		else if (Character.getNumericValue(mobileNumber.charAt(0)) < 5)
			return false;
		else
			return true;
	}

	/**
	 * Validates if the aadharNumber is !12 digit
	 * 
	 * @param aadharNumber The aadharNumber to be validated
	 * @return True if valid aadharNumber else false
	 */
	private Boolean isAadharNumberValid(String aadharNumber) {

		if (aadharNumber == "" || aadharNumber == null)
			return true;
		else if (aadharNumber.length() != 12)
			return false;
		else
			return true;
	}

	/**
	 * Validates if the Email contains @ and .com or .xx
	 * 
	 * @param email The email to be validated
	 * @return True if valid email else false
	 */
	private Boolean isEmailValid(String email) {

		String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

		if (email == "" || email == null)
			return true;
		else if (email.length() < 2)
			return false;
		else if (!email.matches(regex))
			return false;
		else
			return true;
	}

	private Boolean isValid(String value, int i, int j) {

		if (value == null || value.length() < i || value.length() > j) {
			return false;
		}
		return true;
	}

	private Boolean isNotNullValid(Long value) {
		if (value == null) {
			return false;
		}
		return true;
	}

	public void validateDuplicateCopySearch(RequestInfo requestInfo, DuplicateCopySearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN") && criteria == null)
			throw new CustomException("INVALID SEARCH", "Search without any paramters is not allowed");
	}

	public List<DuplicateCopy> validateDuplicateCopyUpdateRequest(DuplicateCopyRequest duplicateCopyRequest) {
		Map<String, String> errorMap = new HashMap<>();

		validateDocument(duplicateCopyRequest);
		validateIds(duplicateCopyRequest);

		String action = duplicateCopyRequest.getDuplicateCopyApplications().get(0).getAction();
		/**
		 * MDMS Document validation
		 */
		if (!action.equalsIgnoreCase(PTConstants.ACTION_REINITIATE)
				|| !action.equalsIgnoreCase(PTConstants.ACTION_DRAFT))
			validateDuplicateCopyDocuments(duplicateCopyRequest, errorMap);
		String propertyId = duplicateCopyRequest.getDuplicateCopyApplications().get(0).getProperty().getId();
		DuplicateCopySearchCriteria criteria = DuplicateCopySearchCriteria.builder()
				.appId(duplicateCopyRequest.getDuplicateCopyApplications().get(0).getId()).propertyId(propertyId)
				.build();
		List<DuplicateCopy> searchedProperties = repository.getDuplicateCopyProperties(criteria);
		if (searchedProperties.size() < 1) {
			errorMap.put("PROPERTY NOT FOUND", "The property to be updated does not exist");
		}
		if (searchedProperties.size() > 1) {
			errorMap.put("INVALID PROPERTY", "Multiple property found");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return searchedProperties;
	}

	// PI validate
	public List<PropertyImages> validatePropertyImagesUpdateRequest(PropertyImagesRequest propertyImagesRequest) {
		Map<String, String> errorMap = new HashMap<>();

		validatePIDocument(propertyImagesRequest);
		validatePIIds(propertyImagesRequest);

		String propertyId = propertyImagesRequest.getPropertyImagesApplications().get(0).getProperty().getId();
		DuplicateCopySearchCriteria criteria = DuplicateCopySearchCriteria.builder()
				.appId(propertyImagesRequest.getPropertyImagesApplications().get(0).getId()).propertyId(propertyId)
				.build();
		List<PropertyImages> searchedProperties = repository.getPropertyImagesProperties(criteria);
		if (searchedProperties.size() < 1) {
			errorMap.put("PROPERTY NOT FOUND", "The property to be updated does not exist");
		}
		if (searchedProperties.size() > 1) {
			errorMap.put("INVALID PROPERTY", "Multiple property found");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return searchedProperties;
	}

	private void validateIds(DuplicateCopyRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		request.getDuplicateCopyApplications().forEach(application -> {

			if (!application.getState().equalsIgnoreCase(DuplicateCopyConstants.STATUS_INITIATED)) {
				if (application.getId() == null)
					errorMap.put("INVALID UPDATE", "Id of property cannot be null");
				if (application.getApplicant().get(0).getId() == null)
					errorMap.put("INVALID UPDATE", "Id of Applicant cannot be null");
				if (application.getProperty().getId() == null)
					errorMap.put("INVALID UPDATE", "Property Id cannot be null");
			}
		});
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	// PI Ids validate
	private void validatePIIds(PropertyImagesRequest propertyImagesRequest) {
		Map<String, String> errorMap = new HashMap<>();
		propertyImagesRequest.getPropertyImagesApplications().forEach(application -> {

			if (application.getId() == null)
				errorMap.put("INVALID UPDATE", "Id of property cannot be null");
			if (application.getProperty().getId() == null)
				errorMap.put("INVALID UPDATE", "Property Id cannot be null");
		});
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	public void validateDuplicateCopyCreateRequest(DuplicateCopyRequest duplicateCopyRequest) {
		validateDocument(duplicateCopyRequest);

	}

	private void validateDocument(DuplicateCopyRequest duplicateCopyRequest) {
		Map<String, String> errorMap = new HashMap<>();

		duplicateCopyRequest.getDuplicateCopyApplications().forEach(application -> {

			if (DuplicateCopyConstants.ACTION_SUBMIT.equalsIgnoreCase(application.getAction())) {
				if (application.getApplicationDocuments() == null)
					errorMap.put("INVALID ACTION",
							"Action cannot be changed to SUBMIT. Application document are not provided");
			}

		});

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	// PI documents validate
	private void validatePIDocument(PropertyImagesRequest propertyImagesRequest) {
		Map<String, String> errorMap = new HashMap<>();

		propertyImagesRequest.getPropertyImagesApplications().forEach(application -> {

			if (application.getApplicationDocuments() == null)
				errorMap.put("INVALID ACTION", "Action cannot be done. Application document are not provided");

		});

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	public void validateDuplicateCreate(DuplicateCopyRequest duplicateCopyRequest) {
		validateDCSpecificNotNullFields(duplicateCopyRequest);
		validateDuplicateDocuments(duplicateCopyRequest);

	}

	private void validateDCSpecificNotNullFields(DuplicateCopyRequest request) {
		request.getDuplicateCopyApplications().forEach(application -> {
			Map<String, String> errorMap = new HashMap<>();
			if (application.getApplicant().get(0).getName() == null)
				errorMap.put("NULL_NAME", " Applicant name cannot be null");
			if (application.getApplicant().get(0).getGuardian() == null)
				errorMap.put("NULL_GUARDIAN", " Applicant Father/husband name cannot be null");
			if (application.getApplicant().get(0).getPhone() == null)
				errorMap.put("NULL_MOBILENUMBER", " Mobile Number cannot be null");
			if (application.getTenantId() == null)
				errorMap.put("NULL_TENANT", " Tenant Id cannot be null");
			if (application.getProperty().getId() == null)
				errorMap.put("NULL_PROPERTYID", "PropertyId cannot be null");

			if (!errorMap.isEmpty())
				throw new CustomException(errorMap);
		});
	}

	private void validateDuplicateDocuments(DuplicateCopyRequest request) {
		request.getDuplicateCopyApplications().stream().map(DuplicateCopy::getApplicationDocuments)
				.forEach(documents -> _validateDuplicateDocuments(documents));
	}

	private void _validateDuplicateDocuments(List<Document> documents) {
		if (CollectionUtils.isEmpty(documents)) {
			return;
		}
		if (!(documents.stream().map(Document::getFileStoreId).distinct().count() == documents.size())) {
			throw new CustomException("DUPLICATE_DOCUMENT ERROR", "Same document cannot be used multiple times");
		}
	}

	public void validateDuplicateUpdate(DuplicateCopyRequest duplicateCopyRequest) {
		validateDuplicateDocuments(duplicateCopyRequest);
		validateDCSpecificNotNullFields(duplicateCopyRequest);
	}

	public List<Property> isPropertyExist(DuplicateCopyRequest duplicateCopyRequest) {

		PropertyCriteria criteria = getPropertyCriteriaForSearch(duplicateCopyRequest);
		List<Property> propertiesFromSearchResponse = repository.getProperties(criteria);
		boolean ifPropertyExists = PropertyExists(propertiesFromSearchResponse);
		if (!ifPropertyExists) {
			throw new CustomException("PROPERTY NOT FOUND", "Please provide valid property details");
		}

		return propertiesFromSearchResponse;
	}

	public List<Property> isPropertyExist(NoticeGenerationRequest noticeGenerationRequest) {

		PropertyCriteria criteria = getPropertyCriteriaForSearch(noticeGenerationRequest);
		List<Property> propertiesFromSearchResponse = repository.getProperties(criteria);
		boolean ifPropertyExists = PropertyExists(propertiesFromSearchResponse);
		if (!ifPropertyExists) {
			throw new CustomException("PROPERTY NOT FOUND", "Please provide valid property details");
		}

		return propertiesFromSearchResponse;
	}

	private PropertyCriteria getPropertyCriteriaForSearch(NoticeGenerationRequest noticeGenerationRequest) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!CollectionUtils.isEmpty(noticeGenerationRequest.getNoticeApplications())) {
			noticeGenerationRequest.getNoticeApplications().forEach(application -> {
				if (application.getProperty().getTransitNumber() != null)
					propertyCriteria.setTransitNumber(application.getProperty().getTransitNumber());
				if (application.getProperty().getId() != null)
					propertyCriteria.setPropertyId(application.getProperty().getId());
			});
		}
		return propertyCriteria;

	}

	private PropertyCriteria getPropertyCriteriaForSearch(DuplicateCopyRequest request) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!CollectionUtils.isEmpty(request.getDuplicateCopyApplications())) {
			request.getDuplicateCopyApplications().forEach(application -> {
				if (application.getProperty().getTransitNumber() != null)
					propertyCriteria.setTransitNumber(application.getProperty().getTransitNumber());
				if (application.getProperty().getColony() != null)
					propertyCriteria.setColony(application.getProperty().getColony());
				if (application.getProperty().getId() != null)
					propertyCriteria.setPropertyId(application.getProperty().getId());
				if (application.getApplicant().get(0).getName() != null)
					propertyCriteria.setName(application.getApplicant().get(0).getName());
			});
		}
		return propertyCriteria;

	}

	// PI Validation
	public List<Property> isPropertyPIExist(PropertyImagesRequest propertyImagesRequest) {

		PropertyCriteria criteria = getPropertyCriteriaForSearchPI(propertyImagesRequest);
		List<Property> propertiesFromSearchResponse = repository.getProperties(criteria);
		boolean ifPropertyExists = PropertyExists(propertiesFromSearchResponse);
		if (!ifPropertyExists) {
			throw new CustomException("PROPERTY NOT FOUND", "Please provide valid property details");
		}

		return propertiesFromSearchResponse;
	}

	private PropertyCriteria getPropertyCriteriaForSearchPI(PropertyImagesRequest propertyImagesRequest) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!CollectionUtils.isEmpty(propertyImagesRequest.getPropertyImagesApplications())) {
			propertyImagesRequest.getPropertyImagesApplications().forEach(application -> {
				if (application.getProperty().getTransitNumber() != null)
					propertyCriteria.setTransitNumber(application.getProperty().getTransitNumber());
				if (application.getProperty().getColony() != null)
					propertyCriteria.setColony(application.getProperty().getColony());
				if (application.getProperty().getId() != null)
					propertyCriteria.setPropertyId(application.getProperty().getId());
			});
		}
		return propertyCriteria;

	}

	public PropertyCriteria getPropertyCriteriaForOT(OwnershipTransferRequest request) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!CollectionUtils.isEmpty(request.getOwners())) {
			request.getOwners().forEach(owner -> {
				if (owner.getProperty().getId() != null)
					propertyCriteria.setPropertyId(owner.getProperty().getId());
			});
		}
		return propertyCriteria;
	}

	public List<Property> isPropertyExist(MortgageRequest mortgageRequest) {
		PropertyCriteria criteria = getPropertyCriteriaForSearch(mortgageRequest);
		List<Property> propertiesFromSearchResponse = repository.getProperties(criteria);
		boolean ifPropertyExists = PropertyExists(propertiesFromSearchResponse);
		if (!ifPropertyExists) {
			throw new CustomException("PROPERTY NOT FOUND", "Please provide valid property details");
		}

		return propertiesFromSearchResponse;
	}

	private PropertyCriteria getPropertyCriteriaForSearch(MortgageRequest request) {
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		if (!CollectionUtils.isEmpty(request.getMortgageApplications())) {
			request.getMortgageApplications().forEach(application -> {
				if (application.getProperty().getTransitNumber() != null)
					propertyCriteria.setTransitNumber(application.getProperty().getTransitNumber());
				if (application.getProperty().getColony() != null)
					propertyCriteria.setColony(application.getProperty().getColony());
				if (application.getProperty().getId() != null)
					propertyCriteria.setPropertyId(application.getProperty().getId());
			});
		}
		return propertyCriteria;

	}

	public void validateMortgageCreateRequest(MortgageRequest mortgageRequest) {
		validateDocument(mortgageRequest);
		validateMGSpecificNotNullFields(mortgageRequest);
		validateDuplicateMortgage(mortgageRequest);
	}

	private void validateDuplicateMortgage(MortgageRequest request) {
		DuplicateCopySearchCriteria criteria = DuplicateCopySearchCriteria.builder()
				.propertyId(request.getMortgageApplications().get(0).getProperty().getId()).build();

		// Except Reject state
		BusinessService otBusinessService = workflowService.getBusinessService(criteria.getTenantId(),
				request.getRequestInfo(), PTConstants.BUSINESS_SERVICE_MG);
		List<State> stateList = otBusinessService.getStates();
		List<String> states = new ArrayList<String>();

		for (State state : stateList) {
			states.add(state.getState());
		}
		states.remove(PTConstants.MG_REJECTED);
		log.info("states:" + states);
		criteria.setStatus(states);

		List<Mortgage> mortgageProperty = repository.getMortgageProperties(criteria);
		if (!CollectionUtils.isEmpty(mortgageProperty)) {
			throw new CustomException("MORTGAGE EXIST", "Already applied for mortgage");
		}
	}

	public void validateMortgageSearch(RequestInfo requestInfo, DuplicateCopySearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN") && criteria == null)
			throw new CustomException("INVALID SEARCH", "Search without any paramters is not allowed");

	}

	public List<Mortgage> validateMOrtgageUpdateRequest(MortgageRequest mortgageRequest) {
		Map<String, String> errorMap = new HashMap<>();

		validateDocument(mortgageRequest);
		validateIds(mortgageRequest);
		String action = mortgageRequest.getMortgageApplications().get(0).getAction();
		/**
		 * MDMS Document validation
		 */
		if (!action.equalsIgnoreCase(PTConstants.ACTION_REINITIATE)
				|| !action.equalsIgnoreCase(PTConstants.ACTION_DRAFT))
			validateMortgageDocuments(mortgageRequest, errorMap);
		String propertyId = mortgageRequest.getMortgageApplications().get(0).getProperty().getId();
		DuplicateCopySearchCriteria criteria = DuplicateCopySearchCriteria.builder()
				.appId(mortgageRequest.getMortgageApplications().get(0).getId()).propertyId(propertyId).build();
		List<Mortgage> searchedApplications = repository.getMortgageProperties(criteria);
		if (searchedApplications.size() < 1) {
			errorMap.put("PROPERTY NOT FOUND", "The property to be updated does not exist");
		}
		if (searchedApplications.size() > 1) {
			errorMap.put("INVALID PROPERTY", "Multiple property found");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return searchedApplications;
	}

	private void validateIds(MortgageRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		request.getMortgageApplications().forEach(application -> {

			if ((!application.getState().equalsIgnoreCase(DuplicateCopyConstants.STATUS_INITIATED))) {
				if (application.getId() == null)
					errorMap.put("INVALID UPDATE", "Id of property cannot be null");
				if (application.getApplicant().get(0).getId() == null)
					errorMap.put("INVALID UPDATE", "Id of Applicant cannot be null");
				if (application.getProperty().getId() == null)
					errorMap.put("INVALID UPDATE", "Property Id cannot be null");
			}
		});
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}

	private void validateDocument(MortgageRequest mortgageRequest) {
		Map<String, String> errorMap = new HashMap<>();

		mortgageRequest.getMortgageApplications().forEach(application -> {
			if (DuplicateCopyConstants.ACTION_SUBMIT.equalsIgnoreCase(application.getAction())) {
				if (application.getApplicationDocuments() == null)
					errorMap.put("INVALID ACTION",
							"Action cannot be changed to SUBMIT. Application document are not provided");
			}

		});

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

	}

	public void validateMortgageUpdate(MortgageRequest mortgageRequest) {
		validateDuplicateDocuments(mortgageRequest);
		validateMGSpecificNotNullFields(mortgageRequest);

	}

	private void validateMGSpecificNotNullFields(MortgageRequest request) {
		request.getMortgageApplications().forEach(application -> {
			Map<String, String> errorMap = new HashMap<>();
			if (application.getApplicant().get(0).getName() == null)
				errorMap.put("NULL_NAME", " Applicant name cannot be null");
			if (application.getApplicant().get(0).getGuardian() == null)
				errorMap.put("NULL_GUARDIAN", " Applicant Father/husband name cannot be null");
			if (application.getApplicant().get(0).getPhone() == null)
				errorMap.put("NULL_MOBILENUMBER", " Mobile Number cannot be null");
			if (application.getTenantId() == null)
				errorMap.put("NULL_TENANT", " Tenant Id cannot be null");
			if (application.getProperty().getId() == null)
				errorMap.put("NULL_PROPERTYID", "PropertyId cannot be null");

			if (!errorMap.isEmpty())
				throw new CustomException(errorMap);
		});

	}

	private void validateDuplicateDocuments(MortgageRequest request) {
		request.getMortgageApplications().stream().map(Mortgage::getApplicationDocuments)
				.forEach(documents -> _validateDuplicateDocuments(documents));
	}

	public List<NoticeGeneration> validateNoticeUpdateRequest(NoticeGenerationRequest noticeGenerationRequest) {
		Map<String, String> errorMap = new HashMap<>();

		String propertyId = noticeGenerationRequest.getNoticeApplications().get(0).getProperty().getId();
		NoticeSearchCriteria criteria = NoticeSearchCriteria.builder()
				.noticeId(noticeGenerationRequest.getNoticeApplications().get(0).getId()).propertyId(propertyId)
				.build();
		List<NoticeGeneration> searchedApplications = repository.getNotices(criteria);
		if (searchedApplications.size() < 1) {
			errorMap.put("NOTICE NOT FOUND", "The notice to be updated does not exist");
		}
		if (searchedApplications.size() > 1) {
			errorMap.put("INVALID NOTICE", "Multiple notice found");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		return searchedApplications;
	}

	public void validateNoticeUpdate(NoticeGenerationRequest noticeGenerationRequest) {
		validateDuplicateDocuments(noticeGenerationRequest);
		validateNoticeSpecificNotNullFields(noticeGenerationRequest);

	}

	private void validateNoticeSpecificNotNullFields(NoticeGenerationRequest request) {
		request.getNoticeApplications().forEach(notice -> {
			Map<String, String> errorMap = new HashMap<>();
			if (notice.getGuardian() == null)
				errorMap.put("NULL_GUARDIAN", " Applicant Father/husband name cannot be null");
			if (notice.getTenantId() == null)
				errorMap.put("NULL_TENANT", " Tenant Id cannot be null");
			if (notice.getProperty().getId() == null)
				errorMap.put("NULL_PROPERTYID", "PropertyId cannot be null");

			if (!errorMap.isEmpty())
				throw new CustomException(errorMap);
		});

	}

	private void validateDuplicateDocuments(NoticeGenerationRequest request) {
		request.getNoticeApplications().stream().map(NoticeGeneration::getApplicationDocuments)
				.forEach(documents -> _validateDuplicateDocuments(documents));
	}

	private void validatePropertyDocuments(PropertyRequest request, Map<String, String> errorMap) {
		request.getProperties().forEach(property -> {
			this.validateDocumentsOnType(request.getRequestInfo(), property.getTenantId(),
					property.getPropertyDetails().getApplicationDocuments(), errorMap, PTConstants.BUSINESS_SERVICE_PM);
		});
	}

	@SuppressWarnings("unchecked")
	private void validateDocumentsOnType(RequestInfo requestInfo, String tenantId, List<Document> documents,
			Map<String, String> errorMap, String code) {

		tenantId = tenantId.split("\\.")[0];
		String filter = "[?(@.code=='" + code + "')].documentList";

		/**
		 * Get list of application documents from MDMS
		 */
		List<Map<String, Object>> data = (List<Map<String, Object>>) mdmsService.getMDMSResponse(requestInfo, tenantId,
				PTConstants.MDMS_PT_MOD_NAME, "applications", filter, PTConstants.JSONPATH_CODES + ".applications");
		List<Map<String, Object>> mdmsDocuments = (List<Map<String, Object>>) (data.get(0));

		/**
		 * Filter the mdms document types for the ones that are required. For each
		 * required document code, verify if it is present in the incoming documents.
		 */
		mdmsDocuments.stream().filter(d -> ((boolean) (d.get("required")))).map(d -> d.get("code"))
				.forEach(documentCode -> {
					long count = documents.stream().filter(Document::getActive).map(Document::getDocumentType)
							.filter(type -> type.equalsIgnoreCase(String.valueOf(documentCode))).count();
					if (count == 0) {
						errorMap.put("REQUIRED DOCUMENT NOT FOUND",
								String.format("The document '%s' is required but it is not present",
										notificationUtil.getMessageTemplate("RP_"+documentCode, notificationUtil.getLocalizationMessages("ch.chandigarh", requestInfo))));
					}
					if (count >= 2) {
						errorMap.put("DUPLICATE DOCUMENT",String.format("The document '%s' is found more than once.",
								notificationUtil.getMessageTemplate("RP_"+documentCode, notificationUtil.getLocalizationMessages("ch.chandigarh", requestInfo))));
					}
				});
	}

	private void validateMortgageDocuments(MortgageRequest request, Map<String, String> errorMap) {
		request.getMortgageApplications().forEach(mortgage -> {
			this.validateDocumentsOnType(request.getRequestInfo(), mortgage.getTenantId(),
					mortgage.getApplicationDocuments(), errorMap, PTConstants.BUSINESS_SERVICE_MG_RP);
		});
	}

	private void validateDuplicateCopyDocuments(DuplicateCopyRequest request, Map<String, String> errorMap) {
		request.getDuplicateCopyApplications().forEach(duplicateCopy -> {
			this.validateDocumentsOnType(request.getRequestInfo(), duplicateCopy.getTenantId(),
					duplicateCopy.getApplicationDocuments(), errorMap, PTConstants.BUSINESS_SERVICE_DC_RP);
		});
	}

	private void validateOwnershipTransferDocuments(OwnershipTransferRequest request, Map<String, String> errorMap) {
		request.getOwners().forEach(owner -> {
			this.validateDocumentsOnType(request.getRequestInfo(), owner.getTenantId(),
					owner.getOwnerDetails().getOwnershipTransferDocuments(), errorMap,
					PTConstants.BUSINESS_SERVICE_FL_RP);
		});
	}

	@SuppressWarnings("unchecked")
	public void validatePropertyImagesUpdateLimit(PropertyImagesRequest propertyImagesRequest) {
		String tenantId = propertyImagesRequest.getPropertyImagesApplications().get(0).getTenantId().split("\\.")[0];
		String filter = "[?(@.code=='" + PTConstants.BUSINESS_SERVICE_PM + "')].documentList";

		/**
		 * Get list of application documents from MDMS
		 */
		List<Map<String, Object>> data = (List<Map<String, Object>>) mdmsService.getMDMSResponse(
				propertyImagesRequest.getRequestInfo(), tenantId, PTConstants.MDMS_PT_MOD_NAME, "applications", filter,
				PTConstants.JSONPATH_CODES + ".applications");
		List<Map<String, Object>> mdmsDocuments = (List<Map<String, Object>>) (data.get(0));

		Integer maxValue = (Integer) mdmsDocuments.stream()
				.filter(document -> document.get("code").equals("TRANSIT_SITE_IMAGES"))
				.map(document -> document.get("maxCount")).findFirst().get();
		Integer minValue = (Integer) mdmsDocuments.stream()
				.filter(document -> document.get("code").equals("TRANSIT_SITE_IMAGES"))
				.map(document -> document.get("minCount")).findFirst().get();

		Map<String, String> errorMap = new HashMap<>();
		propertyImagesRequest.getPropertyImagesApplications().forEach(application -> {
			if (CollectionUtils.isEmpty(application.getApplicationDocuments())) {
				errorMap.put("PROPERTY_IMAGES_NOT_FOUND", "Atleast 1 image should be uploaded");
			} else if (application.getApplicationDocuments().size() > maxValue.intValue()) {
				errorMap.put("PROPERTY_IMAGES_LIMIT_EXCEEDS",
						"Property images upload limit exceeds max limit " + maxValue);
			} else if (application.getApplicationDocuments().size() < minValue.intValue()) {
				errorMap.put("PROPERTY_IMAGES_LIMIT_NOT_REACHED",
						"Property images uploaded should be minimum of " + minValue);
			}
		});

		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}

	}

	@SuppressWarnings("unchecked")
	public void validateNoticeGenerationDocumentUploadLimit(NoticeGenerationRequest noticeGenerationRequest) {
		String tenantId = noticeGenerationRequest.getNoticeApplications().get(0).getTenantId().split("\\.")[0];
		String filter = "[?(@.code=='" + PTConstants.BUSINESS_SERVICE_PM + "')].documentList";

		/**
		 * Get list of application documents from MDMS
		 */
		List<Map<String, Object>> data = (List<Map<String, Object>>) mdmsService.getMDMSResponse(
				noticeGenerationRequest.getRequestInfo(), tenantId, PTConstants.MDMS_PT_MOD_NAME, "applications", filter,
				PTConstants.JSONPATH_CODES + ".applications");
		List<Map<String, Object>> mdmsDocuments = (List<Map<String, Object>>) (data.get(0));

		Integer maxValue = (Integer) mdmsDocuments.stream()
				.filter(document -> document.get("code").equals("TRANSIT_SITE_IMAGES"))
				.map(document -> document.get("maxCount")).findFirst().get();
		Integer minValue = (Integer) mdmsDocuments.stream()
				.filter(document -> document.get("code").equals("TRANSIT_SITE_IMAGES"))
				.map(document -> document.get("minCount")).findFirst().get();

		Map<String, String> errorMap = new HashMap<>();
		noticeGenerationRequest.getNoticeApplications().forEach(application -> {
			if (CollectionUtils.isEmpty(application.getApplicationDocuments())) {
				errorMap.put("PROPERTY_IMAGES_NOT_FOUND", "Atleast 1 image should be uploaded");
			} else if (application.getApplicationDocuments().size() > maxValue.intValue()) {
				errorMap.put("PROPERTY_IMAGES_LIMIT_EXCEEDS",
						"Property images upload limit exceeds max limit " + maxValue);
			} else if (application.getApplicationDocuments().size() < minValue.intValue()) {
				errorMap.put("PROPERTY_IMAGES_LIMIT_NOT_REACHED",
						"Property images uploaded should be minimum of " + minValue);
			}
		});
		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}
	}

	private void validateNumericValueForProperty(PropertyRequest request, Map<String, String> errorMap) {
		List<Property> properties = request.getProperties();
		properties.forEach(property -> {
			if (!isNumeric(property.getPropertyDetails().getFloors())) {
				errorMap.put("INVALID FLOORS", "Invalid floors");
			}
			if (!isNumeric(property.getPropertyDetails().getRentPerSqyd())) {
				errorMap.put("INVALID RENT PER SQYD", "Invalid rent per sqyd");
			}
			if (!isNumeric(property.getPropertyDetails().getArea())) {
				errorMap.put("INVALID AREA", "Invalid AREA");
			}
			property.getOwners().forEach(owner -> {
				if (!isNumeric(owner.getOwnerDetails().getMonthlyRent())) {
					errorMap.put("INVALID MONTHLY RENT", "Invalid monthly rent");
				}
				if (!isNumeric(owner.getOwnerDetails().getRevisionPeriod())) {
					errorMap.put("INVALID REVISION PERIOD", "Invalid revision period");
				}
				if (!isNumeric(owner.getOwnerDetails().getRevisionPercentage())) {
					errorMap.put("INVALID REVISION PERCENTAGE", "Invalid revision percentage");
				}
			});
		});
	}

	public void validatePropertyRentDetails(OwnershipTransferRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		List<Owner> owners = request.getOwners();
		owners.forEach(owner -> {
			if (!isNumeric(owner.getOwnerDetails().getMonthlyRent())) {
				errorMap.put("INVALID MONTHLY RENT", "Invalid monthly rent");
			}
			if (!isNumeric(owner.getOwnerDetails().getRevisionPeriod())) {
				errorMap.put("INVALID REVISION PERIOD", "Invalid revision period");
			}
			if (!isNumeric(owner.getOwnerDetails().getRevisionPercentage())) {
				errorMap.put("INVALID REVISION PERCENTAGE", "Invalid revision percentage");
			}
		});
		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}
	}

	private Boolean isNumeric(String value) {
		if (value != null && !value.matches("(\\d+)?(\\.\\d+)?")) {
			return false;
		}
		return true;
	}

}
