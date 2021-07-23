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

public enum ScheduleAttr {
  /** Used by gateways to return foreign UNIDs */
  FOREIGNUNID(0x10),
  /** Used by V5 C&amp;S to identify new repeating meetings */
  REPEATEVENT(0x20),

  /* these are the entry type bits */

  /** Entry types that block off busy time. */
  BUSY(0x08),

  /** Entry types that don't block off busy time */
  PENCILED(0x01),

  /* Entry types that block off busy time */

  APPT(0x08 + 0x00),
  NONWORK(0x08 + 0x01);

  private int m_val;

  ScheduleAttr(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }
}
