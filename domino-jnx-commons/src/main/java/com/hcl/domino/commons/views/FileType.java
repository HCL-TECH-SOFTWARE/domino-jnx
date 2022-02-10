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

import java.util.EnumSet;

import com.hcl.domino.misc.NotesConstants;

/**
 * File type flags (used with NSFSearch directory searching)
 *
 * @author Karsten Lehmann
 */
public enum FileType {

  /** Any file type */
  ANY(NotesConstants.FILE_ANY),

  /** Starting in V3, any DB that is a candidate for replication */
  DBREPL(NotesConstants.FILE_DBREPL),

  /** Databases that can be templates */
  DBDESIGN(NotesConstants.FILE_DBDESIGN),

  /** BOX - Any .BOX (Mail.BOX, SMTP.Box...) */
  MAILBOX(NotesConstants.FILE_MAILBOX),

  /** NS?, any NSF version */
  DBANY(NotesConstants.FILE_DBANY),

  /** NT?, any NTF version */
  FTANY(NotesConstants.FILE_FTANY),

  /** MDM - modem command file */
  MDMTYPE(NotesConstants.FILE_MDMTYPE),

  /** directories only */
  DIRSONLY(NotesConstants.FILE_DIRSONLY),

  /** VPC - virtual port command file */
  VPCTYPE(NotesConstants.FILE_VPCTYPE),

  /** SCR - comm port script files */
  SCRTYPE(NotesConstants.FILE_SCRTYPE),

  /** ANY Notes database (.NS?, .NT?, .BOX) */
  ANYNOTEFILE(NotesConstants.FILE_ANYNOTEFILE),

  /**
   * DTF - Any .DTF. Used for container and sort temp files to give them a more
   * unique name than .TMP so we can delete *.DTF from the temp directory and
   * hopefully not blow away other application's temp files.
   */
  UNIQUETEMP(NotesConstants.FILE_UNIQUETEMP),

  /** CLN - Any .cln file...multi user cleanup files */
  MULTICLN(NotesConstants.FILE_MULTICLN),

  /** any smarticon file *.smi */
  SMARTI(NotesConstants.FILE_SMARTI),

  /** File type mask (for FILE_xxx codes above) */
  TYPEMASK(NotesConstants.FILE_TYPEMASK),

  /** List subdirectories as well as normal files */
  DIRS(NotesConstants.FILE_DIRS),

  /** Do NOT return ..'s */
  NOUPDIRS(NotesConstants.FILE_NOUPDIRS),

  /** Recurse into subdirectories */
  RECURSE(NotesConstants.FILE_RECURSE),

  /** All directories, linked files &amp; directories */
  LINKSONLY(NotesConstants.FILE_LINKSONLY);

  public static short toBitMask(final EnumSet<FileType> noteClassSet) {
    int result = 0;
    if (noteClassSet != null) {
      for (final FileType currFind : FileType.values()) {
        if (noteClassSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return (short) (result & 0xffff);
  }

  public static int toBitMaskInt(final EnumSet<FileType> noteClassSet) {
    int result = 0;
    if (noteClassSet != null) {
      for (final FileType currFind : FileType.values()) {
        if (noteClassSet.contains(currFind)) {
          result = result | currFind.getValue();
        }
      }
    }
    return result;
  }

  public static EnumSet<FileType> toFileTypes(final int bitMask) {
    final EnumSet<FileType> set = EnumSet.noneOf(FileType.class);
    for (final FileType currClass : FileType.values()) {
      if ((bitMask & currClass.getValue()) == currClass.getValue()) {
        set.add(currClass);
      }
    }
    return set;
  }

  private int m_val;

  FileType(final int val) {
    this.m_val = val;
  }

  public int getValue() {
    return this.m_val;
  }

}
