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
package com.hcl.domino.commons.server;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hcl.domino.DominoClient;
import com.hcl.domino.Name;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.naming.Names;
import com.hcl.domino.naming.UserDirectory;
import com.hcl.domino.security.ServerAclType;
import com.hcl.domino.security.ServerEclType;
import com.hcl.domino.server.ServerInfo;

/**
 * Non-API-specific implementation of {@link ServerInfo} that uses
 * {@link UserDirectory}
 * for information lookups.
 *
 * @author Jesse Gallagher
 * @since 1.0.28
 */
public class DefaultServerInfo implements ServerInfo {
  private final DominoClient client;
  private final String directoryServer;
  private final String serverName;

  public DefaultServerInfo(final DominoClient client, final String directoryServer, final String serverName) {
    this.client = client;
    this.directoryServer = directoryServer;
    this.serverName = Names.toCanonical(serverName);
  }

  @Override
  public List<String> getAclInfo(final ServerAclType aclType) {
    return this.lookupRequiredString(aclType.fieldName);
  }

  @Override
  public List<String> getEclInfo(final ServerEclType eclType) {
    return this.lookupRequiredString(eclType.fieldName);
  }

  @Override
  public Optional<List<Object>> getServerItem(final String itemName) {
    return this.client.openUserDirectory(this.directoryServer).query()
        .namespaces(NotesConstants.SERVERNAMESSPACE)
        .names(this.serverName)
        .items(itemName)
        .stream()
        .findFirst()
        .flatMap(entry -> !entry.isEmpty() ? Optional.of(entry.get(0)) : Optional.empty()) // Find it from the namespace
        .flatMap(entry -> entry.containsKey(itemName) ? Optional.of(entry.get(itemName)) : Optional.empty());
  }

  @Override
  public boolean isAclMember(final ServerAclType aclType, final Name notesName) {
    return this.isAclMember(aclType, notesName.getCanonical());
  }

  @Override
  public boolean isAclMember(final ServerAclType aclType, final String notesNameString) {
    final UserNamesList namesList = Names.buildNamesList(this.client, notesNameString);
    final List<String> val = this.getAclInfo(aclType);
    return namesList.toList().stream()
        .anyMatch(name -> val.contains(name));
  }

  @Override
  public boolean isEclMember(final ServerEclType eclType, final Name notesName) {
    return this.isEclMember(eclType, notesName.getCanonical());
  }

  @Override
  public boolean isEclMember(final ServerEclType eclType, final String notesNameString) {
    final UserNamesList namesList = Names.buildNamesList(this.client, notesNameString);
    final List<String> val = this.getEclInfo(eclType);
    return namesList.toList().stream()
        .anyMatch(name -> val.contains(name));
  }

  // *******************************************************************************
  // * Internal implementation methods
  // *******************************************************************************

  private List<String> lookupRequiredString(final String itemName) {
    return this.getServerItem(itemName)
        .map(val -> val.stream().map(String::valueOf).collect(Collectors.toList()))
        .orElseThrow(
            () -> new IllegalStateException(MessageFormat.format("Unable to locate item {0} for {1}", itemName, this.serverName)));
  }
}
