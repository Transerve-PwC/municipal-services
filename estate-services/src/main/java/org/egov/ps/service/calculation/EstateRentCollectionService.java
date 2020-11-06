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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateAccountStatement;
import org.egov.ps.web.contracts.EstateAccountStatement.Type;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.egov.ps.web.contracts.EstateRentSummary;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.stereotype.Service;

@Service
public class EstateRentCollectionService implements IEstateRentCollectionService{
	
	
	private List<EstateRentCollection> settlePayment(final List<EstateDemand> demandsToBeSettled, final EstatePayment payment,
			final EstateAccount account, double interestRate,boolean isFixGST){
		
		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		List<EstateDemand> demands = demandsToBeSettled.stream()
				.filter(demand -> demand.isUnPaid() && demand.getGenerationDate() <= payment.getReceiptDate())
				.collect(Collectors.toList());
		
		/**
		 * Effective amount to be settled = paidAmount + accountBalance
		 */
		double effectiveAmount = payment.getRentReceived() + account.getRemainingAmount();
		
		/**
		 * Break down payment into a set of collections. Any pending rent and GST  is to be
		 * collected first.
		 */
		List<EstateRentCollection> interestCollections = extractRentAndGST(interestRate, payment.getReceiptDate(), demands,
				effectiveAmount, isFixGST);
		effectiveAmount = (effectiveAmount-interestCollections.stream().mapToDouble((EstateRentCollection::getRentWithGST))	.sum());
		
	//	effectiveAmount = ((effectiveAmount-interestCollections.stream().mapToDouble(EstateRentCollection::getGstCollected)		
		//		.sum()));
		

		/**
		 * Amount is left after deducting interest for all the demands. Extract
		 * Principal.
		 */
		List<EstateRentCollection> principalCollections = effectiveAmount > 0
				? extractPenalty(demands, effectiveAmount, payment.getReceiptDate(),interestRate)
				: Collections.emptyList();
		effectiveAmount -= principalCollections.stream().mapToDouble(EstateRentCollection::getRentPenaltyCollected).sum();
		effectiveAmount -= principalCollections.stream().mapToDouble(EstateRentCollection::getGstPenaltyCollected).sum();
		
		/**
		 * Amount is left after deducting all the principal amounts. Put it back in the
		 * account
		 */
		account.setRemainingAmount(effectiveAmount);
		account.setRemainingSince(payment.getReceiptDate());

		/**
		 * Mark payment as processed.
		 */
		payment.setProcessed(true);
		return Stream.of(interestCollections, principalCollections).flatMap(x -> x.stream())
				.collect(Collectors.toList());

		


		
	}
	
	private List<EstateRentCollection> extractPenalty(List<EstateDemand> demands, double paymentAmount,
			long paymentTimestamp,double interestRate) {
		ArrayList<EstateRentCollection> collections = new ArrayList<EstateRentCollection>();
		List<EstateDemand> filteredDemands = demands.stream().filter(EstateDemand::isUnPaid).collect(Collectors.toList());
		
		double rentPenaltyToBePaid=0;
		double gstPenaltyToBePaid=0;
		double GSTinterest=0;
		
		for (EstateDemand demand : filteredDemands) {
			if (paymentAmount <= 0) {
				break;
			}
			GSTinterest=0;	
			if(! demand.getIsPrevious() ) {
			LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
			LocalDate paymentDate = getLocalDate(paymentTimestamp);

			long noOfDaysBetweenGenerationAndPayment = ChronoUnit.DAYS.between(demandGenerationDate, paymentDate);
			if (noOfDaysBetweenGenerationAndPayment <= demand.getInitialGracePeriod()) {
				continue;
			}

			LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());

			long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, paymentDate);
			
			if (noOfDaysForInterestCalculation == 0) {
				continue;
			}
			GSTinterest = demand.getGst()*(interestRate/100)* noOfDaysForInterestCalculation/365;
					
			}
			else
				GSTinterest=demand.getGstInterest();

		if (demand.getPenaltyInterest() + GSTinterest <= paymentAmount) {
			
			rentPenaltyToBePaid=demand.getPenaltyInterest();
			gstPenaltyToBePaid=GSTinterest;
		} else {
			// 50-50
			//100 gst 80 
			//rent pen =100+20
			
			double halfOfTheRemainingAmount = paymentAmount / 2;
			rentPenaltyToBePaid=gstPenaltyToBePaid=halfOfTheRemainingAmount;
			
			if (halfOfTheRemainingAmount > GSTinterest) {
				rentPenaltyToBePaid=halfOfTheRemainingAmount+(halfOfTheRemainingAmount-GSTinterest);
				gstPenaltyToBePaid=GSTinterest;
				
			}
			
			demand.setCollectedRentPenalty(rentPenaltyToBePaid);
			demand.setCollectedGSTPenalty(gstPenaltyToBePaid);
			demand.setRemainingRentPenalty(demand.getPenaltyInterest()-rentPenaltyToBePaid);
			demand.setRemainingGSTPenalty(GSTinterest-gstPenaltyToBePaid);
				
		}
		paymentAmount-=paymentAmount+(rentPenaltyToBePaid+gstPenaltyToBePaid);
			
			collections.add(EstateRentCollection.builder().demandId(demand.getId()).rentPenaltyCollected(rentPenaltyToBePaid).gstPenaltyCollected(gstPenaltyToBePaid)
					.collectedAt(paymentTimestamp).build());
			//demand.setRemainingPrincipalAndUpdatePaymentStatus(demand.getRemainingPrincipal() - collectionAmount);
		}
		return collections;
	}
	private List<EstateRentCollection> extractRentAndGST(double interestRate, long paymentTimeStamp, List<EstateDemand> demands,
			double paymentAmount,boolean isFixGST) {
//		if (interestRate <= 0) {
//			return Collections.emptyList();
//		}

		double rentTobePaid=0;
		double gstToBePaid=0;
	//	double paymentReceived=0;
		ArrayList<EstateRentCollection> collections = new ArrayList<EstateRentCollection>(demands.size());
		for (EstateDemand demand : demands) {
			if (paymentAmount <= 0) {
				break;
			}
			if(demand.getRemainingRent()<=0)
				continue;
			if (demand.getRemainingRent() + demand.getRemainingGST() <= paymentAmount) {
				rentTobePaid=demand.getRemainingRent();
				gstToBePaid=demand.getRemainingGST();
				

			} else {
				// some calculation
				
				rentTobePaid = paymentAmount*100/(100+interestRate);
				gstToBePaid= paymentAmount-rentTobePaid;
				if(gstToBePaid>demand.getRemainingGST())
					rentTobePaid+=(gstToBePaid-demand.getRemainingGST());
				
			}
			demand.setCollectedRent(rentTobePaid);
			demand.setCollectedGST(gstToBePaid);
			demand.setRemainingRent(demand.getRemainingRent()-rentTobePaid);
			demand.setRemainingGST(demand.getRemainingGST()-gstToBePaid);
			collections.add(EstateRentCollection.builder().rentCollected(rentTobePaid)
					.gstCollected(gstToBePaid)
					.collectedAt(paymentTimeStamp)
					.demandId(demand.getId())
					.rentWithGST(gstToBePaid+rentTobePaid)
					.build());
			
			paymentAmount -= (rentTobePaid + gstToBePaid);
			demand.setInterestSince(paymentTimeStamp);
			
			

//			if(!isFixGST) {
//				LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
//				LocalDate paymentDate = getLocalDate(paymentTimeStamp);
//				long noOfDaysBetweenGenerationAndPayment = ChronoUnit.DAYS.between(demandGenerationDate, paymentDate);
//
//			}
		}
		return collections;
	}

	
	private LocalDate getLocalDate(long atTimestamp) {
		return Instant.ofEpochMilli(atTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
	}
	@Override
	public List<EstateRentCollection> settle(final List<EstateDemand> demandsToBeSettled, final List<EstatePayment> payments,
			final EstateAccount account, double interestRate,boolean isFixGST) {
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
		List<EstateRentCollection> collections = paymentsToBeSettled.stream().map(payment -> {
			return settlePayment(demandsToBeSettled, payment, account, interestRate, isFixGST);
		}).flatMap(Collection::stream).collect(Collectors.toList());

		if (account.getRemainingAmount() == 0
				|| !this.didExtractAllDemandsInterest(demandsToBeSettled, account.getRemainingSince())) {
			return collections;
		}

//		/**
//		 * We have positive account balance.
//		 */
		List<EstateDemand> newerDemands = demandsToBeSettled.stream()
				.filter(d -> d.getGenerationDate() > account.getRemainingSince()).filter(EstateDemand::isUnPaid)
				.collect(Collectors.toList());
		if (newerDemands.size() == 0) {
			return collections;
		}
//
//		/**
//		 * In the case of 1) demand generation at 1st of every month. 2) More amount
//		 * payed toward the end which should be adjusted to left over demands.
//		 */
		ArrayList<EstateRentCollection> result = new ArrayList<EstateRentCollection>(collections);
//
//		/**
//		 * Settle each demand by creating an empty payment with the demand generation
//		 * date.
//		 */
		for (EstateDemand demand : newerDemands) {
			EstatePayment payment = EstatePayment.builder().rentReceived(0D).receiptDate(demand.getGenerationDate())
					.build();
			List<EstateRentCollection> settledCollections = settlePayment(Collections.singletonList(demand), payment, account,
					0,true);
			if (settledCollections.size() == 0) {
				continue;
			}
			result.addAll(settledCollections);
			if (account.getRemainingAmount() == 0) {
				break;
			}
		}
		return collections;
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
	public List<EstateAccountStatement> getAccountStatement(List<EstateDemand> demands, List<EstatePayment> payments,
			double interestRate, Long fromDateTimestamp, Long toDateTimestamp) {
		
		long endTimestamp = toDateTimestamp == null ? System.currentTimeMillis() : toDateTimestamp.longValue();
		demands = demands.stream().filter(demand -> demand.getGenerationDate() <= endTimestamp)
				.collect(Collectors.toList());
		payments = payments.stream().filter(payment -> payment.getRentReceived() > 0)
				.filter(p -> p.getReceiptDate() <= endTimestamp).collect(Collectors.toList());
		Collections.sort(demands);
		Collections.sort(payments);
		List<EstateAccountStatement> accountStatementItems = new ArrayList<EstateAccountStatement>();
		EstateAccount rentAccount = EstateAccount.builder().remainingAmount(0D).build();
		List<EstateDemand> demandsToBeSettled = new ArrayList<EstateDemand>(demands.size());
		Iterator<EstateDemand> demandIterator = demands.iterator();
		Iterator<EstatePayment> paymentIterator = payments.iterator();
		EstateDemand currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
		EstatePayment currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
		while (true) {
			boolean reachedLast = false;
			EstateRentSummary rentSummary;
			EstateAccountStatement statement = EstateAccountStatement.builder().build();
			
			//no demand and payment remaining , calculate the summary
			if (currentDemand == null && currentPayment == null) {
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled,
						EstateDemand.builder().generationDate(endTimestamp).collectedRent(0D).build(), statement);
				reachedLast = true;
			} 
			//no demand remaining
			else if (currentDemand == null) {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
						statement);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			} 
			//no payment remaining
			else if (currentPayment == null) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
						statement);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else if (currentDemand.getGenerationDate() <= currentPayment.getReceiptDate()) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
						statement);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
						statement);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			}
			statement.setRemainingPrincipal(rentSummary.getBalanceRent());
			statement.setRemainingInterest(rentSummary.getBalanceInterest());
			statement.setRemainingBalance(rentSummary.getBalanceAmount());
			statement.setRemainingGST(rentSummary.getBalanceGST());
			statement.setRemainingRentPenalty(rentSummary.getBalanceRentPenalty());
			statement.setRemainingGSTPenalty(rentSummary.getBalanceGSTPenalty());
		//	statement.setReceiptNo(currentPayment!=null?currentPayment.getReceiptNo():"");
			statement.setRent(rentSummary.getRent());
			statement.setCollectedRent(rentSummary.getCollectedRent());
			statement.setRentPanelty(rentSummary.getRentPanelty());
			statement.setGst(rentSummary.getGst());
			statement.setCollectedGST(rentSummary.getCollectedGST());
			statement.setRentPanelty(rentSummary.getRentPanelty());
			statement.setCollectedRentPanelty(rentSummary.getCollectedRentPanelty());
			statement.setGSTPanelty(rentSummary.getGSTPanelty());
			statement.setCollectedGSTPanelty(rentSummary.getCollectedGSTPanelty());
			statement.setIsPrevious(rentSummary.getIsPrevious());
			accountStatementItems.add(statement);
			if (reachedLast) {
				break;
			}
		}
		return accountStatementItems;
	}
	
	private EstateRentSummary calculateSummaryForPayment(double interestRate, EstateAccount rentAccount,
			List<EstateDemand> demandsToBeSettled, EstatePayment currentPayment, EstateAccountStatement statement) {
		currentPayment = this.clonePayment(currentPayment);
		this.settle(demandsToBeSettled, Collections.singletonList(currentPayment), rentAccount, interestRate,true);
		EstateRentSummary rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				currentPayment.getReceiptDate());
		statement.setDate(currentPayment.getReceiptDate());
		statement.setAmount(currentPayment.getRentReceived());
		statement.setType(Type.C);
		statement.setReceiptNo(currentPayment.getReceiptNo());
		return rentSummary;
	}
	
	private EstateRentSummary getSummaryForDemand(double interestRate, EstateAccount rentAccount,
			List<EstateDemand> demandsToBeSettled, EstateDemand currentDemand, EstateAccountStatement statement) {
		EstateRentSummary rentSummary;
		this.settle(demandsToBeSettled, Collections.emptyList(), rentAccount, interestRate,true);
		rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				currentDemand.getGenerationDate());
		statement.setDate(currentDemand.getGenerationDate());
		statement.setAmount(currentDemand.getCollectedRent());
		statement.setType(Type.D);
		return rentSummary;
	}
	
	private EstatePayment clonePayment(EstatePayment rentPayment) {
		return EstatePayment.builder().rentReceived(rentPayment.getRentReceived())
				.receiptDate(rentPayment.getReceiptDate())
				.receiptNo(rentPayment.getReceiptNo())
				.processed(false).build();
	}

	@Override
	public EstateRentSummary calculateRentSummary(List<EstateDemand> demands, EstateAccount rentAccount, double interestRate) {
		return this.calculateRentSummaryAt(demands, rentAccount, interestRate, System.currentTimeMillis());
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
	public EstateRentSummary calculateRentSummaryAt(List<EstateDemand> demands, EstateAccount rentAccount, double interestRate,
			long atTimestamp) {
		final LocalDate atDate = getLocalDate(atTimestamp);
		return demands.stream().filter(EstateDemand::isUnPaid).reduce(
				EstateRentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(), (summary, demand) -> {

					 /** Summarize the result.
					 */
					
					/**
					 * Calculate interest till atDate
					 */
					LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
					double calculatedInterest = 0D;
					long noOfDaysBetweenGenerationAndPayment = 1
							+ ChronoUnit.DAYS.between(demandGenerationDate, atDate);
					if (noOfDaysBetweenGenerationAndPayment > demand.getInitialGracePeriod()) {
						if(demand.getInterestSince()==null)
							demand.setInterestSince(demand.getGenerationDate());
						LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());

						long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, atDate);
						calculatedInterest = demand.getGst() * (interestRate/100)
								* noOfDaysForInterestCalculation / 365 ;
					}
						

					return EstateRentSummary.builder()
							.rent(demand.getRent())
							.collectedRent(demand.getCollectedRent()!=null?demand.getCollectedRent():0)
							.balanceRent(summary.getBalanceRent() + demand.getRemainingRent())
							.gst(demand.getGst())
							.collectedGST(demand.getCollectedGST()!=null?demand.getCollectedGST():0)
							.balanceGST(summary.getBalanceGST() + demand.getRemainingGST())
						     .GSTPanelty( calculatedInterest) 				
							.collectedGSTPanelty(demand.getCollectedGSTPenalty()!=null?demand.getCollectedGSTPenalty():0)
							.balanceGSTPenalty(summary.getBalanceGSTPenalty() + demand.getRemainingGSTPenalty())
							.balanceRentPenalty(demand.getPenaltyInterest())
				            .collectedRentPanelty(  demand.getCollectedRentPenalty()!=null?demand.getCollectedRentPenalty():0)
							.balanceRentPenalty(summary.getBalanceRentPenalty() + demand.getRemainingRentPenalty())
							.balanceAmount(rentAccount.getRemainingAmount())
							.isPrevious(demand.getIsPrevious())
							.build();
							
				}, (summary, demand) -> summary);
	}
	
	private EstateDemand cloneDemand(EstateDemand rentDemand) {
		
		
		return EstateDemand.builder().collectedRent(rentDemand.getCollectedRent())
				.status(PaymentStatusEnum.UNPAID).generationDate(rentDemand.getGenerationDate())
				.initialGracePeriod(rentDemand.getInitialGracePeriod())
				.remainingRent(rentDemand.getRemainingRent())
				.gst(rentDemand.getGst()).remainingGST(rentDemand.getRemainingGST())
				.remainingGSTPenalty(rentDemand.getRemainingGSTPenalty())
				.remainingRentPenalty(rentDemand.getRemainingRentPenalty())
				.collectedGST(rentDemand.getCollectedGST())
				.collectedGSTPenalty(rentDemand.getCollectedGSTPenalty())
				.collectedRent(rentDemand.getCollectedRent())
				.collectedRentPenalty(rentDemand.getCollectedRentPenalty())
				.rent(rentDemand.getRent())
				.gst(rentDemand.getGst())
				.gstInterest(rentDemand.getGstInterest())
				.penaltyInterest(rentDemand.getPenaltyInterest())
				.interestSince(rentDemand.getInterestSince())
				.isPrevious(rentDemand.getIsPrevious())
				.build();
	}
}
