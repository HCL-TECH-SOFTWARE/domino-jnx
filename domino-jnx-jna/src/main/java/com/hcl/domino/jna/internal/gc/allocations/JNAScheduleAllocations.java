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
import com.hcl.domino.jna.freebusy.JNASchedule;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;

public class JNAScheduleAllocations extends APIObjectAllocations<JNASchedule> {
	private boolean m_disposed;
	private Integer m_hSchedule;
	
	@SuppressWarnings("rawtypes")
	public JNAScheduleAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNASchedule referent, ReferenceQueue<? super IAPIObject> q) {
		
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
		
		if (m_hSchedule!=null) {
			JNASchedulesAllocations parentAllocations = (JNASchedulesAllocations)getParentAllocations();
			
			short result=LockUtil.lockHandle(parentAllocations.getSchedulesHandle(), (handleByVal) -> {
				return NotesCAPI.get().Schedule_Free(handleByVal, m_hSchedule);
			});
			
			NotesErrorUtils.checkResult(result);
			
			m_hSchedule = null;
		}
		
		m_disposed = true;
	}

	public Integer getScheduleHandle() {
		return m_hSchedule;
	}

	public void setScheduleHandle(Integer hSchedule) {
		this.m_hSchedule = hSchedule;
	}
}
