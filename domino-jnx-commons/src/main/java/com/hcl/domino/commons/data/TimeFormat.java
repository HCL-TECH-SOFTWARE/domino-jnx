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
 * Specifies how to format the time part of a {@link DominoDateTime} to a
 * string.
 *
 * @author Karsten Lehmann
 */
public enum TimeFormat implements INumberEnum<Byte> {

  /** hour, minute, and second */
  FULL(NotesConstants.TTFMT_FULL),
  /** hour and minute */
  PARTIAL(NotesConstants.TTFMT_PARTIAL),
  /** hour */
  HOUR(NotesConstants.TTFMT_HOUR),
  /**
   * hour, minute, second, hundredths (max resolution). This currently works only
   * for time-to-text conversion!
   */
  FULL_MAX(NotesConstants.TTFMT_FULL_MAX);

  private byte m_val;

  TimeFormat(final byte val) {
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
