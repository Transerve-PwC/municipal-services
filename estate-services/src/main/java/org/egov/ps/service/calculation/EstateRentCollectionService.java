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
	
	
	private List<EstateRentCollection> settlePayment( final List<EstateDemand> demandsToBeSettled, final EstatePayment payment,
			final EstateAccount account, double interestRate,boolean isFixGST,double rentInterest){
		
		
		
//		List<EstateDemand> consolidatedDemands = demandsToBeSettled.stream()
//				.filter(demand -> demand.getIsPrevious())
//				.collect(Collectors.toList());
		
		
		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		
		List<EstateDemand> demands = demandsToBeSettled.stream()
				.filter(demand -> demand.isUnPaid() && demand.getGenerationDate() <= payment.getPaymentDate())
				.collect(Collectors.toList());
		
//		demands.removeAll(consolidatedDemands);
//		demands.addAll(0,consolidatedDemands);
//		
		/**
		 * Effective amount to be settled = paidAmount + accountBalance
		 */
		double effectiveAmount = payment.getRentReceived() + account.getRemainingAmount();
		
		calculateInterest(demands,payment.getPaymentDate(),interestRate,isFixGST,rentInterest);
		
		/**
		 * Break down payment into a set of collections. Any pending rent and GST  is to be
		 * collected first.
		 */
		List<EstateRentCollection> interestCollections = extractRentAndGST(interestRate, payment.getPaymentDate(), demands,
				effectiveAmount, isFixGST);
		effectiveAmount = (effectiveAmount-interestCollections.stream().mapToDouble((EstateRentCollection::getRentWithGST))	.sum());
		
	
		

		/**
		 * Amount is left after deducting Rent and GST  for all the demands. Extract
		 * Rent penalty and GST Penalty.
		 */
		List<EstateRentCollection> principalCollections = effectiveAmount > 0
				? extractPenalty(demands, effectiveAmount, payment.getPaymentDate(),interestRate,isFixGST)
				: Collections.emptyList();
		effectiveAmount -= principalCollections.stream().mapToDouble(EstateRentCollection::getRentPenaltyWithGSTPenalty).sum();
		//effectiveAmount -= principalCollections.stream().mapToDouble(EstateRentCollection::getGstPenaltyCollected).sum();
		
		/**
		 * update the interestSinceDate if Any amount paid 
		 */
		setDemandSettlementDate(demands,payment.getPaymentDate());
		
		
		/**
		 * Amount is left after deducting all the Rent,GST , Rent Penalty and GST penalty amounts. Put it back in the
		 * account
		 */
		account.setRemainingAmount(effectiveAmount);
		account.setRemainingSince(payment.getPaymentDate());

		/**
		 * Mark payment as processed.
		 */
		payment.setProcessed(true);
		return Stream.of(interestCollections, principalCollections).flatMap(x -> x.stream())
				.collect(Collectors.toList());

		


		
	}
	private void setDemandSettlementDate(List<EstateDemand> demands,long paymentTimestamp) {
		
		for (EstateDemand demand : demands) {
			if(demand.getCollectedRent()>0 || demand.getCollectedGST()>0 || demand.getCollectedRentPenalty() >0 || demand.getCollectedGSTPenalty()>0 ) {
				demand.setInterestSince(paymentTimestamp);
			}
		
		}
		
	}
	private void calculateInterest(List<EstateDemand> demands, long paymentTimestamp,double interestRate,boolean isFixGST,double rentInterest) {
		
List<EstateDemand> filteredDemands = demands.stream().filter(EstateDemand::isUnPaid).collect(Collectors.toList());
		
		
		double GSTinterest=0;
		double RentInterest=0;
		
		for (EstateDemand demand : filteredDemands) {
			GSTinterest=RentInterest=0;	
			LocalDate paymentDate = getLocalDate(paymentTimestamp);



			LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());

			long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, paymentDate);
			//&& demand.getIsPrevious()
			if (noOfDaysForInterestCalculation <=demand.getInitialGracePeriod() ) {
				continue;
			}
			 GSTinterest = demand.getRemainingGST()*(interestRate/100)* noOfDaysForInterestCalculation/365;
			GSTinterest+=demand.getRemainingGSTPenalty();
			
			
			if(isFixGST) {
				if(!demand.getIsPrevious() )
					RentInterest=demand.getRent()*rentInterest/100;
				
			}else {
				RentInterest=demand.getRemainingRent()*(rentInterest/100)* noOfDaysForInterestCalculation/365;				
				
			}
			
				
			if(demand.getIsPrevious() || !isFixGST)
			   RentInterest+=demand.getRemainingRentPenalty();
			
			//demand.setPenaltyInterest(RentInterest);
			//demand.setGstInterest(GSTinterest);
			demand.setRemainingRentPenalty(RentInterest);
			demand.setRemainingGSTPenalty(GSTinterest);
				
			
			
			
		}
	}
	private List<EstateRentCollection> extractPenalty(List<EstateDemand> demands, double paymentAmount,
			long paymentTimestamp,double interestRate,boolean isFixGST) {
		ArrayList<EstateRentCollection> collections = new ArrayList<EstateRentCollection>();
		List<EstateDemand> filteredDemands = demands.stream().filter(EstateDemand::isUnPaid).collect(Collectors.toList());
		
		double rentPenaltyToBePaid=0;
		double gstPenaltyToBePaid=0;
		double GSTinterest=0;
		double RentInterest=0;
		
		for (EstateDemand demand : filteredDemands) {
			if (paymentAmount <= 0) {
				break;
			}
//			GSTinterest=RentInterest=0;	
//			if(! demand.getIsPrevious() ) {
//				LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
//				LocalDate paymentDate = getLocalDate(paymentTimestamp);
//	
//				long noOfDaysBetweenGenerationAndPayment = ChronoUnit.DAYS.between(demandGenerationDate, paymentDate);
//				if (noOfDaysBetweenGenerationAndPayment <= demand.getInitialGracePeriod()) {
//					demand.setRemainingPrincipalAndUpdatePaymentStatus(demand.getRemainingGSTPenalty(),demand.getRemainingRentPenalty());
//
//					continue;
//				}
//	
//				LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());
//	
//				long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, paymentDate);
//				
//				if (noOfDaysForInterestCalculation <=demand.getInitialGracePeriod()) {
//					continue;
//				}
//				
//				
//				
////				if(demand.getRemainingRent()==0 && demand.getRemainingGST()==0  ) {
////					GSTinterest=demand.getRemainingGSTPenalty();
////					RentInterest=demand.getRemainingRentPenalty();
////					
////				}
////				else {
//					
//				    GSTinterest = demand.getRemainingGST()*(interestRate/100)* noOfDaysForInterestCalculation/365;
//					GSTinterest+=demand.getRemainingGSTPenalty();
//					
//					if(isFixGST) {
//						RentInterest=demand.getRemainingRent()*0.10;
//						
//					}else {
//						RentInterest=demand.getRemainingRent()*(0.10)* noOfDaysForInterestCalculation/365;
//						
//						
//					}
//					RentInterest+=demand.getRemainingRentPenalty();
//					
//					
//				//}
//				
//				demand.setPenaltyInterest(RentInterest);
//				demand.setGstInterest(GSTinterest);
//					
//			}
//			else {
//				GSTinterest=demand.getRemainingGSTPenalty();
//				RentInterest=demand.getRemainingRentPenalty();
//			}
		
			RentInterest=demand.getRemainingRentPenalty();
			GSTinterest=demand.getRemainingGSTPenalty();
		if (RentInterest + GSTinterest <= paymentAmount) {
			
			rentPenaltyToBePaid=RentInterest;
			gstPenaltyToBePaid=GSTinterest;
			
		} else {
			/**
			 * If no sufficient amount , pay half amount as a rent penalty and half as a GST Penalty
			 */
			double halfOfTheRemainingAmount = paymentAmount / 2;
			rentPenaltyToBePaid=gstPenaltyToBePaid=halfOfTheRemainingAmount;
			
			if (halfOfTheRemainingAmount > GSTinterest) {
				rentPenaltyToBePaid=halfOfTheRemainingAmount+(halfOfTheRemainingAmount-GSTinterest);
				gstPenaltyToBePaid=GSTinterest;
				
			}
			
			
		}
		demand.setCollectedRentPenalty(rentPenaltyToBePaid);
		demand.setCollectedGSTPenalty(gstPenaltyToBePaid);
		demand.setRemainingRentPenalty(demand.getRemainingRentPenalty()-rentPenaltyToBePaid);
		demand.setRemainingGSTPenalty(GSTinterest-gstPenaltyToBePaid);
		
		paymentAmount-=(rentPenaltyToBePaid+gstPenaltyToBePaid);

		

			
			collections.add(EstateRentCollection.builder().demandId(demand.getId())
					.rentPenaltyCollected(rentPenaltyToBePaid)
					.gstPenaltyCollected(gstPenaltyToBePaid)
					.rentPenaltyWithGSTPenalty(rentPenaltyToBePaid+gstPenaltyToBePaid)
					.collectedAt(paymentTimestamp).build());
			demand.setRemainingPrincipalAndUpdatePaymentStatus(demand.getRemainingGSTPenalty(),demand.getRemainingRentPenalty());
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
				/**
				 * If no sufficient amount calculate the amount paid for Rent and GST
				 */
				
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
			//demand.setInterestSince(paymentTimeStamp);
			
			

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
	public List<EstateRentCollection> settle(final List<EstateDemand> demandsToBeSettled, List<EstatePayment> payments,
			final EstateAccount account, double interestRate,boolean isFixGST,double rentInterest) {
		if(null==payments)
			 payments = Collections.emptyList();
		
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
			return settlePayment(demandsToBeSettled, payment, account, interestRate, isFixGST, rentInterest);
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
			EstatePayment payment = EstatePayment.builder().rentReceived(0D).paymentDate(demand.getGenerationDate())
					.build();
			List<EstateRentCollection> settledCollections = settlePayment(Collections.singletonList(demand), payment, account,
					interestRate,isFixGST,rentInterest);
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
			double interestRate, Long fromDateTimestamp, Long toDateTimestamp,boolean isFixGST,double rentInterest) {
		if(null==payments)
		 payments = Collections.emptyList();
		if(demands==null)
			demands=Collections.emptyList();
		
		
		
		long endTimestamp = toDateTimestamp == null ? System.currentTimeMillis() : toDateTimestamp.longValue();
		demands = demands.stream().filter(demand -> demand.getGenerationDate() <= endTimestamp)
				.collect(Collectors.toList());
		
		payments = payments.stream().filter(payment -> payment.getRentReceived() > 0)
				.filter(p -> p.getPaymentDate() <= endTimestamp).collect(Collectors.toList());
		Collections.sort(demands);
		Collections.sort(payments);
		
//		List<EstateDemand> consolidatedDemands = demands.stream()
//				.filter(demand -> demand.getIsPrevious())
//				.collect(Collectors.toList());
//		
//		demands.removeAll(consolidatedDemands);
//		demands.addAll(0,consolidatedDemands);
		
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
						EstateDemand.builder().generationDate(endTimestamp).collectedRent(0D).build(), statement,isFixGST,rentInterest);
				reachedLast = true;
			} 
			//no demand remaining
			else if (currentDemand == null) {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
						statement,isFixGST,rentInterest);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			} 
			//no payment remaining
			else if (currentPayment == null) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
						statement,isFixGST,rentInterest);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else if (currentDemand.getGenerationDate() <= currentPayment.getPaymentDate()) {
				demandsToBeSettled.add(this.cloneDemand(currentDemand));
				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
						statement,isFixGST,rentInterest);
				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
			} else {
				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
						statement,isFixGST,rentInterest);
				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
			}
			statement.setRemainingPrincipal(rentSummary.getBalanceRent());
			//statement.setRemainingInterest(rentSummary.getBalanceRentPenalty());
			statement.setRemainingInterest(rentSummary.getBalanceInterest());
			statement.setRemainingBalance(rentSummary.getBalanceAmount());
			statement.setRemainingGST(rentSummary.getBalanceGST());
			statement.setRemainingRentPenalty(rentSummary.getBalanceRentPenalty());
			statement.setRemainingGSTPenalty(rentSummary.getBalanceGSTPenalty());
		//	statement.setReceiptNo(currentPayment!=null?currentPayment.getReceiptNo():"");
			statement.setRent(rentSummary.getRent());
			statement.setCollectedRent(rentSummary.getCollectedRent());
			statement.setRentPenalty(rentSummary.getRentPenalty());
			statement.setGst(rentSummary.getGst());
			statement.setCollectedGST(rentSummary.getCollectedGST());
			statement.setRentPenalty(rentSummary.getRentPenalty());
			statement.setCollectedRentPenalty(rentSummary.getCollectedRentPenalty());
			statement.setGSTPenalty(rentSummary.getGSTPenalty());
			statement.setCollectedGSTPenalty(rentSummary.getCollectedGSTPenalty());
			statement.setIsPrevious(rentSummary.getIsPrevious());
			accountStatementItems.add(statement);
			if (reachedLast) {
				break;
			}
		}
		return accountStatementItems;
	}
	
	private EstateRentSummary calculateSummaryForPayment(double interestRate, EstateAccount rentAccount,
			List<EstateDemand> demandsToBeSettled, EstatePayment currentPayment, EstateAccountStatement statement,boolean isFixGST,double rentInterest) {
		currentPayment = this.clonePayment(currentPayment);
		this.settle(demandsToBeSettled, Collections.singletonList(currentPayment), rentAccount, interestRate,isFixGST,rentInterest);
		EstateRentSummary rentSummary = calculateRentSummaryAtPayment(demandsToBeSettled, rentAccount, interestRate,
				currentPayment.getPaymentDate());
		statement.setDate(currentPayment.getPaymentDate());
		statement.setAmount(currentPayment.getRentReceived());
		statement.setType(Type.C);
		statement.setReceiptNo(currentPayment.getReceiptNo());
		return rentSummary;
	}
	
	private EstateRentSummary getSummaryForDemand(double interestRate, EstateAccount rentAccount,
			List<EstateDemand> demandsToBeSettled, EstateDemand currentDemand, EstateAccountStatement statement,boolean isFixGST,double rentInterest) {
		EstateRentSummary rentSummary;
		this.settle(demandsToBeSettled, Collections.emptyList(), rentAccount, interestRate,isFixGST,rentInterest);
		rentSummary = calculateRentSummaryAt(demandsToBeSettled, rentAccount, interestRate,
				currentDemand.getGenerationDate(),isFixGST,rentInterest);
		statement.setDate(currentDemand.getGenerationDate());
		//statement.setAmount(currentDemand.getCollectedRent());
		statement.setType(Type.D);
		return rentSummary;
	}
	
	private EstatePayment clonePayment(EstatePayment rentPayment) {
		return EstatePayment.builder().rentReceived(rentPayment.getRentReceived())
				.receiptDate(rentPayment.getReceiptDate())
				.receiptNo(rentPayment.getReceiptNo())
				.paymentDate(rentPayment.getPaymentDate())
				.processed(false).build();
	}

	@Override
	public EstateRentSummary calculateRentSummary(List<EstateDemand> demands, EstateAccount rentAccount, double interestRate,boolean isFixGST, double rentInterest) {
		
			if(demands==null)
				demands=Collections.emptyList();
		return this.calculateRentSummaryAt(demands, rentAccount, interestRate, System.currentTimeMillis(),isFixGST,rentInterest);
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
			long atTimestamp,boolean isFixGST, double rentInterest ) {
		
		final LocalDate atDate = getLocalDate(atTimestamp);
		
			if(demands==null)
				demands=Collections.emptyList();
			
//			List<EstateDemand> consolidatedDemands = demands.stream()
//					.filter(demand -> demand.getIsPrevious())
//					.collect(Collectors.toList());
//			demands.removeAll(consolidatedDemands);
//			demands.addAll(0,consolidatedDemands);
		
		return demands.stream().filter(EstateDemand::isUnPaid).reduce(
				EstateRentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(), (summary, demand) -> {
					double calculatedInterest = 0D;
					double calculateRentInterest=0D;
					 /** Summarize the result.
					 */
					
					/**
					 * Calculate interest till atDate
					 */
					//if(demand.getRemainingRent()!=0 && demand.getRemainingGST()!=0) {
						
					LocalDate demandGenerationDate = getLocalDate(demand.getGenerationDate());
					 calculatedInterest = 0D;
					 calculateRentInterest=0D;
					 if(demand.getRemainingRent()>0 || demand.getRemainingGST()>0) {
					long noOfDaysBetweenGenerationAndPayment = 1
							+ ChronoUnit.DAYS.between(demandGenerationDate, atDate);
//					if(noOfDaysBetweenGenerationAndPayment<0)
//						noOfDaysBetweenGenerationAndPayment*=-1;
					if (noOfDaysBetweenGenerationAndPayment > demand.getInitialGracePeriod()) {
						if(demand.getInterestSince()==null)
							demand.setInterestSince(demand.getGenerationDate());
						LocalDate demandInterestSinceDate = getLocalDate(demand.getInterestSince());
						//for testing TODO
						long noOfDaysForInterestCalculation = ChronoUnit.DAYS.between(demandInterestSinceDate, atDate);
						//if(noOfDaysForInterestCalculation<0)
						//	noOfDaysForInterestCalculation*=-1;
						if(noOfDaysForInterestCalculation>10) {
								calculatedInterest = demand.getRemainingGST() * (interestRate/100)
									* noOfDaysForInterestCalculation / 365 ;
	//						demand.setGstInterest(calculatedInterest);
	//						demand.setRemainingGSTPenalty(calculatedInterest);
							
							if(isFixGST) {
								if(!demand.getIsPrevious())
									calculateRentInterest=demand.getRent()*rentInterest/100;
								//demand.setPenaltyInterest(demand.getRent()*0.10);
							//	demand.setRemainingRentPenalty(demand.getRent()*0.10);
							}else {
								calculateRentInterest=demand.getRemainingRent()*(rentInterest/100)* noOfDaysForInterestCalculation/365;
	//							demand.setPenaltyInterest(demand.getRent()*(0.10)* noOfDaysForInterestCalculation/365);
	//							demand.setRemainingRentPenalty(demand.getRent()*(0.10)* noOfDaysForInterestCalculation/365);
							}
							
							
							
						}
						else {
							if(!demand.getIsPrevious()) {
							//calculateRentInterest+=demand.getRemainingRentPenalty();
							calculatedInterest+=demand.getRemainingGSTPenalty();
							}
						}
					}else  {
						if(!demand.getIsPrevious()) {
							//calculateRentInterest+=demand.getRemainingRentPenalty();
							calculatedInterest+=demand.getRemainingGSTPenalty();
						}
					}
				}else {
					if(!demand.getIsPrevious()) {
					//calculateRentInterest+=demand.getRemainingRentPenalty();
					calculatedInterest+=demand.getRemainingGSTPenalty();
					}
				}
					
					
						
//					else {
//						calculatedInterest=0;
//						demand.setRemainingRentPenalty(0D);
////						demand.setPenaltyInterest(0D);
////						demand.setGstInterest(0D);
////						demand.setRemainingGST(0D);
//					}
//						
					//}
					if(demand.getIsPrevious()) {
						calculatedInterest+=demand.getRemainingGSTPenalty();
						calculateRentInterest+=demand.getRemainingRentPenalty();
					}
					return EstateRentSummary.builder()
							.rent(demand.getRent())
							.collectedRent(demand.getCollectedRent()!=null?demand.getCollectedRent():0)
							.balanceRent(summary.getBalanceRent() + demand.getRemainingRent())
							.gst(demand.getGst())
							
							.collectedGST(demand.getCollectedGST()!=null?demand.getCollectedGST():0)
							.balanceGST(summary.getBalanceGST() + demand.getRemainingGST())
						    // .GSTPenalty( calculatedInterest) 				
						     .GSTPenalty( summary.getBalanceGSTPenalty()+calculatedInterest) 			
							.collectedGSTPenalty(demand.getCollectedGSTPenalty()!=null?demand.getCollectedGSTPenalty():0)
							//.balanceGSTPenalty(summary.getBalanceGSTPenalty() + demand.getRemainingGSTPenalty())
							.balanceGSTPenalty(summary.getBalanceGSTPenalty() + calculatedInterest)
							//.balanceRentPenalty(summary.getBalanceGSTPenalty()+demand.getPenaltyInterest())
							.balanceRentPenalty(summary.getBalanceRentPenalty()+calculateRentInterest)
				            .collectedRentPenalty(  demand.getCollectedRentPenalty()!=null?demand.getCollectedRentPenalty():0)
							
							.balanceAmount(rentAccount.getRemainingAmount())
							.isPrevious(demand.getIsPrevious())
							.rentPenalty(summary.getRentPenalty()+calculateRentInterest)
							.build();
							
				}, (summary, demand) -> summary);
	}
	
	public EstateRentSummary calculateRentSummaryAtPayment(List<EstateDemand> demands, EstateAccount rentAccount, double interestRate,
			long atTimestamp ) {
		
			if(demands==null)
				demands=Collections.emptyList();
//			List<EstateDemand> consolidatedDemands = demands.stream()
//					.filter(demand -> demand.getIsPrevious())
//					.collect(Collectors.toList());
//					
//					demands.removeAll(consolidatedDemands);
		//	demands.addAll(0,consolidatedDemands);
		return demands.stream().filter(EstateDemand::isUnPaid).reduce(
				EstateRentSummary.builder().balanceAmount(rentAccount.getRemainingAmount()).build(), (summary, demand) -> {
				
					
					
					return EstateRentSummary.builder()
							.balanceGSTPenalty(summary.getBalanceGSTPenalty() + demand.getRemainingGSTPenalty())
							.balanceRentPenalty(summary.getBalanceRentPenalty()+demand.getRemainingRentPenalty())
							
							.rent(demand.getRent())
							.collectedRent(demand.getCollectedRent()!=null?demand.getCollectedRent():0)
							.balanceRent(summary.getBalanceRent() + demand.getRemainingRent())
							.gst(demand.getGst())
							
							.collectedGST(demand.getCollectedGST()!=null?demand.getCollectedGST():0)
							.balanceGST(summary.getBalanceGST() + demand.getRemainingGST())
						    // .GSTPenalty( calculatedInterest) 				
						    // .GSTPenalty( summary.getBalanceGSTPenalty()+calculatedInterest) 			
							.collectedGSTPenalty(demand.getCollectedGSTPenalty()!=null?demand.getCollectedGSTPenalty():0)
							//.balanceGSTPenalty(summary.getBalanceGSTPenalty() + demand.getRemainingGSTPenalty())
							
							//.balanceRentPenalty(summary.getBalanceGSTPenalty()+demand.getPenaltyInterest())
							
				            .collectedRentPenalty(  demand.getCollectedRentPenalty()!=null?demand.getCollectedRentPenalty():0)
							
							.balanceAmount(rentAccount.getRemainingAmount())
							.isPrevious(demand.getIsPrevious())
							//.rentPenalty(summary.getRentPenalty()+calculateRentInterest)
							.build();
							
				}, (summary, demand) -> summary);
	}
	
	private EstateDemand cloneDemand(EstateDemand rentDemand) {
		
		if(rentDemand.getIsPrevious()) {
			return EstateDemand.builder().collectedRent(rentDemand.getCollectedRent())
			.status(PaymentStatusEnum.UNPAID)
			.generationDate(rentDemand.getGenerationDate())
			.rent(rentDemand.getRent())
			.gst(rentDemand.getGst())
			.interestSince(rentDemand.getGenerationDate())
			.initialGracePeriod(rentDemand.getInitialGracePeriod())
			.remainingRent(rentDemand.getRent())
			.remainingGST(rentDemand.getGst())
			.isPrevious(rentDemand.getIsPrevious())
			.id(rentDemand.getId())
			.remainingGSTPenalty(rentDemand.getGstInterest())
			.remainingRentPenalty(rentDemand.getPenaltyInterest())
			.gstInterest(rentDemand.getGstInterest())
			.penaltyInterest(rentDemand.getPenaltyInterest())
			.build();
			
		}else {
		return EstateDemand.builder().collectedRent(rentDemand.getCollectedRent())
				.status(PaymentStatusEnum.UNPAID)
				//.status(rentDemand.getStatus())
				.generationDate(rentDemand.getGenerationDate())
				.rent(rentDemand.getRent())
				.gst(rentDemand.getGst())
				.interestSince(rentDemand.getGenerationDate())
				.initialGracePeriod(rentDemand.getInitialGracePeriod())
				.remainingRent(rentDemand.getRent())
				.remainingGST(rentDemand.getGst())
				.isPrevious(rentDemand.getIsPrevious())
				.id(rentDemand.getId())
				.build();
				
		}
				
				
	}
}
