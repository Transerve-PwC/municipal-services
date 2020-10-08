package org.egov.cpt.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.cpt.models.AuditDetails;
import org.egov.cpt.models.Document;
import org.egov.cpt.models.Property;
import org.egov.cpt.models.PropertyImages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PropertyImagesRowMapper implements ResultSetExtractor<List<PropertyImages>> {

	@SuppressWarnings("unused")
	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<PropertyImages> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, PropertyImages> applicationMap = new HashMap<>();
		while (rs.next()) {
			String propertyImagesId = rs.getString("piid");
			PropertyImages currentapplication = applicationMap.get(propertyImagesId);

			if (null == currentapplication) {
				AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("created_by"))
						.lastModifiedTime(rs.getLong("modified_time")).build();

				// List<Owner> owners = addOwnersToProperty(rs, currentProperty);

				Property property = Property.builder().id(rs.getString("pid"))
						.transitNumber(rs.getString("transit_number")).colony(rs.getString("colony"))
						.pincode(rs.getString("pincode")).area(rs.getString("area")).build();

				currentapplication = PropertyImages.builder().id(propertyImagesId).property(property)
						.tenantId(rs.getString("tenantid"))
						.applicationNumber(rs.getString("app_number")).description(rs.getString("pidescription")).auditDetails(auditdetails).build();
				applicationMap.put(propertyImagesId, currentapplication);
			}
			addChildrenToProperty(rs, currentapplication);
		}
		return new ArrayList<>(applicationMap.values());

	}

	private void addChildrenToProperty(ResultSet rs, PropertyImages currentapplication) throws SQLException {

		AuditDetails auditDetails = AuditDetails.builder().createdBy(rs.getString("created_by"))
				.createdTime(rs.getLong("created_time")).lastModifiedBy(rs.getString("modified_by"))
				.lastModifiedTime(rs.getLong("created_time")).build();


		if (currentapplication.getProperty() == null) {
			Property property = Property.builder().id(rs.getString("pid")).transitNumber(rs.getString("transit_number"))
					.build();
			currentapplication.setProperty(property);
		}


		if (rs.getString("docId") != null && rs.getBoolean("doc_active")) {
			Document applicationDocument = Document.builder()
					.documentType(rs.getString("doctype")).fileStoreId(rs.getString("doc_filestoreid"))
					.id(rs.getString("docId")).active(rs.getBoolean("doc_active"))
					.referenceId(rs.getString("doc_referenceId"))
					.propertyId(rs.getString("doc_propertyId"))
					.auditDetails(auditDetails).build();
			currentapplication.addApplicationDocumentsItem(applicationDocument);
		}

	}

}