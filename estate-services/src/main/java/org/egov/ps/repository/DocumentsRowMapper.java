package org.egov.ps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.egov.ps.model.Document;
import org.egov.ps.web.contracts.AuditDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class DocumentsRowMapper implements ResultSetExtractor<List<Document>> {

    @Override
    public List<Document> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Document> documents = new ArrayList<Document>();
        while (rs.next()) {
            final AuditDetails docAuditdetails = AuditDetails.builder().createdBy(rs.getString("dcreated_by"))
                    .createdTime(rs.getLong("dcreated_time")).lastModifiedBy(rs.getString("dmodified_by"))
                    .lastModifiedTime(rs.getLong("dmodified_time")).build();

            final Document document = Document.builder().id(rs.getString("docid"))
                    .referenceId(rs.getString("docreference_id")).tenantId(rs.getString("doctenantid"))
                    .isActive(rs.getBoolean("docis_active")).documentType(rs.getString("document_type"))
                    .fileStoreId(rs.getString("file_store_id")).propertyId(rs.getString("docproperty_id"))
                    .auditDetails(docAuditdetails).build();
            documents.add(document);
        }
        return documents;
    }

}
