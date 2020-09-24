package org.egov.tl.util;

import org.springframework.stereotype.Component;

@Component
public class CTLConstants {

	public static final String businessService_REHRI_RC = "CTL.REHRI_REGISTRATION";
	public static final String businessService_REHRI_DL = "CTL.REHRI_DRIVING_LICENSE";
	public static final String businessService_DHOBI_GHAT = "CTL.DHOBI_GHAT";
	public static final String businessService_BOOK_SHOP = "CTL.OLD_BOOK_MARKET";
	
	 public static final String CTL_NOTIFICATION_PAYMENT_PAYER = "tl.en.counter.payment.successful.payer.sms";
	 public static final String CTL_NOTIFICATION_PAYMENT_OWNER = "tl.en.counter.payment.successful.owner.sms";
	 public static final String STATUS_PENDINGCLARIFICATION = "PENDINGCLARIFICATION";
	 public static final String STATUS_MODIFIED = "MODIFIED";
	 public static final String EMAIL_SIGNATURE = "tl.email.signature";
	 public static final String CTL_PAYMENT_EMAIL_SUBJECT = "tl.email.payment.subject";
	 
	 public static final String CTL_RECEIPT_PARAM_KEY = "$key";
	 public static final String CTL_RECEIPT_PARAM_VALUE = "tl-receipt";
	 public static final String CTL_RECEIPT_PARAM_TENANT_KEY = "$tenantId";
	 public static final String CTL_RECEIPT_GENERATED_BY_VALUE = "System";
	 public static final String CTL_RECEIPT_GENERATED_BY_KEY = "generateBy";
	 
	 public static final String ACTION_SENDFORCLARIFICATION_CLERK  = "SENDBACK_PENDINGCLARIFICATION";
	 
	 //Email notification constants
	 public static final String NOTIFICATION_SUBMITTED_EMAIL = "tl.en.counter.submitted.email";
	 public static final String NOTIFICATION_SENDBACK_CITIZEN_EMAIL= "tl.en.counter.sendback.email";
	 public static final String NOTIFICATION_REJECTED_EMAIL = "tl.en.counter.rejected.email";
	 public static final String NOTIFICATION_APRROVED_AND_PAYMENT_PENDING_EMAIL = "tl.en.counter.approved.payment.pending.email";
	 public static final String NOTIFICATION_APPROVED_EMAIL = "tl.en.counter.approved.email";
	 public static final String CTL_NOTIFICATION_PAYMENT_PAYER_EMAIL = "tl.en.counter.payment.successful.payer.email";
	 public static final String CTL_NOTIFICATION_PAYMENT_OWNER_EMAIL = "tl.en.counter.payment.successful.owner.email";
	 
	 public static final String MAIL_NOTIFICATION ="MAIL";
	 public static final String SMS_NOTIFICATION ="SMS";
}
