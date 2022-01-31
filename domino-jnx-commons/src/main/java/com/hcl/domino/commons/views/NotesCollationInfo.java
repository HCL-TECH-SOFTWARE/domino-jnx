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

import java.util.List;

import com.hcl.domino.data.DominoCollection;
import com.hcl.domino.misc.NotesConstants;

/**
 * Container object that provides access to the available sortings for a
 * {@link DominoCollection}
 *
 * @author Karsten Lehmann
 */
public class NotesCollationInfo {
  public byte m_flags;
  private final List<NotesCollateDescriptor> m_collateDescriptors;

  public NotesCollationInfo(final byte flags, final List<NotesCollateDescriptor> descriptors) {
    this.m_flags = flags;
    this.m_collateDescriptors = descriptors;
  }

  /**
   * Returns the collate descriptors with the sortings used for this collation
   * 
   * @return descriptors
   */
  public List<NotesCollateDescriptor> getDescriptors() {
    return this.m_collateDescriptors;
  }

  /**
   * Flag to indicate only build on demand.
   * 
   * @return true for build on demand
   */
  public boolean isBuildOnDemand() {
    return (this.m_flags & NotesConstants.COLLATION_FLAG_BUILD_ON_DEMAND) == NotesConstants.COLLATION_FLAG_BUILD_ON_DEMAND;
  }

  /**
   * Indicates unique keys. Used for ODBC Access: Generate unique keys in index.
   * 
   * @return true for unique keys
   */
  public boolean isUnique() {
    return (this.m_flags & NotesConstants.COLLATION_FLAG_UNIQUE) == NotesConstants.COLLATION_FLAG_UNIQUE;
  }
}
