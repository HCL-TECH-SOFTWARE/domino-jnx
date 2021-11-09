package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.hcl.domino.jnx.vertx.json.serializer.DominoDateRangeSerializer;
import com.hcl.domino.jnx.vertx.json.serializer.DominoDateTimeSerializer;

public class JnxTypesModule extends SimpleModule {
  private static final long serialVersionUID = 1L;

  public JnxTypesModule() {
    super("Domino JNX Types"); //$NON-NLS-1$

    addSerializer(new DominoDateTimeSerializer());
    addSerializer(new DominoDateRangeSerializer());
  }

  @Override
  public void setupModule(SetupContext context) {
    context.addBeanSerializerModifier(new BeanSerializerModifier() {

      public JsonSerializer<?> modifySerializer(
          SerializationConfig config,
          BeanDescription beanDesc,
          JsonSerializer<?> serializer) {

        if (serializer instanceof BeanSerializerBase) {
          return new DesignAgentLanguageSerializer(
              (BeanSerializerBase) serializer);
        }
        else {
          return serializer; 
        }
      }                   
    });
  }


}
