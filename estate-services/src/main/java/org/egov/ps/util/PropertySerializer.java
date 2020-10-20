package org.egov.ps.util;

import java.io.IOException;

import org.egov.ps.model.Property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A Serializer class to work around the cyclic reference between Property and
 * Application classes.
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
		generator.writeFieldName("fileNumber");
		generator.writeString(property.getFileNumber());
		generator.writeEndObject();
	}
}
