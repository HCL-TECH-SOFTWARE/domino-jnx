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
package com.hcl.domino.commons.util;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.NotesConstants;

/**
 * DateTime conversion utilities between Java and the Notes C API
 *
 * @author Karsten Lehmann
 */
public class NotesDateTimeUtils {

  @Deprecated
  public static int[] calendarToInnards(final Calendar cal) {
    final boolean hasDate = NotesDateTimeUtils.hasDate(cal);
    final boolean hasTime = NotesDateTimeUtils.hasTime(cal);
    int[] innards;
    if (hasDate && hasTime) {
      innards = InnardsConverter.encodeInnards(((GregorianCalendar) cal).toZonedDateTime());
    } else if (hasDate) {
      final LocalDate localDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
      innards = InnardsConverter.encodeInnards(localDate);
    } else {
      final LocalTime localTime = LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
          cal.get(Calendar.MILLISECOND) * 1000 * 1000);
      innards = InnardsConverter.encodeInnards(localTime);
    }
    return innards;
  }

  /**
   * Compares two date/time values and returns -1, if the first value is before
   * the second, 1 if the first value is after the second and 0 if both values are
   * equal.
   * 
   * @param innards1 first date/time
   * @param innards2 second date/time
   * @return compare result
   */
  public static int compareInnards(final int[] innards1, final int[] innards2) {
    if (!NotesDateTimeUtils.hasDate(innards1)) {
      throw new IllegalArgumentException(
          MessageFormat.format("Innard array #1 does not have a date part: {0}", Arrays.toString(innards1)));
    }
    if (!NotesDateTimeUtils.hasDate(innards2)) {
      throw new IllegalArgumentException(
          MessageFormat.format("Innard array #1 does not have a date part: {0}", Arrays.toString(innards2)));
    }
    if (!NotesDateTimeUtils.hasTime(innards1)) {
      throw new IllegalArgumentException(
          MessageFormat.format("Innard array #1 does not have a time part: {0}", Arrays.toString(innards1)));
    }
    if (!NotesDateTimeUtils.hasTime(innards2)) {
      throw new IllegalArgumentException(
          MessageFormat.format("Innard array #1 does not have a time part: {0}", Arrays.toString(innards2)));
    }

    // compare date part
    if (innards1[1] > innards2[1]) {
      return 1;
    } else if (innards1[1] < innards2[1]) {
      return -1;
    } else {
      // compare time part
      if (innards1[0] > innards2[0]) {
        return 1;
      } else if (innards1[0] < innards2[0]) {
        return -1;
      } else {
        return 0;
      }
    }
  }

  @Deprecated
  public static Calendar createCalendar(final int year, final int month, final int day, final int hour, final int minute,
      final int second, final int millis, final TimeZone zone) {
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month - 1);
    cal.set(Calendar.DAY_OF_MONTH, day);
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, minute);
    cal.set(Calendar.SECOND, second);
    cal.set(Calendar.MILLISECOND, millis);
    cal.set(Calendar.ZONE_OFFSET, zone.getRawOffset());
    return cal;
  }

  /**
   * Returns the current timezone's GMT offset
   * 
   * @return offset
   */
  public static int getGMTOffset() {
    final TimeZone tz = TimeZone.getDefault();

    return tz.getRawOffset() / 3600000;
  }

  /**
   * Method to check whether year, month and date fields are set
   * 
   * @param cal calendar to check
   * @return true if we have a date
   */
  public static boolean hasDate(final Calendar cal) {
    final boolean hasDate = cal.isSet(Calendar.YEAR) && cal.isSet(Calendar.MONTH) && cal.isSet(Calendar.DATE);
    return hasDate;
  }

  /**
   * Method to check whether a date/time represented as an innard array has
   * a date part
   * 
   * @param innards innards
   * @return true if it has date
   */
  public static boolean hasDate(final int[] innards) {
    if (innards.length != 2) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid innard size: {0}, expected 2", innards.length));
    }
    return innards[1] != NotesConstants.ANYDAY;
  }

  /**
   * Method to check whether hour, minute, second and millisecond fields are set
   * 
   * @param cal calendar to check
   * @return true if we have a time
   */
  public static boolean hasTime(final Calendar cal) {
    final boolean hasTime = cal.isSet(Calendar.HOUR_OF_DAY) && cal.isSet(Calendar.MINUTE) &&
        cal.isSet(Calendar.SECOND) && cal.isSet(Calendar.MILLISECOND);
    return hasTime;
  }

  /**
   * Method to check whether a date/time represented as an innard array has
   * a time part
   * 
   * @param innards innards
   * @return true if it has time
   */
  public static boolean hasTime(final int[] innards) {
    if (innards.length != 2) {
      throw new IllegalArgumentException(MessageFormat.format("Invalid innard size: {0}, expected 2", innards.length));
    }
    return innards[0] != NotesConstants.ALLDAY;
  }

  /**
   * Converts C API innard values to Java {@link Calendar}
   * 
   * @param innards array with 2 innard values
   * @return calendar or null if invalid innards
   */
  @Deprecated
  public static Calendar innardsToCalendar(final int[] innards) {
    return GregorianCalendar.from(((OffsetDateTime) InnardsConverter.decodeInnards(innards)).toZonedDateTime());
  }

  /**
   * Method to compare two date/time values and check whether the first is after
   * the second
   * 
   * @param innards1 first date/time
   * @param innards2 second date/time
   * @return true if after
   */
  public static boolean isAfter(final int[] innards1, final int[] innards2) {
    return NotesDateTimeUtils.compareInnards(innards1, innards2) > 0;
  }

  /**
   * Method to compare two date/time values and check whether the first is before
   * the second
   * 
   * @param innards1 first date/time
   * @param innards2 second date/time
   * @return true if before
   */
  public static boolean isBefore(final int[] innards1, final int[] innards2) {
    return NotesDateTimeUtils.compareInnards(innards1, innards2) < 0;
  }

  /**
   * Returns whether the current timezone is in daylight savings time
   * 
   * @return true if DST
   */
  public static boolean isDaylightTime() {
    final TimeZone tz = TimeZone.getDefault();

    return tz.useDaylightTime();
  }

  /**
   * Method to compare two date/time values and check whether both are equal
   * 
   * @param innards1 first date/time
   * @param innards2 second date/time
   * @return true if equal
   */
  public static boolean isEqual(final int[] innards1, final int[] innards2) {
    return NotesDateTimeUtils.compareInnards(innards1, innards2) == 0;
  }

  /**
   * Clears the year, month and date fields of a {@link Calendar} object
   * 
   * @param cal calendar
   */
  public static void setAnyDate(final Calendar cal) {
    // clear date fields
    // clear year
    cal.clear(Calendar.YEAR);

    // clear month
    cal.clear(Calendar.MONTH);

    // clear day
    cal.clear(Calendar.DATE);
  }

  /**
   * Clears the hour, minute, second and millisecond fields of a {@link Calendar}
   * object
   * 
   * @param cal calendar
   */
  public static void setAnyTime(final Calendar cal) {
    // set date only
    // clear time fields
    // clear hour of the day
    cal.clear(Calendar.HOUR_OF_DAY);

    // clear minute
    cal.clear(Calendar.MINUTE);

    // clear second
    cal.clear(Calendar.SECOND);

    // clear millisecond
    cal.clear(Calendar.MILLISECOND);
  }

  /**
   * Method to convert a {@link DominoDateTime} object to a Java {@link Calendar}
   * 
   * @param timeDate time date to convert
   * @return calendar or null if timedate contains invalid innards
   */
  public static Calendar timeDateToCalendar(final DominoDateTime timeDate) {
    return GregorianCalendar.from(timeDate.toOffsetDateTime().toZonedDateTime());
  }

}
