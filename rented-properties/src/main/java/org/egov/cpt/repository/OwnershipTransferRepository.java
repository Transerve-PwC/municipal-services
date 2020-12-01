package org.egov.cpt.repository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.Owner;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.web.contracts.OwnershipTransferRequest;
import org.egov.cpt.workflow.WorkflowIntegrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class OwnershipTransferRepository {

	@Autowired
	private OwnershipTransferQueryBuilder queryBuilder;

	@Autowired
	private OwnershipTransferRowMapper rowMapper;

	@Autowired
	private Producer producer;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	WorkflowIntegrator workflowIntegrator;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public List<Owner> searchOwnershipTransfer(DuplicateCopySearchCriteria criteria) {

		Map<String, Object> paramsMap = new HashMap<>();
		String query = queryBuilder.getOwnershipTransferSearchQuery(criteria, paramsMap);
		log.info("Params {}, Query: {}", paramsMap.toString(), query);
		return namedParameterJdbcTemplate.query(query, paramsMap, rowMapper);
	}

	public void update(OwnershipTransferRequest ownershipTransferRequest,
			Map<String, Boolean> idToIsStateUpdatableMap) {
		RequestInfo requestInfo = ownershipTransferRequest.getRequestInfo();
		List<Owner> owners = ownershipTransferRequest.getOwners();

		List<Owner> ownersForUpdate = new LinkedList<>();

		for (Owner owner : owners) {
			if (idToIsStateUpdatableMap.get(owner.getId())) {
				ownersForUpdate.add(owner);
			}
		}

		if (!CollectionUtils.isEmpty(ownersForUpdate)) {
			workflowIntegrator
					.callOwnershipTransferWorkFlow(new OwnershipTransferRequest(requestInfo, ownersForUpdate));
			producer.push(config.getOwnershipTransferUpdateTopic(),
					new OwnershipTransferRequest(requestInfo, ownersForUpdate));
		}
	}

}
