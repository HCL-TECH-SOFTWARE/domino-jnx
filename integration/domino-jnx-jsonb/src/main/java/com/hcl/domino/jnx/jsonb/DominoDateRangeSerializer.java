package com.hcl.domino.jnx.jsonb;

import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.DominoDateRange;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

public class DominoDateRangeSerializer implements JsonbSerializer<DominoDateRange> {
  public static final DominoDateRangeSerializer INSTANCE = new DominoDateRangeSerializer();

  @Override
  public void serialize(DominoDateRange obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(JsonUtil.toIsoString(obj));
  }

}
