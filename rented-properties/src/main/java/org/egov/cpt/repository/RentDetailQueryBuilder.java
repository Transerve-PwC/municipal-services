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
	private static final String INNER_JOIN = "INNER JOIN";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private static final String DEMAND_SEARCH_QUERY = SELECT
			+ " demand.id as demand_id,demand.property_id as demand_pid,demand.initialGracePeriod as demand_IniGracePeriod, demand.generationDate as demand_genDate,"
			+ " demand.collectionPrincipal as demand_colPrincipal,demand.remainingPrincipal as demand_remPrincipal, demand.interestSince as demand_intSince,"
			+ " demand.status as demand_status,"
			+ " demand.mode as demand_mode, demand.created_by as demand_created_by, demand.created_date as demand_created_date,"
			+ " demand.modified_by as demand_modified_by,demand.modified_date as demand_modified_date "

			+ " FROM  cs_pt_demand demand ";

	private static final String ACCOUNT_SEARCH_QUERY = SELECT
			+ " account.id as account_id,account.property_id as account_pid,account.remainingAmount as account_remainingAmount, account.remaining_since as account_remaining_since,"
			+ " account.created_by as account_created_by, account.created_date as account_created_date,"
			+ " account.modified_by as account_modified_by,account.modified_date as account_modified_date "

			+ " FROM cs_pt_account account ";

	private static final String PAYMENT_SEARCH_QUERY = SELECT
			+ " payment.id as payment_id, payment.property_id as payment_pid,payment.receiptNo as payment_receiptNo,payment.amountPaid as payment_amtPaid,"
			+ " payment.processed as payment_processed,"
			+ " payment.dateOfPayment as payment_dateOfPayment,payment.mode as payment_mode,payment.created_by as payment_created_by, payment.created_date as payment_created_date,"
			+ " payment.modified_by as payment_modified_by,payment.modified_date as payment_modified_date "

			+ " FROM cs_pt_payment payment ";
	
	private static final String PROPERTY_ACTIVE_OWNER_QUERY = SELECT 
			+ " pt.id as pid, pt.transit_number as transit_no, pt.tenantid as pttenantid, pt.colony as colony,"

			+ " ptdl.interest_rate as pd_int_rate,"
			
			+ " ownership.id as oid, ownership.property_id as oproperty_id,"
			
			+ " od.id as odid, od.property_id as odproperty_id," + " od.owner_id odowner_id,"
			+ " od.name as ownerName, od.phone as ownerPhone "
			
			+ " FROM cs_pt_property_v1 pt " + INNER_JOIN
			+ " cs_pt_propertydetails_v1 ptdl ON pt.id =ptdl.property_id " + LEFT_JOIN
			
			+" cs_pt_ownership_v1 ownership ON pt.id=ownership.property_id AND ownership.active_state = true "
			+ INNER_JOIN + " cs_pt_ownershipdetails_v1 od ON ownership.id = od.owner_id ";

	
	private static final String PROPERTYID_SEARCH_QUERY = SELECT + " pt.id as pid FROM cs_pt_property_v1 pt ";

	private String addPaginationWrapper(String query, Map<String, Object> preparedStmtList, PropertyCriteria criteria, String paginationWrapper) {

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

		 String paginationWrapper = "SELECT * FROM "
				+ "(SELECT *, DENSE_RANK() OVER (ORDER BY demand_pid desc) offset_ FROM " + "({})"
				+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

		StringBuilder builder = new StringBuilder(DEMAND_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("demand.property_id=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria, paginationWrapper);
	}

	public String getPropertyRentPaymentSearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {

		String paginationWrapper = "SELECT * FROM "
				+ "(SELECT *, DENSE_RANK() OVER (ORDER BY payment_pid desc) offset_ FROM " + "({})"
				+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

		StringBuilder builder = new StringBuilder(PAYMENT_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("payment.property_id=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria, paginationWrapper);
	}

	public String getPropertyRentAccountSearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(ACCOUNT_SEARCH_QUERY);

		String paginationWrapper = "SELECT * FROM "
				+ "(SELECT *, DENSE_RANK() OVER (ORDER BY account_pid desc) offset_ FROM " + "({})"
				+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("account.property_id=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria, paginationWrapper);
	}

	public String getPropertyWithActiveOwnerQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(PROPERTY_ACTIVE_OWNER_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getState())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.master_data_state IN (:states)");
			preparedStmtList.put("states", criteria.getState());
		}
		if (null != criteria.getPropertyId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.id = :id");
			preparedStmtList.put("id", criteria.getPropertyId());
		}
		return builder.toString();
	}

	public String getPropertyIdQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(PROPERTYID_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getState())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.master_data_state IN (:states)");
			preparedStmtList.put("states", criteria.getState());
		}
		return builder.toString();
	}
}
