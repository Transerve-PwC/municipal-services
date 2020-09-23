package org.egov.pmcalculator.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pmcalculator.config.PMCalculatorConfigs;
import org.egov.pmcalculator.kafka.broker.OPMSCalculatorProducer;
import org.egov.pmcalculator.repository.CalculationRepository;
import org.egov.pmcalculator.repository.ServiceRequestRepository;
import org.egov.pmcalculator.utils.CalculationUtils;
import org.egov.pmcalculator.web.models.Calculation;
import org.egov.pmcalculator.web.models.CalculationReq;
import org.egov.pmcalculator.web.models.CalculationRes;
import org.egov.pmcalculator.web.models.CalulationCriteria;
import org.egov.pmcalculator.web.models.demand.Category;
import org.egov.pmcalculator.web.models.demand.TaxHeadEstimate;
import org.egov.pmcalculator.web.models.pm.EstimatesAndSlabs;
import org.egov.pmcalculator.web.models.pm.PMDetail;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalculationService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PMCalculatorConfigs config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private CalculationUtils utils;

	@Autowired
	private DemandService demandService;

	@Autowired
	private OPMSCalculatorProducer producer;

	@Autowired
	private MDMSService mdmsService;

	@Autowired
	private CalculationRepository calculationRepository;

	public List<Calculation> calculate(CalculationReq calculationReq) {

		String tenantId = calculationReq.getCalulationCriteria().get(0).getTenantId();

		Object mdmsData = mdmsService.mDMSCall(calculationReq.getRequestInfo(), tenantId);

		List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),
				calculationReq.getCalulationCriteria(), mdmsData);

		demandService.generateDemand(calculationReq.getRequestInfo(), calculations, mdmsData);
		return calculations;

	}

	public List<Calculation> getCalculation(RequestInfo requestInfo, List<CalulationCriteria> criterias,
			Object mdmsData) {

		List<Calculation> calculations = new LinkedList<>();
		for (CalulationCriteria criteria : criterias) {
			PMDetail opms;
			if (criteria.getOpmsDetail() != null && criteria.getApplicationNumber() != null) {
				// opms = utils.getOmpsDetail(requestInfo, criteria.getApplicationNumber(),
				// criteria.getTenantId());

				opms = criteria.getOpmsDetail();
				criteria.setOpmsDetail(opms);
			} else {
				new CustomException("PM_DETAILS_EXCEPTION", "No PM details found");
			}

			EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria, requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs.getEstimates();

			Calculation calculation = new Calculation();
			calculation.setApplicationNumber(criteria.getApplicationNumber());
			calculation.setOpmsDetail(criteria.getOpmsDetail());
			calculation.setTenantId(criteria.getTenantId());
			calculation.setTaxHeadEstimates(taxHeadEstimates);
			calculations.add(calculation);
		}
		return calculations;
	}

	private EstimatesAndSlabs getTaxHeadEstimates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		// 1. Test Base Tax from MDMS -
		// 2. Get calculated amount for types
		List<TaxHeadEstimate> taxHeadEstimates = new ArrayList<>();

		EstimatesAndSlabs estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);
		List<TaxHeadEstimate> tax = estimatesAndSlabs.getEstimates();

		TaxHeadEstimate estimate = new TaxHeadEstimate();
		estimate.setCategory(Category.FEE);

		if (calulationCriteria.getOpmsDetail().getApplicationType().equals("ADVERTISEMENTNOC")) {
			if (calulationCriteria.getOpmsDetail().getIsExamptedAdvertisement() != null
					&& !calulationCriteria.getOpmsDetail().getIsExamptedAdvertisement().isEmpty()) {

				if (!calulationCriteria.getOpmsDetail().getIsExamptedAdvertisement().equals("1")) {
					BigDecimal amount = getNocFee(calulationCriteria.getOpmsDetail());
					estimate.setTaxHeadCode(config.getBaseAdvertisementFeeHead());
					estimate.setEstimateAmount(amount);

					for (TaxHeadEstimate taxHeadEstimate : tax) {
						BigDecimal taxValue = taxHeadEstimate.getEstimateAmount();
						BigDecimal taxValueTotal = amount.divide(BigDecimal.valueOf(100)).multiply(taxValue);
						taxHeadEstimate.setEstimateAmount(taxValueTotal.setScale(0, BigDecimal.ROUND_UP));
						taxHeadEstimates.add(taxHeadEstimate);
					}
				} else {
					estimate.setTaxHeadCode(config.getBaseAdvertisementFeeHead());
					estimate.setEstimateAmount(BigDecimal.ZERO);
					for (TaxHeadEstimate taxHeadEstimate : tax) {
						taxHeadEstimate.setEstimateAmount(BigDecimal.ZERO);
						taxHeadEstimates.add(taxHeadEstimate);
					}
				}
			} else {
				throw new CustomException("BILLING ERROR", "No Exempted Category found");
			}

		} else if (calulationCriteria.getOpmsDetail().getApplicationType().equals("ROADCUTNOC")) {
			estimate.setTaxHeadCode(
					config.getBaseRoadCutFeeHead() + "_" + calulationCriteria.getOpmsDetail().getRoadCutDivision());
			estimate.setEstimateAmount(new BigDecimal(calulationCriteria.getOpmsDetail().getAmountRoadCut()).setScale(0,
					BigDecimal.ROUND_UP));

			TaxHeadEstimate estimate2 = new TaxHeadEstimate();
			estimate2.setCategory(Category.BANKPERFORMANCE);
			estimate2.setTaxHeadCode(
					config.getBaseRoadCutFeeBankHead() + "_" + calulationCriteria.getOpmsDetail().getRoadCutDivision());
			estimate2.setEstimateAmount(new BigDecimal(calulationCriteria.getOpmsDetail().getBankPerformanceRoadCut())
					.setScale(0, BigDecimal.ROUND_UP));
			taxHeadEstimates.add(estimate2);
			taxHeadEstimates.addAll(tax);

		} else if (calulationCriteria.getOpmsDetail().getApplicationType().equals("PETNOC")) {
			BigDecimal amount = getNocFee(calulationCriteria.getOpmsDetail());
			estimate.setTaxHeadCode(config.getBasePetFeeHead());
			estimate.setEstimateAmount(amount.setScale(0, BigDecimal.ROUND_UP));

			for (TaxHeadEstimate taxHeadEstimate : tax) {
				BigDecimal taxValue = taxHeadEstimate.getEstimateAmount();
				taxValue = taxValue.compareTo(BigDecimal.ZERO) < -1 ? BigDecimal.valueOf(1) : taxValue;
				taxHeadEstimate.setCategory(Category.CHARGES);
				taxHeadEstimate.setEstimateAmount(taxValue.setScale(0, BigDecimal.ROUND_UP));
				taxHeadEstimates.add(taxHeadEstimate);
			}
		}

		taxHeadEstimates.add(estimate);
		estimatesAndSlabs.setEstimates(taxHeadEstimates);

		return estimatesAndSlabs;
	}

	private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {

		// This will get all taxes from mdm

		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		TaxHeadEstimate estimate = new TaxHeadEstimate();

		// Need add MDM Tax data
		BigDecimal totalTax = mdmsService.getTaxAmount(requestInfo, calulationCriteria.getOpmsDetail(), mdmsData);

		// add type wise tax head
		if (calulationCriteria.getOpmsDetail().getApplicationType().equals("ADVERTISEMENTNOC")) {
			estimate.setTaxHeadCode(config.getBaseAdvertisementTaxHead());
			estimate.setEstimateAmount(totalTax);
		} else if (calulationCriteria.getOpmsDetail().getApplicationType().equals("ROADCUTNOC")) {
			estimate.setTaxHeadCode(
					config.getBaseRoadCutTaxHead() + "_" + calulationCriteria.getOpmsDetail().getRoadCutDivision());
			estimate.setEstimateAmount(new BigDecimal(calulationCriteria.getOpmsDetail().getGstRoadCut()).setScale(0,
					BigDecimal.ROUND_UP));
		} else if (calulationCriteria.getOpmsDetail().getApplicationType().equals("PETNOC")) {
			estimate.setTaxHeadCode(config.getBasePetTaxHead());
			estimate.setEstimateAmount(totalTax);
		}

		estimate.setCategory(Category.TAX);

		estimatesAndSlabs.setEstimates(Collections.singletonList(estimate));
		return estimatesAndSlabs;
	}

	private BigDecimal getNocFee(PMDetail pMDetail) {

		BigDecimal results = BigDecimal.ZERO;

		if (pMDetail.getApplicationType().equals("ADVERTISEMENTNOC")) {

			SortedMap<Integer, JSONObject> calData = calculationRepository.getPriceList(pMDetail.getApplicationType(),
					pMDetail.getCategoryIdAdvertisement(), pMDetail.getFromDateAdvertisement(), "true",
					pMDetail.getTenantId());

			log.info("pm-calculator logs :: ADVERTISEMENTNOC NOC Fee Details : {}", calData);

			if (calData != null && !calData.isEmpty()) {
				try {

					String squareFeet = (pMDetail.getSquareFeetAdvertisement() == null
							|| pMDetail.getSquareFeetAdvertisement().isEmpty() ? "0"
									: pMDetail.getSquareFeetAdvertisement());
					String calculateByPer = (pMDetail.getDurationAdvertisement() == null ? ""
							: pMDetail.getDurationAdvertisement());

					String dateBefore = (pMDetail.getFromDateAdvertisement() == null ? ""
							: pMDetail.getFromDateAdvertisement());
					String dateAfter = (pMDetail.getToDateAdvertisement() == null ? ""
							: pMDetail.getToDateAdvertisement());

					String subCatagoryId = (pMDetail.getSubCategotyIdAdvertisement() == null ? ""
							: pMDetail.getSubCategotyIdAdvertisement());

					JSONObject jsonObjectType = calData.get(1);
					String calculationType = jsonObjectType.get("calculation_type").toString();

					// String subCatagoryId = jsonObjectType.get("sub_category_id").toString();

					// Parsing the date 2020-05-12
					LocalDate fromDate = LocalDate.parse(dateBefore);
					LocalDate toDate = LocalDate.parse(dateAfter);
					BigDecimal sqFeets = new BigDecimal(squareFeet);
					BigDecimal duration = BigDecimal.ZERO;

					if (calculateByPer.equalsIgnoreCase("Daily")) {
						calculateByPer = "perday_price";
						duration = new BigDecimal(ChronoUnit.DAYS.between(fromDate, toDate) + 1);
					} else if (calculateByPer.equalsIgnoreCase("Weekly")) {
						calculateByPer = "perweek_price";
						duration = new BigDecimal(ChronoUnit.WEEKS.between(fromDate, toDate) + 1);
					} else if (calculateByPer.equalsIgnoreCase("Monthly")) {
						calculateByPer = "permonth_price";
						duration = new BigDecimal(ChronoUnit.MONTHS.between(fromDate, toDate) + 1);
					} else if (calculateByPer.equalsIgnoreCase("Annual")) {
						calculateByPer = "annual_price";
						duration = new BigDecimal(ChronoUnit.YEARS.between(fromDate, toDate) + 1);
					} else {
						throw new CustomException("BILLING ERROR",
								"No Found BillingSlabs for the given application type or duration");
					}

					if (calculationType.isEmpty() || duration.intValue() < 0) {
						throw new CustomException("BILLINGS ERROR",
								"No Found BillingSlabs for the given application type or calculation type");
					}

					int size = calData.size();
					if (calculationType.equalsIgnoreCase("range")) {

						for (int i = 1; i <= size; i++) {
							if (i != size) {

								JSONObject jsonObject1 = calData.get(i);
								BigDecimal min = new BigDecimal(jsonObject1.get("min_sqft").toString());
								BigDecimal max = new BigDecimal(jsonObject1.get("max_sqft").toString());

								BigDecimal rate = new BigDecimal(((jsonObject1.get(calculateByPer) == null
										|| jsonObject1.get(calculateByPer) != null
												&& jsonObject1.get(calculateByPer).toString().isEmpty()) ? "0"
														: jsonObject1.get(calculateByPer).toString()));
								max = max.subtract(min);
								sqFeets = sqFeets.subtract(max);
								results = results.add(rate.multiply(duration));

								if (sqFeets.intValue() <= 0)
									break;

							} else {
								JSONObject jsonObject1 = calData.get(i);
								BigDecimal min = new BigDecimal(jsonObject1.get("min_sqft").toString());
								BigDecimal max = new BigDecimal(jsonObject1.get("max_sqft").toString());

								BigDecimal rate = new BigDecimal(((jsonObject1.get(calculateByPer) == null
										|| jsonObject1.get(calculateByPer) != null
												&& jsonObject1.get(calculateByPer).toString().isEmpty()) ? "0"
														: jsonObject1.get(calculateByPer).toString()));

								max = max.subtract(min);
								sqFeets = sqFeets.subtract(max);
								results = results.add(rate.multiply(duration));

								while (sqFeets.intValue() > 0) {
									sqFeets = sqFeets.subtract(max);
									results = results.add(rate.multiply(duration));
								}
							}
						}
					} else if (calculationType.equalsIgnoreCase("units")) {
						for (int i = 1; i <= size; i++) {
							JSONObject jsonObject1 = calData.get(i);

							if (jsonObject1.get("sub_category_id").equals(subCatagoryId)) {
								BigDecimal rate = new BigDecimal(((jsonObject1.get(calculateByPer) == null
										|| jsonObject1.get(calculateByPer) != null
												&& jsonObject1.get(calculateByPer).toString().isEmpty()) ? "0"
														: jsonObject1.get(calculateByPer).toString()));

								results = results.add(rate.multiply(duration));
								break;
							}
						}
					} else if (calculationType.equalsIgnoreCase("days")) {
						for (int i = 1; i <= size; i++) {
							if (i != size) {

								JSONObject jsonObject1 = calData.get(i);
								BigDecimal min = new BigDecimal(jsonObject1.get("min_sqft").toString());
								BigDecimal max = new BigDecimal(jsonObject1.get("max_sqft").toString());

								BigDecimal rate = new BigDecimal(((jsonObject1.get(calculateByPer) == null
										|| jsonObject1.get(calculateByPer) != null
												&& jsonObject1.get(calculateByPer).toString().isEmpty()) ? "0"
														: jsonObject1.get(calculateByPer).toString()));

								max = max.subtract(min);
								results = results.add(rate.multiply(sqFeets).multiply(max));
								duration = duration.subtract(max);

								if (duration.intValue() <= 0)
									break;

							} else {
								JSONObject jsonObject1 = calData.get(i);
								BigDecimal rate = new BigDecimal(((jsonObject1.get(calculateByPer) == null
										|| jsonObject1.get(calculateByPer) != null
												&& jsonObject1.get(calculateByPer).toString().isEmpty()) ? "0"
														: jsonObject1.get(calculateByPer).toString()));

								results = results.add(rate.multiply(sqFeets).multiply(duration));
							}
						}
					}
				} catch (Exception e) {
					throw new CustomException("BILLINGSLAB ERROR", e.getMessage());
				}
			} else {
				throw new CustomException("BILLINGSLAB ERROR",
						"No Found BillingSlabs for the given application type or category");
			}
		} else if (pMDetail.getApplicationType().equals("PETNOC")) {
			LocalDate date = LocalDate.now();
			String dateString = date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth();

			SortedMap<Integer, JSONObject> calData = calculationRepository.getPriceList(pMDetail.getApplicationType(),
					"", dateString, "false", pMDetail.getTenantId());

			log.info("pm-calculator logs :: PETNOC NOC Fee Details : {}", calData);

			if (!calData.isEmpty() && calData.size() == 1) {
				results = new BigDecimal(calData.get(0).get("fixed_price").toString());
			}
		}

		return results.setScale(0, BigDecimal.ROUND_UP);
	}
}
