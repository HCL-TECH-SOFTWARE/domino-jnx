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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.Set;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.MimePartNotFoundException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.mime.MimeReader;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import jakarta.mail.internet.MimeMessage;

public class JNAMimeReader extends JNAMimeBase implements MimeReader {
	
	public JNAMimeReader(IAPIObject<?> parent) {
		super(parent);
	}
	
	private int toOpenFlagsAsInt(Set<ReadMimeDataType> dataType) {
		int dwOpenFlags = NotesConstants.MIME_STREAM_OPEN_READ;

		if (dataType.contains(ReadMimeDataType.MIMEHEADERS)) {
			dwOpenFlags |= NotesConstants.MIME_STREAM_MIME_INCLUDE_HEADERS;
		}

		if (dataType.contains(ReadMimeDataType.RFC822HEADERS)) {
			dwOpenFlags |= NotesConstants.MIME_STREAM_RFC2822_INCLUDE_HEADERS;
		}
		return dwOpenFlags;
	}

	@Override
	public void readMIME(Document doc, String itemName, Set<ReadMimeDataType> dataType, Writer targetWriter) throws IOException {
		if (!(doc instanceof JNADocument)) {
			throw new IncompatibleImplementationException(doc, JNADocument.class);
		}
		JNADocument jnaDoc = (JNADocument) doc;

		if (jnaDoc.isDisposed()) {
			throw new ObjectDisposedException(jnaDoc);
		}

		if ("$file".equalsIgnoreCase(itemName)) { //$NON-NLS-1$
			throw new IllegalArgumentException(MessageFormat.format("Invalid item name: {0}", itemName));
		}

		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, false);
		
		int dwOpenFlags = toOpenFlagsAsInt(dataType);

		Pointer mimeStreamPtr = createMimeStream(jnaDoc, itemNameMem, dwOpenFlags);
		try {
			int MAX_BUFFER_SIZE = 60000;
			DisposableMemory pchData = new DisposableMemory(MAX_BUFFER_SIZE);
			try {
				IntByReference puiDataLen = new IntByReference();

				while (true) {
					int resultAsInt = NotesCAPI.get().MIMEStreamRead(pchData,
							puiDataLen, MAX_BUFFER_SIZE, mimeStreamPtr);
					
					if (resultAsInt == NotesConstants.MIME_STREAM_IO) {
						throw new DominoException("I/O error reading MIMEStream");
					}
					
					int len = puiDataLen.getValue();
					if (len > 0) {
						String txt = NotesStringUtils.fromLMBCS(pchData, len);
						targetWriter.write(txt);
					}
					else {
						break;
					}
					
					if (resultAsInt == NotesConstants.MIME_STREAM_EOS) {
						break;
					}
				}
			}
			finally {
				pchData.dispose();
			}
		}
		finally {
			disposeMimeStream(mimeStreamPtr);
		}
	}

	@Override
	public MimeMessage readMIME(Document doc, String itemName, Set<ReadMimeDataType> dataType) {
		if (!(doc instanceof JNADocument)) {
			throw new IncompatibleImplementationException(doc, JNADocument.class);
		}
		JNADocument jnaDoc = (JNADocument) doc;

		if (jnaDoc.isDisposed()) {
			throw new ObjectDisposedException(jnaDoc);
		}

		if ("$file".equalsIgnoreCase(itemName)) { //$NON-NLS-1$
			throw new IllegalArgumentException(MessageFormat.format("Invalid item name: {0}", itemName));
		}

		final Exception[] ex = new Exception[1];
		
		MimeMessage msg = AccessController.doPrivileged((PrivilegedAction<MimeMessage>) () -> {
			
			Path tmpFile = null;
			try {
				//use a temp file to not store the MIME content twice in memory (raw + parsed)
				tmpFile = Files.createTempFile("dominojna_mime_", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
				
				try (BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					readMIME(jnaDoc, itemName, dataType, writer);
				}
				
				try (InputStream fIn = Files.newInputStream(tmpFile);
						BufferedInputStream bufIn = new BufferedInputStream(fIn)) {
					
					Properties props = System.getProperties(); 
					jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, null);
					MimeMessage message = new MimeMessage(mailSession, bufIn);
					return message;
				}
			}
			catch (Exception e1) {
				ex[0] = e1;
				return null;
			}
			finally {
				if(tmpFile != null) {
					try {
						Files.deleteIfExists(tmpFile);
					} catch (IOException e2) {
						ex[0] = e2;
					}
				}
			}
		});
		
		if (ex[0] != null) {
			int errId = 0;
			if(ex[0] instanceof MimePartNotFoundException) {
				throw (MimePartNotFoundException)ex[0];
			} else if (ex[0] instanceof DominoException) {
				errId = ((DominoException)ex[0]).getId();
			}
			
			Database parentDb = doc.getParentDatabase();
			throw new DominoException(errId, MessageFormat.format(
					"Error parsing the MIME content of document with UNID {0} and item name {1} in database {2}!!{3}",
					doc.getUNID(), itemName, parentDb.getServer(), parentDb.getRelativeFilePath()), ex[0]);
		}
		return msg;
	}
	
	@Override
	public Reader getMIMEReader(Document doc, String itemName, Set<ReadMimeDataType> dataType) {
		MIMEReader reader = new MIMEReader(doc, itemName, dataType, 2000);
		getAllocations().registerReader(reader);
		return reader;
	}

	/**
	 * {@link Reader} adapter for the MIME stream
	 * 
	 * @author Karsten Lehmann
	 */
	private class MIMEReader extends Reader {
		private Document m_doc;
		private String m_itemName;
		private Set<ReadMimeDataType> m_dataType;
		private int m_maxBufferSize;
		private Pointer m_mimeStreamPtr;
		private StringBuilder m_buffer;
		private boolean m_closed;
		
		public MIMEReader(Document doc, String itemName, Set<ReadMimeDataType> dataType,
				int maxBufferSize) {
			m_doc = doc;
			m_itemName = itemName;
			m_dataType = dataType;
			if (maxBufferSize<=0) {
				throw new IllegalArgumentException(MessageFormat.format("Max buffer size must be greater than 0: {0}", maxBufferSize));
			}
			m_maxBufferSize = maxBufferSize;
			m_buffer = new StringBuilder();
		}
		
		private void init() {
			if (m_mimeStreamPtr!=null) {
				return;
			}
			
			if (!(m_doc instanceof JNADocument)) {
				throw new IncompatibleImplementationException(m_doc, JNADocument.class);
			}
			JNADocument jnaDoc = (JNADocument) m_doc;

			if (jnaDoc.isDisposed()) {
				throw new ObjectDisposedException(jnaDoc);
			}

			if ("$file".equalsIgnoreCase(m_itemName)) { //$NON-NLS-1$
				throw new IllegalArgumentException(MessageFormat.format("Invalid item name: {0}", m_itemName));
			}

			Memory itemNameMem = NotesStringUtils.toLMBCS(m_itemName, false);
			
			int dwOpenFlags = toOpenFlagsAsInt(m_dataType);

			m_mimeStreamPtr = createMimeStream(jnaDoc, itemNameMem, dwOpenFlags);
		}
		
		public synchronized int readFromBuffer() throws IOException {
			checkClosed();
			init();
			
			if (m_buffer.length() == 0) {
				DisposableMemory pchData = new DisposableMemory(m_maxBufferSize);
				try {
					IntByReference puiDataLen = new IntByReference();
					
					int resultAsInt = NotesCAPI.get().MIMEStreamRead(pchData,
							puiDataLen, m_maxBufferSize, m_mimeStreamPtr);
					
					if (resultAsInt == NotesConstants.MIME_STREAM_IO) {
						throw new DominoException("I/O error reading MIMEStream");
					}
					
					int len = puiDataLen.getValue();
					if (len > 0) {
						String txt = NotesStringUtils.fromLMBCS(pchData, len);
						m_buffer.append(txt);
					}
					else {
						return -1;
					}
				}
				finally {
					pchData.dispose();
				}
			}
			
			char c = m_buffer.charAt(0);
			m_buffer.deleteCharAt(0);
			return c;
		}
		
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			for (int i=0; i<len; i++) {
				int c = readFromBuffer();
				
				if (c==-1) {
					if (i==0) {
						return -1;
					}
					else {
						return i;
					}
				}
				else {
					cbuf[off + i] = (char) c;
				}
			}
			
			return len;
		}

		private void checkClosed() {
			if (m_closed) {
				throw new IllegalStateException("Reader is already closed");
			}
		}
		
		@Override
		public synchronized void close() throws IOException {
			//reader is automatically closed when the DominoClient gets closed
			if (m_closed || m_mimeStreamPtr==null) {
				return;
			}
			
			disposeMimeStream(m_mimeStreamPtr);
			m_mimeStreamPtr = null;
			
			getAllocations().unregisterReader(this);
		}
		
	}
}
