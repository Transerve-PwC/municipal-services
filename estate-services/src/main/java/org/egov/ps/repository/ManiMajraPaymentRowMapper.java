package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.ManiMajraPayment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class ManiMajraPaymentRowMapper implements ResultSetExtractor<List<ManiMajraPayment>> {

	@Override
	public List<ManiMajraPayment> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<ManiMajraPayment> estatePayments = new ArrayList<ManiMajraPayment>();
		while (rs.next()) {

			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("mmp_created_by"))
					.createdTime(rs.getLong("mmp_created_time")).lastModifiedBy(rs.getString("mmp_last_modified_by"))
					.lastModifiedTime(rs.getLong("mmp_last_modified_time")).build();

			ManiMajraPayment estatePayment = ManiMajraPayment.builder().id(rs.getString("mmp_id"))
					.propertyDetailsId(rs.getString("mmp_property_details_id"))
					.receiptDate(rs.getLong("mmp_receipt_date")).rentReceived(rs.getDouble("mmp_rent_received"))
					.receiptNo(rs.getString("mmp_receipt_no")).paymentDate(rs.getLong("mmp_payment_date"))
					.processed(rs.getBoolean("mmp_processed")).auditDetails(auditdetails).build();
			estatePayments.add(estatePayment);
		}
		return estatePayments;
	}
}
