package org.egov.ec.config;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class EcConstants {
	
	


	public static final String MDM_TEMPLATE_AUCTION_NOTIFICATION = "AuctionNotificationTemplate";
	public static final String MDMS_TEMPLATE_PATH = "$.MdmsRes.egec";
	public static final String ROLE_STORE_MANAGER="challanSM";
	public static final String MDM_MODULE = "egov-echallan";

	public static final String NOTIFICATION_LOCALE = "en_IN";

	//success constants
	
	public static final String STATUS_SUCCESSFULL = "Successful";
	
	public static final String STATUS_SUCCESS = "Success";

	
	
	// error constants

	public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";

	public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";

	public static final String INVALID_FINE_DATA = "Fine data does not exist";

	public static final String FAILED_IDGEN_CHALLANID = "ChallanID Generation Failed";

	// EC actions


	public static final String ACTION_APPROVE = "APPROVE";

	public static final String ACTION_REJECT = "REJECT";

	public static final String STATUS_APPROVED = "APPROVED";

	public static final String STATUS_REJECTED = "REJECTED";

	public static final String STATUS_PAID = "PAID";

	public static final String STATUS_PENDING = "PENDING";
	
	public static final String STATUS_CLOSED = "CLOSED";
	
	public static final String STATUS_FAILED = "FAILED";
	
	public static final String STATUS_AUCTION_PENDING = "PENDING FOR AUCTION";
	
	//workflows

	public static final String WORKFLOW_AUCTION = "AUCTION WORKFLOW";
	
	public static final String WORKFLOW_CHALLAN = "CHALLAN WORKFLOW";
	
	public static final String WORKFLOW_PAYMENT = "PAYMENT WORKFLOW";

	public static final String WORKFLOW_FINE = "FINE MASTER APPROVAL";
	
	public static final String WORKFLOW_MODULE = "ECHALLAN";
	
	
	public static final String ITEMMASTERGET = "ITEMMASTERGET";
	public static final String REPORTAGEINGGET = "REPORTAGEINGGET";
	public static final String REPORTPAYMENTGET = "REPORTPAYMENTGET";
	public static final String REPORTSEIZUREGET = "REPORTSEIZUREGET";
	public static final String VENDDORCREATE = "VENDDORCREATE";
	public static final String VENDDORGET = "VENDDORGET";
	public static final String VENDDORUPDATE = "VENDDORUPDATE";
	public static final String FINEMASTERCREATE = "FINEMASTERCREATE";
	public static final String FINEMASTERUPDATE = "FINEMASTERUPDATE";
	public static final String FINEMASTERGET = "FINEMASTERGET";
	public static final String CHALLANCREATE = "CHALLANCREATE";
	public static final String CHALLANUPDATE = "CHALLANUPDATE";
	public static final String CHALLANGET = "CHALLANGET";
	public static final String AUCTIONCREATE = "AUCTIONCREATE";
	public static final String AUCTIONGET = "AUCTIONGET";
	public static final String AUCTIONUPDATE = "AUCTIONUPDATE";
	public static final String ITEMMASTERCREATE = "ITEMMASTERCREATE";
	public static final String AUCTIOCHALANNGET = "AUCTIOCHALANNGET";
	public static final String STOREREGISTRATION = "STOREREGISTRATION";
	

	public static final String SCRIPTTAGXSS = "(.*?)<(\\s*?)script(\\s*?)(.*?)>(.*?)<(\\s*?)/(\\s*?)script(\\s*?)>(.*?)";
	public static final String SRC1TAGXSS = "(.*?)src[\\r\\n]*=[\\r\\n]*\\\'(.*?)\\\'(.*?)";
	public static final String SRC2TAGXSS = "(.*?)src[\\r\\n]*=[\\r\\n]*\\\"(.*?)\\\"(.*?)";
	public static final String SCRIPTENDTAGXSS = "(.*?)</script(\\s*?)>(.*?)";
	public static final String SCRIPTSTARTTAGXSS = "(.*?)<(\\s*?)script(\\s*?)>(.*?)";
	public static final String JSTAGXSS = "(.*?)javascript:(.*?)";
	public static final String VBTAGXSS = "(.*?)vbscript:(.*?)";
	public static final String ONLOADTAGXSS = "(.*?)onload(.*?)=(.*?)";
		
	
}
