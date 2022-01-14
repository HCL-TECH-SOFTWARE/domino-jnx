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
package com.hcl.domino.jna.internal;

import java.nio.ByteBuffer;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class JNAMemoryUtils {
  private JNAMemoryUtils() { }

  public static <T extends MemoryStructure> T readMemory(PointerByReference ppData, short odsType, Class<T> struct) {

    // Straight-read variant
    T result = MemoryStructureUtil.newStructure(struct, 0);
    int len = MemoryStructureUtil.sizeOf(struct);
    result.getData().put(ppData.getValue().getByteBuffer(0, len));
    ppData.setValue(ppData.getValue().share(len));

    return result;
  }

  /**
   * Reads a structure from the provided pointer using {@code ODSReadMemory}, using an internal pointer to
   * avoid incrementing the passed-in value.
   * 
   * @param <T> the class of structure to read
   * @param data the containing pointer
   * @param odsType the ODS type code
   * @param struct a {@link Class} representing {@code <T>}
   * @return the read structure
   */
  public static <T extends MemoryStructure> T odsReadMemory(Pointer data, short odsType, Class<T> struct) {
    PointerByReference ppData = new PointerByReference(data);
    return odsReadMemory(ppData, odsType, struct);
  }

  /**
   * Reads a structure from the provided pointer by reference using {@code ODSReadMemory}, incrementing the pointed-to
   * pointer by the size of the struct.
   * 
   * @param <T> the class of structure to read
   * @param ppData the {@link PointerByReference} referencing the start of memory to read
   * @param odsType the ODS type code
   * @param struct a {@link Class} representing {@code <T>}
   * @return the read structure
   * @since 1.0.38
   */
  public static <T extends MemoryStructure> T odsReadMemory(PointerByReference ppData, short odsType, Class<T> struct) {
    // TODO determine if any architectures need ODSReadMemory. On x64 macOS, it seems harmful.
    //    Docs just say "Intel", but long predate x64. On Windows, it says it should be harmless, but
    //    care has to be taken on "UNIX", which is everything else.
    //    Additionally, not all structures here have ODS numbers
    Memory mem = new Memory(MemoryStructureUtil.sizeOf(struct));
    NotesCAPI.get().ODSReadMemory(ppData, odsType, mem, (short)1);
    return MemoryStructureUtil.forStructure(struct, () -> mem.getByteBuffer(0, mem.size()));

  }
  
  /**
   * Generates a new fixed-size proxy object backed by the provided pointer.
   * 
   * @param <I>       the {@link MemoryStructure} sub-interface to proxy
   * @param subtype   a class representing {@code I}
   * @param ptr       a pointer to the start of the structure in memory
   * @return a new proxy object
   * @since 1.0.38
   */
  public static <I extends MemoryStructure> I readStructure(Class<I> subtype, Pointer ptr) {
    int size = MemoryStructureUtil.sizeOf(subtype);
    ByteBuffer buf = ptr.getByteBuffer(0, size);
    return MemoryStructureUtil.forStructure(subtype, () -> buf);
  }

}
