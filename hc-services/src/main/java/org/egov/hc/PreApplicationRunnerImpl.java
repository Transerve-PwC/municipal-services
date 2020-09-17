package org.egov.hc;


import java.util.HashMap;

import java.util.Map;


import javax.annotation.PostConstruct;

import org.egov.tracer.model.CustomException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;


import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Slf4j
public class PreApplicationRunnerImpl implements ApplicationRunner {

	@Value("${egov.validation.addupdate.json.path}")
	private String configValidationAddUpdatePaths;

	@Autowired
	private ResourceLoader resourceLoader;

	public static final Logger logger = LoggerFactory.getLogger(PreApplicationRunnerImpl.class);
	
	@PostConstruct
	@Bean(name = "validatorAddUpdateJSON")
	public JSONObject loadValidationSourceConfigs() {
		Map<String, String> errorMap = new HashMap<>();
		JSONObject jsonObject = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		log.info("====================== EGOV HC SERVICE ======================");
		log.info("LOADING CONFIGS VALIDATION : " + configValidationAddUpdatePaths);
		try {
			log.info("Attempting to load config: " + configValidationAddUpdatePaths);
			Resource resource = resourceLoader.getResource(configValidationAddUpdatePaths);
			jsonObject = mapper.readValue(resource.getInputStream(), JSONObject.class);

			log.info("Parsed: " + jsonObject);
		} catch (Exception e) {
			log.error("Exception while fetching service map for: " + configValidationAddUpdatePaths, e);
			errorMap.put("FAILED_TO_FETCH_FILE", configValidationAddUpdatePaths);
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		else
			log.info("====================== VALIDATION CONFIGS LOADED SUCCESSFULLY! ====================== ");

		return jsonObject;
	}

	@Override
	public void run(final ApplicationArguments arg0) throws Exception {
		try {
			logger.info("Reading JSON for display Column files......");
		} catch (Exception e) {
			logger.error("Exception while loading JSON for display Column files: ", e);
		}
	}

}
