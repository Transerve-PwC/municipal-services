package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

@ApplicationValidator("length")
@Component
/**
 * A validator that checks the length of stringified value { "type" : "length",
 * "params" : { "min" : 3, "max" : 20 } }
 */
public class LengthValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Length not in valid '%s' for field at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (validation.getType().equalsIgnoreCase("length")) {
			boolean isEmpty = value == null || value.toString().trim().length() == 0;
			if (!field.isRequired() && isEmpty) {
				return null;
			}

			String trimmedValue = isEmpty ? null : value.toString().trim();
			if (!isValid(validation, trimmedValue)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private boolean isValid(IValidation validation, String fieldValue) {
		if (null != fieldValue && !fieldValue.isEmpty()) {
			int max = (int) validation.getParams().get("max");
			int min = (int) validation.getParams().get("min");

			if (fieldValue.length() < min || fieldValue.length() > max) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

}