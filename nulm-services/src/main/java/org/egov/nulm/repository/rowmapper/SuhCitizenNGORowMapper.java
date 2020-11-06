package org.egov.nulm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.model.AuditDetails;
import org.egov.nulm.model.SuhCitizenNGOApplication;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SuhCitizenNGORowMapper implements ResultSetExtractor<List<SuhCitizenNGOApplication>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SuhCitizenNGOApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<SuhCitizenNGOApplication> suhList = new ArrayList<>();
		try {
			while (rs.next()) {
					AuditDetails audit = AuditDetails.builder().createdBy(rs.getString("created_by"))
							.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("last_modified_by"))
							.lastModifiedTime(rs.getLong("last_modified_time")).build();

					SuhCitizenNGOApplication suhapp = SuhCitizenNGOApplication.builder().auditDetails(audit)
							.suhCitizenNGOUuid(rs.getString("suh_citizen_ngo_uuid"))
							.address(rs.getString("address"))
							.shelterRequestedForPerson(rs.getString("shelter_requested_for_person"))
							.age(rs.getInt("age")).gender(rs.getString("gender"))
							.reasonForStaying(rs.getString("reason_for_staying"))
							.nominatedBy(rs.getString("nominated_by"))
							.dob(rs.getString("dob"))
							.nameOfNominatedPerson(rs.getString("name_of_nominated_person"))
							.contactNo(rs.getString("contact_no")).isDisabled(rs.getBoolean("is_disabled")).contactNo(rs.getString("contact_no"))
							.isActive(rs.getBoolean("is_active")).build();
					suhList.add(suhapp);
				}
		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
		return suhList;
	}

}
