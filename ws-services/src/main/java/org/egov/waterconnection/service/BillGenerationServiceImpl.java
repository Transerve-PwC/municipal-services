package org.egov.waterconnection.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
import org.egov.waterconnection.model.BillGenerationRequest;
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
					uploadFileData.setDueDateCash(cellIndex.getRawValue());

					cellIndex = rowIndex.getCell(numCol++);
					uploadFileData.setDueDateCheque(cellIndex.getRawValue());

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

}
