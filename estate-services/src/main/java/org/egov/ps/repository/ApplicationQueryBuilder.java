package org.egov.ps.repository;

import java.util.Map;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.ApplicationCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class ApplicationQueryBuilder {

	@Autowired
	private Configuration config;

	private static final String SELECT = "SELECT ";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private static final String APP_ALL = " app.*, doc.*, ";
	private static final String PT_ALL = " pt.*, ptdl.*, ";
	private static final String OWNER_ALL = " ownership.*, od.*, ";

	private static final String APP_COLUMNS = " app.id as appid, app.tenantid as apptenantid, app.property_id as appproperty_id,"
			+ " app.application_number as appapplication_number,"
			+ " app.branch_type as appbranch_type, app.module_type as appmodule_type, app.application_type as appapplication_type,"
			+ " app.comments as appcomments, app.hardcopy_received_date as apphardcopy_received_date,"
			+ " app.state as appstate, app.action as appaction," + " app.application_details as appapplication_details,"
			+ " app.created_by as appcreated_by, app.last_modified_by as applast_modified_by,"
			+ " app.created_time as appcreated_time, app.last_modified_time as applast_modified_time, "

			+ " doc.id as docid, doc.reference_id as docapplication_id, doc.tenantid as doctenantid,"
			+ " doc.is_active as docis_active, doc.document_type, doc.file_store_id, doc.property_id as docproperty_id,"
			+ " doc.created_by as dcreated_by, doc.created_time as dcreated_time, "
			+ " doc.last_modified_by as dmodified_by, doc.last_modified_time as dmodified_time ";

	private static final String PT_COLUMNS = " pt.id as pid, app.branch_type as branch_type, pt.file_number, pt.tenantid as pttenantid, pt.category, pt.sub_category, "
			+ " pt.site_number, pt.sector_number, pt.state as pstate, pt.action as paction, pt.created_by as pcreated_by, pt.created_time as pcreated_time, "
			+ " pt.last_modified_by as pmodified_by, pt.last_modified_time as pmodified_time, "
			+ " pt.property_master_or_allotment_of_site, pt.is_cancelation_of_site, "

			+ " ptdl.id as ptdlid, ptdl.property_id as pdproperty_id, ptdl.property_type as pdproperty_type, "
			+ " ptdl.tenantid as pdtenantid, ptdl.type_of_allocation, ptdl.mode_of_auction, ptdl.scheme_name,ptdl.date_of_auction, "
			+ " ptdl.area_sqft, ptdl.rate_per_sqft, ptdl.last_noc_date, ptdl.service_category, "
			+ " ptdl.is_property_active, ptdl.trade_type, ptdl.company_name, ptdl.company_address, ptdl.company_registration_number, "
			+ " ptdl.company_type, ptdl.emd_amount, ptdl.emd_date ";

	private static final String OWNER_COLUMNS = " ownership.id as oid, ownership.property_details_id as oproperty_details_id,"
			+ " ownership.tenantid as otenantid, ownership.serial_number as oserial_number,"
			+ " ownership.share as oshare, ownership.cp_number as ocp_number,"

			+ " od.id as odid, od.owner_id as odowner_id,"
			+ " od.owner_name as odowner_name, od.tenantid as odtenantid,"
			+ " od.guardian_name as odguardian_name, od.guardian_relation as odguardian_relation, od.mobile_number as odmobile_number,"
			+ " od.allotment_number as odallotment_number, od.date_of_allotment as oddate_of_allotment,"
			+ " od.due_amount as oddue_amount, od.address as odaddress ";

	private static final String APP_TABLE = " FROM cs_ep_application_v1 app " + LEFT_JOIN
			+ " cs_ep_documents_v1 doc ON app.id=doc.reference_id ";

	private static final String PT_TABLE = " cs_ep_property_v1 pt on app.property_id = pt.id " + LEFT_JOIN
			+ " cs_ep_property_details_v1 ptdl  ON pt.id =ptdl.property_id ";

	private static final String OWNER_TABLE = " cs_ep_owner_v1 ownership  ON ptdl.id=ownership.property_details_id "
			+ LEFT_JOIN + " cs_ep_owner_details_v1 od ON ownership.id = od.owner_id ";

	private final String paginationWrapper = "SELECT * FROM "
			+ " (SELECT *, DENSE_RANK() OVER (ORDER BY applast_modified_time desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private String addPaginationWrapper(String query, Map<String, Object> preparedStmtList,
			ApplicationCriteria criteria) {

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

	public String getApplicationSearchQuery(ApplicationCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = null;

		if (null != criteria.getApplicationNumber()) {
			builder = new StringBuilder(SELECT);
			builder.append(APP_ALL + PT_ALL + OWNER_ALL);
			builder.append(APP_COLUMNS + "," + PT_COLUMNS + "," + OWNER_COLUMNS);
			builder.append(APP_TABLE + LEFT_JOIN + PT_TABLE + LEFT_JOIN + OWNER_TABLE);
		} else {
			builder = new StringBuilder(SELECT);
			builder.append(APP_ALL + PT_ALL);
			builder.append(APP_COLUMNS + "," + PT_COLUMNS);
			builder.append(APP_TABLE + LEFT_JOIN + PT_TABLE);
		}

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.property_id=:prId");
			preparedStmtList.put("prId", criteria.getPropertyId());
		}
		if (!ObjectUtils.isEmpty(criteria.getApplicationId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.id=:appId");
			preparedStmtList.put("appId", criteria.getApplicationId());
		}
		if (null != criteria.getTenantId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.tenantid=:tenantId");
			preparedStmtList.put("tenantId", criteria.getTenantId());
		}
		if (null != criteria.getFileNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.file_number=:fileNumber");
			preparedStmtList.put("fileNumber", criteria.getFileNumber());
		}
		if (null != criteria.getApplicationNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.application_number=:appNumber");
			preparedStmtList.put("appNumber", criteria.getApplicationNumber());
		}
		if (null != criteria.getState()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.state IN (:state)");
			preparedStmtList.put("state", criteria.getState());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
