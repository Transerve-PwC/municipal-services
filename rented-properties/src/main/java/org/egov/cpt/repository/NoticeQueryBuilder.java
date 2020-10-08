package org.egov.cpt.repository;

import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.NoticeSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class NoticeQueryBuilder {

	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = "INNER JOIN";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY ngModifiedTime desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private static final String NOTICE_SEARCH_QUERY = SELECT 
			+ " ng.id as ngid, ng.propertyid as ngpropertyid, ng.tenantid as ngtenantid,ng.memo_number as memoNumber,"
			+ "ng.memo_date as memoDate,ng.notice_type as noticeType,ng.guardian as guardian,ng.relationship as relationship,"
			+ "ng.violations as violations,ng.description as description,ng.demand_notice_from as demandNoticeFrom,"
			+ "ng.demand_notice_to as demandNoticeTo,ng.recovery_type as recoveryType, ng.amount as amount,"
			+ "ng.created_by as ngcreated_by, ng.modified_time as ngModifiedTime,"

			+ " pt.id as pid, pt.transit_number as pttransit_number, pt.colony as ptcolony,"

			+ " doc.id as docId,doc.reference_id as doc_referenceId,doc.document_type as doctype , doc.filestore_id as doc_filestoreid,"
			+ " doc.property_id as doc_propertyid , doc.is_active as doc_active, doc.created_by as doccreated_by, doc.modified_date as docmodified_date"

			+ " FROM cs_pt_notice_generation_application ng " + INNER_JOIN
			+ " cs_pt_property_v1 pt on ng.propertyid=pt.id " + LEFT_JOIN
			+ " cs_pt_documents_v1 doc ON doc.reference_id =  ng.id";

	private String addPaginationWrapper(String query, Map<String, Object> preparedStmtList,
			NoticeSearchCriteria criteria) {

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

	public String getNoticeSearchQuery(NoticeSearchCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(NOTICE_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("ng.propertyid=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}
		if (null != criteria.getNoticeId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("ng.id=:id");
			preparedStmtList.put("id", criteria.getNoticeId());
		}
		if (null != criteria.getTransitNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.transit_number=:trnsNumber");
			preparedStmtList.put("trnsNumber", criteria.getTransitNumber());
		}
		if (null != criteria.getMemoNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("ng.memo_number=:memoNumber");
			preparedStmtList.put("memoNumber", criteria.getMemoNumber());
		}
		if (null != criteria.getColony()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.colony=:colony");
			preparedStmtList.put("colony", criteria.getColony());
		}
		if (null != criteria.getApplicantMobNo()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("ap.mobileno=:mobNumber");
			preparedStmtList.put("mobNumber", criteria.getApplicantMobNo());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
