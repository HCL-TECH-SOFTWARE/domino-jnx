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

import com.hcl.domino.misc.INumberEnum;

/**
 * Replication flags<br>
 * <br>
 * NOTE:  Please note the distinction between {@link ReplicationFlags#DISABLE} and
 * {@link ReplicationFlags#NEVER_REPLICATE}. The former is used to temporarily disable
 * replication.  The latter is used to indicate that this database should
 * NEVER be replicated.  The former may be set and cleared by the Notes
 * user interface.  The latter is intended to be set programmatically
 * and SHOULD NEVER be able to be cleared by the user interface.
 */
public enum ReplicationFlags implements INumberEnum<Short>{

	/** Disable replication */
	DISABLE((short) 0x0004),
	
	/** Mark unread only if newer note */
	UNREADIFFNEW((short) 0x0008),
	
	/** Don't propagate deleted notes when
	replicating from this database */
	IGNORE_DELETES((short) 0x0010),
	
	/** UI does not allow perusal of Design */
	HIDDEN_DESIGN((short) 0x0020),
	
	/** Do not list in catalog */
	DO_NOT_CATALOG((short) 0x0040),
	
	/** Auto-Delete documents prior to cutoff date */
	CUTOFF_DELETE((short) 0x0080),
	
	/** DB is not to be replicated at all */
	NEVER_REPLICATE((short) 0x0100),
	
	/** Abstract during replication */
	ABSTRACT((short) 0x0200),
	
	/** Do not list in database add */
	DO_NOT_BROWSE((short) 0x0400),
	
	/** Do not run chronos on database */
	NO_CHRONOS((short) 0x0800),
	
	/** Don't replicate deleted notes
	 into destination database */
	IGNORE_DEST_DELETES((short) 0x1000),
	
	/** Include in Multi Database indexing */
	MULTIDB_INDEX((short) 0x2000);
	
	private short m_value;
	
	ReplicationFlags(Short value) {
		m_value = value;
	}

	@Override
	public Short getValue() {
		return m_value;
	}

	@Override
	public long getLongValue() {
		return m_value & 0xffff;
	}
	
}
