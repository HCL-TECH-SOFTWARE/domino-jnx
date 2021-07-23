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
 * Specifies the structure of {@link DominoDateTime} value when converted to a
 * string
 *
 * @author Karsten Lehmann
 */
public enum DateTimeStructure implements INumberEnum<Byte> {

  /** DATE */
  DATE(NotesConstants.TSFMT_DATE),
  /** TIME */
  TIME(NotesConstants.TSFMT_TIME),
  /** DATE TIME */
  DATETIME(NotesConstants.TSFMT_DATETIME),
  /** DATE TIME or TIME Today or TIME Yesterday */
  CDATETIME(NotesConstants.TSFMT_CDATETIME),
  /** DATE, Today or Yesterday */
  CDATE(NotesConstants.TSFMT_CDATE);

  private byte m_val;

  DateTimeStructure(final byte val) {
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
