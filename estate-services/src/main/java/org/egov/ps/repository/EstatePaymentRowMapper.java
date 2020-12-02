package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstatePayment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class EstatePaymentRowMapper implements ResultSetExtractor<List<EstatePayment>> {

	@Override
	public List<EstatePayment> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<EstatePayment> estatePayments = new ArrayList<EstatePayment>();
		while (rs.next()) {

			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("estpcreated_by"))
					.createdTime(rs.getLong("estpcreated_time")).lastModifiedBy(rs.getString("estplast_modified_by"))
					.lastModifiedTime(rs.getLong("estplast_modified_time")).build();

			EstatePayment estatePayment = EstatePayment.builder().id(rs.getString("estpid"))
					.propertyDetailsId(rs.getString("estpproperty_details_id"))
					.receiptDate(rs.getLong("estpreceipt_date")).rentReceived(rs.getDouble("estprent_received"))
					.receiptNo(rs.getString("estpreceipt_no")).paymentDate(rs.getLong("estpayment_date")).processed(rs.getBoolean("estpprocessed"))
					.auditDetails(auditdetails).build();
			estatePayments.add(estatePayment);
		}
		return estatePayments;
	}
}
