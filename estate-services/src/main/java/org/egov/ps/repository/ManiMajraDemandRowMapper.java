package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.ManiMajraDemand;
import org.egov.ps.web.contracts.PaymentStatusEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class ManiMajraDemandRowMapper implements ResultSetExtractor<List<ManiMajraDemand>> {

	@Override
	public List<ManiMajraDemand> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<ManiMajraDemand> bidders = new ArrayList<ManiMajraDemand>();
		while (rs.next()) {
			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("mmd_created_by"))
					.createdTime(rs.getLong("mmd_created_time")).lastModifiedBy(rs.getString("mmd_last_modified_by"))
					.lastModifiedTime(rs.getLong("mmd_last_modified_time")).build();

			ManiMajraDemand auction = ManiMajraDemand.builder().id(rs.getString("mmd_id"))
					.propertyDetailsId(rs.getString("mmd_property_details_id"))
					.generationDate(rs.getLong("mmd_demand_date")).paid(rs.getDouble("mmd_paid"))
					.rent(rs.getDouble("mmd_rent")).gst(rs.getDouble("mmd_gst"))
					.status(PaymentStatusEnum.fromValue(rs.getString("mmd_status")))
					.collectedRent(rs.getDouble("mmd_collected_rent")).collectedGST(rs.getDouble("mmd_collected_gst"))
					.comment(rs.getString("mmd_comment")).auditDetails(auditdetails).build();
			bidders.add(auction);
		}
		return bidders;
	}
}
