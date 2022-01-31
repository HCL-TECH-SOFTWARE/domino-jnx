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
package com.hcl.domino.security;

import com.hcl.domino.misc.NamesConstants;

/**
 * Server execution permission types
 * like right to execute formula or
 * different levels of code
 *
 * @author Stephan H. Wissel
 */
public enum ServerEclType {
  /**
   * Matches "Sign or run unrestricted methods and operations"
   * in Domino Directory server document "Security" tab
   */
  RUN_UNRESTRICTED_CODE(NamesConstants.ALLOW_UNRESTRICTED_LOTUSCRIPT_ITEM),

  /**
   * Matches "Sign or run restricted LotusSript/Java agents"
   * in Domino Directory server document "Security" tab
   */
  RUN_RESTRICTED_CODE(NamesConstants.ALLOW_RESTRICTED_LOTUSCRIPT_ITEM),

  /**
   * Matches "Run Simple and Formula agents"
   * in Domino Directory server document "Security" tab
   */
  RUN_FORMULA(NamesConstants.ALLOW_PERSONAL_ITEM);

  /**
   * The fieldName in the Domino directory that maps
   * to the server document setting for the ServerACL
   */
  public final String fieldName;

  ServerEclType(final String fieldName) {
    this.fieldName = fieldName;
  }
}
