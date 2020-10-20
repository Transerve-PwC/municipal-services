package org.egov.ps.validator.application;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

@ApplicationValidator("array-length")
@Component
public class ArrayLengthValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid array length '%s' at path '%s'";

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {

		if (validation.getType().equalsIgnoreCase("array-length")) {
			boolean isEmpty = value == null || value.toString().trim().length() == 0;
			if (!field.isRequired() && isEmpty) {
				return null;
			}
			if (!isValid(validation, value)) {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	private static boolean isValid(IValidation validation, Object objectValue) {
		if (objectValue != null) {
			boolean checkObject = objectValue.getClass().isArray();
			if (checkObject == true) {
				int arrayLength = Array.getLength(objectValue);
				int max = (int) validation.getParams().get("max");
				int min = (int) validation.getParams().get("min");

				if (arrayLength < min || arrayLength > max) {
					return false;
				}
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}

	}

}