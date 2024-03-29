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
package com.hcl.domino.commons.util;

import com.hcl.domino.commons.data.DefaultPreV3Author;
import com.hcl.domino.commons.data.DefaultUserData;
import com.hcl.domino.commons.structures.MemoryStructureUtil;
import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.data.PreV3Author;
import com.hcl.domino.data.UserData;
import com.hcl.domino.richtext.structures.LicenseID;
import com.hcl.domino.richtext.structures.MemoryStructure;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;

/**
 * Contains methods for interpreting raw Notes data in memory.
 * 
 * @author Jesse Gallagher
 * @since 1.0.42
 */
public enum NotesItemDataUtil {
  ;
  
  public static PreV3Author parsePreV3Author(ByteBuffer buf) {
    // Last 8 bytes are a LicenseID - the rest are the username
    
    byte[] lmbcs = new byte[buf.remaining()-8];
    buf.get(lmbcs);
    String name = new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
    
    byte[] licenseBytes = new byte[8];
    buf.get(licenseBytes);
    ByteBuffer licenseBuf = ByteBuffer.wrap(licenseBytes);
    LicenseID license = MemoryStructureUtil.forStructure(LicenseID.class, () -> licenseBuf);
    
    return new DefaultPreV3Author(name, license);
  }
  
  /**
   * Parses item data of TYPE_USERDATA into a {@link UserData} representation.
   * 
   * @param buf the containing data buffer
   * @return a {@link UserData} instance from the item data
   * @since 1.12.0
   */
  public static UserData parseUserData(ByteBuffer buf) {
    // The format is a byte-length Pascal string for the format name,
    //   followed by binary data
    int formatNameLen = Byte.toUnsignedInt(buf.get());
    byte[] lmbcs = new byte[formatNameLen];
    buf.get(lmbcs);
    String formatName = new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
    byte[] data = new byte[buf.remaining()];
    buf.get(data);
    
    return new DefaultUserData(formatName, data);
  }

  /**
   * Reads a structure from the provided ByteBuffer, incrementing its position the size of the struct.
   * 
   * @param <T> the class of structure to read
   * @param data the containing data buffer
   * @param odsType the ODS type, or {@code -1} if not known
   * @param struct a {@link Class} representing {@code <T>}
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
    if(len < 0) {
      throw new IllegalArgumentException(MessageFormat.format("len ({0}) cannot be less than 0", len));
    }
    ByteBuffer tempBuf = buf.slice().order(ByteOrder.nativeOrder());
    if(len > tempBuf.capacity()) {
      throw new IllegalArgumentException(MessageFormat.format("len ({0}) cannot be greater than the remaining buffer length ({1})", len, tempBuf.capacity()));
    }
    tempBuf.limit(len);
    return tempBuf;
  }
  
  /**
   * Checks if the specified buffer at least has the specified size. If not we create
   * a new one and copy over all data, keeping the current position
   * 
   * @param buf buffer
   * @param newSize new minimum size
   * @return buffer, either the same (if capacity was sufficient) or a new one
   */
  public static ByteBuffer ensureBufferCapacity(ByteBuffer buf, int newSize) {
    if (newSize < 1) {
      throw new IllegalArgumentException("New size must be greater than 0 bytes");
    }
    if (buf.capacity() > newSize) {
      return buf;
    }
    
    int pos = buf.position();
    
    final ByteBuffer newData = ByteBuffer.allocate(newSize).order(ByteOrder.nativeOrder());
    final int copySize = Math.min(newSize, buf.capacity());
    buf.position(0);
    buf.limit(copySize);
    newData.put(buf);
    newData.position(pos);
    
    return newData;
  }

}
