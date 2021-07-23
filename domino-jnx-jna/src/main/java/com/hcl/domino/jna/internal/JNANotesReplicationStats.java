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
package com.hcl.domino.jna.internal;

import java.text.MessageFormat;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClient.NotesReplicationStats;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.internal.structs.ReplServStatsStruct;
import com.sun.jna.Pointer;

/**
 * This structure is returned from {@link DominoClient#replicateDbsWithServer}.<br>
 * <br>
 * It contains the returned replication statistics.
 * 
 * @author Karsten Lehmann
 */
public class JNANotesReplicationStats implements NotesReplicationStats {
	private ReplServStatsStruct m_struct;
	
	public JNANotesReplicationStats(IAdaptable adaptable) {
		ReplServStatsStruct struct = adaptable.getAdapter(ReplServStatsStruct.class);
		if (struct!=null) {
			m_struct = struct;
			return;
		}
		Pointer p = adaptable.getAdapter(Pointer.class);
		if (p!=null) {
			m_struct = ReplServStatsStruct.newInstance(p);
			return;
		}
		throw new IllegalArgumentException("Constructor argument cannot provide a supported datatype");
	}
	
	/* general stats */
	
	@Override
	public long getStubsInitialized() {
		return m_struct.StubsInitialized.longValue();
	}
	
	@Override
	public long getTotalUnreadExchanges() {
		return m_struct.TotalUnreadExchanges.longValue();
	}
	
	@Override
	public long getNumberErrors() {
		return m_struct.NumberErrors.longValue();
	}

	/* Pull stats */

	@Override
	public long getPullTotalFiles() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.TotalFiles.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPullFilesCompleted() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.FilesCompleted.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPullNotesAdded() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.NotesAdded.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPullNotesDeleted() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.NotesDeleted.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPullSuccessful() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.Successful.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPullFailed() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.Failed.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPullNumberErrors() {
		if (m_struct.Pull!=null) {
			return m_struct.Pull.NumberErrors.longValue();
		}
		return 0;
	}
	
	/* Push stats */
	
	@Override
	public long getPushTotalFiles() {
		if (m_struct.Push!=null) {
			return m_struct.Push.TotalFiles.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPushFilesCompleted() {
		if (m_struct.Push!=null) {
			return m_struct.Push.FilesCompleted.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPushNotesAdded() {
		if (m_struct.Push!=null) {
			return m_struct.Push.NotesAdded.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPushNotesDeleted() {
		if (m_struct.Push!=null) {
			return m_struct.Push.NotesDeleted.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPushSuccessful() {
		if (m_struct.Push!=null) {
			return m_struct.Push.Successful.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPushFailed() {
		if (m_struct.Push!=null) {
			return m_struct.Push.Failed.longValue();
		}
		return 0;
	}
	
	@Override
	public long getPushNumberErrors() {
		if (m_struct.Push!=null) {
			return m_struct.Push.NumberErrors.longValue();
		}
		return 0;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
			"JNANotesReplicationStats [stubsInitialized={0}, totalUnreadExchanges={1}, numberErrors={2}, pullTotalFiles={3}, pullFilesCompleted={4}, pullNotesAdded={5}, pullNotesDeleted={6}, pullSuccessful={7}, pullFailed={8}, pullNumberErrors={9}, pushTotalFiles={10}, pushFilesCompleted={11}, pushNotesAdded={12}, pushNotesDeleted={13}, pushSuccessful={14}, pushFailed={15}, pushNumberErrors={16}]", //$NON-NLS-1$
			getStubsInitialized(), getTotalUnreadExchanges(), getNumberErrors(), getPullTotalFiles(), getPullFilesCompleted(), getPullNotesAdded(), getPullNotesDeleted(), getPullSuccessful(), getPullFailed(),
			getPullNumberErrors(), getPushTotalFiles(), getPushFilesCompleted(), getPushNotesAdded(), getPushNotesDeleted(), getPushSuccessful(), getPushFailed(), getPushNumberErrors()
		);
	}
	
}
