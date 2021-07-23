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
package com.hcl.domino.commons.admin;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

import com.hcl.domino.UserNamesList;
import com.hcl.domino.admin.ServerAdmin.MultiDatabaseAccessInfo;
import com.hcl.domino.dbdirectory.DatabaseData;
import com.hcl.domino.misc.Pair;
import com.hcl.domino.security.AclAccess;

public class DefaultMultiDatabaseAccessInfo implements MultiDatabaseAccessInfo {
  private final String m_server;
  private final UserNamesList m_user;
  private final List<Pair<DatabaseData, AclAccess>> m_pairs;

  public DefaultMultiDatabaseAccessInfo(final String server, final UserNamesList user,
      final List<Pair<DatabaseData, AclAccess>> pairs) {
    this.m_server = server;
    this.m_user = user;
    this.m_pairs = pairs;
  }

  @Override
  public Stream<Pair<DatabaseData, AclAccess>> allEntries() {
    return this.m_pairs.stream();
  }

  @Override
  public String getServer() {
    return this.m_server;
  }

  @Override
  public UserNamesList getUser() {
    return this.m_user;
  }

  @Override
  public String toString() {
    return MessageFormat.format(
        "JNAMultiDatabaseAccessInfo [server={0}, user= {1}, count={2}]", //$NON-NLS-1$
        this.m_server, this.m_user.getPrimaryName(), this.m_pairs.size());
  }

}