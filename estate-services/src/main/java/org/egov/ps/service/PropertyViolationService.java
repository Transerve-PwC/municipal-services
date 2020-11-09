package org.egov.ps.service;

import java.util.List;

import org.egov.ps.config.Configuration;
import org.egov.ps.model.PropertyPenalty;
import org.egov.ps.producer.Producer;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropertyViolationService {

	@Autowired
	PropertyEnrichmentService propertyEnrichmentService;

	@Autowired
	private Configuration config;

	@Autowired
	private Producer producer;

	public List<PropertyPenalty> penalty(PropertyPenaltyRequest propertyPenaltyRequest) {
		propertyEnrichmentService.enrichPenalty(propertyPenaltyRequest);
		producer.push(config.getSavePenaltyTopic(), propertyPenaltyRequest);
		return propertyPenaltyRequest.getPropertyPenalties();
	}

}
