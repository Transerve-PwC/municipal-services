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
public class RtiConfiguration {

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
	@Value("${rti.service.token.username}")
	private String username;

	@Value("${rti.service.token.password}")
	private String password;
	
	@Value("${rti.service.token.host}")
	private String accountHost;

	@Value("${rti.service.token.path}")
	private String accountPath;
	
	@Value("${rti.service.cpio.host}")
	private String cpioHost;

	@Value("${rti.service.cpio.path}")
	private String cpioPath;
	
	@Value("${rti.service.nodal.host}")
	private String nodalHost;

	@Value("${rti.service.nodal.path}")
	private String nodalPath;
	
	@Value("${rti.service.appellate.host}")
	private String appellateHost;

	@Value("${rti.service.appellate.path}")
	private String appellatePath;
	
	@Value("${rti.service.transaction.confirm.host}")
	private String transConfirmHost;

	@Value("${rti.service.transaction.confirm.path}")
	private String transConfirmPath;
}

