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
 * {@link CalendarActionOptions} values are used to provide additional
 * processing control to some
 * actions taken on Calendar notices and entries
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum CalendarActionOptions {
  /**
   * Indicates that a check should be performed when processing the action to
   * determine
   * if an overwrite of invitee changes to the entry will occur.
   */
  DO_OVERWRITE_CHECK(0x00000001),

  /**
   * New in 9.01 release. Used to indicate that current entry participants should
   * be notified of changes
   * to the participant list in addition to those being added or removed.
   */
  UPDATE_ALL_PARTICIPANTS(0x00000002);

  public static int toBitMask(final Collection<CalendarActionOptions> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final CalendarActionOptions currFind : CalendarActionOptions.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  CalendarActionOptions(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
