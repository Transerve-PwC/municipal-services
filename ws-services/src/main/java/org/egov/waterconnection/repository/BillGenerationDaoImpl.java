package org.egov.waterconnection.repository;

import java.util.List;

import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.model.BillGeneration;
import org.egov.waterconnection.producer.WaterConnectionProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BillGenerationDaoImpl implements BillGenerationDao{

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private WSConfiguration wsConfiguration;

	@Autowired
	private WaterConnectionProducer waterConnectionProducer;
	
	@Override
	public List<BillGeneration> saveBillingData(List<BillGeneration> listOfBills) {

		waterConnectionProducer.push(wsConfiguration.getSaveWaterBilling(),listOfBills);
		
		return listOfBills;
	}

}
