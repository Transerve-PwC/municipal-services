package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.web.contracts.AuditDetails;
import org.egov.ps.web.contracts.EstateDemand;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class EstateDemandRowMapper implements ResultSetExtractor<List<EstateDemand>> {

	@Override
	public List<EstateDemand> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<EstateDemand> bidders = new ArrayList<EstateDemand>();
		while (rs.next()) {
			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("estdcreated_by"))
					.createdTime(rs.getLong("estdcreated_time")).lastModifiedBy(rs.getString("estdlast_modified_by"))
					.lastModifiedTime(rs.getLong("estdlast_modified_time")).build();

			EstateDemand auction = EstateDemand.builder().id(rs.getString("estdid"))
					.propertyDetailsId(rs.getString("estdproperty_details_id"))
					.generationDate(rs.getLong("estddemand_date")).isPrevious(rs.getBoolean("estdis_previous"))
					.rent(rs.getDouble("estdrent")).penaltyInterest(rs.getDouble("estdpenalty_interest"))
					.gstInterest(rs.getDouble("estdgst_interest")).gst(rs.getDouble("estdgst"))
					.collectedRent(rs.getDouble("estdcollected_rent")).collectedGST(rs.getDouble("estdcollected_gst"))
					.noOfDays(rs.getDouble("estdno_of_days")).paid(rs.getDouble("estdpaid"))
					.remainingRentPenalty(rs.getDouble("estdremaining_rent_penalty"))
					.remainingGSTPenalty(rs.getDouble("estdremaining_gst_penalty"))
					.remainingRent(rs.getDouble("estdremaining_rent")).collectedGSTPenalty(rs.getDouble("estdcollected_gst_penalty"))
					.collectedRentPenalty(rs.getDouble("estdcollected_gst_penalty")).interestSince(rs.getLong("estdinterest_since"))
					.remainingGST(rs.getDouble("estdremaining_gst"))
					.auditDetails(auditdetails)
					.build();
			bidders.add(auction);
		}
		return bidders;
	}
}
