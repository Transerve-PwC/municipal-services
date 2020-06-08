package org.egov.assets.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.assets.model.AuditDetails;
import org.egov.assets.model.PurchaseOrder;
import org.egov.assets.model.Supplier;
import org.egov.assets.model.SupplierAdvanceRequisition;
import org.springframework.jdbc.core.RowMapper;

public class SupplierAdvanceRequisitionRowMapper implements RowMapper<SupplierAdvanceRequisition>{
	 
	@Override
	public SupplierAdvanceRequisition mapRow(ResultSet rs, int rowNum) throws SQLException {
		 
	return	new SupplierAdvanceRequisition()
		.id(rs.getString("id"))
		.supplier(new Supplier().code(rs.getString("supplier")))
		.purchaseOrder(new PurchaseOrder().purchaseOrderNumber(rs.getString("purchaseOrder")))
		.advanceAdjustedAmount(rs.getBigDecimal("advanceAdjustedAmount"))
		.sarStatus(rs.getString("sarStatus"))
		.stateId(rs.getString("stateId"))
		.advanceFullyAdjustedInBill(rs.getBoolean("advanceFullyAdjustedInBill"))
		.tenantId(rs.getString("tenantId"))
		.auditDetails(
				new AuditDetails().createdBy(rs.getString("createdBy"))
				.createdTime(rs.getLong("createdTime"))
				.lastModifiedBy(rs.getString("lastModifiedBy"))
				.lastModifiedTime(rs.getLong("lastModifiedTime")));
	}
}
