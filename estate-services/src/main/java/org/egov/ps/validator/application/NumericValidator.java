package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

@ApplicationValidator("numeric")
@Component
public class NumericValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid value '%s' for field at path '%s'";
	private static final String numericRegex = "[0-9]+";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {

		if (validation.getType().equalsIgnoreCase("numeric")) {
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
			Pattern p = Pattern.compile(numericRegex);
			return p.matcher(fieldValue).matches();
		} else {
			return false;
		}

	}
}