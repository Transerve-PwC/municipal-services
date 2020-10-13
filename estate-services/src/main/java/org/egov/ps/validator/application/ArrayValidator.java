package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

import net.minidev.json.JSONArray;

@ApplicationValidator("array")
@Component
public class ArrayValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid array  '%s' at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (validation.getType().equalsIgnoreCase("array")) {
			boolean isEmpty = value == null || value.toString().trim().length() == 0;
			if (!field.isRequired() && isEmpty) {
				return null;
			}
			if (!isValid(value)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private static boolean isValid(Object objectValue) {
		if (objectValue == null)
			return false;
		if (objectValue instanceof JSONArray) {
			return true;
		}
		boolean checkObject = objectValue.getClass().isArray();
		if (checkObject == true)
			return checkObject;
		else
			return checkObject;
	}
}