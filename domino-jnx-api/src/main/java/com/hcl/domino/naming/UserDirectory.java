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
package com.hcl.domino.naming;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents access to the effective user directory for the provided server.
 * This allows lookup of
 * user, server, and group names using the server's primary and secondary
 * directories.
 *
 * @author Jesse Gallagher
 * @since 1.0.2
 */
public interface UserDirectory {
  /**
   * Retrieves a collection of the Domino directory databases used
   * by this runtime or server.
   *
   * @return a {@link Set} of Notes API paths
   */
  Set<String> getDirectoryPaths();

  /**
   * Queries the directory for the specified items from the first match for the
   * user.
   * <p>
   * This method is shorthand for using {@link #query()}.
   * </p>
   *
   * @param name  the name of the user look up
   * @param items the item names to return
   * @return an {@link Optional} describing item names to value lists, or an empty
   *         one if there is no match
   * @since 1.0.17
   */
  default Optional<Map<String, List<Object>>> lookupUserValue(final String name, final String... items) {
    return this.query()
        .names(name)
        .items(items)
        .stream()
        .findFirst()
        .map(queriedName -> queriedName.stream()
            .findFirst()
            .orElse(null));
  }

  /**
   * Initiates a query of the runtime or server's directory.
   *
   * @return a {@link UserDirectoryQuery} builder
   */
  UserDirectoryQuery query();
}
