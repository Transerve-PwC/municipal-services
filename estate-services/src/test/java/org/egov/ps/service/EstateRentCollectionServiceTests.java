package org.egov.ps.service;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import org.egov.ps.service.calculation.EstateRentCollectionService;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RunWith(JUnit4.class)
@SuppressWarnings("unused")
public class EstateRentCollectionServiceTests {
    private static final String JAN_1_2000 = "01 01 2000";
    private static final String MAY_1_2000 = "01 05 2000";
    private static final String JUN_1_2000 = "01 06 2000";
    private static final String JAN_1_2020 = "01 01 2020";
    private static final String JAN_15_2020 = "15 01 2020";
    private static final String FEB_1_2020 = "01 02 2020";
    private static final String FEB_15_2020 = "15 02 2020";
    private static final String MAY_1_2020 = "01 05 2020";
    private static final String DEC_1_1998 = "01 12 1998";
    private static final String JAN_1_1999 = "01 01 1999";
    private static final String FEB_1_1999 = "01 02 1999";
    private static final String FEB_16_1999 = "16 02 1999";
    private static final String MAR_1_1999 = "01 03 1999";
    private static final String APR_1_1999 = "01 04 1999";
    private static final String MAY_1_1999 = "01 05 1999";
    private static final String JUN_1_1999 = "01 06 1999";
    private static final String JUL_1_1999 = "01 07 1999";
    private static final String AUG_1_1999 = "01 08 1999";
    private static final String SEP_1_1999 = "01 09 1999";
    private static final String OCT_1_1999 = "01 10 1999";
    private static final String NOV_1_1999 = "01 11 1999";
    private static final String DEC_1_1999 = "01 12 1999";
    private static final String APR_1_2020 = "01 04 2020";
    private static final String DEC_1_2020 = "01 12 2020";
    private static final String MAR_7_1999 = "07 03 1999";

    private static final String AUG_7_1999 = "07 08 1999";


    public static final double DEFAULT_INTEREST_RATE = 24D;
    private static final double ZERO_INTEREST_RATE = 0D;

    EstateRentCollectionService estateRentCollectionService;
   

    @Before
    public void setup() {
        this.estateRentCollectionService = new EstateRentCollectionService();
        
    }

    /**
     * Always make sure the total paid amount = total collections +
     * 
     * Initial account balance : 0 Payment : 100 collection 100 final account
     * balance : 100
     * 
     * @throws ParseException
     */
    @Test
    public void testSimpleSettlement() throws ParseException {
        // Setup
      //  List<EstateDemand> demands = Collections.emptyList();

        List<EstateDemand> demands = Arrays.asList( getDemand(2678D,482 ,FEB_1_1999,"102",268,10), getDemand(2678D, 482 ,MAR_1_1999,"103",268,6));

        List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999));
        EstateAccount account = getAccount(0D);

        // Test
        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,true);

        // Verify

        
        
        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
        assertEquals(4539.02, collection, 0.1);
        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
        assertEquals(816.98, collectionGST, 0.1);
       // reconcileDemands(demands, collections);
        verifyRemainingBalance(account, 0D);
    }
    
    
    @Test
    public void testSimpleSettlement_Case2() throws ParseException {
        // Setup
      //  List<EstateDemand> demands = Collections.emptyList();
        List<EstateDemand> demands = Arrays.asList( 
        		getDemand(2678D,482 ,FEB_1_1999,"102",268,10), getDemand(2678D, 482 ,MAR_1_1999,"103",268,6),
        		getDemand(2678D, 482 ,APR_1_1999,"104",268,30),getDemand(2678D, 482 ,MAY_1_1999,"103",268,23),
        		getDemand(2678D, 482 ,JUN_1_1999,"103",268,16),getDemand(2678D, 482 ,JUL_1_1999,"103",268,9),
        		getDemand(2678D, 482 ,AUG_1_1999,"103",268,0)
        		);
        List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999),getPayment(10712D, AUG_7_1999));
        EstateAccount account = getAccount(0D);

        // Test
        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,true);

        // Verify
        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
        assertEquals(13617.12, collection, 0.1);
        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
        assertEquals(2450.80, collectionGST, 0.1);
       // reconcileDemands(demands, collections);
        verifyRemainingBalance(account, 0D);
    }
    
    
    private EstatePayment getPayment(double amount, String date) throws ParseException {
        return EstatePayment.builder().rentReceived(amount).receiptNo("X-1212").receiptDate(getEpochFromDateString(date)).build();
    }
    private EstateDemand getDemand(double amount, Integer gst, String date, String demandId,double penaltyInterest,double gstInterest) throws ParseException {
        return EstateDemand.builder()
        		.rent(amount)
        		.gst(gst)
        		.penaltyInterest(penaltyInterest)
        		.gstInterest(gstInterest)
        		.demandDate(getEpochFromDateString(date))
        		.id(demandId)
        		.remainingRent(amount)
        		.remainingGST(new Double(gst))
        		.remainingRentPenalty(penaltyInterest)
        		.remainingGSTPenalty(gstInterest)

               .build();
    }

   
   

      

    private long getEpochFromDateString(String date) throws ParseException {
        return this.getDateFromString(date).getTime();
    }

    private static final String DATE_FORMAT = "dd MM yyyy";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    private Date getDateFromString(String date) throws ParseException {
        return dateFormatter.parse(date);
    }

   
   
   

    private EstateAccount getAccount(double initialBalance) {
        return EstateAccount.builder().remainingAmount(initialBalance).build();
    }

    private void reconcileDemands(List<EstateDemand> demands, List<EstateRentCollection> collections) {

      //  double collectionAccordingToDemands = demands.stream()
        //        .mapToDouble(demand -> demand.getCollectedRent() - demand.getCollectedRentPenalty()).sum();
        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
        assertEquals(13617.12, collection, 0.1);
        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
        assertEquals(2450.80, collectionGST, 0.1);

    }

    

    private void verifyRemainingBalance(EstateAccount account, double expectedBalance) {
        assertEquals(expectedBalance, account.getRemainingAmount(), 0.1);
    }
}