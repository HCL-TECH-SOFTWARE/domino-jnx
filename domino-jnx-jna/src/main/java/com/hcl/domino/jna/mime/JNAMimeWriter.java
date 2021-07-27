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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Set;
import java.util.function.Consumer;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.commons.util.StringUtil;
import com.hcl.domino.data.Document;
import com.hcl.domino.exception.IncompatibleImplementationException;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNARichtextMimeConversionSettingsAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.mime.MimeWriter;
import com.hcl.domino.mime.RichTextMimeConversionSettings;
import com.hcl.domino.misc.NotesConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

public class JNAMimeWriter extends JNAMimeBase implements MimeWriter {
	
	public JNAMimeWriter(IAPIObject<?> parent) {
		super(parent);
	}
	
	/**
	 * Writes the data of a {@link Message} to the MIMEStream in small chunks.
	 * 
	 * @param mimeStreamPtr pointer to MIMEStream
	 * @param mimeMessage message to write
	 * @throws IOException in case of I/O errors
	 * @throws MessagingException in case of errors accessing the {@link Message}
	 */
	private void writeMimeToStream(Pointer mimeStreamPtr, Message mimeMessage) throws IOException, MessagingException {
		//size of in-memory buffer to transfer MIME data from Message object to Domino MIME stream
		final int BUFFERSIZE = 16384;
		
		final DisposableMemory buf = new DisposableMemory(BUFFERSIZE);
		
		OutputStream os = new OutputStream() {
			int bytesInBuffer = 0;

			@Override
			public void write(int b) throws IOException {
				buf.setByte(bytesInBuffer, (byte) (b & 0xff));
				bytesInBuffer++;
				if (bytesInBuffer == buf.size()) {
					flushBuffer();
				}
			}

			@Override
			public void close() throws IOException {
				flushBuffer();
			}

			private void flushBuffer() throws IOException {
				if (bytesInBuffer > 0) {
					int resultAsInt = NotesCAPI.get().MIMEStreamWrite(buf, bytesInBuffer, mimeStreamPtr);

					if (resultAsInt == NotesConstants.MIME_STREAM_IO) {
						throw new IOException("I/O error received during MIME stream operation");
					}

					bytesInBuffer = 0;
				}
			}
		};
		try {
			mimeMessage.writeTo(os);
		} finally {
			os.close();
		}
	}

	/**
	 * Generic method to write data to a MIMEStream.
	 * 
	 * @param doc target document
	 * @param itemName item name for MIME content
	 * @param streamConsumer consumer that writes data into the MIMEStream
	 * @param dataType itemize flags (write header, body or both)
	 */
	private void writeMime(Document doc, String itemName, Consumer<Pointer> streamConsumer, Set<WriteMimeDataType> dataType) {
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

		if (dataType.isEmpty()) {
			throw new IllegalArgumentException("Either header or body should be exported");
		}

		Memory itemNameMem = NotesStringUtils.toLMBCS(itemName, false);
		
		int dwOpenFlags = NotesConstants.MIME_STREAM_OPEN_WRITE;
		
		if (dataType.contains(WriteMimeDataType.BODY) &&
				!dataType.contains(WriteMimeDataType.HEADERS)) {

			//write just the body
			JNADocument tmpDoc = (JNADocument) jnaDoc.getParentDatabase().createDocument();
			try {
				Pointer mimeStreamPtr = createMimeStream(tmpDoc, itemNameMem, dwOpenFlags);
				try {
					streamConsumer.accept(mimeStreamPtr);

					int dwItemizeFlags = NotesConstants.MIME_STREAM_ITEMIZE_FULL;
					
					JNADocumentAllocations tmpDocAllocations = (JNADocumentAllocations) tmpDoc.getAdapter(APIObjectAllocations.class);
					
					short wItemNameLen = itemNameMem == null ? 0 : (short)(itemNameMem.size() & 0xffff);
					LockUtil.lockHandle(tmpDocAllocations.getNoteHandle(), (docHdlByVal) -> {
						NotesCAPI.get().MIMEStreamItemize(docHdlByVal, itemNameMem,
								wItemNameLen, dwItemizeFlags, mimeStreamPtr);
						
						return null;
						
					});
				}
				finally {
					disposeMimeStream(mimeStreamPtr);
				}
				
				//remove old items from target note
				String iname = StringUtil.isEmpty(itemName) ? "Body" : itemName; //$NON-NLS-1$
				while (doc.hasItem(iname)) {
					doc.removeItem(iname);
				}
				
				//copy created MIME items from temp note to target note
				tmpDoc.forEachItem(iname, (item, loop) -> {
					item.copyToDocument(doc, false);
				});
				
				//copy part data that exceeded 64k
				tmpDoc.forEachItem("$file", (item, loop) -> { //$NON-NLS-1$
					item.copyToDocument(doc, false);
				});
			}
			finally {
				tmpDoc.dispose();
			}
		}
		else {
			Pointer mimeStreamPtr = createMimeStream(jnaDoc, itemNameMem, dwOpenFlags);
			try {
				streamConsumer.accept(mimeStreamPtr);
				
				JNADocumentAllocations jnaDocAllocations = (JNADocumentAllocations) jnaDoc.getAdapter(APIObjectAllocations.class);
				
				LockUtil.lockHandle(jnaDocAllocations.getNoteHandle(), (docHdlByVal) -> {
					int dwItemizeFlags = 0;
					if (dataType.contains(WriteMimeDataType.HEADERS)) {
						dwItemizeFlags |= NotesConstants.MIME_STREAM_ITEMIZE_HEADERS;
					}
					if (dataType.contains(WriteMimeDataType.BODY)) {
						dwItemizeFlags |= NotesConstants.MIME_STREAM_ITEMIZE_BODY;
					}
					if (dataType.contains(WriteMimeDataType.NO_DELETE_ATTACHMENTS)) {
						dwItemizeFlags |= NotesConstants.MIME_STREAM_NO_DELETE_ATTACHMENTS;
					}
					
					short wItemNameLen = itemNameMem == null ? 0 : (short)(itemNameMem.size() & 0xffff);
					NotesCAPI.get().MIMEStreamItemize(docHdlByVal, itemNameMem,
							wItemNameLen, dwItemizeFlags, mimeStreamPtr);
					
					return null;
				});
			}
			finally {
				disposeMimeStream(mimeStreamPtr);
			}
		}
	}

	@Override
	public void writeMime(Document doc, String itemName, Message mimeMessage, Set<WriteMimeDataType> dataType) throws IOException, MessagingException {
		IOException[] ioEx = new IOException[1];
		MessagingException[] msgEx = new MessagingException[1];
		
		writeMime(doc, itemName, (mimeStreamPtr) -> {
			try {
				writeMimeToStream(mimeStreamPtr, mimeMessage);
			} catch (IOException e) {
				ioEx[0] = e;
				return;
			} catch (MessagingException e) {
				msgEx[0] = e;
			}
		}, dataType);
		
		if (ioEx[0] != null) {
			throw ioEx[0];
		}
		if (msgEx[0] != null) {
			throw msgEx[0];
		}
	}

	@Override
	public void writeMime(Document doc, String itemName, InputStream in, Set<WriteMimeDataType> dataType) throws IOException {
		IOException[] ioEx = new IOException[1];
		
		writeMime(doc, itemName, (mimeStreamPtr) -> {

			byte[] buffer = new byte[60000];
			int len;

			DisposableMemory bufferMem = new DisposableMemory(buffer.length);
			try {
				while ((len=in.read(buffer))>0) {
					bufferMem.write(0, buffer,0, len);

					int resultAsInt = NotesCAPI.get().MIMEStreamWrite(bufferMem, len, mimeStreamPtr);

					if (resultAsInt == NotesConstants.MIME_STREAM_IO) {
						throw new IOException("I/O error received during MIME stream operation");
					}
				}
			}
			catch (IOException e) {
				ioEx[0] = e;
				return;
			}
			finally {
				bufferMem.dispose();
			}
		}, dataType);

		if (ioEx[0] != null) {
			throw ioEx[0];
		}
	}

	@Override
	public RichTextMimeConversionSettings createRichTextMimeConversionSettings() {
		return new JNARichtextMimeConversionSettings(getParentDominoClient());
	}
	
	@Override
	public void convertToMime(Document doc, RichTextMimeConversionSettings convertSettings) {
		if (!(doc instanceof JNADocument)) {
			throw new IllegalArgumentException("The document object is not of type JNADocument");
		}
		JNADocument jnaDoc = (JNADocument) doc;
		if (jnaDoc.isDisposed()) {
			throw new ObjectDisposedException(jnaDoc);
		}

		JNADocumentAllocations allocations = (JNADocumentAllocations) jnaDoc.getAdapter(APIObjectAllocations.class);

		LockUtil.lockHandle(allocations.getNoteHandle(), (noteHandleByVal) -> {
			short noteFlags;
			
			DisposableMemory retFlags = new DisposableMemory(2);
			try {
				retFlags.clear();

				NotesCAPI.get().NSFNoteGetInfo(noteHandleByVal, NotesConstants._NOTE_FLAGS, retFlags);
				noteFlags = retFlags.getShort(0);
			}
			finally {
				retFlags.dispose();
			}
			
			boolean isCanonical = (noteFlags & NotesConstants.NOTE_FLAG_CANONICAL) == NotesConstants.NOTE_FLAG_CANONICAL;
			boolean isMime = doc.hasMIMEPart();

			if (convertSettings!=null) {
				if (!(convertSettings instanceof JNARichtextMimeConversionSettings)) {
					throw new IllegalArgumentException("The settings object is not of type JNARichtextMimeConversionSettings");
				}
				
				JNARichtextMimeConversionSettings jnaConvertSettings = (JNARichtextMimeConversionSettings) convertSettings;
				JNARichtextMimeConversionSettingsAllocations jnaConvertSettingsAllocations = (JNARichtextMimeConversionSettingsAllocations) jnaConvertSettings.getAdapter(APIObjectAllocations.class);
				
				jnaConvertSettingsAllocations.lockAndGetSettingsPointer((settingsPtr) -> {
					short result = NotesCAPI.get().MIMEConvertCDParts(noteHandleByVal, isCanonical, isMime, settingsPtr);
					NotesErrorUtils.checkResult(result);
					return null;
				});
			}
			else {
				short result = NotesCAPI.get().MIMEConvertCDParts(noteHandleByVal, isCanonical, isMime, null);
				NotesErrorUtils.checkResult(result);
			}
			
			return null;
		});
	}
	
}
