package org.egov.ps.config;

import org.egov.ps.repository.ServiceRequestRepository;
import org.egov.ps.service.MDMSService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Profile("test")
@Configuration
public class ValidatorConfig {

	@Bean
	@Primary
	public MDMSService mdmsService() {
		return Mockito.mock(MDMSService.class);
	}

	@Bean
	@Primary
	public RestTemplate restTemplate() {
		return Mockito.mock(RestTemplate.class);
	}

	@Bean
	@Primary
	public ServiceRequestRepository serviceRequestRepository() {
		return Mockito.mock(ServiceRequestRepository.class);
	}
}
