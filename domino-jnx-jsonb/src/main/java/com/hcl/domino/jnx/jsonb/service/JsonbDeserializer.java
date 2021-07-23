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
package com.hcl.domino.jnx.jsonb.service;

import com.hcl.domino.commons.json.AbstractJsonDeserializer;
import com.hcl.domino.data.Document;
import com.hcl.domino.jnx.jsonb.DocumentJsonbDeserializer;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

public class JsonbDeserializer extends AbstractJsonDeserializer {

  @Override
  public Document fromJson(final Object json) {
    return this.fromJson(json.toString());
  }

  @Override
  public Document fromJson(final String json) {
    DocumentJsonbDeserializer.Builder builder;
    if (this.targetDocument == null) {
      builder = DocumentJsonbDeserializer.newBuilder(this.targetDatabase);
    } else {
      builder = DocumentJsonbDeserializer.newBuilder(this.targetDocument);
    }
    builder
        .booleanValues(this.trueValue, this.falseValue)
        .dateTimeItems(this.dateTimeItems)
        .removeMissingItems(this.removeMissingItems)
        .detectDateTime(this.detectDateTime)
        .customProcessors(this.customProcessors);

    final Jsonb jsonb = JsonbBuilder.newBuilder()
        .withConfig(
            new JsonbConfig().withDeserializers(builder.build()))
        .build();
    return jsonb.fromJson(json, Document.class);
  }

}
