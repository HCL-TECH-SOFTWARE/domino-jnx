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
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.naming.JNAUserDirectoryQueryIterator;
import com.sun.jna.Pointer;

public class JNAUserDirectoryQueryIteratorAllocations extends APIObjectAllocations<JNAUserDirectoryQueryIterator> {
	private boolean m_disposed;
	private DHANDLE.ByReference phBuffer;
	private Pointer valuePointer;
	
	@SuppressWarnings("rawtypes")
	public JNAUserDirectoryQueryIteratorAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNAUserDirectoryQueryIterator referent, ReferenceQueue<? super IAPIObject> queue) {
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
		
		if(phBuffer != null) {
			LockUtil.lockHandle(phBuffer, hBuffer -> {
				Mem.OSUnlockObject(hBuffer);
				return Mem.OSMemFree(hBuffer);
			});
			phBuffer = null;
		}
		
		m_disposed = true;
	}
	
	public Pointer getValuePointer() {
		return valuePointer;
	}
	
	public void setHBuffer(DHANDLE.ByReference phBuffer) {
		this.phBuffer = phBuffer;
		this.valuePointer = LockUtil.lockHandle(phBuffer, hBuffer -> {
			return Mem.OSLockObject(hBuffer);
		});
	}
	
	public DHANDLE.ByReference getHBuffer() {
		return phBuffer;
	}

}
