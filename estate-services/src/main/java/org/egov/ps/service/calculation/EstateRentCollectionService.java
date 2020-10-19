package org.egov.ps.service.calculation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;

public class EstateRentCollectionService implements IEstateRentCollectionService{
	public static void main(String[] args) {
		System.out.println("hello");
	}

	public void settle(List<EstateDemand> lstDemandProcess, EstatePayment payment) {
		double rentReceived = payment.getRentReceived();
		for (EstateDemand demandProcess : lstDemandProcess) {
			double paybleRent=0;
			double paybleGst=0;
			//setting rent and gst amount
			if (demandProcess.getRent() + demandProcess.getGst() <= rentReceived) {
				paybleRent=demandProcess.getRent();
				paybleGst=demandProcess.getGst();
				rentReceived -= demandProcess.getRent() + demandProcess.getGst();

			} else {
				// some calculation
				
				paybleRent = rentReceived*100/(100+demandProcess.getGstInterest());
				paybleGst= rentReceived-paybleRent;
				if(paybleGst>demandProcess.getGst())
					paybleRent=paybleRent+(paybleGst-demandProcess.getGst());
				
			}
			demandProcess.setCollectedRent(paybleRent);
			demandProcess.setCollectedGST(paybleGst);
			
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

//	public void settle(List<EstateDemand> lstDemands, List<EstatePayment> lstPayments) {
//
//		for (EstatePayment payment : lstPayments) {
//			List<EstateDemand> lstDemandProcess = new ArrayList<EstateDemand>();
//			Date paymentDate = new Date(payment.getReceiptDate());
//			for (EstateDemand demand : lstDemands) {
//				Date demandDate = new Date(demand.getDemandDate());
//				if (demandDate.compareTo(paymentDate) <= 0) {
//					lstDemandProcess.add(demand);
//				}
//			}
//		}
//	}
	
	private List<EstateRentCollection> settlePayment(final List<EstateDemand> demandsToBeSettled, final EstatePayment payment,
			final EstateAccount account, double interestRate,boolean isFixGST){
		
		/**
		 * Each payment will only operate on the demands generated before it is paid.
		 */
		List<EstateDemand> demands = demandsToBeSettled.stream()
				.filter(demand -> demand.isUnPaid() && demand.getDemandDate() <= payment.getReceiptDate())
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
		effectiveAmount -= interestCollections.stream().mapToDouble(EstateRentCollection::getRentCollected)		
				.sum();
		
		effectiveAmount -= interestCollections.stream().mapToDouble(EstateRentCollection::getGstCollected)		
				.sum();

		/**
		 * Amount is left after deducting interest for all the demands. Extract
		 * Principal.
		 */
		List<EstateRentCollection> principalCollections = effectiveAmount > 0
				? extractPenalty(demands, effectiveAmount, payment.getReceiptDate())
				: Collections.emptyList();
		effectiveAmount -= principalCollections.stream().mapToDouble(EstateRentCollection::getRentPenaltyCollected).sum();
		effectiveAmount -= principalCollections.stream().mapToDouble(EstateRentCollection::getGstPenaltyCollected).sum();

		return principalCollections;


		
	}
	
	private List<EstateRentCollection> extractPenalty(List<EstateDemand> demands, double paymentAmount,
			long paymentTimestamp) {
		ArrayList<EstateRentCollection> collections = new ArrayList<EstateRentCollection>();
		List<EstateDemand> filteredDemands = demands.stream().filter(EstateDemand::isUnPaid).collect(Collectors.toList());
		
		double rentPenaltyToBePaid=0;
		double gstPenaltyToBePaid=0;
		
		for (EstateDemand demand : filteredDemands) {
			if (paymentAmount <= 0) {
				break;
			}
					
		if (demand.getPenaltyInterest() + demand.getGstInterest() <= paymentAmount) {
			
			rentPenaltyToBePaid=demand.getPenaltyInterest();
			gstPenaltyToBePaid=demand.getGstInterest();
		} else {
			// 50-50
			//100 gst 80 
			//rent pen =100+20
			
			double halfOfTheRemainingAmount = paymentAmount / 2;
			rentPenaltyToBePaid=gstPenaltyToBePaid=halfOfTheRemainingAmount;
			
			if (halfOfTheRemainingAmount > demand.getGstInterest()) {
				rentPenaltyToBePaid=halfOfTheRemainingAmount+(halfOfTheRemainingAmount-demand.getGstInterest());
				gstPenaltyToBePaid=demand.getGstInterest();
				
			}
			
			demand.setCollectedRentPenalty(rentPenaltyToBePaid);
			demand.setCollectedGSTPenalty(gstPenaltyToBePaid);
				
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
			
			if (demand.getRent() + demand.getGst() <= paymentAmount) {
				rentTobePaid=demand.getRent();
				gstToBePaid=demand.getGst();
				

			} else {
				// some calculation
				
				rentTobePaid = paymentAmount*100/(100+demand.getGstInterest());
				gstToBePaid= paymentAmount-rentTobePaid;
				if(gstToBePaid>demand.getGst())
					rentTobePaid+=(gstToBePaid-demand.getGst());
				
			}
			demand.setCollectedRent(rentTobePaid);
			demand.setCollectedGST(gstToBePaid);
			collections.add(EstateRentCollection.builder().rentCollected(rentTobePaid)
					.gstCollected(gstToBePaid)
					.collectedAt(paymentTimeStamp)
					.demandId(demand.getId()).build());
			
			paymentAmount -= (rentTobePaid + gstToBePaid);
			demand.setPaymentSince(paymentTimeStamp);
			
			

//			if(!isFixGST) {
//				LocalDate demandGenerationDate = getLocalDate(demand.getDemandDate());
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

//		if (account.getRemainingAmount() == 0
//				|| !this.didExtractAllDemandsInterest(demandsToBeSettled, account.getRemainingSince())) {
//			return collections;
//		}

//		/**
//		 * We have positive account balance.
//		 */
//		List<RentDemand> newerDemands = demandsToBeSettled.stream()
//				.filter(d -> d.getGenerationDate() > account.getRemainingSince()).filter(RentDemand::isUnPaid)
//				.collect(Collectors.toList());
//		if (newerDemands.size() == 0) {
//			return collections;
//		}
//
//		/**
//		 * In the case of 1) demand generation at 1st of every month. 2) More amount
//		 * payed toward the end which should be adjusted to left over demands.
//		 */
//		ArrayList<RentCollection> result = new ArrayList<RentCollection>(collections);
//
//		/**
//		 * Settle each demand by creating an empty payment with the demand generation
//		 * date.
//		 */
//		for (RentDemand demand : newerDemands) {
//			RentPayment payment = RentPayment.builder().amountPaid(0D).dateOfPayment(demand.getGenerationDate())
//					.build();
//			List<RentCollection> settledCollections = settlePayment(Collections.singletonList(demand), payment, account,
//					0);
//			if (settledCollections.size() == 0) {
//				continue;
//			}
//			result.addAll(settledCollections);
//			if (account.getRemainingAmount() == 0) {
//				break;
//			}
//		}
		return collections;
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
	
//	public List<RentAccountStatement> getAccountStatement(List<EstateDemand> demands, List<EstatePayment> payments,
//			double interestRate, Long fromDateTimestamp, Long toDateTimestamp) {
//		long endTimestamp = toDateTimestamp == null ? System.currentTimeMillis() : toDateTimestamp.longValue();
//		demands = demands.stream().filter(demand -> demand.getDemandDate() <= endTimestamp)
//				.collect(Collectors.toList());
//		payments = payments.stream().filter(payment -> payment.getRentReceived() > 0)
//				.filter(p -> p.getReceiptDate() <= endTimestamp).collect(Collectors.toList());
//		Collections.sort(demands);
//		Collections.sort(payments);
//		List<RentAccountStatement> accountStatementItems = new ArrayList<RentAccountStatement>();
//		EstateAccount rentAccount = EstateAccount.builder().remainingAmount(0D).build();
//		List<EstateDemand> demandsToBeSettled = new ArrayList<EstateDemand>(demands.size());
//		Iterator<EstateDemand> demandIterator = demands.iterator();
//		Iterator<EstatePayment> paymentIterator = payments.iterator();
//		EstateDemand currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
//		EstatePayment currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
//		while (true) {
//			boolean reachedLast = false;
//			RentSummary rentSummary;
//			RentAccountStatement statement = RentAccountStatement.builder().build();
//			if (currentDemand == null && currentPayment == null) {
//				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled,
//						EstateDemand.builder().demandDate(endTimestamp).collectionPrincipal(0D).build(), statement);
//				reachedLast = true;
//			} else if (currentDemand == null) {
//				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
//						statement);
//				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
//			} else if (currentPayment == null) {
//				demandsToBeSettled.add(this.cloneDemand(currentDemand));
//				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
//						statement);
//				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
//			} else if (currentDemand.getGenerationDate() <= currentPayment.getDateOfPayment()) {
//				demandsToBeSettled.add(this.cloneDemand(currentDemand));
//				rentSummary = getSummaryForDemand(interestRate, rentAccount, demandsToBeSettled, currentDemand,
//						statement);
//				currentDemand = demandIterator.hasNext() ? demandIterator.next() : null;
//			} else {
//				rentSummary = calculateSummaryForPayment(interestRate, rentAccount, demandsToBeSettled, currentPayment,
//						statement);
//				currentPayment = paymentIterator.hasNext() ? paymentIterator.next() : null;
//			}
//			statement.setRemainingPrincipal(rentSummary.getBalancePrincipal());
//			statement.setRemainingInterest(rentSummary.getBalanceInterest());
//			statement.setRemainingBalance(rentSummary.getBalanceAmount());
//			accountStatementItems.add(statement);
//			if (reachedLast) {
//				break;
//			}
//		}
//		return accountStatementItems;
//	}



}
