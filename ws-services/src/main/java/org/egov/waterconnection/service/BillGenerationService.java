package org.egov.waterconnection.service;

import java.util.List;

import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.model.BillGenerationRequest;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;

public interface BillGenerationService {

	public List<BillGeneration> saveBillingData(BillGenerationRequest billGenerationRequest);

	
}
