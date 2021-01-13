package org.egov.cpt.service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.ModeEnum;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandCriteria;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.service.notification.DemandNotificationService;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.util.PropertyUtil;
import org.egov.cpt.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RentDemandGenerationService {

	private PropertyRepository propertyRepository;

	private Producer producer;

	private PropertyConfiguration config;

	private RentCollectionService rentCollectionService;

	private DemandNotificationService demandNotificationService;
	
	@Autowired
	private PropertyUtil propertyUtil;

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("d/MM/yyyy");

	@Autowired
	public RentDemandGenerationService(PropertyRepository propertyRepository, Producer producer,
			PropertyConfiguration config, RentCollectionService rentCollectionService,
			DemandNotificationService demandNotificationService) {
		this.propertyRepository = propertyRepository;
		this.producer = producer;
		this.config = config;
		this.rentCollectionService = rentCollectionService;
		this.demandNotificationService = demandNotificationService;
	}

	public AtomicInteger createDemand(RentDemandCriteria demandCriteria) {
		AtomicInteger counter = new AtomicInteger(0);
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setRelations(Collections.singletonList("owner"));
		propertyCriteria.setState(Arrays.asList(PTConstants.PM_STATUS_APPROVED));
		List<Property> propertyList = propertyRepository.getProperties(propertyCriteria);

		propertyList.forEach(property -> {
			try {
				propertyCriteria.setPropertyId(property.getId());

				List<RentDemand> rentDemandList = propertyRepository.getPropertyRentDemandDetails(propertyCriteria);
				if (!CollectionUtils.isEmpty(rentDemandList)) {
					List<RentPayment> rentPaymentList = propertyRepository
							.getPropertyRentPaymentDetails(propertyCriteria);
					RentAccount rentAccount = propertyRepository.getPropertyRentAccountDetails(propertyCriteria);
					Comparator<RentDemand> compare = Comparator.comparing(RentDemand::getGenerationDate);
					Optional<RentDemand> firstDemand = rentDemandList.stream().min(compare);

					List<Long> dateList = rentDemandList.stream().map(r -> r.getGenerationDate())
							.collect(Collectors.toList());

					Date date = demandCriteria.isEmpty() ? new Date() : FORMATTER.parse(demandCriteria.getDate());
					if (!isMonthIncluded(dateList, date)) {
						// generate demand
						counter.getAndIncrement();
						generateRentDemand(property, firstDemand.get(), getFirstDateOfMonth(date), rentDemandList,
								rentPaymentList, rentAccount);
					}
				} else {
					log.debug("We are skipping generating rent demands for this property id: "
							+ property.getTransitNumber() + " as there is no rent history");
				}
			} catch (Exception e) {
				log.error("exception occured for property id: " + property.getId());
			}
		});
		return counter;
	}

	public AtomicInteger createMissingDemands(Property property) {
		AtomicInteger counter = new AtomicInteger(0);
		List<RentDemand> demands = property.getDemands();
		Comparator<RentDemand> compare = Comparator.comparing(RentDemand::getGenerationDate);
		Optional<RentDemand> firstDemand = demands.stream().collect(Collectors.minBy(compare));
		List<Long> dateList = demands.stream().map(demand -> demand.getGenerationDate()).collect(Collectors.toList());
		Date date = Date.from(Instant.now());
		if (!isMonthIncluded(dateList, date)) {
			counter.getAndIncrement();
			generateRentDemand(property, firstDemand.get(), getFirstDateOfMonth(date), demands, property.getPayments(),
					property.getRentAccount());
		}
		return counter;
	}

	private void generateRentDemand(Property property, RentDemand firstDemand, Date date,
			List<RentDemand> rentDemandList, List<RentPayment> rentPaymentList, RentAccount rentAccount) {
		int oldYear = new Date(firstDemand.getGenerationDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
				.getYear();
		int oldMonth = new Date(firstDemand.getGenerationDate()).toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate().getMonthValue();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int currentYear = localDate.getYear();
		int currentMonth = localDate.getMonthValue();

		Double collectionPrincipal = firstDemand.getCollectionPrincipal();
		oldYear = oldYear + property.getPropertyDetails().getRentIncrementPeriod();
		while (oldYear <= currentYear) {
			if (oldYear == currentYear && currentMonth >= oldMonth) {
				collectionPrincipal = (collectionPrincipal
						* (100 + property.getPropertyDetails().getRentIncrementPercentage())) / 100;
			} else if (oldYear < currentYear) {
				collectionPrincipal = (collectionPrincipal
						* (100 + property.getPropertyDetails().getRentIncrementPercentage())) / 100;
			}
			oldYear = oldYear + property.getPropertyDetails().getRentIncrementPeriod();
		}

		AuditDetails auditDetails = AuditDetails.builder().createdBy("System").createdTime(new Date().getTime())
				.lastModifiedBy("System").lastModifiedTime(new Date().getTime()).build();

		RentDemand rentDemand = RentDemand.builder().id(UUID.randomUUID().toString()).propertyId(property.getId())
				.mode(ModeEnum.GENERATED).generationDate(date.getTime()).collectionPrincipal(collectionPrincipal)
				.auditDetails(auditDetails).remainingPrincipal(collectionPrincipal).interestSince(date.getTime())
				.build();

		log.info("Generating Rent demand id '{}' of principal '{}' for property with transit no {}", rentDemand.getId(),
				collectionPrincipal, property.getTransitNumber());
		property.setDemands(Collections.singletonList(rentDemand));
		property.setRentAccount(rentAccount);
		property.setPayments(rentPaymentList);

		if (!CollectionUtils.isEmpty(property.getPayments()) && property.getRentAccount() != null) {
			long interestStartDate = propertyUtil.getInterstStartFromMDMS(property.getColony(),property.getTenantId());
			property.setRentCollections(rentCollectionService.settle(property.getDemands(), property.getPayments(),
					property.getRentAccount(), property.getPropertyDetails().getInterestRate(),interestStartDate));
		}
		PropertyRequest propertyRequest = new PropertyRequest();
		propertyRequest.setProperties(Collections.singletonList(property));

		if (!CollectionUtils.isEmpty(property.getRentCollections())) {
			property.getRentCollections().forEach(collection -> {
				if (collection.getId() == null) {
					collection.setId(UUID.randomUUID().toString());
					collection.setAuditDetails(auditDetails);
				}

			});
		}

		producer.push(config.getUpdatePropertyTopic(), propertyRequest);
		demandNotificationService.process(rentDemand, property);
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
