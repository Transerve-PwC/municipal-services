package org.egov.ps.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.ManiMajraPayment;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ManiMajraDemandGenerationService {

	private PropertyRepository propertyRepository;
	private IManiMajraRentCollectionService maniMajraRentCollectionService;
	private Configuration config;
	private Producer producer;

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	public ManiMajraDemandGenerationService(PropertyRepository propertyRepository, Producer producer,
			Configuration config, IManiMajraRentCollectionService estateRentCollectionService) {
		this.propertyRepository = propertyRepository;
		this.maniMajraRentCollectionService = estateRentCollectionService;
		this.producer = producer;
		this.config = config;
	}

	@Autowired
	private MDMSService mdmsService;

	public AtomicInteger createMissingDemands(Property property) {

		Boolean monthly = property.getPropertyDetails().getDemandType().equalsIgnoreCase(PSConstants.MONTHLY);

		AtomicInteger counter = new AtomicInteger(0);
		List<ManiMajraDemand> demands = property.getPropertyDetails().getManiMajraDemands();
		Comparator<ManiMajraDemand> compare = Comparator.comparing(ManiMajraDemand::getGenerationDate);
		Optional<ManiMajraDemand> firstDemand = demands.stream().collect(Collectors.minBy(compare));
		List<Long> dateList = demands.stream().map(demand -> demand.getGenerationDate()).collect(Collectors.toList());
		Date date = Date.from(Instant.now());
		if (!isMonthIncluded(dateList, date)) {
			counter.getAndIncrement();
			generateRentDemand(property, firstDemand.get(), getFirstDateOfMonth(date), demands,
					property.getPropertyDetails().getManiMajraPayments(),
					property.getPropertyDetails().getEstateAccount());
		}
		return counter;
	}

	private void generateRentDemand(Property property, ManiMajraDemand firstDemand, Date date,
			List<ManiMajraDemand> rentDemandList, List<ManiMajraPayment> rentPaymentList, EstateAccount rentAccount) {

		int pendingDueYear = property.getPropertyDetails().getMmDemandStartYear();
		int pendingDueMonth = property.getPropertyDetails().getMmDemandStartMonth();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int currentYear = localDate.getYear();
		int currentMonth = localDate.getMonthValue();

		Double collectionPrincipal = firstDemand.getCollectionPrincipal();

//		int startYear = 2015;
//		int endYear = 2016;
//		int startMonth = 2;
//		int endMonth = 8;
		double rent = 1000;
//		int gstOrTax = 18;

		while (pendingDueYear <= currentYear) {
			int demandYear = pendingDueYear;
			if (property.getPropertyDetails().getDemandType().equalsIgnoreCase(PSConstants.MONTHLY)) {

				int minMonth = 1;
				int maxMonth = 12;
				String demandDate;
				while (pendingDueMonth <= maxMonth) {
					int firstDemandYear = new Date(firstDemand.getGenerationDate()).toInstant()
							.atZone(ZoneId.systemDefault()).toLocalDate().getYear();

					if (minMonth >= pendingDueMonth && pendingDueYear == firstDemandYear) {
						demandDate = "1-" + pendingDueMonth + "-" + demandYear;
					} else {
						demandDate = "1-" + pendingDueMonth + "-" + demandYear;
					}
					System.out.println("demanddate---> " + demandDate);
					generateMonthlyDemand(property, collectionPrincipal, rentAccount, rentPaymentList, date);
					pendingDueMonth++;
				}
				pendingDueYear++;

				while (pendingDueYear < currentYear && minMonth <= maxMonth) {
					demandDate = "1-" + minMonth + "-" + pendingDueYear;
					System.out.println("demanddate---> " + demandDate);
					generateMonthlyDemand(property, collectionPrincipal, rentAccount, rentPaymentList, date);
					minMonth++;
				}
				minMonth = 1;

				while (currentYear == pendingDueYear && minMonth <= currentMonth) {
					demandDate = "1-" + minMonth + "-" + currentYear;
					System.out.println("demanddate---> " + demandDate);
					generateMonthlyDemand(property, collectionPrincipal, rentAccount, rentPaymentList, date);
					minMonth++;
				}

			}
			collectionPrincipal = rent;
		}

//		AuditDetails auditDetails = AuditDetails.builder().createdBy("System").createdTime(new Date().getTime())
//				.lastModifiedBy("System").lastModifiedTime(new Date().getTime()).build();
//
//		PropertyRequest propertyRequest = new PropertyRequest();
//		propertyRequest.setProperties(Collections.singletonList(property));
//
//		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getManiMajraRentCollections())) {
//			property.getPropertyDetails().getManiMajraRentCollections().forEach(collection -> {
//				if (collection.getId() == null) {
//					collection.setId(UUID.randomUUID().toString());
//					collection.setAuditDetails(auditDetails);
//				}
//
//			});
//		}
//		producer.push(config.getUpdatePropertyTopic(), propertyRequest);

	}

	private void generateMonthlyDemand(Property property, Double collectionPrincipal, EstateAccount rentAccount,
			List<ManiMajraPayment> rentPaymentList, Date date) {

		ManiMajraDemand rentDemand = ManiMajraDemand.builder().id(UUID.randomUUID().toString())
				.propertyDetailsId(property.getPropertyDetails().getId()).generationDate(date.getTime())
				.collectionPrincipal(collectionPrincipal).build();

		property.getPropertyDetails().setManiMajraDemands(Collections.singletonList(rentDemand));
		property.getPropertyDetails().setEstateAccount(rentAccount);
		property.getPropertyDetails().setManiMajraPayments(rentPaymentList);

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getManiMajraPayments())
				&& property.getPropertyDetails().getEstateAccount() != null) {

			boolean isMonthly = false;
			if (property.getPropertyDetails().getDemandType().equalsIgnoreCase(PSConstants.MONTHLY_DEMAND)) {
				isMonthly = true;
			}

			property.getPropertyDetails().setManiMajraRentCollections(
					maniMajraRentCollectionService.settle(property.getPropertyDetails().getManiMajraDemands(),
							property.getPropertyDetails().getManiMajraPayments(),
							property.getPropertyDetails().getEstateAccount(), isMonthly));
		}

		AuditDetails auditDetails = AuditDetails.builder().createdBy("System").createdTime(new Date().getTime())
				.lastModifiedBy("System").lastModifiedTime(new Date().getTime()).build();

		PropertyRequest propertyRequest = new PropertyRequest();
		propertyRequest.setProperties(Collections.singletonList(property));

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getManiMajraRentCollections())) {
			property.getPropertyDetails().getManiMajraRentCollections().forEach(collection -> {
				if (collection.getId() == null) {
					collection.setId(UUID.randomUUID().toString());
					collection.setAuditDetails(auditDetails);
				}

			});
		}
		producer.push(config.getUpdatePropertyTopic(), propertyRequest);
	}

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
			// TODO Auto-generated catch block
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

		String moduleName = "EstateServices";
		String masterName = "ManiMajra_Rent_Config";
		String tenantId = "ch";
		// TODO: 'calculatedRent' and 'gst' should be comming from MDMS
		List<Map<String, Object>> feesConfigurations = mdmsService.getManimajraPropertyRent(masterName, requestInfo,
				tenantId);
		System.out.println(feesConfigurations);
		int gst = 18;
		Double calculatedRent = 1000D;// calculateRentAccordingtoMonth(property, date);
//		Double calculatedRent = calculateRentAccordingtoMonth(property, date);

		fetchEstimateAmountFromMDMSJson(feesConfigurations, property);

		if (property.getPropertyDetails().getDemandType().equalsIgnoreCase(PSConstants.MONTHLY)) {
			date = setDateOfMonthMM(date, 1);
		}

//			ManiMajraDemand.builder().id(UUID.randomUUID().toString())
//			.propertyDetailsId(property.getPropertyDetails().getId()).generationDate(date.getTime())
//			.collectionPrincipal(collectionPrincipal).build();

		ManiMajraDemand estateDemand = ManiMajraDemand.builder().id(UUID.randomUUID().toString())
				.generationDate(date.getTime()).collectionPrincipal(0.0).rent(calculatedRent)
				.propertyDetailsId(property.getPropertyDetails().getId()).gst(calculatedRent * gst / 100).build();

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

//	private Double calculateRentAccordingtoMonth(Property property, Date requestedDate) {
//		PaymentConfig paymentConfig = property.getPropertyDetails().getPaymentConfig();
//		AtomicInteger checkLoopIf = new AtomicInteger();
//		if (paymentConfig != null
//				&& property.getPropertyDetails().getPropertyType().equalsIgnoreCase(PSConstants.ES_PM_LEASEHOLD)) {
//			Date startDate = new Date(paymentConfig.getGroundRentBillStartDate());
//			String startDateText = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
//			String endDateText = new SimpleDateFormat("yyyy-MM-dd").format(requestedDate);
//
//			/* Check Months between both date */
//			long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.parse(startDateText).withDayOfMonth(1),
//					LocalDate.parse(endDateText).withDayOfMonth(1));
//
//			for (PaymentConfigItems paymentConfigItem : paymentConfig.getPaymentConfigItems()) {
//				if (paymentConfigItem.getGroundRentStartMonth() <= monthsBetween
//						&& monthsBetween <= paymentConfigItem.getGroundRentEndMonth()) {
//					checkLoopIf.incrementAndGet();
//					return paymentConfigItem.getGroundRentAmount().doubleValue();
//				}
//			}
//			if (checkLoopIf.get() == 0) {
//				int paymentConfigCount = paymentConfig.getPaymentConfigItems().size() - 1;
//				return paymentConfig.getPaymentConfigItems().get(paymentConfigCount).getGroundRentAmount()
//						.doubleValue();
//			}
//		}
//		return 0.0;
//	}

	// Used for filter fees by using category and sub-category
	public void fetchEstimateAmountFromMDMSJson(List<Map<String, Object>> feesConfigurations, Property property) {
		BigDecimal responseEstimateAmount = new BigDecimal(0.0);
		Integer compareVarForEstimateAmount = 0;
		for (Map<String, Object> feesConfig : feesConfigurations) {

			System.out.println("StartYear--->   " + feesConfig.get("StartYear"));
		}

	}
}
