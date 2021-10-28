package com.hcl.domino.jnx.vertx.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OptInputStreamToBase64Serializer extends JsonSerializer<Optional<InputStream>> {
  public static final OptInputStreamToBase64Serializer INSTANCE = new OptInputStreamToBase64Serializer();

  @Override
  public void serialize(Optional<InputStream> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if(value.isPresent()) {
      byte[] val = IOUtils.toByteArray(value.get());
      String b64 = Base64.getEncoder().encodeToString(val);
      gen.writeString(b64);
    } else {
      gen.writeNull();
    }
  }

}
