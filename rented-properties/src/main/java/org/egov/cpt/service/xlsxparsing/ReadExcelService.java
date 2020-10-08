package org.egov.cpt.service.xlsxparsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentDemandResponse;
import org.egov.cpt.models.RentPayment;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReadExcelService extends AbstractExcelService implements IReadExcelService {

	public RentDemandResponse getDatafromExcelPath(String filePath) {
		RentDemandResponse response = new RentDemandResponse();
		try {
			response = getDatafromExcel(new FileInputStream(new File(filePath)), 0);
		} catch (FileNotFoundException e) {
			log.error("File converting inputstream operation failed due to :" + e.getMessage());
		}
		return response;
	}

	private Integer checkFormatOfexcel(Workbook workbook, int sheetIndex) {
		try {
			Integer formatFlag = -1;
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();
				if (HEADER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					formatFlag = 0;
					break;
				} else if (HEADER_CELL_FORMAT2.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					formatFlag = 1;
					break;
				}
			}
			return formatFlag;
		} catch (Exception e) {
			log.error("File reading operation fails due to :" + e.getMessage());
			throw new CustomException("INVALID_RENT_HISTORY_FORMAT",
					"Uploaded rent history format cannot be determined");
		}
	}

	public RentDemandResponse getDatafromExcel(File file, int sheetIndex) {
		try {
			Workbook workbook = WorkbookFactory.create(file);
			return this.getDataFromWorkBook(workbook, sheetIndex);
		} catch (EncryptedDocumentException | IOException e) {
			log.error("Could not get create workbook or parse data", e);
			throw new CustomException("PARSE_ERROR", "Could not get create workbook or parse data");
		}
	}

	@Deprecated
	public RentDemandResponse getDatafromExcel(InputStream inputStream, int sheetIndex) {
		try {
			Workbook workbook = WorkbookFactory.create(inputStream);
			return this.getDataFromWorkBook(workbook, sheetIndex);
		} catch (EncryptedDocumentException | IOException e) {
			log.error("Could not get create workbook or parse data", e);
			throw new CustomException("PARSE_ERROR", "Could not get create workbook or parse data");
		}
	}

	private RentDemandResponse getDataFromWorkBook(Workbook workbook, int sheetIndex) {
		Integer formatFlag = this.checkFormatOfexcel(workbook, sheetIndex);
		if (1 == formatFlag) {
			return this.getDataFromWorkbookFormat2(workbook, sheetIndex);
		} else if (0 == formatFlag) {
			return this.getDataFromWorkbookFormat1(workbook, sheetIndex);
		} else
			throw new CustomException("INVALID_RENT_HISTORY_FORMAT",
					"Uploaded rent history format cannot be determined");
	}

	private RentDemandResponse getDataFromWorkbookFormat1(Workbook workbook, int sheetIndex) {
		List<RentDemand> demands = new ArrayList<>();
		List<RentPayment> payments = new ArrayList<>();

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
				RentDemandPayment demandPayment = getDemandAndPaymentFromRow(currentRow);
				if (demandPayment == null) {
					continue;
				}
				demands.add(demandPayment.demand);
				if (demandPayment.payment != null) {
					payments.add(demandPayment.payment);
				}
			}
		}
		return new RentDemandResponse(demands, payments);
	}

	private RentDemandResponse getDataFromWorkbookFormat2(Workbook workbook, int sheetIndex) {
		Sheet sheet = workbook.getSheetAt(sheetIndex);
		List<RentDemand> demands = new ArrayList<>();
		List<RentPayment> payments = new ArrayList<>();
		List<String> rentDurations = new ArrayList<>();
		/* Prepare list of RentDemands */
		getRentYearDeatils(sheet).forEach((key, value) -> {
			List<String> rentDuration = getAllSequenceOfYears(key);
			rentDurations.addAll(rentDuration);
			rentDuration.forEach(rent -> {
				demands.add(RentDemand.builder().generationDate(convertStrDatetoLong(rent))
						.interestSince(convertStrDatetoLong(rent)).collectionPrincipal(value).remainingPrincipal(value)
						.build());
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
			if (HEADER_CELL_FORMAT2.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
				shouldParseRows = true;
				continue;
			}

			/* Fetching Data will End after this */
			if (FOOTER_CELL_FORMAT2.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
				break;
			}

			if (shouldParseRows) {
				this.parseFormat2Payments(currentRow, rentDurations, payments);
			}
		}
		return new RentDemandResponse(demands, payments);
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
}
