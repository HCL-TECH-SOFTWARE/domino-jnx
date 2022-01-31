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
package com.hcl.domino.jna.internal.gc.allocations;

import java.lang.ref.ReferenceQueue;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.data.JNAUserNamesList;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;

public class JNAUserNamesListAllocations extends APIObjectAllocations<JNAUserNamesList> {
	private boolean m_disposed;
	private DHANDLE m_handle;

	@SuppressWarnings("rawtypes")
	public JNAUserNamesListAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAUserNamesList referent, ReferenceQueue<? super IAPIObject> q) {
		
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

		if (m_handle!=null) {
			LockUtil.lockHandle(m_handle, (handleByVal) -> {
				Mem.OSMemFree(handleByVal);
				//mark as disposed while lock is active
				m_handle.setDisposed();
				m_handle = null;
				return 0;
			});
		}
		m_disposed = true;
	}

	public DHANDLE getHandle() {
		return m_handle;
	}

	public void setHandle(DHANDLE handle) {
		this.m_handle = handle;
	}

}
