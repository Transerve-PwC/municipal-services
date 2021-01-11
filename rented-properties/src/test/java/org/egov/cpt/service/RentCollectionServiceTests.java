package org.egov.cpt.service;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.models.RentCollection;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.models.RentSummary;
import org.egov.cpt.util.RentCollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@RunWith(JUnit4.class)
@SuppressWarnings("unused")
public class RentCollectionServiceTests {
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

    public static final double DEFAULT_INTEREST_RATE = 24D;
    private static final double ZERO_INTEREST_RATE = 0D;

    RentCollectionService rentCollectionService;
    RentCollectionUtils utils;

    @Before
    public void setup() {
        this.rentCollectionService = new RentCollectionService();
        this.utils = new RentCollectionUtils();
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
        List<RentDemand> demands = Collections.emptyList();
        List<RentPayment> payments = Arrays.asList(getPayment(100D, JAN_1_2020), getPayment(200D, JAN_15_2020));
        RentAccount account = getAccount(0D);

        // Test
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,new Long(0));

        // Verify
        reconcileDemands(demands, collections);
        verifyCollectedAmount(collections, 0D);
        verifyRemainingBalance(account, 300D);
    }

    @Test
    public void testSimpleSettlementWithDemands() throws ParseException {
        // Setup
        List<RentDemand> demands = Arrays.asList(getDemand(400, JAN_1_2020));
        List<RentPayment> payments = Arrays.asList(getPayment(100D, JAN_1_2020), getPayment(200D, JAN_15_2020));
        RentAccount account = getAccount(50D);

        // Test
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,new Long(0));

        // Verify
        reconcileDemands(demands, collections);
        verifyCollectedAmount(collections, 350D);
        verifyRemainingBalance(account, 0D);
    }

    @Test
    public void testSimpleSettlementWithDemands2() throws ParseException {
        // Setup
        List<RentDemand> demands = Arrays.asList(getDemand(200, JAN_1_2020), getDemand(200, FEB_1_2020));
        List<RentPayment> payments = Arrays.asList(getPayment(100D, JAN_1_2020), getPayment(200D, JAN_15_2020));
        RentAccount account = getAccount(50D);

        // Test
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,new Long(0));

        // Verify
        reconcileDemands(demands, collections);
        verifyCollectedAmount(collections, 350D);
        verifyRemainingBalance(account, 0D);
    }

    @Test
    public void testNoPaymentsSettlement() throws ParseException {
        // Setup
        List<RentDemand> demands = Arrays.asList(getDemand(200, JAN_1_2020), getDemand(200, JAN_15_2020));
        List<RentPayment> payments = Collections.emptyList();
        RentAccount account = getAccount(350D);

        // Test
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,new Long(0));

        // Verify
        reconcileDemands(demands, collections);
        verifyCollectedAmount(collections, 350D);
        verifyRemainingBalance(account, 0D);
    }

    @Test
    public void testNoPaymentsSettlement2() throws ParseException {
        // Setup
        List<RentDemand> demands = Arrays.asList(getDemand(200, JAN_1_2020), getDemand(200, JAN_15_2020));
        List<RentPayment> payments = Collections.emptyList();
        RentAccount account = getAccount(550D);

        // Test
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                ZERO_INTEREST_RATE,new Long(0));

        // Verify
        reconcileDemands(demands, collections);
        verifyCollectedAmount(collections, 400D);
        verifyRemainingBalance(account, 150D);
    }

    public void testExcessPaymentSettlement() throws ParseException {
        // Setup
        List<RentDemand> demands = Arrays.asList(getDemand(200, JAN_1_2020), getDemand(200, FEB_1_2020),
                getDemand(200, FEB_15_2020));
        List<RentPayment> payments = Arrays.asList(getPayment(400D, JAN_1_2020), getPayment(200D, JAN_15_2020));
    }

    @Test
    public void testSimpleInterestSettlement() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1998), getDemand(100, JAN_1_1999),
                getDemand(100, FEB_1_1999));
        List<RentPayment> payments = Arrays.asList(getPayment(200, FEB_16_1999));
        RentAccount account = getAccount(0);
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                DEFAULT_INTEREST_RATE,new Long(0));
        RentSummary summary = this.rentCollectionService.calculateRentSummaryAt(demands, account, DEFAULT_INTEREST_RATE,
                getEpochFromDateString(FEB_16_1999),new Long(0));
        // this.rentCollectionService.accountStatement(demands, payments, collections,
        // null, null);
        assertEquals(0, summary.getBalanceAmount(), 0.1);
    }

    @Test
    public void testSimpleInterestSettlementStatement() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1998), getDemand(100, JAN_1_1999),
                getDemand(100, FEB_1_1999));
        List<RentPayment> payments = Arrays.asList(getPayment(200, FEB_16_1999));
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testUsecase2Settlement() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(500, JAN_1_2000));
        List<RentPayment> payments = Arrays.asList(getPayment(400, MAY_1_2000));
        RentAccount rentAccount = getAccount(0);
        this.rentCollectionService.settle(demands, payments, rentAccount, DEFAULT_INTEREST_RATE,new Long(0));
        RentSummary summary = this.rentCollectionService.calculateRentSummary(demands, rentAccount,
                DEFAULT_INTEREST_RATE,new Long(0));
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testSettlementStatement() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1998), getDemand(100, JAN_1_1999),
                getDemand(100, MAR_1_1999), getDemand(100, APR_1_1999));
        List<RentPayment> payments = Arrays.asList(getPayment(400, FEB_16_1999), getPayment(20, APR_1_1999));
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testUsecase1() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(10000, DEC_1_1998));
        List<RentPayment> payments = Arrays.asList(getPayment(200, FEB_16_1999), getPayment(200, APR_1_1999));
        RentAccount account = getAccount(0);
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                DEFAULT_INTEREST_RATE,new Long(0));
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testEmptyItemsStatement() throws ParseException {
        List<RentDemand> demands = Arrays.asList();
        List<RentPayment> payments = Arrays.asList();
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        assertEquals(accountStatementItems.size(), 1);
    }

    @Test
    public void testSingleDebitSummary() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1998));
        RentAccount rentAccount = getAccount(0);
        RentSummary summary = this.rentCollectionService.calculateRentSummaryAt(demands, rentAccount,
                DEFAULT_INTEREST_RATE, getEpochFromDateString(DEC_1_1998),new Long(0));
        System.out.println(summary);
    }

    @Test
    public void testTwoDebitSummary() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1998), getDemand(100, JAN_1_1999));
        RentAccount rentAccount = getAccount(0);
        RentSummary summary = this.rentCollectionService.calculateRentSummaryAt(demands, rentAccount,
                DEFAULT_INTEREST_RATE, getEpochFromDateString(JAN_1_1999),new Long(0));
        System.out.println(summary);
    }

    @Test
    public void testInterestSettlement() throws ParseException {
        List<RentDemand> demands = getDemands(500, JAN_1_2020, 10);
        List<RentPayment> payments = getPayments(500, MAY_1_2020, 10);
        RentAccount account = getAccount(0);
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, account,
                DEFAULT_INTEREST_RATE,new Long(0));
        assertEquals(29, collections.size());
        RentSummary summary = this.rentCollectionService.calculateRentSummary(demands, account, DEFAULT_INTEREST_RATE,new Long(0));
        assertEquals(0, summary.getBalanceAmount(), 0.1);
    }

    @Test
    public void testContinuousDemands() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1998), getDemand(100, JAN_1_1999),
                getDemand(100, FEB_1_1999), getDemand(100, MAR_1_1999), getDemand(100, APR_1_1999),
                getDemand(100, MAY_1_1999), getDemand(100, JUN_1_1999), getDemand(100, JUL_1_1999),
                getDemand(100, AUG_1_1999), getDemand(100, SEP_1_1999), getDemand(100, OCT_1_1999),
                getDemand(100, NOV_1_1999), getDemand(100, DEC_1_1999));
        List<RentPayment> payments = Collections.emptyList();
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        utils.printStatement(accountStatementItems);
    }

    @Test
    public void testFutureDemands() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_2020));
        List<RentPayment> payments = Collections.emptyList();
        RentAccount rentAccount = getAccount(200);
        List<RentCollection> collections = this.rentCollectionService.settle(demands, payments, rentAccount,
                DEFAULT_INTEREST_RATE,new Long(0));
        assertEquals(1, collections.size());
    }

    @Test
    public void testStatementToDate() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1999));
        List<RentPayment> payments = Arrays.asList(getPayment(400, MAY_1_2020));
        RentAccount rentAccount = getAccount(200);
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, null,new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testStatementToDate1() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(100, DEC_1_1999));
        List<RentPayment> payments = Arrays.asList(getPayment(400, MAY_1_2020));
        RentAccount rentAccount = getAccount(200);
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, DEFAULT_INTEREST_RATE, null, getEpochFromDateString(APR_1_2020),new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testAdditionalBalanceUseCase1() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(600, DEC_1_1999), getDemand(1000, DEC_1_1999),
                getDemand(1000, JUN_1_2000));
        List<RentPayment> payments = Arrays.asList(getPayment(100, MAY_1_2000));
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, RentCollectionServiceTests.DEFAULT_INTEREST_RATE, null, getEpochFromDateString(JUN_1_2000),new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, RentCollectionServiceTests.DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testAdditionalBalanceUseCase2() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(1000, DEC_1_1999));
        List<RentPayment> payments = Arrays.asList(getPayment(50, MAY_1_2000));
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(demands,
                payments, RentCollectionServiceTests.DEFAULT_INTEREST_RATE, null, getEpochFromDateString(JUN_1_2000),new Long(0));
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, RentCollectionServiceTests.DEFAULT_INTEREST_RATE);
    }

    @Test
    public void testAdditionalBalanceUsecase1Summary() throws ParseException {
        List<RentDemand> demands = Arrays.asList(getDemand(600, DEC_1_1999), getDemand(1000, DEC_1_1999));
        demands.get(0).setInterestSince(getEpochFromDateString(MAY_1_2000));
        RentAccount rentAccount = getAccount(40.03);
        RentSummary rentSummary = this.rentCollectionService.calculateRentSummaryAt(demands, rentAccount,
                RentCollectionServiceTests.DEFAULT_INTEREST_RATE, getEpochFromDateString(JUN_1_2000),new Long(0));
        assertEquals(0D, rentSummary.getBalanceAmount(), 0.0001);
    }

    private long getEpochFromDateString(String date) throws ParseException {
        return this.getDateFromString(date).getTime();
    }

    private static final String DATE_FORMAT = "dd MM yyyy";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    private Date getDateFromString(String date) throws ParseException {
        return dateFormatter.parse(date);
    }

    private List<RentPayment> getPayments(double amount, String date, int count) throws ParseException {
        return Collections.nCopies(count, 0).stream().map(ele -> {
            try {
                return getPayment(amount, date);
            } catch (ParseException e) {
            	log.error("Error while parsing date ",e);
                return null;
            }
        }).collect(Collectors.toList());// ;);
    }

    private List<RentDemand> getDemands(double amount, String date, int count) throws ParseException {
        return IntStream.range(0, count).mapToObj(index -> {
            try {
                return getDemand(amount, date, "Demand-" + index);
            } catch (ParseException e) {
            	log.error("Error while parsing date ",e);
                return null;
            }
        }).collect(Collectors.toList());
    }

    private RentPayment getPayment(double amount, String date) throws ParseException {
        return RentPayment.builder().amountPaid(amount).receiptNo("X-1212").dateOfPayment(getEpochFromDateString(date)).build();
    }

    private RentDemand getDemand(double amount, String date) throws ParseException {
        return this.getDemand(amount, date, "");
    }

    private RentDemand getDemand(double amount, String date, String demandId) throws ParseException {
        return RentDemand.builder().collectionPrincipal(amount).remainingPrincipal(amount).id(demandId)
                .generationDate(getEpochFromDateString(date)).interestSince(getEpochFromDateString(date)).build();
    }

    private RentAccount getAccount(double initialBalance) {
        return RentAccount.builder().remainingAmount(initialBalance).build();
    }

    private void reconcileDemands(List<RentDemand> demands, List<RentCollection> collections) {
        double collectionAccordingToDemands = demands.stream()
                .mapToDouble(demand -> demand.getCollectionPrincipal() - demand.getRemainingPrincipal()).sum();
        double collection = collections.stream().mapToDouble(RentCollection::getPrincipalCollected).sum();
        assertEquals(collectionAccordingToDemands, collection, 0.1);
    }

    private void verifyCollectedAmount(List<RentCollection> collections, double expectedAmount) {
        double totalCollected = collections.stream().map(RentCollection::getPrincipalCollected)
                .mapToDouble(Double::valueOf).sum();
        assertEquals(expectedAmount, totalCollected, 0.1);
    }

    private void verifyRemainingBalance(RentAccount account, double expectedBalance) {
        assertEquals(expectedBalance, account.getRemainingAmount(), 0.1);
    }
}