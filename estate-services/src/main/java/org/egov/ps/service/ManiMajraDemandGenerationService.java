package org.egov.ps.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.config.Configuration;
import org.egov.ps.model.Property;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.IManiMajraRentCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Slf4j
@Service
public class ManiMajraDemandGenerationService {

	@Autowired
	public ManiMajraDemandGenerationService(PropertyRepository propertyRepository, Producer producer,
			Configuration config, IManiMajraRentCollectionService estateRentCollectionService) {
	}

	@Autowired
	private MDMSService mdmsService;

	public AtomicInteger createMissingDemandsForMM(Property property, RequestInfo requestInfo) {
		AtomicInteger counter = new AtomicInteger(0);

		/* Fetch billing date of the property */
		// should be replaced with the front end value
		Date date = null;
		String demandYearAndMonth = property.getPropertyDetails().getMmDemandStartYear() + "-"
				+ property.getPropertyDetails().getMmDemandStartMonth() + "-01";
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(demandYearAndMonth);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Date propertyBillingDate = getFirstDateOfMonth(date);

		List<Date> allMonthDemandDatesTillCurrentMonth = getAllRemainingDates(propertyBillingDate);
		int year = 0;
		for (Date demandDate : allMonthDemandDatesTillCurrentMonth) {
			Date demandGenerationStartDate = setDateOfMonthMM(demandDate, 1);

			/* Here checking demand date is already created or not */
			List<ManiMajraDemand> inRequestDemands = new ArrayList<ManiMajraDemand>();
			if (null != property.getPropertyDetails().getManiMajraDemands()) {
				inRequestDemands = property.getPropertyDetails().getManiMajraDemands().stream()
						.filter(demand -> checkSameDay(new Date(demand.getGenerationDate()), demandGenerationStartDate))
						.collect(Collectors.toList());
			}
			Date demandGenerationAnnualDate = demandGenerationStartDate;
			if (inRequestDemands.isEmpty()
					&& property.getPropertyDetails().getBranchType().equalsIgnoreCase(PSConstants.MANI_MAJRA)) {
				if (year == 0) {
					demandGenerationAnnualDate = getFirstMonthOfYear(demandGenerationAnnualDate);
					year++;
				}
				// generate demand
				counter.getAndIncrement();
				generateEstateDemandMM(property, getFirstDateOfMonth(demandGenerationStartDate),
						demandGenerationAnnualDate, requestInfo);
			}
		}
		return counter;
	}

	private Date setDateOfMonthMM(Date date, int value) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, value);
		cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
		cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
		return cal.getTime();
	}

	private void generateEstateDemandMM(Property property, Date date, Date demandGenerationAnnualDate,
			RequestInfo requestInfo) {

		int gst = 0;
		Double calculatedRent = 0D;
		Double annualCalculatedRent = 0D;

		if (property.getPropertyDetails().getPropertyType().equalsIgnoreCase(PSConstants.MM_JANTA_READY_MARKET)) {

			List<Map<String, Object>> janathaReadyMonthlyConfigurations = mdmsService.getManimajraPropertyRent(
					PSConstants.MM_PROPERTY_RENT_MASTER_NAME, requestInfo, PSConstants.TENANT_ID,
					PSConstants.MDMS_PS_MM_JANATHA_READY_MONTHLY_FILTER);

			List<Map<String, Object>> janathaReadyAnuallyConfigurations = mdmsService.getManimajraPropertyRent(
					PSConstants.MM_PROPERTY_RENT_MASTER_NAME, requestInfo, PSConstants.TENANT_ID,
					PSConstants.MDMS_PS_MM_JANATHA_READY_ANNUALLY_FILTER);
			/**
			 * Generate Monthly Demands for Janatha Ready Market Mani Majra
			 */
			for (Map<String, Object> janathaReadyMonthlyConfiguration : janathaReadyMonthlyConfigurations) {
				Integer startYear = new Integer(janathaReadyMonthlyConfiguration.get("StartYear").toString());
				Integer startMonth = new Integer(janathaReadyMonthlyConfiguration.get("StartMonth").toString());
				Integer endYear;
				Integer endMonth;
				if (null != janathaReadyMonthlyConfiguration.get("EndYear")
						&& null != janathaReadyMonthlyConfiguration.get("EndMonth")) {
					endYear = new Integer(janathaReadyMonthlyConfiguration.get("EndYear").toString());
					endMonth = new Integer(janathaReadyMonthlyConfiguration.get("EndMonth").toString());
				} else {
					Date currentDate = new Date();
					LocalDate presentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					endYear = presentDate.getYear();
					endMonth = presentDate.getMonthValue();
				}

				Calendar startCal = Calendar.getInstance();
				startCal.set(Calendar.YEAR, startYear);
				startCal.set(Calendar.MONTH, startMonth - 1);
				startCal.set(Calendar.DAY_OF_MONTH, 01);
				startCal.set(Calendar.HOUR_OF_DAY, 0);
				startCal.set(Calendar.MINUTE, 0);
				startCal.set(Calendar.SECOND, 0);
				startCal.set(Calendar.MILLISECOND, 0);
				Date startDate = startCal.getTime();

				Calendar endCal = Calendar.getInstance();
				endCal.set(Calendar.YEAR, endYear);
				endCal.set(Calendar.MONTH, endMonth - 1);
				endCal.set(Calendar.DAY_OF_MONTH, 01);
				endCal.set(Calendar.HOUR_OF_DAY, 0);
				endCal.set(Calendar.MINUTE, 0);
				endCal.set(Calendar.SECOND, 0);
				endCal.set(Calendar.MILLISECOND, 0);
				Date endDate = endCal.getTime();

				if ((date.after(startDate) && date.before(endDate)) || date.equals(startDate) || date.equals(endDate)) {
					gst = new Integer(janathaReadyMonthlyConfiguration.get("TaxOrGst").toString());
					calculatedRent = new Double(janathaReadyMonthlyConfiguration.get("rent").toString());
				}

			}

			for (Map<String, Object> janathaReadyAnnuallyConfig : janathaReadyAnuallyConfigurations) {
				Integer startYear = new Integer(janathaReadyAnnuallyConfig.get("StartYear").toString());
				Integer endYear;
				if (null != janathaReadyAnnuallyConfig.get("EndYear")) {
					endYear = new Integer(janathaReadyAnnuallyConfig.get("EndYear").toString());
				} else {
					Date currentDate = new Date();
					LocalDate presentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					endYear = presentDate.getYear();
				}

				Calendar startCal = Calendar.getInstance();
				startCal.set(Calendar.YEAR, startYear); // 2017-02-01
				startCal.set(Calendar.MONTH, 01 - 1);
				startCal.set(Calendar.DAY_OF_MONTH, 01);
				startCal.set(Calendar.HOUR_OF_DAY, 0);
				startCal.set(Calendar.MINUTE, 0);
				startCal.set(Calendar.SECOND, 0);
				startCal.set(Calendar.MILLISECOND, 0);
				Date startDate = startCal.getTime();

				Calendar endCal = Calendar.getInstance();
				endCal.set(Calendar.YEAR, endYear);// 2019-02-01
				endCal.set(Calendar.MONTH, 01 - 1);
				endCal.set(Calendar.DAY_OF_MONTH, 01);
				endCal.set(Calendar.HOUR_OF_DAY, 0);
				endCal.set(Calendar.MINUTE, 0);
				endCal.set(Calendar.SECOND, 0);
				endCal.set(Calendar.MILLISECOND, 0);
				Date endDate = endCal.getTime();
				// demandGenerationAnnualDate ---> 2018-02-01

				if (demandGenerationAnnualDate.equals(startDate) || demandGenerationAnnualDate.equals(endDate)) {
					annualCalculatedRent = new Double(janathaReadyAnnuallyConfig.get("licenceFee").toString());
				}
			}
		}
		if (property.getPropertyDetails().getPropertyType().equalsIgnoreCase(PSConstants.MM_PUNJAB_AGRO_JUICE)) {

			List<Map<String, Object>> punjabJuiceSiteConfigurations = mdmsService.getManimajraPropertyRent(
					PSConstants.MM_PROPERTY_RENT_MASTER_NAME, requestInfo, PSConstants.TENANT_ID,
					PSConstants.MDMS_PS_MM_PUNJAB_JUICE_SITE_FILTER);
			/**
			 * Generate Monthly Demands for Janatha Ready Market Mani Majra
			 */
			for (Map<String, Object> punjabJuiceSiteConfig : punjabJuiceSiteConfigurations) {
				Integer startYear = new Integer(punjabJuiceSiteConfig.get("StartYear").toString());
				Integer startMonth = new Integer(punjabJuiceSiteConfig.get("StartMonth").toString());
				Integer endYear;
				Integer endMonth;
				if (null != punjabJuiceSiteConfig.get("EndYear") && null != punjabJuiceSiteConfig.get("EndMonth")) {
					endYear = new Integer(punjabJuiceSiteConfig.get("EndYear").toString());
					endMonth = new Integer(punjabJuiceSiteConfig.get("EndMonth").toString());
				} else {
					Date currentDate = new Date();
					LocalDate presentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					endYear = presentDate.getYear();
					endMonth = presentDate.getMonthValue();
				}

				Calendar startCal = Calendar.getInstance();
				startCal.set(Calendar.YEAR, startYear);
				startCal.set(Calendar.MONTH, startMonth - 1);
				startCal.set(Calendar.DAY_OF_MONTH, 01);
				startCal.set(Calendar.HOUR_OF_DAY, 0);
				startCal.set(Calendar.MINUTE, 0);
				startCal.set(Calendar.SECOND, 0);
				startCal.set(Calendar.MILLISECOND, 0);
				Date startDate = startCal.getTime();

				Calendar endCal = Calendar.getInstance();
				endCal.set(Calendar.YEAR, endYear);
				endCal.set(Calendar.MONTH, endMonth - 1);
				endCal.set(Calendar.DAY_OF_MONTH, 01);
				endCal.set(Calendar.HOUR_OF_DAY, 0);
				endCal.set(Calendar.MINUTE, 0);
				endCal.set(Calendar.SECOND, 0);
				endCal.set(Calendar.MILLISECOND, 0);
				Date endDate = endCal.getTime();

				if ((date.after(startDate) && date.before(endDate)) || date.equals(startDate) || date.equals(endDate)) {
					gst = new Integer(punjabJuiceSiteConfig.get("TaxOrGst").toString());
					calculatedRent = new Double(punjabJuiceSiteConfig.get("rent").toString());
				}

			}
		}

		date = setDateOfMonthMM(date, 1);
		double calculatedGst = calculatedRent * gst / 100;

		ManiMajraDemand maniMajraDemand = ManiMajraDemand.builder().id(UUID.randomUUID().toString())
				.generationDate(date.getTime()).collectionPrincipal(0.0).rent(calculatedRent).typeOfDemand("Monthly")
				.propertyDetailsId(property.getPropertyDetails().getId()).gst(calculatedGst)
				.status(PaymentStatusEnum.UNPAID).build();

		ManiMajraDemand maniMajraDemandAnually = ManiMajraDemand.builder().id(UUID.randomUUID().toString())
				.generationDate(date.getTime()).collectionPrincipal(0.0).rent(annualCalculatedRent)
				.typeOfDemand("Annually").propertyDetailsId(property.getPropertyDetails().getId()).gst(0.0)
				.status(PaymentStatusEnum.UNPAID).build();

		if (null == property.getPropertyDetails().getManiMajraDemands()) {
			List<ManiMajraDemand> maniMajraDemands = new ArrayList<ManiMajraDemand>();

			maniMajraDemands.add(maniMajraDemand);
			if (annualCalculatedRent != 0.0) {
				maniMajraDemands.add(maniMajraDemandAnually);
			}
			property.getPropertyDetails().setManiMajraDemands(maniMajraDemands);
		} else {
			property.getPropertyDetails().getManiMajraDemands().add(maniMajraDemand);
			if (annualCalculatedRent != 0.0) {
				property.getPropertyDetails().getManiMajraDemands().add(maniMajraDemandAnually);
			}
		}
		/**
		 * TODO: Remove the log
		 */
		// log.info("Generating Estate demand id '{}' of principal '{}' for property with file no {}",
		// 		maniMajraDemand.getId(), property.getFileNumber());

	}

	private List<Date> getAllRemainingDates(Date propertyBillingDate) {
		List<Date> allMonthDemandDatesTillCurrentMonth = new ArrayList<>();
		Calendar beginCalendar = Calendar.getInstance();
		Calendar finishCalendar = Calendar.getInstance();

		beginCalendar.setTime(propertyBillingDate);
		finishCalendar.setTime(new Date());

		while (beginCalendar.before(finishCalendar)) {
			// add one month to date per loop
			allMonthDemandDatesTillCurrentMonth.add(beginCalendar.getTime());
			beginCalendar.add(Calendar.MONTH, 1);
		}
		return allMonthDemandDatesTillCurrentMonth;
	}

	public boolean checkSameDay(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
	}

	private Date getFirstDateOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
		cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
		return cal.getTime();
	}


	private Date getFirstMonthOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MONTH, 01 - 1);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
		cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
		return cal.getTime();
	}

}
