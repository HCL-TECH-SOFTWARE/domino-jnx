package com.hcl.domino.jnx.vertx.json.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.DominoDateRange;

public class DominoDateRangeSerializer extends StdSerializer<DominoDateRange> {
  private static final long serialVersionUID = 1L;

  public DominoDateRangeSerializer() {
    super(DominoDateRange.class);
  }

  @Override
  public void serialize(DominoDateRange value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(JsonUtil.toIsoString(value));
  }


}
