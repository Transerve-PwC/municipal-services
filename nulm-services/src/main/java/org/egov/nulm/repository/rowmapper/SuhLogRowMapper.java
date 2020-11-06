package org.egov.nulm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.model.AuditDetails;
import org.egov.nulm.model.SmidApplication;
import org.egov.nulm.model.SuhLogMaintenance;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SuhLogRowMapper implements ResultSetExtractor<List<SuhLogMaintenance>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SuhLogMaintenance> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SuhLogMaintenance> logMap = new HashMap<>();
		List<SuhLogMaintenance> listSuhLogMaintenance = new ArrayList<>();
		
		try {
			while (rs.next()) {
				String id = rs.getString("log_uuid");
				
				if (!logMap.containsKey(id)) {
					AuditDetails audit=AuditDetails.builder().createdBy(rs.getString("created_by")).createdTime(rs.getLong("created_time")).
							lastModifiedBy(rs.getString("last_modified_by")).lastModifiedTime(rs.getLong("last_modified_time")).build();
								
					SuhLogMaintenance log = SuhLogMaintenance.builder().auditDetails(audit).aadhaarNo(rs.getString("adhar_no"))
							.address(rs.getString("address"))
							.age(rs.getString("age"))
							.date(rs.getString("date"))
							.gender(rs.getString("gender"))
							.isActive(rs.getBoolean("is_active"))
							.logUuid(rs.getString("log_uuid"))
							.name(rs.getString("name"))
							.nameOfShelter(rs.getString("name_of_shelter"))
							.qualification(rs.getString("qualification"))
							.reasonForStaying(rs.getString("reason_for_staying"))
							.tenantId(rs.getString("tenant_id"))
							.dob(rs.getString("dob"))
							.build();
					logMap.put(id, log);
				listSuhLogMaintenance.add(log);
				}
			}
		
	} catch (Exception e) {
		throw new CustomException(CommonConstants.SUH_LOG_EXCEPTION_CODE, e.getMessage());
	}
	return listSuhLogMaintenance;
}

}
