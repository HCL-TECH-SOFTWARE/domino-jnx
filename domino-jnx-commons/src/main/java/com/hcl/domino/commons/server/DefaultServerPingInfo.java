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
package com.hcl.domino.commons.server;

import java.util.List;
import java.util.Optional;

import com.hcl.domino.server.ServerPingInfo;

/**
 * @since 1.0.20
 */
public class DefaultServerPingInfo implements ServerPingInfo {
  private final Optional<Integer> availabilityIndex;
  private final Optional<String> clusterName;
  private final Optional<List<String>> clusterMembers;

  public DefaultServerPingInfo(final Optional<Integer> availabilityIndex, final Optional<String> clusterName,
      final Optional<List<String>> clusterMembers) {
    this.availabilityIndex = availabilityIndex;
    this.clusterName = clusterName;
    this.clusterMembers = clusterMembers;
  }

  @Override
  public Optional<Integer> getAvailabilityIndex() {
    return this.availabilityIndex;
  }

  @Override
  public Optional<List<String>> getClusterMembers() {
    return this.clusterMembers;
  }

  @Override
  public Optional<String> getClusterName() {
    return this.clusterName;
  }
}
