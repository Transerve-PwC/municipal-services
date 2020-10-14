package org.egov.ps.model.calculation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;

public class EstateCalculation {
	public static void main(String[] args) {

	}

	public void settle(List<EstateDemand> lstDemandProcess, EstatePayment payment) {
		double rentReceived = payment.getRentReceived();
		for (EstateDemand demandProcess : lstDemandProcess) {
			if (demandProcess.getRent() + demandProcess.getGst() <= rentReceived) {
				demandProcess.setCollectedRent(demandProcess.getRent());
				demandProcess.setCollectedGST(Double.parseDouble(String.valueOf(demandProcess.getGst())));
				rentReceived -= demandProcess.getRent() + demandProcess.getGst();

			} else {
				// some calculation
			}
			// setting of penalty and GST penalty

		}
		for (EstateDemand demandProcess : lstDemandProcess) {
			if (demandProcess.getPenaltyInterest() + demandProcess.getGstInterest() <= rentReceived) {
				demandProcess.setGstInterest(0.0);
				demandProcess.setPenaltyInterest(0.0);

				/*
				 * Removed this column from model collectedInterestPenalty, collectedGSTPenalty
				 */
				demandProcess.setPenaltyInterest(demandProcess.getGstInterest());
				demandProcess.setPenaltyInterest(demandProcess.getGstInterest());
			} else {
				// 50-50
				double paybleAmount = rentReceived / 2;
				if (paybleAmount > demandProcess.getPenaltyInterest()) {
					demandProcess.setPenaltyInterest(paybleAmount - demandProcess.getPenaltyInterest());
					paybleAmount = paybleAmount - (paybleAmount + demandProcess.getPenaltyInterest());
				} else {
					demandProcess.setPenaltyInterest(demandProcess.getPenaltyInterest() - paybleAmount);
					paybleAmount = demandProcess.getPenaltyInterest() - paybleAmount;
				}

			}

		}
	}

	public void settle(List<EstateDemand> lstDemands, List<EstatePayment> lstPayments) {

		for (EstatePayment payment : lstPayments) {
			List<EstateDemand> lstDemandProcess = new ArrayList<EstateDemand>();
			Date paymentDate = new Date(payment.getReceiptDate());
			for (EstateDemand demand : lstDemands) {
				Date demandDate = new Date(demand.getDemandDate());
				if (demandDate.compareTo(paymentDate) <= 0) {
					lstDemandProcess.add(demand);
				}
			}
		}
	}

}
