/*
 * ==========================================================================
 * Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.domino.jnx.vertx.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jnx.vertx.json.deserializer.DominoDateRangeDeserializer;
import com.hcl.domino.jnx.vertx.json.deserializer.DominoDateTimeDeserializer;
import com.hcl.domino.jnx.vertx.json.serializer.DominoDateRangeSerializer;
import com.hcl.domino.jnx.vertx.json.serializer.DominoDateTimeSerializer;
import com.hcl.domino.jnx.vertx.json.serializer.GenericSIGRecordSerializer;

public class JnxTypesModule extends SimpleModule {
  private static final long serialVersionUID = 1L;

  public JnxTypesModule() {
    super("Domino JNX Types"); //$NON-NLS-1$
    
    addSerializer(new DominoDateTimeSerializer());
    addSerializer(new DominoDateRangeSerializer());
    addSerializer(new GenericSIGRecordSerializer());
    addDeserializer(DominoDateTime.class, new DominoDateTimeDeserializer((Class<DominoDateTime>)DominoDateTime.class));
    addDeserializer(DominoDateRange.class, new DominoDateRangeDeserializer((Class<DominoDateRange>)DominoDateRange.class));
  }

  @Override
  public void setupModule(SetupContext context) {
    context.addBeanSerializerModifier(new BeanSerializerModifier() {

      @Override
      public JsonSerializer<?> modifySerializer(
          SerializationConfig config,
          BeanDescription beanDesc,
          JsonSerializer<?> serializer) {

        if (serializer instanceof BeanSerializerBase) {
          return new AgentOrLibraryLanguageSerializer(
              (BeanSerializerBase) serializer);
        }
        else {
          return serializer; 
        }
      }             
    });
    
    super.setupModule(context);
  }


}
