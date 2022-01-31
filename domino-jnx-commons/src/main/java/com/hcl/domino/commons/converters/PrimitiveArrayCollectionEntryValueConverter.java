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
package com.hcl.domino.commons.converters;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.CollectionEntryValueConverter;
import com.hcl.domino.data.DocumentValueConverter;

/**
 * {@link DocumentValueConverter} implementation that supports converting to
 * and from primitive array types.
 *
 * @author Jesse Gallagher
 */
public class PrimitiveArrayCollectionEntryValueConverter implements CollectionEntryValueConverter {
  private Class<?> getBoxedClass(final Class<?> clazz) {
    if (boolean.class.equals(clazz)) {
      return Boolean.class;
    } else if (char.class.equals(clazz)) {
      return Character.class;
    } else if (byte.class.equals(clazz)) {
      return Byte.class;
    } else if (short.class.equals(clazz)) {
      return Short.class;
    } else if (int.class.equals(clazz)) {
      return Integer.class;
    } else if (long.class.equals(clazz)) {
      return Long.class;
    } else if (float.class.equals(clazz)) {
      return Float.class;
    } else if (double.class.equals(clazz)) {
      return Double.class;
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Unsupported {0}", clazz));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(final CollectionEntry obj, final int index, final Class<T> valueType, final T defaultValue) {
    final Class<?> type = valueType.getComponentType();
    final Class<?> boxed = this.getBoxedClass(type);
    final List<?> val = obj.getAsList(index, boxed, null);
    if (val == null) {
      return defaultValue;
    }

    final T result = (T) Array.newInstance(type, val.size());
    for (int i = 0; i < val.size(); i++) {
      Array.set(result, i, val.get(i));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(final CollectionEntry obj, final String itemName, final Class<T> valueType, final T defaultValue) {
    final Class<?> type = valueType.getComponentType();
    final Class<?> boxed = this.getBoxedClass(type);
    final List<?> val = obj.getAsList(itemName, boxed, null);
    if (val == null) {
      return defaultValue;
    }

    final T result = (T) Array.newInstance(type, val.size());
    for (int i = 0; i < val.size(); i++) {
      Array.set(result, i, val.get(i));
    }
    return result;
  }

  @Override
  public <T> List<T> getValueAsList(final CollectionEntry obj, final int index, final Class<T> valueType,
      final List<T> defaultValue) {
    // This is a weird case, and this intentionally should return an e.g.
    // List<int[]>
    return Arrays.asList(this.getValue(obj, index, valueType, null));
  }

  @Override
  public <T> List<T> getValueAsList(final CollectionEntry obj, final String itemName, final Class<T> valueType,
      final List<T> defaultValue) {
    // This is a weird case, and this intentionally should return an e.g.
    // List<int[]>
    return Arrays.asList(this.getValue(obj, itemName, valueType, null));
  }

  @Override
  public boolean supportsRead(final Class<?> valueType) {
    return valueType.isArray() && valueType.getComponentType().isPrimitive();
  }
}
