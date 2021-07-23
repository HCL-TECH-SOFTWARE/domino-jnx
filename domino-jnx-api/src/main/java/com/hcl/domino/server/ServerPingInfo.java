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

import com.hcl.domino.DominoClient;

/**
 * Contains information resulting from {@link DominoClient#pingServer}.
 *
 * @since 1.0.20
 */
public interface ServerPingInfo {
  /**
   * Retrieves the server's availability index, if requested in the ping. The
   * availability
   * index is an integer value from 1-100.
   *
   * @return an {@link Optional} describing the availability index, or an empty
   *         one
   *         if this was not requested
   */
  Optional<Integer> getAvailabilityIndex();

  /**
   * Retrieves the server's cluster members, if requested in the ping.
   * <p>
   * If cluster information is requested but the server is not a member of a
   * cluster, then
   * this will be an {@link Optional} describing an empty list.
   * </p>
   *
   * @return an {@link Optional} describing the server's cluster members as a
   *         {@link List}, or
   *         an empty one if this was not requested
   */
  Optional<List<String>> getClusterMembers();

  /**
   * Retrieves the server's cluster name, if requested in the ping.
   * <p>
   * If cluster information is requested but the server is not a member of a
   * cluster, then
   * this will be an {@link Optional} describing an empty string.
   * </p>
   *
   * @return an {@link Optional} describing the server's cluster name, or an empty
   *         one
   *         if this was not requested
   */
  Optional<String> getClusterName();
}
