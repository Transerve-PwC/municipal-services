package org.egov.pm.util;

public class CommonConstants {

	/* No args Constructor */
	private CommonConstants() {
	}

	// Status Name
	public static final String APPLY = "APPLY";
	public static final String INITIATE = "INITIATE";
	public static final String DRAFT = "DRAFT";
	public static final String PAY = "PAY";
	public static final String APPLIED = "APPLIED";
	public static final String SYSTEMPAYMENT = "SYSTEM_PAYMENT";
	public static final String REFUNDSUCCESS = "SUCCESSFUL";

	public static final String INSERT = "I";
	public static final String UPDATE = "U";

	// Error Messages
	public static final String SAVEERROR = "Unable to Save the record.";
	public static final String GETERROR = "Unable to Get the record.";
	public static final String UPDATEERROR = "Unable to Update the record.";
	public static final String UPDATESTATUSERROR = "Unable to Update Status the record.";

	// Columns Name
	public static final String APPLICANTNAME = "applicantName";
	public static final String APPLICANTUUID = "application_uuid";
	public static final String HOUSENO = "houseNo";
	public static final String SECTOR = "sector";
	public static final String APPLICATIONDETAIL = "application_detail";
	public static final String APPLICATIONDETAILUUID = "application_detail_uuid";
	public static final String APPLICATIONUUID = "application_uuid";
	public static final String CREATEDBY = "created_by";
	public static final String ISACTIVE = "is_active";
	public static final String LASTMODIFIEDBY = "last_modified_by";
	public static final String LASTMODIFIEDTIME = "last_modified_time";
	public static final String TENANTID = "tenantid";
	public static final String STATUSAPPUUID="applicationuuid";
	public static final String REMARKS = "remarks";
	public static final String AMOUNT = "amount";
	public static final String BADGENUMBER = "badgeNumber";
	public static final String GSTAMOUNT = "gstAmount";
	public static final String PERFORMANCEBANKGUARANTEECHARGES = "performanceBankGuaranteeCharges";
	public static final String FILESTOREID="fileStoreId";
	public static final String BADGENUMBERDB = "badgenumber";
	public static final String GSTAMOUNTDB = "gstamount";
	public static final String TOTALAMOUNT = "totalamount";
	public static final String INVALIDAPPID="Invalid application Id";
	public static final String EMAILAPPID="[:applicationId:]";
	public static final String PERFORMANCEBANKGUARANTEECHARGESDB = "performancebankguaranteecharges";
	public static final String WITHDRAWAPPROVALAMOUNT="withdrawapprovalamount";
	public static final String WITHDRAWAPPROVALTAXAMOUNT="withdrawapprovaltaxamount";
	public static final String CREATEDBYDB = "createdby";

	public static final String WITHDRAWAPPROVALAMOUNTERRORCODE="WITHDRAW_AMOUNT_EXCEPTION";
	public static final String WITHDRAWAPPROVALAMOUNTERROR="Withdraw amount should be less than actual amount";
	public static final String WITHDRAWAPPROVALTAXAMOUNTERRORCODE="WITHDRAW_TAXAMOUNT_EXCEPTION";
	public static final String WITHDRAWAPPROVALTAXAMOUNTERROR="Withdraw tax amount should be less than actual tax amount";



	public static final String APPLICATIONID = "applicationId";
	public static final String DOCUMENTDETAIL = "documentDetail";
	
	
	public static final String CATEGORYID = "categoryId";
	public static final String SUBCATEGORYID = "subCategoryId";
	public static final String FROMDATE = "effectiveFromDate";
	public static final String TODATE = "effectiveFromDate";
	public static final String MINSQFT = "minSqFt";
	public static final String MAXSQFT = "maxSqFt";
	public static final String PER_DAY_PRICE = "perDayPrice";
	public static final String PER_WEEK_PRICE = "perWeekPrice";
	public static final String PER_MONTH_PRICE = "perMonthPrice";
	public static final String ANNUAL_PRICE = "annualPrice";
	public static final String FIXED_PRICE = "fixedPrice";
	public static final String PRICE_BOOK_ID = "priceBookId";

	public static final String CALCULATION_SEQUENCE = "calculation_sequence";
	public static final String CALCULATION_TYPE = "calculation_type";
	public static final String MINVALUESQFT = "min_sqft";
	public static final String MAXVALUESQFT = "max_sqft";

	
	
	
	
	public static final String SUCCESS = "success";
	public static final String FAIL = "Fail";
	public static final String SUCCESSFUL = "successful";
	public static final String UNABLETOPROCESSREQUEST = "Unable to Process request : ";
}
