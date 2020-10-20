package org.egov.ps.validator;

import java.util.List;

import com.jayway.jsonpath.DocumentContext;

public interface IApplicationField {

    public String getPath();

    public boolean isRequired();

    public DocumentContext getRootObject();

    public List<IValidation> getValidations();

    public Object getValue();
}
