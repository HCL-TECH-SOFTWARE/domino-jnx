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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.richtext.JNACompoundTextStandaloneBuffer;
import com.hcl.domino.misc.Pair;

public class JNACompoundTextStandaloneBufferAllocations extends APIObjectAllocations<JNACompoundTextStandaloneBuffer> {
	private static final Logger log = Logger.getLogger(JNACompoundTextStandaloneBufferAllocations.class.getPackage().getName());
	
	private boolean m_disposed;
	private DHANDLE m_compoundTextHandle;
	private List<Pair<Path,InputStream>> m_createdTempFileStreams;

	@SuppressWarnings("rawtypes")
	public JNACompoundTextStandaloneBufferAllocations(IGCDominoClient parentDominoClient,
			APIObjectAllocations parentAllocations, JNACompoundTextStandaloneBuffer referent,
			ReferenceQueue<? super IAPIObject> queue) {
		
		super(parentDominoClient, parentAllocations, referent, queue);
		
		m_createdTempFileStreams = new ArrayList<>();
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
		
		short result = LockUtil.lockHandle(m_compoundTextHandle, (hCompoundTextHandleByVal) -> {
			return Mem.OSMemFree(hCompoundTextHandleByVal);
		});
		NotesErrorUtils.checkResult(result);
		
		m_compoundTextHandle = null;
		
		List<Pair<Path,InputStream>> streams = m_createdTempFileStreams;
		if(streams != null) {
			if(!streams.isEmpty()) {
				for (Pair<Path,InputStream> currEntry : streams) {
					try {
						currEntry.getValue2().close();
					} catch (IOException e) {
						//just write to stderr, go on and try to delete the file
						e.printStackTrace();
					}
					
					try {
						Files.deleteIfExists(currEntry.getValue1());
					} catch (IOException e) {
						if(log.isLoggable(Level.SEVERE)) {
							log.log(Level.SEVERE, "Encountered exception when deleting temporary compound text buffer", e);
						}
					}
				}
			}
			streams.clear();
		}
		
		
		m_disposed = true;
	}

	public void setCompoundTextHandle(DHANDLE handle) {
		m_compoundTextHandle = handle;
	}

	public DHANDLE getCompoundTextHandle() {
		return m_compoundTextHandle;
	}
	
	public void addCreatedTempFileStream(Path file, InputStream in) {
		m_createdTempFileStreams.add(new Pair<>(file, in));
	}
}
