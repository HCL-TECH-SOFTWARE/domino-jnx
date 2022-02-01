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
package com.hcl.domino.design.format;

import java.util.EnumSet;

import com.hcl.domino.data.StandardFonts;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;
import com.hcl.domino.richtext.structures.MemoryStructureWrapperService;

/**
 * @since 1.0.27
 */
@StructureDefinition(name = "VIEW_CALENDAR_FORMAT2", members = {
    @StructureMember(name = "Signature", type = short.class),
    @StructureMember(name = "DayDateBkColor", type = ColorValue.class),
    @StructureMember(name = "NonMonthBkColor", type = ColorValue.class),
    @StructureMember(name = "NonMonthTextColor", type = ColorValue.class),
    @StructureMember(name = "DayDateColor", type = ColorValue.class),
    @StructureMember(name = "TimeSlotColor", type = ColorValue.class),
    @StructureMember(name = "HeaderColor", type = ColorValue.class),
    @StructureMember(name = "TodayRGBColor", type = ColorValue.class),
    @StructureMember(name = "WeekDayMonthFont", type = FontStyle.class),
    @StructureMember(name = "Spare", type = int[].class, length = 3)
})
public interface ViewCalendarFormat2 extends MemoryStructure {
  public static ViewCalendarFormat2 newInstanceWithDefaults() {
    ViewCalendarFormat2 format2 = MemoryStructureWrapperService.get().newStructure(ViewCalendarFormat2.class, 0);

    format2.getTodayColor().setRed((short)255).setGreen((short)0).setBlue((short)0).setFlags(EnumSet.of(ColorValue.Flag.ISRGB));
    format2.setSignature(ViewFormatConstants.VIEW_CALENDAR_FORMAT2_SIGNATURE);
    format2.getDayDateColor().copyFrom(DesignColorsAndFonts.blackColor());
    format2.getNonMonthBackgroundColor().copyFrom(DesignColorsAndFonts.whiteColor());
    format2.getWeekDayMonthFont().setStandardFont(StandardFonts.SWISS).setPointSize(9).setFontFace((byte)1);
    format2.getTimeSlotColor().copyFrom(DesignColorsAndFonts.blackColor());
    format2.getDayDateBackgroundColor().copyFrom(DesignColorsAndFonts.whiteColor());
    format2.getNonMonthTextColor().copyFrom(DesignColorsAndFonts.blackColor());
    format2.getHeaderColor().copyFrom(DesignColorsAndFonts.blackColor());
    
    return format2;
  }

  @StructureGetter("DayDateBkColor")
  ColorValue getDayDateBackgroundColor();

  @StructureGetter("DayDateColor")
  ColorValue getDayDateColor();

  @StructureGetter("HeaderColor")
  ColorValue getHeaderColor();

  @StructureGetter("NonMonthBkColor")
  ColorValue getNonMonthBackgroundColor();
  
  @StructureGetter("NonMonthTextColor")
  ColorValue getNonMonthTextColor();

  @StructureGetter("Signature")
  short getSignature();

  @StructureGetter("TimeSlotColor")
  ColorValue getTimeSlotColor();

  @StructureGetter("TodayRGBColor")
  ColorValue getTodayColor();

  @StructureGetter("WeekDayMonthFont")
  FontStyle getWeekDayMonthFont();

  @StructureSetter("Signature")
  ViewCalendarFormat2 setSignature(short signature);
}
