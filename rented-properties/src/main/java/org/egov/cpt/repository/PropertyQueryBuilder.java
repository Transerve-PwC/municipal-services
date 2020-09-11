package org.egov.cpt.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.PropertyCriteria;
import org.egov.cpt.util.PTConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class PropertyQueryBuilder {

	@Autowired
	private PropertyConfiguration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = "INNER JOIN";
	private static final String LEFT_JOIN = "LEFT OUTER JOIN";

	private static final String PT_ALL = " pt.*, ptdl.*,doc.*,address.*, ";
	private static final String OWNER_ALL = " ownership.*,od.*, ";
	private static final String PI_ALL = " pi.*,pidoc.*, ";
	private static final String NOTICE_ALL = " ng.*,ngdoc.*, ";
	private static final String GD_ALL = " gd.*,";

	private static final String PT_COLUMNS = " pt.id as pid, pt.transit_number, pt.tenantid as pttenantid, pt.colony, pt.master_data_state, pt.master_data_action,"
			+ " pt.created_by as pcreated_by, pt.created_date as pcreated_date, pt.modified_by as pmodified_by, pt.modified_date as pmodified_date,"

			+ " ptdl.id as pdid, ptdl.property_id as pdproperty_id, ptdl.transit_number as pdtransit_number,"
			+ " ptdl.interest_rate as pd_int_rate,ptdl.rent_increment_percentage as pd_rent_inc_pg,ptdl.rent_increment_period as pd_rent_inc_period,"
			+ " ptdl.tenantid as pdtenantid, ptdl.area, ptdl.rent_per_sqyd, ptdl.current_owner, ptdl.floors, ptdl.additional_details, "

			+ " address.id as aid, address.property_id as aproperty_id, address.transit_number as atransit_number,"
			+ " address.tenantid as atenantid, address.colony, address.area as addressArea, address.district,"
			+ " address.state, address.country, address.pincode, address.landmark,"

			+ " doc.id as docid, doc.property_id as docproperty_id,doc.reference_id as docreference_id, doc.tenantid as doctenantid,"
			+ " doc.is_active as docis_active, doc.document_type as doc_document_type, doc.fileStore_id as doc_fileStore_id,"
			+ " doc.created_by as dcreated_by, doc.created_date as dcreated_date, doc.modified_by as dmodified_by, doc.modified_date as dmodified_date ";

	private static final String OWNER_COLUMNS = " ownership.id as oid, ownership.property_id as oproperty_id,"
			+ " ownership.tenantid as otenantid, ownership.allotmen_number as oallotmen_number,"
			+ " ownership.active_state as oactive_state, ownership.is_primary_owner as ois_primary_owner,"
			+ " ownership.created_by as ocreated_by, ownership.created_date as ocreated_date, ownership.modified_by as omodified_by, ownership.modified_date as omodified_date,"

			+ " od.id as odid, od.property_id as odproperty_id," + " od.owner_id odowner_id, od.tenantid as odtenantid,"
			+ " od.name, od.email, od.phone,"
			+ " od.gender, od.date_of_birth, od.aadhaar_number,od.permanent as od_permanent,"
			+ " od.allotment_startdate, od.allotment_enddate," + " od.posession_startdate, od.posession_enddate,"
			+ " od.monthly_rent, od.revision_period, od.revision_percentage, od.father_or_husband, od.relation, od.application_number as odapplication_number ";

	private static final String PI_COLUMNS = " pi.id as piid, pi.propertyid as pipropertyid, pi.tenantid as pitenantid, pi.application_number as piapp_number, pi.description as pidescription, pi.capturedby as picapturedBy,"
			+ " pi.created_by as piCreatedBy, pi.created_time as piCreatedTime, pi.modified_by as piModifiedBy, pi.modified_time as piModifiedTime,"

			+ " pidoc.id as pidocid,pidoc.reference_id as pidoc_referenceid, pidoc.tenantId as pidoctenantid, pidoc.document_type as pidoctype , pidoc.filestore_id as pidoc_filestoreid,"
			+ " pidoc.property_id as pidoc_propertyid , pidoc.is_active as pidoc_active ";

	private static final String NOTICE_COLUMNS = " ng.id as ngid, ng.propertyid as ng_propertyid, ng.tenantid as ngtenantid,ng.memo_number as ng_memoNumber,"
			+ " ng.memo_date as ng_memoDate,ng.notice_type as ng_noticeType,ng.guardian as ng_guardian,ng.relationship as ng_relationship,"
			+ " ng.violations as ng_violations,ng.description as ng_description,ng.demand_notice_from as ng_demandNoticeFrom,"
			+ " ng.demand_notice_to as ng_demandNoticeTo,ng.recovery_type as ng_recoveryType, ng.amount as ng_amount, ng.property_image_id as ng_p_image_id,"
			+ " ng.modified_time as ngModifiedTime,ng.modified_by as ngModifiedBy,ng.created_by as ngCreatedBy,ng.created_time as ngCreatedTime,"

			+ " ngdoc.id as ngdoc_id,ngdoc.reference_id as ngdoc_referenceid, ngdoc.tenantId as ngdoc_tenantid,ngdoc.document_type as ngdoc_type , ngdoc.filestore_id as ngdoc_filestoreid,"
			+ " ngdoc.property_id as ngdoc_propertyid , ngdoc.is_active as ngdoc_active ";

	private static final String GD_COLUMNS = " gd.id as gd_id, gd.property_id as gd_property_id,"
			+ " gd.bank_name as gd_bank_name, gd.mortgage_amount as gd_mortgage_amount,"
			+ " gd.sanction_letter_number as gd_sanLetterNum, gd.sanction_date as gd_sanDate, gd.mortgage_end_date as gd_mortgageEndDate,"
			+ " gd.created_by as gd_createdBy, gd.modified_by as gd_modifiedBy, gd.created_time as gd_createdTime, gd.modified_time as gd_modifiedTime ";

	private static final String PT_TABLE = " FROM cs_pt_property_v1 pt " + INNER_JOIN
			+ " cs_pt_propertydetails_v1 ptdl ON pt.id =ptdl.property_id " + LEFT_JOIN
			+ " cs_pt_address_v1 address ON pt.id=address.property_id " + LEFT_JOIN
			+ " cs_pt_documents_v1 doc ON pt.id=doc.reference_id ";

	private static final String OWNER_TABLE = " cs_pt_ownership_v1 ownership ON pt.id=ownership.property_id "
			+ INNER_JOIN + " cs_pt_ownershipdetails_v1 od ON ownership.id = od.owner_id AND od.permanent = true ";

	private static final String PI_TABLE = " cs_pt_property_images_application pi ON pi.propertyid = pt.id " + LEFT_JOIN
			+ " cs_pt_documents_v1 pidoc ON pidoc.reference_id =  pi.id ";

	private static final String NOTICE_TABLE = " cs_pt_notice_generation_application ng ON ng.propertyid = pt.id "
			+ LEFT_JOIN + " cs_pt_documents_v1 ngdoc ON ngdoc.reference_id =  ng.id ";

	private static final String GD_TABLE = " cs_pt_mortgage_approved_grantdetails gd ON pt.id=gd.property_id ";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY pmodified_date desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	private String addPaginationWrapper(String query, Map<String, Object> preparedStmtList, PropertyCriteria criteria) {

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

	/**
	 * 
	 * @param criteria
	 * @param preparedStmtList
	 * @return
	 */
	public String getPropertySearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {

		// StringBuilder builder = new StringBuilder(SEARCH_QUERY);
		StringBuilder builder;
		List<String> relations = criteria.getRelations();

		if (relations == null) {
			builder = new StringBuilder(SELECT);
			builder.append(PT_ALL + OWNER_ALL + PI_ALL + NOTICE_ALL + GD_ALL);
			builder.append(
					PT_COLUMNS + "," + OWNER_COLUMNS + "," + PI_COLUMNS + "," + NOTICE_COLUMNS + " , " + GD_COLUMNS);
			builder.append(PT_TABLE + LEFT_JOIN + OWNER_TABLE + LEFT_JOIN + PI_TABLE + LEFT_JOIN + NOTICE_TABLE
					+ LEFT_JOIN + GD_TABLE);
		} else {

			builder = new StringBuilder(SELECT);
			String columns[] = { PT_COLUMNS };
			List<String> columnList = new ArrayList<>(Arrays.asList(columns));

			String tables[] = { PT_TABLE };
			List<String> tableList = new ArrayList<>(Arrays.asList(tables));

			builder.append(PT_ALL);

			if (relations.contains(PTConstants.RELATION_OWNER)) {
				builder.append(OWNER_ALL);
			}

			if (relations.contains(PTConstants.RELATION_PI)) {
				builder.append(PI_ALL);
			}

			if (relations.contains(PTConstants.RELATION_NOTICE)) {
				builder.append(NOTICE_ALL);
			}

			if (relations.contains(PTConstants.RELATION_GD)) {
				builder.append(GD_ALL);
			}

			// columns
			if (relations.contains("owner")) {
				columnList.add(OWNER_COLUMNS);
			}

			if (relations.contains("propertyImage")) {
				columnList.add(PI_COLUMNS);
			}

			if (relations.contains("notice")) {
				columnList.add(NOTICE_COLUMNS);
			}

			if (relations.contains("grantDetail")) {
				columnList.add(GD_COLUMNS);
			}

			String output = columnList.stream().reduce(null, (str1, str2) -> str1 == null ? str2 : str1 + " , " + str2);
			builder.append(output);

			// Joins
			if (relations.contains("owner")) {
				tableList.add(OWNER_TABLE);
			}

			if (relations.contains("propertyImage")) {
				tableList.add(PI_TABLE);
			}

			if (relations.contains("notice")) {
				tableList.add(NOTICE_TABLE);
			}

			if (relations.contains("grantDetail")) {
				tableList.add(GD_TABLE);
			}

			String tableOutput = tableList.stream().reduce(null,
					(str1, str2) -> str1 == null ? str2 : str1 + LEFT_JOIN + str2);
			builder.append(tableOutput);
		}

		if (!ObjectUtils.isEmpty(criteria.getTransitNumber())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.transit_number=:trnNumber");
			preparedStmtList.put("trnNumber", criteria.getTransitNumber());
		}

		if (null != criteria.getColony()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.colony = :colony");
			preparedStmtList.put("colony", criteria.getColony());
		}

		if (null != criteria.getName()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("od.name = :name");
			preparedStmtList.put("name", criteria.getName());
		}

		if (null != criteria.getPhone()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("od.phone = :phone");
			preparedStmtList.put("phone", criteria.getPhone());
		}

		if (null != criteria.getState()) {
			if (criteria.getState().contains(PTConstants.PM_DRAFTED)) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append("pt.master_data_state IN (:states)");
				preparedStmtList.put("states", criteria.getState());
				builder.append(" AND ");
				builder.append("pt.created_by =:createdBy");
				preparedStmtList.put("createdBy", criteria.getCreatedBy());
			} else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append("pt.master_data_state IN (:states)");
				preparedStmtList.put("states", criteria.getState());
				builder.append(" OR ");
				builder.append("pt.created_by =:createdBy");
				preparedStmtList.put("createdBy", criteria.getCreatedBy());
			}
		}

		if (null != criteria.getPropertyId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.id = :id");
			preparedStmtList.put("id", criteria.getPropertyId());
		}

		if (relations == null || relations.contains(PTConstants.RELATION_OWNER)) {
			builder.append(" ORDER BY ownership.active_state desc, ownership.created_date desc ");
		}
		if(relations == null || relations.contains(PTConstants.RELATION_NOTICE)) {
			builder.append(", ng.created_time desc");
		}
		if(relations == null || relations.contains(PTConstants.RELATION_PI)) {
			builder.append(", pi.created_time desc");
		}
		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}

	private static void addClauseIfRequired(Map<String, Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND ");
		}
	}
}
