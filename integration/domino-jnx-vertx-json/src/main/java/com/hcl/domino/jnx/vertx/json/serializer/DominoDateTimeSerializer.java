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
