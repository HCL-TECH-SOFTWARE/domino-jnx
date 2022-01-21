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
package com.hcl.domino.misc;

import com.hcl.domino.constants.EditOds;
import com.hcl.domino.constants.OleOds;
import com.hcl.domino.constants.QueryOds;
import com.hcl.domino.constants.StdNames;

public interface NotesConstants extends ViewFormatConstants, StdNames, QueryOds, EditOds, OleOds {

  public enum AgentCheck {
    CheckRights(0),
    CheckAndSign(1);

    private final int m_value;

    AgentCheck(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum DESIGN_COMPONENT_ATTR {
    VALS_UNORDERED(0),
    VALS_ASCENDING(1),
    VALS_DESCENDING(2);

    private final int m_value;

    DESIGN_COMPONENT_ATTR(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum DesignUpdateReset {
    NoChange(0),
    TurnOffPDU(1),
    TurnOnPDU(2);

    private final int m_value;

    DesignUpdateReset(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum EmptyFieldMeans {
    EmptyMeansAll(0),
    EmptyMeansNone(1);

    private final int m_value;

    EmptyFieldMeans(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  /*	Defines for Authentication flags */

  public enum EProvidingAuthor {
    AuthorIsProvided(0),
    AuthorIsNotProvided(1);

    private final int m_value;

    EProvidingAuthor(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum EUsePABsAdminServer {
    UsePABsAdminServer(0),
    DontUsePABsAdminServer(1);

    private final int m_value;

    EUsePABsAdminServer(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum QUEP_LISTTYPE {
    INPUT_RESULTS_LST(0),
    SORT_COL_LST(1),
    COMBINES_LST(2),
    FIELD_FORMULA_LST(3), BAD_LISTTYPE(4);

    private final int m_value;

    QUEP_LISTTYPE(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum TypeOfLookup {
    AdminpServerAdmin(0),
    AdminpCreateReplica(1),
    AdminpCreateDatabase(2),
    AdminpAllowAccess(3),
    AdminpDenyAccess(4),
    AdminpPrivateList(5),
    AdminpRestrictedList(6);

    private final int m_value;

    TypeOfLookup(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  public enum TypeOfProcessing {
    NoDatabasesFound(0),
    NoDatabasesModified(1),
    DatabasesModified(2);

    private final int m_value;

    TypeOfProcessing(final int value) {
      this.m_value = value;
    }

    public int getValue() {
      return this.m_value;
    }
  }

  int queueEntryHeaderSize = 8; // DHANDLE/DHANDLE with 32 bit
  int resultsStreamBufferHeaderSize = 12;
  short ERR_MASK = 0x3fff;

  /** error came from remote machine */
  short STS_REMOTE = 0x4000;
  /** Set if names list has been authenticated via Notes */
  short NAMES_LIST_AUTHENTICATED = 0x0001;
  /**
   * Set if names list has been authenticated using external password -- Triggers
   * "maximum password access allowed" feature
   */
  short NAMES_LIST_PASSWORD_AUTHENTICATED = 0x0002;
  /** Set if user requested full admin access and it was granted */
  short NAMES_LIST_FULL_ADMIN_ACCESS = 0x0004;

  short OS_TRANSLATE_NATIVE_TO_LMBCS = 0; /* Translate platform-specific to LMBCS */
  short OS_TRANSLATE_LMBCS_TO_NATIVE = 1; /* Translate LMBCS to platform-specific */
  short OS_TRANSLATE_LOWER_TO_UPPER = 3; /* current int'l case table */
  short OS_TRANSLATE_UPPER_TO_LOWER = 4; /* current int'l case table */
  short OS_TRANSLATE_UNACCENT = 5; /* int'l unaccenting table */

  short OS_TRANSLATE_LMBCS_TO_UNICODE = 20;
  short OS_TRANSLATE_LMBCS_TO_UTF8 = 22;

  short OS_TRANSLATE_UNICODE_TO_LMBCS = 23;
  short OS_TRANSLATE_UTF8_TO_LMBCS = 24;
  int MAXSPRINTF = 256;

  int MAXPATH = 256;
  short MAXUSERNAME = 256; /* Maximum user name */
  int MAXDOMAINNAME = 32;
  int FILETITLEMAX = 97;

  int MAXCOUNTRYNAME = 3;
  int MAXORGNAME = 65;
  int MAXORGUNITNAME =33;
  int MAXLDAPBASE = (MAXORGUNITNAME*4 + MAXORGNAME + MAXCOUNTRYNAME + 22);
  // TODO needs to be checked
  int MAXUSERPASSWORD = 64;
  short MAXENVVALUE = 256;
  int ALLDAY = 0xffffffff;
  int ANYDAY = 0xffffffff;
  short TIMEDATE_MINIMUM = 0;
  short TIMEDATE_MAXIMUM = 1;
  short TIMEDATE_WILDCARD = 2;
  /* year, month, and day */
  byte TDFMT_FULL = 0;
  /* month and day, year if not this year */
  byte TDFMT_CPARTIAL = 1;

  /* month and day */
  byte TDFMT_PARTIAL = 2;
  /* year and month */
  byte TDFMT_DPARTIAL = 3;
  /* year(4digit), month, and day */
  byte TDFMT_FULL4 = 4;

  /* month and day, year(4digit) if not this year */
  byte TDFMT_CPARTIAL4 = 5;
  /* year(4digit) and month */
  byte TDFMT_DPARTIAL4 = 6;
  /* hour, minute, and second */
  byte TTFMT_FULL = 0;
  /* hour and minute */
  byte TTFMT_PARTIAL = 1;
  /* hour */
  byte TTFMT_HOUR = 2;

  // lengths of strings in INTLFORMAT

  /* hour, minute, second, hundredths (max resolution). This currently works only for time-to-text conversion! */
  byte TTFMT_FULL_MAX = 3;
  /* all times converted to THIS zone*/
  byte TZFMT_NEVER = 0;

  /* show only when outside this zone */
  byte TZFMT_SOMETIMES = 1;

  /*	International Environment Parameter Definitions */

  /* show on all times, regardless */
  byte TZFMT_ALWAYS = 2;
  /* DATE */
  byte TSFMT_DATE = 0;
  /* TIME */
  byte TSFMT_TIME = 1;
  /* DATE TIME */
  byte TSFMT_DATETIME = 2;
  /* DATE TIME or TIME Today or TIME Yesterday */
  byte TSFMT_CDATETIME = 3;
  /* DATE, Today or Yesterday */
  byte TSFMT_CDATE = 4;
  int ISTRMAX = 5;
  int YTSTRMAX = 32;
  short MAXALPHATIMEDATE = 80;
  short CURRENCY_SUFFIX = 0x0001;
  short CURRENCY_SPACE = 0x0002;

  /*	NSF File Information Buffer size.  This buffer is defined to contain
  Text (host format) that is NULL-TERMINATED.  This is the ONLY null-terminated
  field in all of NSF. */

  short NUMBER_LEADING_ZERO = 0x0004;

  /*	Define argument to NSFDbInfoParse/Modify to manipulate components from DbInfo */

  short CLOCK_24_HOUR = 0x0008;
  short DAYLIGHT_SAVINGS = 0x0010;
  short DATE_MDY = 0x0020;
  short DATE_DMY = 0x0040;

  short DATE_YMD = 0x0080;
  short DATE_4DIGIT_YEAR = 0x0100;
  short TIME_AMPM_PREFIX = 0x0400;

  /*	Define NSF Special Note ID Indices.  The first 16 of these are reserved
  for "default notes" in each of the 16 note classes.  In order to access
  these, use SPECIAL_ID_NOTE+NOTE_CLASS_XXX.  This is generally used
  when calling NSFDbGetSpecialNoteID. NOTE: NSFNoteOpen, NSFDbReadObject
  and NSFDbWriteObject support reading special notes or objects directly
  (without calling NSFDbGetSpecialNoteID).  They use a DIFFERENT flag
  with a similar name: NOTE_ID_SPECIAL (see nsfnote.h).  Remember this
  rule:

  SPECIAL_ID_NOTE is a 16 bit mask and is used as a NoteClass argument.
  NOTE_ID_SPECIAL is a 32 bit mask and is used as a NoteID or RRV argument.
  */

  short DATE_ABBREV = 0x0800;

  int NSF_INFO_SIZE = 128;
  /** database title */
  short INFOPARSE_TITLE = 0;
  /** database categories */
  short INFOPARSE_CATEGORIES = 1;
  /** template name (for a design template database) */
  short INFOPARSE_CLASS = 2;
  /**
   * inherited template name (for a database that inherited its design from a
   * design template)
   */
  short INFOPARSE_DESIGN_CLASS = 3;
  int MAXDWORD = 0xffffffff;
  short MAXWORD = (short) (0xffff & 0xffff);
  int MAXWORDAsInt = NotesConstants.MAXWORD & 0xffff;
  short SPECIAL_ID_NOTE = (short) (0x8000 & 0xffff); /* use in combination w/NOTE_CLASS when calling NSFDbGetSpecialNoteID */
  /** document note */
  short NOTE_CLASS_DOCUMENT = 0x0001;
  /** old name for document note */
  short NOTE_CLASS_DATA = NotesConstants.NOTE_CLASS_DOCUMENT;
  /** notefile info (help-about) note */
  short NOTE_CLASS_INFO = 0x0002;
  /** form note */
  short NOTE_CLASS_FORM = 0x0004;
  /** view note */
  short NOTE_CLASS_VIEW = 0x0008;
  /** icon note */
  short NOTE_CLASS_ICON = 0x0010;
  /** design note collection */
  short NOTE_CLASS_DESIGN = 0x0020;
  /** acl note */
  short NOTE_CLASS_ACL = 0x0040;
  /** Notes product help index note */
  short NOTE_CLASS_HELP_INDEX = 0x0080;
  /** designer's help note */
  short NOTE_CLASS_HELP = 0x0100;

  /** filter note */
  short NOTE_CLASS_FILTER = 0x0200;

  /** field note */
  short NOTE_CLASS_FIELD = 0x0400;
  /** replication formula */
  short NOTE_CLASS_REPLFORMULA = 0x0800;

  /** Private design note, use $PrivateDesign view to locate/classify */
  short NOTE_CLASS_PRIVATE = 0x1000;
  /** MODIFIER - default version of each */
  short NOTE_CLASS_DEFAULT = (short) (0x8000 & 0xffff);
  /** see {@link #SEARCH_NOTIFYDELETIONS} */
  short NOTE_CLASS_NOTIFYDELETION = NotesConstants.NOTE_CLASS_DEFAULT;
  /** all note types */
  short NOTE_CLASS_ALL = 0x7fff;
  /** all non-data notes */
  short NOTE_CLASS_ALLNONDATA = 0x7ffe;
  /** no notes */
  short NOTE_CLASS_NONE = 0x0000;
  /** Define symbol for those note classes that allow only one such in a file */
  short NOTE_CLASS_SINGLE_INSTANCE = NotesConstants.NOTE_CLASS_DESIGN |
      NotesConstants.NOTE_CLASS_ACL |
      NotesConstants.NOTE_CLASS_INFO |
      NotesConstants.NOTE_CLASS_ICON |
      NotesConstants.NOTE_CLASS_HELP_INDEX |
      0;
  short IDTABLE_MODIFIED = 0x0001; /* modified - set by Insert/Delete */

  /* 	Note structure member IDs for NSFNoteGet and SetInfo. */

  /* and can be cleared by caller if desired */
  short IDTABLE_INVERTED = 0x0002; /* sense of list inverted */
  /* (reserved for use by caller only) */
  short FR_RUN_ALL = 0x1000;
  short FR_RUN_CLEANUPSCRIPT_ONLY = 0x1;
  short FR_RUN_NSD_ONLY = 0x2;
  short FR_DONT_RUN_ANYTHING = 0x4;
  short FR_SHUTDOWN_HANG = 0x8;
  short FR_PANIC_DIRECT = 0x10;
  short FR_RUN_QOS_NSD = 0x20;
  short FR_NSD_AUTOMONITOR = 0x40;
  /** IDs for NSFNoteGet and SetInfo */
  short _NOTE_DB = 0;
  /** (When adding new values, see the table in NTINFO.C */
  short _NOTE_ID = 1;
  /** Get/set the Originator ID (OID). */
  short _NOTE_OID = 2;
  /** Get/set the NOTE_CLASS (WORD). */
  short _NOTE_CLASS = 3;

  /** Get/set the Modified in this file time/date (TIMEDATE : GMT normalized). */
  short _NOTE_MODIFIED = 4;

  /** For pre-V3 compatibility. Should use $Readers item */
  short _NOTE_PRIVILEGES = 5;
  /** Get/set the note flags (WORD). See NOTE_FLAG_xxx. */
  short _NOTE_FLAGS = 7;

  /** Get/set the Accessed in this file date (TIMEDATE). */
  short _NOTE_ACCESSED = 8;

  /** For response hierarchy */
  short _NOTE_PARENT_NOTEID = 10;
  /** For response hierarchy */
  short _NOTE_RESPONSE_COUNT = 11;
  /** For response hierarchy */
  short _NOTE_RESPONSES = 12;
  /** For AddedToFile time */
  short _NOTE_ADDED_TO_FILE = 13;

  /** DBHANDLE of object store used by linked items */
  short _NOTE_OBJSTORE_DB = 14;

  short _NOTE_FLAGS2 = 16;
  /** Flag to indicate unique keys. */
  byte COLLATION_FLAG_UNIQUE = 0x01;
  /** Flag to indicate only build demand. */
  byte COLLATION_FLAG_BUILD_ON_DEMAND = 0x02;
  byte COLLATION_SIGNATURE = 0x44;
  /** Collate by key in summary buffer (requires key name string) */
  byte COLLATE_TYPE_KEY = 0;
  /** Collate by note ID */
  byte COLLATE_TYPE_NOTEID = 3;
  /** Collate by "tumbler" summary key (requires key name string) */
  byte COLLATE_TYPE_TUMBLER = 6;
  /** Collate by "category" summary key (requires key name string) */
  byte COLLATE_TYPE_CATEGORY = 7;
  byte COLLATE_TYPE_MAX = 7;
  /** True if descending */
  byte CDF_S_descending = 0;

  /** False if ascending order (default) */
  byte CDF_M_descending = 0x01;

  /** Obsolete - see new constant below */
  byte CDF_M_caseinsensitive = 0x02;
  /** If prefix list, then ignore for sorting */
  byte CDF_M_ignoreprefixes = 0x02;
  /** Obsolete - see new constant below */
  byte CDF_M_accentinsensitive = 0x04;
  /** If set, lists are permuted */
  byte CDF_M_permuted = 0x08;
  /**
   * Qualifier if lists are permuted; if set, lists are pairwise permuted,
   * otherwise lists are multiply permuted.
   */
  byte CDF_M_permuted_pairwise = 0x10;

  /** If set, treat as permuted */
  byte CDF_M_flat_in_v5 = 0x20;
  /** If set, text compares are case-sensitive */
  byte CDF_M_casesensitive_in_v5 = 0x40;
  /** If set, text compares are accent-sensitive */
  byte CDF_M_accentsensitive_in_v5 = (byte) (0x80 & 0xff);
  byte COLLATE_DESCRIPTOR_SIGNATURE = 0x66;

  /*	Object Types, a sub-category of TYPE_OBJECT */

  /** Color space is RGB */
  short COLOR_VALUE_FLAGS_ISRGB = 0x0001;
  /** This object has no color */
  short COLOR_VALUE_FLAGS_NOCOLOR = 0x0004;
  /** Use system default color, ignore color here */
  short COLOR_VALUE_FLAGS_SYSTEMCOLOR = 0x0008;
  /** This color has a gradient color that follows */
  short COLOR_VALUE_FLAGS_HASGRADIENT = 0x0010;

  /** upper 4 bits are reserved for application specific use */
  short COLOR_VALUE_FLAGS_APPLICATION_MASK = (short) (0xf000 & 0xffff);
  /** Defined for Yellow Highlighting, (not reserved). */
  short COLOR_VALUE_FLAGS_RESERVED1 = (short) (0x8000 & 0xffff);
  /** Defined for Pink Highlighting, (not reserved). */
  short COLOR_VALUE_FLAGS_RESERVED2 = 0x4000;
  /** Defined for Blue Highlighting, (not reserved). */
  short COLOR_VALUE_FLAGS_RESERVED3 = 0x2000;

  /** Reserved. */
  short COLOR_VALUE_FLAGS_RESERVED4 = 0x1000;

  /** File Attachment */
  short OBJECT_FILE = 0;
  /** IDTable of "done" docs attached to filter */
  short OBJECT_FILTER_LEFTTODO = 3;
  /** Assistant run data object */
  short OBJECT_ASSIST_RUNDATA = 8;
  /** Used as input to NSFDbGetObjectSize */
  short OBJECT_UNKNOWN = (short) (0xffff & 0xffff);
  /** file object has object digest appended */
  short FILEFLAG_SIGN = 0x0001;

  // The mime part type cPartType within the MIME_PART structure.

  /** file is represented by an editor run in the document */
  short FILEFLAG_INDOC = 0x0002;
  /** file object has mime data appended */
  short FILEFLAG_MIME = 0x0004;
  /** file is a folder automaticly compressed by Notes */
  short FILEFLAG_AUTOCOMPRESSED = 0x0080;
  short MIME_PART_VERSION = 2;
  /** Mime part has boundary. */
  int MIME_PART_HAS_BOUNDARY = 0x00000001;

  /*	Item Flags */
  // These flags define the characteristics of an item (field) in a note. The
  // flags may be bitwise or'ed together for combined functionality.

  /** Mime part has headers. */
  int MIME_PART_HAS_HEADERS = 0x00000002;

  /** Mime part has body in database object. */
  int MIME_PART_BODY_IN_DBOBJECT = 0x00000004;

  /**
   * Mime part has shared database object. Used only with
   * MIME_PART_BODY_IN_DBOBJECT.
   */
  int MIME_PART_SHARED_DBOBJECT = 0x00000008; /*	Used only with MIME_PART_BODY_IN_DBOBJECT. */

  /** Skip for conversion. */
  int MIME_PART_SKIP_FOR_CONVERSION = 0x00000010; /* only used during MIME->CD conversion */

  /** Mime part type is a prolog. */
  byte MIME_PART_PROLOG = 1;

  /** Mime part type is a body. */
  byte MIME_PART_BODY = 2;

  /** Mime part type is a epilog. */
  byte MIME_PART_EPILOG = 3;

  /** Mime part type is retrieve information. */
  byte MIME_PART_RETRIEVE_INFO = 4;

  /** Mime part type is a message. */
  byte MIME_PART_MESSAGE = 5;

  /** This item is signed. */
  short ITEM_SIGN = 0x0001;

  /*	Flags returned (beginning in V3) in the _NOTE_FLAGS */

  /**
   * This item is sealed. When used in NSFItemAppend, the item is encryption
   * enabled; it can later be encrypted if edited from the Notes UI and saved
   * in a form that specifies Encryption.
   */
  short ITEM_SEAL = 0x0002;
  /**
   * This item is stored in the note's summary buffer. Summary items may be used
   * in view columns, selection formulas, and @-functions. Summary items may be
   * accessed via the SEARCH_MATCH structure provided by NSFSearch or in the
   * buffer returned by NIFReadEntries. API program may read, modify, and write
   * items in the summary buffer without opening the note first. The maximum size
   * of the summary buffer is 32K. Items of TYPE_COMPOSITE may not have the
   * ITEM_SUMMARY flag set.
   */
  short ITEM_SUMMARY = 0x0004;
  /**
   * This item is an Author Names field as indicated by the READ/WRITE-ACCESS
   * flag. Item is TYPE_TEXT or TYPE_TEXT_LIST. Author Names fields have the
   * ITEM_READWRITERS flag or'd with the ITEM_NAMES flag.
   */
  short ITEM_READWRITERS = 0x0020;
  /**
   * This item is a Names field. Indicated by the NAMES (distinguished names)
   * flag. Item is TYPE_TEXT or TYPE_TEXT_LIST.
   */
  short ITEM_NAMES = 0x0040;
  /**
   * Item will not be written to disk
   */
  short ITEM_NOUPDATE = 0x0080;

  /**
   * This item is a placeholder field in a form note. Item is
   * TYPE_INVALID_OR_UNKNOWN.
   */
  short ITEM_PLACEHOLDER = 0x0100;

  /** A user requires editor access to change this field. */
  short ITEM_PROTECTED = 0x0200;

  /**
   * This is a Reader Names field. Indicated by the READER-ACCESS flag. Item is
   * TYPE_TEXT or TYPE_TEXT_LIST.
   */
  short ITEM_READERS = 0x0400;
  /** Item is same as on-disk. */
  short ITEM_UNCHANGED = 0x1000;

  /** TRUE if document cannot be updated */
  short NOTE_FLAG_READONLY = 0x0001;

  /** missing some data */
  short NOTE_FLAG_ABSTRACTED = 0x0002;

  /** Incremental note (place holders) */
  short NOTE_FLAG_INCREMENTAL = 0x0004;

  /** Note contains linked items or linked objects */
  short NOTE_FLAG_LINKED = 0x0020;

  /**
   * Incremental type note Fully opened (NO place holders)
   * This type of note is meant to retain the
   * Item sequence numbers
   */
  short NOTE_FLAG_INCREMENTAL_FULL = 0x0040;

  /** Ghost entries do not appear in any views or searches */
  short NOTE_FLAG_GHOST = 0x200;

  /** Note is (opened) in canonical form */
  short NOTE_FLAG_CANONICAL = 0x4000;

  /** display only views and folder; version filtering */
  String DFLAGPAT_VIEWS_AND_FOLDERS = "-G40n^"; //$NON-NLS-1$

  /** display only views and folder; all notes &amp; web */
  String DFLAGPAT_VIEWS_AND_FOLDERS_DESIGN = "-G40^"; //$NON-NLS-1$

  /** display only folders; version filtering, ignore hidden notes */
  String DFLAGPAT_FOLDER_DESIGN = "(+-04*F"; //$NON-NLS-1$

  /** display only views, ignore hidden from notes */
  String DFLAGPAT_VIEW_DESIGN = "-FG40^"; //$NON-NLS-1$
  /** display things that are runnable; version filtering */
  String DFLAGPAT_TOOLSRUNMACRO = "-QXMBESIst5nmz{"; //$NON-NLS-1$
  /**
   * display things that show up in agents list. No version filtering (for design)
   */
  String DFLAGPAT_AGENTSLIST = "-QXstmz{"; //$NON-NLS-1$
  /** display only folders; no version filtering (for design) */
  String DFLAGPAT_FOLDER_ALL_VERSIONS = "*F"; //$NON-NLS-1$

  /** display only views (not folders, navigators or shared columns) */
  String DFLAGPAT_VIEW_ALL_VERSIONS = "-FG^"; //$NON-NLS-1$

  /** display only GraphicViews; all notes &amp; web navs */
  String DFLAGPAT_VIEWMAP_DESIGN = "(+-04*G"; //$NON-NLS-1$

  /** SiteMap notes (actually, "mQ345") */
  String DFLAGPAT_SITEMAP = "+m"; //$NON-NLS-1$
  /** display only database level script */
  String DFLAGPAT_DATABASESCRIPT = "+t"; //$NON-NLS-1$
  /** display only database global script libraries */
  String DFLAGPAT_SCRIPTLIB = "+sh."; //$NON-NLS-1$

  /** display only database global LotusScript script libraries */
  String DFLAGPAT_SCRIPTLIB_LS = "(+s-jh.*"; //$NON-NLS-1$

  /** display only database global Java script libraries */
  String DFLAGPAT_SCRIPTLIB_JAVA = "*sj"; //$NON-NLS-1$

  /*	Please keep these flags in alphabetic order (based on the flag itself) so that
  we can easily tell which flags to use next. Note that some of these flags apply
  to a particular NOTE_CLASS; others apply to all design elements. The comments
  indicate which is which. In theory, flags that apply to two different NOTE_CLASSes
  could overlap, but for now, try to make each flag unique. */

  /** display only database global Javascript script libraries */
  String DFLAGPAT_SCRIPTLIB_JS = "+h"; //$NON-NLS-1$
  /** display only database global JS server side script libraries */
  String DFLAGPAT_SCRIPTLIB_SERVER_JS = "+."; //$NON-NLS-1$
  /** display only shared data connection resources */
  String DFLAGPAT_DATA_CONNECTION_RESOURCE = "+k"; //$NON-NLS-1$
  /**
   * display things editable with dialog box; no version filtering (for design)
   */
  String DFLAGPAT_FORM_ALL_VERSIONS = "-FQMUGXWy#i:|@K;g~%z^}"; //$NON-NLS-1$
  /** display only files */
  String DFLAGPAT_FILE = "+g-K[];`,"; //$NON-NLS-1$
  /** list of files that should show in file DL */
  String DFLAGPAT_FILE_DL = "(+g-~K[];`,*"; //$NON-NLS-1$
  /** display only html files */
  String DFLAGPAT_HTMLFILES = "(+-*g>"; //$NON-NLS-1$
  /**
   * Synthesized, non-official pattern to select file resources but not Components
   */
  String DFLAGPAT_FILE_RESOURCE = "(+g-~K[];`,_*"; //$NON-NLS-1$
  String DESIGN_FLAGS = "$Flags"; //$NON-NLS-1$
  
  /** an extended flags field ($FlagsExt) that is in the design collection.  $Flags is just
  so full that we need some wiggle room!! */
  String DESIGN_FLAGS_EXTENDED = "$FlagsExt"; //$NON-NLS-1$
  
  /** for web apps, this file is ready for primetime
  for DESIGN_FLAG_CUSTOMELT this indicates desing element can be fetched via URL */
  String DESIGN_FLAGEXT_FILE_DEPLOYABLE = "D"; //$NON-NLS-1$
  /** for web apps, this file should not be replaced on redeploy */
  String DESIGN_FLAGEXT_DONTREFRESH_ON_REDEPLOY = "R"; //$NON-NLS-1$
  /** for WebDAV resources.  The note has a dead properties in the $DavProperties field */
  String DESIGN_FLAGEXT_NOTE_HAS_DAVPROPERTIES = "P"; //$NON-NLS-1$
  /** for WebDAV: indicates that certain MS properties are among the dead properites */
  String DESIGN_FLAGEXT_NOTE_HAS_MSPROPERTIES = "M"; //$NON-NLS-1$
  /** for WebDAV lock null resources */
  String DESIGN_FLAGEXT_DAVLOCKNULL = "N"; //$NON-NLS-1$
  /** for WebDAV: the note is hidden */
  String DESIGN_FLAGEXT_WEBDAV_HIDDEN = "H"; //$NON-NLS-1$
  /**  for davs, a compute with form dav */
  String DESIGN_FLAGEXT_DAVCOMPUTEFORM = "C"; //$NON-NLS-1$
  /**  for davs, this one supports attachments */
  String DESIGN_FLAGEXT_DAVATTACH = "A"; //$NON-NLS-1$
  /**  for davs, this one GMT normalizes */
  String DESIGN_FLAGEXT_DAVGMTNORMAL = "Z"; //$NON-NLS-1$
  /**  can reuse D because old use is obsolete */
  String DESIGN_FLAGEXT_JAVADEBUG = "D"; //$NON-NLS-1$
  /**  profile code running in this note */
  String DESIGN_FLAGEXT_PROFILE = "F"; //$NON-NLS-1$
  /** for Java script libs and agents, indicates if it has errors */
  String DESIGN_FLAGEXT_JAVA_ERROR = "E"; //$NON-NLS-1$
  /**  Scriptlib is a web service consumer lib */
  String DESIGN_FLAGEXT_WEBSERVICELIB = "W"; //$NON-NLS-1$
  /** for files, indicates it's in the web content directory */
  String DESIGN_FLAGEXT_WEBCONTENTFILE = "w"; //$NON-NLS-1$
  /** After initial creation of a replica, these view are rebuilt */
  String DESIGN_FLAGEXT_REBUILD_VIEW = "X"; //$NON-NLS-1$
  /** for design elements created by HCL Volt vs by the user */
  String DESIGN_FLAGEXT_VOLT = "V"; //$NON-NLS-1$

  /** FORM: Indicates that a subform is in the add subform list */
  String DESIGN_FLAG_ADD = "A"; //$NON-NLS-1$
  /** VIEW: Indicates that a view is an antifolder view */
  String DESIGN_FLAG_ANTIFOLDER = "a"; //$NON-NLS-1$
  /** FILTER: Indicates FILTER_TYPE_BACKGROUND is asserted */
  String DESIGN_FLAG_BACKGROUND_FILTER = "B"; //$NON-NLS-1$
  /** VIEW: Indicates view can be initially built only by designer and above */
  String DESIGN_FLAG_INITBYDESIGNONLY = "b"; //$NON-NLS-1$
  /**
   * FORM: Indicates a form that is used only for query by form (not on compose
   * menu).
   */
  String DESIGN_FLAG_NO_COMPOSE = "C"; //$NON-NLS-1$
  /** VIEW: Indicates a form is a calendar style view. */
  String DESIGN_FLAG_CALENDAR_VIEW = "c"; //$NON-NLS-1$
  /** FORM: Indicates a form that should not be used in query by form */
  String DESIGN_FLAG_NO_QUERY = "D"; //$NON-NLS-1$
  /** ALL: Indicates the default design note for it's class (used for VIEW) */
  String DESIGN_FLAG_DEFAULT_DESIGN = "d"; //$NON-NLS-1$
  /** FILTER: Indicates FILTER_TYPE_MAIL is asserted */
  String DESIGN_FLAG_MAIL_FILTER = "E"; //$NON-NLS-1$
  /** VIEW: Indicates that a view is a public antifolder view */
  String DESIGN_FLAG_PUBLICANTIFOLDER = "e"; //$NON-NLS-1$
  /** VIEW: This is a V4 folder view. */
  String DESIGN_FLAG_FOLDER_VIEW = "F"; //$NON-NLS-1$
  /** FILTER: This is a V4 agent */
  String DESIGN_FLAG_V4AGENT = "f"; //$NON-NLS-1$
  /** VIEW: This is ViewMap/GraphicView/Navigator */
  String DESIGN_FLAG_VIEWMAP = "G"; //$NON-NLS-1$
  /** FORM: file design element */
  String DESIGN_FLAG_FILE = "g"; //$NON-NLS-1$
  /** ALL: Indicates a form that is placed in Other... dialog */
  String DESIGN_FLAG_OTHER_DLG = "H"; //$NON-NLS-1$
  /** Javascript library. */
  String DESIGN_FLAG_JAVASCRIPT_LIBRARY = "h"; //$NON-NLS-1$
  /** FILTER: This is a V4 paste agent */
  String DESIGN_FLAG_V4PASTE_AGENT = "I"; //$NON-NLS-1$
  /** FORM: Note is a shared image resource */
  String DESIGN_FLAG_IMAGE_RESOURCE = "i"; //$NON-NLS-1$
  /** FILTER: If its Java */
  String DESIGN_FLAG_JAVA_AGENT = "J"; //$NON-NLS-1$
  /** FILTER: If it is a java agent with java source code. */
  String DESIGN_FLAG_JAVA_AGENT_WITH_SOURCE = "j"; //$NON-NLS-1$
  /** to keep mobile digests out of form lists */
  String DESIGN_FLAG_MOBILE_DIGEST = "K"; //$NON-NLS-1$
  /**
   * FORM: with "g", design element is an xpage, much like a file resource, but
   * special!
   */
  String DESIGN_FLAG_XSPPAGE = "K"; //$NON-NLS-1$
  /** Data Connection Resource (DCR) for 3rd party database */
  String DESIGN_FLAG_CONNECTION_RESOURCE = "k"; //$NON-NLS-1$
  /** FILTER: If its LOTUSSCRIPT */
  String DESIGN_FLAG_LOTUSSCRIPT_AGENT = "L"; //$NON-NLS-1$
  /** VIEW: Indicates that a view is a deleted documents view */
  String DESIGN_FLAG_DELETED_DOCS = "l"; //$NON-NLS-1$
  /** FILTER: Stored FT query AND macro */
  String DESIGN_FLAG_QUERY_MACRO_FILTER = "M"; //$NON-NLS-1$
  /** FILTER: This is a site(m)ap. */
  String DESIGN_FLAG_SITEMAP = "m"; //$NON-NLS-1$
  /** FORM: Indicates that a subform is listed when making a new form. */
  String DESIGN_FLAG_NEW = "N"; //$NON-NLS-1$
  /**
   * ALL: notes stamped with this flag will be hidden from Notes clients
   * We need a separate value here because it is possible to be
   * hidden from V4 AND to be hidden from Notes, and clearing one should not clear
   * the other
   */
  String DESIGN_FLAG_HIDE_FROM_NOTES = "n"; //$NON-NLS-1$
  /** FILTER: Indicates V4 search bar query object - used in addition to 'Q' */
  String DESIGN_FLAG_QUERY_V4_OBJECT = "O"; //$NON-NLS-1$
  /** VIEW: If Private_1stUse, store the private view in desktop */
  String DESIGN_FLAG_PRIVATE_STOREDESK = "o"; //$NON-NLS-1$
  /** ALL: related to data dictionary */
  String DESIGN_FLAG_PRESERVE = "P"; //$NON-NLS-1$
  /** VIEW: This is a private copy of a private on first use view. */
  String DESIGN_FLAG_PRIVATE_1STUSE = "p"; //$NON-NLS-1$
  /** FILTER: Indicates full text query ONLY, no filter macro */
  String DESIGN_FLAG_QUERY_FILTER = "Q"; //$NON-NLS-1$
  /** FILTER: Search part of this agent should be shown in search bar */
  String DESIGN_FLAG_AGENT_SHOWINSEARCH = "q"; //$NON-NLS-1$
  /**
   * SPECIAL: this flag is the opposite of DESIGN_FLAG_PRESERVE, used
   * only for the 'About' and 'Using' notes + the icon bitmap in the icon note
   */
  String DESIGN_FLAG_REPLACE_SPECIAL = "R"; //$NON-NLS-1$
  /** DESIGN: this flag is used to propagate the prohibition of design change */
  String DESIGN_FLAG_PROPAGATE_NOCHANGE = "r"; //$NON-NLS-1$
  /** FILTER: This is a V4 background agent */
  String DESIGN_FLAG_V4BACKGROUND_MACRO = "S"; //$NON-NLS-1$
  /** FILTER: A database global script library note */
  String DESIGN_FLAG_SCRIPTLIB = "s"; //$NON-NLS-1$
  /** VIEW: Indicates a view that is categorized on the categories field */
  String DESIGN_FLAG_VIEW_CATEGORIZED = "T"; //$NON-NLS-1$
  /** FILTER: A database script note */
  String DESIGN_FLAG_DATABASESCRIPT = "t"; //$NON-NLS-1$
  /** FORM: Indicates that a form is a subform. */
  String DESIGN_FLAG_SUBFORM = "U"; //$NON-NLS-1$
  /** FILTER: Indicates agent should run as effective user on web */
  String DESIGN_FLAG_AGENT_RUNASWEBUSER = "u"; //$NON-NLS-1$
  /**
   * FILTER: Indicates agent should run as invoker (generalize the web user
   * notion, reuse the flag
   */
  String DESIGN_FLAG_AGENT_RUNASINVOKER = "u"; //$NON-NLS-1$
  /** ALL: This is a private element stored in the database */
  String DESIGN_FLAG_PRIVATE_IN_DB = "V"; //$NON-NLS-1$
  /**
   * FORM: Used with 'i' to indicate the image is an image well.
   * Used for images with images across, not images down. 'v' looks like a bucket
   */
  String DESIGN_FLAG_IMAGE_WELL = "v"; //$NON-NLS-1$
  /** FORM: Note is a WEBPAGE */
  String DESIGN_FLAG_WEBPAGE = "W"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from WEB clients */
  String DESIGN_FLAG_HIDE_FROM_WEB = "w"; //$NON-NLS-1$
  /**
   * WARNING: A formula that build Design Collection relies on the fact that Agent
   * Data's
   * $Flags is the only Design Collection element whose $Flags="X"
   * FILTER: This is a V4 agent data note
   */
  String DESIGN_FLAG_V4AGENT_DATA = "X"; //$NON-NLS-1$
  /**
   * SUBFORM: indicates whether we should render a subform in the parent form = =
   */
  String DESIGN_FLAG_SUBFORM_NORENDER = "x"; //$NON-NLS-1$
  /** ALL: Indicates that folder/view/etc. should be hidden from menu. */
  String DESIGN_FLAG_NO_MENU = "Y"; //$NON-NLS-1$
  /** Shared actions note */
  String DESIGN_FLAG_SACTIONS = "y"; //$NON-NLS-1$

  /**
   * ALL: Used to indicate design element was hidden before the 'Notes Global
   * Designer' modified it. (used with the "!" flag)
   */
  String DESIGN_FLAG_MULTILINGUAL_PRESERVE_HIDDEN = "Z"; //$NON-NLS-1$
  /** FILTER: this is a servlet, not an agent! */
  String DESIGN_FLAG_SERVLET = "z"; //$NON-NLS-1$
  /** FORM: reuse obsoleted servlet flag */
  String DESIGN_FLAG_ACCESSVIEW = "z"; //$NON-NLS-1$
  /** FORM: Indicates that this is a frameset note */
  String DESIGN_FLAG_FRAMESET = "#"; //$NON-NLS-1$
  /**
   * ALL: Indicates this design element supports the 'Notes Global Designer'
   * multilingual addin
   */
  String DESIGN_FLAG_MULTILINGUAL_ELEMENT = "!"; //$NON-NLS-1$
  /** FORM: Note is a shared Java resource */
  String DESIGN_FLAG_JAVA_RESOURCE = "@"; //$NON-NLS-1$
  /** Style Sheet Resource (SSR) */
  String DESIGN_FLAG_STYLESHEET_RESOURCE = "="; //$NON-NLS-1$
  /** FILTER: web service design element */
  String DESIGN_FLAG_WEBSERVICE = "{"; //$NON-NLS-1$
  /** VIEW: shared column design element */
  String DESIGN_FLAG_SHARED_COL = "^"; //$NON-NLS-1$
  /** hide this element from mobile clients */
  String DESIGN_FLAG_HIDE_FROM_MOBILE = "1"; //$NON-NLS-1$
  /** hide from portal */
  String DESIGN_FLAG_HIDE_FROM_PORTAL = "2"; //$NON-NLS-1$
  /** xpage/cc properties file */
  String DESIGN_FLAG_PROPFILE = "2"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from V3 client */
  String DESIGN_FLAG_HIDE_FROM_V3 = "3"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from V4 client */
  String DESIGN_FLAG_HIDE_FROM_V4 = "4"; //$NON-NLS-1$
  /**
   * FILTER: 'Q5'= hide from V4.5 search list
   * ALL OTHER: notes stamped with this flag will be hidden from V5 client
   */
  String DESIGN_FLAG_HIDE_FROM_V5 = "5"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from V6 client */
  String DESIGN_FLAG_HIDE_FROM_V6 = "6"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from V7 client */
  String DESIGN_FLAG_HIDE_FROM_V7 = "7"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from V8 client */
  String DESIGN_FLAG_HIDE_FROM_V8 = "8"; //$NON-NLS-1$
  /** ALL: notes stamped with this flag will be hidden from V9 client */
  String DESIGN_FLAG_HIDE_FROM_V9 = "9"; //$NON-NLS-1$
  /**
   * ALL: notes stamped with this flag will be hidden from the client
   * usage is for different language versions of the design list to be
   * hidden completely =
   */
  String DESIGN_FLAG_MUTILINGUAL_HIDE = "0"; //$NON-NLS-1$
  /** shimmer design docs */
  String DESIGN_FLAG_WEBHYBRIDDB = "%"; //$NON-NLS-1$
  /** for files, at least for starters */
  String DESIGN_FLAG_READONLY = "&"; //$NON-NLS-1$
  /** for files, at least for now */
  String DESIGN_FLAG_NEEDSREFRESH = "$"; //$NON-NLS-1$
  /** this design element is an html file */
  String DESIGN_FLAG_HTMLFILE = ">"; //$NON-NLS-1$
  /** this design element is a jsp */
  String DESIGN_FLAG_JSP = "<"; //$NON-NLS-1$
  /** VIEW - Query View in design list */
  String DESIGN_FLAG_QUERYVIEW = "<"; //$NON-NLS-1$
  /** this file element is a directory */
  String DESIGN_FLAG_DIRECTORY = "/"; //$NON-NLS-1$
  /** FORM - used for printing. */
  String DESIGN_FLAG_PRINTFORM = "?"; //$NON-NLS-1$

  /** keep this thing out of a design list */
  String DESIGN_FLAG_HIDEFROMDESIGNLIST = "~"; //$NON-NLS-1$

  /** keep this thing out of a design list but allow users to view doc using it */
  String DESIGN_FLAG_HIDEONLYFROMDESIGNLIST = "}"; //$NON-NLS-1$

  /** FORM: This is a "composite application" design element. LI 3925.04 */
  String DESIGN_FLAG_COMPOSITE_APP = "|"; //$NON-NLS-1$

  /**
   * FORM: Design element is "wiring properties". Always accompanied by hide flags
   * for versions prior to 8.0. LI 3925.05
   */
  String DESIGN_FLAG_COMPOSITE_DEF = ":"; //$NON-NLS-1$

  /** note class form, a custom control */
  String DESIGN_FLAG_XSP_CC = ";"; //$NON-NLS-1$

  /** note class filter, with 's', server side JS script library */
  String DESIGN_FLAG_JS_SERVER = "."; //$NON-NLS-1$

  /** style kit design element */
  String DESIGN_FLAG_STYLEKIT = "`"; //$NON-NLS-1$

  /** also has a g and is a file, but a component/widget design element */
  String DESIGN_FLAG_WIDGET = "_"; //$NON-NLS-1$

  /** Java design element */
  String DESIGN_FLAG_JAVAFILE = "["; //$NON-NLS-1$

  /**
   * At least one of the "definition"
   * view items ($FORMULA, $COLLATION,
   * or $FORMULACLASS) has been modified
   * by another user since last ReadEntries.
   * Upon receipt, you may wish to
   * re-read the view note if up-to-date
   * copies of these items are needed.
   * Upon receipt, you may also wish to
   * re-synchronize your index position
   * and re-read the rebuilt index.<br>
   * <br>
   * Signal returned only ONCE per detection
   */
  int SIGNAL_DEFN_ITEM_MODIFIED = 0x0001;

  /**
   * At least one of the non-"definition"
   * view items ($TITLE,etc) has been
   * modified since last ReadEntries.
   * Upon receipt, you may wish to
   * re-read the view note if up-to-date
   * copies of these items are needed.<br>
   * <br>
   * Signal returned only ONCE per detection
   */
  int SIGNAL_VIEW_ITEM_MODIFIED = 0x0002;

  /**
   * Collection index has been modified
   * by another user since last ReadEntries.
   * Upon receipt, you may wish to
   * re-synchronize your index position
   * and re-read the modified index.<br>
   * <br>
   * Signal returned only ONCE per detection
   */
  int SIGNAL_INDEX_MODIFIED = 0x0004;

  /**
   * Unread list has been modified
   * by another window using the same
   * hCollection context
   * Upon receipt, you may wish to
   * repaint the window if the window
   * contains the state of unread flags
   * (This signal is never generated
   * by NIF - only unread list users)
   */
  int SIGNAL_UNREADLIST_MODIFIED = 0x0008;

  /** Collection is not up to date */
  int SIGNAL_DATABASE_MODIFIED = 0x0010;
  /**
   * End of collection has not been reached
   * due to buffer being too full.
   * The ReadEntries should be repeated
   * to continue reading the desired entries.
   */
  int SIGNAL_MORE_TO_DO = 0x0020;
  /**
   * The view contains a time-relative formula
   * (e.g., @Now). Use this flag to tell if the
   * collection will EVER be up-to-date since
   * time-relative views, by definition, are NEVER
   * up-to-date.
   */
  int SIGNAL_VIEW_TIME_RELATIVE = 0x0040;
  /**
   * Returned if signal flags are not supported
   * This is used by NIFFindByKeyExtended when it
   * is talking to a pre-V4 server that does not
   * support signal flags for FindByKey
   */
  int SIGNAL_NOT_SUPPORTED = 0x0080;
  /** The view contains documents with readers fields */
  int SIGNAL_VIEW_HASPRIVS = 0x0100;

  /**
   * Mask that defines all "sharing conflicts", which are cases when
   * the database or collection has changed out from under the user.
   */
  int SIGNAL_ANY_CONFLICT = NotesConstants.SIGNAL_DEFN_ITEM_MODIFIED | NotesConstants.SIGNAL_VIEW_ITEM_MODIFIED
      | NotesConstants.SIGNAL_INDEX_MODIFIED | NotesConstants.SIGNAL_UNREADLIST_MODIFIED | NotesConstants.SIGNAL_DATABASE_MODIFIED;

  /**
   * Mask that defines all "sharing conflicts" except for
   * SIGNAL_DATABASE_MODIFIED.
   * This can be used in combination with SIGNAL_VIEW_TIME_RELATIVE to tell if
   * the database or collection has truly changed out from under the user or if
   * the
   * view is a time-relative view which will NEVER be up-to-date.
   * SIGNAL_DATABASE_MODIFIED
   * is always returned for a time-relative view to indicate that it is never
   * up-to-date.
   */
  int SIGNAL_ANY_NONDATA_CONFLICT = NotesConstants.SIGNAL_DEFN_ITEM_MODIFIED | NotesConstants.SIGNAL_VIEW_ITEM_MODIFIED
      | NotesConstants.SIGNAL_INDEX_MODIFIED | NotesConstants.SIGNAL_UNREADLIST_MODIFIED;
  long NOTEID_RESERVED = 0x80000000L; /*	Reserved Note ID, used for categories in NIFReadEntries and for deleted notes in a lot of interfaces. */
  long RRV_DELETED = NotesConstants.NOTEID_RESERVED;
  long NOTEID_GHOST_ENTRY = 0x40000000L; /* Bit 30 -> partial thread ghost collection entry */
  long NOTEID_CATEGORY = 0x80000000L; /* Bit 31 -> (ghost) "category entry" */
  long NOTEID_CATEGORY_TOTAL = 0xC0000000L; /* Bit 31+30 -> (ghost) "grand total entry" */
  long NOTEID_CATEGORY_INDENT = 0x3F000000L; /* Bits 24-29 -> category indent level within this column */
  long NOTEID_CATEGORY_ID = 0x00FFFFFFL; /* Low 24 bits are unique category # */
  /**
   * If the following is ORed in with a note class, the resultant note ID
   * may be passed into NSFNoteOpen and may be treated as though you first
   * did an NSFGetSpecialNoteID followed by an NSFNoteOpen, all in a single
   * transaction.
   */
  int NOTE_ID_SPECIAL = 0xFFFF0000;
  /** No filter specified (hFilter ignored). */
  int SEARCH_FILTER_NONE = 0x00000000;
  /** hFilter is a Note ID table. */
  int SEARCH_FILTER_NOTEID_TABLE = 0x00000001;
  /** hFilter is a View note handle */
  int SEARCH_FILTER_FOLDER = 0x00000002;
  /** Filter on particular Properties. */
  int SEARCH_FILTER_DBDIR_PROPERTY = 0x00000004;

  /** Filter on Database Options (bits set). */
  int SEARCH_FILTER_DBOPTIONS = 0x00000010;

  /** Filter on Database Options (bits clear). */
  int SEARCH_FILTER_DBOPTIONS_CLEAR = 0x00000020;

  /** Filter based on a set of form names */
  int SEARCH_FILTER_FORMSKIMMED = 0x00000040;
  /** Don't try to filter on form names, we know it won't work */
  int SEARCH_FILTER_NOFORMSKIMMED = 0x00000080;

  /** Filter on Query View SQL */
  int SEARCH_FILTER_QUERY_VIEW = 0x00000100;

  /** Filter on item revision times */
  int SEARCH_FILTER_ITEM_TIME = 0x00000200;

  /** Filter on time range input */
  int SEARCH_FILTER_RANGE = 0x00000400;
  /** Filter out .ndx files */
  int SEARCH_FILTER_NO_NDX = 0x00000800;

  /** Search for databases with inline indexing */
  int SEARCH_FILTER_INLINE_INDEX = 0x00001000;
  /**
   * Include deleted and non-matching notes in search (ALWAYS "ON" in partial
   * searches!)
   */
  int SEARCH_ALL_VERSIONS = 0x0001;
  /** obsolete synonym */
  int SEARCH_INCLUDE_DELETED = NotesConstants.SEARCH_ALL_VERSIONS;
  /** TRUE to return summary buffer with each match */
  int SEARCH_SUMMARY = 0x0002;
  /**
   * For directory mode file type filtering. If set, "NoteClassMask" is
   * treated as a FILE_xxx mask for directory filtering
   */
  int SEARCH_FILETYPE = 0x0004;
  /** special caching for dir scan */
  int SEARCH_SERVERCACHE = 0x0008;
  /** Set NOTE_CLASS_NOTIFYDELETION bit of NoteClass for deleted notes */
  int SEARCH_NOTIFYDELETIONS = 0x0010;

  /** do not put item names into summary info */
  int SEARCH_NOITEMNAMES = 0x0020;
  /** return error if we don't have full privileges */
  int SEARCH_ALLPRIVS = 0x0040;
  /** for dir scans, only return files needing fixup */
  int SEARCH_FILEFIXUP = 0x0080;
  /** Formula buffer is hashed UNID table */
  int SEARCH_UNID_TABLE = 0x0100;
  /** Return buffer in canonical form */
  int SEARCH_CANONICAL = 0x0200;
  /** Use current session's user name, not server's */
  int SEARCH_SESSION_USERNAME = 0x0400;
  /** Allow search to return id's only, i.e. no summary buffer */
  int SEARCH_NOPRIVCHECK = 0x0800;
  /** Filter out "Truncated" documents */
  int SEARCH_NOABSTRACTS = 0x1000;
  /** Perform unread flag sync */
  int SEARCH_SYNC = 0x2000;
  /** Search formula applies only to data notes, i.e., others match */
  int SEARCH_DATAONLY_FORMULA = 0x4000;
  /** INCLUDE notes with non-replicatable OID flag */
  int SEARCH_NONREPLICATABLE = 0x8000;
  /**
   * SEARCH_MATCH is V4 style. That is MatchesFormula is now a bit field where
   * the lower bit indicates whether the document matches. If it does, the
   * other bits provide additional information regarding the note.
   */
  int SEARCH_V4INFO = 0x00010000;

  /** Search includes all children of matching documents. */
  int SEARCH_ALLCHILDREN = 0x00020000;
  /** Search includes all descendants of matching documents. */
  int SEARCH_ALLDESCENDANTS = 0x00040000;
  /** First pass in a multipass hierarchical search. */
  int SEARCH_FIRSTPASS = 0x00080000;
  /** Descendants were added on this pass. */
  int SEARCH_DESCENDANTSADDED = 0x00100000;
  /** Formula is an Array of Formulas. */
  int SEARCH_MULTI_FORMULA = 0x00200000;

  /** Return purged note ids as deleted notes. */
  int SEARCH_INCLUDE_PURGED = 0x00400000;
  /** Only return templates without the "advanced" bit set */
  int SEARCH_NO_ADV_TEMPLATES = 0x00800000;

  /** Only Private Views or Agents */
  int SEARCH_PRIVATE_ONLY = 0x01000000;

  /**
   * Full search (as if Since was "1") but exclude DATA notes prior to
   * passed-in Since time
   */
  int SEARCH_FULL_DATACUTOFF = 0x02000000;

  /**
   * If specified, the progress field in the SEARCH_ENTRY structure will be
   * filled in. This avoids performing the calculation if it was not wanted.
   */
  int SEARCH_CALC_PROGRESS = 0x04000000;

  /**
   * Include *** ALL *** named ghost notes in the search (profile docs,
   * xACL's, etc). Note: use SEARCH1_PROFILE_DOCS, etc., introduced in R6, for
   * finer control
   */
  int SEARCH_NAMED_GHOSTS = 0x08000000;
  /** Perform optimized unread sync */
  int SEARCH_SYNC_OPTIMIZED = 0x10000000;
  /**
   * Return only docs with protection fields (BS_PROTECTED set in note header)
   */
  int SEARCH_ONLYPROTECTED = 0x20000000;

  /** Return soft deleted documents */
  int SEARCH_SOFTDELETIONS = 0x40000000;
  /*
   * the SEARCH2_* flags are passed to NSFSearchExt only &amp; mapped to SEARCH1_* flags
   * passed to NSFSearchExtended3
   */
  /** return flag in orig structs if &gt; MAXONESEGSIZE */
  int SEARCH2_LARGE_BUCKETS = 0x00000001;
  /** return SEARCH_MATCH_LARGE etc structs */
  int SEARCH2_RET_LARGE_DATA = 0x00000002;

  /** for setting/verifying that bits 28-31 of search 1 flags are 1000 */
  int SEARCH1_SIGNATURE = 0x80000000;
  int SEARCH1_SELECT_NAMED_GHOSTS = 0x00000001 | NotesConstants.SEARCH1_SIGNATURE;

  /*	File type flags (used with NSFSearch directory searching). */

  /**
   * Include profile documents (a specific type of named ghost note) in the
   * search Note: set SEARCH1_SELECT_NAMED_GHOSTS, too, if you want the
   * selection formula to be applied to the profile docs (so as not to get
   * them all back as matches).
   */
  int SEARCH1_PROFILE_DOCS = 0X00000002 | NotesConstants.SEARCH1_SIGNATURE;
  /**
   * Skim off notes whose summary buffer can't be generated because its size
   * is too big.
   */
  int SEARCH1_SKIM_SUMMARY_BUFFER_TOO_BIG = 0x00000004 | NotesConstants.SEARCH1_SIGNATURE;
  int SEARCH1_RETURN_THREAD_UNID_ARRAY = 0x00000008 | NotesConstants.SEARCH1_SIGNATURE;
  int SEARCH1_RETURN_TUA = NotesConstants.SEARCH1_RETURN_THREAD_UNID_ARRAY;

  /**
   * flag for reporting noaccess in case of reader's field at the doc level
   */
  int SEARCH1_REPORT_NOACCESS = 0x000000010 | NotesConstants.SEARCH1_SIGNATURE;
  /** Search "Truncated" documents */
  int SEARCH1_ONLY_ABSTRACTS = 0x000000020 | NotesConstants.SEARCH1_SIGNATURE;
  /**
   * Search documents fixup purged. This distinct and mutually exlusive from
   * SEARCH_INCLUDE_PURGED which is used for view processing by NIF etc to
   * remove purged notes from views. This is used for replication restoring
   * corrupt documents.
   */
  int SEARCH1_FIXUP_PURGED = 0x000000040 | NotesConstants.SEARCH1_SIGNATURE;
  /** used to indicate large summary buckets is supported by caller */
  int SEARCH1_LARGE_BUCKETS = 0x00000080 | NotesConstants.SEARCH1_SIGNATURE;
  /** used to indicate to return SEARCH_MATCH_LARGE &amp; ilk */
  int SEARCH1_RET_LARGE_DATA = 0x00000100 | NotesConstants.SEARCH1_SIGNATURE;
  /** Any file type */
  int FILE_ANY = 0;
  /** Starting in V3, any DB that is a candidate for replication */
  int FILE_DBREPL = 1;
  /** Databases that can be templates */
  int FILE_DBDESIGN = 2;
  /** BOX - Any .BOX (Mail.BOX, SMTP.Box...) */
  int FILE_MAILBOX = 3;
  /** NS?, any NSF version */
  int FILE_DBANY = 4;

  /** NT?, any NTF version */
  int FILE_FTANY = 5;
  /** MDM - modem command file */
  int FILE_MDMTYPE = 6;
  /** directories only */
  int FILE_DIRSONLY = 7;
  /** VPC - virtual port command file */
  int FILE_VPCTYPE = 8;
  /** SCR - comm port script files */
  int FILE_SCRTYPE = 9;

  /** ANY Notes database (.NS?, .NT?, .BOX) */
  int FILE_ANYNOTEFILE = 10;
  /**
   * DTF - Any .DTF. Used for container and sort temp files to give them a more
   * unique name than .TMP so we can delete *.DTF from the temp directory and
   * hopefully not blow away other application's temp files.
   */
  int FILE_UNIQUETEMP = 11;

  /** CLN - Any .cln file...multi user cleanup files */
  int FILE_MULTICLN = 12;
  /** any smarticon file *.smi */
  int FILE_SMARTI = 13;
  /** File type mask (for FILE_xxx codes above) */
  int FILE_TYPEMASK = 0x00ff;
  /** List subdirectories as well as normal files */
  int FILE_DIRS = 0x8000;
  /** Do NOT return ..'s */
  int FILE_NOUPDIRS = 0x4000;
  /** Recurse into subdirectories */
  int FILE_RECURSE = 0x2000;
  /** All directories, linked files &amp; directories */
  int FILE_LINKSONLY = 0x1000;
  /** hDB refers to a normal database file */
  short DB_LOADED = 1;
  /** hDB refers to a "directory" and not a file */
  short DB_DIRECTORY = 2;

  /** does not match formula (deleted or updated) */
  byte SE_FNOMATCH = 0x00;
  /** matches formula */
  byte SE_FMATCH = 0x01;
  /** document truncated */
  byte SE_FTRUNCATED = 0x02;
  /** note has been purged. Returned only when SEARCH_INCLUDE_PURGED is used */
  byte SE_FPURGED = 0x04;
  /**
   * note has no purge status. Returned only when SEARCH_FULL_DATACUTOFF is used
   */
  byte SE_FNOPURGE = 0x08;
  /**
   * if SEARCH_NOTIFYDELETIONS: note is soft deleted; NoteClass &amp;
   * NOTE_CLASS_NOTIFYDELETION also on (off for hard delete)
   */
  byte SE_FSOFTDELETED = 0x10;
  /**
   * if there is reader's field at doc level this is the return value so that we
   * could mark the replication as incomplete
   */
  byte SE_FNOACCESS = 0x20;
  /**
   * note has truncated attachments. Returned only when SEARCH1_ONLY_ABSTRACTS is
   * used
   */
  byte SE_FTRUNCATT = 0x40;
  /**
   * note has a large summary, a note open is required to get the data, only valid
   * if SEARCH1_LARGE_BUCKETS is passed in
   */
  byte SE_FLARGESUMMARY = (byte) (0x80 & 0xff);
  /** Authors can't create new notes (only edit existing ones) */
  short ACL_FLAG_AUTHOR_NOCREATE = 0x0001;
  /** Entry represents a Server (V4) */
  short ACL_FLAG_SERVER = 0x0002;
  /** User cannot delete notes */
  short ACL_FLAG_NODELETE = 0x0004;
  /** User can create personal agents (V4) */
  short ACL_FLAG_CREATE_PRAGENT = 0x0008;
  /** User can create personal folders (V4) */
  short ACL_FLAG_CREATE_PRFOLDER = 0x0010;
  /** Entry represents a Person (V4) */
  short ACL_FLAG_PERSON = 0x0020;

  /** Entry represents a group (V4) */
  short ACL_FLAG_GROUP = 0x0040;
  /**
   * User can create and update shared views &amp; folders (V4)<br>
   * This allows an Editor to assume some Designer-level access
   */
  short ACL_FLAG_CREATE_FOLDER = 0x0080;
  /** User can create LotusScript */
  short ACL_FLAG_CREATE_LOTUSSCRIPT = 0x0100;
  /** User can read public notes */
  short ACL_FLAG_PUBLICREADER = 0x0200;
  /** User can write public notes */
  short ACL_FLAG_PUBLICWRITER = 0x0400;
  /** User CANNOT register monitors for this database */
  short ACL_FLAG_MONITORS_DISALLOWED = 0x800;
  /** User cannot replicate or copy this database */
  short ACL_FLAG_NOREPLICATE = 0x1000;

  /** Admin server can modify reader and author fields in db */
  short ACL_FLAG_ADMIN_READERAUTHOR = 0X4000;
  /** Entry is administration server (V4) */
  short ACL_FLAG_ADMIN_SERVER = (short) (0x8000 & 0xffff);

  /** User or Server has no access to the database. */
  short ACL_LEVEL_NOACCESS = 0;
  /**
   * User or Server can add new data documents to a database, but cannot examine
   * the new document or the database.
   */
  short ACL_LEVEL_DEPOSITOR = 1;
  /** User or Server can only view data documents in the database. */
  short ACL_LEVEL_READER = 2;

  /**
   * User or Server can create and/or edit their own data documents and examine
   * existing ones in the database.
   */
  short ACL_LEVEL_AUTHOR = 3;

  /* ACLUpdateEntry flags - Set flag if parameter is being modified */

  /** User or Server can create and/or edit any data document. */
  short ACL_LEVEL_EDITOR = 4;
  /**
   * User or Server can create and/or edit any data document and/or design
   * document.
   */
  short ACL_LEVEL_DESIGNER = 5;
  /**
   * User or Server can create and/or maintain any type of database or document,
   * including the ACL.
   */
  short ACL_LEVEL_MANAGER = 6;
  /** Highest access level */
  short ACL_LEVEL_HIGHEST = 6;

  /** Number of access levels */
  short ACL_LEVEL_COUNT = 7;
  /** Number of privilege bits (10 bytes) */
  int ACL_PRIVCOUNT = 80;
  /** Privilege name max (including null) */
  int ACL_PRIVNAMEMAX = 16;
  /** Privilege string max (including parentheses and null) */
  int ACL_PRIVSTRINGMAX = 16 + 2;
  /** Require same ACL in ALL replicas of database */
  int ACL_UNIFORM_ACCESS = 0x00000001;

  short ACL_UPDATE_NAME = 0x01;

  short ACL_UPDATE_LEVEL = 0x02;
  short ACL_UPDATE_PRIVILEGES = 0x04;
  short ACL_UPDATE_FLAGS = 0x08;
  /** (e.g. Times Roman family) */
  byte FONT_FACE_ROMAN = 0;
  /** (e.g. Helv family) */
  byte FONT_FACE_SWISS = 1;

  /*	Standard colors -- so useful they're available by name. */

  /** (e.g. Monotype Sans WT) */
  byte FONT_FACE_UNICODE = 2;

  /** (e.g. Arial */
  byte FONT_FACE_USERINTERFACE = 3;
  /** (e.g. Courier family) */
  byte FONT_FACE_TYPEWRITER = 4;
  /**
   * Use this style ID in CompoundTextAddText to continue using the
   * same paragraph style as the previous paragraph.
   */
  int STYLE_ID_SAMEASPREV = 0xFFFFFFFF;
  /** CompoundText is derived from a file */
  int COMP_FROM_FILE = 0x00000001;
  /**
   * Insert a line break (0) for each line delimiter found in the input text
   * buffer. This preserves input line breaks.
   */
  int COMP_PRESERVE_LINES = 0x00000002;
  /**
   * Create a new paragraph for each line delimiter found in the input text
   * buffer.
   */
  int COMP_PARA_LINE = 0x00000004;
  /**
   * Create a new paragraph for each blank line found in the input text buffer.
   * A blank line is defined as a line containing just a line delimiter (specified
   * by the
   * pszLineDelim parameter to CompoundTextAddTextExt).
   */
  int COMP_PARA_BLANK_LINE = 0x00000008;
  /**
   * A "hint" follows the comment for a document link. If this flag is set,
   * the pszComment argument points to the comment string, the terminating NUL
   * ('\0'),
   * the hint string, and the terminating NUL.
   */
  int COMP_SERVER_HINT_FOLLOWS = 0x00000010;
  byte MAX_NOTES_SOLIDCOLORS = 16;
  byte NOTES_COLOR_BLACK = 0;
  byte NOTES_COLOR_WHITE = 1;
  byte NOTES_COLOR_RED = 2;
  byte NOTES_COLOR_GREEN = 3;
  byte NOTES_COLOR_BLUE = 4;
  byte NOTES_COLOR_MAGENTA = 5;
  byte NOTES_COLOR_YELLOW = 6;

  byte NOTES_COLOR_CYAN = 7;
  byte NOTES_COLOR_DKRED = 8;
  byte NOTES_COLOR_DKGREEN = 9;
  byte NOTES_COLOR_DKBLUE = 10;
  byte NOTES_COLOR_DKMAGENTA = 11;
  byte NOTES_COLOR_DKYELLOW = 12;
  byte NOTES_COLOR_DKCYAN = 13;
  byte NOTES_COLOR_GRAY = 14;
  byte NOTES_COLOR_LTGRAY = 15;
  byte ISBOLD = 0x01;

  /*	Paragraph justification type codes */

  byte ISITALIC = 0x02;
  byte ISUNDERLINE = 0x04;
  byte ISSTRIKEOUT = 0x08;
  byte ISSUPER = 0x10;
  byte ISSUB = 0x20;

  /*	One Inch */

  byte ISEFFECT = (byte) (0x80 & 0xff); /* Used for implementation of special effect styles */

  /*	Paragraph Flags */

  byte ISSHADOW = (byte) (0x80 & 0xff); /* Used for implementation of special effect styles */
  byte ISEMBOSS = (byte) (0x90 & 0xff); /* Used for implementation of special effect styles */
  byte ISEXTRUDE = (byte) (0xa0 & 0xff); /* Used for implementation of special effect styles */
  /** flush left, ragged right */
  short JUSTIFY_LEFT = 0;
  /** flush right, ragged left */
  short JUSTIFY_RIGHT = 1;
  /** full block justification */
  short JUSTIFY_BLOCK = 2;
  /** centered */
  short JUSTIFY_CENTER = 3;
  /** no line wrapping AT ALL (except hard CRs) */
  short JUSTIFY_NONE = 4;

  /* the pab was saved in V4.	*/

  int ONEINCH = 20 * 72; /* One inch worth of TWIPS */
  /** start new page with this par */
  short PABFLAG_PAGINATE_BEFORE = 0x0001;
  /** don't separate this and next par */
  short PABFLAG_KEEP_WITH_NEXT = 0x0002;
  /** don't split lines in paragraph */
  short PABFLAG_KEEP_TOGETHER = 0x0004;
  /** propagate even PAGINATE_BEFORE and KEEP_WITH_NEXT */
  short PABFLAG_PROPAGATE = 0x0008;
  /** hide paragraph in R/O mode */
  short PABFLAG_HIDE_RO = 0x0010;
  /** hide paragraph in R/W mode */
  short PABFLAG_HIDE_RW = 0x0020;
  /** hide paragraph when printing */
  short PABFLAG_HIDE_PR = 0x0040;

  /**
   * in V4 and below, set if PAB.RightMargin (when nonzero)
   * is to have meaning. Turns out, is set iff para is in
   * a table. Anyway, V5+ no longer use this bit but it
   * matters to V4 and below. V5+ runs with this bit
   * zeroed throughout runtime but, for backward
   * compatibility, outputs it to disk at Save() time
   * per whether paragraph is in a table.
   */
  short PABFLAG_DISPLAY_RM = 0x0080;

  /**
   * set this bit or the Notes client will assume the pab
   * was saved pre-V4 and will thus "link" these bit
   * definitions (assign the right one to the left one)
   * since preview did not exist pre-V4:<br>
   * PABFLAG_HIDE_PV = PABFLAG_HIDE_RO<br>
   * PABFLAG_HIDE_PVE = PABFLAG_HIDE_RW
   */
  short PABFLAG_HIDE_UNLINK = 0x0100;

  /* Extra Paragraph Flags (stored in Flags2 field) */

  /** hide paragraph when copying/forwarding */
  short PABFLAG_HIDE_CO = 0x0200;
  /** display paragraph with bullet */
  short PABFLAG_BULLET = 0x0400;
  /** use the hide when formula even if there is one. */
  short PABFLAG_HIDE_IF = 0x0800;
  /** display paragraph with number */
  short PABFLAG_NUMBEREDLIST = 0x1000;
  /** hide paragraph when previewing */
  short PABFLAG_HIDE_PV = 0x2000;
  /** hide paragraph when editing in the preview pane. */
  short PABFLAG_HIDE_PVE = 0x4000;
  /** hide paragraph from Notes clients */
  short PABFLAG_HIDE_NOTES = (short) (0x8000 & 0xffff);
  short PABFLAG_HIDEBITS = (short) ((NotesConstants.PABFLAG_HIDE_RO | NotesConstants.PABFLAG_HIDE_RW
      | NotesConstants.PABFLAG_HIDE_CO | NotesConstants.PABFLAG_HIDE_PR | NotesConstants.PABFLAG_HIDE_PV
      | NotesConstants.PABFLAG_HIDE_PVE | NotesConstants.PABFLAG_HIDE_IF | NotesConstants.PABFLAG_HIDE_NOTES) & 0xffff);
  short TABLE_PABFLAGS = (short) ((NotesConstants.PABFLAG_KEEP_TOGETHER | NotesConstants.PABFLAG_KEEP_WITH_NEXT) & 0xffff);
  short PABFLAG2_HIDE_WEB = 0x0001;
  short PABFLAG2_CHECKEDLIST = 0x0002;
  /** PAB.LeftMargin is an offset value. */
  short PABFLAG2_LM_OFFSET = 0x0004;
  /** PAB.LeftMargin is a percentage value. */
  short PABFLAG2_LM_PERCENT = 0x0008;
  /** PAB.LeftMargin is an offset value. */
  short PABFLAG2_FLLM_OFFSET = 0x0010;
  /** PAB.LeftMargin is a percentage value. */
  short PABFLAG2_FLLM_PERCENT = 0x0020;
  /** PAB.RightMargin is an offset value. */
  short PABFLAG2_RM_OFFSET = 0x0040;

  /** PAB.RightMargin is a percentage value. */
  short PABFLAG2_RM_PERCENT = 0x0080;

  /** If to use default value instead of PAB.LeftMargin. */
  short PABFLAG2_LM_DEFAULT = 0x0100;

  /** If to use default value instead of PAB.FirstLineLeftMargin. */
  short PABFLAG2_FLLM_DEFAULT = 0x0200;

  /** If to use default value instead of PAB.RightMargin. */
  short PABFLAG2_RM_DEFAULT = 0x0400;

  short PABFLAG2_CIRCLELIST = 0x0800;

  short PABFLAG2_SQUARELIST = 0x1000;
  short PABFLAG2_UNCHECKEDLIST = 0x2000;
  /** set if right to left reading order */
  short PABFLAG2_BIDI_RTLREADING = 0x4000;
  /** TRUE if Pab needs to Read more Flags */
  short PABFLAG2_MORE_FLAGS = (short) (0x8000 & 0xffff);

  /*	Table Flags */

  short PABFLAG2_HIDEBITS = NotesConstants.PABFLAG2_HIDE_WEB;

  /* Cell Flags */

  short PABFLAG2_CHECKLIST = (short) ((NotesConstants.PABFLAG2_UNCHECKEDLIST | NotesConstants.PABFLAG2_CHECKEDLIST) & 0xffff);

  /*	This DWORD, ExtendPabFlags, extends the PAB structure.<br>
   * Use the ExtendedPab flags to know what to read next */

  short PABFLAG2_MARGIN_DEFAULTS_MASK = (short) ((NotesConstants.PABFLAG2_LM_DEFAULT
      | NotesConstants.PABFLAG2_RM_DEFAULT
      | NotesConstants.PABFLAG2_FLLM_DEFAULT) & 0xffff);

  /* 	This DWORD extends the flags and flags 2 in the CDPABDEFINITION record */

  short PABFLAG2_MARGIN_STYLES_MASK = (short) ((NotesConstants.PABFLAG2_LM_OFFSET
      | NotesConstants.PABFLAG2_LM_PERCENT
      | NotesConstants.PABFLAG2_FLLM_OFFSET
      | NotesConstants.PABFLAG2_FLLM_PERCENT
      | NotesConstants.PABFLAG2_RM_OFFSET
      | NotesConstants.PABFLAG2_RM_PERCENT) & 0xffff);
  short PABFLAG2_MARGIN_MASK = (short) ((NotesConstants.PABFLAG2_MARGIN_STYLES_MASK | NotesConstants.PABFLAG2_MARGIN_DEFAULTS_MASK)
      & 0xffff);
  short PABFLAG2_ROMANUPPERLIST = (short) ((NotesConstants.PABFLAG2_CHECKEDLIST | NotesConstants.PABFLAG2_CIRCLELIST) & 0xffff);

  short PABFLAG2_ROMANLOWERLIST = (short) ((NotesConstants.PABFLAG2_CHECKEDLIST | NotesConstants.PABFLAG2_SQUARELIST) & 0xffff);
  short PABFLAG2_ALPHAUPPERLIST = (short) ((NotesConstants.PABFLAG2_SQUARELIST | NotesConstants.PABFLAG2_CIRCLELIST) & 0xffff);
  short PABFLAG2_ALPHALOWERLIST = (short) ((NotesConstants.PABFLAG2_CHECKEDLIST | NotesConstants.PABFLAG2_SQUARELIST
      | NotesConstants.PABFLAG2_CIRCLELIST) & 0xffff);
  /* Cells grow/shrink to fill window */
  short TABFLAG_AUTO_CELL_WIDTH = 0x0001;
  /* Cell uses background color */
  byte CELLFLAG_USE_BKGCOLOR = 0x01;
  int EXTENDEDPABFLAGS3 = 0x00000001; /* If True then need make another read for Flags3 */
  /** True, if Hide when embedded */
  int PABFLAG3_HIDE_EE = 0x00000001;

  /** True, if hidden from mobile clients */
  int PABFLAG3_HIDE_MOBILE = 0x00000002;
  /** True if boxes in a layer have set PABFLAG_DISPLAY_RM on pabs */
  int PABFLAG3_LAYER_USES_DRM = 0x00000004;
  short CDIMAGETYPE_GIF = 1;

  /*	The following flag indicates that the DestSize field contains
  pixel values instead of twips. */

  short CDIMAGETYPE_JPEG = 2;
  short CDIMAGETYPE_BMP = 3;

  /*	HOTSPOT_RUN Types */

  short CDIMAGETYPE_PNG = 4;
  /* Images not supported in Notes rich text, but which can be useful for MIME/HTML external files */
  short CDIMAGETYPE_SVG = 5;
  short CDIMAGETYPE_TIF = 6;
  short CDIMAGETYPE_PDF = 7;
  /* Version control of graphic header */
  byte CDGRAPHIC_VERSION1 = 0; /* Created by Notes version 2 */
  byte CDGRAPHIC_VERSION2 = 1; /* Created by Notes version 3 */
  byte CDGRAPHIC_VERSION3 = 2; /* Created by Notes version 4.5 */
  byte CDGRAPHIC_FLAG_DESTSIZE_IS_PIXELS = 0x01;
  byte CDGRAPHIC_FLAG_SPANSLINES = 0x02;
  short HOTSPOTREC_TYPE_POPUP = 1;
  short HOTSPOTREC_TYPE_HOTREGION = 2;
  short HOTSPOTREC_TYPE_BUTTON = 3;
  short HOTSPOTREC_TYPE_FILE = 4;
  short HOTSPOTREC_TYPE_SECTION = 7;
  short HOTSPOTREC_TYPE_ANY = 8;
  short HOTSPOTREC_TYPE_HOTLINK = 11;
  short HOTSPOTREC_TYPE_BUNDLE = 12;
  short HOTSPOTREC_TYPE_V4_SECTION = 13;
  short HOTSPOTREC_TYPE_SUBFORM = 14;
  short HOTSPOTREC_TYPE_ACTIVEOBJECT = 15;
  short HOTSPOTREC_TYPE_OLERICHTEXT = 18;
  short HOTSPOTREC_TYPE_EMBEDDEDVIEW = 19; /* embedded view */
  short HOTSPOTREC_TYPE_EMBEDDEDFPANE = 20; /* embedded folder pane */
  short HOTSPOTREC_TYPE_EMBEDDEDNAV = 21; /* embedded navigator */

  short HOTSPOTREC_TYPE_MOUSEOVER = 22;
  short HOTSPOTREC_TYPE_FILEUPLOAD = 24; /* file upload placeholder */
  short HOTSPOTREC_TYPE_EMBEDDEDOUTLINE = 27; /* embedded outline */
  short HOTSPOTREC_TYPE_EMBEDDEDCTL = 28; /* embedded control window */
  short HOTSPOTREC_TYPE_EMBEDDEDCALENDARCTL = 30; /* embedded calendar control (date picker) */
  short HOTSPOTREC_TYPE_EMBEDDEDSCHEDCTL = 31; /* embedded scheduling control */
  short HOTSPOTREC_TYPE_RCLINK = 32; /* Not a new type, but renamed for V5 terms*/
  short HOTSPOTREC_TYPE_EMBEDDEDEDITCTL = 34; /* embedded editor control */
  short HOTSPOTREC_TYPE_CONTACTLISTCTL = 36; /* Embeddeble buddy list */
  int HOTSPOTREC_RUNFLAG_BEGIN = 0x00000001;
  int HOTSPOTREC_RUNFLAG_END = 0x00000002;
  int HOTSPOTREC_RUNFLAG_BOX = 0x00000004;
  int HOTSPOTREC_RUNFLAG_NOBORDER = 0x00000008;

  int HOTSPOTREC_RUNFLAG_FORMULA = 0x00000010; /*	Popup is a formula, not text. */
  int HOTSPOTREC_RUNFLAG_MOVIE = 0x00000020; /*	File is a QuickTime movie. */
  int HOTSPOTREC_RUNFLAG_IGNORE = 0x00000040; /*	Run is for backward compatibility
                                              (i.e. ignore the run)
                                              */
  int HOTSPOTREC_RUNFLAG_ACTION = 0x00000080; /*	Hot region executes a canned action	*/
  int HOTSPOTREC_RUNFLAG_SCRIPT = 0x00000100; /*	Hot region executes a script.	*/
  int HOTSPOTREC_RUNFLAG_INOTES = 0x00001000;
  int HOTSPOTREC_RUNFLAG_ISMAP = 0x00002000;

  int HOTSPOTREC_RUNFLAG_INOTES_AUTO = 0x00004000;

  int HOTSPOTREC_RUNFLAG_ISMAP_INPUT = 0x00008000;

  /*	CDCAPTION - Text to display with an object (e.g., a graphic) */

  int HOTSPOTREC_RUNFLAG_SIGNED = 0x00010000;
  int HOTSPOTREC_RUNFLAG_ANCHOR = 0x00020000;

  /* Use this flag to tell the run context that when it runs an
  agent, you want it to check the privileges of the signer of
  that agent and apply them. For example, if the signer of the
  agent has "restricted" agent privileges, then the agent will
  be restricted. If you don't set this flag, all agents run as
  unrestricted.

  List of security checks enabled by this flag:
  	Restricted/unrestricted agent
  	Can create databases
  	Is agent targeted to this machine
  	Is user allowed to access this machine
  	Can user run personal agents
   */

  int HOTSPOTREC_RUNFLAG_COMPUTED = 0x00040000; /*	Used in conjunction
                                                with computed hotspots.
                                                */
  int HOTSPOTREC_RUNFLAG_TEMPLATE = 0x00080000; /*	used in conjunction
                                                with embedded navigator
                                                panes.
                                                */
  int HOTSPOTREC_RUNFLAG_HIGHLIGHT = 0x00100000;

  /* Definitions for stdout redirection types. This specifies where
  output from the LotusScript "print" statement will go */

  int HOTSPOTREC_RUNFLAG_EXTACTION = 0x00200000; /*  Hot region executes an extended action */
  int HOTSPOTREC_RUNFLAG_NAMEDELEM = 0x00400000; /*	Hot link to a named element */
  /*	Allow R6 dual action type buttons, e.g. client LotusScript, web JS */
  int HOTSPOTREC_RUNFLAG_WEBJAVASCRIPT = 0x00800000;
  int HOTSPOTREC_RUNFLAG_ODSMASK = 0x00FFFFFC; /*	Mask for bits stored on disk*/

  /*	Open Flag Definitions.  These flags are passed to NSFNoteOpen. */

  byte CAPTION_POSITION_BELOW_CENTER = 0; /*	Centered below object */
  byte CAPTION_POSITION_MIDDLE_CENTER = 1; /*	Centered on object */
  int AGENT_SECURITY_OFF = 0x00; /* CreateRunContext */
  int AGENT_SECURITY_ON = 0x01; /* CreateRunContext */
  int AGENT_REOPEN_DB = 0x10; /* AgentRun */
  short AGENT_REDIR_NONE = 0; /* goes to the bit bucket */
  short AGENT_REDIR_LOG = 1; /* goes to the Notes log (default) */
  short AGENT_REDIR_MEMORY = 2; /* goes to a memory buffer, cleared each AgentRun */
  short AGENT_REDIR_MEMAPPEND = 3; /* goes to buffer, append mode for each agent */
  /* open only summary info */
  int OPEN_SUMMARY = 0x0001;
  /* don't bother verifying default bit */
  int OPEN_NOVERIFYDEFAULT = 0x0002;
  /* expand data while opening */
  int OPEN_EXPAND = 0x0004;
  /* don't include any objects */
  int OPEN_NOOBJECTS = 0x0008;

  /* open in a "shared" memory mode */
  int OPEN_SHARE = 0x0020;

  /* Return ALL item values in canonical form */
  int OPEN_CANONICAL = 0x0040;
  /* Mark unread if unread list is currently associated */
  int OPEN_MARK_READ = 0x0100;
  /* Only open an abstract of large documents */
  int OPEN_ABSTRACT = 0x0200;
  /* Return response ID table */
  int OPEN_RESPONSE_ID_TABLE = 0x1000;
  /* Include folder objects - default is not to */
  int OPEN_WITH_FOLDERS = 0x00020000;

  /*	Options to MQGet */

  /* If specified, the open will check to see if this note had already been read and
   * saved in memory.  If not, and the database is server	based, we will also check
   * the on-disk cache.  If the note is not found, it is cached in memory and at some
   * time in the future commited to a local on disk cache.
   * The notes are guaranteed to be as up to date as the last time NSFValidateNoteCache was called.#
   * Minimally, this should be called	the 1st time a database is opened prior to specifying
   * this flag. */
  int OPEN_CACHE = 0x00100000;

  /* Options to MQOpen */

  /* If set, leave TYPE_RFC822_TEXT items in native
  format.  Otherwise, convert to TYPE_TEXT/TYPE_TIME. */
  int OPEN_RAW_RFC822_TEXT = 0x01000000;

  /*	Public Queue Names */

  /* If set, leave TYPE_MIME_PART items in native
  format.  Otherwise, convert to TYPE_COMPOSITE. */
  int OPEN_RAW_MIME_PART = 0x02000000;

  int OPEN_RAW_MIME = NotesConstants.OPEN_RAW_RFC822_TEXT | NotesConstants.OPEN_RAW_MIME_PART;
  /* Message queue flags and constants */
  int MAXONESEGSIZE = 0xffff - 1 - 128;
  int MQ_MAX_MSGSIZE = NotesConstants.MAXONESEGSIZE - 0x50;
  short NOPRIORITY = (short) (0xffff & 0xffff);
  short LOWPRIORITY = (short) (0xffff & 0xffff);
  short HIGHPRIORITY = 0;
  short MQ_WAIT_FOR_MSG = 0x0001;
  /** Create the queue if it doesn't exist */
  int MQ_OPEN_CREATE = 0x00000001;
  /** Prepended to "addin" task name to form task's queue name */
  String TASK_QUEUE_PREFIX = "MQ$";			 //$NON-NLS-1$
  /** DB Server */
  String SERVER_QUEUE_NAME = "_SERVER";			 //$NON-NLS-1$
  /** Replicator */
  String REPL_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "REPLICATOR"; //$NON-NLS-1$
  /** Mail Router */
  String ROUTER_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "ROUTER"; //$NON-NLS-1$
  /** Index views &amp; full text process */
  String UPDATE_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "INDEXER"; //$NON-NLS-1$
  /** Login Process */
  String LOGIN_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "LOGIN"; //$NON-NLS-1$
  /** Event process */
  String EVENT_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "EVENT"; //$NON-NLS-1$
  /** Report process */
  String REPORT_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "REPORTER"; //$NON-NLS-1$
  /** Cluster Replicator */
  String CLREPL_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "CLREPL"; //$NON-NLS-1$
  /** Fixup */
  String FIXUP_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "FIXUP"; //$NON-NLS-1$
  /** Collector */
  String COLLECT_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "COLLECTOR"; //$NON-NLS-1$
  /** NOI Process */
  String NOI_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "DIIOP"; //$NON-NLS-1$
  /** Alarms Cache daemon */
  String ALARM_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "ALARMS"; //$NON-NLS-1$
  /** Monitor */
  String MONITOR_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "MONITOR"; //$NON-NLS-1$
  /** Monitor */
  String MONALARM_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "MONITORALARM"; //$NON-NLS-1$
  /** Admin Panel Daemon (Request Queue) */
  String APDAEMON_REQ_QUEUE = NotesConstants.TASK_QUEUE_PREFIX + "APDAEMONREQ"; //$NON-NLS-1$
  /** Admin Panel Daemon (File Response Queue) */
  String APDAEMON_FILERES_QUEUE = NotesConstants.TASK_QUEUE_PREFIX + "APDAEMONFILERESPONSE"; //$NON-NLS-1$
  /** Admin Panel Daemon (Server Response Queue) */
  String APDAEMON_FILEREQ_QUEUE = NotesConstants.TASK_QUEUE_PREFIX + "APDAEMONFILEREQUEST"; //$NON-NLS-1$
  /** bktasks */
  String BKTASKS_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "BKTASKS"; //$NON-NLS-1$
  /** Red Zone Interface to Collector */
  String RZINTER_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "RZINTER"; //$NON-NLS-1$
  /** Red Zone Extra MQ */
  String RZEXTRA_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "RZEXTRA"; //$NON-NLS-1$

  /** Red Zone Background MQ */
  String RZBG_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "RZBG"; //$NON-NLS-1$
  /** Red Zone Background Extra MQ */
  String RZBGEXTRA_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "RZBGEXTRA"; //$NON-NLS-1$

  /** Monitor */
  String REALTIME_STATS_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "REALTIME"; //$NON-NLS-1$
  /** Runjava (used by ISpy) */
  String RUNJAVA_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "RUNJAVA"; //$NON-NLS-1$

  /** Runjava (used by ISpy) */
  String STATS_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "STATS"; //$NON-NLS-1$

  /** Runjava (used by ISpy) */
  String LOG_SEARCH_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "LOGSEARCH"; //$NON-NLS-1$
  /** Event process */
  String DAEMON_EVENT_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "DAEMONEVENT"; //$NON-NLS-1$
  /** Collector */
  String DAEMON_COLLECT_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "DAEMONCOLLECTOR"; //$NON-NLS-1$

  /** Dircat */
  String DIRCAT_QUEUE_NAME = NotesConstants.TASK_QUEUE_PREFIX + "DIRCAT"; //$NON-NLS-1$
  /** Open the Stream for Read */
  int MIME_STREAM_OPEN_READ = 0x00000001;
  /** Open the Stream for Write */
  int MIME_STREAM_OPEN_WRITE = 0x00000002;

  /** Include MIME Headers */
  int MIME_STREAM_MIME_INCLUDE_HEADERS = 0x00000010;

  /*	Definitions for NSFDbGetMultNoteInfo and NSFDbGetMultNoteInfoByUNID */

  /** Include RFC822 Headers */
  int MIME_STREAM_RFC2822_INCLUDE_HEADERS = 0x00000020;
  /** Include RFC822, MIME Headers */
  int MIME_STREAM_INCLUDE_HEADERS = NotesConstants.MIME_STREAM_MIME_INCLUDE_HEADERS
      | NotesConstants.MIME_STREAM_RFC2822_INCLUDE_HEADERS;
  int MIME_STREAM_SUCCESS = 0;
  int MIME_STREAM_EOS = 1;
  int MIME_STREAM_IO = 2;
  /*	Define the MIME stream itemize options. */
  int MIME_STREAM_NO_DELETE_ATTACHMENTS = 0x00000001;
  int MIME_STREAM_ITEMIZE_HEADERS = 0x00000002;

  int MIME_STREAM_ITEMIZE_BODY = 0x00000004;
  int MIME_STREAM_ITEMIZE_FULL = NotesConstants.MIME_STREAM_ITEMIZE_HEADERS | NotesConstants.MIME_STREAM_ITEMIZE_BODY;
  /** Return NoteID */
  short fINFO_NOTEID = 0x0001;
  /** Return SequenceTime from OID */
  short fINFO_SEQTIME = 0x0002;
  /** Return Sequence number from OID */
  short fINFO_SEQNUM = 0x0004;
  /** Return OID (disables SeqTime &amp; number &amp; UNID) */
  short fINFO_OID = 0x0008;
  /** Compress non-existent UNIDs */
  short fINFO_COMPRESS = 0x0040;
  /** Return UNID */
  short fINFO_UNID = 0x0080;
  /** Allow the returned buffer to exceed 64k. */
  short fINFO_ALLOW_HUGE = 0x0400;
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
  short NONS_NOASSIGN = (short) (0x8000 & 0xffff);
  /** Named Object "User Unread ID Table" Name Space */
  short NONS_USER_UNREAD = 0;

  /** Named Note */
  short NONS_NAMED_NOTE = 1;
  /** Named Object "User NameList" name space */
  short NONS_USER_NAMELIST = 2;
  /** Named object - Folder Directory Object */
  short NONS_FDO = 3;

  /*	Define NSF DB Classes - These all begin with 0xf000 for no good
  reason other than to ENSURE that callers of NSFDbCreate call the
  routine with valid parameters, since in earlier versions of NSF
  the argument to the call was typically 0. */

  /** Named object - Execution Control List object */
  short NONS_ECL_OBJECT = 4;

  /** Named object - design note (exists in ODS37+). */
  short NONS_DESIGN_NOTE = 5;
  /** Named object - IMAP visable folders (exists in build 166+) */
  short NONS_IMAP_FOLDERS = 6;
  /** Named object - activity log for unread marks */
  short NONS_USER_UNREAD_ACTIVITY_LOG = 7;
  /** Named object - DAOS pending object delete */
  short NONS_DAOS_DELETED = 8;
  /** Named object - DAOS pending object delete */
  short NONS_DAOS_OBJECT = 9;
  /** Return status of lock */
  int NOTE_LOCK_STATUS = 0x00000008;
  /** Take out a hard note lock */
  int NOTE_LOCK_HARD = 0x00000010;
  /** Take out a provisional hard note lock */
  int NOTE_LOCK_PROVISIONAL = 0x00000020;
  /* The type of the database is determined by the filename extension.
   * The extensions and their database classes are .NSX (NSFTESTFILE),
   * .NSF (NOTEFILE), .DSK (DESKTOP), .NCF (NOTECLIPBOARD), .NTF (TEMPLATEFILE),
   * .NSG (GIANTNOTEFILE), .NSH (HUGENOTEFILE), NTD (ONEDOCFILE),
   * NS2 (V2NOTEFILE), NTM (ENCAPSMAILFILE). */
  short DBCLASS_BY_EXTENSION = 0;
  /** A test database. */
  short DBCLASS_NSFTESTFILE = (short) (0xff00 & 0xffff);
  /** A standard Domino database. */
  short DBCLASS_NOTEFILE = (short) (0xff01 & 0xffff);
  /** A Notes desktop (folders, icons, etc.). */
  short DBCLASS_DESKTOP = (short) (0xff02 & 0xffff);
  /** A Notes clipboard (used for cutting and pasting). */
  short DBCLASS_NOTECLIPBOARD = (short) (0xff03 & 0xffff);
  /**
   * A database that contains every type of note (forms, views, ACL, icon, etc.)
   * except data notes.
   */
  short DBCLASS_TEMPLATEFILE = (short) (0xff04 & 0xffff);
  /**
   * A standard Domino database, with size up to 1 GB. This was used
   * in Notes Release 3 when the size of a previous version of a database had been
   * limited to 200 MB.
   */
  short DBCLASS_GIANTNOTEFILE = (short) (0xff05 & 0xffff);
  /**
   * A standard Domino database, with size up to 1 GB. This was used in Notes
   * Release
   * 3 when the size of a previous version of a database had been limited to 300
   * MB.
   */
  short DBCLASS_HUGENOTEFILE = (short) (0xff06 & 0xffff);
  /**
   * One document database with size up to 10MB. Specifically used by alternate
   * mail to create an encapsulated database. Components of the document are
   * further limited in size. It is not recommended that you use this database
   * class with NSFDbCreate. If you do, and you get an error when saving the
   * document,
   * you will need to re-create the database using DBCLASS_NOTEFILE.
   */
  short DBCLASS_ONEDOCFILE = (short) (0xff07 & 0xffff);
  /** Database was created as a Notes Release 2 database. */
  short DBCLASS_V2NOTEFILE = (short) (0xff08 & 0xffff);
  /**
   * One document database with size up to 5MB. Specifically used by alternate
   * mail
   * to create an encapsulated database. Components of the document are further
   * limited in size. It is not recommended that you use this database class with
   * NSFDbCreate. If you do, and you get an error when saving the document, you
   * will
   * need to re-create the database using DBCLASS_NOTEFILE.
   */
  short DBCLASS_ENCAPSMAILFILE = (short) (0xff09 & 0xffff);
  /**
   * Specifically used by alternate mail. Not recomended for use with NSFDbCreate.
   */
  short DBCLASS_LRGENCAPSMAILFILE = (short) (0xff0a & 0xffff);
  /** Database was created as a Notes Release 3 database. */
  short DBCLASS_V3NOTEFILE = (short) (0xff0b & 0xffff);

  /** Object store. */
  short DBCLASS_OBJSTORE = (short) (0xff0c & 0xffff);
  /**
   * One document database with size up to 10MB. Specifically used by Notes
   * Release 3
   * alternate mail to create an encapsulated database. Not recomended for use
   * with NSFDbCreate.
   */
  short DBCLASS_V3ONEDOCFILE = (short) (0xff0d & 0xffff);

  /* 	Option flags for NSFDbCreateExtended */

  /** Database was created specifically for Domino and Notes Release 4. */
  short DBCLASS_V4NOTEFILE = (short) (0xff0e & 0xffff);
  /** Database was created specifically for Domino and Notes Release 5. */
  short DBCLASS_V5NOTEFILE = (short) (0xff0f & 0xffff);
  /**
   * Database was created specifically for Domino and Notes Release Notes/Domino
   * 6.
   */
  short DBCLASS_V6NOTEFILE = (short) (0xff10 & 0xffff);
  /**
   * Database was created specifically for Domino and Notes Release Notes/Domino
   * 8.
   */
  short DBCLASS_V8NOTEFILE = (short) (0xff11 & 0xffff);
  /**
   * Database was created specifically for Domino and Notes Release Notes/Domino
   * 8.5.
   */
  short DBCLASS_V85NOTEFILE = (short) (0xff12 & 0xffff);
  /**
   * Database was created specifically for Domino and Notes Release Notes/Domino
   * 9.
   */
  short DBCLASS_V9NOTEFILE = (short) (0xff13 & 0xffff);
  /**
   * Database was created specifically for Domino and Notes Release Notes/Domino
   * 10.
   */
  short DBCLASS_V10NOTEFILE = (short) (0xff14 & 0xffff);
  short DBCLASS_MASK = 0x00ff & 0xffff;
  short DBCLASS_VALID_MASK = (short) (0xff00 & 0xffff);
  /** Create a locally encrypted database. */
  short DBCREATE_LOCALSECURITY = 0x0001;
  /** NSFNoteUpdate will not use an object store for notes in the database. */
  short DBCREATE_OBJSTORE_NEVER = 0x0002;

  /* Values for EncryptStrength of NSFDbCreateExtended */

  /** The maximum database length is specified in bytes in NSFDbCreateExtended. */
  short DBCREATE_MAX_SPECIFIED = 0x0004;
  /** Don't support note hierarchy - ODS21 and up only */
  short DBCREATE_NORESPONSE_INFO = 0x0010;
  /** Don't maintain unread lists for this DB */
  short DBCREATE_NOUNREAD = 0x0020;
  /** Skip overwriting freed disk buffer space */
  short DBCREATE_NO_FREE_OVERWRITE = 0x0200;
  /** Maintain form/bucket bitmap */
  short DBCREATE_FORM_BUCKET_OPT = 0x0400;
  /** Disable transaction logging for this database if specified */
  short DBCREATE_DISABLE_TXN_LOGGING = 0x0800;

  /* Option flags for NSFDbCreateAndCopy */

  /** Enable maintaining last accessed time */
  short DBCREATE_MAINTAIN_LAST_ACCESSED = 0x1000;
  /** TRUE if database is a mail[n].box database */
  short DBCREATE_IS_MAILBOX = 0x4000;
  /** TRUE if database should allow "large" (&lt;64K bytes) UNK table */
  short DBCREATE_LARGE_UNKTABLE = (short) (0x8000 & 0xffff);
  byte DBCREATE_ENCRYPT_NONE = 0x00;
  byte DBCREATE_ENCRYPT_SIMPLE = 0x01;
  byte DBCREATE_ENCRYPT_MEDIUM = 0x02;
  byte DBCREATE_ENCRYPT_STRONG = 0x03;
  byte DBCREATE_ENCRYPT_AES128 = 0x04;
  byte DBCREATE_ENCRYPT_AES256 = 0x05;
  int DBCOPY_REPLICA = 0x00000001;
  int DBCOPY_SUBCLASS_TEMPLATE = 0x00000002;
  int DBCOPY_DBINFO2 = 0x00000004;
  int DBCOPY_SPECIAL_OBJECTS = 0x00000008;
  int DBCOPY_NO_ACL = 0x00000010;
  int DBCOPY_NO_FULLTEXT = 0x00000020;
  int DBCOPY_ENCRYPT_SIMPLE = 0x00000040;

  /*	Collection navigation directives */

  int DBCOPY_ENCRYPT_MEDIUM = 0x00000080;
  int DBCOPY_ENCRYPT_STRONG = 0x00000100;
  int DBCOPY_KEEP_NOTE_MODTIME = 0x00000200;
  int DBCOPY_REPLICA_NAMELIST = 0x01000000; /* Copy the NameList (applicable only when DBCOPY_REPLICA is specified) */
  int DBCOPY_DEST_IS_NSF = 0x02000000; /* Destination is NSF-backed database */
  int DBCOPY_DEST_IS_DB2 = 0x04000000; /* Destination is DB2-backed database */
  int DBCOPY_OVERRIDE_DEST = 0x08000000; /* Destination should override default if able to */
  int DBCOPY_DBCLASS_HIGHEST_NOTEFILE = 0x10000000; /* Create Db using the latest ODS, regardless of INI settings */
  int DBCOPY_COMPACT_REPLICA = 0x20000000; /* Create Db for copy style compaction */
  short NAVIGATE_CURRENT = 0; /* Remain at current position */
  /* (reset position & return data) */
  short NAVIGATE_PARENT = 3; /* Up 1 level */
  short NAVIGATE_CHILD = 4; /* Down 1 level to first child */

  short NAVIGATE_NEXT_PEER = 5; /* Next node at our level */
  short NAVIGATE_PREV_PEER = 6; /* Prev node at our level */
  short NAVIGATE_FIRST_PEER = 7; /* First node at our level */
  short NAVIGATE_LAST_PEER = 8; /* Last node at our level */
  short NAVIGATE_CURRENT_MAIN = 11; /* Highest level non-category entry */
  short NAVIGATE_NEXT_MAIN = 12; /* CURRENT_MAIN, then NEXT_PEER */
  short NAVIGATE_PREV_MAIN = 13; /* CURRENT_MAIN, then PREV_PEER only if already there */
  short NAVIGATE_NEXT_PARENT = 19; /* PARENT, then NEXT_PEER */
  short NAVIGATE_PREV_PARENT = 20; /* PARENT, then PREV_PEER */
  short NAVIGATE_NEXT = 1; /* Next entry over entire tree */
  /* (parent first, then children,...) */
  short NAVIGATE_PREV = 9; /* Previous entry over entire tree */
  /* (opposite order of PREORDER) */
  short NAVIGATE_ALL_DESCENDANTS = 17; /* NEXT, but only descendants */
  /* below NIFReadEntries StartPos */
  short NAVIGATE_NEXT_UNREAD = 10; /* NEXT, but only "unread" entries */
  short NAVIGATE_NEXT_UNREAD_MAIN = 18; /* NEXT_UNREAD, but stop at main note also */
  short NAVIGATE_PREV_UNREAD_MAIN = 34; /* Previous unread main. */
  short NAVIGATE_PREV_UNREAD = 21; /* PREV, but only "unread" entries */
  short NAVIGATE_NEXT_SELECTED = 14; /* NEXT, but only "selected" entries */
  short NAVIGATE_PREV_SELECTED = 22; /* PREV, but only "selected" entries */
  short NAVIGATE_NEXT_SELECTED_MAIN = 32; /* Next selected main. (Next unread */
  /* main can be found above.) */
  short NAVIGATE_PREV_SELECTED_MAIN = 33; /* Previous selected main. */
  short NAVIGATE_NEXT_EXPANDED = 15; /* NEXT, but only "expanded" entries */
  short NAVIGATE_PREV_EXPANDED = 16; /* PREV, but only "expanded" entries */
  short NAVIGATE_NEXT_EXPANDED_UNREAD = 23; /* NEXT, but only "expanded" AND "unread" entries */
  short NAVIGATE_PREV_EXPANDED_UNREAD = 24; /* PREV, but only "expanded" AND "unread" entries */
  short NAVIGATE_NEXT_EXPANDED_SELECTED = 25; /* NEXT, but only "expanded" AND "selected" entries */
  short NAVIGATE_PREV_EXPANDED_SELECTED = 26; /* PREV, but only "expanded" AND "selected" entries */
  short NAVIGATE_NEXT_EXPANDED_CATEGORY = 27; /* NEXT, but only "expanded" AND "category" entries */
  short NAVIGATE_PREV_EXPANDED_CATEGORY = 28; /* PREV, but only "expanded" AND "category" entries */
  short NAVIGATE_NEXT_EXP_NONCATEGORY = 39; /* NEXT, but only "expanded" "non-category" entries */
  short NAVIGATE_PREV_EXP_NONCATEGORY = 40; /* PREV, but only "expanded" "non-category" entries */
  short NAVIGATE_NEXT_HIT = 29; /* NEXT, but only FTSearch "hit" entries */
  /* (in the SAME ORDER as the hit's relevance ranking) */
  short NAVIGATE_PREV_HIT = 30; /* PREV, but only FTSearch "hit" entries */

  /* (in the SAME ORDER as the hit's relevance ranking) */
  short NAVIGATE_CURRENT_HIT = 31; /* Remain at current position in hit's relevance rank array */
  /* (in the order of the hit's relevance ranking) */
  short NAVIGATE_NEXT_SELECTED_HIT = 35; /* NEXT, but only "selected" and FTSearch "hit" entries */

  /* (in the SAME ORDER as the hit's relevance ranking) */
  short NAVIGATE_PREV_SELECTED_HIT = 36; /* PREV, but only "selected" and FTSearch "hit" entries */

  /* (in the SAME ORDER as the hit's relevance ranking) */
  short NAVIGATE_NEXT_UNREAD_HIT = 37; /* NEXT, but only "unread" and FTSearch "hit" entries */

  /*	Flag which can be used with ALL navigators which causes the navigation
  	to be limited to entries at a specific level (specified by the
  	field "MinLevel" in the collection position) or any higher levels
  	but never a level lower than the "MinLevel" level.  Note that level 0
  	means the top level of the index, so the term "minimum level" really
  	means the "highest level" the navigation can move to.
  	This can be used to find all entries below a specific position
  	in the index, limiting yourself only to that subindex, and yet be
  	able to use any of the navigators to move around within that subindex.
  	This feature was added in Version 4 of Notes, so it cannot be used
  	with earlier Notes Servers. */

  /* (in the SAME ORDER as the hit's relevance ranking) */
  short NAVIGATE_PREV_UNREAD_HIT = 38; /* PREV, but only "unread" and FTSearch "hit" entries */
  /* (in the SAME ORDER as the hit's relevance ranking) */
  short NAVIGATE_NEXT_CATEGORY = 41; /* NEXT, but only "category" entries */

  short NAVIGATE_PREV_CATEGORY = 42; /* PREV, but only "category" entries */

  /* Flag that can be combined with a navigation directive to cause
   * the search to be limited based on the top level tumbler position. */

  short NAVIGATE_NEXT_NONCATEGORY = 43; /* NEXT, but only "non-category" entries */

  /*  This flag can be combined with any navigation directive to
  	prevent having a navigation (Skip) failure abort the (ReadEntries) operation.
  	For example, this is used by the Notes user interface when
  	getting the entries to display in the view, so that if an attempt is made to
  	skip past either end of the index (e.g. using PageUp/PageDown),
  	the skip will be left at the end of the index, and the return will return
  	whatever can be returned using the separate return navigator.

  	This flag is also used to get the "last" N entries of a view by setting the
  	Skip Navigator to NAVIGATE_NEXT | NAVIGATE_CONTINUE, setting the SkipCount to MAXDWORD,
  	setting the ReturnNavigator to NAVIGATE_PREV_EXPANDED, and setting the ReturnCount
  	to N (N must be greater than 0). */

  short NAVIGATE_PREV_NONCATEGORY = 44; /* PREV, but only "non-category" entries */

  /*	Define search results data structure */

  short NAVIGATE_PREV_MAIN_ALWAYS = 45; /* CURRENT_MAIN, then PREV_PEER regardless if already there */
  short NAVIGATE_NEXT_NONCATEGORY_NONRESP = 46; /* Same as NEXT_NONCATEGORY but skip responses also */
  short NAVIGATE_ALL_DESCENDANTS_ACCESS = 47; /* Same as NAVIGATE_ALL_DESCENDANTS but navigate/check
                                              /* for one or more children this user has access, */
  /* with proper reader access checking etc. */
  /* Note: This will over ride the Maxlevel Navigation setting for access checking. */

  short NAVIGATE_MASK = 0x007F; /* Navigator code (see above) */
  short NAVIGATE_MINLEVEL = 0x0100; /* Honor "Minlevel" field in position */
  short NAVIGATE_MAXLEVEL = 0x0200; /* Honor "Maxlevel" field in position */
  /* When parents are in the select list, autoexpand immediate children */
  short NAVIGATE_AUTOEXPAND_SELECTED = 0x0400;
  short NAVIGATE_WITHIN_MAIN = 0x4000; /* Don't navigate to next/prev main */
  short NAVIGATE_CONTINUE = (short) (0x8000 & 0xffff); /* "Return" even if "Skip" error */
  /** Array of scores follows */
  short FT_RESULTS_SCORES = 0x0001;
  /** Search results are series of FT_SEARCH_RESULT_ENTRY structures */
  short FT_RESULTS_EXPANDED = 0x0002;
  /** Url expanded format returned by FTSearchExt only */
  short FT_RESULTS_URL = 0x0004;

  /* UnreadList has been modified */
  short FILTER_UNREAD = 0x0001;
  /* CollpasedList has been modified */
  short FILTER_COLLAPSED = 0x0002;

  /* SelectedList has been modified */
  short FILTER_SELECTED = 0x0004;
  /* UNID table has been modified. */
  short FILTER_UNID_TABLE = 0x0008;

  /* Conditionaly do FILTER_UNREAD if current unread list indicates it - see NSFDbUpdateUnread */
  short FILTER_UPDATE_UNREAD = 0x0010;

  /* Mark specified ID table Read */
  short FILTER_MARK_READ = 0x0020;
  /* Mark specified ID table Unread */
  short FILTER_MARK_UNREAD = 0x0040;
  /* Mark all read */
  short FILTER_MARK_READ_ALL = 0x0080;
  /* Mark all unread */
  short FILTER_MARK_UNREAD_ALL = 0x0100;

  /* Note is shared (always located in the database) */
  int DESIGN_TYPE_SHARED = 0;
  /* Note is private and is located in the database */
  int DESIGN_TYPE_PRIVATE_DATABASE = 1;
  /** Cascade can go only one level deep parent\sub */
  int DESIGN_LEVELS = 2;
  /** Maximum size of a level */
  int DESIGN_LEVEL_MAX = 64;

  /**
   * Guaranteed to be the greatest of Form, View or Macro
   * length. NOTE: We need
   * space for LEVELS-1 cascade
   * characters and a NULL term.
   * The +1 takes care of that.
   */
  int DESIGN_NAME_MAX = (NotesConstants.DESIGN_LEVEL_MAX + 1) * NotesConstants.DESIGN_LEVELS;

  /*	Define flags for NSFFolderGetIDTable */

  /** Forms can cascade a level */
  int DESIGN_FORM_MAX = NotesConstants.DESIGN_NAME_MAX;

  /** Views can cascade a level */
  int DESIGN_VIEW_MAX = NotesConstants.DESIGN_NAME_MAX;
  /** Macros can cascade a level */
  int DESIGN_MACRO_MAX = NotesConstants.DESIGN_NAME_MAX;
  /** Fields cannot cascade */
  int DESIGN_FIELD_MAX = NotesConstants.DESIGN_LEVEL_MAX + 1;
  /** Design element comment max size. */
  int DESIGN_COMMENT_MAX = 256;
  /** All names, including sysnonyms */
  int DESIGN_ALL_NAMES_MAX = 256;
  /** Same as for views */
  int DESIGN_FOLDER_MAX = NotesConstants.DESIGN_VIEW_MAX;

  /** Same as for views */
  int DESIGN_FOLDER_MAX_NAME = NotesConstants.DESIGN_LEVEL_MAX;
  int DESIGN_FLAGS_MAX = 32;
  /** If set, return only "validated" noteIDs */
  int DB_GETIDTABLE_VALIDATE = 0x00000001;
  int HTMLAPI_PROP_TEXTLENGTH = 0;
  int HTMLAPI_PROP_NUMREFS = 1;
  int HTMLAPI_PROP_USERAGENT_LEN = 3;
  int HTMLAPI_PROP_USERAGENT = 4;
  int HTMLAPI_PROP_BINARYDATA = 6;
  int HTMLAPI_PROP_MIMEMAXLINELENSEEN = 102;
  int CAI_Start = 0;
  int CAI_StartKey = 1;
  int CAI_Count = 2;
  int CAI_Expand = 3;
  int CAI_FullyExpand = 4;
  int CAI_ExpandView = 5;
  int CAI_Collapse = 6;
  int CAI_CollapseView = 7;
  int CAI_3PaneUI = 8;
  int CAI_TargetFrame = 9;
  int CAI_FieldElemType = 10;
  int CAI_FieldElemFormat = 11;
  int CAI_SearchQuery = 12;
  int CAI_OldSearchQuery = 13;
  int CAI_SearchMax = 14;
  int CAI_SearchWV = 15;
  int CAI_SearchOrder = 16;
  int CAI_SearchThesarus = 17;
  int CAI_ResortAscending = 18;
  int CAI_ResortDescending = 19;
  int CAI_ParentUNID = 20;
  int CAI_Click = 21;
  int CAI_UserName = 22;
  int CAI_Password = 23;
  int CAI_To = 24;
  int CAI_ISMAPx = 25;
  int CAI_ISMAPy = 26;
  int CAI_Grid = 27;
  int CAI_Date = 28;
  int CAI_TemplateType = 29;
  int CAI_TargetUNID = 30;
  int CAI_ExpandSection = 31;
  int CAI_Login = 32;
  int CAI_PickupCert = 33;
  int CAI_PickupCACert = 34;
  int CAI_SubmitCert = 35;
  int CAI_ServerRequest = 36;
  int CAI_ServerPickup = 37;
  int CAI_PickupID = 38;
  int CAI_TranslateForm = 39;
  int CAI_SpecialAction = 40;
  int CAI_AllowGetMethod = 41;
  int CAI_Seq = 42;
  int CAI_BaseTarget = 43;
  int CAI_ExpandOutline = 44;
  int CAI_StartOutline = 45;
  int CAI_Days = 46;
  int CAI_TableTab = 47;
  int CAI_MIME = 48;
  int CAI_RestrictToCategory = 49;
  int CAI_Highlight = 50;
  int CAI_Frame = 51;
  int CAI_FrameSrc = 52;
  int CAI_Navigate = 53;
  int CAI_SkipNavigate = 54;
  int CAI_SkipCount = 55;
  int CAI_EndView = 56;
  int CAI_TableRow = 57;
  int CAI_RedirectTo = 58;
  int CAI_SessionId = 59;
  int CAI_SourceFolder = 60;
  int CAI_SearchFuzzy = 61;
  int CAI_HardDelete = 62;
  int CAI_SimpleView = 63;
  int CAI_SearchEntry = 64;
  int CAI_Name = 65;
  int CAI_Id = 66;
  int CAI_RootAlias = 67;
  int CAI_Scope = 68;
  int CAI_DblClkTarget = 69;

  int CAI_Charset = 70;
  int CAI_EmptyTrash = 71;
  int CAI_EndKey = 72;
  int CAI_PreFormat = 73;
  int CAI_ImgIndex = 74;
  int CAI_AutoFramed = 75;
  int CAI_OutputFormat = 76;
  int CAI_InheritParent = 77;
  int CAI_Last = 78;
  int kUnknownCmdId = 0;
  int kOpenServerCmdId = 1;
  int kOpenDatabaseCmdId = 2;
  int kOpenViewCmdId = 3;
  int kOpenDocumentCmdId = 4;
  int kOpenElementCmdId = 5;
  int kOpenFormCmdId = 6;
  int kOpenAgentCmdId = 7;
  int kOpenNavigatorCmdId = 8;
  int kOpenIconCmdId = 9;
  int kOpenAboutCmdId = 10;
  int kOpenHelpCmdId = 11;
  int kCreateDocumentCmdId = 12;
  int kSaveDocumentCmdId = 13;
  int kEditDocumentCmdId = 14;
  int kDeleteDocumentCmdId = 15;
  int kSearchViewCmdId = 16;
  int kSearchSiteCmdId = 17;
  int kNavigateCmdId = 18;
  int kReadFormCmdId = 19;
  int kRequestCertCmdId = 20;
  int kReadDesignCmdId = 21;
  int kReadViewEntriesCmdId = 22;
  int kReadEntriesCmdId = 23;
  int kOpenPageCmdId = 24;
  int kOpenFrameSetCmdId = 25;
  /** OpenField command for Java applet(s) and HAPI */
  int kOpenFieldCmdId = 26;
  int kSearchDomainCmdId = 27;
  int kDeleteDocumentsCmdId = 28;
  int kLoginUserCmdId = 29;
  int kLogoutUserCmdId = 30;
  int kOpenImageResourceCmdId = 31;
  int kOpenImageCmdId = 32;
  int kCopyToFolderCmdId = 33;
  int kMoveToFolderCmdId = 34;
  int kRemoveFromFolderCmdId = 35;
  int kUndeleteDocumentsCmdId = 36;
  int kRedirectCmdId = 37;
  int kGetOrbCookieCmdId = 38;
  int kOpenCssResourceCmdId = 39;
  int kOpenFileResourceCmdId = 40;
  int kOpenJavascriptLibCmdId = 41;
  int kUnImplemented_01 = 42;
  int kChangePasswordCmdId = 43;
  int kOpenPreferencesCmdId = 44;

  int kOpenWebServiceCmdId = 45;
  int kWsdlCmdId = 46;
  int kGetImageCmdId = 47;
  int kNumberOfCmds = 48;
  /**
   * arg value is a pointer to a nul-terminated string
   */
  int CAVT_String = 0;
  /**
   * arg value is an int
   */
  int CAVT_Int = 1;
  /**
   * arg value is a NOTEID
   */
  int CAVT_NoteId = 2;
  /**
   * arg value is an UNID
   */
  int CAVT_UNID = 3;
  /**
   * arg value is a list of null-terminated strings
   */
  int CAVT_StringList = 4;
  int UAT_None = 0;
  int UAT_Server = 1;
  int UAT_Database = 2;
  int UAT_View = 3;
  int UAT_Form = 4;
  int UAT_Navigator = 5;
  int UAT_Agent = 6;
  int UAT_Document = 7;
  /** internal filename of attachment */
  int UAT_Filename = 8;
  /** external filename of attachment if different */
  int UAT_ActualFilename = 9;
  int UAT_Field = 10;
  int UAT_FieldOffset = 11;
  int UAT_FieldSuboffset = 12;
  int UAT_Page = 13;
  int UAT_FrameSet = 14;
  int UAT_ImageResource = 15;
  int UAT_CssResource = 16;

  int UAT_JavascriptLib = 17;
  int UAT_FileResource = 18;
  int UAT_About = 19;
  int UAT_Help = 20;
  int UAT_Icon = 21;
  int UAT_SearchForm = 22;
  int UAT_SearchSiteForm = 23;
  int UAT_Outline = 24;
  /** must be the last one */
  int UAT_NumberOfTypes = 25;
  int URT_None = 0;
  int URT_Name = 1;
  int URT_Unid = 2;
  int URT_NoteId = 3;
  int URT_Special = 4;
  int URT_RepId = 5;
  int USV_About = 0;
  int USV_Help = 1;
  int USV_Icon = 2;
  int USV_DefaultView = 3;
  int USV_DefaultForm = 4;
  int USV_DefaultNav = 5;
  int USV_SearchForm = 6;
  int USV_DefaultOutline = 7;
  int USV_First = 8;
  int USV_FileField = 9;
  int USV_NumberOfValues = 10;
  /**
   * unknown purpose
   */
  int HTMLAPI_REF_UNKNOWN = 0;

  /* wMailNoteFlags -- for extended control via MailNoteJitEx */

  /**
   * A tag HREF= value
   */
  int HTMLAPI_REF_HREF = 1;
  /**
   * IMG tag SRC= value
   */
  int HTMLAPI_REF_IMG = 2;

  /* wMailNoteFlags -- for extended control via MailNoteJitEx2 */

  /**
   * (I)FRAME tag SRC= value
   */
  int HTMLAPI_REF_FRAME = 3;
  /**
   * Java applet reference
   */
  int HTMLAPI_REF_APPLET = 4;
  /**
   * plugin SRC= reference
   */
  int HTMLAPI_REF_EMBED = 5;
  /**
   * active object DATA= referendce
   */
  int HTMLAPI_REF_OBJECT = 6;
  /**
   * BASE tag value
   */
  int HTMLAPI_REF_BASE = 7;
  /**
   * BODY BACKGROUND
   */
  int HTMLAPI_REF_BACKGROUND = 8;
  /**
   * IMG SRC= value from MIME message
   */
  int HTMLAPI_REF_CID = 9;
  /* mail the note if there is at least one recipient, i.e. To, CC, or BCC */
  short MAILNOTE_ANYRECIPIENT = 0x0001;

  /* enabled logging */
  short MAIL_JIT_LOG = 0x0002;
  /* caller is enforcing OSGetEnvironmentInt(SECUREMAIL) ... don't force MSN_SIGN OR MSN_SEAL */
  short MAILNOTE_NO_SECUREMAIL_MODE = 0x0004;
  /* use local mail.box, regardless of LOCINFO_REALMAILSERVER or LOCINFO_MAILSERVER setting. */
  short MAILNOTE_USELOCALMAILBOX = 0x0008;
  /* don't use $$LNABHAS* in place of actual cert  */
  short MAILNOTE_NO_LNAB_ENTRIES = 0x0010;

  /* don't have NAMELookup use local directories */
  short MAILNOTE_NO_SEARCH_LOCAL_DIRECTORIES = 0x0020;
  /* if recips only have Notes certs, do Notes encryption of MIME message */
  short MAILNOTE_NOTES_ENCRYPT_MIME = 0x0040;
  /* message is in mime format */
  short MAILNOTE_MIMEBODY = 0x0080;
  /* use X509 cert if it's the only one found */
  short MAILNOTE_CANUSEX509 = 0x0100;
  short MAILNOTE_SKIP_LOOKUP = 0x0200;

  /* license holders. */

  /* mail note that is not a jit canidate*/
  short MAIL_NO_JIT = 0;
  /* mail a JIT canidate note*/
  short MAIL_JIT = 1;
  /* mail a miem message that is not a jit canidate*/
  short MAIL_MIME_NO_JIT = 2;
  /* mail a note that is not a JIT, but caller has set recipient's field */
  short MAIL_NO_JIT_RECIPIENTS_DONE = 3;
  /* Query for Sign/Seal */
  short MSN_QUERY = 0x0001;

  /* The mailer should process the */
  /* note, skip recipient work */
  /* and prompt the user for */
  /* addressing. */

  /* Sign */
  short MSN_SIGN = 0x0002;

  /* and refresh the mail addresses */
  /* via lookup */

  /* Seal */
  short MSN_SEAL = 0x0004;

  /* Use results of previous query */
  short MSN_PREVQUERY = 0x0008;

  /* Must send to North American */
  short MSN_AMERICAN_ONLY = 0x0010;
  /* Recipient must have valid */
  short MSN_PUBKEY_ONLY = 0x0020;

  /* Sending a receit message. */
  short MSN_RECEIPT = 0x0040;

  /* The dialog is for a DDE request */
  short MSN_DDEDIALOG = 0x0080;

  /* Don't allow mail encryption */
  short MSN_NOSEAL = 0x0100;
  /* Used by alternate mail */
  short MSN_FWDASATT = 0x0200;
  /* Disregard all other flags */
  short MSN_ADDRESS_ONLY = 0x0400;
  /* Don't lookup the supplied names */
  short MSN_NOLOOKUP = 0x0800;
  /*  MailForwardNoteNoEdit only */

  /* Used by alternate mail */
  short MSN_FWDASTEXT = 0x1000;
  /* The mailer should process the */
  /* note, skip recipient work */
  /* and prompt the user for */
  /* addressing. */

  /* Indicates that the From field has */
  /* already been set up and that the */
  /* mailer should not slam it with the */
  /* user name. This is used by agents */
  /* running on the server */
  short MSN_FROMOVERRIDE = 0x2000;
  /* deposit in smtp.box instead of mail.box */
  short MSN_SMTP_MAILBOX = 0X4000;
  /* 2 pass mailer */
  short MSN_2PASSMAILER = (short) (0x8000 & 0xffff);

  String DESIGN_CLASS = "$Class"; //$NON-NLS-1$
  String FIELD_UPDATED_BY = "$UpdatedBy"; //$NON-NLS-1$

  String FIELD_FORM = "Form"; //$NON-NLS-1$

  String FIELD_TYPE_TYPE = "Type"; //$NON-NLS-1$
  /** Note should never be purged. */
  String FIELD_NOPURGE = "$NoPurge"; //$NON-NLS-1$
  /* form item to hold form CD */
  String ITEM_NAME_TEMPLATE = "$Body"; //$NON-NLS-1$

  /** SendTo item name */
  String MAIL_SENDTO_ITEM = "SendTo"; //$NON-NLS-1$
  /** CopyTo item name */
  String MAIL_COPYTO_ITEM = "CopyTo"; //$NON-NLS-1$

  /** Blind copy to item name */
  String MAIL_BLINDCOPYTO_ITEM = "BlindCopyTo"; //$NON-NLS-1$
  short DGN_SKIPBLANK = 0x0001;
  /** only match with the main name of the design element. */
  short DGN_SKIPSYNONYMS = 0x0002;
  /** remove underlines from the names (they indicate a hotkey) */
  short DGN_STRIPUNDERS = 0x0004;
  /**
   * convert underscore to a character that identifies the next character as a
   * hotkey (&amp; on windows)
   */
  short DGN_CONVUNDERS = 0x0008;
  short DGN_CASEINSENSITIVE = 0x0010;

  short DGN_STRIPBACKS = 0x0020;
  short DGN_ONLYSHARED = 0x0040;
  short DGN_ONLYPRIVATE = 0x0080;

  short DGN_FILTERPRIVATE1STUSE = 0x0100;

  short DGN_ALLPRIVATE = 0x0200;

  short DGN_HASUNID = 0x0400;
  short DGN_NOCHECKACCESS = 0x0800;

  short DGN_LISTBOX = NotesConstants.DGN_STRIPUNDERS | NotesConstants.DGN_SKIPSYNONYMS | NotesConstants.DGN_SKIPBLANK;

  short DGN_MENU = NotesConstants.DGN_SKIPSYNONYMS | NotesConstants.DGN_SKIPBLANK;

  /**
   * tells enumeration functions to only enumerate if readily available not if
   * NIFReadEntries necessary.
   * Used to allow Desk cache to work or fallback to NSFDbFindDesignNote direct
   * lookup
   */
  short DGN_ONLYIFFAST = 0x1000;

  /** return alias only if it has, don't return display name as alias */
  short DGN_ONLYALIAS = 0x2000;

  /** only match if the name or alias is exactly the same as the name supplied */
  short DGN_EXACTNAME = 0x4000;

  /* display things editable with dialog box; no version filtering (for design) */
  String DFLAGPAT_VIEWFORM_ALL_VERSIONS = "-FQMUGXWy#i:|@K;g~%z^}"; //$NON-NLS-1$
  /*	If this field exists in a mail note, it means that */
  /*	mail message was created by an agent. */
  String ASSIST_MAIL_ITEM = "$AssistMail"; //$NON-NLS-1$
  /* indicates if message was auto generated */
  String MAIL_ITEM_AUTOSUBMITTED = "Auto-submitted"; //$NON-NLS-1$
  /* value for 	MAIL_ITEM_AUTOSUBMITTED */
  String MAIL_AUTOGENERATED = "auto-generated"; //$NON-NLS-1$

  /* From item name */
  String MAIL_FROM_ITEM = "From"; //$NON-NLS-1$
  /* Posted date item name */
  String MAIL_POSTEDDATE_ITEM = "PostedDate"; //$NON-NLS-1$
  /* Unique ID of this message */
  String MAIL_ID_ITEM = "$MessageID"; //$NON-NLS-1$

  String ITEM_IS_NATIVE_MIME = "$NoteHasNativeMIME"; //$NON-NLS-1$
  /* Body item name */
  String MAIL_BODY_ITEM = "Body"; //$NON-NLS-1$
  String FORM_SCRIPT_ITEM_NAME = "$$FormScript"; //$NON-NLS-1$

  String DOC_SCRIPT_ITEM = "$Script"; //$NON-NLS-1$
  String DOC_SCRIPT_NAME = "$$ScriptName"; //$NON-NLS-1$
  String DOC_ACTION_ITEM = "$$FormAction"; //$NON-NLS-1$
  String ITEM_NAME_NOTE_SIGNATURE = "$Signature"; //$NON-NLS-1$

  /* Shared actions must be visible to both Notes and the Web since there is
  only one of these puppies - there is no list in the designer to get at
  more than one.  However, for completeness, I'll make the appropriate
  patterns for the day we may want to have separateness. */

  /* stored form signature */
  String ITEM_NAME_NOTE_STOREDFORM_SIG = "$SIG$Form"; //$NON-NLS-1$
  /* stored form and subform signature prefix - followed by either $FORM or the subform name*/
  String ITEM_NAME_NOTE_STOREDFORM_SIG_PREFIX = "$SIG"; //$NON-NLS-1$
  String FIELD_TITLE = "$TITLE"; //$NON-NLS-1$

  /*  Options used when calling ReplicateWithServer */

  /* document header info */
  String ITEM_NAME_DOCUMENT = "$Info"; //$NON-NLS-1$
  String SUBFORM_ITEM_NAME = "$SubForms"; //$NON-NLS-1$
  /* only subforms; no version filtering */
  String DFLAGPAT_SUBFORM_ALL_VERSIONS = "+U"; //$NON-NLS-1$
  /* display only shared image resources */
  String DFLAGPAT_IMAGE_RESOURCE = "+i"; //$NON-NLS-1$
  /* display only shared style sheet resources */
  String DFLAGPAT_STYLE_SHEET_RESOURCE = "+="; //$NON-NLS-1$
  /* display only shared Java resources */
  String DFLAGPAT_JAVA_RESOURCE = "+@"; //$NON-NLS-1$
  String DFLAGPAT_SACTIONS_DESIGN = "+y"; //$NON-NLS-1$
  /* display only Frameset notes */
  String DFLAGPAT_FRAMESET = "(+-*#"; //$NON-NLS-1$
  /** display only GraphicViews; no version filtering (for design) */
  String DFLAGPAT_VIEWMAP_ALL_VERSIONS = "*G"; //$NON-NLS-1$

  /* Use following bits with
  ReplicateWithServerExt only */

  /* 0x00010000-0x8000000 WILL NOT BE HONORED BY V3 SERVERS, BECAUSE V3 ONLY LOOKS AT THE FIRST 16 BITS! */

  /* display WebPages	*/
  String DFLAGPAT_WEBPAGE = "(+-*W"; //$NON-NLS-1$
  /** Receive notes from server (pull) */
  int REPL_OPTION_RCV_NOTES = 0x00000001;
  /** Send notes to server (push) */
  int REPL_OPTION_SEND_NOTES = 0x00000002;

  /** Replicate all database files */
  int REPL_OPTION_ALL_DBS = 0x00000004;

  /*	Definitions specific to busy signal handler */

  /** Close sessions when done */
  int REPL_OPTION_CLOSE_SESS = 0x00000040;
  /** Replicate NTFs as well */
  int REPL_OPTION_ALL_NTFS = 0x00000400;
  /** Low, Medium &amp; High priority databases */
  int REPL_OPTION_PRI_LOW = 0x00000000;
  /** Medium &amp; High priority databases only */
  int REPL_OPTION_PRI_MED = 0x00004000;
  /** High priority databases only */
  int REPL_OPTION_PRI_HI = 0x00008000;
  /** Abstract/truncate docs to summary data and first RTF field. (~40K) */
  int REPL_OPTION_ABSTRACT_RTF = 0x00010000;
  /** Abstract/truncate docs to summary only data. */
  int REPL_OPTION_ABSTRACT_SMRY = 0x00020000;

  /** Replicate private documents even if not selected by default. */
  int REPL_OPTION_PRIVATE = 0x00400000;

  int REPL_OPTION_ALL_FILES = NotesConstants.REPL_OPTION_ALL_DBS | NotesConstants.REPL_OPTION_ALL_NTFS;

  /** Remove the "File Activity" indicator */
  short BUSY_SIGNAL_FILE_INACTIVE = 0;
  /** Display the "File Activity" indicator (not supported on all platforms) */
  short BUSY_SIGNAL_FILE_ACTIVE = 1;
  /** Remove the "Network Activity" indicator. */
  short BUSY_SIGNAL_NET_INACTIVE = 2;
  /** Display the "Network Activity" indicator. */
  short BUSY_SIGNAL_NET_ACTIVE = 3;
  /** Display the "Poll" indicator. */
  short BUSY_SIGNAL_POLL = 4;
  /** Display the "Wan Sending" indicator. */
  short BUSY_SIGNAL_WAN_SENDING = 5;
  /** Display the "Wan Receiving" indicator. */
  short BUSY_SIGNAL_WAN_RECEIVING = 6;
  /** Called from NET to see if user cancelled I/O */
  short OS_SIGNAL_CHECK_BREAK = 5;

  /** Put up and manipulate the system wide progress indicator. */
  short OS_SIGNAL_PROGRESS = 13;

  /*  Definitions for replication state signal handler */
  /*	pText1		pText2. */

  /** N/A N/A */
  short PROGRESS_SIGNAL_BEGIN = 0;
  /** N/A N/A */
  short PROGRESS_SIGNAL_END = 1;
  /** Range N/A */
  short PROGRESS_SIGNAL_SETRANGE = 2;
  /** pText1 pText2 - usually NULL. */
  short PROGRESS_SIGNAL_SETTEXT = 3;
  /** New progress pos N/A */
  short PROGRESS_SIGNAL_SETPOS = 4;
  /** Delta of progress pos N/A */
  short PROGRESS_SIGNAL_DELTAPOS = 5;
  /** Total Bytes */
  short PROGRESS_SIGNAL_SETBYTERANGE = 6;
  /** Bytes Done */
  short PROGRESS_SIGNAL_SETBYTEPOS = 7;
  short OS_SIGNAL_REPL = 15;
  /** None */
  short REPL_SIGNAL_IDLE = 0;
  /** None */
  short REPL_SIGNAL_PICKSERVER = 1;

  /** pServer pPort */
  short REPL_SIGNAL_CONNECTING = 2;

  /** pServer pPort */
  short REPL_SIGNAL_SEARCHING = 3;

  /** pServerFile pLocalFile */
  short REPL_SIGNAL_SENDING = 4;
  /** pServerFile pLocalFile */
  short REPL_SIGNAL_RECEIVING = 5;
  /** pSrcFile */
  short REPL_SIGNAL_SEARCHINGDOCS = 6;
  /** pLocalFile pReplFileStats */
  short REPL_SIGNAL_DONEFILE = 7;

  // *******************************************************************************
  // * odstypes.h
  // *******************************************************************************

  /** pServerFile pLocalFile */
  short REPL_SIGNAL_REDIRECT = 8;

  /** None */
  short REPL_SIGNAL_BUILDVIEW = 9;
  /** None */
  short REPL_SIGNAL_ABORT = 10;

  /* 	Definitions ---------------------------------------------------------- */
  /* The following InfoType codes are defined for REGGetIDInfo */
  /* Note that the Certifier Flag can only exist on a hierarchical ID */
  /* and that Certifier, NotesExpress, and Desktop flags are not */
  /* present in safe copies of ID files */

  /**
   * Don't set environment variable used to identify the ID file during process
   * initialization -
   * usually either ServerKeyFileName or KeyFileName. See SECKFMSwitchToIDFile.
   */
  int fKFM_switchid_DontSetEnvVar = 0x00000008;

  short fSECToken_EnableRenewal = 0x0001;

  short MIME_HEADER_MAP_EMAIL = 0;

  short MIME_HEADER_MAP_HTTP = 1;

  short MIME_HEADER_MAP_NEWS = 2;

  short MIME_HEADER_MAP_UNKNOWN = NotesConstants.MIME_HEADER_MAP_EMAIL;

  short _RFC822ITEMDESC = 675;

  /** Open and read all information out of the id file */
  int SECKFM_open_All = 0x00000001;

  /** Write information conatined inthe handle out to the specified ID file */
  int SECKFM_close_WriteIdFile = 0x00000001;

  short REGIDGetUSAFlag = 1;
  /* Data structure returned is BOOL */

  short REGIDGetHierarchicalFlag = 2;
  /* Data structure returned is BOOL */

  short REGIDGetSafeFlag = 3;
  /* Data structure returned is BOOL */

  short REGIDGetCertifierFlag = 4;
  /* Data structure returned is BOOL */

  /*	DecryptFlags used in NSFNoteDecrypt */

  short REGIDGetNotesExpressFlag = 5;
  /* Data structure returned is BOOL */

  /* Define NSFDbGetModifiedNoteTableExt option bit */

  short REGIDGetDesktopFlag = 6;
  /* Data structure returned is BOOL */
  short REGIDGetName = 7;
  /* Data structure returned is char xx[MAXUSERNAME] */
  short REGIDGetPublicKey = 8;
  /* Data structure returned is char xx[xx] */
  short REGIDGetPrivateKey = 9;
  /* Data structure returned is char xx[xx] */
  short REGIDGetIntlPublicKey = 10;
  /* Data structure returned is char xx[xx] */
  short REGIDGetIntlPrivateKey = 11;
  short KFM_access_GetIDFHFlags = 51;
  int SIGN_NOTES_IF_MIME_PRESENT = 0x00000001;
  short DECRYPT_ATTACHMENTS_IN_PLACE = 0x0001;
  /* Get only new and deleted notes */
  short DBGETMOD_NEW_AND_DEL_NOTES = 0x0001;
  /* Only get ID table if it can be done ODS21 "fast" way */
  short DBGETMOD_FAST_ONLY = 0x0002;
  /* If TRUE, deleted notes shouldn't have high bit set in ID Table */
  short DBGETMOD_NODELETED_BIT = 0x0004;
  /* Get named ghost notes as well. */
  short DBGETMOD_NAMED_GHOSTS = 0x0008;
  /* Get new notes only */
  short DBGETMOD_NEW_ONLY = 0x0010;
  /* Special internal option to server - If the client-specified time
  does not match with the server's unread list time, return the
  ENTIRE unread table rather than a modified table */
  short DBGETMOD_FULL_TBL_OK = 0x0020;
  /* Get deleted notes only */
  short DBGETMOD_DELETED_ONLY = 0x0040;

  /* Define NSFDbGetModifiedNotesInfo InfoRequestedFlags bit (DWORD)*/

  /* Return soft-deleted notes without high bit on */
  short DBGETMOD_NO_SOFT_DELETES = 0x0080;
  /* Return purged notes as deleted notes */
  short DBGETMOD_PURGED = 0x0100;
  /* Return deleted ghost notes as deleted */
  short DBGETMOD_DELETED_GHOSTS = 0x0200;
  /* Return foreign updated notes (nebulous seqtime via replication) */
  short DBGETMOD_FOREIGN_UPDATES = 0x0400;
  /* Return soft deleted notes only */
  short DBGETMOD_SOFTDEL_RESTORES_ONLY = 0x0800;
  /* Return all notes until a certain time passed in by the retUntil argument */
  short DBGETMOD_USE_UNTIL = 0x1000;
  /* Return ghost notes as well */
  short DBGETMOD_GHOSTS = 0x2000;
  /* Only get the ID Table the "slow" way */
  short DBGETMOD_SLOW_ONLY = 0x4000;
  /* Return ghosts notes as if they are deleted */
  short DBGETMOD_GHOSTS_AS_DELETED = (short) (0x8000 & 0xffff);
  /* Get only new and deleted notes */
  int DB_GET_MODIFIED_NOTES_INFO_NEW_AND_DEL_NOTES = 0x00000001;
  /* Get named ghost notes as well. */
  int DB_GET_MODIFIED_NOTES_INFO_NAMED_GHOSTS = 0x00000002;
  /* Get new notes only */
  int DB_GET_MODIFIED_NOTES_INFO_NEW_ONLY = 0x00000004;
  /* Get deleted notes only */
  int DB_GET_MODIFIED_NOTES_INFO_DELETED_ONLY = 0x00000008;
  /* Return soft-deleted notes without high bit on */
  int DB_GET_MODIFIED_NOTES_INFO_NO_SOFT_DELETES = 0x00000010;

  // Next 3 defines are commented out when added to V901FP7 for VOP, while they
  // are not commented in V902.
  // On merging from 901FP7 to V100, leave them uncomented as they are in 902.

  /* Return deleted ghost notes as deleted */
  int DB_GET_MODIFIED_NOTES_INFO_DELETED_GHOSTS = 0x00000020;
  /* Return the Thread Root UNID too */
  int DB_GET_MODIFIED_NOTES_INFO_TRU = 0x00000040;
  /* If TRUE, deleted notes shouldn't have high bit set in ID Table */
  int DB_GET_MODIFIED_NOTES_INFO_NODELETED_BIT = 0x00000080;
  /* return the noteid too */
  int DB_GET_MODIFIED_NOTES_INFO_NOTEID = 0x00000100;

  /* return info for docs that have $TUA[0] == TRU */
  int DB_GET_MODIFIED_NOTES_INFO_MUST_HAVE_TRU = 0x00000200;

  /* return the $REF as the TRU if no parent */
  int DB_GET_MODIFIED_NOTES_INFO_RETURN_PARENT_IF_NO_TRU = 0x00000400;

  /* Get ghost notes as well. */
  int DB_GET_MODIFIED_NOTES_INFO_GHOSTS = 0x00000800;

  /* Use the sequence time compared against since time. */
  int DB_GET_MODIFIED_NOTES_INFO_SEQ_TIME = 0x00001000;
  /* ReplMon request to get expected pieces of the modified note log (SAAS) */
  int DB_GET_MODIFIED_NOTES_REPLMON = 0x00002000;
  /* Return the Thread sequence num too */
  int DB_GET_MODIFIED_NOTES_INFO_SEQ = 0x00004000;
  /* Return the doc's modified time */
  int DB_GET_MODIFIED_NOTES_MODIFIED_TIME = 0x00008000;
  /* Return all notes until a certain time passed in by the retUntil argument  */
  int DB_GET_MODIFIED_NOTES_INFO_USE_UNTIL = 0x00010000;
  /* Return the doc's Delivered/Posted/Created time -- Placeholder from 851SAAS */
  int DB_GET_MODIFIED_NOTES_DELIVERED_POSTED_TIME = 0x00020000;
  // Collision in SaaS with DB_GET_MODIFIED_NOTES_INFO_SEQ_TIME moved to
  // 0x00040000
  /* (was 0x00001000) Special handling for no formula replication. */
  int DB_GET_MODIFIED_NOTES_NO_FORMULA_REPLICATION = 0x00040000;

  int OOOPROF_MAX_BODY_SIZE = 32767;		 // Buffers passed into OOOGetGeneralSubject should be this size
  int CWF_CONTINUE_ON_ERROR = 0x0001; /*	Ignore compute errors */

  /*	Definitions specific to message signal handler */

  /* 	Possible validation phases for NSFNoteComputeWithForm()  */
  short CWF_DV_FORMULA = 1;
  short CWF_IT_FORMULA = 2;
  short CWF_IV_FORMULA = 3;
  short CWF_COMPUTED_FORMULA = 4;
  short CWF_DATATYPE_CONVERSION = 5;
  short CWF_COMPUTED_FORMULA_LOAD = NotesConstants.CWF_COMPUTED_FORMULA;
  short CWF_COMPUTED_FORMULA_SAVE = 6;

  /** Force operation, even if destination "up to date" */
  int DESIGN_FORCE = 0x00000001;
  /** Return an error if the template is not found */
  int DESIGN_ERR_TMPL_NOT_FOUND = 0x00000008;
  short OSMESSAGETYPE_OK = 0;

  short OSMESSAGETYPE_OKCANCEL = 1;
  short OSMESSAGETYPE_YESNO = 2;
  short OSMESSAGETYPE_YESNOCANCEL = 3;
  short OSMESSAGETYPE_RETRYCANCEL = 4;

  /* Address Book - "Servers" name space, items, and names */

  short OSMESSAGETYPE_POST = 5;
  short OSMESSAGETYPE_POST_NOSERVER = 6;
  String HTTP_PASSWORD_ITEM = "HTTPPassword"; //$NON-NLS-1$
  String MAIL_HTTPPASSWORD_ITEM = NotesConstants.HTTP_PASSWORD_ITEM;

  /* Address Book - "$Groups" name space, items, and names */

  String MAIL_FULLNAME_ITEM = "FullName"; //$NON-NLS-1$
  /* Address Book - "Users" name space, items, and names */
  /* Address Book - local $Users namespace */
  String LOCAL_USERNAMESSPACE = "1\\$Users"; //$NON-NLS-1$

  String USERNAMESSPACE = "$Users"; //$NON-NLS-1$
  String USERNAMESSPACE_ALT = "($Users)"; //$NON-NLS-1$
  String TYPEAHEADNAMESSPACE = "$NamesFieldLookup"; //$NON-NLS-1$

  String SERVERNAMESSPACE = "$Servers"; //$NON-NLS-1$
  String SERVERNAMESSPACE_1 = "1\\$Servers"; //$NON-NLS-1$
  String DIRECTORIESNAMESSPACE = "$Directories"; //$NON-NLS-1$
  String DIRECTORIESNAMESSPACE_1 = "1\\$Directories"; //$NON-NLS-1$

  String MAIL_GROUPSNAMESPACE = "$Groups"; //$NON-NLS-1$
  String MAIL_GROUPSNAMESPACE_1 = "1\\$Groups"; //$NON-NLS-1$
  String REGISTER_GROUPSNAMESPACE = "($RegisterGroups)"; //$NON-NLS-1$

  String REGISTER_GROUPSNAMESPACE_1 = "1\\($RegisterGroups)"; //$NON-NLS-1$

  // Define functions for IDV_ctxitem_DoDownload

  String REGISTER_GROUPS_LISTNAME_COLUMN = "$4"; //$NON-NLS-1$
  // *******************************************************************************
  // * lookup.h
  // *******************************************************************************
  /**
   * Return all entries in the view
   * <p>
   * (Note: a Names value of "" must also be specified)
   * </p>
   */
  short NAME_LOOKUP_ALL = 0x0001;
  /**
   * Only look in first names database containing desired namespace (view) for
   * specified names
   * rather than searching other names databases if name was not found. Note that
   * this may not
   * necessarily be the first names database in the search path - just the first
   * one containing
   * the desired view
   */
  short NAME_LOOKUP_NOSEARCHING = 0x0002;
  /**
   * Do not stop searching when the first matching entry is found.
   */
  short NAME_LOOKUP_EXHAUSTIVE = 0x0020;
  /**
   * Force the namespaces (views) to be made current
   */
  short NAME_LOOKUP_UPDATE = 0x0100;

  /** Low priority */
  short REPLFLG_PRIORITY_LOW = (short) (0xC000 & 0xffff);
  /** Medium priority */
  short REPLFLG_PRIORITY_MED = 0x0000;
  /** High priority */
  short REPLFLG_PRIORITY_HI = 0x4000;
  /** TRUE if note is soft-deleted */
  short NOTE_FLAG2_SOFT_DELETED = 0x0800;
  short IDV_download_fct_Cleanup = 0;

  short IDV_download_fct_Init = 1;

  short IDV_download_fct_Continue = 2;

  short IDV_download_fct_TryPassword = 3;

  short IDV_download_fct_GetAuth = 4;
  int MD5_DIGEST_SIZE = 16;

  int SHA1_DIGEST_SIZE = 20;
  int SHA256_DIGEST_SIZE = 32;

  int SHA384_DIGEST_SIZE = 48;
  int SHA512_DIGEST_SIZE = 64;
  int MAX_BSAFE_DIGEST_SIZE = 512;

  int AES_BLOCK_SIZE = 16; /* AES block size in bytes */
  int BSAFE_DIGEST_SIZE = 16;
  int KEYBITS_256 = 256;
  int KEYBITS_2048 = 2048;
  int MAX_CLIENT_AUTH_SIZE = NotesConstants.KEYBITS_256 / 8 + NotesConstants.AES_BLOCK_SIZE;

  int PARTIAL_DH_TLV_SIZE = NotesConstants.KEYBITS_2048 / 8 + NotesConstants.BSAFE_DIGEST_SIZE;

  short SEC_mpfct_MMCreateTempFile = 43;
  short SEC_mpfct_GetEnvVarInt = 22;
  short SEC_mpfct_MMFileClose = 53;

  int SEC_envvar_scope_Always = 0;

  int SEC_envvar_scope_Debug = 1;
  int SEC_envvar_scope_NonProduction = 2;
  int SEC_envvar_scope_Debug_Or_NonProduction = 3;
  int SEC_envvar_scope_Debug_And_NonProduction = 4;
  int MAX_DOMINO_URL_LENGTH = 2048;
  int MAX_DOMINO_USERAGENT_LENGTH = 256;
  int MAX_IDPURL = 4096;
  int NFL_MAX_FORMULA_LEN = 29999; /* max chars in an edit control */
  int MAX_HOSTNAME = 1024; /* Talked to Mike K. 512 X 2 */
  /*	SAML debugging flags - These are set using notes.ini: DEBUG_SAML=... Flags are shared with http */
  short SAML_DEBUG_HTTP = 0x0001; /* Debug output contains information from http */
  short SAML_DEBUG_PARSE = 0x0002; /* Debug output contains SAML parse information */

  /*	Define function codes for SECKFMMakeSafeCopy */

  short SAML_DEBUG_ERRORS = 0x0004; /* Debug output only contains errors */

  short SAML_DEBUG_DECODE_ASSERT = 0x0008; /* Debug to dump decoded assertion */

  short SAML_DEBUG_IDPCAT = 0x0010; /* Debug to trace idpcat activity */

  short SAML_DEBUG_REPLAY = 0x0020; /* Trace replay prevention    */
  /* Removed SAML_DEBUG_ALLOW_EXPIRED	0x0040 available */
  short SAML_DEBUG_DUMP_XML_TREE = 0x0080; /* Dump the entire XML tree   */
  short SAML_DEBUG_DUMP_C14N = 0x0100; /* Dump canonicalized buffers */

  /* Removed SAML_DEBUG_SORT  		0x0200 available */
  /* Removed SAML_DEBUG_VERIFY_SIG	0x0400 available */
  /* Removed SAML_DEBUG_NAMESPACES	0x0800 available */
  short SAML_DEBUG_SKIP_AUDIENCE = 0x1000; /* Ignore Audience verification in NONPROD */

  short SAML_DEBUG_CERT = 0x2000; /* Debug output for certificate management */
  short SAML_DEBUG_TESTSAAS = 0x4000; /* Allows us to enable some SAAS specific code for testing. */
  /** Create a safe-copy containing the "active" RSA keys. */
  short KFM_safecopy_Standard = 0;
  /** Create a safe-copy containing the "pending" RSA keys */
  short KFM_safecopy_NewPubKey = 1;
  /**
   * Create a safe-copy containing the "pending" RSA keys if any, else use the
   * "active" RSA keys.
   */
  short KFM_safecopy_NewestKey = 2;
  //
  // Action values for SECIdPCatMgmt function
  //
  short IDPCAT_ACTION_INIT = 1;

  /* Flags for NSFProcessResults */

  short IDPCAT_ACTION_TERM = 2;
  short IDPCAT_ACTION_CACHE_CLEAR = 3;
  int MAX_ITEMDEF_SEGMENTS = 25;

  /* Flags governing JSON output */

  int REMCON_GET_CONSOLE = 0x000000001;
  int REMCON_GET_TASKS = 0x000000002;

  /* default is the index into the RESULTS_INFO_LIST */

  int REMCON_GET_USERS = 0x000000004;
  int REMCON_CMD_ONLY = 0x000000008;
  int REMCON_GET_CONSOLE_META = 0x000000010;
  int REMCON_SYNC = 0x000000020;
  /** Output of results processing is a created, populated view */
  int PROCRES_CREATE_VIEW = 0x00000001;

  /** Output of results processing is a JSON stream */
  int PROCRES_JSON_OUTPUT = 0x00000002;

  /**
   * Output of results processing is a SEARCH_MATCH || ITEM_TABLE summary stream
   */
  int PROCRES_SUMMARY_OUTPUT = 0x00000004;

  /** return UNID(s) with each result, default is NoteID(s) */
  int PROCRES_RETURN_UNID = 0X00000008;

  /** return the replicaID of the database of the entry */
  int PROCRES_RETURN_REPLICAID = 0x00000010;

  /** return a root element for each returned entry */
  int PROCRES_RETURN_MULTIPLE_ROOTS = 0x00000020;

  int PROCRES_STREAMED_OUTPUT = NotesConstants.PROCRES_JSON_OUTPUT | NotesConstants.PROCRES_SUMMARY_OUTPUT;

  /** internal flag to indicate category processing */
  int PROCRES_HAS_CATEGORIZED_KEYS = 0x00000040;

  /** For formatting JSON arrays (from the outside) */
  int PROCRES_JSON_PREPEND_COMMA = 0x00000080;

  /** If TYPE_ERROR occurs, drop the document from results with no error */
  int PROCRES_IGNORE_TYPE_ERROR = 0x00000100;

  int MAX_CMD_VALLEN = NotesConstants.MAXSPRINTF + 1; // 256 + null term

  int MAX_ADDGROUP_ADMINP_ITEMS = 4;

  String ADMINP_DOC_PROCESS_ITEM = "ProxyProcess"; //$NON-NLS-1$
  String ADMINP = "Adminp"; //$NON-NLS-1$

  String ADMINP_DOC_SERVER_ITEM = "ProxyServer"; //$NON-NLS-1$
  String ADMINP_ALL_SERVERS = "*"; //$NON-NLS-1$
  String ADMINP_ACTION_ITEM = "ProxyAction"; //$NON-NLS-1$

  String ADMINP_NAME_LIST_ITEM = "ProxyNameList"; //$NON-NLS-1$
  String ADMINP_DOC_MEMBER_LIST_ITEM = "ProxyMemberList"; //$NON-NLS-1$

  String ADMINP_DOC_AUTHOR_ITEM = "ProxyAuthor"; //$NON-NLS-1$
  String ADMINP_ORG_NAME_ITEM = "Fullname"; //$NON-NLS-1$

  String ADMINP_REQUESTS_VIEW = "($Requests)"; //$NON-NLS-1$

  String adminpSetPasswordFields = "34"; //$NON-NLS-1$
  String adminpServerClusterAdd = "11"; //$NON-NLS-1$
  String adminpServerClusterRemove = "12"; //$NON-NLS-1$
  String adminpSetMABField = "37"; //$NON-NLS-1$
  String adminpRenameGroupInNAB = "40"; //$NON-NLS-1$

  /* known values of fields in the proxy database document form */
  String AdminpDelete = "0"; //$NON-NLS-1$
  String AdminpRenameInTheACL = "1"; //$NON-NLS-1$
  String AdminpCopyPublicKey = "2"; //$NON-NLS-1$
  String AdminpStoreServerVersion = "3"; //$NON-NLS-1$
  String AdminpRenameServerInNAB = "4"; //$NON-NLS-1$
  String AdminpRenameUserInNAB = "5"; //$NON-NLS-1$
  String AdminpMoveUserInHier = "6"; //$NON-NLS-1$
  String AdminpDeleteStats = "7"; //$NON-NLS-1$
  String AdminpInitiateNABChange = "8"; //$NON-NLS-1$
  String AdminpRecertServerInNAB = "9"; //$NON-NLS-1$
  String AdminpRecertUserInNAB = "10"; //$NON-NLS-1$
  String AdminpServerClusterAdd = "11"; //$NON-NLS-1$
  String AdminpServerClusterRemove = "12"; //$NON-NLS-1$
  String AdminpCreateReplicas = "13"; //$NON-NLS-1$
  String AdminpMoveReplicas = "14"; //$NON-NLS-1$
  String AdminpPendedDeleteForMove = "15"; //$NON-NLS-1$
  String AdminpDeleteInPersonDocs = "16"; //$NON-NLS-1$
  String AdminpDeleteInTheACL = "17"; //$NON-NLS-1$
  String AdminpDeleteInReadersAuthors = "18"; //$NON-NLS-1$
  String AdminpRenameInPersonDocs = "19"; //$NON-NLS-1$
  String AdminpRenameInReadersAuthors = "20"; //$NON-NLS-1$
  String AdminpDeleteMailFile = "21"; //$NON-NLS-1$
  String AdminpApproveMailFileInfo = "22"; //$NON-NLS-1$
  String AdminpDeleteUnlinkedMailFile = "23"; //$NON-NLS-1$
  String AdminpCreateMailFile = "24"; //$NON-NLS-1$
  String AdminpMonitorMovedReplica = "25"; //$NON-NLS-1$
  String AdminpDeleteChangeRequests = "26"; //$NON-NLS-1$
  String AdminpGetMailFileInfo = "27"; //$NON-NLS-1$
  String AdminpRequestDeleteMailFile = "28"; //$NON-NLS-1$
  String AdminpResourceAdd = "29"; //$NON-NLS-1$
  String AdminpResourceDelete = "30"; //$NON-NLS-1$
  String AdminpApproveResourceDelete = "31"; //$NON-NLS-1$
  String AdminpCreateReplicasCheckAccess = "32"; //$NON-NLS-1$
  String AdminpMoveReplicasCheckAccess = "33"; //$NON-NLS-1$
  String AdminpSetPasswordFields = "34"; //$NON-NLS-1$
  String AdminpUpdateUserPW = "35"; //$NON-NLS-1$
  String AdminpUpdateServerPW = "36"; //$NON-NLS-1$
  String AdminpSetMABField = "37"; //$NON-NLS-1$
  String AdminpRenamePersonInFreeTime = "38"; //$NON-NLS-1$
  String AdminpRenamePersonInMailFile = "39"; //$NON-NLS-1$
  String AdminpRenameGroupInNAB = "40"; //$NON-NLS-1$
  String AdminpRenameGroupInPersonDocs = "41"; //$NON-NLS-1$
  String AdminpRenameGroupInTheACL = "42"; //$NON-NLS-1$
  String AdminpRenameGroupInReadersAuthors = "43"; //$NON-NLS-1$
  String AdminpAddPersonsX509Certificate = "44"; //$NON-NLS-1$
  String AdminpCheckMailServersAccess = "45"; //$NON-NLS-1$
  String AdminpUpgradeUser = "46"; //$NON-NLS-1$
  String AdminpCopyExternalDomainAddresses = "47"; //$NON-NLS-1$
  String AdminpPromoteMailServersAccess = "48"; //$NON-NLS-1$
  String AdminpCreateNewMailFileReplica = "49"; //$NON-NLS-1$
  String AdminpAddNewMailFileFields = "50"; //$NON-NLS-1$
  String AdminpMonitorNewMailFileFields = "51"; //$NON-NLS-1$
  String AdminpReplaceMailFileFields = "52"; //$NON-NLS-1$
  String AdminpLastPushToNewMailServer = "53"; //$NON-NLS-1$
  String AdminpDeletePersonInNAB = "54"; //$NON-NLS-1$
  String AdminpDeleteServerInNAB = "55"; //$NON-NLS-1$
  String AdminpDeleteGroupInNAB = "56"; //$NON-NLS-1$
  String AdminpDelegateMailFile = "57"; //$NON-NLS-1$
  String AdminpApproveDeletePersonInNAB = "58"; //$NON-NLS-1$
  String AdminpApproveDeleteServerInNAB = "59"; //$NON-NLS-1$
  String AdminpApproveRenamePersonInNAB = "60"; //$NON-NLS-1$
  String AdminpApproveRenameServerInNAB = "61"; //$NON-NLS-1$
  String AdminpResourceModify = "62"; //$NON-NLS-1$
  String AdminpUpdateNetworkTables = "63"; //$NON-NLS-1$
  String AdminpCreateISPYMailInDb = "64"; //$NON-NLS-1$
  String AdminpNCMoveReplicasCheckAccess = "65"; //$NON-NLS-1$
  String AdminpNCMoveReplicas = "66"; //$NON-NLS-1$
  String AdminpStoreServerCPUCount = "67"; //$NON-NLS-1$
  String AdminpRenamePersonInUnreadList = "68"; //$NON-NLS-1$
  String AdminpDeleteReplicaAfterMove = "69"; //$NON-NLS-1$
  String AdminpSetDNSFullHostName = "70"; //$NON-NLS-1$
  String AdminpStoreServerPlatform = "71"; //$NON-NLS-1$
  String AdminpApproveDeleteDesignElements = "72"; //$NON-NLS-1$
  String AdminpRequestDeleteDesignElements = "73"; //$NON-NLS-1$
  String AdminpDeleteDesignElements = "74"; //$NON-NLS-1$
  String AdminpApproveDeleteMovedReplica = "75"; //$NON-NLS-1$
  String AdminpRequestDeleteMovedReplica = "76"; //$NON-NLS-1$
  String AdminpSetDomainCatalog = "77"; //$NON-NLS-1$
  String AdminpWebDelegateMailFile = "78"; //$NON-NLS-1$
  String AdminpGetFileInfo = "79"; //$NON-NLS-1$
  String AdminpRequestDeleteFile = "80"; //$NON-NLS-1$
  String AdminpDeleteFile = "81"; //$NON-NLS-1$
  String AdminpApproveFileInfo = "82"; //$NON-NLS-1$
  String AdminpSetWebAdminFields = "83"; //$NON-NLS-1$
  String AdminpAcceleratedCreateReplica = "84"; //$NON-NLS-1$
  String AdminpSetConfigNAB = "85"; //$NON-NLS-1$
  String AdminpStoreServerDirectoryName = "86"; //$NON-NLS-1$
  String AdminpCreateRoamingUserRoamingFiles = "87"; //$NON-NLS-1$
  String AdminpPromoteRoamingServersAccess = "88"; //$NON-NLS-1$
  String AdminpReplaceRoamingServerField = "89"; //$NON-NLS-1$
  String AdminpMonitorMovedRoamingReplica = "90"; //$NON-NLS-1$
  String AdminpCreateRoamingReplStubs = "91"; //$NON-NLS-1$
  String AdminpRemoveRoamingUserRoamingFiles = "92"; //$NON-NLS-1$
  String AdminpCheckRoamingServerAccess = "93"; //$NON-NLS-1$
  String AdminpCreateRoamingReplicas = "94"; //$NON-NLS-1$
  String AdminpCertPublicationRequest = "95"; //$NON-NLS-1$
  String AdminpCrlPublicationRequest = "96"; //$NON-NLS-1$
  String AdminpUserModifyRequest = "97"; //$NON-NLS-1$
  String AdminpCertRemoveRequest = "98"; //$NON-NLS-1$
  String AdminpPolicyPublicationRequest = "99"; //$NON-NLS-1$
  String AdminpLastPushToNewRoamingServer = "100"; //$NON-NLS-1$
  String AdminpSignDatabase = "101"; //$NON-NLS-1$
  String AdminpCAConfigPublicationRequest = "102"; //$NON-NLS-1$
  String AdminpCrlRemoveRequest = "103"; //$NON-NLS-1$
  String AdminpDelegateIMAPMailFiles = "104"; //$NON-NLS-1$
  String AdminpCAConfigToBeSigned = "105"; //$NON-NLS-1$
  String AdminpRejectRenameUserInNAB = "106"; //$NON-NLS-1$
  String AdminpRetractNameChange = "107"; //$NON-NLS-1$
  String AdminpEnableMailAgent = "108"; //$NON-NLS-1$
  String AdminpReportServerUse = "109"; //$NON-NLS-1$
  String AdminpRejectRetractNameChange = "110"; //$NON-NLS-1$
  String AdminpDeleteServerFromCatalog = "111"; //$NON-NLS-1$
  String AdminpCopyTrendsRecord = "112"; //$NON-NLS-1$
  String AdminpDeletePolicy = "113"; //$NON-NLS-1$
  String AdminpApproveRetractNameChange = "114"; //$NON-NLS-1$
  String AdminpApproveRecertify = "115"; //$NON-NLS-1$
  String AdminpApproveNameChange = "116"; //$NON-NLS-1$
  String AdminpApproveNewPublicKeys = "117"; //$NON-NLS-1$
  String AdminpInitiateWebNameChange = "118"; //$NON-NLS-1$
  String AdminpRenameWebNameInTheACL = "119"; //$NON-NLS-1$
  String AdminpRenameWebNameInNAB = "120"; //$NON-NLS-1$
  String AdminpRenameWebNameInPersonDocs = "121"; //$NON-NLS-1$
  String AdminpRenameWebNameInReadersAuthors = "122"; //$NON-NLS-1$
  String AdminpRenameWebNameInFreeTime = "123"; //$NON-NLS-1$
  String AdminpRenameWebNameInMailFile = "124"; //$NON-NLS-1$
  String AdminpRenameWebNameInUnreadList = "125"; //$NON-NLS-1$
  String AdminpRemoveNameChangeInLDAPDir = "126"; //$NON-NLS-1$
  String AdminpChangeHTTPPasswordRequest = "127"; //$NON-NLS-1$
  String AdminpDefineServerMonitorQuery = "128"; //$NON-NLS-1$
  String AdminpCollectServerMonitorData = "129"; //$NON-NLS-1$
  String AdminpConsolidateServerMonitorData = "130"; //$NON-NLS-1$
  String AdminpCreateIMAPDelegations = "131"; //$NON-NLS-1$
  String AdminpDeleteHostedOrg = "132"; //$NON-NLS-1$
  String AdminpUpdateRoamingState = "133"; //$NON-NLS-1$
  String AdminpUpdateRoamingFields = "134"; //$NON-NLS-1$
  String AdminpCreateHostedOrgStorage = "135"; //$NON-NLS-1$
  String AdminpRecertCrossCert = "136"; //$NON-NLS-1$
  String AdminpCreateObjStore = "137"; //$NON-NLS-1$
  String AdminpDeleteHostedOrgStorageGetInfo = "138"; //$NON-NLS-1$
  String AdminpApproveDeleteHostedOrgStorage = "139"; //$NON-NLS-1$
  String AdminpDeleteHostedOrgStorage = "140"; //$NON-NLS-1$
  String AdminpRecertCAInNAB = "141"; //$NON-NLS-1$
  String AdminpFindNameInDomain = "142"; //$NON-NLS-1$
  String AdminpVerifyHostedOrgStorage = "143"; //$NON-NLS-1$
  String AdminpAddGroup = "144"; //$NON-NLS-1$
  String AdminpRecoveryIdRequest = "145"; //$NON-NLS-1$
  String AdminpPublishRecoveryInfo = "146"; //$NON-NLS-1$
  String AdminpDeletePersonInUnreadList = "147"; //$NON-NLS-1$
  String AdminpMonitorRoamingReplStubs = "148"; //$NON-NLS-1$
  String AdminpDelegateOnAdminServer = "149"; //$NON-NLS-1$
  String AdminpCreateReplicasCheckExeTime = "150"; //$NON-NLS-1$
  String AdminpMoveReplicasCheckExeTime = "151"; //$NON-NLS-1$
  String AdminpMoveMailCheckExeTime = "152"; //$NON-NLS-1$
  String AdminpNCMoveReplicasCheckExeTime = "153"; //$NON-NLS-1$
  String AdminpRegNewUserWithPolicy = "154"; //$NON-NLS-1$
  String AdminpRegNewUser = "155"; //$NON-NLS-1$
  String AdminpUpdateServerKeyring = "156"; //$NON-NLS-1$
  String AdminpEnableSSLPorts = "157"; //$NON-NLS-1$
  String AdminpNewAgentsMachine = "158"; //$NON-NLS-1$
  String AdminpXCertPublicationRequest = "159"; //$NON-NLS-1$
  String AdminpWebEnableMailAgent = "160"; //$NON-NLS-1$
  String AdminpUpdateReplicaSettings = "161"; //$NON-NLS-1$
  String AdminpRenameInSharedAgents = "162"; //$NON-NLS-1$
  String AdminpWebMailSetSoftDeletionTime = "163"; //$NON-NLS-1$
  String AdminpRenameInAgentsReadersField = "164"; //$NON-NLS-1$
  String AdminpDeleteInAgentsReadersField = "165"; //$NON-NLS-1$
  String AdminpMonitorServerSSLStatus = "166"; //$NON-NLS-1$
  String AdminpDelegateOnHomeServer = "167"; //$NON-NLS-1$
  String AdminpSetFaultRecoverySettings = "168"; //$NON-NLS-1$
  String AdminpCertNewServerKey = "169"; //$NON-NLS-1$
  String AdminpCertNewPersonKey = "170"; //$NON-NLS-1$
  String AdminpCertNewCertifierKey = "171"; //$NON-NLS-1$
  String AdminpAddDB2ToServerDoc = "172"; //$NON-NLS-1$
  String AdminpMonitorDB2ReplStub = "173"; //$NON-NLS-1$
  String AdminpDB2SetID = "174"; //$NON-NLS-1$
  String AdminpDB2MoveContainer = "175"; //$NON-NLS-1$
  String AdminpRenamePersonInDesignElements = "176"; //$NON-NLS-1$
  String AdminpDeletePersonInDesignElements = "177"; //$NON-NLS-1$
  String AdminpDB2AccessConnection = "178"; //$NON-NLS-1$
  String AdminpRenameWebNameInDesignElements = "179"; //$NON-NLS-1$
  String AdminpRenameGroupInDesignElements = "180"; //$NON-NLS-1$
  String AdminpIDVaultModify = "181"; //$NON-NLS-1$
  String AdminpSaaSPullMailfile = "182"; //$NON-NLS-1$ /* FAP added to aid in merge conflict resolution with fixes 217521, 214942,
                                         // 218323 */
  String AdminpReplaceSaaSMailFileFields = "183"; //$NON-NLS-1$ /* FAP added to aid in merge conflict resolution with fixes 217521,
                                                  // 214942, 218323 */
  String AdminpDeletePersonInNABTimed = "184"; //$NON-NLS-1$ /* FAP added to aid in merge conflict resolution with fixes 217521,
                                               // 214942, 218323 */
  String AdminpRenamePersonInCalendarFiles = "185"; //$NON-NLS-1$
  String AdminpRenameWebPersonInCalendarFiles = "186"; //$NON-NLS-1$
  String AdminpSetMicrosoftOutlookSupportFlag = "187"; //$NON-NLS-1$
  String AdminpUpdateSSLCipherSpecs = "188"; //$NON-NLS-1$
  String AdminpRenameCommonNameUserInNAB = "189"; //$NON-NLS-1$

  short AdminpDeleteWord = 0;
  short AdminpRenameInTheACLWord = 1;
  short AdminpCopyPublicKeyWord = 2;
  short AdminpStoreServerVersionWord = 3;
  short AdminpRenameServerInNABWord = 4;
  short AdminpRenameUserInNABWord = 5;
  short AdminpMoveUserInHierWord = 6;
  short AdminpDeleteStatsWord = 7;
  short AdminpInitiateNABChangeWord = 8;
  short AdminpRecertServerInNABWord = 9;
  short AdminpRecertUserInNABWord = 10;
  short AdminpServerClusterAddWord = 11;
  short AdminpServerClusterRemoveWord = 12;
  short AdminpCreateReplicasWord = 13;
  short AdminpMoveReplicasWord = 14;
  short AdminpPendedDeleteForMoveWord = 15;
  short AdminpDeleteInPersonDocsWord = 16;
  short AdminpDeleteInTheACLWord = 17;
  short AdminpDeleteInReadersAuthorsWord = 18;
  short AdminpRenameInPersonDocsWord = 19;
  short AdminpRenameInReadersAuthorsWord = 20;
  short AdminpDeleteMailFileWord = 21;
  short AdminpApproveMailFileInfoWord = 22;
  short AdminpDeleteUnlinkedMailFileWord = 23;
  short AdminpCreateMailFileWord = 24;
  short AdminpMonitorMovedReplicaWord = 25;
  short AdminpDeleteChangeRequestsWord = 26;
  short AdminpGetMailFileInfoWord = 27;
  short AdminpRequestDeleteMailFileWord = 28;
  short AdminpResourceAddWord = 29;
  short AdminpResourceDeleteWord = 30;
  short AdminpApproveResourceDeleteWord = 31;
  short AdminpCreateReplicasCheckAccessWord = 32;
  short AdminpMoveReplicasCheckAccessWord = 33;
  short AdminpSetPasswordFieldsWord = 34;
  short AdminpUpdateUserPWWord = 35;
  short AdminpUpdateServerPWWord = 36;
  short AdminpSetMABFieldWord = 37;
  short AdminpRenamePersonInFreeTimeWord = 38;
  short AdminpRenamePersonInMailFileWord = 39;
  short AdminpRenameGroupInNABWord = 40;
  short AdminpRenameGroupInPersonDocsWord = 41;
  short AdminpRenameGroupInTheACLWord = 42;
  short AdminpRenameGroupInReadersAuthorsWord = 43;
  short AdminpAddPersonsX509CertificateWord = 44;

  short AdminpNewAdminpRequestFormat = 45;

  short AdminpCheckMailServersAccessWord = 45;
  short AdminpUpgradeUserWord = 46;
  short AdminpCopyExternalDomainAddressesWord = 47;
  short AdminpPromoteMailServersAccessWord = 48;
  short AdminpCreateNewMailFileReplicaWord = 49;
  short AdminpAddNewMailFileFieldsWord = 50;
  short AdminpMonitorNewMailFileFieldsWord = 51;
  short AdminpReplaceMailFileFieldsWord = 52;
  short AdminpLastPushToNewMailServerWord = 53;
  short AdminpDeletePersonInNABWord = 54;
  short AdminpDeleteServerInNABWord = 55;
  short AdminpDeleteGroupInNABWord = 56;
  short AdminpDelegateMailFileWord = 57;
  short AdminpApproveDeletePersonInNABWord = 58;
  short AdminpApproveDeleteServerInNABWord = 59;
  short AdminpApproveRenamePersonInNABWord = 60;
  short AdminpApproveRenameServerInNABWord = 61;
  short AdminpResourceModifyWord = 62;
  short AdminpUpdateNetworkTablesWord = 63;
  short AdminpCreateISPYMailInDbWord = 64;
  short AdminpNCMoveReplicasCheckAccessWord = 65;
  short AdminpNCMoveReplicasWord = 66;
  short AdminpStoreServerCPUCountWord = 67;
  short AdminpRenamePersonInUnreadListWord = 68;
  short AdminpDeleteReplicaAfterMoveWord = 69;
  short AdminpSetDNSFullHostNameWord = 70;
  short AdminpStoreServerPlatformWord = 71;
  short AdminpApproveDeleteDesignElementsWord = 72;
  short AdminpRequestDeleteDesignElementsWord = 73;
  short AdminpDeleteDesignElementsWord = 74;
  short AdminpApproveDeleteMovedReplicaWord = 75;
  short AdminpRequestDeleteMovedReplicaWord = 76;
  short AdminpSetDomainCatalogWord = 77;
  short AdminpWebDelegateMailFileWord = 78;
  short AdminpGetFileInfoWord = 79;
  short AdminpRequestDeleteFileWord = 80;
  short AdminpDeleteFileWord = 81;
  short AdminpApproveFileInfoWord = 82;
  short AdminpSetWebAdminFieldsWord = 83;
  short AdminpAcceleratedCreateReplicaWord = 84;
  short AdminpSetConfigNABWord = 85;
  short AdminpStoreServerDirectoryNameWord = 86;
  short AdminpCreateRoamingUserRoamingFilesWord = 87;
  short AdminpPromoteRoamingServersAccessWord = 88;
  short AdminpReplaceRoamingServerFieldWord = 89;
  short AdminpMonitorMovedRoamingReplicaWord = 90;
  short AdminpCreateRoamingReplStubsWord = 91;
  short AdminpRemoveRoamingUserRoamingFilesWord = 92;
  short AdminpCheckRoamingServerAccessWord = 93;
  short AdminpCreateRoamingReplicasWord = 94;
  short AdminpCertPublicationRequestWord = 95;
  short AdminpCrlPublicationRequestWord = 96; /* AdminpCrlPublicationRequestWord */
  short AdminpUserModifyRequestWord = 97;
  short AdminpCertRemoveRequestWord = 98;
  short AdminpPolicyPublicationRequestWord = 99;
  short AdminpLastPushToNewRoamingServerWord = 100;
  short AdminpSignDatabaseWord = 101;
  short AdminpCAConfigPublicationRequestWord = 102; /* AdminpCAConfigPublicationRequestWord */
  short AdminpCrlRemoveRequestWord = 103;
  short AdminpDelegateIMAPMailFilesWord = 104;
  short AdminpCAConfigToBeSignedWord = 105;
  short AdminpRejectRenameUserInNABWord = 106;
  short AdminpRetractNameChangeWord = 107;
  short AdminpEnableMailAgentWord = 108;
  short AdminpReportServerUseWord = 109;
  short AdminpRejectRetractNameChangeWord = 110;
  short AdminpDeleteServerFromCatalogWord = 111;
  short AdminpCopyTrendsRecordWord = 112;
  short AdminpDeletePolicyWord = 113;
  short AdminpApproveRetractNameChangeWord = 114;
  short AdminpApproveRecertifyWord = 115;
  short AdminpApproveNameChangeWord = 116;
  short AdminpApproveNewPublicKeysWord = 117;
  short AdminpInitiateWebNameChangeWord = 118;
  short AdminpRenameWebNameInTheACLWord = 119;
  short AdminpRenameWebNameInNABWord = 120;
  short AdminpRenameWebNameInPersonDocsWord = 121;
  short AdminpRenameWebNameInReadersAuthorsWord = 122;
  short AdminpRenameWebNameInFreeTimeWord = 123;
  short AdminpRenameWebNameInMailFileWord = 124;
  short AdminpRenameWebNameInUnreadListWord = 125;
  short AdminpRemoveNameChangeInLDAPDirWord = 126;
  short AdminpChangeHTTPPasswordRequestWord = 127;
  short AdminpDefineServerMonitorQueryWord = 128;
  short AdminpCollectServerMonitorDataWord = 129;
  short AdminpConsolidateServerMonitorDataWord = 130;
  short AdminpCreateIMAPDelegationsWord = 131;
  short AdminpDeleteHostedOrgWord = 132;
  short AdminpUpdateRoamingStateWord = 133;
  short AdminpUpdateRoamingFieldsWord = 134;
  short AdminpCreateHostedOrgStorageWord = 135;
  short AdminpRecertCrossCertWord = 136;
  short AdminpCreateObjStoreWord = 137;
  short AdminpDeleteHostedOrgStorageGetInfoWord = 138;
  short AdminpApproveDeleteHostedOrgStorageWord = 139;
  short AdminpDeleteHostedOrgStorageWord = 140;
  short AdminpRecertCAInNABWord = 141;
  short AdminpFindNameInDomainWord = 142;
  short AdminpVerifyHostedOrgStorageWord = 143;
  short AdminpAddGroupWord = 144;
  short AdminpRecoveryIdRequestWord = 145;
  short AdminpPublishRecoveryInfoWord = 146;
  short AdminpDeletePersonInUnreadListWord = 147;
  short AdminpMonitorRoamingReplStubsWord = 148;
  short AdminpDelegateOnAdminServerWord = 149;
  short AdminpCreateReplicasCheckExeTimeWord = 150;
  short AdminpMoveReplicasCheckExeTimeWord = 151;
  short AdminpMoveMailCheckExeTimeWord = 152;
  short AdminpNCMoveReplicasCheckExeTimeWord = 153;
  short AdminpRegNewUserWithPolicyWord = 154;
  short AdminpRegNewUserWord = 155;
  short AdminpUpdateServerKeyringWord = 156;
  short AdminpEnableSSLPortsWord = 157;
  short AdminpNewAgentsMachineWord = 158;
  short AdminpXCertPublicationRequestWord = 159;
  short AdminpWebEnableMailAgentWord = 160;
  short AdminpUpdateReplicaSettingsWord = 161;
  short AdminpRenameInSharedAgentsWord = 162;
  short AdminpWebMailSetSoftDeletionTimeWord = 163;
  short AdminpRenameInAgentsReadersFieldWord = 164;
  short AdminpDeleteInAgentsReadersFieldWord = 165;
  short AdminpMonitorServerSSLStatusWord = 166;
  short AdminpDelegateOnHomeServerWord = 167;
  short AdminpSetFaultRecoverySettingsWord = 168;
  short AdminpCertNewServerKeyWord = 169;
  short AdminpCertNewPersonKeyWord = 170;
  short AdminpCertNewCertifierKeyWord = 171;
  short AdminpAddDB2ToServerDocWord = 172;
  short AdminpMonitorDB2ReplStubWord = 173;
  short AdminpDB2SetIDWord = 174;
  short AdminpDB2MoveContainerWord = 175;
  short AdminpRenamePersonInDesignElementsWord = 176;
  short AdminpDeletePersonInDesignElementsWord = 177;
  short AdminpDB2AccessConnectionWord = 178;
  short AdminpRenameWebNameInDesignElementsWord = 179;
  short AdminpRenameGroupInDesignElementsWord = 180;
  short AdminpIDVaultModifyWord = 181;
  short AdminpSaaSPullMailfileWord = 182; /* FAP added to aid in merge conflict resolution with fixes 217521, 214942, 218323 */
  short AdminpReplaceSaaSMailFileFieldsWord = 183; /* FAP added to aid in merge conflict resolution with fixes 217521, 214942, 218323 */
  short AdminpDeletePersonInNABTimedWord = 184; /* FAP added to aid in merge conflict resolution with fixes 217521, 214942, 218323 */
  short AdminpRenamePersonInCalendarFilesWord = 185;
  short AdminpRenameWebPersonInCalendarFilesWord = 186;
  short AdminpSetMicrosoftOutlookSupportFlagWord = 187;
  short AdminpUpdateSSLCipherSpecsWord = 188;
  short AdminpRenameCommonNameUserInNABWord = 189;

  String ADMINP_FULLNAME = "FullName"; //$NON-NLS-1$
  String ADMINP_LISTNAME = "ListName"; //$NON-NLS-1$
  String ADMINP_SERVERNAME = "ServerName"; //$NON-NLS-1$
  String ADMINP_COMMONNAME = "CommonName"; //$NON-NLS-1$
  String ADMINP_PERSON = "Person"; //$NON-NLS-1$
  String ADMINP_GROUP = "Group"; //$NON-NLS-1$
  String ADMINP_SERVER = "Server"; //$NON-NLS-1$
  String ADMINP_CERTSUBMIT = "CertSubmitApproval"; //$NON-NLS-1$
  String ADMINP_PROXY_DOCUMENT = "AdminRequest"; //$NON-NLS-1$
  String ADMINP_DOC_PASS_CHECK_PASSWORD_ITEM = "ProxyPasswordCheck"; //$NON-NLS-1$
  String ADMINP_DOC_PASS_GRACE_PERIOD_ITEM = "ProxyPasswordGracePeriod"; //$NON-NLS-1$
  String ADMINP_DOC_PASS_CHANGE_INTERVAL_ITEM = "ProxyPasswordChangeInterval"; //$NON-NLS-1$
  String ADMINP_DOC_PASS_HTTP_OPTIONS_ITEM = "ProxyHTTPPasswordOptions"; //$NON-NLS-1$
  String ADMINP_DOC_CLUSTER_NAME_ITEM = "ProxyClusterName"; //$NON-NLS-1$
  String ADMINP_DOC_REPLICA_ID_ITEM = "ProxyReplicaId"; //$NON-NLS-1$
  String ADMINP_DOC_DATABASE_PATH_ITEM = "ProxyDatabasePath"; //$NON-NLS-1$
  String ADMINP_NEW_GROUP_NAME_ITEM = "ProxyNewGroupName"; //$NON-NLS-1$
  String ADMINP_DOC_DIRECTORY_SERVER_NAME_ITEM = "ProxyDirectoryServerName"; //$NON-NLS-1$
  String ADMINP_DOC_SECNAB_PATH_ITEM = "ProxySecondaryDirectoryPath"; //$NON-NLS-1$
  String ADMINP_DOC_SECNAB_NAME_ITEM = "ProxySecondaryDirectoryName"; //$NON-NLS-1$
  String ADMINP_DOC_DIRECTORY_REPLICA_ID_ITEM = "$ProxyDirectoryReplicaId"; //$NON-NLS-1$
  String ADMINP_DOC_DIRECTORY_NOTE_UNID_ITEM = "ProxyDirectoryNoteUNID"; //$NON-NLS-1$
  String ADMINP_DOC_DIRECTORY_DOMAIN_ITEM = "$ProxyDirectoryDomain"; //$NON-NLS-1$
  String ADMINP_DOC_DIRECTORY_ENTRYID_ITEM = "ProxyDirectoryEntryID"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_REQUEST_UNID = "ProxyOriginatingRequestUNID"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_UNID = NotesConstants.ADMINP_ORIGINATING_REQUEST_UNID;
  String ADMINP_ORIGINATING_REQUEST_AUTHORID = "ProxyOriginatingAuthor"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_TIMEDATE_ITEM = "ProxyOriginatingTimeDate"; //$NON-NLS-1$
  /**
   * used by agents to specify whose authority the agents
   * runs under (other than the signer)
   */
  String ASSIST_ONBEHALFOF = "$OnBehalfOf"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_REQUEST_FULLNAME = "FullName"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_REQUEST_ORG = "ProxyOriginatingOrganization"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_REQUEST_INTERNET_DOMAIN = "ProxyOriginatingInternetDomain"; //$NON-NLS-1$

  String LOCATION_IMAIL_INTERNET_DOMAIN = "InternetDomain"; //$NON-NLS-1$

  String ADMINP_ORIG_UNID_VIEW = "$Requests by UNID"; //$NON-NLS-1$
  String ADMINP_DOC_DELETE_MAIL_FILE_ITEM = "ProxyDeleteMailfile"; //$NON-NLS-1$

  /*
  ** Routine to build lists of expected and possible requests
  ** which will be associated with this request.  It then adds
  ** these lists to the request provided.
  **
  ** Requests which are singular will simply return with no
  ** error.  No lists are necessary and therefore not added.
  */
  String ListSeperator = "-1"; //$NON-NLS-1$

  String[] lpchMoveMailFile = {
      NotesConstants.AdminpCheckMailServersAccess,
      NotesConstants.AdminpCreateNewMailFileReplica,
      NotesConstants.AdminpAddNewMailFileFields,
      NotesConstants.AdminpMonitorNewMailFileFields,
      NotesConstants.AdminpReplaceMailFileFields,
      NotesConstants.AdminpLastPushToNewMailServer, /* Terminator check on the admin server */
      NotesConstants.ListSeperator,
      NotesConstants.AdminpGetMailFileInfo,
      NotesConstants.AdminpRequestDeleteMailFile,
      NotesConstants.AdminpDeleteMailFile, /* TERMINATOR SHOULD BE HERE!!! WILL FIX LATER */
      NotesConstants.AdminpApproveMailFileInfo, /* No purge for this is handled by the template */

  };

  String[] lpchMoveRoamingFiles = {
      NotesConstants.AdminpCheckRoamingServerAccess,
      NotesConstants.AdminpCreateRoamingReplicas,
      NotesConstants.AdminpReplaceRoamingServerField,
      NotesConstants.AdminpLastPushToNewRoamingServer, /* Terminator check on the admin server */
      NotesConstants.ListSeperator,
      NotesConstants.AdminpGetFileInfo,
      NotesConstants.AdminpRequestDeleteFile,
      NotesConstants.AdminpDeleteFile, /* TERMINATOR SHOULD BE HERE!!! WILL FIX LATER */
      NotesConstants.AdminpApproveFileInfo /* No purge for this is handled by the template */
  };

  /*
   ** Delete Group Lists
   */
  String[] lpchDeleteGroupNab = {
      NotesConstants.AdminpDeleteGroupInNAB,
      NotesConstants.AdminpDeleteInPersonDocs
  };
  String[] lpchCommonXNoFilesSupplied = {
      NotesConstants.AdminpDeleteInTheACL,
      NotesConstants.AdminpDeleteInReadersAuthors /* Terminator check on admin server */
  };

  /*
   ** Delete user lists
   */
  String[] lpchDeleteUser = {
      NotesConstants.AdminpDeletePersonInNAB,
      NotesConstants.AdminpDeleteInPersonDocs
  };
  String[] lpchDeleteUserImm = {
  };
  String[] lpchCommonXForDeleteUser = {
      NotesConstants.AdminpDeleteInTheACL,
      NotesConstants.AdminpDeleteInReadersAuthors, /* Terminator check on admin server */
      NotesConstants.ListSeperator,
      NotesConstants.AdminpGetMailFileInfo,
      NotesConstants.AdminpApproveMailFileInfo,
      NotesConstants.AdminpRequestDeleteMailFile,
      NotesConstants.AdminpDeleteMailFile
  };

  /*
   ** Rename/Recertify user lists
   */
  /*
   ** Need to add rename of web users to this.
   */
  String[] lpchMoveCertifier = {
      NotesConstants.AdminpInitiateNABChange,
      NotesConstants.AdminpRenameUserInNAB
  };
  String[] lpchRenameUser = {
      NotesConstants.AdminpRenameUserInNAB
  };
  String[] lpchRenameCommonNameUser = {
      NotesConstants.AdminpMoveUserInHier,
      NotesConstants.AdminpRenameCommonNameUserInNAB
  };
  String[] lpchCommonXForRename = {
      NotesConstants.AdminpRenameInTheACL,
      NotesConstants.AdminpRenameInPersonDocs,
      NotesConstants.AdminpRenamePersonInUnreadList,
      NotesConstants.AdminpRenameInReadersAuthors, /* Terminator check on admin server */
      NotesConstants.ListSeperator,
      NotesConstants.AdminpRenamePersonInFreeTime,
      NotesConstants.AdminpRenamePersonInMailFile
  };

  /*
   ** Rename Group Lists
   */
  String[] lpchRenameGroup = {
      NotesConstants.AdminpRenameGroupInNAB,
      NotesConstants.AdminpRenameGroupInPersonDocs
  };
  String[] lpchCommonXForRenameGroup = {
      NotesConstants.AdminpRenameGroupInTheACL,
      NotesConstants.AdminpRenameGroupInReadersAuthors /* Terminator check on admin server */
  };

  /*
  ** Upgrade to Roaming User State List
  */
  String[] lpchUpgradeToRoamn = {
      NotesConstants.AdminpCreateRoamingReplStubs,
      NotesConstants.AdminpUpdateRoamingFields,
      NotesConstants.AdminpMonitorRoamingReplStubs,
      NotesConstants.AdminpUpdateRoamingState
  };

  String ADMINP_ORIGINATING_REQUEST_EXPECTED_LIST = "ProxyOriginatingReqsExpected"; //$NON-NLS-1$
  String ADMINP_ORIGINATING_REQUEST_POSSIBLE_LIST = "ProxyOriginatingReqsPossible"; //$NON-NLS-1$

  /**
   * Unique identifier of a directory entry, returned from NAMELookup.
   * Good for Domino or LDAP backends.
   */
  String ITEM_DIRENTRYID = "$$DirEntryID";							  //$NON-NLS-1$
  /**
   * Name of the directory domain (not mailDomian) the entry resides in,
   * typically defined in directory assistance
   */
  String ITEM_DIRDOMAIN = "$$Domain"; //$NON-NLS-1$ #
  /** The name of the server specified in the DIRCTX of a search or get */
  String ITEM_DIRSERVER = "$$DirServer"; //$NON-NLS-1$

  int DIRENTRYID_PART_UNKNOWN = 0;
  /** Servername part of a \ref DIRENTRYID_TYPE_NOTES URL */
  int DIRENTRYID_PART_NOTES_SERVERNAME = 1;
  /** Filename part of a \ref DIRENTRYID_TYPE_NOTES URL */
  int DIRENTRYID_PART_NOTES_FILENAME = 2;

  int DIRENTRYIDTRIM_LOCAL = NotesConstants.DIRENTRYID_PART_NOTES_SERVERNAME | NotesConstants.DIRENTRYID_PART_NOTES_FILENAME;

  /** Unknown directory entry id type */
  int DIRENTRYID_TYPE_UNKNOWN = 0;
  /** Directory entry id has a Notes URL */
  int DIRENTRYID_TYPE_NOTES = 1;
  /** Directory entry id has an LDAP URL */
  int DIRENTRYID_TYPE_LDAP = 2;

  /* Domain info types. */

  /** Is Domain Virtualized? */
  int DIR_DOMAIN_INFO_TYPE_IS_VIRTUALIZED = 0;
  /** Is Domain a Central Directory (userless) configuration? */
  int DIR_DOMAIN_INFO_TYPE_IS_CDACONFIG = 1;
  /** Is Domain primary? */
  int DIR_DOMAIN_INFO_TYPE_IS_PRIMARY = 2;
  /** Obsolete as of 8.51, use SECURE_INTERNET_PWD_VERSION instead! */
  int DIR_DOMAIN_INFO_TYPE_SUPPORTS_SECURE_INTERNET_PW = 3;
  /** Return the path of the directory associated with this domain */
  int DIR_DOMAIN_INFO_TYPE_DIRECTORY_PATH = 4;
  /** Indicates if DirEntries should be "promoted" rather than created */
  int DIR_DOMAIN_INFO_TYPE_PROMOTE = 5;
  /** Version # of secure internet password digest */
  int DIR_DOMAIN_INFO_TYPE_SECURE_INTERNET_PWD_VERSION = 6;
  /** Is this Domain an EDC? (does the Extended Dir Config doc have stuff? */
  int DIR_DOMAIN_INFO_TYPE_IS_EDC = 7;
  /** Is Domain Directory Independence Configuration? */
  int DIR_DOMAIN_INFO_TYPE_IS_DICONFIG = 8;
  /** Is Domain a Central Directory or Directory Independent Config? */
  int DIR_DOMAIN_INFO_TYPE_IS_CONFIG = 9;
  /**
   * is this Domain in the DA tables, and therefore valid? (different from
   * available)
   */
  int DIR_DOMAIN_INFO_TYPE_IS_VALIDDOMAIN = 10;

  /*	Flags returned in the 'Flags' section of the BUILDVERSION structure */

  /** Non-production style build (internal only). */
  int BLDVERFLAGS_NONPRODUCTION = 0x00000001;

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

  /*	Name and Address Book lookup package definitions */

  /**
   * Add the title of the database to each entry in the buffer returned by
   * NAMEGetAddressBooks.
   */
  short NAME_GET_AB_TITLES = 0x0001;
  /**
   * If {@link #NAME_GET_AB_TITLES} is specified then return the database path in
   * place
   * of the title for any database that has no title. {@link #NAME_DEFAULT_TITLES}
   * has no
   * effect if {@link #NAME_GET_AB_TITLES} is not also specified.
   */
  short NAME_DEFAULT_TITLES = 0x0002;
  /**
   * Return only the first Address book in use locally or Domino Directory on a
   * server.
   */
  short NAME_GET_AB_FIRSTONLY = 0x0004;
  /** Get Master address book name only */
  short NAME_GET_MAB_ONLY = 0x0008;
  /** Get Server Based Enterprise Directory name only */
  short NAME_GET_ED_ONLY = 0x0010;
  /** Include Server based ED as last book */
  short NAME_INCLUDE_ED = 0x0020;
  /** Get All enterprise Directories */
  short NAME_GET_ALL_EDS = 0x0040;
  /** Include only NAB's that this server is the admin server of */
  short NAME_ADMIN_ONLY = 0x0100;
  /** Include Config (userless) NAB's */
  short NAME_INCLUDE_CONFIGNAB = 0x0200;
  /** Include First AB that has config info */
  short NAME_CONFIG_ONLY = 0x0400;

  /** notes storage file */
  String DBTYPE = ".nsf"; //$NON-NLS-1$

  String ADMINP_ORIG_REQ_CASCADE = "C"; //$NON-NLS-1$

  /** Types of DIRENTRY objects. */

  /** Unknown directory entry type */
  int DIRENTRY_TYPE_UNKNOWN = 0;
  /**
   * Directory entry type for a user that has not been promoted to a Domino Person
   */
  int DIRENTRY_TYPE_PERSON = 1;
  /**
   * Directory entry type for a group that has not been promoted to a Domino Group
   */
  int DIRENTRY_TYPE_GROUP = 2;
  /** Directory entry type for a Domino Person (might reside in LDAP) */
  int DIRENTRY_TYPE_DOMINO_PERSON = 3;
  /** Directory entry type for a Domino Group (might reside in LDAP) */
  int DIRENTRY_TYPE_DOMINO_GROUP = 4;
  /** Directory entry type for a Domino Server */
  int DIRENTRY_TYPE_DOMINO_SERVER = 5;
  /** Directory entry type for a Domino Certifier */
  int DIRENTRY_TYPE_DOMINO_CERTIFIER = 6;
  /** Directory entry type for a Domino Resource */
  int DIRENTRY_TYPE_DOMINO_RESOURCE = 7;
  /** Directory entry type for a Domino Mail-In Database */
  int DIRENTRY_TYPE_DOMINO_MAILINDB = 8;
  /** Directory entry type for a Domino CrossCertificate */
  int DIRENTRY_TYPE_DOMINO_CROSSCERTIFICATE = 9;
  /** Directory entry type for a Domino Domain */
  int DIRENTRY_TYPE_DOMINO_DOMAIN = 10;
  /** Directory entry type for a Domino Connection */
  int DIRENTRY_TYPE_DOMINO_CONNECTION = 11;
  /** Directory entry type for a Domino VaultTrustCertificate */
  int DIRENTRY_TYPE_DOMINO_VAULTTRUSTCERTIFICATE = 12;
  /** Directory entry type for an LotusLive Notes 2 DirSyncConfig doc */
  int DIRENTRY_TYPE_DOMINO_DIRSYNCCONFIG = 13;
  /** Directory entry type for an LotusLive Notes 2 DirSyncConfig response doc */
  int DIRENTRY_TYPE_DOMINO_DIRSYNC_REQUEST = 14;
  /**
   * Directory entry type for an LotusLive Notes 2 DirSyncConfig Hosted BES doc
   */
  int DIRENTRY_TYPE_DOMINO_DIRSYNCCONFIGCDIR = 15;
  /** Directory entry type for a Domino Policy Master */
  int DIRENTRY_TYPE_DOMINO_POLICYMASTER = 16;
  /** Directory entry type for a Domino Policy Setting */
  int DIRENTRY_TYPE_DOMINO_POLICYSETTINGS = 17;
  /** Directory entry type for a Domino Policy Desktop Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_DESKTOP_SETTINGS = 18;
  /** Directory entry type for a Domino Policy Security Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_SECURITY_SETTINGS = 19;
  /** Directory entry type for a Domino Policy Traveler Desktop Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_TRAVELER_SETTINGS = 20;
  /** Directory entry type for a Domino Policy Registration Desktop Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_REGISTRATION_SETTINGS = 21;
  /** Directory entry type for a Domino Policy Archive Desktop Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_ARCHIVE_SETTINGS = 22;
  /** Directory entry type for a Domino Policy Mail */
  int DIRENTRY_TYPE_DOMINO_POLICY_MAIL_SETTINGS = 23;
  /** Directory entry type for a Domino Policy Roaming Desktop Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_ROAMING_SETTINGS = 24;
  /** Directory entry type for a Domino Policy Setup Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_SETUP_SETTINGS = 25;
  /** Directory entry type for a Domino Policy Symphony Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_SYMPHONY_SETTINGS = 26;
  /** Directory entry type for a Domino Policy Connections Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_CONNECTIONS_SETTINGS = 27;
  /** Directory entry type for a Domino Account */
  int DIRENTRY_TYPE_DOMINO_ACCOUNT = 28;
  /** Directory entry type for a NamedObjectID ECL */
  int DIRENTRY_TYPE_DOMINO_ECL = 29;
  /** Directory entry type for a Domino Policy Archive Desktop Setting */
  int DIRENTRY_TYPE_DOMINO_POLICY_ARCHIVE_CRITERIA_SETTINGS = 30;

  /** Only search Virtual directories */
  int DIR_VIRTUALIZE = 0x00000010;

  int MAXDIRENTRYID = 512 + 9 + NotesConstants.MAXUSERNAME;

  String DIR_ITEMS_ALL_DOMINO = "$$DIR_ITEMS_ALL_DOMINO"; //$NON-NLS-1$

  /** Network Acct Name (for dir synching) */
  String MAIL_NETUSERNAME_ITEM = "NetUserName"; //$NON-NLS-1$

  String LOCATION_TYPE = "LocationType"; //$NON-NLS-1$

  /* Values of LOCATION_TYPE */

  String LOCTYPE_NETWORK = "0"; //$NON-NLS-1$
  String LOCTYPE_MODEM = "1"; //$NON-NLS-1$
  String LOCTYPE_NETWORKANDMODEM = "2"; //$NON-NLS-1$
  String LOCTYPE_NONE = "3"; //$NON-NLS-1$
  String LOCTYPE_NETWORKDIALUP = "4"; //$NON-NLS-1$
  String LOCTYPE_INTERNETDIALUP = "5"; //$NON-NLS-1$

  /** Mail server name */
  String MAIL_MAILSERVER_ITEM = "MailServer"; //$NON-NLS-1$
  /** Mail file name */
  String MAIL_MAILFILE_ITEM = "MailFile"; //$NON-NLS-1$

  /** info (TYPE_ASSISTANT_INFO) */
  String ASSIST_INFO_ITEM = "$AssistInfo"; //$NON-NLS-1$
  /** Type of assistant - related to action type */
  String ASSIST_TYPE_ITEM = "$AssistType"; //$NON-NLS-1$
  /** assistant query item */
  String ASSIST_QUERY_ITEM = "$AssistQuery"; //$NON-NLS-1$
  /** assistant action item */
  String ASSIST_ACTION_ITEM = "$AssistAction"; //$NON-NLS-1$
  /** TimeDate of last run */
  String ASSIST_LASTRUN_ITEM = "$AssistLastRun"; //$NON-NLS-1$
  /** Number of docs run on last run */
  String ASSIST_DOCCOUNT_ITEM = "$AssistDocCount"; //$NON-NLS-1$
  /** Run information object */
  String ASSIST_RUNINFO_ITEM = "$AssistRunInfo"; //$NON-NLS-1$
  /** assistant action item - extra data */
  String ASSIST_EXACTION_ITEM = "$AssistAction_Ex"; //$NON-NLS-1$
  /**
   * TIMEDATE of when the agent design (as opposed to enable/disable state)
   * was changed
   */
  String ASSIST_VERSION_ITEM = "$AssistVersion"; //$NON-NLS-1$
  /** format of the agent structure */
  String ASSIST_FORMAT_VERSION = "$AssistFormatVer"; //$NON-NLS-1$
  /**
   * @since Notes/Domino 5.04
   */
  String ASSIST_FORMAT_VER5040 = "05040"; //$NON-NLS-1$
  /**
   * @since Notes/Domino 5.03
   */
  String ASSIST_FORMAT_VER5030 = "05030"; //$NON-NLS-1$

  /** name of machine on which this background filter may run */
  String FILTER_MACHINE_NAME = "$MachineName"; //$NON-NLS-1$

  String AGENT_HSCRIPT_ITEM = "$AgentHScript"; //$NON-NLS-1$

  int PROCESS_GROUP_QUERY = NotesConstants.MAXDWORD;
  int PROCESS_GROUP_NOTES_SERVER = 0;

  /*	Shared Resources */

  String ITEM_NAME_IMAGE_DATA = "$ImageData"; //$NON-NLS-1$
  String ITEM_NAME_IMAGE_NAMES = "$ImageNames"; //$NON-NLS-1$
  String ITEM_NAME_IMAGES_WIDE = "$ImagesWide"; //$NON-NLS-1$
  String ITEM_NAME_IMAGES_HIGH = "$ImagesHigh"; //$NON-NLS-1$
  String ITEM_NAME_IMAGES_COLORIZE = "$ImagesColorize"; //$NON-NLS-1$
  String ITEM_NAME_IMAGES_WEB_BROWSER_COMPATIBLE = "$WebBrowserCompatible"; //$NON-NLS-1$
  String ITEM_NAME_IMAGE_URL_SOURCE = "$ImageURLSource"; //$NON-NLS-1$

  String ITEM_NAME_JAVA_FILES = "$JavaFiles"; //$NON-NLS-1$

  String ITEM_NAME_STYLE_SHEET_DATA = "$StyleSheetData"; //$NON-NLS-1$
  String ITEM_NAME_STYLE_SHEET_NAME = "$StyleSheetName"; //$NON-NLS-1$

  String ITEM_NAME_FILE_DATA = "$FileData"; //$NON-NLS-1$
  String ITEM_NAME_FILE_NAMES = "$FileNames"; //$NON-NLS-1$
  String ITEM_NAME_FILE_WEBPATH = "$WebFilePath"; //$NON-NLS-1$
  String ITEM_NAME_FILE_EDITFILE = "$EditFilePath"; //$NON-NLS-1$
  String ITEM_NAME_FILE_EDITOR = "$FileEditor"; //$NON-NLS-1$
  String ITEM_NAME_FILE_SIZE = "$FileSize"; //$NON-NLS-1$
  String ITEM_NAME_FILE_MIMETYPE = "$MimeType"; //$NON-NLS-1$
  String ITEM_NAME_FILE_MIMECHARSET = "$MimeCharSet"; //$NON-NLS-1$
  String ITEM_NAME_FILE_MODINFO = "$FileModDT"; //$NON-NLS-1$

  /* Script Library items */
  String SCRIPTLIB_ITEM_NAME = "$ScriptLib"; //$NON-NLS-1$
  String SCRIPTLIB_OBJECT = "$ScriptLib_O"; //$NON-NLS-1$
  String JAVASCRIPTLIBRARY_CODE = "$JavaScriptLibrary"; //$NON-NLS-1$
  String SERVER_JAVASCRIPTLIBRARY_CODE = "$ServerJavaScriptLibrary"; //$NON-NLS-1$
  
  /* Outline sitemap */
  String OUTLINE_SITEMAP_LIST = "$SiteMapList";

  String VIEW_COMMENT_ITEM = "$Comment"; //$NON-NLS-1$
  String FILTER_COMMENT_ITEM = "$Comment"; //$NON-NLS-1$

  String FIELD_LANGUAGE = "$Language"; //$NON-NLS-1$

  /** No subtotalling */
  byte NIF_STAT_NONE = 0;
  /** Total of values in subtree */
  byte NIF_STAT_TOTAL = 1;
  /** Total / # direct entries in parent's index (1 level only below parent) */
  byte NIF_STAT_AVG_PER_CHILD = 2;
  /** Total / total of values in entire index */
  byte NIF_STAT_PCT_OVERALL = 3;
  /** Total / total of values in parent's index */
  byte NIF_STAT_PCT_PARENT = 4;
  /** Total / # descendants in parent's index (all levels below parent) */
  byte NIF_STAT_AVG_PER_ENTRY = 5;
  
  /** Reader List role name for public users */
  String FIELD_PUBLICROLE = "$P"; //$NON-NLS-1$
  /** Note has public access if ACL_FLAG_PUBLICREADER is set. */
  String FIELD_PUBLICACCESS = "$PublicAccess"; //$NON-NLS-1$
  /** Form Note has public access if ACL_FLAG_PUBLICREADER is set. */
  String FORM_FIELD_PUBLICACCESS = "$FormPublicAccess"; //$NON-NLS-1$
  String FIELD_PUBLICACCESS_ENABLED = "1"; //$NON-NLS-1$
  String DESIGN_READERS = "$Readers"; //$NON-NLS-1$
  
  String ITEM_CONFLICT_ACTION = "$ConflictAction"; //$NON-NLS-1$
  String CONFLICT_AUTOMERGE = "1"; //$NON-NLS-1$
  String CONFLICT_NONE = "2"; //$NON-NLS-1$
  String CONFLICT_BEST_MERGE = "3"; //$NON-NLS-1$
  
  String ITEM_NAME_WEBFLAGS = "$WebFlags"; //$NON-NLS-1$
  String WEBFLAG_NOTE_IS_HTML = "H"; //$NON-NLS-1$
  String WEBFLAG_NOTE_CONTAINS_VIEW = "V"; //$NON-NLS-1$
  String WEBFLAG_NOTE_HTML_ALL_FLDS = "F"; //$NON-NLS-1$
  String WEBFLAG_NOTE_CONTAINS_JSBUTTON = "J"; //$NON-NLS-1$
  String WEBFLAG_NOTE_ALLOW_DOC_SELECTIONS = "S"; //$NON-NLS-1$
  String WEBFLAG_NOTE_USEJSCTL_INBROWSER = "D"; //$NON-NLS-1$
  String WEBFLAG_NOTE_CRAWLABLE = "C"; //$NON-NLS-1$
  String WEBFLAG_NOTE_RESTAPIALLOWED = "A"; //$NON-NLS-1$
  
  String VIEW_SCRIPT_NAME = "$ViewScript"; //$NON-NLS-1$
  /** Formula for view script event */
  String VIEW_ACTION_ITEM_NAME = "$ViewAction"; //$NON-NLS-1$
  String VIEW_FORM_FORMULA_ITEM = "$FormFormula"; //$NON-NLS-1$
  
  /**
   * This value specifies the maximum length of ID name.See SCRIPTCONTEXTDESCR for more information.
   */
  int MAXIMUM_ID_NAME_LENGTH = 40;
  
  String DESIGNER_VERSION = "$DesignerVersion"; //$NON-NLS-1$
  
  /** agent designer can force the agent to have restricted
  rights, even if the signer has unrestricted rights , or
  raise their rights to be full admin + unrestricted */
  String ASSIST_RESTRICTED = "$Restricted"; //$NON-NLS-1$

  /* NABLookupBasicAuthentication options */
  
  int BASIC_AUTH_NO_AMBIGUOUS_NAMES = 0;
  int BASIC_AUTH_ALLOW_AMBIGUOUS_NAMES = 1;

}
