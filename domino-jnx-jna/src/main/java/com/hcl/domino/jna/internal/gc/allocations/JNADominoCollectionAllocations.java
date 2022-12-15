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
import java.util.Arrays;
import java.util.Collection;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.IDTable;
import com.hcl.domino.jna.data.JNADominoCollection;
import com.hcl.domino.jna.data.JNAIDTable;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.misc.NotesConstants;

public class JNADominoCollectionAllocations extends APIObjectAllocations<JNADominoCollection> {
	private boolean m_disposed;
	private DHANDLE m_collectionHandle;
	private JNAIDTable m_collapsedList;
	private JNAIDTable m_selectedList;
	private JNAIDTable m_unreadTable;
	
	@SuppressWarnings("rawtypes")
	public JNADominoCollectionAllocations(IGCDominoClient parentDominoClient,
			APIObjectAllocations parentAllocations,
			JNADominoCollection referent, ReferenceQueue<? super IAPIObject> q) {
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
		
		if (m_unreadTable!=null) {
			m_unreadTable.dispose();
			m_unreadTable = null;
		}
		
		//clear search handle here in case we add FT view search
		
		
		if (m_collectionHandle!=null) {
			short result = LockUtil.lockHandle(m_collectionHandle, (hdlByVal) -> {
				return NotesCAPI.get().NIFCloseCollection(hdlByVal);
				
			});
			NotesErrorUtils.checkResult(result);
			m_collectionHandle = null;
		}
		
		m_disposed = true;
	}

	public void setCollectionHandle(DHANDLE handle) {
		m_collectionHandle = handle;
	}

	public DHANDLE getCollectionHandle() {
		return m_collectionHandle;
	}

	public void setCollapsedList(JNAIDTable collapsedList) {
		m_collapsedList = collapsedList;
	}

	public JNAIDTable getCollapsedList() {
		return m_collapsedList;
	}
	
	public void setSelectedList(JNAIDTable selectedList) {
		m_selectedList = selectedList;
	}

	public JNAIDTable getSelectedList() {
		return m_selectedList;
	}
	
	public void setUnreadTable(JNAIDTable unreadTable) {
		m_unreadTable = unreadTable;
	}
	
	public JNAIDTable getUnreadTable() {
		return m_unreadTable;
	}

	public void select(int noteId, boolean clear) {
	  select(Arrays.asList(noteId), clear);
	}

	public void select(Collection<Integer> noteIds, boolean clear) {
	  checkDisposed();

	  IDTable selectedList = getSelectedList();
	  if (clear) {
	    selectedList.clear();
	  }

	  selectedList.addAll(noteIds);

	  short result = LockUtil.lockHandle(m_collectionHandle, (hdlByVal) -> {
	    return NotesCAPI.get().NIFUpdateFilters(hdlByVal, NotesConstants.FILTER_SELECTED);

	  });
	  NotesErrorUtils.checkResult(result);
	}

}
