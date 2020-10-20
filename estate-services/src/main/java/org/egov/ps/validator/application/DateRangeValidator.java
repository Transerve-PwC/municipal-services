package org.egov.ps.validator.application;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.ps.annotation.ApplicationValidator;
import org.egov.ps.validator.IApplicationField;
import org.egov.ps.validator.IApplicationValidator;
import org.egov.ps.validator.IValidation;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/*
 * date-range : Validate based on start and end dates, start and end dates could be strings like “now”, 
 * 																							“1 day ago”, 
 * 																							“6 months ago” 
 * 																							or dates like “01-Jan-2020”, “23-Sep-2020” etc. 
 * example: 
 * { "type" : "date-range", "params" : { "start" : "01-Jan-2020", "end" : "23-Sep-2020" } }
 * { "type" : "date-range", "params": { "start" : { "unit" : "month", "value" : -6}, "end": { "unit": "time", "value" : now }}}
 * The above validator config should make sure the value is in the last 6 months and no before or no after.
 * 
 */

@ApplicationValidator("date-range")
@Component
@Slf4j
public class DateRangeValidator implements IApplicationValidator {

	private static final String DEFAULT_FORMAT = "Invalid date '%s' at path '%s' for range";
	SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

	private List<String> formatErrorMessage(String format, Object value, String path) {
		if (format == null) {
			format = DEFAULT_FORMAT;
		}
		return Arrays.asList(String.format(format, value, path));
	}

	@Override
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent) {
		if (validation.getType().equalsIgnoreCase("date-range")) {
			if (null != validation.getParams() && validation.getParams().size() > 0) {

				boolean isEmpty = value == null || value.toString().trim().length() == 0;
				if (!field.isRequired() && isEmpty) {
					return null;
				}

				Long trimmedValue = null;
				try {
					trimmedValue = isEmpty ? null : Long.parseLong(value.toString().trim());
				} catch (Exception e) {
					return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
				}

				if (!isValid(validation, trimmedValue)) {
					return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
				}
			} else {
				return this.formatErrorMessage(validation.getErrorMessageFormat(), value, field.getPath());
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean isValid(IValidation validation, Long fieldValue) {
		try {
			if (null != fieldValue) {
				Date actualDateValidat = new Date((long) fieldValue);

				String startDateStr = null;
				String endDateStr = null;

				if (validation.getParams().get("start") instanceof String) {
					startDateStr = validation.getParams().get("start").toString();
				}

				if (validation.getParams().get("end") instanceof String) {
					endDateStr = validation.getParams().get("end").toString();
				}

				if (null == startDateStr && validation.getParams().get("start") instanceof Object) {
					Map<String, Object> startDt = (Map<String, Object>) validation.getParams().get("start");
					startDateStr = calculateDate(startDt);
				}

				if (null == endDateStr && validation.getParams().get("end") instanceof Object) {
					Map<String, Object> endDt = (Map<String, Object>) validation.getParams().get("end");
					endDateStr = calculateDate(endDt);
				}

				// ###################################

				// checking null of any start or end date then return false.
				if (null == startDateStr || null == endDateStr) {
					return false;
				}

				// checking start date must be smaller then end date.

				Date startDate = formatter.parse(startDateStr);
				Date endDate = formatter.parse(endDateStr);

				/*
				 * start date occurs before end date - if ( startDate.compareTo(endDate) < 0 )
				 * start date occurs after end date - if ( startDate.compareTo(endDate) > 0 )
				 * Both dates are equal dates - if (startDate.compareTo(endDate) == 0 )
				 */

				if (startDate.compareTo(endDate) > 0 || startDate.compareTo(endDate) == 0) {
					return false;
				}

				if (!(startDate.compareTo(actualDateValidat) < 0 && endDate.compareTo(actualDateValidat) > 0)) {
					return false;
				}

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			log.error("Error while validating date " + fieldValue, e);
			return false;
		}
	}

	private String calculateDate(Map<String, Object> values) {
		Date d = null;
		if (null != values.get("k")) {
			String unit = values.get("k").toString();
			int val = Integer.parseInt(values.get("v").toString());

			if (unit.equalsIgnoreCase("month")) {
				d = diffDate(Calendar.MONTH, val);
			} else if (unit.equalsIgnoreCase("year")) {
				d = diffDate(Calendar.YEAR, val);
			} else if (unit.equalsIgnoreCase("date")) {
				d = diffDate(Calendar.DATE, val);
			} else if (unit.equalsIgnoreCase("second")) {
				d = diffDate(Calendar.SECOND, val);
			}
		}

		if (null != values.get("unit")) {
			String unit = values.get("unit").toString();
			int val = Integer.parseInt(values.get("value").toString());

			if (unit.equalsIgnoreCase("month")) {
				d = diffDate(Calendar.MONTH, val);
			} else if (unit.equalsIgnoreCase("year")) {
				d = diffDate(Calendar.YEAR, val);
			} else if (unit.equalsIgnoreCase("date")) {
				d = diffDate(Calendar.DATE, val);
			} else if (unit.equalsIgnoreCase("second")) {
				d = diffDate(Calendar.SECOND, val);
			}
		}

		if (null != d) {
			return formatter.format(d);
		}
		return null;
	}

	private Date diffDate(int unit, int val) {
		Calendar c = Calendar.getInstance();
		c.add(unit, val);

		return c.getTime();
	}

	@SuppressWarnings("unused")
	private Map<String, Object> getFieldNamesAndValues(Object obj) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Class<? extends Object> c1 = obj.getClass();
			Field[] fields = c1.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				String name = fields[i].getName();
				fields[i].setAccessible(true);
				Object value;
				value = fields[i].get(obj);
				map.put(name, value);
			}
		} catch (Exception e) {
			log.error("Error getting field names and values", e);
		}
		return map;
	}

}
