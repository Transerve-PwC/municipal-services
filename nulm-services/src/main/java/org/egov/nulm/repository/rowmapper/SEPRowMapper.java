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
import org.egov.nulm.model.SepApplication;
import org.egov.nulm.model.SepApplicationDocument;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SEPRowMapper implements ResultSetExtractor<List<SepApplication>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SepApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SepApplication> sepMap = new HashMap<>();
		
		List<SepApplication> listSEPApplication = new ArrayList<>();

		try {
			while (rs.next()) {
				String id = rs.getString("application_uuid");
				SepApplication sepapp = new SepApplication();
				
				sepapp = sepapp.builder().build();
				if (!sepMap.containsKey(id)) {
					AuditDetails audit = AuditDetails.builder().createdBy(rs.getString("created_by"))
							.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("last_modified_by"))
							.lastModifiedTime(rs.getLong("last_modified_time")).build();
					sepapp.setAddress(rs.getString("address"));
					sepapp.setAdharNo(rs.getString("adhar_no"));
					sepapp.setAge(rs.getInt("age"));
					sepapp.setApplicationId(rs.getString("application_id"));
					sepapp.setApplicationStatus(
							SepApplication.StatusEnum.fromValue(rs.getString("application_status")));
					sepapp.setApplicationUuid(rs.getString("application_uuid"));
					sepapp.setBplNo(rs.getString("bpl_no"));
					sepapp.setCategory(rs.getString("category"));
					sepapp.setContact(rs.getString("contact"));
					sepapp.setAuditDetails(audit);
					sepapp.setSinceHowLongInChandigarh(rs.getString("since_how_long_in_chandigarh"));
					sepapp.setDob(rs.getString("dob"));
					sepapp.setFatherOrHusbandName(rs.getString("father_or_husband_name"));
					sepapp.setGender(rs.getString("gender"));
					sepapp.setIsActive(rs.getBoolean("is_active"));
					sepapp.setIsHandicapped(rs.getBoolean("is_handicapped"));
					sepapp.setIsLoanFromBankinginstitute(rs.getBoolean("is_loan_from_bankinginstitute"));
					sepapp.setIsMinority(rs.getBoolean("is_minority"));
					sepapp.setIsRepaymentMade(rs.getBoolean("is_repayment_made"));
					sepapp.setIsUrbanPoor(rs.getBoolean("is_urban_poor"));
					sepapp.setLoanAmount(rs.getBigDecimal("loan_amount"));
					sepapp.setMinority(rs.getString("minority"));
					sepapp.setName(rs.getString("name"));
					sepapp.setMotherName(rs.getString("mother_name"));
					sepapp.setNoOfFamilyMembers(rs.getString("no_of_family_members"));
					sepapp.setNulmApplicationId(rs.getString("nulm_application_id"));
					sepapp.setOccupation(rs.getString("occupation"));
					sepapp.setPlaceOfWork(rs.getString("place_of_work"));
					sepapp.setPreviousExperience(rs.getString("previous_experience"));
					sepapp.setQualification(rs.getString("qualification"));
					sepapp.setRemark(rs.getString("remark"));
					sepapp.setRecommendedBy(rs.getString("recommended_by"));
					sepapp.setRepresentativeAddress(rs.getString("representative_address"));
					sepapp.setRepresentativeName(rs.getString("representative_name"));
					sepapp.setTenantId(rs.getString("tenant_id"));
					sepapp.setAccountName(rs.getString("account_name"));
					sepapp.setBankName(rs.getString("bank_name"));
					sepapp.setBranchName(rs.getString("branch_name"));
					sepapp.setIsDisabilityCertificateAvailable(rs.getBoolean("is_disability_certificate_available"));
					sepapp.setTaskCommitteeApprovedAmount(rs.getString("task_committee_approved_amount"));
					sepapp.setTaskCommitteeRemark(rs.getString("task_committee_remark"));
					sepapp.setTaskCommitteeActionDate(rs.getString("task_committee_action_date"));
					sepapp.setTaskCommitteeStatus(rs.getString("task_committee_status"));
					sepapp.setCommitteeBankName(rs.getString("committee_bank_name"));
					sepapp.setCommitteeBranchName(rs.getString("committee_branch_name"));
					sepapp.setApplicationForwardedOnDate(rs.getString("application_forwarded_on_date"));
					sepapp.setSanctionDate(rs.getString("sanction_date"));
					sepapp.setSanctionRemarks(rs.getString("sanction_remarks"));
					sepapp.setTypeOfBusinessToBeStarted(rs.getString("type_of_business_to_be_started"));
					List<SepApplicationDocument> documentAttachment = null;
					if (rs.getString("document") != null) {
						documentAttachment = Arrays
								.asList(mapper.readValue(rs.getString("document"), SepApplicationDocument[].class));
					}
					sepapp.setApplicationDocument(documentAttachment);
					sepMap.put(id, sepapp);
					listSEPApplication.add(sepapp);
				}
			}

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SEP_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
		return listSEPApplication;
	}

}
