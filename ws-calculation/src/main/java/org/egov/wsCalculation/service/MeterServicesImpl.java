package org.egov.wsCalculation.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.repository.WSCalculationDao;
import org.egov.wsCalculation.util.MeterReadingUtil;
import org.egov.wsCalculation.validator.MDMSValidator;
import org.egov.wsCalculation.validator.WSCalculationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MeterServicesImpl implements MeterService {

	@Autowired
	MeterReadingUtil meterReadingUtil;

	@Autowired
	WSCalculationDao wSCalculationDao;

	@Autowired
	MDMSValidator mDMSValidator;

	@Autowired
	WSCalculationValidator wsCalculationValidator;
	
	@Autowired
	WSCalculationService wSCalculationService;

	private ServiceRequestRepository serviceRequestRepository;

	private EnrichmentService enrichmentService;

	@Autowired
	public MeterServicesImpl(ServiceRequestRepository serviceRequestRepository, EnrichmentService enrichmentService) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.enrichmentService = enrichmentService;
	}

	/**
	 * 
	 * @param meterConnectionRequest
	 *            MeterConnectionRequest contains meter reading connection to be
	 *            created
	 * @return List of MeterReading after create
	 */

	@Override
	public List<MeterReading> createMeterReading(MeterConnectionRequest meterConnectionRequest) {
		List<MeterReading> meterReadingsList = new ArrayList<MeterReading>();
		wsCalculationValidator.validateMeterReading(meterConnectionRequest, true);
		//mDMSValidator.validateMasterData(meterConnectionRequest);
		enrichmentService.enrichMeterReadingRequest(meterConnectionRequest);
		
		
		// Object result =
		// serviceRequestRepository.fetchResult(meterReadingUtil.getDemandGenerationCreateURL(),
		// meterConnectionRequest);
		// meterReadingUtil.getMeterReadingDetails(result);
		meterReadingsList.add(meterConnectionRequest.getMeterReading());
		wSCalculationDao.saveWaterConnection(meterConnectionRequest);
		return meterReadingsList;
	}
	
	/**
	 * 
	 * @param meterConnectionSearchCriteria
	 *            MeterConnectionSearchCriteria contains meter reading
	 *            connection criterias to be searched for in the meter
	 *            connection table
	 * @return List of MeterReading after search
	 */


	@Override
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria, RequestInfo requestInfo) {
		wsCalculationValidator.validateMeterReadingSearchCriteria(criteria);
		List<MeterReading> meterReadings = wSCalculationDao.searchMeterReadings(criteria);
		return meterReadings;
	}
}
