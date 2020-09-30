
package org.egov.nulm.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.Role;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSuhCitizenNGORequest;
import org.egov.nulm.model.SuhCitizenNGOApplication;
import org.egov.nulm.producer.Producer;
import org.egov.nulm.repository.builder.NULMQueryBuilder;
import org.egov.nulm.repository.rowmapper.ColumnsRowMapper;
import org.egov.nulm.repository.rowmapper.SuhCitizenNGORowMapper;
import org.egov.nulm.repository.rowmapper.SuhRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SuhCitizenNGORepository {

	private JdbcTemplate jdbcTemplate;

	private Producer producer;

	private NULMConfiguration config;

	private SuhCitizenNGORowMapper suhrowMapper;

	@Autowired
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public SuhCitizenNGORepository(JdbcTemplate jdbcTemplate, Producer producer, NULMConfiguration config,
			SuhCitizenNGORowMapper suhrowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.producer = producer;
		this.config = config;
		this.suhrowMapper = suhrowMapper;
	}

	public void createSuhApplication(SuhCitizenNGOApplication suhApplication) {
		NulmSuhCitizenNGORequest infoWrapper = NulmSuhCitizenNGORequest.builder().nulmSuhRequest(suhApplication)
				.build();
		producer.push(config.getSuhCitizenNGOApplicationSaveTopic(), infoWrapper);
	}

	public void updateSuhApplication(SuhCitizenNGOApplication suhApplication) {
		NulmSuhCitizenNGORequest infoWrapper = NulmSuhCitizenNGORequest.builder().nulmSuhRequest(suhApplication)
				.build();
		producer.push(config.getSuhCitizenNGOApplicationUpdateTopic(), infoWrapper);
	}

	public List<SuhCitizenNGOApplication> getSuhApplication(SuhCitizenNGOApplication suh) {
		try {
			Map<String, Object> paramValues = new HashMap<>();
			paramValues.put("fromDate", suh.getFromDate());
			paramValues.put("toDate", suh.getToDate());
			paramValues.put("suhCitizenNGOId", suh.getSuhCitizenNGOUuid());
			
			return namedParameterJdbcTemplate.query(NULMQueryBuilder.GET_SUH_CITIZEN_QUERY, paramValues, suhrowMapper);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}
	}
}
