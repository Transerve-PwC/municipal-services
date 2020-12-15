package org.egov.ps.service.calculation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.ManiMajraPayment;
import org.egov.ps.web.contracts.ManiMajraRentCollection;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class ManiMajraRentCollectionService implements IManiMajraRentCollectionService {

	@Override
	public List<ManiMajraRentCollection> settle(List<ManiMajraDemand> demandsToBeSettled,
			List<ManiMajraPayment> payments, boolean isMonthly) {

		Collections.sort(demandsToBeSettled);
		Collections.sort(payments);

		/**
		 * Don't process payments that are already processed.
		 */
		List<ManiMajraPayment> paymentsToBeSettled = payments.stream().filter(payment -> !payment.isProcessed())
				.collect(Collectors.toList());

		/**
		 * Settle unprocessed payments
		 */
		List<ManiMajraRentCollection> collections = paymentsToBeSettled.stream().map(payment -> {
			return settlePayment(demandsToBeSettled, payment, isMonthly);
		}).flatMap(Collection::stream).collect(Collectors.toList());

		return collections;
	}

	private List<ManiMajraRentCollection> settlePayment(final List<ManiMajraDemand> demandsToBeSettled,
			final ManiMajraPayment payment, boolean isMonthly) {

		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		List<ManiMajraDemand> demands = demandsToBeSettled.stream()
				.filter(demand -> demand.isUnPaid() && demand.getGenerationDate() <= payment.getDateOfPayment())
				.collect(Collectors.toList());

		double effectiveAmount;
		double gstAmount;

		if (isMonthly) {
			effectiveAmount = payment.getAmountPaid();
			gstAmount = extractGst(effectiveAmount);
			effectiveAmount = effectiveAmount - gstAmount;
		} else {
			effectiveAmount = payment.getAmountPaid();
		}

		/**
		 * deduct payment from each demand
		 * 
		 * set status as per payments to demand
		 * 
		 * return collections
		 */
		ArrayList<ManiMajraRentCollection> collections = new ArrayList<ManiMajraRentCollection>(demands.size());
		for (ManiMajraDemand unPaidDemand : demands) {
			double rentWithGst = unPaidDemand.getRent() + unPaidDemand.getGst();
			if (rentWithGst <= effectiveAmount) {
				effectiveAmount -= rentWithGst;
				unPaidDemand.setStatus(PaymentStatusEnum.PAID);
				unPaidDemand.setCollectionPrincipal(rentWithGst);
				unPaidDemand.setCollectedRent(unPaidDemand.getRent());
				unPaidDemand.setCollectedGST(unPaidDemand.getGst());
			} else {
				break;
			}

			collections.add(ManiMajraRentCollection.builder().rentCollected(unPaidDemand.getRent())
					.gstCollected(unPaidDemand.getGst()).collectedAt(payment.getPaymentDate())
					.demandId(unPaidDemand.getId()).paymentId(payment.getId()).build());
		}

		/**
		 * Mark payment as processed.
		 */
		payment.setProcessed(true);

		return collections;

	}

	public double extractGst(double effectiveAmount) {
//		TODO: get it from mdms
		double gstPercentage = 18;
		return (gstPercentage / 100) * effectiveAmount;
	}

}
