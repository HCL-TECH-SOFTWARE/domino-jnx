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
package com.hcl.domino.commons.design.view;

import java.time.Duration;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardColors;
import com.hcl.domino.design.CollectionDesignElement;
import com.hcl.domino.design.CollectionDesignElement.CalendarHeaderStyle;
import com.hcl.domino.design.CollectionDesignElement.CalendarSettings;
import com.hcl.domino.design.CollectionDesignElement.CalendarTab;
import com.hcl.domino.design.DesignColorsAndFonts;
import com.hcl.domino.design.format.CalendarLayout;
import com.hcl.domino.design.format.ViewCalendarFormat;
import com.hcl.domino.design.format.ViewCalendarFormat2;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.RawColorValue;

public class DefaultCalendarSettings implements CalendarSettings {
  private final CollectionDesignElement collection;
  private final DominoCalendarFormat format;

  public DefaultCalendarSettings(CollectionDesignElement collection, DominoCalendarFormat format) {
    this.collection = collection;
    this.format = format;
  }
  
  @Override
  public ColorValue getDaySeparatorColor() {
    return getFormat1().getDaySeparatorColorExt();
  }
  
  @Override
  public RawColorValue getHeaderBackgroundColor() {
    return getFormat1().getHeaderBackgroundColor();
  }

  @Override
  public Set<CalendarTab> getTabs() {
    EnumSet<CalendarTab> result = EnumSet.noneOf(CalendarTab.class);
    Set<ViewCalendarFormat.Flag> flags = getFormat1().getFlags();
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_DAYTAB)) {
      result.add(CalendarTab.DAY);
    }
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_WEEKTAB)) {
      result.add(CalendarTab.WEEK);
    }
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_MONTHTAB)) {
      result.add(CalendarTab.MONTH);
    }
    if(flags.contains(ViewCalendarFormat.Flag.SHOW_ALLDOCSVIEW)) {
      result.add(CalendarTab.MEETINGS);
    }
    if(flags.contains(ViewCalendarFormat.Flag.SHOW_TRASHVIEW)) {
      result.add(CalendarTab.TRASH);
    }
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_MONTH_HEADER)) {
      result.add(CalendarTab.CURRENT_MONTH);
    }
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_GOTOTODAY)) {
      result.add(CalendarTab.GOTO_TODAY);
    }
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_FORMATBTN)) {
      result.add(CalendarTab.FORMAT_OPTIONS);
    }
    if(!flags.contains(ViewCalendarFormat.Flag.HIDE_OWNERNAME)) {
      result.add(CalendarTab.OWNER_NAME);
    }
    
    return result;
  }
  
  @Override
  public CalendarHeaderStyle getHeaderStyle() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ColorValue getDateBackgroundColor() {
    return getFormat2()
      .map(ViewCalendarFormat2::getDayDateBackgroundColor)
      .orElseGet(DesignColorsAndFonts::whiteColor);
  }

  @Override
  public StandardColors getTodayColor() {
    return getFormat1().getTodayColor().get();
  }

  @Override
  public RawColorValue getToDoAreaColor() {
    return getFormat1().getToDoBackgroundColor();
  }

  @Override
  public boolean isDisplayLargeNumbers() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.SHOW_DAYPLANNER);
  }

  @Override
  public RawColorValue getDailyWorkHoursColor() {
    return getFormat1().getWorkHoursColor();
  }

  @Override
  public RawColorValue getDailyOtherHoursColor() {
    return getFormat1().getGridBackgroundColor();
  }

  @Override
  public ColorValue getNonCurrentMonthColor() {
    return getFormat2()
      .map(ViewCalendarFormat2::getNonMonthBackgroundColor)
      .orElseGet(DesignColorsAndFonts::whiteColor);
  }

  @Override
  public ColorValue getMonthlyTextColor() {
    return getFormat2()
      .map(ViewCalendarFormat2::getNonMonthTextColor)
      .orElseGet(DesignColorsAndFonts::blackColor);
  }

  @Override
  public ColorValue getEntryBackgroundColor() {
    return getFormat1().getBusyColorExt();
  }

  @Override
  public boolean isShowConflictMarks() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.DISPLAY_CONFLICTS);
  }

  @Override
  public NotesFont getTimeSlotsFont() {
    return new TextFontItemNotesFont(collection.getDocument(), getFormat1().getTimeSlotFont());
  }

  @Override
  public NotesFont getHeaderFont() {
    return new TextFontItemNotesFont(collection.getDocument(), getFormat1().getHeaderFont());
  }

  @Override
  public NotesFont getDayAndDateFont() {
    return new TextFontItemNotesFont(collection.getDocument(), getFormat1().getDayDateFont());
  }

  @Override
  public NotesFont getWeeklyDayAndMonthFont() {
    return getFormat2()
      .map(format -> new TextFontItemNotesFont(collection.getDocument(), format.getWeekDayMonthFont()))
      .orElseGet(() -> new TextFontItemNotesFont(collection.getDocument(), DesignColorsAndFonts.defaultFont()));
  }

  @Override
  public Set<CalendarLayout> getUserCalendarFormats() {
    return getFormat1().getSupportedFormats();
  }

  @Override
  public Optional<CalendarLayout> getInitialUserCalendarFormat() {
    return getFormat1().getInitialFormat();
  }

  @Override
  public boolean isTimeSlotDisplayAvailable() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.ENABLE_TIMESLOTS);
  }

  @Override
  public LocalTime getTimeSlotStart() {
    int minutes = getFormat1().getTimeSlotStartMinutes();
    return LocalTime.ofSecondOfDay(TimeUnit.MINUTES.toSeconds(minutes));
  }

  @Override
  public LocalTime getTimeSlotEnd() {
    int minutes = getFormat1().getTimeSlotEndMinutes();
    return LocalTime.ofSecondOfDay(TimeUnit.MINUTES.toSeconds(minutes));
  }

  @Override
  public Duration getTimeSlotDuration() {
    int minutes = getFormat1().getTimeSlotDurationMinutes();
    return Duration.ofMinutes(minutes);
  }

  @Override
  public boolean isTimeSlotsOverridable() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.TIMESLOT_OVERRIDE);
  }

  @Override
  public boolean isAllowUserTimeSlotToggle() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.DISPLAY_TIMESLOT_BMPS);
  }

  @Override
  public boolean isGroupEntriesByTimeSlot() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.ENABLE_TIMEGROUPING);
  }

  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************
  
  private ViewCalendarFormat getFormat1() {
    return format.getAdapter(ViewCalendarFormat.class);
  }
  
  private Optional<ViewCalendarFormat2> getFormat2() {
    ViewCalendarFormat2 format2 = format.getAdapter(ViewCalendarFormat2.class);
    return Optional.ofNullable(format2);
  }
}
