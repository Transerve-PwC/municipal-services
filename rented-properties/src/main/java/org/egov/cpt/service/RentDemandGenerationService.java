package org.egov.cpt.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandCriteria;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.service.notification.DemandNotificationService;
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

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d/MM/yyyy");

	@Autowired
	public RentDemandGenerationService(PropertyRepository propertyRepository, Producer producer,
			PropertyConfiguration config, RentCollectionService rentCollectionService, DemandNotificationService demandNotificationService) {
		this.propertyRepository = propertyRepository;
		this.producer = producer;
		this.config = config;
		this.rentCollectionService = rentCollectionService;
		this.demandNotificationService = demandNotificationService;
	}

	public void createDemand(RentDemandCriteria demandCriteria, RequestInfo requestInfo) {

		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setRelations(Collections.singletonList("owner"));
		List<Property> propertyList = propertyRepository.getProperties(propertyCriteria);

		propertyList.forEach(property -> {
			try {
				propertyCriteria.setPropertyId(property.getId());

				List<RentDemand> rentDemandList = propertyRepository.getPropertyRentDemandDetails(propertyCriteria);
				List<RentPayment> rentPaymentList = propertyRepository.getPropertyRentPaymentDetails(propertyCriteria);
				RentAccount rentAccount = propertyRepository.getPropertyRentAccountDetails(propertyCriteria);

				if (!CollectionUtils.isEmpty(rentDemandList)) {
					Comparator<RentDemand> compare = Comparator.comparing(RentDemand::getGenerationDate);
					Optional<RentDemand> collectionDemand = rentDemandList.stream().min(compare);

					List<String> dateList = rentDemandList.stream().map(r -> r.getGenerationDate()).map(
							date -> new Date(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString())
							.collect(Collectors.toList());

					if (demandCriteria.isEmpty()) {
						LocalDate currentDate = LocalDate.now();
						if (!dateList.contains(currentDate.toString())) {
							// generate demand
							generateRentDemand(property, collectionDemand.get(), currentDate, rentDemandList,
									rentPaymentList, rentAccount, requestInfo);
						}
					} else {
						LocalDate date = LocalDate.parse(demandCriteria.getDate(), FORMATTER);
						if (!dateList.contains(date.toString())) {
							// generate demand
							generateRentDemand(property, collectionDemand.get(), date, rentDemandList, rentPaymentList,
									rentAccount, requestInfo);
						}
					}
				} else {
					log.debug("We are skipping generating rent demands for this property id: " + property.getId()
							+ " as there is no rent history");
				}
			} catch (Exception e) {
				log.error("exception occured for property id: " + property.getId());
			}
		});

	}

	private void generateRentDemand(Property property, RentDemand collectionDemand, LocalDate date,
			List<RentDemand> rentDemandList, List<RentPayment> rentPaymentList, RentAccount rentAccount, RequestInfo requestInfo) {

		int oldYear = new Date(collectionDemand.getGenerationDate()).toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate().getYear();
		int oldMonth = new Date(collectionDemand.getGenerationDate()).toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate().getMonthValue();
		int currentYear = date.getYear();
		int currentMonth = date.getMonthValue();
		RentDemand rentDemand = new RentDemand();
		String rendDemandId = UUID.randomUUID().toString();
		rentDemand.setId(rendDemandId);
		rentDemand.setPropertyId(property.getId());
		Long creationTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		rentDemand.setGenerationDate(creationTime);

		Double collectionPrincipal = collectionDemand.getCollectionPrincipal();
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

		rentDemand.setCollectionPrincipal(collectionPrincipal);
		rentDemand.setRemainingPrincipal(rentDemand.getCollectionPrincipal());
		rentDemand.setInterestSince(rentDemand.getGenerationDate());

		AuditDetails auditDetails = new AuditDetails();
		auditDetails.setCreatedBy("System");
		auditDetails.setCreatedTime(creationTime);
		auditDetails.setLastModifiedBy("System");
		auditDetails.setLastModifiedTime(creationTime);
		rentDemand.setAuditDetails(auditDetails);
		
		List<RentDemand> newRentDemandList = new ArrayList<>(rentDemandList);
		newRentDemandList.add(rentDemand);

		log.info("rend demand id: " + rendDemandId);
		log.info("collection principal: " + collectionPrincipal);
		property.setDemands(newRentDemandList);
		property.setRentAccount(rentAccount);
		property.setPayments(rentPaymentList);

		if (!CollectionUtils.isEmpty(property.getPayments()) && property.getRentAccount() != null) {

			property.setRentCollections(rentCollectionService.settle(property.getDemands(), property.getPayments(),
					property.getRentAccount(), property.getPropertyDetails().getInterestRate()));
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
		demandNotificationService.process(rentDemand, property, requestInfo);
	}

}
