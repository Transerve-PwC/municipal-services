package org.egov.waterconnection.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.AuditDetails;
import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.model.BillGenerationFile;
import org.egov.waterconnection.model.BillGenerationRequest;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.repository.BillGenerationDao;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillGenerationServiceImpl implements BillGenerationService {

	@Autowired
	private BillGenerationDao billRepository;

	@Autowired
	private WaterServicesUtil waterServicesUtil;

	@Override
	public List<BillGeneration> saveBillingData(BillGenerationRequest billGenerationRequest) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
		InputStream input = null;
		List<BillGeneration> listOfBills = new ArrayList<BillGeneration>();
		try {

			input = new URL(billGenerationRequest.getBillGeneration().getDocument().getFileStroreUrl()).openStream();
			XSSFWorkbook workbook = new XSSFWorkbook(input);
			int numRow = 1;
			XSSFSheet sheet = workbook.getSheetAt(0);
			XSSFRow rowIndex;
			int rows = sheet.getPhysicalNumberOfRows();
			AuditDetails auditDetails = waterServicesUtil
					.getAuditDetails(billGenerationRequest.getRequestInfo().getUserInfo().getUuid(), true);
			while (numRow <= rows) {
				rowIndex = sheet.getRow(numRow);
				if (rowIndex != null) {
					BillGeneration uploadFileData = new BillGeneration();
					int numCol = 0;
					uploadFileData.setBillGenerationId(UUID.randomUUID().toString());
					uploadFileData.setAuditDetails(auditDetails);
					uploadFileData.setStatus(WCConstants.STATUS_INITIATED);
					uploadFileData.setIsFileGenerated(false);
					XSSFCell cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setCcCode(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setDivSdiv(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setConsumerCode(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setBillCycle(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setBillGroup(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setSubGroup(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setBillType(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setName(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setAddress(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setAdd1(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setAdd2(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setAdd3(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setAdd4(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setAdd5(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setCessCharge(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setNetAmount(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setSurcharge(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setGrossAmount(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setTotalNetAmount(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setTotalSurcharge(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setTotalGrossAmount(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setFixChargeCode(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setFixCharge(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setDueDateCash(dateFormatter.format(cellIndex.getDateCellValue()));

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setDueDateCheque(dateFormatter.format(cellIndex.getDateCellValue()));

					listOfBills.add(uploadFileData);
					numRow++;
				} else {
					numRow++;
				}

			}

			billRepository.saveBillingData(listOfBills);
			return listOfBills;

		} catch (Exception e) {
			throw new CustomException("EXCELREADERROR", e.getMessage());
		} finally {

			try {
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public List<BillGenerationFile> generateBillFile(BillGenerationRequest billGenerationRequest) {
		PrintWriter writer;
		List<BillGenerationFile> billFileList = new ArrayList<BillGenerationFile>();
		try {
			String timeStamp = new SimpleDateFormat("hh:mm:ss a").format(new Date());

			List<BillGeneration> bill = billRepository.getBillingEstimation();

			if (bill.isEmpty()) {
				throw new CustomException("FILE_GENERATION_FAILED",
						"Data may not present or may have downloaded earlier please check history");
			}

			writer = new PrintWriter(WCConstants.WS_BILLING_FILENAME, "UTF-8");
			for (BillGeneration billGeneration : bill) {
				writer.println(billGeneration.getCcCode() + " " + timeStamp + " " + billGeneration.getDivSdiv()
						+ billGeneration.getConsumerCode() + " " + billGeneration.getTotalNetAmount() + " "
						+ billGeneration.getBillId());
			}

			writer.close();
			BillGenerationFile billFile = billRepository.getFilesStoreUrl();

			billRepository.savefileHistory(billFile, bill);
			billFileList.add(billFile);

		} catch (Exception e) {
			throw new CustomException("FILE_GENERATION_FAILED", e.getMessage());
		}
		return billFileList;
	}

	@Override
	public List<BillGenerationFile> getBillingFiles() {
		List<BillGenerationFile> billFile = billRepository.getBillingFiles();
		return billFile;
	}

	@Override
	public List<BillGeneration> getBillData(BillGenerationRequest billGenerationRequest) {
		List<BillGeneration> billData = billRepository.getBillData(billGenerationRequest.getBillGeneration());
		return billData;
	}

}
