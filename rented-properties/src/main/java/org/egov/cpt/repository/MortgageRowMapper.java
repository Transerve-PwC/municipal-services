package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.Mortgage;
import org.egov.cpt.models.MortgageApplicant;
import org.egov.cpt.models.MortgageApprovedGrantDetails;
import org.egov.cpt.models.Property;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class MortgageRowMapper implements ResultSetExtractor<List<Mortgage>> {

	@Override
	public List<Mortgage> extractData(ResultSet rs) throws SQLException, DataAccessException {
		LinkedHashMap<String, Mortgage> applicationMap = new LinkedHashMap<>();
		while (rs.next()) {
			String mortgageId = rs.getString("mgid");
			Mortgage currentapplication = applicationMap.get(mortgageId);

			if (null == currentapplication) {
				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("created_by"))
						.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("modified_by"))
						.lastModifiedTime(rs.getLong("modified_time")).build();

				// List<Owner> owners = addOwnersToProperty(rs, currentProperty);

				Property property = Property.builder().id(rs.getString("pid"))
						.transitNumber(rs.getString("transit_number")).colony(rs.getString("colony"))
						.pincode(rs.getString("pincode")).area(rs.getString("area")).build();

				currentapplication = Mortgage.builder().id(mortgageId).property(property)
						.tenantId(rs.getString("tenantid")).state(rs.getString("state")).action(rs.getString("action"))
						.applicationNumber(rs.getString("app_number"))
						.allotmentNumber(rs.getString("owner_allot_number"))
						.allotmentStartDate(rs.getString("allot_start_date"))
						.allotmentEndDate(rs.getString("allot_end_date"))
						.auditDetails(auditdetails).build();
				applicationMap.put(mortgageId, currentapplication);
			}
			addChildrenToProperty(rs, currentapplication);
		}
		return new ArrayList<>(applicationMap.values());

	}

	private void addChildrenToProperty(ResultSet rs, Mortgage currentapplication) throws SQLException {
		Map<String, MortgageApplicant> applicantMap = new HashMap<>();
		MortgageApplicant applicant = null;

		AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("created_by"))
				.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("modified_by"))
				.lastModifiedTime(rs.getLong("created_time")).build();

		if (currentapplication.getApplicant() == null) {
			if (rs.getString("aid") != null) {
				applicant = MortgageApplicant.builder().id(rs.getString("aid")).tenantId(rs.getString("aptenantid"))
						.mortgageId(rs.getString("mg_id")).name(rs.getString("name")).email(rs.getString("email"))
						.phone(rs.getString("mobileno")).guardian(rs.getString("guardian"))
						.relationship(rs.getString("relationship")).adhaarNumber(rs.getString("adhaarnumber"))
						.auditDetails(auditDetails).build();
				applicantMap.put(rs.getString("aid"), applicant);
				currentapplication.setApplicant(new ArrayList<>(applicantMap.values()));
			}
		}

		if (currentapplication.getProperty() == null) {
			Property property = Property.builder().id(rs.getString("pid")).transitNumber(rs.getString("transit_number"))
					.build();
			currentapplication.setProperty(property);
		}

		if (rs.getString("gdid") != null) {
			AuditDetails magdAuditDetails = AuditDetails.builder().createdBy(rs.getString("gdcreated_by"))
					.createdTime(rs.getLong("gdcreated_time")).lastModifiedBy(rs.getString("gdmodified_by"))
					.lastModifiedTime(rs.getLong("gdmodified_time")).build();
			MortgageApprovedGrantDetails mortgageApprovedGrantDetails = MortgageApprovedGrantDetails.builder()
					.id(rs.getString("gdid")).propertyDetailId(rs.getString("gdproperty_detail_id"))
					.ownerId(rs.getString("gdowner_id")).tenentId(rs.getString("gdtenantid"))
					.bankName(rs.getString("gdbank_name")).mortgageAmount(rs.getBigDecimal("gdmortgage_amount"))
					.sanctionLetterNumber(rs.getString("gdsanction_letter_number"))
					.sanctionDate(rs.getLong("gdsanction_date")).mortgageEndDate(rs.getLong("gdmortgage_end_date"))
					.auditDetails(magdAuditDetails).build();
			currentapplication.addMortgageApprovedGrantDetails(mortgageApprovedGrantDetails);
		}

		if (rs.getString("docId") != null && rs.getBoolean("doc_active")) {
			Document applicationDocument = Document.builder().documentType(rs.getString("doctype"))
					.fileStoreId(rs.getString("doc_filestoreid")).id(rs.getString("docId"))
					.tenantId(rs.getString("doctenantid")).active(rs.getBoolean("doc_active"))
					.referenceId(rs.getString("doc_referenceid")).propertyId(rs.getString("doc_propertyid"))
					.auditDetails(auditDetails).build();
			currentapplication.addApplicationDocumentsItem(applicationDocument);
		}

	}

}