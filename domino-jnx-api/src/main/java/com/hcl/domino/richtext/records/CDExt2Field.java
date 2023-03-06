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

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.data.NativeItemCoder;
import com.hcl.domino.design.format.DateShowFormat;
import com.hcl.domino.design.format.DateShowSpecial;
import com.hcl.domino.design.format.DateTimeFlag;
import com.hcl.domino.design.format.DateTimeFlag2;
import com.hcl.domino.design.format.DayFormat;
import com.hcl.domino.design.format.MonthFormat;
import com.hcl.domino.design.format.NumberPref;
import com.hcl.domino.design.format.TimeShowFormat;
import com.hcl.domino.design.format.TimeZoneFormat;
import com.hcl.domino.design.format.WeekFormat;
import com.hcl.domino.design.format.YearFormat;
import com.hcl.domino.formula.FormulaCompiler;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.WSIG;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(name = "CDEXT2FIELD", members = {
    @StructureMember(name = "Header", type = WSIG.class),
    @StructureMember(name = "NumSymPref", type = NumberPref.class),
    @StructureMember(name = "NumSymFlags", type = byte.class),
    @StructureMember(name = "DecimalSymLength", type = int.class, unsigned = true),
    @StructureMember(name = "MilliSepSymLength", type = int.class, unsigned = true),
    @StructureMember(name = "NegativeSymLength", type = int.class, unsigned = true),
    @StructureMember(name = "MilliGroupSize", type = short.class, unsigned = true),
    @StructureMember(name = "VerticalSpacing", type = short.class),
    @StructureMember(name = "HorizontalSpacing", type = short.class),
    @StructureMember(name = "Unused2", type = short.class),
    @StructureMember(name = "FirstFieldLimitType", type = short.class, unsigned = true),
    @StructureMember(name = "CurrencyPref", type = NumberPref.class),
    @StructureMember(name = "CurrencyType", type = CurrencyType.class),
    @StructureMember(name = "CurrencyFlags", type = CurrencyFlag.class, bitfield = true),
    @StructureMember(name = "CurrencySymLength", type = int.class, unsigned = true),
    @StructureMember(name = "ISOCountry", type = int.class, unsigned = true),
    @StructureMember(name = "ThumbnailImageWidth", type = short.class, unsigned = true),
    @StructureMember(name = "ThumbnailImageHeight", type = short.class, unsigned = true),
    @StructureMember(name = "wThumbnailImageFileNameLength", type = short.class, unsigned = true),
    @StructureMember(name = "wIMOnlineNameFormulaLen", type = short.class, unsigned = true),
    @StructureMember(name = "DTPref", type = NumberPref.class),
    @StructureMember(name = "DTFlags", type = DateTimeFlag.class, bitfield = true),
    @StructureMember(name = "DTFlags2", type = DateTimeFlag2.class, bitfield = true),
    @StructureMember(name = "DTDOWFmt", type = WeekFormat.class),
    @StructureMember(name = "DTYearFmt", type = YearFormat.class),
    @StructureMember(name = "DTMonthFmt", type = MonthFormat.class),
    @StructureMember(name = "DTDayFmt", type = DayFormat.class),
    @StructureMember(name = "DTDsep1Len", type = byte.class, unsigned = true),
    @StructureMember(name = "DTDsep2Len", type = byte.class, unsigned = true),
    @StructureMember(name = "DTDsep3Len", type = byte.class, unsigned = true),
    @StructureMember(name = "DTTsepLen", type = byte.class, unsigned = true),
    @StructureMember(name = "DTDShow", type = DateShowFormat.class),
    @StructureMember(name = "DTDSpecial", type = DateShowSpecial.class, bitfield = true),
    @StructureMember(name = "DTTShow", type = TimeShowFormat.class),
    @StructureMember(name = "DTTZone", type = TimeZoneFormat.class),
    @StructureMember(name = "Unused5", type = int.class),
    @StructureMember(name = "ECFlags", type = CDExt2Field.FormatFlag.class, bitfield = true),
    @StructureMember(name = "Unused612", type = byte.class),
    @StructureMember(name = "wCharacters", type = short.class, unsigned = true),
    @StructureMember(name = "wInputEnabledLen", type = short.class, unsigned = true),
    @StructureMember(name = "wIMGroupFormulaLen", type = short.class, unsigned = true)
})
public interface CDExt2Field extends RichTextRecord<WSIG> {
  enum FormatFlag implements INumberEnum<Byte> {
    PROPORTIONAL((byte) RichTextConstants.EC_FLAG_WIDTH_PROPORTIONAL);

    private final byte value;

    FormatFlag(final byte value) {
      this.value = value;
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

  @StructureGetter("CurrencyFlags")
  Set<CurrencyFlag> getCurrencyFlags();

  @StructureGetter("CurrencyPref")
  Optional<NumberPref> getCurrencyPreference();

  /**
   * Retrieves the currency pref as a raw {@code byte}.
   * 
   * @return the currency pref as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("CurrencyPref")
  byte getCurrencyPreferenceRaw();

  default String getCurrencySymbol() {
    final int len = (int) this.getCurrencySymbolLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("CurrencySymLength")
  long getCurrencySymbolLength();

  @StructureGetter("CurrencyType")
  Optional<CurrencyType> getCurrencyType();

  /**
   * Retrieves the currency type as a raw {@code byte}.
   * 
   * @return the currency type as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("CurrencyType")
  byte getCurrencyTypeRaw();

  default String getDateSeparator1() {
    final int len = this.getDateSeparator1Length();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("DTDsep1Len")
  short getDateSeparator1Length();

  default String getDateSeparator2() {
    final int len = this.getDateSeparator2Length();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength() + this.getDateSeparator1Length());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("DTDsep2Len")
  short getDateSeparator2Length();

  default String getDateSeparator3() {
    final int len = this.getDateSeparator3Length();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("DTDsep3Len")
  short getDateSeparator3Length();

  @StructureGetter("DTDShow")
  Optional<DateShowFormat> getDateShowFormat();

  /**
   * Retrieves the date-show format as a {@code byte}.
   * 
   * @return the date-show format as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTDShow")
  byte getDateShowFormatRaw();

  @StructureGetter("DTDSpecial")
  Set<DateShowSpecial> getDateShowSpecial();

  @StructureGetter("DTFlags")
  Set<DateTimeFlag> getDateTimeFlags();

  @StructureGetter("DTFlags2")
  Set<DateTimeFlag2> getDateTimeFlags2();

  @StructureGetter("DTPref")
  Optional<NumberPref> getDateTimePreference();

  /**
   * Retrieves the date-time pref as a raw {@code byte}.
   * 
   * @return the date-time pref as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTPref")
  byte getDateTimePreferenceRaw();

  @StructureGetter("DTDayFmt")
  Optional<DayFormat> getDayFormat();

  /**
   * Retrieves the day format as a raw {@code byte}.
   * 
   * @return the day format as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTDayFmt")
  byte getDayFormatRaw();

  @StructureGetter("DTDOWFmt")
  Optional<WeekFormat> getDayOfWeekFormat();

  /**
   * Retrieves the day-of-week format as a raw {@code byte}.
   * 
   * @return the day-of-week format as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTDOWFmt")
  byte getDayOfWeekFormatRaw();

  default String getDecimalSymbol() {
    final int len = (int) this.getDecimalSymbolLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final ByteBuffer buf = this.getVariableData();
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("DecimalSymLength")
  long getDecimalSymbolLength();

  @StructureGetter("ECFlags")
  Set<FormatFlag> getFormatFlags();

  @StructureGetter("Header")
  @Override
  WSIG getHeader();

  @StructureGetter("HorizontalSpacing")
  short getHorizontalSpacing();

  default String getIMGroupFormula() {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
      + this.getCurrencySymbolLength()
      + this.getThumbnailImageFileNameLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length()
      + this.getDateSeparator3Length() + this.getTimeSeparatorLength() + this.getIMOnlineNameFormulaLength()
      + this.getInputEnabledFormulaLength());
    
    // This appears to actually be stored as a string
    return StructureSupport.extractStringValue(
      this,
      preLen,
      getIMGroupFormulaLength()
    );
//    final int len = this.getIMGroupFormulaLength();
//    if (len == 0) {
//      return ""; //$NON-NLS-1$
//    }
//
//    final ByteBuffer buf = this.getVariableData();
//    buf.position(preLen);
//    final byte[] compiled = new byte[len];
//    buf.get(compiled);
//    return FormulaCompiler.get().decompile(compiled);
  }

  @StructureGetter("wIMGroupFormulaLen")
  int getIMGroupFormulaLength();

  default String getIMOnlineNameFormula() {
    final int len = this.getIMOnlineNameFormulaLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] compiled = new byte[len];
    buf.get(compiled);
    return FormulaCompiler.get().decompile(compiled);
  }

  @StructureGetter("wIMOnlineNameFormulaLen")
  int getIMOnlineNameFormulaLength();

  default String getInputEnabledFormula() {
    final int len = this.getInputEnabledFormulaLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength()
        + this.getThumbnailImageFileNameLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length()
        + this.getDateSeparator3Length() + this.getTimeSeparatorLength() + this.getIMOnlineNameFormulaLength());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] compiled = new byte[len];
    buf.get(compiled);
    return FormulaCompiler.get().decompile(compiled);
  }

  @StructureGetter("wInputEnabledLen")
  int getInputEnabledFormulaLength();

  @StructureGetter("ISOCountry")
  long getISOCountry();

  @StructureGetter("MilliGroupSize")
  int getMilliGroupSize();

  default String getMilliSeparator() {
    final int len = (int) this.getMilliSeparatorLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) this.getDecimalSymbolLength();

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("MilliSepSymLength")
  long getMilliSeparatorLength();

  @StructureGetter("DTMonthFmt")
  Optional<MonthFormat> getMonthFormat();

  /**
   * Retrieves the month format as a raw {@code byte}.
   * 
   * @return the month format as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTMonthFmt")
  byte getMonthFormatRaw();

  default String getNegativeSymbol() {
    final int len = (int) this.getNegativeSymbolLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("NegativeSymLength")
  long getNegativeSymbolLength();

  @StructureGetter("NumSymPref")
  Optional<NumberPref> getNumberSymbolPreference();

  /**
   * Retrieves the number-symbol pref as a {@code byte}.
   * 
   * @return the number-symbol pref as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("NumSymPref")
  byte getNumberSymbolPreferenceRaw();

  @StructureGetter("wCharacters")
  int getProportionalWidthCharacters();

  default String getThumbnailImageFileName() {
    final int len = this.getThumbnailImageFileNameLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("wThumbnailImageFileNameLength")
  int getThumbnailImageFileNameLength();

  @StructureGetter("ThumbnailImageHeight")
  int getThumbnailImageHeight();

  @StructureGetter("ThumbnailImageWidth")
  int getThumbnailImageWidth();

  default String getTimeSeparator() {
    final int len = this.getTimeSeparatorLength();
    if (len == 0) {
      return ""; //$NON-NLS-1$
    }

    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length()
        + this.getDateSeparator3Length());

    final ByteBuffer buf = this.getVariableData();
    buf.position(preLen);
    final byte[] lmbcs = new byte[len];
    buf.get(lmbcs);
    return new String(lmbcs, NativeItemCoder.get().getLmbcsCharset());
  }

  @StructureGetter("DTTsepLen")
  short getTimeSeparatorLength();

  @StructureGetter("DTTShow")
  Optional<TimeShowFormat> getTimeShowFormat();

  @StructureGetter("DTTShow")
  byte getTimeShowFormatRaw();

  @StructureGetter("DTTZone")
  Optional<TimeZoneFormat> getTimeZoneFormat();

  /**
   * Retrieves the time-zone format as a raw {@code byte}.
   * 
   * @return the time-zone format as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTTZone")
  byte getTimeZoneFormatRaw();

  @StructureGetter("VerticalSpacing")
  short getVerticalSpacing();

  @StructureGetter("DTYearFmt")
  Optional<YearFormat> getYearFormat();

  /**
   * Retrieves the year format as a raw {@code byte}.
   * 
   * @return the year format as a {@code byte}
   * @since 1.24.0
   */
  @StructureGetter("DTYearFmt")
  byte getYearFormatRaw();

  @StructureSetter("CurrencyFlags")
  CDExt2Field setCurrencyFlags(Collection<CurrencyFlag> flags);

  @StructureSetter("CurrencyPref")
  CDExt2Field setCurrencyPreference(NumberPref pref);

  /**
   * Sets the currency pref as a raw {@code byte}.
   * 
   * @param pref the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CurrencyPref")
  CDExt2Field setCurrencyPreferenceRaw(byte pref);

  default CDExt2Field setCurrencySymbol(final String symbol) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength());
    final int currentLen = (int) this.getCurrencySymbolLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] lmbcs = symbol == null ? new byte[0] : symbol.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setCurrencySymbolLength(lmbcs.length);
    final int newLen = preLen + lmbcs.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(lmbcs);
    buf.put(postData);

    return this;
  }

  @StructureSetter("CurrencySymLength")
  CDExt2Field setCurrencySymbolLength(long len);

  @StructureSetter("CurrencyType")
  CDExt2Field setCurrencyType(CurrencyType type);

  /**
   * Sets the currency type as a raw {@code byte}.
   * 
   * @param type the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("CurrencyType")
  CDExt2Field setCurrencyTypeRaw(byte type);

  default CDExt2Field setDateSeparator1(final String sep) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength());
    final int currentLen = this.getDateSeparator1Length();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] compiled = sep == null ? new byte[0] : sep.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setDateSeparator1Length((short) compiled.length);
    final int newLen = preLen + compiled.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(compiled);
    buf.put(postData);

    return this;
  }

  @StructureSetter("DTDsep1Len")
  CDExt2Field setDateSeparator1Length(short len);

  default CDExt2Field setDateSeparator2(final String sep) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength() + this.getDateSeparator1Length());
    final int currentLen = this.getDateSeparator2Length();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] compiled = sep == null ? new byte[0] : sep.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setDateSeparator2Length((short) compiled.length);
    final int newLen = preLen + compiled.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(compiled);
    buf.put(postData);

    return this;
  }

  @StructureSetter("DTDsep2Len")
  CDExt2Field setDateSeparator2Length(short len);

  default CDExt2Field setDateSeparator3(final String sep) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length());
    final int currentLen = this.getDateSeparator3Length();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] compiled = sep == null ? new byte[0] : sep.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setDateSeparator3Length((short) compiled.length);
    final int newLen = preLen + compiled.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(compiled);
    buf.put(postData);

    return this;
  }

  @StructureSetter("DTDsep3Len")
  CDExt2Field setDateSeparator3Length(short len);

  @StructureSetter("DTDShow")
  CDExt2Field setDateShowFormat(DateShowFormat format);

  /**
   * Sets the date-show format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTDShow")
  CDExt2Field setDateShowFormatRaw(byte format);

  @StructureSetter("DTDSpecial")
  CDExt2Field setDateShowSpecial(Collection<DateShowSpecial> format);

  @StructureSetter("DTFlags")
  CDExt2Field setDateTimeFlags(Collection<DateTimeFlag> flags);

  @StructureSetter("DTFlags2")
  CDExt2Field setDateTimeFlags2(Collection<DateTimeFlag2> flags);

  @StructureSetter("DTPref")
  CDExt2Field setDateTimePreference(NumberPref pref);

  /**
   * Sets the date-time pref as a raw {@code byte}.
   * 
   * @param pref the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTPref")
  CDExt2Field setDateTimePreferenceRaw(byte pref);

  @StructureSetter("DTDayFmt")
  CDExt2Field setDayFormat(DayFormat format);

  /**
   * Sets the day format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTDayFmt")
  CDExt2Field setDayFormatRaw(byte format);

  @StructureSetter("DTDOWFmt")
  CDExt2Field setDayOfWeekFormat(WeekFormat format);

  /**
   * Sets the day-of-week format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTDOWFmt")
  CDExt2Field setDayOfWeekFormatRaw(byte format);

  default CDExt2Field setDecimalSymbol(final String symbol) {
    final int currentLen = (int) this.getDecimalSymbolLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen;

    buf.position(currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] lmbcs = symbol == null ? new byte[0] : symbol.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setDecimalSymbolLength(lmbcs.length);
    final int newLen = lmbcs.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.put(lmbcs);
    buf.put(postData);

    return this;
  }

  @StructureSetter("DecimalSymLength")
  CDExt2Field setDecimalSymbolLength(long len);

  @StructureSetter("ECFlags")
  CDExt2Field setFormatFlags(Collection<FormatFlag> flags);

  @StructureSetter("HorizontalSpacing")
  CDExt2Field setHorizontalSpacing(short spacing);

  default CDExt2Field setIMGroupFormula(final String formula) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength()
        + this.getThumbnailImageFileNameLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length()
        + this.getDateSeparator3Length() + this.getTimeSeparatorLength() + this.getIMOnlineNameFormulaLength()
        + this.getInputEnabledFormulaLength());

    // This appears to actually be stored as a string
    return StructureSupport.writeStringValue(
      this,
      preLen,
      getIMGroupFormulaLength(),
      formula,
      this::setIMGroupFormulaLength
    );
//    final int currentLen = this.getIMGroupFormulaLength();
//    ByteBuffer buf = this.getVariableData();
//    final int postLen = buf.remaining() - currentLen - preLen;
//
//    buf.position(preLen + currentLen);
//    final byte[] postData = new byte[postLen];
//    buf.get(postData);
//
//    final byte[] compiled = FormulaCompiler.get().compile(formula);
//    this.setIMGroupFormulaLength(compiled.length);
//    final int newLen = preLen + compiled.length + postLen;
//    this.resizeVariableData(newLen);
//    buf = this.getVariableData();
//    buf.position(preLen);
//    buf.put(compiled);
//    buf.put(postData);
//
//    return this;
  }

  @StructureSetter("wIMGroupFormulaLen")
  CDExt2Field setIMGroupFormulaLength(int len);

  default CDExt2Field setIMOnlineNameFormula(final String formula) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength());
    final int currentLen = this.getIMOnlineNameFormulaLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] compiled = FormulaCompiler.get().compile(formula);
    this.setIMOnlineNameFormulaLength(compiled.length);
    final int newLen = preLen + compiled.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(compiled);
    buf.put(postData);

    return this;
  }

  @StructureSetter("wIMOnlineNameFormulaLen")
  CDExt2Field setIMOnlineNameFormulaLength(int len);

  default CDExt2Field setInputEnabledFormula(final String formula) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength()
        + this.getThumbnailImageFileNameLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length()
        + this.getDateSeparator3Length() + this.getTimeSeparatorLength() + this.getIMOnlineNameFormulaLength());
    final int currentLen = this.getInputEnabledFormulaLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] compiled = FormulaCompiler.get().compile(formula);
    this.setInputEnabledFormulaLength(compiled.length);
    final int newLen = preLen + compiled.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(compiled);
    buf.put(postData);

    return this;
  }

  @StructureSetter("wInputEnabledLen")
  CDExt2Field setInputEnabledFormulaLength(int len);

  @StructureSetter("ISOCountry")
  CDExt2Field setISOCountry(long countryCode);

  @StructureSetter("MilliGroupSize")
  CDExt2Field setMilliGroupSize(int size);

  default CDExt2Field setMilliSeparator(final String sep) {
    final int preLen = (int) this.getDecimalSymbolLength();
    final int currentLen = (int) this.getMilliSeparatorLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] lmbcs = sep == null ? new byte[0] : sep.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setMilliSeparatorLength(lmbcs.length);
    final int newLen = preLen + lmbcs.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(lmbcs);
    buf.put(postData);

    return this;
  }

  @StructureSetter("MilliSepSymLength")
  CDExt2Field setMilliSeparatorLength(long len);

  @StructureSetter("DTMonthFmt")
  CDExt2Field setMonthFormat(MonthFormat format);

  /**
   * Sets the month format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTMonthFmt")
  CDExt2Field setMonthFormatRaw(byte format);

  default CDExt2Field setNegativeSymbol(final String symbol) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength());
    final int currentLen = (int) this.getNegativeSymbolLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] lmbcs = symbol == null ? new byte[0] : symbol.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setNegativeSymbolLength(lmbcs.length);
    final int newLen = preLen + lmbcs.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(lmbcs);
    buf.put(postData);

    return this;
  }

  @StructureSetter("NegativeSymLength")
  CDExt2Field setNegativeSymbolLength(long len);

  @StructureSetter("NumSymPref")
  CDExt2Field setNumberSymbolPreference(NumberPref pref);

  /**
   * Sets the number-symbol pref as a raw {@code byte}.
   * 
   * @param pref the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("NumSymPref")
  CDExt2Field setNumberSymbolPreferenceRaw(byte pref);

  @StructureSetter("wCharacters")
  CDExt2Field setProportionalWidthCharacters(int characters);

  default CDExt2Field setThumbnailImageFileName(final String fileName) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength());
    final int currentLen = this.getThumbnailImageFileNameLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] lmbcs = fileName == null ? new byte[0] : fileName.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setThumbnailImageFileNameLength(lmbcs.length);
    final int newLen = preLen + lmbcs.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(lmbcs);
    buf.put(postData);

    return this;
  }

  @StructureSetter("wThumbnailImageFileNameLength")
  CDExt2Field setThumbnailImageFileNameLength(int len);

  @StructureSetter("ThumbnailImageHeight")
  CDExt2Field setThumbnailImageHeight(int height);

  @StructureSetter("ThumbnailImageWidth")
  CDExt2Field setThumbnailImageWidth(int width);

  default CDExt2Field setTimeSeparator(final String sep) {
    final int preLen = (int) (this.getDecimalSymbolLength() + this.getMilliSeparatorLength() + this.getNegativeSymbolLength()
        + this.getCurrencySymbolLength() + this.getThumbnailImageFileNameLength()
        + this.getIMOnlineNameFormulaLength() + this.getDateSeparator1Length() + this.getDateSeparator2Length()
        + this.getDateSeparator3Length());
    final int currentLen = this.getTimeSeparatorLength();
    ByteBuffer buf = this.getVariableData();
    final int postLen = buf.remaining() - currentLen - preLen;

    buf.position(preLen + currentLen);
    final byte[] postData = new byte[postLen];
    buf.get(postData);

    final byte[] compiled = sep == null ? new byte[0] : sep.getBytes(NativeItemCoder.get().getLmbcsCharset());
    this.setTimeSeparatorLength((short) compiled.length);
    final int newLen = preLen + compiled.length + postLen;
    this.resizeVariableData(newLen);
    buf = this.getVariableData();
    buf.position(preLen);
    buf.put(compiled);
    buf.put(postData);

    return this;
  }

  @StructureSetter("DTTsepLen")
  CDExt2Field setTimeSeparatorLength(short len);

  @StructureSetter("DTTShow")
  CDExt2Field setTimeShowFormat(TimeShowFormat format);

  /**
   * Sets the time-show format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTTShow")
  CDExt2Field setTimeShowFormatRaw(byte format);

  @StructureSetter("DTTZone")
  CDExt2Field setTimeZoneFormat(TimeZoneFormat format);

  /**
   * Sets the time-zone format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTTZone")
  CDExt2Field setTimeZoneFormatRaw(byte format);

  @StructureSetter("VerticalSpacing")
  CDExt2Field setVerticalSpacing(short spacing);

  @StructureSetter("DTYearFmt")
  CDExt2Field setYearFormat(YearFormat format);

  /**
   * Sets the year format as a raw {@code byte}.
   * 
   * @param format the value to set
   * @return this structure
   * @since 1.24.0
   */
  @StructureSetter("DTYearFmt")
  CDExt2Field setYearFormatRaw(byte format);
}
