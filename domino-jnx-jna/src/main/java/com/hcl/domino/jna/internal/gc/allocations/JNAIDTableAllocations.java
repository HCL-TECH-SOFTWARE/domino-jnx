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
import com.hcl.domino.jna.data.JNAIDTable;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;

public class JNAIDTableAllocations extends APIObjectAllocations<JNAIDTable> {
	private boolean m_disposed;
	private DHANDLE m_idTableHandle;
	private boolean m_noDispose;
	
	@SuppressWarnings("rawtypes")
	public JNAIDTableAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAIDTable referent, ReferenceQueue<? super IAPIObject> q) {
		
		super(parentDominoClient, parentAllocations, referent, q);
	}

	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	public void setNoDispose() {
		m_noDispose = true;
	}
	
	@Override
	public void dispose() {
		if (m_noDispose || isDisposed()) {
			return;
		}

		if (m_idTableHandle!=null) {
			LockUtil.lockHandle(m_idTableHandle, (handleByVal) -> {
				short result = NotesCAPI.get().IDDestroyTable(handleByVal);
				NotesErrorUtils.checkResult(result);
				//mark as disposed while lock is active
				m_idTableHandle.setDisposed();
				m_idTableHandle = null;
				return 0;
			});
		}
		
		m_disposed = true;
	}

	public DHANDLE getIdTableHandle() {
		return m_idTableHandle;
	}

	public void setIdTableHandle(DHANDLE m_idTableHandle) {
		this.m_idTableHandle = m_idTableHandle;
	}

}
