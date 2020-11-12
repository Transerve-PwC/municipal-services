package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class PropertyPenaltyRowMapper implements ResultSetExtractor<List<PropertyPenalty>> {

	@Override
	public List<PropertyPenalty> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<PropertyPenalty> propertyPenalties = new ArrayList<PropertyPenalty>();
		while (rs.next()) {
			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("penalty_created_by"))
					.createdTime(rs.getLong("penalty_created_time"))
					.lastModifiedBy(rs.getString("penalty_last_modified_by"))
					.lastModifiedTime(rs.getLong("penalty_last_modified_time")).build();

			Property property = Property.builder().id(rs.getString("penalty_property_id")).build();

			PropertyPenalty propertyPenalty = PropertyPenalty.builder().id(rs.getString("penalty_id"))
					.tenantId(rs.getString("penalty_tenantid")).property(property)
					.branchType(rs.getString("penalty_branch_type"))
					.penaltyAmount(rs.getDouble("penalty_penalty_amount"))
					.violationType(rs.getString("penalty_violation_type")).isPaid(rs.getBoolean("penalty_paid"))
					.generationDate(rs.getLong("penalty_generation_date"))
					.remainingPenaltyDue(rs.getDouble("penalty_remaining_penalty_due"))
					.status(PaymentStatusEnum.fromValue(rs.getString("penalty_status"))).auditDetails(auditdetails)
					.build();
			propertyPenalties.add(propertyPenalty);
		}
		return propertyPenalties;
	}
}
