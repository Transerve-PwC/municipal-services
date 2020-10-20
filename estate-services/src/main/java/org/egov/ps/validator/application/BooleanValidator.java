package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

@ApplicationValidator("boolean")
@Component
public class BooleanValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid boolean '%s' at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (validation.getType().equalsIgnoreCase("boolean")) {
			boolean isEmpty = value == null || value.toString().trim().length() == 0;
			if (!field.isRequired() && isEmpty) {
				return null;
			}
			String trimmedValue = isEmpty ? null : value.toString().trim();
			if (!isValid(trimmedValue)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private static boolean isValid(String fieldValue) {
		if (null != fieldValue && !fieldValue.isEmpty()) {
			if ("true".equals(fieldValue) || "false".equals(fieldValue) || "1".equals(fieldValue)
					|| "0".equals(fieldValue)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

}
