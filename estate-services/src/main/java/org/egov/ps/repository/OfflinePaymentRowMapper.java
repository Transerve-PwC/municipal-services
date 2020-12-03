package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.OfflinePaymentDetails;
import org.egov.ps.model.OfflinePaymentDetails.OfflinePaymentType;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class OfflinePaymentRowMapper implements ResultSetExtractor<List<OfflinePaymentDetails>> {

	@Override
	public List<OfflinePaymentDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<OfflinePaymentDetails> estatePayments = new ArrayList<OfflinePaymentDetails>();
		while (rs.next()) {

			AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("offline_created_by"))
					.createdTime(rs.getLong("offline_created_time"))
					.lastModifiedBy(rs.getString("offline_last_modified_by"))
					.lastModifiedTime(rs.getLong("offline_last_modified_time")).build();

			OfflinePaymentDetails estatePayment = OfflinePaymentDetails.builder().id(rs.getString("offlineid"))
					.propertyDetailsId(rs.getString("offlineproperty_details_id"))
					.demandId(rs.getString("offlinedemand_id")).amount(rs.getBigDecimal("offlineamount"))
					.bankName(rs.getString("offlinebank_name"))
					.transactionNumber(rs.getString("offlinetransaction_number"))
					.dateOfPayment(rs.getLong("offlinedate_of_payment"))
					.type(OfflinePaymentType.fromValue(rs.getString("offline_type"))).auditDetails(auditDetails)
					.build();
			estatePayments.add(estatePayment);
		}
		return estatePayments;
	}
}
