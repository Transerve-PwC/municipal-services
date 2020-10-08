package org.egov.cpt.service.xlsxparsing;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExcelService {

    protected static final int CELL_DATE = 0;
    protected static final int CELL_PRINCIPAL = 1;
    protected static final int CELL_REALIZATION = 2;
    protected static final int CELL_RECEIPT_NO = 8;

    protected static final String HEADER_CELL = "Month";
    protected static final String FOOTER_CELL = "Total";
    protected static final String RENT_CELL = "RENT";
    protected static final String HEADER_CELL_FORMAT2 = "YEAR";
    protected static final String FOOTER_CELL_FORMAT2 = "G.TOTAL";
    protected static final String[] MONTHS = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG",
            "SEP", "OCT", "NOV", "DEC" };

    public class RentDemandPayment {
        RentDemand demand;
        RentPayment payment;

        RentDemandPayment(RentDemand demand, RentPayment payment) {
            this.demand = demand;
            this.payment = payment;
        }
    }

    protected RentDemandPayment getDemandAndPaymentFromRow(Row currentRow) {
        RentDemand demand = new RentDemand();
        RentPayment payment = null;
        /**
         * First cell as month year.
         */
        Object generationDateCell = getValueFromCell(currentRow, CELL_DATE, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        if (generationDateCell instanceof String && !generationDateCell.toString().isEmpty()) {
            // log.debug("Parsing first cell with value {} as date", generationDateCell);
            try {
                demand.setGenerationDate(extractDateFromString(generationDateCell.toString()));
            } catch (DateTimeParseException exception) {
                log.debug(exception.getLocalizedMessage());
                return null;
            }
        } else if (generationDateCell instanceof Long && !generationDateCell.toString().isEmpty()) {
            demand.setGenerationDate((Long) generationDateCell);
        } else {
            return null;
        }

        /**
         * generated rent amount for the month.
         */

        if (!generationDateCell.toString().isEmpty()) {
            demand.setCollectionPrincipal(
                    (Double) getValueFromCell(currentRow, CELL_PRINCIPAL, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));

            /**
             * collected payment amount for the month.
             */
            if (currentRow.getCell(CELL_REALIZATION, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null) {
                payment = RentPayment.builder().amountPaid(Double.parseDouble(String.valueOf(
                        getValueFromCell(currentRow, CELL_REALIZATION, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))))
                        .build();

                /**
                 * parse last cell data for receipt no and receipt date.
                 */
                String lastCellData = String.valueOf(
                        getValueFromCell(currentRow, CELL_RECEIPT_NO, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
                String[] components = lastCellData.split("\\s+");
                if (components.length == 0 || components[0].trim().length() == 0) {
                    payment.setReceiptNo("");
                    payment.setDateOfPayment(demand.getGenerationDate());
                } else if (components.length == 1) {
                    payment.setReceiptNo(components[0]);
                    payment.setDateOfPayment(demand.getGenerationDate());
                } else if (components.length > 1) {
                    try {
                        payment.setReceiptNo(components[0]);
                        int date = this.extractFirstNumericPart(components[1]);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(demand.getGenerationDate());
                        calendar.set(Calendar.DATE, date);
                        payment.setDateOfPayment(calendar.getTimeInMillis());
                    } catch (Exception exception) {
                        log.debug(exception.getLocalizedMessage());
                    }
                }
            }
            demand.setRemainingPrincipal(demand.getCollectionPrincipal());
            demand.setInterestSince(demand.getGenerationDate());

        }
        // if (payment != null) {
        // java.util.Date d = new java.util.Date(demand.getGenerationDate());
        // System.out.println(
        // "++ " + d.getMonth() + " " + demand.getCollectionPrincipal() + " " +
        // payment.getAmountPaid());
        // }
        return new RentDemandPayment(demand, payment);
    }

    /**
     * Parse values like Aug.-20 Sep. 20
     * 
     * @param str
     * @return
     * @throws DateTimeParseException
     */
    protected Long extractDateFromString(String str) throws DateTimeParseException {
        Pattern monthPattern = Pattern.compile("^\\w*");
        Matcher monthMatcher = monthPattern.matcher(str);
        if (monthMatcher.find()) {
            String month = monthMatcher.group().toUpperCase();
            int monthIndex = Arrays.asList(MONTHS).indexOf(month.substring(0, 3));
            if (monthIndex < 0) {
                throw new DateTimeParseException("Cannot parse ''" + str + "'' as a date.", "", 0);
            }
            Pattern datePattern = Pattern.compile("\\d*$");
            Matcher dateMatcher = datePattern.matcher(str);
            if (dateMatcher.find()) {
                String twoYearDate = dateMatcher.group();
                int twoYearDateInt = Integer.parseInt(twoYearDate);
                if (twoYearDateInt >= 100) {
                    throw new DateTimeParseException("Cannot parse ''" + str + "'' as a date.", "", 0);
                }
                int year = twoYearDateInt < 50 ? 2000 + twoYearDateInt : 1900 + twoYearDateInt;
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthIndex, 1, 12, 0);
                return calendar.getTimeInMillis();
            }
        }
        throw new DateTimeParseException("Cannot parse ''" + str + "'' as a date.", "", 0);
    }

    protected Object getValueFromCell(Row row, int cellNo, Row.MissingCellPolicy cellPolicy) {
        Cell cell1 = row.getCell(cellNo, cellPolicy);
        Object objValue = "";
        switch (cell1.getCellType()) {
            case BLANK:
                objValue = "";
                break;
            case STRING:
                objValue = cell1.getStringCellValue();
                break;
            case NUMERIC:
                try {
                    if (DateUtil.isCellDateFormatted(cell1)) {
                        objValue = cell1.getDateCellValue().getTime();
                    } else {
                        throw new InvalidFormatException();
                    }
                } catch (Exception ex1) {
                    try {
                        objValue = cell1.getNumericCellValue();
                    } catch (Exception ex2) {
                        objValue = 0.0;
                    }
                }

                break;
            case FORMULA:
                objValue = cell1.getNumericCellValue();
                break;

            default:
                objValue = "";
        }
        return objValue;
    }

    private int extractFirstNumericPart(String str) throws NumberFormatException {
        Pattern pattern = Pattern.compile("^\\d*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        throw new NumberFormatException("Could not exract numeric part from " + str);
    }

    /**
     * Format 2 related stuff
     */
    protected List<String> getAllSequenceOfYears(String rentYears) {
        String[] yearsCombo = rentYears.split("-");
        int startMonthnumber = Arrays.asList(MONTHS).indexOf(yearsCombo[0].split("'")[0].trim());
        int endMonthnumber = Arrays.asList(MONTHS).indexOf(yearsCombo[1].split("'")[0].trim());
        int startYear = Integer.parseInt(yearsCombo[0].split("'")[1]);
        int endYear = Integer.parseInt(yearsCombo[1].split("'")[1]);
        int yearCounter = startYear;
        boolean startMaking = false;
        List<String> rentDuration = new ArrayList<>();
        while (yearCounter <= endYear) {
            for (String month : MONTHS) {
                if ((MONTHS[startMonthnumber] + "-" + yearCounter).equalsIgnoreCase(month + "-" + startYear)) {
                    startMaking = true;
                }
                if (startMaking) {
                    rentDuration.add(1 + "-" + month + "-" + yearCounter);
                }
                if ((MONTHS[endMonthnumber] + "-" + yearCounter).equalsIgnoreCase(month + "-" + endYear)) {
                    break;
                }
            }
            yearCounter++;
        }
        return rentDuration;
    }

    protected long convertStrDatetoLong(String dateStr) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
            Date d = f.parse(dateStr);
            return d.getTime();
        } catch (Exception e) {
            log.error("Date parsing issue occur :" + e.getMessage());
        }
        return 0;
    }

    protected void parseFormat2Payments(Row currentRow, List<String> rentDurations, List<RentPayment> payments) {
        Object value = getValueFromCell(currentRow, 0, MissingCellPolicy.RETURN_NULL_AND_BLANK);
        if (!(value instanceof Double)) {
            return;
        }
        Integer currentRowYear = ((Double) value).intValue();
        for (int i = 1; i <= MONTHS.length; i++) {
            if (rentDurations.contains(1 + "-" + MONTHS[i - 1] + "-" + currentRowYear)) {
                if (!String.valueOf(getValueFromCell(currentRow, i, MissingCellPolicy.RETURN_NULL_AND_BLANK))
                        .isEmpty()) {
                    payments.add(RentPayment.builder()
                            .amountPaid(
                                    (Double) getValueFromCell(currentRow, i, MissingCellPolicy.RETURN_NULL_AND_BLANK))
                            .dateOfPayment(convertStrDatetoLong(1 + "-" + MONTHS[i - 1] + "-" + currentRowYear))
                            .build());
                }
            }

        }
    }
}
