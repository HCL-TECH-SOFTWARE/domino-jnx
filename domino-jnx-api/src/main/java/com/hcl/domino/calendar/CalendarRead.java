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
package com.hcl.domino.calendar;

import java.util.Collection;

import com.hcl.domino.data.Database;

/**
 * Flags that control behavior of the calendar APIs that return iCalendar data
 * for an entry or notice
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum CalendarRead {

  /**
   * Used when APIs generate iCalendar<br>
   * <br>
   * By default, some X-LOTUS properties and parameters will be included in
   * iCalendar data
   * returned by these APIs.<br>
   * {@link #HIDE_X_LOTUS} causes all X-LOTUS properties and parameters to be
   * removed from
   * the generated iCalendar data.<br>
   * <br>
   * Note: This overrides {@link #INCLUDE_X_LOTUS}
   */
  HIDE_X_LOTUS(0x00000001),

  /**
   * Used when APIs generate iCalendar<br>
   * <br>
   * Include all Lotus specific properties like X-LOTUS-UPDATE-SEQ,
   * X-LOTUS-UPDATE_WISL, etc
   * in the generated iCalendar data.<br>
   * These properties are NOT included by default in any iCalendar data returned
   * by the APIs.<br>
   * <br>
   * Caution: Unless the caller knows how to use these it can be dangerous since
   * their
   * presence will be honored and can cause issues if not updated properly.<br>
   * Ignored if {@link #HIDE_X_LOTUS} is also specified.
   */
  INCLUDE_X_LOTUS(0x00000002),

  /**
   * RESERVED: This functionality is not currently in plan<br>
   * When generating ATTENDEE info in
   * {@link Calendaring#readCalendarEntry(Database, String, String, Collection)},
   * determine and populate response Status (which might be a performance hit)
   */
  SKIP_RESPONSE_DATA(0x00000004);

  public static int toBitMask(final Collection<CalendarRead> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final CalendarRead currFind : CalendarRead.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  CalendarRead(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
