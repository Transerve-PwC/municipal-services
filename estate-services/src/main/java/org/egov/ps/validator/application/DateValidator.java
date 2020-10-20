package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

@ApplicationValidator("date")
@Component
public class DateValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid date '%s' for field at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {

		if (validation.getType().equalsIgnoreCase("date")) {
			boolean isEmpty = value == null || value.toString().trim().length() == 0;
			if (!field.isRequired() || isEmpty) {
				return null;
			}
			Long trimmedValue = null;
			try {
				trimmedValue = isEmpty ? null : Long.parseLong(value.toString().trim());
			} catch (Exception e) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}

			if (!isValid(trimmedValue)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private static boolean isValid(Long strDate) {
		if (null != strDate) {
			return true;
		} else {
			return false;
		}
	}

}