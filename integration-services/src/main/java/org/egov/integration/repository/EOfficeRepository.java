
package org.egov.integration.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.integration.config.ApiConfiguration;
import org.egov.integration.model.EOfficeMapRequestInfoWrapper;
import org.egov.integration.model.EmployeePostDetailMap;
import org.egov.integration.producer.Producer;
import org.egov.integration.repository.builder.QueryBuilder;
import org.egov.integration.repository.rowmapper.ColumnsRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EOfficeRepository {

	@Autowired
	private Producer producer;

	@Autowired
	private ApiConfiguration config;
	
	@Autowired
	private ColumnsRowMapper rowMapper;
	
	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public void saveData(EmployeePostDetailMap data) {
		EOfficeMapRequestInfoWrapper infoWrapper = EOfficeMapRequestInfoWrapper.builder().employeePostDetailMap(data).build();
		producer.push(config.getEofficeSaveTopic(), infoWrapper);
	}

	public List<EmployeePostDetailMap> getPostDetailId(EOfficeMapRequestInfoWrapper request) {
		List<EmployeePostDetailMap> list = new ArrayList<>();
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("employeeCode", request.getEmployeePostDetailMap().getEmployeeCode());	
		list = namedParameterJdbcTemplate.query(QueryBuilder.GET_POST_DETAIL_QUERY, paramValues, rowMapper);	
		System.out.println(list);
		return list;
	}

	

}

