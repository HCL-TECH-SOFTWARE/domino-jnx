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
  /** Wiring Properties element is a Form note */
  String DFLAGPAT_COMPDEF = "+:"; //$NON-NLS-1$
  /** display only stylekits */
  String DFLAGPAT_STYLEKIT = "(+-*g`"; //$NON-NLS-1$
  
  String VIEW_GLOBAL_SCRIPT_NAME = "$ViewGlobalScript"; //$NON-NLS-1$
  /** View selection query object */
  String VIEW_SELQUERY_ITEM = "$SelQuery"; //$NON-NLS-1$
  /** Calendar View format item */
  String VIEW_CALENDAR_FORMAT_ITEM = "$CalendarFormat"; //$NON-NLS-1$
  
  String FILTER_TYPE_ITEM = "$Type"; //$NON-NLS-1$
  String FILTER_FORMULA_ITEM = "$Formula"; //$NON-NLS-1$
  String FILTER_SCAN_ITEM = "$Scan"; //$NON-NLS-1$
  String FILTER_OPERATION_ITEM = "$Operation"; //$NON-NLS-1$
  
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
}
