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
import com.hcl.domino.jna.data.JNAAgent;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;

public class JNAAgentAllocations extends APIObjectAllocations<JNAAgent> {
	private DHANDLE m_agentHandle;
	
	@SuppressWarnings("rawtypes")
	public JNAAgentAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAAgent referent, ReferenceQueue<? super IAPIObject> q) {
		super(parentDominoClient, parentAllocations, referent, q);
	}

	private boolean m_disposed;
	
	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	@Override
	public void dispose() {
		LockUtil.lockHandle(m_agentHandle, (hAgentByVal) -> {
			if (isDisposed()) {
				return 0;
			}
			
			NotesCAPI.get().AgentClose(hAgentByVal);
			m_disposed = true;
			return 0;
		});
	}

	public void setAgentHandle(DHANDLE agentHandle) {
		m_agentHandle = agentHandle;
	}

	public DHANDLE getAgentHandle() {
		return m_agentHandle;
	}
	
}
