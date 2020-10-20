package org.egov.cpt.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillV2 {

	@JsonProperty("id")
	@Size(max = 64)
	private String id;

	@JsonProperty("mobileNumber")
	@Size(min = 10, max = 10)
	private String mobileNumber;

	@JsonProperty("payerName")
	@Size(max = 256)
	private String payerName;

	@JsonProperty("payerAddress")
	@Size(max = 1024)
	private String payerAddress;

	@JsonProperty("payerEmail")
	@Size(max = 256)
	private String payerEmail;

	@JsonProperty("status")
	@Size(max = 64)
	private StatusEnum status;

	@JsonProperty("totalAmount")
	private BigDecimal totalAmount;

	@JsonProperty("businessService")
	@Size(max = 250)
	private String businessService;

	@JsonProperty("billNumber")
	@Size(max = 1024)
	private String billNumber;
	
	@JsonProperty("billDate")
	private Long billDate;

	@JsonProperty("consumerCode")
	@Size(max = 250)
	private String consumerCode;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("billDetails")
	@Valid
	private List<BillDetailV2> billDetails;

	@JsonProperty("tenantId")
	@Size(max = 250)
	private String tenantId;

	@JsonProperty("fileStoreId")
	@Size(max = 256)
	private String fileStoreId;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;
	
	/**
	 * status of the bill .
	 */
	public enum StatusEnum {
		
		ACTIVE("ACTIVE"),

		CANCELLED("CANCELLED"),

		PAID("PAID"),
		
		PARTIALLY_PAID ("PARTIALLY_PAID"),
		
		PAYMENT_CANCELLED ("PAYMENT_CANCELLED"),

		EXPIRED("EXPIRED");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static StatusEnum fromValue(String text) {
			for (StatusEnum b : StatusEnum.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}

	public BillV2 addBillDetailsItem(BillDetailV2 billDetailsItem) {
		if (this.billDetails == null) {
			this.billDetails = new ArrayList<>();
		}
		this.billDetails.add(billDetailsItem);
		return this;
	}

}
