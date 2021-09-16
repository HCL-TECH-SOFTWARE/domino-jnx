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
package com.hcl.domino.richtext.records;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hcl.domino.design.ClassicThemeBehavior;
import com.hcl.domino.design.DesignConstants;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FramesetLength;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDFRAMESET",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDFrameset.Flag.class, bitfield = true),
    @StructureMember(name = "BorderEnable", type = byte.class),
    @StructureMember(name = "byAvail1", type = byte.class),
    @StructureMember(name = "Reserved1", type = short.class),
    @StructureMember(name = "Reserved2", type = short.class),
    @StructureMember(name = "FrameBorderWidth", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved3", type = short.class),
    @StructureMember(name = "FrameSpacingWidth", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved4", type = short.class),
    @StructureMember(name = "ReservedColor1", type = ColorValue.class),
    @StructureMember(name = "ReservedColor2", type = ColorValue.class),
    @StructureMember(name = "RowQty", type = short.class, unsigned = true),
    @StructureMember(name = "ColQty", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved5", type = short.class),
    @StructureMember(name = "Reserved6", type = short.class),
    @StructureMember(name = "FrameBorderColor", type = ColorValue.class),
    @StructureMember(name = "ThemeSetting", type = ClassicThemeBehavior.class),
    @StructureMember(name = "Reserved7", type = byte.class),
  }
)
public interface CDFrameset extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    BorderEnable(DesignConstants.fFSBorderEnable),
    FrameBorderDims(DesignConstants.fFSFrameBorderDims),
    FrameSpacingDims(DesignConstants.fFSFrameSpacingDims),
    FrameBorderColor(DesignConstants.fFSFrameBorderColor);
    private final int value;
    private Flag(int value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Integer getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDFrameset setFlags(Collection<Flag> flags);
  
  @StructureGetter("BorderEnable")
  byte getBorderEnable();
  
  @StructureSetter("BorderEnable")
  CDFrameset setBorderEnable(byte borderEnable);
  
  @StructureGetter("FrameBorderWidth")
  int getFrameBorderWidth();
  
  @StructureSetter("FrameBorderWidth")
  CDFrameset setFrameBorderWidth(int width);
  
  @StructureGetter("FrameSpacingWidth")
  int getFrameSpacingWidth();
  
  @StructureSetter("FrameSpacingWidth")
  CDFrameset setFrameSpacingWidth(int width);
  
  @StructureGetter("RowQty")
  int getRowCount();
  
  @StructureSetter("RowQty")
  CDFrameset setRowCount(int count);
  
  @StructureGetter("ColQty")
  int getColumnCount();
  
  @StructureSetter("ColQty")
  CDFrameset setColumnCount(int count);
  
  @StructureGetter("FrameBorderColor")
  ColorValue getFrameBorderColor();
  
  @StructureGetter("ThemeSetting")
  ClassicThemeBehavior getThemeSetting();
  
  @StructureSetter("ThemeSetting")
  CDFrameset setThemeSetting(ClassicThemeBehavior behavior);
  
  default List<FramesetLength> getLengths() {
    // Only one of RowQty and ColQty is legal to be > 0
    int qty = getRowCount();
    if(qty < 1) {
      qty = Math.max(0, getColumnCount());
    }
    ByteBuffer data = getVariableData();
    MemoryStructureWrapperService wrapper = MemoryStructureWrapperService.get();
    List<FramesetLength> result = new ArrayList<>(qty);
    for(int i = 0; i < qty; i++) {
      data.position(i * 4); // FRAMESETLENGTH is WORD + WORD
      ByteBuffer buf = data.slice();
      buf.limit(4);
      result.add(wrapper.wrapStructure(FramesetLength.class, buf));
    }
    return result;
  }

  default void setLengths(List<FramesetLength> lengths, boolean isRow) {
    resizeVariableData(4 * lengths.size());
    ByteBuffer data = getVariableData();

    byte[] currLengthData = new byte[4];

    for (int i = 0; i < lengths.size(); i++) {
      FramesetLength currLength = lengths.get(i);
      currLength.getData().get(currLengthData);

      data.position(i * 4);
      data.put(currLengthData);
    }

    if (isRow) {
      setRowCount(lengths.size());
      setColumnCount(0);
    } else {
      setRowCount(0);
      setColumnCount(lengths.size());
    }
  }

}
