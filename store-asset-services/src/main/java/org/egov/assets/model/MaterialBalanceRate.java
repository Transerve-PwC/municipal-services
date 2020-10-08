package org.egov.assets.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This object holds the Material Balance AND Rate information.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialBalanceRate {

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("receiptId")
	private String receiptId;

	@JsonProperty("receiptDetailId")
	private String receiptDetailId;

	@JsonProperty("mrnNumber")
	private String mrnNumber;

	@JsonProperty("issueStoreCode")
	private String issueStoreCode;

	@JsonProperty("materialCode")
	private String materialCode;

	@JsonProperty("materialName")
	private String materialName;

	@JsonProperty("uomCode")
	private String uomCode;

	@JsonProperty("balance")
	private BigDecimal balance;

	@JsonProperty("unitRate")
	private BigDecimal unitRate;
	
	@JsonProperty("receiptDate")
	private String receiptDate;


	@Override
	public String toString() {
		return "MaterialBalanceRate [tenantId=" + tenantId + ", receiptId=" + receiptId + ", receiptDetailId="
				+ receiptDetailId + ", mrnNumber=" + mrnNumber + ", issueStoreCode=" + issueStoreCode
				+ ", materialCode=" + materialCode + ", materialName=" + materialName + ", uomCode=" + uomCode
				+ ", balance=" + balance + ", unitRate=" + unitRate + ", receiptDate=" + receiptDate + "]";
	}

}
