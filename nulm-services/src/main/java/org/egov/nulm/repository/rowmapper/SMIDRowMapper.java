package org.egov.nulm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.model.AuditDetails;
import org.egov.nulm.model.SmidApplication;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SMIDRowMapper implements ResultSetExtractor<List<SmidApplication>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SmidApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SmidApplication> smidMap = new HashMap<>();
		List<SmidApplication> listSMIDApplication = new ArrayList<>();

		try {
			while (rs.next()) {
				String id = rs.getString("application_uuid");
				new SmidApplication();

				if (!smidMap.containsKey(id)) {
					AuditDetails audit = AuditDetails.builder().createdBy(rs.getString("created_by"))
							.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("last_modified_by"))
							.lastModifiedTime(rs.getLong("last_modified_time")).build();

					SmidApplication smidapp = SmidApplication.builder().auditDetails(audit)
							.accountNo(rs.getString("account_no")).address(rs.getString("address"))
							.adharAcknowledgementNo(rs.getString("adhar_acknowledgement_no"))
							.adharNo(rs.getString("adhar_no")).applicationId(rs.getString("application_id"))
							.applicationStatus(SmidApplication.StatusEnum.fromValue(rs.getString("application_status")))
							.applicationUuid(rs.getString("application_uuid")).bankName(rs.getString("bank_name"))
							.bplNo(rs.getString("bpl_no")).branchName(rs.getString("branch_name"))
							.caste(rs.getString("caste")).dateOfOpeningAccount(rs.getString("date_of_opening_account"))
							.dob(rs.getString("dob")).emailId(rs.getString("email_id"))
							.fatherOrHusbandName(rs.getString("father_or_husband_name")).gender(rs.getString("gender"))
							.insuranceThrough(rs.getString("insurance_through")).isActive(rs.getBoolean("is_active"))
							.isHomeless(rs.getBoolean("is_homeless")).isInsurance(rs.getBoolean("is_insurance"))
							.isMinority(rs.getBoolean("is_minority")).isPwd(rs.getBoolean("is_pwd"))
							.isStreetVendor(rs.getBoolean("is_street_vendor"))
							.isUrbanPoor(rs.getBoolean("is_urban_poor")).minority(rs.getString("minority"))
							.mobileNo(rs.getString("mobile_no")).motherName(rs.getString("mother_name"))
							.name(rs.getString("name")).nameAsPerAdhar(rs.getString("name_as_per_adhar"))
							.nulmApplicationId(rs.getString("nulm_application_id")).phoneNo(rs.getString("phone_no"))
							.qualification(rs.getString("qualification")).remark(rs.getString("remark"))
							.tenantId(rs.getString("tenant_id")).wardNo(rs.getString("ward_no"))
							.isRegistered(rs.getBoolean("is_registered")).cobNumber(rs.getString("cob_number")).age(rs.getInt("age")).build();
					JSONArray documentAttachment = null;
					if (rs.getString("document_attachemnt") != null) {
						documentAttachment = (mapper.readValue(rs.getString("document_attachemnt"), JSONArray.class));
					}
					smidapp.setDocumentAttachemnt(documentAttachment);
					smidMap.put(id, smidapp);
					listSMIDApplication.add(smidapp);
				}
			}

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SMID_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
		return listSMIDApplication;
	}

}
