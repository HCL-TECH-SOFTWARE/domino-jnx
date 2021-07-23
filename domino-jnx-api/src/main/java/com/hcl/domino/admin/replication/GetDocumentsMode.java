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
package com.hcl.domino.admin.replication;

import com.hcl.domino.misc.INumberEnum;

/**
 * Control flags for NSFDbGetNotes.
 */
public enum GetDocumentsMode implements INumberEnum<Integer> {

  /** Preserve order of notes in NoteID list */
  PRESERVE_ORDER(0x00000001),

  /** Send (copiable) objects along with note */
  SEND_OBJECTS(0x00000002),

  /** Order returned notes by (approximate) ascending size */
  ORDER_BY_SIZE(0x00000004),

  /** Continue to next on list if error encountered */
  CONTINUE_ON_ERROR(0x00000008),

  /** Enable folder-add callback function after the note-level callback */
  GET_FOLDER_ADDS(0x00000010),

  /** Apply folder ops directly - don't bother using callback */
  APPLY_FOLDER_ADDS(0x00000020),

  /** Don't stream - used primarily for testing purposes */
  NO_STREAMING(0x00000040);

  private int m_val;

  GetDocumentsMode(final int val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return this.m_val & 0xffffffff;
  }

  @Override
  public Integer getValue() {
    return this.m_val;
  }

}
