package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.AuctionBidder;
import org.egov.ps.model.Document;
import org.egov.ps.model.MortgageDetails;
import org.egov.ps.model.Notifications;
import org.egov.ps.model.NotificationsEmail;
import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.model.Payment;
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
import org.egov.ps.web.contracts.AuctionSaveRequest;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnrichmentService {

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

	public void enrichPropertyRequest(PropertyRequest request) {

		RequestInfo requestInfo = request.getRequestInfo();

		if (!CollectionUtils.isEmpty(request.getProperties())) {
			request.getProperties().forEach(property -> {
				AuditDetails propertyAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
						property.getId() == null);

				if (property.getId() == null) {
					property.setId(UUID.randomUUID().toString());
					property.setState(PSConstants.PM_DRAFTED);
					property.setFileNumber(property.getFileNumber().toUpperCase());
				}

				property.setAuditDetails(propertyAuditDetails);

				enrichPropertyDetail(property, requestInfo);

			});
		}
	}

	private void enrichPropertyDetail(Property property, RequestInfo requestInfo) {

		PropertyDetails propertyDetail = property.getPropertyDetails();

		AuditDetails propertyDetailsAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
				propertyDetail.getId() == null);

		if (propertyDetail.getId() == null) {
			propertyDetail.setId(UUID.randomUUID().toString());
			propertyDetail.setTenantId(property.getTenantId());
			propertyDetail.setPropertyId(property.getId());
		}
		propertyDetail.setAuditDetails(propertyDetailsAuditDetails);

		enrichOwners(property, requestInfo);
		enrichCourtCases(property, requestInfo);
		enrichPaymentDetails(property, requestInfo);
		enrichBidders(property, requestInfo);
		enrichEstateDemand(property, requestInfo);
		enrichEstatePayment(property, requestInfo);

	}

	private void enrichOwners(Property property, RequestInfo requestInfo) {

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {

			property.getPropertyDetails().getOwners().forEach(owner -> {

				if (owner.getId() == null) {

					owner.setId(UUID.randomUUID().toString());
					owner.setTenantId(property.getTenantId());
					owner.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails ownerAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				owner.setAuditDetails(ownerAuditDetails);
				enrichOwnerDetail(property, owner, requestInfo);

			});
		}
	}

	private void enrichOwnerDetail(Property property, Owner owner, RequestInfo requestInfo) {

		OwnerDetails ownerDetails = owner.getOwnerDetails();

		if (ownerDetails.getId() == null || ownerDetails.getId().isEmpty()) {

			ownerDetails.setId(UUID.randomUUID().toString());
			ownerDetails.setTenantId(property.getTenantId());
			ownerDetails.setOwnerId(owner.getId());

		}

		AuditDetails ownerDetailsAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		ownerDetails.setAuditDetails(ownerDetailsAuditDetails);
		enrichOwnerDocs(property, requestInfo);

	}

	private void enrichOwnerDocs(Property property, RequestInfo requestInfo) {
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {

			property.getPropertyDetails().getOwners().forEach(owner -> {
				List<Document> ownerDocuments = owner.getOwnerDetails().getOwnerDocuments();

				if (!CollectionUtils.isEmpty(ownerDocuments)) {
					ownerDocuments.forEach(document -> {
						if (document.getId() == null || document.getId().isEmpty()) {
							document.setId(UUID.randomUUID().toString());
						}
						document.setTenantId(property.getTenantId());
						document.setReferenceId(owner.getOwnerDetails().getId());
						document.setPropertyId(property.getId());
						AuditDetails docAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
						document.setAuditDetails(docAuditDetails);
					});
				}
			});
		}
	}

	private void enrichCourtCases(Property property, RequestInfo requestInfo) {

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getCourtCases())) {

			property.getPropertyDetails().getCourtCases().forEach(courtCase -> {

				if (courtCase.getId() == null || courtCase.getId().isEmpty()) {

					courtCase.setId(UUID.randomUUID().toString());
					courtCase.setTenantId(property.getTenantId());
					courtCase.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails courtCaseAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				courtCase.setAuditDetails(courtCaseAuditDetails);

			});
		}
	}

	private void enrichPaymentDetails(Property property, RequestInfo requestInfo) {

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getOwners())) {
			property.getPropertyDetails().getOwners().forEach(owner -> {

				List<Payment> payments = property.getPropertyDetails().getPaymentDetails();
				if (!CollectionUtils.isEmpty(payments)) {

					payments.forEach(payment -> {
						if (payment.getId() == null || payment.getId().isEmpty()) {
							AuditDetails paymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
									true);
							String gen_payment_detail_id = UUID.randomUUID().toString();
							payment.setId(gen_payment_detail_id);
							payment.setTenantId(property.getTenantId());
							payment.setOwnerDetailsId(owner.getOwnerDetails().getId());
							payment.setAuditDetails(paymentAuditDetails);
						} else {
							AuditDetails paymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
									true);
							payment.setAuditDetails(paymentAuditDetails);
						}
					});
				}
			});
		}
	}

	private void enrichBidders(Property property, RequestInfo requestInfo) {

		/**
		 * Delete existing data as new data is coming in.
		 */
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getBidders())) {

			boolean hasAnyNewBidder = property.getPropertyDetails().getBidders().stream()
					.filter(bidder -> bidder.getId() == null || bidder.getId().isEmpty()).findAny().isPresent();

			if (hasAnyNewBidder) {
				List<AuctionBidder> existingBidders = propertyRepository.getBiddersForPropertyDetailsIds(
						Collections.singletonList(property.getPropertyDetails().getId()));
				property.getPropertyDetails().setInActiveBidders(existingBidders);
			} else {
				property.getPropertyDetails().setInActiveBidders(Collections.emptyList());
			}
		}

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getBidders())) {

			property.getPropertyDetails().getBidders().forEach(bidder -> {

				if (bidder.getId() == null) {

					bidder.setId(UUID.randomUUID().toString());
					bidder.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails buidderAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				bidder.setAuditDetails(buidderAuditDetails);

			});
		}

	}

	private void enrichEstateDemand(Property property, RequestInfo requestInfo) {

		/**
		 * Delete existing data as new data is coming in.
		 */
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateDemands())) {

			boolean hasAnyNewEstateDemands = property.getPropertyDetails().getEstateDemands().stream()
					.filter(estateDemand -> estateDemand.getId() == null || estateDemand.getId().isEmpty()).findAny()
					.isPresent();

			if (hasAnyNewEstateDemands) {
				List<EstateDemand> existingEstateDemands = propertyRepository.getDemandDetailsForPropertyDetailsIds(
						Collections.singletonList(property.getPropertyDetails().getId()));
				property.getPropertyDetails().setInActiveEstateDemands(existingEstateDemands);
			} else {
				property.getPropertyDetails().setInActiveEstateDemands(Collections.emptyList());
			}
		}

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateDemands())) {

			property.getPropertyDetails().getEstateDemands().forEach(estateDemand -> {

				if (estateDemand.getId() == null) {

					estateDemand.setId(UUID.randomUUID().toString());
					estateDemand.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails estateDemandAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
				estateDemand.setAuditDetails(estateDemandAuditDetails);

			});
		}

	}

	private void enrichEstatePayment(Property property, RequestInfo requestInfo) {

		/**
		 * Delete existing data as new data is coming in.
		 */
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstatePayments())) {

			boolean hasAnyNewEstatePayments = property.getPropertyDetails().getEstatePayments().stream()
					.filter(estatePayment -> estatePayment.getId() == null || estatePayment.getId().isEmpty()).findAny()
					.isPresent();

			if (hasAnyNewEstatePayments) {
				List<EstatePayment> existingEstatePayments = propertyRepository.getEstatePaymentsForPropertyDetailsIds(
						Collections.singletonList(property.getPropertyDetails().getId()));
				property.getPropertyDetails().setInActiveEstatePayments(existingEstatePayments);
			} else {
				property.getPropertyDetails().setInActiveEstatePayments(Collections.emptyList());
			}
		}

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstatePayments())) {

			property.getPropertyDetails().getEstatePayments().forEach(estatePayment -> {

				if (estatePayment.getId() == null) {

					estatePayment.setId(UUID.randomUUID().toString());
					estatePayment.setPropertyDetailsId(property.getPropertyDetails().getId());

				}
				AuditDetails estatePaymentAuditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid(),
						true);
				estatePayment.setAuditDetails(estatePaymentAuditDetails);

			});
		}

	}

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

	/**
	 * Invoke process notification on each application in the request
	 */
	public void enrichProcessNotifications(ApplicationRequest request) {
		request.getApplications().forEach(application -> {
			/**
			 * Get the notification config from mdms.
			 */
			List<Map<String, Object>> notificationConfigs = mdmsservice.getNotificationConfig(
					application.getApplicationType(), request.getRequestInfo(), application.getTenantId(), application);
			/**
			 * Process the notification config
			 */
			processNotification(notificationConfigs, application);
		});
	}

	public void processNotification(List<Map<String, Object>> rawNotificationsList, Application enrichedApplication) {
		if (CollectionUtils.isEmpty(rawNotificationsList)) {
			log.debug("No notifications configured in MDMS for application no {} for state {}",
					enrichedApplication.getApplicationNumber(), enrichedApplication.getState());
			return;
		}
		ObjectMapper mapper = new ObjectMapper();
		List<Notifications> notificationList = mapper.convertValue(rawNotificationsList,
				new TypeReference<List<Notifications>>() {
				});

		/**
		 * Filter notification object relevant to current state of application.
		 */
		Optional<Notifications> notificationOptional = notificationList.stream()
				.filter(x -> x.getState().equalsIgnoreCase(enrichedApplication.getState())).findAny();
		if (!notificationOptional.isPresent()) {
			log.debug("No notification configured for application no {} for state {}",
					enrichedApplication.getApplicationNumber(), enrichedApplication.getState());
			return;
		}
		Notifications notification = notificationOptional.get();

		try {
			String applicationJsonString = mapper.writeValueAsString(enrichedApplication);
			String contentWithPathsEnriched = enrichPathPatternsWithApplication(notification.getContent(),
					applicationJsonString);
			String enrichedContent = enrichLocalizationPatternsInString(contentWithPathsEnriched);

			NotificationsEmail emailConfig = notification.getModes().getEmail();
			if (emailConfig.isEnabled()) {
				emailConfig.getTo();
				System.out.println("Sending email to ");
			}
		} catch (JsonProcessingException e) {
			log.error("Could not convert enrichedApplication to JSON", e);
		}
	}

	private String enrichPathPatternsWithApplication(String sourceString, String applicationJsonString) {
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher m = p.matcher(sourceString);
		Set<String> allMatches = new HashSet<String>();
		while (m.find()) {
			allMatches.add(m.group());
		}

		String replacedString = allMatches.stream().reduce(sourceString, (result, match) -> {
			String path = match.substring(1, match.length() - 1);
			Object value = (JsonPath.read(applicationJsonString, path));
			return result.replaceAll(String.format("\\{%s\\}", path), "" + value);
		});
		log.debug("Modified {} to {} ", sourceString, replacedString);
		return replacedString;
	}

	private String enrichLocalizationPatternsInString(String sourceString) {
		return sourceString;
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

				List<Document> applicationDocuments = application.getApplicationDocuments();
				if (!CollectionUtils.isEmpty(applicationDocuments)) {
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
				}
				enrichGenerateDemand(application);
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

	private void enrichGenerateDemand(Application application) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();

		if (application.getState().contains(PSConstants.EM_STATE_PENDING_DA_FEE)) {

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

	public void enrichUpdateAuctionRequest(AuctionSaveRequest request, List<AuctionBidder> auctionFromSearch) {
		RequestInfo requestInfo = request.getRequestInfo();
		AuditDetails auditDetails = util.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), false);

		if (!CollectionUtils.isEmpty(request.getAuctions())) {
			request.getAuctions().forEach(auction -> {
				auction.getAuditDetails().setLastModifiedBy(auditDetails.getLastModifiedBy());
				auction.getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
			});
		}
	}
}
