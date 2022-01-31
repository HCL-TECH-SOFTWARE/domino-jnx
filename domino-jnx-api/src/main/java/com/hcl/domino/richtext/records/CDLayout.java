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
package com.hcl.domino.richtext.records;

import java.util.Collection;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.BSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDLAYOUT",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "wLeft", type = short.class, unsigned = true),
    @StructureMember(name = "wWidth", type = short.class, unsigned = true),
    @StructureMember(name = "wHeight", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDLayout.Flag.class, bitfield = true),
    @StructureMember(name = "wGridSize", type = short.class, unsigned = true),
    @StructureMember(name = "Reserved", type = byte[].class, length = 14)
  }
)
public interface CDLayout extends RichTextRecord<BSIG> {
  enum Flag implements INumberEnum<Integer> {
    SHOWBORDER(RichTextConstants.LAYOUT_FLAG_SHOWBORDER),
    SHOWGRID(RichTextConstants.LAYOUT_FLAG_SHOWGRID),
    SNAPTOGRID(RichTextConstants.LAYOUT_FLAG_SNAPTOGRID),
    STYLE3D(RichTextConstants.LAYOUT_FLAG_3DSTYLE),
    RTL(RichTextConstants.LAYOUT_FLAG_RTL),
    DONTWRAP(RichTextConstants.LAYOUT_FLAG_DONTWRAP);
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
  BSIG getHeader();
  
  @StructureGetter("wLeft")
  int getLeft();
  
  @StructureSetter("wLeft")
  CDLayout setLeft(int left);
  
  @StructureGetter("wWidth")
  int getWidth();
  
  @StructureSetter("wWidth")
  CDLayout setWidth(int width);
  
  @StructureGetter("wHeight")
  int getHeight();
  
  @StructureSetter("wHeight")
  CDLayout setHeight(int height);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDLayout setFlags(Collection<Flag> flags);
  
  @StructureGetter("wGridSize")
  int getGridSize();
  
  @StructureSetter("wGridSize")
  CDLayout setGridSize(int gridSize);
}
