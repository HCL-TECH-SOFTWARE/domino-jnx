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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.json.AbstractJsonSerializer;
import com.hcl.domino.commons.json.JsonUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentClass;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.DominoTimeType;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.exception.EntryNotFoundInIndexException;
import com.hcl.domino.exception.ItemNotFoundException;
import com.hcl.domino.html.HtmlConversionResult;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.html.RichTextHTMLConverter;
import com.hcl.domino.json.DateRangeFormat;
import com.hcl.domino.json.JsonSerializer;
import com.hcl.domino.mime.MimeReader.ReadMimeDataType;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class DocumentJsonbSerializer implements JsonbSerializer<Document> {
  public static class Builder {
    private Collection<String> skippedItemNames;
    private Collection<String> includedItemNames;
    private Collection<ItemDataType> excludedTypes;
    private boolean lowercaseProperties;
    private boolean includeMetadata;
    private Collection<String> booleanItemNames;
    private Collection<Object> booleanTrueValues;
    private DateRangeFormat dateRangeFormat = DateRangeFormat.ISO;
    private Map<HtmlConvertOption, String> htmlConvertOptions;
    protected Map<String, BiFunction<Document, String, Object>> customProcessors = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private Builder() {
    }

    /**
     * Sets the item names that should be considered boolean values when serialized
     * to JSON.
     * <p>
     * Items of type {@link ItemDataType#TYPE_TEXT TEXT},
     * {@link ItemDataType#TYPE_NUMBER NUMBER}, and
     * {@link ItemDataType#TYPE_RFC822_TEXT RFC822_TEXT}, as well as
     * single-value items of type {@link ItemDataType#TYPE_TEXT_LIST TEXT_LIST} or
     * {@link ItemDataType#TYPE_NUMBER_RANGE NUMBER_RANGE} are evaluated for boolean
     * conversion.
     * </p>
     * 
     * @param booleanItemNames the names of items to serialize as boolean
     * @return this builder
     */
    public Builder booleanItemNames(final Collection<String> booleanItemNames) {
      this.booleanItemNames = booleanItemNames;
      return this;
    }

    /**
     * Sets the values used to determine {@code true} and {@code false} when
     * serializing items configured
     * via {@link #booleanItemNames(Collection)}.
     * <p>
     * All values not specified here will be considered {@code false}.
     * </p>
     * 
     * @param trueValues a collection of objects to compare to resolve as
     *                   {@code true}
     * @return this builder
     */
    public Builder booleanTrueValues(final Collection<Object> trueValues) {
      this.booleanTrueValues = trueValues;
      return this;
    }

    /**
     * Constructs a new {@link DocumentJsonbSerializer} based on the configuration
     * of this builder.
     * 
     * @return the newly-constructed serializer
     */
    public DocumentJsonbSerializer build() {
      final DocumentJsonbSerializer result = new DocumentJsonbSerializer();
      result.skippedItemNames = JsonbUtil.toSet(this.skippedItemNames);
      result.includedItemNames = JsonbUtil.toSet(this.includedItemNames);
      if (this.excludedTypes != null) {
        result.excludedTypes = EnumSet.copyOf(this.excludedTypes);
      }

      result.lowercaseProperties = this.lowercaseProperties;
      result.includeMetadata = this.includeMetadata;
      result.booleanItemNames = JsonUtil.toInsensitiveSet(this.booleanItemNames);
      result.booleanTrueValues = this.booleanTrueValues == null ? Collections.emptySet() : this.booleanTrueValues;
      result.dateRangeFormat = this.dateRangeFormat;
      result.htmlConvertOptions = this.htmlConvertOptions;
      result.customProcessors = this.customProcessors;

      return result;
    }

    /**
     * Sets the custom processors for this serializer, which will be called when
     * encountering
     * item names found in the map.
     * 
     * @param customProcessors a {@link Map} of {@link BiFunction} processors
     * @return this builder
     * @since 1.0.28
     */
    public Builder customProcessors(final Map<String, BiFunction<Document, String, Object>> customProcessors) {
      this.customProcessors = customProcessors;
      return this;
    }

    /**
     * Sets the format for date/time range values.
     * 
     * @param format the {@link DateRangeFormat} type to use
     * @return this builder
     */
    public Builder dateRangeFormat(final DateRangeFormat format) {
      this.dateRangeFormat = format == null ? DateRangeFormat.ISO : format;
      return this;
    }

    /**
     * Exclude items by name.
     * 
     * @param skippedItemNames a {@link Collection} of case-insensitive item names,
     *                         or {@code null} to not
     *                         exclude any items
     * @return this builder
     */
    public Builder excludeItems(final Collection<String> skippedItemNames) {
      this.skippedItemNames = skippedItemNames;
      return this;
    }

    /**
     * Exclude item types.
     * 
     * @param excludedTypes a {@link Collection} of item types to exclude, or
     *                      {@code null} to not exclude any
     *                      item types
     * @return this builder
     */
    public Builder excludeTypes(final Collection<ItemDataType> excludedTypes) {
      this.excludedTypes = excludedTypes;
      return this;
    }

    /**
     * Include only specific items by name.
     * <p>
     * Note: {@link #excludeItems} is still applied if both are specified.
     * </p>
     * 
     * @param includedItemNames a {@link Collection} of case-insensitive item names,
     *                          or {@code null} to not
     *                          include only specific items
     * @return this builder
     */
    public Builder includeItems(final Collection<String> includedItemNames) {
      this.includedItemNames = includedItemNames;
      return this;
    }

    /**
     * Sets whether to include a metadata object in the output JSON, using the
     * {@value JsonSerializer#PROP_METADATA} property.
     * {@code false} by default.
     * 
     * @param includeMetadata whether to include a document-metadata object
     * @return this builder
     */
    public Builder includeMetadata(final boolean includeMetadata) {
      this.includeMetadata = includeMetadata;
      return this;
    }

    /**
     * Sets whether property names in the emitted JSON should be lowercased. When
     * {@code false} (the default),
     * emitted properties will match the capitalization of the first of each named
     * item in the source
     * document.
     * 
     * @param lowercaseProperties whether to lowercase property names
     * @return this builder
     */
    public Builder lowercaseProperties(final boolean lowercaseProperties) {
      this.lowercaseProperties = lowercaseProperties;
      return this;
    }

    /**
     * Sets the options to use when converting rich text to HTML.
     * <p>
     * Setting this overrides the default behavior of
     * {@link HtmlConvertOption#XMLCompatibleHTML XMLCompatibleHTML=1}
     * </p>
     * .
     * 
     * @param richTextHtmlOptions the map of options to set
     * @return this builder
     * @since 1.0.27
     */
    public Builder richTextHtmlOptions(final Map<HtmlConvertOption, String> richTextHtmlOptions) {
      this.htmlConvertOptions = richTextHtmlOptions;
      return this;
    }
  }

  /**
   * Creates a new serializer configuration builder.
   * 
   * @return a new serializer builder
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Creates a new serializer with the default configuration.
   * 
   * @return the newly-constructed serializer
   */
  public static DocumentJsonbSerializer newSerializer() {
    return new Builder().build();
  }

  private Collection<String> skippedItemNames;
  private Collection<String> includedItemNames;
  private Collection<ItemDataType> excludedTypes;
  private boolean lowercaseProperties;
  private boolean includeMetadata;
  private Collection<String> booleanItemNames;
  private Collection<Object> booleanTrueValues;
  private DateRangeFormat dateRangeFormat;
  Map<HtmlConvertOption, String> htmlConvertOptions;
  Map<String, BiFunction<Document, String, Object>> customProcessors;

  private DocumentJsonbSerializer() {
  }

  @Override
  public void serialize(final Document obj, final JsonGenerator generator, final SerializationContext ctx) {
    final Set<String> handledItems = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    handledItems.addAll(JsonSerializer.DEFAULT_EXCLUDED_ITEMS);
    if (this.includedItemNames != null) {
      handledItems.removeAll(this.includedItemNames);
    }
    if (this.skippedItemNames != null) {
      handledItems.addAll(this.skippedItemNames);
    }

    generator.writeStartObject();

    if (this.includeMetadata) {
      generator.writeStartObject(JsonSerializer.PROP_METADATA);
      generator.write(JsonSerializer.PROP_META_NOTEID, obj.getNoteID());
      generator.write(JsonSerializer.PROP_META_UNID, obj.getUNID());
      generator.write(JsonSerializer.PROP_META_CREATED, JsonUtil.toIsoString(obj.getCreated()));
      generator.write(JsonSerializer.PROP_META_LASTMODIFIED, JsonUtil.toIsoString(obj.getLastModified()));
      generator.write(JsonSerializer.PROP_META_LASTACCESSED, JsonUtil.toIsoString(obj.getLastAccessed()));
      generator.write(JsonSerializer.PROP_META_LASTMODIFIEDINFILE, JsonUtil.toIsoString(obj.getModifiedInThisFile()));
      generator.write(JsonSerializer.PROP_META_ADDEDTOFILE, JsonUtil.toIsoString(obj.getAddedToFile()));
      generator.writeStartArray(JsonSerializer.PROP_META_NOTECLASS);
      for (final DocumentClass c : obj.getDocumentClass()) {
        generator.write(c.name());
      }
      generator.writeEnd();
      generator.writeEnd();
    }

    obj.forEachItem((item, loop) -> {
      final String itemName = item.getName();
      if (itemName != null && !handledItems.contains(itemName)) {
        handledItems.add(itemName);

        if (this.includedItemNames != null && !this.includedItemNames.contains(itemName)) {
          // Skip
          return;
        }

        final ItemDataType type = item.getType();
        if ((this.excludedTypes != null && this.excludedTypes.contains(type)) || AbstractJsonSerializer.isExcludedField(itemName)) {
          return;
        }

        final String propName = this.lowercaseProperties ? itemName.toLowerCase() : itemName;

        if (this.customProcessors != null && this.customProcessors.containsKey(itemName)) {
          final Object val = this.customProcessors.get(itemName).apply(obj, itemName);
          this.writeArbitraryValue(generator, ctx, propName, val);
          return;
        }

        switch (type) {
          case TYPE_NUMBER: {
            final double value = item.get(double.class, 0d);
            if (this.booleanItemNames.contains(propName)) {
              final boolean val = AbstractJsonSerializer.matchesBooleanValues(value, this.booleanTrueValues);
              generator.write(propName, val);
            } else {
              generator.write(propName, value);
            }
            break;
          }
          case TYPE_NUMBER_RANGE: {
            final List<Double> vals = item.getAsList(Double.class, Collections.emptyList());
            if (this.booleanItemNames.contains(propName)) {
              if (vals.size() == 1) {
                final boolean val = AbstractJsonSerializer.matchesBooleanValues(vals.get(0), this.booleanTrueValues);
                generator.write(propName, val);
              } else {
                generator.write(propName, false);
              }
            } else {
              generator.writeStartArray(propName);
              for (final Double val : vals) {
                generator.write(val);
              }
              generator.writeEnd();
            }
            break;
          }
          case TYPE_RFC822_TEXT:
          case TYPE_TEXT: {
            final String val = item.get(String.class, null);
            if (this.booleanItemNames.contains(propName)) {
              final boolean boolVal = AbstractJsonSerializer.matchesBooleanValues(val, this.booleanTrueValues);
              generator.write(propName, boolVal);
            } else {
              if (val == null) {
                generator.writeNull(propName);
              } else {
                generator.write(propName, val);
              }
            }
            break;
          }
          case TYPE_TEXT_LIST: {
            final List<String> vals = item.getAsList(String.class, Collections.emptyList());
            if (this.booleanItemNames.contains(propName)) {
              if (vals.size() == 1) {
                final boolean val = AbstractJsonSerializer.matchesBooleanValues(vals.get(0), this.booleanTrueValues);
                generator.write(propName, val);
              } else {
                generator.write(propName, false);
              }
            } else {
              generator.writeStartArray(propName);
              for (final String val : vals) {
                generator.write(val);
              }
              generator.writeEnd();
            }
            break;
          }
          case TYPE_TIME: {
            final DominoTimeType val = item.get(DominoTimeType.class, null);
            this.writeTimeProperty(generator, propName, val);
            break;
          }
          case TYPE_TIME_RANGE: {
            final List<DominoTimeType> vals = item.getAsList(DominoTimeType.class, Collections.emptyList());
            if (vals.size() == 1) {
              this.writeTimeProperty(generator, propName, vals.get(0));
            } else {
              generator.writeStartArray(propName);
              for (final DominoTimeType val : vals) {
                if (val == null) {
                  generator.writeNull();
                } else {
                  if (val instanceof DominoDateTime) {
                    generator.write(JsonUtil.toIsoString((DominoDateTime) val));
                  } else {
                    switch (this.dateRangeFormat) {
                      case OBJECT:
                        generator.writeStartObject();
                        generator.write(JsonSerializer.PROP_RANGE_FROM,
                            JsonUtil.toIsoString(((DominoDateRange) val).getStartDateTime()));
                        generator.write(JsonSerializer.PROP_RANGE_TO,
                            JsonUtil.toIsoString(((DominoDateRange) val).getEndDateTime()));
                        generator.writeEnd();
                        break;
                      case ISO:
                      default:
                        generator.write(JsonUtil.toIsoString((DominoDateRange) val));
                        break;
                    }
                  }
                }
              }
              generator.writeEnd();
            }
            break;
          }
          case TYPE_COMPOSITE:
            try {
              final RichTextHTMLConverter.Builder builder = obj.getParentDatabase()
                  .getParentDominoClient()
                  .getRichTextHtmlConverter()
                  .renderItem(obj, propName);
              if (this.htmlConvertOptions == null || this.htmlConvertOptions.isEmpty()) {
                builder.option(HtmlConvertOption.XMLCompatibleHTML, "1"); //$NON-NLS-1$
              } else {
                this.htmlConvertOptions.forEach(builder::option);
              }
              final HtmlConversionResult conv = builder.convert();
              generator.write(propName, conv.getHtml());
            } catch (ItemNotFoundException | EntryNotFoundInIndexException e) {
              // Occurs with design notes
              generator.write(propName, ""); //$NON-NLS-1$
            } catch (final DominoException e) {
              switch (e.getId()) {
                case 14941:
                case 14944:
                  // Un-messaged error codes observed with design notes
                  generator.write(propName, ""); //$NON-NLS-1$
                  break;
                default:
                  throw e;
              }
            }
            break;
          case TYPE_MIME_PART:
            // TODO read inline?
            // TODO rationalize multiple body types
            final MimeMessage mime = obj.getParentDatabase()
                .getParentDominoClient()
                .getMimeReader()
                .readMIME(obj, propName, EnumSet.of(ReadMimeDataType.MIMEHEADERS));
            String content;
            try (InputStream is = mime.getInputStream()) {
              content = IOUtils.toString(is, StandardCharsets.UTF_8);
            } catch (IOException | MessagingException e) {
              throw new RuntimeException(e);
            }
            if (content == null) {
              generator.writeNull(propName);
            } else {
              generator.write(propName, content.toString());
            }
            break;
          case TYPE_HTML:
            // TODO this is probably a specialized value, but the underlying API could
            // handle converting to string
            break;
          case TYPE_USERDATA:
            // TODO Base64? Custom adapters?
            break;
          case TYPE_FORMULA:
          case TYPE_ERROR:
          case TYPE_NOTEREF_LIST:
            // TODO convert to string?
            break;
          case TYPE_ACTION:
          case TYPE_ASSISTANT_INFO:
          case TYPE_CALENDAR_FORMAT:
          case TYPE_COLLATION:
          case TYPE_HIGHLIGHTS:
          case TYPE_ICON:
          case TYPE_INVALID_OR_UNKNOWN:
          case TYPE_LSOBJECT:
          case TYPE_NOTELINK_LIST:
          case TYPE_OBJECT:
          case TYPE_QUERY:
          case TYPE_SCHED_LIST:
          case TYPE_SEAL:
          case TYPE_SEAL2:
          case TYPE_SEALDATA:
          case TYPE_SEAL_LIST:
          case TYPE_SIGNATURE:
          case TYPE_UNAVAILABLE:
          case TYPE_USERID:
          case TYPE_VIEWMAP_DATASET:
          case TYPE_VIEWMAP_LAYOUT:
          case TYPE_VIEW_FORMAT:
          case TYPE_WORKSHEET_DATA:
          default:
            break;

        }
      }
    });

    generator.writeEnd();
  }

  private void writeArbitraryValue(final JsonGenerator generator, SerializationContext ctx, final String propName, final Object val) {
    if (val == null) {
      if (propName == null) {
        generator.writeNull();
      } else {
        generator.writeNull(propName);
      }
    } else if (val instanceof Short || val instanceof Integer || val instanceof Long) {
      if (propName == null) {
        generator.write(((Number) val).longValue());
      } else {
        generator.write(propName, ((Number) val).longValue());
      }
    } else if (val instanceof Number) {
      if (propName == null) {
        generator.write(((Number) val).doubleValue());
      } else {
        generator.write(propName, ((Number) val).doubleValue());
      }
    } else if (val instanceof Character || val instanceof CharSequence) {
      if (propName == null) {
        generator.write(val.toString());
      } else {
        generator.write(propName, val.toString());
      }
    } else if (val instanceof Boolean) {
      if (propName == null) {
        generator.write((Boolean) val);
      } else {
        generator.write(propName, (Boolean) val);
      }
    } else if (val instanceof Map) {
      if (propName == null) {
        generator.writeStartObject();
      } else {
        generator.writeStartObject(propName);
      }
      ((Map<?, ?>) val).forEach((key, value) -> {
        if (key == null) {
          throw new IllegalArgumentException("Unable to serialize null key in map");
        }
        this.writeArbitraryValue(generator, ctx, key.toString(), value);
      });
      generator.writeEnd();
    } else if (val instanceof Iterable) {
      if (propName == null) {
        generator.writeStartArray();
      } else {
        generator.writeStartArray(propName);
      }
      final Spliterator<?> iter = ((Iterable<?>) val).spliterator();
      StreamSupport.stream(iter, false).forEach(value -> this.writeArbitraryValue(generator, ctx, null, value));
      generator.writeEnd();
    } else {
      // Pass this along to the context to let it try
      ctx.serialize(propName, val, generator);
    }
  }

  private void writeTimeProperty(final JsonGenerator generator, final String propName, final DominoTimeType val) {
    if (val == null) {
      generator.writeNull(propName);
    } else {
      if (val instanceof DominoDateTime) {
        generator.write(propName, JsonUtil.toIsoString((DominoDateTime) val));
      } else {
        switch (this.dateRangeFormat) {
          case OBJECT:
            generator.writeStartObject(propName);
            generator.write(JsonSerializer.PROP_RANGE_FROM, JsonUtil.toIsoString(((DominoDateRange) val).getStartDateTime()));
            generator.write(JsonSerializer.PROP_RANGE_TO, JsonUtil.toIsoString(((DominoDateRange) val).getEndDateTime()));
            generator.writeEnd();
            break;
          case ISO:
          default:
            generator.write(propName, JsonUtil.toIsoString((DominoDateRange) val));
        }
      }
    }
  }
}
