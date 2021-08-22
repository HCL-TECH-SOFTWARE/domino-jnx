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
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "OLE_GUID",
  members = {
    @StructureMember(name = "Data1", type = int.class),
    @StructureMember(name = "Data2", type = short.class),
    @StructureMember(name = "Data3", type = short.class),
    @StructureMember(name = "Data4", type = byte[].class, length = 8),
  }
)
public interface OLE_GUID extends MemoryStructure {
  static final Pattern GUID_PATTERN = Pattern.compile("^([0-9a-fA-F]{8})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{4})-([0-9a-fA-F]{12})$"); //$NON-NLS-1$
  
  @StructureGetter("Data1")
  int getData1();
  
  @StructureSetter("Data1")
  OLE_GUID setData1(int data);
  
  @StructureGetter("Data2")
  short getData2();
  
  @StructureSetter("Data2")
  OLE_GUID setData2(short data);
  
  @StructureGetter("Data3")
  short getData3();
  
  @StructureSetter("Data3")
  OLE_GUID setData3(short data);
  
  @StructureGetter("Data4")
  byte[] getData4();
  
  @StructureSetter("Data4")
  OLE_GUID setData4(byte[] data);
  
  default String toGuidString() {
    ByteBuffer buf = getData();
    StringBuilder result = new StringBuilder();
    // little-endian DWORD
    result.append(String.format("%08x", buf.getInt())); //$NON-NLS-1$
    result.append("-"); //$NON-NLS-1$
    // little-endian WORD
    result.append(String.format("%04x", buf.getShort())); //$NON-NLS-1$
    result.append("-"); //$NON-NLS-1$
    // little-endian WORD
    result.append(String.format("%04x", buf.getShort())); //$NON-NLS-1$
    result.append("-"); //$NON-NLS-1$
    // big-endian WORD
    for(int i = 0; i < 2; i++) {
      result.append(String.format("%02x", buf.get())); //$NON-NLS-1$
    }
    result.append("-"); //$NON-NLS-1$
    // big-endian byte[6]
    for(int i = 0; i < 6; i++) {
      result.append(String.format("%02x", buf.get())); //$NON-NLS-1$
    }
    return result.toString();
  }
  
  default OLE_GUID setGuidString(String guid) {
    Objects.requireNonNull(guid, "guid cannot be null");
    Matcher m = GUID_PATTERN.matcher(guid);
    if(!m.matches()) {
      throw new IllegalArgumentException(MessageFormat.format("Value must be a proper GUID: {0}", guid));
    }
    
    ByteBuffer buf = getData().order(ByteOrder.LITTLE_ENDIAN);
    // Little-endian DWORD
    String comp = m.group(1);
    buf.putInt(Integer.parseInt(comp, 16));
    // Little-endian WORD
    comp = m.group(2);
    buf.putShort(Short.parseShort(comp, 16));
    // Little-endian WORD
    comp = m.group(3);
    buf.putShort(Short.parseShort(comp, 16));
    // Big-endian WORD
    comp = m.group(4);
    for(int i = 0; i < 2; i++) { // Big-endian
      buf.put((byte)Short.parseShort(comp.substring(i*2, i*2+2), 16));
    }
    // Big-endian byte[6]
    comp = m.group(5);
    for(int i = 0; i < 6; i++) { // Big-endian
      buf.put((byte)Short.parseShort(comp.substring(i*2, i*2+2), 16));
    }
    
    return this;
  }
}
