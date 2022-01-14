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
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.person.JNAOutOfOffice;
import com.sun.jna.Pointer;

public class JNAOOOContextAllocations extends APIObjectAllocations<JNAOutOfOffice> {
	private boolean m_disposed;
	private int m_hOOOContext;
	private Pointer m_pOOOContext;
	
	@SuppressWarnings("rawtypes")
	public JNAOOOContextAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAOutOfOffice referent, ReferenceQueue<? super IAPIObject> queue) {
		super(parentDominoClient, parentAllocations, referent, queue);
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
		
		if (m_hOOOContext!=0 && m_pOOOContext!=null) {
			short result = NotesCAPI.get().OOOEndOperation(m_hOOOContext, m_pOOOContext);
			NotesErrorUtils.checkResult(result);
			
			m_hOOOContext = 0;
			m_pOOOContext = null;

			result = NotesCAPI.get().OOOTerm();
			NotesErrorUtils.checkResult(result);
		}
		
		m_disposed = true;
	}

	public void init(Integer hOOOContext, Pointer pOOOContext) {
		if (m_hOOOContext!=0) {
			throw new IllegalStateException("Already initialized");
		}
		m_hOOOContext = hOOOContext;
		m_pOOOContext = pOOOContext;
	}

	public int getOOOHandle() {
		return m_hOOOContext;
	}
	
	public Pointer getOOOPointer() {
		return m_pOOOContext;
	}
}
