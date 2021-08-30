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
package com.hcl.domino.commons.structures;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.function.BiFunction;

import com.hcl.domino.misc.INumberEnum;

class StructMember {
  private final String name;
  final int offset;
  final Class<?> type;
  private final boolean unsigned;
  final int length;
  final BiFunction<ByteBuffer, Integer, Object> reader;
  final TriConsumer<ByteBuffer, Integer, Object> writer;
  final boolean bitfield;

  public StructMember(final String name, final int offset, final Class<?> clazz, final boolean unsigned, final boolean bitfield,
      final int length) {
    this.name = name;
    this.offset = offset;
    this.unsigned = unsigned;
    this.length = length;
    this.bitfield = bitfield;
    this.type = (Class<?>)clazz;
    
    // Unwrap INumberEnum types to their underlying numbers for readers and writers
    Class<?> structuralType;
    if(INumberEnum.class.isAssignableFrom(clazz)) {
      structuralType = MemoryStructureUtil.getNumberType(clazz);
    } else if(clazz.isArray() && INumberEnum.class.isAssignableFrom(clazz.getComponentType())) {
      structuralType = MemoryStructureUtil.getNumberArrayType(clazz);
    } else {
      structuralType = (Class<?>)clazz;
    }
    
    this.reader = MemoryStructureProxy.reader(structuralType, unsigned, length);
    this.writer = MemoryStructureProxy.writer(structuralType, unsigned, length);
  }

  @Override
  public String toString() {
    return MessageFormat.format("StructMember [name={0}, offset={1}, type={2}, unsigned={3}, length={4}]", this.name, this.offset, //$NON-NLS-1$
        this.type, this.unsigned, this.length);
  }
}
