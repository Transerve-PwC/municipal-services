package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.ModeEnum;
import org.egov.cpt.models.RentPayment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class RentPaymentRowMapper implements ResultSetExtractor<List<RentPayment>> {

	@Override
	public List<RentPayment> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, RentPayment> paymentMap = new LinkedHashMap<>();

		while (rs.next()) {
			String paymentId = rs.getString("payment_id");
			RentPayment currentapplication = paymentMap.get(paymentId);

			if (null == currentapplication) {
				AuditDetails paymentAuditDetails = AuditDetails.builder().createdBy(rs.getString("payment_created_by"))
						.createdTime(rs.getLong("payment_created_date"))
						.lastModifiedBy(rs.getString("payment_modified_by"))
						.lastModifiedTime(rs.getLong("payment_modified_date")).build();
				RentPayment rentPayment = RentPayment.builder().id(rs.getString("payment_id")).propertyId(rs.getString("payment_pid"))
						.receiptNo(rs.getString("payment_receiptNo")).processed(rs.getBoolean("processed"))
						.amountPaid(rs.getDouble("payment_amtPaid")).dateOfPayment(rs.getLong("payment_dateOfPayment"))
						.mode(ModeEnum.fromValue(rs.getString("payment_mode"))).auditDetails(paymentAuditDetails)
						.build();
				paymentMap.put(paymentId, rentPayment);
			}
		}
		return new ArrayList<>(paymentMap.values());
	}
}
