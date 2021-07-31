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
}
