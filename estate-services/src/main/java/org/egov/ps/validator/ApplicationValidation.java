package org.egov.ps.validator;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationValidation implements IValidation {

    private String type;

    private String errorMessageFormat;

    private Map<String, Object> params;

}