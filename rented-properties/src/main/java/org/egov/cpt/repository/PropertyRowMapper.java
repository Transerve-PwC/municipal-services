package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.egov.cpt.models.Address;
import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.MortgageApprovedGrantDetails;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.Owner;
import org.egov.cpt.models.OwnerDetails;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyDetails;
import org.egov.cpt.models.PropertyImages;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class PropertyRowMapper implements ResultSetExtractor<List<Property>> {

	@Override
	public List<Property> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<>();

		while (rs.next()) {
			String propertyId = rs.getString("pid");
			Property currentProperty = propertyMap.get(propertyId);
			String tenantId = rs.getString("pttenantid");

			if (null == currentProperty) {
				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("pcreated_by"))
						.createdTime(rs.getLong("pcreated_date")).lastModifiedBy(rs.getString("pmodified_by"))
						.lastModifiedTime(rs.getLong("pmodified_date")).build();

				Address address = Address.builder().id(rs.getString("aid")).propertyId(rs.getString("aproperty_id"))
						.transitNumber(rs.getString("atransit_number")).tenantId(tenantId)
						.colony(rs.getString("colony")).area(rs.getString("addressArea"))
						.district(rs.getString("district")).state(rs.getString("state"))
						.country(rs.getString("country")).pincode(rs.getString("pincode"))
						.landmark(rs.getString("landmark")).build();

				PropertyDetails propertyDetails = PropertyDetails.builder().id(rs.getString("pdid"))
						.propertyId(rs.getString("pdproperty_id")).transitNumber(rs.getString("transit_number"))
						.tenantId(tenantId).area(rs.getString("area")).rentPerSqyd(rs.getString("rent_per_sqyd"))
						.currentOwner(rs.getString("current_owner")).floors(rs.getString("floors"))
						.additionalDetails(rs.getString("additional_details")).address(address)
						.rentIncrementPercentage(rs.getDouble("pd_rent_inc_pg"))
						.rentIncrementPeriod(rs.getInt("pd_rent_inc_period")).interestRate(rs.getDouble("pd_int_rate"))
						.auditDetails(auditdetails).build();

				currentProperty = Property.builder().id(propertyId).transitNumber(rs.getString("transit_number"))
						.tenantId(tenantId).colony(rs.getString("colony"))
						.masterDataState(rs.getString("master_data_state"))
						.masterDataAction(rs.getString("master_data_action")).propertyDetails(propertyDetails)
						.auditDetails(auditdetails).build();
				propertyMap.put(propertyId, currentProperty);
			}
			addChildrenToProperty(rs, currentProperty);
		}
		return new ArrayList<>(propertyMap.values());
	}

	private void addChildrenToProperty(ResultSet rs, Property property) throws SQLException {

		String docPropertyId = rs.getString("docproperty_id");
		if (rs.getString("docid") != null && rs.getBoolean("docis_active") && docPropertyId.equals(property.getId())) {

			AuditDetails docAuditdetails = AuditDetails.builder().createdBy(rs.getString("dcreated_by"))
					.createdTime(rs.getLong("dcreated_date")).lastModifiedBy(rs.getString("dmodified_by"))
					.lastModifiedTime(rs.getLong("dmodified_date")).build();

			Document applicationDocument = Document.builder().id(rs.getString("docid"))
					.referenceId(rs.getString("docreference_id")).propertyId(rs.getString("docproperty_id"))
					.tenantId(rs.getString("doctenantid")).active(rs.getBoolean("docis_active"))
					.documentType(rs.getString("doc_document_type")).fileStoreId(rs.getString("doc_fileStore_id"))
					.auditDetails(docAuditdetails).build();
			property.getPropertyDetails().addApplicationDocumentsItem(applicationDocument);
		}

		if (hasColumn(rs, "oid")) {
			String OwnerPropertyId = rs.getString("oproperty_id");
			if (rs.getString("oid") != null && OwnerPropertyId.equals(property.getId())) {

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
						.applicationNumber(rs.getString("odapplication_number"))
						.dateOfDeathAllottee(rs.getLong("date_of_death_allottee"))
						.relationWithDeceasedAllottee(rs.getString("relation_with_deceased_allottee"))
						.permanent(rs.getBoolean("od_permanent")).auditDetails(auditdetails).build();

				Owner owners = Owner.builder().id(rs.getString("oid")).property(property)
						.tenantId(rs.getString("otenantid")).allotmenNumber(rs.getString("oallotmen_number"))
						.activeState(rs.getBoolean("oactive_state")).isPrimaryOwner(rs.getBoolean("ois_primary_owner"))
						.ownerDetails(ownerDetails).auditDetails(auditdetails).build();

				property.addOwnerItem(owners);
			}
		}
		if (hasColumn(rs, "piid")) {
			String propertyImagesPropertyId = rs.getString("pipropertyid");
			if (rs.getString("piid") != null && propertyImagesPropertyId.equals(property.getId())) {

				AuditDetails piAuditDetails = AuditDetails.builder().createdBy(rs.getString("piCreatedBy"))
						.createdTime(rs.getLong("piCreatedTime")).lastModifiedBy(rs.getString("piModifiedBy"))
						.lastModifiedTime(rs.getLong("piModifiedTime")).build();

				PropertyImages propertyImages = PropertyImages.builder().id(rs.getString("piid")).property(property)
						.tenantId(rs.getString("pitenantid")).applicationNumber(rs.getString("piapp_number"))
						.description(rs.getString("pidescription")).capturedBy(rs.getString("picapturedBy"))
						.auditDetails(piAuditDetails).build();

				property.addPropertyImagesItem(propertyImages);

				if (rs.getString("pidocid") != null && rs.getBoolean("pidoc_active")) {
					Document applicationDocument = Document.builder().documentType(rs.getString("pidoctype"))
							.fileStoreId(rs.getString("pidoc_filestoreid")).id(rs.getString("pidocId"))
							.tenantId(rs.getString("pidoctenantid")).active(rs.getBoolean("pidoc_active"))
							.referenceId(rs.getString("pidoc_referenceid")).propertyId(rs.getString("pidoc_propertyid"))
							.auditDetails(piAuditDetails).build();

					for (PropertyImages propertyImage : property.getPropertyImages()) {
						if (propertyImage.getId().equalsIgnoreCase(rs.getString("pidoc_referenceid"))) {
							propertyImage.addApplicationDocumentsItem(applicationDocument);
						}
					}

					/*
					 * PropertyImages propertyImages1 =
					 * property.getPropertyImages().stream().filter(p -> { try { return
					 * p.getId().equalsIgnoreCase(rs.getString("pidocid")); } catch (SQLException e)
					 * { e.printStackTrace(); } return false; }).findFirst().get();
					 * propertyImages1.addApplicationDocumentsItem(applicationDocument);
					 */

				}
			}
		}

		// Notice Generation
		if (hasColumn(rs, "ngid")) {
			if (rs.getString("ngid") != null && rs.getString("ng_propertyid").equals(property.getId())) {

				AuditDetails ngAuditDetails = AuditDetails.builder().createdBy(rs.getString("ngCreatedBy"))
						.createdTime(rs.getLong("ngCreatedTime")).lastModifiedBy(rs.getString("ngModifiedBy"))
						.lastModifiedTime(rs.getLong("ngModifiedTime")).build();

				NoticeGeneration noticeGeneration = NoticeGeneration.builder().id(rs.getString("ngid"))
						.property(property).tenantId(rs.getString("ngtenantid"))
						.memoNumber(rs.getString("ng_memoNumber")).memoDate(rs.getLong("ng_memoDate"))
						.noticeType(rs.getString("ng_noticeType")).guardian(rs.getString("ng_guardian"))
						.relationship(rs.getString("ng_relationship")).violations(rs.getString("ng_violations"))
						.description(rs.getString("ng_description")).demandNoticeFrom(rs.getLong("ng_demandNoticeFrom"))
						.demandNoticeTo(rs.getLong("ng_demandNoticeTo")).recoveryType(rs.getString("ng_recoveryType"))
						.amount(rs.getDouble("ng_amount")).propertyImageId(rs.getString("ng_p_image_id"))
						.allotmentNumber(rs.getString("oallotmen_number")).auditDetails(ngAuditDetails).build();

				property.addNoticeItem(noticeGeneration);

				if (rs.getString("ngdoc_id") != null && rs.getBoolean("ngdoc_active")) {
					Document applicationDocument = Document.builder().documentType(rs.getString("ngdoc_type"))
							.fileStoreId(rs.getString("ngdoc_filestoreid")).id(rs.getString("ngdoc_id"))
							.tenantId(rs.getString("ngdoc_tenantid")).active(rs.getBoolean("ngdoc_active"))
							.referenceId(rs.getString("ngdoc_referenceid")).propertyId(rs.getString("ngdoc_propertyid"))
							.auditDetails(ngAuditDetails).build();

					for (NoticeGeneration notice : property.getNotices()) {
						if (notice.getId().equalsIgnoreCase(rs.getString("ngdoc_referenceid"))) {
							notice.addApplicationDocumentsItem(applicationDocument);
						}
					}

					/*
					 * NoticeGeneration notice = property.getNotices().stream().filter(p -> { try {
					 * return p.getId().equalsIgnoreCase(rs.getString("ngdoc_id")); } catch
					 * (SQLException e) { e.printStackTrace(); } return false; }).findFirst().get();
					 * notice.addApplicationDocumentsItem(applicationDocument);
					 */

				}
			}
		}

		// GrantDetails
		if (hasColumn(rs, "gd_id")) {
			if (rs.getString("gd_id") != null) {
				AuditDetails magdAuditDetails = AuditDetails.builder().createdBy(rs.getString("gd_createdBy"))
						.createdTime(rs.getLong("gd_createdTime")).lastModifiedBy(rs.getString("gd_modifiedBy"))
						.lastModifiedTime(rs.getLong("gd_modifiedTime")).build();
				MortgageApprovedGrantDetails mortgageApprovedGrantDetails = MortgageApprovedGrantDetails.builder()
						.id(rs.getString("gd_id")).propertyDetailId(rs.getString("gd_property_id"))
						.bankName(rs.getString("gd_bank_name")).mortgageAmount(rs.getBigDecimal("gd_mortgage_amount"))
						.sanctionLetterNumber(rs.getString("gd_sanLetterNum")).sanctionDate(rs.getLong("gd_sanDate"))
						.mortgageEndDate(rs.getLong("gd_mortgageEndDate")).auditDetails(magdAuditDetails).build();
				property.addGrantDetailItem(mortgageApprovedGrantDetails);
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
