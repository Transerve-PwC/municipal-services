package org.egov.ps.service.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class PenaltyCollectionService {

	public List<PropertyPenalty> settle(final List<PropertyPenalty> demandsToBeSettled, double ammountPaying) {
		Collections.sort(demandsToBeSettled);

		ArrayList<PropertyPenalty> newerDemands = new ArrayList<PropertyPenalty>();
		for (PropertyPenalty demand : demandsToBeSettled) {
			if (!demand.getIsPaid()) {
				newerDemands.add(demand);
			}
		}

		ArrayList<PropertyPenalty> result = new ArrayList<PropertyPenalty>();
		for (PropertyPenalty demand : newerDemands) {

			if (ammountPaying <= 0) {
				break;
			}
			if (demand.getStatus().equals(PaymentStatusEnum.PARTIALLY_PAID)) {
				ammountPaying = ammountPaying - demand.getRemainingPenaltyDue();
				demand.setRemainingPenaltyDue(demand.getRemainingPenaltyDue() - ammountPaying);
				if (demand.getRemainingPenaltyDue() <= 0) {
					demand.setRemainingPenaltyDue(00.00);
					demand.setIsPaid(true);
					demand.setStatus(PaymentStatusEnum.PAID);
				} else {
					demand.setStatus(PaymentStatusEnum.PARTIALLY_PAID);
				}
			} else if (demand.getPenaltyAmount() <= ammountPaying) {
				ammountPaying = ammountPaying - demand.getPenaltyAmount();
				demand.setRemainingPenaltyDue(00.00);
				demand.setIsPaid(true);
				demand.setStatus(PaymentStatusEnum.PAID);
			} else {
				demand.setRemainingPenaltyDue(demand.getPenaltyAmount() - ammountPaying);
				if (demand.getRemainingPenaltyDue() == 0) {
					demand.setIsPaid(true);
					demand.setStatus(PaymentStatusEnum.PAID);
				} else {
					demand.setStatus(PaymentStatusEnum.PARTIALLY_PAID);
				}
			}
			result.add(demand);
		}
		return newerDemands;
	}

}
