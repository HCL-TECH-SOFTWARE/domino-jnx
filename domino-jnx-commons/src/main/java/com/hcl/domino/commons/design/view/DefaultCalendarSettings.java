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
package com.hcl.domino.commons.design.view;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hcl.domino.commons.design.AbstractCollectionDesignElement;
import com.hcl.domino.data.NotesFont;
import com.hcl.domino.data.StandardColors;
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
  private final AbstractCollectionDesignElement<?> collection;
  private final DominoCalendarFormat format;

  public DefaultCalendarSettings(AbstractCollectionDesignElement<?> collection, DominoCalendarFormat format) {
    this.collection = collection;
    this.format = format;
  }
  
  private void markCalendarFormatDirty() {
    this.collection.setCalendarFormatDirty(true);
  }
  
  @Override
  public ColorValue getDaySeparatorColor() {
    return getFormat1().getDaySeparatorColorExt();
  }
  
  @Override
  public CalendarSettings setDaySeparatorColor(ColorValue color) {
    getFormat1().getDaySeparatorColorExt().copyFrom(color);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public RawColorValue getHeaderBackgroundColor() {
    return getFormat1().getHeaderBackgroundColor();
  }

  @Override
  public CalendarSettings setHeaderBackgroundColor(RawColorValue color) {
    getFormat1().getHeaderBackgroundColor().copyFrom(color);
    markCalendarFormatDirty();
    return this;
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
  public CalendarSettings setTabs(Collection<CalendarTab> tabs) {
    Set<ViewCalendarFormat.Flag> oldFlags = getFormat1().getFlags();
    Set<ViewCalendarFormat.Flag> newFlags = new HashSet<>(oldFlags);
    
    if (tabs.contains(CalendarTab.DAY)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_DAYTAB);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_DAYTAB);
    }
    
    if (tabs.contains(CalendarTab.WEEK)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_WEEKTAB);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_WEEKTAB);
    }
    
    if (tabs.contains(CalendarTab.MONTH)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_MONTHTAB);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_MONTHTAB);
    }
    
    if (tabs.contains(CalendarTab.MEETINGS)) {
      newFlags.add(ViewCalendarFormat.Flag.SHOW_ALLDOCSVIEW);
    }
    else {
      newFlags.remove(ViewCalendarFormat.Flag.SHOW_ALLDOCSVIEW);
    }
    
    if (tabs.contains(CalendarTab.TRASH)) {
      newFlags.add(ViewCalendarFormat.Flag.SHOW_TRASHVIEW);
    }
    else {
      newFlags.remove(ViewCalendarFormat.Flag.SHOW_TRASHVIEW);
    }
    
    if (tabs.contains(CalendarTab.CURRENT_MONTH)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_MONTH_HEADER);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_MONTH_HEADER);
    }
    
    if (tabs.contains(CalendarTab.GOTO_TODAY)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_GOTOTODAY);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_GOTOTODAY);
    }
    
    if (tabs.contains(CalendarTab.FORMAT_OPTIONS)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_FORMATBTN);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_FORMATBTN);
    }
    
    if (tabs.contains(CalendarTab.OWNER_NAME)) {
      newFlags.remove(ViewCalendarFormat.Flag.HIDE_OWNERNAME);
    }
    else {
      newFlags.add(ViewCalendarFormat.Flag.HIDE_OWNERNAME);
    }
    
    getFormat1().setFlags(newFlags);
    return this;
  }
  
  @Override
  public CalendarHeaderStyle getHeaderStyle() {
    // TODO tbd
    return null;
  }

  @Override
  public CalendarSettings setHeaderStyle(CalendarHeaderStyle style) {
    // TODO tbd
    return this;
  }
  
  @Override
  public ColorValue getDateBackgroundColor() {
    return getFormat2(false)
      .map(ViewCalendarFormat2::getDayDateBackgroundColor)
      .orElseGet(DesignColorsAndFonts::whiteColor);
  }

  @Override
  public CalendarSettings setDateBackgroundColor(ColorValue color) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.getDayDateBackgroundColor().copyFrom(color);
      markCalendarFormatDirty();
    });
    return this;
  }
  
  @Override
  public StandardColors getTodayColor() {
    return getFormat1().getTodayColor().get();
  }

  @Override
  public CalendarSettings setTodayColor(StandardColors color) {
    getFormat1().setTodayColorRaw(color.getValue());
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public RawColorValue getToDoAreaColor() {
    return getFormat1().getToDoBackgroundColor();
  }

  @Override
  public CalendarSettings setToDoAreaColor(RawColorValue color) {
    getFormat1().getToDoBackgroundColor().copyFrom(color);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public boolean isDisplayLargeNumbers() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.SHOW_DAYPLANNER);
  }

  @Override
  public CalendarSettings setDisplayLargeNumbers(boolean b) {
    getFormat1().setFlag(ViewCalendarFormat.Flag.SHOW_DAYPLANNER, b);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public RawColorValue getDailyWorkHoursColor() {
    return getFormat1().getWorkHoursColor();
  }

  @Override
  public CalendarSettings setDailyWorkHoursColor(RawColorValue color) {
    getFormat1().getWorkHoursColor().copyFrom(color);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public RawColorValue getDailyOtherHoursColor() {
    return getFormat1().getGridBackgroundColor();
  }

  @Override
  public CalendarSettings setDailyOthersHoursColors(RawColorValue color) {
    getFormat1().getGridBackgroundColor().copyFrom(color);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public ColorValue getNonCurrentMonthColor() {
    return getFormat2(false)
      .map(ViewCalendarFormat2::getNonMonthBackgroundColor)
      .orElseGet(DesignColorsAndFonts::whiteColor);
  }

  @Override
  public CalendarSettings setNonCurrentMonthColor(ColorValue color) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.getNonMonthBackgroundColor().copyFrom(color);
      markCalendarFormatDirty();
    });
    return this;
  }
  
  @Override
  public ColorValue getMonthlyTextColor() {
    return getFormat2(false)
      .map(ViewCalendarFormat2::getNonMonthTextColor)
      .orElseGet(DesignColorsAndFonts::blackColor);
  }

  @Override
  public CalendarSettings setMonthlyTextColor(ColorValue color) {
    getFormat2(true)
    .ifPresent((fmt2) -> {
      fmt2.getNonMonthTextColor().copyFrom(color);
      markCalendarFormatDirty();
    });
    return this;
  }
  
  @Override
  public ColorValue getEntryBackgroundColor() {
    return getFormat1().getBusyColorExt();
  }

  @Override
  public CalendarSettings setEntryBackgroundColor(ColorValue color) {
    getFormat1().getBusyColorExt().copyFrom(color);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public boolean isShowConflictMarks() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.DISPLAY_CONFLICTS);
  }

  @Override
  public CalendarSettings setShowConflictMarks(boolean b) {
    getFormat1().setFlag(ViewCalendarFormat.Flag.DISPLAY_CONFLICTS, b);
    markCalendarFormatDirty();
    return this;
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
    return getFormat2(false)
      .map(format -> new TextFontItemNotesFont(collection.getDocument(), format.getWeekDayMonthFont()))
      .orElseGet(() -> new TextFontItemNotesFont(collection.getDocument(), DesignColorsAndFonts.defaultFont()));
  }

  @Override
  public Set<CalendarLayout> getUserCalendarFormats() {
    return getFormat1().getSupportedFormats();
  }

  @Override
  public CalendarSettings setUserCalendarFormats(Collection<CalendarLayout> layouts) {
    getFormat1().setSupportedFormats(layouts);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public Optional<CalendarLayout> getInitialUserCalendarFormat() {
    return getFormat1().getInitialFormat();
  }

  @Override
  public CalendarSettings setInitialUserCalendarFormat(CalendarLayout layout) {
    getFormat1().setInitialFormat(layout);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public boolean isTimeSlotDisplayAvailable() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.ENABLE_TIMESLOTS);
  }

  @Override
  public CalendarSettings setTimeSlotDisplayAvailable(boolean b) {
    getFormat1().setFlag(ViewCalendarFormat.Flag.ENABLE_TIMESLOTS, b);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public LocalTime getTimeSlotStart() {
    int minutes = getFormat1().getTimeSlotStartMinutes();
    return LocalTime.ofSecondOfDay(TimeUnit.MINUTES.toSeconds(minutes));
  }

  @Override
  public CalendarSettings setTimeSlotStart(LocalTime time) {
    int seconds = time.toSecondOfDay();
    int minutes = seconds / 60;
    getFormat1().setTimeSlotStartMinutes(minutes);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public LocalTime getTimeSlotEnd() {
    int minutes = getFormat1().getTimeSlotEndMinutes();
    return LocalTime.ofSecondOfDay(TimeUnit.MINUTES.toSeconds(minutes));
  }

  @Override
  public CalendarSettings setTimeSlotEnd(LocalTime time) {
    int seconds = time.toSecondOfDay();
    int minutes = seconds / 60;
    getFormat1().setTimeSlotEndMinutes(minutes);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public Duration getTimeSlotDuration() {
    int minutes = getFormat1().getTimeSlotDurationMinutes();
    return Duration.ofMinutes(minutes);
  }

  @Override
  public CalendarSettings setTimeSlotDuration(Duration d) {
    long minutes = d.toMinutes();
    getFormat1().setTimeSlotDurationMinutes((int) minutes);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public boolean isTimeSlotsOverridable() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.TIMESLOT_OVERRIDE);
  }

  @Override
  public CalendarSettings setTimeSlotsOverridable(boolean b) {
    getFormat1().setFlag(ViewCalendarFormat.Flag.TIMESLOT_OVERRIDE, b);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public boolean isAllowUserTimeSlotToggle() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.DISPLAY_TIMESLOT_BMPS);
  }

  @Override
  public CalendarSettings setAllowUserTimeSlotToggle(boolean b) {
    getFormat1().setFlag(ViewCalendarFormat.Flag.DISPLAY_TIMESLOT_BMPS, b);
    markCalendarFormatDirty();
    return this;
  }
  
  @Override
  public boolean isGroupEntriesByTimeSlot() {
    return getFormat1().getFlags().contains(ViewCalendarFormat.Flag.ENABLE_TIMEGROUPING);
  }

  @Override
  public CalendarSettings setGroupEntriesByTimeSlot(boolean b) {
    getFormat1().setFlag(ViewCalendarFormat.Flag.ENABLE_TIMEGROUPING, b);
    markCalendarFormatDirty();
    return this;
  }
  
  // *******************************************************************************
  // * Internal implementation utilities
  // *******************************************************************************
  
  private ViewCalendarFormat getFormat1() {
    return format.getFormat1();
  }
  
  private Optional<ViewCalendarFormat2> getFormat2(boolean createIfMissing) {
    return format.getFormat2(createIfMissing);
  }
}
