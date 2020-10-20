package org.egov.ps.validator;

import java.util.List;

import com.jayway.jsonpath.DocumentContext;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ApplicationField implements IApplicationField {

    private String path;

    private boolean required;

    private DocumentContext rootObject;

    private List<IValidation> validations;

    private Object value;
}