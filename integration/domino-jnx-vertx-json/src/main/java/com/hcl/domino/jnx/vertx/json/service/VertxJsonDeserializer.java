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
package com.hcl.domino.jnx.vertx.json.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.hcl.domino.commons.json.AbstractJsonDeserializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.jnx.vertx.json.JnxTypesModule;
import com.hcl.domino.json.JsonSerializer;
import io.vertx.core.json.JsonObject;

/**
 * @author Jesse Gallagher
 * @since 1.0.9
 */
public class VertxJsonDeserializer extends AbstractJsonDeserializer {
  
  public VertxJsonDeserializer() {
    io.vertx.core.json.jackson.DatabindCodec.mapper().registerModule(new JnxTypesModule());
  }
  
  private boolean areCompatibleTypes(final Object a, final Object b) {
    if (a == null || b == null) {
      return true;
    } else {
      return a.getClass().equals(b.getClass());
    }
  }

  @Override
  public Document fromJson(final String json) {
    final JsonObject jsonObj = new JsonObject(json);
    final Document doc = this.targetDocument == null ? this.targetDatabase.createDocument() : this.targetDocument;
    final Set<String> processedNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    jsonObj.stream()
        .filter(entry -> !JsonSerializer.PROP_METADATA.equals(entry.getKey()))
        .forEach(entry -> {
          final String itemName = entry.getKey();
          processedNames.add(itemName);
          final Object value = entry.getValue();

          if (this.customProcessors.containsKey(itemName)) {
            this.customProcessors.get(itemName).apply(value, itemName, doc);
            return;
          }

          if (value == null) {
            doc.replaceItemValue(itemName, null);
          } else if (value instanceof Number) {
            doc.replaceItemValue(itemName, value);
          } else if (value instanceof CharSequence) {
            doc.replaceItemValue(itemName, JsonUtil.convertStringValue(doc.getParentDatabase().getParentDominoClient(),
                this.detectDateTime, this.dateTimeItems, itemName, value.toString()));
          } else if (value instanceof Boolean || boolean.class.equals(value.getClass())) {
            doc.replaceItemValue(itemName, (Boolean) value ? this.trueValue : this.falseValue);
          } else if (value instanceof Iterable) {
            final List<Object> arr = new ArrayList<>();
            Object arrayType = null;
            final List<Object> result = new ArrayList<>(arr.size());
            @SuppressWarnings("unchecked")
            final Iterator<Object> iter = ((Iterable<Object>) value).iterator();
            while (iter.hasNext()) {
              final Object arrVal = iter.next();
              if (arrVal == null) {
                result.add(null);
              } else if (!this.areCompatibleTypes(arrVal, arrayType)) {
                throw new IllegalArgumentException(MessageFormat.format("Encountered unsupported mixed array value: {0}", arr));
              } else {
                arrayType = arrVal;
                if (arrVal instanceof Number) {
                  result.add(arrVal);
                } else if (arrVal instanceof CharSequence) {
                  result.add(JsonUtil.convertStringValue(doc.getParentDatabase().getParentDominoClient(), this.detectDateTime,
                      this.dateTimeItems, itemName, arrVal.toString()));
                } else if (arrVal instanceof Boolean || boolean.class.equals(arrVal.getClass())) {
                  result.add((Boolean) arrVal ? this.trueValue : this.falseValue);
                }
              }
            }
            doc.replaceItemValue(itemName, result);
          } else {
            throw new IllegalArgumentException(MessageFormat.format("Encountered unsupported JSON item value: {0}", value));
          }
        });

    if (this.targetDocument != null && this.removeMissingItems) {
      doc.getItemNames().stream()
          .filter(name -> !"Form".equalsIgnoreCase(name)) //$NON-NLS-1$
          .filter(name -> !processedNames.contains(name))
          .filter(name -> !name.startsWith("$")) //$NON-NLS-1$
          .forEach(name -> {
            doc.removeItem(name);
          });
    }
    return doc;
  }
}
