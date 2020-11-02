package org.egov.ps.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.FileStoreUtils;
import org.egov.ps.web.contracts.AccountStatementResponse;
import org.egov.ps.web.contracts.EstateAccountStatement;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountStatementExcelGenerationService {

	private PropertyRepository propertyRepository;
	private PropertyService propertyService;
	private FileStoreUtils fileStoreUtils;

	private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String RENT = "Rent";
	private static final String PAYMENT = "Payment";
	private static final String STGST = "ST/GST";
	private static String[] headerColumns = { "Date", "Amount", "Type (Payment)", "Type (Rent)", "Principal due",
			"GST Due", "Interest Due", "Gst Penalty Due", "Total Due", "Account Balance", "Receipt" };
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

	@Autowired
	public AccountStatementExcelGenerationService(PropertyRepository propertyRepository,
			PropertyService propertyService, FileStoreUtils fileStoreUtils) {
		this.propertyRepository = propertyRepository;
		this.propertyService = propertyService;
		this.fileStoreUtils = fileStoreUtils;
	}

	public List<HashMap<String, String>> generateAccountStatementExcel(
			AccountStatementCriteria accountStatementCriteria, RequestInfo requestInfo) {

		List<Property> properties = propertyRepository
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid())
						.relations(Collections.singletonList("owner")).build());

		Property property = properties.get(0);

		AccountStatementResponse accountStatementResponse = propertyService.searchPayments(accountStatementCriteria,
				requestInfo);
		
		System.out.println(accountStatementResponse);
		
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("AccountStatement");

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setColor(IndexedColors.RED.getIndex());

			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);

			// Create a Row
			Row headerRow = sheet.createRow(0);

			Cell cell = headerRow.createCell(8);
			cell.setCellValue("STATEMENT SHOWING THE RENT RECEIVED FROM SHOP NO. "+ property.getSiteNumber() +" VILLAGE DARIA");
			//cell.setBlank();
			cell.setCellStyle(headerCellStyle);
			
			Font headerFont1 = workbook.createFont();
			headerFont1.setBold(true);
			headerFont1.setFontHeightInPoints((short) 10);
			headerFont1.setColor(IndexedColors.BLACK.getIndex());
			// Create a CellStyle with the font
						CellStyle headerCellStyle1= workbook.createCellStyle();
						headerCellStyle1.setFont(headerFont1);
						headerCellStyle1.setAlignment(HorizontalAlignment.CENTER);
			Row headerRow2 = sheet.createRow(1);
			cell = headerRow2.createCell(0);
			cell.setCellValue(RENT);
			cell.setCellStyle(headerCellStyle1);
			// Create a cellRangeAddress to select a range to merge.
			CellRangeAddress cellRangeAddress = new CellRangeAddress(1,1,0,11);
 
			// Merge the selected cells.
			sheet.addMergedRegion(cellRangeAddress);
			
			cell = headerRow2.createCell(12);
			cell.setCellValue(STGST);
			cell.setCellStyle(headerCellStyle1);

			// Create a cellRangeAddress to select a range to merge.
			CellRangeAddress cellRangeAddress1 = new CellRangeAddress(1,1,12,22);
 
			// Merge the selected cells.
			sheet.addMergedRegion(cellRangeAddress1);

			Row headerRow3 = sheet.createRow(2);
			for (int i = 0; i < headerColumns.length; i++) {
				cell = headerRow3.createCell(i);
				cell.setCellValue(headerColumns[i]);
				cell.setCellStyle(headerCellStyle1);
			}
			
			int rowNum = 3;
			int statementsSize = accountStatementResponse.getEstateAccountStatements().size();
 
			for (int i = 0; i < statementsSize; i++) {
				EstateAccountStatement rentAccountStmt = accountStatementResponse.getEstateAccountStatements().get(i);
				Row row = sheet.createRow(rowNum++);
				if (i < statementsSize - 1) {
					row.createCell(0).setCellValue(getFormattedDate(rentAccountStmt.getDate()));
					row.createCell(1).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getAmount())));
					Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(EstateAccountStatement.Type.C.name()))
							.ifPresent(o -> row.createCell(2).setCellValue(PAYMENT));
					Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(EstateAccountStatement.Type.D.name()))
							.ifPresent(o -> row.createCell(3).setCellValue(RENT));
				} else {
					row.createCell(0).setCellValue("Balance as on " + getFormattedDate(rentAccountStmt.getDate()));
				}

				row.createCell(4).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingPrincipal())));
				//gst due
				row.createCell(5).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingGST())));
				
				row.createCell(6).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingRentPenalty())));
				//gst penalty due
				row.createCell(7).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingGSTPenalty())));
				row.createCell(8).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getDueAmount())));
				row.createCell(9).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingBalance())));
				if (i < statementsSize - 1) {
					Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(EstateAccountStatement.Type.C.name()))
							.ifPresent(o -> row.createCell(10).setCellValue(o.getReceiptNo()));
				}
			}
			
			/**
			 * Write workbook to byte array
			 */
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			workbook.write(baos);
			String fileName = String.format("AccountStatement-%s.xlsx", property.getSiteNumber());
			List<HashMap<String, String>> response = fileStoreUtils.uploadStreamToFileStore(baos,
					property.getTenantId(), fileName, XLSX_CONTENT_TYPE);

			baos.close();

			// Closing the workbook
			workbook.close();
			return response;

		} catch(Exception e) {
			log.error(e.getMessage());
		}
		throw new CustomException("XLS_NOT_GENERATED", "Could not generate account statement");
	}

	private String getFormattedDate(long date) {
		return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER);
	}

}
