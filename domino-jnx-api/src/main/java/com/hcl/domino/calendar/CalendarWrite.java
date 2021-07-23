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

/**
 * Flags that control behavior of the calendar APIs - Used when APIS take
 * iCalendar input to modify calendar data
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum CalendarWrite {

  /**
   * Used when APIs modify entry data via CalUpdateEntry.<br>
   * This flag means that NO data is preserved from the original entry and the
   * resulting entry is 100%
   * a product of the iCalendar passed in.<br>
   * NOTE: When this flag is NOT used, some content may be preserved during an
   * update if that particular
   * content was not included in the iCalendar input.<br>
   * This includes:<br>
   * <ul>
   * <li>Body</li>
   * <li>Attachments</li>
   * <li>Custom data properties as specified in $CSCopyItems</li>
   * </ul>
   */
  COMPLETEREPLACE(0x00000001),

  /**
   * Used when APIs create or modify calendar entries where the organizer is the
   * mailfile owner.<br>
   * When a calendar entry is modified with {@link #DISABLE_IMPLICIT_SCHEDULING}
   * set, no notices
   * are sent (invites, updates, reschedules, cancels, etc)<br>
   * <br>
   * Note: This is not intended for cases where you are saving a meeting as a
   * draft (since there
   * is currently not a capability to then send it later. It will also not allow
   * some notices to
   * go out but other notices not to go out (such as, send invites to added
   * invitees but dont send
   * updates to existing invitees).<br>
   * Rather, this is targeted at callers that prefer to be responsible for sending
   * out notices themselves
   * through a separate mechanism
   */
  DISABLE_IMPLICIT_SCHEDULING(0x00000002),

  /**
   * Used when APIs create or modify entries on the calendar<br>
   * This will allow creation/modification of calendar entries, even if the
   * database is not a mailfile
   */
  IGNORE_VERIFY_DB(0x00000004),

  /**
   * By default, alarms will be created on calendar entries based on VALARM
   * content of iCalendar input.<br>
   * Use of this flag will disregard VALARM information in the iCalendar and use
   * the user's default
   * alarm settings for created or updated entries.
   */
  USE_ALARM_DEFAULTS(0x00000008);

  public static int toBitMask(final Collection<CalendarWrite> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final CalendarWrite currFind : CalendarWrite.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  CalendarWrite(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
