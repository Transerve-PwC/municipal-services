package org.egov.ps.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentConfigItems {

    /**
     * Unique id of the payment
     */
    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("paymentConfigId")
    private String paymentConfigId;

    @JsonProperty("groundRentAmount")
    private BigDecimal groundRentAmount;

    @JsonProperty("groundRentStartMonth")
    private Long groundRentStartMonth;

    @JsonProperty("groundRentEndMonth")
    private Long groundRentEndMonth;
}
