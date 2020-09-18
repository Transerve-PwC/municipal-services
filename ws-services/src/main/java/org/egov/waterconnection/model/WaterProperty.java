package org.egov.waterconnection.model;

import java.util.Objects;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T12:39:45.543+05:30[Asia/Kolkata]")
public class WaterProperty {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("usageCategory")
	private String usageCategory = null;

	@JsonProperty("usageSubCategory")
	private String usageSubCategory = null;

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		WaterProperty waterApplication = (WaterProperty) o;
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
}
