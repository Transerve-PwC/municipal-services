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
import org.egov.nulm.model.SusvApplication;
import org.egov.nulm.model.SusvApplicationDocument;
import org.egov.nulm.model.SusvApplicationFamilyDetails;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SusvRowMapper implements ResultSetExtractor<List<SusvApplication>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SusvApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SusvApplication> susvMap = new HashMap<>();
		List<SusvApplication> susvList = new ArrayList<>();

		try {
			while (rs.next()) {
				String id = rs.getString("application_uuid");
				
				if (!susvMap.containsKey(id)) {
					AuditDetails audit = AuditDetails.builder().createdBy(rs.getString("created_by"))
							.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("last_modified_by"))
							.lastModifiedTime(rs.getLong("last_modified_time")).build();
					
					
					SusvApplication susvapp = SusvApplication.builder().auditDetails(audit)
							.applicationStatus(SusvApplication.StatusEnum.fromValue(rs.getString("application_status")))
							.adharNo(rs.getString("adhar_no"))
							.age(rs.getInt("age"))
							.applicationId(rs.getString("application_id"))
							.applicationUuid(rs.getString("application_uuid"))
							.bloodGroup(rs.getString("blood_group"))
							.category(rs.getString("category"))
							.categoryOfVending(rs.getString("category_of_vending"))
							.fatherOrHusbandName(rs.getString("father_or_husband_name"))
							.govermentScheme(rs.getString("goverment_scheme"))
							.gender(rs.getString("gender"))
							.isDisability(rs.getBoolean("is_disability"))
							.mobileNo(rs.getString("mobile_no"))
							.motherName(rs.getString("mother_name"))
							.nameOfApplicant(rs.getString("name_of_applicant"))
							.nameOfNominee(rs.getString("name_of_nominee"))
							.nulmApplicationId(rs.getString("nulm_application_id"))
							.permanentAddress(rs.getString("permanent_address"))
							.presentAddress(rs.getString("present_address"))
							.proposedLocationOfVending(rs.getString("proposed_location_of_vending"))
							.proposedTimeOfVending(rs.getString("proposed_time_of_vending"))
							.qualification(rs.getString("qualification"))
							.remark(rs.getString("remark"))
							.date(rs.getString("date"))
							.isUndertaking(rs.getBoolean("is_undertaking"))
							.place(rs.getString("place"))
							.tenantId(rs.getString("tenant_id"))
							.isActive(rs.getBoolean("is_active"))
							.tenantId(rs.getString("tenant_id"))
							.dob(rs.getString("dob"))
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
