package org.egov.swservice.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SWProperty {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("usageCategory")
	private String usageCategory = null;

	@JsonProperty("usageSubCategory")
	private String usageSubCategory = null;
	
	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SWProperty waterApplication = (SWProperty) o;
		return Objects.equals(this.id, waterApplication.id) 
				&& Objects.equals(this.usageCategory, waterApplication.usageCategory)
				&& Objects.equals(this.usageSubCategory, waterApplication.usageSubCategory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, usageCategory, usageSubCategory);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class waterApplication {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    usageCategory: ").append(toIndentedString(usageCategory)).append("\n");
		sb.append("    usageSubCategory: ").append(toIndentedString(usageSubCategory)).append("\n");
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsageCategory() {
		return usageCategory;
	}

	public void setUsageCategory(String usageCategory) {
		this.usageCategory = usageCategory;
	}

	public String getUsageSubCategory() {
		return usageSubCategory;
	}

	public void setUsageSubCategory(String usageSubCategory) {
		this.usageSubCategory = usageSubCategory;
	}

	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}
}
