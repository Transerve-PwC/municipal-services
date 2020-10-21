package org.egov.ps.model;

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
public class RentAccountStatement {
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
    
    /**
	 * Receipt no of the payment
	 */
	@JsonProperty("receiptNo")
	private String receiptNo;

    public double getRemainingBalance() {
        return Math.max(0, this.remainingBalance - this.remainingInterest);
    }

    public double getDueAmount() {
        return Math.max(0, this.remainingPrincipal + this.remainingInterest - this.remainingBalance);
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
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");

    @Override
    public String toString() {
        return String.format("%s\tamount=%.2f\t%s\tprincipalDue=%.2f\tinterestDue=%.2f\ttotalDue=%.2f\tbalance=%.2f",
                dateFormat.format(new Date(this.date)), this.amount, this.type == Type.C ? "Payment" : "Rent",
                this.getRemainingPrincipal(), this.getRemainingInterest(), this.getDueAmount(),
                this.getRemainingBalance());
    }
}