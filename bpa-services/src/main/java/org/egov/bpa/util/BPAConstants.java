package org.egov.bpa.util;

import org.springframework.stereotype.Component;

@Component
public class BPAConstants {

	// MDMS

	public static final String BPA_MODULE = "BPA";

	public static final String BPA_BusinessService = "BPA";

	public static final String BPA_MODULE_CODE = "BPA";

	public static final String BPA_LOW_MODULE_CODE = "BPA_LOW";

	public static final String COMMON_MASTERS_MODULE = "common-masters";

	public static final String NOTIFICATION_LOCALE = "en_IN";

	public static final String NOTIFICATION_INITIATED = "tl.en.counter.initiate";

	public static final String NOTIFICATION_PANDING_APPL_FEE = "tl.en.counter.appl.fee";

	public static final String NOTIFICATION_APPLIED = "tl.en.counter.submit";

	public static final String NOTIFICATION_DOCUMENT_VERIFICATION = "bpa.en.document";

	public static final String NOTIFICATION_FIELD_INSPECTION = "bpa.en.field.inspection";

	public static final String NOTIFICATION_NOC_UPDATION = "bpa.en.field.inspection";

	public static final String NOTIFICATION_PAYMENT_OWNER = "tl.en.counter.payment.successful.owner";

	public static final String NOTIFICATION_PAYMENT_PAYER = "bpa.en.counter.payment.successful.payer";

	public static final String NOTIFICATION_PAID = "bpa.en.counter.pending.approval";

	public static final String NOTIFICATION_APPROVED = "bpa.en.counter.approved";

	public static final String NOTIFICATION_REJECTED = "bpa.en.counter.rejected";

	public static final String NOTIFICATION_CANCELLED = "bpa.en.counter.cancelled";

	public static final String NOTIFICATION_FIELD_CHANGED = "bpa.en.edit.field.change";

	public static final String NOTIFICATION_OBJECT_ADDED = "bpa.en.edit.object.added";

	public static final String NOTIFICATION_OBJECT_REMOVED = "bpa.en.edit.object.removed";

	public static final String NOTIFICATION_OBJECT_MODIFIED = "bpa.en.edit.object.modified";

	public static final String DEFAULT_OBJECT_MODIFIED_MSG = "Dear <1>,Your Building Plan with application number <APPLICATION_NUMBER> was modified.";

	// mdms path codes

	public static final String BPA_JSONPATH_CODE = "$.MdmsRes.BPA";

	public static final String COMMON_MASTER_JSONPATH_CODE = "$.MdmsRes.common-masters";

	// error constants

//	public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";
//
//	public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";

	// mdms master names

	public static final String SERVICE_TYPE = "ServiceType";

	public static final String APPLICATION_TYPE = "ApplicationType";

	public static final String OCCUPANCY_TYPE = "OccupancyType";

	public static final String SUB_OCCUPANCY_TYPE = "SubOccupancyType";

	public static final String USAGES = "Usages";

	public static final String CalculationType = "CalculationType";

	public static final String DOCUMENT_TYPE_MAPPING = "DocTypeMapping";

	public static final String RISKTYPE_COMPUTATION = "RiskTypeComputation";

	public static final String DOCUMENT_TYPE = "DocumentType";

	public static final String OWNER_TYPE = "OwnerType";

	public static final String OWNERSHIP_CATEGORY = "OwnerShipCategory";

	public static final String CHECKLIST_NAME = "CheckList";

	public static final String NOC_TYPE_MAPPING = "NocTypeMapping";

	// FINANCIAL YEAR

	public static final String MDMS_EGF_MASTER = "egf-master";

	public static final String MDMS_FINANCIALYEAR = "FinancialYear";

	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

	public static final String MDMS_STARTDATE = "startingDate";

	public static final String MDMS_ENDDATE = "endingDate";

	// BPA actions

	public static final String ACTION_INITIATE = "INITIATE";

	public static final String ACTION_APPLY = "APPLY";

	public static final String ACTION_APPROVE = "APPROVE";

	public static final String ACTION_FORWORD = "FORWARD";

	public static final String ACTION_MARK = "MARK";

	public static final String ACTION_SENDBACK = "SENDBACK";

	public static final String ACTION_DOC_VERIFICATION_FORWARD = "DOC_VERIFICATION_FORWARD";

	public static final String ACTION_FIELDINSPECTION_FORWARD = "FIELDINSPECTION_FORWARD";

	public static final String ACTION_NOC_FORWARD = "NOC_FORWARD";

	public static final String ACTION_PENDINGAPPROVAL = "PENDINGAPPROVAL";

	public static final String ACTION_REJECT = "REJECT";
	public static final String ACTION_REVOCATE = "REVOCATE";

	public static final String ACTION_CANCEL = "CANCEL";

	public static final String ACTION_PAY = "PAY";

	public static final String ACTION_SKIP_PAY = "SKIP_PAYMENT";

	public static final String ACTION_ADHOC = "ADHOC";
	
	public static final String ACTION_SEND_TO_ARCHITECT = "SEND_TO_ARCHITECT";
	
	public static final String ACTION_SEND_TO_CITIZEN = "SEND_TO_CITIZEN";

	// BPA Status

	public static final String STATUS_INITIATED = "INPROGRESS";

	public static final String STATUS_APPLIED = "INPROGRESS";

	public static final String STATUS_APPROVED = "APPROVED";

	public static final String STATUS_REJECTED = "REJECTED";

	public static final String STATUS_REVOCATED = "PERMIT REVOCATION";

	public static final String STATUS_DOCUMENTVERIFICATION = "INPROGRESS";

	public static final String STATUS_FIELDINSPECTION = "INPROGRESS";

	public static final String STATUS_NOCUPDATION = "INPROGRESS";

	public static final String STATUS_PENDINGAPPROVAL = "INPROGRESS";

	public static final String STATUS_CANCELLED = "CANCELLED";

	public static final String STATUS_PAID = "INPROGRESS";

	public static final String BILL_AMOUNT = "$.Demands[0].demandDetails[0].taxAmount";

	// ACTION_STATUS combinations for notification

	public static final String ACTION_STATUS_INITIATED = "INITIATE_INITIATED";

	public static final String ACTION_STATUS_SEND_TO_CITIZEN = "SEND_TO_CITIZEN_CITIZEN_APPROVAL_INPROCESS";

	public static final String ACTION_STATUS_SEND_TO_ARCHITECT = "SEND_TO_ARCHITECT_INITIATED";

	public static final String ACTION_STATUS_CITIZEN_APPROVE = "APPROVE_INPROGRESS";

	public static final String ACTION_STATUS_PENDING_APPL_FEE = "APPLY_PENDING_APPL_FEE";

	public static final String ACTION_STATUS_DOC_VERIFICATION = "PAY_DOC_VERIFICATION_INPROGRESS";

	public static final String ACTION_STATUS_FI_VERIFICATION = "FORWARD_FIELDINSPECTION_INPROGRESS";

	public static final String ACTION_STATUS_NOC_VERIFICATION = "FORWARD_NOC_VERIFICATION_INPROGRESS";

	public static final String ACTION_STATUS_PENDING_APPROVAL = "FORWARD_APPROVAL_INPROGRESS";

	public static final String ACTION_STATUS_PENDING_SANC_FEE = "APPROVE_PENDING_SANC_FEE_PAYMENT";

	public static final String ACTION_STATUS_APPROVED = "PAY_APPROVED";

	public static final String ACTION_STATUS_APPLIED = "APPLIED";

	public static final String ACTION_STATUS_REJECTED = "REJECT_REJECTED";

	public static final String ACTION_STATUS_DOCUMENTVERIFICATION = "FORWARD_DOCUMENTVERIFICATION";

	// public static final String ACTION_CANCEL_CANCELLED = "CANCEL_CANCELLED";

	public static final String ACTION_STATUS_PAID = "PAID";

	public static final String ACTION_STATUS_FIELDINSPECTION = "FORWARD_FIELDINSPECTION";

	public static final String ACTION_CANCEL_CANCELLED = "CANCEL_CANCELLED";

	public static final String ACTION_STATUS_NOCUPDATION = "FORWARD_NOCUPDATION";

	public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
	public static final String USREVENTS_EVENT_NAME = "Building Plan";
	public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-BPA";

	public static final String FI_STATUS = "FIELDINSPECTION_INPROGRESS";
	public static final String FI_ADDITIONALDETAILS = "fieldinspection_pending";
	
	public static final String  STATUS_CITIZEN_APPROVAL_INPROCESS = "CITIZEN_APPROVAL_INPROCESS";

	// OCCUPANCY TYPE

	public static final String RESIDENTIAL_OCCUPANCY = "A";

	// CALCULATION FEEe
	public static final String APPLICATION_FEE_KEY = "ApplicationFee";
	public static final String SANCTION_FEE_KEY = "SanctionFee";
	public static final String LOW_RISK_PERMIT_FEE_KEY = "LOW_RISK_PERMIT_FEE";

	public static final String SANC_FEE_STATE = "PENDING_SANC_FEE_PAYMENT";
	public static final String APPL_FEE_STATE = "PENDING_APPL_FEE";
	public static final String BPA_LOW_APPL_FEE_STATE = "PENDING_FEE";
	public static final String APPROVED_STATE = "APPROVED";
	public static final String DOCVERIFICATION_STATE = "DOC_VERIFICATION_PENDING";
	public static final String NOCVERIFICATION_STATUS = "NOC_VERIFICATION_INPROGRESS";

	public static final String PENDING_APPROVAL_STATE = "APPROVAL_PENDING";

	public static final String APPL_FEE = "BPA.NC_APP_FEE";

	public static final String SANC_FEE = "BPA.NC_SAN_FEE";
	public static final String INPROGRESS_STATUS = "INPROGRESS";

	// CheckList
	public static final String QUESTIONS_MAP = "$.MdmsRes.BPA.CheckList[?(@.WFState==\"{1}\" && @.RiskType==\"{2}\" && @.ServiceType==\"{3}\" && @.applicationType==\"{4}\")].questions";

	public static final String DOCTYPES_MAP = "$.MdmsRes.BPA.CheckList[?(@.WFState==\"{1}\" && @.RiskType==\"{2}\" && @.ServiceType==\"{3}\" && @.applicationType==\"{4}\")].docTypes";
	public static final String CONDITIONS_MAP = "$.MdmsRes.BPA.CheckList[?(@.WFState==\"{1}\" && @.RiskType==\"{2}\" && @.ServiceType==\"{3}\" && @.applicationType==\"{4}\")].conditions";
	public static final String CHECKLISTFILTER = "$.[?(@.WFState==\"{}\")]";

	public static final String CHECKLIST_TYPE = "checkList";
	public static final String DOCTYPES_TYPE = "docTypes";
	public static final String QUESTIONS_TYPE = "questions";
	public static final String QUESTION_TYPE = "question";
	public static final String INSPECTION_DATE = "date";
	public static final String INSPECTION_TIME = "time";
	public static final String DOCS = "docs";
	public static final String CODE = "documentType";
	public static final String QUESTIONS_PATH = "$.[?(@.active==true)].question";
	public static final String DOCTYPESS_PATH = "$.[?(@.required==true)].code";
	public static final String NOCTYPE_MAP = "$.MdmsRes.BPA.NocTypeMapping[?(@.applicationType==\"{1}\" && @.serviceType==\"{2}\" && @.riskType==\"{3}\" && @.nocTriggerState==\"{4}\")].nocTypes";
	public static final String NOCTYPE_REQUIRED_MAP = "$.MdmsRes.BPA.NocTypeMapping[?(@.applicationType==\"{1}\" && @.serviceType==\"{2}\" && @.riskType==\"{3}\")].nocTypes";
	public static final String NOCTYPE_OFFLINE_MAP = "$.MdmsRes.NOC.NocType[?(@.mode==\"offline\")].code";
	public static final String NOC_TRIGGER_STATE_MAP = "$.MdmsRes.BPA.NocTypeMapping[?(@.applicationType==\"{1}\" && @.serviceType==\"{2}\" && @.riskType==\"{3}\")].nocTriggerState";

	// SMS Notification messages
	public static final String APP_CREATE = "APPLICATION_CREATE_MSG";

	public static final String SEND_TO_CITIZEN = "SEND_TO_CITIZEN_MSG";

	public static final String CITIZEN_APPROVED = "CITIZEN_APPROVED_MSG";

	public static final String SEND_TO_ARCHITECT = "SEND_TO_ARCHITECT_MSG";

	public static final String APP_CLOSED = "APP_CLOSED_MSG";

	public static final String APP_FEE_PENDNG = "APPLICATION_FEE_PENDING_MSG";

	public static final String PAYMENT_RECEIVE = "PAYMENT_RECEIVED_MSG";

	public static final String DOC_VERIFICATION = "DOC_VERIFICATION_DONE_MSG";

	public static final String NOC_VERIFICATION = "NOC_FIELD_VERIFICATION_DONE_MSG";

	public static final String NOC_APPROVE = "NOC_APPROVED_MSG";

	public static final String PERMIT_FEE_GENERATED = "PERMIT_FEE_GENERATED_MSG";

	public static final String APPROVE_PERMIT_GENERATED = "APPROVED_AND_PERMIT_GENERATED_MSG";

	public static final String APP_REJECTED = "APPLICATION_REJECTED_MSG";

	public static final String M_APP_CREATE = "M_APPLICATION_CREATE_MSG";

	public static final String M_SEND_TO_CITIZEN = "M_SEND_TO_CITIZEN_MSG";

	public static final String M_CITIZEN_APPROVED = "M_CITIZEN_APPROVED_MSG";

	public static final String M_SEND_TO_ARCHITECT = "M_SEND_TO_ARCHITECT_MSG";

	public static final String M_APP_CLOSED = "M_APP_CLOSED_MSG";

	public static final String M_APP_FEE_PENDNG = "M_APPLICATION_FEE_PENDING_MSG";

	public static final String M_PAYMENT_RECEIVE = "M_PAYMENT_RECEIVED_MSG";

	public static final String M_DOC_VERIFICATION = "M_DOC_VERIFICATION_DONE_MSG";

	public static final String M_NOC_VERIFICATION = "M_NOC_FIELD_VERIFICATION_DONE_MSG";

	public static final String M_NOC_APPROVE = "M_NOC_APPROVED_MSG";

	public static final String M_PERMIT_FEE_GENERATED = "M_PERMIT_FEE_GENERATED_MSG";

	public static final String M_APPROVE_PERMIT_GENERATED = "M_APPROVED_AND_PERMIT_GENERATED_MSG";

	public static final String M_APP_REJECTED = "M_APPLICATION_REJECTED_MSG";

	public static final String SEARCH_MODULE = "rainmaker-bpa";

//	public static final String INVALID_SEARCH = "INVALID SEARCH";

	public static final String INVALID_UPDATE = "INVALID UPDATE";

	public static final String EMPLOYEE = "EMPLOYEE";

	public static final String FILESTOREID = "fileStoreId";

	public static final String LOW_RISKTYPE = "LOW";

	public static final String EDCR_PDF = "ScrutinyReport.pdf";

	public static final String PERMIT_ORDER_NO = "BPA_PDF_PLANPERMISSION_NO";

	public static final String GENERATEDON = "BPA_PDF_GENERATED_ON";

	public static final String CITIZEN = "CITIZEN";

	public static final String ACTION_SENDBACKTOCITIZEN = "SEND_BACK_TO_CITIZEN";

	public static final String HIGH_RISKTYPE = "HIGH";

	public static final String OTHER_RISKTYPE = "OTHER";

	public static final String BUILDING_PLAN = "BUILDING_PLAN_SCRUTINY";

	public static final String BUILDING_PLAN_OC = "BUILDING_OC_PLAN_SCRUTINY";

	public static final String BPA_OC_MODULE_CODE = "BPA_OC";

	public static final String OC_OCCUPANCY = "$.edcrDetail[0].planDetail.planInformation.occupancy";

	public static final String OC_KHATHANO = "$.edcrDetail[0].planDetail.planInformation.khataNo";

	public static final String OC_PLOTNO = "$.edcrDetail[0].planDetail.planInformation.plotNo";

	public static final String BUILDING_PLAN_PC = "BUILDING_PC_PLAN_SCRUTINY";

	public static final String BPA_PC_MODULE_CODE = "BPA_PC";

	public static final String SERVICETYPE = "serviceType";

	public static final String APPLICATIONTYPE = "applicationType";

	public static final String PERMIT_NO = "permitNumber";

	public static final String NOC_MODULE = "NOC";

	public static final String NOC_TYPE = "NocType";

	public static final String NOC_APPLICATIONTYPE = "NEW";

	public static final String NOC_SOURCE = "BPA";

//	public static final String REQUIRED_NOCS="requiredNOCs";

	public static final String PLOT_AREA_PATH = "edcrDetail.*.planDetail.plot.area";
	public static final String SUB_OCCUPANCY_TYPE_PATH = "edcrDetail.*.planDetail.virtualBuilding.mostRestrictiveFarHelper.subtype.code";
	public static final String BUILDING_HEIGHT_PATH = "edcrDetail.*.planDetail.virtualBuilding.buildingHeight";
	public static final String SPECIAL_BUILDING_PATH = "edcrDetail.*.planDetail.planInformation.specialBuilding";
	public static final String BUSINESS_SERVICE_PATH = "edcrDetail.*.planDetail.planInformation.businessService";

	// Occupancies code
	public static final String A = "A"; // Residential
	public static final String B = "B"; // Commercial
	public static final String C = "C"; // Public-Semi Public/Institutional
	public static final String D = "D"; // Public Utility
	public static final String E = "E"; // Industrial Zone
	public static final String F = "F"; // Education
	public static final String G = "G"; // Transportation
	public static final String H = "H"; // Agriculture

	// Sub occupancies code
	public static final String A_P = "A-P";// Plotted Detached/Individual Residential building
	public static final String A_S = "A-S";// Semi-detached
	public static final String A_R = "A-R";// Row housing
	public static final String A_AB = "A-AB";// Apartment Building
	public static final String A_HP = "A-HP";// Housing Project
	public static final String A_WCR = "A-WCR";// work-cum-residential
	public static final String A_SA = "A-SA";// Studio Apartments
	public static final String A_DH = "A-DH";// Dharmasala
	public static final String A_D = "A-D";// Dormitory
	public static final String A_E = "A-E";// EWS
	public static final String A_LIH = "A-LIH";// Low Income Housing
	public static final String A_MIH = "A-MIH";// Medium Income Housing
	public static final String A_H = "A-H";// Hostel
	public static final String A_SH = "A-SH";// Shelter House
	public static final String A_SQ = "A-SQ";// Staff Qaurter

	public static final String B_H = "B-H";// Hotel
	public static final String B_5S = "B-5S";// 5 Star Hotel
	public static final String B_M = "B-M";// Motels
	public static final String B_SFH = "B-SHF";// Services for households
	public static final String B_SCR = "B-SCR";// Shop Cum Residential
	public static final String B_B = "B-B";// Bank
	public static final String B_R = "B-R";// Resorts
	public static final String B_IIR = "B-IIR";// lagoons and lagoon resort
	public static final String B_AB = "A-AB";// Amusement Building/Park and water sports
	public static final String B_F = "B-F";// financial services and stock exchanges
	public static final String B_C = "B-C";// Cold Storage and Ice Factory
	public static final String B_CBO = "B-CBO";// Commercial and Business Offices/Complex
	public static final String B_CNS = "B-CNS";// Convenience and Neighborhood Shopping
	public static final String B_P = "B-P";// Professional offices
	public static final String B_D = "B-D";// Departmental store
	public static final String B_GG = "B-GG";// Gas Godown
	public static final String B_G = "B-G";// Godowns
	public static final String B_GS = "B-GS";// Good Storage
	public static final String B_GH = "B-GH";// Guest Houses
	public static final String B_HR = "B-HR";// Holiday Resort
	public static final String B_BLH = "B-BLH";// Boarding and lodging houses
	public static final String B_P1 = "B-P1";// Petrol Pump (Only Filling Station)
	public static final String B_P2 = "B-P2";// Petrol Pump (Filling Station and Service station)
	public static final String B_CMS = "B-CMS";// CNG Mother Station
	public static final String B_RES = "B-RES";// Restaurant
	public static final String B_LS = "B-LS";// local(retail) shopping
	public static final String B_SC = "B-SC";// Shopping Center
	public static final String B_SM = "B-SM";// Shopping Mall
	public static final String B_ShR = "B-ShR";// Showroom
	public static final String B_WSt1 = "B-WSt1";// Wholesale Storage (Perishable)
	public static final String B_WSt2 = "B-WSt2";// Wholesale Storage (Non Perishable)
	public static final String B_St = "B-St";// Storage/ Hangers/ Terminal Depot
	public static final String B_Sup = "B-Sup";// Supermarkets
	public static final String B_WH = "B-WH";// Ware House
	public static final String B_WM = "B-WM";// Wholesale Market
	public static final String B_MC = "B-MC";// media centres
	public static final String B_FC = "B-FC";// food courts
	public static final String B_WB = "B-WB";// Weigh bridges
	public static final String B_ME = "B-ME";// Mercentile

	public static final String C_A = "C-A";// Auditorium
	public static final String C_B = "C-B";// Banquet Hall
	public static final String C_C = "C-C";// Cinema
	public static final String C_CL = "C-CL";// Club
	public static final String C_MP = "C-MP";// music pavilions
	public static final String C_CH = "C-CH";// Community Hall
	public static final String C_O = "C-O";// Orphanage
	public static final String C_OAH = "C-OAH";// Old Age Home
	public static final String C_SC = "C-SC";// Science Centre/Museum
	public static final String C_C1H = "C-C1H";// Confernce Hall
	public static final String C_C2H = "C-C2H";// Convention Hall
	public static final String C_SCC = "C-SCC";// sculpture complex
	public static final String C_CC = "C-CC";// Cultural Complex
	public static final String C_EC = "C-EC";// Exhibition Center
	public static final String C_G = "C-G";// Gymnasia
	public static final String C_MH = "C-MH";// Marriage Hall/Kalyan Mandap
	public static final String C_ML = "C-ML";// Multiplex
	public static final String C_M = "C-M";// Musuem
	public static final String C_PW = "C-PW";// Place of workship
	public static final String C_PL = "C-PL";// Public Libraries
	public static final String C_REB = "C-REB";// Recreation Bldg
	public static final String C_SPC = "C-SPC";// Sports Complex
	public static final String C_S = "C-S";// Stadium
	public static final String C_T = "C-T";// Theatre
	public static final String C_AB = "C-AB";// Administrative Buildings
	public static final String C_GO = "C-GO";// Government offices
	public static final String C_LSGO = "C-LSGO";// Local and semi Government offices
	public static final String C_P = "C-P";// Police/Army/Barrack
	public static final String C_RB = "C-RB";// Religious Building
	public static final String C_SWC = "C-SWC";// Social and welfare centres
	public static final String C_CI = "C-CI";// Clinic
	public static final String C_D = "C-D";// Dispensary
	public static final String C_YC = "C-YC";// Yoga Center
	public static final String C_DC = "C-DC";// Diagnostic Centre
	public static final String C_GSGH = "C-GSGH";// Govt-Semi Govt. Hospital
	public static final String C_RT = "C-RT";// Registered Trust
	public static final String C_HC = "C-HC";// Health centre
	public static final String C_H = "C-H";// Hospital
	public static final String C_L = "C-L";// Lab
	public static final String C_MTH = "C-MTH";// Maternity Home
	public static final String C_MB = "C-MB";// Medical Building
	public static final String C_NH = "C-NH";// Nursing Home
	public static final String C_PLY = "C-PLY";// Polyclinic
	public static final String C_RC = "C-RC";// Rehabilitaion Center
	public static final String C_VHAB = "C-VHAB";// Veterinary Hospital for pet animals and birds
	public static final String C_RTI = "C-RTI";// Research and Training Institute
	public static final String C_PS = "C-PS";// Police Station
	public static final String C_FS = "C-FS";// Fire Station
	public static final String C_J = "C-J";// Jail/Prison
	public static final String C_PO = "C-PO";// Post Office

	public static final String D_BCC = "D-BCC";// Bill Collection Center
	public static final String D_BTC = "D-BTC";// Broadcasting-Transmission Centre
	public static final String D_BCG = "D-BCG";// Burial and cremation grounds
	public static final String D_PDSS = "D-PDSS";// Public Distribution System Shop
	public static final String D_PTPA = "D-PTPA";// Public Toilets in Public Area
	public static final String D_PUB = "D-PUB";// Public Utility Bldg
	public static final String D_SS = "D-SS";// Sub-Station
	public static final String D_TEL = "D-TEL";// Telecommunication
	public static final String D_WPS = "D-WPS";// water pumping stations
	public static final String D_SSY = "D-SSY";// service and storage yards
	public static final String D_EDD = "D-EDD";// electrical distribution depots

	public static final String E_IB = "E-IB";// Industrial Buildings (Factories, Workshops, etc.)
	public static final String E_NPI = "E-NPI";// Non Polluting Industrial
	public static final String E_ITB = "E-ITB";// IT, ITES Buildings
	public static final String E_SI = "E-SI";// SEZ Industrial
	public static final String E_L = "E-L";// Loading/Unloading Spaces
	public static final String E_FF = "E-FF";// Flatted Factory
	public static final String E_SF = "E-SF";// small factories and etc falls in industrial

	public static final String F_CC = "F-CC";// Coaching Centre
	public static final String F_CI = "F-CI";// Commercial Institute
	public static final String F_C = "F-C";// College
	public static final String F_CTI = "F-CTI";// Computer Training Institute
	public static final String F_NS = "F-NS";// Nursery School
	public static final String F_PS = "F-PS";// Primary School
	public static final String F_H = "F-H";// Hostel (Captive)
	public static final String F_HS = "F-HS";// High School
	public static final String F_PLS = "F-PLS";// Play School,
	public static final String F_CR = "F-CR";// crÃ¨che
	public static final String F_SMC = "F-SMC";// School for Mentally Challenged.
	public static final String F_AA = "F-AA";// Art academy
	public static final String F_TC = "F-TC";// Technical College
	public static final String F_STC = "F-STC";// Sports training centers
	public static final String F_TI = "F-TI";// Training Institute
	public static final String F_VI = "F-VI";// Veterinary Institute
	public static final String F_MC = "F-MC";// Medical College
	public static final String F_RTC = "F-RTC";// Research and Training Center
		
	public static final String G_A = "G-A";// Airport
	public static final String G_AS = "G-AS";// Auto Stand
	public static final String G_MS = "G-MS";// Metro Station
	public static final String G_BS = "G-BS";// Bus Stand
	public static final String G_BT = "G-BT";// Bus Terminal
	public static final String G_I = "G-I";// ISBT
	public static final String G_RS = "G-RS";// Railway station
	public static final String G_TS = "G-TS";// Taxi Stand
	public static final String G_MLCP = "G-MLCP";// Multi Level Car Parking
	public static final String G_PP = "G-PP";// Public Parking
	public static final String G_TP = "G-TP";// Toll Plaza
	public static final String G_TT = "G-TT";// Truck Terminal
	
	public static final String H_AF = "H-AF";// Agriculture Farm
	public static final String H_AG = "H-AG";// Agro Godown
	public static final String H_ARF = "H-ARF";// Agro-Research Farm
	public static final String H_FH = "H-FH";// Farm House
	public static final String H_CH = "H-CH";// Country Homes
	public static final String H_NGH = "H-NGH";// Nursery and green houses
	public static final String H_PDS = "H-PDS";// Polutry, Diary and Swine/Goat/Horse
	public static final String H_H = "H-H";// Horticulture
	public static final String H_SC = "H-SC";// Seri culture

	public static final String BPA_PA_MODULE_CODE = "BPA1";
	public static final String BPA_PO_MODULE_CODE = "BPA2";
	public static final String BPA_PM_MODULE_CODE = "BPA3";
	public static final String BPA_DP_BP_MODULE_CODE = "BPA4";
	
	public static final String YES = "YES";

}
