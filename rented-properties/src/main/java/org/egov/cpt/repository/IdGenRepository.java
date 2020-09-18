package org.egov.cpt.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.Idgen.IdGenerationRequest;
import org.egov.cpt.models.Idgen.IdGenerationResponse;
import org.egov.cpt.models.Idgen.IdRequest;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Repository
public class IdGenRepository {

	private RestTemplate restTemplate;

	private PropertyConfiguration config;

	@Autowired
	public IdGenRepository(RestTemplate restTemplate, PropertyConfiguration config) {
		this.restTemplate = restTemplate;
		this.config = config;
	}

	/**
	 * Call iDgen to generateIds
	 * 
	 * @param requestInfo The rquestInfo of the request
	 * @param tenantId    The tenantiD of the tradeLicense
	 * @param name        Name of the foramt
	 * @param format      Format of the ids
	 * @param count       Total Number of idGen ids required
	 * @return
	 */
	public IdGenerationResponse getId(RequestInfo requestInfo, String tenantId, String name, int count) {

		List<IdRequest> reqList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			reqList.add(IdRequest.builder().idName(name).tenantId(tenantId).build());
		}
		IdGenerationRequest req = IdGenerationRequest.builder().idRequests(reqList).requestInfo(requestInfo).build();
		IdGenerationResponse response = null;
		try {
			response = restTemplate.postForObject(config.getIdGenHost() + config.getIdGenPath(), req,
					IdGenerationResponse.class);
		} catch (HttpClientErrorException e) {
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			Map<String, String> map = new HashMap<>();
			map.put(e.getCause().getClass().getName(), e.getMessage());
			throw new CustomException(map);
		}
		return response;
	}

}
