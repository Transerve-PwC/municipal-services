package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

@ApplicationValidator("object")
@Component
public class ObjectValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid object '%s' at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (validation.getType().equalsIgnoreCase("object")) {
			if (!field.isRequired()) {
				return null;
			}
			if (!isValid(value)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private static boolean isValid(Object objectValue) {
		if (objectValue != null)
			return true;
		else
			return false;

	}

}
