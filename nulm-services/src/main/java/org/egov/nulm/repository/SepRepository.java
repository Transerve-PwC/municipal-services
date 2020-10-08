
package org.egov.nulm.repository;

import java.util.ArrayList;
import java.util.List;
import org.egov.common.contract.request.Role;
import org.egov.nulm.common.CommonConstants;
import org.egov.nulm.config.NULMConfiguration;
import org.egov.nulm.model.NulmSepRequest;
import org.egov.nulm.model.SepApplication;
import org.egov.nulm.model.SepApplicationDocument;
import org.egov.nulm.producer.Producer;
import org.egov.nulm.repository.builder.NULMQueryBuilder;
import org.egov.nulm.repository.rowmapper.SEPRowMapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SepRepository {

	private JdbcTemplate jdbcTemplate;

	private Producer producer;

	private NULMConfiguration config;

	private SEPRowMapper seprowMapper;

	@Autowired
	public SepRepository(JdbcTemplate jdbcTemplate, Producer producer, NULMConfiguration config,
			SEPRowMapper seprowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.producer = producer;
		this.config = config;
		this.seprowMapper = seprowMapper;
	}

	public void createSEPApplication(SepApplication sepApplication) {
		NulmSepRequest infoWrapper = NulmSepRequest.builder().nulmSepRequest(sepApplication).build();
		producer.push(config.getSEPApplicationSaveTopic(), infoWrapper);
	}

	public List<SepApplication> getSEPApplication(SepApplication sepApplication, List<Role> role, Long userId) {
		List<SepApplication> sep = new ArrayList<>();
		boolean isEmployee=false;
		try {
			for (Role roleobj : role) {
				if ((roleobj.getCode()).equalsIgnoreCase(config.getRoleEmployee())) {
					isEmployee=true;
				}
			}
			return sep = jdbcTemplate.query(NULMQueryBuilder.GET_SEP_APPLICATION_QUERY,
					new Object[] {  sepApplication.getApplicationId(), 
									sepApplication.getApplicationId(), 
									isEmployee ? "" : userId.toString(), 
									isEmployee ? "" : userId.toString(),
									isEmployee ? "" : sepApplication.getTenantId(),
									isEmployee ? "" : sepApplication.getTenantId(),
									sepApplication.getApplicationStatus() == null ? "" : sepApplication.getApplicationStatus().toString(),
									sepApplication.getApplicationStatus() == null ? "" : sepApplication.getApplicationStatus().toString(),
									sepApplication.getFromDate(), 
									sepApplication.getFromDate(),
									sepApplication.getToDate(),
									sepApplication.getToDate(),
									sepApplication.getName(),
									sepApplication.getName(),
									isEmployee ? SepApplication.StatusEnum.DRAFTED.toString() : ""
								 }, seprowMapper);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CustomException(CommonConstants.ROLE, e.getMessage());
		}

	}

	public void updateSEPApplication(SepApplication sepapplication) {
		NulmSepRequest infoWrapper = NulmSepRequest.builder().nulmSepRequest(sepapplication).build();
		producer.push(config.getSEPApplicationUpdateTopic(), infoWrapper);
	}

	public int checkDocExist(SepApplicationDocument sepApplication, String appId, String tenantId) {

		return jdbcTemplate.queryForObject(NULMQueryBuilder.GET_SEP_DOCUMENT_QUERY,
				new Object[] { appId, tenantId, sepApplication.getFilestoreId(), sepApplication.getDocumentType() },
				Integer.class);
	}

	public void updateSEPApplicationStatus(SepApplication sepapplication) {
		NulmSepRequest infoWrapper = NulmSepRequest.builder().nulmSepRequest(sepapplication).build();
		producer.push(config.getSEPApplicationUpdateStatusTopic(), infoWrapper);
	}

}
