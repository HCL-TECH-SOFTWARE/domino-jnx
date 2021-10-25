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
package com.hcl.domino.commons.json;

import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.DominoDateRange;
import com.hcl.domino.data.DominoDateTime;

/**
 * Utility methods for dealing with converting to and from
 * JSON.
 *
 * @author Jesse Gallagher
 * @since 1.0.9
 */
public enum JsonUtil {
  ;

  /**
   * Attempts to convert the provided JSON string value to a suitable Domino
   * value.
   * 
   * @param client         the context client to use for date/time conversions
   * @param detectDateTime whether conversion should attempt to auto-detect
   *                       date/time values
   * @param dateTimeItems  a collection of item names to attempt to convert to
   *                       date/time values
   * @param itemName       the name of the item being converted
   * @param val            the JSON string value
   * @return a Domino-friendly version of the provided value
   * @throws IllegalArgumentException if {@code itemName} is in
   *                                  {@code dateTimeItems} but the value cannot
   *                                  be converted to a date/time value
   */
  public static Object convertStringValue(final DominoClient client, final boolean detectDateTime,
      final Collection<String> dateTimeItems, final String itemName, final String val) {
    final Collection<String> dtItems = JsonUtil.toInsensitiveSet(dateTimeItems);
    if (detectDateTime) {
      final TemporalAccessor dt = JsonUtil.tryDateTime(val);
      if (dt != null) {
        return dt;
      } else {
        return val;
      }
    } else if (dtItems.contains(itemName)) {
      if (val == null || val.isEmpty()) {
        return null;
      } else {
        final TemporalAccessor dt = JsonUtil.tryDateTime(val);
        if (dt != null) {
          return dt;
        } else {
          final TemporalAccessor[] range = JsonUtil.tryDateRange(val);
          if (range != null) {
            return client.createDateRange(range[0], range[1]);
          } else {
            throw new IllegalArgumentException(MessageFormat.format("Encountered unparseable value in date/time item: {0}", val));
          }
        }
      }
    } else {
      return val;
    }
  }

  /**
   * Creates a case-insensitive {@link Set} from the provided {@link Collection}
   * of strings, removing {@code null} values.
   * 
   * @param value the collection to convert
   * @return a case-insenstive set of the provided values
   */
  public static Set<String> toInsensitiveSet(final Collection<String> value) {
    if (value == null || value.isEmpty()) {
      return Collections.emptySet();
    } else {
      final Set<String> holder = new HashSet<>(value);
      holder.remove(null);
      final Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      result.addAll(holder);
      return result;
    }
  }

  /**
   * Converts the provided date range to an ISO string.
   * <p>
   * This is effectively equivalent to converting the upper and lower portions
   * with {@link #toIsoString(DominoDateTime)} and concatenating them with a
   * {@code '/'}.
   * 
   * @param range the range to convert
   * @return the range converted to an ISO string
   */
  public static String toIsoString(final DominoDateRange range) {
    final String start = JsonUtil.toIsoString(range.getStartDateTime());
    final String end = JsonUtil.toIsoString(range.getEndDateTime());
    return start + '/' + end;
  }

  /**
   * Converts the provided {@link DominoDateTime} instance to an ISO 8601 string.
   * 
   * @param dt the date/time to convert
   * @return an ISO string representation
   */
  public static String toIsoString(final DominoDateTime dt) {
    if (dt.hasDate() && dt.hasTime()) {
      return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt);
    } else if (dt.hasDate()) {
      return DateTimeFormatter.ISO_LOCAL_DATE.format(dt);
    } else if (dt.hasTime()) {
      return DateTimeFormatter.ISO_LOCAL_TIME.format(dt);
    } else {
      return ""; //$NON-NLS-1$
    }
  }

  /**
   * Attempts to convert the provided string value to a date/time range using ISO
   * formatting.
   * 
   * @param value the string value to convert
   * @return a 2-element {@link TemporalAccessor} array containing the start and
   *         end of the
   *         range, or {@code null} if {@code value} does not contain an ISO time
   *         range
   */
  public static TemporalAccessor[] tryDateRange(final String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    final int slashIndex = value.indexOf('/');
    if (slashIndex < 1) {
      return null;
    }
    final String start = value.substring(0, slashIndex);
    final String end = value.substring(slashIndex + 1);
    final TemporalAccessor startTime = JsonUtil.tryDateTime(start);
    final TemporalAccessor endTime = JsonUtil.tryDateTime(end);
    if (startTime == null || endTime == null) {
      return null;
    }
    if (startTime.getClass().equals(endTime.getClass())) {
      // It's a good range
      return new TemporalAccessor[] { startTime, endTime };
    } else {
      return null;
    }
  }

  /**
   * Attempts to convert the provided string value to a date/time using ISO
   * formatting.
   * 
   * @param value the string value to convert
   * @return a {@link TemporalAccessor} implementation for the converted value, or
   *         {@code null} if {@code value} does not contain an ISO time range
   */
  public static TemporalAccessor tryDateTime(final String value) {
    try {
      return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(value));
    } catch (final DateTimeParseException e) {
    }
    try {
      return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(value));
    } catch (final DateTimeParseException e) {
    }
    try {
      return OffsetDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value));
    } catch (final DateTimeParseException e) {
    }
    try {
      return ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(value));
    } catch (final DateTimeParseException e) {
    }

    return null;
  }
  
  public static String toBase64String(ByteBuffer val)  {
    byte[] byteArr = new byte[val.remaining()];
    val.get(byteArr);
    return Base64.getEncoder().encodeToString(byteArr);
  }
}
