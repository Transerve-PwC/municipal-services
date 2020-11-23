package org.egov.ps.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.EstateDemandCriteria;
import org.egov.ps.model.PaymentConfig;
import org.egov.ps.model.PaymentConfigItems;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.IEstateRentCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PropertyRequest;
import org.joda.time.DateTimeComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EstateDemandGenerationService {

	private PropertyRepository propertyRepository;
	private IEstateRentCollectionService estateRentCollectionService;
	private Configuration config;
	private Producer producer;

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	public EstateDemandGenerationService(PropertyRepository propertyRepository, Producer producer, Configuration config,
			IEstateRentCollectionService estateRentCollectionService) {
		this.propertyRepository = propertyRepository;
		this.estateRentCollectionService = estateRentCollectionService;
		this.producer = producer;
		this.config = config;
	}

	public AtomicInteger createDemand(EstateDemandCriteria demandCriteria) {
		List<String> relations = new ArrayList<String>();
		relations.add("owner");
		relations.add("paymentconfig");
		AtomicInteger counter = new AtomicInteger(0);
		PropertyCriteria propertyCriteria = new PropertyCriteria();
		propertyCriteria.setRelations(relations);
		propertyCriteria.setState(Arrays.asList(PSConstants.PM_APPROVED));
		List<Property> propertyList = propertyRepository.getProperties(propertyCriteria);

		propertyList.forEach(property -> {
			try {
				propertyCriteria.setPropertyId(property.getId());
				List<String> propertyDetailsId = Arrays.asList(property.getPropertyDetails().getId());
				List<EstateDemand> estateDemandList = propertyRepository
						.getPropertyDetailsEstateDemandDetails(propertyDetailsId);
				property.getPropertyDetails().setEstateDemands(estateDemandList);
				if (!CollectionUtils.isEmpty(estateDemandList)) {
					List<EstatePayment> estatePaymentList = propertyRepository
							.getPropertyDetailsEstatePaymentDetails(propertyDetailsId);
					property.getPropertyDetails().setEstatePayments(estatePaymentList);
					EstateAccount estateAccount = propertyRepository
							.getAccountDetailsForPropertyDetailsIds(propertyDetailsId);
					property.getPropertyDetails().setEstateAccount(estateAccount);

					Date date = demandCriteria.isEmpty() ? new Date() : FORMATTER.parse(demandCriteria.getDate());
					Date generateDemandDate = getFirstDateOfMonth(new Date());

					List<EstateDemand> existingDemands = estateDemandList.stream().filter(demand -> DateTimeComparator
							.getDateOnlyInstance().compare(demand.getGenerationDate(), date) == 0)
							.collect(Collectors.toList());

					if (property.getPropertyDetails().getPaymentConfig() != null) {
						PaymentConfig paymentConfig = property.getPropertyDetails().getPaymentConfig();
						if (paymentConfig.getGroundRentGenerationType().equalsIgnoreCase(PSConstants.MONTHLY)) {
							generateDemandDate = setDateOfMonth(date,
									Integer.parseInt(paymentConfig.getGroundRentGenerateDemand().toString()));
						}
					}

					if (existingDemands.isEmpty()
							&& DateTimeComparator.getDateOnlyInstance().compare(date, generateDemandDate) == 0) {
						// generate demand
						counter.getAndIncrement();
						generateEstateDemand(property, getFirstDateOfMonth(date), estateDemandList, estatePaymentList,
								estateAccount);
					}
				} else {
					log.debug("We are skipping generating estate demands for this property id: " + property.getId()
							+ " as there is no estate history");
				}

			} catch (Exception e) {
				log.error("exception occured for property id: " + property.getId());
			}
		});

		return counter;
	}

	private void generateEstateDemand(Property property, Date date, List<EstateDemand> estateDemandList,
			List<EstatePayment> estatePaymentList, EstateAccount estateAccount) {

		Double calculatedRent = calculateRentAccordingtoMonth(property, date);
		if (property.getPropertyDetails().getPaymentConfig() != null) {
			PaymentConfig paymentConfig = property.getPropertyDetails().getPaymentConfig();
			if (paymentConfig.getGroundRentGenerationType().equalsIgnoreCase(PSConstants.MONTHLY)) {
				date = setDateOfMonth(date, Integer.parseInt(paymentConfig.getGroundRentGenerateDemand().toString()));
			}
		}

		AuditDetails auditDetails = AuditDetails.builder().createdBy("System").createdTime(new Date().getTime())
				.lastModifiedBy("System").lastModifiedTime(new Date().getTime()).build();

		EstateDemand estateDemand = EstateDemand.builder().id(UUID.randomUUID().toString())
				.propertyDetailsId(property.getPropertyDetails().getId()).generationDate(date.getTime())
				.collectionPrincipal(0.0).remainingPrincipal(calculatedRent).interestSince(date.getTime())
				.isPrevious(false).rent(calculatedRent).penaltyInterest(0.0).gstInterest(0.0)
				.gst(calculatedRent * 18 / 100).noOfDays(0.0).paid(0.0).remainingRent(calculatedRent)
				.remainingGST(calculatedRent * 18 / 100).remainingRentPenalty(0.0).remainingGSTPenalty(0.0)
				.auditDetails(auditDetails).build();

		property.getPropertyDetails().getEstateDemands().add(estateDemand);

		log.info("Generating Estate demand id '{}' of principal '{}' for property with file no {}",
				estateDemand.getId(), property.getFileNumber());

		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstatePayments())
				&& property.getPropertyDetails().getEstateAccount() != null) {

			// Replace the 4th line 18 with the MDMS data
			property.getPropertyDetails().setEstateRentCollections(estateRentCollectionService.settle(
					property.getPropertyDetails().getEstateDemands(), property.getPropertyDetails().getEstatePayments(),
					property.getPropertyDetails().getEstateAccount(), 18,
					property.getPropertyDetails().getPaymentConfig().getIsIntrestApplicable(),
					property.getPropertyDetails().getPaymentConfig().getRateOfInterest().doubleValue()));

		}
		PropertyRequest propertyRequest = new PropertyRequest();
		propertyRequest.setProperties(Collections.singletonList(property));
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateRentCollections())) {
			property.getPropertyDetails().getEstateRentCollections().forEach(collection -> {
				if (collection.getId() == null) {
					collection.setId(UUID.randomUUID().toString());
					collection.setAuditDetails(auditDetails);
				}

			});
		}
		producer.push(config.getUpdatePropertyTopic(), propertyRequest);
	}

	private Double calculateRentAccordingtoMonth(Property property, Date requestedDate) {
		PaymentConfig paymentConfig = property.getPropertyDetails().getPaymentConfig();
		AtomicInteger checkLoopIf = new AtomicInteger();
		if (paymentConfig != null) {
			Date startDate = new Date(paymentConfig.getGroundRentBillStartDate());
			String startDateText = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
			String endDateText = new SimpleDateFormat("yyyy-MM-dd").format(requestedDate);

			/* Check Months between both date */
			long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.parse(startDateText).withDayOfMonth(1),
					LocalDate.parse(endDateText).withDayOfMonth(1));

			for (PaymentConfigItems paymentConfigItem : paymentConfig.getPaymentConfigItems()) {
				if (paymentConfigItem.getGroundRentStartMonth() <= monthsBetween
						&& monthsBetween <= paymentConfigItem.getGroundRentEndMonth()) {
					checkLoopIf.incrementAndGet();
					return paymentConfigItem.getGroundRentAmount().doubleValue();
				}
			}
			if (checkLoopIf.get() == 0) {
				int paymentConfigCount = paymentConfig.getPaymentConfigItems().size() - 1;
				return paymentConfig.getPaymentConfigItems().get(paymentConfigCount).getGroundRentAmount()
						.doubleValue();
			}
		}
		return 0.0;
	}

	private Date setDateOfMonth(Date date, int value) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, value);
		cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));
		cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
		return cal.getTime();
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
