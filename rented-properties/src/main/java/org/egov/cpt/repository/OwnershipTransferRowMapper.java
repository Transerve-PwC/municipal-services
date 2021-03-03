package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.OwnerDetails;
import org.egov.cpt.models.Property;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class OwnershipTransferRowMapper implements ResultSetExtractor<List<Owner>> {

	@Override
	public List<Owner> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, Owner> ownerMap = new LinkedHashMap<>();
		while (rs.next()) {
			String ownerId = rs.getString("oid");
			String applicationType = rs.getString("application_type");
			Owner currentOwner = ownerMap.get(ownerId);

			if (null == currentOwner && applicationType != null
					&& applicationType.equalsIgnoreCase("CitizenApplication")) {

				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ocreated_by"))
						.createdTime(rs.getLong("ocreated_date")).lastModifiedBy(rs.getString("omodified_by"))
						.lastModifiedTime(rs.getLong("omodified_date")).build();

				OwnerDetails ownerDetails = OwnerDetails.builder().id(rs.getString("odid"))
						.propertyId(rs.getString("oproperty_id")).ownerId(rs.getString("owner_id"))
						.tenantId(rs.getString("otenantid")).name(rs.getString("name")).email(rs.getString("email"))
						.phone(rs.getString("phone")).gender(rs.getString("gender"))
						.dateOfBirth(rs.getLong("date_of_birth")).aadhaarNumber(rs.getString("aadhaar_number"))
						.allotmentStartdate(rs.getLong("allotment_startdate"))
						.allotmentEnddate(rs.getLong("allotment_enddate"))
						.posessionStartdate(rs.getLong("posession_startdate"))
						.posessionEnddate(rs.getLong("posession_enddate")).monthlyRent(rs.getString("monthly_rent"))
						.revisionPeriod(rs.getString("revision_period"))
						.revisionPercentage(rs.getString("revision_percentage"))
						.fatherOrHusband(rs.getString("father_or_husband")).relation(rs.getString("relation"))
						.applicationType(OwnerDetails.ApplicationTypeEnum.fromValue(rs.getString("application_type")))
						.applicationNumber(rs.getString("application_number"))
						.dateOfDeathAllottee(rs.getLong("date_of_death_allottee"))
						.relationWithDeceasedAllottee(rs.getString("relation_with_deceased_allottee"))
						.permanent(rs.getBoolean("permanent")).dueAmount(rs.getBigDecimal("due_amount"))
						.isAPROChargePaid(rs.getBoolean("isapro_charge_paid"))
						.aproCharge(rs.getBigDecimal("apro_charge")).auditDetails(auditdetails).build();

				Property property = Property.builder().id(rs.getString("pid"))
						.transitNumber(rs.getString("transit_number")).colony(rs.getString("colony"))
						.pincode(rs.getString("pincode")).area(rs.getString("area")).build();

				currentOwner = Owner.builder().id(rs.getString("oid")).property(property)
						.tenantId(rs.getString("otenantid")).allotmenNumber(rs.getString("oallotmen_number"))
						.activeState(rs.getBoolean("oactive_state")).isPrimaryOwner(rs.getBoolean("ois_primary_owner"))
						.applicationState(rs.getString("application_state"))
						.applicationAction(rs.getString("application_action")).ownerDetails(ownerDetails)
						.auditDetails(auditdetails).build();

				ownerMap.put(ownerId, currentOwner);
			}
			addChildrenToProperty(rs, currentOwner);
		}
		return new ArrayList<>(ownerMap.values());

	}

	private void addChildrenToProperty(ResultSet rs, Owner owner) throws SQLException {
		if (rs.getString("docid") != null && rs.getBoolean("docis_active")) {
			AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ocreated_by"))
					.createdTime(rs.getLong("ocreated_date")).lastModifiedBy(rs.getString("omodified_by"))
					.lastModifiedTime(rs.getLong("omodified_date")).build();

			Document ownershipTransferDocument = Document.builder().id(rs.getString("docid"))
					.referenceId(rs.getString("doc_referenceId")).tenantId(rs.getString("doctenantid"))
					.active(rs.getBoolean("docis_active")).documentType(rs.getString("document_type"))
					.fileStoreId(rs.getString("fileStore_id")).auditDetails(auditdetails)
					.propertyId(rs.getString("doc_propertyId")).build();
			owner.getOwnerDetails().addownershipTransferDocumentsItem(ownershipTransferDocument);
		}
	}

}
