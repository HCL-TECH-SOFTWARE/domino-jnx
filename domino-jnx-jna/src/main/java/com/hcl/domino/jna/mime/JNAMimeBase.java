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
package com.hcl.domino.jna.mime;

import java.lang.ref.ReferenceQueue;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAMimeBaseAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Base class for MIME reader and writer
 * 
 * @author Karsten Lehmann
 */
public class JNAMimeBase extends BaseJNAAPIObject<JNAMimeBaseAllocations> {

	public JNAMimeBase(IAPIObject<?> parent) {
		super(parent);
		
		init();
		setInitialized();
	}


	protected void init() {
		//placeholder for future development
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAMimeBaseAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAMimeBaseAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	/**
	 * Creates a new MIMEStream
	 * 
	 * @param doc target document
	 * @param itemNameMem item name as LMBCS data
	 * @param dwOpenFlags flags to open stream
	 * @return pointer to stream
	 */
	protected Pointer createMimeStream(JNADocument doc, Memory itemNameMem, int dwOpenFlags) {
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);

		return LockUtil.lockHandle(docAllocations.getNoteHandle(), (hdlByVal) -> {
			PointerByReference rethMIMEStream = new PointerByReference();
			rethMIMEStream.setValue(null);

			short wItemNameLen = itemNameMem == null ? 0 : (short)(itemNameMem.size() & 0xffff);
			short result = NotesCAPI.get().MIMEStreamOpen(hdlByVal,
					itemNameMem, wItemNameLen, dwOpenFlags, rethMIMEStream);
			NotesErrorUtils.checkResult(result);
			return rethMIMEStream.getValue();
		});
	}

	/**
	 * Disposes the MIMEStream
	 * 
	 * @param ptr pointer to stream
	 */
	protected void disposeMimeStream(Pointer ptr) {
		if (ptr!=null) {
			NotesCAPI.get().MIMEStreamClose(ptr);
		}
	}

}
