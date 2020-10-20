package org.egov.ps.service;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstateModuleResponse;
import org.egov.ps.web.contracts.EstatePayment;
import org.javers.common.collections.Arrays;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EstateCalculationExcelReadService {

	private static final String HEADER_CELL = "Month";
	private static final String FOOTER_CELL = "Total";
	private static final String FOOTER_CELL2 = "Summery";
	private static final int[] REQUIRED_COLUMNS = { 0, 1, 2, 5, 6, 8, 9, 11, 12, 13, 14, 17, 18, 21, 22 };
	private static final String[] EXCELMAPPINGNAME = new String[] { "month", "rentDue", "rentReceived", "rentReceiptNo",
			"date", "dueDateOfRent", "rentDateOfReceipt", "penaltyInterest", "stGstRate", "stGstDue", "paid",
			"dateOfReceipt", "stGstReceiptNo", "noOfDays", "delayedPaymentOfGST" };
	private static final DecimalFormat DOUBLE_RISTRICT = new DecimalFormat("#.##");
	private static int SKIP_ROW_COUNT = 1;

	public EstateModuleResponse getDatafromExcel(InputStream inputStream, int sheetIndex) {
		List<Map<String, Object>> estateCalculations = new ArrayList<>();
		List<EstateDemand> estateDemands = new ArrayList<EstateDemand>();
		List<EstatePayment> estatePayments = new ArrayList<EstatePayment>();
		try {
			Workbook workbook = WorkbookFactory.create(inputStream);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> rowIterator = sheet.iterator();

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			boolean shouldParseRows = false;
			List<String> headerCells = new ArrayList<>();
			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();

				if (HEADER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					shouldParseRows = true;
					headerCells = new ArrayList<>();
					for (int cn = 0; cn < currentRow.getLastCellNum(); cn++) {
						if (Arrays.asList(REQUIRED_COLUMNS).contains(cn)) {
							Cell cell = currentRow.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
							headerCells.add(cell.getRichStringCellValue().getString());
						}
					}
					continue;
				}

				if (shouldParseRows && SKIP_ROW_COUNT > 0) {
					SKIP_ROW_COUNT--;
					continue;
				}

				/* Fetching Data will End after this */
				if (FOOTER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))
						|| FOOTER_CELL2.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					break;
				}

				if (shouldParseRows && SKIP_ROW_COUNT == 0 && !checkEmpty(currentRow.getCell(0))) {
					Map<String, Object> cellData = new HashedMap<String, Object>();
					int headerCount = 0;
					/* Fetching Body Data will read after this */
					for (int columnNumber : REQUIRED_COLUMNS) {
						Cell cell = currentRow.getCell(columnNumber, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						if (columnNumber == 6) {
							cellData.put(EXCELMAPPINGNAME[headerCount],
									extractDateFromString(String.valueOf(getValueFromCell(cell, evaluator))));
						} else {
							cellData.put(EXCELMAPPINGNAME[headerCount], getValueFromCell(cell, evaluator));
						}
						headerCount++;
					}
					estateCalculations.add(cellData);
				}
			}
			estateCalculations.forEach(estateCalculationMap -> {
				estateDemands.add(EstateDemand.builder().isPrevious(checkPreviousTab(estateCalculationMap.get("month")))
						.demandDate(checkModifyValueLong(estateCalculationMap.get("dueDateOfRent")))
						.rent(checkModifyValue(estateCalculationMap.get("rentDue")))
						.penaltyInterest(checkModifyValue(estateCalculationMap.get("penaltyInterest")))
						.gstInterest(calculateDelayedPayment(estateCalculationMap))
						.gst((int) (checkModifyValue(estateCalculationMap.get("stGstRate")) * 100))
						.collectedRent(checkModifyValue(estateCalculationMap.get("rentReceived")))
						.collectedGST(checkModifyValue(estateCalculationMap.get("stGstDue")))
						.noOfDays(checkModifyValue(estateCalculationMap.get("noOfDays")))
						.paid(checkModifyValue(estateCalculationMap.get("paid"))).build());

				if (parseInDouble(estateCalculationMap.get("rentReceived")) != null
						&& parseInDouble(estateCalculationMap.get("rentReceived")) > 0) {
					estatePayments.add(EstatePayment.builder()
							.receiptDate(checkModifyValueLong(estateCalculationMap.get("rentDateOfReceipt")))
							.rentReceived(checkModifyValue(estateCalculationMap.get("rentReceived")))
							.receiptNo(String.valueOf(estateCalculationMap.get("rentReceiptNo"))).build());

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EstateModuleResponse.builder().estateDemands(estateDemands).estatePayments(estatePayments).build();
	}

	private Boolean checkPreviousTab(Object value) {
		if (!checkEmpty(value)) {
			return "Previous bal".equalsIgnoreCase(value.toString()) ? true : false;
		} else {
			return false;
		}
	}

	private Long parseInLong(Object value) {
		if (value == null || "null".equalsIgnoreCase(value.toString()) || value.toString().isEmpty()
				|| !NumberUtils.isNumber(value.toString())) {
			return null;
		} else {
			return Long.parseLong(value.toString().split("\\.")[0]);
		}
	}

	private Double parseInDouble(Object value) {
		if (value == null || "null".equalsIgnoreCase(value.toString()) || value.toString().isEmpty()
				|| !NumberUtils.isNumber(value.toString())) {
			return null;
		} else {
			return Double.parseDouble(value.toString());
		}
	}

	private boolean checkEmpty(Object value) {
		if (value == null || "null".equalsIgnoreCase(value.toString()) || value.toString().isEmpty()) {
			return true;
		}
		return false;
	}

	private Double checkModifyValue(Object value) {
		return parseInDouble(value) == null ? 0.0 : parseInDouble(value);
	}

	private Long checkModifyValueLong(Object value) {
		return parseInLong(value) == null ? Long.parseLong("0") : parseInLong(value);
	}

	private Double calculateDelayedPayment(Map<String, Object> estateCalculationModel) {
		Double stGSTDue = checkModifyValue(estateCalculationModel.get("stGstRate"))
				* checkModifyValue(estateCalculationModel.get("rentDue"));
		Double stGSTRate = checkModifyValue(estateCalculationModel.get("stGstRate"));
		Double noOfDays = checkModifyValue(estateCalculationModel.get("noOfDays"));
		return Double.parseDouble(DOUBLE_RISTRICT.format(stGSTDue * stGSTRate * noOfDays / 365));
	}

	/**
	 * Parse values like 8.4.19
	 * 
	 * @param str
	 * @return
	 * @throws DateTimeParseException
	 */
	private Long extractDateFromString(String str) throws DateTimeParseException {
		if (!str.isEmpty()) {
			str = str.split(",")[0];
			String[] splittedDate = str.split("[\\s@&.?$+-]+");
			if (splittedDate.length == 2) {
				splittedDate = (String[]) ArrayUtils.add(splittedDate, 0, "1");
				str = "1." + str;
			}
			if (splittedDate.length == 3) {
				int monthIndex = Integer.parseInt(splittedDate[1]) - 1;
				Pattern datePattern = Pattern.compile("\\d*$");
				Matcher dateMatcher = datePattern.matcher(str);
				if (dateMatcher.find()) {
					String twoYearDate = dateMatcher.group();
					twoYearDate = twoYearDate.substring(twoYearDate.length() - 2);
					int twoYearDateInt = Integer.parseInt(twoYearDate);
					if (twoYearDateInt >= 100) {
						throw new DateTimeParseException("Cannot parse " + str + " as a date.", "", 0);
					}
					int year = twoYearDateInt < 50 ? 2000 + twoYearDateInt : 1900 + twoYearDateInt;
					Calendar calendar = Calendar.getInstance();
					calendar.set(year, monthIndex, Integer.parseInt(splittedDate[0]), 12, 0);
					return calendar.getTimeInMillis();
				}
			} else {
				return Long.parseLong(str);
			}
			throw new DateTimeParseException("Cannot parse " + str + " as a date.", "", 0);
		}
		return null;
	}

	private Object getValueFromCell(Cell cell1, FormulaEvaluator evaluator) {
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
				objValue = new DataFormatter().formatCellValue(cell1, evaluator);
				if (NumberUtils.isNumber(objValue.toString())) {
					objValue = cell1.getNumericCellValue();
				}
				break;

			default:
				objValue = "";
		}
		return objValue;
	}
}
