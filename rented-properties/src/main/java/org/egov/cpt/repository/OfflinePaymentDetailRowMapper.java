package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.OfflinePaymentDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class OfflinePaymentDetailRowMapper implements ResultSetExtractor<List<OfflinePaymentDetails>>{

	@Override
	public List<OfflinePaymentDetails> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, OfflinePaymentDetails> offlinePaymentDetailMap = new LinkedHashMap<>();

		while (rs.next()) {
		String offlinePaymentDetailId = rs.getString("opdid");
		OfflinePaymentDetails currentApplication = offlinePaymentDetailMap.get(offlinePaymentDetailId);

		if(null == currentApplication) {
			AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("opdcreated_by"))
					.createdTime(rs.getLong("opdcreated_date")).lastModifiedBy(rs.getString("opdmodified_by"))
					.lastModifiedTime(rs.getLong("opdmodified_date")).build();

			OfflinePaymentDetails offlinePaymentDetails = OfflinePaymentDetails.builder().id(rs.getString("opdid"))
					.propertyId(rs.getString("opdproperty_id"))
					.demandId(rs.getString("opddemand_id"))
					.amount(rs.getDouble("opdamount"))
					.bankName(rs.getString("opdbankname"))
					.transactionNumber(rs.getString("opdtransactionnumber"))
					.auditDetails(auditDetails)
					.build();
			offlinePaymentDetailMap.put(offlinePaymentDetailId, offlinePaymentDetails);
		}
		} 
		return new ArrayList<>(offlinePaymentDetailMap.values());
	}

	
}
