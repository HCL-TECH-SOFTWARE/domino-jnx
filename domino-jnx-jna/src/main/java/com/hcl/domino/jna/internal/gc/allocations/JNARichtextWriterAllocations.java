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
import com.hcl.domino.data.Document;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.richtext.JNACompoundTextStandaloneBuffer;
import com.hcl.domino.jna.richtext.JNARichtextWriter;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public class JNARichtextWriterAllocations extends APIObjectAllocations<JNARichtextWriter> {
	private boolean m_disposed;
	
	private DHANDLE.ByReference m_compoundTextHandle;
	private boolean m_isStandalone;
	
	private CloseResult m_closeResult;

	private boolean m_closed;
	
	@SuppressWarnings("rawtypes")
	public JNARichtextWriterAllocations(IGCDominoClient parentDominoClient, APIObjectAllocations parentAllocations,
			JNARichtextWriter referent, ReferenceQueue<? super IAPIObject> q) {
		
		super(parentDominoClient, parentAllocations, referent, q);
	}

	@Override
	public boolean isDisposed() {
		return m_disposed;
	}

	@Override
	public void dispose() {
		discard();
	}

	public void setCompoundTextHandle(DHANDLE.ByReference rethCompound, boolean isStandalone) {
		m_compoundTextHandle = rethCompound;
		m_isStandalone = isStandalone;
	}

	public DHANDLE.ByReference getCompoundTextHandle() {
		return m_compoundTextHandle;
	}
	
	public boolean isStandalone() {
		return m_isStandalone;
	}
	
	public void discard() {
		if (isDisposed()) {
			return;
		}
		
		LockUtil.lockHandle(getCompoundTextHandle(), (hCompoundTextByVal) -> {
			if (isDisposed()) {
				return 0;
			}
			
			NotesCAPI.get().CompoundTextDiscard(hCompoundTextByVal);
			
			m_disposed = true;
			
			return 0;
		});
	}
	
	public boolean isClosed() {
		return m_closed;
	}
	
	/**
	 * This routine closes the build process. Use {@link Document#save()} 
	 * after {@link #closeItemContext()} to update and save the document.
	 */
	public void closeItemContext() {
		if (isStandalone()) {
			throw new UnsupportedOperationException("This is a standalone compound text");
		}

		checkDisposed();
		if (isClosed()) {
			return;
		}
		
		LockUtil.lockHandle(m_compoundTextHandle, (hCompoundTextByVal) -> {
			if (isDisposed()) {
				return 0;
			}
			
			short result = NotesCAPI.get().CompoundTextClose(hCompoundTextByVal, null, null, null, (short) 0);
			NotesErrorUtils.checkResult(result);
			
			m_disposed = true;
			m_closed = true;
			return 0;
		});
	}

	public CloseResult getStandaloneContextCloseResult() {
		return m_closeResult;
	}
	
	/**
	 * This routine closes the standalone CompoundText. The result is either an in-memory buffer or
	 * a temporary file on disk, depending on the memory size of the CD records
	 * 
	 * @return close result
	 */
	public CloseResult closeStandaloneContext() {
		if (!isStandalone()) {
			throw new UnsupportedOperationException("This is not a standalone compound text");
		}
		
		if (isClosed()) {
			return m_closeResult;
		}
		
		checkDisposed();
		
		DHANDLE.ByReference rethBuffer = DHANDLE.newInstanceByReference();
		IntByReference retBufSize = new IntByReference();

		Memory returnFileMem = new Memory(NotesConstants.MAXPATH);
		returnFileMem.clear();

		LockUtil.lockHandle(m_compoundTextHandle, (hCompoundTextByVal) -> {
			if (isDisposed()) {
				return 0;
			}

			short result = NotesCAPI.get().CompoundTextClose(hCompoundTextByVal, rethBuffer, retBufSize,
					returnFileMem, (short) (NotesConstants.MAXPATH & 0xffff));
			NotesErrorUtils.checkResult(result);
			
			m_disposed = true;
			m_closed = true;
			return 0;
		});
		
		if (!rethBuffer.isNull()) {
			//content was small enough to fit into an in-memory buffer
			int bufSize = retBufSize.getValue();
			JNACompoundTextStandaloneBuffer buf = new JNACompoundTextStandaloneBuffer((JNADominoClient)getParentDominoClient(), rethBuffer, bufSize);
			
			m_closeResult = new CloseResult();
			m_closeResult.setType(CloseResultType.Buffer);
			m_closeResult.setBuffer(buf);
			
		}
		else {
			//content had to be written to a temp file
			String fileName = NotesStringUtils.fromLMBCS(returnFileMem, -1);
			m_closeResult = new CloseResult();
			m_closeResult.setType(CloseResultType.File);
			m_closeResult.setFilePath(fileName);
		}
		
		return m_closeResult;
		
	}
	
	public enum CloseResultType {Buffer, File}
	
	public static class CloseResult {
		private CloseResultType m_type;
		private JNACompoundTextStandaloneBuffer m_buffer;
		private String m_filePath;
		
		public CloseResultType getType() {
			return m_type;
		}
		
		private void setType(CloseResultType type) {
			this.m_type = type;
		}
		
		public JNACompoundTextStandaloneBuffer getBuffer() {
			return m_buffer;
		}
		
		private void setBuffer(JNACompoundTextStandaloneBuffer buf) {
			m_buffer = buf;
		}
		
		public String getFilePath() {
			return m_filePath;
		}

		private void setFilePath(String fileName) {
			this.m_filePath = fileName;
		}
	}
	
}
