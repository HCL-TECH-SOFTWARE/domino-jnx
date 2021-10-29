package com.hcl.domino.jnx.vertx.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.hcl.domino.design.JavaAgentAndLibrarySupport;

/**
 * Translates the {@code List<String>} of embedded JAR names in a {@link JavaAgentAndLibrarySupport}
 * into a map of names to Base64'd content.
 * 
 * @author Jesse Gallagher
 * @since 1.0.43
 */
public class JavaAgentContentEmbeddedJarsSerializer extends JsonSerializer<List<String>> {

  @Override
  public void serialize(List<String> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    Object ctx = gen.getOutputContext().getCurrentValue();
    if(ctx instanceof JavaAgentAndLibrarySupport) {
      JavaAgentAndLibrarySupport javaAgent = (JavaAgentAndLibrarySupport)ctx;
      
      gen.writeStartObject();
      for(String jar : value) {
        Optional<InputStream> content = javaAgent.getEmbeddedJar(jar);
        gen.writeFieldName(jar);
        OptInputStreamToBase64Serializer.INSTANCE.serialize(content, gen, serializers);
      }
      gen.writeEndObject();
    }
  }

}
