package org.egov.wscalculation.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.egov.wscalculation.model.MeterReadingSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class WSCalculatorQueryBuilder {

	@Autowired
	WSCalculationConfiguration config;

	private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	private final static String Query = "SELECT mr.id, mr.connectionNo as connectionId, mr.billingPeriod, mr.meterStatus, mr.lastReading, mr.lastReadingDate, mr.currentReading,"
			+ " mr.currentReadingDate, mr.createdBy as mr_createdBy, mr.lastModifiedBy as mr_lastModifiedBy,"
			+ " mr.createdTime as mr_createdTime, mr.lastModifiedTime as mr_lastModifiedTime FROM eg_ws_meterreading mr";

	private final static String noOfConnectionSearchQuery = "SELECT count(*) FROM eg_ws_meterreading WHERE";
    
	private final static String noOfConnectionSearchQueryForCurrentMeterReading= "select mr.currentReading from eg_ws_meterreading mr";
	
	private final static String tenentIdWaterConnectionSearchQuery="select DISTINCT tenantid from eg_ws_connection";
	
	private final static String connectionNoWaterConnectionSearchQuery = "SELECT conn.connectionNo as conn_no FROM eg_ws_service wc INNER JOIN eg_ws_connection conn ON wc.connection_id = conn.id";
	
	private static final String connectionNoListQuery = "SELECT distinct(conn.connectionno) FROM eg_ws_connection conn INNER JOIN eg_ws_service ws ON conn.id = ws.connection_id";

	private static final String distinctTenantIdsCriteria = "SELECT distinct(tenantid) FROM eg_ws_connection ws";

	public static final String getBillingDataForDemandGeneration = "SELECT id as billGenerationId, cccode as ccCode, divsdiv as divSdiv, consumercode as consumerCode, billcycle as billCycle, billgroup as billGroup,\r\n" + 
			" subgroup as subGroup, \r\n" + 
			"       billtype as billType, name as name, address as address, add1 as add1, add2 as add2, add3 as , add4 as , add5 as , cesscharge as , \r\n" + 
			"       netamount as , grossamount as , surcharge as , totalnetamount as , totalsurcharge as , \r\n" + 
			"       totalgrossamount as , fixchargecode as , fixcharge as , duedatecash as , duedatecheque as , \r\n" + 
			"       status as , billid as , paymentid as , paymentstatus as , createdby as , lastmodifiedby as , \r\n" + 
			"       createdtime as , lastmodifiedtime\r\n" + 
			"  FROM public.eg_ws_savebilling where status ='INITIATED' and duedatecash::date >= now()::date;";
	
	public static final String getBillingDataForSurcharge = "SELECT id as billGenerationId, cccode as ccCode, divsdiv as divSdiv, consumercode as consumerCode, billcycle as billCycle, billgroup as billGroup,\r\n" + 
			" subgroup as subGroup, \r\n" + 
			"       billtype as billType, name as name, address as address, add1 as add1, add2 as add2, add3 as , add4 as , add5 as , cesscharge as , \r\n" + 
			"       netamount as , grossamount as , surcharge as , totalnetamount as , totalsurcharge as , \r\n" + 
			"       totalgrossamount as , fixchargecode as , fixcharge as , duedatecash as , duedatecheque as , \r\n" + 
			"       status as , billid as , paymentid as , paymentstatus as , createdby as , lastmodifiedby as , \r\n" + 
			"       createdtime as , lastmodifiedtime\r\n" + 
			"  FROM public.eg_ws_savebilling where status not in ('PAID')::date and duedatecash::date < now()::date;";

	public static final String getBillingDataForConnection = "SELECT id as billGenerationId, cccode as ccCode, divsdiv as divSdiv, consumercode as consumerCode, billcycle as billCycle, billgroup as billGroup,\r\n" + 
			" subgroup as subGroup, \r\n" + 
			"       billtype as billType, name as name, address as address, add1 as add1, add2 as add2, add3 as add3 , add4 as add4 , add5 as add5, cesscharge as cessCharge, \r\n" + 
			"       netamount as netAmount, grossamount as grossAmount , surcharge  , totalnetamount  , totalsurcharge  , \r\n" + 
			"       totalgrossamount  , fixchargecode  , fixcharge  , duedatecash  , duedatecheque  , \r\n" + 
			"       status  , billid  , paymentid , paymentstatus  , createdby  , lastmodifiedby  , \r\n" + 
			"       createdtime  , lastmodifiedtime\r\n" + 
			"  FROM public.eg_ws_savebilling where concat(divsdiv,consumercode) = ?;";

	public String getDistinctTenantIds() {
		return distinctTenantIdsCriteria;
	}
	/**
	 * 
	 * @param criteria
	 *            would be meter reading criteria
	 * @param preparedStatement
	 * @return Query for given criteria
	 */
	public String getSearchQueryString(MeterReadingSearchCriteria criteria, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(Query);
		if (CollectionUtils.isEmpty(criteria.getConnectionNos())) {
			return null;
		}
		addClauseIfRequired(preparedStatement, query);
		query.append(" mr.connectionNo IN (").append(createQuery(criteria.getConnectionNos())).append(" )");
		addToPreparedStatement(preparedStatement, criteria.getConnectionNos());
		addOrderBy(query);
		return addPaginationWrapper(query, preparedStatement, criteria);
	}

	private String createQuery(Set<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}

	private void addToPreparedStatement(List<Object> preparedStatement, Set<String> ids) {
		ids.forEach(id -> {
			preparedStatement.add(id);
		});
	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private String addPaginationWrapper(StringBuilder query, List<Object> preparedStmtList,
			MeterReadingSearchCriteria criteria) {
		query.append(" ").append(Offset_Limit_String);
		Integer limit = config.getMeterReadingDefaultLimit();
		Integer offset = config.getMeterReadingDefaultOffset();

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMeterReadingDefaultLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMeterReadingDefaultLimit())
			limit = config.getMeterReadingDefaultLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		return query.toString();
	}

	public String getNoOfMeterReadingConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		Set<String> listOfIds = new HashSet<>();
		connectionIds.forEach(id -> listOfIds.add(id));
		query.append(" connectionNo in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, listOfIds);
		return query.toString();
	}
	
	public String getCurrentReadingConnectionQuery(MeterReadingSearchCriteria criteria,
			List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQueryForCurrentMeterReading);
		if (CollectionUtils.isEmpty(criteria.getConnectionNos()))
			return null;
		addClauseIfRequired(preparedStatement, query);
		query.append(" mr.connectionNo IN (").append(createQuery(criteria.getConnectionNos())).append(" )");
		addToPreparedStatement(preparedStatement, criteria.getConnectionNos());
		query.append(" ORDER BY mr.currentReadingDate DESC LIMIT 1");
		return query.toString();
	}
	
	public String getTenentIdConnectionQuery() {
		return tenentIdWaterConnectionSearchQuery;
	}
	
	private void addOrderBy(StringBuilder query) {
		query.append(" ORDER BY mr.currentReadingDate DESC");
	}
	
	public String getConnectionNumberFromWaterServicesQuery(List<Object> preparedStatement, String connectionType,
			String tenentId) {
		StringBuilder query = new StringBuilder(connectionNoWaterConnectionSearchQuery);
		if (!StringUtils.isEmpty(connectionType)) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" wc.connectionType = ? ");
			preparedStatement.add(connectionType);
		}

		if (!StringUtils.isEmpty(tenentId)) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.tenantId = ? ");
			preparedStatement.add(tenentId);
		}
		return query.toString();

	}
	
	
	public String getConnectionNumberList(String tenantId, String connectionType, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(connectionNoListQuery);
		// Add connection type
		addClauseIfRequired(preparedStatement, query);
		query.append(" ws.connectiontype = ? ");
		preparedStatement.add(connectionType);
		// add tenantid
		addClauseIfRequired(preparedStatement, query);
		query.append(" conn.tenantid = ? ");
		preparedStatement.add(tenantId);
		return query.toString();
		
	}
	
	public String isBillingPeriodExists(String connectionNo, String billingPeriod, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		query.append(" connectionNo = ? ");
		preparedStatement.add(connectionNo);
		addClauseIfRequired(preparedStatement, query);
		query.append(" billingPeriod = ? ");
		preparedStatement.add(billingPeriod);
		return query.toString();
	}

}
