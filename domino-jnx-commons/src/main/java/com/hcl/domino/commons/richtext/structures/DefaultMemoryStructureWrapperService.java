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
package com.hcl.domino.commons.richtext.structures;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

public class DefaultMemoryStructureWrapperService implements MemoryStructureWrapperService {
  @Override
  public <T extends MemoryStructure> T wrapStructure(final Class<T> structureClass, final ByteBuffer data) {
    if (ResizableMemoryStructure.class.isAssignableFrom(structureClass)) {
      final GenericResizableMemoryStructure struct = new GenericResizableMemoryStructure(data, structureClass);
      return MemoryStructureUtil.forStructure(structureClass, struct);
    } else {
      return MemoryStructureUtil.forStructure(structureClass, () -> data.slice().order(ByteOrder.nativeOrder()));
    }
  }

  @Override
  public int sizeOf(Class<?> type) {
    return MemoryStructureUtil.sizeOf(type);
  }
  
  @Override
  public <I extends MemoryStructure> I newStructure(Class<I> subtype, int variableDataLength) {
    return MemoryStructureUtil.newStructure(subtype, variableDataLength);
  }
  
}
