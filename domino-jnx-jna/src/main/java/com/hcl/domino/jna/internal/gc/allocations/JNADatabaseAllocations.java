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
package com.hcl.domino.jna.internal.gc.allocations;

import java.lang.ref.ReferenceQueue;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.HANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;

public class JNADatabaseAllocations extends APIObjectAllocations<JNADatabase> {
	private boolean m_disposed;
	private HANDLE m_hDB;
	private JNAUserNamesList m_namesList;

	@SuppressWarnings("rawtypes")
	public JNADatabaseAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNADatabase referent, ReferenceQueue<? super IAPIObject> q) {
		
		super(parentDominoClient, parentAllocations, referent, q);
	}

	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	@Override
	public void dispose() {
		if (isDisposed()) {
			return;
		}

		if (m_hDB!=null) {
			LockUtil.lockHandle(m_hDB, (handleByVal) -> {
				short result = NotesCAPI.get().NSFDbClose(handleByVal);
				NotesErrorUtils.checkResult(result);
				//mark as disposed while lock is active
				m_hDB.setDisposed();
				m_hDB=null;
				return 0;
			});
		}

		if (m_namesList!=null && !m_namesList.isDisposed()) {
			m_namesList.dispose();
			m_namesList = null;
		}
		
		m_disposed = true;
	}

	public HANDLE getDBHandle() {
		return m_hDB;
	}

	public void setDBHandle(HANDLE m_hDB) {
		this.m_hDB = m_hDB;
	}

	public JNAUserNamesList getNamesList() {
		return m_namesList;
	}

	public void setNamesList(JNAUserNamesList namesList) {
		this.m_namesList = namesList;
	}

}
