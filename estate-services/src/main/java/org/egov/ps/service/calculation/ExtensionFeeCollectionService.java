package org.egov.ps.service.calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.ps.model.ExtensionFee;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class ExtensionFeeCollectionService {

	public List<ExtensionFee> settle(final List<ExtensionFee> demandsToBeSettled, double amountPaying) {
		Collections.sort(demandsToBeSettled);

		List<ExtensionFee> unpaidExtensionFees = demandsToBeSettled.stream().filter(ExtensionFee::isUnPaid)
				.collect(Collectors.toList());

		ArrayList<ExtensionFee> result = new ArrayList<ExtensionFee>();
		for (ExtensionFee extensionFee : unpaidExtensionFees) {
			if (extensionFee.getRemainingDue() <= amountPaying) {
				amountPaying -= extensionFee.getRemainingDue();
				extensionFee.setRemainingDue(0D);
				extensionFee.setIsPaid(true);
				extensionFee.setStatus(PaymentStatusEnum.PAID);
			} else {
				extensionFee.setRemainingDue(extensionFee.getRemainingDue() - amountPaying);
				extensionFee.setStatus(PaymentStatusEnum.UNPAID);
				amountPaying = 0;
				break;
			}
			result.add(extensionFee);
			if (amountPaying == 0) {
				break;
			}
		}
		return unpaidExtensionFees;
	}
}
