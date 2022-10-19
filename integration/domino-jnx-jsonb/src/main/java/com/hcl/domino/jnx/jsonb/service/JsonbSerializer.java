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
package com.hcl.domino.jnx.jsonb.service;

import com.hcl.domino.commons.json.AbstractJsonSerializer;
import com.hcl.domino.data.Document;
import com.hcl.domino.jnx.jsonb.DocumentJsonbSerializer;
import com.hcl.domino.jnx.jsonb.DominoDateRangeSerializer;
import com.hcl.domino.jnx.jsonb.DominoDateTimeSerializer;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

public class JsonbSerializer extends AbstractJsonSerializer {
  @Override
  public Object toJson(final Document doc) {
    final Jsonb jsonb = buildSerializer();
    return jsonb.toJson(doc, Document.class);
  }
  
  @Override
  public Object toJson(final Object value) {
    final Jsonb jsonb = buildSerializer();
    return jsonb.toJson(value);
  }
  
  private Jsonb buildSerializer() {
    return JsonbBuilder.newBuilder()
      .withConfig(
          new JsonbConfig()
              .withSerializers(
                  DocumentJsonbSerializer.newBuilder()
                      .excludeItems(this.skippedItemNames)
                      .excludeTypes(this.excludedTypes)
                      .includeItems(this.includedItemNames)
                      .includeMetadata(this.includeMetadata)
                      .lowercaseProperties(this.lowercaseProperties)
                      .booleanItemNames(this.booleanItemNames)
                      .booleanTrueValues(this.booleanTrueValues)
                      .dateRangeFormat(this.dateRangeFormat)
                      .richTextHtmlOptions(this.htmlConvertOptions)
                      .customProcessors(this.customProcessors)
                      .metaOnly(this.metaOnly)
                      .build(),
                  DominoDateTimeSerializer.INSTANCE,
                  DominoDateRangeSerializer.INSTANCE
               )
              )
      .build();
  }
}
