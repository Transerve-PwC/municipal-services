package org.egov.wscalculation.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.tracer.model.CustomException;
import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.model.BillGeneration;
import org.egov.wscalculation.model.BillGenerationRequest;
import org.egov.wscalculation.model.Calculation;
import org.egov.wscalculation.model.Category;
import org.egov.wscalculation.model.TaxHeadEstimate;
import org.egov.wscalculation.model.TaxHeadMaster;
import org.egov.wscalculation.model.WaterConnection;
import org.egov.wscalculation.repository.BillingRepository;
import org.egov.wscalculation.util.CalculatorUtil;
import org.egov.wscalculation.util.WSCalculationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class BillingService {

	@Autowired
	private BillingRepository billingRepository;

	@Autowired
	private MasterDataService masterDataService;

	@Autowired
	private WSCalculationUtil wSCalculationUtil;

	@Autowired
	private CalculatorUtil calculatorUtil;

	@Autowired
	private DemandService demandService;



	


	public List<BillGeneration> getBillingEstimation(BillGenerationRequest billGenerationRequest) {
		BillGeneration bill = billingRepository.getBillingEstimation(billGenerationRequest.getBillGeneration().getConsumerCode());
		Map<String, Object> masterMap = masterDataService.loadMasterData(billGenerationRequest.getRequestInfo(),
				billGenerationRequest.getBillGeneration().getTenantId());
		List<BillGeneration> billList = new ArrayList<BillGeneration>();
		try {
		String paymentMode =billGenerationRequest.getBillGeneration().getPaymentMode();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
		Date	todayDate = dateFormatter.parse(dateFormatter.format(new Date() ));
		Date	dueDateCheque = dateFormatter.parse(dateFormatter.format(bill.getDueDateCheque()));
		Date	dueDateCash = dateFormatter.parse(dateFormatter.format(bill.getDueDateCash()));
		
		if(paymentMode.equalsIgnoreCase(WSCalculationConstant.payment_cheque)) {
			if(todayDate.compareTo(dueDateCheque) > 0) {
				
           enrichWaterCalculation(billGenerationRequest,bill,masterMap,true);

				}
			  enrichWaterCalculation(billGenerationRequest,bill,masterMap,false);
			
				
			}
			else {
				if(todayDate.compareTo(dueDateCash) > 0) {
					
					  enrichWaterCalculation(billGenerationRequest,bill,masterMap,true);
					
				}else {
					  enrichWaterCalculation(billGenerationRequest,bill,masterMap,false);
				}
}
		if(billGenerationRequest.getBillGeneration().isGenerateDemand()) {
			List<Calculation> calculations = new ArrayList<Calculation>();
			calculations.add(bill.getCalculation());

			demandService.generateDemand(billGenerationRequest.getRequestInfo(), calculations, masterMap, true);
			
		}
		 billList.add(bill);
		

		}catch(Exception e) {
			throw new CustomException("ERROR_GETTING_ESTIMATION" , e.getMessage());
		}
		return billList;
	}

	private void enrichWaterCalculation(BillGenerationRequest billGenerationRequest, BillGeneration bill, Map<String, Object> masterMap, boolean isPenalty) {
		
		
		WaterConnection connection = calculatorUtil.getWaterConnection(billGenerationRequest.getRequestInfo(),
				bill.getConsumerCode(), billGenerationRequest.getBillGeneration().getTenantId());
		
		if (connection == null) {
			throw new CustomException("CONNECTION_NOT_FOUND","water connection not found for consumer id ");
		} else {

			List<TaxHeadEstimate> estimates = new ArrayList<>();

			estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.WS_CHARGE)
					.estimateAmount(new BigDecimal(bill.getNetAmount())).build());

			if(isPenalty) {	
				 estimates.add(TaxHeadEstimate.builder().taxHeadCode(WSCalculationConstant.
				  WS_ADHOC_PENALTY) .estimateAmount(new BigDecimal(bill.getSurcharge())).build());
			}
			Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
			estimatesAndBillingSlabs.put("estimates", estimates);
			Calculation calculation = getCalculation(estimatesAndBillingSlabs, masterMap, bill, connection);

			enrichBillingPeriod(bill, masterMap);
			bill.setCalculation(calculation);
		
	}

}
	

	public Map<String, Object> enrichBillingPeriod(BillGeneration billGeneration, Map<String, Object> masterMap) {

		Map<String, Object> billingPeriod = new HashMap<>();

		billingPeriod.put(WSCalculationConstant.STARTING_DATE_APPLICABLES, billGeneration.getFromDate());
		billingPeriod.put(WSCalculationConstant.ENDING_DATE_APPLICABLES, billGeneration.getToDate());
		masterMap.put(WSCalculationConstant.BILLING_PERIOD, billingPeriod);
		return masterMap;
	}
	
	@SuppressWarnings("unchecked")
	private Calculation getCalculation(Map<String, List> estimatesAndBillingSlabs, Map<String, Object> masterMap,
			BillGeneration billGeneration, WaterConnection connection) {
		List<TaxHeadEstimate> estimates = estimatesAndBillingSlabs.get("estimates");
		Map<String, Category> taxHeadCategoryMap = new HashMap<String, Category>();
		for (TaxHeadMaster master : (List<TaxHeadMaster>) masterMap
				.get(WSCalculationConstant.TAXHEADMASTER_MASTER_KEY)) {
			if (null != master.getCategory()) {
				taxHeadCategoryMap.put(master.getCode(), master.getCategory());
			} else {
				log.info("Category is null in TaxHeadMaster for code {}", master.getCategory());
			}
		}

		BigDecimal taxAmt = BigDecimal.ZERO;
		BigDecimal waterCharge = BigDecimal.ZERO;
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal exemption = BigDecimal.ZERO;
		BigDecimal rebate = BigDecimal.ZERO;
		BigDecimal fee = BigDecimal.ZERO;

		for (TaxHeadEstimate estimate : estimates) {

			Category category = taxHeadCategoryMap.get(estimate.getTaxHeadCode());
			estimate.setCategory(category);

			switch (category) {

			case CHARGES:
				waterCharge = waterCharge.add(estimate.getEstimateAmount());
				break;

			case PENALTY:
				penalty = penalty.add(estimate.getEstimateAmount());
				break;

			case REBATE:
				rebate = rebate.add(estimate.getEstimateAmount());
				break;

			case EXEMPTION:
				exemption = exemption.add(estimate.getEstimateAmount());
				break;
			case FEE:
				fee = fee.add(estimate.getEstimateAmount());
				break;
			default:
				taxAmt = taxAmt.add(estimate.getEstimateAmount());
				break;
			}
		}

		BigDecimal totalAmount = taxAmt.add(penalty).add(rebate).add(exemption).add(waterCharge).add(fee);

		Calculation cal = Calculation.builder().totalAmount(totalAmount).taxAmount(taxAmt).penalty(penalty)
				.exemption(exemption).charge(waterCharge).fee(fee).rebate(rebate).tenantId(billGeneration.getTenantId())
				.taxHeadEstimates(estimates).connectionNo(billGeneration.getConsumerCode()).waterConnection(connection)
				.build();

		return cal;
	}
}
