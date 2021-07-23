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
import com.hcl.domino.jna.mime.JNAMimeBase;

public class JNAMimeBaseAllocations extends APIObjectAllocations<JNAMimeBase> {
	private boolean m_disposed;
	private List<Reader> m_openMimeReaders;
	
	@SuppressWarnings("rawtypes")
	public JNAMimeBaseAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAMimeBase referent, ReferenceQueue<? super IAPIObject> queue) {
		super(parentDominoClient, parentAllocations, referent, queue);
		
		m_openMimeReaders = new ArrayList<>();
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
		
		if (!m_openMimeReaders.isEmpty()) {
			//copy list of readers, .close() changes m_openMimeReaders
			List<Reader> readersCopy = new ArrayList<>(m_openMimeReaders);
			
			for (Reader currReader : readersCopy) {
				try {
					currReader.close();
				} catch (IOException e) {
					//
				}
			}
			m_openMimeReaders.clear();
		}
		
		m_disposed = true;
	}

	public void registerReader(Reader reader) {
		m_openMimeReaders.add(reader);
	}
	
	public void unregisterReader(Reader reader) {
		m_openMimeReaders.remove(reader);
	}

}
