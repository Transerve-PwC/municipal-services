package org.egov.cpt.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuplicateCopySearchCriteria {

	@Builder.Default
	private String tenantId = "ch.chandigarh";

	private String transitNumber;

	private String colony;

	private List<String> status;

	private String applicationNumber;

	private String applicantMobNo;

	private String propertyId;

	private String appId;

	private Long offset;

	private Long limit;

	private String createdBy;

	public boolean isEmpty() {
		return (this.transitNumber == null && this.colony == null && this.status == null
				&& this.applicationNumber == null && this.applicantMobNo == null && this.propertyId == null
				&& this.appId == null && this.offset == null && this.limit == null);
	}

}
