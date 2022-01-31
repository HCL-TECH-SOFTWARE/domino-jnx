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
import java.io.UncheckedIOException;
import java.lang.ref.ReferenceQueue;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.jna.internal.richtext.JNACDFileRichTextNavigator;

public class JNACDFileRichTextNavigatorAllocations extends APIObjectAllocations<JNACDFileRichTextNavigator> {
	private boolean m_disposed;
	private String m_filePath;
	private InputStream m_fileIn;
	private SeekableByteChannel m_fileChannel;
	private boolean m_autoDelete;
	private long m_fileSize;
	
	@SuppressWarnings("rawtypes")
	public JNACDFileRichTextNavigatorAllocations(IGCDominoClient parentDominoClient,
			APIObjectAllocations parentAllocations, JNACDFileRichTextNavigator referent,
			ReferenceQueue<? super IAPIObject> queue) {
		
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

		if (m_fileChannel!=null) {
			try {
				m_fileChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_fileChannel = null;
		}

		if (m_fileIn!=null) {
			try {
				m_fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m_fileIn = null;
		}
		
		if (m_autoDelete) {
			try {
				Files.delete(Paths.get(m_filePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		m_disposed = true;
	}

	public void init(String filePath, InputStream fIn, long fileSize, boolean autoDelete) {
		m_filePath = filePath;
		m_fileIn = fIn;
		m_fileSize = fileSize;
		m_autoDelete = autoDelete;
	}
	
	public synchronized SeekableByteChannel getFileChannel() {
		checkDisposed();
		if(m_fileChannel == null) {
			try {
				m_fileChannel = Files.newByteChannel(Paths.get(m_filePath));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return m_fileChannel;
	}

	public long getFileSize() {
		return m_fileSize;
	}
	
	public String getFilePath() {
		return m_filePath;
	}
}
