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
	private DominoDateTime m_replicationTime;
	private AclLevel m_accessLevel;
	private Set<AclFlag> m_accessFlags;
	private ReplicationDirection m_direction;
	private String m_server;
	private String m_filepath;

	/**
	 * These values describe the direction member of the {@link ReplicationHistorySummary}
	 * entry (the direction of the replication in the replication history). 
	 */
	public enum ReplicationDirection implements INumberEnum<Short> {

		NEVER((short) 0),
		SEND((short) 1),
		RECEIVE((short) 2);

		private Short m_value;

		ReplicationDirection(Short value) {
			m_value = value;
		}

		@Override
		public Short getValue() {
			return m_value;
		}

		@Override
		public long getLongValue() {
			return (long) m_value & 0xffff;
		}
	}

	
	public ReplicationHistorySummary(DominoDateTime replicationTime, AclLevel accessLevel,
			Set<AclFlag> accessFlags, ReplicationDirection direction, String server, String filePath) {
		m_replicationTime = replicationTime;
		m_accessLevel = accessLevel;
		m_accessFlags = accessFlags;
		m_direction = direction;
		m_server = server;
		m_filepath = filePath;
	}
	
	public DominoDateTime getReplicationTime() {
		return m_replicationTime;
	}
	
	public AclLevel getAccessLevel() {
		return m_accessLevel;
	}
	
	public Set<AclFlag> getAccessFlags() {
		return m_accessFlags;
	}
	
	public ReplicationDirection getReplicationDirection() {
		return m_direction;
	}

	public String getServer() {
		return m_server;
	}
	
	public String getFilePath() {
		return m_filepath;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
			"ReplicationHistorySummary [server={0}, filepath={1}, replicationtime={2}, direction={3}, accesslevel={4}, accessflags={5}]", //$NON-NLS-1$
			m_server, m_filepath, m_replicationTime, m_direction, m_accessLevel, m_accessFlags
		);
	}

	
}
