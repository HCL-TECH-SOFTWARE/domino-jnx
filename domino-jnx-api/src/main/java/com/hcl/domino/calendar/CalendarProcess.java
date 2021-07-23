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
 * {@link CalendarProcess} values are used to define the action taken taken on
 * Calendar notices and entries
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum CalendarProcess {
  /**
   * Accept (regardless of conflicts)<br>
   * For Information update notices or confirm notices, this will apply the
   * changes to the relavent
   * calendar entry.<br>
   * Used by the organizer to accept a counter proposal.
   */
  ACCEPT(0x00000002),
  /** Tentatively accept (regardless of conflicts) */
  TENTATIVE(0x00000004),
  /**
   * Decline<br>
   * Can be used by the organizer to decline a counter if done from a counter
   * notice
   */
  DECLINE(0x00000008),
  /** Delegate to {@link CalendarActionData#withDelegateTo(String)} */
  DELEGATE(0x00000010),
  /**
   * Counter to a new time (requires populating
   * {@link CalendarActionData#withChangeToStart} /
   * {@link CalendarActionData#withChangeToEnd} values)
   */
  COUNTER(0x00000020),
  /**
   * Request updated information from the organizer for this meeting.
   * Also used by the organizer to respond to a request for updated info.
   */
  REQUESTINFO(0x00000040),
  /**
   * This will process a cancelation notice, removing the meeting from the
   * calendar
   */
  REMOVECANCEL(0x00000080),
  /**
   * This will physically delete a meeting from the calendar. This will NOT send
   * notices out
   */
  DELETE(0x00000100),
  /**
   * This will remove the meeting or appointment from the calendar and send
   * notices if
   * necessary.<br>
   * It is treated as a {@link #CANCEL} if the entry is a meeting the mailfile
   * owner is the organizer of.<br>
   * It is treated as a {@link #DECLINE} if the entry is a meeting that the
   * mailfile
   * owner is not the organizer of except when the entry is a broadcast. In that
   * case it
   * is treated as a {@link #DELETE}.<br>
   * It is treated as a {@link #DELETE} if the entry is a non-meeting
   */
  SMARTREMOVE(0x00000200),
  /** This will cancel a meeting that the mailfile owner is the organizer of */
  CANCEL(0x00000400),
  /**
   * This will update the invitee lists on the specified entry (or entries) to
   * include or remove
   * those users specified in lists contained in the
   * {@link CalendarActionData#withAddNamesRequired(String...)} etc. and
   * {@link CalendarActionData#withRemoveNames(String...)} values
   */
  UPDATEINVITEES(0x00002000);

  public static int toBitMask(final Collection<CalendarProcess> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final CalendarProcess currFind : CalendarProcess.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  CalendarProcess(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
