package org.egov.cpt.repository;

import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class PropertyImagesQueryBuilder {

	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = "INNER JOIN";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY piModifiedTime desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private static final String PROPERTY_IMAGES_SEARCH_QUERY = SELECT
			+ " pi.id as piid, pi.propertyid, pi.tenantid as pitenantid, pi.application_number as app_number, pi.description as pidescription,"
			+ "pi.modified_time as piModifiedTime,pi.created_by as piCreateBy,"

			+ " pt.id as pid, pt.transit_number, pt.colony,"

			+ " address.pincode, address.area,"

			+ " doc.id as docId,doc.reference_id as doc_referenceId, doc.document_type as doctype , doc.filestore_id as doc_filestoreid,"
			+ " doc.property_id as doc_propertyId , doc.is_active as doc_active"

			+ " FROM cs_pt_property_images_application pi " + INNER_JOIN
			+ " cs_pt_property_v1 pt on pi.propertyid=pt.id " + INNER_JOIN
			+ " cs_pt_address_v1 address ON pt.id=address.property_id " + LEFT_JOIN
			+ " cs_pt_property_images_document doc ON doc.property_images_application_id =  pi.id";

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

	public String getPropertyImagesSearchQuery(DuplicateCopySearchCriteria criteria,
			Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(PROPERTY_IMAGES_SEARCH_QUERY);

		if (!ObjectUtils.isEmpty(criteria.getPropertyId())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pi.propertyid=:propId");
			preparedStmtList.put("propId", criteria.getPropertyId());
		}
		if (null != criteria.getAppId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pi.id=:id");
			preparedStmtList.put("id", criteria.getAppId());
		}
		if (null != criteria.getTransitNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.transit_number=:trnsNumber");
			preparedStmtList.put("trnsNumber", criteria.getTransitNumber());
		}
		if (null != criteria.getApplicationNumber()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pi.application_number=:appNumber");
			preparedStmtList.put("appNumber", criteria.getApplicationNumber());
		}
		if (null != criteria.getColony()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.colony=:colony");
			preparedStmtList.put("colony", criteria.getColony());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}
}
