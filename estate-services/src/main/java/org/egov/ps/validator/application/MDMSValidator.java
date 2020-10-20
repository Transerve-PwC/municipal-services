package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.service.MDMSService;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ApplicationValidator("mdms")
@Component
public class MDMSValidator implements IApplicationValidator {

	@Autowired
	MDMSService mdmsService;

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		Map<String, Object> params = validation.getParams();
		String moduleName = (String) params.get("moduleName");
		String masterName = (String) params.get("masterName");
		String filter = (String) params.get("filter");

		List<String> allowedValues = mdmsService.getMdmsFields(moduleName, masterName, filter);

		if (!allowedValues.stream().filter(allowedValue -> allowedValue.equals(value)).findAny().isPresent()) {
			return Arrays.asList(String.format("Value '%s' not found in expected mdms values [%s] for path '%s'", value,
					StringUtils.join(allowedValues, ","), field.getPath()));
		}
		return null;
	}

}