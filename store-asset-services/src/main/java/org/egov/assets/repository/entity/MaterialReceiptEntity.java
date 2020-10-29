package org.egov.assets.repository.entity;

import lombok.*;

import java.math.BigDecimal;
import java.util.Collections;

import org.egov.assets.model.AuditDetails;
import org.egov.assets.model.MaterialReceipt;
import org.egov.assets.model.Store;
import org.egov.assets.model.Supplier;

import static org.springframework.util.StringUtils.isEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialReceiptEntity {
	public static final String TABLE_NAME = "materialreceipt";
	public static final String SEQUENCE_NAME = "seq_materialreceipt";
	public static final String ALIAS = "materialreceipt";

	private String id;

	private String mrnNumber;

	private String issueNumber;

	private Long receiptDate;

	private String receiptType;

	private String financialYear;

	private String externalPoNumber;

	private String receiptPurpose;

	private String receivingStore;

	private String issueingStore;

	private String supplierCode;

	private String supplierBillNo;

	private Long supplierBillDate;

	private String challanNo;

	private Long challanDate;

	private String description;

	private String receivedBy;

	private String designation;

	private String bill;

	private String inspectedBy;

	private Long inspectionDate;

	private String mrnStatus;

	private String inspectionRemarks;

	private String receiptdetailsId;

	private BigDecimal totalReceiptValue;

	private String fileStoreId;

	private String createdBy;

	private Long createdTime;

	private String lastModifiedBy;

	private Long lastModifiedTime;

	private String tenantId;

	private String paymentTerms;

	private Boolean isAdHoc;

	public MaterialReceipt toDomain() {
		MaterialReceipt materialReceipt = new MaterialReceipt();

		return materialReceipt.id(id).mrnNumber(mrnNumber).issueNumber(issueNumber).receiptDate(receiptDate)
				.receiptType(MaterialReceipt.ReceiptTypeEnum.fromValue(receiptType)).financialYear(financialYear)
				.receiptPurpose(
						null != receiptPurpose ? MaterialReceipt.ReceiptPurposeEnum.fromValue(receiptPurpose) : null)
				.receivingStore(!isEmpty(receivingStore) ? buildStore(receivingStore) : null)
				.issueingStore(!isEmpty(issueingStore) ? buildStore(issueingStore) : null)
				.supplier(!isEmpty(supplierCode) ? buildSupplier() : null).supplierBillNo(supplierBillNo)
				.supplierBillDate(supplierBillDate).challanNo(challanNo).challanDate(challanDate)
				.description(description).receivedBy(receivedBy).designation(designation).bill(bill)
				.inspectedBy(inspectedBy).inspectionDate(inspectionDate)
				.mrnStatus(null != mrnStatus ? MaterialReceipt.MrnStatusEnum.fromValue(mrnStatus) : null)
				.inspectionRemarks(inspectionRemarks).receiptDetails(Collections.emptyList())
				.totalReceiptValue(totalReceiptValue).fileStoreId(fileStoreId).paymentTerms(paymentTerms)
				.tenantId(tenantId).isAdHoc(isAdHoc).auditDetails(buildAuditDetails());
	}

	public MaterialReceiptEntity toEntity(MaterialReceipt materialReceipt) {
		this.id = materialReceipt.getId();
		this.bill = materialReceipt.getBill();
		this.challanDate = materialReceipt.getChallanDate();
		this.challanNo = materialReceipt.getChallanNo();
		this.createdBy = materialReceipt.getAuditDetails() != null ? materialReceipt.getAuditDetails().getCreatedBy()
				: null;
		this.createdTime = materialReceipt.getAuditDetails() != null
				? materialReceipt.getAuditDetails().getCreatedTime()
				: null;
		this.description = materialReceipt.getDescription();
		this.designation = materialReceipt.getDesignation();
		this.fileStoreId = materialReceipt.getFileStoreId();
		this.financialYear = materialReceipt.getFinancialYear();
		this.inspectedBy = materialReceipt.getInspectedBy();
		this.inspectionDate = materialReceipt.getInspectionDate();
		this.inspectionRemarks = materialReceipt.getInspectionRemarks();
		this.issueingStore = materialReceipt.getIssueingStore() != null ? materialReceipt.getIssueingStore().getCode()
				: null;
		this.issueNumber = materialReceipt.getIssueNumber();
		this.lastModifiedBy = materialReceipt.getAuditDetails() != null
				? materialReceipt.getAuditDetails().getLastModifiedBy()
				: null;
		this.lastModifiedTime = materialReceipt.getAuditDetails() != null
				? materialReceipt.getAuditDetails().getLastModifiedTime()
				: null;
		this.mrnNumber = materialReceipt.getMrnNumber();
		this.mrnStatus = materialReceipt.getMrnStatus() != null ? materialReceipt.getMrnStatus().toString() : null;
		this.paymentTerms = materialReceipt.getPaymentTerms();
		this.receiptPurpose = materialReceipt.getReceiptPurpose() != null
				? materialReceipt.getReceiptPurpose().toString()
				: null;
		this.receiptType = materialReceipt.getReceiptType() != null ? materialReceipt.getReceiptType().toString()
				: null;
		this.receivedBy = materialReceipt.getReceivedBy();
		this.receivingStore = materialReceipt.getReceivingStore() != null
				? materialReceipt.getReceivingStore().getCode()
				: null;
		this.supplierBillDate = materialReceipt.getSupplierBillDate();
		this.supplierBillNo = materialReceipt.getSupplierBillNo();
		this.supplierCode = materialReceipt.getSupplier() != null ? materialReceipt.getSupplier().getCode() : null;
		this.totalReceiptValue = materialReceipt.getTotalReceiptValue();
		this.tenantId = materialReceipt.getTenantId();
		this.isAdHoc = materialReceipt.getIsAdHoc() != null ? materialReceipt.getIsAdHoc() : null;
		return this;
	}

	private Supplier buildSupplier() {
		Supplier supplier = new Supplier();
		return supplier.code(supplierCode);
	}

	private Store buildStore(String storeCode) {
		Store store = new Store();
		return store.code(storeCode);
	}

	private AuditDetails buildAuditDetails() {
		AuditDetails auditDetails = new AuditDetails();
		return auditDetails.createdBy(createdBy).createdTime(createdTime).lastModifiedBy(lastModifiedBy)
				.lastModifiedTime(lastModifiedTime);
	}
}
