package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.ModeEnum;
import org.egov.cpt.models.PaymentStatusEnum;
import org.egov.cpt.models.RentDemand;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class RentDemandRowMapper implements ResultSetExtractor<List<RentDemand>> {

	@Override
	public List<RentDemand> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, RentDemand> demandMap = new LinkedHashMap<>();

		while (rs.next()) {
			String demandId = rs.getString("demand_id");
			RentDemand currentapplication = demandMap.get(demandId);

			if (null == currentapplication) {
				AuditDetails demandAuditDetails = AuditDetails.builder().createdBy(rs.getString("demand_created_by"))
						.createdTime(rs.getLong("demand_created_date"))
						.lastModifiedBy(rs.getString("demand_modified_by"))
						.lastModifiedTime(rs.getLong("demand_modified_date")).build();
				RentDemand rentDemand = RentDemand.builder().id(rs.getString("demand_id")).propertyId("demand_pid")
						.initialGracePeriod(rs.getInt("demand_IniGracePeriod"))
						.generationDate(rs.getLong("demand_genDate"))
						.collectionPrincipal(rs.getDouble("demand_colPrincipal"))
						.remainingPrincipal(rs.getDouble("demand_remPrincipal"))
						.interestSince(rs.getLong("demand_intSince"))
						.status(PaymentStatusEnum.fromValue(rs.getString("demand_status")))
						.mode(ModeEnum.fromValue(rs.getString("demand_mode"))).auditDetails(demandAuditDetails).build();
				demandMap.put(demandId, rentDemand);
			}
		}
		return new ArrayList<>(demandMap.values());
	}
}
