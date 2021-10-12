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
package com.hcl.domino.jna.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.errors.INotesErrorConstants;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Attachment;
import com.hcl.domino.data.Attachment.IDataCallback;
import com.hcl.domino.data.Attachment.IDataCallback.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.callbacks.NotesCallbacks;
import com.hcl.domino.jna.internal.callbacks.Win32NotesCallbacks;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADatabaseAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesBlockIdStruct;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Pointer;

/**
 * Data container to access metadata and binary data of a note attachment
 * 
 * @author Karsten Lehmann
 */
public class JNAAttachment implements Attachment {
	private String m_fileName;
	private Compression m_compression;
	private short m_fileFlags;
	private long m_fileSize;
	private DominoDateTime m_fileCreated;
	private DominoDateTime m_fileModified;
	private JNADocument m_parentDoc;
	private NotesBlockIdStruct m_itemBlockId;
	private int m_rrv;
	
	JNAAttachment(String fileName, Compression compression, short fileFlags, long fileSize,
			DominoDateTime fileCreated, DominoDateTime fileModified, JNADocument parentNote,
			NotesBlockIdStruct itemBlockId, int rrv) {
		m_fileName = StringUtil.toString(fileName);
		m_compression = Objects.requireNonNull(compression, "compression cannot be null");
		m_fileFlags = fileFlags;
		m_fileSize = fileSize;
		m_fileCreated = Objects.requireNonNull(fileCreated, "fileCreated cannot be null");
		m_fileModified = Objects.requireNonNull(fileModified, "fileModified cannot be null");
		m_parentDoc = Objects.requireNonNull(parentNote, "parentNode cannot be null");
		m_itemBlockId = Objects.requireNonNull(itemBlockId, "itemBlockId cannot be null");
		m_rrv = rrv;
	}

	/**
	 * Returns the RRV ID that identifies the object in the database
	 * 
	 * @return RRV
	 */
	public int getRRV() {
		return m_rrv;
	}
	
	@Override
	public String getFileName() {
		return m_fileName;
	}
	
	@Override
	public Compression getCompression() {
		return m_compression;
	}
	
	/**
	 * Returns file flags, e.g. {@link NotesConstants#FILEFLAG_SIGN}
	 * 
	 * @return flags
	 */
	public short getFileFlags() {
		return m_fileFlags;
	}
	
	@Override
	public long getFileSize() {
		return m_fileSize;
	}

	@Override
	public DominoDateTime getFileCreated() {
		return m_fileCreated;
	}

	@Override
	public DominoDateTime getFileModified() {
		return m_fileModified;
	}

	@Override
	public void readData(IDataCallback callback, int offset) {
		readData(callback, offset, 1000000);
	}

	private void readData(IDataCallback callback, int offset, int bufferSize) {
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();

		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) m_parentDoc.getParent().getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		if (getCompression() != Compression.NONE) {
			throw new UnsupportedOperationException("This operation is only supported on attachments without compression.");
		}
		if (bufferSize<=0) {
			throw new IllegalArgumentException("Buffer size must be a positive number");
		}
		
		AtomicLong currOffset = new AtomicLong(offset);
		AtomicBoolean aborted = new AtomicBoolean();
		
		while (!aborted.get()) {
			long bytesToRead;
			if ((currOffset.get()+bufferSize) < m_fileSize) {
				bytesToRead = bufferSize;
			}
			else {
				bytesToRead = m_fileSize - currOffset.get();
			}
			if (bytesToRead<=0) {
				//we're done
				break;
			}
			
			DHANDLE.ByReference rethBuffer = DHANDLE.newInstanceByReference();
			
			short result = LockUtil.lockHandle(dbAllocations.getDBHandle(), (dbHandleByVal) -> {
				return NotesCAPI.get().NSFDbReadObject(dbHandleByVal, m_rrv, (int) (currOffset.get() & 0xffffffff), 
						(int) (bytesToRead & 0xffffffff), rethBuffer);
			});
			NotesErrorUtils.checkResult(result);
			
			LockUtil.lockHandle(rethBuffer, (hBufferByVal) -> {
				Pointer ptr = Mem.OSLockObject(hBufferByVal);
				try {
					byte[] buffer = ptr.getByteArray(0, (int) bytesToRead);
					IDataCallback.Action action = callback.read(buffer);
					if (action==IDataCallback.Action.Stop) {
						aborted.set(true);
					}
					return 0;
				}
				finally {
					Mem.OSUnlockObject(hBufferByVal);
					Mem.OSMemFree(hBufferByVal);
				}
			});
			
			currOffset.addAndGet(bytesToRead);
		}
	}
	
	@Override
	public void readData(final IDataCallback callback) {
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();

		JNADatabaseAllocations dbAllocations = (JNADatabaseAllocations) m_parentDoc.getParent().getAdapter(APIObjectAllocations.class);
		dbAllocations.checkDisposed();
		
		final NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;
		
		final int extractFlags = 0;
		final int hDecryptionCipher = 0;
		
		final NotesCallbacks.NoteExtractCallback extractCallback;
		final Throwable[] extractError = new Throwable[1];
		
		if (PlatformUtils.isWin32()) {
			extractCallback = (Win32NotesCallbacks.NoteExtractCallbackWin32) (data, length, param) -> {
				if (length==0) {
					return 0;
				}
				
				try {
					byte[] dataArr = data.getByteArray(0, length);
					IDataCallback.Action action = callback.read(dataArr);
					if (action==IDataCallback.Action.Continue) {
						return 0;
					}
					else {
						return INotesErrorConstants.ERR_NSF_INTERRUPT;
					}
				}
				catch (Throwable t) {
					extractError[0] = t;
					return INotesErrorConstants.ERR_NSF_INTERRUPT;
				}
			};
		}
		else {
			extractCallback = (data, length, param) -> {
				if (length==0) {
					return 0;
				}
				
				try {
					byte[] dataArr = data.getByteArray(0, length);
					IDataCallback.Action action = callback.read(dataArr);
					if (action==IDataCallback.Action.Continue) {
						return 0;
					}
					else {
						return INotesErrorConstants.ERR_NSF_INTERRUPT;
					}
				}
				catch (Throwable t) {
					extractError[0] = t;
					return INotesErrorConstants.ERR_NSF_INTERRUPT;
				}
			};
		}
		
		short result;
		try {
			result = AccessController.doPrivileged((PrivilegedExceptionAction<Short>) () -> LockUtil.lockHandle(docAllocations.getNoteHandle(), (noteHandleByVal) -> {
				return NotesCAPI.get().NSFNoteCipherExtractWithCallback(noteHandleByVal,
						itemBlockIdByVal, extractFlags, hDecryptionCipher,
						extractCallback, null, 0, null);
			}));
		} catch (PrivilegedActionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new DominoException("Error extracting attachment", e);
			}
		}
		
		if (extractError[0] != null) {
			throw new DominoException("Extraction interrupted", extractError[0]);
		}
		
		if (result != INotesErrorConstants.ERR_NSF_INTERRUPT) {
			NotesErrorUtils.checkResult(result);
		}
	}
	
	@Override
	public void deleteFromDocument() {
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) ((JNADocument)getParent()).getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();
		
		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		short result = LockUtil.lockHandle(docAllocations.getNoteHandle(), (handleByVal) -> {
			return NotesCAPI.get().NSFNoteDetachFile(handleByVal, itemBlockIdByVal);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public Document getParent() {
		return m_parentDoc;
	}

	@Override
	public void extract(Path targetFilePath) throws IOException {
		Files.deleteIfExists(targetFilePath);
		
		IOException[] ex = new IOException[1];
		
		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(targetFilePath, StandardOpenOption.CREATE_NEW))) {
			readData((data) -> {
				try {
					out.write(data);
					return Action.Continue;
				} catch (Exception e) {
					ex[0] = new IOException(MessageFormat.format("Error writing attachment {0} to {1}", getFileName(), targetFilePath), e);
					return Action.Stop;
				}
			});
			
			if (ex[0] != null) {
				throw ex[0]; 
			}
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		Path tmpFile = Files.createTempFile("jnxtmp_", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		extract(tmpFile);
		return new BufferedInputStream(new TempFileInputStream(tmpFile));
	}
	
	/**
	 * InputStream of a temporary file that automatically delete the
	 * file when the data is read.
	 */
	private class TempFileInputStream extends InputStream {
		private Path tmpFile;
	    private InputStream inputStream;
	    
	    public TempFileInputStream(Path tmpFile) throws IOException {
	    	this.tmpFile = tmpFile;
	        this.inputStream = Files.newInputStream(tmpFile);
	    }

	    @Override
	    public int available() throws IOException {
	    	return this.inputStream.available();
	    }
	    
	    @Override
	    public synchronized void mark(int readlimit) {
	    	this.inputStream.mark(readlimit);
	    }
	    
	    @Override
	    public boolean markSupported() {
	    	return this.inputStream.markSupported();
	    }
	    
	    @Override
	    public int read(byte[] b) throws IOException {
	    	return this.inputStream.read(b);
	    }
	    
	    @Override
	    public int read(byte[] b, int off, int len) throws IOException {
	    	return this.inputStream.read(b, off, len);
	    }
	    
	    @Override
	    public synchronized void reset() throws IOException {
	    	this.inputStream.reset();
	    }
	    
	    @Override
	    public long skip(long n) throws IOException {
	    	return this.inputStream.skip(n);
	    }
	    
	    @Override
	    public void close() throws IOException {
	    	this.inputStream.close();
	    	Files.deleteIfExists(this.tmpFile);
	    }
	    
	    @Override
	    protected void finalize() throws Throwable {
	    	this.inputStream.close();
	    	Files.deleteIfExists(this.tmpFile);
	    }
	    
	    @Override
	    public int read() throws IOException {
	    	return this.inputStream.read();
	    }
	}
}
