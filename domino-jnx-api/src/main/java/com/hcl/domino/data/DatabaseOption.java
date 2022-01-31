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
package com.hcl.domino.data;

import com.hcl.domino.misc.INumberEnum;

/**
 * Enum of the available database option bits that can be set via
 * {@link Database#getOption(DatabaseOption)} and
 * {@link Database#setOption(DatabaseOption, boolean)}
 *
 * @author Karsten Lehmann
 */
public enum DatabaseOption implements INumberEnum<Integer> {

  /** Enable full text indexing */
  FT_INDEX(DatabaseOptionConstants.DBOPTBIT_FT_INDEX),

  /**
   * TRUE if database is being used as an object store - for garbage collection
   */
  IS_OBJSTORE(DatabaseOptionConstants.DBOPTBIT_IS_OBJSTORE),

  /**
   * TRUE if database has notes which refer to an object store - for garbage
   * collection
   */
  USES_OBJSTORE(DatabaseOptionConstants.DBOPTBIT_USES_OBJSTORE),

  /** TRUE if NoteUpdate of notes in this db should never use an object store. */
  OBJSTORE_NEVER(DatabaseOptionConstants.DBOPTBIT_OBJSTORE_NEVER),

  /** TRUE if database is a library */
  IS_LIBRARY(DatabaseOptionConstants.DBOPTBIT_IS_LIBRARY),

  /** TRUE if uniform access control across all replicas */
  UNIFORM_ACCESS(DatabaseOptionConstants.DBOPTBIT_UNIFORM_ACCESS),

  /**
   * TRUE if NoteUpdate of notes in this db should always try to use an object
   * store.
   */
  OBJSTORE_ALWAYS(DatabaseOptionConstants.DBOPTBIT_OBJSTORE_ALWAYS),

  /** TRUE if garbage collection is never to be done on this object store */
  COLLECT_NEVER(DatabaseOptionConstants.DBOPTBIT_COLLECT_NEVER),

  /**
   * TRUE if this is a template and is considered an advanced one (for experts
   * only.)
   */
  ADV_TEMPLATE(DatabaseOptionConstants.DBOPTBIT_ADV_TEMPLATE),

  /** TRUE if db has no background agent */
  NO_BGAGENT(DatabaseOptionConstants.DBOPTBIT_NO_BGAGENT),

  /**
   * TRUE is db is out-of-service, no new opens allowed, unless
   * DBOPEN_IGNORE_OUTOFSERVICE is specified
   */
  OUT_OF_SERVICE(DatabaseOptionConstants.DBOPTBIT_OUT_OF_SERVICE),

  /** TRUE if db is personal journal */
  IS_PERSONALJOURNAL(DatabaseOptionConstants.DBOPTBIT_IS_PERSONALJOURNAL),

  /**
   * TRUE if db is marked for delete. no new opens allowed, cldbdir will delete
   * the database when ref count = = 0;
   */
  MARKED_FOR_DELETE(DatabaseOptionConstants.DBOPTBIT_MARKED_FOR_DELETE),

  /** TRUE if db stores calendar events */
  HAS_CALENDAR(DatabaseOptionConstants.DBOPTBIT_HAS_CALENDAR),

  /** TRUE if db is a catalog index */
  IS_CATALOG_INDEX(DatabaseOptionConstants.DBOPTBIT_IS_CATALOG_INDEX),

  /** TRUE if db is an address book */
  IS_ADDRESS_BOOK(DatabaseOptionConstants.DBOPTBIT_IS_ADDRESS_BOOK),

  /** TRUE if db is a "multi-db-search" repository */
  IS_SEARCH_SCOPE(DatabaseOptionConstants.DBOPTBIT_IS_SEARCH_SCOPE),

  /**
   * TRUE if db's user activity log is confidential, only viewable by designer and
   * manager
   */
  IS_UA_CONFIDENTIAL(DatabaseOptionConstants.DBOPTBIT_IS_UA_CONFIDENTIAL),

  /**
   * TRUE if item names are to be treated as if the ITEM_RARELY_USED_NAME flag is
   * set.
   */
  RARELY_USED_NAMES(DatabaseOptionConstants.DBOPTBIT_RARELY_USED_NAMES),

  /** TRUE if db is a "multi-db-site" repository */
  IS_SITEDB(DatabaseOptionConstants.DBOPTBIT_IS_SITEDB),

  /** TRUE if docs in folders in this db have folder references */
  FOLDER_REFERENCES(DatabaseOptionConstants.DBOPTBIT_FOLDER_REFERENCES),

  /** TRUE if the database is a proxy for non-NSF data */
  IS_PROXY(DatabaseOptionConstants.DBOPTBIT_IS_PROXY),

  /** TRUE for NNTP server add-in dbs */
  IS_NNTP_SERVER_DB(DatabaseOptionConstants.DBOPTBIT_IS_NNTP_SERVER_DB),

  /**
   * TRUE if this is a replica of an IMAP proxy, enables certain * special cases
   * for interacting with db
   */
  IS_INET_REPL(DatabaseOptionConstants.DBOPTBIT_IS_INET_REPL),

  /** TRUE if db is a Lightweight NAB */
  IS_LIGHT_ADDRESS_BOOK(DatabaseOptionConstants.DBOPTBIT_IS_LIGHT_ADDRESS_BOOK),

  /**
   * TRUE if database has notes which refer to an object store - for garbage
   * collection
   */
  ACTIVE_OBJSTORE(DatabaseOptionConstants.DBOPTBIT_ACTIVE_OBJSTORE),

  /** TRUE if database is globally routed */
  GLOBALLY_ROUTED(DatabaseOptionConstants.DBOPTBIT_GLOBALLY_ROUTED),

  /** TRUE if database has mail autoprocessing enabled */
  CS_AUTOPROCESSING_ENABLED(DatabaseOptionConstants.DBOPTBIT_CS_AUTOPROCESSING_ENABLED),

  /** TRUE if database has mail filters enabled */
  MAIL_FILTERS_ENABLED(DatabaseOptionConstants.DBOPTBIT_MAIL_FILTERS_ENABLED),

  /** TRUE if database holds subscriptions */
  IS_SUBSCRIPTIONDB(DatabaseOptionConstants.DBOPTBIT_IS_SUBSCRIPTIONDB),

  /** TRUE if data base supports "check-in" "check-out" */
  IS_LOCK_DB(DatabaseOptionConstants.DBOPTBIT_IS_LOCK_DB),

  /** TRUE if editor must lock notes to edit */
  IS_DESIGNLOCK_DB(DatabaseOptionConstants.DBOPTBIT_IS_DESIGNLOCK_DB),

  /* ODS26+ options */

  /** if TRUE, store all modified index blocks in lz1 compressed form */
  COMPRESS_INDEXES(DatabaseOptionConstants.DBOPTBIT_COMPRESS_INDEXES),
  /** if TRUE, store all modified buckets in lz1 compressed form */
  COMPRESS_BUCKETS(DatabaseOptionConstants.DBOPTBIT_COMPRESS_BUCKETS),
  /**
   * FALSE by default, turned on forever if DBFLAG_COMPRESS_INDEXES or
   * DBFLAG_COMPRESS_BUCKETS are ever turned on.
   */
  POSSIBLY_COMPRESSED(DatabaseOptionConstants.DBOPTBIT_POSSIBLY_COMPRESSED),
  /** TRUE if freed space in db is not overwritten */
  NO_FREE_OVERWRITE(DatabaseOptionConstants.DBOPTBIT_NO_FREE_OVERWRITE),
  /** DB doesn't maintain unread marks */
  NOUNREAD(DatabaseOptionConstants.DBOPTBIT_NOUNREAD),
  /** TRUE if the database does not maintain note hierarchy info. */
  NO_RESPONSE_INFO(DatabaseOptionConstants.DBOPTBIT_NO_RESPONSE_INFO),
  /** Disabling of response info will happen on next compaction */
  DISABLE_RSP_INFO_PEND(DatabaseOptionConstants.DBOPTBIT_DISABLE_RSP_INFO_PEND),
  /** Enabling of response info will happen on next compaction */
  ENABLE_RSP_INFO_PEND(DatabaseOptionConstants.DBOPTBIT_ENABLE_RSP_INFO_PEND),
  /** Form/Bucket bitmap optimization is enabled */
  FORM_BUCKET_OPT(DatabaseOptionConstants.DBOPTBIT_FORM_BUCKET_OPT),
  /** Disabling of Form/Bucket bitmap opt will happen on next compaction */
  DISABLE_FORMBKT_PEND(DatabaseOptionConstants.DBOPTBIT_DISABLE_FORMBKT_PEND),
  /** Enabling of Form/Bucket bitmap opt will happen on next compaction */
  ENABLE_FORMBKT_PEND(DatabaseOptionConstants.DBOPTBIT_ENABLE_FORMBKT_PEND),
  /** If TRUE, maintain LastAccessed */
  MAINTAIN_LAST_ACCESSED(DatabaseOptionConstants.DBOPTBIT_MAINTAIN_LAST_ACCESSED),
  /** If TRUE, transaction logging is disabled for this database */
  DISABLE_TXN_LOGGING(DatabaseOptionConstants.DBOPTBIT_DISABLE_TXN_LOGGING),
  /** If TRUE, monitors can't be used against this database (non-replicating) */
  MONITORS_NOT_ALLOWED(DatabaseOptionConstants.DBOPTBIT_MONITORS_NOT_ALLOWED),
  /** If TRUE, all transactions on this database are nested top actions */
  NTA_ALWAYS(DatabaseOptionConstants.DBOPTBIT_NTA_ALWAYS),
  /** If TRUE, objects are not to be logged */
  DONTLOGOBJECTS(DatabaseOptionConstants.DBOPTBIT_DONTLOGOBJECTS),
  /**
   * If set, the default delete is soft. Can be overwritten by UPDATE_DELETE_HARD
   */
  DELETES_ARE_SOFT(DatabaseOptionConstants.DBOPTBIT_DELETES_ARE_SOFT),

  /* The following bits are used by the webserver and are gotten from the icon note */

  /** if TRUE, the Db needs to be opened using SSL over HTTP */
  HTTP_DBIS_SSL(DatabaseOptionConstants.DBOPTBIT_HTTP_DBIS_SSL),
  /**
   * if TRUE, the Db needs to use JavaScript to render the HTML for formulas,
   * buttons, etc
   */
  HTTP_DBIS_JS(DatabaseOptionConstants.DBOPTBIT_HTTP_DBIS_JS),
  /** if TRUE, there is a $DefaultLanguage value on the $icon note */
  HTTP_DBIS_MULTILANG(DatabaseOptionConstants.DBOPTBIT_HTTP_DBIS_MULTILANG),

  /* ODS37+ options */

  /** if TRUE, database is a mail.box (ODS37 and up) */
  IS_MAILBOX(DatabaseOptionConstants.DBOPTBIT_IS_MAILBOX),
  /** if TRUE, database is allowed to have /gt;64KB UNK table */
  LARGE_UNKTABLE(DatabaseOptionConstants.DBOPTBIT_LARGE_UNKTABLE),
  /** If TRUE, full-text index is accent sensitive */
  ACCENT_SENSITIVE_FT(DatabaseOptionConstants.DBOPTBIT_ACCENT_SENSITIVE_FT),
  /** TRUE if database has NSF support for IMAP enabled */
  IMAP_ENABLED(DatabaseOptionConstants.DBOPTBIT_IMAP_ENABLED),
  /** TRUE if database is a USERless N&amp;A Book */
  USERLESS_NAB(DatabaseOptionConstants.DBOPTBIT_USERLESS_NAB),
  /** TRUE if extended ACL's apply to this Db */
  EXTENDED_ACL(DatabaseOptionConstants.DBOPTBIT_EXTENDED_ACL),
  /** TRUE if connections to = 3;rd party DBs are allowed */
  DECS_ENABLED(DatabaseOptionConstants.DBOPTBIT_DECS_ENABLED),
  /** TRUE if a = 1;+ referenced shared template. Sticky bit once referenced. */
  IS_SHARED_TEMPLATE(DatabaseOptionConstants.DBOPTBIT_IS_SHARED_TEMPLATE),
  /** TRUE if database is a mailfile */
  IS_MAILFILE(DatabaseOptionConstants.DBOPTBIT_IS_MAILFILE),
  /** TRUE if database is a web application */
  IS_WEBAPPLICATION(DatabaseOptionConstants.DBOPTBIT_IS_WEBAPPLICATION),
  /** TRUE if the database should not be accessible via the standard URL syntax */
  HIDE_FROM_WEB(DatabaseOptionConstants.DBOPTBIT_HIDE_FROM_WEB),
  /** TRUE if database contains one or more enabled background agent */
  ENABLED_BGAGENT(DatabaseOptionConstants.DBOPTBIT_ENABLED_BGAGENT),
  /** database supports LZ1 compression. */
  LZ1(DatabaseOptionConstants.DBOPTBIT_LZ1),
  /** TRUE if database has default language */
  HTTP_DBHAS_DEFLANG(DatabaseOptionConstants.DBOPTBIT_HTTP_DBHAS_DEFLANG),
  /** TRUE if database design refresh is only on admin server */
  REFRESH_DESIGN_ON_ADMIN(DatabaseOptionConstants.DBOPTBIT_REFRESH_DESIGN_ON_ADMIN),
  /** TRUE if shared template should be actively used to merge in design. */
  ACTIVE_SHARED_TEMPLATE(DatabaseOptionConstants.DBOPTBIT_ACTIVE_SHARED_TEMPLATE),
  /** TRUE to allow the use of themes when displaying the application. */
  APPLY_THEMES(DatabaseOptionConstants.DBOPTBIT_APPLY_THEMES),
  /** TRUE if unread marks replicate */
  UNREAD_REPLICATION(DatabaseOptionConstants.DBOPTBIT_UNREAD_REPLICATION),

  /** TRUE if unread marks replicate out of the cluster */
  UNREAD_REP_OUT_OF_CLUSTER(DatabaseOptionConstants.DBOPTBIT_UNREAD_REP_OUT_OF_CLUSTER),

  /** TRUE, if the mail file is a migrated one from Exchange */
  IS_MIGRATED_EXCHANGE_MAILFILE(DatabaseOptionConstants.DBOPTBIT_IS_MIGRATED_EXCHANGE_MAILFILE),

  /** TRUE, if the mail file is a migrated one from Exchange */
  NEED_EX_NAMEFIXUP(DatabaseOptionConstants.DBOPTBIT_NEED_EX_NAMEFIXUP),

  /** TRUE, if out of office service is enabled in a mail file */
  OOS_ENABLED(DatabaseOptionConstants.DBOPTBIT_OOS_ENABLED),

  /** TRUE if Support Response Threads is enabled in database */
  SUPPORT_RESP_THREADS(DatabaseOptionConstants.DBOPTBIT_SUPPORT_RESP_THREADS),

  /**
   * TRUE if the database search is disabled. Give the admin a mechanism to
   * prevent db search in scenarios
   * where the db is very large, they don't want to create new views, and they
   * don't want a full text index
   */
  NO_SIMPLE_SEARCH(DatabaseOptionConstants.DBOPTBIT_NO_SIMPLE_SEARCH),

  /** TRUE if the database FDO is repaired to proper coalation function. */
  FDO_REPAIRED(DatabaseOptionConstants.DBOPTBIT_FDO_REPAIRED),

  /** TRUE if the policy settings have been removed from a db with no policies */
  POLICIES_REMOVED(DatabaseOptionConstants.DBOPTBIT_POLICIES_REMOVED),

  /** TRUE if Superblock is compressed. */
  COMPRESSED_SUPERBLOCK(DatabaseOptionConstants.DBOPTBIT_COMPRESSED_SUPERBLOCK),

  /** TRUE if design note non-summary should be compressed */
  COMPRESSED_DESIGN_NS(DatabaseOptionConstants.DBOPTBIT_COMPRESSED_DESIGN_NS),

  /** TRUE if the db has opted in to use DAOS */
  DAOS_ENABLED(DatabaseOptionConstants.DBOPTBIT_DAOS_ENABLED),

  /**
   * TRUE if all data documents in database should be compressed (compare with
   * DBOPTBIT_COMPRESSED_DESIGN_NS)
   */
  COMPRESSED_DATA_DOCS(DatabaseOptionConstants.DBOPTBIT_COMPRESSED_DATA_DOCS),

  /**
   * TRUE if views in this database should be skipped by server-side update task
   */
  DISABLE_AUTO_VIEW_UPDS(DatabaseOptionConstants.DBOPTBIT_DISABLE_AUTO_VIEW_UPDS),

  /**
   * if TRUE, Domino can suspend T/L check for DAOS items because the dbtarget is
   * expendable
   */
  DAOS_LOGGING_NOT_REQD(DatabaseOptionConstants.DBOPTBIT_DAOS_LOGGING_NOT_REQD),

  /** TRUE if exporting of view data is to be disabled */
  DISABLE_VIEW_EXPORT(DatabaseOptionConstants.DBOPTBIT_DISABLE_VIEW_EXPORT),

  /**
   * TRUE if database is a NAB which contains config information, groups, and
   * mailin databases but where users are stored externally.
   */
  USERLESS2_NAB(DatabaseOptionConstants.DBOPTBIT_USERLESS2_NAB),

  /** LLN2 specific, added to this codestream to reserve this value */
  ADVANCED_PROP_OVERRIDE(DatabaseOptionConstants.DBOPTBIT_ADVANCED_PROP_OVERRIDE),

  /** Turn off VerySoftDeletes for ODS51 */
  NO_VSD(DatabaseOptionConstants.DBOPTBIT_NO_VSD),

  /** NSF is to be used as a cache */
  LOCAL_CACHE(DatabaseOptionConstants.DBOPTBIT_LOCAL_CACHE),

  /**
   * Set to force next compact to be out of place. Initially done for ODS upgrade
   * of in use Dbs, but may have other uses down the road. The next compact will
   * clear this bit, it is transitory.
   */
  COMPACT_NO_INPLACE(DatabaseOptionConstants.DBOPTBIT_COMPACT_NO_INPLACE),
  /** from LLN2 */
  NEEDS_ZAP_LSN(DatabaseOptionConstants.DBOPTBIT_NEEDS_ZAP_LSN),

  /**
   * set to indicate this is a system db (eg NAB, mail.box, etc) so we don't rely
   * on the db name
   */
  IS_SYSTEM_DB(DatabaseOptionConstants.DBOPTBIT_IS_SYSTEM_DB),

  /** TRUE if the db has opted in to use PIRC */
  PIRC_ENABLED(DatabaseOptionConstants.DBOPTBIT_PIRC_ENABLED),

  /** from lln2 */
  DBMT_FORCE_FIXUP(DatabaseOptionConstants.DBOPTBIT_DBMT_FORCE_FIXUP),

  /**
   * TRUE if the db has likely a complete design replication - for PIRC control
   */
  DESIGN_REPLICATED(DatabaseOptionConstants.DBOPTBIT_DESIGN_REPLICATED),

  /**
   * on the = 1;-&gt;0 transition rename the file (for LLN2 keep in sync please)
   */
  MARKED_FOR_PENDING_DELETE(DatabaseOptionConstants.DBOPTBIT_MARKED_FOR_PENDING_DELETE),

  IS_NDX_DB(DatabaseOptionConstants.DBOPTBIT_IS_NDX_DB),

  /** move NIF containers &amp; collection objects out of nsf into .ndx db */
  SPLIT_NIF_DATA(DatabaseOptionConstants.DBOPTBIT_SPLIT_NIF_DATA),

  /** NIFNSF is off but not all containers have been moved out yet */
  NIFNSF_OFF(DatabaseOptionConstants.DBOPTBIT_NIFNSF_OFF),

  /** Inlined indexing exists for this DB */
  INLINE_INDEX(DatabaseOptionConstants.DBOPTBIT_INLINE_INDEX),

  /** db solr search enabled */
  SOLR_SEARCH(DatabaseOptionConstants.DBOPTBIT_SOLR_SEARCH),

  /** init solr index done */
  SOLR_SEARCH_INIT_DONE(DatabaseOptionConstants.DBOPTBIT_SOLR_SEARCH_INIT_DONE),

  /**
   * Folder sync enabled for database (sync Drafts, Sent and Trash views to IMAP
   * folders)
   */
  IMAP_FOLDERSYNC(DatabaseOptionConstants.DBOPTBIT_IMAP_FOLDERSYNC),

  /** Large Summary Support (LSS) */
  LARGE_BUCKETS_ENABLED(DatabaseOptionConstants.DBOPTBIT_LARGE_BUCKETS_ENABLED),

  /** Pair1 primary db mathced tds filled */
  SOLR_PAIR1PRIMARY(DatabaseOptionConstants.DBOPTBIT_SOLR_PAIR1PRIMARY),

  /** Pair2 init solr index done */
  SOLR_SEARCH_INIT_DONE2(DatabaseOptionConstants.DBOPTBIT_SOLR_SEARCH_INIT_DONE2),

  /** Pair1 secondary db mathced tds filled */
  SOLR_PAIR1SECONDARY(DatabaseOptionConstants.DBOPTBIT_SOLR_PAIR1SECONDARY),

  /** Pair2 primary db mathced tds filled */
  SOLR_PAIR2PRIMARY(DatabaseOptionConstants.DBOPTBIT_SOLR_PAIR2PRIMARY),

  /** Pair2 secondary db mathced tds filled */
  SOLR_PAIR2SECONDARY(DatabaseOptionConstants.DBOPTBIT_SOLR_PAIR2SECONDARY),

  /** ODS 54 + large buckets + compact -c */
  LARGE_ITEMS_ENABLED(DatabaseOptionConstants.DBOPTBIT_LARGE_ITEMS_ENABLED),

  DELETE_LOGGING(DatabaseOptionConstants.DBOPTBIT_DELETE_LOGGING);

  private static interface DatabaseOptionConstants {
    /** Enable full text indexing */
    int DBOPTBIT_FT_INDEX = 0;
    /**
     * TRUE if database is being used as an object store - for garbage collection
     */
    int DBOPTBIT_IS_OBJSTORE = 1;
    /**
     * TRUE if database has notes which refer to an object store - for garbage
     * collection
     */
    int DBOPTBIT_USES_OBJSTORE = 2;
    /** TRUE if NoteUpdate of notes in this db should never use an object store. */
    int DBOPTBIT_OBJSTORE_NEVER = 3;
    /** TRUE if database is a library */
    int DBOPTBIT_IS_LIBRARY = 4;
    /** TRUE if uniform access control across all replicas */
    int DBOPTBIT_UNIFORM_ACCESS = 5;
    /**
     * TRUE if NoteUpdate of notes in this db should always try to use an object
     * store.
     */
    int DBOPTBIT_OBJSTORE_ALWAYS = 6;
    /** TRUE if garbage collection is never to be done on this object store */
    int DBOPTBIT_COLLECT_NEVER = 7;
    /**
     * TRUE if this is a template and is considered an advanced one (for experts
     * only.)
     */
    int DBOPTBIT_ADV_TEMPLATE = 8;
    /** TRUE if db has no background agent */
    int DBOPTBIT_NO_BGAGENT = 9;
    /**
     * TRUE is db is out-of-service, no new opens allowed, unless
     * DBOPEN_IGNORE_OUTOFSERVICE is specified
     */
    int DBOPTBIT_OUT_OF_SERVICE = 10;
    /** TRUE if db is personal journal */
    int DBOPTBIT_IS_PERSONALJOURNAL = 11;
    /**
     * TRUE if db is marked for delete. no new opens allowed, cldbdir will delete
     * the database when ref count = = 0;
     */
    int DBOPTBIT_MARKED_FOR_DELETE = 12;
    /** TRUE if db stores calendar events */
    int DBOPTBIT_HAS_CALENDAR = 13;
    /** TRUE if db is a catalog index */
    int DBOPTBIT_IS_CATALOG_INDEX = 14;
    /** TRUE if db is an address book */
    int DBOPTBIT_IS_ADDRESS_BOOK = 15;
    /** TRUE if db is a "multi-db-search" repository */
    int DBOPTBIT_IS_SEARCH_SCOPE = 16;
    /**
     * TRUE if db's user activity log is confidential, only viewable by designer and
     * manager
     */
    int DBOPTBIT_IS_UA_CONFIDENTIAL = 17;
    /**
     * TRUE if item names are to be treated as if the ITEM_RARELY_USED_NAME flag is
     * set.
     */
    int DBOPTBIT_RARELY_USED_NAMES = 18;
    /** TRUE if db is a "multi-db-site" repository */
    int DBOPTBIT_IS_SITEDB = 19;

    /** TRUE if docs in folders in this db have folder references */
    int DBOPTBIT_FOLDER_REFERENCES = 20;

    /** TRUE if the database is a proxy for non-NSF data */
    int DBOPTBIT_IS_PROXY = 21;
    /** TRUE for NNTP server add-in dbs */
    int DBOPTBIT_IS_NNTP_SERVER_DB = 22;
    /**
     * TRUE if this is a replica of an IMAP proxy, enables certain * special cases
     * for interacting with db
     */
    int DBOPTBIT_IS_INET_REPL = 23;
    /** TRUE if db is a Lightweight NAB */
    int DBOPTBIT_IS_LIGHT_ADDRESS_BOOK = 24;
    /**
     * TRUE if database has notes which refer to an object store - for garbage
     * collection
     */
    int DBOPTBIT_ACTIVE_OBJSTORE = 25;
    /** TRUE if database is globally routed */
    int DBOPTBIT_GLOBALLY_ROUTED = 26;
    /** TRUE if database has mail autoprocessing enabled */
    int DBOPTBIT_CS_AUTOPROCESSING_ENABLED = 27;
    /** TRUE if database has mail filters enabled */
    int DBOPTBIT_MAIL_FILTERS_ENABLED = 28;
    /** TRUE if database holds subscriptions */
    int DBOPTBIT_IS_SUBSCRIPTIONDB = 29;
    /** TRUE if data base supports "check-in" "check-out" */
    int DBOPTBIT_IS_LOCK_DB = 30;
    /** TRUE if editor must lock notes to edit */
    int DBOPTBIT_IS_DESIGNLOCK_DB = 31;

    /* ODS26+ options */
    /** if TRUE, store all modified index blocks in lz1 compressed form */
    int DBOPTBIT_COMPRESS_INDEXES = 33;
    /** if TRUE, store all modified buckets in lz1 compressed form */
    int DBOPTBIT_COMPRESS_BUCKETS = 34;
    /**
     * FALSE by default, turned on forever if DBFLAG_COMPRESS_INDEXES or
     * DBFLAG_COMPRESS_BUCKETS are ever turned on.
     */
    int DBOPTBIT_POSSIBLY_COMPRESSED = 35;
    /** TRUE if freed space in db is not overwritten */
    int DBOPTBIT_NO_FREE_OVERWRITE = 36;
    /** DB doesn't maintain unread marks */
    int DBOPTBIT_NOUNREAD = 37;
    /** TRUE if the database does not maintain note hierarchy info. */
    int DBOPTBIT_NO_RESPONSE_INFO = 38;
    /** Disabling of response info will happen on next compaction */
    int DBOPTBIT_DISABLE_RSP_INFO_PEND = 39;
    /** Enabling of response info will happen on next compaction */
    int DBOPTBIT_ENABLE_RSP_INFO_PEND = 40;
    /** Form/Bucket bitmap optimization is enabled */
    int DBOPTBIT_FORM_BUCKET_OPT = 41;
    /** Disabling of Form/Bucket bitmap opt will happen on next compaction */
    int DBOPTBIT_DISABLE_FORMBKT_PEND = 42;
    /** Enabling of Form/Bucket bitmap opt will happen on next compaction */
    int DBOPTBIT_ENABLE_FORMBKT_PEND = 43;
    /** If TRUE, maintain LastAccessed */
    int DBOPTBIT_MAINTAIN_LAST_ACCESSED = 44;
    /** If TRUE, transaction logging is disabled for this database */
    int DBOPTBIT_DISABLE_TXN_LOGGING = 45;
    /** If TRUE, monitors can't be used against this database (non-replicating) */
    int DBOPTBIT_MONITORS_NOT_ALLOWED = 46;
    /** If TRUE, all transactions on this database are nested top actions */
    int DBOPTBIT_NTA_ALWAYS = 47;
    /** If TRUE, objects are not to be logged */
    int DBOPTBIT_DONTLOGOBJECTS = 48;
    /**
     * If set, the default delete is soft. Can be overwritten by UPDATE_DELETE_HARD
     */
    int DBOPTBIT_DELETES_ARE_SOFT = 49;

    /* The following bits are used by the webserver and are gotten from the icon note */
    /** if TRUE, the Db needs to be opened using SSL over HTTP */
    int DBOPTBIT_HTTP_DBIS_SSL = 50;
    /**
     * if TRUE, the Db needs to use JavaScript to render the HTML for formulas,
     * buttons, etc
     */
    int DBOPTBIT_HTTP_DBIS_JS = 51;
    /** if TRUE, there is a $DefaultLanguage value on the $icon note */
    int DBOPTBIT_HTTP_DBIS_MULTILANG = 52;

    /* ODS37+ options */
    /** if TRUE, database is a mail.box (ODS37 and up) */
    int DBOPTBIT_IS_MAILBOX = 53;
    /** if TRUE, database is allowed to have /gt;64KB UNK table */
    int DBOPTBIT_LARGE_UNKTABLE = 54;
    /** If TRUE, full-text index is accent sensitive */
    int DBOPTBIT_ACCENT_SENSITIVE_FT = 55;
    /** TRUE if database has NSF support for IMAP enabled */
    int DBOPTBIT_IMAP_ENABLED = 56;
    /** TRUE if database is a USERless N&amp;A Book */
    int DBOPTBIT_USERLESS_NAB = 57;
    /** TRUE if extended ACL's apply to this Db */
    int DBOPTBIT_EXTENDED_ACL = 58;
    /** TRUE if connections to = 3;rd party DBs are allowed */
    int DBOPTBIT_DECS_ENABLED = 59;
    /** TRUE if a = 1;+ referenced shared template. Sticky bit once referenced. */
    int DBOPTBIT_IS_SHARED_TEMPLATE = 60;
    /** TRUE if database is a mailfile */
    int DBOPTBIT_IS_MAILFILE = 61;
    /** TRUE if database is a web application */
    int DBOPTBIT_IS_WEBAPPLICATION = 62;
    /** TRUE if the database should not be accessible via the standard URL syntax */
    int DBOPTBIT_HIDE_FROM_WEB = 63;
    /** TRUE if database contains one or more enabled background agent */
    int DBOPTBIT_ENABLED_BGAGENT = 64;
    /** database supports LZ1 compression. */
    int DBOPTBIT_LZ1 = 65;
    /** TRUE if database has default language */
    int DBOPTBIT_HTTP_DBHAS_DEFLANG = 66;
    /** TRUE if database design refresh is only on admin server */
    int DBOPTBIT_REFRESH_DESIGN_ON_ADMIN = 67;
    /** TRUE if shared template should be actively used to merge in design. */
    int DBOPTBIT_ACTIVE_SHARED_TEMPLATE = 68;
    /** TRUE to allow the use of themes when displaying the application. */
    int DBOPTBIT_APPLY_THEMES = 69;
    /** TRUE if unread marks replicate */
    int DBOPTBIT_UNREAD_REPLICATION = 70;
    /** TRUE if unread marks replicate out of the cluster */
    int DBOPTBIT_UNREAD_REP_OUT_OF_CLUSTER = 71;
    /** TRUE, if the mail file is a migrated one from Exchange */
    int DBOPTBIT_IS_MIGRATED_EXCHANGE_MAILFILE = 72;
    /** TRUE, if the mail file is a migrated one from Exchange */
    int DBOPTBIT_NEED_EX_NAMEFIXUP = 73;
    /** TRUE, if out of office service is enabled in a mail file */
    int DBOPTBIT_OOS_ENABLED = 74;
    /** TRUE if Support Response Threads is enabled in database */
    int DBOPTBIT_SUPPORT_RESP_THREADS = 75;
    /**
     * TRUE if the database search is disabled<br>
     * LI = 4463;.02. give the admin a mechanism to prevent db search in scenarios
     * where the db is very large, they don't want to create new views, and they
     * don't want a full text index
     */
    int DBOPTBIT_NO_SIMPLE_SEARCH = 76;
    /** TRUE if the database FDO is repaired to proper coalation function. */
    int DBOPTBIT_FDO_REPAIRED = 77;
    /** TRUE if the policy settings have been removed from a db with no policies */
    int DBOPTBIT_POLICIES_REMOVED = 78;
    /** TRUE if Superblock is compressed. */
    int DBOPTBIT_COMPRESSED_SUPERBLOCK = 79;
    /** TRUE if design note non-summary should be compressed */
    int DBOPTBIT_COMPRESSED_DESIGN_NS = 80;
    /** TRUE if the db has opted in to use DAOS */
    int DBOPTBIT_DAOS_ENABLED = 81;

    /**
     * TRUE if all data documents in database should be compressed (compare with
     * DBOPTBIT_COMPRESSED_DESIGN_NS)
     */
    int DBOPTBIT_COMPRESSED_DATA_DOCS = 82;
    /**
     * TRUE if views in this database should be skipped by server-side update task
     */
    int DBOPTBIT_DISABLE_AUTO_VIEW_UPDS = 83;
    /**
     * if TRUE, Domino can suspend T/L check for DAOS items because the dbtarget is
     * expendable
     */
    int DBOPTBIT_DAOS_LOGGING_NOT_REQD = 84;
    /** TRUE if exporting of view data is to be disabled */
    int DBOPTBIT_DISABLE_VIEW_EXPORT = 85;
    /**
     * TRUE if database is a NAB which contains config information, groups, and
     * mailin databases but where users are stored externally.
     */
    int DBOPTBIT_USERLESS2_NAB = 86;
    /** LLN2 specific, added to this codestream to reserve this value */
    int DBOPTBIT_ADVANCED_PROP_OVERRIDE = 87;
    /** Turn off VerySoftDeletes for ODS51 */
    int DBOPTBIT_NO_VSD = 88;
    /** NSF is to be used as a cache */
    int DBOPTBIT_LOCAL_CACHE = 89;
    /**
     * Set to force next compact to be out of place. Initially done for ODS upgrade
     * of in use Dbs, but may have other uses down the road. The next compact will
     * clear this bit, it is transitory.
     */
    int DBOPTBIT_COMPACT_NO_INPLACE = 90;
    /** from LLN2 */
    int DBOPTBIT_NEEDS_ZAP_LSN = 91;
    /**
     * set to indicate this is a system db (eg NAB, mail.box, etc) so we don't rely
     * on the db name
     */
    int DBOPTBIT_IS_SYSTEM_DB = 92;
    /** TRUE if the db has opted in to use PIRC */
    int DBOPTBIT_PIRC_ENABLED = 93;
    /** from lln2 */
    int DBOPTBIT_DBMT_FORCE_FIXUP = 94;
    /**
     * TRUE if the db has likely a complete design replication - for PIRC control
     */
    int DBOPTBIT_DESIGN_REPLICATED = 95;
    /**
     * on the = 1;-&gt;0 transition rename the file (for LLN2 keep in sync please)
     */
    int DBOPTBIT_MARKED_FOR_PENDING_DELETE = 96;
    int DBOPTBIT_IS_NDX_DB = 97;
    /** move NIF containers &amp; collection objects out of nsf into .ndx db */
    int DBOPTBIT_SPLIT_NIF_DATA = 98;
    /** NIFNSF is off but not all containers have been moved out yet */
    int DBOPTBIT_NIFNSF_OFF = 99;
    /** Inlined indexing exists for this DB */
    int DBOPTBIT_INLINE_INDEX = 100;
    /** db solr search enabled */
    int DBOPTBIT_SOLR_SEARCH = 101;
    /** init solr index done */
    int DBOPTBIT_SOLR_SEARCH_INIT_DONE = 102;
    /**
     * Folder sync enabled for database (sync Drafts, Sent and Trash views to IMAP
     * folders)
     */
    int DBOPTBIT_IMAP_FOLDERSYNC = 103;
    /** Large Summary Support (LSS) */
    int DBOPTBIT_LARGE_BUCKETS_ENABLED = 104;
    /** Pair1 primary db mathced tds filled */
    int DBOPTBIT_SOLR_PAIR1PRIMARY = 105;
    /** Pair2 init solr index done */
    int DBOPTBIT_SOLR_SEARCH_INIT_DONE2 = 106;
    /** Pair1 secondary db mathced tds filled */
    int DBOPTBIT_SOLR_PAIR1SECONDARY = 107;
    /** Pair2 primary db mathced tds filled */
    int DBOPTBIT_SOLR_PAIR2PRIMARY = 108;
    /** Pair2 secondary db mathced tds filled */
    int DBOPTBIT_SOLR_PAIR2SECONDARY = 109;
    /** ODS 54 + large buckets + compact -c */
    int DBOPTBIT_LARGE_ITEMS_ENABLED = 110;
    int DBOPTBIT_DELETE_LOGGING = 111;
  }

  private Integer m_val;

  DatabaseOption(final int val) {
    this.m_val = val;
  }

  @Override
  public long getLongValue() {
    return this.m_val.longValue();
  }

  @Override
  public Integer getValue() {
    return this.m_val;
  }

}
