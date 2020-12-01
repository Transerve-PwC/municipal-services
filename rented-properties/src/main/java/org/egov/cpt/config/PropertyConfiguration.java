package org.egov.cpt.config;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Import({ TracerConfiguration.class })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class PropertyConfiguration {

	@Value("${app.timezone}")
	private String timeZone;

	@PostConstruct
	public void initialize() {
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	}

	@Bean
	@Autowired
	public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);
		return converter;
	}

	// PERSISTER
	@Value("${persister.save.property.topic}")
	private String savePropertyTopic;

	@Value("${persister.update.property.topic}")
	private String updatePropertyTopic;

	@Value("${persister.save.duplicateCopy.topic}")
	private String saveDuplicateCopyTopic;

	@Value("${persister.update.duplicateCopy.topic}")
	private String UpdateDuplicateCopyTopic;

	@Value("${persister.save.propertyImages.topic}")
	private String savePropertyImagesTopic;

	@Value("${persister.update.propertyImages.topic}")
	private String UpdatePropertyImagesTopic;

	@Value("${ownership.transfer.save.topic}")
	private String ownershipTransferSaveTopic;

	@Value("${ownership.transfer.update.topic}")
	private String ownershipTransferUpdateTopic;

	@Value("${persister.save.mortgage.topic}")
	private String saveMortgageTopic;

	@Value("${persister.update.mortgage.topic}")
	private String updateMortgageTopic;

	@Value("${persister.save.notice.topic}")
	private String saveNoticeTopic;

	@Value("${persister.update.notice.topic}")
	private String updateNoticeTopic;

	@Value("${persister.save.dueamount.topic}")
	private String dueAmountTopic;

	// USER
	@Value("${egov.user.host}")
	private String userHost;

	@Value("${egov.user.search.path}")
	private String userSearchEndpoint;

	// NOTIFICATION TOPICS
	@Value("${kafka.topics.notification.sms}")
	private String smsNotifTopic;

	@Value("${kafka.topics.notification.fullpayment}")
	private String receiptTopic;

	@Value("${egov.localization.statelevel}")
	private Boolean isStateLevel;

	@Value("${notification.sms.enabled}")
	private Boolean isSMSNotificationEnabled;

	@Value("${notification.email.enabled}")
	private Boolean isEMAILNotificationEnabled;

	@Value("${kafka.topics.notification.email}")
	private String emailNotifTopic;

	@Value("${egov.user.event.notification.enabledForRP}")
	private Boolean isUserEventsNotificationEnabledForRP;

	// Property Search Params
	@Value("${citizen.allowed.search.params}")
	private String citizenSearchParams;

	@Value("${employee.allowed.search.params}")
	private String employeeSearchParams;

	@Value("${pt.search.pagination.default.limit}")
	private Long defaultLimit;

	@Value("${pt.search.pagination.default.offset}")
	private Long defaultOffset;

	@Value("${pt.search.pagination.max.search.limit}")
	private Long maxSearchLimit;

	// Localization
	@Value("${egov.localization.host}")
	private String localizationHost;

	@Value("${egov.localization.context.path}")
	private String localizationContextPath;

	@Value("${egov.localization.search.endpoint}")
	private String localizationSearchEndpoint;

	// USER EVENTS
	@Value("${egov.ui.app.host}")
	private String uiAppHost;

	@Value("${egov.usr.events.create.topic}")
	private String saveUserEventsTopic;

	@Value("${egov.usr.events.pay.link}")
	private String payLink;

	@Value("${egov.usr.events.ot.pay.link}")
	private String payLinkForOT;

	@Value("${egov.usr.events.dc.pay.link}")
	private String payLinkForDC;

	@Value("${egov.usr.events.rent.pay.link}")
	private String payLinkForRent;

	@Value("${egov.usr.events.pay.code}")
	private String payCode;

	@Value("${egov.usr.events.pay.triggers}")
	private String payTriggers;

	// Workflow

	@Value("${workflow.context.path}")
	private String wfHost;

	@Value("${workflow.transition.path}")
	private String wfTransitionPath;

	@Value("${workflow.businessservice.search.path}")
	private String wfBusinessServiceSearchPath;

	@Value("${is.workflow.enabled}")
	private Boolean isWorkflowEnabled;

	@Value("${create.csp.workflow.name}")
	private String cSPBusinessServiceValue;

	@Value("${ownershipTransfer.worlflow.name}")
	private String ownershipTransferBusinessServiceValue;

	@Value("${duplicateCopy.workflow.name}")
	private String duplicateCopyBusinessServiceValue;

	@Value("${mortgage.workflow.name}")
	private String mortgageBusinessServiceValue;

	// ##### mdms

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;

	// ID Generation

	@Value("${egov.idgen.host}")
	private String idGenHost;

	@Value("${egov.idgen.path}")
	private String idGenPath;

	@Value("${egov.idgen.rp.applicationNum.name}")
	private String applicationNumberIdgenNameRP;

	@Value("${egov.idgen.rp.allotmentNum.name}")
	private String allotmentNumberIdgenNameRP;

	@Value("${egov.idgen.dc.applicationNum.name}")
	private String applicationNumberIdgenNameDC;

	@Value("${egov.idgen.mg.applicationNum.name}")
	private String applicationNumberIdgenNameMG;

	@Value("${egov.idgen.pi.applicationNum.name}")
	private String applicationNumberIdgenNamePI;

	@Value("${egov.idgen.ng.memoNum.name}")
	private String memoNumberIdgenNameNG;

	// BilllingService generating demand

	@Value("${egov.billingservice.host}")
	private String billingHost;

	@Value("${egov.demand.create.endpoint}")
	private String demandCreateEndpoint;

	@Value("${egov.demand.minimum.payable.amount}")
	private BigDecimal minimumPayableAmount;

	@Value("${egov.demand.search.endpoint}")
	private String demandSearchEndpoint;

	@Value("${egov.demand.update.endpoint}")
	private String demandUpdateEndpoint;

	@Value("${egov.bill.gen.endpoint}")
	private String billGenearateEndpoint;

	@Value("${egov.collectionservice.host}")
	private String collectionPaymentHost;

	@Value("${egov.collectionservice.payment.create.path}")
	private String collectionPaymentEndPoint;

}