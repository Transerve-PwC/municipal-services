package org.egov.ps.util;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.Owner;
import org.egov.ps.model.Property;
import org.egov.ps.model.notification.uservevents.Action;
import org.egov.ps.model.notification.uservevents.ActionItem;
import org.egov.ps.model.notification.uservevents.Event;
import org.egov.ps.model.notification.uservevents.EventRequest;
import org.egov.ps.model.notification.uservevents.Recepient;
import org.egov.ps.model.notification.uservevents.Source;
import org.egov.ps.producer.Producer;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.BusinessService;
import org.egov.ps.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Util {

	@Autowired
	private Configuration config;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private Producer producer;

	public AuditDetails getAuditDetails(String by, Boolean isCreate) {

		Long time = System.currentTimeMillis();
		if (isCreate)
			return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}

	public MdmsCriteriaReq prepareMdMsRequest(String tenantId, String moduleName, List<String> names, String filter,
			RequestInfo requestInfo) {

		List<MasterDetail> masterDetails = new ArrayList<>();

		names.forEach(name -> {
			masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});

		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(moduleName).masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * Creates demand Search url based on tenanatId,businessService and ConsumerCode
	 *
	 * @return demand search url
	 */
	public String getDemandSearchURL() {
		StringBuilder url = new StringBuilder(config.getBillingHost());
		url.append(config.getDemandSearchEndpoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("businessService=");
		url.append("{2}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{3}");
		return url.toString();
	}

	/**
	 * Creates a map of id to isStateUpdatable
	 *
	 * @param searchresult    Licenses from DB
	 * @param businessService The businessService configuration
	 * @return Map of is to isStateUpdatable
	 */
	public Map<String, Boolean> getIdToIsStateUpdatableMap(BusinessService businessService,
			List<Application> searchresult) {
		Map<String, Boolean> idToIsStateUpdatableMap = new HashMap<>();
		searchresult.forEach(result -> {
			if (result.getState().equals("")) {
				idToIsStateUpdatableMap.put(result.getId(), true);
			} else {
				idToIsStateUpdatableMap.put(result.getId(),
						workflowService.isStateUpdatable(result.getState(), businessService));
			}
		});
		return idToIsStateUpdatableMap;
	}

	public Owner getCurrentOwnerFromProperty(Property property) {
		/**
		 * Validate that there is an existing active owner.
		 */
		Optional<Owner> currentOwnerOptional = property.getPropertyDetails().getOwners().stream()
				.filter(owner -> owner.getOwnerDetails().getIsCurrentOwner()).findFirst();

		if (!currentOwnerOptional.isPresent()) {
			throw new CustomException(Collections.singletonMap("PROPERTY_OWNER_NOT_FOUND",
					"Could not find current owner for property with id " + property.getId()));
		}

		return currentOwnerOptional.get();
	}

	/**
	 * Generates a new consumer code from a transit number to be sent while creating
	 * a bill.
	 *
	 * @param fileNumber
	 * @return
	 */
	public String getPropertyRentConsumerCode(String fileNumber) {
		return String.format("SITE-%s-%s", fileNumber.trim().toUpperCase(), dateFormat.format(new Date()));
	}

	public String getPropertyPenaltyConsumerCode(String fileNumber) {
		return String.format("ES-PN-%s-%s", fileNumber.toUpperCase(), dateFormat.format(new Date()));
	}

	public String getExtensionFeeConsumerCode(String fileNumber) {
		return String.format("ES-EF-%s-%s", fileNumber.toUpperCase(), dateFormat.format(new Date()));
	}

	public String getSecurityDepositConsumerCode(String fileNumber) {
		return String.format("ES-SD-%s-%s", fileNumber.toUpperCase(), dateFormat.format(new Date()));
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD-HH-MM-SS");

	public String getStateLevelTenantId(String tenantId) {
		String[] components = tenantId.split(".");
		if (components.length == 0) {
			return "ch";
		}
		return components[0];
	}

	/**
	 * Convert camel case string to snake case string and capitalise string.
	 */
	public static String camelToSnake(String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		str = str.replaceAll(regex, replacement).toUpperCase();
		return str;
	}

	public List<Event> createEvent(String message, String mobileNumber, String uuid, RequestInfo requestInfo,
			String tenantId, String applicationStatus, String applicationNumber, boolean isPayLink) {
		List<Event> events = new ArrayList<>();
		List<String> toUsers = new ArrayList<>();
		toUsers.add(uuid);
		Recepient recepient = Recepient.builder().toUsers(toUsers).toRoles(null).build();
		Action action = null;
		if (isPayLink) {
			action = generateAction(applicationStatus, mobileNumber, applicationNumber, tenantId);
		}
		events.add(Event.builder().tenantId(tenantId).description(message).eventType(PSConstants.USREVENTS_EVENT_TYPE)
				.name(PSConstants.USREVENTS_EVENT_NAME).postedBy(PSConstants.USREVENTS_EVENT_POSTEDBY)
				.source(Source.WEBAPP).recepient(recepient).eventDetails(null).actions(action).build());

		return events;
	}

	private Action generateAction(String applicationStatus, String mobile, String applicationNumber, String tenantId) {
		List<ActionItem> items = new ArrayList<>();
		String actionLink = null;
		actionLink = config.getPayLinkForApplication().replace("$mobile", mobile)
				.replace("$applicationNo", applicationNumber).replace("$tenantId", tenantId);
		actionLink = config.getUiAppHost() + actionLink;
		ActionItem item = ActionItem.builder().actionUrl(actionLink).code(config.getPayCode()).build();
		items.add(item);
		return Action.builder().actionUrls(items).build();
	}

	/**
	 * User event
	 * 
	 * @param request
	 */
	public void sendEventNotification(EventRequest request) {
		log.info("userList:" + request.getEvents().get(0).getRecepient().getToUsers());
		producer.push(config.getSaveUserEventsTopic(), request);
	}

	/**
	 * Extracts transit site number from consumer code.
	 * 
	 * @param consumerCode
	 * @return
	 */
	public String getFileNumberFromConsumerCode(String consumerCode) {
		try {
			Pattern pattern = Pattern.compile("^SITE-");
			Matcher matcher = pattern.matcher(consumerCode);
			if (matcher.find()) {
				// String formatted = consumerCode;
				String[] tokens = consumerCode.split("-");
				String fileNumber = "";
				int length = Array.getLength(tokens) - 7;
				for (int i = 1; i < length + 1; i++) {
					if (i == 1) {
						fileNumber = fileNumber.concat(tokens[i]);
					}
					if (i > 1) {
						String x = "-" + tokens[i];
						fileNumber = fileNumber.concat(x);
					}
				}
				return fileNumber;
			}
			log.error("Could not match rent consumer code pattern {}", consumerCode);
		} catch (Exception e) {
			log.error("Failed during parsing transit number from consumer code", e);
			throw new CustomException(Collections.singletonMap("INVALID_RENT_CONSUMER_CODE",
					"File number could not be extracted from consumer code " + consumerCode));
		}
		return null;
	}

	public String getMmFileNumberFromConsumerCode(String consumerCode) {
		try {
			Pattern pattern = Pattern.compile("^SITE-");
			Matcher matcher = pattern.matcher(consumerCode);
			if (matcher.find()) {
				// String formatted = consumerCode;
				String[] tokens = consumerCode.split("-");
				String fileNumber = "";
				int length = Array.getLength(tokens) - 7;
				for (int i = 1; i < length + 1; i++) {
					if (i == 1) {
						fileNumber = fileNumber.concat(tokens[i]);
					}
					if (i > 1) {
						String x = "-" + tokens[i];
						fileNumber = fileNumber.concat(x);
					}
				}
				return fileNumber;
			}
			log.error("Could not match rent consumer code pattern {}", consumerCode);
		} catch (Exception e) {
			log.error("Failed during parsing transit number from consumer code", e);
			throw new CustomException(Collections.singletonMap("INVALID_RENT_CONSUMER_CODE",
					"File number could not be extracted from consumer code " + consumerCode));
		}
		return null;
	}
	
// 	public double extractGst(double effectiveAmount) {
// 		double gstPercentage = 18;
// 		return (gstPercentage / 100) * effectiveAmount;
// 	}

/**
 * Extract file number from rent/fee consumer code	
 */
	public String getFileNumberFromRentFeeConsumerCode(String consumerCode) {
		try {
			Pattern pattern = Pattern.compile("^ES-");
			Matcher matcher = pattern.matcher(consumerCode);
			if (matcher.find()) {
				// String formatted = consumerCode;
				String[] tokens = consumerCode.split("-");
				String fileNumber = "";
				int length = Array.getLength(tokens) - 8;
				for (int i = 2; i <= length + 1; i++) {
					if (i == 2) {
						fileNumber = fileNumber.concat(tokens[i]);
					}
					if (i > 2) {
						String x = "-" + tokens[i];
						fileNumber = fileNumber.concat(x);
					}
				}
				return fileNumber;
			}
			log.error("Could not match rent consumer code pattern {}", consumerCode);
		} catch (Exception e) {
			log.error("Failed during parsing transit number from consumer code", e);
			throw new CustomException(Collections.singletonMap("INVALID_RENT_CONSUMER_CODE",
					"File number could not be extracted from consumer code " + consumerCode));
		}
		return null;
	}
}
