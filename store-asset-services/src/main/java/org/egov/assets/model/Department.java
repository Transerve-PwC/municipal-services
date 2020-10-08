package org.egov.assets.model;

import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 */
@Validated
public class Department {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("code")
	private String code = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("deductionPercentage")
	private BigDecimal deductionPercentage;

	@JsonProperty("deptCategory")
	private String deptCategory;
	
	@JsonProperty("active")
	private Boolean active = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	public Department id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Unique Identifier of the Department
	 * 
	 * @return id
	 **/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Department tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * Tenant id of the Department
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

	public Department code(String code) {
		this.code = code;
		return this;
	}

	/**
	 * code of the Department
	 * 
	 * @return code
	 **/

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Department name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * name of the Department
	 * 
	 * @return name
	 **/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Department deptCategory(String deptCategory) {
		this.deptCategory = deptCategory;
		return this;
	}

	/**
	 * name of the Department
	 * 
	 * @return name
	 **/

	public String getDeptCategory() {
		return deptCategory;
	}

	public void setDeptCategory(String deptCategory) {
		this.deptCategory = deptCategory;
	}

	public BigDecimal getDeductionPercentage() {
		return deductionPercentage;
	}

	public void setDeductionPercentage(BigDecimal deductionPercentage) {
		this.deductionPercentage = deductionPercentage;
	}

	public Department active(Boolean active) {
		this.active = active;
		return this;
	}

	/**
	 * Whether Department is Active or not. If the value is TRUE, then Department is
	 * active,If the value is FALSE then Department is inactive,Default value is
	 * TRUE
	 * 
	 * @return active
	 **/

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Department auditDetails(AuditDetails auditDetails) {
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
		Department department = (Department) o;
		return Objects.equals(this.id, department.id) && Objects.equals(this.tenantId, department.tenantId)
				&& Objects.equals(this.code, department.code) && Objects.equals(this.name, department.name)
				&& Objects.equals(this.active, department.active)
				&& Objects.equals(this.auditDetails, department.auditDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, tenantId, code, name, active, auditDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Department {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    code: ").append(toIndentedString(code)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    active: ").append(toIndentedString(active)).append("\n");
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
