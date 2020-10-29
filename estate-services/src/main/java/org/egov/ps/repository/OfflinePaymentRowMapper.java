package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.OfflinePaymentDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class OfflinePaymentRowMapper implements ResultSetExtractor<List<OfflinePaymentDetails>> {

	@Override
	public List<OfflinePaymentDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<OfflinePaymentDetails> estatePayments = new ArrayList<OfflinePaymentDetails>();
		while (rs.next()) {

			OfflinePaymentDetails estatePayment = OfflinePaymentDetails.builder().id(rs.getString("offlineid"))
					.propertyDetailsId(rs.getString("offlineproperty_details_id"))
					.demandId(rs.getString("offlinedemand_id")).amount(rs.getBigDecimal("offlineamount"))
					.bankName(rs.getString("offlinebank_name"))
					.transactionNumber(rs.getString("offlinetransaction_number"))
					.dateOfPayment(rs.getLong("offlinedate_of_payment")).build();
			estatePayments.add(estatePayment);
		}
		return estatePayments;
	}
}
