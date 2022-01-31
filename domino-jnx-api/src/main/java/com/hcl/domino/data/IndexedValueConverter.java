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
package com.hcl.domino.data;

import java.util.List;

/**
 * This subinterface of {@link ValueConverter} provides access to indexed data
 * from a source.
 *
 * @author Jesse Gallagher
 * @param <OBJ> type of object containing the data
 */
public interface IndexedValueConverter<OBJ> extends ValueConverter<OBJ> {
  /**
   * Implement this method to return the converted value type.<br>
   * Will only be invoked if {@link #supportsRead(Class)} returns
   * true.
   *
   * @param <T>          value type
   * @param obj          object
   * @param index        the index of the item to read
   * @param valueType    requested return value type
   * @param defaultValue default value to return if object property is not set
   * @return return value or null
   */
  <T> T getValue(OBJ obj, int index, Class<T> valueType, T defaultValue);

  /**
   * Implement this method to return a list of the converted value type.<br>
   * Will only be invoked if {@link #supportsRead(Class)} returns
   * true.
   *
   * @param <T>          value type
   * @param obj          object
   * @param index        the index of the item to read
   * @param valueType    requested return value type
   * @param defaultValue default value to return if object property is not set
   * @return return value or null
   */
  <T> List<T> getValueAsList(OBJ obj, int index, Class<T> valueType, List<T> defaultValue);

  /**
   * Implement this method to write a value to the object
   *
   * @param <T>      value type
   * @param obj      object
   * @param index    the index of the item to write
   * @param newValue new value
   */
  <T> void setValue(OBJ obj, int index, T newValue);
}
