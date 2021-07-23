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
package com.hcl.domino.freebusy;

import java.util.Collection;

public enum ScheduleOptions {

  /** Return composite sched */
  COMPOSITE(0x0001),

  /** Return each person's sched */
  EACHPERSON(0x0002),

  /** Do only local lookup */
  LOCAL(0x0004),

  /** force remote even if you are using workstation based email */
  FORCEREMOTE(0x0020);

  public static short toBitMask(final Collection<ScheduleOptions> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final ScheduleOptions currFind : ScheduleOptions.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return (short) (result & 0xffff);
  }

  public static int toBitMaskInt(final Collection<ScheduleOptions> findSet) {
    int result = 0;
    if (findSet != null) {
      for (final ScheduleOptions currFind : ScheduleOptions.values()) {
        if (findSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  private int m_val;

  ScheduleOptions(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
