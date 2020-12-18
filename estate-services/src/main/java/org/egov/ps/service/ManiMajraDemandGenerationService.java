package org.egov.ps.service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

		int oldYear = new Date(firstDemand.getGenerationDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
				.getYear();
		int oldMonth = new Date(firstDemand.getGenerationDate()).toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate().getMonthValue();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int currentYear = localDate.getYear();
		int currentMonth = localDate.getMonthValue();

		Double collectionPrincipal = firstDemand.getCollectionPrincipal();

		int startYear = 2015;
		int endYear = 2016;
		int startMonth = 2;
		int endMonth = 8;
		int rent = 1000;
		int gstOrTax = 18;

//		oldYear = oldYear + property.getPropertyDetails().getRentIncrementPeriod();
//		while (oldYear <= currentYear) {
//			if (oldYear == currentYear && currentMonth >= oldMonth) {
//				collectionPrincipal = (collectionPrincipal
//						* (100 + property.getPropertyDetails().getRentIncrementPercentage())) / 100;
//			} else if (oldYear < currentYear) {
//				collectionPrincipal = (collectionPrincipal
//						* (100 + property.getPropertyDetails().getRentIncrementPercentage())) / 100;
//			}
//			oldYear = oldYear + property.getPropertyDetails().getRentIncrementPeriod();
//		}

		AuditDetails auditDetails = AuditDetails.builder().createdBy("System").createdTime(new Date().getTime())
				.lastModifiedBy("System").lastModifiedTime(new Date().getTime()).build();

		ManiMajraDemand rentDemand = ManiMajraDemand.builder().id(UUID.randomUUID().toString())
				.propertyDetailsId(property.getPropertyDetails().getId()).generationDate(date.getTime())
				.collectionPrincipal(collectionPrincipal).build();

		property.getPropertyDetails().setManiMajraDemands(Collections.singletonList(rentDemand));
		property.getPropertyDetails().setEstateAccount(rentAccount);
		property.getPropertyDetails().setManiMajraPayments(rentPaymentList);

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getManiMajraPayments())
				&& property.getPropertyDetails().getEstateAccount() != null) {

			boolean isMonthly = false;
			if (property.getPropertyDetails().getDemandType().contentEquals(PSConstants.MONTHLY_DEMAND)) {
				isMonthly = true;
			}

			property.getPropertyDetails().setManiMajraRentCollections(
					maniMajraRentCollectionService.settle(property.getPropertyDetails().getManiMajraDemands(),
							property.getPropertyDetails().getManiMajraPayments(),
							property.getPropertyDetails().getEstateAccount(), isMonthly));
		}
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
}
