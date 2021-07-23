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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.mime.JNARichtextMimeConversionSettings;
import com.sun.jna.Pointer;

public class JNARichtextMimeConversionSettingsAllocations extends APIObjectAllocations<JNARichtextMimeConversionSettings> {
	private boolean m_disposed;
	private Pointer m_convControls;
	private ReentrantLock m_convControlsLock = new ReentrantLock();
	
	@SuppressWarnings("rawtypes")
	public JNARichtextMimeConversionSettingsAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNARichtextMimeConversionSettings referent, ReferenceQueue<? super IAPIObject> q) {
		
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
		
		lockAndGetSettingsPointer((ptr) -> {
			if (ptr!=null) {
				NotesCAPI.get().MMDestroyConvControls(ptr);
				m_convControls = null;
			}
			return null;
		});
		
		m_disposed = true;
	}

	public <R> R lockAndGetSettingsPointer(Function<Pointer,R> consumer) {
		m_convControlsLock.lock();
		try {
			checkDisposed();
			return consumer.apply(m_convControls);
		}
		finally {
			m_convControlsLock.unlock();
		}
	}
	
	public void setSettingsPointer(Pointer ptr) {
		m_convControls = ptr;
	}
}
