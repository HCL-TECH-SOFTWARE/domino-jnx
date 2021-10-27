package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jnx.vertx.json.deserializer.DominoDateRangeDeserializer;
import com.hcl.domino.jnx.vertx.json.deserializer.DominoDateTimeDeserializer;
import com.hcl.domino.jnx.vertx.json.serializer.DominoDateRangeSerializer;
import com.hcl.domino.jnx.vertx.json.serializer.DominoDateTimeSerializer;

public class JnxTypesModule extends SimpleModule {
  private static final long serialVersionUID = 1L;

  public JnxTypesModule() {
    super("Domino JNX Types"); //$NON-NLS-1$
    
    addSerializer(new DominoDateTimeSerializer());
    addSerializer(new DominoDateRangeSerializer());
    addDeserializer(DominoDateTime.class, new DominoDateTimeDeserializer((Class<DominoDateTime>)DominoDateTime.class));
    addDeserializer(DominoDateRange.class, new DominoDateRangeDeserializer((Class<DominoDateRange>)DominoDateRange.class));
  }

}
