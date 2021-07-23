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

/**
 * Constants to configure the compaction operation
 *
 * @since 1.0.19
 */
public enum CompactMode {

  /** Don't preserve view indexes */
  NO_INDEXES(1, CompactConstants.DBCOMPACT_NO_INDEXES),
  /** Don't lock out database users */
  NO_LOCKOUT(1, CompactConstants.DBCOMPACT_NO_LOCKOUT),
  /** Revert current ODS to the previous ODS version */
  REVERT_ODS(1, CompactConstants.DBCOMPACT_REVERT_ODS),

  /** Compact XXXX.BOX for mail router and other MTAs */
  MAILBOX(1, CompactConstants.DBCOMPACT_MAILBOX),
  /** Don't do in-place compaction */
  NO_INPLACE(1, CompactConstants.DBCOMPACT_NO_INPLACE),

  /** Disable unread marks in destination database */
  DISABLE_UNREAD(1, CompactConstants.DBCOMPACT_DISABLE_UNREAD),
  /** Reenable unread marks in destination database (default) */
  ENABLE_UNREAD(1, CompactConstants.DBCOMPACT_ENABLE_UNREAD),
  /** Disable response info in resulting database */
  DISABLE_RESPONSE_INFO(1, CompactConstants.DBCOMPACT_DISABLE_RESPONSE_INFO),
  /** Disable response info in resulting database (default) */
  ENABLE_RESPONSE_INFO(1, CompactConstants.DBCOMPACT_ENABLE_RESPONSE_INFO),
  /** Enable form/bucket bitmap optimization */
  ENABLE_FORM_BKT_OPT(1, CompactConstants.DBCOMPACT_ENABLE_FORM_BKT_OPT),
  /** Diable form/bucket bitmap optimization (default) */
  DISABLE_FORM_BKT_OPT(1, CompactConstants.DBCOMPACT_DISABLE_FORM_BKT_OPT),
  /**
   * Ignore errors encountered during compaction.
   * That is, make best effort to get something at the end
   */
  IGNORE_ERRORS(1, CompactConstants.DBCOMPACT_IGNORE_ERRORS),

  /** If set, disable transaction logging for new database */
  DISABLE_TXN_LOGGING(1, CompactConstants.DBCOMPACT_DISABLE_TXN_LOGGING),
  /** If set, enable transaction logging for new database */
  ENABLE_TXN_LOGGING(1, CompactConstants.DBCOMPACT_ENABLE_TXN_LOGGING),

  /** If set, do only bitmap correction if in-place can be done */
  RECOVER_SPACE_ONLY(1, CompactConstants.DBCOMPACT_RECOVER_SPACE_ONLY),
  /** Archive/delete, then compact the database */
  ARCHIVE(1, CompactConstants.DBCOMPACT_ARCHIVE),
  /** Just archive/delete, no need to compact */
  ARCHIVE_ONLY(1, CompactConstants.DBCOMPACT_ARCHIVE_ONLY),

  /** If set, always do full space recovery compaction */
  RECOVER_ALL_SPACE(1, CompactConstants.DBCOMPACT_RECOVER_ALL_SPACE),

  /** Disable large UNK table in destination database (default) */
  DISABLE_LARGE_UNKTBL(1, CompactConstants.DBCOMPACT_DISABLE_LARGE_UNKTBL),
  /** Enable large UNK table in destination database */
  ENABLE_LARGE_UNKTBL(1, CompactConstants.DBCOMPACT_ENABLE_LARGE_UNKTBL),

  /** TRUE if design note non-summary should be compressed */
  COMPRESS_DESIGN_NS(2, CompactConstants.DBCOMPACT2_COMPRESS_DESIGN_NS),
  /** TRUE if design note non-summary should be uncompressed */
  UNCOMPRESS_DESIGN_NS(2, CompactConstants.DBCOMPACT2_UNCOMPRESS_DESIGN_NS),

  /** TRUE if all data note non-summary should be compressed */
  COMPRESS_DATA_DOCS(2, CompactConstants.DBCOMPACT2_COMPRESS_DATA_DOCS),
  /** TRUE if all data note non-summary should be uncompressed */
  UNCOMPRESS_DATA_DOCS(2, CompactConstants.DBCOMPACT2_UNCOMPRESS_DATA_DOCS),

  /** enable compact TO DAOS */
  FORCE_DAOS_ON(2, CompactConstants.DBCOMPACT2_FORCE_DAOS_ON),
  /** enable compact FROM DAOS */
  FORCE_DAOS_OFF(2, CompactConstants.DBCOMPACT2_FORCE_DAOS_OFF),

  /** split NIF containers out to their own database */
  SPLIT_NIF_DATA(2, CompactConstants.DBCOMPACT2_SPLIT_NIF_DATA),
  /** see above, but off */
  UNSPLIT_NIF_DATA(2, CompactConstants.DBCOMPACT2_UNSPLIT_NIF_DATA),

  /** enable compact with PIRC */
  FORCE_PIRC_ON(2, CompactConstants.DBCOMPACT2_FORCE_PIRC_ON),
  /** enable compact without PIRC */
  FORCE_PIRC_OFF(2, CompactConstants.DBCOMPACT2_FORCE_PIRC_OFF),

  /**
   * for copy style compaction, force "new" target to be encrypted even if source
   * db is not
   */
  ENABLE_ENCRYPTION(2, CompactConstants.DBCOMPACT2_ENABLE_ENCRYPTION),

  /** Create a new replica in the copy style compact */
  COPY_REPLICA(2, CompactConstants.DBCOMPACT2_COPY_REPLICA);

  private interface CompactConstants {
    /*	Define options for DB compact. */

    /* SDK BEGIN */

    /** Don't preserve view indexes */
    int DBCOMPACT_NO_INDEXES = 0x00000001;
    /** Don't lock out database users */
    int DBCOMPACT_NO_LOCKOUT = 0x00000002;
    /** Revert current ODS to the previous ODS version */
    int DBCOMPACT_REVERT_ODS = 0x00000004;

    /* SDK END */

    /** Indicate we are encrypting database */
    int DBCOMPACT_FOR_ENCRYPT = 0x00000008;
    /** Indicate we are decrypting database */
    int DBCOMPACT_FOR_DECRYPT = 0x00000010;

    /* SDK BEGIN */

    /** Create new file with 4GB file size limit */
    int DBCOMPACT_MAX_4GB = 0x00000020;

    /* SDK END */

    /** This note should be updated as a ghost note */
    int DBCOMPACT_GHOST_NOTE = 0x00000040;

    /* SDK BEGIN */

    /** Compact XXXX.BOX for mail router and other MTAs */
    int DBCOMPACT_MAILBOX = 0x00000080;
    /** Don't do in-place compaction */
    int DBCOMPACT_NO_INPLACE = 0x00000100;

    /* SDK END */

    int DBCOMPACT_ENCRYPT_DEFAULT = 0x00000200;

    /* SDK BEGIN */

    /** Disable unread marks in destination database */
    int DBCOMPACT_DISABLE_UNREAD = 0x00002000;
    /** Reenable unread marks in destination database (default) */
    int DBCOMPACT_ENABLE_UNREAD = 0x00004000;
    /** Disable response info in resulting database */
    int DBCOMPACT_DISABLE_RESPONSE_INFO = 0x00008000;
    /** Disable response info in resulting database (default) */
    int DBCOMPACT_ENABLE_RESPONSE_INFO = 0x00010000;
    /** Enable form/bucket bitmap optimization */
    int DBCOMPACT_ENABLE_FORM_BKT_OPT = 0x00020000;
    /** Diable form/bucket bitmap optimization (default) */
    int DBCOMPACT_DISABLE_FORM_BKT_OPT = 0x00040000;
    /**
     * Ignore errors encountered during compaction.
     * That is, make best effort to get something at the end
     */
    int DBCOMPACT_IGNORE_ERRORS = 0x00080000;

    /* SDK END */

    /** If set, disable transaction logging for new database */
    int DBCOMPACT_DISABLE_TXN_LOGGING = 0x00100000;
    /** If set, enable transaction logging for new database */
    int DBCOMPACT_ENABLE_TXN_LOGGING = 0x00200000;

    /* SDK BEGIN */

    /** If set, do only bitmap correction if in-place can be done */
    int DBCOMPACT_RECOVER_SPACE_ONLY = 0x00400000;
    /** Archive/delete, then compact the database */
    int DBCOMPACT_ARCHIVE = 0x00800000;
    /** Just archive/delete, no need to compact */
    int DBCOMPACT_ARCHIVE_ONLY = 0x01000000;

    /* SDK END */

    /** Just check object size and position fidelity - looking for overlap */
    int DBCOMPACT_VERIFY_NOOVERLAP = 0x02000000;

    /* SDK BEGIN */

    /** If set, always do full space recovery compaction */
    int DBCOMPACT_RECOVER_ALL_SPACE = 0x04000000;

    /* SDK END */

    /** If set and inplace is possible, just dump space map - don't compact */
    int DBCOMPACT_DUMP_SPACE_MAP_ONLY = 0x08000000;
    /** Disable large UNK table in destination database (default) */
    int DBCOMPACT_DISABLE_LARGE_UNKTBL = 0x10000000;
    /** Enable large UNK table in destination database */
    int DBCOMPACT_ENABLE_LARGE_UNKTBL = 0x20000000;
    /** Only do compaction if it can be done inplace - error return otherwise */
    int DBCOMPACT_ONLY_IF_INPLACE = 0x40000000;
    /** Recursively explore subdirectores during NSFSearchExtended */
    int DBCOMPACT_RECURSE_SUBDIRECTORIES = 0x80000000;

    /** Retain bodyheader values for imap enabled databases. */
    int DBCOMPACT2_KEEP_IMAP_ITEMS = 0x00000001;
    /** Upgrade to LZ1 attachments for entire db. */
    int DBCOMPACT2_LZ1_UPGRADE = 0x00000002;
    /**
     * Do not change without checking ARCHIVE_DELETE_ONLY<br>
     * Archive delete only! Must specify DBCOMPACT_ARCHIVE or
     * DBCOMPACT_ARCHIVE_COMPACT
     */
    int DBCOMPACT2_ARCHIVE_JUST_DELETE = 0x00000004;
    /**
     * Force the compact to open the DB with the O_SYNC flag on
     * this is for large db's on systems with huge amounts of memory
     * DBCOMPACT_ARCHIVE_COMPACT
     */
    int DBCOMPACT2_SYNC_OPEN = 0x00000008;
    /** Convert the source database to an NSFDB2 database. */
    int DBCOMPACT2_CONVERT_TO_NSFDB2 = 0x00000010;
    /** Fixup busted LZ1 attachments (really huffman) for entire db. */
    int DBCOMPACT2_LZ1_FIXUP = 0x00000020;
    /** Check busted LZ1 attachments (really huffman) for entire db. */
    int DBCOMPACT2_LZ1_CHECK = 0x00000040;
    /** Downgrade attachments to huffman for entire db. */
    int DBCOMPACT2_LZ1_DOWNGRADE = 0x00000080;
    /** skip NSFDB2 databases found while traversing files index */
    int DBCOMPACT2_SKIP_NSFDB2 = 0x00000100;
    /** skip NSF databases while processing NSFDB2 databases */
    int DBCOMPACT2_SKIP_NSF = 0x00000200;

    /* SDK BEGIN */

    /** TRUE if design note non-summary should be compressed */
    int DBCOMPACT2_COMPRESS_DESIGN_NS = 0x00000400;
    /** TRUE if design note non-summary should be uncompressed */
    int DBCOMPACT2_UNCOMPRESS_DESIGN_NS = 0x00000800;

    /* SDK END */

    /** if TRUE, do db2 group compression for group associated with this nsf */
    int DBCOMPACT2_DB2_ASSOCGRP_COMPACT = 0x00001000;
    /** if TRUE, do db2 group compression directly on group */
    int DBCOMPACT2_DB2_GROUP_COMPACT = 0x00002000;

    /* SDK BEGIN */

    /** TRUE if all data note non-summary should be compressed */
    int DBCOMPACT2_COMPRESS_DATA_DOCS = 0x00004000;
    /** TRUE if all data note non-summary should be uncompressed */
    int DBCOMPACT2_UNCOMPRESS_DATA_DOCS = 0x00008000;

    /* SDK END */

    /**
     * TRUE if return file sizes should be in granules to handle large file sizes
     */
    int DBCOMPACT2_STATS_IN_GRANULES = 0x00010000;

    /* SDK BEGIN */

    /** enable compact TO DAOS */
    int DBCOMPACT2_FORCE_DAOS_ON = 0x00020000;
    /** enable compact FROM DAOS */
    int DBCOMPACT2_FORCE_DAOS_OFF = 0x00040000;

    /* SDK END */

    /** revert one ods based on current ods of the database */
    int DBCOMPACT2_REVERT_ONE_ODS = 0x00080000;
    /** Process attachments inplace for entire db. */
    int DBCOMPACT2_LZ1_INPLACE = 0x00100000;
    /**
     * If ODS is lower than desired ODS based on INI settings, compact it to upgrade
     * ODS
     */
    int DBCOMPACT2_ODS_DEFAULT_UPGRADE = 0x00200000;
    /** split NIF containers out to their own database */
    int DBCOMPACT2_SPLIT_NIF_DATA = 0x01000000;
    /** see above, but off */
    int DBCOMPACT2_UNSPLIT_NIF_DATA = 0x02000000;

    /* SDK BEGIN */

    /** enable compact with PIRC */
    int DBCOMPACT2_FORCE_PIRC_ON = 0x00400000;
    /** enable compact without PIRC */
    int DBCOMPACT2_FORCE_PIRC_OFF = 0x00800000;

    /* SDK END */

    /** SaaS option, enable advanced property override */
    int DBCOMPACT2_ADV_OPT_OVERRIDE_ON = 0x01000000;
    /** SaaS option, disable advanced property override */
    int DBCOMPACT2_ADV_OPT_OVERRIDE_OFF = 0x02000000;

    /** compact is running as DataBaseMaintenanceTool */
    int DBCOMPACT2_DBMT = 0x04000000;
    /** Take database offline for compact */
    int DBCOMPACT2_FORCE = 0x08000000;
    /**
     * for copy style compaction, force "new" target to be encrypted even if source
     * db is not
     */
    int DBCOMPACT2_ENABLE_ENCRYPTION = 0x10000000;
    /** a saas-only option to collect information when compacting for import */
    int DBCOMPACT2_SAAS_IMPORT = 0x20000000;
    /** Upgrade previous DBCLASS_V*NOTEFILE classes to DBCLASS_NOTEFILE */
    int DBCOMPACT2_DBCLASS_UPGRADE = 0x40000000;
    /** Create a new replica in the copy style compact */
    int DBCOMPACT2_COPY_REPLICA = 0x80000000;

  }

  private int setIndex;

  private int value;

  CompactMode(final int setIndex, final int value) {
    this.setIndex = setIndex;
    this.value = value;
  }

  public int getSetIndex() {
    return this.setIndex;
  }

  public int getValue() {
    return this.value;
  }
}
