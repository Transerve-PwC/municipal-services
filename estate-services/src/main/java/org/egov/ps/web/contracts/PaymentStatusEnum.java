package org.egov.ps.web.contracts;



import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatusEnum {
    UNPAID("Unpaid"), PARTIALLY_PAID("PartiallyPaid"), PAID("Paid");

    private String value;

    PaymentStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static PaymentStatusEnum fromValue(String text) {
        for (PaymentStatusEnum b : PaymentStatusEnum.values()) {
            if (String.valueOf(b.value).equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}

