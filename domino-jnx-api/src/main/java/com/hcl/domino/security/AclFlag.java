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
package com.hcl.domino.security;

import com.hcl.domino.misc.INumberEnum;

/**
 * These symbols represent access level modifier flags in access control
 * lists.<br>
 * <br>
 * Each access level taken by itself implies a certain set of immutable
 * capabilities.<br>
 * Each access level has a different set of access modifier bits that are
 * relevant for that level.<br>
 * <br>
 * All of the other bits that are returned in the Access Flag parameter of C API
 * functions are
 * irrelevant and are unpredictable.<br>
 * <br>
 * <br>
 * <table>
 * <caption>The table depicts which Access Level Modifier Flags
 * ({@link AclFlag}) are applicable to the Access Levels
 * ({@link AclLevel})</caption>
 * <tr>
 * <th>{@link AclLevel}</th>
 * <th>
 * <th>{@link AclFlag} Applicable to {@link AclLevel}</th>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#MANAGER}</td>
 * <td>{@link #NODELETE}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#DESIGNER}</td>
 * <td>{@link #NODELETE}<br>
 * {@link #CREATE_LOTUSSCRIPT}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#EDITOR}</td>
 * <td>{@link #NODELETE}<br>
 * {@link #CREATE_PRAGENT}<br>
 * {@link #CREATE_PRFOLDER}<br>
 * {@link #CREATE_FOLDER}<br>
 * {@link #CREATE_LOTUSSCRIPT}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#AUTHOR}</td>
 * <td>{@link #AUTHOR_NOCREATE}<br>
 * {@link #NODELETE}<br>
 * {@link #CREATE_PRAGENT}<br>
 * {@link #CREATE_PRFOLDER}<br>
 * {@link #CREATE_LOTUSSCRIPT}<br>
 * {@link #PUBLICWRITER}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#READER}</td>
 * <td>{@link #CREATE_PRAGENT}<br>
 * {@link #CREATE_PRFOLDER}<br>
 * {@link #CREATE_LOTUSSCRIPT}<br>
 * {@link #PUBLICWRITER}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#DEPOSITOR}</td>
 * <td>{@link #PUBLICREADER}<br>
 * {@link #PUBLICWRITER}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * <tr>
 * <td>{@link AclLevel#NOACCESS}</td>
 * <td>{@link #PUBLICREADER}<br>
 * {@link #PUBLICWRITER}<br>
 * {@link #PERSON}<br>
 * {@link #GROUP}<br>
 * {@link #SERVER}</td>
 * </tr>
 * </table>
 */
public enum AclFlag implements INumberEnum<Short> {

  /** Authors can't create new notes (only edit existing ones) */
  AUTHOR_NOCREATE((short) 0x0001),

  /** Entry represents a Server (V4) */
  SERVER((short) 0x0002),

  /** User cannot delete notes */
  NODELETE((short) 0x0004),

  /** User can create personal agents (V4) */
  CREATE_PRAGENT((short) 0x0008),

  /** User can create personal folders (V4) */
  CREATE_PRFOLDER((short) 0x0010),

  /** Entry represents a Person (V4) */
  PERSON((short) 0x0020),

  /** Entry represents a group (V4) */
  GROUP((short) 0x0040),

  /**
   * User can create and update shared views &amp; folders (V4)<br>
   * This allows an Editor to assume some Designer-level access
   */
  CREATE_FOLDER((short) 0x0080),

  /** User can create LotusScript */
  CREATE_LOTUSSCRIPT((short) 0x0100),

  /** User can read public notes */
  PUBLICREADER((short) 0x0200),

  /** User can write public notes */
  PUBLICWRITER((short) 0x0400),

  /** User CANNOT register monitors for this database */
  MONITORS_DISALLOWED((short) 0x800),

  /** User cannot replicate or copy this database */
  NOREPLICATE((short) 0x1000),

  /** Admin server can modify reader and author fields in db */
  ADMIN_READERAUTHOR((short) 0X4000),

  /** Entry is administration server (V4) */
  ADMIN_SERVER((short) (0x8000 & 0xffff));

  private Short m_val;

  AclFlag(final Short val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return (long) this.m_val & 0xffff;
  }

  @Override
  public Short getValue() {
    return this.m_val;
  }

}
