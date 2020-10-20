package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.AuctionBidder;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AuctionRowMapper implements ResultSetExtractor<List<AuctionBidder>> {

	@Override
	public List<AuctionBidder> extractData(ResultSet rs) throws SQLException, DataAccessException {

		List<AuctionBidder> bidders = new ArrayList<AuctionBidder>();
		while (rs.next()) {
			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("aucreated_by"))
					.createdTime(rs.getLong("aucreated_time")).lastModifiedBy(rs.getString("aulast_modified_by"))
					.lastModifiedTime(rs.getLong("aulast_modified_time")).build();

			AuctionBidder auction = AuctionBidder.builder()
					.id(rs.getString("auid"))
					.auctionId(rs.getString("auauction_id"))
					.propertyDetailsId(rs.getString("auproperty_details_id"))
					.description(rs.getString("audescription"))
					.bidderName(rs.getString("aubidder_name"))
					.depositedEMDAmount(rs.getBigDecimal("audeposited_emd_mount"))
					.depositDate(rs.getLong("audeposit_date"))
					.emdValidityDate(rs.getLong("auemd_validity_date"))
					.refundStatus(rs.getString("aurefund_status"))
					.state(rs.getString("austate"))
					.action(rs.getString("auaction"))
					.comments(rs.getString("aucomments"))
					.auditDetails(auditdetails)
					.build();
			bidders.add(auction);
		}
		return bidders;
	}
}
