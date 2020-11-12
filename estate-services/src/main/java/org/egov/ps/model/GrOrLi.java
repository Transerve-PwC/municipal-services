package org.egov.ps.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GrOrLi {

    /**
     * Unique id of the payment
     */
    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

	@JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("grOrLiGenerationType")
    private String grOrLiGenerationType;

    @JsonProperty("grOrLiAdvanceRent")
    private BigDecimal grOrLiAdvanceRent;

    @JsonProperty("grOrLiBillStartDate")
    private Long grOrLiBillStartDate;

    @JsonProperty("grOrLiAdvanceRentDate")
    private Long grOrLiAdvanceRentDate;

    @JsonProperty("grOrLiAmount")
    private BigDecimal grOrLiAmount;

    @JsonProperty("grOrLiStartMonth")
    private Long grOrLiStartMonth;

    @JsonProperty("grOrLiEndMonth")
    private Long grOrLiEndMonth;
}
