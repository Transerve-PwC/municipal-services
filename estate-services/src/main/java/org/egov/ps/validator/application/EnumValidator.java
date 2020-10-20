package org.egov.ps.validator.application;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IValidation;
import org.egov.ps.validator.IApplicationValidator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@ApplicationValidator("enum")
@Component
@Slf4j

/**
 * Validate given value against a specified set of values as mentioned in the
 * 'values' key of field config.
 */
public class EnumValidator implements IApplicationValidator {

    private static final String KEY = "values";

    @Override
    @SuppressWarnings("unchecked")
    public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
        final Map<String, Object> params = validation.getParams();
        if (!params.containsKey(KEY)) {
            log.error("Misconfiguration for path '{}'' in field config. Expected 'values' array but none found");
            return null;
        }
        List<String> allowedValues = (List<String>) params.get("values");
        if (!allowedValues.stream().filter(allowedValue -> allowedValue.equals(value)).findAny().isPresent()) {
            return Arrays.asList(String.format("Value '%s' not found in expected enum values [%s] for path '%s'", value,
                    StringUtils.join(allowedValues, ","), field.getPath()));
        }
        return null;
    }

}