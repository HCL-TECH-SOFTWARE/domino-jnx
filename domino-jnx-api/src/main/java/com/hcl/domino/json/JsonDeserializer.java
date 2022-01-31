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

import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.misc.JNXServiceFinder;

/**
 * This interface allows for deserialization of JSON into documents, both as
 * strings
 * and as implementation-specific Java object representations.
 * <p>
 * Implementations of this service are expected to be provided by additional
 * integration modules, such as {@code domino-jnx-jsonp}.
 * </p>
 *
 * @author Jesse Gallagher
 * @since 1.0.9
 */
public interface JsonDeserializer {
  /**
   * This interface is used by {@link JsonDeserializer#customProcessor} to provide
   * custom behavior when deserializing a named property.
   *
   * @author Jesse Gallagher
   * @since 1.0.28
   */
  @FunctionalInterface
  interface CustomProcessor {
    /**
     * Performs this operation on the given arguments.
     *
     * @param jsonValue the JSON value to process
     * @param propName  the name of the property currently being processed
     * @param target    the context document to write to
     */
    void apply(Object jsonValue, String propName, Document target);
  }

  /**
   * Constructs a new deserializer using the first available JSON service.
   *
   * @return a new {@link JsonSerializer} implementation
   * @throws IllegalStateException if there is no registered deserializer
   *                               implementation
   */
  static JsonDeserializer createDeserializer() {
    return JNXServiceFinder.findServices(JsonDeserializerFactory.class)
        .findFirst()
        .map(JsonDeserializerFactory::newDeserializer)
        .orElseThrow(() -> new IllegalStateException(
            MessageFormat.format("Unable to find a {0} service implementation", JsonDeserializerFactory.class.getName())));
  }

  /**
   * Configures the values to be stored in the target document when the
   * deserializer
   * encounters JSON boolean values.
   *
   * @param trueValue  the value used when converting {@code true}
   * @param falseValue the value used when converting {@code false}
   * @return this deserializer
   */
  JsonDeserializer booleanValues(Object trueValue, Object falseValue);

  /**
   * Specifies a custom processor for a named property. Calling this method will
   * delegate handling of
   * the named property on the incoming JSON to the processor object.
   *
   * @param propName  the name of the property to process
   * @param processor a {@link CustomProcessor} functional object to apply when
   *                  the named property is encountered
   * @return this deserializer
   * @since 1.0.28
   * @throws IllegalArgumentException if {@code propName} is empty
   * @throws NullPointerException     if {@code processor} or {@code propName} is
   *                                  {@code null}
   */
  JsonDeserializer customProcessor(String propName, CustomProcessor processor);

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
   * @return this deserializer
   */
  JsonDeserializer dateTimeItems(Collection<String> dateTimeItems);

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
   * @return this deserializer
   */
  JsonDeserializer detectDateTime(boolean detectDateTime);

  /**
   * Deserializes the provided implementation-specific JSON object into a
   * document.
   *
   * @param json the implementation-specific native JSON object to deserialize
   * @return the modified or newly-created document
   * @throws RuntimeException an implementation-specific exception when the JSON
   *                          is unprocessable
   *                          as configured
   */
  default Document fromJson(final Object json) {
    return this.fromJson(json.toString());
  }

  /**
   * Deserializes the provided JSON object string into a document.
   *
   * @param json the JSON string to deserialize
   * @return the modified or newly-created document
   * @throws RuntimeException an implementation-specific exception when the JSON
   *                          is unprocessable
   *                          as configured
   */
  Document fromJson(String json);

  /**
   * Configures whether items in a document provided via {@link #target(Document)}
   * that do not exist in the incoming JSON should be removed.
   * <p>
   * The default behavior is to only update items that are represented as
   * properties
   * in the incoming JSON.
   * </p>
   * <p>
   * The {@code "Form"} item and items beginning with {@code "$"} will never be
   * removed
   * in this way.
   * </p>
   *
   * @param removeMissingItems whether items not present in JSON should be removed
   * @return this deserializer
   */
  JsonDeserializer removeMissingItems(boolean removeMissingItems);

  /**
   * Sets the deserialization target to be a new document in the target database.
   * <p>
   * Documents imported this way will be returned unsaved in memory.
   * </p>
   *
   * @param database the database to import into
   * @return this deserializer
   */
  JsonDeserializer target(Database database);

  /**
   * Sets the deserialization target to be an existing document in the target
   * database.
   * <p>
   * Documents imported this way will be modified in-place in memory but not
   * saved.
   * </p>
   *
   * @param document the document to write to
   * @return this deserializer
   */
  JsonDeserializer target(Document document);
}
