package org.egov.cpt.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.ModeEnum;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyDetails;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentCollection;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandCriteria;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class RentDemandGenerationServiceTest {

	@InjectMocks
	RentDemandGenerationService rentDemandGenerationService;

	@Mock
	private PropertyRepository propertyRepository;

	@Mock
	private Producer producer;

	@Mock
	private PropertyConfiguration config;

	@Mock
	private RentCollectionService rentCollectionService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(propertyRepository.getProperties(Mockito.any())).thenReturn(buildPropertyList());
	}

	@Test
	public void createDemandTest() {
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	@Test
	public void createDemandTestWithDemandCriteriaAndDemandAlreadyExists() {
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteriaWhereDemandAlreadyExists());
	}

	@Test
	public void createDemandTestWithoutDemandHistory() {
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	@Test
	public void createDemandTestWithoutDemandCriteria() {
		// Mockito.when(propertyutil.getAuditDetails(Mockito.anyString(),
		// Mockito.anyBoolean())).thenReturn(buildAuditDetails());
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(new RentDemandCriteria());
	}

	@Test
	public void createDemandTestWithoutDemandCriteriaAndDenandAlreadyExists() {
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any()))
				.thenReturn(buildRentDemandListWithCurrentDate());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(new RentDemandCriteria());
	}

	@Test
	public void createDemandTestThrowsException() {
		Mockito.when(propertyRepository.getProperties(Mockito.any()))
				.thenReturn(buildPropertyListWithoutPropertyDetails());
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	@Test
	public void createDemandTestWithoutRentAccount() {
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	@Test
	public void createDemandTestWithoutPayments() {
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	@Test
	public void createDemandTestWithRentCollectionsWithoutId() {
		Mockito.when(rentCollectionService.settle(Mockito.anyListOf(RentDemand.class),
				Mockito.anyListOf(RentPayment.class), Mockito.any(RentAccount.class), Mockito.anyDouble()))
				.thenReturn(buildRentCollections());
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	@Test
	public void createDemandTestWithRentCollectionsWithId() {
		Mockito.when(rentCollectionService.settle(Mockito.anyListOf(RentDemand.class),
				Mockito.anyListOf(RentPayment.class), Mockito.any(RentAccount.class), Mockito.anyDouble()))
				.thenReturn(buildRentCollectionsWithId());
		Mockito.when(propertyRepository.getPropertyRentDemandDetails(Mockito.any())).thenReturn(buildRentDemandList());
		Mockito.when(propertyRepository.getPropertyRentPaymentDetails(Mockito.any()))
				.thenReturn(buildRentPaymentList());
		Mockito.when(propertyRepository.getPropertyRentAccountDetails(Mockito.any())).thenReturn(buildRentAccount());
		rentDemandGenerationService.createDemand(buildDemandCriteria());
	}

	Long[] generationDates = new Long[] { new GregorianCalendar(2020, Calendar.JANUARY, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.FEBRUARY, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.MARCH, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.APRIL, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.MAY, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.JUNE, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.JULY, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.AUGUST, 1).getTimeInMillis(),
			new GregorianCalendar(2020, Calendar.SEPTEMBER, 1).getTimeInMillis(), };

	@Test
	public void testGeneratedDate() {
		assertFalse("Oct 9th Should not be included", isMonthIncluded(Arrays.asList(generationDates),
				new GregorianCalendar(2020, Calendar.OCTOBER, 9, 10, 25).getTime()));
		assertTrue("May 15th should be included", isMonthIncluded(Arrays.asList(generationDates),
				new GregorianCalendar(2020, Calendar.MAY, 15, 15, 15).getTime()));
	}

	private boolean isMonthIncluded(List<Long> dates, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
		cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getActualMinimum(Calendar.HOUR_OF_DAY));
		long firstDaysDate = cal.getTimeInMillis();
		return dates.contains(new Long(firstDaysDate));
	}

	private RentDemandCriteria buildDemandCriteria() {
		RentDemandCriteria rentDemandCriteria = new RentDemandCriteria();
		rentDemandCriteria.setDate("01/04/2020");
		return rentDemandCriteria;
	}

	private RentDemandCriteria buildDemandCriteriaWhereDemandAlreadyExists() {
		RentDemandCriteria rentDemandCriteria = new RentDemandCriteria();
		rentDemandCriteria.setDate("02/06/2015");
		return rentDemandCriteria;
	}

	private List<Property> buildPropertyList() {
		Property property = new Property();
		property.setId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		property.setTransitNumber("1234");
		property.setTenantId("ch.chandigarh");
		property.setArea("pato plaza");
		PropertyDetails propertyDetails = new PropertyDetails();
		propertyDetails.setInterestRate(24D);
		propertyDetails.setRentIncrementPercentage(5D);
		propertyDetails.setRentIncrementPeriod(1);
		property.setPropertyDetails(propertyDetails);
		return Collections.singletonList(property);
	}

	private List<Property> buildPropertyListWithoutPropertyDetails() {
		Property property = new Property();
		property.setId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		property.setTransitNumber("1234");
		property.setTenantId("ch.chandigarh");
		property.setArea("pato plaza");
		return Collections.singletonList(property);
	}

	private List<RentDemand> buildRentDemandList() {
		RentDemand rentDemand = new RentDemand();
		rentDemand.setCollectionPrincipal(100D);
		rentDemand.setGenerationDate(1433184076000L);
		rentDemand.setId("d2fed7b6-eb32-4b56-99d5-0361285e43eg");
		rentDemand.setInitialGracePeriod(1);
		rentDemand.setInterestSince(1433184076000L);
		rentDemand.setMode(ModeEnum.GENERATED);
		rentDemand.setPropertyId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		rentDemand.setRemainingPrincipal(100D);
		return Collections.singletonList(rentDemand);
	}

	private List<RentDemand> buildRentDemandListWithCurrentDate() {
		RentDemand rentDemand = new RentDemand();
		rentDemand.setCollectionPrincipal(100D);
		rentDemand.setGenerationDate(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		rentDemand.setId("d2fed7b6-eb32-4b56-99d5-0361285e43eg");
		rentDemand.setInitialGracePeriod(1);
		rentDemand.setInterestSince(1433184076000L);
		rentDemand.setMode(ModeEnum.GENERATED);
		rentDemand.setPropertyId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		rentDemand.setRemainingPrincipal(100D);
		return Collections.singletonList(rentDemand);
	}

	private List<RentPayment> buildRentPaymentList() {
		RentPayment rentPayment = new RentPayment();
		rentPayment.setId("d4fed7b6-eb22-4b56-99e7-0864295w42tf");
		rentPayment.setPropertyId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		rentPayment.setReceiptNo("rpt19");
		rentPayment.setAmountPaid(50D);
		rentPayment.setDateOfPayment(1599046968891L);
		return Collections.singletonList(rentPayment);
	}

	private RentAccount buildRentAccount() {
		RentAccount rentAccount = new RentAccount();
		rentAccount.setId("d6frd7b4-eb25-4b59-11d4-0361285e42df");
		rentAccount.setPropertyId("d1fed7b6-eb22-4b56-99d4-0361285e42df");
		rentAccount.setRemainingAmount(50D);
		return rentAccount;
	}

	private List<RentCollection> buildRentCollections() {
		RentCollection rentCollection = new RentCollection();
		rentCollection.setDemandId("d2fed7b6-eb32-4b56-99d5-0361285e43eg");
		rentCollection.setInterestCollected(50D);
		rentCollection.setPrincipalCollected(50D);
		rentCollection.setCollectedAt(1599046968891L);
		return Collections.singletonList(rentCollection);
	}

	private List<RentCollection> buildRentCollectionsWithId() {
		RentCollection rentCollection = new RentCollection();
		rentCollection.setId("w2fed7b6-eb32-4b56-99d5-0361285e43ut");
		rentCollection.setDemandId("d2fed7b6-eb32-4b56-99d5-0361285e43eg");
		rentCollection.setInterestCollected(50D);
		rentCollection.setPrincipalCollected(50D);
		rentCollection.setCollectedAt(1599046968891L);
		return Collections.singletonList(rentCollection);
	}

}
