package org.egov.integration.repository;

import java.util.HashMap;
import java.util.Map;

import org.egov.integration.config.PtConfiguration;
import org.egov.integration.model.PtMapping;
import org.egov.integration.model.PtMappingRequest;
import org.egov.integration.model.PtMappingRequestInfoWrapper;
import org.egov.integration.producer.Producer;
import org.egov.integration.repository.builder.PtMappingQueryBuilder;
import org.egov.integration.repository.rowmapper.PtMappingRowMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PtMappingRepository {
	
	private PtMappingRowMapper rowMapper;	
	private Producer producer;
	private PtConfiguration config;
	
	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Autowired
	public PtMappingRepository(PtMappingRowMapper rowMapper, Producer producer, PtConfiguration config) {
		this.rowMapper = rowMapper;
		this.producer = producer;
		this.config = config;
	}
	
	public void savePtMap(PtMapping ptMapping) {
		PtMappingRequest infoWrapper = PtMappingRequest.builder().ptMappingRequest(ptMapping).build();
		producer.push(config.getPtMappingApplicationSaveTopic(), infoWrapper);
	}
	
	public JSONObject getPropertyTaxList(PtMappingRequestInfoWrapper request) {
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("userId", request.getRequestInfo().getUserInfo().getId());	
		return namedParameterJdbcTemplate.query(PtMappingQueryBuilder.GET_PT_MAPPING_QUERY, paramValues, rowMapper);
	}
}
