package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.CourtCase;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class CourtCasesRowMapper implements ResultSetExtractor<List<CourtCase>> {

	@Override
	public List<CourtCase> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<CourtCase> courtCases = new ArrayList<CourtCase>();
		while (rs.next()) {

			final String courtCasePropertDetailId = rs.getString("ccproperty_details_id");

			final AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("cccreated_by"))
					.createdTime(rs.getLong("cccreated_time")).lastModifiedBy(rs.getString("ccmodified_by"))
					.lastModifiedTime(rs.getLong("ccmodified_time")).build();

			final CourtCase courtCase = CourtCase.builder().id(rs.getString("ccid"))
					.propertyDetailsId(courtCasePropertDetailId).tenantId(rs.getString("cctenantid"))
					.estateOfficerCourt(rs.getString("ccestate_officer_court"))
					.commissionersCourt(rs.getString("cccommissioners_court"))
					.chiefAdministartorsCourt(rs.getString("ccchief_administartors_court"))
					.advisorToAdminCourt(rs.getString("ccadvisor_to_admin_court"))
					.honorableDistrictCourt(rs.getString("cchonorable_district_court"))
					.honorableHighCourt(rs.getString("cchonorable_high_court"))
					.honorableSupremeCourt(rs.getString("cchonorable_supreme_court")).auditDetails(auditdetails)
					.build();

			courtCases.add(courtCase);

		}
		return courtCases;
	}

}
