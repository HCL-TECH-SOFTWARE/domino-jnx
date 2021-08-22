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
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.misc.DominoEnumUtil;
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
  name = "CDTABLECELL",
  members = {
    @StructureMember(name = "Header", type = BSIG.class),
    @StructureMember(name = "Row", type = byte.class, unsigned = true),
    @StructureMember(name = "Column", type = byte.class, unsigned = true),
    @StructureMember(name = "LeftMargin", type = short.class, unsigned = true),
    @StructureMember(name = "RightMargin", type = short.class, unsigned = true),
    @StructureMember(name = "FractionalWidth", type = short.class, unsigned = true),
    @StructureMember(name = "Border", type = byte.class),
    @StructureMember(name = "Flags", type = CDTableCell.Flag.class, bitfield = true),
    @StructureMember(name = "v42Border", type = short.class),
    @StructureMember(name = "RowSpan", type = byte.class, unsigned = true),
    @StructureMember(name = "ColumnSpan", type = byte.class, unsigned = true),
    @StructureMember(name = "BackgroundColor", type = short.class, unsigned = true)
  }
)
public interface CDTableCell extends RichTextRecord<BSIG> {
  enum Flag implements INumberEnum<Byte> {
    /**  True if background color  */
    USE_BKGCOLOR(RichTextConstants.CDTABLECELL_USE_BKGCOLOR),
    /**  True if version 4.2 or after  */
    USE_V42BORDERS(RichTextConstants.CDTABLECELL_USE_V42BORDERS),
    /**  True if cell is spanned  */
    INVISIBLEH(RichTextConstants.CDTABLECELL_INVISIBLEH),
    /**  True if cell is spanned  */
    INVISIBLEV(RichTextConstants.CDTABLECELL_INVISIBLEV),
    /**  True if gradient color  */
    USE_GRADIENT(RichTextConstants.CDTABLECELL_USE_GRADIENT),
    /**  True if contents centered vertically  */
    VALIGNCENTER(RichTextConstants.CDTABLECELL_VALIGNCENTER),
    /**  True if gradient should go left to right  */
    GRADIENT_LTR(RichTextConstants.CDTABLECELL_GRADIENT_LTR),
    /**  True if contents bottomed vertically  */
    VALIGNBOTTOM(RichTextConstants.CDTABLECELL_VALIGNBOTTOM);
    private final byte value;
    private Flag(byte value) {
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
  
  enum TableBorder implements INumberEnum<Byte> {
    NONE(RichTextConstants.TABLE_BORDER_NONE),
    SINGLE(RichTextConstants.TABLE_BORDER_SINGLE),
    DOUBLE(RichTextConstants.TABLE_BORDER_DOUBLE);
    private final byte value;
    private TableBorder(byte value) {
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
  BSIG getHeader();
  
  @StructureGetter("Row")
  short getRow();
  
  @StructureSetter("Row")
  CDTableCell setRow(short row);
  
  @StructureGetter("Column")
  short getColumn();
  
  @StructureSetter("Column")
  CDTableCell setColumn(short column);
  
  @StructureGetter("LeftMargin")
  int getLeftMargin();
  
  @StructureSetter("LeftMargin")
  CDTableCell setLeftMargin(int margin);
  
  @StructureGetter("RightMargin")
  int getRightMargin();
  
  @StructureSetter("RightMargin")
  CDTableCell setRightMargin(int margin);
  
  @StructureGetter("FractionalWidth")
  int getFractionalWidth();
  
  @StructureSetter("FractionalWidth")
  CDTableCell setFractionalWidth(int width);
  
  @StructureGetter("Border")
  byte getBordersRaw();
  
  @StructureSetter("Border")
  CDTableCell setBordersRaw(byte borders);
  
  @StructureGetter("Flags")
  Set<Flag> getFlags();

  @StructureSetter("Flags")
  CDTableCell setFlags(Collection<Flag> flags);
  
  @StructureGetter("v42Border")
  short getBorderWidthsRaw();
  
  @StructureSetter("v42Border")
  CDTableCell setBorderWidthsRaw(short widths);
  
  @StructureGetter("RowSpan")
  short getRowSpan();
  
  @StructureSetter("RowSpan")
  CDTableCell setRowSpan(short span);
  
  @StructureGetter("ColumnSpan")
  short getColumnSpan();
  
  @StructureSetter("ColumnSpan")
  CDTableCell setColumnSpan(short span);
  
  @StructureGetter("BackgroundColor")
  short getBackgroundColor();
  
  @StructureSetter("BackgroundColor")
  CDTableCell setBackgroundColor(short color);
  
  default Optional<TableBorder> getBorderLeft() {
    byte val = (byte)((getBordersRaw() & RichTextConstants.CDTC_M_Left) >> RichTextConstants.CDTC_S_Left);
    return DominoEnumUtil.valueOf(TableBorder.class, val);
  }
  
  default CDTableCell setBorderLeft(TableBorder style) {
    byte val = style == null ? 0 : style.getValue();
    byte existing = (byte)(getBordersRaw() & ~RichTextConstants.CDTC_M_Left);
    byte newVal = (byte)(val << RichTextConstants.CDTC_S_Left);
    setBordersRaw((byte)(existing | newVal));
    return this;
  }
  
  default Optional<TableBorder> getBorderRight() {
    byte val = (byte)((getBordersRaw() & RichTextConstants.CDTC_M_Right) >> RichTextConstants.CDTC_S_Right);
    return DominoEnumUtil.valueOf(TableBorder.class, val);
  }
  
  default CDTableCell setBorderRight(TableBorder style) {
    byte val = style == null ? 0 : style.getValue();
    byte existing = (byte)(getBordersRaw() & ~RichTextConstants.CDTC_M_Right);
    byte newVal = (byte)(val << RichTextConstants.CDTC_S_Right);
    setBordersRaw((byte)(existing | newVal));
    return this;
  }
  
  default Optional<TableBorder> getBorderTop() {
    byte val = (byte)((getBordersRaw() & RichTextConstants.CDTC_M_Top) >> RichTextConstants.CDTC_S_Top);
    return DominoEnumUtil.valueOf(TableBorder.class, val);
  }
  
  default CDTableCell setBorderTop(TableBorder style) {
    byte val = style == null ? 0 : style.getValue();
    byte existing = (byte)(getBordersRaw() & ~RichTextConstants.CDTC_M_Top);
    byte newVal = (byte)(val << RichTextConstants.CDTC_S_Top);
    setBordersRaw((byte)(existing | newVal));
    return this;
  }
  
  default Optional<TableBorder> getBorderBottom() {
    byte val = (byte)((getBordersRaw() & RichTextConstants.CDTC_M_Bottom) >> RichTextConstants.CDTC_S_Bottom);
    return DominoEnumUtil.valueOf(TableBorder.class, val);
  }
  
  default CDTableCell setBorderBottom(TableBorder style) {
    byte val = style == null ? 0 : style.getValue();
    byte existing = (byte)(getBordersRaw() & ~RichTextConstants.CDTC_M_Bottom);
    byte newVal = (byte)(val << RichTextConstants.CDTC_S_Bottom);
    setBordersRaw((byte)(existing | newVal));
    return this;
  }
  
  default short getBorderWidthLeft() {
    return (short)((getBorderWidthsRaw() & RichTextConstants.CDTC_M_V42_Left) >> RichTextConstants.CDTC_S_V42_Left);
  }
  
  default CDTableCell setBorderWidthLeft(short width) {
    short existing = (short)(getBorderWidthsRaw() & ~RichTextConstants.CDTC_M_V42_Left);
    short newVal = (short)(width << RichTextConstants.CDTC_S_V42_Left);
    setBorderWidthsRaw((short)(existing | newVal));
    return this;
  }
  
  default short getBorderWidthRight() {
    return (short)((getBorderWidthsRaw() & RichTextConstants.CDTC_M_V42_Right) >> RichTextConstants.CDTC_S_V42_Right);
  }
  
  default CDTableCell setBorderWidthRight(short width) {
    short existing = (short)(getBorderWidthsRaw() & ~RichTextConstants.CDTC_M_V42_Right);
    short newVal = (short)(width << RichTextConstants.CDTC_S_V42_Right);
    setBorderWidthsRaw((short)(existing | newVal));
    return this;
  }
  
  default short getBorderWidthTop() {
    return (short)((getBorderWidthsRaw() & RichTextConstants.CDTC_M_V42_Top) >> RichTextConstants.CDTC_S_V42_Top);
  }
  
  default CDTableCell setBorderWidthTop(short width) {
    short existing = (short)(getBorderWidthsRaw() & ~RichTextConstants.CDTC_M_V42_Top);
    short newVal = (short)(width << RichTextConstants.CDTC_S_V42_Top);
    setBorderWidthsRaw((short)(existing | newVal));
    return this;
  }
  
  default short getBorderWidthBottom() {
    return (short)((getBorderWidthsRaw() & RichTextConstants.CDTC_M_V42_Bottom) >> RichTextConstants.CDTC_S_V42_Bottom);
  }
  
  default CDTableCell setBorderWidthBottom(short width) {
    short existing = (short)(getBorderWidthsRaw() & ~RichTextConstants.CDTC_M_V42_Bottom);
    short newVal = (short)(width << RichTextConstants.CDTC_S_V42_Bottom);
    setBorderWidthsRaw((short)(existing | newVal));
    return this;
  }
}
