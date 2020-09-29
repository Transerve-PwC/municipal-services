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
import org.egov.nulm.model.SuhApplication;
import org.egov.nulm.model.SuhFacilitiesDetails;
import org.egov.nulm.model.SuhRecordMaintenance;
import org.egov.nulm.model.SuhStaffMaintenance;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SuhRowMapper implements ResultSetExtractor<List<SuhApplication>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<SuhApplication> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, SuhApplication> suhMap = new HashMap<>();
		List<SuhApplication> suhList = new ArrayList<>();
		JSONArray addressPicture = new JSONArray();
		JSONArray programPicture = new JSONArray();
		JSONArray documentAttachment = new JSONArray();
		try {
			while (rs.next()) {
				String id = rs.getString("suh_uuid");
				JSONParser jsonParser = new JSONParser();
				if (!suhMap.containsKey(id)) {
					AuditDetails audit = AuditDetails.builder().createdBy(rs.getString("created_by"))
							.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("last_modified_by"))
							.lastModifiedTime(rs.getLong("last_modified_time")).build();
					if (rs.getString("address_picture") != null) {
						addressPicture = (JSONArray) jsonParser.parse(rs.getString("address_picture"));
					}
					if (rs.getString("program_picture") != null) {
						programPicture = (JSONArray) jsonParser.parse(rs.getString("program_picture"));
					}

					if (rs.getString("document_attachment") != null) {
						documentAttachment = (JSONArray) jsonParser.parse(rs.getString("document_attachment"));
					}

					SuhApplication suhapp = SuhApplication.builder().auditDetails(audit)
							.address(rs.getString("address")).area(rs.getString("area"))
							.nulmApplicationId(rs.getString("nulm_application_id")).capacity(rs.getString("capacity"))
							.category(rs.getString("category"))
							.applicationStatus(SuhApplication.StatusEnum.fromValue(rs.getString("application_status")))
							.constitutionOfShelterManagementCommitteeRemark(
									rs.getString("constitution_of_shelter_management_committee_remark"))
							.iecAndPromotionalInitiativesRemark(rs.getString("iec_and_promotional_initiatives_remark"))
							.isActive(rs.getBoolean("is_active"))
							.isConstitutionOfShelterManagementCommittee(
									rs.getBoolean("is_constitution_of_shelter_management_committee"))
							.isIECAndPromotionalInitiatives(rs.getBoolean("is_iec_and_promotional_initiatives"))
							.isLinkageToCentralGovtWelfareSchemes(
									rs.getBoolean("is_linkage_to_central_govt_welfare_schemes"))
							.isLinkageToLocalCommunity(rs.getBoolean("is_linkage_to_local_community"))
							.isLinkageToOtherGovtSchemes(rs.getBoolean("is_linkage_to_other_govt_schemes"))
							.isLinkageToPublicHealthInitiatives(
									rs.getBoolean("is_linkage_to_public_health_initiatives"))
							.isLinkageToSocialWorkersAndPhilanthropists(
									rs.getBoolean("is_linkage_to_social_workers_and_philanthropists"))
							.isQuarterlyReporting(rs.getBoolean("is_quarterly_reporting"))
							.isSocialAudit(rs.getBoolean("is_social_audit"))
							.isUserCharges(rs.getBoolean("is_user_charges")).isVisits(rs.getBoolean("is_visits"))
							.visitsRemark(rs.getString("visits_remark"))
							.linkageToCentralGovtWelfareSchemesRemark(
									rs.getString("linkage_to_central_govt_welfare_schemes_remark"))
							.linkageToLocalCommunityRemark(rs.getString("linkage_to_local_community_remark"))
							.linkageToOtherGovtSchemesRemark(rs.getString("linkage_to_other_govt_schemes_remark"))
							.linkageToPublicHealthInitiativesRemark(
									rs.getString("linkage_to_public_health_initiatives_remark"))
							.linkageToSocialWorkersAndPhilanthropistsRemark(
									rs.getString("linkage_to_social_workers_and_philanthropists_remark"))
							.nameOfShelter(rs.getString("name_of_shelter"))
							.operationAndManagementOfShelters(rs.getString("operation_and_management_of_shelters"))
							.otherClassification(rs.getString("other_classification"))
							.ownership(rs.getString("ownership"))
							.quarterlyReportingRemark(rs.getString("quarterly_reporting_remark"))
							.remark(rs.getString("remark")).shelterBackground(rs.getString("shelter_background"))
							.shelterClassification(rs.getString("shelter_classification"))
							.socialAuditRemark(rs.getString("social_audit_remark")).suhId(rs.getString("suh_id"))
							.suhUuid(rs.getString("suh_uuid")).tenantId(rs.getString("tenant_id"))
							.userChargesRemark(rs.getString("user_charges_remark"))
							.visitsRemark(rs.getString("created_by")).addressPicture(addressPicture)
							.programPicture(programPicture).documentAttachment(documentAttachment)
							.assignedTo(rs.getString("assigned_to"))
							.assignedToName(rs.getString("organization_name"))
							.weatherCondition(rs.getString("weather_condition")).build();
					List<SuhFacilitiesDetails> facilitiesList = null;
					if (rs.getString("facilities") != null) {
						facilitiesList = Arrays
								.asList(mapper.readValue(rs.getString("facilities"), SuhFacilitiesDetails[].class));
					}
					if (facilitiesList != null)
						facilitiesList = facilitiesList.stream().filter(ele -> ele.getSuhUuid() != null)
								.collect(Collectors.toList());
					List<SuhRecordMaintenance> recordList = null;
					if (rs.getString("record") != null) {
						recordList = Arrays
								.asList(mapper.readValue(rs.getString("record"), SuhRecordMaintenance[].class));
					}
					if (recordList != null)
						recordList = recordList.stream().filter(ele -> ele.getSuhUuid() != null)
								.collect(Collectors.toList());
					List<SuhStaffMaintenance> staffList = null;
					if (rs.getString("staff") != null) {
						staffList = Arrays
								.asList(mapper.readValue(rs.getString("staff"), SuhStaffMaintenance[].class));
					}
					if (staffList != null)
						staffList = staffList.stream().filter(ele -> ele.getSuhUuid() != null)
								.collect(Collectors.toList());
					suhapp.setSuhFacilitiesDetails(facilitiesList);
					suhapp.setSuhRecordMaintenance(recordList);
					suhapp.setSuhStaffMaintenance(staffList);
					suhMap.put(id, suhapp);
					suhList.add(suhapp);
				}
			}

		} catch (Exception e) {
			throw new CustomException(CommonConstants.SUH_APPLICATION_EXCEPTION_CODE, e.getMessage());
		}
		return suhList;
	}

}
