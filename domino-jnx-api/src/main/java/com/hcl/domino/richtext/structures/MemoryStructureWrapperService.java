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
package com.hcl.domino.richtext.structures;

import java.nio.ByteBuffer;

import com.hcl.domino.misc.JNXServiceFinder;

/**
 * This interface represents a service capable of wrapping {@link ByteBuffer}s
 * into
 * {@link MemoryStructure} instances in an implementation-specific way.
 *
 * @author Jesse Gallagher
 * @since 1.0.15
 */
public interface MemoryStructureWrapperService {
  static MemoryStructureWrapperService get() {
    return JNXServiceFinder.findRequiredService(MemoryStructureWrapperService.class,
        MemoryStructureWrapperService.class.getClassLoader());
  }

  /**
   * Wraps the provided {@link ByteBuffer} into an instance of the provided
   * structure class, without appending
   * it to the destination rich-text entity. This is useful in specific situations
   * where variable data consists
   * of structured values.
   *
   * @param <T>            the type of structure contained in the data
   * @param structureClass a {@link Class} representing {@code <T>}
   * @param data           the data to wrap
   * @return the newly-wrapped structure
   */
  <T extends MemoryStructure> T wrapStructure(Class<T> structureClass, ByteBuffer data);
  
  public int sizeOf(final Class<?> type);
}
