package org.egov.cpt.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A Object holds the basic data for a Property
 */
@ApiModel(description = "A Object holds the basic data for a Property")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PropertyDetails {

	/**
	 * Current interest rate per year. This will not change and is constant.
	 * 
	 * Vikas Nagar Mauli Jagran (Sites 1-2765) - 0 Sector 52-53 - 0 Milk Colony
	 * Maloya - 24% Kumhar Colony Maloya - 24%
	 */
	@NotNull
	@Builder.Default
	@JsonProperty("interestRate")
	private Double interestRate = 0.0;

	/**
	 * How much the monthly rent increases once the period ends.
	 * 
	 * Vikas Nagar Mauli Jagran (Sites 1-2765) - 5% Sector 52-53 - 5% Milk Colony
	 * Maloya - 25% Kumhar Colony Maloya - 25%
	 */
	@NotNull
	@JsonProperty("rentIncrementPercentage")
	@Builder.Default
	private Double rentIncrementPercentage = 5D;

	/**
	 * How often does the monthly rent amount increase.
	 * 
	 * Vikas Nagar Mauli Jagran (Sites 1-2765) - 1 Sector 52-53 - 1 Milk Colony
	 * Maloya - 5 Kumhar Colony Maloya - 5
	 */
	@NotNull
	@JsonProperty("rentIncrementPeriod")
	@Builder.Default
	@Max(value = 99, message = "rentIncrementPeriod can not be more than 99")
	@Min(value = 0, message = "rentIncrementPeriod can not be less than zero")
	private int rentIncrementPeriod = 1;

	@JsonProperty("id")
	@Size(max = 256)
	private String id;

	@JsonProperty("propertyId")
	@Size(max = 256)
	private String propertyId;

	@JsonProperty("transitNumber")
	@Size(max = 256)
	private String transitNumber;

	@JsonProperty("tenantId")
	@Size(max = 256)
	private String tenantId;

	@NotNull
	@JsonProperty("area")
	@Size(max = 256)
	private String area;

	@JsonProperty("rentPerSqyd")
	@Size(max = 256)
	private String rentPerSqyd;

	/**
	 * The id of the currently owning user. During property master this will be set.
	 * During ownership transfer, new value should be also set here.
	 */
	@JsonProperty("currentOwner")
	@Size(max = 256)
	private String currentOwner;

	@JsonProperty("floors")
	@Size(max = 256)
	private String floors;

	@JsonProperty("additionalDetails")
	@Size(max = 256)
	private String additionalDetails;

	@Valid
	@JsonProperty("address")
	private Address address;

	@Valid
	@JsonProperty("applicationDocuments")
	private List<Document> applicationDocuments;

	@JsonProperty("auditDetails")
	@Builder.Default
	private AuditDetails auditDetails = null;

	public PropertyDetails addApplicationDocumentsItem(Document applicationDocumentsItem) {
		if (this.applicationDocuments == null) {
			this.applicationDocuments = new ArrayList<>();
		}
		for (Document applicationDocument : applicationDocuments) {
			if (applicationDocument.getId().equalsIgnoreCase(applicationDocumentsItem.getId())) {
				return this;
			}
		}
		this.applicationDocuments.add(applicationDocumentsItem);
		return this;

	}

}
