package org.egov.cpt.util;

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
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.calculation.BusinessService;
import org.egov.cpt.service.MDMSService;
import org.egov.cpt.workflow.WorkflowService;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PropertyUtil {

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private MDMSService mdmsService;

	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by
	 * @param isCreate
	 * @return AuditDetails
	 */
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
	 * Creates url for tl-calculator service
	 * 
	 * @return url for tl-calculator service
	 */
	// public StringBuilder getCalculationURI(String businessService) {
	// StringBuilder uri = new StringBuilder();
	// uri.append(config.getCalculatorHost());
	// uri.append(config.getCalculateEndpointTL());
	// return uri;
	// }

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
	public Map<String, Boolean> getIdToIsStateUpdatableMap(BusinessService businessService, List<Owner> searchresult) {
		Map<String, Boolean> idToIsStateUpdatableMap = new HashMap<>();
		searchresult.forEach(result -> {
			if (result.getApplicationState().equalsIgnoreCase(PTConstants.STATUS_INITIATED)) {
				idToIsStateUpdatableMap.put(result.getId(), true);
			} else {
				idToIsStateUpdatableMap.put(result.getId(),
						workflowService.isStateUpdatable(result.getApplicationState(), businessService));
			}
		});
		return idToIsStateUpdatableMap;
	}

	public Map<String, Boolean> getIdToIsStateUpdatableMapDc(BusinessService businessService,
			List<DuplicateCopy> searchresult) {
		Map<String, Boolean> idToIsStateUpdatableMapDc = new HashMap<>();
		searchresult.forEach(result -> {
			if (result.getState().equalsIgnoreCase(PTConstants.STATUS_INITIATED)) {
				idToIsStateUpdatableMapDc.put(result.getId(), true);
			} else
				idToIsStateUpdatableMapDc.put(result.getId(),
						workflowService.isStateUpdatable(result.getState(), businessService));
		});
		return idToIsStateUpdatableMapDc;
	}

	public String getStateLevelTenantId(String tenantId) {
		String[] components = tenantId.split(".");
		if (components.length == 0) {
			return "ch";
		}
		return components[0];
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD-HH-MM-SS");

	public Owner getCurrentOwnerFromProperty(Property property) {
		/**
		 * Validate that there is an existing active owner.
		 */
		Optional<Owner> ownerOptional = property.getOwners().stream().filter(Owner::getActiveState).findAny();
		if (!ownerOptional.isPresent()) {
			throw new CustomException(Collections.singletonMap("PROPERTY_OWNER_NOT_FOUND",
					"Could not find current owner for property with id " + property.getId()));
		}

		/**
		 * Make sure we create a user or else billing service will throw an error while
		 * generating demand.
		 */
		Owner owner = ownerOptional.get();
		return owner;
	}

	/**
	 * Generates a new consumer code from a transit number to be sent while creating
	 * a bill.
	 * 
	 * @param transitNumber
	 * @return
	 */
	public String getPropertyRentConsumerCode(String transitNumber) {
		return String.format("SITE-%s-%s", transitNumber.toUpperCase(), dateFormat.format(new Date()));
	}

	/**
	 * Extracts transit site number from consumer code.
	 * 
	 * @param consumerCode
	 * @return
	 */
	public String getTransitNumberFromConsumerCode(String consumerCode) {
		try {
			Pattern pattern = Pattern.compile("^SITE-\\d{1,4}?-");
			Matcher matcher = pattern.matcher(consumerCode);
			if (matcher.find()) {
				String formatted = matcher.group();
				String[] tokens = formatted.split("-");
				return tokens[1];
			}
			log.error("Could not match rent consumer code pattern {}", consumerCode);
		} catch (Exception e) {
			log.error("Failed during parsing transit number from consumer code", e);
			throw new CustomException(Collections.singletonMap("INVALID_RENT_CONSUMER_CODE",
					"Transit number could not be extracted from consumer code " + consumerCode));
		}
		return null;
	}

	@Cacheable(value = "interestStartDate", key = "{#colony, #tenantId}")
	public long getInterstStartFromMDMS(String colony,String tenantId) {
		RequestInfo requestInfo = new RequestInfo();
		String filter = "[?(@.code=='" + colony +"')].interestStartYear"; 
		Map<String, List<Long>> interestStartDateList =(Map<String, List<Long>>)mdmsService.getMDMSResponse(requestInfo,
				tenantId.split("\\.")[0], PTConstants.MDMS_PT_MOD_NAME,
						"colonies",filter, PTConstants.JSONPATH_CODES); 
				long interestStartDate = ((Number)interestStartDateList.get(PTConstants.MDMS_PT_COLONY).get(0)).longValue();
				return interestStartDate;
	}
}
