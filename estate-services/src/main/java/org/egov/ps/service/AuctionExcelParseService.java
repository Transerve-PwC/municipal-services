package org.egov.ps.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.egov.ps.model.AuctionBidder;
import org.egov.tracer.model.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuctionExcelParseService {

	private static final String HEADER_CELL = "Auction id";
	private static final String FOOTER_CELL = "Will be same for all users ";

	public List<AuctionBidder> getDatafromExcel(InputStream inputStream, int sheetIndex) {
		List<AuctionBidder> auctions = new ArrayList<>();
		try {
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			Iterator<Row> rowIterator = sheet.iterator();
			boolean shouldParseRows = false;
			List<String> headerCells = new ArrayList<>();

			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();

				if (HEADER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					shouldParseRows = true;
					headerCells = new ArrayList<>();
					for (int cn = 0; cn < currentRow.getLastCellNum(); cn++) {
						Cell cell = currentRow.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
						headerCells.add(cell.getRichStringCellValue().getString());
					}
					continue;
				}

				/* Fetching Data will End after this */
				if (FOOTER_CELL.equalsIgnoreCase(String.valueOf(currentRow.getCell(0)))) {
					break;
				}

				if (shouldParseRows) {
					AuctionBidder auction = AuctionBidder.builder().build();

					auction.setAuctionId(getStringValueOfCellInRowAtIndex(currentRow, 0));
					auction.setDescription(getStringValueOfCellInRowAtIndex(currentRow, 1));
					auction.setBidderName(getStringValueOfCellInRowAtIndex(currentRow, 2));
					auction.setDepositedEMDAmount(new BigDecimal(getNumericValueOfCellInRowAtIndex(currentRow, 3)));
					auction.setDepositDate((long) getNumericValueOfCellInRowAtIndex(currentRow, 4));
					auction.setEmdValidityDate((long) getNumericValueOfCellInRowAtIndex(currentRow, 5));
					auction.setRefundStatus(getStringValueOfCellInRowAtIndex(currentRow, 6));

					auctions.add(auction);
				}

			}
			if (headerCells.isEmpty()) {
				throw new CustomException(HttpStatus.FORBIDDEN.getReasonPhrase(),
						"Invalid Excel Format,Could not get create workbook or parse data");
			}
		} catch (Exception e) {
			log.error("File reading operation fails due to :" + e.getMessage());
			throw new CustomException(HttpStatus.FORBIDDEN.getReasonPhrase(),
					"Invalid Excel Format,Could not get create workbook or parse data");
		}
		return auctions;
	}

	private String getStringValueOfCellInRowAtIndex(Row row, int index) {
		Cell cell1 = row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		switch (cell1.getCellType()) {
			case STRING:
				return cell1.getRichStringCellValue().getString();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell1)) {
					return String.valueOf(cell1.getDateCellValue().getTime());
				} else {
					return String.valueOf(cell1.getNumericCellValue());
				}
			default:
				return "";
		}
	}

	private double getNumericValueOfCellInRowAtIndex(Row row, int index) {
		Cell cell1 = row.getCell(index, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		switch (cell1.getCellType()) {
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell1)) {
					return cell1.getDateCellValue().getTime();
				} else {
					return cell1.getNumericCellValue();
				}
			case STRING:
				return Double.parseDouble(cell1.getRichStringCellValue().getString());
			default:
				return 0.0;
		}
	}
}
