package org.egov.ps.validator;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class DateField {

	private String unit;
	private String value;
}
