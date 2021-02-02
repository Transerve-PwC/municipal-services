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
							.bankName(rs.getString("appbank_name")).transactionId(rs.getString("apptransaction_number"))
							.paymentAmount(rs.getBigDecimal("appamount")).paymentType(rs.getString("apppayment_type"))
							.dateOfPayment(rs.getLong("appdate_of_payment"))
							.newsPaperAdvertisementDate(rs.getLong("appnews_paper_advertisement_date"))
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

					if (hasColumn(rs, "appproperty_id")) {
						PropertyDetails propertyDetails = PropertyDetails.builder().id(rs.getString("ptdlid"))
								.branchType(rs.getString("appbranch_type")).build();
						Property property = Property.builder().id(rs.getString("appproperty_id"))
								.propertyDetails(propertyDetails).build();
						currentApplication.setProperty(property);
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

				final AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ocreated_by"))
						.createdTime(rs.getLong("ocreated_time")).lastModifiedBy(rs.getString("omodified_by"))
						.lastModifiedTime(rs.getLong("omodified_time")).build();

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
							.isDirector(rs.getBoolean("is_director"))
							.isPreviousOwnerRequired(rs.getBoolean("is_previous_owner_required"))
							.sellerName(rs.getString("seller_name"))
							.sellerGuardianName(rs.getString("seller_guardian_name"))
							.sellerRelation(rs.getString("seller_relation"))
							.modeOfTransfer(rs.getString("mode_of_transfer")).dob(rs.getLong("dob"))
							.auditDetails(auditdetails).build();

					final Owner owners = Owner.builder().id(ownerId).propertyDetailsId(OwnerPropertyDetailId)
							.tenantId(rs.getString("otenantid")).serialNumber(rs.getString("oserial_number"))
							.share(rs.getDouble("oshare")).cpNumber(rs.getString("ocp_number"))
							.state(rs.getString("ostate")).action(rs.getString("oaction"))
							.ownershipType(rs.getString("ownership_type")).ownerDetails(ownerDetails)
							.auditDetails(auditdetails).build();

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
