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
package com.hcl.domino.commons.data;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.misc.NotesConstants;

/**
 * Specifies how to format the date part of a {@link DominoDateTime} to a
 * string.
 *
 * @author Karsten Lehmann
 */
public enum DateFormat implements INumberEnum<Byte> {

  /** year, month, and day */
  FULL(NotesConstants.TDFMT_FULL),
  /** month and day, year if not this year */
  CPARTIAL(NotesConstants.TDFMT_CPARTIAL),
  /** month and day */
  PARTIAL(NotesConstants.TDFMT_PARTIAL),
  /** year and month */
  DPARTIAL(NotesConstants.TDFMT_DPARTIAL),
  /** year(4digit), month, and day */
  FULL4(NotesConstants.TDFMT_FULL4),
  /** month and day, year(4digit) if not this year */
  CPARTIAL4(NotesConstants.TDFMT_CPARTIAL4),
  /** year(4digit) and month */
  DPARTIAL4(NotesConstants.TDFMT_DPARTIAL4);

  private byte m_val;

  DateFormat(final byte val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return this.m_val;
  }

  @Override
  public Byte getValue() {
    return this.m_val;
  }

}
