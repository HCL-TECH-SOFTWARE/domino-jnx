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
package com.hcl.domino.jnx.jsonb;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.json.JsonDeserializer.CustomProcessor;
import com.hcl.domino.json.JsonSerializer;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

public class DocumentJsonbDeserializer implements JsonbDeserializer<Document> {
  public static class Builder {
    private final Database database;
    private final Document document;
    private boolean detectDateTime = false;
    private Collection<String> dateTimeItems = Collections.emptyList();
    private Object trueValue = 1;
    private Object falseValue = 0;
    private boolean removeMissingItems = false;
    private Map<String, CustomProcessor> customProcessors = new HashMap<>();

    private Builder(final Database database) {
      this.database = database;
      this.document = null;
    }

    private Builder(final Document document) {
      this.database = null;
      this.document = document;
    }

    /**
     * Configures the values to be stored in the target document when the
     * deserializer
     * encounters JSON boolean values.
     * 
     * @param trueValue  the value used when converting {@code true}
     * @param falseValue the value used when converting {@code false}
     * @return this builder
     */
    public Builder booleanValues(final Object trueValue, final Object falseValue) {
      this.trueValue = trueValue;
      this.falseValue = falseValue;
      return this;
    }

    public DocumentJsonbDeserializer build() {
      final DocumentJsonbDeserializer result = new DocumentJsonbDeserializer(this.database, this.document);
      result.detectDateTime = this.detectDateTime;
      result.dateTimeItems = JsonUtil.toInsensitiveSet(this.dateTimeItems);
      result.trueValue = this.trueValue;
      result.falseValue = this.falseValue;
      result.removeMissingItems = this.removeMissingItems;
      result.customProcessors = this.customProcessors;
      return result;
    }

    /**
     * Specifies a {@link Map} of custom processors that will be applies when
     * matching property names are encountered.
     * 
     * @param customProcessors a {@link Map} of {@code String} property names to
     *                         {@link CustomProcessor}s to apply
     * @return this builder
     * @since 1.0.28
     */
    public Builder customProcessors(final Map<String, CustomProcessor> customProcessors) {
      this.customProcessors = customProcessors;
      return this;
    }

    /**
     * Configures the deserializer to expect the named items to contain date/time
     * values.
     * <p>
     * This will cause such values to be parsed as date/times and ranges compatible
     * with
     * the output of {@link JsonSerializer} and to throw an exception when the value
     * in the
     * incoming JSON object is not either a valid value or empty.
     * </p>
     * 
     * @param dateTimeItems the names of items expected to contain date/time values
     * @return this builder
     */
    public Builder dateTimeItems(final Collection<String> dateTimeItems) {
      this.dateTimeItems = dateTimeItems;
      return this;
    }

    /**
     * Indicates whether date/time value should be detected automatically.
     * <p>
     * Setting this to {@code true} means that the deserializer will check string
     * values to see if they are
     * valid ISO dates, times, or offset date/times and store them as date/time
     * items if so.
     * 
     * @param detectDateTime whether the deserializer should attempt to detect
     *                       date/time string values
     * @return this builder
     */
    public Builder detectDateTime(final boolean detectDateTime) {
      this.detectDateTime = detectDateTime;
      return this;
    }

    /**
     * Configures whether items in a document provided via
     * {@link DocumentJsonbDeserializer#newBuilder(Document)}
     * that do not exist in the incoming JSON should be removed.
     * <p>
     * The default behavior is to only update items that are represented as
     * properties
     * in the incoming JSON.
     * </p>
     * 
     * @param removeMissingItems whether items not present in JSON should be removed
     * @return this builder
     */
    public Builder removeMissingItems(final boolean removeMissingItems) {
      this.removeMissingItems = removeMissingItems;
      return this;
    }
  }

  /**
   * Creates a new deserializer configuration builder.
   * 
   * @param database the target database to store deserialized documents
   * @return a new deserializer builder
   */
  public static Builder newBuilder(final Database database) {
    return new Builder(database);
  }

  /**
   * Creates a new deserializer configuration builder.
   * 
   * @param document the target document to serialize the JSON to
   * @return a new deserializer builder
   * @since 1.0.9
   */
  public static Builder newBuilder(final Document document) {
    return new Builder(document);
  }

  /**
   * Creates a new deserializer with the default configuration.
   * 
   * @param database the target database to store deserialized documents
   * @return the newly-constructed serializer
   */
  public static DocumentJsonbDeserializer newDeserializer(final Database database) {
    return new Builder(database).build();
  }

  /**
   * Creates a new deserializer with the default configuration.
   * 
   * @param document the target document to serialize the JSON to
   * @return the newly-constructed serializer
   */
  public static DocumentJsonbDeserializer newDeserializer(final Document document) {
    return new Builder(document).build();
  }

  private final Database database;
  private final Document document;
  private boolean detectDateTime;
  private Collection<String> dateTimeItems = Collections.emptyList();
  private Object trueValue;
  private Object falseValue;
  private boolean removeMissingItems = false;
  private Map<String, CustomProcessor> customProcessors;

  private DocumentJsonbDeserializer(final Database database, final Document document) {
    this.database = database;
    this.document = document;
  }

  private boolean areCompatibleTypes(final ValueType a, final ValueType b) {
    if (a == ValueType.NULL || b == ValueType.NULL) {
      return true;
    } else if (a == ValueType.TRUE && b == ValueType.FALSE) {
      return true;
    } else if (a == ValueType.FALSE && b == ValueType.TRUE) {
      return true;
    } else {
      return a == b;
    }
  }

  private Object convertObjectValue(final JsonObject objectValue) {
    // TODO implement/don't think about
    // This could be a TEXT item (summary or no) or MIME, or it could just throw an
    // exception
    throw new IllegalArgumentException(
        MessageFormat.format(Messages.getString("DocumentJsonbDeserializer.unsupportedObjectValue"), objectValue)); //$NON-NLS-1$
  }

  @Override
  public Document deserialize(final JsonParser parser, final DeserializationContext ctx, final Type rtType) {

    final Document doc = this.document == null ? this.database.createDocument() : this.document;
    final Set<String> processedNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    parser.getObjectStream()
        .filter(entry -> !JsonSerializer.PROP_METADATA.equals(entry.getKey()))
        .forEach(entry -> {
          final String itemName = entry.getKey();
          processedNames.add(itemName);
          final JsonValue value = entry.getValue();

          final CustomProcessor proc = this.customProcessors == null ? null : this.customProcessors.get(itemName);

          final ValueType type = value == null ? ValueType.NULL : value.getValueType();
          switch (type) {
            case NUMBER:
              if (proc != null) {
                proc.apply(((JsonNumber) value).doubleValue(), itemName, doc);
              } else {
                doc.replaceItemValue(itemName, ((JsonNumber) value).doubleValue());
              }
              break;
            case STRING: {
              final String val = ((JsonString) value).getString();
              if (proc != null) {
                proc.apply(val, itemName, doc);
              } else {
                doc.replaceItemValue(itemName, JsonUtil.convertStringValue(doc.getParentDatabase().getParentDominoClient(),
                    this.detectDateTime, this.dateTimeItems, itemName, val));
              }
              break;
            }
            case TRUE:
              if (proc != null) {
                proc.apply(true, itemName, doc);
              } else {
                doc.replaceItemValue(itemName, this.trueValue);
              }
              break;
            case FALSE:
              if (proc != null) {
                proc.apply(false, itemName, doc);
              } else {
                doc.replaceItemValue(itemName, this.falseValue);
              }
              break;
            case ARRAY: {
              final JsonArray arr = (JsonArray) value;
              final List<Object> result = new ArrayList<>(arr.size());
              ValueType arrayType = ValueType.NULL;
              for (int i = 0; i < arr.size(); i++) {
                final JsonValue arrVal = arr.get(i);
                final ValueType arrValType = arrVal.getValueType();
                if (arrayType == ValueType.NULL) {
                  arrayType = arrValType;
                } else if (arrVal != null && arrValType != ValueType.NULL) {
                  if (!this.areCompatibleTypes(arrayType, arrValType)) {
                    throw new IllegalArgumentException(
                        MessageFormat.format(Messages.getString("DocumentJsonbDeserializer.unsupportedMixedArray"), arr)); //$NON-NLS-1$
                  }
                }
                switch (arrValType) {
                  case NUMBER:
                    result.add(((JsonNumber) arrVal).doubleValue());
                    break;
                  case STRING: {
                    final String val = ((JsonString) arrVal).getString();
                    result.add(JsonUtil.convertStringValue(doc.getParentDatabase().getParentDominoClient(), this.detectDateTime,
                        this.dateTimeItems, itemName, val));
                    break;
                  }
                  case TRUE: {
                    result.add(this.trueValue);
                    break;
                  }
                  case FALSE: {
                    result.add(this.falseValue);
                    break;
                  }
                  case ARRAY: {
                    throw new IllegalArgumentException(
                        MessageFormat.format(Messages.getString("DocumentJsonbDeserializer.unsupportedEmbeddedArray"), arr)); //$NON-NLS-1$
                  }
                  case OBJECT: {
                    final Object val = this.convertObjectValue((JsonObject) arrVal);
                    if (proc != null) {
                      proc.apply(val, itemName, doc);
                    } else {
                      doc.replaceItemValue(itemName, val);
                    }
                    return;
                  }
                  default:
                  case NULL: {
                    if (proc != null) {
                      proc.apply(null, itemName, doc);
                    } else {
                      doc.replaceItemValue(itemName, null);
                    }
                    return;
                  }
                }
              }
              doc.replaceItemValue(itemName, result);

              break;
            }
            case OBJECT: {
              final Object val = this.convertObjectValue((JsonObject) value);
              doc.replaceItemValue(itemName, val);
              break;
            }
            case NULL:
            default:
              break;
          }
        });

    if (this.document != null && this.removeMissingItems) {
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
