package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateAccount;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class EstateAccountRowMapper implements ResultSetExtractor<EstateAccount> {

	@Override
	public EstateAccount extractData(ResultSet rs) throws SQLException, DataAccessException {
		EstateAccount estateAccount = null;
		while (rs.next()) {

			AuditDetails accountAuditDetails = AuditDetails.builder().createdBy(rs.getString("account_created_by"))
					.createdTime(rs.getLong("account_created_date")).lastModifiedBy(rs.getString("account_modified_by"))
					.lastModifiedTime(rs.getLong("account_modified_date")).build();
			estateAccount = EstateAccount.builder().id(rs.getString("account_id"))
					.propertyDetailsId("account_pid")
					.remainingAmount(rs.getDouble("account_remainingAmount"))
					.remainingSince(rs.getLong("account_remaining_since")).auditDetails(accountAuditDetails).build();
		}
		return estateAccount;
	}

}
