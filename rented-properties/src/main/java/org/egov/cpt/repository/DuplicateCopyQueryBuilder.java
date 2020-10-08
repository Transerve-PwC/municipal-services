package org.egov.cpt.repository;

import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class DuplicateCopyQueryBuilder {

	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = "INNER JOIN";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY dcModifiedTime desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private static final String DUPLICATE_COPY_SEARCH_QUERY = SELECT
			+ " dca.id as appid, dca.property_id as dcaproperty_id, dca.tenantid as pttenantid, dca.state as dcastate, dca.action as dcaaction,"
			+ " dca.application_number as app_number,"
			+ " dca.modified_time as dcModifiedTime, dca.created_by as dcCreatedBy,"

			+ " pt.id as pid, pt.transit_number as pttransit_number,pt.colony as ptcolony,pt.modified_date as pmodified_date,"

			+ " address.pincode as addresspincode, address.area as addressarea,"
			
			+ " ownership.allotmen_number as owner_allot_number,"

			+ " od.allotment_startdate as allot_start_date, od.allotment_enddate as allot_end_date,"

			+ " ap.id as aid, ap.application_id as app_id,"
			+ " ap.name as apname,ap.email as apemail,ap.mobileno as apmobileno,ap.guardian as apguardian ,"
			+ " ap.relationship as aprelationship,ap.aadhaar_number as adhaarnumber,"
			+ " ap.fee_amount as apfee_amount, ap.apro_charge as apapro_charge,"
			
			+ " doc.id as docId,doc.reference_id as doc_referenceid, doc.document_type as doctype , doc.fileStore_id as doc_filestoreid,"
			+ " doc.property_id as doc_propertyid , doc.is_active as doc_active"

			+ " FROM cs_pt_duplicate_ownership_application dca " + INNER_JOIN
			+ " cs_pt_property_v1 pt on dca.property_id=pt.id " + INNER_JOIN
			+ " cs_pt_address_v1 address ON pt.id=address.property_id " + LEFT_JOIN
			+ " cs_pt_ownership_v1 ownership ON dca.property_id = ownership.property_id and ownership.active_state=true " + LEFT_JOIN
			+ " cs_pt_ownershipdetails_v1 od ON ownership.id = od.owner_id " + LEFT_JOIN
			+ " cs_pt_duplicatecopy_applicant ap ON dca.id =ap.application_id " + LEFT_JOIN
			+ " cs_pt_documents_v1 doc ON doc.reference_id =  dca.id";

	private String addPaginationWrapper(String query, Map<String, Object> preparedStmtList,
			DuplicateCopySearchCriteria criteria) {

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

	public String getDuplicateCopyPropertySearchQuery(DuplicateCopySearchCriteria criteria,
			Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(DUPLICATE_COPY_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("dca.property_id=:prId");
			preparedStmtList.put("prId", criteria.getPropertyId());
		}
		if (null != criteria.getAppId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("dca.id=:id");
			preparedStmtList.put("id", criteria.getAppId());
		}
		if (null != criteria.getTransitNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.transit_number=:trnNumber");
			preparedStmtList.put("trnNumber", criteria.getTransitNumber());
		}
		if (null != criteria.getApplicationNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("dca.application_number=:appNumber");
			preparedStmtList.put("appNumber", criteria.getApplicationNumber());
		}
		if (null != criteria.getCreatedBy()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("dca.created_by = :createdBy");
			preparedStmtList.put("createdBy", criteria.getCreatedBy());
		}
		if (null != criteria.getColony()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.colony=:colony");
			preparedStmtList.put("colony", criteria.getColony());
		}
		if (null != criteria.getApplicantMobNo()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("ap.mobileno=:mobNo");
			preparedStmtList.put("mobNo", criteria.getApplicantMobNo());
		}
		if (null != criteria.getStatus()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("dca.state IN (:state)");
			preparedStmtList.put("state", criteria.getStatus());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
