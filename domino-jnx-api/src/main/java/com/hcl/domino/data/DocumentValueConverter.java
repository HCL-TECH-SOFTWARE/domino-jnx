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

import java.util.EnumSet;
import java.util.ServiceLoader;
import java.util.Set;

import com.hcl.domino.data.Item.ItemFlag;

/**
 * Implementations of this interface are loaded via {@link ServiceLoader}
 * in {@link Document#get(String, Class, Object)}
 */
public interface DocumentValueConverter extends ValueConverter<Document> {

  /**
   * Implement this method to write a value to the object
   *
   * @param <T>      value type
   * @param obj      object
   * @param itemName name of item to write
   * @param newValue new value
   */
  @Override
  default <T> void setValue(Document obj, String itemName, T newValue) {
    setValue(obj, EnumSet.of(ItemFlag.SUMMARY), itemName, newValue);
  }

  /**
   * Implement this method to write a value to the object
   *
   * @param <T>      value type
   * @param obj      object
   * @param itemFlags item flags
   * @param itemName name of item to write
   * @param newValue new value
   */
  <T> void setValue(Document obj, Set<ItemFlag> itemFlags, String itemName, T newValue);

}
