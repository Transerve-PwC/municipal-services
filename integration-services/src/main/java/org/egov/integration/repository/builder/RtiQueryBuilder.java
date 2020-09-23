package org.egov.integration.repository.builder;

import org.springframework.stereotype.Component;

@Component
public class RtiQueryBuilder {	
	 
	 public static final String GET_MINISTRY_MASTER_QUERY="SELECT RM.ministry_code, RM.ministry_name,MAP.username,MAP.user_id,MAP.effective_from, MAP.effective_to,  MAP.user_tenantid\n" + 
	 		"  FROM rti_ministry_master RM inner join rti_ministry_officer_mapping MAP on RM.ministry_code=MAP.ministry_code WHERE MAP.user_id=:userId ";
}
