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

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		for (Date demandDate : allMonthDemandDatesTillCurrentMonth) {
			Date demandGenerationStartDate = setDateOfMonthMM(demandDate, 1);

			/* Here checking demand date is already created or not */
			List<ManiMajraDemand> inRequestDemands = new ArrayList<ManiMajraDemand>();
			if (null != property.getPropertyDetails().getManiMajraDemands()) {
				inRequestDemands = property.getPropertyDetails().getManiMajraDemands().stream()
						.filter(demand -> checkSameDay(new Date(demand.getGenerationDate()), demandGenerationStartDate))
						.collect(Collectors.toList());
			}
			if (inRequestDemands.isEmpty()
					&& property.getPropertyDetails().getBranchType().equalsIgnoreCase(PSConstants.MANI_MAJRA)) {
				// generate demand
				counter.getAndIncrement();
				generateEstateDemandMM(property, getFirstDateOfMonth(demandGenerationStartDate), requestInfo);
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

	private void generateEstateDemandMM(Property property, Date date, RequestInfo requestInfo) {

		int gst = 0;
		Double calculatedRent = 0D;

		List<Map<String, Object>> feesConfigurations = mdmsService
				.getManimajraPropertyRent(PSConstants.MM_PROPERTY_RENT_MASTER_NAME, requestInfo, PSConstants.TENANT_ID);

		for (Map<String, Object> feesConfig : feesConfigurations) {
			Integer startYear = new Integer(feesConfig.get("StartYear").toString());
			Integer startMonth = new Integer(feesConfig.get("StartMonth").toString());
			Integer endYear;
			Integer endMonth;
			if (null != feesConfig.get("EndYear") && null != feesConfig.get("EndMonth")) {
				endYear = new Integer(feesConfig.get("EndYear").toString());
				endMonth = new Integer(feesConfig.get("EndMonth").toString());
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
				gst = new Integer(feesConfig.get("TaxOrGst").toString());
				calculatedRent = new Double(feesConfig.get("rent").toString());
			}

		}

		date = setDateOfMonthMM(date, 1);
		double calculatedGst = calculatedRent * gst / 100;

		ManiMajraDemand estateDemand = ManiMajraDemand.builder().id(UUID.randomUUID().toString())
				.generationDate(date.getTime()).collectionPrincipal(0.0).rent(calculatedRent)
				.propertyDetailsId(property.getPropertyDetails().getId()).gst(calculatedGst)
				.status(PaymentStatusEnum.UNPAID).build();

		if (null == property.getPropertyDetails().getManiMajraDemands()) {
			List<ManiMajraDemand> maniMajraDemands = new ArrayList<ManiMajraDemand>();

			maniMajraDemands.add(estateDemand);
			property.getPropertyDetails().setManiMajraDemands(maniMajraDemands);
		} else {
			property.getPropertyDetails().getManiMajraDemands().add(estateDemand);
		}

		log.info("Generating Estate demand id '{}' of principal '{}' for property with file no {}",
				estateDemand.getId(), property.getFileNumber());

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

	private boolean isMonthIncluded(List<Long> dates, Date date) {
		final Date givenDate = getFirstDateOfMonth(date);
		return dates.stream().map(d -> new Date(d))
				.anyMatch(d -> getFirstDateOfMonth(d).getTime() == givenDate.getTime());
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

	// private Double calculateRentAccordingtoMonth(Property property, Date
	// requestedDate) {
	// PaymentConfig paymentConfig =
	// property.getPropertyDetails().getPaymentConfig();
	// AtomicInteger checkLoopIf = new AtomicInteger();
	// if (paymentConfig != null
	// &&
	// property.getPropertyDetails().getPropertyType().equalsIgnoreCase(PSConstants.ES_PM_LEASEHOLD))
	// {
	// Date startDate = new Date(paymentConfig.getGroundRentBillStartDate());
	// String startDateText = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
	// String endDateText = new
	// SimpleDateFormat("yyyy-MM-dd").format(requestedDate);
	//
	// /* Check Months between both date */
	// long monthsBetween =
	// ChronoUnit.MONTHS.between(LocalDate.parse(startDateText).withDayOfMonth(1),
	// LocalDate.parse(endDateText).withDayOfMonth(1));
	//
	// for (PaymentConfigItems paymentConfigItem :
	// paymentConfig.getPaymentConfigItems()) {
	// if (paymentConfigItem.getGroundRentStartMonth() <= monthsBetween
	// && monthsBetween <= paymentConfigItem.getGroundRentEndMonth()) {
	// checkLoopIf.incrementAndGet();
	// return paymentConfigItem.getGroundRentAmount().doubleValue();
	// }
	// }
	// if (checkLoopIf.get() == 0) {
	// int paymentConfigCount = paymentConfig.getPaymentConfigItems().size() - 1;
	// return
	// paymentConfig.getPaymentConfigItems().get(paymentConfigCount).getGroundRentAmount()
	// .doubleValue();
	// }
	// }
	// return 0.0;
	// }
}
