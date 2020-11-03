package org.egov.assets.repository.entity;

import java.math.BigDecimal;

import org.egov.assets.model.AuditDetails;
import org.egov.assets.model.PurchaseOrder;
import org.egov.assets.model.Store;
import org.egov.assets.model.Supplier;
import org.egov.assets.model.PurchaseOrder.PurchaseTypeEnum;
import org.egov.assets.model.PurchaseOrder.RateTypeEnum;
import org.egov.assets.model.PurchaseOrder.StatusEnum;

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
public class PurchaseOrderEntity {
	public static final String TABLE_NAME = "PurchaseOrder";
	private String id;
	private String tenantId;
	private String store;
	private String purchaseOrderNumber;
	private Long purchaseOrderDate;
	private String purchaseType;
	private String rateType;
	private String supplier;
	private String poCreatedBy;
	private BigDecimal advanceAmount;
	private BigDecimal totalAmount;
	private BigDecimal advancePercentage;
	private Long expectedDeliveryDate;
	private String deliveryTerms;
	private String paymentTerms;
	private String remarks;
	private String status;
	private String fileStoreId;
	private String designation;
	private Long stateId;
	private String createdBy;
	private String lastModifiedBy;
	private Long createdTime;
	private Long lastModifiedTime;
	private String externalPoNumber;

	public PurchaseOrder toDomain() {
		PurchaseOrder purchaseOrder = new PurchaseOrder();
		purchaseOrder.setId(this.id);
		purchaseOrder.setTenantId(this.tenantId);
		purchaseOrder.setStore(new Store().code(store));
		purchaseOrder.setPurchaseOrderNumber(this.purchaseOrderNumber);
		purchaseOrder.setAdvanceAmount(this.advanceAmount);
		purchaseOrder.setTotalAmount(this.totalAmount);
		purchaseOrder.setPoCreatedBy(poCreatedBy);
		AuditDetails auditDetail = new AuditDetails().createdBy(createdBy).lastModifiedBy(lastModifiedBy)
				.createdTime(createdTime).lastModifiedTime(lastModifiedTime);
		purchaseOrder.setAuditDetails(auditDetail);

		purchaseOrder.setAdvancePercentage(advancePercentage);
		purchaseOrder.setDeliveryTerms(deliveryTerms);
		purchaseOrder.setDesignation(designation);
		purchaseOrder.setExpectedDeliveryDate(expectedDeliveryDate);
		purchaseOrder.setFileStoreId(fileStoreId);
		purchaseOrder.setPaymentTerms(paymentTerms);
		purchaseOrder.setPurchaseOrderDate(purchaseOrderDate);

		purchaseOrder.setPurchaseType(PurchaseTypeEnum.fromValue(this.purchaseType));
		purchaseOrder.setRateType(RateTypeEnum.fromValue(rateType));
		purchaseOrder.setRemarks(remarks);
		purchaseOrder.setStateId(stateId);
		if (status != null)
			purchaseOrder.setStatus(StatusEnum.fromValue(status));
		purchaseOrder.setSupplier(new Supplier().code(supplier));

		purchaseOrder.setStateId(this.stateId);
		purchaseOrder.setExternalPoNumber(this.externalPoNumber);

		return purchaseOrder;
	}

	public PurchaseOrderEntity toEntity(PurchaseOrder po) {

		this.id = po.getId();
		this.tenantId = po.getTenantId();
		this.advanceAmount = po.getAdvanceAmount() != null ? po.getAdvanceAmount() : null;
		this.totalAmount = po.getTotalAmount() != null ? po.getTotalAmount() : null;
		this.advancePercentage = po.getAdvancePercentage() != null ? po.getAdvancePercentage() : null;
		this.deliveryTerms = po.getDeliveryTerms() != null ? po.getDeliveryTerms() : null;
		this.designation = po.getDesignation() != null ? po.getDesignation() : null;
		this.expectedDeliveryDate = po.getExpectedDeliveryDate();
		this.paymentTerms = po.getPaymentTerms() != null ? po.getPaymentTerms() : null;
		this.purchaseOrderDate = po.getPurchaseOrderDate();
		this.purchaseOrderNumber = po.getPurchaseOrderNumber() != null ? po.getPurchaseOrderNumber() : null;
		this.purchaseType = po.getPurchaseType() != null ? po.getPurchaseType().toString() : null;
		this.rateType = po.getRateType() != null ? po.getRateType().toString() : null;
		this.remarks = po.getRemarks();
		this.status = po.getStatus() != null ? po.getStatus().toString() : null;
		this.store = po.getStore() != null ? po.getStore().getCode() : null;
		this.supplier = po.getSupplier() != null ? po.getSupplier().getCode() : null;
		this.fileStoreId = po.getFileStoreId();
		this.designation = po.getDesignation();
		this.stateId = po.getStateId();
		this.createdBy = po.getAuditDetails().getCreatedBy();
		this.lastModifiedBy = po.getAuditDetails().getLastModifiedBy();
		this.createdTime = po.getAuditDetails().getCreatedTime();
		this.lastModifiedTime = po.getAuditDetails().getLastModifiedTime();
		this.externalPoNumber = po.getExternalPoNumber() != null ? po.getExternalPoNumber() : null;
		return this;
	}

}
