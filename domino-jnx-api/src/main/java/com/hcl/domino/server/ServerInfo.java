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
package com.hcl.domino.server;

import java.util.List;
import java.util.Optional;

import com.hcl.domino.Name;
import com.hcl.domino.security.ServerAclType;
import com.hcl.domino.security.ServerEclType;

/**
 * Get access to server information
 * Interface needs an implementation
 * that processes one server
 *
 * @author Stephan H. Wissel
 */
public interface ServerInfo {

  /**
   * Returns members for given acl type
   *
   * @param aclType the ACL field to retrieve
   * @return List of item members
   */
  List<String> getAclInfo(final ServerAclType aclType);

  /**
   * Returns members for given ecl type
   *
   * @param eclType the ECL field to retrieve
   * @return List of item members, might be empty
   */
  List<String> getEclInfo(final ServerEclType eclType);

  /**
   * Catch all for server info that doesn't have an ACL or ECL type available
   *
   * @param itemName the item name to look up
   * @return an {@link Optional} describing the item value if available, or an
   *         empty
   *         one if not
   */
  Optional<List<Object>> getServerItem(final String itemName);

  /**
   * Checks if a given user has an ACL type access
   *
   * @param aclType   the ACL field to check
   * @param notesName the name to check against the field
   * @return {@link true} if the provided user has the requested access;
   *         {@code false} otherwise
   */
  boolean isAclMember(final ServerAclType aclType, final Name notesName);

  /**
   * Checks if a given user has an ACL type access
   *
   * @param aclType         the ACL field to check
   * @param notesNameString the name to check against the field
   * @return {@link true} if the provided user has the requested access;
   *         {@code false} otherwise
   */
  boolean isAclMember(final ServerAclType aclType, final String notesNameString);

  /**
   * Checks if a given user has an ECL type access
   *
   * @param eclType   the ECL field to check
   * @param notesName the name to check against the field
   * @return {@link true} if the provided user has the requested access;
   *         {@code false} otherwise
   */
  boolean isEclMember(final ServerEclType eclType, final Name notesName);

  /**
   * Checks if a given user has an ECL type access
   *
   * @param eclType         the ECL field to check
   * @param notesNameString the name to check against the field
   * @return {@link true} if the provided user has the requested access;
   *         {@code false} otherwise
   */
  boolean isEclMember(final ServerEclType eclType, final String notesNameString);
}
