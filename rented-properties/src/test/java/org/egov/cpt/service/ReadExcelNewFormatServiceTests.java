package org.egov.cpt.service;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.stream.Collectors;

import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.service.xlsxparsing.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReadExcelNewFormatServiceTests {
    IReadExcelService readExcelService;

    @Before
    public void setup() {
        // this.readExcelService = new ReadExcelService();
        this.readExcelService = new ReadExcelStreamingService();
    }

    private void _testExcelParsing(String excelFileToParse, int sheetNo, double expectedTotalRent,
            double expectedTotalPaid) {
        System.out.println("Parsing file: " + excelFileToParse + ", sheet no " + sheetNo);
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

    private static final String TEMPLATE = "calculations/Vikas Nagar Mauli Jagran (451 to 520).xlsx";

    @Test
    public void testSheet451() throws FileNotFoundException {
        _testExcelParsing(TEMPLATE, 0, 39684, 900);
    }

    @Test
    public void testSheet452() throws FileNotFoundException {
        _testExcelParsing(TEMPLATE, 1, 39684, 35100);
    }

    @Test
    public void testSheet453() throws FileNotFoundException {
        _testExcelParsing(TEMPLATE, 2, 39684, 4200);
    }

    @Test
    public void testSheet454() throws FileNotFoundException {
        _testExcelParsing(TEMPLATE, 3, 39684, 10500);
    }

    @Test
    public void testSheet455() throws FileNotFoundException {
        _testExcelParsing(TEMPLATE, 4, 39684, 17400);
    }

    @Test
    public void testSheet456() throws FileNotFoundException {
        _testExcelParsing(TEMPLATE, 5, 39196, 31800);
    }
}
