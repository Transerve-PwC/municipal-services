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
			String applicationType = rs.getString("odapplication_type");
			Owner currentOwner = ownerMap.get(ownerId);

			if (null == currentOwner && applicationType != null
					&& applicationType.equalsIgnoreCase("CitizenApplication")) {

				AuditDetails auditdetails = AuditDetails.builder()
						
						  .createdBy(rs.getString("ocreated_by"))
						 						.lastModifiedTime(rs.getLong("omodified_date")).build();

				OwnerDetails ownerDetails = OwnerDetails.builder().id(rs.getString("odid"))
						.propertyId(rs.getString("oproperty_id")).ownerId(rs.getString("odowner_id"))
						.name(rs.getString("odname")).email(rs.getString("odemail"))
						.phone(rs.getString("odphone"))
						.aadhaarNumber(rs.getString("odaadhaar_number"))
						.allotmentStartdate(rs.getLong("odallotment_startdate"))
						.monthlyRent(rs.getString("odmonthly_rent"))
						.fatherOrHusband(rs.getString("odfather_or_husband"))
						.relation(rs.getString("odrelation"))
						.applicationType(OwnerDetails.ApplicationTypeEnum.fromValue(rs.getString("odapplication_type")))
						.applicationNumber(rs.getString("odapplication_number"))
						.dateOfDeathAllottee(rs.getLong("oddate_of_death_allottee"))
						.relationWithDeceasedAllottee(rs.getString("odrelation_with_deceased_allottee"))
						.dueAmount(rs.getBigDecimal("oddue_amount"))
						.aproCharge(rs.getBigDecimal("odapro_charge"))
						.auditDetails(auditdetails).build();

				Property property = Property.builder().id(rs.getString("pid"))
						.transitNumber(rs.getString("pttransit_number")).colony(rs.getString("ptcolony"))
						.pincode(rs.getString("addresspincode"))
						.area(rs.getString("addressarea"))
						.build();

				currentOwner = Owner.builder().id(rs.getString("oid")).property(property)
						.tenantId(rs.getString("otenantid")).allotmenNumber(rs.getString("oallotmen_number"))
						.activeState(rs.getBoolean("oactive_state")).isPrimaryOwner(rs.getBoolean("ois_primary_owner"))
						.applicationState(rs.getString("oapplication_state"))
						.applicationAction(rs.getString("oapplication_action")).ownerDetails(ownerDetails)
						.auditDetails(auditdetails).build();

				ownerMap.put(ownerId, currentOwner);
			}
			addChildrenToProperty(rs, currentOwner);
		}
		return new ArrayList<>(ownerMap.values());

	}

	private void addChildrenToProperty(ResultSet rs, Owner owner) throws SQLException {
		if (rs.getString("docid") != null && rs.getBoolean("docis_active")) {
			
			Document ownershipTransferDocument = Document.builder().id(rs.getString("docid"))
					.referenceId(rs.getString("doc_referenceId"))
					.active(rs.getBoolean("docis_active")).documentType(rs.getString("document_type"))
					.fileStoreId(rs.getString("fileStore_id"))
					.propertyId(rs.getString("doc_propertyId")).build();
			owner.getOwnerDetails().addownershipTransferDocumentsItem(ownershipTransferDocument);
		}
	}

}
