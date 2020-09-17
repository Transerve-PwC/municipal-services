package org.egov.cpt.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RentPayment;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadExcelService {

	public RentDemandResponse getDatafromExcelPath(String filePath) {
		RentDemandResponse response = new RentDemandResponse();
		try {
			response = getDatafromExcel(new FileInputStream(new File(filePath)), 0);
		} catch (FileNotFoundException e) {
			log.error("File converting inputstream operation failed due to :" + e.getMessage());
		}
		return response;
	}

	private static final int CELL_DATE = 0;
	private static final int CELL_PRINCIPAL = 1;
	private static final int CELL_REALIZATION = 2;
	private static final int CELL_RECEIPT_NO = 8;

	private static final String HEADER_CELL = "Month";
	private static final String FOOTER_CELL = "Total";

	public Integer checkFormatOfexcel(InputStream inputStream, int sheetIndex) {
		Integer formatFlag = null;
		try {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();
				if("Month".equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					formatFlag =0;
					break;
				}else if("YEAR".equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					formatFlag =1;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("File reading operation fails due to :" + e.getMessage());
		}
		return formatFlag;
	}

	public RentDemandResponse getDatafromExcel(InputStream inputStream, int sheetIndex) {
		List<RentDemand> demands = new ArrayList<>();
		List<RentPayment> payments = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> rowIterator = sheet.iterator();
			boolean shouldParseRows = false;
			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();

				/**
				 * Fetching Data will Start after this header row. This will also skip
				 * intermediate header rows.
				 */
				if (HEADER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					shouldParseRows = true;
					continue;
				}

				/* Fetching Data will End after this */
				if (FOOTER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					break;
				}

				if (shouldParseRows) {
					RentDemand demand = new RentDemand();

					/**
					 * First cell as month year.
					 */
					Object generationDateCell = getValueFromCell(
							currentRow.getCell(CELL_DATE, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));

					if (generationDateCell instanceof String && !generationDateCell.toString().isEmpty()) {
						log.debug("Parsing first cell with value {} as date", generationDateCell);
						try {
							demand.setGenerationDate(extractDateFromString(generationDateCell.toString()));
						} catch (DateTimeParseException exception) {
							log.debug(exception.getLocalizedMessage());
							continue;
						}
					} else if (generationDateCell instanceof Long && !generationDateCell.toString().isEmpty()) {
						demand.setGenerationDate((Long) generationDateCell);
					} else {
						continue;
					}

					/**
					 * generated rent amount for the month.
					 */

					if (!generationDateCell.toString().isEmpty()) {
						demand.setCollectionPrincipal((Double) getValueFromCell(
								currentRow.getCell(CELL_PRINCIPAL, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));

						/**
						 * collected payment amount for the month.
						 */
						if (currentRow.getCell(CELL_REALIZATION, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) != null) {
							RentPayment payment = RentPayment.builder()
									.amountPaid(Double.parseDouble(String.valueOf(getValueFromCell(currentRow
											.getCell(CELL_REALIZATION, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)))))
									.build();

							/**
							 * parse last cell data for receipt no and receipt date.
							 */
							String lastCellData = String.valueOf(getValueFromCell(
									currentRow.getCell(CELL_RECEIPT_NO, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)));
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
							payments.add(payment);
						}
						demand.setRemainingPrincipal(demand.getCollectionPrincipal());
						demand.setInterestSince(demand.getGenerationDate());
						demands.add(demand);

					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("File reading operation fails due to :" + e.getMessage());
		}
		return new RentDemandResponse(demands, payments);
	}

	private int extractFirstNumericPart(String str) throws NumberFormatException {
		Pattern pattern = Pattern.compile("^\\d*");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group());
		}
		throw new NumberFormatException("Could not exract numeric part from " + str);
	}

	private static final String[] MONTHS = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP",
			"OCT", "NOV", "DEC" };

	/**
	 * Parse values like Aug.-20 Sep. 20
	 * 
	 * @param str
	 * @return
	 * @throws DateTimeParseException
	 */
	private Long extractDateFromString(String str) throws DateTimeParseException {
		Pattern monthPattern = Pattern.compile("^\\w*");
		Matcher monthMatcher = monthPattern.matcher(str);
		if (monthMatcher.find()) {
			String month = monthMatcher.group().toUpperCase();
			int monthIndex = Arrays.asList(MONTHS).indexOf(month.substring(0, 3));
			if (monthIndex < 0) {
				throw new DateTimeParseException("Cannot parse " + str + " as a date.", "", 0);
			}
			Pattern datePattern = Pattern.compile("\\d*$");
			Matcher dateMatcher = datePattern.matcher(str);
			if (dateMatcher.find()) {
				String twoYearDate = dateMatcher.group();
				int twoYearDateInt = Integer.parseInt(twoYearDate);
				if (twoYearDateInt >= 100) {
					throw new DateTimeParseException("Cannot parse " + str + " as a date.", "", 0);
				}
				int year = twoYearDateInt < 50 ? 2000 + twoYearDateInt : 1900 + twoYearDateInt;
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthIndex, 1, 12, 0);
				return calendar.getTimeInMillis();
			}
		}
		throw new DateTimeParseException("Cannot parse " + str + " as a date.", "", 0);
	}

	private Object getValueFromCell(Cell cell1) {
		Object objValue = "";
		switch (cell1.getCellType()) {
		case BLANK:
			objValue = "";
			break;
		case STRING:
			objValue = cell1.getRichStringCellValue().getString();
			break;
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell1)) {
				objValue = cell1.getDateCellValue().getTime();
			} else {
				objValue = cell1.getNumericCellValue();
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
}
