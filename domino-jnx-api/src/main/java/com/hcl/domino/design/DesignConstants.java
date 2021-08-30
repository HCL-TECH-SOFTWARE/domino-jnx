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
package com.hcl.domino.design;

/**
 * Contains Notes API constants used specifically for database design.
 *
 * @since 1.0.18
 */
public interface DesignConstants {
  String DESIGNER_VERSION = "$DesignerVersion"; //$NON-NLS-1$
  String DB_NEW_HTML = "$AllowPost8HTML"; //$NON-NLS-1$

  String VIEW_VIEW_FORMAT_ITEM = "$ViewFormat"; //$NON-NLS-1$

  String XPAGE_ALTERNATE = "$XPageAlt"; //$NON-NLS-1$
  String XPAGE_ALTERNATE_CLIENT = "$XPageAltClient"; //$NON-NLS-1$
  
  String VIEW_FORMULA_ITEM = "$Formula"; //$NON-NLS-1$
  String VIEW_CLASSES_ITEM = "$FormulaClass"; //$NON-NLS-1$
  /** Index disposition options */
  String VIEW_INDEX_ITEM = "$Index"; //$NON-NLS-1$
  /** Collation buffer */
  String VIEW_COLLATION_ITEM = "$Collation"; //$NON-NLS-1$
  /** NIF will log all incremental view updates */
  String FIELD_LOGVIEWUPDATES = "$LogViewUpdates"; //$NON-NLS-1$
  String FIELD_LOGVIEWUPDATES_ENABLED = "1"; //$NON-NLS-1$
  
  /** Web related flags for form or document */
  String ITEM_NAME_WEBFLAGS = "$WebFlags"; //$NON-NLS-1$
  /** use appropriate applet when serving to browser */
  String WEBFLAG_NOTE_USEAPPLET_INBROWSER = "B"; //$NON-NLS-1$
  /** treat this document or form as plain HTML, do not convert styled text to HTML */
  String WEBFLAG_NOTE_IS_HTML = "H"; //$NON-NLS-1$
  /** optimization for web server: this note contains an embedded view */
  String WEBFLAG_NOTE_CONTAINS_VIEW = "V"; //$NON-NLS-1$
  /** gen'd HTML for all fields */
  String WEBFLAG_NOTE_HTML_ALL_FLDS = "F"; //$NON-NLS-1$
  /** Generate {@code <FORM>} tag */
  String WEBFLAG_NOTE_CONTAINS_JSBUTTON = "J"; //$NON-NLS-1$
  String WEBFLAG_NOTE_ALLOW_DOC_SELECTIONS = "S"; //$NON-NLS-1$
  /** use JS control to render the view (reuse deployable) */
  String WEBFLAG_NOTE_USEJSCTL_INBROWSER = "D"; //$NON-NLS-1$
  /** this view can be crawled */
  String WEBFLAG_NOTE_CRAWLABLE = "C"; //$NON-NLS-1$
  /** this view is accessible to rest api */
  String WEBFLAG_NOTE_RESTAPIALLOWED = "A"; //$NON-NLS-1$
  
  /** frameset used to open form */
  String ITEM_NAME_FRAMEINFO = "$FrameInfo"; //$NON-NLS-1$
  String ITEM_NAME_HTMLCODE = "$HTMLCode"; //$NON-NLS-1$
  
  String VIEW_COLUMN_PROFILE_DOC = "$ColumnProfileDoc"; //$NON-NLS-1$
  String VIEW_COLUMN_FORMAT_ITEM = "$ColumnFormatItem"; //$NON-NLS-1$
  String VIEW_COLUMN_FORMAT_EXT_ITEM = "$ColumnFormatExtItem"; //$NON-NLS-1$
  
  /* Outline entry list */
  String OUTLINE_SITEMAPLIST_ITEM = "$SiteMapList"; //$NON-NLS-1$
  
  /** font table */
  String ITEM_NAME_FONTS = "$Fonts"; //$NON-NLS-1$
  
  byte THEME_DEFAULT = 0;
  byte THEME_ENABLE = 1;
  byte THEME_DISABLE = 2;
  
  /**
   * Non-documented item for Hannover "viewers" property.
   */
  String VIEW_VIEWERS_ITEM = "$Viewers"; //$NON-NLS-1$
  /**
   * Non-documented item for Hannover "thread view" property.
   */
  String VIEW_THREADVIEW_ITEM = "$ThreadView"; //$NON-NLS-1$
  
  /**
   * Non-documented flag used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the view should hide empty categories.
   */
  String INDEXDISPOSITION_HIDEEMPTYCATEGORIES = "C"; //$NON-NLS-1$
  /**
   * Non-documented entry used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the view should be refreshed at most every X seconds.
   * 
   * <p>In that item, this value is followed by "=" and then the number of seconds.</p>
   */
  String INDEXDISPOSITION_REFRESHAUTOATMOST = "R"; //$NON-NLS-1$
  /**
   * Non-documented entry used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the view should be refreshed automatically.
   */
  String INDEXDISPOSITION_REFRESHAUTO = "O"; //$NON-NLS-1$
  /**
   * Non-documented entry used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the view should be refreshed manually.
   */
  String INDEXDISPOSITION_REFRESHMANUAL = "M"; //$NON-NLS-1$
  /**
   * Non-documented entry used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the view index should be discarded if inactive for X hours.
   * 
   * <p>In that item, this value is followed by "=" and then the number of hours.</p>
   */
  String INDEXDISPOSITION_DISCARDINACTIVEFOR = "P"; //$NON-NLS-1$
  /**
   * Non-documented entry used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the view index should be discarded after each use.
   */
  String INDEXDISPOSITION_DISCARDEACHUSE = "T"; //$NON-NLS-1$
  /**
   * Non-documented entry used in the "/"-separated {@value #VIEW_INDEX_ITEM} item
   * to indicate that the initial index building should be restricted to Designer-
   * or Manager-level users.
   */
  String INDEXDISPOSITION_RESTRICTTODESIGNER = "B"; //$NON-NLS-1$
  
  String ACTION_ITEM = "$ACTIONS"; //$NON-NLS-1$
  String V5ACTION_ITEM = "$V5ACTIONS"; //$NON-NLS-1$
  
  short ACTION_SYS_CMD_CATEGORIZE     = 0x7c4e;
  short ACTION_SYS_CMD_EDIT       =   0x0a02;
  short ACTION_SYS_CMD_SEND       =   0x0a03;
  short ACTION_SYS_CMD_FORWARD      =   0x0a04;
  short ACTION_SYS_CMD_MOVE_TO_FOLDER   = 0x7c3d;
  short ACTION_SYS_CMD_REMOVE_FROM_FOLDER = 0x7c3e;
  short ACTION_SYS_CMD_MARK_SEL_READ    = 0x7c5e;
  short ACTION_SYS_CMD_MARK_SEL_UNREAD  =   0x7c6e;
  short ACTION_SYS_CMD_OPEN_SELECTED_NEWWND = 0x7c7e;
  short ACTION_SYS_CMD_FILE_PRINT     = 0x7c8e;
  short ACTION_SYS_CMD_DELETE       = 0x7c9e;
  short ACTION_SYS_CMD_INFOBOX      =   0x7cae;
  short ACTION_SYS_CMD_CUT        =   0x7cbe;
  short ACTION_SYS_CMD_COPY       =   0x7cce;
  short ACTION_SYS_CMD_COPY_LINK_DOC    = 0x7cde;
  short ACTION_SYS_CMD_COPY_VIEW_TABLE  =   0x7cee;
  short ACTION_SYS_CMD_PASTE        = 0x7cfe  ;
  short ACTION_SYS_CMD_OPEN_SELECTED    = 0x7d0e;
  short ACTION_SYS_CMD_BOOKMARK     =   0x7d1e;
  
  /**  Use Reference Note  */
  short TPL_FLAG_REFERENCE = 0x0001;
  /**  Mail during DocSave  */
  short TPL_FLAG_MAIL = 0x0002;
  /**  Add note ref. to "reference note"  */
  short TPL_FLAG_NOTEREF = 0x0004;
  /**  Add note ref. to main parent of "reference note"  */
  short TPL_FLAG_NOTEREF_MAIN = 0x0008;
  /**  Recalc when leaving fields  */
  short TPL_FLAG_RECALC = 0x0010;
  /**  Store form item in with note  */
  short TPL_FLAG_BOILERPLATE = 0x0020;
  /**  Use foreground color to paint  */
  short TPL_FLAG_FGCOLOR = 0x0040;
  /**  Spare DWORDs have been zeroed  */
  short TPL_FLAG_SPARESOK = 0x0080;
  /**  Activate OLE objects when composing a new doc  */
  short TPL_FLAG_ACTIVATE_OBJECT_COMP = 0x0100; 
  /**  Activate OLE objects when editing an existing doc  */
  short TPL_FLAG_ACTIVATE_OBJECT_EDIT = 0x0200; 
  /**  Activate OLE objects when reading an existing doc  */
  short TPL_FLAG_ACTIVATE_OBJECT_READ = 0x0400; 
  /**  Show Editor window if TPL_FLAG_ACTIVATE_OBJECT_COMPOSE  */
  short TPL_FLAG_SHOW_WINDOW_COMPOSE = 0x0800;
  /**  Show Editor window if TPL_FLAG_ACTIVATE_OBJECT_EDIT  */
  short TPL_FLAG_SHOW_WINDOW_EDIT = 0x1000;
  /**  Show Editor window if TPL_FLAG_ACTIVATE_OBJECT_READ  */
  short TPL_FLAG_SHOW_WINDOW_READ = 0x2000;
  /**  V3 Updates become responses  */
  short TPL_FLAG_UPDATE_RESPONSE = 0x4000;
  /**  V3 Updates become parents  */
  short TPL_FLAG_UPDATE_PARENT = (short)0x8000;
    /* for FormFlags2 */
  /**  insert copy of ref note  */
  short TPL_FLAG_INCLUDEREF = 0x0001;
  /**  render ref (else it's a doclink)  */
  short TPL_FLAG_RENDERREF = 0x0002;
  /**  render it collapsed?  */
  short TPL_FLAG_RENDCOLLAPSE = 0x0004;
  /**  edit mode on open  */
  short TPL_FLAG_EDITONOPEN = 0x0008;
  /**  open context panes  */
  short TPL_FLAG_OPENCNTXT = 0x0010;
  /**  context pane is parent  */
  short TPL_FLAG_CNTXTPARENT = 0x0020;
  /**  manual versioning  */
  short TPL_FLAG_MANVCREATE = 0x0040;
  /**  V4 versioning - updates are sibblings  */
  short TPL_FLAG_UPDATE_SIBLING = 0x0080;
  /**  V4 Anonymous form  */
  short TPL_FLAG_ANONYMOUS = 0x0100;
  /**  Doclink dive into same window  */
  short TPL_FLAG_NAVIG_DOCLINK_IN_PLACE = 0x0200;
  /**  InterNotes special form  */
  short TPL_FLAG_INTERNOTES = 0x0400;
  /**  Disable FX for this doc */
  short TPL_FLAG_DISABLE_FX = 0x0800;
  /**  Disable menus for this DOC  */
  short TPL_FLAG_NOMENUS = 0x1000;
  /**  check display before displaying background  */
  short TPL_FLAG_CHECKDISPLAY = 0x2000;
  /**  This is a Right To Left Form  */
  short TPL_FLAG_FORMISRTL = 0x4000;
  /**  hide background graphic in design mode  */
  short TPL_FLAG_HIDEBKGRAPHIC = (short)0x8000;
    /* for FormFlags3 */
  /**  editor resizes header area to contents  */
  short TPL_FLAG_RESIZEHEADER = 0x0001;
  /**  No initial focus to any object on a form or page */
  short TPL_FLAG_NOINITIALFOCUS = 0x0002;
  /**  Sign this document when it gets saved  */
  short TPL_FLAG_SIGNWHENSAVED = 0x0004;
  /**  No focus when doing F6 or tabbing.  */
  short TPL_FLAG_NOFOCUSWHENF6 = 0x0008;
  /**  Render pass through HTML in the client.  */
  short TPL_FLAG_RENDERPASSTHROUGH = 0x0010;
  /**  Don't automatically add form fields to field index  */
  short TPL_FLAG_NOADDFIELDNAMESTOINDEX = 0x0020;
  /**  Autosave Documents created using this form  */
  short TPL_FLAG_CANAUTOSAVE = 0x0040;
  /**  2 bits to reflect three possible settings.  */
  short TPL_FLAG_THEMESETTING = 0x0180;
  /**  shift right this many bits to get the theme setting into the low-order bits.  */
  short TPL_SHIFT_THEMESETTING = 7;
  
  String ITEM_NAME_DEFAULTDECSINFO = "$DefaultDECSInfo"; //$NON-NLS-1$
  
  short FDECS_KEY_FIELD = 0x0001;
  short FDECS_STORE_LOCALLY = 0x0002;
  
  /**
   * Name of a form autolaunch item.  This optional item is created when
   * designing a Notes form using the auto launch options.
   */
  String FORM_AUTOLAUNCH_ITEM = "$AUTOLAUNCH"; //$NON-NLS-1$
  
  /**
   * Name of an OLE object item.  One of these is created for every
   * OLE embedded object that exists in a Notes document.  This item
   * is used to access OLE objects witout having to parse the
   * Rich Text item within the document to find an OLE CD record
   */
  String OLE_OBJECT_ITEM = "$OLEOBJINFO"; //$NON-NLS-1$

  /* Autolaunch Object type flags */
  int AUTOLAUNCH_OBJTYPE_NONE = 0x00000000;
  /**  OLE Class ID (GUID)  */
  int AUTOLAUNCH_OBJTYPE_OLE_CLASS = 0x00000001;
  /**  First OLE Object  */
  int AUTOLAUNCH_OBJTYPE_OLEOBJ = 0x00000002;
  /**  First Notes doclink  */
  int AUTOLAUNCH_OBJTYPE_DOCLINK = 0x00000004;
  /**  First Attachment  */
  int AUTOLAUNCH_OBJTYPE_ATTACH = 0x00000008;
  /**  AutoLaunch the url in the URL field  */
  int AUTOLAUNCH_OBJTYPE_URL = 0x00000010;
  
  /*  Hide-when flags */
  /*  Hide when opening flags  */
  int HIDE_OPEN_CREATE = 0x00000001;
  int HIDE_OPEN_EDIT = 0x00000002;
  int HIDE_OPEN_READ = 0x00000004;
  /*  Hide when closing flags  */
  int HIDE_CLOSE_CREATE = 0x00000008;
  int HIDE_CLOSE_EDIT = 0x00000010;
  int HIDE_CLOSE_READ = 0x00000020;
  
  /*  Launch-when flags */
  int LAUNCH_WHEN_CREATE = 0x00000001;
  int LAUNCH_WHEN_EDIT = 0x00000002;
  int LAUNCH_WHEN_READ = 0x00000004;
  
  /* OLE Flags */

  int OLE_EDIT_INPLACE = 0x00000001;
  int OLE_MODAL_WINDOW = 0x00000002;
  int OLE_ADV_OPTIONS = 0x00000004;
  
  /* Field Location Flags */

  /** Don't copy obj to any field (V3 compatible) */
  int FIELD_COPY_NONE = 0x00000001;
  /** Copy obj to named rich text field */
  int FIELD_COPY_NAMED = 0x00000004;
  /** Copy obj to first rich text field */
  int FIELD_COPY_FIRST = 0x00000002;
  
  String ITEM_NAME_BACKGROUNDGRAPHICR5 = "$BackgroundR5"; //$NON-NLS-1$
  
  String ITEM_NAME_RESTRICTBKOVERRIDE = "$NoBackgroundOverride"; //$NON-NLS-1$
  String RESTRICTBK_FLAG_NOOVERRIDE = "1"; //$NON-NLS-1$
  
  String ITEM_NAME_BACKGROUNDGRAPHIC_REPEAT = "$BackgroundRepeat"; //$NON-NLS-1$
  String ITEM_NAME_BACKGROUNDGRAPHIC_REPEATBODY = "$BackgroundRepeatBody"; //$NON-NLS-1$
  String ITEM_NAME_USER_BACKGROUNDGRAPHIC_REPEAT = "$UserBackgroundRepeat"; //$NON-NLS-1$
  String ITEM_NAME_USER_BACKGROUNDGRAPHIC_REPEATBODY = "$UserBackgroundRepeatBody"; //$NON-NLS-1$
  String BACKGROUNDGRAPHIC_REPEAT_ONCE = "1"; //$NON-NLS-1$
  String BACKGROUNDGRAPHIC_REPEAT_VERT = "2"; //$NON-NLS-1$
  String BACKGROUNDGRAPHIC_REPEAT_HORIZ = "3"; //$NON-NLS-1$
  String BACKGROUNDGRAPHIC_REPEAT_BOTH = "4"; //$NON-NLS-1$
  String BACKGROUNDGRAPHIC_REPEAT_SIZE = "5"; //$NON-NLS-1$
  String BACKGROUNDGRAPHIC_REPEAT_CENTER = "6"; //$NON-NLS-1$
  
  short FRAMESETHEADER_VERSION = 2;
  
  /**  Set if BorderEnable is specified  */
  int fFSBorderEnable = 0x00000001;
  /**  Set if FrameBorderWidth is specified  */
  int fFSFrameBorderDims = 0x00000004;
  /**  Set if FrameSpacingWidth is specified  */
  int fFSFrameSpacingDims = 0x00000008;
  /**  Set if FrameBorderColor is specified  */
  int fFSFrameBorderColor = 0x00000040;
  
  /**  Attribute is expressed as pixels  */
  short PIXELS_LengthType = 1;
  /**  Attribute is expressed as a percentage  */
  short PERCENTAGE_LengthType = 2;
  /**  Attribute is expressed as relative  */
  short RELATIVE_LengthType = 3;
  
  /**  Set if BorderEnable is specified  */
  int fFRBorderEnable = 0x00000001;
  /**  Set if MarginWidth is specified  */
  int fFRMarginWidth = 0x00000002;
  /**  Set if MarginHeight is specified  */
  int fFRMarginHeight = 0x00000004;
  /**  Set if FrameBorderColor is specified  */
  int fFRFrameBorderColor = 0x00000008;
  /**  Set if ScrollBarStyle is specified  */
  int fFRScrolling = 0x00000010;
  /**  Set if this frame has a notes only border */
  int fFRNotesOnlyBorder = 0x00000020;
  /**  Set if this fram want arrows shown in Notes */
  int fFRNotesOnlyArrows = 0x00000040;
  /**  Open value specified for Border caption is in percent. */
  int fFRNotesOpenPercent = 0x00000080;
  /**  if set, set initial focus to this frame  */
  int fFRNotesInitialFocus = 0x00000100;
  /**  Set if this fram caption reading order is Right-To-Left */
  int fFRNotesReadingOrder = 0x00000200;
  
  short fFRNotesBorder = (short)0x8000;
  short fFRNotesBorderFontAndColor = 0x4000;
  short fFRNotesBorderCaption = 0x2000;
  short fFRNotesCaptionFontName = 0x1000;
  /**  set this if frame has a sequence set other than the default 0  */
  short fFRSequence = 0x0800;
  
  /**  SCROLLING = ALWAYS  */
  short ALWAYS_ScrollStyle = 1;
  /**  SCROLLING = NEVER  */
  short NEVER_ScrollStyle = 2;
  /**  SCROLLING = AUTO  */
  short AUTO_ScrollStyle = 3;
  
  String ITEM_NAME_HEADERAREA = "$HeaderArea"; //$NON-NLS-1$
  String ITEM_NAME_REGIONFRAMESET = "$RegionFrameset"; //$NON-NLS-1$
  
  /** print page header */
  String ITEM_NAME_HEADER = "$Header"; //$NON-NLS-1$
  /** print page footer */
  String ITEM_NAME_FOOTER = "$Footer"; //$NON-NLS-1$
  /** header/footer flags */
  String ITEM_NAME_HFFLAGS = "$HFFlags"; //$NON-NLS-1$
  /** suppress printing header/footer on first page */
  String HFFLAGS_NOPRINTONFIRSTPAGE = "1"; //$NON-NLS-1$
  /** header/footer is RTL */
  String HFFLAGS_DIRECTION_RTL = "R"; //$NON-NLS-1$
  
  String ITEM_NAME_FIELDS = "$Fields"; //$NON-NLS-1$
  
  String ITEM_NAME_WINDOWTITLE = "$WindowTitle"; //$NON-NLS-1$
  String ITEM_NAME_HTMLHEADTAG = "$HTMLHeadTag"; //$NON-NLS-1$
  String ITEM_NAME_HTMLBODYTAG = "$HTMLBodyTag"; //$NON-NLS-1$
  String ITEM_NAME_WEBQUERYSAVE = "$WEBQuerySave"; //$NON-NLS-1$
  String ITEM_NAME_WEBQUERYOPEN = "$WEBQueryOpen"; //$NON-NLS-1$
  String ITEM_NAME_APPHELPFORMULA = "$AppHelpFormula"; //$NON-NLS-1$
  String ITEM_NAME_STYLESHEETLIST = "$StyleSheetList"; //$NON-NLS-1$

  String ASSIST_FLAGS_ITEM     = "$AssistFlags"; //$NON-NLS-1$
  String ASSIST_FLAGS_ITEM2    = "$AssistFlags2"; //$NON-NLS-1$
  String ASSIST_FLAG_ENABLED   = "E"; //$NON-NLS-1$
  String ASSIST_FLAG_DISABLED  = "D"; //$NON-NLS-1$
  String ASSIST_FLAG_NEWCOPY   = "N"; //$NON-NLS-1$
  String ASSIST_FLAG_HIDDEN    = "H"; //$NON-NLS-1$
  String ASSIST_FLAG_PRIVATE   = "P"; //$NON-NLS-1$
  String ASSIST_FLAG_THREAD    = "T"; //$NON-NLS-1$
  String ASSIST_FLAG_ALLOW_REMOTE_DEBUGGING = "R"; //$NON-NLS-1$
  String ASSIST_FLAG_ALLOW_UNSECURE_LS_LIBS = "L"; //$NON-NLS-1$
  String ASSIST_FLAG_AGENT_RUNASWEBUSER = "u"; //$NON-NLS-1$
  String ASSIST_FLAG_AGENT_RUNASSIGNER  = "s"; //$NON-NLS-1$
  String ASSIST_RESTRICTED = "$Restricted"; //$NON-NLS-1$

  /** base value for restricted */
  int ASSIST_RESTRICTED_RESTRICTED = 0x00000001;
  /** base value for unrestricted */
  int ASSIST_RESTRICTED_UNRESTRICTED = 0x00000000;
  /** unrestricted (low bit of zero, plus fulladmin) */
  int ASSIST_RESTRICTED_FULLADMIN = 0x00000002;
  
  /** profile code running in this note */
  String DESIGN_FLAGEXT_PROFILE = "F"; //$NON-NLS-1$
}
