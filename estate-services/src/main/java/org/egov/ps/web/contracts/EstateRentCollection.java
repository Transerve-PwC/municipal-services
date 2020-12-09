package org.egov.ps.web.contracts;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode
public class EstateRentCollection {
	/**
	 * Unique id of the collection
	 */
	@JsonProperty("id")
	private String id;

	/**
	 * The timestamp at which this collection was made. This is not same as
	 * createdAt.
	 */
	private long collectedAt;

	/**
	 * Demand Id of the demand that this fulfils.
	 */
	@ToString.Include
	@JsonProperty("demandId")
	private String demandId;

	/**
	 * Payment Id of the payment that this fulfils.
	 */
	@ToString.Include
	@JsonProperty("paymentId")
	private String paymentId;

	/**
	 * Interest collected.
	 */
	@Builder.Default
	@ToString.Include
	@JsonProperty("interestCollected")
	private Double interestCollected = 0D;

	/**
	 * Principal collected.
	 */
	@Builder.Default
	@ToString.Include
	@JsonProperty("rentCollected")
	private Double rentCollected = 0D;
	
	@Builder.Default
	@ToString.Include
	@JsonProperty("gstCollected")
	private Double gstCollected = 0D;
	
	@Builder.Default
	@ToString.Include
	@JsonProperty("gstPenaltyCollected")
	private Double gstPenaltyCollected = 0D;

	@Builder.Default
	@ToString.Include
	@JsonProperty("rentPenaltyCollected")
	private Double rentPenaltyCollected = 0D;


	@Builder.Default
	@ToString.Include
	@JsonProperty("rentWithGST")
	private Double rentWithGST = 0D;
	
	@Builder.Default
	@ToString.Include
	@JsonProperty("rentPenaltyWithGSTPenalty")
	private Double rentPenaltyWithGSTPenalty = 0D;


	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

}

