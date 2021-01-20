package org.egov.ps.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.common.contract.request.RequestInfo;
import org.egov.ps.model.AccountStatementCriteria;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.FileStoreUtils;
import org.egov.ps.util.PSConstants;
import org.egov.ps.web.contracts.AccountStatementResponse;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateAccountStatement;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
	private static String[] headerColumns = { "Date", "Consolidated Demand", "Amount", "Type (Payment)", "Type (Rent)", "Principal due",
			"GST Due", "Interest Due", "Gst Penalty Due", "Total Due", "Account Balance", "Receipt No." };
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
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid()).build());

		Property property = properties.get(0);
		List<HashMap<String, String>> response = new ArrayList<HashMap<String, String>>();

		EstateAccount estateAccount = propertyRepository.getPropertyEstateAccountDetails(Collections.singletonList(property.getPropertyDetails().getId()));
		if (!CollectionUtils.isEmpty(property.getPropertyDetails().getEstateDemands())
				&& null != estateAccount
				&& property.getPropertyDetails().getPaymentConfig() != null
				&& property.getPropertyDetails().getPropertyType().equalsIgnoreCase(PSConstants.ES_PM_LEASEHOLD)) {
			AccountStatementResponse accountStatementResponse = propertyService.searchPayments(accountStatementCriteria,
					requestInfo);

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

				Cell cell = headerRow.createCell(1);
				cell.setCellValue("Account Statement of the Property. " + property.getSiteNumber());
				cell.setCellStyle(headerCellStyle);

				Row headerRow3 = sheet.createRow(2);
				for (int i = 0; i < headerColumns.length; i++) {
					cell = headerRow3.createCell(i);
					cell.setCellValue(headerColumns[i]);
					cell.setCellStyle(headerCellStyle);
				}

				int rowNum = 3;
				int statementsSize = accountStatementResponse.getEstateAccountStatements().size();

				for (int i = 0; i < statementsSize; i++) {
					EstateAccountStatement rentAccountStmt = accountStatementResponse.getEstateAccountStatements()
							.get(i);
					Row row = sheet.createRow(rowNum++);
					if (i < statementsSize - 1) {
						row.createCell(0).setCellValue(getFormattedDate(rentAccountStmt.getDate()));

						String result = rentAccountStmt.getIsPrevious() ? "CF" : "-";
						row.createCell(1).setCellValue(result);
						row.createCell(2)
								.setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getAmount())));
						Optional.ofNullable(rentAccountStmt)
								.filter(r -> r.getType().name().equals(EstateAccountStatement.Type.C.name()))
								.ifPresent(o -> row.createCell(3).setCellValue(PAYMENT));
						Optional.ofNullable(rentAccountStmt)
								.filter(r -> r.getType().name().equals(EstateAccountStatement.Type.D.name()))
								.ifPresent(o -> row.createCell(4).setCellValue(RENT));
					} else {
						row.createCell(0).setCellValue("Balance as on " + getFormattedDate(rentAccountStmt.getDate()));
					}

					row.createCell(5).setCellValue(
							String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingPrincipal())));
					row.createCell(6)
							.setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingGST())));
					row.createCell(7).setCellValue(
							String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingRentPenalty())));
					row.createCell(8).setCellValue(
							String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingGSTPenalty())));
					row.createCell(9)
							.setCellValue(String.format("%,.2f", Double.valueOf(rentAccountStmt.getDueAmount())));
					row.createCell(10).setCellValue(
							String.format("%,.2f", Double.valueOf(rentAccountStmt.getRemainingBalance())));
					if (i < statementsSize - 1) {
						Optional.ofNullable(rentAccountStmt)
								.filter(r -> r.getType().name().equals(EstateAccountStatement.Type.C.name()))
								.ifPresent(o -> row.createCell(11).setCellValue(o.getReceiptNo()));
					}
				}

				/**
				 * Write workbook to byte array
				 */
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				workbook.write(baos);
				String fileName = String.format("AccountStatement-%s.xlsx", property.getSiteNumber());
				response = fileStoreUtils.uploadStreamToFileStore(baos,
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
		
		return response;
	}

	private String getFormattedDate(long date) {
		return Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER);
	}

}
