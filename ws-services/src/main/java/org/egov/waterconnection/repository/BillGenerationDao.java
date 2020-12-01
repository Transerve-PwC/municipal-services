package org.egov.waterconnection.repository;

import java.util.List;

import org.egov.waterconnection.model.BillGeneration;

public interface BillGenerationDao {

	List<BillGeneration> saveBillingData(List<BillGeneration> listOfBills);

}
