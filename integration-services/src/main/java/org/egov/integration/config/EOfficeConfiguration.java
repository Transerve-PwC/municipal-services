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
public class EOfficeConfiguration {

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

	@Value("${eoffice.service.host}")
	private String eofficeHost;
	
	

	@Value("${eoffice.service.url.filepending}")
	private String eofficeuserfilepending;

	@Value("${eoffice.service.url.fileclosed}")
	private String eofficeuserfileclosed;

	@Value("${eoffice.service.url.receiptpending}")
	private String eofficeuserreceiptpending;

	@Value("${eoffice.service.url.receiptclosed}")
	private String eofficeuserreceiptclosed;

	@Value("${eoffice.service.url.vipreceiptpending}")
	private String eofficevipreceiptpending;

	@Value("${eoffice.service.url.filependinghierarchy}")
	private String eofficefilependinghierarchy;
	
	
	@Value("${eoffice.service.url.employeeDetails}")
	private String eofficeEmplyoeeDetials;
	
	@Value("${eoffice.service.departmentId}")
	private String eofficeDepartmentId;
	
	@Value("${eoffice.service.authEncoded}")
	private String eofficeAuthEncoded;


}
