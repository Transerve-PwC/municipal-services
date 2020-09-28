package org.egov.nulm.config;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
public class NULMConfiguration {

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

	// Idgen Config
	@Value("${egov.idgen.host}")
	private String idGenHost;

	@Value("${egov.idgen.path}")
	private String idGenPath;
	
	@Value("${egov.user.host}")
	private String userHost;

	@Value("${egov.user.path}")
	private String userPath;
	
	@Value("${egov.workflow.host}")
	private String workFlowHost;

	@Value("${egov.workflow.path}")
	private String workFlowPath;

	@Value("${egov.idgen.nulm.sep.idname}")
	private String sepapplicationNumberIdgenName;

	@Value("${egov.idgen.nulm.sep.idformat}")
	private String sepapplicationNumberIdgenFormat;

	@Value("${egov.idgen.nulm.smid.idname}")
	private String smidapplicationNumberIdgenName;

	@Value("${egov.idgen.nulm.smid.idformat}")
	private String smidapplicationNumberIdgenFormat;

	@Value("${egov.idgen.nulm.smid.shg.idname}")
	private String SmidShgIdgenName;

	@Value("${egov.idgen.nulm.smid.shg.idformat}")
	private String SmidShgIdgenFormat;
	
	@Value("${egov.idgen.nulm.suh.idname}")
	private String suhapplicationNumberIdgenName;

	@Value("${egov.idgen.nulm.suh.idformat}")
	private String suhapplicationNumberIdgenFormat;
	
	@Value("${egov.idgen.nulm.susv.idname}")
	private String susvApplicationNumberIdgenName;

	@Value("${egov.idgen.nulm.susv.idformat}")
	private String susvApplicationNumberIdgenFormat;
	
	@Value("${nulm.businessservice}")
	private String businessservice;
	
	
	
	//organization topic
	
	@Value("${persister.create.organization.topic}")
	private String organizationCreateTopic;

	// SEP Config topics
	@Value("${persister.save.sepapplication.topic}")
	private String SEPApplicationSaveTopic;

	@Value("${persister.update.sepapplication.topic}")
	private String SEPApplicationUpdateTopic;

	@Value("${persister.update.sepapplication.status.topic}")
	private String SEPApplicationUpdateStatusTopic;

	// SEP Status
	@Value("${sep.application.approved.status}")
	private String approved;

	@Value("${sep.application.rejected.status}")
	private String rejected;

	// SEP Search parameter config
	@Value("${egov.user.role.citizen}")
	private String roleCitizenUser;
	
	@Value("${egov.user.role.employee}")
	private String roleEmployee;

	@Value("${egov.user.role.ngo}")
	private String roleNgoUser;
	
	@Value("${nulm.suh.deleteDate.period}")
	private String SuhDeleteDatePeriod;
	
	@Value("${egov.user.role.ja}")
	private String roleJA;

	
	@Value("${egov.user.role.sdo}")
	private String roleSDO;

	
	@Value("${egov.user.role.acmc}")
	private String roleACMC;

	
	// SMID SHG Status
	@Value("${smid.shg.awaitingforapproval.status}")
	private String awaitingforapproval;

	// SMID kafka topic

	@Value("${persister.save.smidapplication.topic}")
	private String SMIDApplicationSaveTopic;

	@Value("${persister.update.smidapplication.topic}")
	private String SMIDApplicationUpdateTopic;

	@Value("${persister.update.smidapplication.status.topic}")
	private String SMIDApplicationUpdateStatusTopic;

	@Value("${persister.save.smidshg.topic}")
	private String SMIDSHGSaveTopic;

	@Value("${persister.delete.smidshg.topic}")
	private String SMIDSHGDeleteTopic;

	@Value("${persister.update.smidshg.topic}")
	private String SMIDSHGUpdateTopic;

	@Value("${persister.updatestatus.smidshg.topic}")
	private String SMIDSHGUpdateStatusTopic;

	@Value("${persister.save.smidshg.member.topic}")
	private String SmidShgMemberSaveTopic;

	@Value("${persister.update.smidshg.member.topic}")
	private String SmidShgMemberUpdateTopic;

	@Value("${persister.delete.smidshg.member.topic}")
	private String SmidShgMemberDeleteTopic;
	
	@Value("${persister.hard.delete.smidshg.member.topic}")
	private String SmidShgMemberHardDeleteTopic;
	
	@Value("${persister.save.suhapplication.topic}")
	private String SuhApplicationSaveTopic;
	
	@Value("${persister.update.suhapplication.topic}")
	private String SuhApplicationUpdateTopic;
	
	@Value("${persister.update.suhapplicationStatus.topic}")
	private String SuhApplicationUpdateStatusTopic;
	
	@Value("${persister.save.suhLog.topic}")
	private String SuhLogSaveTopic;
	
	@Value("${persister.delete.suhLog.topic}")
	private String SuhLogDeleteTopic;
	
	@Value("${persister.save.susvapplication.topic}")
	private String SusvApplicationSaveTopic;
	
	@Value("${persister.update.susvapplication.topic}")
	private String SusvApplicationUpdateTopic;
	
	@Value("${persister.update.susvapplication.status.topic}")
	private String SusvApplicationUpdateStatusTopic;
	
	@Value("${persister.save.susvtransaction.topic}")
	private String SusvTransactionSaveTopic;
	
	@Value("${persister.update.susvtransaction.topic}")
	private String SusvTransactionUpdateTopic;

	@Value("${persister.save.suhcitizenngoapplication.topic}")
	private String SuhCitizenNGOApplicationSaveTopic;
	
	@Value("${persister.update.suhcitizenngoapplication.topic}")
	private String SuhCitizenNGOApplicationUpdateTopic;
}
