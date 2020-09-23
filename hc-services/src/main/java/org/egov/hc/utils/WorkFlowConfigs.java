package org.egov.hc.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *  Utility Class to keep all the values related to workflow
 *  
 * @author kavi elrey
 *
 */
public class WorkFlowConfigs {

	public static final String EDIT = "EDIT";
	public static final String ACTION_OPEN = "open";
	public static final String ACTION_ASSIGN = "ASSIGN";
	public static final String ACTION_CLOSE = "close";
	public static final String ACTION_APPROVE = "";
	public static final String APPROVE = "APPROVE";
	public static final String ACTION_RESOLVE = "resolve";
	public static final String ACTION_REOPEN = "reopen";
	public static final String ACTION_REASSIGN = "reassign";
	public static final String ACTION_REQUEST_FOR_REASSIGN = "requestforreassign";
	public static final String VERIFY_AND_FORWARD = "VERIFY AND FORWARD";
	public static final String VERIFYANDFORWARD = "VERIFYANDFORWARD";
	public static final String VERIFY_AND_FORWARD_TO_SDO = "VERIFYANDFORWARDTOSDO";
	public static final String ACTION_UPDATE  = "UPDATE";
	public static final String FORWARDFORINSPECTION  = "FORWARDFORINSPECTION";
	public static final String INSPECT = "INSPECT";
	public static final String REQUESTCLARIFICATION  = "REQUESTCLARIFICATION";

	public static final String ACTION_INITIATE = "INITIATE";
	public static final String ACTION_VERIFY_AND_ORWARD = "VERIFY AND FORWARD";
	public static final String ACTION_REJECT = "REJECT";
	public static final String ACTION_COMPLETE = "COMPLETE";
	
	public static final String VERIFY_FOR_CLOSURE = "VERIFYFORCLOSURE";
	public static final String FORWARDED_FOR_COMPLETION = "FORWARDFORCOMPLETION";
	
	public static final String ACTION_INITIATED = "INITIATED";
	

	public static final String STATUS_OPENED = "open";
	public static final String STATUS_ASSIGNED = "assigned";
	public static final String STATUS_CLOSED = "closed";
	public static final String STATUS_REJECTED = "rejected";
	public static final String STATUS_RESOLVED = "resolved";
	public static final String STATUS_REASSIGN_REQUESTED = "reassignrequested";
	
	public static final String REMINDER = "REMINDER";
	
	private WorkFlowConfigs() {}

	private static Map<String, String> actionStatusMap = prepareStatusMap();

	private static Map<String, List<String>> actionCurrentStatusMap = prepareActionCurrentStatusMap();

	private static Map<String, List<String>> roleActionMap = prepareRoleActionMap();
	
	private static Map<String, List<String>> mapOfStatusAndReceptors = prepareMapOfStatusAndNotificationReceptors();
	
	private static Map<String, List<String>> mapOfActionAndNotificationReceptors = prepareMapOfActionAndNotificationReceptors();



	private static Map<String, String> prepareStatusMap() {

		Map<String, String> map = new HashMap<>();
		map.put(ACTION_OPEN, STATUS_OPENED);
		map.put(ACTION_ASSIGN, STATUS_ASSIGNED);
		map.put(ACTION_CLOSE, STATUS_CLOSED);
		map.put(ACTION_REJECT, STATUS_REJECTED);
		map.put(ACTION_RESOLVE, STATUS_RESOLVED);
		map.put(ACTION_REOPEN, STATUS_OPENED);
		map.put(ACTION_REASSIGN, STATUS_ASSIGNED);
		map.put(ACTION_REQUEST_FOR_REASSIGN, STATUS_REASSIGN_REQUESTED);
		return map;
	}

	private static Map<String, List<String>> prepareActionCurrentStatusMap() {

		Map<String, List<String>> map = new HashMap<>();
		map.put(ACTION_ASSIGN, Arrays.asList(STATUS_OPENED));
		map.put(ACTION_CLOSE, Arrays.asList(STATUS_REJECTED, STATUS_RESOLVED));
		map.put(ACTION_REJECT, Arrays.asList(STATUS_ASSIGNED, STATUS_OPENED , STATUS_REASSIGN_REQUESTED));
		map.put(ACTION_RESOLVE, Arrays.asList(STATUS_ASSIGNED));
		map.put(ACTION_REOPEN, Arrays.asList(STATUS_REJECTED, STATUS_RESOLVED));
		map.put(ACTION_REASSIGN, Arrays.asList(STATUS_ASSIGNED , STATUS_REASSIGN_REQUESTED));
		map.put(ACTION_REQUEST_FOR_REASSIGN, Arrays.asList(STATUS_ASSIGNED));
		return map;
	}

	/**
	 * This map has the mapping between Role 'codes' and actions.
	 * @return
	 */
	private static Map<String, List<String>> prepareRoleActionMap() {

		Map<String, List<String>> map = new HashMap<>();
		map.put(HCConstants.ROLE_EMPLOYEE, Arrays.asList(ACTION_RESOLVE, ACTION_REQUEST_FOR_REASSIGN));
		map.put(HCConstants.ROLE_CITIZEN, Arrays.asList(ACTION_OPEN, ACTION_CLOSE, ACTION_REOPEN));
		map.put(HCConstants.ROLE_GRO, Arrays.asList(ACTION_ASSIGN, ACTION_REJECT, ACTION_REASSIGN));
		map.put(HCConstants.ROLE_DGRO, Arrays.asList(ACTION_ASSIGN, ACTION_REJECT, ACTION_REASSIGN));
		map.put(HCConstants.ROLE_CSR, Arrays.asList(ACTION_OPEN, ACTION_CLOSE, ACTION_REOPEN));
		return map;
	}
	
	/**
	 * Mapping between the actors that are to receive notification and the status on which they do so.
	 * @return
	 */
	private static Map<String, List<String>> prepareMapOfStatusAndNotificationReceptors(){
		Map<String, List<String>> map = new HashMap<>();
		
		map.put(WorkFlowConfigs.STATUS_ASSIGNED, Arrays.asList(HCConstants.ROLE_CITIZEN, HCConstants.ROLE_EMPLOYEE));
		map.put(WorkFlowConfigs.STATUS_OPENED, Arrays.asList(HCConstants.ROLE_CITIZEN));
		map.put(WorkFlowConfigs.STATUS_REJECTED, Arrays.asList(HCConstants.ROLE_CITIZEN));
		map.put(WorkFlowConfigs.STATUS_RESOLVED, Arrays.asList(HCConstants.ROLE_CITIZEN));
		map.put(WorkFlowConfigs.STATUS_CLOSED, Arrays.asList(HCConstants.ROLE_EMPLOYEE));
		
		return map;
	}
	
	/**
	 * Mapping between the actors that are to receive notification and the action on which they do so.
	 * @return
	 */
	private static Map<String, List<String>> prepareMapOfActionAndNotificationReceptors(){
		Map<String, List<String>> map = new HashMap<>();
		
		map.put(WorkFlowConfigs.ACTION_REASSIGN, Arrays.asList(HCConstants.ROLE_CITIZEN, HCConstants.ROLE_EMPLOYEE));
		map.put(WorkFlowConfigs.ACTION_REOPEN, Arrays.asList(HCConstants.ROLE_CITIZEN));
		
		return map;
	}
	
	public static Map<String, String> getActionStatusMap(){
		return actionStatusMap;
	}
	
	public static Map<String, List<String>> getActionCurrentStatusMap(){
		return actionCurrentStatusMap;
	}
	
	public static Map<String, List<String>> getRoleActionMap(){
		return roleActionMap;
	}
	
	public static Map<String, List<String>> getMapOfStatusAndReceptors(){
		return mapOfStatusAndReceptors;
	}
	
	public static Map<String, List<String>> getMapOfActionAndReceptors(){
		return mapOfActionAndNotificationReceptors;
	}
}