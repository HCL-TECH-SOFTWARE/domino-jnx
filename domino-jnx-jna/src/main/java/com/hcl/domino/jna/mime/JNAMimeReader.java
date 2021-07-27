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
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
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
	public void readMIME(Document doc, String itemName, Set<ReadMimeDataType> dataType, OutputStream targetOut) throws IOException {
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
						byte[] data = pchData.getByteArray(0, len);
						targetOut.write(data);
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
				
				try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile.toFile()))) {
					readMIME(jnaDoc, itemName, dataType, out);
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
	public InputStream readMIMEAsStream(Document doc, String itemName, Set<ReadMimeDataType> dataType) {
		MIMEInputStream in = new MIMEInputStream(doc, itemName, dataType, 2000);
		getAllocations().registerStream(in);
		return in;
	}

	/**
	 * {@link Reader} adapter for the MIME stream
	 * 
	 * @author Karsten Lehmann
	 */
	private class MIMEInputStream extends InputStream {
		private Document m_doc;
		private String m_itemName;
		private Set<ReadMimeDataType> m_dataType;
		private Pointer m_mimeStreamPtr;
		private boolean m_closed;

		private byte[] m_buffer;
		private int m_bufferPos;
		private int m_leftInBuffer;

		
		public MIMEInputStream(Document doc, String itemName, Set<ReadMimeDataType> dataType,
				int bufSize) {
			m_doc = doc;
			m_itemName = itemName;
			m_dataType = dataType;
			if (bufSize<=0) {
				throw new IllegalArgumentException(MessageFormat.format("Buffer size must be greater than 0: {0}", bufSize));
			}
			m_buffer = new byte[bufSize];
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
		
		@Override
		public int read(byte[] b) throws IOException {
			 return read(b, 0, b.length);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (b == null) {
	            throw new NullPointerException();
	        } else if (off < 0 || len < 0 || len > b.length - off) {
	            throw new IndexOutOfBoundsException();
	        } else if (len == 0) {
	            return 0;
	        }

	        int c = read();
	        if (c == -1) {
	            return -1;
	        }
	        b[off] = (byte)c;

	        int i = 1;
	        try {
	            for (; i < len ; i++) {
	                c = read();
	                if (c == -1) {
	                    break;
	                }
	                b[off + i] = (byte)c;
	            }
	        } catch (IOException ee) {
	        }
	        return i;
		}
		
		@Override
		public int read() throws IOException {
			if (m_leftInBuffer == 0) {
				//end reached, read more data
				int read = readInto(m_buffer);
				if (read==-1 || read==0) {
					return -1;
				}
				m_leftInBuffer = read;
				m_bufferPos = 0;
			}
			
			byte b = m_buffer[m_bufferPos++];
			m_leftInBuffer--;
			return (int) (b & 0xff);
		}

		/**
		 * This function copies the MIME stream content into a {@link Writer}.
		 * 
		 * @param buffer buffer to receive the MIME stream data
		 * @param maxBufferSize max characters to read from the stream into the appendable
		 * @return number of bytes read or -1 for EOF
		 * @throws IOException in case of MIME stream I/O errors
		 */
		public int readInto(byte[] buffer) throws IOException {
			checkClosed();
			init();

			int maxBufferSize = buffer.length;
			DisposableMemory pchData = new DisposableMemory(maxBufferSize);
			try {
				IntByReference puiDataLen = new IntByReference();
				puiDataLen.setValue(0);
				
				int resultAsInt = NotesCAPI.get().MIMEStreamRead(pchData,
						puiDataLen, m_buffer.length, m_mimeStreamPtr);
				
				if (resultAsInt == NotesConstants.MIME_STREAM_IO) {
					throw new DominoException("I/O error reading MIMEStream");
				}

				int len = puiDataLen.getValue();
				if (len > 0) {
					pchData.read(0, buffer, 0, len);
					return len;
				}
				else {
					return -1;
				}
			}
			finally {
				pchData.dispose();
			}
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
			
			getAllocations().unregisterStream(this);
		}
		
	}
}
