package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.cpt.models.Applicant;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.Property;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DuplicateCopyPropertyRowMapper implements ResultSetExtractor<List<DuplicateCopy>> {

	@Override
	public List<DuplicateCopy> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, DuplicateCopy> applicationMap = new LinkedHashMap<>();
		while (rs.next()) {
			String applicationId = rs.getString("appid");
			DuplicateCopy currentapplication = applicationMap.get(applicationId);

			if (null == currentapplication) {
				AuditDetails auditdetails = AuditDetails.builder()
						//.createdBy(rs.getString("dcacreated_by"))
						//.createdTime(rs.getLong("dcacreated_time"))
						.lastModifiedBy(rs.getString("dcamodified_by"))
						.lastModifiedTime(rs.getLong("dcModifiedTime")).build();

				// List<Owner> owners = addOwnersToProperty(rs, currentProperty);

				Property property = Property.builder().id(rs.getString("pid"))
						.transitNumber(rs.getString("pttransit_number")).colony(rs.getString("ptcolony"))
						.pincode(rs.getString("addresspincode")).area(rs.getString("addressarea")).build();

				currentapplication = DuplicateCopy.builder().id(applicationId).property(property)
						.tenantId(rs.getString("pttenantid")).state(rs.getString("dcastate"))
						.action(rs.getString("dcaaction"))
						.applicationNumber(rs.getString("app_number"))
						//.allotmentNumber(rs.getString("owner_allot_number"))
						//.allotmentStartDate(rs.getString("allot_start_date"))
						//.allotmentEndDate(rs.getString("allot_end_date"))
						.auditDetails(auditdetails).build();
				applicationMap.put(applicationId, currentapplication);
			}
			addChildrenToProperty(rs, currentapplication);
		}
		return new ArrayList<>(applicationMap.values());
	}

	private void addChildrenToProperty(ResultSet rs, DuplicateCopy currentapplication) throws SQLException {
		Map<String, Applicant> applicantMap = new HashMap<>();
		Applicant applicant = null;

		/*
		 * AuditDetails auditDetails =
		 * AuditDetails.builder().createdBy(rs.getString("apcreated_by"))
		 * .createdTime(rs.getLong("apcreated_time")).lastModifiedBy(rs.getString(
		 * "apmodified_by")) .lastModifiedTime(rs.getLong("apcreated_time")).build();
		 */
		if (currentapplication.getApplicant() == null) {
			if (rs.getString("aid") != null) {
				applicant = Applicant.builder().id(rs.getString("aid")).tenantId(rs.getString("aptenantid"))
						.applicationId(rs.getString("app_id")).name(rs.getString("apname")).email(rs.getString("apemail"))
						.phone(rs.getString("apmobileno"))
						//.guardian(rs.getString("apguardian"))
						.relationship(rs.getString("aprelationship")).adhaarNumber(rs.getString("adhaarnumber"))
						.feeAmount(rs.getBigDecimal("apfee_amount")).aproCharge(rs.getBigDecimal("apapro_charge"))
						//.auditDetails(auditDetails)
						.build();
				applicantMap.put(rs.getString("aid"), applicant);
				currentapplication.setApplicant(new ArrayList<>(applicantMap.values()));
			}
		}

		if (currentapplication.getProperty() == null) {
			Property property = Property.builder().id(rs.getString("pid"))
					.transitNumber(rs.getString("pttransit_number")).build();
			currentapplication.setProperty(property);
		}

		if (rs.getString("docId") != null && rs.getBoolean("doc_active")) {
			Document applicationDocument = Document.builder().documentType(rs.getString("doctype"))
					.fileStoreId(rs.getString("doc_filestoreid")).id(rs.getString("docId"))
					.referenceId(rs.getString("doc_referenceid")).tenantId(rs.getString("doctenantid"))
					.active(rs.getBoolean("doc_active"))
					//.auditDetails(auditDetails)
					.propertyId(rs.getString("doc_propertyid")).build();
			currentapplication.addApplicationDocumentsItem(applicationDocument);
		}

	}

}
