package org.egov.cpt.service;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RentPayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("deprecation")
public class ReadExcelServiceTests {

    ReadExcelService readExcelService;

    @Before
    public void setup() {
        this.readExcelService = new ReadExcelService();
    }

    private void _testExcelParsing(String excelFileToParse, int sheetNo, double expectedTotalRent,
            double expectedTotalPaid) {
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, sheetNo);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(0, rentDemandResponse.getPayment().stream().map(RentPayment::getDateOfPayment)
                .filter(date -> date == null).count());
        assertEquals(expectedTotalRent, totalRent, 1.0);
        assertEquals(expectedTotalPaid, totalPaid, 1.0);
    }

    @Test
    public void testSimpleParsing() {
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 0, 135708.0, 123536.0);
    }

    @Test
    public void testSheet1() {
        /* Sheet no. 502 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 1, 135708.0, 123035.0);
    }

    @Test
    public void testSheet2() {
        /* Sheet no. 503 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 2, 135708.0, 76485.0);
    }

    @Test
    public void testSheet3() {
        /* Sheet no. 504 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 3, 135708.0, 117637.0);
    }

    @Test
    public void testSheet4() {
        /* Sheet no. 505 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 4, 135708.0, 123535.0);
    }

    @Test
    public void testSheet5() {
        /* Sheet no. 506 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 5, 135708.0, 98970.0);
    }

    @Test
    public void testSheet6() {
        /* Sheet no. 507 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 6, 135708.0, 123535.0);
    }

    @Test
    public void testSheet7() {
        /* Sheet no. 508 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 7, 135708.0, 95158.0);
    }

    @Test
    public void testSheet8() {
        /* Sheet no. 509 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 8, 135734.0, 90373.0);
    }

    @Test
    public void testSheet9() {
        /* Sheet no. 510 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 9, 135734.0, 111937.0);
    }

    @Test
    public void testSheet10() {
        /* Sheet no. 511 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 10, 135734.0, 114027.0);
    }

    @Test
    public void testSheet11() {
        /* Sheet no. 512 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 11, 135734.0, 116652.0);
    }

    @Test
    public void testSheet12() {
        /* Sheet no. 513 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 12, 135734.0, 125426.0);
    }

    @Test
    public void testSheet13() {
        /* Sheet no. 514 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 13, 135734.0, 123535.0);
    }

    @Test
    public void testSheet14() {
        /* Sheet no. 515 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 14, 135734.0, 123557.0);
    }

    @Test
    public void testSheet15() {
        /* Sheet no. 516 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 15, 135734.0, 111715.0);
    }

    @Test
    public void testSheet16() {
        /* Sheet no. 517 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 16, 135734.0, 123535.0);
    }

    @Test
    public void testSheet17() {
        /* Sheet no. 518 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 17, 135734.0, 126663.0);
    }

    @Test
    public void testSheet18() {
        /* Sheet no. 519 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 18, 135734.0, 142764.0);
    }

    @Test
    public void testSheet19() {
        /* Sheet no. 520 */
        String excelFileToParse = "calculations/501 to 520.xlsx";
        this._testExcelParsing(excelFileToParse, 19, 135734.0, 121612.0);
    }

    @Test
    public void testSheet521() {
        /* Sheet no. 521 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 0);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(20478.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet522() {
        /* Sheet no. 522 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 1);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(115709.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet523() {
        /* Sheet no. 523 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 2);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(45186.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet524() {
        /* Sheet no. 524 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 3);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(34892.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet525() {
        /* Sheet no. 525 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 4);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(123788.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet526() {
        /* Sheet no. 526 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 5);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(61728.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet527() {
        /* Sheet no. 527 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 6);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(35150.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet528() {
        /* Sheet no. 528 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 7);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(111937.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet529() {
        /* Sheet no. 529 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 8);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(97525.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet530() {
        /* Sheet no. 530 */
        String excelFileToParse = "calculations/521 to 530.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 9);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(105158.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet531() {
        /* Sheet no. 531 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 7);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(45478.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet532() {
        /* Sheet no. 532 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 8);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(95725.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet533() {
        /* Sheet no. 533 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 9);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(123535.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet534() {
        /* Sheet no. 534 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 10);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(123535.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet535() {
        /* Sheet no. 535 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 11);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(123535.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet536() {
        /* Sheet no. 536 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 12);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(105658.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet537() {
        /* Sheet no. 537 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 13);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(62775.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet538() {
        /* Sheet no. 538 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 14);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(62372.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet539() {
        /* Sheet no. 539 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 15);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(90380.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet540() {
        /* Sheet no. 540 */
        String excelFileToParse = "calculations/531 to 540.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 16);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(112838.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet541() {
        /* Sheet no. 541 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 0);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(91500.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet542() {
        /* Sheet no. 542 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 1);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(102782.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet543() {
        /* Sheet no. 543 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 2);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(62000.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet544() {
        /* Sheet no. 544 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 3);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(112924.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet545() {
        /* Sheet no. 545 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 4);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(81792.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet546() {
        /* Sheet no. 546 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 5);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(101546.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet547() {
        /* Sheet no. 547 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 6);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(112000.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet548() {
        /* Sheet no. 548 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 7);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(135708.0, totalRent, 1.0);
        assertEquals(111730.0, totalPaid, 1.0);
    }

    @Test
    public void testSheet549() {
        /* Sheet no. 549 */
        String excelFileToParse = "calculations/541 to 549.xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        RentDemandResponse rentDemandResponse = this.readExcelService.getDatafromExcel(inputStream, 8);
        Double totalRent = rentDemandResponse.getDemand().stream().map(RentDemand::getCollectionPrincipal)
                .collect(Collectors.summingDouble(Double::doubleValue));
        Double totalPaid = rentDemandResponse.getPayment().stream().map(RentPayment::getAmountPaid)
                .collect(Collectors.summingDouble(Double::doubleValue));
        assertEquals(124531.0, totalRent, 1.0);
        assertEquals(116852.0, totalPaid, 1.0);
    }

    @Test
    public void testSplitReceiptData() {
        final String str = "10/10";
        Pattern pattern = Pattern.compile("^\\d*");
        Matcher matcher = pattern.matcher(str);
        assertEquals(true, matcher.find());
        assertEquals("10", matcher.group());
    }

    @Test
    public void testExtractDate() throws ParseException {
        final String[] MONTHS = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT",
                "NOV", "DEC" };
        String str = "Aug.-20";
        Pattern monthPattern = Pattern.compile("^\\w*");
        Matcher monthMatcher = monthPattern.matcher(str);
        if (monthMatcher.find()) {
            String month = monthMatcher.group().toUpperCase();
            int monthIndex = Arrays.asList(MONTHS).indexOf(month);
            if (monthIndex < 0) {
                throw new DateTimeParseException("Cannot parse " + str + " as a date.", null, 0);
            }
            Pattern datePattern = Pattern.compile("\\d*$");
            Matcher dateMatcher = datePattern.matcher(str);
            if (dateMatcher.find()) {
                String twoYearDate = dateMatcher.group();
                int twoYearDateInt = Integer.parseInt(twoYearDate);
                if (twoYearDateInt >= 100) {
                    throw new DateTimeParseException("Cannot parse " + str + " as a date.", null, 0);
                }
                int year = twoYearDateInt < 50 ? 2000 + twoYearDateInt : 1900 + twoYearDateInt;
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthIndex, 1);
            }
        }
    }
}