package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IValidation;
import org.json.JSONArray;
import org.egov.ps.validator.IApplicationValidator;
import org.springframework.stereotype.Component;

@ApplicationValidator("required")
@Component
public class RequiredValidator implements IApplicationValidator {

	private final String DEFAULT_ERROR_FORMAT = "%s is required but found %s. It is empty or doesnot exist";

	/**
	 * if string, then make sure we have atleast 1 non whitespace character. if
	 * object, then make sure it is not null. if array, then make sure it is not
	 * empty.
	 */
	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (!this.isValid(value)) {
			return Arrays.asList(String.format(DEFAULT_ERROR_FORMAT, field.getPath(), value));
		}
		return null;
	}

	private boolean isValid(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			return ((String) value).trim().length() > 0;
		}
		if (value instanceof JSONArray) {
			return ((Object[]) value).length != 0;
		}
		return true;
	}
}
