package org.egov.cpt.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.models.AccountStatementCriteria;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.util.FileStoreUtils;
import org.egov.cpt.web.contracts.AccountStatementResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountStatementExcelGenerationService {

	private PropertyRepository propertyRepository;
	private PropertyService propertyService;
	private FileStoreUtils fileStoreUtils;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static String[] columns = { "Date", "Amount", "Type", "Interest Due", "Principal Due", "Total Due",
			"Account Balance" };
	private static String[] propertyColumns = { "Name", "Date of Allotment As Per Lease Deed", "Plot No.", "Area",
			"Rent", "Security Advance Taken By o/o BDPO U.T.Interest",
			"Yr. Rent (increase as per Clause 4 of Lease Deed)", "Montly Rent", "Interest" };

	@Autowired
	public AccountStatementExcelGenerationService(PropertyRepository propertyRepository,
			PropertyService propertyService, FileStoreUtils fileStoreUtils) {
		this.propertyRepository = propertyRepository;
		this.propertyService = propertyService;
		this.fileStoreUtils = fileStoreUtils;
	}

	public String generageAccountStatementExcel(AccountStatementCriteria accountStatementCriteria,
			RequestInfo requestInfo) {

		AccountStatementResponse accountStatementResponse = propertyService.searchPayments(accountStatementCriteria,
				requestInfo);

		List<Property> properties = propertyRepository
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid())
						.relations(Collections.singletonList("owner")).build());

		List<String> propertyList = new ArrayList<>();
		propertyList.add(properties.get(0).getOwners().get(0).getOwnerDetails().getName());
		propertyList.add(
				Instant.ofEpochMilli(properties.get(0).getOwners().get(0).getOwnerDetails().getAllotmentStartdate())
						.atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER).toString());
		propertyList.add(properties.get(0).getTransitNumber());
		propertyList.add(properties.get(0).getPropertyDetails().getArea());
		propertyList.add(properties.get(0).getPropertyDetails().getRentPerSqyd());
		propertyList.add("");
		propertyList.add(properties.get(0).getPropertyDetails().getRentIncrementPercentage().toString());
		propertyList.add(properties.get(0).getOwners().get(0).getOwnerDetails().getMonthlyRent());
		propertyList.add(properties.get(0).getPropertyDetails().getInterestRate().toString()
				+ "% P.A as per clause 15 of Lease Deed");
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("AccountStatement");

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setColor(IndexedColors.BLACK.getIndex());

			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);

			// Create a Row
			Row headerRow = sheet.createRow(0);

			Cell cell = headerRow.createCell(0);
			cell.setCellValue(" ");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow.createCell(1);
			cell.setCellValue("Provisional Statement of Plot No. ,");
			cell.setCellStyle(headerCellStyle);

			Row headerRow2 = sheet.createRow(1);
			cell = headerRow2.createCell(0);
			cell.setCellValue(" ");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow2.createCell(1);
			cell.setCellValue("Vikas Nagar, Mauli Jagaran, UT Chandigarh");
			cell.setCellStyle(headerCellStyle);
			int j = 0;
			for (int i = 3; i < 12; i++) {
				Row headerRow3 = sheet.createRow(i);
				cell = headerRow3.createCell(0);
				if (j < propertyColumns.length) {
					cell.setCellValue(propertyColumns[j]);
					cell.setCellStyle(headerCellStyle);
				}
				cell = headerRow3.createCell(1);
				if (j < propertyList.size()) {
					cell.setCellValue(propertyList.get(j));
				}
				j++;
			}

			Row headerRow1 = sheet.createRow(13);
			for (int i = 0; i < columns.length; i++) {
				cell = headerRow1.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			int rowNum = 14;
			for (RentAccountStatement rentAccountStmt : accountStatementResponse.getRentAccountStatements()) {
				Row row = sheet.createRow(rowNum++);

				row.createCell(0).setCellValue(Instant.ofEpochMilli(rentAccountStmt.getDate())
						.atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER));
				row.createCell(1).setCellValue(rentAccountStmt.getAmount());
				row.createCell(2).setCellValue(rentAccountStmt.getType().toString());
				row.createCell(3).setCellValue(rentAccountStmt.getRemainingInterest());
				row.createCell(4).setCellValue(rentAccountStmt.getRemainingPrincipal());
				row.createCell(5).setCellValue(rentAccountStmt.getDueAmount());
				row.createCell(6).setCellValue(rentAccountStmt.getRemainingBalance());
			}

			System.out.println(sheet);
			System.out.println(headerRow);
			// Write the output to a file
			File file = new File("poi-generated-file.xlsx");
			FileOutputStream fileOut = new FileOutputStream(file);

			String response = fileStoreUtils.fetchFileStoreId(file, properties.get(0));

			workbook.write(fileOut);
			fileOut.close();

			// Closing the workbook
			workbook.close();
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}
}
