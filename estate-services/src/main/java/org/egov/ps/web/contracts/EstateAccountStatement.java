package org.egov.ps.web.contracts;



import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class EstateAccountStatement {
    /*
     * Demand/Payment generation date
     */
    private long date;
    /*
     * Demand/Payment amount
     */
    private double amount;

    private Type type;
    private double remainingPrincipal;

    private double remainingInterest;

    public double getRemainingInterest() {
        return Math.max(0, this.remainingInterest - this.remainingBalance);
    }

    @JsonProperty("remainingBalance")
    private double remainingBalance;

    public double getRemainingBalance() {
        return Math.max(0, this.remainingBalance - this.remainingPrincipal - this.remainingInterest);
    }
   
    
    @JsonProperty("remainingGST")
    private double remainingGST;
    
    @JsonProperty("remainingRentPenalty")
    private double remainingRentPenalty;
    
    @JsonProperty("remainingGSTPenalty")
    private double remainingGSTPenalty;
    
    /**
	 * Receipt no of the payment
	 */
	@JsonProperty("receiptNo")
	private String receiptNo;
	
	
	 /**
		 * Adjustment Date  of the demand
	 */
	@JsonProperty("adjustmentDate")
	private long adjustmentDate;
	
	 /**
	 * Adjustment Date  of the demand
     */
     @JsonProperty("comment")
     private String comment;
	
	@Builder.Default
	private double rent = 0D;
	
	@Builder.Default
	private double collectedRent = 0D;
	
	@Builder.Default
	private double gst = 0D;
	
	@Builder.Default
	private double collectedGST = 0D;
	
	@Builder.Default
	private double rentPenalty = 0D;
	
	@Builder.Default
	private double collectedRentPenalty = 0D;
	
	@Builder.Default
	private double GSTPenalty = 0D;
	
	@Builder.Default
	private double collectedGSTPenalty = 0D;


    public double getDueAmount() {
    	return Math.max(0, this.remainingPrincipal + this.remainingGST+this.remainingRentPenalty+this.remainingGSTPenalty - this.remainingBalance);
    }

    public enum Type {
        C("C"), D("D");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static String fromValue(String text) {
            for (Type b : Type.values()) {
                if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                    return b.value;
                }
            }
            return null;
        }
    }

    @JsonIgnore
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
    
    @JsonProperty("isPrevious") 
	private Boolean isPrevious=false;

    @Override
    public String toString() {
        return String.format("%s\tamount=%.2f\t%s\tprincipalDue=%.2f\tinterestDue=%.2f\ttotalDue=%.2f\tbalance=%.2f",
                dateFormat.format(new Date(this.date)), this.amount, this.type == Type.C ? "Payment" : "Rent",
                this.getRemainingPrincipal(), this.getRemainingInterest(), this.getDueAmount(),
                this.getRemainingBalance());
    }
}
