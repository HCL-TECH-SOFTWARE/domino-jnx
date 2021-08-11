package com.hcl.domino.jnx.vertx.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hcl.domino.data.Document;

/**
 * Custom  vertx Json Serializer for Document Object in Designs
 *
 * @since 1.0.32
 */
public class DocumentToUnidSerializer extends JsonSerializer<Document> {
	private static String UNID = "unid";

	@Override
	public void serialize(Document value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeStringField(UNID, value.getUNID());
		gen.writeEndObject();
	}
}
