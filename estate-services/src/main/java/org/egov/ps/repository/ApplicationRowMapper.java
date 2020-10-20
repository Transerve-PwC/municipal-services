package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.ps.model.Application;
import org.egov.ps.model.Document;
import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyDetails;
import org.egov.ps.web.contracts.AuditDetails;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ApplicationRowMapper implements ResultSetExtractor<List<Application>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<Application> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, Application> applicationMap = new LinkedHashMap<>();

		while (rs.next()) {

			Application currentApplication = null;

			if (hasColumn(rs, "appid")) {
				String applicationId = rs.getString("appid");
				currentApplication = applicationMap.get(applicationId);

				if (null == currentApplication) {
					AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("appcreated_by"))
							.createdTime(rs.getLong("appcreated_time"))
							.lastModifiedBy(rs.getString("applast_modified_by"))
							.lastModifiedTime(rs.getLong("applast_modified_time")).build();

					currentApplication = Application.builder().id(applicationId).tenantId(rs.getString("apptenantid"))
							.applicationNumber(rs.getString("appapplication_number"))
							.branchType(rs.getString("appbranch_type")).moduleType(rs.getString("appmodule_type"))
							.applicationType(rs.getString("appapplication_type")).comments(rs.getString("appcomments"))
							.hardcopyReceivedDate(rs.getLong("apphardcopy_received_date"))
							.state(rs.getString("appstate")).action(rs.getString("appaction"))
							.auditDetails(auditdetails).build();

					PGobject applicationDetailsPgObject = (PGobject) rs.getObject("appapplication_details");
					if (applicationDetailsPgObject != null) {
						JsonNode applicationDetails = null;
						try {
							applicationDetails = mapper.readTree(applicationDetailsPgObject.getValue());
							currentApplication.setApplicationDetails(applicationDetails);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					applicationMap.put(applicationId, currentApplication);

					if (hasColumn(rs, "pid")) {
						try {
							if (rs.getString("pid") != null) {

								final String PropertyDetailId = rs.getString("ptdlid");

								PropertyDetails propertyDetails = PropertyDetails.builder().id(PropertyDetailId)
										.propertyId(rs.getString("pdproperty_id"))
										.propertyType(rs.getString("pdproperty_type"))
										.tenantId(rs.getString("pttenantid"))
										.typeOfAllocation(rs.getString("type_of_allocation"))
										.modeOfAuction(rs.getString("mode_of_auction"))
										.schemeName(rs.getString("scheme_name")).areaSqft(rs.getInt("area_sqft"))
										.dateOfAuction(rs.getLong("date_of_auction"))
										.ratePerSqft(rs.getBigDecimal("rate_per_sqft"))
										.lastNocDate(rs.getLong("last_noc_date"))
										.serviceCategory(rs.getString("service_category"))
										.isPropertyActive(rs.getBoolean("is_property_active"))
										.tradeType(rs.getString("trade_type")).companyName(rs.getString("company_name"))
										.companyAddress(rs.getString("company_address"))
										.companyRegistrationNumber(rs.getString("company_registration_number"))
										.companyType(rs.getString("company_type"))
										.emdAmount(rs.getBigDecimal("emd_amount")).emdDate(rs.getLong("emd_date"))
										.build();

								Property property = Property.builder().id(rs.getString("pid"))
										.fileNumber(rs.getString("file_number")).tenantId(rs.getString("pttenantid"))
										.category(rs.getString("category")).subCategory(rs.getString("sub_category"))
										.sectorNumber(rs.getString("sector_number"))
										.siteNumber(rs.getString("site_number"))
										.propertyMasterOrAllotmentOfSite(
												rs.getString("property_master_or_allotment_of_site"))
										.isCancelationOfSite(rs.getBoolean("is_cancelation_of_site"))
										.state(rs.getString("state"))
										.action(rs.getString("action")).propertyDetails(propertyDetails).build();

								currentApplication.setProperty(property);

							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
			addChildrenToApplication(rs, currentApplication, applicationMap);
		}
		return new ArrayList<>(applicationMap.values());
	}

	private void addChildrenToApplication(ResultSet rs, Application currentApplication,
			LinkedHashMap<String, Application> applicationMap) throws SQLException {

		if (hasColumn(rs, "docid")) {
			String docApplicationId = rs.getString("docapplication_id");

			try {
				if (rs.getString("docid") != null && rs.getBoolean("docis_active")
						&& docApplicationId.equals(currentApplication.getId())) {

					AuditDetails docAuditdetails = AuditDetails.builder().createdBy(rs.getString("dcreated_by"))
							.createdTime(rs.getLong("dcreated_time")).lastModifiedBy(rs.getString("dmodified_by"))
							.lastModifiedTime(rs.getLong("dmodified_time")).build();

					Document applicationDocuments = Document.builder().id(rs.getString("docid"))
							.referenceId(docApplicationId).tenantId(rs.getString("doctenantid"))
							.isActive(rs.getBoolean("docis_active")).documentType(rs.getString("document_type"))
							.fileStoreId(rs.getString("file_store_id")).propertyId(rs.getString("docproperty_id"))
							.auditDetails(docAuditdetails).build();
					currentApplication.addApplicationDocumentsItem(applicationDocuments);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (hasColumn(rs, "oid")) {
			try {
				final String ownerId = rs.getString("oid");
				final String ownerDetailId = rs.getString("odid");
				final String OwnerPropertyDetailId = rs.getString("oproperty_details_id");

				if (ownerId != null) {

					final OwnerDetails ownerDetails = OwnerDetails.builder().id(ownerDetailId)
							.ownerId(rs.getString("odowner_id")).ownerName(rs.getString("odowner_name"))
							.tenantId(rs.getString("otenantid")).guardianName(rs.getString("guardian_name"))
							.guardianRelation(rs.getString("guardian_relation"))
							.mobileNumber(rs.getString("mobile_number"))
							.allotmentNumber(rs.getString("allotment_number"))
							.dateOfAllotment(rs.getLong("date_of_allotment"))
							.possesionDate(rs.getLong("possesion_date")).isApproved(rs.getBoolean("is_approved"))
							.isCurrentOwner(rs.getBoolean("is_current_owner"))
							.isMasterEntry(rs.getBoolean("is_master_entry")).address(rs.getString("address"))
							.isDirector(rs.getBoolean("is_director")).build();

					final Owner owners = Owner.builder().id(ownerId).propertyDetailsId(OwnerPropertyDetailId)
							.tenantId(rs.getString("otenantid")).serialNumber(rs.getString("oserial_number"))
							.share(rs.getDouble("oshare")).cpNumber(rs.getString("ocp_number"))
							.ownershipType(rs.getString("ownership_type")).ownerDetails(ownerDetails).build();

					currentApplication.getProperty().getPropertyDetails().addOwnerItem(owners);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equals(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}
}
