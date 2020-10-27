package org.egov.ps.util;

public class PSConstants {

	public static final String TENANT_ID = "ch.chandigarh";

	public static final String PM_DRAFTED = "ES_DRAFTED";

	public static final String PM_APPROVED = "ES_APPROVED";

	public static final String ES_WF_DOCS = "WF_DOCS";

	public static final String ESTATE_BRANCH = "ESTATE_BRANCH";

	public static final String BUILDING_BRANCH = "BUILDING_BRANCH";

	public static final String MANIMAJRA_BRANCH = "MANIMAJRA_BRANCH";

	public static final String JSONPATH_PROPERTY_SERVICE = "$.MdmsRes.OwnershipTransferSaleDeed";

	public static final String MDMS_PS_MODULE_NAME = "EstateServices";

	public static final String MDMS_PS_FIELDS = "EstateBranch_OwnershipTransfer_SaleDeed";

	public static final String MDMS_PS_CODE_FILTER = "$.*.code";

	public static final String MDMS_PS_FIELD_FILTER = "[0].fields";
	
	public static final String MDMS_PS_FEES_FILTER = "[0].fees";
	
	public static final String MDMS_PS_FEE_GST_FILTER = "[0].feePercentGST";

	public static final String MDMS_PS_NOTIFICATIONS_FILTER = "[0].notifications";

	public static final String MDMS_DOCUMENT_FIELD_FILTER = ".*.documentList.*";

	public static final String EB_OT_SD = "EstateBranch_OwnershipTransfer_SaleDeed";

	public static final String EB_OT_RW = "EstateBranch_OwnershipTransfer_RegisteredWill";

	public static final String EB_OC_LH_FH = "EstateBranch_OtherCitizen_LeaseholdToFreehold";

	public static final String EB_OC_SCF_SCO = "EstateBranch_OtherCitizen_ScfToSco";

	public static final String EB_IS_AOS = "EstateBranch_InternalServices_AllotmentOfSite";

	public static final String MDMS_PS_MORTGAGE_FILTER = ".*.documentList.*";

	public static final String EM_ACTION_APPROVE = "Approve";

	public static final String EM_STATE_PENDING_DA_FEE = "PENDING_DA_FEE";

	public static final String TAX_HEAD_CODE_APPLICATION_CHARGE = "APPLICATION";

	public static final String ESTATE_SERVICE = "ESTATE_SERVICE";

	public static final String ES_DRAFT = "DRAFT";

	public static final String ES_APPROVE = "APPROVE";

	public static final String ROLE_EMPLOYEE = "EMPLOYEE";
}
