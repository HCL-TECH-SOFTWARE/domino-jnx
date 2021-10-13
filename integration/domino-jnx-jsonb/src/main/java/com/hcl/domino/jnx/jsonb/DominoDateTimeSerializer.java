package com.hcl.domino.jnx.jsonb;

import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.DominoDateTime;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

public class DominoDateTimeSerializer implements JsonbSerializer<DominoDateTime> {
  public static final DominoDateTimeSerializer INSTANCE = new DominoDateTimeSerializer();
  
  @Override
  public void serialize(DominoDateTime obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(JsonUtil.toIsoString(obj));
  }

}
