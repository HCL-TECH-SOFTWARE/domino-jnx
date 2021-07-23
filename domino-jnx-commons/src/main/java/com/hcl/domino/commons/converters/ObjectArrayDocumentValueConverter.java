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
package com.hcl.domino.commons.converters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;

/**
 * {@link DocumentValueConverter} implementation that supports converting to
 * and from object array types.
 *
 * @author Jesse Gallagher
 */
public class ObjectArrayDocumentValueConverter implements DocumentValueConverter {

  @Override
  public <T> T getValue(final Document obj, final String itemName, final Class<T> valueType, final T defaultValue) {
    final Class<?> type = valueType.getComponentType();
    final List<?> val = obj.getAsList(itemName, type, null);
    if (val == null) {
      return defaultValue;
    }

    @SuppressWarnings("unchecked")
    final T result = (T) Array.newInstance(type, val.size());
    for (int i = 0; i < val.size(); i++) {
      Array.set(result, i, val.get(i));
    }
    return result;
  }

  @Override
  public <T> List<T> getValueAsList(final Document obj, final String itemName, final Class<T> valueType,
      final List<T> defaultValue) {
    // This is a weird case, and this intentionally should return an e.g.
    // List<Object[]>
    return Arrays.asList(this.getValue(obj, itemName, valueType, null));
  }

  @Override
  public <T> void setValue(final Document obj, final String itemName, final T newValue) {
    // Pour it into a List
    final int length = Array.getLength(newValue);
    final List<Object> listVal = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      listVal.add(Array.get(newValue, i));
    }
    obj.replaceItemValue(itemName, listVal);
  }

  @Override
  public boolean supportsRead(final Class<?> valueType) {
    return valueType.isArray() && !valueType.getComponentType().isPrimitive();
  }

  @Override
  public boolean supportsWrite(final Class<?> valueType, final Object value) {
    return valueType.isArray() && !valueType.getComponentType().isPrimitive();
  }

}
