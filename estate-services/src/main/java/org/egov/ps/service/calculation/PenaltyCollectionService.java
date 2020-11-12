package org.egov.ps.service.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class PenaltyCollectionService {

	public List<PropertyPenalty> settle(final List<PropertyPenalty> demandsToBeSettled, double amountPaying) {
		Collections.sort(demandsToBeSettled);

		List<PropertyPenalty> unpaidPenalties = demandsToBeSettled.stream().filter(PropertyPenalty::isUnPaid)
				.collect(Collectors.toList());

		ArrayList<PropertyPenalty> result = new ArrayList<PropertyPenalty>();
		for (PropertyPenalty penalty : unpaidPenalties) {
			if (penalty.getRemainingPenaltyDue() <= amountPaying) {
				amountPaying -= penalty.getRemainingPenaltyDue();
				penalty.setRemainingPenaltyDue(0D);
				penalty.setIsPaid(true);
				penalty.setStatus(PaymentStatusEnum.PAID);
			} else {
				amountPaying = 0;
				penalty.setRemainingPenaltyDue(penalty.getRemainingPenaltyDue() - amountPaying);
				penalty.setStatus(PaymentStatusEnum.UNPAID);
				break;
			}
			result.add(penalty);
			if (amountPaying == 0) {
				break;
			}
		}
		return unpaidPenalties;
	}
}
