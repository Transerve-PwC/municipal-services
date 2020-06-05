Create table materialreceipt( 
	id varchar(50),
	mrnNumber varchar(50),
	receiptDate bigint,
	receiptType varchar(21),
	financialYear varchar(128) ,
	receiptPurpose varchar(19),
	receivingStore varchar(50),
	issueingStore varchar(50),
	supplierCode varchar(50),
	supplierBillNo varchar(50),
	supplierBillDate bigint,
	challanNo boolean,
	challanDate bigint,
	description varchar(512),
	receivedBy varchar(50),
	designation varchar(50),
	bill varchar(50),
	inspectedBy varchar(50),
	inspectionDate bigint,
	mrnStatus character varying(64),
	inspectionRemarks varchar(50),
	receiptDetailsId character varying(256),
	totalReceiptValue numeric (13,2),
	fileStoreId varchar(50),
	createdby varchar(50),
	createdTime bigint,
	lastmodifiedby varchar(50),
	lastModifiedTime bigint,
	tenantId varchar(250),
	version bigint
);
alter table materialreceipt add constraint pk_materialreceipt primary key (mrnNumber,tenantId);
create sequence seq_materialreceipt;

