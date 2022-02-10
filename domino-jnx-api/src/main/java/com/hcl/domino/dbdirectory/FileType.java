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
package com.hcl.domino.dbdirectory;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * File type flags used for directory searching.
 * Note: The values of these constants are the very same constants used by the
 * C-API.
 *
 * @author Karsten Lehmann
 */
public enum FileType {

  /** Any file type */
  ANY(0),

  /** Starting in V3, any DB that is a candidate for replication */
  DBREPL(1),

  /** Databases that can be templates */
  DBDESIGN(2),

  /** BOX - Any .BOX (Mail.BOX, SMTP.Box...) */
  MAILBOX(3),

  /** NS?, any NSF version */
  DBANY(4),

  /** NT?, any NTF version */
  FTANY(5),

  /** MDM - modem command file */
  MDMTYPE(6),

  /** directories only */
  DIRSONLY(7),

  /** VPC - virtual port command file */
  VPCTYPE(8),

  /** SCR - comm port script files */
  SCRTYPE(9),

  /** ANY Notes database (.NS?, .NT?, .BOX) */
  ANYNOTEFILE(10),

  /**
   * DTF - Any .DTF. Used for container and sort temp files to give them a more
   * unique name than .TMP so we can delete *.DTF from the temp directory and
   * hopefully not blow away other application's temp files.
   */
  UNIQUETEMP(11),

  /** CLN - Any .cln file...multi user cleanup files */
  MULTICLN(12),

  /** any smarticon file *.smi */
  SMARTI(13),

  /** File type mask (for FILE_xxx codes above) */
  TYPEMASK(0x00ff),

  /** List subdirectories as well as normal files */
  DIRS(0x8000),

  /** Do NOT return ..'s */
  NOUPDIRS(0x4000),

  /** Recurse into subdirectories */
  RECURSE(0x2000),

  /** All directories, linked files &amp; directories */
  LINKSONLY(0x1000);

  public static short toBitMask(final Collection<FileType> noteClassSet) {
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

  public static int toBitMaskInt(final Collection<FileType> noteClassSet) {
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

  public static Set<FileType> toFileTypes(final int bitMask) {
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
