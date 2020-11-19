package org.egov.ps.model;

import java.math.BigDecimal;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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
public class OfflinePaymentDetails {

	@Size(max = 256)
	@JsonProperty("id")
	private String id;

	@Size(max = 256)
	@JsonProperty("propertyDetailsId")
	private String propertyDetailsId;

	@Size(max = 256)
	@JsonProperty("demandId")
	private String demandId;

	@JsonProperty("amount")
	private BigDecimal amount;

	@Size(max = 100)
	@JsonProperty("bankName")
	private String bankName;

	@Size(max = 100)
	@JsonProperty("transactionNumber")
	private String transactionNumber;

	@JsonProperty("dateOfPayment")
	private Long dateOfPayment;

	private OfflinePaymentType type;

	public enum OfflinePaymentType {
		RENT("rent"), PENALTY("penalty"), SECURITY("security_deposit");

		private String value;

		OfflinePaymentType(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static OfflinePaymentType fromValue(String text) {
			for (OfflinePaymentType b : OfflinePaymentType.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}

}
