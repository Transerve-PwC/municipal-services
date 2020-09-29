package org.egov.cpt.service;

import java.util.List;

import org.egov.cpt.config.PropertyConfiguration;
import org.egov.cpt.models.NoticeGeneration;
import org.egov.cpt.models.Property;
import org.egov.cpt.producer.Producer;
import org.egov.cpt.service.notification.NoticeNotificationService;
import org.egov.cpt.util.PTConstants;
import org.egov.cpt.validator.PropertyValidator;
import org.egov.cpt.web.contracts.NoticeGenerationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoticeGenerationService {
	@Autowired
	private PropertyValidator propertyValidator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private PropertyConfiguration config;

	@Autowired
	private Producer producer;
	
	@Autowired
	NoticeNotificationService notificationService;
	
	public List<NoticeGeneration> createNotice(NoticeGenerationRequest noticeGenerationRequest) {
		List<Property> propertiesFromDb = propertyValidator.isPropertyExist(noticeGenerationRequest);
		noticeGenerationRequest.getNoticeApplications().forEach(application -> {
			if (application.getNoticeType().equals(PTConstants.NG_TYPE_VIOLATION)) {
				propertyValidator.validateNoticeGenerationDocumentUploadLimit(noticeGenerationRequest);
			}
		});
//		propertyValidator.validateMortgageCreateRequest(noticeGenerationRequest);
		enrichmentService.enrichNoticeCreateRequest(noticeGenerationRequest);
		producer.push(config.getSaveNoticeTopic(), noticeGenerationRequest);
		notificationService.process(noticeGenerationRequest,propertiesFromDb);
		return noticeGenerationRequest.getNoticeApplications();
	}

	public List<NoticeGeneration> updateNotice(NoticeGenerationRequest noticeGenerationRequest) {
		List<NoticeGeneration> searchedApplication = propertyValidator.validateNoticeUpdateRequest(noticeGenerationRequest); 
		enrichmentService.enrichNoticeUpdateRequest(noticeGenerationRequest,searchedApplication);
		propertyValidator.validateNoticeUpdate(noticeGenerationRequest);
		producer.push(config.getUpdateNoticeTopic(), noticeGenerationRequest);
		List<Property> propertiesFromDb = propertyValidator.isPropertyExist(noticeGenerationRequest);
		notificationService.process(noticeGenerationRequest,propertiesFromDb);
		return noticeGenerationRequest.getNoticeApplications();
				
	}
}
