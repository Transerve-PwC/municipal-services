package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.Owner;
import org.egov.ps.model.OwnerDetails;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class OwnerRowMapper implements ResultSetExtractor<List<Owner>> {

    @Override
    public List<Owner> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Owner> owners = new ArrayList<Owner>();
        while (rs.next()) {
            final String ownerId = rs.getString("oid");
            final String ownerDetailId = rs.getString("odid");
            final String OwnerPropertyDetailId = rs.getString("oproperty_details_id");

            final AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("ocreated_by"))
                    .createdTime(rs.getLong("ocreated_time")).lastModifiedBy(rs.getString("omodified_by"))
                    .lastModifiedTime(rs.getLong("omodified_time")).build();

            final OwnerDetails ownerDetails = OwnerDetails.builder().id(ownerDetailId)
                    .ownerId(rs.getString("odowner_id")).ownerName(rs.getString("odowner_name"))
                    .tenantId(rs.getString("otenantid")).guardianName(rs.getString("guardian_name"))
                    .guardianRelation(rs.getString("guardian_relation")).mobileNumber(rs.getString("mobile_number"))
                    .allotmentNumber(rs.getString("allotment_number")).dateOfAllotment(rs.getLong("date_of_allotment"))
                    .possesionDate(rs.getLong("possesion_date")).isApproved(rs.getBoolean("is_approved"))
                    .isCurrentOwner(rs.getBoolean("is_current_owner")).isMasterEntry(rs.getBoolean("is_master_entry"))
                    .address(rs.getString("address")).isDirector(rs.getBoolean("is_director"))
                    .sellerName(rs.getString("seller_name")).sellerGuardianName(rs.getString("seller_guardian_name"))
                    .sellerRelation(rs.getString("seller_relation")).modeOfTransfer(rs.getString("mode_of_transfer"))
                    .dob(rs.getLong("dob"))
                    .auditDetails(auditdetails).build();

            final Owner owner = Owner.builder().id(ownerId).propertyDetailsId(OwnerPropertyDetailId)
                    .tenantId(rs.getString("otenantid")).serialNumber(rs.getString("oserial_number"))
                    .share(rs.getDouble("oshare")).cpNumber(rs.getString("ocp_number")).state(rs.getString("ostate"))
                    .action(rs.getString("oaction")).ownershipType(rs.getString("ownership_type"))
                    .ownerDetails(ownerDetails).auditDetails(auditdetails).build();
            owners.add(owner);
        }
        return owners;
    }

}
