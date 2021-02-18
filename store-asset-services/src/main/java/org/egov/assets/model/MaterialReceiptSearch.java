package org.egov.assets.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaterialReceiptSearch {

	private List<String> ids;

	private boolean forprint;

	private List<String> mrnNumber;

	private List<String> mrnStatus;

	private List<String> issueNumber;

	private Long receiptDate;

	private List<String> receiptType;

	private String receiptPurpose;

	private String receivingStore;

	private String issueingStore;

	List<String> materials;

	private String supplierCode;

	private Boolean supplierBillPaid;

	private String receivedBy;

	private String materialTypeName;

	private String storeName;

	private String financialYear;
	
	private String asOnDate;

	private String tenantId;

	private String sortBy;

	private Integer pageSize;

	private Integer pageNumber;

	private Integer offset;
}
