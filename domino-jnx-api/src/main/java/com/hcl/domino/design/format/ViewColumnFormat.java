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
package com.hcl.domino.design.format;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;
import com.hcl.domino.richtext.structures.NFMT;
import com.hcl.domino.richtext.structures.NFMT.Format;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;
import com.hcl.domino.richtext.structures.TFMT;
import com.hcl.domino.richtext.structures.TFMT.DateFormat;
import com.hcl.domino.richtext.structures.TFMT.TimeFormat;
import com.hcl.domino.richtext.structures.TFMT.TimeStructure;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "VIEW_COLUMN_FORMAT", members = {
    @StructureMember(name = "Signature", type = short.class),
    @StructureMember(name = "Flags1", type = ViewColumnFormat.Flag.class, bitfield = true),
    @StructureMember(name = "ItemNameSize", type = short.class, unsigned = true),
    @StructureMember(name = "TitleSize", type = short.class, unsigned = true),
    @StructureMember(name = "FormulaSize", type = short.class, unsigned = true),
    @StructureMember(name = "ConstantValueSize", type = short.class, unsigned = true),
    @StructureMember(name = "DisplayWidth", type = short.class, unsigned = true),
    @StructureMember(name = "FontID", type = FontStyle.class),
    @StructureMember(name = "Flags2", type = short.class),
    @StructureMember(name = "NumberFormat", type = NFMT.class),
    @StructureMember(name = "TimeFormat", type = TFMT.class),
    @StructureMember(name = "FormatDataType", type = ViewColumnFormat.DataType.class),
    @StructureMember(name = "ListSep", type = ViewColumnFormat.ListDelimiter.class)
})
public interface ViewColumnFormat extends ResizableMemoryStructure {
  
  public static ViewColumnFormat newInstance() {
    ViewColumnFormat format = MemoryStructureWrapperService.get().newStructure(ViewColumnFormat.class, 0);
    format.setTitle("#"); //$NON-NLS-1$
    format.setReadingOrder(ReadingOrder.LTR);
    format.setDisplayWidth(80);
    format.setTotalType(StatType.NONE);
    
    format
    .getNumberFormat()
    .setFormat(Format.GENERAL)
    .setDigits((short) 0);
    
    format.setFormula("@DocNumber"); //$NON-NLS-1$
//    format.setConstantValueLength(format.getConstantValueLength());
    format.setHeaderAlignment(Alignment.LEFT);
    format.setDataType(DataType.TEXT);
    format.setSignature(ViewFormatConstants.VIEW_COLUMN_FORMAT_SIGNATURE);
    
    format
    .getTimeFormat()
    .setZoneFormat(TimeZoneFormat.NEVER)
    .setTimeStructure(TimeStructure.DATETIME)
    .setTimeFormat(TimeFormat.FULL)
    .setDateFormat(DateFormat.FULL);
    
    format.setAlignment(Alignment.LEFT);
    format.setHeaderReadingOrder(ReadingOrder.LTR);
    format.setItemName("$0"); //$NON-NLS-1$
    format.getFontStyle().setPointSize(10).setFontFace((byte) 1);
    
    format.setListDelimiter(ListDelimiter.NONE);
    
    return format;
  }
  
  enum Alignment implements INumberEnum<Byte> {
    LEFT(ViewFormatConstants.VIEW_COL_ALIGN_LEFT),
    RIGHT(ViewFormatConstants.VIEW_COL_ALIGN_RIGHT),
    CENTER(ViewFormatConstants.VIEW_COL_ALIGN_CENTER);

    private final byte value;

    Alignment(final int value) {
      this.value = (byte) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  enum DataType implements INumberEnum<Short> {
    NUMBER(NotesConstants.VIEW_COL_NUMBER),
    TIMEDATE(NotesConstants.VIEW_COL_TIMEDATE),
    TEXT(NotesConstants.VIEW_COL_TEXT);

    private final short value;

    DataType(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  enum Flag implements INumberEnum<Short> {
    Sort(NotesConstants.VCF1_M_Sort), SortCategorize(NotesConstants.VCF1_M_SortCategorize),
    SortDescending(NotesConstants.VCF1_M_SortDescending), Hidden(NotesConstants.VCF1_M_Hidden),
    Response(NotesConstants.VCF1_M_Response), HideDetail(NotesConstants.VCF1_M_HideDetail),
    Icon(NotesConstants.VCF1_M_Icon), NoResize(NotesConstants.VCF1_M_NoResize),
    ResortAscending(NotesConstants.VCF1_M_ResortAscending), ResortDescending(NotesConstants.VCF1_M_ResortDescending),
    Twistie(NotesConstants.VCF1_M_Twistie), ResortToView(NotesConstants.VCF1_M_ResortToView),
    SecondResort(NotesConstants.VCF1_M_SecondResort), SecondResortDescending(NotesConstants.VCF1_M_SecondResortDescending),
    /**
     * @deprecated replaced by {@link ViewColumnFormat2.Flag3#CaseSensitiveSortInV5}
     */
    CaseInsensitiveSort(NotesConstants.VCF1_M_CaseInsensitiveSort),
    /**
     * @deprecated replaced by
     *             {@link ViewColumnFormat2.Flag3#AccentSensitiveSortInV5}
     */
    AccentInsensitiveSort(NotesConstants.VCF1_M_AccentInsensitiveSort);

    private final short value;

    Flag(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  enum Flag2 implements INumberEnum<Short> {
    HeaderAlignment(NotesConstants.VCF2_M_HeaderAlignment),
    SortPermute(NotesConstants.VCF2_M_SortPermute),
    SecondResortUniqueSort(NotesConstants.VCF2_M_SecondResortUniqueSort),
    SecondResortCategorized(NotesConstants.VCF2_M_SecondResortCategorized),
    SecondResortPermute(NotesConstants.VCF2_M_SecondResortPermute),
    SecondResortPermutePair(NotesConstants.VCF2_M_SecondResortPermutePair),
    ShowValuesAsLinks(NotesConstants.VCF2_M_ShowValuesAsLinks),
    DisplayReadingOrder(NotesConstants.VCF2_M_DisplayReadingOrder),
    HeaderReadingOrder(NotesConstants.VCF2_M_HeaderReadingOrder);

    private final short value;

    Flag2(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  enum ListDelimiter implements INumberEnum<Short> {
    NONE(0),
    SPACE(RichTextConstants.LDDELIM_SPACE),
    COMMA(RichTextConstants.LDDELIM_COMMA),
    SEMICOLON(RichTextConstants.LDDELIM_SEMICOLON),
    NEWLINE(RichTextConstants.LDDELIM_NEWLINE);

    private final short value;

    ListDelimiter(final int value) {
      this.value = (short) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Short getValue() {
      return this.value;
    }
  }

  enum ReadingOrder implements INumberEnum<Byte> {
    LTR(ViewFormatConstants.VIEW_COL_LTR),
    RTL(ViewFormatConstants.VIEW_COL_RTL);

    private final byte value;

    ReadingOrder(final int value) {
      this.value = (byte) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  enum StatType implements INumberEnum<Byte> {
    NONE(NotesConstants.NIF_STAT_NONE),
    TOTAL(NotesConstants.NIF_STAT_TOTAL),
    AVG_PER_CHILD(NotesConstants.NIF_STAT_AVG_PER_CHILD),
    PCT_OVERALL(NotesConstants.NIF_STAT_PCT_OVERALL),
    PCT_PARENT(NotesConstants.NIF_STAT_PCT_PARENT),
    AVG_PER_ENTRY(NotesConstants.NIF_STAT_AVG_PER_ENTRY);

    private final byte value;

    StatType(final int value) {
      this.value = (byte) value;
    }

    @Override
    public long getLongValue() {
      return this.value;
    }

    @Override
    public Byte getValue() {
      return this.value;
    }
  }

  default Alignment getAlignment() {
    final short rawFlags = this.getFlags2Raw();
    return DominoEnumUtil.valueOf(Alignment.class, rawFlags & ViewFormatConstants.VCF2_M_DisplayAlignment)
        .orElse(Alignment.LEFT);
  }

  @StructureGetter("ConstantValueSize")
  int getConstantValueLength();

  @StructureGetter("FormatDataType")
  DataType getDataType();

  @StructureGetter("DisplayWidth")
  int getDisplayWidth();

  @StructureGetter("Flags1")
  Set<Flag> getFlags();

  default Set<Flag2> getFlags2() {
    final short rawFlags = this.getFlags2Raw();
    return DominoEnumUtil.valuesOf(Flag2.class, rawFlags & ViewFormatConstants.VCF2_MASK_FLAGS);
  }

  @StructureGetter("Flags2")
  short getFlags2Raw();

  @StructureGetter("FontID")
  FontStyle getFontStyle();

  default String getFormula() {
    return StructureSupport.extractCompiledFormula(this,
        this.getItemNameLength() + this.getTitleLength(),
        this.getFormulaLength());
  }

  @StructureGetter("FormulaSize")
  int getFormulaLength();

  default Alignment getHeaderAlignment() {
    final short rawFlags = this.getFlags2Raw();
    return DominoEnumUtil
        .valueOf(Alignment.class,
            (rawFlags & ViewFormatConstants.VCF2_M_HeaderAlignment) >> ViewFormatConstants.VCF2_S_HeaderAlignment)
        .orElse(Alignment.LEFT);
  }

  default ReadingOrder getHeaderReadingOrder() {
    final short rawFlags = this.getFlags2Raw();
    return DominoEnumUtil
        .valueOf(ReadingOrder.class,
            (rawFlags & ViewFormatConstants.VCF2_M_HeaderReadingOrder) >> ViewFormatConstants.VCF2_S_HeaderReadingOrder)
        .orElse(ReadingOrder.LTR);
  }

  default String getItemName() {
    return StructureSupport.extractStringValue(this,
        0,
        this.getItemNameLength());
  }

  @StructureGetter("ItemNameSize")
  int getItemNameLength();

  @StructureGetter("ListSep")
  ListDelimiter getListDelimiter();

  @StructureGetter("NumberFormat")
  NFMT getNumberFormat();

  default ReadingOrder getReadingOrder() {
    final short rawFlags = this.getFlags2Raw();
    return DominoEnumUtil
        .valueOf(ReadingOrder.class,
            (rawFlags & ViewFormatConstants.VCF2_M_DisplayReadingOrder) >> ViewFormatConstants.VCF2_S_DisplayReadingOrder)
        .orElse(ReadingOrder.LTR);
  }

  @StructureGetter("Signature")
  short getSignature();

  @StructureGetter("TimeFormat")
  TFMT getTimeFormat();

  default String getTitle() {
    return StructureSupport.extractStringValue(this,
        this.getItemNameLength(),
        this.getTitleLength());
  }

  @StructureGetter("TitleSize")
  int getTitleLength();

  default StatType getTotalType() {
    final short rawFlags = this.getFlags2Raw();
    return DominoEnumUtil
        .valueOf(StatType.class, (rawFlags & ViewFormatConstants.VCF2_M_SubtotalCode) >> ViewFormatConstants.VCF2_S_SubtotalCode)
        .orElse(StatType.NONE);
  }

  default ViewColumnFormat setAlignment(final Alignment alignment) {
    final byte val = alignment == null ? 0 : alignment.getValue();
    final short rawFlags = this.getFlags2Raw();
    this.setFlags2Raw((short) (rawFlags & ~ViewFormatConstants.VCF2_M_DisplayAlignment | val));
    return this;
  }

  @StructureSetter("ConstantValueSize")
  ViewColumnFormat setConstantValueLength(int len);

  @StructureSetter("FormatDataType")
  ViewColumnFormat setDataType(DataType type);

  @StructureSetter("DisplayWidth")
  ViewColumnFormat setDisplayWidth(int len);

  @StructureSetter("Flags1")
  ViewColumnFormat setFlags(Collection<Flag> flags);

  default ViewColumnFormat setFlag(Flag flag, boolean b) {
    Set<Flag> oldFlags = getFlags();
    if (b) {
      if (!oldFlags.contains(flag)) {
        Set<Flag> newFlags = new HashSet<>(oldFlags);
        newFlags.add(flag);
        setFlags(newFlags);
      }
    }
    else {
      if (oldFlags.contains(flag)) {
        Set<Flag> newFlags = oldFlags
            .stream()
            .filter(currFlag -> !flag.equals(currFlag))
            .collect(Collectors.toSet());
        setFlags(newFlags);
      }
    }
    return this;
  }
  
  default ViewColumnFormat setFlag(Flag2 flag, boolean b) {
    Set<Flag2> oldFlags = getFlags2();
    if (b) {
      if (!oldFlags.contains(flag)) {
        Set<Flag2> newFlags = new HashSet<>(oldFlags);
        newFlags.add(flag);
        setFlags2(newFlags);
      }
    }
    else {
      if (oldFlags.contains(flag)) {
        Set<Flag2> newFlags = oldFlags
            .stream()
            .filter(currFlag -> !flag.equals(currFlag))
            .collect(Collectors.toSet());
        setFlags2(newFlags);
      }
    }
    return this;
  }
  
  default ViewColumnFormat setFlags2(final Collection<Flag2> flags) {
    final short val = DominoEnumUtil.toBitField(Flag2.class, flags);
    final short rawFlags = this.getFlags2Raw();
    this.setFlags2Raw((short) (val | rawFlags & ~ViewFormatConstants.VCF2_MASK_FLAGS));
    return this;
  }

  @StructureSetter("Flags2")
  ViewColumnFormat setFlags2Raw(short flags);

  default ViewColumnFormat setFormula(final String formula) {
    return StructureSupport.writeCompiledFormula(
        this,
        this.getItemNameLength() + this.getTitleLength(),
        this.getFormulaLength(),
        formula,
        this::setFormulaLength);
  }

  @StructureSetter("FormulaSize")
  ViewColumnFormat setFormulaLength(int len);

  default ViewColumnFormat setHeaderAlignment(final Alignment alignment) {
    final byte val = alignment == null ? 0 : alignment.getValue();
    final short rawFlags = this.getFlags2Raw();
    this.setFlags2Raw(
        (short) (rawFlags & ~ViewFormatConstants.VCF2_M_HeaderAlignment | val << ViewFormatConstants.VCF2_S_HeaderAlignment));
    return this;
  }

  default ViewColumnFormat setHeaderReadingOrder(final ReadingOrder type) {
    final byte val = type == null ? 0 : type.getValue();
    final short rawFlags = this.getFlags2Raw();
    this.setFlags2Raw(
        (short) (rawFlags & ~ViewFormatConstants.VCF2_M_HeaderReadingOrder | val << ViewFormatConstants.VCF2_S_HeaderReadingOrder));
    return this;
  }

  default ViewColumnFormat setItemName(final String itemName) {
    return StructureSupport.writeStringValue(
        this,
        0,
        this.getItemNameLength(),
        itemName,
        this::setItemNameLength);
  }

  @StructureSetter("ItemNameSize")
  ViewColumnFormat setItemNameLength(int len);

  @StructureSetter("ListSep")
  ViewColumnFormat setListDelimiter(ListDelimiter delim);

  default ViewColumnFormat setReadingOrder(final ReadingOrder type) {
    final byte val = type == null ? 0 : type.getValue();
    final short rawFlags = this.getFlags2Raw();
    this.setFlags2Raw((short) (rawFlags & ~ViewFormatConstants.VCF2_M_DisplayReadingOrder
        | val << ViewFormatConstants.VCF2_S_DisplayReadingOrder));
    return this;
  }

  @StructureSetter("Signature")
  ViewColumnFormat setSignature(short signature);

  default ViewColumnFormat setTitle(final String title) {
    return StructureSupport.writeStringValue(
        this,
        this.getItemNameLength(),
        this.getTitleLength(),
        title,
        this::setTitleLength);
  }

  @StructureSetter("TitleSize")
  ViewColumnFormat setTitleLength(int len);

  default ViewColumnFormat setTotalType(final StatType type) {
    final byte val = type == null ? 0 : type.getValue();
    final short rawFlags = this.getFlags2Raw();
    this.setFlags2Raw(
        (short) (rawFlags & ~ViewFormatConstants.VCF2_M_SubtotalCode | val << ViewFormatConstants.VCF2_S_SubtotalCode));
    return this;
  }
}
