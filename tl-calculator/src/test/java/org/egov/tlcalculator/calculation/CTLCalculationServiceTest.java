package org.egov.tlcalculator.calculation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.egov.TLCalculatorApp;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tlcalculator.config.TLCalculatorConfigs;
import org.egov.tlcalculator.kafka.broker.TLCalculatorProducer;
import org.egov.tlcalculator.repository.CTLBillingslabRepository;
import org.egov.tlcalculator.repository.builder.CTLBillingslabQueryBuilder;
import org.egov.tlcalculator.service.CTLCalculationService;
import org.egov.tlcalculator.service.DemandService;
import org.egov.tlcalculator.service.MDMSService;
import org.egov.tlcalculator.utils.CalculationUtils;
import org.egov.tlcalculator.utils.TLCalculatorConstants;
import org.egov.tlcalculator.web.models.Calculation;
import org.egov.tlcalculator.web.models.CalculationReq;
import org.egov.tlcalculator.web.models.FeeAndBillingSlabIds;
import org.egov.tlcalculator.web.models.demand.Category;
import org.egov.tlcalculator.web.models.demand.TaxHeadEstimate;
import org.egov.tlcalculator.web.models.tradelicense.TradeLicense;
import org.egov.tracer.model.CustomException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TLCalculatorApp.class)

public class CTLCalculationServiceTest {

	private String timeZone ="UTC";

	@Autowired
	private CTLCalculationService ctlCalculationService ;

	@Autowired
	private DemandService demandService;

	@Autowired
	private TLCalculatorProducer producer;

	@Autowired
	private MDMSService mdmsService;

	private ObjectMapper objectMapper ;

	@Autowired
	private TLCalculatorConfigs config;
	
	Object mdms = "{ResponseInfo=null, "
			+ "MdmsRes={egf-master={FinancialYear=[{id=9, tenantId=ch, finYearRange=2020-21, name=2020-21, code=2020-21, startingDate=1585679400000, endingDate=1617215399000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=TL},"
			+ " {id=8, tenantId=ch, finYearRange=2019-20, name=2019-20, code=2019-20, startingDate=1554076800000, endingDate=1585699199000, active=true, isActiveForPosting=false, isClosed=false, transferClosingBalance=false, module=TL}, "
			+ "{id=6, tenantId=ch, finYearRange=2019-20, name=2019-20, code=2019-20, startingDate=1554076800000, endingDate=1585699199000, active=true, isActiveForPosting=false, isClosed=false, transferClosingBalance=false, module=PT},"
			+ " {id=5, tenantId=ch, finYearRange=2018-19, name=2018-19, code=2018-19, startingDate=1522540800000, endingDate=1554076799000, active=true, isActiveForPosting=false, isClosed=false, transferClosingBalance=false, module=PT}, "
			+ "{id=4, tenantId=ch, finYearRange=2017-18, name=2017-18, code=2017-18, startingDate=1491004800000, endingDate=1522540799000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=PT}, "
			+ "{id=3, tenantId=ch, finYearRange=2016-17, name=2016-17, code=2016-17, startingDate=1459468800000, endingDate=1491004799000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=PT}, "
			+ "{id=2, tenantId=ch, finYearRange=2015-16, name=2015-16, code=2015-16, startingDate=1427846400000, endingDate=1459468799000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=PT},"
			+ " {id=1, tenantId=ch, finYearRange=2014-15, name=2014-15, code=2014-15, startingDate=1396310400000, endingDate=1427846399000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=PT}, "
			+ "{id=11, tenantId=ch, finYearRange=2013-14, name=2013-14, code=2013-14, startingDate=1364774400000, endingDate=1396310399000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=PT},"
			+ " {id=10, tenantId=ch, finYearRange=2019-20, name=2019-20, code=2019-20, startingDate=1554057001000, endingDate=1585593001000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=OPMS}, "
			+ "{id=9, tenantId=ch, finYearRange=2020-21, name=2020-21, code=2020-21, startingDate=1585679400000, endingDate=1617129000000, active=true, isActiveForPosting=true, isClosed=false, transferClosingBalance=false, module=OPMS},"
			+ " {id=50, tenantId=ch, finYearRange=2020-21, name=2020-21, code=2020-21, startingDate=1585679409000, endingDate=1617215399000, active=true, isActiveForPosting=false, isClosed=false, transferClosingBalance=false, module=TL},"
			+ " {id=51, tenantId=ch, finYearRange=2021-22, name=2021-22, code=2021-22, startingDate=1617215409000, endingDate=1648751399000, active=true, isActiveForPosting=false, isClosed=false, transferClosingBalance=false, module=TL}]},"
			+ " TradeLicense={}}}";

	@Before
	public void setup() {
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
		objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).setTimeZone(TimeZone.getTimeZone(timeZone));
		Mockito.when(mdmsService.mDMSCall(Mockito.any(RequestInfo.class),any(String.class))).thenReturn(mdms);
		Mockito.when(mdmsService.getCalculationType(Mockito.any(RequestInfo.class), any(TradeLicense.class), any(Object.class))).thenReturn(defaultMap());
		Mockito.doNothing().when(demandService).generateDemand(Mockito.any(RequestInfo.class),Matchers.anyListOf(Calculation.class),any(Object.class),any(String.class));

	}
	
	/**
	 * Test for new RegisterCertificate application calculation
	 * 5 YEARS
	 * @throws Exception
	 */
	
	@Test
	public void testRegisterCertificateCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("registerCertificateRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
		assertFalse(calculationList.isEmpty());
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(0).getEstimateAmount().doubleValue(), 600.00,0);  
	}
	
	/**
	 * Test for new DrivingLicense Certificate application calculation
	 * 15 YEARS
	 * @throws Exception
	 */
	
	@Test
	public void testDrivingLicenseCertificateCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("drivingLicenseRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
		assertFalse(calculationList.isEmpty());
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(0).getEstimateAmount().doubleValue(), 300.00,0);  
	}	

	/**
	 * Test for new Dhobighat application calculation
	 * 1 YEAR
	 * @throws Exception
	 */
	
	@Test
	public void testDhobiGhatCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("getDhobighatCalculationRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
		assertFalse(calculationList.isEmpty());
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(0).getEstimateAmount().doubleValue(), 15.00,0);  
	}
	
	/**
	 * Test for new OldBookMarket application calculation
	 * 5 YEAR
	 * 18% Tax
	 * roundoff value
	 * @throws Exception
	 */
	
	@Test
	public void testOldBookMarketCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("oldBookMarketRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
		assertFalse(calculationList.isEmpty());
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(0).getEstimateAmount().doubleValue(), 4038.00,0);
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(1).getEstimateAmount().doubleValue(), 726.84,0);
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(2).getEstimateAmount().doubleValue(), 0.16,0);
		
	}
	
	/**
	 * Test for Renewal RegisterCertificate application calculation
	 * 10 YEARS
	 * old license validity < 3
	 * @throws Exception
	 */
	
	@Test
	public void testRenewRegisterCertificateCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("registerCertificateRenewRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
		assertFalse(calculationList.isEmpty());
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(0).getEstimateAmount().doubleValue(), 1000.00,0);
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(1).getEstimateAmount().doubleValue(), 100.00,0);
	}
	
	/**
	 * Test for new DrivingLicense Certificate application calculation
	 * 5 YEARS
	 * 3 < old license validity < 6
	 * @throws Exception
	 */
	
	@Test
	public void testRenewDrivingLicenseCertificateCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("drivingLicenseRenewRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
		assertFalse(calculationList.isEmpty());
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(0).getEstimateAmount().doubleValue(), 200.00,0);
		assertEquals(calculationList.get(0).getTaxHeadEstimates().get(1).getEstimateAmount().doubleValue(), 150.00,0);
	}
	
	
	/**
	 * Test for Bad Request
	 * Invalid UOM value
	 * @throws Exception
	 */
	
	@Test(expected= CustomException.class) 
	public void testBadRequestCalculation() throws Exception {
		CalculationReq calculationReq = this.objectMapper.readValue(getFileContents("badRequest.json"), CalculationReq.class);
		List<Calculation> calculationList = ctlCalculationService.calculate(calculationReq);
	}
	

	
	private String getFileContents(String fileName) {
		try {
			return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileName), "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map defaultMap(){
		Map defaultMap = new HashMap();
		defaultMap.put(TLCalculatorConstants.MDMS_CALCULATIONTYPE_TRADETYPE,config.getDefaultTradeUnitCalculationType());
		defaultMap.put(TLCalculatorConstants.MDMS_CALCULATIONTYPE_ACCESSORY,config.getDefaultAccessoryCalculationType());
		return defaultMap;
	}
}

