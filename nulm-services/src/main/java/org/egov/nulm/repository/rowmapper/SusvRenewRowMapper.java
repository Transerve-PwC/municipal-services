package org.egov.nulm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.model.AuditDetails;
import org.egov.nulm.model.SusvRenewApplication;
import org.egov.nulm.model.SusvApplicationDocument;
import org.egov.nulm.model.SusvApplicationFamilyDetails;
import org.egov.nulm.model.SusvRenewApplication;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SusvRenewRowMapper implements ResultSetExtractor<List<SusvRenewApplication>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SusvRenewApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SusvRenewApplication> susvMap = new HashMap<>();
		List<SusvRenewApplication> susvList = new ArrayList<>();

		try {
			while (rs.next()) {
				String id = rs.getString("application_uuid");
				
				if (!susvMap.containsKey(id)) {
					AuditDetails audit = AuditDetails.builder().createdBy(rs.getString("created_by"))
							.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("last_modified_by"))
							.lastModifiedTime(rs.getLong("last_modified_time")).build();
					
					
					SusvRenewApplication susvapp = SusvRenewApplication.builder().auditDetails(audit)
							.applicationUuId(rs.getString("application_uuid"))
							.applicationId(rs.getString("application_id"))
							.tenantId(rs.getString("tenant_id"))
							.lookingFor(rs.getString("looking_for"))
							.nameOfStreetVendor(rs.getString("name_of_street_vendor"))
							.covNo(rs.getString("cov_no"))
							.applicationStatus(SusvRenewApplication.StatusEnum.fromValue(rs.getString("application_status")))
							.residentialAddress(rs.getString("residential_address"))
							.changeOfLocation(rs.getBoolean("change_of_location"))
							.proposedAddress(rs.getString("proposed_address"))
							.nameOfProposedNewStreetVendor(rs.getString("name_of_proposed_new_street_vendor"))
							.isActive(rs.getBoolean("is_active"))
							.build();
					List<SusvApplicationDocument> documentAttachment = null;
					if (rs.getString("document") != null) {
						documentAttachment = Arrays
								.asList(mapper.readValue(rs.getString("document"), SusvApplicationDocument[].class));
					}
					if (documentAttachment != null)
						documentAttachment = documentAttachment.stream().filter(ele -> ele.getApplicationUuid() != null)
								.collect(Collectors.toList());
					susvapp.setApplicationDocument(documentAttachment);
					List<SusvApplicationFamilyDetails> family = null;
					if (rs.getString("familymembers") != null) {
						family = Arrays
								.asList(mapper.readValue(rs.getString("familymembers"), SusvApplicationFamilyDetails[].class));
					}
					
					susvapp.setSusvApplicationFamilyDetails(family);
					susvMap.put(id, susvapp);
					susvList.add(susvapp);
				}
			}

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUSV_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
		return susvList;
	}

}
