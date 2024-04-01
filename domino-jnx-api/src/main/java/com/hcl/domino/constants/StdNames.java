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
package com.hcl.domino.constants;

/**
 * Represents constants originally from the {@code stdnames.h} header file.
 * 
 * @author Jesse Gallagher
 * @since 1.0.37
 */
public interface StdNames {
  /** display only shared columns */
  String DFLAGPAT_SHARED_COLS = "(+-*^"; //$NON-NLS-1$
  /** xsp pages */
  String DFLAGPAT_XSPPAGE = "*gK"; //$NON-NLS-1$
  /** list of web only files */
  String DFLAGPAT_FILE_WEB = "(+g-K[];w`,*";
  /** display only stylekits */
  String DFLAGPAT_STYLEKIT = "(+-*g`"; //$NON-NLS-1$
  /** display only widgets */
  String DFLAGPAT_WIDGET = "(+-*g_"; //$NON-NLS-1$
  /** Wiring Properties element is a Form note */
  String DFLAGPAT_COMPDEF = "+:"; //$NON-NLS-1$
  /** Composite Application element is a Form note */
  String DFLAGPAT_COMPAPP = "+|"; //$NON-NLS-1$
  
  String VIEW_GLOBAL_SCRIPT_NAME = "$ViewGlobalScript"; //$NON-NLS-1$
  /** View selection query object */
  String VIEW_SELQUERY_ITEM = "$SelQuery"; //$NON-NLS-1$
  /** Calendar View format item */
  String VIEW_CALENDAR_FORMAT_ITEM = "$CalendarFormat"; //$NON-NLS-1$
  
  String FILTER_TYPE_ITEM = "$Type"; //$NON-NLS-1$
  String FILTER_FORMULA_ITEM = "$Formula"; //$NON-NLS-1$
  String FILTER_SCAN_ITEM = "$Scan"; //$NON-NLS-1$
  String FILTER_OPERATION_ITEM = "$Operation"; //$NON-NLS-1$
  String FILTER_PERIOD_ITEM = "$Period"; //$NON-NLS-1$
  
  int FILTER_TYPE_MENU  = 0;
  int FILTER_TYPE_BACKGROUND = 1;
  int FILTER_TYPE_MAIL  = 2;
  int FILTER_TYPE_ONCE  = 3;
  
  int FILTER_SCAN_ALL = 0;
  int FILTER_SCAN_UNREAD = 1;
  int FILTER_SCAN_VIEW = 2;
  int FILTER_SCAN_SELECTED = 3;
  int FILTER_SCAN_MAIL = 4;
  int FILTER_SCAN_NEW = 5;
  
  int FILTER_OP_UPDATE = 0;
  int FILTER_OP_SELECT = 1;
  int FILTER_OP_NEW_COPY = 2;
  
  int PERIOD_HOURLY = 0;
  int PERIOD_DAILY = 1;
  int PERIOD_WEEKLY = 2;
  int PERIOD_DISABLED = 3;
  
  /** Text Properties table */
  String ITEM_NAME_TEXTPROPERTIES = "$TextProperties"; //$NON-NLS-1$
  String ITEM_NAME_NOTE_SEALNAMES = "SecretEncryptionKeys"; //$NON-NLS-1$
  String ITEM_NAME_NOTE_SEALUSERS = "PublicEncryptionKeys"; //$NON-NLS-1$
  String ITEM_META_TITLE = "$$TITLE"; //$NON-NLS-1$
  
  /* ViewMap note item names */

  /** Contains ViewMap dataset data */
  String VIEWMAP_DATASET_ITEM  = "$ViewMapDataset"; //$NON-NLS-1$
  /** Contains layout objects */
  String VIEWMAP_LAYOUT_ITEM   = "$ViewMapLayout"; //$NON-NLS-1$
  /** Contains the navigator's imagemap */
  String VIEWMAP_IMAGEMAP_ITEM = "$NavImagemap"; //$NON-NLS-1$
}
