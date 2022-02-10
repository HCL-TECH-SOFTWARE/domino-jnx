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

import java.util.ServiceLoader;

/**
 * Implementations of this interface are loaded via {@link ServiceLoader}
 * in {@link CollectionEntry#get(String, Class, Object)}
 */
public interface CollectionEntryValueConverter extends IndexedValueConverter<CollectionEntry> {
  @Override
  default <T> void setValue(final CollectionEntry obj, final int index, final T newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  default <T> void setValue(final CollectionEntry obj, final String itemName, final T newValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  default boolean supportsWrite(final Class<?> valueType, final Object value) {
    return false;
  }

}
