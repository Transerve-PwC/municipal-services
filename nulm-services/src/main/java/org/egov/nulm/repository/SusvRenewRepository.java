
package org.egov.nulm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.Role;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSusvRenewRequest;
import org.egov.nulm.model.NulmSusvRequest;
import org.egov.nulm.model.SusvApplication;
import org.egov.nulm.model.SusvRenewApplication;
import org.egov.nulm.producer.Producer;
import org.egov.nulm.repository.builder.NULMQueryBuilder;
import org.egov.nulm.repository.rowmapper.SusvRenewRowMapper;
import org.egov.nulm.repository.rowmapper.SusvRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SusvRenewRepository {

	private JdbcTemplate jdbcTemplate;

	private Producer producer;

	private NULMConfiguration config;

	private SusvRenewRowMapper susvRenewRowMapper;

	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public SusvRenewRepository(JdbcTemplate jdbcTemplate, Producer producer, NULMConfiguration config,
			SusvRenewRowMapper susvRenewRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.producer = producer;
		this.config = config;
		this.susvRenewRowMapper = susvRenewRowMapper;
	}

	public void createSusvRenewApplication(SusvRenewApplication susvRenewapplication) {
		NulmSusvRenewRequest infoWrapper = NulmSusvRenewRequest.builder().nulmSusvRenewRequest(susvRenewapplication)
				.build();
		producer.push(config.getSusvRenewApplicationSaveTopic(), infoWrapper);
	}

	public void updateSusvRenewApplication(SusvRenewApplication susvRenewapplication) {
		NulmSusvRenewRequest infoWrapper = NulmSusvRenewRequest.builder().nulmSusvRenewRequest(susvRenewapplication)
				.build();
		producer.push(config.getSusvRenewApplicationUpdateTopic(), infoWrapper);
	}

	public void updateSusvApplicationStatus(SusvRenewApplication susvRenewapplication) {
		NulmSusvRenewRequest infoWrapper = NulmSusvRenewRequest.builder().nulmSusvRenewRequest(susvRenewapplication)
				.build();
		producer.push(config.getSusvRenewApplicationUpdateStatusTopic(), infoWrapper);
	}

	public List<SusvRenewApplication> getSusvRenewApplication(SusvRenewApplication request, String userType, Long userId) {
		Map<String, Object> paramValues = new HashMap<>();
		paramValues.put("tenantId", request.getTenantId());
		paramValues.put("fromDate", request.getFromDate());
		paramValues.put("toDate", request.getToDate());
//		paramValues.put("nameOfApplicant", request.getNameOfApplicant());
		try {
			if (userType.equalsIgnoreCase(config.getRoleCitizenUser())) {

				paramValues.put("createdBy", userId.toString());
				paramValues.put("applicationId", request.getApplicationId());
				paramValues.put("applicationStaus", "");
				return  namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUSV_RENEW_QUERY, paramValues,
						susvRenewRowMapper);
			}
			paramValues.put("applicationStaus", SusvRenewApplication.StatusEnum.DRAFTED.toString());
			paramValues.put("createdBy", "");
			paramValues.put("applicationId", request.getApplicationId());
			return namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUSV_RENEW_QUERY, paramValues, susvRenewRowMapper);

		} catch (Exception e) {
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}
}
