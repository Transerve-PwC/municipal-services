package org.egov.ec.service.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.egov.ec.config.EcConstants;
import org.egov.tracer.model.CustomException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class CustomBeanValidatorImpl implements CustomBeanValidator {

	ValidatorFactory valdiatorFactory = null;
	
	


	public CustomBeanValidatorImpl() {
		valdiatorFactory = Validation.buildDefaultValidatorFactory();
	}

	@Override
	public <T> void validateFields(T object) {
		Validator validator = valdiatorFactory.getValidator();
		Set<ConstraintViolation<T>> failedValidations = validator.validate(object);

		if (!failedValidations.isEmpty()) {
			List<String> allErrors = failedValidations.stream().map(failure -> failure.getMessageTemplate())
					.collect(Collectors.toList());
			throw new ValidationsFatalException(allErrors.toString(), new Exception());
		}
	}
	
}
