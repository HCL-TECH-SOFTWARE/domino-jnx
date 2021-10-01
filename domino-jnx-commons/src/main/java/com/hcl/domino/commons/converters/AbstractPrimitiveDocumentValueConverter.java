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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentValueConverter;
import com.hcl.domino.data.Item.ItemFlag;

/**
 * Shared logic for {@link DocumentValueConverter} implementations that handle
 * conversion
 * to and from Java primitive builtins.
 *
 * @param <BOX> the boxed value that the subclass handles
 * @author Jesse Gallagher
 */
public abstract class AbstractPrimitiveDocumentValueConverter<BOX> implements DocumentValueConverter {

  protected abstract BOX convertFromDouble(double value);

  protected abstract double convertToDouble(BOX value);

  protected abstract Class<BOX> getBoxedClass();

  protected abstract Class<?> getPrimitiveClass();

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValue(final Document obj, final String itemName, final Class<T> valueType, final T defaultValue) {
    final double result = obj.get(itemName, Double.class, 0d);
    return (T) this.convertFromDouble(result);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> getValueAsList(final Document obj, final String itemName, final Class<T> valueType,
      final List<T> defaultValue) {
    return obj.getAsList(itemName, Double.class, new ArrayList<>()).stream()
        .map(this::convertFromDouble)
        .map(b -> (T) b)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void setValue(final Document obj, Set<ItemFlag> itemFlags, final String itemName, final T newValue) {
    if (newValue instanceof Iterable) {
      final List<Double> listVal = StreamSupport.stream(((Iterable<BOX>) newValue).spliterator(), false)
          .map(this::convertToDouble)
          .collect(Collectors.toList());
      obj.replaceItemValue(itemName, itemFlags, listVal);
    } else {
      obj.replaceItemValue(itemName, itemFlags, this.convertToDouble((BOX) newValue));
    }
  }

  @Override
  public boolean supportsRead(final Class<?> valueType) {
    return this.getPrimitiveClass().equals(valueType) || this.getBoxedClass().equals(valueType);
  }

  @Override
  public boolean supportsWrite(final Class<?> valueType, final Object value) {
    if (Iterable.class.isAssignableFrom(valueType)) {
      final Object firstVal = ((Iterable<?>) value).iterator().next();
      return this.getBoxedClass().isInstance(firstVal);
    } else {
      return this.getPrimitiveClass().equals(valueType) || this.getBoxedClass().equals(valueType);
    }
  }
}
