package org.egov.assets.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Purchase order and associated indent Detail mapping.
 */

public class PurchaseIndentDetail {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("indentDetail")
	private IndentDetail indentDetail = null;

	@JsonProperty("quantity")
	private BigDecimal quantity = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	
	public PurchaseIndentDetail id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Unique Identifier of the Purchase Indent
	 * 
	 * @return id
	 **/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PurchaseIndentDetail tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * Tenant id of the Purchase Indent
	 * 
	 * @return tenantId
	 **/
	@Size(min = 2, max = 128)
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}


	public PurchaseIndentDetail indentDetail(IndentDetail indentDetail) {
		this.indentDetail = indentDetail;
		return this;
	}
	/**
	 * Get indentDetail
	 * 
	 * @return indentDetail
	 **/

	@NotNull
	@Valid
	public IndentDetail getIndentDetail() {
		return indentDetail;
	}

	public void setIndentDetail(IndentDetail indentDetail) {
		this.indentDetail = indentDetail;
	}

	public PurchaseIndentDetail quantity(BigDecimal quantity) {
		this.quantity = quantity;
		return this;
	}

	/**
	 * quantity used in each indent.
	 * 
	 * @return quantity
	 **/
	@Valid

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public PurchaseIndentDetail auditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
		return this;
	}

	/**
	 * Get auditDetails
	 * 
	 * @return auditDetails
	 **/
	@Valid

	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PurchaseIndentDetail purchaseIndentDetail = (PurchaseIndentDetail) o;
		return Objects.equals(this.id, purchaseIndentDetail.id)
				&& Objects.equals(this.tenantId, purchaseIndentDetail.tenantId)
				&& Objects.equals(this.indentDetail, purchaseIndentDetail.indentDetail)
				&& Objects.equals(this.quantity, purchaseIndentDetail.quantity)
				&& Objects.equals(this.auditDetails, purchaseIndentDetail.auditDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, tenantId, indentDetail, quantity, auditDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PurchaseIndentDetail {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    indentDetail: ").append(toIndentedString(indentDetail)).append("\n");
		sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
		sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
