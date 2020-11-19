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
import org.egov.ps.util.EstateRentCollectionUtils;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateAccountStatement;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.egov.ps.web.contracts.EstateRentSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RunWith(JUnit4.class)
@SuppressWarnings("unused")
public class EstateRentCollectionServiceTests1 {
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
    private static final String FEB_17_2000 = "17 02 2000";
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
    private static final String MAR_1_2020 = "01 03 2000";
    private static final String JUN_1_2020="01 06 2000";
    private static final String JUL_1_2020="01 07 2000";
    private static final String AUG_1_2020="01 08 2000";
    private static final String SEP_1_2020="01 09 2000";
    private static final String JUl_7_2000 = "07 07 2000";
    private static final String NOV_30_2000 = "30 11 2000";
    private static final String MAR_27_1999 = "27 03 1999";
    private static final String APR_15_1999="15 04 1999";
    private static final String AUG_19_1999="19 08 1999";
    
    public static final double DEFAULT_INTEREST_RATE = 18D;
 //   private static final double ZERO_INTEREST_RATE = 0D;

    EstateRentCollectionService estateRentCollectionService;
    EstateRentCollectionUtils utils;
   

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
      	 List<EstateDemand> demands = Arrays.asList( getDemand(2678D,482 ,FEB_1_1999,"102",0,0,0,0,0,0,false), getDemand(2678D, 482 ,MAR_1_1999,"103",0,0,0,0,0,0,false));
         List<EstatePayment> payments = Arrays.asList( getPayment(10356D, MAR_7_1999,MAR_7_1999));

        EstateAccount account = getAccount(0D);

        // Test
        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
        		DEFAULT_INTEREST_RATE,true);

        // Verify
        
        
        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
        assertEquals(5356.00, collection, 0.1);
        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
        assertEquals(964, collectionGST, 0.1);
       // reconcileDemands(demands, collections);
        verifyRemainingBalance(account, 3760.1);
    }
    
    
    @Test
    public void testSimpleSettlement_Case2() throws ParseException {
    	List<EstateDemand> demands = Arrays.asList( getDemand(2678D,482 ,FEB_1_1999,"102",0,0,0,0,0,0,false), getDemand(2678D, 482 ,MAR_1_1999,"103",0,0,0,0,0,0,false));
        List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999,MAR_7_1999));

       EstateAccount account = getAccount(0D);

       // Test
       List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
    		   DEFAULT_INTEREST_RATE,true);

       // Verify
       
       
       double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
       assertEquals(4539.00, collection, 0.1);
       double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
       assertEquals(816.9, collectionGST, 0.1);
      // reconcileDemands(demands, collections);
       verifyRemainingBalance(account, 0.0);
    }
    
    
   
    @Test
    public void testSimpleSettlement_Case5() throws ParseException {
        // Setup
      //  List<EstateDemand> demands = Collections.emptyList();
        List<EstateDemand> demands = Arrays.asList( 
        		
        		getDemand(2678D,482 ,FEB_1_1999,"102",0,0,0,0,0,0,false), getDemand(2678D, 482 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,APR_1_1999,"104",0,0,0,0,0,0,false),getDemand(2678D, 482 ,MAY_1_1999,"105",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,JUN_1_1999,"106",0,0,0,0,0,0,false),getDemand(2678D, 482 ,JUL_1_1999,"107",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,AUG_1_1999,"108",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,AUG_1_1999,"109",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,SEP_1_1999,"110",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,OCT_1_1999,"111",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,NOV_1_1999,"112",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,DEC_1_1999,"113",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,JAN_1_2000,"114",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,FEB_1_2020,"115",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,MAR_1_2020,"116",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,APR_1_2020,"117",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,MAY_1_2020,"118",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,JUN_1_2020,"119",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,JUL_1_2020,"120",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,AUG_1_2020,"121",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,SEP_1_2020,"122",0,0,0,0,0,0,false));
        		
        List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999,MAR_7_1999),getPayment(10712D, AUG_7_1999,AUG_7_1999),getPayment(12956D, FEB_17_2000,FEB_17_2000),
        		getPayment(8034D,JUl_7_2000,JUl_7_2000));
        EstateAccount account = getAccount(0D);

        // Test
        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
        		DEFAULT_INTEREST_RATE,true);

        // Verify
        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
        assertEquals(31406.22, collection, 0.1);
        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
        assertEquals(5651.77, collectionGST, 0.1);
       // reconcileDemands(demands, collections);
        verifyRemainingBalance(account, 0D);
    }
  
  

    
    //Demo
    @Test

    public void testSimpleInterestSettlementStatement() throws ParseException {
    	 List<EstateDemand> demands = Arrays.asList( getDemand(1000D,180 ,FEB_1_1999,"102",0,0,0,0,0,0,false), 
                   getDemand(1000D, 180 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
                   getDemand(1000D, 180 ,APR_1_1999,"104",0,0,0,0,0,0,false));

List<EstatePayment> payments = Arrays.asList( getPayment(1200D, MAR_27_1999,MAR_27_1999),
					   getPayment(2500D, APR_15_1999,APR_15_1999)	
);
EstateAccount account = getAccount(0D);

        utils=new EstateRentCollectionUtils();
      List<EstateAccountStatement> accountStatementItems = this.estateRentCollectionService.getAccountStatement(demands,
              payments, DEFAULT_INTEREST_RATE, null,getEpochFromDateString(NOV_30_2000));
     // getEpochFromDateString(NOV_30_2000)
      utils.printStatement(accountStatementItems);
      utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
  }
    //Demo
    @Test

    public void testSimpleInterestSettlementStatement_lessAmount() throws ParseException {
    	  List<EstateDemand> demands = Arrays.asList( getDemand(2678D,482 ,FEB_1_1999,"102",0,0,false,FEB_1_1999), getDemand(2678D, 482 ,MAR_1_1999,"103",0,0,false,MAR_1_1999));
          List<EstatePayment> payments = Arrays.asList( getPayment(3756D, MAR_27_1999,MAR_27_1999));
          EstateAccount account = getAccount(0D);

          utils=new EstateRentCollectionUtils();
        List<EstateAccountStatement> accountStatementItems = this.estateRentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null,getEpochFromDateString(NOV_30_2000));
       // getEpochFromDateString(NOV_30_2000)
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }
   
    
    @Test

    public void accountStatementWithPrevious() throws ParseException {
    	 List<EstateDemand> demands = Arrays.asList( getDemand(2678D,482 ,FEB_1_1999,"102",0,0,false,FEB_1_1999), getDemand(2678D, 482 ,MAR_1_1999,"103",0,0,false,MAR_1_1999));
         List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999));

          utils=new EstateRentCollectionUtils();
        List<EstateAccountStatement> accountStatementItems = this.estateRentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null,getEpochFromDateString(NOV_30_2000));
       // getEpochFromDateString(NOV_30_2000)
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }
    
    @Test

    public void testSimpleInterestSettlementStatement_case2() throws ParseException {
    	
    	List<EstateDemand> demands = Arrays.asList( 
    			getDemand(2678D,482 ,FEB_1_1999,"102",0,0,0,0,0,0,false),
        		
        		getDemand(2678D, 482 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,APR_1_1999,"104",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,MAY_1_1999,"105",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,JUN_1_1999,"106",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,JUL_1_1999,"107",0,0,0,0,0,0,false),
        		getDemand(2678D, 482 ,AUG_1_1999,"108",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,SEP_1_1999,"110",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,OCT_1_1999,"111",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,NOV_1_1999,"112",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,DEC_1_1999,"113",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,JAN_1_2000,"114",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,FEB_1_2020,"115",0,0,0,0,0,0,false),
        		getDemand(2813D, 506 ,MAR_1_2020,"116",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,APR_1_2020,"117",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,MAY_1_2020,"118",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,JUN_1_2020,"119",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,JUL_1_2020,"120",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,AUG_1_2020,"121",0,0,0,0,0,0,false),
        		getDemand(3094D, 557 ,SEP_1_2020,"122",0,0,0,0,0,0,false));
        		
    	 List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999,MAR_7_1999),getPayment(10712D, AUG_7_1999,AUG_7_1999),getPayment(12956D, FEB_17_2000,FEB_17_2000),
         		getPayment(8034D,JUl_7_2000,JUl_7_2000));

          utils=new EstateRentCollectionUtils();
        List<EstateAccountStatement> accountStatementItems = this.estateRentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null);
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }
    
    
   
  	
	  @Test
    public void testAdditionalBalanceUsecase1Summary() throws ParseException {
		  List<EstateDemand> demands = Arrays.asList( getDemand(2678D,482 ,FEB_1_1999,"102",268,10,0,0,0,0), getDemand(2678D, 482 ,MAR_1_1999,"103",268,6,0,0,0,0));
          List<EstatePayment> payments = Arrays.asList( getPayment(5356D, MAR_7_1999));
        EstateAccount rentAccount = getAccount(40.03);
        EstateRentSummary rentSummary = this.estateRentCollectionService.calculateRentSummaryAt(demands, rentAccount,
                DEFAULT_INTEREST_RATE, getEpochFromDateString(JUN_1_2000));
        System.out.println(rentSummary);
        assertEquals(0D, rentSummary.getBalanceAmount(), 0.0001);


	  }
	  
	  


	  private EstatePayment getPayment(double amount, String date,String paymentDate) throws ParseException {
	        return EstatePayment.builder().rentReceived(amount).receiptNo("X-1212").receiptDate(getEpochFromDateString(date))
	        		.paymentDate(getEpochFromDateString(paymentDate))
	        		.build();
	    }
	    private EstatePayment getPayment(double amount, String date) throws ParseException {
	        return EstatePayment.builder().rentReceived(amount).receiptNo("X-1212").receiptDate(getEpochFromDateString(date))
	        		
	        		.build();
	    }
	    private EstateDemand getDemand(double amount, double gst, String date, String demandId,double penaltyInterest,double gstInterest) throws ParseException {
	        return EstateDemand.builder()
	        		.rent(amount)
	        		.gst(gst)
	        		.penaltyInterest(penaltyInterest)
	        		.gstInterest(gstInterest)
	        		.generationDate(getEpochFromDateString(date))
	        		.id(demandId)
	        		.remainingRent(amount)
	        		.remainingGST(new Double(gst))
	        		.remainingRentPenalty(penaltyInterest)
	        		.remainingGSTPenalty(gstInterest)
	               .build();
	    }

	    private EstateDemand getDemand(double amount, double gst, String date, String demandId,double penaltyInterest,double gstInterest,double collectedRent
	    		,double collectedGST,double collectedRentPenalty,double collectedGSTPenaty) throws ParseException {
	        return EstateDemand.builder()
	        		.rent(amount)
	        		.gst(gst)
	        		.penaltyInterest(penaltyInterest)
	        		.gstInterest(gstInterest)
	        		.generationDate(getEpochFromDateString(date))
	        		.id(demandId)
	        		.remainingRent(amount)
	        		.remainingGST(new Double(gst))
	        		.remainingRentPenalty(penaltyInterest)
	        		.remainingGSTPenalty(gstInterest)
	        		.collectedGST(collectedGST)
	        		.collectedGSTPenalty(collectedGSTPenaty)
	        		.collectedRentPenalty(collectedRentPenalty)
	        		.collectedRent(collectedRent)
	        		.interestSince(getEpochFromDateString(date))
	               .build();
	    }


	 

	    private EstateDemand getDemand(double amount, double gst, String date, String demandId,double penaltyInterest,double gstInterest,double collectedRent
	    		,double collectedGST,double collectedRentPenalty,double collectedGSTPenaty,boolean isPrevious) throws ParseException {
	        return EstateDemand.builder()
	        		.rent(amount)
	        		.gst(gst)
	        		.penaltyInterest(penaltyInterest)
	        		.gstInterest(gstInterest)
	        		.generationDate(getEpochFromDateString(date))
	        		.id(demandId)
	        		.remainingRent(amount)
	        		.remainingGST(new Double(gst))
	        		.remainingRentPenalty(penaltyInterest)
	        		.remainingGSTPenalty(gstInterest)
	        		.collectedGST(collectedGST)
	        		.collectedGSTPenalty(collectedGSTPenaty)
	        		.collectedRentPenalty(collectedRentPenalty)
	        		.collectedRent(collectedRent)
	        		.interestSince(getEpochFromDateString(date))
	        		.isPrevious(isPrevious)
	               .build();
	    }
	    private EstateDemand getDemand(double amount, double gst, String date, String demandId,double penaltyInterest,double gstInterest,boolean isPrevious,String interestSince) throws ParseException {
	        return EstateDemand.builder()
	        		.rent(amount)
	        		.gst(gst)
	        		.penaltyInterest(penaltyInterest)
	        		.gstInterest(gstInterest)
	        		.generationDate(getEpochFromDateString(date))
	        		.id(demandId)
	        		.remainingRent(amount)
	        		.remainingGST(new Double(gst))
	        		.remainingRentPenalty(penaltyInterest)
	        		.remainingGSTPenalty(gstInterest)
	        		.isPrevious(isPrevious)
	        		.interestSince(getEpochFromDateString(interestSince))
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
	    
	    @Test
	    public void testPenaltiesPartiallyPaid() throws ParseException {
	      	 List<EstateDemand> demands = Arrays.asList( getDemand(1000D,180 ,FEB_1_1999,"102",0,0,0,0,0,0,false), 
	      			                                     getDemand(1000D, 180 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
	      			                                     getDemand(1000D, 180 ,APR_1_1999,"104",0,0,0,0,0,0,false),
	      	 									      	 getDemand(1000D,180 ,MAY_1_1999,"105",0,0,0,0,0,0,false), 
											             getDemand(1000D, 180 ,JUN_1_1999,"106",0,0,0,0,0,0,false),
											             getDemand(1000D, 180 ,JUL_1_1999,"107",0,0,0,0,0,0,false),
											             getDemand(1000D, 180 ,AUG_1_1999,"108",0,0,0,0,0,0,false));
	      	 
	      	 		
	      	 
	         List<EstatePayment> payments = Arrays.asList( getPayment(1200D, MAR_27_1999,MAR_27_1999),
	        		 									   getPayment(2500D, APR_15_1999,APR_15_1999),
	        		 									  getPayment(10712D, AUG_19_1999,AUG_19_1999)
	        		 );

	        EstateAccount account = getAccount(0D);

	        // Test
	        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
	        		DEFAULT_INTEREST_RATE,true);

	        // Verify
	        
	        
	        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
	      //  assertEquals(5356.00, collection, 0.1);
	        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
	        //assertEquals(964, collectionGST, 0.1);
	       // reconcileDemands(demands, collections);
	        verifyRemainingBalance(account, 5318.44);
	        
	    }
	    
	    
	    @Test
	    public void testPenaltiesPartiallyPaid_cycle3() throws ParseException {
	      	 List<EstateDemand> demands = Arrays.asList( getDemand(1000D,180 ,FEB_1_1999,"102",0,0,0,0,0,0,false), 
	      			                                     getDemand(1000D, 180 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
	      			                                     getDemand(1000D, 180 ,APR_1_1999,"104",0,0,0,0,0,0,false));
	      	 
	         List<EstatePayment> payments = Arrays.asList( getPayment(1200D, MAR_27_1999,MAR_27_1999),
	        		 									   getPayment(2500D, APR_15_1999,APR_15_1999)	
	        		 );

	        EstateAccount account = getAccount(0D);

	        // Test
	        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
	        		DEFAULT_INTEREST_RATE,true);

	        // Verify
	        double rentDue=demands.stream().mapToDouble(EstateDemand::getRemainingRent).sum();
	        double gstDue=demands.stream().mapToDouble(EstateDemand::getRemainingGST).sum();
	        double rentPenaltyDue=demands.stream().mapToDouble(EstateDemand::getRemainingRentPenalty).sum();
	        double GSTPenaltyDue=demands.stream().mapToDouble(EstateDemand::getRemainingGSTPenalty).sum();
	       
	        
	        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
	        assertEquals(5356.00, collection, 0.1);
	        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
	        assertEquals(964, collectionGST, 0.1);
	       // reconcileDemands(demands, collections);
	        verifyRemainingBalance(account, 3760.1);
	    }
	    
@Test
	    public void testPenaltiesPartiallyPaid_previousDemand() throws ParseException {
	      	 List<EstateDemand> demands = Arrays.asList( 
	      			 									 getDemand(650D,100 ,FEB_1_1999,"101",100,3,0,0,0,0,true),
	      			 									 getDemand(1000D,180 ,FEB_1_1999,"102",0,0,0,0,0,0,false), 
	      			                                     getDemand(1000D, 180 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
	      			                                     getDemand(1000D, 180 ,APR_1_1999,"104",0,0,0,0,0,0,false));
	      	 
	         List<EstatePayment> payments = Arrays.asList( getPayment(1200D, MAR_27_1999,MAR_27_1999),
	        		 									   getPayment(2500D, APR_15_1999,APR_15_1999)	
	        		 );

	        EstateAccount account = getAccount(0D);

	        // Test
	        List<EstateRentCollection> collections = this.estateRentCollectionService.settle(demands, payments, account,
	        		DEFAULT_INTEREST_RATE,true);

	        // Verify
	        double rentDue=demands.stream().mapToDouble(EstateDemand::getRemainingRent).sum();
	        double gstDue=demands.stream().mapToDouble(EstateDemand::getRemainingGST).sum();
	        double rentPenaltyDue=demands.stream().mapToDouble(EstateDemand::getRemainingRentPenalty).sum();
	        double GSTPenaltyDue=demands.stream().mapToDouble(EstateDemand::getRemainingGSTPenalty).sum();
	       
	        
	        double collection = collections.stream().mapToDouble(EstateRentCollection::getRentCollected).sum();
	        assertEquals(5356.00, collection, 0.1);
	        double collectionGST = collections.stream().mapToDouble(EstateRentCollection::getGstCollected).sum();
	        assertEquals(964, collectionGST, 0.1);
	       // reconcileDemands(demands, collections);
	        verifyRemainingBalance(account, 3760.1);
	    }
	    

	    @Test

	    public void testSimpleInterestSettlementStatement_Latest() throws ParseException {
	    	
	    	 List<EstateDemand> demands = Arrays.asList( 
	    			 getDemand(650D,100 ,JAN_1_1999,"101",100,3,0,0,0,0,true),
	    			 getDemand(1000D,180 ,FEB_1_1999,"102",0,0,0,0,0,0,false), 
                       getDemand(1000D, 180 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
                       getDemand(1000D, 180 ,APR_1_1999,"104",0,0,0,0,0,0,false));

List<EstatePayment> payments = Arrays.asList( getPayment(1200D, MAR_27_1999,MAR_27_1999),
						   getPayment(2500D, APR_15_1999,APR_15_1999)	
);
	          utils=new EstateRentCollectionUtils();
	        List<EstateAccountStatement> accountStatementItems = this.estateRentCollectionService.getAccountStatement(demands,
	                payments, DEFAULT_INTEREST_RATE, null, null);
	        utils.printStatement(accountStatementItems);
	        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
	    }
 
    
    
@Test

public void testSimpleInterestSettlementStatement_Latest1() throws ParseException {
	
	List<EstateDemand> demands = Arrays.asList(
			getDemand(650D,100 ,FEB_1_1999,"101",100,3,0,0,0,0,true),
			getDemand(1000D,180 ,FEB_1_1999,"102",0,0,0,0,0,0,false), 
              getDemand(1000D, 180 ,MAR_1_1999,"103",0,0,0,0,0,0,false),
              getDemand(1000D, 180 ,APR_1_1999,"104",0,0,0,0,0,0,false),
		      	 getDemand(1000D,180 ,MAY_1_1999,"105",0,0,0,0,0,0,false), 
            getDemand(1000D, 180 ,JUN_1_1999,"106",0,0,0,0,0,0,false),
            getDemand(1000D, 180 ,JUL_1_1999,"107",0,0,0,0,0,0,false),
            getDemand(1000D, 180 ,AUG_1_1999,"108",0,0,0,0,0,0,false));



List<EstatePayment> payments = Arrays.asList( getPayment(1200D, MAR_27_1999,MAR_27_1999),
			   getPayment(2500D, APR_15_1999,APR_15_1999),
			  getPayment(10712D, AUG_19_1999,AUG_19_1999)
);

      utils=new EstateRentCollectionUtils();
    List<EstateAccountStatement> accountStatementItems = this.estateRentCollectionService.getAccountStatement(demands,
            payments, DEFAULT_INTEREST_RATE, null, null);
    utils.printStatement(accountStatementItems);
    utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
}

}

