package org.egov.cpt.repository;

import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class MortgageQueryBuilder {

	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = "INNER JOIN";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY mgModifiedTime desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private static final String MORTGAGE_SEARCH_QUERY = SELECT + "mg.*,ap.*,doc.*,pt.*,address.*,ownership.*,od.*,gd.*,"
			+ " mg.id as mgid, mg.propertyid, mg.tenantid as mgtenantid, mg.state, mg.action,mg.application_number as app_number,"
			+ "mg.modified_time as mgModifiedTime,"

			+ " pt.id as pid, pt.transit_number, pt.colony,"

			+ " ownership.allotmen_number as owner_allot_number,"
			
			+ " od.allotment_startdate as allot_start_date, od.allotment_enddate as allot_end_date,"

			+ " address.pincode, address.area,"

			+ " gd.id as gdid, gd.property_id as gdproperty_detail_id, gd.owner_id as gdowner_id, gd.tenantid as gdtenantid,"
			+ " gd.bank_name as gdbank_name, gd.mortgage_amount as gdmortgage_amount,"
			+ " gd.sanction_letter_number as gdsanction_letter_number, gd.sanction_date as gdsanction_date, gd.mortgage_end_date as gdmortgage_end_date,"
			+ " gd.created_by as gdcreated_by, gd.modified_by as gdmodified_by, gd.created_time as gdcreated_time, gd.modified_time as gdmodified_time,"

			+ " ap.id as aid, ap.mortgage_id as mg_id,ap.tenantid as aptenantid,"
			+ " ap.name,ap.email,ap.mobileno,ap.guardian,ap.relationship,ap.aadhaar_number as adhaarnumber,"

			+ " doc.id as docId,doc.reference_id as doc_referenceid, doc.tenantId as doctenantid,doc.document_type as doctype , doc.filestore_id as doc_filestoreid,"
			+ " doc.property_id as doc_propertyid, doc.is_active as doc_active"

			+ " FROM cs_pt_mortgage_application mg " + INNER_JOIN + " cs_pt_property_v1 pt on mg.propertyid=pt.id "
			+ INNER_JOIN + " cs_pt_mortgage_applicant ap ON mg.id =ap.mortgage_id " + LEFT_JOIN
			+ " cs_pt_address_v1 address ON pt.id=address.property_id " + LEFT_JOIN
			+ " cs_pt_ownership_v1 ownership ON mg.propertyid = ownership.property_id " + LEFT_JOIN
			+ " cs_pt_ownershipdetails_v1 od ON ownership.id = od.owner_id "+LEFT_JOIN
			+ " cs_pt_mortgage_approved_grantdetails gd ON pt.id=gd.property_id " + LEFT_JOIN
			+ " cs_pt_documents_v1 doc ON doc.reference_id =  mg.id";

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

	public String getMortgageSearchQuery(DuplicateCopySearchCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(MORTGAGE_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("mg.propertyid=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}
		if (null != criteria.getAppId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("mg.id=:id");
			preparedStmtList.put("id", criteria.getAppId());
		}
		if (null != criteria.getTransitNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.transit_number=:trnsNumber");
			preparedStmtList.put("trnsNumber", criteria.getTransitNumber());
		}
		if (null != criteria.getApplicationNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("mg.application_number=:appNumber");
			preparedStmtList.put("appNumber", criteria.getApplicationNumber());
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
		if (null != criteria.getStatus()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("mg.state IN(:states)");
			preparedStmtList.put("states", criteria.getStatus());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
