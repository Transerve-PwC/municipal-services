package org.egov.pt.util;

import static org.egov.pt.util.PTConstants.ASMT_MODULENAME;
import static org.egov.pt.util.PTConstants.BILL_AMOUNT_PATH;
import static org.egov.pt.util.PTConstants.BILL_NODEMAND_ERROR_CODE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Address;
import org.egov.pt.models.Locality;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.enums.CreationReason;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.workflow.ProcessInstance;
import org.egov.pt.models.workflow.ProcessInstanceRequest;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.ServiceCallException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PropertyUtil extends CommonUtils {

	@Autowired
	private PropertyConfiguration configs;

	@Autowired
	private ServiceRequestRepository restRepo;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Populates the owner fields inside of property objects from the response by
	 * user api
	 * 
	 * Ignoring if now user is not found in user response, no error will be thrown
	 * 
	 * @param userDetailResponse response from user api which contains list of user
	 *                           which are used to populate owners in properties
	 * @param properties         List of property whose owner's are to be populated
	 *                           from userDetailResponse
	 */
	public void enrichOwner(UserDetailResponse userDetailResponse, List<Property> properties, Boolean isSearchOpen) {

		List<OwnerInfo> users = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToOwnerMap = new HashMap<>();
		users.forEach(user -> userIdToOwnerMap.put(user.getUuid(), user));

		properties.forEach(property -> {

			property.getOwners().forEach(owner -> {

				if (userIdToOwnerMap.get(owner.getUuid()) == null)
					log.info("OWNER SEARCH ERROR",
							"The owner with UUID : \"" + owner.getUuid() + "\" for the property with Id \""
									+ property.getPropertyId() + "\" is not present in user search response");
				else {

					OwnerInfo info = userIdToOwnerMap.get(owner.getUuid());
					if (isSearchOpen) {
						owner.addUserDetail(getMaskedOwnerInfo(info));
					} else {
						owner.addUserDetail(info);
					}
				}
			});
		});
	}

	/**
	 * nullifying the PII's for open search
	 * 
	 * @param info
	 * @return
	 */
	private org.egov.pt.models.user.User getMaskedOwnerInfo(OwnerInfo info) {

		info.setMobileNumber(null);
		info.setUuid(null);
		info.setUserName(null);
		info.setGender(null);
		info.setAltContactNumber(null);
		info.setPwdExpiryDate(null);

		return info;
	}

	public ProcessInstanceRequest getProcessInstanceForMutationPayment(PropertyRequest propertyRequest) {

		Property property = propertyRequest.getProperty();

		ProcessInstance process = ProcessInstance.builder().businessService(configs.getMutationWfName())
				.businessId(property.getAcknowldgementNumber()).comment("Payment for property processed")
				.moduleName(PTConstants.ASMT_MODULENAME).tenantId(property.getTenantId()).action(PTConstants.ACTION_PAY)
				.build();

		return ProcessInstanceRequest.builder().requestInfo(propertyRequest.getRequestInfo())
				.processInstances(Arrays.asList(process)).build();
	}

	public ProcessInstanceRequest getWfForPropertyRegistry(PropertyRequest request,
			CreationReason creationReasonForWorkflow) {

		Property property = request.getProperty();
		ProcessInstance wf = null != property.getWorkflow() ? property.getWorkflow() : new ProcessInstance();

		wf.setBusinessId(property.getAcknowldgementNumber());
		wf.setTenantId(property.getTenantId());

		switch (creationReasonForWorkflow) {

		case CREATE:

			wf.setBusinessService(configs.getCreatePTWfName());
			wf.setModuleName(configs.getPropertyModuleName());
			wf.setAction("OPEN");
			break;

		case UPDATE:
			break;

		case MUTATION:
			break;

		default:
			break;
		}

		property.setWorkflow(wf);
		return ProcessInstanceRequest.builder().processInstances(Arrays.asList(wf))
				.requestInfo(request.getRequestInfo()).build();
	}

	/**
	 * 
	 * @param request
	 * @param propertyFromSearch
	 */
	public void mergeAdditionalDetails(PropertyRequest request, Property propertyFromSearch) {

		request.getProperty().setAdditionalDetails(
				jsonMerge(propertyFromSearch.getAdditionalDetails(), request.getProperty().getAdditionalDetails()));
	}

	/**
	 * Setting the uuid of old peoprty record to the new record
	 * 
	 * @param request
	 * @param uuid
	 * @return
	 */
	public JsonNode saveOldUuidToRequest(PropertyRequest request, String uuid) {

		ObjectNode objectNodeDetail;
		JsonNode additionalDetails = request.getProperty().getAdditionalDetails();

		if (null == additionalDetails || (null != additionalDetails && additionalDetails.isNull())) {
			objectNodeDetail = mapper.createObjectNode();

		} else {

			objectNodeDetail = (ObjectNode) additionalDetails;
		}
		request.getProperty().setAdditionalDetails(objectNodeDetail);
		return objectNodeDetail.put(PTConstants.PREVIOUS_PROPERTY_PREVIOUD_UUID, uuid);
	}

	public void clearSensitiveDataForPersistance(Property property) {
		property.getOwners().forEach(owner -> owner.setMobileNumber(null));
	}

	/**
	 * Utility method to fetch bill for validation of payment
	 * 
	 * @param propertyId
	 * @param tenantId
	 * @param request
	 */
	public Boolean isBillUnpaid(String propertyId, String tenantId, RequestInfo request) {

		Object res = null;

		StringBuilder uri = new StringBuilder(configs.getEgbsHost()).append(configs.getEgbsFetchBill())
				.append("?tenantId=").append(tenantId).append("&consumerCode=").append(propertyId)
				.append("&businessService=").append(ASMT_MODULENAME);

		try {
			res = restRepo.fetchResult(uri, new RequestInfoWrapper(request)).get();
		} catch (ServiceCallException e) {

			if (!e.getError().contains(BILL_NODEMAND_ERROR_CODE))
				throw e;
		}

		if (res != null) {
			JsonNode node = mapper.convertValue(res, JsonNode.class);
			Double amount = node.at(BILL_AMOUNT_PATH).asDouble();
			return amount > 0;
		}
		return false;
	}

	/**
	 * Public method to infer whether the search is for open or authenticated user
	 * 
	 * @param userInfo
	 * @return
	 */
	public Boolean isPropertySearchOpen(User userInfo) {

		return userInfo.getType().equalsIgnoreCase("SYSTEM")
				&& userInfo.getRoles().stream().map(Role::getCode).collect(Collectors.toSet()).contains("ANONYMOUS");
	}

	public PropertyRequest sendOTP(@Valid PropertyRequest propertyRequest) {

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(configs.getPropertyTaxHost() + configs.getPropertySendOTP())
				.queryParam(PTConstants.PARAM_UID, propertyRequest.getProperty().getPropertyUID())
				.queryParam(PTConstants.PARAM_MOBILE_NUMBER, propertyRequest.getProperty().getPropertyMobileNum());

		Object res = restRepo.fetchGetResult(new StringBuilder(builder.toUriString()));
		JsonNode node = mapper.convertValue(res, JsonNode.class);
		JsonNode json = node.get(0);

		propertyRequest.getProperty().setPropertyMessage(json.get(PTConstants.Message).asText());

		propertyRequest.getProperty().setPropertyTokenId(json.get(PTConstants.PARAM_TOKENID).asText());

		propertyRequest.getProperty().setPropertyApiStatus(json.get(PTConstants.PARAM_STATUS).asText());

		return propertyRequest;

	}

	public PropertyRequest verifyOTP(@Valid PropertyRequest propertyRequest) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromUriString(configs.getPropertyTaxHost() + configs.getPropertyVerifyOTP())
					.queryParam(PTConstants.PARAM_UID, propertyRequest.getProperty().getPropertyUID())
					.queryParam(PTConstants.PARAM_MOBILE_NUMBER, propertyRequest.getProperty().getPropertyMobileNum())
					.queryParam(PTConstants.PARAM_OTP, propertyRequest.getProperty().getPropertyOtp())
					.queryParam(PTConstants.PARAM_TOKENID, propertyRequest.getProperty().getPropertyTokenId());

			Object res = restRepo.fetchGetResult(new StringBuilder(builder.toUriString()));
			JsonNode node = mapper.convertValue(res, JsonNode.class);
			propertyRequest.getProperty().setPropertyMessage(node.get(0).get(PTConstants.Message).asText());
			propertyRequest.getProperty().setPropertyApiStatus(node.get(0).get(PTConstants.PARAM_STATUS).asText());
			JsonNode json = mapper.readTree(node.get(0).get(PTConstants.PROPERTY_DETAILS).asText());
			if (!json.isEmpty()) {
				JsonNode propertydetails = json.get(0);
				ArrayList<OwnerInfo> owners = new ArrayList<OwnerInfo>();
				OwnerInfo owner = new OwnerInfo();
				Address address = new Address();
				Locality locality = new Locality();
				int propertyid = propertydetails.get("PROPERTYTypeId").asInt();
				if(propertyid == 1 || propertyid == 2) {
					propertyRequest.getProperty().setPropertyType(PTConstants.PROPERTY_DOMESTIC);
				}
				else if(propertyid == 3 || propertyid == 5) {
					propertyRequest.getProperty().setPropertyType(PTConstants.PROPERTY_INSTITUTIONAL);
				}
				else if(propertyid == 4) {
					propertyRequest.getProperty().setPropertyType(PTConstants.PROPERTY_COMMERCIAL);
				}
				propertyRequest.getProperty().setLandArea(
						Double.parseDouble(propertydetails.get(PTConstants.TotalCoveredareainsqft).asText()));
				propertyRequest.getProperty().setOwnershipCategory(PTConstants.DEFAULTOWNER);

				address.setCity(PTConstants.CITY);
				address.setDoorNo(propertydetails.get(PTConstants.HOUSENO).asText());

				locality.setCode(propertydetails.get(PTConstants.SectorId).asText());
				locality.setName(propertydetails.get(PTConstants.SECTORNAME).asText());
				address.setLocality(locality);
				propertyRequest.getProperty().setAddress(address);

				owner.setName(propertydetails.get(PTConstants.DepositerName).asText());
				//owner.setMobileNumber(propertydetails.get(PTConstants.MOBILE).asText());
				owner.setMobileNumber(propertyRequest.getRequestInfo().getUserInfo().getMobileNumber());
				owner.setFatherOrHusbandName(propertydetails.get(PTConstants.FATHERNAME).asText());
				owner.setStatus(Status.ACTIVE);
				owner.setCorrespondenceAddress(
						address.getDoorNo() + ", " + locality.getName() + ", " + PTConstants.CITY);
				owners.add(owner);
				//propertyRequest.getProperty().setCreationReason(CreationReason.CREATE);
				propertyRequest.getProperty().setOwners(owners);

			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return propertyRequest;
	}
}
