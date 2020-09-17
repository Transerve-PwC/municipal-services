package org.egov.assets.repository.entity;

import java.math.BigDecimal;

import org.egov.assets.model.MaterialBalanceRate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialBalanceRateEntity {

	private String tenantId;

	private String receiptId;

	private String receiptDetailId;

	private String mrnNumber;

	private String issueStoreCode;

	private String materialCode;

	private String uomCode;

	private BigDecimal balance;

	private BigDecimal unitRate;

	private String receiptDate;

	public MaterialBalanceRate toDomain() {
		MaterialBalanceRate materialBalanceRate = new MaterialBalanceRate();
		materialBalanceRate.setTenantId(tenantId);
		materialBalanceRate.setReceiptId(receiptId);
		materialBalanceRate.setReceiptDetailId(receiptDetailId);
		materialBalanceRate.setMrnNumber(mrnNumber);
		materialBalanceRate.setIssueStoreCode(issueStoreCode);
		materialBalanceRate.setMaterialCode(materialCode);
		materialBalanceRate.setUomCode(uomCode);
		materialBalanceRate.setBalance(balance);
		materialBalanceRate.setUnitRate(unitRate);
		materialBalanceRate.setReceiptDate(receiptDate);
		return materialBalanceRate;
	}

	public MaterialBalanceRateEntity toEntity(MaterialBalanceRate materialBalanceRate) {
		this.receiptId = materialBalanceRate.getReceiptId();
		this.receiptDetailId = materialBalanceRate.getReceiptDetailId();
		this.tenantId = materialBalanceRate.getTenantId();
		this.mrnNumber = materialBalanceRate.getMrnNumber();
		this.issueStoreCode = materialBalanceRate.getIssueStoreCode();
		this.materialCode = materialBalanceRate.getMaterialCode();
		this.uomCode = materialBalanceRate.getUomCode();
		this.balance = materialBalanceRate.getBalance();
		this.unitRate = materialBalanceRate.getUnitRate();
		this.receiptDate=materialBalanceRate.getReceiptDate();
		return this;
	}

}
