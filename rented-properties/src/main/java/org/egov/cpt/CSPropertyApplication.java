package org.egov.cpt;

import java.util.TimeZone;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = { "org.egov.cpt", "org.egov.cpt.web.controllers", "org.egov.cpt.config",
		"org.egov.cpt.repository" })
@Import({ TracerConfiguration.class })
public class CSPropertyApplication {

	@Value("${app.timezone}")
	private String timeZone;

	public static void main(String[] args) {
		SpringApplication.run(CSPropertyApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).setTimeZone(TimeZone.getTimeZone(timeZone));
	}

}
