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
package com.hcl.domino.admin.replication;

import java.text.MessageFormat;
import java.util.Set;

import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.misc.INumberEnum;
import com.hcl.domino.security.AclFlag;
import com.hcl.domino.security.AclLevel;

/**
 * One entry of the database replication history
 */
public class ReplicationHistorySummary {
  /**
   * These values describe the direction member of the
   * {@link ReplicationHistorySummary}
   * entry (the direction of the replication in the replication history).
   */
  public enum ReplicationDirection implements INumberEnum<Short> {

    NEVER((short) 0),
    SEND((short) 1),
    RECEIVE((short) 2);

    private Short m_value;

    ReplicationDirection(final Short value) {
      this.m_value = value;
    }

    @Override
    public long getLongValue() {
      return (long) this.m_value & 0xffff;
    }

    @Override
    public Short getValue() {
      return this.m_value;
    }
  }

  private final DominoDateTime m_replicationTime;
  private final AclLevel m_accessLevel;
  private final Set<AclFlag> m_accessFlags;
  private final ReplicationDirection m_direction;
  private final String m_server;

  private final String m_filepath;

  public ReplicationHistorySummary(final DominoDateTime replicationTime, final AclLevel accessLevel,
      final Set<AclFlag> accessFlags, final ReplicationDirection direction, final String server, final String filePath) {
    this.m_replicationTime = replicationTime;
    this.m_accessLevel = accessLevel;
    this.m_accessFlags = accessFlags;
    this.m_direction = direction;
    this.m_server = server;
    this.m_filepath = filePath;
  }

  public Set<AclFlag> getAccessFlags() {
    return this.m_accessFlags;
  }

  public AclLevel getAccessLevel() {
    return this.m_accessLevel;
  }

  public String getFilePath() {
    return this.m_filepath;
  }

  public ReplicationDirection getReplicationDirection() {
    return this.m_direction;
  }

  public DominoDateTime getReplicationTime() {
    return this.m_replicationTime;
  }

  public String getServer() {
    return this.m_server;
  }

  @Override
  public String toString() {
    return MessageFormat.format(
        "ReplicationHistorySummary [server={0}, filepath={1}, replicationtime={2}, direction={3}, accesslevel={4}, accessflags={5}]", //$NON-NLS-1$
        this.m_server, this.m_filepath, this.m_replicationTime, this.m_direction, this.m_accessLevel, this.m_accessFlags);
  }

}
