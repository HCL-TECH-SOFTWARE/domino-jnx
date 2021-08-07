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
import java.util.Set;

import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.StructureSupport;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ResizableMemoryStructure;

/**
 * @author Jesse Gallagher
 * @since 1.0.24
 */
@StructureDefinition(
  name = "VIEW_COLUMN_FORMAT3",
  members = {
    @StructureMember(name = "Signature", type = short.class),
    @StructureMember(name = "DTPref", type = NumberPref.class),
    @StructureMember(name = "DTFlags", type = int.class),
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
    @StructureMember(name = "DatePreference", type = short.class),
    @StructureMember(name = "bUnused", type = byte.class),
    @StructureMember(name = "Unused", type = int.class)
  }
)
public interface ViewColumnFormat3 extends ResizableMemoryStructure {
  @StructureGetter("Signature")
  short getSignature();

  @StructureSetter("Signature")
  ViewColumnFormat3 setSignature(short signature);

  @StructureGetter("DTPref")
  NumberPref getDateTimePreference();

  @StructureSetter("DTPref")
  ViewColumnFormat3 setDateTimePreference(NumberPref pref);

  @StructureGetter("DTFlags")
  int getDateTimeFlagsRaw();

  @StructureSetter("DTFlags")
  ViewColumnFormat3 setDateTimeFlagsRaw(int flags);

  @StructureGetter("DTFlags2")
  Set<DateTimeFlag2> getDateTimeFlags2();

  @StructureSetter("DTFlags2")
  ViewColumnFormat3 setDateTimeFlags2(Collection<DateTimeFlag2> flags);

  @StructureGetter("DTDOWFmt")
  WeekFormat getDayOfWeekFormat();

  @StructureSetter("DTDOWFmt")
  ViewColumnFormat3 setDayOfWeekFormat(WeekFormat format);

  @StructureGetter("DTYearFmt")
  YearFormat getYearFormat();

  @StructureSetter("DTYearFmt")
  ViewColumnFormat3 setYearFormat(YearFormat format);

  @StructureGetter("DTMonthFmt")
  MonthFormat getMonthFormat();

  @StructureSetter("DTMonthFmt")
  ViewColumnFormat3 setMonthFormat(MonthFormat format);

  @StructureGetter("DTDayFmt")
  DayFormat getDayFormat();

  @StructureSetter("DTDayFmt")
  ViewColumnFormat3 setDayFormat(DayFormat format);
  
  @StructureGetter("DTDsep1Len")
  short getDateSeparator1Length();

  @StructureSetter("DTDsep1Len")
  ViewColumnFormat3 setDateSeparator1Length(short len);

  @StructureGetter("DTDsep2Len")
  short getDateSeparator2Length();

  @StructureSetter("DTDsep2Len")
  ViewColumnFormat3 setDateSeparator2Length(short len);

  @StructureGetter("DTDsep3Len")
  short getDateSeparator3Length();

  @StructureSetter("DTDsep3Len")
  ViewColumnFormat3 setDateSeparator3Length(short len);

  @StructureGetter("DTTsepLen")
  short getTimeSeparatorLength();

  @StructureSetter("DTTsepLen")
  ViewColumnFormat3 setTimeSeparatorLength(short len);

  @StructureGetter("DTDShow")
  DateShowFormat getDateShowFormat();

  @StructureSetter("DTDShow")
  ViewColumnFormat3 setDateShowFormat(DateShowFormat format);

  @StructureGetter("DTDSpecial")
  Set<DateShowSpecial> getDateShowSpecial();

  @StructureSetter("DTDSpecial")
  ViewColumnFormat3 setDateShowSpecial(Collection<DateShowSpecial> format);

  @StructureGetter("DTTShow")
  TimeShowFormat getTimeShowFormat();

  @StructureSetter("DTTShow")
  ViewColumnFormat3 setTimeShowFormat(TimeShowFormat format);

  @StructureGetter("DTTZone")
  TimeZoneFormat getTimeZoneFormat();

  @StructureSetter("DTTZone")
  ViewColumnFormat3 setTimeZoneFormat(TimeZoneFormat format);
  
  default Set<DateTimeFlag> getDateTimeFlags() {
    int bitfield = getDateTimeFlagsRaw();
    int unmasked = bitfield & ~RichTextConstants.DT_STYLE_MSK;
    return DominoEnumUtil.valuesOf(DateTimeFlag.class, unmasked);
  }
  default ViewColumnFormat3 setDateTimeFlags(Collection<DateTimeFlag> flags) {
    int bitfield = DominoEnumUtil.toBitField(DateTimeFlag.class, flags);
    bitfield |= (getDateTimeFlagsRaw() & RichTextConstants.DT_STYLE_MSK);
    setDateTimeFlagsRaw(bitfield);
    return this;
  }
  
  default DateComponentOrder getDateComponentOrder() {
    int bitfield = getDateTimeFlagsRaw();
    int val = (bitfield & RichTextConstants.DT_STYLE_MSK) >> 0x10;
    return DominoEnumUtil.valueOf(DateComponentOrder.class, val).orElse(DateComponentOrder.WMDY);
  }
  default ViewColumnFormat3 setDateComponentOrder(DateComponentOrder order) {
    int val = order == null ? 0 : order.getValue();
    int bitfield = getDateTimeFlagsRaw();
    bitfield |= val << 0x10;
    setDateTimeFlagsRaw(bitfield);
    return this;
  }

  default String getDateSeparator1() {
    return StructureSupport.extractStringValue(
        this,
        0,
        this.getDateSeparator1Length());
  }

  default String getDateSeparator2() {
    return StructureSupport.extractStringValue(
        this,
        this.getDateSeparator1Length(),
        this.getDateSeparator2Length());
  }

  default String getDateSeparator3() {
    return StructureSupport.extractStringValue(
        this,
        this.getDateSeparator1Length() + this.getDateSeparator2Length(),
        this.getDateSeparator3Length());
  }

  default String getTimeSeparator() {
    return StructureSupport.extractStringValue(
        this,
        this.getDateSeparator1Length() + this.getDateSeparator2Length() + this.getDateSeparator3Length(),
        this.getTimeSeparatorLength());
  }

  default ViewColumnFormat3 setDateSeparator1(final String sep) {
    return StructureSupport.writeStringValueShort(
        this,
        0,
        this.getDateSeparator1Length(),
        sep,
        this::setDateSeparator1Length);
  }

  default ViewColumnFormat3 setDateSeparator2(final String sep) {
    return StructureSupport.writeStringValueShort(
        this,
        this.getDateSeparator1Length(),
        this.getDateSeparator2Length(),
        sep,
        this::setDateSeparator2Length);
  }

  default ViewColumnFormat3 setDateSeparator3(final String sep) {
    return StructureSupport.writeStringValueShort(
        this,
        this.getDateSeparator1Length() + this.getDateSeparator2Length(),
        this.getDateSeparator3Length(),
        sep,
        this::setDateSeparator3Length);
  }

  default ViewColumnFormat3 setTimeSeparator(final String sep) {
    return StructureSupport.writeStringValueShort(
        this,
        this.getDateSeparator1Length() + this.getDateSeparator2Length() + this.getDateSeparator3Length(),
        this.getTimeSeparatorLength(),
        sep,
        this::setTimeSeparatorLength);
  }
}
