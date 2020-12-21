package org.egov.ps.service.calculation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.ManiMajraAccountStatement;
import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.ManiMajraPayment;
import org.egov.ps.web.contracts.ManiMajraRentCollection;
import org.egov.ps.web.contracts.ManiMajraRentSummary;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.egov.ps.web.contracts.ManiMajraAccountStatement.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManiMajraRentCollectionService implements IManiMajraRentCollectionService {

	@Autowired
	Util util;

	@Override
	public List<ManiMajraRentCollection> settle(List<ManiMajraDemand> demandsToBeSettled,
			List<ManiMajraPayment> payments, EstateAccount account, boolean isMonthly) {

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
			return settlePayment(demandsToBeSettled, payment, account, isMonthly);
		}).flatMap(Collection::stream).collect(Collectors.toList());

		return collections;
	}

	private List<ManiMajraRentCollection> settlePayment(final List<ManiMajraDemand> demandsToBeSettled,
			final ManiMajraPayment payment, EstateAccount account, boolean isMonthly) {

		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		List<ManiMajraDemand> demands = demandsToBeSettled.stream()
				.filter(demand -> demand.isUnPaid() && demand.getGenerationDate() <= payment.getDateOfPayment())
				.collect(Collectors.toList());

		double effectiveAmount;
		double gstAmount;

		if (isMonthly) {
			effectiveAmount = payment.getAmountPaid() + account.getRemainingAmount();
			// TODO: gst/tax and values should be from mdms
			gstAmount = util.extractGst(effectiveAmount);
			effectiveAmount = effectiveAmount - gstAmount;
		} else {
			effectiveAmount = payment.getAmountPaid() + account.getRemainingAmount();
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
				account.setRemainingAmount(effectiveAmount);
				account.setRemainingSince(payment.getPaymentDate());
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

	@Override
	public List<ManiMajraAccountStatement> getAccountStatement(List<ManiMajraDemand> demands,
			List<ManiMajraPayment> payments, Long fromDateTimestamp, Long toDateTimestamp) {

		if (null == payments)
			payments = Collections.emptyList();
		if (demands == null)
			demands = Collections.emptyList();

		long endTimestamp = toDateTimestamp == null ? System.currentTimeMillis() : toDateTimestamp.longValue();
		demands = demands.stream().filter(demand -> demand.getGenerationDate() <= endTimestamp)
				.collect(Collectors.toList());

		payments = payments.stream().filter(payment -> payment.getRentReceived() > 0)
				.filter(p -> p.getPaymentDate() <= endTimestamp).collect(Collectors.toList());
		Collections.sort(demands);
		Collections.sort(payments);

		List<ManiMajraAccountStatement> accountStatementItems = new ArrayList<ManiMajraAccountStatement>();
		EstateAccount rentAccount = EstateAccount.builder().remainingAmount(0D).build();
		List<ManiMajraDemand> demandsToBeSettled = new ArrayList<ManiMajraDemand>(demands.size());
		Iterator<ManiMajraDemand> demandIterator = demands.iterator();
		Iterator<ManiMajraPayment> paymentIterator = payments.iterator();
		ManiMajraDemand currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
		ManiMajraPayment currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
		while (true) {
			boolean reachedLast = false;
			ManiMajraRentSummary rentSummary;
			ManiMajraAccountStatement statement = ManiMajraAccountStatement.builder().build();

			// no demand and payment remaining , calculate the summary
			if (currentDemand == null && currentPayment == null) {
				rentSummary = getSummaryForDemand(rentAccount, demandsToBeSettled,
						ManiMajraDemand.builder().generationDate(endTimestamp).collectedRent(0D).build(), statement);
				reachedLast = true;
			}
			// no demand remaining
			else if (currentDemand == null) {
				rentSummary = calculateSummaryForPayment(rentAccount, demandsToBeSettled, currentPayment, statement);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			}
			// no payment remaining
			else if (currentPayment == null) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(rentAccount, demandsToBeSettled, currentDemand, statement);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else if (currentDemand.getGenerationDate() <= currentPayment.getPaymentDate()) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(rentAccount, demandsToBeSettled, currentDemand, statement);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else {
				rentSummary = calculateSummaryForPayment(rentAccount, demandsToBeSettled, currentPayment, statement);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			}
			statement.setRemainingPrincipal(rentSummary.getBalanceRent());
			// statement.setRemainingInterest(rentSummary.getBalanceRentPenalty());
			statement.setRemainingBalance(rentSummary.getBalanceAmount());
			statement.setRemainingGST(rentSummary.getBalanceGST());
			// statement.setReceiptNo(currentPayment!=null?currentPayment.getReceiptNo():"");
			statement.setRent(rentSummary.getRent());
			statement.setCollectedRent(rentSummary.getCollectedRent());
			statement.setGst(rentSummary.getGst());
			statement.setCollectedGST(rentSummary.getCollectedGST());
			accountStatementItems.add(statement);
			if (reachedLast) {
				break;
			}
		}
		if (fromDateTimestamp == null) {
			return accountStatementItems;
		} else {
			List<LocalDate> dates = accountStatementItems.stream()
					.map(stmt -> Instant.ofEpochMilli(stmt.getDate()).atZone(ZoneId.systemDefault()).toLocalDate())
					.collect(Collectors.toList());

			if (!dates.contains(Instant.ofEpochMilli(fromDateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate())) {
				Optional<Long> afterDate = accountStatementItems.stream().map(stmt -> stmt.getDate())
						.filter(stmt -> fromDateTimestamp < stmt).findFirst();
				return accountStatementItems.stream()
						.filter(statementItem -> statementItem.getDate() >= afterDate.get().longValue())
						.collect(Collectors.toList());
			}

			Long fromDate = accountStatementItems.stream().map(stmt -> stmt.getDate())
					.filter(date -> Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().equals(
							Instant.ofEpochMilli(fromDateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()))
					.findFirst().get();
			return accountStatementItems.stream()
					.filter(statementItem -> statementItem.getDate() >= fromDate.longValue())
					.collect(Collectors.toList());
		}
		// return accountStatementItems;
	}

	private ManiMajraRentSummary calculateSummaryForPayment(EstateAccount rentAccount,
			List<ManiMajraDemand> demandsToBeSettled, ManiMajraPayment currentPayment,
			ManiMajraAccountStatement statement) {
		currentPayment = this.clonePayment(currentPayment);
		// TODO: Update hardcode boolean 'ture' with the getProperty method
		this.settle(demandsToBeSettled, Collections.singletonList(currentPayment), rentAccount, true);
		ManiMajraRentSummary rentSummary = calculateRentSummaryAtPayment(demandsToBeSettled, rentAccount,
				currentPayment.getPaymentDate());
		statement.setDate(currentPayment.getPaymentDate());
		statement.setAmount(currentPayment.getRentReceived());
		statement.setType(Type.C);
		statement.setReceiptNo(currentPayment.getReceiptNo());
		return rentSummary;
	}

	private ManiMajraRentSummary calculateRentSummaryAtPayment(List<ManiMajraDemand> demands, EstateAccount rentAccount,
			Long paymentDate) {
		if (demands == null)
			demands = Collections.emptyList();

		return demands.stream().filter(ManiMajraDemand::isUnPaid).reduce(
				ManiMajraRentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(),
				(summary, demand) -> {

					return ManiMajraRentSummary.builder().rent(demand.getRent())
							.collectedRent(demand.getCollectedRent() != null ? demand.getCollectedRent() : 0)
							.collectedGST(demand.getCollectedGST() != null ? demand.getCollectedGST() : 0)
							.balanceAmount(rentAccount.getRemainingAmount()).build();
				}, (summary, demand) -> summary);
	}

	private ManiMajraPayment clonePayment(ManiMajraPayment rentPayment) {

		return ManiMajraPayment.builder().rentReceived(rentPayment.getRentReceived())
				.receiptDate(rentPayment.getReceiptDate()).receiptNo(rentPayment.getReceiptNo())
				.paymentDate(rentPayment.getPaymentDate()).processed(false).build();
	}

	private ManiMajraDemand cloneDemand(ManiMajraDemand rentDemand) {

		return ManiMajraDemand.builder().collectedRent(rentDemand.getCollectedRent())
				.collectedGST(rentDemand.getCollectedGST()).status(PaymentStatusEnum.UNPAID)
				// .status(rentDemand.getStatus())
				.generationDate(rentDemand.getGenerationDate()).rent(rentDemand.getRent()).gst(rentDemand.getGst())
				// .interestSince(rentDemand.getInterestSince()).remainingRent(rentDemand.getRent())
				.id(rentDemand.getId()).build();
	}

	private ManiMajraRentSummary getSummaryForDemand(EstateAccount rentAccount,
			List<ManiMajraDemand> demandsToBeSettled, ManiMajraDemand currentDemand,
			ManiMajraAccountStatement statement) {
		ManiMajraRentSummary rentSummary;
		this.settle(demandsToBeSettled, Collections.emptyList(), rentAccount, true);
		rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, currentDemand.getGenerationDate());
		statement.setDate(currentDemand.getGenerationDate());
		// statement.setAmount(currentDemand.getCollectedRent());
		statement.setType(Type.D);
		statement.setComment(currentDemand.getComment());
		return rentSummary;
	}

	private ManiMajraRentSummary calculateRentSummaryAt(List<ManiMajraDemand> demands, EstateAccount rentAccount,
			Long atTimestamp) {
		final LocalDate atDate = getLocalDate(atTimestamp);

		if (demands == null)
			demands = Collections.emptyList();

		return demands.stream().filter(ManiMajraDemand::isUnPaid).reduce(
				ManiMajraRentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(),
				(summary, demand) -> {

					final LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());

					return ManiMajraRentSummary.builder().rent(demand.getRent())
							.collectedRent(demand.getCollectedRent() != null ? demand.getCollectedRent() : 0)
							.gst(demand.getGst())
							.collectedGST(demand.getCollectedGST() != null ? demand.getCollectedGST() : 0).build();

				}, (summary, demand) -> summary);
	}

	private LocalDate getLocalDate(Long atTimestamp) {
		return Instant.ofEpochMilli(atTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
