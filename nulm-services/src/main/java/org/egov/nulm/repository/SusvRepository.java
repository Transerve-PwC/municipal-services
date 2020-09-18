
package org.egov.nulm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.Role;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSusvRequest;
import org.egov.nulm.model.SmidShgGroup;
import org.egov.nulm.model.SusvApplication;
import org.egov.nulm.producer.Producer;
import org.egov.nulm.repository.builder.NULMQueryBuilder;
import org.egov.nulm.repository.rowmapper.SusvRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SusvRepository {

	private JdbcTemplate jdbcTemplate;

	private Producer producer;

	private NULMConfiguration config;

	private SusvRowMapper susvrowMapper;
	
	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public SusvRepository(JdbcTemplate jdbcTemplate, Producer producer, NULMConfiguration config,
			SusvRowMapper susvrowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.producer = producer;
		this.config = config;
		this.susvrowMapper = susvrowMapper;
	}

	public void createSusvApplication(SusvApplication susvApplication) {
		NulmSusvRequest infoWrapper = NulmSusvRequest.builder().nulmSusvRequest(susvApplication).build();
		producer.push(config.getSusvApplicationSaveTopic(), infoWrapper);
	}
	
	public void updateSusvApplication(SusvApplication susvApplication) {
		NulmSusvRequest infoWrapper = NulmSusvRequest.builder().nulmSusvRequest(susvApplication).build();
		producer.push(config.getSusvApplicationUpdateTopic(), infoWrapper);
	}
	public void updateSusvApplicationStatus(SusvApplication susvApplication) {
		NulmSusvRequest infoWrapper = NulmSusvRequest.builder().nulmSusvRequest(susvApplication).build();
		producer.push(config.getSusvApplicationUpdateStatusTopic(), infoWrapper);
	}
	
	public List<SusvApplication> getSusvApplication(SusvApplication request, List<Role> role,
			Long userId) {
		List<SusvApplication> susv = new ArrayList<>();
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("tenantId", request.getTenantId());
		paramValues.put("fromDate", request.getFromDate());
		paramValues.put("toDate", request.getToDate());
		paramValues.put("nameOfApplicant", request.getNameOfApplicant());
		try {
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleCitizenUser())) {
					
					paramValues.put("createdBy",userId.toString());
					paramValues.put("applicationId", request.getApplicationId());
					paramValues.put("applicationStaus","");
					return susv = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUSV_QUERY, paramValues,
							susvrowMapper);
				}
			}
			
			paramValues.put("applicationStaus",SusvApplication.StatusEnum.DRAFTED.toString());
			paramValues.put("createdBy","");
			paramValues.put("applicationId", request.getApplicationId());
			return susv = namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUSV_QUERY, paramValues, susvrowMapper);
		

		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}
}
