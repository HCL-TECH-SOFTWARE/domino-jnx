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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.34
 */
@StructureDefinition(
  name = "CDPRETABLEBEGIN",
  members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "Flags", type = CDPreTableBegin.Flag.class, bitfield = true),
    @StructureMember(name = "Rows", type = byte.class, unsigned = true),
    @StructureMember(name = "Columns", type = byte.class, unsigned = true),
    @StructureMember(name = "ColumnSizingBits1", type = int.class),
    @StructureMember(name = "ColumnSizingBits2", type = int.class),
    @StructureMember(name = "ViewerType", type = CDPreTableBegin.ViewerType.class),
    @StructureMember(name = "Spare", type = byte.class),
    @StructureMember(name = "MinRowHeight", type = short.class, unsigned = true),
    @StructureMember(name = "Spares", type = short.class),
    @StructureMember(name = "StyleColor1", type = int.class),
    @StructureMember(name = "StyleColor2", type = int.class),
    @StructureMember(name = "InnerBorderColor", type = ColorValue.class),
    @StructureMember(name = "NameLength", type = short.class, unsigned = true),
    @StructureMember(name = "ImagePacketLength", type = short.class),
    @StructureMember(name = "RowLabelDataLength", type = short.class),
  }
)
public interface CDPreTableBegin extends RichTextRecord<WSIG> {
  enum Flag implements INumberEnum<Integer> {
    /**  True if automatic cell width calculation  */
    AUTO_CELL_WIDTH(RichTextConstants.CDPRETABLE_AUTO_CELL_WIDTH),
    DONTWRAP(RichTextConstants.CDPRETABLE_DONTWRAP),
    DROPSHADOW(RichTextConstants.CDPRETABLE_DROPSHADOW),
    FIELDDRIVEN(RichTextConstants.CDPRETABLE_FIELDDRIVEN),
    V4SPACING(RichTextConstants.CDPRETABLE_V4SPACING),
    USEBORDERCOLOR(RichTextConstants.CDPRETABLE_USEBORDERCOLOR),
    /**  True if the table width equal to window width  */
    WIDTHSAMEASWINDOW(RichTextConstants.CDPRETABLE_WIDTHSAMEASWINDOW),
    /**  True if field driven table should also show tabs  */
    SHOWTABS(RichTextConstants.CDPRETABLE_SHOWTABS),
    SHOWTABSONLEFT(RichTextConstants.CDPRETABLE_SHOWTABSONLEFT),
    SHOWTABSONBOTTOM(RichTextConstants.CDPRETABLE_SHOWTABSONBOTTOM),
    SHOWTABSONRIGHT(RichTextConstants.CDPRETABLE_SHOWTABSONRIGHT);
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
  enum ViewerType implements INumberEnum<Byte> {
    ONCLICK(RichTextConstants.CDTABLEVIEWER_ONCLICK),
    ONLOADTIMER(RichTextConstants.CDTABLEVIEWER_ONLOADTIMER),
    ONLOADCYCLEONCE(RichTextConstants.CDTABLEVIEWER_ONLOADCYCLEONCE),
    TABS(RichTextConstants.CDTABLEVIEWER_TABS),
    FIELDDRIVEN(RichTextConstants.CDTABLEVIEWER_FIELDDRIVEN),
    CYCLEONCE(RichTextConstants.CDTABLEVIEWER_CYCLEONCE),
    CAPTIONS(RichTextConstants.CDTABLEVIEWER_CAPTIONS),
    LAST(RichTextConstants.CDTABLEVIEWER_LAST);
    private final byte value;
    private ViewerType(byte value) {
      this.value = value;
    }

    @Override
    public long getLongValue() {
      return value;
    }

    @Override
    public Byte getValue() {
      return value;
    }
  }

  @StructureGetter("Header")
  @Override
  WSIG getHeader();
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  CDPreTableBegin setFlags(Collection<Flag> flags);
  
  @StructureGetter("Rows")
  short getRows();
  
  @StructureSetter("Rows")
  CDPreTableBegin setRows(short rows);
  
  @StructureGetter("Columns")
  short getColumns();
  
  @StructureSetter("Columns")
  CDPreTableBegin setColumns(short columns);
  
  @StructureGetter("ColumnSizingBits1")
  int getColumnSizingBits1Raw();
  
  @StructureSetter("ColumnSizingBits1")
  CDPreTableBegin setColumnSizingBits1Raw(int sizing);
  
  @StructureGetter("ColumnSizingBits2")
  int getColumnSizingBits2Raw();
  
  @StructureSetter("ColumnSizingBits2")
  CDPreTableBegin setColumnSizingBits2Raw(int sizing);
  
  @StructureGetter("ViewerType")
  Optional<ViewerType> getViewerType();
  
  @StructureSetter("ViewerType")
  CDPreTableBegin setViewerType(ViewerType type);
  
  @StructureGetter("MinRowHeight")
  int getMinRowHeight();
  
  @StructureSetter("MinRowHeight")
  CDPreTableBegin setMinRowHeight(int height);
  
  @StructureGetter("StyleColor1")
  int getStyleColor1();
  
  @StructureSetter("StyleColor1")
  CDPreTableBegin setStyleColor1(int color);
  
  @StructureGetter("StyleColor2")
  int getStyleColor2();
  
  @StructureSetter("StyleColor2")
  CDPreTableBegin setStyleColor2(int color);
  
  @StructureGetter("InnerBorderColor")
  ColorValue getInnerBorderColor();
  
  @StructureGetter("NameLength")
  int getNameLength();
  
  @StructureSetter("NameLength")
  CDPreTableBegin setNameLength(int len);
  
  default String getName() {
    return StructureSupport.extractStringValue(
      this,
      0,
      getNameLength()
    );
  }
  
  default CDPreTableBegin setName(String name) {
    return StructureSupport.writeStringValue(
      this,
      0,
      getNameLength(),
      name,
      this::setNameLength
    );
  }
}
