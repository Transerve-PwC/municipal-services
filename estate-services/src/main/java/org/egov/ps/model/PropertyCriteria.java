package org.egov.ps.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	
	private String subCategory;
	
	private String siteNumber;

	private String mohalla;

	private String street;

	private String houseNumber;

	private String sector;

	private List<String> state;

	private Long offset;

	private Long limit;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String propertyId;

	public void setPropertyId(String propertyId) {
		this.propertyIds.add(propertyId);
	}

	@Builder.Default
	private Set<String> propertyIds = new HashSet<String>();

	private List<String> branchType;

	@Builder.Default
	private List<String> relations = new ArrayList<String>();

	private String tenantId;

	/**
	 * Created by
	 */
	private String userId;
	
	private String auctionId;

	public boolean isEmpty() {
		return (this.fileNumber == null && this.category == null && this.state == null && this.offset == null
				&& this.limit == null && this.propertyId == null && this.relations.size() == 0 && this.tenantId == null
				&& this.userId == null && this.propertyIds.size() == 0 && this.branchType == null);
	}
	
	public boolean branchTypeOnly() {
        return (this.branchType!=null && this.tenantId == null && this.fileNumber==null && this.category == null && this.subCategory == null && this.siteNumber == null
                && this.mohalla == null && this.street == null && this.houseNumber == null &&
                this.sector == null && this.state==null && this.offset == null 
                && this.limit == null && this.propertyIds.size() == 0 && this.relations.size() == 0 && auctionId==null
        );
    }

}
