package org.egov.waterconnection.repository;

import java.util.List;

import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.model.BillGenerationFile;

public interface BillGenerationDao {

	List<BillGeneration> saveBillingData(List<BillGeneration> listOfBills);

	List<BillGeneration> getBillingEstimation();

	BillGenerationFile getFilesStoreUrl();

	void savefileHistory(BillGenerationFile billFile, List<BillGeneration> bill);

	List<BillGenerationFile> getBillingFiles();

	List<BillGeneration> getBillData(BillGeneration billGeneration);

}
