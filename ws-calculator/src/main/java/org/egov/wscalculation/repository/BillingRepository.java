package org.egov.wscalculation.repository;

import java.util.List;

import org.egov.wscalculation.builder.WSCalculatorQueryBuilder;
import org.egov.wscalculation.model.BillGeneration;
import org.egov.wscalculation.producer.WSCalculationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BillingRepository {

	@Autowired
	private WSCalculationProducer wSCalculationProducer;

	@Autowired
	private WSCalculatorQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<BillGeneration> getBillingDataforDemand() {
		
		
		List<BillGeneration> billingDetails;

		billingDetails = jdbcTemplate.query(queryBuilder.getBillingDataForDemandGeneration,
				new Object[] {},
				new BeanPropertyRowMapper<BillGeneration>(BillGeneration.class));
		return billingDetails;


		
		
		
		
	}

	public List<BillGeneration> getBillingDataforSurcharge() {
		
		
		List<BillGeneration> billingDetails;

		billingDetails = jdbcTemplate.query(queryBuilder.getBillingDataForSurcharge,
				new Object[] {},
				new BeanPropertyRowMapper<BillGeneration>(BillGeneration.class));
		return billingDetails;
		
	}

	public BillGeneration getBillingEstimation(String connectionNumber) {
		BillGeneration billingDetails;

		billingDetails = jdbcTemplate.queryForObject(queryBuilder.getBillingDataForConnection,
				new Object[] {connectionNumber},
				new BeanPropertyRowMapper<BillGeneration>(BillGeneration.class));
		return billingDetails;
	}

}
