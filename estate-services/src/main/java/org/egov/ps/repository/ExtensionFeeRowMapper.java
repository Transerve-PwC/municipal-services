package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.ExtensionFee;
import org.egov.ps.model.Property;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class ExtensionFeeRowMapper implements ResultSetExtractor<List<ExtensionFee>> {

	@Override
	public List<ExtensionFee> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<ExtensionFee> extensionFees = new ArrayList<ExtensionFee>();
		while (rs.next()) {
			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ef_created_by"))
					.createdTime(rs.getLong("ef_created_time")).lastModifiedBy(rs.getString("ef_last_modified_by"))
					.lastModifiedTime(rs.getLong("ef_last_modified_time")).build();

			Property property = Property.builder().id(rs.getString("ef_property_id")).build();

			ExtensionFee extensionFee = ExtensionFee.builder().id(rs.getString("ef_id"))
					.tenantId(rs.getString("ef_tenantid")).property(property).branchType(rs.getString("ef_branch_type"))
					.amount(rs.getDouble("ef_amount")).isPaid(rs.getBoolean("ef_paid"))
					.generationDate(rs.getLong("ef_generation_date")).remainingDue(rs.getDouble("ef_remaining_due"))
					.status(PaymentStatusEnum.fromValue(rs.getString("ef_status"))).auditDetails(auditdetails).build();
			extensionFees.add(extensionFee);
		}
		return extensionFees;
	}
}
