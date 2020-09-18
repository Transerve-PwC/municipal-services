package org.egov.cpt.service;

import java.util.Collections;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.DuplicateCopy;
import org.egov.cpt.models.DuplicateCopySearchCriteria;
import org.egov.cpt.models.PropertyImages;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.repository.PropertyRepository;
import org.egov.cpt.validator.PropertyValidator;
import org.egov.cpt.web.contracts.PropertyImagesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PropertyImagesService {

	@Autowired
	private PropertyValidator propertyValidator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private PropertyRepository repository;


	public List<PropertyImages> createPropertyImages(PropertyImagesRequest propertyImagesRequest) {
		propertyValidator.isPropertyPIExist(propertyImagesRequest);
//		propertyValidator.validateDuplicateCopyCreateRequest(propertyImagesRequest);
		propertyValidator.validatePropertyImagesUpdateLimit(propertyImagesRequest);
		enrichmentService.enrichpropertyImageCreateRequest(propertyImagesRequest);

		producer.push(config.getSavePropertyImagesTopic(), propertyImagesRequest);
		return propertyImagesRequest.getPropertyImagesApplications();
	}

	public List<DuplicateCopy> searchPropertyImages(DuplicateCopySearchCriteria criteria, RequestInfo requestInfo) {
		propertyValidator.validateDuplicateCopySearch(requestInfo,criteria);
	    enrichmentService.enrichDuplicateCopySearchCriteria(requestInfo,criteria);
		List<DuplicateCopy> properties = getApplication(criteria, requestInfo);
		return properties;
	}

	private List<DuplicateCopy> getApplication(DuplicateCopySearchCriteria criteria, RequestInfo requestInfo) {
		List<DuplicateCopy> application = repository.getDuplicateCopyProperties(criteria);
		if (application.isEmpty())
			return Collections.emptyList();
		return application;
	}

	public List<PropertyImages> updatePropertyImages(PropertyImagesRequest propertyImagesRequest) {

		List<PropertyImages> searchedProperty = propertyValidator
				.validatePropertyImagesUpdateRequest(propertyImagesRequest);
		enrichmentService.enrichpropertyImagesUpdateRequest(propertyImagesRequest, searchedProperty);

		producer.push(config.getUpdatePropertyImagesTopic(), propertyImagesRequest);

		return propertyImagesRequest.getPropertyImagesApplications();
	}

}
