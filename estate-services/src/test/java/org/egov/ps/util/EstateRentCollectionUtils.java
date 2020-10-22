package org.egov.ps.util;



import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import org.egov.ps.web.contracts.EstateAccountStatement;
import org.egov.ps.web.contracts.EstateAccountStatement.Type;

public class EstateRentCollectionUtils {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");

	public void printStatement(List<EstateAccountStatement> accountStatementItems) {
		if (CollectionUtils.isEmpty(accountStatementItems)) {
			System.out.println("=> Statement is empty <==");
		}
		// accountStatementItems.forEach(statementItem -> {
		// System.out.println(statementItem);
		// });
		System.out.println(String.format("%10s |%10s |%10s |"
				+ "%14s|%14s|%14s"
				+ "|%14s|%14s|%14s"
				+ "|%14s|%14s|%14s"
				+ "|%14s|%14s|%14s"
				+ "|%13s |%13s |%15s |%5s", 
				"Date", "Amount", "Type",
				"Rent","Rent Received","Principal Due", 
				"GST","GST Received","GST Due",
				"Interest Panelty","Interest Panelty Collected","Interest Panelty Due",
				"GST Panelty","GST Panelty Collected","GST Panelty Due",
				"Interest Due", "Total Due", "Account Balance","Receipt No"));
		System.out.println(
				"===============================================================================================================================================================================================================================================================================================================================================================================================================");
		accountStatementItems.forEach(item -> {
			System.out.println(String.format("%10s |%10.2f |%10s|"
					+ "%14.2f |%14.2f |%14.2f "
					+ "|%14.2f |%14.2f |%14.2f  "
					+ "|%17.2f |%17.2f |%17.2f  "
					+ "|%11.2f |%11.2f |%11.2f  "
					+ "| %13.2f |%13.2f |%15.2f |%5s",
					dateFormat.format(new Date(item.getDate())), item.getAmount(),
					item.getType() == EstateAccountStatement.Type.C ? "Payment" : "Rent", 
							item.getRent(),item.getCollectedRent(),item.getRemainingPrincipal(),
							item.getGst(),item.getCollectedGST(),item.getRemainingGST(),
							item.getRentPanelty(),item.getCollectedRentPanelty(),item.getRemainingRentPenalty(),
							item.getGSTPanelty(),item.getCollectedGSTPanelty(),item.getRemainingGSTPenalty(),
					item.getRemainingInterest(), item.getDueAmount(), item.getRemainingBalance(),item.getReceiptNo()));
		});
	}

	public void reconcileStatement(List<EstateAccountStatement> accountStatementItems, double interestRate) {
		Iterator<EstateAccountStatement> accountStatementIterator = accountStatementItems.iterator();
		EstateAccountStatement prevItem = null;
		EstateAccountStatement currentItem = null;
		while (accountStatementIterator.hasNext()) {
			prevItem = currentItem;
			currentItem = accountStatementIterator.next();
			if (prevItem == null) {
				continue;
			}
			/**
			 * If account has balance, it should mean principal, interest and due amounts
			 * should be zero.
			 */
			if (!this.diffInRange(currentItem.getRemainingBalance(), 0, 0.0000001)) {
				assertEquals(dateFormat.format(currentItem.getDate()), currentItem.getRemainingPrincipal(), 0D, 0.0);
				assertEquals(dateFormat.format(currentItem.getDate()), currentItem.getRemainingInterest(), 0D, 0.0);
				assertEquals(dateFormat.format(currentItem.getDate()), currentItem.getDueAmount(), 0D, 0.0);
			}
			boolean isPayment = currentItem.getType() == EstateAccountStatement.Type.C;

			/**
			 * If there was no principal earlier, there should be no interest now. Any
			 * credit should reduce the principal to zero and accumulate into account if any
			 */
			final double totalAmount = isPayment ? -currentItem.getAmount() : currentItem.getAmount();
			if (prevItem.getRemainingPrincipal() == 0) {
				assertEquals(0, currentItem.getRemainingInterest(), 0D);
				assertEquals(Math.max(0, totalAmount - prevItem.getRemainingBalance()),
						currentItem.getRemainingPrincipal(), 0D);
				assertEquals(Math.max(0, prevItem.getRemainingBalance() - totalAmount),
						currentItem.getRemainingBalance(), 0D);
			} else {

				long daysBetween = getDaysBetween(prevItem.getDate(), currentItem.getDate());
				/**
				 * DISCLAIMER: There is no way to check the exact interest when the payment date
				 * is close to the last demand generation date. For all practical purposes it is
				 * sufficient to consider only one demand before the current payment for tax
				 * grace period.
				 */
				double interestBetweenRange = prevItem.getRemainingPrincipal() * daysBetween * interestRate / 100 / 365;
				if (currentItem.getType() == Type.C && prevItem.getType() == Type.D && daysBetween > 0
						&& daysBetween < 10) {
					interestBetweenRange = (prevItem.getRemainingPrincipal() - prevItem.getAmount()) * daysBetween
							* interestRate / 100 / 365;
				}
				double expectedInterest = isPayment
						? Math.max(0, prevItem.getRemainingInterest() + interestBetweenRange - currentItem.getAmount())
						: prevItem.getRemainingInterest() + interestBetweenRange;
				assertInRange(dateFormat.format(currentItem.getDate()), expectedInterest,
						currentItem.getRemainingInterest());

				double expectedPrincipal = isPayment
						? (expectedInterest > 0 ? prevItem.getRemainingPrincipal()
								: Math.max(0,
										(prevItem.getRemainingPrincipal() + prevItem.getRemainingInterest()
												+ interestBetweenRange) - currentItem.getAmount()))
						: prevItem.getRemainingPrincipal() + currentItem.getAmount();
				assertInRange(dateFormat.format(currentItem.getDate()), expectedPrincipal,
						currentItem.getRemainingPrincipal());

				double expectedBalance = isPayment ? Math.max(0, currentItem.getAmount()
						- (prevItem.getRemainingPrincipal() + prevItem.getRemainingInterest() + interestBetweenRange))
						: 0;
				assertInRange(dateFormat.format(currentItem.getDate()), expectedBalance,
						currentItem.getRemainingBalance());
			}
		}
	}

	private static final double ERROR_RANGE = 6.0D;

	private void assertInRange(String message, Double expected, Double actual) {
		if (!diffInRange(expected, actual, 0.000001)) {
			System.out.println(String.format("%s, error range %.10f", message, expected - actual));
		}
		assertEquals(message, expected, actual, ERROR_RANGE);
	}

	private boolean diffInRange(double expected, double actual, double range) {
		try {
			assertEquals(expected, actual, range);
			return true;
		} catch (AssertionError err) {
			return false;
		}
	}

	private long getDaysBetween(long startTimestamp, long endTimestamp) {
		LocalDate startDate = Instant.ofEpochMilli(startTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = Instant.ofEpochMilli(endTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
		return ChronoUnit.DAYS.between(startDate, endDate);
	}
}
