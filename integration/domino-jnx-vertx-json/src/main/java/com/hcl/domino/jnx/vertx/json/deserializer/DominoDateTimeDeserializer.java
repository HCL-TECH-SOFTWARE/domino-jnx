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
package com.hcl.domino.jnx.vertx.json.deserializer;

import java.io.IOException;
import java.util.Iterator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hcl.domino.commons.data.DefaultDominoDateTime;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.DominoDateTime;

public class DominoDateTimeDeserializer extends StdDeserializer<DominoDateTime> {
  private static final long serialVersionUID = 1L;

  public DominoDateTimeDeserializer(Class<DominoDateTime> vc) {
    super(vc);
  }

  @Override
  public DominoDateTime deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    JsonNode node = p.getCodec().readTree(p);
    Iterator<String> fnames = node.fieldNames();
    return DefaultDominoDateTime.from(JsonUtil.tryDateTime(node.get(fnames.next()).asText()));
  }

}
