package org.egov.cpt.service;

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

import org.egov.cpt.models.PaymentStatusEnum;
import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.models.RentAccountStatement.Type;
import org.egov.cpt.models.RentCollection;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.models.RentSummary;
import org.springframework.stereotype.Service;

@Service
public class RentCollectionService implements IRentCollectionService {

	@Override
	public List<RentCollection> settle(final List<RentDemand> demandsToBeSettled, final List<RentPayment> payments,
			final RentAccount account, double interestRate,Long interestStartDate) {
		Collections.sort(demandsToBeSettled);
		Collections.sort(payments);
		/**
		 * Don't process payments that are already processed.
		 */
		List<RentPayment> paymentsToBeSettled = payments.stream().filter(payment -> !payment.isProcessed())
				.collect(Collectors.toList());

		/**
		 * Settle unprocessed payments
		 */
		List<RentCollection> collections = paymentsToBeSettled.stream().map(payment -> {
			return settlePayment(demandsToBeSettled, payment, account, interestRate,interestStartDate);
		}).flatMap(Collection::stream).collect(Collectors.toList());

		if (account.getRemainingAmount() == 0
				|| !this.didExtractAllDemandsInterest(demandsToBeSettled, account.getRemainingSince(),interestStartDate)) {
			return collections;
		}

		/**
		 * We have positive account balance.
		 */
		List<RentDemand> newerDemands = demandsToBeSettled.stream()
				.filter(d -> d.getGenerationDate() > account.getRemainingSince()).filter(RentDemand::isUnPaid)
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
		for (RentDemand demand : newerDemands) {
			RentPayment payment = RentPayment.builder().amountPaid(0D).dateOfPayment(demand.getGenerationDate())
					.build();
			List<RentCollection> settledCollections = settlePayment(Collections.singletonList(demand), payment, account,
					0,interestStartDate);
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

	private List<RentCollection> settlePayment(final List<RentDemand> demandsToBeSettled, final RentPayment payment,
			final RentAccount account, double interestRate,Long interestStartDate) {
		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		List<RentDemand> demands = demandsToBeSettled.stream()
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
				effectiveAmount,interestStartDate);
		effectiveAmount -= interestCollections.stream().mapToDouble(RentCollection::getInterestCollected).sum();

		/**
		 * Principal is to be extracted only when there are no demands with interest not
		 * extracted.
		 */
		boolean shouldExtractPrincipal = effectiveAmount > 0
				&& (interestRate == 0 || didExtractAllDemandsInterest(demands, payment.getDateOfPayment(),interestStartDate));
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
	 * @param interestStartDate 
	 * @return
	 */
	private boolean didExtractAllDemandsInterest(List<RentDemand> demands, long dateOfPayment, Long interestStartDate) {
		return demands.stream().filter(RentDemand::isUnPaid).allMatch(demand -> {
			LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
			LocalDate paymentDate = getLocalDate(dateOfPayment);
			if(demand.getGenerationDate()<interestStartDate)
				return true;
			boolean isPaymentDateWithinGraceperiod = demand.getInitialGracePeriod() >= ChronoUnit.DAYS
					.between(demandGenerationDate, paymentDate);
			return isPaymentDateWithinGraceperiod || Instant.ofEpochMilli(dateOfPayment).atZone(ZoneId.systemDefault()).toLocalDate().equals(Instant.ofEpochMilli( demand.getInterestSince()).atZone(ZoneId.systemDefault()).toLocalDate());
		});
	}

	private List<RentCollection> extractPrincipal(List<RentDemand> demands, double paymentAmount,
			long paymentTimestamp) {
		ArrayList<RentCollection> collections = new ArrayList<RentCollection>();
		List<RentDemand> filteredDemands = demands.stream().filter(RentDemand::isUnPaid).collect(Collectors.toList());
		for (RentDemand demand : filteredDemands) {
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

	private List<RentCollection> extractInterest(double interestRate, long paymentTimeStamp, List<RentDemand> demands,
			double paymentAmount, Long interestStartDate) {
		if (interestRate <= 0) {
			return Collections.emptyList();
		}

		ArrayList<RentCollection> collections = new ArrayList<RentCollection>(demands.size());
		for (RentDemand demand : demands) {
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
			
			if(demand.getInterestSince() >= interestStartDate) {
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
	 * @param interestStartDate 
	 * @param collections
	 * @param payment
	 * @return
	 */
	@Override
	public RentSummary calculateRentSummaryAt(List<RentDemand> demands, RentAccount rentAccount, double interestRate,
			long atTimestamp, Long interestStartDate) {
		final LocalDate atDate = getLocalDate(atTimestamp);
		return demands.stream().filter(RentDemand::isUnPaid).reduce(
				RentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(), (summary, demand) -> {

					/**
					 * Calculate interest till atDate
					 */
					LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
					double calculatedInterest = 0D;
					long noOfDaysBetweenGenerationAndPayment = 1
							+ ChronoUnit.DAYS.between(demandGenerationDate, atDate);
					LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());
					
					if (noOfDaysBetweenGenerationAndPayment > demand.getInitialGracePeriod() && demandInterestSinceDate.getYear() >= getLocalDate(interestStartDate).getYear()) {
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
	public List<RentAccountStatement> getAccountStatement(List<RentDemand> demands, List<RentPayment> payments,
			double interestRate, Long fromDateTimestamp, Long toDateTimestamp,Long interestStartDate) {
		long endTimestamp = toDateTimestamp == null ? System.currentTimeMillis() : toDateTimestamp.longValue();
		demands = demands.stream().filter(demand -> demand.getGenerationDate() <= endTimestamp)
				.collect(Collectors.toList());
		payments = payments.stream().filter(payment -> payment.getAmountPaid() > 0)
				.filter(p -> p.getDateOfPayment() <= endTimestamp).collect(Collectors.toList());
		Collections.sort(demands);
		Collections.sort(payments);
		List<RentAccountStatement> accountStatementItems = new ArrayList<RentAccountStatement>();
		RentAccount rentAccount = RentAccount.builder().remainingAmount(0D).build();
		List<RentDemand> demandsToBeSettled = new ArrayList<RentDemand>(demands.size());
		Iterator<RentDemand> demandIterator = demands.iterator();
		Iterator<RentPayment> paymentIterator = payments.iterator();
		RentDemand currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
		RentPayment currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
		while (true) {
			boolean reachedLast = false;
			RentSummary rentSummary;
			RentAccountStatement statement = RentAccountStatement.builder().build();
			if (currentDemand == null && currentPayment == null) {
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled,
						RentDemand.builder().generationDate(endTimestamp).collectionPrincipal(0D).build(), statement,interestStartDate);
				reachedLast = true;
			} else if (currentDemand == null) {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
						statement,interestStartDate);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			} else if (currentPayment == null) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
						statement,interestStartDate);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else if (currentDemand.getGenerationDate() <= currentPayment.getDateOfPayment()) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
						statement,interestStartDate);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
						statement,interestStartDate);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
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
	}

	private RentDemand cloneDemand(RentDemand rentDemand) {
		return RentDemand.builder().collectionPrincipal(rentDemand.getCollectionPrincipal())
				.status(PaymentStatusEnum.UNPAID).generationDate(rentDemand.getGenerationDate())
				.interestSince(rentDemand.getGenerationDate()).initialGracePeriod(rentDemand.getInitialGracePeriod())
				.remainingPrincipal(rentDemand.getCollectionPrincipal()).build();
	}

	private RentPayment clonePayment(RentPayment rentPayment) {
		return RentPayment.builder().amountPaid(rentPayment.getAmountPaid())
				.dateOfPayment(rentPayment.getDateOfPayment()).receiptNo(rentPayment.getReceiptNo()).processed(false)
				.build();
	}

	private RentSummary getSummaryForDemand(double interestRate, RentAccount rentAccount,
			List<RentDemand> demandsToBeSettled, RentDemand currentDemand, RentAccountStatement statement, Long interestStartDate) {
		RentSummary rentSummary;
		this.settle(demandsToBeSettled, Collections.emptyList(), rentAccount, interestRate,interestStartDate);
		rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				currentDemand.getGenerationDate(),interestStartDate);
		statement.setDate(currentDemand.getGenerationDate());
		statement.setAmount(currentDemand.getCollectionPrincipal());
		statement.setType(Type.D);
		return rentSummary;
	}

	private RentSummary calculateSummaryForPayment(double interestRate, RentAccount rentAccount,
			List<RentDemand> demandsToBeSettled, RentPayment currentPayment, RentAccountStatement statement,Long interestStartDate) {
		currentPayment = this.clonePayment(currentPayment);
		this.settle(demandsToBeSettled, Collections.singletonList(currentPayment), rentAccount, interestRate,interestStartDate);
		RentSummary rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				currentPayment.getDateOfPayment(),interestStartDate);
		statement.setDate(currentPayment.getDateOfPayment());
		statement.setAmount(currentPayment.getAmountPaid());
		statement.setType(Type.C);
		statement.setReceiptNo(currentPayment.getReceiptNo());
		return rentSummary;
	}

	@Override
	public RentSummary calculateRentSummary(List<RentDemand> demands, RentAccount rentAccount, double interestRate,Long interestStartDate) {
		return this.calculateRentSummaryAt(demands, rentAccount, interestRate, System.currentTimeMillis(),interestStartDate);
	}
}
