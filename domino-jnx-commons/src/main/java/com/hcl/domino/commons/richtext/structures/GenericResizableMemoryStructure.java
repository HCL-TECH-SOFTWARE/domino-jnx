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
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

public class GenericResizableMemoryStructure implements ResizableMemoryStructure {
  private ByteBuffer data;
  private final Class<? extends MemoryStructure> recordClass;

  public GenericResizableMemoryStructure(final ByteBuffer data, final Class<? extends MemoryStructure> recordClass) {
    this.data = data;
    this.recordClass = recordClass;
  }

  @Override
  public ByteBuffer getData() {
    return this.data.duplicate().order(ByteOrder.nativeOrder());
  }

  @Override
  public ByteBuffer getVariableData() {
    final Class<?> sizeClass = this.recordClass == null ? this.getClass() : this.recordClass;
    final int structureSize = MemoryStructureUtil.sizeOf(sizeClass);
    return ((ByteBuffer) this.getData().position(structureSize)).slice().order(ByteOrder.nativeOrder());
  }

  @Override
  public void resize(final int size) {
    if (size < 1) {
      throw new IllegalArgumentException("New size must be greater than 0 bytes");
    }
    final ByteBuffer newData = ByteBuffer.allocate(size);
    final int copySize = Math.min(size, this.data.capacity());
    this.data.position(0);
    this.data.limit(copySize);
    newData.put(this.data);
    newData.position(0);
    this.data = newData;
  }

  @Override
  public void resizeVariableData(final int size) {
    if (size < 1) {
      throw new IllegalArgumentException("New size must be greater than 0 bytes");
    }
    final Class<?> sizeClass = this.recordClass == null ? this.getClass() : this.recordClass;
    final int totalSize = MemoryStructureUtil.sizeOf(sizeClass) + size;
    this.resize(totalSize);
  }
}
