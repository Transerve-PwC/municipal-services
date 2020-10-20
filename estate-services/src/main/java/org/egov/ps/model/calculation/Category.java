package org.egov.ps.model.calculation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {

	FEE("FEE"),

	TAX("TAX"),

	PRINCIPAL("PRINCIPAL"),

	ADVANCE_COLLECTION("ADVANCE_COLLECTION"),

	INTEREST("INTEREST");

	private String value;

	Category(String value) {
		this.value = value;
	}

	@Override
	@JsonValue
	public String toString() {
		return String.valueOf(value);
	}

	@JsonCreator
	public static Category fromValue(String text) {
		for (Category b : Category.values()) {
			if (String.valueOf(b.value).equals(text)) {
				return b;
			}
		}
		return null;
	}
}
