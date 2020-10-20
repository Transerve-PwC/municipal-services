package org.egov.ps.model;

import java.util.ArrayList;
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
public class PropertyCriteria {

	private String fileNumber;

	private String category;

	private List<String> state;

	private Long offset;

	private Long limit;

	private String propertyId;

	private String branchType;

	@Builder.Default
	private List<String> relations = new ArrayList<String>();

	private String tenantId;

	/**
	 * Created by
	 */
	private String userId;

	public boolean isEmpty() {
		return (this.fileNumber == null && this.category == null && this.state == null && this.offset == null
				&& this.limit == null && this.propertyId == null && this.relations.size() == 0 && this.tenantId == null
				&& this.userId == null);
	}
}
