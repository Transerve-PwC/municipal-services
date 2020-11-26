package org.egov.ps.service.calculation;

import java.util.List;

import org.egov.ps.web.contracts.EstateAccount;
import org.egov.ps.web.contracts.EstateAccountStatement;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;
import org.egov.ps.web.contracts.EstateRentCollection;
import org.egov.ps.web.contracts.EstateRentSummary;

public interface IEstateRentCollectionService {

	/**
	 * Get the list of collections for the given demand and payments for the same
	 * property.
	 * 
	 * @apiNote When a new set of demands are saved in the database on every
	 *          _update.
	 * @apiNote This might change demand objects. This will create new Collection
	 *          objects.
	 * @param demands
	 * @param payment
	 * @return List<RentCollection> Collections to be saved in the database.
	 */
	public List<EstateRentCollection> settle(final List<EstateDemand> demandsToBeSettled,
			final List<EstatePayment> payments, final EstateAccount account, double interestRate, boolean isFixGST,
			double rentInterest);

	public EstateRentSummary calculateRentSummary(List<EstateDemand> demands, EstateAccount rentAccount,
			double interestRate, boolean isFixGST, double rentInterest);

	EstateRentSummary calculateRentSummaryAt(List<EstateDemand> demands, EstateAccount rentAccount, double interestRate,
			long atTimestamp, boolean isFixGST, double rentInterest);

	public List<EstateAccountStatement> getAccountStatement(List<EstateDemand> demands, List<EstatePayment> payments,
			double interestRate, Long fromDateTimestamp, Long toDateTimestamp, boolean isFixGST, double rentInterest);

}