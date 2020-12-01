package org.egov.pm.repository.querybuilder;

import java.util.List;

import org.egov.pm.config.ApplicationProperties;
import org.egov.pm.model.RequestData;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {

	@Autowired
	private ApplicationProperties applicationProperties;

	public static final String SELECT_PETS_QUERY = "select ED.* from egpm_noc_application_detail ED inner join egpm_noc_application EA on ED.application_uuid=EA.application_uuid WHERE EA.application_type=? AND EA.application_status=? AND ED.is_active=TRUE AND EA.is_active=TRUE";
	public static final String SELECT_APPLICATION_QUERY = "select * from egpm_noc_application_detail ED inner join egpm_noc_application EA on ED.application_uuid=EA.application_uuid WHERE  ED.application_uuid=? and ED.is_active=true and EA.is_active=TRUE and EA.application_status=?";

	public static final String SELECT_REMARKS_QUERY = "select * from egpm_noc_application_remark WHERE application_uuid=? AND is_active=true";
	public static final String SELECT_APPID_QUERY = "select ED.application_uuid from egpm_noc_application ED WHERE ED.noc_number=?";

	public static final String ALL_REMARKS_QUERY = "select application_uuid applicationUuid,remark,created_by,document_detail documentDetail,application_status applicationStatus, created_time createdTime from egpm_noc_application_remark WHERE application_uuid=? order by created_time DESC";
	public static final String SELECT_VIEW_QUERY = "select EA.application_uuid applicationUuid, EA.noc_number nocNumber,EA.application_type applicationType,EA.applicant_name applicantName,ED.application_detail applicationDetail,EA.house_number houseNumber,EA.sector sector,EA.applied_date appliedDate,EA.application_status applicationStatus,EA.amount amount,EA.gst_amount gstAmount,EA.performance_bank_guarantee performanceBankGuaranteeCharges,EA.total_amount totalamount,EA.created_by createdby from egpm_noc_application_detail ED inner join egpm_noc_application EA on ED.application_uuid=EA.application_uuid WHERE EA.noc_number=? AND EA.is_active=TRUE AND ED.is_active=TRUE";
		
	public static final String SELECT_FALLBACK_QUERY = "select EA.application_uuid as applicationUuid, EA.noc_number as nocNumber,EA.application_type as applicationType,\r\n"
			+ "EA.applicant_name as applicantName,ED.application_detail as applicationDetail,EA.house_number as houseNumber,\r\n"
			+ "EA.sector as sector,EA.applied_date as appliedDate,EA.application_status as applicationStatus,\r\n"
			+ "EA.amount as amount,EA.gst_amount as gstAmount,EA.performance_bank_guarantee as performanceBankGuaranteeCharges,\r\n"
			+ "EA.total_amount as totalamount,ER.remark_id as remarkid\r\n"
			+ "from egpm_noc_application EA inner join egpm_noc_application_detail ED on EA.application_uuid=ED.application_uuid inner join egpm_noc_application_remark ER on EA.application_uuid=ER.application_uuid  where EA.noc_number=?\r\n"
			+ "and EA.is_active=true and ED.is_active=true and ER.is_active=true";

	public static final String SELECT_NOC_BY_NOCNUMBER = "select EA.application_uuid applicationUuid, EA.noc_number nocNumber,EA.application_type applicationType,EA.applicant_name applicantName,EA.house_number houseNumber,EA.sector sector,EA.applied_date appliedDate,EA.application_status applicationStatus,EA.tenant_id tenantId,EA.amount amount,EA.gst_amount gstAmount,EA.performance_bank_guarantee performanceBankGuaranteeCharges,EA.total_amount totalamount,EA.created_by createdBy from egpm_noc_application EA WHERE EA.noc_number=? AND EA.is_active=TRUE";

	public static final String SELECT_APPLICATION_ID_QUERY = "select * from egpm_noc_application where noc_number=? AND is_active=TRUE";
	public static final String SELECT_APPLICATION_DETAIL_QUERY = "select application_detail_uuid, application_uuid, application_detail, is_active, created_by, created_time, last_modified_by, last_modified_time, tenant_id from egpm_noc_application_detail where application_uuid=(select application_uuid from egpm_noc_application where noc_number=? and is_active='true') and is_active='true'";
	public static final String SELECT_USER_BY_APPLICATION_ID_QUERY = "select created_by,total_amount from egpm_noc_application where noc_number=? AND is_active=TRUE";

	public static final String SELECT_TOTAL_AMOUNT_BY_APPLICATION_ID_QUERY = "select total_amount from egpm_noc_application where noc_number=? AND is_active=TRUE";

	// add
	public static final String INSERT_NOC_DETAILS_QUERY = "INSERT into public.egpm_noc_application_detail "
			+ "(application_detail_uuid, application_uuid, application_detail, is_active, created_by, created_time, last_modified_by, last_modified_time)"
			+ "values(?,?,to_json(?::json),?,?,?,?,?)";

	public static final String GET_NOC_REMARKS_QUERY = "select ROW_NUMBER() OVER (ORDER BY ED.created_time) AS id,EA.applied_date applied_date, EA.noc_number applicationId, ED.application_status applicationStatus, ED.remark, ED.remark_by remarkBy\n"
			+ "	from egpm_noc_application_remark ED INNER JOIN egpm_noc_application EA\n"
			+ "	ON ED.application_uuid=EA.application_uuid where EA.tenant_id=? and EA.noc_number=? order by ED.created_time asc";

	public static final String SELECT_PRICE_BOOK_QUERY = "SELECT price_book_id FROM egpm_noc_price_book WHERE effective_from_date::DATE=to_date(?, 'YYYY-MM-DD') and category_id=? and sub_category_id=? and application_type=?";
	public static final String SELECT_PRICE_BOOK_INBETWEEN_QUERY = " SELECT price_book_id,min_sqft,max_sqft, calculation_sequence, calculation_type FROM egpm_noc_price_book WHERE category_id=? and sub_category_id=? AND to_date(?, 'YYYY-MM-DD') between effective_from_date::date and coalesce(effective_to_date::date,current_timestamp + interval '100 year') and application_type=? ";
	public static final String SELECT_PRICE_BOOK_GETALL_QUERY = "SELECT price_book_id,category_id,sub_category_id,tenant_id,calculation_sequence,fixed_price,perday_price,perweek_price,permonth_price,annual_price,effective_from_date,effective_to_date FROM egpm_noc_price_book WHERE coalesce(effective_to_date::date,current_timestamp + interval '100 year')>current_timestamp AND tenant_id=? and application_type=? order by created_time desc";
	public static final String SELECT_PRICE_BOOK_ID_QUERY = " SELECT price_book_id,category_id,sub_category_id,tenant_id,calculation_sequence,calculation_type,fixed_price,perday_price,perweek_price,permonth_price,annual_price,effective_from_date,effective_to_date FROM egpm_noc_price_book WHERE PRICE_BOOK_ID=? AND tenant_id=? and application_type=?";

	public static final String INSERT_NOC_AVERAGE_TIME_QUERY = "insert into egpm_noc_report_avgproctime_aggregate(tenant_id,application_type,total_average,total_avg_pending_10days_to_30days,total_avg_pending_greater_than30days,report_generation_datetime,dimension_type) values(?,?,?,?,?,?,?);";
	public static final String GET_NOC_AVERAGE_TIME_QUERY = "SELECT\r\n" + "   CASE\r\n" + "      WHEN\r\n"
			+ "         MainTable2.tenant_id is NULL \r\n" + "      THEN\r\n" + "         MainTable1.tenant_id \r\n"
			+ "      ELSE\r\n" + "         MainTable2.tenant_id \r\n" + "   END\r\n" + "   AS tenantId, \r\n"
			+ "   CASE\r\n" + "      WHEN\r\n" + "         MainTable2.application_type is NULL \r\n" + "      THEN\r\n"
			+ "         MainTable1.application_type \r\n" + "      ELSE\r\n"
			+ "         MainTable2.application_type \r\n" + "   END\r\n"
			+ "   application_type, COALESCE(MainTable2.processingTime, 0) AS totalAverage, COALESCE(MainTable1.pendingBetween10To30Days, 0) AS totalAveragePending10DayasTo30Days, COALESCE(MainTable1.pendingMorethan30Days, 0) AS totalAveragePendingGreaterThan30Days \r\n"
			+ "FROM\r\n" + "   (\r\n" + "      SELECT\r\n" + "         MIN(MainTable.tenant_id) AS tenant_id,\r\n"
			+ "         MainTable.application_type,\r\n" + "         COUNT((MainTable.dateDiffer > 10 \r\n"
			+ "         and MainTable.dateDiffer < 30) \r\n" + "         or null) AS pendingBetween10To30Days,\r\n"
			+ "         COUNT((MainTable.dateDiffer > 30) \r\n" + "         or null) AS pendingMorethan30Days \r\n"
			+ "      FROM\r\n" + "         (\r\n" + "            SELECT\r\n"
			+ "               MIN(subTable1.tenant_id) AS tenant_id,\r\n"
			+ "               subTable1.application_type AS application_type,\r\n"
			+ "               SUM((DATE_PART('day', TO_TIMESTAMP(subTable2.dt / 1000)::timestamp - TO_TIMESTAMP(subTable1.dt / 1000)::timestamp))) dateDiffer \r\n"
			+ "            FROM\r\n" + "               (\r\n" + "                  SELECT DISTINCT\r\n"
			+ "                     min(Table1.tenant_id) tenant_id,\r\n"
			+ "                     min(Table1.application_uuid) AS application_uuid,\r\n"
			+ "                     min(Table1.application_type) AS application_type,\r\n"
			+ "                     min(Table1.dt) dt,\r\n"
			+ "                     min(Table1.remark_by) AS remark_by,\r\n"
			+ "                     min(Table1.application_status) AS application_status,\r\n"
			+ "                     dense_rank() OVER (partition BY Table1.application_uuid \r\n"
			+ "                  ORDER BY\r\n" + "                     min(Table1.dt)) rownum \r\n"
			+ "                  FROM\r\n" + "                     (\r\n" + "                        SELECT\r\n"
			+ "                           a.tenant_id,\r\n"
			+ "                           b.application_uuid AS application_uuid,\r\n"
			+ "                           a.application_type AS application_type,\r\n"
			+ "                           b.created_time dt,\r\n"
			+ "                           b.remark_by AS remark_by,\r\n"
			+ "                           b.application_status AS application_status \r\n"
			+ "                        FROM\r\n" + "                           egpm_noc_application a \r\n"
			+ "                           INNER JOIN\r\n"
			+ "                              egpm_noc_application_remark b \r\n"
			+ "                              ON a.application_uuid = b.application_uuid \r\n"
			+ "                        WHERE\r\n"
			+ "                           a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                           AND a.application_status NOT IN \r\n" + "                           (\r\n"
			+ "                              'APPROVED',\r\n" + "                              'REJECTED'\r\n"
			+ "                           )\r\n" + "                        UNION\r\n"
			+ "                        SELECT DISTINCT\r\n" + "                           a.tenant_id,\r\n"
			+ "                           a.application_uuid,\r\n"
			+ "                           a.application_type,\r\n"
			+ "                           ROUND((EXTRACT(epoch \r\n" + "                        FROM\r\n"
			+ "                           now()) * 1000)) dt,\r\n"
			+ "                           'SYSTEM' remark_by,\r\n"
			+ "                           'PENIDNG' application_status \r\n" + "                        FROM\r\n"
			+ "                           egpm_noc_application a \r\n" + "                           INNER JOIN\r\n"
			+ "                              egpm_noc_application_remark b \r\n"
			+ "                              ON a.application_uuid = b.application_uuid \r\n"
			+ "                              AND a.application_status = b.application_status \r\n"
			+ "                        WHERE\r\n" + "                           a.application_status NOT IN \r\n"
			+ "                           (\r\n" + "                              'APPROVED',\r\n"
			+ "                              'REJECTED'\r\n" + "                           )\r\n"
			+ "                           AND a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                     )\r\n" + "                     Table1 \r\n" + "                  GROUP BY\r\n"
			+ "                     Table1.dt,\r\n" + "                     Table1.application_uuid,\r\n"
			+ "                     application_status \r\n" + "               )\r\n" + "               subTable1 \r\n"
			+ "               INNER JOIN\r\n" + "                  (\r\n" + "                     SELECT DISTINCT\r\n"
			+ "                        min(Table1.tenant_id) tenant_id,\r\n"
			+ "                        min(Table1.application_uuid) AS application_uuid,\r\n"
			+ "                        min(Table1.application_type) AS application_type,\r\n"
			+ "                        min(Table1.dt) dt,\r\n"
			+ "                        min(Table1.remark_by) AS remark_by,\r\n"
			+ "                        min(Table1.application_status) AS application_status,\r\n"
			+ "                        dense_rank() OVER (partition BY Table1.application_uuid \r\n"
			+ "                     ORDER BY\r\n" + "                        min(Table1.dt)) rownum \r\n"
			+ "                     FROM\r\n" + "                        (\r\n"
			+ "                           SELECT\r\n" + "                              a.tenant_id,\r\n"
			+ "                              b.application_uuid AS application_uuid,\r\n"
			+ "                              a.application_type AS application_type,\r\n"
			+ "                              b.created_time dt,\r\n"
			+ "                              b.remark_by AS remark_by,\r\n"
			+ "                              b.application_status AS application_status \r\n"
			+ "                           FROM\r\n" + "                              egpm_noc_application a \r\n"
			+ "                              INNER JOIN\r\n"
			+ "                                 egpm_noc_application_remark b \r\n"
			+ "                                 ON a.application_uuid = b.application_uuid \r\n"
			+ "                           WHERE\r\n"
			+ "                              a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                              AND a.application_status NOT IN \r\n"
			+ "                              (\r\n" + "                                 'APPROVED',\r\n"
			+ "                                 'REJECTED'\r\n" + "                              )\r\n"
			+ "                           UNION\r\n" + "                           SELECT DISTINCT\r\n"
			+ "                              a.tenant_id,\r\n" + "                              a.application_uuid,\r\n"
			+ "                              a.application_type,\r\n"
			+ "                              ROUND((EXTRACT(epoch \r\n" + "                           FROM\r\n"
			+ "                              now()) * 1000)) dt,\r\n"
			+ "                              'SYSTEM' remark_by,\r\n"
			+ "                              'PENIDNG' application_status \r\n" + "                           FROM\r\n"
			+ "                              egpm_noc_application a \r\n"
			+ "                              INNER JOIN\r\n"
			+ "                                 egpm_noc_application_remark b \r\n"
			+ "                                 ON a.application_uuid = b.application_uuid \r\n"
			+ "                                 AND a.application_status = b.application_status \r\n"
			+ "                           WHERE\r\n" + "                              a.application_status NOT IN \r\n"
			+ "                              (\r\n" + "                                 'APPROVED',\r\n"
			+ "                                 'REJECTED'\r\n" + "                              )\r\n"
			+ "                              AND a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                        )\r\n" + "                        Table1 \r\n"
			+ "                     GROUP BY\r\n" + "                        Table1.dt,\r\n"
			+ "                        Table1.application_uuid,\r\n" + "                        application_status \r\n"
			+ "                  )\r\n" + "                  subTable2 \r\n"
			+ "                  ON subTable1.rownum + 1 = subTable2.rownum \r\n"
			+ "                  and subTable1.application_uuid = subTable2.application_uuid \r\n"
			+ "                  AND subTable2.remark_by <> 'CITIZEN' \r\n" + "            GROUP BY\r\n"
			+ "               subTable1.application_uuid,\r\n" + "               subTable1.application_type \r\n"
			+ "         )\r\n" + "         MainTable \r\n" + "      GROUP BY\r\n"
			+ "         MainTable.application_type \r\n" + "   )\r\n" + "   MainTable1 \r\n" + "   FULL OUTER JOIN\r\n"
			+ "      (\r\n" + "         SELECT\r\n" + "            min(T4.tenant_id) AS tenant_id,\r\n"
			+ "            T4.application_type,\r\n"
			+ "            ROUND(SUM(T4.TotalSum) / SUM(T4.noOfApplication)) As processingTime \r\n"
			+ "         FROM\r\n" + "            (\r\n" + "               SELECT\r\n"
			+ "                  min(T3.tenant_id) AS tenant_id,\r\n" + "                  T3.application_type,\r\n"
			+ "                  COUNT(DISTINCT T3.application_uuid) noOfApplication,\r\n"
			+ "                  (SUM(T3.DateDif) + 1) As TotalSum \r\n" + "               FROM\r\n"
			+ "                  (\r\n" + "                     SELECT\r\n"
			+ "                        T2.tenant_id AS tenant_id,\r\n"
			+ "                        Date_part('day', To_timestamp(t2.dt / 1000)::timestamp - to_timestamp(t1.dt / 1000)::timestamp) DateDif,\r\n"
			+ "                        T2.application_type As application_type,\r\n"
			+ "                        T2.application_uuid AS application_uuid \r\n" + "                     FROM\r\n"
			+ "                        (\r\n" + "                           SELECT DISTINCT\r\n"
			+ "                              min(a.tenant_id) AS tenant_id,\r\n"
			+ "                              min(b.application_uuid) AS application_uuid,\r\n"
			+ "                              min(a.application_type) AS application_type,\r\n"
			+ "                              min(b.created_time) AS dt,\r\n"
			+ "                              min(b.remark_by) AS remark_by,\r\n"
			+ "                              min(b.application_status) AS application_status,\r\n"
			+ "                              dense_rank() OVER (partition BY b.application_uuid \r\n"
			+ "                           ORDER BY\r\n"
			+ "                              min(b.created_time)) rownum \r\n" + "                           FROM\r\n"
			+ "                              egpm_noc_application a \r\n"
			+ "                              INNER JOIN\r\n"
			+ "                                 egpm_noc_application_remark b \r\n"
			+ "                                 ON a.application_uuid = b.application_uuid \r\n"
			+ "                           WHERE  a.application_type in ('PETNOC','ADVERTISEMENTNOC','SELLMEATNOC') AND a.application_status = 'APPROVED' AND\r\n"
			+ "                              a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                           GROUP BY\r\n" + "                              b.created_time,\r\n"
			+ "                              b.application_uuid \r\n" + "                        )\r\n"
			+ "                        T1 \r\n" + "                        INNER JOIN\r\n"
			+ "                           (\r\n" + "                              SELECT DISTINCT\r\n"
			+ "                                 min(a.tenant_id) AS tenant_id,\r\n"
			+ "                                 min(b.application_uuid) AS application_uuid,\r\n"
			+ "                                 min(a.application_type) AS application_type,\r\n"
			+ "                                 min(b.created_time) AS dt,\r\n"
			+ "                                 min(b.remark_by) AS remark_by,\r\n"
			+ "                                 min(b.application_status) AS application_status,\r\n"
			+ "                                 dense_rank() OVER (partition BY b.application_uuid \r\n"
			+ "                              ORDER BY\r\n"
			+ "                                 min(b.created_time)) rownum \r\n"
			+ "                              FROM\r\n" + "                                 egpm_noc_application a \r\n"
			+ "                                 INNER JOIN\r\n"
			+ "                                    egpm_noc_application_remark b \r\n"
			+ "                                    ON a.application_uuid = b.application_uuid \r\n"
			+ "                              WHERE  a.application_type in ('PETNOC','ADVERTISEMENTNOC','SELLMEATNOC') AND a.application_status = 'APPROVED' AND\r\n"
			+ "                                 a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                              GROUP BY\r\n" + "                                 b.created_time,\r\n"
			+ "                                 b.application_uuid \r\n" + "                           )\r\n"
			+ "                           T2 \r\n" + "                           ON T1.rownum + 1 = T2.rownum \r\n"
			+ "                           AND T1.application_uuid = T2.application_uuid \r\n"
			+ "                           AND T2.remark_by <> 'CITIZEN' \r\n" + "					  UNION ALL\r\n"
			+ "					                       SELECT\r\n"
			+ "                        T2.tenant_id AS tenant_id,\r\n"
			+ "                        Date_part('day', To_timestamp(t2.dt / 1000)::timestamp - to_timestamp(t1.dt / 1000)::timestamp) DateDif,\r\n"
			+ "                        T2.application_type As application_type,\r\n"
			+ "                        T2.application_uuid AS application_uuid \r\n" + "                     FROM\r\n"
			+ "                        (\r\n" + "                           SELECT DISTINCT\r\n"
			+ "                              min(a.tenant_id) AS tenant_id,\r\n"
			+ "                              min(b.application_uuid) AS application_uuid,\r\n"
			+ "                              min(a.application_type) AS application_type,\r\n"
			+ "                              min(b.created_time) AS dt,\r\n"
			+ "                              min(b.remark_by) AS remark_by,\r\n"
			+ "                              min(b.application_status) AS application_status,\r\n"
			+ "                              dense_rank() OVER (partition BY b.application_uuid \r\n"
			+ "                           ORDER BY\r\n"
			+ "                              min(b.created_time)) rownum \r\n" + "                           FROM\r\n"
			+ "                              egpm_noc_application a \r\n"
			+ "                              INNER JOIN\r\n"
			+ "                                 egpm_noc_application_remark b \r\n"
			+ "                                 ON a.application_uuid = b.application_uuid \r\n"
			+ "                           WHERE a.application_type='ROADCUTNOC' AND a.application_status in ('PAID','APPROVED') AND\r\n"
			+ "                              a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                           GROUP BY\r\n" + "                              b.created_time,\r\n"
			+ "                              b.application_uuid \r\n" + "                        )\r\n"
			+ "                        T1 \r\n" + "                        INNER JOIN\r\n"
			+ "                           (\r\n" + "                              SELECT DISTINCT\r\n"
			+ "                                 min(a.tenant_id) AS tenant_id,\r\n"
			+ "                                 min(b.application_uuid) AS application_uuid,\r\n"
			+ "                                 min(a.application_type) AS application_type,\r\n"
			+ "                                 min(b.created_time) AS dt,\r\n"
			+ "                                 min(b.remark_by) AS remark_by,\r\n"
			+ "                                 min(b.application_status) AS application_status,\r\n"
			+ "                                 dense_rank() OVER (partition BY b.application_uuid \r\n"
			+ "                              ORDER BY\r\n"
			+ "                                 min(b.created_time)) rownum \r\n"
			+ "                              FROM\r\n" + "                                 egpm_noc_application a \r\n"
			+ "                                 INNER JOIN\r\n"
			+ "                                    egpm_noc_application_remark b \r\n"
			+ "                                    ON a.application_uuid = b.application_uuid \r\n"
			+ "                              WHERE a.application_type='ROADCUTNOC' AND a.application_status in ('PAID','APPROVED') AND\r\n"
			+ "                                 a.applied_date::date BETWEEN (now() - interval '1 YEAR')::date AND now()::date \r\n"
			+ "                              GROUP BY\r\n" + "                                 b.created_time,\r\n"
			+ "                                 b.application_uuid \r\n" + "                           )\r\n"
			+ "                           T2 \r\n" + "                           ON T1.rownum + 1 = T2.rownum \r\n"
			+ "                           AND T1.application_uuid = T2.application_uuid \r\n"
			+ "                           AND T2.remark_by <> 'CITIZEN' \r\n" + "\r\n" + "                  )\r\n"
			+ "                  T3 \r\n" + "               GROUP BY\r\n" + "                  T3.application_type,\r\n"
			+ "                  T3.application_uuid \r\n" + "            )\r\n" + "            T4 \r\n"
			+ "         GROUP BY\r\n" + "            T4.application_type \r\n" + "      )\r\n" + "      MainTable2 \r\n"
			+ "      ON MainTable1.application_type = MainTable2.application_type";

	public static String getPetsQuery() {

		StringBuilder petsQuery = new StringBuilder(SELECT_PETS_QUERY);
		return petsQuery.toString();
	}

	public static String getApplicationQuery() {
		StringBuilder petsQuery = new StringBuilder(SELECT_APPLICATION_QUERY);
		return petsQuery.toString();
	}

	private static final String BASE_QUERY = "select noc_number \"applicationId\",applicant_name \"applicantName\",application_status \"applicationStatus\" from egpm_noc_application_detail ED inner join egpm_noc_application EA on ED.application_uuid=EA.application_uuid ";

	public static String getSearchQuery(RequestData criteria, List<Object> preparedStmtList) {
		StringBuilder builder = new StringBuilder(BASE_QUERY);
		JSONObject dataPayload = criteria.getDataPayload();
		// service type
		if (criteria.getApplicationType() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" EA.application_type=?");
			preparedStmtList.add(criteria.getApplicationType());
		}

		// service request id
		if (dataPayload.get("applicationId") != null) {

			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" EA.noc_number like ?");
			preparedStmtList.add("%" + dataPayload.get("applicationId").toString().trim() + "%");
		}

		// service request status
		if (dataPayload.get("applicationStatus") != null) {

			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" EA.application_status   = ?");
			preparedStmtList.add(dataPayload.get("applicationStatus").toString());
		}

		// from date

		if (dataPayload.get("fromDate") != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" EA.created_time >= ? ");
			preparedStmtList.add(Long.parseLong(dataPayload.get("fromDate").toString()));
		}

		// to date
		if (dataPayload.get("toDate") != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" EA.created_time <= ? ");
			preparedStmtList.add(Long.parseLong(dataPayload.get("toDate").toString()));

		}

		builder.append(
				" AND ED.is_active=TRUE AND EA.is_active=TRUE AND EA.application_status!='DRAFT' ORDER BY EA.created_time desc");
		System.out.println(builder.toString());
		return builder.toString();
	}

	// private String addPaginationWrapper(String query, List<Object>
	// preparedStmtList, RequestData criteria) {
	//
	// String finalQuery = paginationWrapper.replace("{}", query);
	// return finalQuery;
	// }

	private static void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

}
