package org.egov.waterconnection.repository.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.SearchCriteria;
import org.egov.waterconnection.service.UserService;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Component
public class WsQueryBuilder {

	@Autowired
	private WaterServicesUtil waterServicesUtil;

	@Autowired
	private WSConfiguration config;

	@Autowired
	private UserService userService;

	
	public static final String getBillingDataForDemandGeneration = "SELECT id as billGenerationId, cccode as ccCode, divsdiv as divSdiv, consumercode as consumerCode, billcycle as billCycle, billgroup as billGroup,\r\n" + 
			" subgroup as subGroup, \r\n" + 
			"       billtype as billType, name as name, address as address, add1 as add1, add2 as add2, add3  , add4  , add5  , cesscharge , \r\n" + 
			"       netamount , grossamount , surcharge , totalnetamount  , totalsurcharge  , \r\n" + 
			"       totalgrossamount , fixchargecode , fixcharge  , duedatecash , duedatecheque  , \r\n" + 
			"       status , billid , paymentid  , paymentstatus , createdby , lastmodifiedby  , \r\n" + 
			"       createdtime , lastmodifiedtime\r\n" + 
			"  FROM public.eg_ws_savebilling where status ='PAID' and isFileGenerated = false;";

	public static final String GET_WS_BILLING_FILES = "SELECT  filestore_url as billFileStoreUrl, filestore_id as billFileStoreId, filegeneration_time as fileGenerationTime\r\n" + 
			"  FROM public.eg_ws_billfile_history;";
	public static final String GET_WS_BILLING_Data = "SELECT id as billGenerationId, cccode as ccCode, divsdiv as divSdiv, consumercode as consumerCode, billcycle as billCycle, billgroup as billGroup,\r\n" + 
			" subgroup as subGroup, \r\n" + 
			"       billtype as billType, name as name, address as address, add1 as add1, add2 as add2, add3 as add3 , add4 as add4 , add5 as add5, cesscharge as cessCharge, \r\n" + 
			"       netamount as netAmount, grossamount as grossAmount , surcharge  , totalnetamount  , totalsurcharge  , \r\n" + 
			"       totalgrossamount  , fixchargecode  , fixcharge  , duedatecash  , duedatecheque  , \r\n" + 
			"       status  , billid  , paymentid , paymentstatus  , createdby  , lastmodifiedby  , \r\n" + 
			"       createdtime  , lastmodifiedtime\r\n" + 
			"  FROM public.eg_ws_savebilling where concat(divsdiv,consumercode) = ?;";
	private static final String INNER_JOIN_STRING = " INNER JOIN ";
    private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";

	private static String holderSelectValues = "connectionholder.tenantid as holdertenantid, connectionholder.connectionid as holderapplicationId, userid, connectionholder.status as holderstatus, isprimaryholder, connectionholdertype,connectionholder.correspondance_address as holdercorrepondanceaddress, holdershippercentage, connectionholder.relationship as holderrelationship, connectionholder.createdby as holdercreatedby, connectionholder.createdtime as holdercreatedtime, connectionholder.lastmodifiedby as holderlastmodifiedby, connectionholder.lastmodifiedtime as holderlastmodifiedtime, ";
	
	private static final String WATER_SEARCH_QUERY = "SELECT "
			/* + " conn.*, wc.*, document.*, plumber.*, application.*, property.*, " */
			+ " wc.connectionCategory, wc.connectionType, wc.waterSource, wc.meterCount, wc.meterRentCode, wc.mfrCode, wc.meterDigits, wc.meterUnit, wc.sanctionedCapacity,"
			+ " wc.meterId, wc.meterInstallationDate, wc.pipeSize, wc.noOfTaps, wc.proposedPipeSize, wc.proposedTaps, wc.connection_id as connection_Id, wc.connectionExecutionDate, wc.initialmeterreading, wc.appCreatedDate,"
			+ " wc.detailsprovidedby, wc.estimationfileStoreId , wc.sanctionfileStoreId , wc.estimationLetterDate, "
			+ " conn.id as conn_id, conn.tenantid, conn.applicationNo, conn.applicationStatus, conn.status, conn.connectionNo, conn.oldConnectionNo, conn.property_id, conn.roadcuttingarea,"
			+ " conn.action, conn.adhocpenalty, conn.adhocrebate, conn.adhocpenaltyreason, conn.applicationType, conn.dateEffectiveFrom,"
			+ " conn.adhocpenaltycomment, conn.adhocrebatereason, conn.adhocrebatecomment, conn.cccode, conn.div, conn.subdiv, conn.ledger_no,conn.ledgergroup, conn.createdBy as ws_createdBy, conn.lastModifiedBy as ws_lastModifiedBy,"
			+ " conn.createdTime as ws_createdTime, conn.lastModifiedTime as ws_lastModifiedTime, "
			+ " conn.roadtype, conn.waterApplicationType, conn.securityCharge, conn.connectionusagestype, conn.inworkflow, conn.billGroup, conn.contract_value, "
			+ " document.id as doc_Id, document.documenttype, document.filestoreid, document.active as doc_active, plumber.id as plumber_id,"
			+ " plumber.name as plumber_name, plumber.licenseno,"
			+ " plumber.mobilenumber as plumber_mobileNumber, plumber.gender as plumber_gender, plumber.fatherorhusbandname, plumber.correspondenceaddress,"
			+ " plumber.relationship, " + holderSelectValues
			+ " application.id as application_id, application.applicationno as app_applicationno, application.activitytype as app_activitytype, application.applicationstatus as app_applicationstatus, application.action as app_action, application.comments as app_comments, application.is_ferrule_applicable as app_ferrule, application.security_charges as app_securitycharge, "
			+ " application.createdBy as app_createdBy, application.lastModifiedBy as app_lastModifiedBy, application.createdTime as app_createdTime, application.lastModifiedTime as app_lastModifiedTime, "
			+ " property.id as waterpropertyid, property.usagecategory, property.usagesubcategory "
			+ " FROM eg_ws_connection conn "
			+  INNER_JOIN_STRING 
			+ "eg_ws_service wc ON wc.connection_id = conn.id"
			+  INNER_JOIN_STRING
			+ "eg_ws_application application ON application.wsid = conn.id"
			+  INNER_JOIN_STRING
			+ "eg_ws_property property ON property.wsid = conn.id"
			+  LEFT_OUTER_JOIN_STRING
			+ "eg_ws_applicationdocument document ON document.applicationid = application.id"
			+  LEFT_OUTER_JOIN_STRING
			+ "eg_ws_plumberinfo plumber ON plumber.wsid = conn.id"
			+ LEFT_OUTER_JOIN_STRING
		    + "eg_ws_connectionholder connectionholder ON connectionholder.connectionid = conn.id";
			//		+ LEFT_OUTER_JOIN_STRING
			//	    + "eg_ws_connection_mapping cm ON cm.wsid = conn.id";
	
	private static final String NO_OF_CONNECTION_SEARCH_QUERY = "SELECT count(*) FROM eg_ws_connection WHERE";
	
	private static final String PAGINATION_WRAPPER = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY app_applicationno) offset_ FROM " +
            "({})" +
            " result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";
	
	private static final String ORDER_BY_CLAUSE= " ORDER BY application.createdTime DESC";
	/**
	 * 
	 * @param criteria
	 *            The WaterCriteria
	 * @param preparedStatement
	 *            The Array Of Object
	 * @param requestInfo
	 *            The Request Info
	 * @return query as a string
	 */
	public String getSearchQueryString(SearchCriteria criteria, List<Object> preparedStatement,
			RequestInfo requestInfo) {
		if (criteria.isEmpty())
				return null;
		StringBuilder query = new StringBuilder(WATER_SEARCH_QUERY);
		boolean propertyIdsPresent = false;
		/*
		 * if (!StringUtils.isEmpty(criteria.getMobileNumber())) { Set<String>
		 * propertyIds = new HashSet<>(); List<Property> propertyList =
		 * waterServicesUtil.propertySearchOnCriteria(criteria, requestInfo);
		 * propertyList.forEach(property -> propertyIds.add(property.getId())); if
		 * (!propertyIds.isEmpty()) { addClauseIfRequired(preparedStatement, query);
		 * query.append(" (conn.property_id in (").append(createQuery(propertyIds)).
		 * append(" )"); addToPreparedStatement(preparedStatement, propertyIds);
		 * propertyIdsPresent = true; } }
		 */
		if(!StringUtils.isEmpty(criteria.getMobileNumber())) {
			Set<String> uuids = userService.getUUIDForUsers(criteria.getMobileNumber(), criteria.getTenantId(), requestInfo);
			boolean userIdsPresent = false;
			if (!CollectionUtils.isEmpty(uuids)) {
				addORClauseIfRequired(preparedStatement, query);
				if(!propertyIdsPresent)
					query.append("(");
				query.append(" connectionholder.userid in (").append(createQuery(uuids)).append(" ))");
				addToPreparedStatement(preparedStatement, uuids);
				userIdsPresent = true;
			}
			/*
			 * if(propertyIdsPresent && !userIdsPresent){ query.append(")"); }
			 */
		}
		if (!StringUtils.isEmpty(criteria.getTenantId())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.tenantid = ? ");
			preparedStatement.add(criteria.getTenantId());
		}
//		if (!StringUtils.isEmpty(criteria.getPropertyId())) {
//			addClauseIfRequired(preparedStatement, query);
//			query.append(" conn.property_id = ? ");
//			preparedStatement.add(criteria.getPropertyId());
//		}
		if (!CollectionUtils.isEmpty(criteria.getIds())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" application.id in (").append(createQuery(criteria.getIds())).append(" )");
			addToPreparedStatement(preparedStatement, criteria.getIds());
		}
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
			query.append(" application.applicationno = ? ");
			preparedStatement.add(criteria.getApplicationNumber());
		}
		if (!StringUtils.isEmpty(criteria.getApplicationStatus())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" application.applicationStatus = ? ");
			preparedStatement.add(criteria.getApplicationStatus());
		}
		if (criteria.getFromDate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append("  wc.appCreatedDate >= ? ");
			preparedStatement.add(criteria.getFromDate());
		}
		if (criteria.getToDate() != null) {
			addClauseIfRequired(preparedStatement, query);
			query.append("  wc.appCreatedDate <= ? ");
			preparedStatement.add(criteria.getToDate());
		}
		if(!StringUtils.isEmpty(criteria.getApplicationType())) {
			addClauseIfRequired(preparedStatement, query);
			query.append(" conn.applicationType = ? ");
			preparedStatement.add(criteria.getApplicationType());
		}
		/*
		 * if (!StringUtils.isEmpty(criteria.getConnectionUserId())) {
		 * addORClauseIfRequired(preparedStatement, query);
		 * query.append(" cm.user_id = ? ");
		 * preparedStatement.add(criteria.getConnectionUserId()); }
		 */
		query.append(ORDER_BY_CLAUSE);
		return addPaginationWrapper(query.toString(), preparedStatement, criteria);
	}
	
	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private void addORClauseIfRequired(List<Object> values, StringBuilder queryString){
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" OR");
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
		Integer limit = config.getDefaultLimit();
		Integer offset = config.getDefaultOffset();
		if (criteria.getLimit() == null && criteria.getOffset() == null)
			limit = config.getMaxLimit();

		if (criteria.getLimit() != null && criteria.getLimit() <= config.getDefaultLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getDefaultOffset())
			limit = config.getDefaultLimit();

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		preparedStmtList.add(offset);
		preparedStmtList.add(limit + offset);
		return PAGINATION_WRAPPER.replace("{}",query);
	}

	public String getNoOfWaterConnectionQuery(Set<String> connectionIds, List<Object> preparedStatement) {
		StringBuilder query = new StringBuilder(NO_OF_CONNECTION_SEARCH_QUERY);
		query.append(" connectionno in (").append(createQuery(connectionIds)).append(" )");
		addToPreparedStatement(preparedStatement, connectionIds);
		return query.toString();
	}
	
}
