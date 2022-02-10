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
package com.hcl.domino.calendar;

import java.util.Collection;

/**
 * Flags that control behavior of the calendar APIs that return iCalendar data
 * for an entry or notice
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum CalendarReadRange {

  DTSTART(1, 0x00000001),
  DTEND(1, 0x00000002),
  DTSTAMP(1, 0x00000004),
  SUMMARY(1, 0x00000008),
  CLASS(1, 0x00000010),
  PRIORITY(1, 0x00000020),
  RECURRENCE_ID(1, 0x00000040),
  SEQUENCE(1, 0x00000080),
  LOCATION(1, 0x00000100),
  TRANSP(1, 0x00000200),
  CATEGORY(1, 0x00000400),
  APPTTYPE(1, 0x00000800),
  NOTICETYPE(1, 0x00001000),
  STATUS(1, 0x00002000),
  /**
   * Includes online meeting URL as well as any online meeting password or conf ID
   */
  ONLINE_URL(1, 0x00004000),
  /**
   * Note: For performance reasons, the organizer may not be stored in
   * ORGANIZER but rather in X-LOTUS-ORGANIZER to avoid lookups necessary
   * to get the internet address.
   */
  NOTESORGANIZER(1, 0x00008000),
  /**
   * Note: For performance reasons, the organizer may not be stored in PARTICIPANT
   * but
   * rather in X-LOTUS-ROOM to avoid lookups necessary to get the internet
   * address.
   */
  NOTESROOM(1, 0x00010000),
  /** Output alarm information for this entry */
  ALARM(1, 0x00020000),

  /**
   * X-LOTUS-HASATTACH is set to 1 if there are any file attachments for this
   * entry
   */
  HASATTACH(2, 0x00000001),
  /**
   * X-LOTUS-UNID will always be set for notices (as it is used as the identifier
   * for
   * a notice), but setting this flag will also set X-LOTUS-UNID for calendar
   * entries,
   * where this will be set with the UNID of the note that currently contains this
   * instance (can be used to construct a URL to open the instance in Notes, for
   * instance)
   */
  UNID(2, 0x00000002);

  public static int toBitMask(final Collection<CalendarReadRange> flagSet) {
    int result = 0;
    if (flagSet != null) {
      for (final CalendarReadRange currFlag : CalendarReadRange.values()) {
        if (currFlag.getMaskNr() == 1) {
          if (flagSet.contains(currFlag)) {
            result = result | currFlag.getValue();
          }
        }
      }
    }
    return result;
  }

  public static int toBitMask2(final Collection<CalendarReadRange> flagSet) {
    int result = 0;
    if (flagSet != null) {
      for (final CalendarReadRange currFlag : CalendarReadRange.values()) {
        if (currFlag.getMaskNr() == 2) {
          if (flagSet.contains(currFlag)) {
            result = result | currFlag.getValue();
          }
        }
      }
    }
    return result;
  }

  private int m_maskNr;

  private int m_val;

  CalendarReadRange(final int maskNr, final int val) {
    this.m_maskNr = maskNr;
    this.m_val = val;
  }

  public int getMaskNr() {
    return this.m_maskNr;
  }

  public int getValue() {
    return this.m_val;
  }

}
