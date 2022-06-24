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
package it.com.hcl.domino.test.vertx.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.data.Document;
import com.hcl.domino.jnx.vertx.json.service.VertxJsonDeserializer;
import com.hcl.domino.jnx.vertx.json.service.VertxJsonSerializerFactory;
import com.hcl.domino.json.JsonDeserializer;
import com.hcl.domino.json.JsonSerializerFactory;
import com.hcl.domino.misc.JNXServiceFinder;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestVertxJsonDeserialization extends AbstractNotesRuntimeTest {
  @Test
  public void testServiceRegistered() {
    final Optional<JsonSerializerFactory> fac = JNXServiceFinder.findServices(JsonSerializerFactory.class)
        .filter(VertxJsonSerializerFactory.class::isInstance)
        .findFirst();
    Assertions.assertTrue(fac.isPresent(), "VertxJsonSerializerFactory not registered");
  }
  
  @Test
  public void testInstantHandling() throws Exception {
    this.withTempDb(database -> {
      JsonDeserializer deserializer = new VertxJsonDeserializer();
      deserializer.target(database);
      deserializer.detectDateTime(true);

      Map<String, Object> obj = new HashMap<>();
      obj.put("Form", "Hello");
      LocalDate localDate = LocalDate.of(2022, 6, 24);
      LocalTime localTime = LocalTime.of(11, 57, 20);
      OffsetDateTime dt = OffsetDateTime.of(localDate, localTime, ZoneOffset.UTC);
      Instant time = dt.toInstant();
      obj.put("Time", time);

      Document doc = deserializer.fromJson(obj);
      
      assertEquals("Hello", doc.get("Form", String.class, ""));
      assertEquals(time, doc.get("Time", Instant.class, null));

    });
  }
}
