package org.egov.ps.web.contracts;




import org.egov.ps.web.contracts.PaymentStatusEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class EstateDemand implements Comparable<EstateDemand>{

  /**
   * Unique id of the demand
   */
  @JsonProperty("id")
  private String id;
  
  /**
   * Property that this rent is generated for.
   */
  @JsonProperty("propertyId")
  private String propertyId;
  
  /**
   * No of days of grace period before interest starts getting applied.
   */
  @Builder.Default
  @JsonProperty("initialGracePeriod")
  private int initialGracePeriod = 10;

  
  
  /**
   * Date of demand.
   */
  @JsonProperty("demandDate")
  private Long demandDate;
  
  @JsonProperty("isPrevious")
  private Boolean isPrevious;

  /**
   * Rent of demand.
   */
  @JsonProperty("rent")
  private Double rent;
  
  /**
   * Penalty Interest of demand.
   */
  @JsonProperty("penaltyInterest")
  private Double penaltyInterest;
  
  /**
   * Gst Interest of demand.
   */
  @JsonProperty("gstInterest")
  private Double gstInterest;
  
  /**
   * GST of demand.
   */
  @JsonProperty("gst")
  private Integer gst;
  
  /**
   * Collected Rent of demand.
   */
  @JsonProperty("collectedRent")
  private Double collectedRent;
  
  /**
   * Collected GST of demand.
   */
  @JsonProperty("collectedGST")
  private Double collectedGST;
  
  /**
   * Collected Rent Penalty of demand.
   */
  @JsonProperty("collectedRentPenalty")
  private Double collectedRentPenalty;
  
  /**
   * Collected STt Penalty of demand.
   */
  @JsonProperty("collectedGSTPenalty")
  private Double collectedGSTPenalty;
  
  
  /**
   * No of days of demand.
   */
  @JsonProperty("noOfDays")
  private Double noOfDays;
  
  /**
   * paid of demand.
   */
  @JsonProperty("paid")
  private Double paid;
  
  @JsonProperty("status")
  @Builder.Default
  private PaymentStatusEnum status = PaymentStatusEnum.UNPAID;

  
  public boolean isPaid() {
	    return this.status == PaymentStatusEnum.PAID;
	  }
  
  public boolean isUnPaid() {
	    return !this.isPaid();
	  }
  
  /**
   * Last date on which payment made
   */
  @JsonProperty("paymentSince")
  private Long paymentSince;

  @Override
  public int compareTo(EstateDemand other) {
    return this.getDemandDate().compareTo(other.getDemandDate());
  }
 
}
