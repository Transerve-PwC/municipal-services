package org.egov.cpt.util;

import java.io.IOException;

import org.egov.cpt.models.Property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A serializer class to work around the cyclic reference between Property and
 * Owner classes.
 */
public class PropertySerializer extends StdSerializer<Property> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PropertySerializer() {
		this(null);
	}

	public PropertySerializer(Class<Property> t) {
		super(t);
	}

	@Override
	public void serialize(Property property, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		generator.writeStartObject();
		generator.writeFieldName("id");
		generator.writeString(property.getId());
		generator.writeFieldName("transitNumber");
		generator.writeString(property.getTransitNumber());
		generator.writeFieldName("colony");
		generator.writeString(property.getColony());
		generator.writeFieldName("pincode");
		generator.writeString(property.getPincode());
		generator.writeFieldName("area");
		generator.writeString(property.getArea());
		generator.writeFieldName("rentSummary");
		generator.writeObject(property.getRentSummary());
		generator.writeEndObject();
	}
}
