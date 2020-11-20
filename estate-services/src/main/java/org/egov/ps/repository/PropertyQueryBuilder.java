package org.egov.ps.repository;

import java.util.List;
import java.util.Map;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.util.PSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PropertyQueryBuilder {

	@Autowired
	private Configuration config;

	private static final String SELECT = "SELECT ";
	private static final String INNER_JOIN = " INNER JOIN ";
	private static final String LEFT_JOIN = " LEFT OUTER JOIN ";

	private static final String PT_COLUMNS = " pt.id as pid, pt.file_number, pt.tenantid as pttenantid, pt.category, pt.sub_category, "
			+ " pt.site_number, pt.sector_number, pt.state as pstate, pt.action as paction, pt.created_by as pcreated_by, pt.created_time as pcreated_time, "
			+ " pt.last_modified_by as pmodified_by, pt.last_modified_time as pmodified_time, "
			+ " pt.property_master_or_allotment_of_site, pt.is_cancelation_of_site, "

			+ " ptdl.id as ptdlid, ptdl.property_id as pdproperty_id, ptdl.branch_type as branch_type, ptdl.property_type as pdproperty_type, "
			+ " ptdl.tenantid as pdtenantid, ptdl.type_of_allocation, ptdl.mode_of_auction, ptdl.scheme_name,ptdl.date_of_auction, "
			+ " ptdl.area_sqft, ptdl.rate_per_sqft, ptdl.last_noc_date, ptdl.service_category, "
			+ " ptdl.is_property_active, ptdl.trade_type, ptdl.company_name, ptdl.company_address, ptdl.company_registration_number, "
			+ " ptdl.company_registration_date, ptdl.decree_date, ptdl.court_details, ptdl.civil_titled_as, ptdl.company_or_firm, "
			+ " ptdl.company_type, ptdl.emd_amount, ptdl.emd_date , ptdl.property_registered_to, ptdl.entity_type, "
			+ " ptdl.house_number, ptdl.mohalla, ptdl.village, ptdl.interest_rate ";

	private static final String OWNER_COLUMNS = " ownership.id as oid, ownership.property_details_id as oproperty_details_id, "
			+ " ownership.tenantid as otenantid, ownership.serial_number as oserial_number, "
			+ " ownership.share as oshare, ownership.cp_number as ocp_number, ownership.state as ostate, ownership.action as oaction, "
			+ " ownership.created_by as ocreated_by, ownership.created_time as ocreated_time, ownership.ownership_type, "
			+ " ownership.last_modified_by as omodified_by, ownership.last_modified_time as omodified_time, "

			+ " od.id as odid, od.owner_id as odowner_id,"
			+ " od.owner_name as odowner_name, od.tenantid as odtenantid,"
			+ " od.guardian_name, od.guardian_relation, od.mobile_number,"
			+ " od.allotment_number, od.date_of_allotment, od.possesion_date, od.is_approved, "
			+ " od.is_current_owner, od.is_master_entry, od.address, od.is_director, od.is_previous_owner_required, "
			+ " od.seller_name, od.seller_guardian_name, od.seller_relation, od.mode_of_transfer, od.dob ";

	// + " payment.id as payid, payment.tenantid as paytenantid,
	// payment.property_details_id as payproperty_details_id, "
	// + " payment.payment_type, payment.due_date_of_payment, payment.payable,
	// payment.amount, "
	// + " payment.total, payment.date_of_deposit, payment.delay_in_payment, "
	// + " payment.interest_for_delay, payment.total_amount_due_with_interest,
	// payment.amount_deposited, "
	// + " payment.amount_deposited_intt, payment.balance, payment.balance_intt, "
	// + " payment.total_due, payment.receipt_number, payment.receipt_date, "
	// + " payment.st_rate_of_st_gst, payment.st_amount_of_gst,
	// payment.st_payment_made_by, "
	// + " payment.bank_name, payment.cheque_number, "
	// + " payment.installment_one, payment.installment_two,
	// payment.installment_two_due_date, payment.installment_three,
	// payment.installment_three_due_date, "
	// + " payment.monthly_or_annually, payment.ground_rent_start_date,
	// payment.rent_revision, "
	// + " payment.lease_period, payment.license_fee_of_year, payment.license_fee,
	// payment.security_amount, payment.security_date, "
	// + " payment.created_by as paycreated_by, payment.created_time as
	// paycreated_time, payment.last_modified_by as paymodified_by,
	// payment.last_modified_time as paymodified_time ";

	private static final String CC_COLUMNS = " cc.id as ccid, cc.property_details_id as ccproperty_details_id,"
			+ " cc.tenantid as cctenantid, cc.estate_officer_court as ccestate_officer_court,"
			+ " cc.commissioners_court as cccommissioners_court, cc.chief_administartors_court as ccchief_administartors_court, cc.advisor_to_admin_court as ccadvisor_to_admin_court, cc.honorable_district_court as cchonorable_district_court,"
			+ " cc.honorable_high_court as cchonorable_high_court, cc.honorable_supreme_court as cchonorable_supreme_court,"
			+ " cc.created_by as cccreated_by, cc.created_time as cccreated_time, cc.last_modified_by as ccmodified_by, cc.last_modified_time as ccmodified_time ";

	private static final String BIDDER_COLUMNS = " aut.id as auid, aut.auction_id as auauction_id, aut.property_details_id as auproperty_details_id,"
			+ " aut.description as audescription, "
			+ " aut.bidder_name as aubidder_name, aut.deposited_emd_amount as audeposited_emd_mount, "
			+ " aut.deposit_date as audeposit_date, aut.emd_validity_date as auemd_validity_date, aut.refund_status as aurefund_status, "
			+ " aut.comments as aucomments, aut.state as austate, aut.action as auaction, "
			+ " aut.created_by as aucreated_by, aut.last_modified_by as aulast_modified_by, aut.created_time as aucreated_time, "
			+ " aut.last_modified_time as aulast_modified_time ";

	private static final String ESTATE_DEMAND_COLUMNS = " estd.id as estdid, estd.property_details_id as estdproperty_details_id, "
			+ " estd.demand_date as estddemand_date, estd.is_previous as estdis_previous, "
			+ " estd.rent as estdrent, estd.penalty_interest as estdpenalty_interest, "
			+ " estd.gst_interest as estdgst_interest, estd.gst as estdgst, estd.collected_rent as estdcollected_rent, "
			+ " estd.collected_gst as estdcollected_gst, estd.no_of_days as estdno_of_days, estd.paid as estdpaid, "
			+ " estd.remaining_rent_penalty as estdremaining_rent_penalty, estd.remaining_gst_penalty as estdremaining_gst_penalty,"
			+ " estd.remaining_rent as estdremaining_rent,estd.collected_gst_penalty as estdcollected_gst_penalty, "
			+ " estd.collected_rent_penalty as estdcollected_rent_penalty,estd.interest_since as estdinterest_since, estd.remaining_gst as estdremaining_gst,"
			+ " estd.created_by as estdcreated_by, estd.last_modified_by as estdlast_modified_by, "
			+ " estd.created_time as estdcreated_time, estd.last_modified_time as estdlast_modified_time ";

	private static final String ESTATE_PAYMENT_COLUMNS = " estp.id as estpid, estp.property_details_id as estpproperty_details_id, "
			+ " estp.receipt_date as estpreceipt_date, estp.rent_received as estprent_received, estp.receipt_no as estpreceipt_no, "
			+ " estp.payment_date as estpayment_date, estp.created_by as estpcreated_by, estp.last_modified_by as estplast_modified_by, "
			+ " estp.created_time as estpcreated_time, estp.last_modified_time as estplast_modified_time ";

	private static final String PT_TABLE = " FROM cs_ep_property_v1 pt " + INNER_JOIN
			+ " cs_ep_property_details_v1 ptdl  ON pt.id =ptdl.property_id ";

	private static final String OWNER_TABLE = " cs_ep_owner_v1 ownership " + LEFT_JOIN
			+ " cs_ep_owner_details_v1 od ON ownership.id = od.owner_id ";

	private static final String ACCOUNT_SEARCH_COLUMN = " account.id as account_id,account.property_details_id as account_pdid,account.remainingAmount as account_remainingAmount,account.remaining_since as account_remaining_since,"
			+ " account.created_by as account_created_by, account.created_date as account_created_date,"
			+ " account.modified_by as account_modified_by,account.modified_date as account_modified_date ";

	private static final String OWNER_DOCS_COLUMNS = " doc.id as docid, doc.reference_id as docreference_id, doc.tenantid as doctenantid,"
			+ " doc.is_active as docis_active, doc.document_type, doc.file_store_id, doc.property_id as docproperty_id,"
			+ " doc.created_by as dcreated_by, doc.created_time as dcreated_time, doc.last_modified_by as dmodified_by, doc.last_modified_time as dmodified_time ";

	private static final String OFFLINE_PAYMENT_COLUMN = " offline.id as offlineid, "
			+ " offline.property_details_id as offlineproperty_details_id, offline.demand_id as offlinedemand_id, "
			+ " offline.amount as offlineamount, offline.bank_name as offlinebank_name, offline.type as offline_type, "
			+ " offline.transaction_number as offlinetransaction_number, offline.date_of_payment as offlinedate_of_payment ";

	private static final String PROPERTY_PENALTY_COLUMN = " penalty.id as penalty_id, penalty.tenantid as penalty_tenantid, "
			+ " penalty.property_id as penalty_property_id, penalty.branch_type as penalty_branch_type, "
			+ " penalty.penalty_amount as penalty_penalty_amount, penalty.violation_type as penalty_violation_type, "
			+ " penalty.paid as penalty_paid, penalty.generation_date as penalty_generation_date, "
			+ " penalty.remaining_penalty_due as penalty_remaining_penalty_due, penalty.status as penalty_status, "
			+ " penalty.created_by as penalty_created_by, penalty.last_modified_by as penalty_last_modified_by, "
			+ " penalty.created_time as penalty_created_time, penalty.last_modified_time as penalty_last_modified_time ";

	private static final String EXTENSION_FEE_COLUMN = " ef.id as ef_id, ef.tenantid as ef_tenantid, "
			+ " ef.property_id as ef_property_id, ef.branch_type as ef_branch_type, "
			+ " ef.amount as ef_amount, ef.remaining_due as ef_remaining_due, "
			+ " ef.paid as ef_paid, ef.status as ef_status, ef.generation_date as ef_generation_date, "
			+ " ef.created_by as ef_created_by, ef.last_modified_by as ef_last_modified_by, "
			+ " ef.created_time as ef_created_time, ef.last_modified_time as ef_last_modified_time ";

	private static final String PROPERTY_PAYMENT_CONFIG_COLUMNS = " pc.id as pc_id, "
			+ " pc.tenant_id as pc_tenant_id, pc.property_details_id as pc_property_details_id, "
			+ " pc.is_intrest_applicable as pc_is_intrest_applicable, pc.due_date_of_payment as pc_due_date_of_payment, "
			+ " pc.no_of_months as pc_no_of_months, pc.rate_of_interest as pc_rate_of_interest, "
			+ " pc.security_amount as pc_security_amount, pc.total_amount as pc_total_amount, "
			+ " pc.is_ground_rent as pc_is_ground_rent, pc.ground_rent_generation_type as pc_ground_rent_generation_type, "
			+ " pc.ground_rent_generate_demand as pc_ground_rent_generate_demand, pc.ground_rent_advance_rent as pc_ground_rent_advance_rent, "
			+ " pc.ground_rent_bill_start_date as pc_ground_rent_bill_start_date, pc.ground_rent_advance_rent_date as pc_ground_rent_advance_rent_date, "
			+ " pc.created_by as pc_created_by, pc.last_modified_by as pc_last_modified_by, "
			+ " pc.created_time as pc_created_time, pc.last_modified_time as pc_last_modified_time, "

			+ " pci.id as pci_id, pci.tenant_id as pci_tenant_id, "
			+ " pci.payment_config_id as pci_payment_config_id, pci.ground_rent_amount as pci_ground_rent_amount, "
			+ " pci.ground_rent_start_month as pci_ground_rent_start_month, pci.ground_rent_end_month as pci_ground_rent_end_month, "

			+ " paci.id as paci_id, paci.tenant_id as paci_tenant_id, "
			+ " paci.payment_config_id as paci_payment_config_id, paci.premium_amount as paci_premium_amount, "
			+ " paci.premiumamountdate as paci_premiumamountdate ";

	// + LEFT_JOIN
	// + " cs_ep_payment_v1 payment ON ptdl.id=payment.property_details_id ";

	private static final String CC_TABLE = " cs_ep_court_case_v1 cc ";

	private static final String BIDDER_TABLE = " cs_ep_auction aut ";

	private static final String ESTATE_DEMAND_TABLE = " cs_ep_demand estd ";

	private static final String ESTATE_PAYMENT_TABLE = " cs_ep_payment estp ";

	private static final String OFFLINE_PAYMENT_TABLE = " cs_ep_offline_payment_detail offline ";

	private static final String ESTATE_ACCOUNT_COLUMN = " cs_ep_account account ";

	private static final String PROPERTY_PENALTY_TABLE = " cs_ep_property_penalty_v1 penalty ";

	private static final String EXTENSION_FEE_TABLE = " cs_ep_extension_fee_v1 ef ";

	private static final String PROPERTY_PAYMENT_CONFIG_TABLE = " cs_ep_payment_config_v1 pc " + LEFT_JOIN
			+ " cs_ep_payment_config_items_v1 pci ON pc.id = pci.payment_config_id " + LEFT_JOIN
			+ " cs_ep_premium_amount_config_items_v1 paci ON pc.id = paci.payment_config_id ";

	private final String paginationWrapper = "SELECT * FROM "
			+ "(SELECT *, DENSE_RANK() OVER (ORDER BY pmodified_time desc) offset_ FROM " + "({})"
			+ " result) result_offset " + "WHERE offset_ > :start AND offset_ <= :end";

	public static final String RELATION_OWNER = "owner";
	public static final String RELATION_OWNER_DOCUMENTS = "ownerdocs";
	public static final String RELATION_COURT = "court";
	public static final String RELATION_BIDDER = "bidder";
	public static final String RELATION_ESTATE_FINANCE = "finance";
	public static final String RELATION_OFFLINE_PAYMENT = "offline";
	public static final String RELATION_PAYMENT_CONFIG = "paymentconfig";

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

		log.debug(finalQuery);

		return finalQuery;
	}

	/**
	 *
	 * @param criteria
	 * @param preparedStmtList
	 * @return
	 */
	public String getPropertySearchQuery(PropertyCriteria criteria, Map<String, Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(SELECT);

		builder.append(PT_COLUMNS);

		builder.append(PT_TABLE);

		if (null != criteria.getState()) {
			addClauseIfRequired(preparedStmtList, builder);
			if (criteria.getUserId() != null) {
				if (criteria.getState().contains(PSConstants.PM_DRAFTED)) {
					builder.append("pt.created_by = '" + criteria.getUserId() + "' AND ");
				} else {
					builder.append("pt.created_by = '" + criteria.getUserId() + "' OR ");
				}
			}
			builder.append("pt.state IN (:state)");
			preparedStmtList.put("state", criteria.getState());
		}

		if (!ObjectUtils.isEmpty(criteria.getFileNumber())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.file_number=:fileNumber");
			preparedStmtList.put("fileNumber", criteria.getFileNumber());
		}

		if (null != criteria.getCategory()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.category = :category");
			preparedStmtList.put("category", criteria.getCategory());
		}

		if (null != criteria.getPropertyId()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.id = :id");
			preparedStmtList.put("id", criteria.getPropertyId());
		}

		if (!CollectionUtils.isEmpty(criteria.getPropertyIds())) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("pt.id IN (:pids)");
			preparedStmtList.put("pids", criteria.getPropertyIds());
		}

		if (null != criteria.getBranchType()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append("ptdl.branch_type = :branch_type");
			preparedStmtList.put("branch_type", criteria.getBranchType());
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

	public String getCourtCasesQuery(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(CC_COLUMNS);
		sb.append(" FROM " + CC_TABLE);
		sb.append(" where cc.property_details_id IN (:propertyDetailIds)");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getBiddersQuery(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(BIDDER_COLUMNS);
		sb.append(" FROM " + BIDDER_TABLE);
		sb.append(" where aut.property_details_id IN (:propertyDetailIds)");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getEstateDemandQuery(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(ESTATE_DEMAND_COLUMNS);
		sb.append(" FROM " + ESTATE_DEMAND_TABLE);
		sb.append(" where estd.property_details_id IN (:propertyDetailIds)");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getEstatePaymentsQuery(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(ESTATE_PAYMENT_COLUMNS);
		sb.append(" FROM " + ESTATE_PAYMENT_TABLE);
		sb.append(" where estp.property_details_id IN (:propertyDetailIds)");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getPropertyRentAccountSearchQuery(List<String> propertyDetailIds,
			Map<String, Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(SELECT);
		builder.append(ACCOUNT_SEARCH_COLUMN);
		builder.append(" FROM " + ESTATE_ACCOUNT_COLUMN);
		if (!ObjectUtils.isEmpty(propertyDetailIds)) {
			builder.append(" where account.property_details_id IN (:propDetailsId)");
			preparedStmtList.put("propDetailsId", propertyDetailIds);
		}
		return builder.toString();
	}

	public String getOfflinePaymentsQuery(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(OFFLINE_PAYMENT_COLUMN);
		sb.append(" FROM " + OFFLINE_PAYMENT_TABLE);
		sb.append(" where offline.property_details_id IN (:propertyDetailIds)");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getEstateAccountQuery(List<String> propertyDetailsIds, Map<String, Object> params) {
		StringBuilder builder = new StringBuilder(SELECT);
		builder.append(ACCOUNT_SEARCH_COLUMN);
		builder.append(" FROM " + ESTATE_ACCOUNT_COLUMN);
		builder.append(" where account.property_details_id IN (:propertyDetailsIds)");
		params.put("propertyDetailsIds", propertyDetailsIds);
		return builder.toString();
	}

	public String getPropertyPenaltyQuery(String propertyId, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(PROPERTY_PENALTY_COLUMN);
		sb.append(" FROM " + PROPERTY_PENALTY_TABLE);
		sb.append(" where penalty.property_id IN (:propertyId)");
		params.put("propertyId", propertyId);
		return sb.toString();
	}

	public String getExtensionFeeQuery(String propertyId, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(EXTENSION_FEE_COLUMN);
		sb.append(" FROM " + EXTENSION_FEE_TABLE);
		sb.append(" where ef.property_id IN (:propertyId)");
		params.put("propertyId", propertyId);
		return sb.toString();
	}

	public String getPaymentConfigQuery(List<String> propertyDetailsIds, Map<String, Object> params) {
		StringBuilder builder = new StringBuilder(SELECT);
		builder.append(PROPERTY_PAYMENT_CONFIG_COLUMNS);
		builder.append(" FROM " + PROPERTY_PAYMENT_CONFIG_TABLE);
		builder.append(" where pc.property_details_id IN (:propertyDetailsIds)");
		params.put("propertyDetailsIds", propertyDetailsIds);
		return builder.toString();
	}

	public String getEstateDemandQueryWithDateASC(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(ESTATE_DEMAND_COLUMNS);
		sb.append(" FROM " + ESTATE_DEMAND_TABLE);
		sb.append(" where estd.property_details_id IN (:propertyDetailIds)");
		sb.append(" order by demand_date asc ");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}

	public String getEstatePaymentQueryWithDateASC(List<String> propertyDetailIds, Map<String, Object> params) {
		StringBuilder sb = new StringBuilder(SELECT);
		sb.append(ESTATE_PAYMENT_COLUMNS);
		sb.append(" FROM " + ESTATE_PAYMENT_TABLE);
		sb.append(" where estp.property_details_id IN (:propertyDetailIds)");
		sb.append(" order by payment_date asc ");
		params.put("propertyDetailIds", propertyDetailIds);
		return sb.toString();
	}
}
