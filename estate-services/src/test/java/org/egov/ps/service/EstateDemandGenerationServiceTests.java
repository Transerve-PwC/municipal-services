package org.egov.ps.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.EstateDemandCriteria;
import org.egov.ps.model.ModeEnum;
import org.egov.ps.model.PaymentConfig;
import org.egov.ps.model.PaymentConfigItems;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.producer.Producer;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.service.calculation.IEstateRentCollectionService;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.junit.Before;
import org.junit.Test;
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
    	
    	when(propertyRepository.getProperties(anyObject())).thenReturn(propertyDummyList);
    	when(propertyRepository.getPropertyDetailsEstateDemandDetails(anyList())).thenReturn(estateDemandDummyList);
    	when(propertyRepository.getPropertyDetailsEstatePaymentDetails(anyList())).thenReturn(estatePaymentDummyList);
    	when(propertyRepository.getAccountDetailsForPropertyDetailsIds(anyList())).thenReturn(estateDummyAccount);
    }
    
    @Test
    public void createDemandTest() {
    	
    	AtomicInteger dummayResult = estateDemandGenerationService.createDemand(EstateDemandCriteria.builder().build());
    	System.out.println(dummayResult);
    	assertTrue("Error, can't update more than one record", 1 >= dummayResult.get());
    }
    

	private void setDummydata() {
		
	
		/* Set Dummy data for EstateDemand */		
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
		
		EstateDemand estateDemand2 = EstateDemand.builder().id(UUID.randomUUID().toString())
				.status(PaymentStatusEnum.PAID)
				.propertyDetailsId(UUID.randomUUID().toString())
				.generationDate(Long.parseLong("1551378600000"))
				.collectionPrincipal(new Double(0))
				.remainingPrincipal(new Double(0))
				.interestSince(Long.parseLong("1553538600000"))
				.isPrevious(false)
				.rent(new Double(2678))
				.penaltyInterest(new Double(268))
				.gstInterest(new Double(482))
				.gst(new Double(18))
				.collectedRent(new Double(2196))
				.collectedGST(new Double(482))
				.collectedGSTPenalty(new Double(0))
				.collectedRentPenalty(new Double(0))
				.paid(new Double(0))
				.remainingRent(new Double(482))
				.remainingRentPenalty(new Double(268))
				.remainingGST(new Double(482))
				.remainingGSTPenalty(new Double(0)).build();
		estateDemandDummyList.add(estateDemand);
		estateDemandDummyList.add(estateDemand2);
		
		/* Set Dummy data for EstatePayment */
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
		
		/* Set Dummy data for EstateAccount */
		estateDummyAccount = EstateAccount.builder()
				.remainingSince(Long.parseLong("1553538600000"))
				.id(UUID.randomUUID().toString())
				.propertyDetailsId(UUID.randomUUID().toString())
				.remainingAmount(new Double(0))
				.build();
		
		/* Set Dummy data for PaymentConfigItems */
		PaymentConfigItems paymentConfigItems = new PaymentConfigItems();
		paymentConfigItems.setGroundRentStartMonth(Long.parseLong("1"));
		paymentConfigItems.setGroundRentEndMonth(Long.parseLong("20"));
		paymentConfigItems.setGroundRentAmount(new BigDecimal(1000));
		paymentConfigItems.setId(UUID.randomUUID().toString());
		paymentConfigItems.setPaymentConfigId(UUID.randomUUID().toString());
		
		PaymentConfigItems paymentConfigItems2 = new PaymentConfigItems();
		paymentConfigItems2.setGroundRentStartMonth(Long.parseLong("21"));
		paymentConfigItems2.setGroundRentEndMonth(Long.parseLong("40"));
		paymentConfigItems2.setGroundRentAmount(new BigDecimal(2000));
		paymentConfigItems2.setId(UUID.randomUUID().toString());
		paymentConfigItems2.setPaymentConfigId(UUID.randomUUID().toString());
		
		/* Set Dummy data for PaymentConfig */
		PaymentConfig paymentConfig = PaymentConfig.builder()
				.groundRentBillStartDate(Long.parseLong("1566671400000"))
				.id(UUID.randomUUID().toString())
				.propertyDetailsId(UUID.randomUUID().toString())
				.paymentConfigItems(Arrays.asList(paymentConfigItems,paymentConfigItems2))
				.isGroundRent(true)
				.groundRentGenerateDemand("18")
				.groundRentGenerationType(PSConstants.MONTHLY)
				.build();
		
		/* Set Dummy data for PropertyDetails */
		PropertyDetails propertyDetails = PropertyDetails.builder()
				.estateDemands(estateDemandDummyList)
				.estatePayments(estatePaymentDummyList)
				.estateAccount(estateDummyAccount)
				.paymentConfigs(Arrays.asList(paymentConfig))
				.id(UUID.randomUUID().toString())
				.build();
		
		/* Set Dummy data for EstateDemand */
		propertyDummyList.add(Property.builder()
				.id(UUID.randomUUID().toString())
				.propertyDetails(propertyDetails)
				.build());
	}
}
