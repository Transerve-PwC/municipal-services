package org.egov.cpt.service;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.util.RentCollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExcelStatementTests {
    private static final String CALCULATIONS_501_TO_520_XLSX = "calculations/501 to 520.xlsx";
    private ReadExcelService readExcelService;
    private RentCollectionService rentCollectionService;
    private RentCollectionUtils utils;

    @Before
    public void setup() {
        this.readExcelService = new ReadExcelService();
        this.rentCollectionService = new RentCollectionService();
        this.utils = new RentCollectionUtils();
    }

    @SuppressWarnings("deprecation")
    private List<RentAccountStatement> _testSheetWithIndex(String excelFileToParse, int index) {
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, index);
        List<RentAccountStatement> accountStatementItems = this.rentCollectionService.getAccountStatement(
                rentDemandResponse.getDemand(), rentDemandResponse.getPayment(),
                RentCollectionServiceTests.DEFAULT_INTEREST_RATE, null, System.currentTimeMillis());
        utils.printStatement(accountStatementItems);
        utils.reconcileStatement(accountStatementItems, RentCollectionServiceTests.DEFAULT_INTEREST_RATE);
        return accountStatementItems;
    }

    @Test
    public void test501Statement() {
        List<RentAccountStatement> accountStatementItems = _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 0);
        assertEquals(206, accountStatementItems.size());
    }

    @Test
    public void test502Statement() {
        List<RentAccountStatement> accountStatementItems = _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 1);
        assertEquals(208, accountStatementItems.size());
    }

    @Test
    public void test503Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 2);
    }

    @Test
    public void test504Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 3);
    }

    @Test
    public void test505Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 4);
    }

    @Test
    public void test506Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 5);
    }

    @Test
    public void test507Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 6);
    }

    @Test
    public void test508Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 7);
    }

    @Test
    public void test509Statement() {
        _testSheetWithIndex(CALCULATIONS_501_TO_520_XLSX, 8);
    }
}