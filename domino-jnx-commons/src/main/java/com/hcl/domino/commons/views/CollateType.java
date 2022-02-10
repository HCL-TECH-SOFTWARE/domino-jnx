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

import java.text.MessageFormat;

import com.hcl.domino.misc.NotesConstants;

/**
 * These are the possible values for the keytype member of the
 * {@code CollateDescriptor} data structure.<br>
 * The keytype structure member specifies the type of sorting that is done in
 * the specified column in a view.
 *
 * @author Karsten Lehmann
 */
public enum CollateType {

  /** Collate by key in summary buffer (requires key name string) */
  KEY(NotesConstants.COLLATE_TYPE_KEY),
  /** Collate by note ID */
  NOTEID(NotesConstants.COLLATE_TYPE_NOTEID),
  /** Collate by "tumbler" summary key (requires key name string) */
  TUMBLER(NotesConstants.COLLATE_TYPE_TUMBLER),
  /** Collate by "category" summary key (requires key name string) */
  CATEGORY(NotesConstants.COLLATE_TYPE_CATEGORY);

  /**
   * Converts a numeric constant to a collate type
   * 
   * @param value constant
   * @return collate type
   */
  public static CollateType toType(final int value) {
    if (value == NotesConstants.COLLATE_TYPE_KEY) {
      return CollateType.KEY;
    } else if (value == NotesConstants.COLLATE_TYPE_NOTEID) {
      return CollateType.NOTEID;
    } else if (value == NotesConstants.COLLATE_TYPE_TUMBLER) {
      return CollateType.TUMBLER;
    } else if (value == NotesConstants.COLLATE_TYPE_CATEGORY) {
      return CollateType.CATEGORY;
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Unknown type constant: {0}", value));
    }
  }

  private int m_val;

  CollateType(final int val) {
    this.m_val = val;
  }

  /**
   * Returns the numeric constant for the collate type
   * 
   * @return constant
   */
  public int getValue() {
    return this.m_val;
  }
}
