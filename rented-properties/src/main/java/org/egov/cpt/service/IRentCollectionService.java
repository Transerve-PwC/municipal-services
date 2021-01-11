package org.egov.cpt.service;

import java.util.List;

import org.egov.cpt.models.RentAccount;
import org.egov.cpt.models.RentAccountStatement;
import org.egov.cpt.models.RentCollection;
import org.egov.cpt.models.RentDemand;
import org.egov.cpt.models.RentPayment;
import org.egov.cpt.models.RentSummary;

public interface IRentCollectionService {

	/**
	 * Get the list of collections for the given demand and payments for the same
	 * property.
	 * @param interestStartDate 
	 * 
	 * @apiNote When a new set of demands are saved in the database on every
	 *          _update.
	 * @apiNote This might change demand objects. This will create new Collection
	 *          objects.
	 * @param demands
	 * @param payment
	 * @return List<RentCollection> Collections to be saved in the database.
	 */
	public List<RentCollection> settle(List<RentDemand> demandsToBeSettled, List<RentPayment> paymentsToBeSettled,
			RentAccount account, double interestRate, Long interestStartDate);

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
	 * @param interestStartDate 
	 * @return
	 */
	public RentSummary calculateRentSummary(List<RentDemand> demands, RentAccount rentAccount, double interestRate, Long interestStartDate);

	/**
	 * Get the rent summary
	 * 
	 * @param demands
	 * @param rentAccount
	 * @param interestRate
	 * @param atTimestamp
	 * @return
	 */
	public RentSummary calculateRentSummaryAt(List<RentDemand> demands, RentAccount rentAccount, double interestRate,
			long atTimestamp,Long interestStartDate);

	/**
	 * @apiNote This will provide the account statement between the date specified
	 *          by the user.
	 * @param demands
	 * @param payments
	 * @param interestStartDate 
	 * @param lstCollection
	 * @return List<RentAccountStatement>
	 */
	public List<RentAccountStatement> getAccountStatement(List<RentDemand> demands, List<RentPayment> payments,
			double interesetRate, Long fromDateTimestamp, Long toDateTimestamp, Long interestStartDate);

}
