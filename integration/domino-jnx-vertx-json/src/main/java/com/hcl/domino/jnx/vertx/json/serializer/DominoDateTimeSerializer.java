package com.hcl.domino.jnx.vertx.json.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.DominoDateTime;

public class DominoDateTimeSerializer extends StdSerializer<DominoDateTime> {
  private static final long serialVersionUID = 1L;

  public DominoDateTimeSerializer() {
    super(DominoDateTime.class);
  }

  @Override
  public void serialize(DominoDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(JsonUtil.toIsoString(value));
  }


}
