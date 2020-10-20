package org.egov.ps.service.calculation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Application;
import org.egov.ps.model.UserResponse;
import org.egov.ps.model.UserSearchRequestCore;
import org.egov.ps.model.calculation.Demand;
import org.egov.ps.model.calculation.Demand.StatusEnum;
import org.egov.ps.model.calculation.DemandDetail;
import org.egov.ps.model.calculation.DemandResponse;
import org.egov.ps.model.calculation.TaxHeadEstimate;
import org.egov.ps.repository.ServiceRequestRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.RequestInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DemandService {

	@Autowired
	private Configuration config;

	@Autowired
	private DemandRepository demandRepository;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private Util utils;

	/**
	 * Creates demand for the given list of calculations
	 * 
	 * @param requestInfo  The RequestInfo of the calculation request
	 * @param applications List of calculation object
	 * @return Demands that are created
	 */
	public List<Demand> createDemand(RequestInfo requestInfo, List<Application> applications) {
		List<Demand> demands = new LinkedList<>();
		for (Application application : applications) {
			if (application == null)
				throw new CustomException("INVALID APPLICATIONNUMBER",
						"Demand cannot be generated for this application");

			String tenantId = application.getTenantId();
			String consumerCode = application.getApplicationNumber();

			String url = config.getUserHost().concat(config.getUserSearchEndpoint());

			List<org.egov.ps.model.User> applicationUser = null;
			Set<String> uuid = new HashSet<>();
			uuid.add(application.getAuditDetails().getCreatedBy());

			UserSearchRequestCore userSearchRequest = UserSearchRequestCore.builder().requestInfo(requestInfo)
					.uuid(uuid).build();

			applicationUser = mapper
					.convertValue(serviceRequestRepository.fetchResult(url, userSearchRequest), UserResponse.class)
					.getUser();

			log.info("applicationUser:" + applicationUser);

			// User requestUser = requestInfo.getUserInfo(); // user from request
			// information
			User requestUser = applicationUser.get(0).toCommonUser();
			log.info("requestUser:" + requestUser);
			User user = null;
			if (requestUser.getMobileNumber() != null) {
				user = User.builder().id(requestUser.getId()).userName(requestUser.getUserName())
						.name(requestUser.getName()).type(requestInfo.getUserInfo().getType())
						.mobileNumber(requestUser.getMobileNumber()).emailId(requestUser.getEmailId())
						.roles(requestUser.getRoles()).tenantId(requestUser.getTenantId()).uuid(requestUser.getUuid())
						.build();
			} else {
				user = User.builder().id(requestUser.getId()).userName(requestUser.getUserName())
						.name(requestUser.getName()).type(requestInfo.getUserInfo().getType())
						.mobileNumber(requestUser.getUserName()).emailId(requestUser.getEmailId())
						.roles(requestUser.getRoles()).tenantId(requestUser.getTenantId()).uuid(requestUser.getUuid())
						.build();
			}

			List<DemandDetail> demandDetails = new LinkedList<>();
			if (!CollectionUtils.isEmpty(application.getCalculation().getTaxHeadEstimates())) {
				application.getCalculation().getTaxHeadEstimates().forEach(taxHeadEstimate -> {
					demandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
							.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).collectionAmount(BigDecimal.ZERO)
							.tenantId(tenantId).build());
				});
			}

			Long taxPeriodFrom = System.currentTimeMillis();
			Long taxPeriodTo = System.currentTimeMillis();

			Demand singleDemand = Demand.builder().status(StatusEnum.ACTIVE).consumerCode(consumerCode)
					.demandDetails(demandDetails).payer(user).minimumAmountPayable(config.getMinimumPayableAmount())
					.tenantId(tenantId).taxPeriodFrom(taxPeriodFrom).taxPeriodTo(taxPeriodTo)
					.consumerType(PSConstants.ESTATE_SERVICE).businessService(application.getBillingBusinessService())
					.additionalDetails(null).build();

			demands.add(singleDemand);
		}
		return demandRepository.saveDemand(requestInfo, demands);
	}

	/**
	 * Updates demand for the given list of calculations
	 * 
	 * @param requestInfo  The RequestInfo of the calculation request
	 * @param calculations List of calculation object
	 * @return Demands that are updated
	 */
	public List<Demand> generateDemand(RequestInfo requestInfo, List<Application> applications) {
		List<Demand> demands = new LinkedList<>();
		for (Application application : applications) {

			List<Demand> searchResult = searchDemand(application.getTenantId(),
					Collections.singleton(application.getApplicationNumber()), requestInfo,
					application.getBillingBusinessService());

			if (CollectionUtils.isEmpty(searchResult)) {
				demands = createDemand(requestInfo, applications);
				/*
				 * throw new CustomException("INVALID UPDATE",
				 * "No demand exists for applicationNumber: " +
				 * owner.getOwnerDetails().getApplicationNumber());
				 */
			} else {
				Demand demand = searchResult.get(0);
				List<DemandDetail> demandDetails = demand.getDemandDetails();
				List<DemandDetail> updatedDemandDetails = getUpdatedDemandDetails(application, demandDetails);
				demand.setDemandDetails(updatedDemandDetails);
				demands.add(demand);
				demands = demandRepository.updateDemand(requestInfo, demands);
			}

		}
		return demands;
	}

	/**
	 * Searches demand for the given consumerCode and tenantIDd
	 * 
	 * @param tenantId      The tenantId of the tradeLicense
	 * @param consumerCodes The set of consumerCode of the demands
	 * @param requestInfo   The RequestInfo of the incoming request
	 * @return Lis to demands for the given consumerCode
	 */
	private List<Demand> searchDemand(String tenantId, Set<String> consumerCodes, RequestInfo requestInfo,
			String businessService) {
		String uri = utils.getDemandSearchURL();
		uri = uri.replace("{1}", tenantId);
		uri = uri.replace("{2}", businessService);
		uri = uri.replace("{3}", StringUtils.join(consumerCodes, ','));

		Object result = serviceRequestRepository.fetchResult(new StringBuilder(uri),
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		DemandResponse response;
		try {
			response = mapper.convertValue(result, DemandResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException("PARSING ERROR", "Failed to parse response from Demand Search");
		}

		if (CollectionUtils.isEmpty(response.getDemands())) {
			return null;
		} else {
			return response.getDemands();
		}
	}

	/**
	 * Returns the list of new DemandDetail to be added for updating the demand
	 * 
	 * @param application         The calculation object for the update tequest
	 * @param demandDetails The list of demandDetails from the existing demand
	 * @return The list of new DemandDetails
	 */
	private List<DemandDetail> getUpdatedDemandDetails(Application application, List<DemandDetail> demandDetails) {

		List<DemandDetail> newDemandDetails = new ArrayList<>();
		Map<String, List<DemandDetail>> taxHeadToDemandDetail = new HashMap<>();

		demandDetails.forEach(demandDetail -> {
			if (!taxHeadToDemandDetail.containsKey(demandDetail.getTaxHeadMasterCode())) {
				List<DemandDetail> demandDetailList = new LinkedList<>();
				demandDetailList.add(demandDetail);
				taxHeadToDemandDetail.put(demandDetail.getTaxHeadMasterCode(), demandDetailList);
			} else
				taxHeadToDemandDetail.get(demandDetail.getTaxHeadMasterCode()).add(demandDetail);
		});

		BigDecimal diffInTaxAmount;
		List<DemandDetail> demandDetailList;
		BigDecimal total;

		for (TaxHeadEstimate taxHeadEstimate : application.getCalculation().getTaxHeadEstimates()) {
			if (!taxHeadToDemandDetail.containsKey(taxHeadEstimate.getTaxHeadCode()))
				newDemandDetails.add(DemandDetail.builder().taxAmount(taxHeadEstimate.getEstimateAmount())
						.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).tenantId(application.getTenantId())
						.collectionAmount(BigDecimal.ZERO).build());
			else {
				demandDetailList = taxHeadToDemandDetail.get(taxHeadEstimate.getTaxHeadCode());
				total = demandDetailList.stream().map(DemandDetail::getTaxAmount).reduce(BigDecimal.ZERO,
						BigDecimal::add);
				diffInTaxAmount = taxHeadEstimate.getEstimateAmount().subtract(total);
				if (diffInTaxAmount.compareTo(BigDecimal.ZERO) != 0) {
					newDemandDetails.add(DemandDetail.builder().taxAmount(diffInTaxAmount)
							.taxHeadMasterCode(taxHeadEstimate.getTaxHeadCode()).tenantId(application.getTenantId())
							.collectionAmount(BigDecimal.ZERO).build());
				}
			}
		}
		List<DemandDetail> combinedBillDetials = new LinkedList<>(demandDetails);
		combinedBillDetials.addAll(newDemandDetails);
		return combinedBillDetials;
	}
	
}
