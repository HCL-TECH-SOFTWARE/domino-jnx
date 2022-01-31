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
 * Flags that control behavior of the calendar APIs - Used when opening a
 * document for calendar data.
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum CalendarDocumentOpen {

  /**
   * Used when getting a handle via CalOpenNoteHandle (Handy for read-only
   * cases)<br>
   * When a specific instance of a recurring entry is requested, the underlying
   * note may
   * represent multiple instances.<br>
   * <br>
   * Default behavior makes appropriate modifications so that the returned handle
   * represents
   * a single instance (but this might cause notes to be created or modified as a
   * side effect).<br>
   * <br>
   * Using {@link #HANDLE_NOSPLIT} will bypass any note creations or modifications
   * and return a
   * note handle that may represent more than a single instance on the calendar.
   */
  HANDLE_NOSPLIT(0x00000001);

  public static int toBitMask(final Collection<CalendarDocumentOpen> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final CalendarDocumentOpen currFind : CalendarDocumentOpen.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  CalendarDocumentOpen(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
