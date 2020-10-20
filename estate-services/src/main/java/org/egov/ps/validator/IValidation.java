package org.egov.ps.validator;

import java.util.Map;

public interface IValidation {

    public String getType();

    public String getErrorMessageFormat();

    public Map<String, Object> getParams();
}