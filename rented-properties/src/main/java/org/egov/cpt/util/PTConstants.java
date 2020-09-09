package org.egov.cpt.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PTConstants {

	private PTConstants() {
	}

	public static final String ROLE_EMPLOYEE = "EMPLOYEE";

	public static final String ROLE_CITIZEN = "CITIZEN";

	public static final String businessService_csp = "CSP";

	public static final String MDMS_PT_MOD_NAME = "RentedProperties";

	public static final String JSONPATH_CODES = "$.MdmsRes." + MDMS_PT_MOD_NAME;

	public static final String MDMS_PT_PROPERTYTYPE = "PropertyType";

	public static final String MDMS_PT_USAGECATEGORY = "UsageCategory";

	public static final String MDMS_PT_PROPERTYSUBTYPE = "PropertySubType";

	public static final String MDMS_PT_OCCUPANCYTYPE = "OccupancyType";

	public static final String MDMS_PT_CONSTRUCTIONTYPE = "ConstructionType";

	public static final String MDMS_PT_CONSTRUCTIONSUBTYPE = "ConstructionSubType";

	public static final String MDMS_PT_OWNERSHIP = "OwnerShipCategory";

	public static final String MDMS_PT_SUBOWNERSHIP = "SubOwnerShipCategory";

	public static final String MDMS_PT_USAGEMAJOR = "UsageCategoryMajor";

	public static final String MDMS_PT_USAGEMINOR = "UsageCategoryMinor";

	public static final String MDMS_PT_USAGEDETAIL = "UsageCategoryDetail";

	public static final String MDMS_PT_USAGESUBMINOR = "UsageCategorySubMinor";

	public static final String MDMS_PT_OWNERTYPE = "OwnerType";

	public static final String MDMS_PT_EGF_MASTER = "egf-master";

	public static final String MDMS_PT_FINANCIALYEAR = "FinancialYear";

	public static final String MDMS_PT_COLONY = "colonies";

	public static final String JSONPATH_FINANCIALYEAR = "$.MdmsRes.egf-master";

	public static final String BOUNDARY_HEIRARCHY_CODE = "REVENUE";

	// payment
	public static final String BUSINESS_SERVICE_PM = "MasterRP";

	public static final String BUSINESS_SERVICE_OT = "OwnershipTransferRP";

	public static final String BILLING_BUSINESS_SERVICE_OT = "RentedProperties.OwnershipTransfer";

	public static final String BILLING_BUSINESS_SERVICE_DC = "RentedProperties.DuplicateAllotmentLetter";

	public static final String TAX_HEAD_CODE_APPLICATION_CHARGE = "APPLICATION";

	public static final String TAX_HEAD_CODE_PUBLICATION_CHARGE = "PUBLICATION";

	public static final String BILLING_BUSINESS_SERVICE_RENT = "RentedProperties.Rent";

	public static final String BUSINESS_SERVICE_DC = "DuplicateCopyOfAllotmentLetterRP";

	public static final String BUSINESS_SERVICE_MG = "PermissionToMortgage";

	public static final String ACTION_INITIATE = "INITIATE";

	public static final String ACTION_APPLY = "APPLY";

	public static final String ACTION_APPROVE = "APPROVE";

	public static final String ACTION_REJECT = "REJECT";

	public static final String ACTION_REINITIATE = "REINITIATE";

	public static final String ACTION_DRAFT = "REINITIATE";

	public static final String TRIGGER_NOWORKFLOW = "NOWORKFLOW";

	public static final String ACTION_CANCEL = "CANCEL";

	public static final String ACTION_ADHOC = "ADHOC";

	public static final String STATUS_INITIATED = "INITIATED";

	public static final String STATUS_APPLIED = "APPLIED";

	public static final String STATUS_APPROVED = "APPROVED";

	public static final String STATUS_REJECTED = "REJECTED";

	public static final String STATUS_FIELDINSPECTION = "FIELDINSPECTION";

	public static final String STATUS_CANCELLED = "CANCELLED";

	public static final String STATUS_PAID = "PAID";

	public static final String BILL_AMOUNT_JSONPATH = "$.billResponse.Bill[0].totalAmount";

	public static final String MODULE = "rainmaker-tl";

	public static final String NOTIFICATION_LOCALE = "en_IN";

	public static final String NOTIFICATION_CREATE_CODE = "pt.property.en.create";

	public static final String NOTIFICATION_UPDATE_CODE = "pt.property.en.update";

	public static final String NOTIFICATION_EMPLOYEE_UPDATE_CODE = "pt.property.en.update.employee";

	public static final String NOTIFICATION_PAYMENT_ONLINE = "pt.payment.online";

	public static final String NOTIFICATION_PAYMENT_OFFLINE = "pt.payment.offline";

	public static final String NOTIFICATION_PAYMENT_FAIL = "pt.payment.fail";

	public static final String NOTIFICATION_OLDPROPERTYID_ABSENT = "pt.oldpropertyid.absent";

	public static final String ACTION_PAY = "PAY";

	public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
	public static final String USREVENTS_EVENT_NAME = "Property Tax";
	public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-PT";

	public static final String BUSINESS_SERVICE_MG_RP = "MortgageRP";

	public static final String BUSINESS_SERVICE_DC_RP = "DuplicateCopyOfAllotmentLetterRP";

	public static final String BUSINESS_SERVICE_FL_RP = "FRESHLICENSE";

	// Variable names for diff

	public static final String VARIABLE_ACTION = "action";

	public static final String VARIABLE_WFDOCUMENTS = "wfDocuments";

	public static final String VARIABLE_ACTIVE = "active";

	public static final String VARIABLE_USERACTIVE = "status";

	public static final String VARIABLE_CREATEDBY = "createdBy";

	public static final String VARIABLE_LASTMODIFIEDBY = "lastModifiedBy";

	public static final String VARIABLE_CREATEDTIME = "createdTime";

	public static final String VARIABLE_LASTMODIFIEDTIME = "lastModifiedTime";

	public static final String VARIABLE_OWNER = "ownerInfo";

	public static final List<String> FIELDS_TO_IGNORE = Collections
			.unmodifiableList(Arrays.asList(VARIABLE_ACTION, VARIABLE_WFDOCUMENTS, VARIABLE_CREATEDBY,
					VARIABLE_LASTMODIFIEDBY, VARIABLE_CREATEDTIME, VARIABLE_LASTMODIFIEDTIME));

	public static final List<String> FIELDS_FOR_OWNER_MUTATION = Collections
			.unmodifiableList(Arrays.asList("name", "gender", "fatherOrHusbandName"));

	public static final List<String> FIELDS_FOR_PROPERTY_MUTATION = Collections.unmodifiableList(
			Arrays.asList("propertyType", "usageCategory", "ownershipCategory", "noOfFloors", "landArea"));

	// OwnershipTransfer ACTION
	public static final String ACTION_OT_SUBMIT = "SUBMIT";
	public static final String ACTION_OT_REJECT = "REJECT";
	public static final String ACTION_OT_SENDBACK = "SENDBACK";
	public static final String ACTION_OT_APPROVE = "APPROVE";
	public static final String ACTION_OT_PAY = "PAY";

	// OwnershipTransfer STATE
	public static final String STATE_OT_PENDING_CLARIFICATION = "PENDINGCLARIFICATION";
	public static final String STATE_OT_INITIATED = "INITIATED";
	public static final String STATE_OT_APPROVED = "APPROVED";

	// OwnershipTransfer Notifications
	public static final String NOTIFICATION_OT_SUBMIT = "rp.en.counter.submit";
	public static final String NOTIFICATION_OT_REJECTED = "rp.en.counter.rejected";
	public static final String NOTIFICATION_OT_SENDBACK = "rp.en.counter.sendback";
	public static final String NOTIFICATION_OT_PAYMENT = "rp.en.counter.payment";
	public static final String NOTIFICATION_OT_PAYMENT_SUCCESS = "rp.en.counter.paymentsuccess";
	public static final String NOTIFICATION_OT_PAYMENT_SUCCESS_PAYER = "rp.en.counter.paymentsuccesspayer";
	public static final String NOTIFICATION_OT_APPROVED = "rp.en.counter.approved";
	public static final String OWNERSHIP_TRANSFER_APPLICATION = "Ownership Transfer request";
	public static final String DUPLICATE_COPY_APPLICATION = "Duplicate Copy request";
	public static final String MORTGAGE_APPLICATION = "Mortgage request";
	public static final String NOTIFICATION_NG_VIOLATION = "rp.en.counter.violationnoticegenerated";
	public static final String NOTIFICATION_NG_RECOVERY = "rp.en.counter.recoverynoticegenerated";

	// Ownership Transfer ACTION_STATUS combinations for notification

	public static final String OT_ACTION_STATUS_INITIATED = "INITIATE_OT_DRAFTED";
	public static final String OT_ACTION_STATUS_SUBMIT = "SUBMIT_OT_PENDINGCLVERIFICATION";
	public static final String OT_ACTION_STATUS_REJECTED = "REJECT_OT_REJECTED";
	public static final String OT_ACTION_STATUS_SENDBACK = "SENDBACK_OT_PENDINGCLARIFICATION";
	public static final String OT_ACTION_STATUS_PAYMENT = "SENDFORPAYMENT_OT_PENDINGPAYMENT";
	public static final String OT_ACTION_STATUS_PAYMENT_SUCCESS = "PAY_OT_APPROVED";
	public static final String OT_ACTION_STATUS_APPROVED = "APPROVE_OT_APPROVED";
	public static final String OT_ACTION_STATUS_REJECTED_AFTER_PAYMENT = "REJECT_OT_REJECTEDPAID";

	// Duplicate Copy ACTION_STATUS combinations for notification

	public static final String DC_ACTION_STATUS_INITIATED = "INITIATE_DC_DRAFTED";
	public static final String DC_ACTION_STATUS_SUBMIT = "SUBMIT_DC_PENDINGCLVERIFICATION";
	public static final String DC_ACTION_STATUS_REJECTED = "REJECT_DC_REJECTED";
	public static final String DC_ACTION_STATUS_SENDBACK = "SENDBACK_DC_PENDINGCLARIFICATION";
	public static final String DC_ACTION_STATUS_PAYMENT = "SENDFORPAYMENT_DC_PENDINGPAYMENT";
	public static final String DC_ACTION_STATUS_PAYMENT_SUCCESS = "PAY_DC_APPROVED";
	public static final String DC_ACTION_STATUS_APPROVED = "APPROVE_DC_APPROVED";
	public static final String DC_ACTION_STATUS_REJECTED_AFTER_PAYMENT = "REJECT_DC_REJECTEDPAID";

	// Mortgage ACTION_STATUS combinations for notification

	public static final String MG_ACTION_STATUS_INITIATED = "INITIATE_MG_DRAFTED";
	public static final String MG_ACTION_STATUS_SUBMIT = "SUBMIT_MG_PENDINGCLVERIFICATION";
	public static final String MG_ACTION_STATUS_REJECTED = "REJECT_MG_REJECTED";
	public static final String MG_ACTION_STATUS_SENDBACK = "SENDBACK_MG_PENDINGCLARIFICATION";
	public static final String MG_ACTION_STATUS_MORTGAGE_APPROVED = "APPROVE_MG_APPROVED";

	// demand generation
	public static final String OT_STATE_PENDING_SA_VERIFICATION = "OT_PENDINGSAVERIFICATION";
	public static final String OT_STATE_PENDING_APRO = "OT_PENDINGAPRO";
	public static final String DC_STATE_PENDING_SA_VERIFICATION = "DC_PENDINGSAVERIFICATION";
	public static final String DC_STATE_PENDING_APRO = "DC_PENDINGAPRO";

	public static final String MG_STATE_PENDING_GRANTDETAIL = "MG_PENDINGGRANTDETAIL";

	// Draft sates
	public static final String OT_DRAFTED = "OT_DRAFTED";
	public static final String MG_DRAFTED = "MG_DRAFTED";
	public static final String DC_DRAFTED = "DC_DRAFTED";
	public static final String PM_DRAFTED = "PM_DRAFTED";

	// Reject State
	public static final String MG_REJECTED = "MG_REJECTED";
	public static final String PM_REJECTED = "PM_REJECTED";

	public static final String EMAIL_SUBJECT = "Chandigarh mSeva Application Status";

	public static final String NG_TYPE_VIOLATION = "Violation";
	public static final String NG_TYPE_RECOVERY = "Recovery";

	// Mode of payment
	public static final String MODE_UPLOADED = "Uploaded";
	public static final String MODE_GENERATED = "Generated";

	// Relations in PM
	public static final String RELATION_OWNER = "owner";
	public static final String RELATION_PI = "propertyImage";
	public static final String RELATION_GD = "GrantDetail";
	public static final String RELATION_NOTICE = "notice";
	public static final String RELATION_FINANCE = "finance";

	public static final String OT_STATUS_APPROVED = "OT_APPROVED";
	public static final String PM_STATUS_APPROVED = "PM_APPROVED";

	public static final String false_value = "false";
	public static final String true_value = "true";

}
