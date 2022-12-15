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
package com.hcl.domino.constants;

import com.hcl.domino.misc.INumberEnum;

/**
 * These flags control the manner in which NIFOpenCollection opens a collection
 * of notes.
 *
 * @author Karsten Lehmann
 */
public enum OpenCollection implements INumberEnum<Short> {

  /**
   * Throw away existing index and rebuild it from scratch
   */
  REBUILD_INDEX((short) 0x0001),

  /**
   * Do not update index or unread list as part of open (usually set by server
   * when it does it incrementally instead).
   */
  NOUPDATE((short) 0x0002),

  /**
   * If collection object has not yet been created, do NOT create it
   * automatically, but instead return a special internal error called
   * ERR_COLLECTION_NOT_CREATED
   */
  OPEN_DO_NOT_CREATE((short) 0x0004);

  private short m_val;

  OpenCollection(final short val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return this.m_val & 0xffff;
  }

  @Override
  public Short getValue() {
    return this.m_val;
  }

}
