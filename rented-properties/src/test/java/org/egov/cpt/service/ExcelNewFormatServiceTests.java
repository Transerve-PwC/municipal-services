package org.egov.cpt.service;

import java.io.InputStream;

import org.egov.cpt.service.xlsxparsing.ReadExcelService;
import org.junit.Test;

public class ExcelNewFormatServiceTests {

    private ReadExcelService excelService = new ReadExcelService();

    @SuppressWarnings("deprecation")
    @Test
    public void testNewFormat() {
        String excelFileToParse = "calculations/New format excel Vikas nagar .xlsx";
        InputStream inputStream = ReadExcelServiceTests.class.getClassLoader().getResourceAsStream(excelFileToParse);
        this.excelService.getDatafromExcel(inputStream, 0);
    }
}
