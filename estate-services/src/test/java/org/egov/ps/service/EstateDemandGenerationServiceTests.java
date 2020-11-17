package org.egov.ps.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.ModeEnum;
import org.egov.ps.model.PaymentConfig;
import org.egov.ps.model.PaymentConfigItems;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.IEstateRentCollectionService;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EstateDemandGenerationServiceTests {

	@InjectMocks
	private EstateDemandGenerationService estateDemandGenerationService;
	
	@Mock
	private PropertyRepository propertyRepository;
	@Mock
	private IEstateRentCollectionService estateRentCollectionService;
	@Mock
	private Configuration config;
	@Mock
	private Producer producer;
	
	List<Property> propertyDummyList = new ArrayList<>();
	List<EstateDemand> estateDemandDummyList = new ArrayList<>();
	List<EstatePayment> estatePaymentDummyList = new ArrayList<>();
	EstateAccount estateDummyAccount = EstateAccount.builder().build();
	
	 /* In Setup Functions */
    @Before
    public void setUp() {
    	
    	// Arrange
    	setDummydata();
    }

	private void setDummydata() {
		
		EstateDemand estateDemand = EstateDemand.builder().id(UUID.randomUUID().toString())
				.status(PaymentStatusEnum.PAID)
				.propertyDetailsId(UUID.randomUUID().toString())
				.generationDate(Long.parseLong("1548959400000"))
				.collectionPrincipal(new Double(0))
				.remainingPrincipal(new Double(0))
				.interestSince(Long.parseLong("1553538600000"))
				.isPrevious(false)
				.rent(new Double(2678))
				.penaltyInterest(new Double(268))
				.gstInterest(new Double(482))
				.gst(new Double(18))
				.collectedRent(new Double(2678))
				.collectedGST(new Double(482))
				.collectedGSTPenalty(new Double(0))
				.collectedRentPenalty(new Double(0))
				.paid(new Double(0))
				.remainingRent(new Double(0))
				.remainingRentPenalty(new Double(268))
				.remainingGST(new Double(0))
				.remainingGSTPenalty(new Double(0)).build();
		estateDemandDummyList.add(estateDemand);
		
		EstatePayment estatePayment = EstatePayment.builder()
				.processed(true)
				.id(UUID.randomUUID().toString())
				.propertyDetailsId(UUID.randomUUID().toString())
				.receiptDate(Long.parseLong("1553538600000"))
				.rentReceived(new Double(5356))
				.receiptNo("rec-123")
				.mode(ModeEnum.UPLOAD)
				.build();
		estatePaymentDummyList.add(estatePayment);
		
		estateDummyAccount = EstateAccount.builder()
				.remainingSince(Long.parseLong("1553538600000"))
				.id(UUID.randomUUID().toString())
				.propertyDetailsId(UUID.randomUUID().toString())
				.remainingAmount(new Double(0))
				.build();
		
		PaymentConfigItems paymentConfigItems = new PaymentConfigItems();
		paymentConfigItems.setGroundRentStartMonth(Long.parseLong("1"));
		paymentConfigItems.setGroundRentEndMonth(Long.parseLong("20"));
		paymentConfigItems.setGroundRentAmount(new BigDecimal(1000));
		paymentConfigItems.setId(UUID.randomUUID().toString());
		paymentConfigItems.setPaymentConfigId(UUID.randomUUID().toString());
		
		PaymentConfig paymentConfig = PaymentConfig.builder()
				.groundRentBillStartDate(Long.parseLong("1408905000000"))
				.id(UUID.randomUUID().toString())
				.propertyDetailsId(UUID.randomUUID().toString())
				.paymentConfigItems(Arrays.asList(paymentConfigItems))
				.build();
		
		PropertyDetails propertyDetails = PropertyDetails.builder()
				.estateDemands(estateDemandDummyList)
				.estatePayments(estatePaymentDummyList)
				.estateAccount(estateDummyAccount)
				.paymentConfigs(Arrays.asList(paymentConfig))
				.build();
		
		propertyDummyList.add(Property.builder()
				.id(UUID.randomUUID().toString())
				.propertyDetails(propertyDetails)
				.build());
	}
}
