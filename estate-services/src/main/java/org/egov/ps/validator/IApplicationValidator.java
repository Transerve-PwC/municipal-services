package org.egov.ps.validator;

import java.util.List;

/**
 * @param validation The validation configuration object. Usually contains the
 *                   type of validation and <code>params</code> which is a
 *                   <code>Map<String, Object></code>
 * @param field      The field configuration object. Also has the current
 *                   validation as one among a list.
 * @param value      The value which is being validated. This can be assumed to
 *                   be non empty.
 * @param parent     The parent object. Can be used when this validation depends
 *                   on other fields.
 */
public interface IApplicationValidator {
	public List<String> validate(IValidation validation, IApplicationField field, Object value, Object parent);
}
