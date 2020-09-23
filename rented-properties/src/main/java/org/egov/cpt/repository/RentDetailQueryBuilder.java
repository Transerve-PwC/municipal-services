package org.egov.cpt.repository;

import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.PropertyCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class RentDetailQueryBuilder {

	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY property_id desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private static final String DEMAND_SEARCH_QUERY = SELECT
			//+ " demand.*,"
			+ " demand.id as demand_id,demand.property_id as demand_pid,demand.initialGracePeriod as demand_IniGracePeriod, demand.generationDate as demand_genDate,"
			+ " demand.collectionPrincipal as demand_colPrincipal,demand.remainingPrincipal as demand_remPrincipal, demand.interestSince as demand_intSince,"
			+ " demand.status as demand_status,"
			+ " demand.mode as demand_mode, demand.created_by as demand_created_by, demand.created_date as demand_created_date,"
			+ " demand.modified_by as demand_modified_by,demand.modified_date as demand_modified_date "

			+ " FROM  cs_pt_demand demand ";

	private static final String ACCOUNT_SEARCH_QUERY = SELECT
			//+ " account.*, "
			+ " account.id as account_id,account.property_id as account_pid,account.remainingAmount as account_remainingAmount, account.remaining_since as account_remaining_since,"
			+ " account.created_by as account_created_by, account.created_date as account_created_date,"
			+ " account.modified_by as account_modified_by,account.modified_date as account_modified_date "

			+ " FROM cs_pt_account account ";

	private static final String PAYMENT_SEARCH_QUERY = SELECT
			//+ " payment.*,"
			+ " payment.id as payment_id, payment.property_id as payment_pid,payment.receiptNo as payment_receiptNo,payment.amountPaid as payment_amtPaid,"
			+ " payment.processed as payment_processed,"
			+ " payment.dateOfPayment as payment_dateOfPayment,payment.mode as payment_mode,payment.created_by as payment_created_by, payment.created_date as payment_created_date,"
			+ " payment.modified_by as payment_modified_by,payment.modified_date as payment_modified_date "

			+ " FROM cs_pt_payment payment ";

	private String addPaginationWrapper(String query, Map<String, Object> preparedStmtList, PropertyCriteria criteria) {

		/*
		 * if (criteria.getLimit() == null && criteria.getOffset() == null) return
		 * query;
		 */

		Long limit = config.getDefaultLimit();
		Long offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit())
			limit = config.getMaxSearchLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.put("start", offset);
		preparedStmtList.put("end", limit + offset);

		return finalQuery;
	}

	private static void addClauseIfRequired(Map<String, Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND ");
		}
	}

	public String getPropertyRentDemandSearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(DEMAND_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("demand.property_id=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}

	public String getPropertyRentPaymentSearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(PAYMENT_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("payment.property_id=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}

	public String getPropertyRentAccountSearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(ACCOUNT_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("account.property_id=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
