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
package com.hcl.domino.json;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.html.HtmlConvertOption;
import com.hcl.domino.misc.JNXServiceFinder;

/**
 * This interface allows for serialization of documents into JSON, both as
 * strings
 * and as implementation-specific Java object representations.
 * <p>
 * Implementations of this service are expected to be provided by additional
 * integration modules, such as {@code domino-jnx-jsonp}.
 * </p>
 *
 * @author Jesse Gallagher
 * @since 1.0.7
 */
public interface JsonSerializer {
  Collection<String> DEFAULT_EXCLUDED_ITEMS = Collections.unmodifiableSet(Stream.of(
      "$Fonts" //$NON-NLS-1$
  ).collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))));

  String PROP_METADATA = "@meta"; //$NON-NLS-1$
  String PROP_META_NOTEID = "noteid"; //$NON-NLS-1$
  String PROP_META_UNID = "unid"; //$NON-NLS-1$
  String PROP_META_CREATED = "created"; //$NON-NLS-1$
  String PROP_META_LASTMODIFIED = "lastmodified"; //$NON-NLS-1$
  String PROP_META_LASTMODIFIEDINFILE = "lastmodifiedinfile"; //$NON-NLS-1$
  String PROP_META_LASTACCESSED = "lastaccessed"; //$NON-NLS-1$
  String PROP_META_ADDEDTOFILE = "addedtofile"; //$NON-NLS-1$
  String PROP_META_NOTECLASS = "noteclass"; //$NON-NLS-1$
  String PROP_META_SEQUENCENUMBER = "seq"; //$NON-NLS-1$
  String PROP_META_SEQUENCETIME = "seqtime"; //$NON-NLS-1$
  /**
   * @since 1.11.0
   */
  String PROP_META_PARENTUNID = "parentunid"; //$NON-NLS-1$
  /**
   * @since 1.11.0
   */
  String PROP_META_UNREAD = "unread"; //$NON-NLS-1$
  /**
   * @since 1.11.0
   */
  String PROP_META_THREADID = "threadid"; //$NON-NLS-1$
  /**
   * @since 1.11.0
   */
  String PROP_META_REVISION = "revision"; //$NON-NLS-1$

  String PROP_RANGE_FROM = "from"; //$NON-NLS-1$
  String PROP_RANGE_TO = "to"; //$NON-NLS-1$

  /**
   * Constructs a new serializer using the first available JSON service.
   *
   * @return a new {@link JsonSerializer} implementation
   * @throws IllegalStateException if there is no registered serializer
   *                               implementation
   */
  static JsonSerializer createSerializer() {
    return JNXServiceFinder.findServices(JsonSerializerFactory.class)
        .findFirst()
        .map(JsonSerializerFactory::newSerializer)
        .orElseThrow(() -> new IllegalStateException(
            MessageFormat.format("Unable to find a {0} service implementation", JsonDeserializerFactory.class.getName())));
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
   * @return this serializer
   */
  JsonSerializer booleanItemNames(Collection<String> booleanItemNames);

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
   * @return this serializer
   */
  JsonSerializer booleanTrueValues(Collection<Object> trueValues);

  /**
   * Specifies a custom processor for a named field. Calling this method will
   * delegate handling of
   * the named item on the document to the processor object.
   * <p>
   * This processor is provided the contextual document and item name. To
   * ensure compatibility, it should return a JSON-compatible object.
   * That is, one of:
   * </p>
   * <ul>
   * <li>A primitive value</li>
   * <li>{@code String}</li>
   * <li>A {@link java.util.List List} of above or below objects or other such
   * {@code List}s</li>
   * <li>A {@link java.util.Map Map} of {@code String} keys to above objects or
   * other such {@code Map}s</li>
   * </ul>
   * 
   * <p>Specific service implementations may handle objects not of these types,
   * but that behavior is not specified by this interface.</p>
   *
   * @param propName  the name of the property to process
   * @param processor a {@link BiFunction} to process the context {@link Document}
   *                  and item and produce
   *                  a compatible result
   * @return this serializer
   * @since 1.0.28
   * @throws IllegalArgumentException if {@code propName} is empty
   * @throws NullPointerException     if {@code processor} or {@code propName} is
   *                                  {@code null}
   */
  JsonSerializer customProcessor(String propName, BiFunction<Document, String, Object> processor);

  /**
   * Sets the format for date/time range values.
   *
   * @param format the {@link DateRangeFormat} type to use
   * @return this serializer
   */
  JsonSerializer dateRangeFormat(DateRangeFormat format);

  /**
   * Exclude items by name.
   *
   * @param skippedItemNames a {@link Collection} of case-insensitive item names,
   *                         or {@code null} to not
   *                         exclude any items
   * @return this serializer
   */
  JsonSerializer excludeItems(Collection<String> skippedItemNames);

  /**
   * Exclude item types.
   *
   * @param excludedTypes a {@link Collection} of item types to exclude, or
   *                      {@code null} to not exclude any
   *                      item types
   * @return this serializer
   */
  JsonSerializer excludeTypes(Collection<ItemDataType> excludedTypes);

  /**
   * Include only specific items by name.
   * <p>
   * Note: {@link #excludeItems} is still applied if both are specified.
   * </p>
   *
   * @param includedItemNames a {@link Collection} of case-insensitive item names,
   *                          or {@code null} to not
   *                          include only specific items
   * @return this serializer
   */
  JsonSerializer includeItems(Collection<String> includedItemNames);

  /**
   * Sets whether to include a metadata object in the output JSON, using the
   * {@value #PROP_METADATA} property.
   * {@code false} by default.
   *
   * @param includeMetadata whether to include a document-metadata object
   * @return this serializer
   */
  JsonSerializer includeMetadata(boolean includeMetadata);

  /**
   * Sets whether property names in the emitted JSON should be lowercased. When
   * {@code false} (the default),
   * emitted properties will match the capitalization of the first of each named
   * item in the source
   * document.
   *
   * @param lowercaseProperties whether to lowercase property names
   * @return this serializer
   */
  JsonSerializer lowercaseProperties(boolean lowercaseProperties);

  /**
   * Sets an HTML conversion option for rich text.
   * <p>
   * By default, the only applied option is
   * {@link HtmlConvertOption#XMLCompatibleHTML XMLCompatibleHTML=1}.
   * Calling this method will remove that default in favor of user-specified
   * options.
   * </p>
   *
   * @param option the option to set
   * @param value  the value to set the option to
   * @return this serializer
   * @since 1.0.27
   */
  JsonSerializer richTextConvertOption(HtmlConvertOption option, String value);
  
  /**
   * Sets whether or not the serializer will emit only the {@link #PROP_METADATA} object
   * and its attributes, as opposed to also including the full document contents.
   * 
   * @param metaOnly {@code true} to emit only metadata; {@code false} (the default)
   *                 to emit both meta and document data
   * @return this serializer
   * @since 1.11.0
   */
  JsonSerializer metaOnly(boolean metaOnly);

  /**
   * Serializes the provided document as a JSON object using the implementation's
   * native JSON type.
   *
   * @param doc the document to serialize
   * @return an implementation-dependent JSON object
   */
  Object toJson(Document doc);
  
  /**
   * Serializes the provided Object as a JSON object using the implementation's
   * native JSON type.
   *
   * @param value the Object to serialize
   * @return an implementation-dependent JSON object
   */
  Object toJson(Object value);

  /**
   * Serializes the provided document as a JSON string.
   *
   * @param doc the document to serialize
   * @return a string of JSON content
   */
  default String toJsonString(final Document doc) {
    return String.valueOf(this.toJson(doc));
  }
}
