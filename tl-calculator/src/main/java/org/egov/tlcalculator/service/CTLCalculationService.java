package org.egov.tlcalculator.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tlcalculator.config.TLCalculatorConfigs;
import org.egov.tlcalculator.kafka.broker.TLCalculatorProducer;
import org.egov.tlcalculator.repository.CTLBillingslabRepository;
import org.egov.tlcalculator.repository.builder.CTLBillingslabQueryBuilder;
import org.egov.tlcalculator.utils.CalculationUtils;
import org.egov.tlcalculator.utils.TLCalculatorConstants;
import org.egov.tlcalculator.web.models.CTLBillingSlab;
import org.egov.tlcalculator.web.models.CTLBillingSlabSearchCriteria;
import org.egov.tlcalculator.web.models.Calculation;
import org.egov.tlcalculator.web.models.CalculationReq;
import org.egov.tlcalculator.web.models.CalculationRes;
import org.egov.tlcalculator.web.models.CalulationCriteria;
import org.egov.tlcalculator.web.models.FeeAndBillingSlabIds;
import org.egov.tlcalculator.web.models.demand.Category;
import org.egov.tlcalculator.web.models.demand.TaxHeadEstimate;
import org.egov.tlcalculator.web.models.enums.CalculationType;
import org.egov.tlcalculator.web.models.tradelicense.EstimatesAndSlabs;
import org.egov.tlcalculator.web.models.tradelicense.TradeLicense;
import org.egov.tlcalculator.web.models.tradelicense.TradeUnit;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CTLCalculationService {


    @Autowired
    private CTLBillingslabRepository repository;

    @Autowired
    private CTLBillingslabQueryBuilder queryBuilder;

    @Autowired
    private TLCalculatorConfigs config;

    @Autowired
    private CalculationUtils utils;

    @Autowired
    private DemandService demandService;

    @Autowired
    private TLCalculatorProducer producer;

    @Autowired
    private MDMSService mdmsService;


    /**
     * Calculates tax estimates and creates demand
     * @param calculationReq The calculationCriteria request
     * @return List of calculations for all applicationNumbers or tradeLicenses in calculationReq
     */
   public List<Calculation> calculate(CalculationReq calculationReq){
       String tenantId = calculationReq.getCalulationCriteria().get(0).getTenantId();
       Object mdmsData = mdmsService.mDMSCall(calculationReq.getRequestInfo(),tenantId);
       List<Calculation> calculations = getCalculation(calculationReq.getRequestInfo(),
               calculationReq.getCalulationCriteria(),mdmsData);
       demandService.generateDemand(calculationReq.getRequestInfo(), calculations, mdmsData, calculationReq.getCalulationCriteria().get(0).getTradelicense().getBusinessService());
       CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
       producer.push(config.getSaveTopic(),calculationRes);
       return calculations;
   }


    /***
     * Calculates tax estimates
     * @param requestInfo The requestInfo of the calculation request
     * @param criterias list of CalculationCriteria containing the tradeLicense or applicationNumber
     * @return  List of calculations for all applicationNumbers or tradeLicenses in criterias
     */
  private List<Calculation> getCalculation(RequestInfo requestInfo, List<CalulationCriteria> criterias,Object mdmsData){
      List<Calculation> calculations = new LinkedList<>();
      for(CalulationCriteria criteria : criterias) {
          TradeLicense license;
          if (criteria.getTradelicense()==null && criteria.getApplicationNumber() != null) {
              license = utils.getTradeLicense(requestInfo, criteria.getApplicationNumber(), criteria.getTenantId());
              criteria.setTradelicense(license);
          }
          EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria,requestInfo,mdmsData);
          List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs.getEstimates();
          FeeAndBillingSlabIds tradeTypeFeeAndBillingSlabIds = estimatesAndSlabs.getTradeTypeFeeAndBillingSlabIds();
//          FeeAndBillingSlabIds accessoryFeeAndBillingSlabIds = null;
//          if(estimatesAndSlabs.getAccessoryFeeAndBillingSlabIds()!=null)
//              accessoryFeeAndBillingSlabIds = estimatesAndSlabs.getAccessoryFeeAndBillingSlabIds();
          Calculation calculation = new Calculation();
          calculation.setTradeLicense(criteria.getTradelicense());
          calculation.setTenantId(criteria.getTenantId());
          calculation.setTaxHeadEstimates(taxHeadEstimates);
          calculation.setTradeTypeBillingIds(tradeTypeFeeAndBillingSlabIds);
//          if(accessoryFeeAndBillingSlabIds!=null)
//              calculation.setAccessoryBillingIds(accessoryFeeAndBillingSlabIds);

          calculations.add(calculation);

      }
      return calculations;
  }


    /**
     * Fetches TaxHeadEstimates
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @param requestInfo The requestInfo of the calculation request
     * @return TaxHeadEstimates and the billingSlabs used to calculate it
     */
    private EstimatesAndSlabs getTaxHeadEstimates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,Object mdmsData){
      List<TaxHeadEstimate> estimates = new LinkedList<>();
      TradeLicense tradeLicense = calulationCriteria.getTradelicense();
      
      EstimatesAndSlabs  estimatesAndSlabs = getBaseFee(calulationCriteria,requestInfo,mdmsData);

      estimates.addAll(estimatesAndSlabs.getEstimates());
      
      try {
		  CTLBillingSlab billingSlab = this.getBillingSlabForCategory(tradeLicense, Category.CHARGES.toString(), null, null);
		  TaxHeadEstimate estimate = new TaxHeadEstimate();
  	      estimate.setEstimateAmount(billingSlab.getRate());
  	      estimate.setCategory(Category.PENALTY);
  	      estimate.setTaxHeadCode(getTaxHeadCode(tradeLicense.getBusinessService(), Category.CHARGES));
  	      if (estimate.getEstimateAmount().compareTo(new BigDecimal(0)) > 0) {
  	    	  estimates.add(estimate);	    	  
  	      }
	  } catch (CustomException customBillingSlabException) {
		  log.error("Ignoring the error", customBillingSlabException);
	  }
      
	  if(tradeLicense.getApplicationType() == TradeLicense.ApplicationTypeEnum.RENEW) {
		  Long now = System.currentTimeMillis();
		  JsonNode oldLicenseValidTo = tradeLicense.getTradeLicenseDetail().getAdditionalDetail().get("oldLicenseValidTo");
			if((oldLicenseValidTo != null) && (oldLicenseValidTo.asLong() < now)){
			  		Date oldLicenseValidDate = new Date(oldLicenseValidTo.asLong());
			  		long monthsDifference = this.getMonthsDifferenceToNow(oldLicenseValidDate);
			  		try {
			  		  CTLBillingSlab taxBillingSlab = this.getBillingSlabForCategory(tradeLicense, Category.PENALTY.toString(), "month", ""+monthsDifference);
			  		  TaxHeadEstimate estimate = new TaxHeadEstimate();
			  	      estimate.setEstimateAmount(taxBillingSlab.getRate());
			  	      estimate.setCategory(Category.PENALTY);
			  	      estimate.setTaxHeadCode(getTaxHeadCode(tradeLicense.getBusinessService(), Category.PENALTY));
			  	      if (estimate.getEstimateAmount().compareTo(new BigDecimal(0)) > 0) {
			  	    	  estimates.add(estimate);	    	  
			  	      }
			  	  } catch (CustomException customBillingSlabException) {
			  		  //Ignore as there is might be no tax slab defined for the given parameters. 
			  		  log.error("Ignoring the error", customBillingSlabException);
			  	  }
			}
	  }
  
	  try {
		  CTLBillingSlab taxBillingSlab = this.getBillingSlabForCategory(tradeLicense, Category.TAX.toString(), null, null);
		  TaxHeadEstimate estimate = new TaxHeadEstimate();
		  BigDecimal tradeUnitTotalFee = estimates.stream().map(_estimate -> _estimate.getEstimateAmount()).reduce(new BigDecimal(0), (a, b) -> a.add(b));
	      estimate.setEstimateAmount(taxBillingSlab.getRate().multiply(tradeUnitTotalFee.divide(new BigDecimal(100))));
	      estimate.setCategory(Category.TAX);
	      estimate.setTaxHeadCode(getTaxHeadCode(tradeLicense.getBusinessService(), Category.TAX));
	      if (estimate.getEstimateAmount().compareTo(new BigDecimal(0)) > 0) {
	    	  estimates.add(estimate);	    	  
	      }
	  } catch (CustomException customBillingSlabException) {
		  //Ignore as there is no tax slab defined for this business service. 
	  }

	  
	  this.addRoundOffTaxHead(estimates, tradeLicense.getBusinessService());
	  
      estimatesAndSlabs.setEstimates(estimates);

      return estimatesAndSlabs;
  }

    private void addRoundOffTaxHead(List<TaxHeadEstimate> estimates, String businessService){
        
    	BigDecimal totalTax = estimates.stream()
    			.map(estimate -> estimate.getEstimateAmount())
    			.reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal decimalValue = totalTax.remainder(BigDecimal.ONE);
        BigDecimal midVal = new BigDecimal(0.5);
        BigDecimal roundOff = BigDecimal.ZERO;

        /*
        * If the decimal amount is greater than 0.5 we subtract it from 1 and put it as roundOff taxHead
        * so as to nullify the decimal eg: If the tax is 12.64 we will add extra tax roundOff taxHead
        * of 0.36 so that the total becomes 13
        * */
        if(decimalValue.compareTo(midVal) > 0)
            roundOff = BigDecimal.ONE.subtract(decimalValue);


        /*
         * If the decimal amount is less than 0.5 we put negative of it as roundOff taxHead
         * so as to nullify the decimal eg: If the tax is 12.36 we will add extra tax roundOff taxHead
         * of -0.36 so that the total becomes 12
         * */
        if(decimalValue.compareTo(midVal) < 0)
            roundOff = decimalValue.negate();

        

        if(roundOff.compareTo(BigDecimal.ZERO)!=0){
        	estimates.add(TaxHeadEstimate.builder()
        			.estimateAmount(roundOff)
        			.category(Category.TAX)
        			.taxHeadCode(businessService+"_ROUNDOFF")
        			.build());
        }
    }

    /**
     * Calculates base tax and cretaes its taxHeadEstimate
     * @param calulationCriteria CalculationCriteria containing the tradeLicense or applicationNumber
     * @param requestInfo The requestInfo of the calculation request
     * @return BaseTax taxHeadEstimate and billingSlabs used to calculate it
     */
  @SuppressWarnings("rawtypes")
private EstimatesAndSlabs getBaseFee(CalulationCriteria calulationCriteria, RequestInfo requestInfo,Object mdmsData){
      TradeLicense license = calulationCriteria.getTradelicense();
      EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();

      Map calculationTypeMap = mdmsService.getCalculationType(requestInfo,license,mdmsData);
      String tradeUnitCalculationType = (String)calculationTypeMap.get(TLCalculatorConstants.MDMS_CALCULATIONTYPE_TRADETYPE);
      String accessoryCalculationType  = (String)calculationTypeMap.get(TLCalculatorConstants.MDMS_CALCULATIONTYPE_ACCESSORY);

      FeeAndBillingSlabIds tradeTypeFeeAndBillingSlabIds = getTradeUnitFeeAndBillingSlabIds(license,CalculationType
              .fromValue(tradeUnitCalculationType));
      BigDecimal tradeUnitFee = tradeTypeFeeAndBillingSlabIds.getFee();

      estimatesAndSlabs.setTradeTypeFeeAndBillingSlabIds(tradeTypeFeeAndBillingSlabIds);

      TaxHeadEstimate estimate = new TaxHeadEstimate();
      BigDecimal totalTax = tradeUnitFee;

      if(totalTax.compareTo(BigDecimal.ZERO)==-1)
          throw new CustomException("INVALID AMOUNT","Tax amount is negative");

      estimate.setEstimateAmount(totalTax);
      estimate.setCategory(Category.FEE);
      estimate.setTaxHeadCode(getTaxHeadCode(license.getBusinessService(), Category.FEE));

      estimatesAndSlabs.setEstimates(Collections.singletonList(estimate));

      return estimatesAndSlabs;
  }

  
    /**
     * @param license TradeLicense for which fee has to be calculated
     * @param calculationType Calculation logic to be used
     * @return TradeUnit Fee and billingSlab used to calculate it
     */
  private FeeAndBillingSlabIds getTradeUnitFeeAndBillingSlabIds(TradeLicense license, CalculationType calculationType){

      List<BigDecimal> tradeUnitFees = new LinkedList<>();
      List<TradeUnit> tradeUnits = license.getTradeLicenseDetail().getTradeUnits();
      List<String> billingSlabIds = new LinkedList<>();
      int i = 0;
      for(TradeUnit tradeUnit : tradeUnits) { 
    	  if(tradeUnit.getActive()) {
              BigDecimal tradeUnitTotalFee = new BigDecimal(0);
    		  
    		  //Calculate Fee
    		  CTLBillingSlab billingSlab = this.getBillingSlabForCategory(license, Category.FEE.toString(), tradeUnit.getUom(), tradeUnit.getUomValue());
    		  billingSlabIds.add(billingSlab.getId()+"|"+i+"|"+tradeUnit.getId());
			  tradeUnitFees.add(billingSlab.getRate());
			  tradeUnitTotalFee = tradeUnitTotalFee.add(billingSlab.getRate());
    		  i++;
    	  }
      }

      BigDecimal tradeUnitTotalFee = getTotalFee(tradeUnitFees,calculationType);

      FeeAndBillingSlabIds feeAndBillingSlabIds = new FeeAndBillingSlabIds();
      feeAndBillingSlabIds.setFee(tradeUnitTotalFee);
      feeAndBillingSlabIds.setBillingSlabIds(billingSlabIds);
      feeAndBillingSlabIds.setId(UUID.randomUUID().toString());

      return feeAndBillingSlabIds;
  }
  
  private CTLBillingSlab getBillingSlabForCategory(TradeLicense license, String category, String uom, String uomValue) {
	  List<Object> preparedStmtList = new ArrayList<>();
      CTLBillingSlabSearchCriteria searchCriteria = new CTLBillingSlabSearchCriteria();
      searchCriteria.setTenantId(license.getTenantId());
      searchCriteria.setFeeType(category);
      searchCriteria.setApplicationType(license.getApplicationType().toString());
      searchCriteria.setBusinessService(license.getBusinessService());
      if(uom != null && uomValue != null)
      {
          searchCriteria.setUomValue(Double.parseDouble(uomValue));
          searchCriteria.setUom(uom);
      }
      // Call the Search
      String query = queryBuilder.getSearchQuery(searchCriteria, preparedStmtList);
      log.info("query "+query);
      log.info("preparedStmtList "+preparedStmtList.toString());
      List<CTLBillingSlab> billingSlabs = repository.getDataFromDB(query, preparedStmtList);
      if(billingSlabs.size()>1)
          throw new CustomException("BILLINGSLAB ERROR","Found multiple BillingSlabs for the given BusinessService");
      if(CollectionUtils.isEmpty(billingSlabs))
          throw new CustomException("BILLINGSLAB ERROR","No BillingSlab Found for the given BusinessService");
      return billingSlabs.get(0);
  }

    /**
     * Calculates total fee of by applying logic on list based on calculationType
     * @param fees List of fee for different tradeType or accessories
     * @param calculationType Calculation logic to be used
     * @return Total Fee
     */
  private BigDecimal getTotalFee(List<BigDecimal> fees,CalculationType calculationType){
      BigDecimal totalFee = BigDecimal.ZERO;
      //Summation
      if(calculationType.equals(CalculationType.SUM))
          totalFee = fees.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

      //Average
      if(calculationType.equals(CalculationType.AVERAGE))
          totalFee = (fees.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                  .divide(new BigDecimal(fees.size()))).setScale(2,2);

      //Max
      if(calculationType.equals(CalculationType.MAX))
          totalFee = fees.stream().reduce(BigDecimal::max).get();

      //Min
      if(calculationType.equals(CalculationType.MIN))
          totalFee = fees.stream().reduce(BigDecimal::min).get();

       return totalFee;
  }

  private String getTaxHeadCode(String businessService, Category category) {
	  return String.format("%s_%s", businessService, category.toString());
  }
  
  public boolean isAllowedBusinessService(String businessService) {
	  return Arrays.asList(config.getAllowedBusinessServices().split(",")).contains(businessService);
  }
  
  private long getMonthsDifferenceToNow(Date dateFrom) {
	  LocalDate localDateFrom = dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	  LocalDate today = LocalDate.now();
	  Period age = Period.between(localDateFrom, today); 
	  return age.getYears() * 12 + age.getMonths();
  }
}
