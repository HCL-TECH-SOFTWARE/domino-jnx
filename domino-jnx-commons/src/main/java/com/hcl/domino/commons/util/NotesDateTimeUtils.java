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
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
   * Method to convert a {@link DominoDateTime} object to a Java {@link Calendar}
   * 
   * @param timeDate time date to convert
   * @return calendar or null if timedate contains invalid innards
   */
  public static Calendar timeDateToCalendar(final DominoDateTime timeDate) {
    return GregorianCalendar.from(timeDate.toOffsetDateTime().toZonedDateTime());
  }

}
