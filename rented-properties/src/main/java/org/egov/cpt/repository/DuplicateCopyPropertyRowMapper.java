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
				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("created_by"))
						.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("modified_by"))
						.lastModifiedTime(rs.getLong("modified_time")).build();

				// List<Owner> owners = addOwnersToProperty(rs, currentProperty);

				Property property = Property.builder().id(rs.getString("property_id"))
						.transitNumber(rs.getString("transit_number")).colony(rs.getString("colony"))
						.pincode(rs.getString("pincode")).area(rs.getString("area")).build();

				currentapplication = DuplicateCopy.builder().id(applicationId).property(property)
						.tenantId(rs.getString("tenantid")).state(rs.getString("state")).action(rs.getString("action"))
						.applicationNumber(rs.getString("app_number"))
						.allotmentNumber(rs.getString("owner_allot_number"))
						.allotmentStartDate(rs.getLong("allot_start_date"))
						.allotmentEndDate(rs.getLong("allot_end_date"))
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

		AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("created_by"))
				.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("modified_by"))
				.lastModifiedTime(rs.getLong("created_time")).build();

		if (currentapplication.getApplicant() == null) {
			if (rs.getString("aid") != null) {
				applicant = Applicant.builder().id(rs.getString("aid")).tenantId(rs.getString("aptenantid"))
						.applicationId(rs.getString("app_id")).name(rs.getString("name")).email(rs.getString("email"))
						.phone(rs.getString("mobileno")).guardian(rs.getString("guardian"))
						.relationship(rs.getString("relationship")).adhaarNumber(rs.getString("adhaarnumber"))
						.feeAmount(rs.getBigDecimal("fee_amount"))
						.auditDetails(auditDetails).build();
				applicantMap.put(rs.getString("aid"), applicant);
				currentapplication.setApplicant(new ArrayList<>(applicantMap.values()));
			}
		}

		if (currentapplication.getProperty() == null) {
			Property property = Property.builder().id(rs.getString("property_id"))
					.transitNumber(rs.getString("transit_number")).build();
			currentapplication.setProperty(property);
		}

		if (rs.getString("docId") != null && rs.getBoolean("doc_active")) {
			Document applicationDocument = Document.builder().documentType(rs.getString("doctype"))
					.fileStoreId(rs.getString("doc_filestoreid")).id(rs.getString("docId"))
					.referenceId(rs.getString("doc_referenceid")).tenantId(rs.getString("doctenantid"))
					.active(rs.getBoolean("doc_active")).auditDetails(auditDetails)
					.propertyId(rs.getString("doc_propertyid")).build();
			currentapplication.addApplicationDocumentsItem(applicationDocument);
		}

	}

}
