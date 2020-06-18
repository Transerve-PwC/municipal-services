package org.egov.tlcalculator.config;

import org.egov.tlcalculator.kafka.broker.TLCalculatorProducer;
import org.egov.tlcalculator.service.DemandService;
import org.egov.tlcalculator.service.MDMSService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class TestConfigurationCTL {
	    
	    @Bean
	    @Primary
		 public DemandService demandService(){
	    	return Mockito.mock(DemandService.class);
	    }

	    @Bean
	    @Primary
	    public TLCalculatorProducer producer(){
	    	return Mockito.mock(TLCalculatorProducer.class);
	    }

	    @Bean
	    @Primary
	    public  MDMSService mdmsService(){
	    	return Mockito.mock(MDMSService.class);
	    }
	    
	}

