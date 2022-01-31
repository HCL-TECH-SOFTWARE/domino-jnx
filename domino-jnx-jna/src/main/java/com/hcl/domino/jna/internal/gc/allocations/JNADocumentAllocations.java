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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.richtext.JNARichtextWriter;

public class JNADocumentAllocations extends APIObjectAllocations<JNADocument> {
	private boolean m_disposed;

	private DHANDLE m_noteHandle;
	private Map<String,List<JNARichtextWriter>> m_pendingRichtextWriter;

	private boolean m_noRecycle;
	
	@SuppressWarnings("rawtypes")
	public JNADocumentAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNADocument referent, ReferenceQueue<? super IAPIObject> q) {

		super(parentDominoClient, parentAllocations, referent, q);
		m_pendingRichtextWriter = Collections.synchronizedMap(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
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

		if (m_noteHandle!=null) {
			LockUtil.lockHandle(m_noteHandle, (handleByVal) -> {
				if (!m_noRecycle) {
					short result = NotesCAPI.get().NSFNoteClose(handleByVal);
					NotesErrorUtils.checkResult(result);
					//mark as disposed while lock is active
					m_noteHandle.setDisposed();
				}
				m_noteHandle = null;
				return 0;
			});
		}
		m_disposed = true;
	}

	public DHANDLE getNoteHandle() {
		return m_noteHandle;
	}

	public void setNoteHandle(DHANDLE noteHandle) {
		this.m_noteHandle = noteHandle;
	}

	public void registerRichtextWriter(String itemName, JNARichtextWriter writer) {
		synchronized (m_pendingRichtextWriter) {
			List<JNARichtextWriter> itemWriters = m_pendingRichtextWriter.get(itemName);
			if (itemWriters==null) {
				itemWriters = new ArrayList<>();
				m_pendingRichtextWriter.put(itemName, itemWriters);
			}
			
			itemWriters.add(writer);
		}
	}

	public void closeAllRichtextWriters() {
		synchronized (m_pendingRichtextWriter) {
			for (String currItemName : m_pendingRichtextWriter.keySet()) {
				List<JNARichtextWriter> writers = m_pendingRichtextWriter.get(currItemName);
				for (JNARichtextWriter currWriter : writers) {
					try {
						currWriter.close();
					} catch (Exception e) {
						throw new DominoException("Error closing richtext writer for item "+currItemName, e);
					}
				}
			}
		}
	}

	public void setNoRecycle(boolean noRecycle) {
		m_noRecycle = noRecycle;
	}
}
