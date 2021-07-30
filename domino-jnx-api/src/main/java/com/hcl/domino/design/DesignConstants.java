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
}
