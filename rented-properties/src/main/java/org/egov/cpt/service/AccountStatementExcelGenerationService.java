package org.egov.cpt.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.egov.cpt.models.RentAccountStatement.Type;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.util.FileStoreUtils;
import org.egov.cpt.util.NotificationUtil;
import org.egov.cpt.web.contracts.AccountStatementResponse;
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
	private NotificationUtil notificationUtil;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static String[] propertyColumns = { "Name", "Date of Allotment As Per Lease Deed", "Transit Site No.",
			"Area", "Rent per sq yd", "Security Advance Taken By o/o BDPO U.T.Interest",
			"Yr. Rent (increase as per Clause 4 of Lease Deed)", "Montly Rent", "Interest" };
	private static final String PAYMENT = "Payment";
	private static final String RENT = "Rent";

	@Autowired
	public AccountStatementExcelGenerationService(PropertyRepository propertyRepository,
			PropertyService propertyService, FileStoreUtils fileStoreUtils, NotificationUtil notificationUtil) {
		this.propertyRepository = propertyRepository;
		this.propertyService = propertyService;
		this.fileStoreUtils = fileStoreUtils;
		this.notificationUtil = notificationUtil;
	}

	public List<HashMap<String, String>> generateAccountStatementExcel(
			AccountStatementCriteria accountStatementCriteria, RequestInfo requestInfo) {

		List<Property> properties = propertyRepository
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid())
						.relations(Collections.singletonList("owner")).build());

		Property property = properties.get(0);
		
		AccountStatementResponse accountStatementResponse = propertyService.searchPayments(accountStatementCriteria,
				requestInfo);
		List<Double> amounts = accountStatementResponse.getRentAccountStatements().stream()
				.filter(rentAccountStmt -> rentAccountStmt.getType().name().equals(Type.D.name()))
				.map(rent -> rent.getAmount()).collect(Collectors.toList());
		List<String> propertyList = new ArrayList<>();
		propertyList.add(property.getOwners().get(0).getOwnerDetails().getName());
		propertyList.add(getFormattedDate(property.getOwners().get(0).getOwnerDetails().getAllotmentStartdate()));
		propertyList.add(property.getTransitNumber());
		Double rent = 0.00;
		if (property.getPropertyDetails().getArea() != null) {
			propertyList.add(property.getPropertyDetails().getArea() + " sq.yd");
			Double area = Double.valueOf(property.getPropertyDetails().getArea());
			if (amounts.size() >= 2) {
				rent = amounts.get(amounts.size() - 2) / area;
			}
		} else {
			propertyList.add("0.00 sq.yd");
		}
		propertyList.add(String.format("%,.2f", rent));

		propertyList.add("");
		propertyList.add(property.getPropertyDetails().getRentIncrementPercentage().intValue() + "%");
		if (amounts.size() >= 2) {
			propertyList.add(String.format("%,.2f", amounts.get(amounts.size() - 2)));
		} else {
			propertyList.add("0.00");
		}
		propertyList.add(
				property.getPropertyDetails().getInterestRate().intValue() + "% P.A as per clause 15 of Lease Deed");
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
			cell.setCellValue(String.format("Provisional Statement of Plot No. %s,", property.getTransitNumber()));
			cell.setCellStyle(headerCellStyle);

			Row headerRow2 = sheet.createRow(1);
			cell = headerRow2.createCell(0);
			cell.setCellValue(" ");
			cell.setCellStyle(headerCellStyle);

			cell = headerRow2.createCell(1);
			String localizationMessages = notificationUtil.getLocalizationMessages(property.getTenantId(), requestInfo);
			String colony = notificationUtil.getMessageTemplate(property.getColony(), localizationMessages);
			cell.setCellValue(colony);
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

			String rupees = "\u20B9";
			byte[] utf8 = rupees.getBytes("UTF-8");
			rupees = new String(utf8, "UTF-8");
			String[] columns = { "Date", "Amount(" + rupees + ")", "Type(Payment)", "Type(Rent)",
					"Principal Due(" + rupees + ")", "Interest Due(" + rupees + ")", "Total Due(" + rupees + ")",
					"Account Balance(" + rupees + ")", "Receipt Number" };
			for (int i = 0; i < columns.length; i++) {
				cell = headerRow1.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			int rowNum = 14;
			int statementsSize = accountStatementResponse.getRentAccountStatements().size();
 
			for (int i = 0; i < statementsSize; i++) {
				RentAccountStatement rentAccountStmt = accountStatementResponse.getRentAccountStatements().get(i);
				Row row = sheet.createRow(rowNum++);
				if (i < statementsSize - 1) {
					row.createCell(0).setCellValue(getFormattedDate(rentAccountStmt.getDate()));
					row.createCell(1).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getAmount())));
					Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(Type.C.name()))
							.ifPresent(o -> row.createCell(2).setCellValue(PAYMENT));
					Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(Type.D.name()))
							.ifPresent(o -> row.createCell(3).setCellValue(RENT));
				} else {
					row.createCell(0).setCellValue("Balance as on " + getFormattedDate(rentAccountStmt.getDate()));
				}

				row.createCell(4).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingPrincipal())));
				row.createCell(5).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingInterest())));
				row.createCell(6).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getDueAmount())));
				row.createCell(7).setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingBalance())));
				if (i < statementsSize - 1) {
					Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(Type.C.name()))
							.ifPresent(o -> row.createCell(8).setCellValue(o.getReceiptNo()));
				}
			}

			/**
			 * Write workbook to byte array
			 */
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			workbook.write(baos);
			String fileName = String.format("AccountStatement-%s.xlsx", property.getTransitNumber());
			List<HashMap<String, String>> response = fileStoreUtils.uploadStreamToFileStore(baos,
					property.getTenantId(), fileName, XLSX_CONTENT_TYPE);

			baos.close();

			// Closing the workbook
			workbook.close();
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		throw new CustomException("XLS_NOT_GENERATED", "Could not generate account statement");
	}

	private String getFormattedDate(long date) {
		return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER);
	}
}
