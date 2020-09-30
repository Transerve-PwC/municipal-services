package org.egov.cpt.service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
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
import org.egov.cpt.models.AccountStatementCriteria;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.models.RentAccountStatement.Type;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.util.FileStoreUtils;
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

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static String[] columns = { "Date", "Amount", "Type (payment)", "Type (Rent)", "Remaining Principal", "Total Due", "Account Balance",
			"Remaining Interest", "Receipt Number" };
	private static String[] propertyColumns = { "Name", "Date of Allotment As Per Lease Deed", "Transit Site No.", "Area",
			"Rent", "Security Advance Taken By o/o BDPO U.T.Interest",
			"Yr. Rent (increase as per Clause 4 of Lease Deed)", "Montly Rent", "Interest" };
	private static final String PAYMENT = "Payment";
	private static final String RENT = "Rent";

	@Autowired
	public AccountStatementExcelGenerationService(PropertyRepository propertyRepository,
			PropertyService propertyService, FileStoreUtils fileStoreUtils) {
		this.propertyRepository = propertyRepository;
		this.propertyService = propertyService;
		this.fileStoreUtils = fileStoreUtils;
	}

	public List<HashMap<String, String>> generateAccountStatementExcel(
			AccountStatementCriteria accountStatementCriteria, RequestInfo requestInfo) {

		AccountStatementResponse accountStatementResponse = propertyService.searchPayments(accountStatementCriteria,
				requestInfo);

		List<Property> properties = propertyRepository
				.getProperties(PropertyCriteria.builder().propertyId(accountStatementCriteria.getPropertyid())
						.relations(Collections.singletonList("owner")).build());

		Property property = properties.get(0);
		List<String> propertyList = new ArrayList<>();
		propertyList.add(property.getOwners().get(0).getOwnerDetails().getName());
		propertyList.add(Instant.ofEpochMilli(property.getOwners().get(0).getOwnerDetails().getAllotmentStartdate())
				.atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER).toString());
		propertyList.add(property.getTransitNumber());
		propertyList.add(property.getPropertyDetails().getArea());
		propertyList.add(property.getPropertyDetails().getRentPerSqyd());
		propertyList.add("");
		propertyList.add(property.getPropertyDetails().getRentIncrementPercentage().toString());
		propertyList.add(property.getOwners().get(0).getOwnerDetails().getMonthlyRent());
		propertyList.add(
				property.getPropertyDetails().getInterestRate().toString() + "% P.A as per clause 15 of Lease Deed");
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
			int sumRemainingPrincipal=0;
			int sumTotalDue=0;
			int sumRemainingInterest=0;
			for (RentAccountStatement rentAccountStmt : accountStatementResponse.getRentAccountStatements()) {
				Row row = sheet.createRow(rowNum++);
				sumRemainingPrincipal+=rentAccountStmt.getRemainingPrincipal();
				sumTotalDue+=rentAccountStmt.getDueAmount();
				sumRemainingInterest+=rentAccountStmt.getRemainingInterest();
				row.createCell(0).setCellValue(Instant.ofEpochMilli(rentAccountStmt.getDate())
						.atZone(ZoneId.systemDefault()).toLocalDate().format(FORMATTER));
				row.createCell(1).setCellValue(rentAccountStmt.getAmount());

				Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(Type.C.name()))
						.ifPresent(o -> row.createCell(2).setCellValue(PAYMENT));
				Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(Type.D.name()))
						.ifPresent(o -> row.createCell(3).setCellValue(RENT));

				row.createCell(4).setCellValue(rentAccountStmt.getRemainingPrincipal());
				row.createCell(5).setCellValue(rentAccountStmt.getDueAmount());
				row.createCell(6).setCellValue(rentAccountStmt.getRemainingBalance());
				row.createCell(7).setCellValue(rentAccountStmt.getRemainingInterest());
				Optional.ofNullable(rentAccountStmt).filter(r -> r.getType().name().equals(Type.C.name()))
						.ifPresent(o -> row.createCell(8).setCellValue(o.getReceiptNo()));
			}

			Row row = sheet.createRow(rowNum);
			row.createCell(0).setCellValue("Total as on "+ LocalDate.now().format(FORMATTER));
			row.createCell(4).setCellValue(sumRemainingPrincipal);
			row.createCell(5).setCellValue(sumTotalDue);
			row.createCell(7).setCellValue(sumRemainingInterest);
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
}
