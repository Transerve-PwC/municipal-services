package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

/*
 * { "type" : "minmax", "params" : { "min" : 0, "max" : 100 }
 */
@ApplicationValidator("minmax")
@Component
public class MinMaxValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Value '%s' at path '%s' is out of min max range configured";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (validation.getType().equalsIgnoreCase("minmax")) {
			boolean isEmpty = value == null || value.toString().trim().length() == 0;
			if (!field.isRequired() && isEmpty) {
				return null;
			}

			Integer trimmedValue = null;
			try {
				trimmedValue = isEmpty ? null : Integer.parseInt(value.toString().trim());
			} catch (Exception e) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}

			if (!isValid(validation, trimmedValue)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private boolean isValid(IValidation validation, Integer fieldValue) {
		if (null != fieldValue) {
			int max = (int) validation.getParams().get("max");
			int min = (int) validation.getParams().get("min");

			if (fieldValue < min || fieldValue > max) {
				return false;
			}
			return true;
		} else {
			return false;
		}

	}

}
