package org.egov.wscalculation.model;

import java.util.Objects;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T12:39:45.543+05:30[Asia/Kolkata]")
public class WaterApplication {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("applicationNo")
	private String applicationNo = null;

	@JsonProperty("applicationStatus")
	private String applicationStatus = null;

	@JsonProperty("action")
	private String action = null;

	@JsonProperty("comments")
	private String comments = null;

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
		WaterApplication waterApplication = (WaterApplication) o;
		return Objects.equals(this.id, waterApplication.id) && Objects.equals(this.applicationNo, waterApplication.applicationNo)
				&& Objects.equals(this.applicationStatus, waterApplication.applicationStatus)
				&& Objects.equals(this.action, waterApplication.action)
				&& Objects.equals(this.comments, waterApplication.comments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, applicationNo, applicationStatus, action, comments);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class waterApplication {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    applicationNo: ").append(toIndentedString(applicationNo)).append("\n");
		sb.append("    applicationStatus: ").append(toIndentedString(applicationStatus)).append("\n");
		sb.append("    action: ").append(toIndentedString(action)).append("\n");
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
