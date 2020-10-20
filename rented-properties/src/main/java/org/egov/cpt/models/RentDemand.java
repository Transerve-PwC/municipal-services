package org.egov.cpt.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.validation.constraints.Size;

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
public class RentDemand implements Comparable<RentDemand> {

  /**
   * Unique id of the demand
   */
  @Size(max = 256)
  @JsonProperty("id")
  private String id;

  /**
   * Property that this rent is generated for.
   */
  @Size(max = 256)
  @JsonProperty("propertyId")
  private String propertyId;
  /**
   * No of days of grace period before interest starts getting applied.
   */
  @Builder.Default
  @JsonProperty("initialGracePeriod")
  private int initialGracePeriod = 10;

  /**
   * Date of generation of this demand.
   */
  @JsonProperty("generationDate")
  private Long generationDate;

  /**
   * The principal rent amount that is to be collected
   */
  @JsonProperty("collectionPrincipal")
  private Double collectionPrincipal;

  /**
   * The remaining principal that still has to be collected.
   */
  @Builder.Default
  @JsonProperty("remainingPrincipal")
  private Double remainingPrincipal = 0.0;

  /**
   * Last date on which interest was made as 0.
   */
  @JsonProperty("interestSince")
  private Long interestSince;

  @Size(max = 64)
  @JsonProperty("mode")
  @Builder.Default
  private ModeEnum mode = ModeEnum.UPLOAD;

  @Size(max = 64)
  @JsonProperty("status")
  @Builder.Default
  private PaymentStatusEnum status = PaymentStatusEnum.UNPAID;

  @JsonProperty("auditDetails")
  @Builder.Default
  private AuditDetails auditDetails = null;

  @Override
  public int compareTo(RentDemand other) {
    return this.getGenerationDate().compareTo(other.getGenerationDate());
  }

  public boolean isPaid() {
    return this.status == PaymentStatusEnum.PAID;
  }

  public boolean isUnPaid() {
    return !this.isPaid();
  }

  public void setRemainingPrincipalAndUpdatePaymentStatus(Double d) {
    this.setRemainingPrincipal(d);
    if (this.remainingPrincipal == 0) {
      this.status = PaymentStatusEnum.PAID;
    } else {
      this.status = PaymentStatusEnum.UNPAID;
    }
  }

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd yy");

  public String toString() {
    return String.format("Collection: %.2f, remaining: %.2f, remainingSince: %s, generatedOn: %s",
        this.collectionPrincipal, this.remainingPrincipal, DATE_FORMAT.format(this.interestSince),
        DATE_FORMAT.format(this.generationDate));
  }

}
