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
package com.hcl.domino.commons.constants;

import java.util.Set;

import com.hcl.domino.misc.NotesConstants;

/**
 * Option flags for NSFDbCopyExtended and NSFDbCreateAndCopyExtended
 *
 * @author Karsten Lehmann
 */
public enum CopyDatabase {

  /** New database is a replica of the original. */
  REPLICA(NotesConstants.DBCOPY_REPLICA),

  /** Copy template notes. Only supported for NSFDbCopyExtended. */
  SUBCLASS_TEMPLATE(NotesConstants.DBCOPY_SUBCLASS_TEMPLATE),

  /**
   * Copy secondary header (such as database quota information).<br>
   * This flag may only be used with a local database; using this flag with a
   * remote database
   * will return the error ERR_NOT_LOCAL. Only supported for NSFDbCopyExtended.
   */
  DBINFO2(NotesConstants.DBCOPY_DBINFO2),

  /**
   * Copy mail router objects. This flag may only be used with a local database;
   * using this
   * flag with a remote database will return the error ERR_NOT_LOCAL. Only
   * supported for NSFDbCopyExtended.
   */
  SPECIAL_OBJECTS(NotesConstants.DBCOPY_SPECIAL_OBJECTS),

  /** Do not copy access control list. Only supported for NSFDbCopyExtended. */
  NO_ACL(NotesConstants.DBCOPY_NO_ACL),

  /** Do not copy full text index. Only supported for NSFDbCopyExtended. */
  NO_FULLTEXT(NotesConstants.DBCOPY_NO_FULLTEXT),

  /** Use simple encryption in new database. */
  ENCRYPT_SIMPLE(NotesConstants.DBCOPY_ENCRYPT_SIMPLE),

  /** Use middle level of encryption in new database. */
  ENCRYPT_MEDIUM(NotesConstants.DBCOPY_ENCRYPT_MEDIUM),

  /** Use strongest level of encryption in new database. */
  ENCRYPT_STRONG(NotesConstants.DBCOPY_ENCRYPT_STRONG),

  /**
   * Do not update each note's modification time. Only supported for
   * NSFDbCopyExtended.
   */
  KEEP_NOTE_MODTIME(NotesConstants.DBCOPY_KEEP_NOTE_MODTIME),

  /** Copy the NameList (applicable only when DBCOPY_REPLICA is specified) */
  REPLICA_NAMELIST(NotesConstants.DBCOPY_REPLICA_NAMELIST),

  /** Destination should override default if able to */
  OVERRIDE_DEST(NotesConstants.DBCOPY_OVERRIDE_DEST),

  /** Create Db using the latest ODS, regardless of INI settings */
  DBCLASS_HIGHEST_NOTEFILE(NotesConstants.DBCOPY_DBCLASS_HIGHEST_NOTEFILE),

  /** Create Db for copy style compaction */
  COMPACT_REPLICA(NotesConstants.DBCOPY_COMPACT_REPLICA);

  public static int toBitMask(final Set<CopyDatabase> flags) {
    int result = 0;
    if (flags != null) {
      for (final CopyDatabase currFlag : CopyDatabase.values()) {
        if (flags.contains(currFlag)) {
          result = result | currFlag.getValue();
        }
      }
    }

    return result;
  }

  /**
   * Converts a numeric constant to a db creation flag
   * 
   * @param value constant
   * @return db creation flag
   */
  public static CopyDatabase toType(final int value) {
    for (final CopyDatabase currClass : CopyDatabase.values()) {
      if (value == currClass.getValue()) {
        return currClass;
      }
    }
    throw new IllegalArgumentException("Unknown constant: " + value);
  }

  private int m_val;

  CopyDatabase(final int val) {
    this.m_val = val;
  }

  /**
   * Returns the numeric constant for the db creation flag
   * 
   * @return constant
   */
  public int getValue() {
    return this.m_val;
  }
}
