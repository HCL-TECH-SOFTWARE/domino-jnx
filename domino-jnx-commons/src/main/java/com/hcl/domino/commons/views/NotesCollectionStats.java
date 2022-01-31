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
package com.hcl.domino.commons.views;

/**
 * If requested, this structure is returned by
 * {@code DominoCollection#readEntries}
 * at the front of the returned information buffer.<br>
 * The structure describes statistics about the overall collection.
 *
 * @author Karsten Lehmann
 */
public class NotesCollectionStats {
  private final int m_topLevelEntries;
  private final int m_lastModifiedTime;

  public NotesCollectionStats(final int topLevelEntries, final int lastModifiedTime) {
    this.m_topLevelEntries = topLevelEntries;
    this.m_lastModifiedTime = lastModifiedTime;
  }

  /**
   * Currently not used in the C API
   * 
   * @return 0
   */
  public int getLastModifiedTime() {
    return this.m_lastModifiedTime;
  }

  /**
   * # top level entries (level 0)
   * 
   * @return entries
   */
  public int getTopLevelEntries() {
    return this.m_topLevelEntries;
  }
}
