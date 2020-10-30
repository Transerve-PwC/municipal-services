package org.egov.ps.repository;

import java.util.List;
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

	private static final String APP_COLUMNS = " app.id as appid, app.tenantid as apptenantid, app.property_id as appproperty_id,"
			+ " app.application_number as appapplication_number,"
			+ " app.branch_type as appbranch_type, app.module_type as appmodule_type, app.application_type as appapplication_type,"
			+ " app.comments as appcomments, app.hardcopy_received_date as apphardcopy_received_date,"
			+ " app.state as appstate, app.action as appaction," + " app.application_details as appapplication_details,"
			+ " app.created_by as appcreated_by, app.last_modified_by as applast_modified_by,"
			+ " app.created_time as appcreated_time, app.last_modified_time as applast_modified_time, "
			+ " app.bank_name as appbank_name, app.transaction_number as apptransaction_number, "
			+ " app.amount as appamount,  app.payment_type as apppayment_type, app.date_of_payment as appdate_of_payment, "

			+ " doc.id as docid, doc.reference_id as docapplication_id, doc.tenantid as doctenantid,"
			+ " doc.is_active as docis_active, doc.document_type, doc.file_store_id, doc.property_id as docproperty_id,"
			+ " doc.created_by as dcreated_by, doc.created_time as dcreated_time, "
			+ " doc.last_modified_by as dmodified_by, doc.last_modified_time as dmodified_time ";

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

	private static final String OWNER_TABLE = " cs_ep_owner_v1 ownership " + LEFT_JOIN
			+ " cs_ep_owner_details_v1 od ON ownership.id = od.owner_id ";

	private final String paginationWrapper = "SELECT * FROM "
			+ " (SELECT *, DENSE_RANK() OVER (ORDER BY applast_modified_time desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	public static final String RELATION_OWNER = "owner";
	public static final String RELATION_OWNER_DOCUMENTS = "ownerdocs";

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

	private static final String OWNER_DOCS_COLUMNS = " doc.id as docid, doc.reference_id as docreference_id, doc.tenantid as doctenantid,"
			+ " doc.is_active as docis_active, doc.document_type, doc.file_store_id, doc.property_id as docproperty_id,"
			+ " doc.created_by as dcreated_by, doc.created_time as dcreated_time, doc.last_modified_by as dmodified_by, doc.last_modified_time as dmodified_time ";

	public String getOwnerDocsQuery(List<String> ownerDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(OWNER_DOCS_COLUMNS);
		sb.append(" FROM cs_ep_documents_v1 doc ");
		sb.append(" where doc.reference_id IN (:references)");
		params.put("references", ownerDetailIds);
		return sb.toString();
	}

	public String getOwnersQuery(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(OWNER_COLUMNS);
		sb.append(" FROM " + OWNER_TABLE);
		sb.append(" where ownership.property_details_id IN (:propertyDetailIds)");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getApplicationSearchQuery(ApplicationCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(SELECT);

		builder.append(APP_COLUMNS);

		builder.append(APP_TABLE);

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
		if (null != criteria.getApplicationType()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.application_type IN (:applicationType)");
			preparedStmtList.put("applicationType", criteria.getApplicationType());
		}
		if (null != criteria.getModuleType()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.module_type IN (:moduleType)");
			preparedStmtList.put("moduleType", criteria.getModuleType());
		}
		if (null != criteria.getBranchType()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("app.branch_type IN (:branchType)");
			preparedStmtList.put("branchType", criteria.getBranchType());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
