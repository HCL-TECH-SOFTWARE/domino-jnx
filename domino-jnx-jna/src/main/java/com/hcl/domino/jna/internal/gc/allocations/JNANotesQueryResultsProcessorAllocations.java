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

import java.io.IOException;
import java.io.Reader;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.data.JNAQueryResultsProcessor;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.structs.NotesQueryResultsHandles;

public class JNANotesQueryResultsProcessorAllocations extends APIObjectAllocations<JNAQueryResultsProcessor> {
	private boolean m_disposed;
	private NotesQueryResultsHandles m_queryResultsHandles;
	private List<Reader> m_pendingJsonReader = new ArrayList<>();
	
	public JNANotesQueryResultsProcessorAllocations(IGCDominoClient parentDominoClient,
			APIObjectAllocations parentAllocations, JNAQueryResultsProcessor referent,
			ReferenceQueue<? super IAPIObject> queue) {
		super(parentDominoClient, parentAllocations, referent, queue);
	}

	public NotesQueryResultsHandles getNotesQueryResultsHandles() {
		checkDisposed();
		
		if (m_queryResultsHandles==null) {
			m_queryResultsHandles = NotesQueryResultsHandles.newInstance();
		}
		return m_queryResultsHandles;
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

		synchronized (m_pendingJsonReader) {
			//make sure that all created readers have been closed
			for (Reader currReader : m_pendingJsonReader) {
				try {
					currReader.close();
				}
				catch (IOException e) {
					//
				}
			}
			m_pendingJsonReader.clear();
		}

		if (m_queryResultsHandles!=null) {
			if (m_queryResultsHandles.hInResults!=0) {
				Mem.OSMemoryFree(m_queryResultsHandles.hInResults);
				m_queryResultsHandles.hInResults = 0;
			}
			
			if (m_queryResultsHandles.hOutFields!=0) {
				Mem.OSMemoryFree(m_queryResultsHandles.hOutFields);
				m_queryResultsHandles.hOutFields = 0;
			}
			
			if (m_queryResultsHandles.hFieldRules!=0) {
				Mem.OSMemoryFree(m_queryResultsHandles.hFieldRules);
				m_queryResultsHandles.hFieldRules = 0;
			}
			
			if (m_queryResultsHandles.hCombineRules!=0) {
				Mem.OSMemoryFree(m_queryResultsHandles.hCombineRules);
				m_queryResultsHandles.hCombineRules = 0;
			}
		}
		
		m_disposed = true;
	}

	/**
	 * Registers a reader to be closed when this allocations object is disposed
	 * 
	 * @param reader reader
	 */
	public void registerReaderForClose(Reader reader) {
		synchronized (m_pendingJsonReader) {
			m_pendingJsonReader.add(reader);
		}
	}

	/**
	 * Unregisters a reader from the auto close list. Used if it has been closed
	 * manually.
	 * 
	 * @param reader reader
	 */
	public void unregisterReaderForClose(Reader reader) {
		synchronized (m_pendingJsonReader) {
			m_pendingJsonReader.remove(reader);
		}
	}
}
