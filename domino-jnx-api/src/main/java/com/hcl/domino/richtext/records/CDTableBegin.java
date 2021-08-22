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
  name = "CDTABLEBEGIN",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "LeftMargin", type = short.class, unsigned = true),
    @StructureMember(name = "HorizInterCellSpace", type = short.class, unsigned = true),
    @StructureMember(name = "VertInterCellSpace", type = short.class, unsigned = true),
    @StructureMember(name = "V4HorizInterCellSpace", type = short.class, unsigned = true),
    @StructureMember(name = "V4VertInterCellSpace", type = short.class, unsigned = true),
    @StructureMember(name = "Flags", type = CDTableBegin.Flag.class, bitfield = true),
  }
)
public interface CDTableBegin extends RichTextRecord<BSIG> {
  enum Flag implements INumberEnum<Short> {
    /**  True if automatic cell width calculation  */
    AUTO_CELL_WIDTH(RichTextConstants.CDTABLE_AUTO_CELL_WIDTH),
    /**  True if the table was created in v4  */
    V4_BORDERS(RichTextConstants.CDTABLE_V4_BORDERS),
    /**  True if the table uses embossed borders  */
    BORDER3D_EMBOSS(RichTextConstants.CDTABLE_3D_BORDER_EMBOSS),
    /**  True if the table uses extruded borders  */
    BORDER3D_EXTRUDE(RichTextConstants.CDTABLE_3D_BORDER_EXTRUDE),
    /**  True if the table reading order is right to left  */
    BIDI_RTLTABLE(RichTextConstants.CDTABLE_BIDI_RTLTABLE),
    /**  True if the table alignment is right  */
    ALIGNED_RIGHT(RichTextConstants.CDTABLE_ALIGNED_RIGHT),
    /**  True if the table is collapsible to one row  */
    COLLAPSIBLE(RichTextConstants.CDTABLE_COLLAPSIBLE),
    LEFTTOP(RichTextConstants.CDTABLE_LEFTTOP),
    TOP(RichTextConstants.CDTABLE_TOP),
    LEFT(RichTextConstants.CDTABLE_LEFT),
    ALTERNATINGCOLS(RichTextConstants.CDTABLE_ALTERNATINGCOLS),
    ALTERNATINGROWS(RichTextConstants.CDTABLE_ALTERNATINGROWS),
    RIGHTTOP(RichTextConstants.CDTABLE_RIGHTTOP),
    RIGHT(RichTextConstants.CDTABLE_RIGHT),
    /**  all styles on means solid color */
    SOLID(RichTextConstants.CDTABLE_SOLID),
    TEMPLATEBITS(RichTextConstants.CDTABLE_TEMPLATEBITS),
    /**  True if the table alignment is center  */
    ALIGNED_CENTER(RichTextConstants.CDTABLE_ALIGNED_CENTER),
    /**  True if the table rows text flows cell to cell  */
    TEXTFLOWS(RichTextConstants.CDTABLE_TEXTFLOWS);
    private final short value;
    private Flag(short value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Short getValue() {
      return value;
    }
  }
  
  @StructureGetter("Header")
  @Override
  BSIG getHeader();
  
  @StructureGetter("LeftMargin")
  int getLeftMargin();
  
  @StructureSetter("LeftMargin")
  CDTableBegin setLeftMargin(int margin);
  
  @StructureGetter("HorizInterCellSpace")
  int getPreV4HorizontalInterCellSpace();
  
  @StructureSetter("HorizInterCellSpace")
  CDTableBegin setPreV4HorizontalInterCellSpace(int space);
  
  @StructureGetter("VertInterCellSpace")
  int getPreV4VerticalInterCellSpace();
  
  @StructureSetter("VertInterCellSpace")
  CDTableBegin setPreV4VerticalInterCellSpace(int space);
  
  @StructureGetter("V4HorizInterCellSpace")
  int getHorizontalInterCellSpace();
  
  @StructureSetter("V4HorizInterCellSpace")
  CDTableBegin setHorizontalInterCellSpace(int space);
  
  @StructureGetter("V4VertInterCellSpace")
  int getVerticalInterCellSpace();
  
  @StructureSetter("V4VertInterCellSpace")
  CDTableBegin setVerticalInterCellSpace(int space);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();
  
  @StructureSetter("Flags")
  CDTableBegin setFlags(Collection<Flag> flags);
}
