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
package com.hcl.domino.security;

import com.hcl.domino.misc.NamesConstants;

/**
 * Server execution permission types
 * like right to execute formula or
 * different levels of code
 *
 * @author Stephan H. Wissel
 */
public enum ServerAclType {
  /**
   * Matches "Administrators"
   * in Domino Directory server document "Security" tab
   */
  SERVER_ADMIN(NamesConstants.ADMIN_ACCESS_ITEM),

  /**
   * Matches "FUll Access administrators"
   * in Domino Directory server document "Security" tab
   */
  FULL_ADMIN(NamesConstants.FULL_ADMIN_ACCESS_ITEM),

  /**
   * Matches "Database Administrators"
   * in Domino Directory server document "Security" tab
   */
  DATABASE_ADMIN(NamesConstants.DB_ADMIN_ACCESS_ITEM),

  /**
   * Matches "Create databases and templates"
   * in Domino Directory server document "Security" tab
   */
  CREATE_DATABASE(NamesConstants.CREATE_FILE_ACCESS_ITEM),

  /**
   * Matches "Create new replicas"
   * in Domino Directory server document "Security" tab
   */
  CREATE_REPLICA(NamesConstants.CREATE_REPLICA_ACCESS_ITEM),

  /**
   * Matches "Create master templates"
   * in Domino Directory server document "Security" tab
   */
  CREATE_MASTER_TEMPLATE(NamesConstants.CREATE_TEMPLATE_ACCESS_ITEM);

  /**
   * The fieldName in the Domino directory that maps
   * to the server document setting for the ServerACL
   */
  public final String fieldName;

  ServerAclType(final String fieldName) {
    this.fieldName = fieldName;
  }
}
