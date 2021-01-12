package org.egov.waterconnection.service;

import java.util.List;

import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.model.BillGenerationFile;
import org.egov.waterconnection.model.BillGenerationRequest;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;

public interface BillGenerationService {

	public List<BillGeneration> saveBillingData(BillGenerationRequest billGenerationRequest);

	public List<BillGenerationFile> generateBillFile(BillGenerationRequest billGenerationRequest);

	public List<BillGenerationFile> getBillingFiles();

	public List<BillGeneration> getBillData(BillGenerationRequest billGenerationRequest);

	
}
