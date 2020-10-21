package org.egov.ps.service;

import java.util.List;

import org.egov.ps.model.RentAccount;
import org.egov.ps.model.RentAccountStatement;
import org.egov.ps.model.RentCollection;
import org.egov.ps.model.RentSummary;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.EstatePayment;



public interface IRentCollectionService {

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
	public List<RentCollection> settle(List<EstateDemand> demandsToBeSettled, List<EstatePayment> paymentsToBeSettled,
			RentAccount account, double interestRate);

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

	/**
	 * Get the rent summary
	 * 
	 * @param demands
	 * @param rentAccount
	 * @param interestRate
	 * @return
	 */
	public RentSummary calculateRentSummary(List<EstateDemand> demands, RentAccount rentAccount, double interestRate);

	/**
	 * Get the rent summary
	 * 
	 * @param demands
	 * @param rentAccount
	 * @param interestRate
	 * @param atTimestamp
	 * @return
	 */
	public RentSummary calculateRentSummaryAt(List<EstateDemand> demands, RentAccount rentAccount, double interestRate,
			long atTimestamp);

	/**
	 * @apiNote This will provide the account statement between the date specified
	 *          by the user.
	 * @param demands
	 * @param payments
	 * @param lstCollection
	 * @return List<RentAccountStatement>
	 */
	public List<RentAccountStatement> getAccountStatement(List<EstateDemand> demands, List<EstatePayment> payments,
			double interesetRate, Long fromDateTimestamp, Long toDateTimestamp);

}
