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
package com.hcl.domino.jna.internal.richtext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoClient;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.gc.allocations.JNACompoundTextStandaloneBufferAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Pointer;

/**
 * Container to store and track the in-memory buffer that Domino creates when a
 * compound text is closed and its content is small enough to fit into a memory segment.
 * 
 * @author Karsten Lehmann
 */
public class JNACompoundTextStandaloneBuffer extends BaseJNAAPIObject<JNACompoundTextStandaloneBufferAllocations> {
	private int m_size;
	
	public JNACompoundTextStandaloneBuffer(JNADominoClient client, DHANDLE handle, int size) {
		super(client);
		m_size = size;
		
		getAllocations().setCompoundTextHandle(handle);
		setInitialized();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNACompoundTextStandaloneBufferAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

		return new JNACompoundTextStandaloneBufferAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	public static class FileInfo {
		private String m_filePath;
		private long m_fileSize;
		private InputStream m_fileIn;
		
		public FileInfo(String filePath, long fileSize, InputStream fileIn) {
			m_filePath = filePath;
			m_fileSize = fileSize;
			m_fileIn = fileIn;
		}
		
		public String getFilePath() {
			return m_filePath;
		}
		
		public long getFileSize() {
			return m_fileSize;
		}
		
		public InputStream getStream() {
			return m_fileIn;
		}
	}
	
	/**
	 * Writes the memory buffer content to a temporary file and returns a {@link InputStream}
	 * to read the data. The stream will be auto-closed and the file deleted when this buffer is freed.
	 * 
	 * @return stream to temporary file on disk
	 * @throws IOException  in case of I/O errors
	 */
	public FileInfo asFileOnDisk() throws IOException {
		checkDisposed();
		
		Path file;
		try {
			file = AccessController.doPrivileged((PrivilegedExceptionAction<Path>) () -> {
				Path tmpFile = Files.createTempFile("jnx_comptext", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$

				LockUtil.lockHandle(getAllocations().getCompoundTextHandle(), (handleByVal) -> {
					Pointer ptr = Mem.OSLockObject(handleByVal);

					OutputStream fOut = null;
					try {
						byte[] bufferData = ptr.getByteArray(0, m_size);
						fOut = Files.newOutputStream(tmpFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
						fOut.write(bufferData, 0, bufferData.length);
						fOut.flush();
					} catch (IOException e1) {
						throw new DominoException("Error writing to temporary file: "+tmpFile, e1);
					}
					finally {
						Mem.OSUnlockObject(handleByVal);

						if (fOut!=null) {
							try {
								fOut.close();
							} catch (IOException e2) {
								e2.printStackTrace();
							}
						}
					}
					
					return 0;
				});

				return tmpFile;
			});
		} catch (PrivilegedActionException e) {
			if (e.getCause() instanceof DominoException) {
				throw (DominoException) e.getCause();
			} else {
				throw new DominoException("Could not write content of memory buffer to disk", e);
			}
		}
	
		
		InputStream in = Files.newInputStream(file);
		//keep track of stream to auto-close when memory is freed
		getAllocations().addCreatedTempFileStream(file,in);
		
		return new FileInfo(file.toString(), Files.size(file), in);
	}
	
	public int getSize() {
		return m_size;
	}
		
	@Override
	public String toStringLocal() {
		return MessageFormat.format("JNACompoundTextStandaloneBuffer [handle={0}, size={1}]", getAllocations().getCompoundTextHandle(), getSize()); //$NON-NLS-1$
	}
}