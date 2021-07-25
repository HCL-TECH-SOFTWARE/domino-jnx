/*
 * ==========================================================================
 * Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.hcl.domino.jnx.vertx.json.service.VertxJsonSerializerFactory;
import com.hcl.domino.json.JsonSerializerFactory;
import com.hcl.domino.misc.JNXServiceFinder;

import it.com.hcl.domino.test.AbstractNotesRuntimeTest;

@SuppressWarnings("nls")
public class TestVertxJsonSerialization extends AbstractNotesRuntimeTest {
  @Test
  public void testServiceRegistered() {
    final Optional<JsonSerializerFactory> fac = JNXServiceFinder.findServices(JsonSerializerFactory.class)
        .filter(VertxJsonSerializerFactory.class::isInstance)
        .findFirst();
    Assertions.assertTrue(fac.isPresent(), "VertxJsonSerializerFactory not registered");
  }

}
