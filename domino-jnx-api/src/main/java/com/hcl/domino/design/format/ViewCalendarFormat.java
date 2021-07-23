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

import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.ViewFormatConstants;
import com.hcl.domino.richtext.annotation.StructureDefinition;
import com.hcl.domino.richtext.annotation.StructureGetter;
import com.hcl.domino.richtext.annotation.StructureMember;
import com.hcl.domino.richtext.annotation.StructureSetter;
import com.hcl.domino.richtext.structures.ColorValue;
import com.hcl.domino.richtext.structures.FontStyle;
import com.hcl.domino.richtext.structures.MemoryStructure;

@StructureDefinition(name = "VIEW_CALENDAR_FORMAT", members = {
    @StructureMember(name = "Version", type = ViewCalendarFormat.Version.class),
    @StructureMember(name = "Formats", type = ViewCalendarFormat.Format.class, bitfield = true),
    @StructureMember(name = "DayDateFont", type = FontStyle.class),
    @StructureMember(name = "TimeSlotFont", type = FontStyle.class),
    @StructureMember(name = "HeaderFont", type = FontStyle.class),
    @StructureMember(name = "DaySeparatorsColor", type = short.class),
    @StructureMember(name = "TodayColor", type = short.class),
    @StructureMember(name = "wFlags", type = ViewCalendarFormat.Flag.class, bitfield = true),
    @StructureMember(name = "BusyColor", type = short.class),
    @StructureMember(name = "wTimeSlotStart", type = short.class, unsigned = true),
    @StructureMember(name = "wTimeSlotEnd", type = short.class, unsigned = true),
    @StructureMember(name = "wTimeSlotDuration", type = short.class, unsigned = true),
    @StructureMember(name = "DaySeparatorsColorExt", type = ColorValue.class),
    @StructureMember(name = "BusyColorExt", type = ColorValue.class),
    @StructureMember(name = "MinorVersion", type = ViewCalendarFormat.MinorVersion.class),
    @StructureMember(name = "InitialFormat", type = byte.class),
    @StructureMember(name = "CalGridBkColor", type = int.class),
    @StructureMember(name = "WorkHoursColor", type = int.class),
    @StructureMember(name = "ToDoBkColor", type = int.class),
    @StructureMember(name = "HeaderBkColor", type = int.class)
})
public interface ViewCalendarFormat extends MemoryStructure {
  enum Flag implements INumberEnum<Short> {
    /** Display Conflict marks */
    DISPLAY_CONFLICTS(ViewFormatConstants.CAL_DISPLAY_CONFLICTS),
    /** Disable Time Slots */
    ENABLE_TIMESLOTS(ViewFormatConstants.CAL_ENABLE_TIMESLOTS),
    /** Show Time Slot Bitmaps */
    DISPLAY_TIMESLOT_BMPS(ViewFormatConstants.CAL_DISPLAY_TIMESLOT_BMPS),
    /** Enable Timegrouping */
    ENABLE_TIMEGROUPING(ViewFormatConstants.CAL_ENABLE_TIMEGROUPING),
    /** Allow user to override time slots */
    TIMESLOT_OVERRIDE(ViewFormatConstants.CAL_TIMESLOT_OVERRIDE),
    /** Don't show the month header in the view (i.e. January 2001) */
    HIDE_MONTH_HEADER(ViewFormatConstants.CAL_HIDE_MONTH_HEADER),
    /** Don't show the GoToToday button in the view */
    HIDE_GOTOTODAY(ViewFormatConstants.CAL_HIDE_GOTOTODAY),
    /** Don't show the trash view in the header */
    SHOW_TRASHVIEW(ViewFormatConstants.CAL_SHOW_TRASHVIEW),
    /** Don't show the all docs view in the header */
    SHOW_ALLDOCSVIEW(ViewFormatConstants.CAL_SHOW_ALLDOCSVIEW),
    /** Don't show the formatting button */
    HIDE_FORMATBTN(ViewFormatConstants.CAL_HIDE_FORMATBTN),
    /** Don't show the day tab */
    HIDE_DAYTAB(ViewFormatConstants.CAL_HIDE_DAYTAB),
    /** Don't show the week tab */
    HIDE_WEEKTAB(ViewFormatConstants.CAL_HIDE_WEEKTAB),
    /** Don't show the month tab */
    HIDE_MONTHTAB(ViewFormatConstants.CAL_HIDE_MONTHTAB),
    /** show the header as dayplanner */
    SHOW_DAYPLANNER(ViewFormatConstants.CAL_SHOW_DAYPLANNER),
    /** show the owner name */
    HIDE_OWNERNAME(ViewFormatConstants.CAL_HIDE_OWNERNAME),
    RTLVIEW(ViewFormatConstants.VIEW_CALENDAR_RTLVIEW);

    private final short value;

    Flag(final short value) {
      this.value = value;
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

  enum Format implements INumberEnum<Byte> {
    TWO_DAY(ViewFormatConstants.VIEW_CAL_FORMAT_TWO_DAY),
    ONE_WEEK(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_WEEK),
    TWO_WEEKS(ViewFormatConstants.VIEW_CAL_FORMAT_TWO_WEEKS),
    ONE_MONTH(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_MONTH),
    ONE_YEAR(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_YEAR),
    ONE_DAY(ViewFormatConstants.VIEW_CAL_FORMAT_ONE_DAY),
    WORK_WEEK(ViewFormatConstants.VIEW_CAL_FORMAT_WORK_WEEK);

    private final byte value;

    Format(final byte value) {
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

  enum MinorVersion implements INumberEnum<Byte> {
    /** V4.5, V4.6 has minor version of 0 */
    MINOR_V4x(ViewFormatConstants.VIEW_CAL_FORMAT_MINOR_V4x),
    /** V5 */
    MINOR_1(ViewFormatConstants.VIEW_CAL_FORMAT_MINOR_1),
    /** V5.03 and up - added custom work week format */
    MINOR_2(ViewFormatConstants.VIEW_CAL_FORMAT_MINOR_2),
    /** Calendar Grid Color */
    MINOR_3(ViewFormatConstants.VIEW_CAL_FORMAT_MINOR_3),
    /** more damn colors */
    MINOR_4(ViewFormatConstants.VIEW_CAL_FORMAT_MINOR_4);

    private final byte value;

    MinorVersion(final byte value) {
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

  enum Version implements INumberEnum<Byte> {
    VERSION_1(ViewFormatConstants.VIEW_CALENDAR_FORMAT_VERSION);

    private final byte value;

    Version(final byte value) {
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

  @StructureGetter("BusyColor")
  short getBusyColor();

  @StructureGetter("BusyColorExt")
  ColorValue getBusyColorExt();

  @StructureGetter("DayDateFont")
  FontStyle getDayDateFont();

  @StructureGetter("DaySeparatorsColor")
  short getDaySeparatorColor();

  @StructureGetter("DaySeparatorsColorExt")
  ColorValue getDaySeparatorColorExt();

  @StructureGetter("wFlags")
  Set<Flag> getFlags();

  @StructureGetter("CalGridBkColor")
  int getGridBackgroundColor();

  @StructureGetter("HeaderBkColor")
  int getHeaderBackgroundColor();

  @StructureGetter("HeaderFont")
  FontStyle getHeaderFont();

  @StructureGetter("InitialFormat")
  byte getInitialFormat();

  @StructureGetter("MinorVersion")
  MinorVersion getMinorVersion();

  @StructureGetter("Formats")
  Set<Format> getSupportedFormats();

  @StructureGetter("wTimeSlotDuration")
  int getTimeSlotDurationMinutes();

  @StructureGetter("wTimeSlotEnd")
  int getTimeSlotEndMinutes();

  @StructureGetter("TimeSlotFont")
  FontStyle getTimeSlotFont();

  @StructureGetter("wTimeSlotStart")
  int getTimeSlotStartMinutes();

  @StructureGetter("TodayColor")
  short getTodayColor();

  @StructureGetter("ToDoBkColor")
  int getToDoBackgroundColor();

  @StructureGetter("Version")
  Version getVersion();

  @StructureGetter("WorkHoursColor")
  int getWorkHoursColor();

  @StructureSetter("BusyColor")
  ViewCalendarFormat setBusyColor(short color);

  @StructureSetter("DaySeparatorsColor")
  ViewCalendarFormat setDaySeparatorColor(short color);

  @StructureSetter("wFlags")
  ViewCalendarFormat setFlags(Collection<Flag> formats);

  @StructureSetter("CalGridBkColor")
  ViewCalendarFormat setGridBackgroundColor(int color);

  @StructureSetter("HeaderBkColor")
  ViewCalendarFormat setHeaderBackgroundColor(int color);

  @StructureSetter("InitialFormat")
  ViewCalendarFormat setInitialFormat(byte format);

  @StructureSetter("MinorVersion")
  ViewCalendarFormat setMinorVersion(MinorVersion version);

  @StructureSetter("Formats")
  ViewCalendarFormat setSupportedFormats(Collection<Format> formats);

  @StructureSetter("wTimeSlotDuration")
  ViewCalendarFormat setTimeSlotDurationMinutes(int mins);

  @StructureSetter("wTimeSlotEnd")
  ViewCalendarFormat setTimeSlotEndMinutes(int mins);

  @StructureSetter("wTimeSlotStart")
  ViewCalendarFormat setTimeSlotStartMinutes(int mins);

  @StructureSetter("TodayColor")
  ViewCalendarFormat setTodayColor(short color);

  @StructureSetter("ToDoBkColor")
  ViewCalendarFormat setToDoBackgroundColor(int color);

  @StructureSetter("Version")
  ViewCalendarFormat setVersion(Version version);

  @StructureSetter("WorkHoursColor")
  ViewCalendarFormat setWorkHoursColor(int color);
}
