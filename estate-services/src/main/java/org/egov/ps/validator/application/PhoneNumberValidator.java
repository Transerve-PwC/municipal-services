package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IValidation;
import org.egov.ps.validator.IApplicationValidator;
import org.springframework.stereotype.Component;

@ApplicationValidator("mobile")
@Component
public class PhoneNumberValidator implements IApplicationValidator {
	private static final String phoneRegex = "(0/91)?[7-9][0-9]{9}";

	private static String DEFAULT_FORMAT = "Valid phone number expected for path %s but found %s";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		boolean isEmpty = value == null || value.toString().trim().length() == 0;
		if (!field.isRequired() && isEmpty) {
			return null;
		}

		String trimmedValue = isEmpty ? null : value.toString().trim();
		if (!isValid(trimmedValue)) {
			return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
		}
		return null;
	}

	private boolean isValid(String phoneNumber) {
		if (null != phoneNumber && !phoneNumber.isEmpty()) {
			Pattern pattern = Pattern.compile(phoneRegex);
			Matcher matcher = pattern.matcher(phoneNumber);
			return matcher.matches();
		}else {
			return false;
		}

	}
}
