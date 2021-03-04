package org.egov.swservice.repository.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.util.SewerageServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component
public class SWQueryBuilder {

	@Autowired
	private SewerageServicesUtil sewerageServicesUtil;

	@Autowired
	private SWConfiguration config;

	private static final String INNER_JOIN_STRING = " INNER JOIN ";
	 private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";
	//private static final String Offset_Limit_String = "OFFSET ? LIMIT ?";
	
	private final static String noOfConnectionSearchQuery = "SELECT count(*) FROM eg_sw_connection WHERE";
	private static String holderSelectValues = "connectionholder.tenantid as holdertenantid, connectionholder.connectionid as holderapplicationId, userid, connectionholder.status as holderstatus, isprimaryholder, connectionholdertype,connectionholder.correspondance_address as holdercorrepondanceaddress, holdershippercentage, connectionholder.relationship as holderrelationship,connectionholder.name as holdername, connectionholder.createdby as holdercreatedby, connectionholder.createdtime as holdercreatedtime, connectionholder.lastmodifiedby as holderlastmodifiedby, connectionholder.lastmodifiedtime as holderlastmodifiedtime, connectionholder.mobile_no as holdermobileno, connectionholder.gender as holdergender, connectionholder.guardian_name as holderguardianname ";
	private final static String SEWERAGE_SEARCH_QUERY = "SELECT "
			/*+ " conn.*, sc.*, document.*, plumber.*, "*/
			+ " sc.connectionExecutionDate, sc.noOfWaterClosets, sc.noOfToilets,sc.proposedWaterClosets, sc.proposedToilets, sc.connectionType, sc.connection_id as connection_Id, sc.appCreatedDate,"
			+ " sc.metercount,sc.meterrentcode,sc.mfrcode,sc.meterdigits,sc.meterunit,sc.sanctionedcapacity,sc.detailsprovidedby, sc.estimationfileStoreId , sc.sanctionfileStoreId , sc.estimationLetterDate,"
			+ " conn.id as conn_id, conn.tenantid, conn.applicationNo, conn.applicationStatus, conn.status, conn.connectionNo, conn.oldConnectionNo, conn.property_id,"
			+ " conn.cccode,conn.div,conn.subdiv,conn.ledger_no,conn.ledgergroup,conn.billgroup,conn.contract_value,conn.roadcuttingarea, conn.action, conn.adhocpenalty, conn.adhocrebate, conn.createdBy as sw_createdBy,"
			+ " conn.lastModifiedBy as sw_lastModifiedBy, conn.createdTime as sw_createdTime, conn.lastModifiedTime as sw_lastModifiedTime, "
			+ " conn.adhocpenaltyreason, conn.adhocpenaltycomment, conn.adhocrebatereason, conn.adhocrebatecomment,"
			+ " conn.roadtype, document.id as doc_Id, document.documenttype, document.filestoreid, document.active as doc_active, plumber.id as plumber_id, plumber.name as plumber_name, plumber.licenseno,"
			+ " plumber.mobilenumber as plumber_mobileNumber, plumber.gender as plumber_gender, plumber.fatherorhusbandname, plumber.correspondenceaddress, plumber.relationship, "
			+ " property.id as seweragepropertyid, property.usagecategory, property.usagesubcategory, " +holderSelectValues
			+ " FROM eg_sw_connection conn "
	+  INNER_JOIN_STRING 
	+" eg_sw_service sc ON sc.connection_id = conn.id"
	+  INNER_JOIN_STRING
	+ "eg_sw_property property ON property.swid = conn.id"
	+  LEFT_OUTER_JOIN_STRING
	+ "eg_sw_applicationdocument document ON document.swid = conn.id" 
	+  LEFT_OUTER_JOIN_STRING
	+ "eg_sw_plumberinfo plumber ON plumber.swid = conn.id"
			+ LEFT_OUTER_JOIN_STRING
		    + "eg_sw_connectionholder connectionholder ON connectionholder.connectionid = conn.id";

	/**
	 * 
	 * @param criteria on search criteria
	 * @param preparedStatement preparedStatement
	 * @param requestInfo
	 * @return
	 */
	
	
	private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY conn_id) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";
	
	
	public String getSearchQueryString(SearchCriteria criteria, List<Object> preparedStatement,
			RequestInfo requestInfo) {
		if(criteria.isEmpty())
			return null;
		StringBuilder query = new StringBuilder(SEWERAGE_SEARCH_QUERY);
		if (!StringUtils.isEmpty(criteria.getMobileNumber())) {

			addClauseIfRequired(preparedStatement, query);
			query.append(" connectionholder.mobile_no = ? ");
			preparedStatement.add(criteria.getMobileNumber());
		}
		if (!StringUtils.isEmpty(criteria.getTenantId())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.tenantid = ? ");
			preparedStatement.add(criteria.getTenantId());
		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.id in (").append(createQuery(criteria.getIds())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getIds());
		}

//		if (!StringUtils.isEmpty(criteria.getPropertyId())) {
//			addClauseIfRequired(preparedStatement, query);
//			query.append(" conn.property_id = ? ");
//			preparedStatement.add(criteria.getPropertyId());
//		}
		if (!StringUtils.isEmpty(criteria.getOldConnectionNumber())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.oldconnectionno = ? ");
			preparedStatement.add(criteria.getOldConnectionNumber());
		}

		if (!StringUtils.isEmpty(criteria.getConnectionNumber())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.connectionno = ? ");
			preparedStatement.add(criteria.getConnectionNumber());
		}
		if (!StringUtils.isEmpty(criteria.getStatus())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.status = ? ");
			preparedStatement.add(criteria.getStatus());
		}
		if (!StringUtils.isEmpty(criteria.getApplicationNumber())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.applicationno = ? ");
			preparedStatement.add(criteria.getApplicationNumber());
		}
		if (!StringUtils.isEmpty(criteria.getApplicationStatus())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.applicationStatus = ? ");
			preparedStatement.add(criteria.getApplicationStatus());
		}
		if (criteria.getFromDate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append("  sc.appCreatedDate >= ? ");
			preparedStatement.add(criteria.getFromDate());
		}
		if (criteria.getToDate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append("  sc.appCreatedDate <= ? ");
			preparedStatement.add(criteria.getToDate());
		}
		//Add OrderBy clause
		query.append(" ORDER BY sc.appCreatedDate DESC");
		
		if (query.toString().indexOf("WHERE") > -1)
			 return addPaginationWrapper(query.toString(), preparedStatement, criteria);
		return query.toString();
	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
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


	/**
	 * 
	 * @param query
	 *            The
	 * @param preparedStmtList
	 *            Array of object for preparedStatement list
	 * @return It's returns query
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, SearchCriteria criteria) {
//		query = query + " " + Offset_Limit_String;
		Integer limit = config.getDefaultLimit();
		Integer offset = config.getDefaultOffset();

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getDefaultLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getDefaultOffset())
			limit = config.getDefaultLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		return paginationWrapper.replace("{}",query);
	}
	
	public String getNoOfSewerageConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(noOfConnectionSearchQuery);
		Set<String> listOfIds = new HashSet<>();
		connectionIds.forEach(id -> listOfIds.add(id));
		query.append(" connectionno in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, listOfIds);
		return query.toString();
	}

}
