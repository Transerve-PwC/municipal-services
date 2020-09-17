package org.egov.cpt.service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class ReadExcelNewFormatService {

	private static final String RENT_CELL = "RENT";
	private static final String HEADER_CELL = "YEAR";
	private static final String FOOTER_CELL = "G.TOTAL";

	private static final String[] MONTHS = new String[] { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP",
			"OCT", "NOV", "DEC" };

	public RentDemandResponse getDatafromExcel(InputStream inputStream, int sheetIndex) {
		List<RentDemand> demands = new ArrayList<>();
		List<RentPayment> payments = new ArrayList<>();
		List<String> rentDurations = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			/* Prepare list of RentDemands */
			getRentYearDeatils(sheet).forEach((key, value) -> {
				List<String> rentDuration = getAllSequenceOfYears(key);
				rentDurations.addAll(rentDuration);
				rentDuration.forEach(rent -> {
					demands.add(RentDemand.builder().generationDate(convertStrDatetoLong(rent))
							.interestSince(convertStrDatetoLong(rent)).collectionPrincipal(value)
							.remainingPrincipal(value).build());
				});
			});
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
					Integer currentRowYear = ((Double) getValueFromCell(currentRow.getCell(0))).intValue();
					for (int i = 1; i < currentRow.getLastCellNum() - 1; i++) {
						if (rentDurations.contains(1 + "-" + MONTHS[i - 1] + "-" + currentRowYear)) {
							payments.add(RentPayment.builder()
									.amountPaid((Double) getValueFromCell(currentRow.getCell(i)))
									.dateOfPayment(convertStrDatetoLong(1 + "-" + MONTHS[i - 1] + "-" + currentRowYear))
									.build());
						}

					}
				}
			}
			System.out.println(demands.size() + "<:temp:>" + payments.size());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("File reading operation fails due to :" + e.getMessage());
		}
		return new RentDemandResponse(demands, payments);
	}

	private long convertStrDatetoLong(String dateStr) {
		try {
			SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
			Date d = f.parse(dateStr);
			return d.getTime();
		} catch (Exception e) {
			log.error("Date parsing issue occur :" + e.getMessage());
		}
		return 0;
	}

	private Map<String, Double> getRentYearDeatils(Sheet sheet) {
		Map<String, Double> rentYearDetails = new HashMap<String, Double>();
		int rowRentCell = 0;
		int columnRentCell = 0;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row currentRow = rowIterator.next();
			for (int i = 0; i < currentRow.getLastCellNum(); i++) {
				if (RENT_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(i)))) {
					rowRentCell = currentRow.getRowNum();
					columnRentCell = i;
					break;
				}
			}
		}
		for (int i = rowRentCell + 1; i < rowRentCell + 6; i++) {
			Row row = sheet.getRow(i);
			rentYearDetails.put(String.valueOf(row.getCell(columnRentCell)),
					Double.valueOf(String.valueOf(row.getCell(columnRentCell + 2))));
		}
		return rentYearDetails;
	}

	private List<String> getAllSequenceOfYears(String rentYears) {
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
