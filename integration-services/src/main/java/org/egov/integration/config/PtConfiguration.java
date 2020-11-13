package org.egov.integration.config;

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
public class PtConfiguration {
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

	@Value("${pt.service.host}")
	private String ptHost;

	@Value("${pt.service.path}")
	private String ptPath;

	@Value("${persister.save.ptmapping.topic}")
	private String ptMappingApplicationSaveTopic;

	@Value("${pt.service.demohost}")
	private String ptDemoHost;

	@Value("${pt.service.sendotp}")
	private String ptSendotp;

	@Value("${pt.service.verfyotp}")
	private String ptVerifyOtp;

	@Value("${pt.service.sectorlist}")
	private String ptSectorList;

	@Value("${pt.service.searchproperty}")
	private String ptSearchProperty;
}
