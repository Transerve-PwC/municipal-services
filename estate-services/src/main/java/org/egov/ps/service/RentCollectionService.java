package org.egov.ps.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.ps.model.RentAccount;
import org.egov.ps.model.RentAccountStatement;
import org.egov.ps.model.RentAccountStatement.Type;
import org.egov.ps.model.RentCollection;
import org.egov.ps.model.RentSummary;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class RentCollectionService implements IRentCollectionService {

	@Override
	public List<RentCollection> settle(final List<EstateDemand> demandsToBeSettled, final List<EstatePayment> payments,
			final RentAccount account, double interestRate) {
		Collections.sort(demandsToBeSettled);
		Collections.sort(payments);
		/**
		 * Don't process payments that are already processed.
		 */
		List<EstatePayment> paymentsToBeSettled = payments.stream().filter(payment -> !payment.isProcessed())
				.collect(Collectors.toList());

		/**
		 * Settle unprocessed payments
		 */
		List<RentCollection> collections = paymentsToBeSettled.stream().map(payment -> {
			return settlePayment(demandsToBeSettled, payment, account, interestRate);
		}).flatMap(Collection::stream).collect(Collectors.toList());

		if (account.getRemainingAmount() == 0
				|| !this.didExtractAllDemandsInterest(demandsToBeSettled, account.getRemainingSince())) {
			return collections;
		}

		/**
		 * We have positive account balance.
		 */
		List<EstateDemand> newerDemands = demandsToBeSettled.stream()
				.filter(d -> d.getGenerationDate() > account.getRemainingSince()).filter(EstateDemand::isUnPaid)
				.collect(Collectors.toList());
		if (newerDemands.size() == 0) {
			return collections;
		}

		/**
		 * In the case of 1) demand generation at 1st of every month. 2) More amount
		 * payed toward the end which should be adjusted to left over demands.
		 */
		ArrayList<RentCollection> result = new ArrayList<RentCollection>(collections);

		/**
		 * Settle each demand by creating an empty payment with the demand generation
		 * date.
		 */
		for (EstateDemand demand : newerDemands) {
			EstatePayment payment = EstatePayment.builder().amountPaid(0D).dateOfPayment(demand.getGenerationDate())
					.build();
			List<RentCollection> settledCollections = settlePayment(Collections.singletonList(demand), payment, account,
					0);
			if (settledCollections.size() == 0) {
				continue;
			}
			result.addAll(settledCollections);
			if (account.getRemainingAmount() == 0) {
				break;
			}
		}
		return result;
	}

	private List<RentCollection> settlePayment(final List<EstateDemand> demandsToBeSettled, final EstatePayment payment,
			final RentAccount account, double interestRate) {
		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		List<EstateDemand> demands = demandsToBeSettled.stream()
				.filter(demand -> demand.isUnPaid() && demand.getGenerationDate() <= payment.getDateOfPayment())
				.collect(Collectors.toList());

		/**
		 * Effective amount to be settled = paidAmount + accountBalance
		 */
		double effectiveAmount = payment.getAmountPaid() + account.getRemainingAmount();

		/**
		 * Break down payment into a set of collections. Any pending interest is to be
		 * collected first.
		 */
		List<RentCollection> interestCollections = extractInterest(interestRate, payment.getDateOfPayment(), demands,
				effectiveAmount);
		effectiveAmount -= interestCollections.stream().mapToDouble(RentCollection::getInterestCollected).sum();

		/**
		 * Principal is to be extracted only when there are no demands with interest not
		 * extracted.
		 */
		boolean shouldExtractPrincipal = effectiveAmount > 0
				&& (interestRate == 0 || didExtractAllDemandsInterest(demands, payment.getDateOfPayment()));
		/**
		 * Amount is left after deducting interest for all the demands. Extract
		 * Principal.
		 */
		List<RentCollection> principalCollections = shouldExtractPrincipal
				? extractPrincipal(demands, effectiveAmount, payment.getDateOfPayment())
				: Collections.emptyList();
		effectiveAmount -= principalCollections.stream().mapToDouble(RentCollection::getPrincipalCollected).sum();

		/**
		 * Amount is left after deducting all the principal amounts. Put it back in the
		 * account
		 */
		account.setRemainingAmount(effectiveAmount);
		account.setRemainingSince(payment.getDateOfPayment());

		/**
		 * Mark payment as processed.
		 */
		payment.setProcessed(true);
		return Stream.of(interestCollections, principalCollections).flatMap(x -> x.stream())
				.collect(Collectors.toList());
	}

	/**
	 * For each demand check if payment date is after the initialGracePeriod and
	 * interest since is on the same date.
	 * 
	 * @param demands
	 * @param dateOfPayment
	 * @return
	 */
	private boolean didExtractAllDemandsInterest(List<EstateDemand> demands, long dateOfPayment) {
		return demands.stream().filter(EstateDemand::isUnPaid).allMatch(demand -> {
			LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
			LocalDate paymentDate = getLocalDate(dateOfPayment);
			boolean isPaymentDateWithinGraceperiod = demand.getInitialGracePeriod() >= ChronoUnit.DAYS
					.between(demandGenerationDate, paymentDate);
			return isPaymentDateWithinGraceperiod || dateOfPayment == demand.getInterestSince();
		});
	}

	private List<RentCollection> extractPrincipal(List<EstateDemand> demands, double paymentAmount,
			long paymentTimestamp) {
		ArrayList<RentCollection> collections = new ArrayList<RentCollection>();
		List<EstateDemand> filteredDemands = demands.stream().filter(EstateDemand::isUnPaid)
				.collect(Collectors.toList());
		for (EstateDemand demand : filteredDemands) {
			if (paymentAmount <= 0) {
				break;
			}
			double collectionAmount = Math.min(demand.getRemainingPrincipal(), paymentAmount);

			paymentAmount -= collectionAmount;
			collections.add(RentCollection.builder().demandId(demand.getId()).principalCollected(collectionAmount)
					.collectedAt(paymentTimestamp).build());
			demand.setRemainingPrincipalAndUpdatePaymentStatus(demand.getRemainingPrincipal() - collectionAmount);
		}
		return collections;
	}

	private List<RentCollection> extractInterest(double interestRate, long paymentTimeStamp, List<EstateDemand> demands,
			double paymentAmount) {
		if (interestRate <= 0) {
			return Collections.emptyList();
		}

		ArrayList<RentCollection> collections = new ArrayList<RentCollection>(demands.size());
		for (EstateDemand demand : demands) {
			if (paymentAmount <= 0) {
				break;
			}
			LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
			LocalDate paymentDate = getLocalDate(paymentTimeStamp);

			long noOfDaysBetweenGenerationAndPayment = ChronoUnit.DAYS.between(demandGenerationDate, paymentDate);
			if (noOfDaysBetweenGenerationAndPayment <= demand.getInitialGracePeriod()) {
				continue;
			}

			LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());

			long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, paymentDate);

			if (noOfDaysForInterestCalculation == 0) {
				continue;
			}
			double interest = demand.getRemainingPrincipal() * noOfDaysForInterestCalculation * interestRate / 365
					/ 100;
			if (interest < paymentAmount) {
				collections.add(RentCollection.builder().interestCollected(interest).collectedAt(paymentTimeStamp)
						.demandId(demand.getId()).build());
				demand.setInterestSince(paymentTimeStamp);
				paymentAmount -= interest;
			}
		}
		return collections;
	}

	/**
	 * Get the current rent summary by calculating from the given demands and
	 * collections for the same property.
	 * 
	 * @apiNote This is called every time we return a property in search.
	 * @apiNote This will not change the database in anyway.
	 * @param demands
	 * @param collections
	 * @param payment
	 * @return
	 */
	@Override
	public RentSummary calculateRentSummaryAt(List<EstateDemand> demands, RentAccount rentAccount, double interestRate,
			long atTimestamp) {
		final LocalDate atDate = getLocalDate(atTimestamp);
		return demands.stream().filter(EstateDemand::isUnPaid).reduce(
				RentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(), (summary, demand) -> {

					/**
					 * Calculate interest till atDate
					 */
					LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
					double calculatedInterest = 0D;
					long noOfDaysBetweenGenerationAndPayment = 1
							+ ChronoUnit.DAYS.between(demandGenerationDate, atDate);
					if (noOfDaysBetweenGenerationAndPayment > demand.getInitialGracePeriod()) {

						LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());

						long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, atDate);
						calculatedInterest = demand.getRemainingPrincipal() * noOfDaysForInterestCalculation
								* interestRate / 365 / 100;
					}
					/**
					 * Summarize the result.
					 */
					return RentSummary.builder()
							.balancePrincipal(summary.getBalancePrincipal() + demand.getRemainingPrincipal())
							.balanceInterest(summary.getBalanceInterest() + calculatedInterest)
							.balanceAmount(summary.getBalanceAmount()).build();
				}, (summary, demand) -> summary);
	}

	private LocalDate getLocalDate(long atTimestamp) {
		return Instant.ofEpochMilli(atTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * @apiNote This will provide the account statement between the date specified
	 *          by the user. Jan 2000 ... December 2020 Jan 2020
	 * 
	 * @param demands
	 * @param payments
	 * @param lstCollection
	 * @return List<RentAccountStatement>
	 */
	@Override
	public List<RentAccountStatement> getAccountStatement(List<EstateDemand> demands, List<EstatePayment> payments,
			double interestRate, Long fromDateTimestamp, Long toDateTimestamp) {
		long endTimestamp = toDateTimestamp == null ? System.currentTimeMillis() : toDateTimestamp.longValue();
		demands = demands.stream().filter(demand -> demand.getGenerationDate() <= endTimestamp)
				.collect(Collectors.toList());
		payments = payments.stream().filter(payment -> payment.getAmountPaid() > 0)
				.filter(p -> p.getDateOfPayment() <= endTimestamp).collect(Collectors.toList());
		Collections.sort(demands);
		Collections.sort(payments);
		List<RentAccountStatement> accountStatementItems = new ArrayList<RentAccountStatement>();
		RentAccount rentAccount = RentAccount.builder().remainingAmount(0D).build();
		List<EstateDemand> demandsToBeSettled = new ArrayList<EstateDemand>(demands.size());
		Iterator<EstateDemand> demandIterator = demands.iterator();
		Iterator<EstatePayment> paymentIterator = payments.iterator();
		EstateDemand curEstateDemand = demandIterator.hasNext() ? demandIterator.next() : null;
		EstatePayment curEstatePayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
		while (true) {
			boolean reachedLast = false;
			RentSummary rentSummary;
			RentAccountStatement statement = RentAccountStatement.builder().build();
			if (curEstateDemand == null && curEstatePayment == null) {
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled,
						EstateDemand.builder().generationDate(endTimestamp).collectionPrincipal(0D).build(), statement);
				reachedLast = true;
			} else if (curEstateDemand == null) {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled,
						curEstatePayment, statement);
				curEstatePayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			} else if (curEstatePayment == null) {
				demandsToBeSettled.add(this.cloneDemand(curEstateDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, curEstateDemand,
						statement);
				curEstateDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else if (curEstateDemand.getGenerationDate() <= curEstatePayment.getDateOfPayment()) {
				demandsToBeSettled.add(this.cloneDemand(curEstateDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, curEstateDemand,
						statement);
				curEstateDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled,
						curEstatePayment, statement);
				curEstatePayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			}
			statement.setRemainingPrincipal(rentSummary.getBalancePrincipal());
			statement.setRemainingInterest(rentSummary.getBalanceInterest());
			statement.setRemainingBalance(rentSummary.getBalanceAmount());
			accountStatementItems.add(statement);
			if (reachedLast) {
				break;
			}
		}
		if (fromDateTimestamp == null) {
			return accountStatementItems;
		} else {
			Long fromDate = accountStatementItems.stream().map(stmt -> stmt.getDate())
					.filter(date -> Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate().equals(
							Instant.ofEpochMilli(fromDateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate()))
					.findFirst().get();
			return accountStatementItems.stream()
					.filter(statementItem -> statementItem.getDate() >= fromDate.longValue())
					.collect(Collectors.toList());
		}
	}

	private EstateDemand cloneDemand(EstateDemand EstateDemand) {
		return EstateDemand.builder().collectionPrincipal(EstateDemand.getCollectionPrincipal())
				.status(PaymentStatusEnum.UNPAID).generationDate(EstateDemand.getGenerationDate())
				.interestSince(EstateDemand.getGenerationDate())
				.initialGracePeriod(EstateDemand.getInitialGracePeriod())
				.remainingPrincipal(EstateDemand.getCollectionPrincipal()).build();
	}

	private EstatePayment clonePayment(EstatePayment EstatePayment) {
		return EstatePayment.builder().amountPaid(EstatePayment.getAmountPaid())
				.dateOfPayment(EstatePayment.getDateOfPayment()).receiptNo(EstatePayment.getReceiptNo())
				.processed(false).build();
	}

	private RentSummary getSummaryForDemand(double interestRate, RentAccount rentAccount,
			List<EstateDemand> demandsToBeSettled, EstateDemand curEstateDemand, RentAccountStatement statement) {
		RentSummary rentSummary;
		this.settle(demandsToBeSettled, Collections.emptyList(), rentAccount, interestRate);
		rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				curEstateDemand.getGenerationDate());
		statement.setDate(curEstateDemand.getGenerationDate());
		statement.setAmount(curEstateDemand.getCollectionPrincipal());
		statement.setType(Type.D);
		return rentSummary;
	}

	private RentSummary calculateSummaryForPayment(double interestRate, RentAccount rentAccount,
			List<EstateDemand> demandsToBeSettled, EstatePayment curEstatePayment, RentAccountStatement statement) {
		curEstatePayment = this.clonePayment(curEstatePayment);
		this.settle(demandsToBeSettled, Collections.singletonList(curEstatePayment), rentAccount, interestRate);
		RentSummary rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				curEstatePayment.getDateOfPayment());
		statement.setDate(curEstatePayment.getDateOfPayment());
		statement.setAmount(curEstatePayment.getAmountPaid());
		statement.setType(Type.C);
		statement.setReceiptNo(curEstatePayment.getReceiptNo());
		return rentSummary;
	}

	@Override
	public RentSummary calculateRentSummary(List<EstateDemand> demands, RentAccount rentAccount, double interestRate) {
		return this.calculateRentSummaryAt(demands, rentAccount, interestRate, System.currentTimeMillis());
	}
}
