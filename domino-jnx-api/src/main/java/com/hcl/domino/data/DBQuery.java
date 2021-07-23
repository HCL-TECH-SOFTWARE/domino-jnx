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
package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

/**
 * Flags to query {@link Database} data using DQL
 */
public enum DBQuery implements INumberEnum<Integer> {
  /** Explain only mode, only plan and return the explain output */
  NO_EXEC(0x00000001),
  /** produce debugging output (notes.ini setting is independent of this) */
  DEBUG(0x00000002),
  /** refresh all views when they are opened (default is NO_UPDATE) */
  VIEWREFRESH(0x00000004),
  /** to check for syntax only - stops short of planning */
  PARSEONLY(0x00000008),
  /** Governs producing Explain output */
  EXPLAIN(0x00000010),
  /** NSF scans only */
  NOVIEWS(0x00000020),
  /** For the 1st FT search, update the index */
  FT_REFRESH(0x00000040),
  /** before running the query, build/refresh the design catalog */
  DESIGN_CATALOG_REFRESH(0x00000080),
  /** before running the query, rebuild the design catalog */
  DESIGN_CATALOG_REBUILD(0x00000100);

  private int m_val;

  DBQuery(final int val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return this.m_val;
  }

  @Override
  public Integer getValue() {
    return this.m_val;
  }

}
