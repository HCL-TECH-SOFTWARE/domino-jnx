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
package com.hcl.domino.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.List;

/**
 * Common interface to read Domino data with a specific type
 */
public interface TypedAccess {

  /**
   * Returns an item value converted to the specified data type.<br>
   * <br>
   * We currently support the following value types out of the box:<br>
   * <ul>
   * <li>{@link String}</li>
   * <li>{@link Integer}</li>
   * <li>{@link Long}</li>
   * <li>{@link Double}</li>
   * <li>{@link DominoDateTime}</li>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link TemporalAccessor} (returned as {@link DominoDateTime})</li>
   * </ul>
   * <br>
   * Additional value types are supported by implementing and registering
   * {@link DocumentValueConverter} as Java {@link java.util.ServiceLoader
   * services}.
   *
   * @param <T>          type of return value
   * @param itemName     item name, case insensitive
   * @param valueType    class of return value
   * @param defaultValue default value returned of object does not contain
   *                     property
   * @return return value
   * @throws IllegalArgumentException if the specified value type is unsupported
   */
  <T> T get(String itemName, Class<T> valueType, T defaultValue);

  /**
   * Returns a list of item values converted to the specified data type.<br>
   * <br>
   * We currently support the following value types out of the box:<br>
   * <ul>
   * <li>{@link String}</li>
   * <li>{@link Integer}</li>
   * <li>{@link Long}</li>
   * <li>{@link Double}</li>
   * <li>{@link DominoDateTime}</li>
   * <li>{@link LocalDate}</li>
   * <li>{@link LocalTime}</li>
   * <li>{@link OffsetDateTime}</li>
   * <li>{@link TemporalAccessor} (returned as {@link DominoDateTime})</li>
   * </ul>
   * <br>
   * Additional value types are supported by implementing and registering
   * {@link DocumentValueConverter} as Java {@link java.util.ServiceLoader
   * services}.
   *
   * @param <T>          type of return value
   * @param itemName     item name, case insensitive
   * @param valueType    class of return value
   * @param defaultValue default value returned of object does not contain
   *                     property
   * @return value list
   * @throws IllegalArgumentException if the specified value type is unsupported
   */
  <T> List<T> getAsList(String itemName, Class<T> valueType, List<T> defaultValue);

  /**
   * Returns the names of all available items
   *
   * @return names
   */
  List<String> getItemNames();

  /**
   * Checks if an item is available
   *
   * @param itemName item name
   * @return true if available
   */
  boolean hasItem(String itemName);

}
