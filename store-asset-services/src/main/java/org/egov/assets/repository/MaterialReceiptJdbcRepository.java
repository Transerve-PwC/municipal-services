/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.assets.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.assets.common.JdbcRepository;
import org.egov.assets.common.MdmsRepository;
import org.egov.assets.common.Pagination;
import org.egov.assets.model.Department;
import org.egov.assets.model.MaterialBalanceRate;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.MaterialReceiptSearch;
import org.egov.assets.repository.entity.MaterialBalanceRateEntity;
import org.egov.assets.repository.entity.MaterialReceiptEntity;
import org.egov.common.contract.request.RequestInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MaterialReceiptJdbcRepository extends JdbcRepository {

	private static final Logger LOG = LoggerFactory.getLogger(MaterialReceipt.class);
	
	
	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private MdmsRepository mdmsRepository;

	static {
		init(MaterialReceiptEntity.class);
	}

	public Pagination<MaterialReceipt> search(MaterialReceiptSearch materialReceiptSearch) {
		String searchQuery = "select * from materialreceipt" + " :condition :orderby";
		StringBuffer params = new StringBuffer();
		Map<String, Object> paramValues = new HashMap<>();

		if (materialReceiptSearch.getSortBy() != null && !materialReceiptSearch.getSortBy().isEmpty()) {
			validateSortByOrder(materialReceiptSearch.getSortBy());
			validateEntityFieldName(materialReceiptSearch.getSortBy(), MaterialReceiptSearch.class);
		}

		String orderBy = "order by mrnnumber";

		if (materialReceiptSearch.getSortBy() != null && !materialReceiptSearch.getSortBy().isEmpty()) {
			orderBy = "order by " + materialReceiptSearch.getSortBy();
		}

		if (materialReceiptSearch.getIds() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("id in (:ids)");
			paramValues.put("ids", materialReceiptSearch.getIds());
		}

		if (materialReceiptSearch.getMrnNumber() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("mrnnumber in (:mrnNumber)");
			paramValues.put("mrnNumber", materialReceiptSearch.getMrnNumber());
		}

		if (materialReceiptSearch.getReceiptDate() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("receiptdate = :receiptDate");
			paramValues.put("receiptDate", materialReceiptSearch.getReceiptDate());
		}

		if (materialReceiptSearch.getReceiptType() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("receipttype in(:receiptType)");
			paramValues.put("receiptType", materialReceiptSearch.getReceiptType());
		}

		if (materialReceiptSearch.getReceivingStore() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("receivingstore = :receivingStore");
			paramValues.put("receivingStore", materialReceiptSearch.getReceivingStore());
		}

		if (materialReceiptSearch.getSupplierCode() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("suppliercode = :supplierCode");
			paramValues.put("supplierCode", materialReceiptSearch.getSupplierCode());
		}

		if (materialReceiptSearch.getIssueingStore() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("issueingstore = :issueingStore");
			paramValues.put("issueingStore", materialReceiptSearch.getIssueingStore());
		}

		if (materialReceiptSearch.getReceiptPurpose() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("receiptpurpose = :receiptPurpose");
			paramValues.put("receiptPurpose", materialReceiptSearch.getReceiptPurpose());
		}

		if (materialReceiptSearch.getSupplierBillPaid() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("supplierbillpaid = :supplierBillPaid");
			paramValues.put("supplierBillPaid", materialReceiptSearch.getSupplierBillPaid());
		}

		if (materialReceiptSearch.getFinancialYear() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("financialyear = :financialYear");
			paramValues.put("financialYear", materialReceiptSearch.getFinancialYear());
		}

		if (materialReceiptSearch.getMrnStatus() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("mrnStatus = :mrnStatus");
			paramValues.put("mrnStatus", materialReceiptSearch.getMrnStatus());
		}

		if (materialReceiptSearch.getIssueNumber() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("issueNumber = :issueNumber");
			paramValues.put("issueNumber", materialReceiptSearch.getIssueNumber());
		}

		if (materialReceiptSearch.getTenantId() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append("tenantId = :tenantId");
			paramValues.put("tenantId", materialReceiptSearch.getTenantId());
		}

		if (materialReceiptSearch.getAsOnDate() != null) {
			if (params.length() > 0)
				params.append(" and ");
			params.append(" TO_DATE(TO_CHAR(TO_TIMESTAMP(receiptDate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <=DATE(:asOnDate)");
			paramValues.put("asOnDate", materialReceiptSearch.getAsOnDate());
		}

		Pagination<MaterialReceipt> page = new Pagination<>();
		if (materialReceiptSearch.getPageSize() != null)
			page.setPageSize(materialReceiptSearch.getPageSize());
		if (materialReceiptSearch.getOffset() != null)
			page.setOffset(materialReceiptSearch.getOffset());
		if (params.length() > 0)
			searchQuery = searchQuery.replace(":condition", " where " + params.toString());
		else
			searchQuery = searchQuery.replace(":condition", "");

		searchQuery = searchQuery.replace(":orderby", orderBy);
		page = (Pagination<MaterialReceipt>) getPagination(searchQuery, page, paramValues);

		searchQuery = searchQuery + " :pagination";
		searchQuery = searchQuery.replace(":pagination",
				"limit " + page.getPageSize() + " offset " + page.getOffset() * page.getPageSize());
		BeanPropertyRowMapper row = new BeanPropertyRowMapper(MaterialReceiptEntity.class);

		List<MaterialReceipt> materialReceipts = new ArrayList<>();

		List<MaterialReceiptEntity> materialReceiptEntities = namedParameterJdbcTemplate.query(searchQuery.toString(),
				paramValues, row);

		for (MaterialReceiptEntity materialReceiptEntity : materialReceiptEntities) {

			materialReceipts.add(materialReceiptEntity.toDomain());
		}

		page.setTotalResults(materialReceipts.size());

		page.setPagedData(materialReceipts);

		return page;
	}
	public JSONArray searchStock(MaterialReceiptSearch materialReceiptSearch) {
		String searchQuery = "SELECT ( unitrate ) unitrate, SUM(finalResult.section1::numeric(18,2)) AS below90Days, SUM(finalResult.section2::numeric(18,2)) AS between90to180Days,SUM(finalResult.section3::numeric(18,2))\r\n" + 
				"AS above180Days FROM \r\n" + 
				"( SELECT tab.unitrate,SUM(tab.balance) section1 ,0 section2,0 section3 from\r\n" + 
				"(select materialreceipt.tenantid as tenantId, materialreceipt.id as receiptId,rctdtl.id as receiptDetailId,rctdtl.mrnnumber as mrnNumber,receivingstore as issueStoreCode, material as materialCode, uomno as uomCode,materialreceipt.receiptdate as receiptDate, (COALESCE(addinfo.quantity,acceptedqty) - COALESCE (case when addinfo.id is not null then (select sum(issuereceipt.quantity) from materialissuedfromreceipt\r\n" + 
				"issuereceipt where addinfo.id=issuereceipt.receiptdetailaddnlinfoid and issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true)\r\n" + 
				"else (select sum(issuereceipt.quantity) from materialissuedfromreceipt issuereceipt where issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true) end,0)) as balance,unitrate\r\n" + 
				"from materialreceipt left outer join materialreceiptdetail rctdtl on materialreceipt.mrnnumber = rctdtl.mrnnumber left outer join\r\n" + 
				"materialreceiptdetailaddnlinfo  addinfo on rctdtl.id= addinfo.receiptdetailid\r\n" + 
				"where  (isscrapitem IS NULL or isscrapitem=false) and (rctdtl.deleted=false or rctdtl.deleted is null ) and receivingstore=?  and materialreceipt.tenantid= ?\r\n" + 
				"and material in (?) and mrnstatus in ('Approved') ) as tab where\r\n" + 
				"  TO_DATE(TO_CHAR(TO_TIMESTAMP(tab.receiptdate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <=DATE(?) and ( DATE_PART( 'day', now() :: timestamp - TO_DATE( TO_CHAR(TO_TIMESTAMP(tab.receiptdate / 1000 ), 'YYYY-MM-DD' ),\r\n" + 
				" 'YYYY-MM-DD'):: timestamp ))<= 90 and ( DATE_PART('day', now() :: timestamp - TO_DATE( TO_CHAR( TO_TIMESTAMP( tab.receiptdate /\r\n" + 
				" 1000),'YYYY-MM-DD' ), 'YYYY-MM-DD' ):: timestamp ) )>= 0 GROUP BY tab.unitrate \r\n" + 
				" UNION ALL \r\n" + 
				" SELECT tab.unitrate, 0 section1,\r\n" + 
				" SUM(tab.balance) section2 , 0 section3 from (select materialreceipt.tenantid as tenantId, materialreceipt.id as receiptId,rctdtl.id as receiptDetailId,rctdtl.mrnnumber as mrnNumber,receivingstore as issueStoreCode, material as materialCode, uomno as uomCode,materialreceipt.receiptdate as receiptDate, (COALESCE(addinfo.quantity,acceptedqty) - COALESCE (case when addinfo.id is not null then (select sum(issuereceipt.quantity) from materialissuedfromreceipt\r\n" + 
				"issuereceipt where addinfo.id=issuereceipt.receiptdetailaddnlinfoid and issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true)\r\n" + 
				"else (select sum(issuereceipt.quantity) from materialissuedfromreceipt issuereceipt where issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true) end,0)) as balance,unitrate\r\n" + 
				"from materialreceipt left outer join materialreceiptdetail rctdtl on materialreceipt.mrnnumber = rctdtl.mrnnumber left outer join\r\n" + 
				"materialreceiptdetailaddnlinfo  addinfo on rctdtl.id= addinfo.receiptdetailid\r\n" + 
				"where  (isscrapitem IS NULL or isscrapitem=false) and (rctdtl.deleted=false or rctdtl.deleted is null ) and receivingstore=?  and materialreceipt.tenantid= ?\r\n" + 
				"and material in (?) and mrnstatus in ('Approved') ) as tab where \r\n" + 
				" TO_DATE(TO_CHAR(TO_TIMESTAMP(tab.receiptdate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <=DATE(?) and\r\n" + 
				" ( DATE_PART( 'day', now() :: timestamp - TO_DATE( TO_CHAR(  TO_TIMESTAMP(  tab.receiptdate / 1000  ),  'YYYY-MM-DD' ),\r\n" + 
				" 'YYYY-MM-DD' ):: timestamp ) )>= 90 and ( DATE_PART( 'day', now() :: timestamp - TO_DATE( TO_CHAR(  TO_TIMESTAMP( \r\n" + 
				" tab.receiptdate / 1000  ),  'YYYY-MM-DD' ), 'YYYY-MM-DD' ):: timestamp ) )<= 180 GROUP BY tab.unitrate\r\n" + 
				" UNION ALL \r\n" + 
				" SELECT tab.unitrate, 0 section2, 0 section1, SUM(tab.balance) section3 from \r\n" + 
				" (select materialreceipt.tenantid as tenantId, materialreceipt.id as receiptId,rctdtl.id as receiptDetailId,rctdtl.mrnnumber as mrnNumber,receivingstore as issueStoreCode, material as materialCode, uomno as uomCode,materialreceipt.receiptdate as receiptDate, (COALESCE(addinfo.quantity,acceptedqty) - COALESCE (case when addinfo.id is not null then (select sum(issuereceipt.quantity) from materialissuedfromreceipt\r\n" + 
				"issuereceipt where addinfo.id=issuereceipt.receiptdetailaddnlinfoid and issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true)\r\n" + 
				"else (select sum(issuereceipt.quantity) from materialissuedfromreceipt issuereceipt where issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true) end,0)) as balance,unitrate\r\n" + 
				"from materialreceipt left outer join materialreceiptdetail rctdtl on materialreceipt.mrnnumber = rctdtl.mrnnumber left outer join\r\n" + 
				"materialreceiptdetailaddnlinfo  addinfo on rctdtl.id= addinfo.receiptdetailid\r\n" + 
				"where  (isscrapitem IS NULL or isscrapitem=false) and (rctdtl.deleted=false or rctdtl.deleted is null ) and receivingstore=?  and materialreceipt.tenantid= ?\r\n" + 
				"and material in (?) and mrnstatus in ('Approved') ) as tab where TO_DATE(TO_CHAR(TO_TIMESTAMP(tab.receiptdate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <=DATE(?) and ( DATE_PART( 'day', now() :: timestamp - TO_DATE( TO_CHAR(  TO_TIMESTAMP(\r\n" + 
				" tab.receiptdate / 1000  ),  'YYYY-MM-DD' ), 'YYYY-MM-DD' ):: timestamp ) )>= 180 GROUP BY tab.unitrate )\r\n" + 
				" finalResult GROUP BY finalResult.unitrate ORDER BY finalResult.unitrate ";
		StringBuffer params = new StringBuffer();
		Map<String, Object> paramValues = new HashMap<>();
		List<JSONArray> sep = new ArrayList<>();
		if (materialReceiptSearch.getSortBy() != null && !materialReceiptSearch.getSortBy().isEmpty()) {
			validateSortByOrder(materialReceiptSearch.getSortBy());
			validateEntityFieldName(materialReceiptSearch.getSortBy(), MaterialReceiptSearch.class);
		}

	
		StockRowMapper stockowMapper=new StockRowMapper();
		sep = jdbcTemplate.query(searchQuery,
				new Object[] { materialReceiptSearch.getReceivingStore(),materialReceiptSearch.getTenantId(),materialReceiptSearch.getMaterials().get(0).toString(),materialReceiptSearch.getAsOnDate(),
						 materialReceiptSearch.getReceivingStore(),materialReceiptSearch.getTenantId(),materialReceiptSearch.getMaterials().get(0).toString(),materialReceiptSearch.getAsOnDate(),
						 materialReceiptSearch.getReceivingStore(),materialReceiptSearch.getTenantId(),materialReceiptSearch.getMaterials().get(0).toString(),materialReceiptSearch.getAsOnDate()
							 },stockowMapper);
		return sep.isEmpty() ? new JSONArray() : sep.get(0);
		
	}
	public Pagination<MaterialBalanceRate> searchBalanceRate(MaterialReceiptSearch materialReceiptSearch) {
		String searchQuery = "select * from (select materialreceipt.tenantid as tenantId, materialreceipt.id as receiptId,rctdtl.id as receiptDetailId,rctdtl.mrnnumber as mrnNumber,receivingstore as issueStoreCode, material as materialCode, uomno as uomCode,materialreceipt.receiptdate as receiptDate,\n"
				+ "(COALESCE(addinfo.quantity,acceptedqty) - COALESCE (case when addinfo.id is not null then (select sum(issuereceipt.quantity) from materialissuedfromreceipt\n"
				+ "issuereceipt where addinfo.id=issuereceipt.receiptdetailaddnlinfoid and issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true)\n"
				+ "else (select sum(issuereceipt.quantity) from materialissuedfromreceipt issuereceipt where issuereceipt.receiptdetailid=rctdtl.id and issuereceipt.status=true) end,0)) as balance , unitRate \n"
				+ "from materialreceipt left outer join materialreceiptdetail rctdtl on materialreceipt.mrnnumber = rctdtl.mrnnumber left outer join\n"
				+ "materialreceiptdetailaddnlinfo  addinfo on rctdtl.id= addinfo.receiptdetailid\n"
				+ "where  (isscrapitem IS NULL or isscrapitem=false) and (rctdtl.deleted=false or rctdtl.deleted is null ) and receivingstore= :store  and materialreceipt.tenantid= :tenantId\n"
				+ ":materialcondition and mrnstatus in ('Approved') and TO_DATE(TO_CHAR(TO_TIMESTAMP(receiptdate / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD') <= TO_DATE(TO_CHAR(TO_TIMESTAMP(:date / 1000), 'YYYY-MM-DD'),'YYYY-MM-DD')  order by addinfo.expirydate,addinfo.receiveddate,receiptdate)as fifo where balance >0";

		Map<String, Object> paramValues = new HashMap<>();

		if (materialReceiptSearch.getMaterials() != null && !materialReceiptSearch.getMaterials().isEmpty()) {
			searchQuery = searchQuery.replace(":materialcondition", "and material in (:material)");
			paramValues.put("material", materialReceiptSearch.getMaterials());
		} else {
			searchQuery = searchQuery.replace(":materialcondition", "");
		}

		if (materialReceiptSearch.getIssueingStore() != null) {
			paramValues.put("store", materialReceiptSearch.getIssueingStore());
		}

		if (materialReceiptSearch.getTenantId() != null) {
			paramValues.put("tenantId", materialReceiptSearch.getTenantId());
		}
		paramValues.put("date", new Date().getTime());

		Pagination<MaterialBalanceRate> page = new Pagination<>();
		page = (Pagination<MaterialBalanceRate>) getPagination(searchQuery, page, paramValues);

		BeanPropertyRowMapper row = new BeanPropertyRowMapper(MaterialBalanceRateEntity.class);

		List<MaterialBalanceRate> materialBalanceRate = new ArrayList<>();

		List<MaterialBalanceRateEntity> materialBalanceRateEntity = namedParameterJdbcTemplate
				.query(searchQuery.toString(), paramValues, row);

		for (MaterialBalanceRateEntity materialReceiptEntity : materialBalanceRateEntity) {
			materialBalanceRate.add(materialReceiptEntity.toDomain());
		}

		page.setTotalResults(materialBalanceRate.size());
		page.setPagedData(materialBalanceRate);
		return page;
	}

	public JSONArray getInventoryReport(MaterialReceiptSearch materialReceiptSearch) {
		String searchQuery = "select tenantid,openmrn, openstore, opendepart, openmat, openuom, openqty, openrate, opentotalvalue, TO_CHAR(TO_TIMESTAMP(recptdate/1000), 'DD-MM-YYYY') recptdate, recptreceiptno, recptdepartment, recptqtypurchased, recptuom,\r\n"
				+ " recptunitrate, recpttotalvalue, issuenum, issuestore, issuedepart, TO_CHAR(TO_TIMESTAMP(issuedate/1000), 'DD-MM-YYYY') issuedate, issueqty, issuerate, issueuom, issuetotalvalue, balanceqty,  \r\n"
				+ " balanceuom, balancetotalvalue from ((select tenantid,opnBalance.mrnnumber openmrn, opnBalance.storecode openstore, opnBalance.department opendepart, opnBalance.material openmat,\r\n"
				+ " opnBalance.uomno openuom,opnBalance.qtyopeningbalance openqty, opnBalance.unitrateopeningbalance openrate, opnBalance.valueopeningbalance opentotalvalue,\r\n"
				+ " null as recptdate, '' as recptreceiptno, '' as recptdepartment, 0 as recptqtypurchased, '' as recptuom, 0 as recptunitrate, 0 as recpttotalvalue,\r\n"
				+ " issues.issuenumber issuenum,issues.tostore issuestore, issues.department issuedepart,issues.issuedate issuedate, issues.quantity issueqty, opnBalance.unitrateopeningbalance issuerate,\r\n"
				+ " issues.uom issueuom, opnBalance.unitrateopeningbalance*issues.quantity issuetotalvalue, (opnBalance.qtyopeningbalance::numeric(18,2) - SUM(COALESCE(issues.quantity,0)::numeric(18,2)) OVER (PARTITION BY opnBalance.mrnnumber ORDER BY issues.id, issues.issuedate)) balanceqty, opnBalance.uomno balanceuom, \r\n"
				+ " ((opnBalance.qtyopeningbalance::numeric(18,2) - SUM(COALESCE(issues.quantity,0)::numeric(18,2)) OVER (PARTITION BY opnBalance.mrnnumber ORDER BY issues.id, issues.issuedate)) * opnBalance.unitrateopeningbalance::numeric(18,2))  balancetotalvalue\r\n"
				+ "from (select materialreceipt.tenantid,rctdtl.id,materialreceipt.receiptdate, materialreceipt.receivingstore storecode, st.department,rctdtl.material,rctdtl.uomno, materialreceipt.mrnnumber,COALESCE(addinfo.quantity,acceptedqty)::numeric(18,2) qtyopeningbalance,unitRate::numeric(18,2) unitrateopeningbalance, (COALESCE(addinfo.quantity,acceptedqty)::numeric(18,2)*unitRate::numeric(18,2)) valueopeningbalance\r\n"
				+ "from materialreceipt materialreceipt inner join materialreceiptdetail rctdtl on materialreceipt.mrnnumber = rctdtl.mrnnumber inner join\r\n"
				+ "materialreceiptdetailaddnlinfo  addinfo on rctdtl.id= addinfo.receiptdetailid left join store st on materialreceipt.receivingstore=st.code\r\n"
				+ "where materialreceipt.receipttype= 'OPENING BALANCE' and materialreceipt.mrnstatus='Approved') as opnBalance left join (select iss.id, iss.issuenumber,iss.tostore,st.department,issuedetails.materialcode, issuedetails.uom, iss.issuedate,receiptdetailid, issuedetails.id issuedetailid,issuereceipt.status,issuereceipt.quantity::numeric(18,2) from materialissue iss inner join materialissuedetail issuedetails on iss.issuenumber=issuedetails.materialissuenumber inner join materialissuedfromreceipt issuereceipt \r\n"
				+ "on issuedetails.id=issuereceipt.issuedetailid left join store st on iss.tostore=st.code where iss.materialissuestatus='Approved') issues on issues.status=true and issues.receiptdetailid=opnBalance.id\r\n"
				+ "order by opnBalance.mrnnumber, issues.id, issues.issuedate asc)\r\n" + "union all\r\n"
				+ "(select tenantid,'' openmrn, opnBalance.storecode openstore, opnBalance.department opendepart, opnBalance.material openmat,\r\n"
				+ " '' openuom,0 openqty, 0 openrate, 0 opentotalvalue,\r\n"
				+ " opnBalance.receiptdate recptdate, opnBalance.mrnnumber recptreceiptno, opnBalance.department recptdepartment, opnBalance.qtyopeningbalance recptqtypurchased,\r\n"
				+ " opnBalance.uomno recptuom, opnBalance.unitrateopeningbalance recptunitrate, (opnBalance.qtyopeningbalance::numeric(18,2)*opnBalance.unitrateopeningbalance::numeric(18,2)) recpttotalvalue,\r\n"
				+ " issues.issuenumber issuenum,issues.tostore issuestore, issues.department issuedepart,issues.issuedate issuedate, \r\n"
				+ " issues.quantity issueqty, opnBalance.unitrateopeningbalance issuerate, issues.uom issueuom, opnBalance.unitrateopeningbalance*issues.quantity issuetotalvalue,\r\n"
				+ " (opnBalance.qtyopeningbalance::numeric(18,2) - SUM(COALESCE(issues.quantity,0)::numeric(18,2)) OVER (PARTITION BY opnBalance.mrnnumber ORDER BY issues.id, issues.issuedate)) balanceqty, opnBalance.uomno balanceuom, \r\n"
				+ " ((opnBalance.qtyopeningbalance::numeric(18,2) - SUM(COALESCE(issues.quantity,0)::numeric(18,2)) OVER (PARTITION BY opnBalance.mrnnumber ORDER BY issues.id, issues.issuedate)) * opnBalance.unitrateopeningbalance::numeric(18,2))  balancetotalvalue\r\n"
				+ "from (select materialreceipt.tenantid,rctdtl.id,materialreceipt.receiptdate, materialreceipt.receivingstore storecode, st.department,rctdtl.material,rctdtl.uomno, materialreceipt.mrnnumber,COALESCE(addinfo.quantity,acceptedqty)::numeric(18,2) qtyopeningbalance,unitRate::numeric(18,2) unitrateopeningbalance, (COALESCE(addinfo.quantity,acceptedqty)::numeric(18,2)*unitRate::numeric(18,2)) valueopeningbalance\r\n"
				+ "from materialreceipt materialreceipt inner join materialreceiptdetail rctdtl on materialreceipt.mrnnumber = rctdtl.mrnnumber inner join\r\n"
				+ "materialreceiptdetailaddnlinfo  addinfo on rctdtl.id= addinfo.receiptdetailid left join store st on materialreceipt.receivingstore=st.code\r\n"
				+ "where materialreceipt.receipttype <> 'OPENING BALANCE' and materialreceipt.mrnstatus='Approved') as opnBalance left join (select iss.id, iss.issuenumber,iss.tostore,st.department,issuedetails.materialcode, issuedetails.uom, iss.issuedate,receiptdetailid, issuedetails.id issuedetailid,issuereceipt.status,issuereceipt.quantity::numeric(18,2) from materialissue iss inner join materialissuedetail issuedetails on iss.issuenumber=issuedetails.materialissuenumber inner join materialissuedfromreceipt issuereceipt \r\n"
				+ "on issuedetails.id=issuereceipt.issuedetailid left join store st on iss.tostore=st.code where iss.materialissuestatus='Approved') issues on issues.status=true and issues.receiptdetailid=opnBalance.id order by opnBalance.mrnnumber, issues.id, issues.issuedate asc)) as MainTable\r\n"
				+ "where MainTable.openstore=:storecode and MainTable.openmat in (:material) and MainTable.tenantid=:tenantid";

		Map<String, Object> paramValues = new HashMap<>();
		if (materialReceiptSearch.getReceivingStore() != null) {
			paramValues.put("storecode", materialReceiptSearch.getReceivingStore());
		}

		if (materialReceiptSearch.getTenantId() != null) {
			paramValues.put("tenantid", materialReceiptSearch.getTenantId());
		}
		if (materialReceiptSearch.getMaterials() != null && !materialReceiptSearch.getMaterials().isEmpty()) {
			paramValues.put("material", materialReceiptSearch.getMaterials());
		}

		Map<String, Department> departmentMap = getDepartment(materialReceiptSearch.getTenantId());
		InventoryRowMapper inventoryRowMapper = new InventoryRowMapper(departmentMap);
		List<JSONArray> inventoryResponse = namedParameterJdbcTemplate.query(searchQuery, paramValues,
				inventoryRowMapper);

		return inventoryResponse.isEmpty() ? new JSONArray() : inventoryResponse.get(0);
	}

	private Map<String, Department> getDepartment(String tenantId) {
		net.minidev.json.JSONArray responseJSONArray = mdmsRepository.getByCriteria(tenantId, "store-asset",
				"Department", null, null, new RequestInfo());
		Map<String, Department> departmentMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		if (responseJSONArray != null && !responseJSONArray.isEmpty()) {
			for (int i = 0; i < responseJSONArray.size(); i++) {
				Department department = mapper.convertValue(responseJSONArray.get(i), Department.class);
				departmentMap.put(department.getCode(), department);
			}
		}
		return departmentMap;
	}

}
