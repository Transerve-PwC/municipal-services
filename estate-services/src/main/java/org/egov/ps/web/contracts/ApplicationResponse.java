package org.egov.ps.web.contracts;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.ps.model.Application;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Applications")
	@Valid
	private List<Application> applications;

	public ApplicationResponse addApplicationsItem(Application applicationsItem) {
		if (this.applications == null) {
			this.applications = new ArrayList<>();
		}
		this.applications.add(applicationsItem);
		return this;
	}

}
