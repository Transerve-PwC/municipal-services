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
public class PropertyDueRowMapper implements ResultSetExtractor<List<Property>> {

	@Override
	public List<Property> extractData(ResultSet rs) throws SQLException, DataAccessException {

		LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<>();

		while (rs.next()) {
			String propertyId = rs.getString("pid");
			Property currentProperty = propertyMap.get(propertyId);
			String tenantId = rs.getString("pttenantid");

			if (null == currentProperty) {
				PropertyDetails propertyDetails = PropertyDetails.builder().interestRate(rs.getDouble("pd_int_rate")).build();

				currentProperty = Property.builder().id(propertyId).transitNumber(rs.getString("transit_no"))
						.tenantId(tenantId).colony(rs.getString("colony"))
						.propertyDetails(propertyDetails).build();
				propertyMap.put(propertyId, currentProperty);
			}
			addChildrenToProperty(rs, currentProperty);
		}
		return new ArrayList<>(propertyMap.values());
	}

	private void addChildrenToProperty(ResultSet rs, Property property) throws SQLException {


			String OwnerPropertyId = rs.getString("oproperty_id");
			if (rs.getString("oid") != null && OwnerPropertyId.equals(property.getId())) {

				OwnerDetails ownerDetails = OwnerDetails.builder().id(rs.getString("odid"))
						.propertyId(rs.getString("oproperty_id")).ownerId(rs.getString("odowner_id"))
						.name(rs.getString("ownerName"))
						.phone(rs.getString("ownerPhone")).build();

				Owner owners = Owner.builder().id(rs.getString("oid"))
						.ownerDetails(ownerDetails).build();

				property.addOwnerItem(owners);
			}
		
	}
}
