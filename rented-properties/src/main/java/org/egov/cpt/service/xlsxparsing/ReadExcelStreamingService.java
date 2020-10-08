package org.egov.cpt.service.xlsxparsing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.apache.poi.xssf.model.StylesTable;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.service.xlsxparsing.StreamingSheetContentsHandler.StreamingRowProcessor;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadExcelStreamingService extends AbstractExcelService implements IReadExcelService {

    @Override
    public RentDemandResponse getDatafromExcel(InputStream is, int sheetIndex) {
        try {
            OPCPackage opcPackage = OPCPackage.open(is);
            return this.process(opcPackage, sheetIndex);
        } catch (IOException | OpenXML4JException | SAXException e) {
            log.error("Error while parsing Excel", e);
            throw new CustomException("PARSE_ERROR", "Could not parse excel. Error is " + e.getMessage());
        }
    }

    @Override
    public RentDemandResponse getDatafromExcel(File file, int sheetIndex) {
        try {
            OPCPackage opcPackage = OPCPackage.open(file);
            return this.process(opcPackage, sheetIndex);
        } catch (IOException | OpenXML4JException | SAXException e) {
            log.error("Error while parsing Excel", e);
            throw new CustomException("PARSE_ERROR", "Could not parse excel. Error is " + e.getMessage());
        }
    }

    /**
     * Parses and shows the content of one sheet using the specified styles and
     * shared-strings tables.
     *
     * @param styles           The table of styles that may be referenced by cells
     *                         in the sheet
     * @param strings          The table of strings that may be referenced by cells
     *                         in the sheet
     * @param sheetInputStream The stream to read the sheet-data from.
     * 
     * @exception java.io.IOException An IO exception from the parser, possibly from
     *                                a byte stream or character stream supplied by
     *                                the application.
     * @throws SAXException if parsing the XML data fails.
     */
    private void processSheet(Styles styles, SharedStrings strings, SheetContentsHandler sheetHandler,
            InputStream sheetInputStream) throws IOException, SAXException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setNamespaceAware(false);
            SAXParser saxParser = saxFactory.newSAXParser();
            XMLReader sheetParser = saxParser.getXMLReader();
            ContentHandler handler = new MyXSSFSheetXMLHandler(styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
        }
    }

    private RentDemandResponse process(OPCPackage xlsxPackage, int sheetNo)
            throws IOException, OpenXML4JException, SAXException, CustomException {
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage);
        XSSFReader xssfReader = new XSSFReader(xlsxPackage);
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        int index = 0;
        while (iter.hasNext()) {
            try (InputStream stream = iter.next()) {
                // String sheetName = iter.getSheetName();
                if (index == sheetNo) {
                    SheetContentsProcessor processor = new SheetContentsProcessor();
                    processSheet(styles, strings, new StreamingSheetContentsHandler(processor), stream);
                    return new RentDemandResponse(processor.demands, processor.payments);
                }
                index++;
            }
        }
        throw new CustomException("PARSE_ERROR", "Could not process sheet no " + sheetNo);
    }

    private class SheetContentsProcessor implements StreamingRowProcessor {

        private static final int PARSING_WAITING_DEMANDS = 0;
        private static final int PARSING_DEMANDS = 1;
        private static final int PARSING_WAITING_PAYMENTS = 2;
        private static final int PARSING_PAYMENTS = 3;
        private static final int PARSING_DONE = 4;
        List<RentDemand> demands = new ArrayList<RentDemand>();
        List<RentPayment> payments = new ArrayList<RentPayment>();
        boolean isFormatIdentified = false;
        boolean isFormat2 = true;

        boolean isParsingFormat1 = false;

        int format2ParsingState = 0;
        int format2RentCell = -1;
        Map<String, Double> format2rentYearDetails = new HashMap<String, Double>();
        List<String> format2RentDurations = new ArrayList<>();

        @Override
        public void processRow(Row currentRow) {
            if (isFormatIdentified) {
                if (isFormat2) {
                    processFormat2(currentRow);
                } else {
                    processFormat1(currentRow);
                }
                return;
            }
            String firstCell = String
                    .valueOf(getValueFromCell(currentRow, 0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
            if (firstCell.isEmpty()) {
                return;
            }
            if ("Transit Site No.".equalsIgnoreCase(firstCell)) {
                isFormatIdentified = true;
                isFormat2 = true;
                return;
            }
            if (HEADER_CELL.equalsIgnoreCase(firstCell)) {
                isFormatIdentified = true;
                isFormat2 = false;
                this.isParsingFormat1 = true;
                return;
            }
        }

        private void processFormat2(Row currentRow) {
            if (format2ParsingState == PARSING_WAITING_DEMANDS) {
                for (int i = 0; i < currentRow.getLastCellNum(); i++) {
                    if (RENT_CELL.equalsIgnoreCase(
                            String.valueOf(getValueFromCell(currentRow, i, MissingCellPolicy.CREATE_NULL_AS_BLANK)))) {
                        format2RentCell = i;
                        format2ParsingState = PARSING_DEMANDS;
                        return;
                    }
                }
                return;
            }
            if (format2ParsingState == PARSING_DEMANDS) {
                String rentCellValue = String
                        .valueOf(getValueFromCell(currentRow, format2RentCell, MissingCellPolicy.CREATE_NULL_AS_BLANK))
                        .trim();
                if ("TOTAL".equalsIgnoreCase(rentCellValue)) {
                    format2ParsingState = PARSING_WAITING_PAYMENTS;
                    return;
                }
                format2rentYearDetails.put(rentCellValue, Double.valueOf(String.valueOf(
                        getValueFromCell(currentRow, format2RentCell + 2, MissingCellPolicy.CREATE_NULL_AS_BLANK))));
                return;
            }
            if (format2ParsingState == PARSING_WAITING_PAYMENTS) {
                String firstCellValue = String
                        .valueOf(getValueFromCell(currentRow, 0, MissingCellPolicy.CREATE_NULL_AS_BLANK)).trim();
                if (HEADER_CELL_FORMAT2.equalsIgnoreCase(firstCellValue)) {
                    format2ParsingState = PARSING_PAYMENTS;
                    format2rentYearDetails.forEach((key, value) -> {
                        List<String> rentDuration = getAllSequenceOfYears(key);
                        format2RentDurations.addAll(rentDuration);
                        rentDuration.forEach(rent -> {
                            demands.add(RentDemand.builder().generationDate(convertStrDatetoLong(rent))
                                    .interestSince(convertStrDatetoLong(rent)).collectionPrincipal(value)
                                    .remainingPrincipal(value).build());
                        });
                    });
                }
                return;
            }
            if (format2ParsingState == PARSING_PAYMENTS) {
                if (FOOTER_CELL_FORMAT2.equalsIgnoreCase(
                        String.valueOf(getValueFromCell(currentRow, 0, MissingCellPolicy.CREATE_NULL_AS_BLANK)))) {
                    format2ParsingState = PARSING_DONE;
                    return;
                }
                parseFormat2Payments(currentRow, format2RentDurations, payments);
            }
        }

        private void processFormat1(Row currentRow) {
            String firstCell = String
                    .valueOf(getValueFromCell(currentRow, 0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if (!this.isParsingFormat1) {
                if (HEADER_CELL.equalsIgnoreCase(firstCell)) {
                    this.isParsingFormat1 = true;
                }
                return;
            }
            if (FOOTER_CELL.equalsIgnoreCase(firstCell)) {
                this.isParsingFormat1 = false;
                return;
            }
            if (HEADER_CELL.equalsIgnoreCase(firstCell)) {
                return;
            }
            RentDemandPayment demandPayment = getDemandAndPaymentFromRow(currentRow);
            if (demandPayment == null) {
                return;
            }
            demands.add(demandPayment.demand);
            if (demandPayment.payment != null) {
                payments.add(demandPayment.payment);
            }
        }
    }
}
