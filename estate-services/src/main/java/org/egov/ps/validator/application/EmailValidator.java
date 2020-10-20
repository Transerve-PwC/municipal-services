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

@ApplicationValidator("email")
@Component
public class EmailValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid email '%s' at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	// private static final String emailRegex =
	// "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
	private static final String emailRegex = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";

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

	private boolean isValid(String email) {
		if (null != email && !email.isEmpty()) {
			if (email.split("@").length != 2) {
				return false;
			}
			Pattern pattern = Pattern.compile(emailRegex);
			Matcher matcher = pattern.matcher(email);
			return matcher.matches();

		} else {
			return false;
		}

	}

}
