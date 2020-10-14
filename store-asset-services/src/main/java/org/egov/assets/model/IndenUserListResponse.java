package org.egov.assets.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contract class for web response. Array of Indent items are used in case of
 * search ,create or update request.
 */

public class IndenUserListResponse {
	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo = null;

	@JsonProperty("users")
	private List<String> users = null;

	@JsonProperty("page")
	private Page page = null;

	public IndenUserListResponse responseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
		return this;
	}

	/**
	 * Get responseInfo
	 * 
	 * @return responseInfo
	 **/
	@Valid

	public ResponseInfo getResponseInfo() {
		return responseInfo;
	}

	public void setResponseInfo(ResponseInfo responseInfo) {
		this.responseInfo = responseInfo;
	}

	public IndenUserListResponse users(List<String> users) {
		this.users = users;
		return this;
	}


	/**
	 * Used for search result and create only
	 * 
	 * @return indents
	 **/
	@Valid

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public IndenUserListResponse page(Page page) {
		this.page = page;
		return this;
	}

	/**
	 * Get page
	 * 
	 * @return page
	 **/
	@Valid

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndenUserListResponse indentResponse = (IndenUserListResponse) o;
		return Objects.equals(this.responseInfo, indentResponse.responseInfo)
				&& Objects.equals(this.users, indentResponse.users)
				&& Objects.equals(this.page, indentResponse.page);
	}

	@Override
	public int hashCode() {
		return Objects.hash(responseInfo, users, page);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class IndentResponse {\n");

		sb.append("    responseInfo: ").append(toIndentedString(responseInfo)).append("\n");
		sb.append("    users: ").append(toIndentedString(users)).append("\n");
		sb.append("    page: ").append(toIndentedString(page)).append("\n");
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
