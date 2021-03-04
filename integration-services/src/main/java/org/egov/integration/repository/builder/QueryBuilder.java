package org.egov.integration.repository.builder;

import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {	
	 
	public static final String GET_POST_DETAIL_QUERY="select employee_code,array_to_json(array_agg(json_build_object('post_detail_id',post_detail_id) ))as postDetail from employee_post_detail_map where employee_code='11819'\n" + 
			"GROUP BY  employee_code ";
}
