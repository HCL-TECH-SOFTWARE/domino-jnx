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
package com.hcl.domino.jna.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public enum MemoryUtils {
  ;

  @SuppressWarnings("unused")
  public static <T extends MemoryStructure> T readMemory(PointerByReference ppData, short odsType, Class<T> struct) {

    // Straight-read variant
    T result = MemoryStructureUtil.newStructure(struct, 0);
    int len = MemoryStructureUtil.sizeOf(struct);
    result.getData().put(ppData.getValue().getByteBuffer(0, len));
    ppData.setValue(ppData.getValue().share(len));

    return result;
  }

  @SuppressWarnings("unused")
  public static <T extends MemoryStructure> T odsReadMemory(Pointer data, short odsType, Class<T> struct) {
    // TODO determine if any architectures need ODSReadMemory. On x64 macOS, it seems harmful.
    //    Docs just say "Intel", but long predate x64. On Windows, it says it should be harmless, but
    //    care has to be taken on "UNIX", which is everything else.
    //    Additionally, not all structures here have ODS numbers
    PointerByReference ppData = new PointerByReference(data);
    Memory mem = new Memory(MemoryStructureUtil.sizeOf(struct));
    NotesCAPI.get().ODSReadMemory(ppData, odsType, mem, (short)1);
    return MemoryStructureUtil.forStructure(struct, () -> mem.getByteBuffer(0, mem.size()));

  }

  /**
   * Reads a structure from the provided ByteBuffer, incrementing its position the size of the struct.
   * 
   * @param <T> the class of structure to read
   * @param data the containing data buffer
   * @param odsType the ODS type, or {@code -1} if not known
   * @param struct a {@link Class} represening {@code <T>}
   * @return the read structure
   */
  public static <T extends MemoryStructure> T readMemory(ByteBuffer data, short odsType, Class<T> struct) {
    T result = MemoryStructureUtil.newStructure(struct, 0);
    int len = MemoryStructureUtil.sizeOf(struct);
    byte[] bytes = new byte[len];
    data.get(bytes);
    result.getData().put(bytes);
    return result;
  }

  public static ByteBuffer readBuffer(ByteBuffer buf, long len) {
    ByteBuffer result = subBuffer(buf, (int)len);
    buf.position(buf.position()+(int)len);
    return result;
  }

  public static byte[] getBufferBytes(ByteBuffer buf, long len) {
    byte[] result = getSubBufferBytes(buf, (int)len);
    buf.position(buf.position()+(int)len);
    return result;
  }

  public static byte[] getSubBufferBytes(ByteBuffer buf, int len) {
    byte[] result = new byte[len];
    buf.slice().order(ByteOrder.nativeOrder()).get(result);
    return result;
  }

  public static ByteBuffer subBuffer(ByteBuffer buf, int len) {
    ByteBuffer tempBuf = buf.slice().order(ByteOrder.nativeOrder());
    tempBuf.limit(len);
    return tempBuf;
  }

}
