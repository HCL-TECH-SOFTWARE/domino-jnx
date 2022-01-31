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
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.richtext.RichTextUtil;
import com.hcl.domino.commons.richtext.RichtextNavigator;
import com.hcl.domino.data.Document;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.gc.allocations.JNACDFileRichTextNavigatorAllocations;
import com.hcl.domino.richtext.RichTextConstants;
import com.hcl.domino.richtext.RichTextWriter;
import com.hcl.domino.richtext.records.RichTextRecord;
import com.sun.jna.Memory;

/**
 * Implementation of {@link RichtextNavigator} that works with an on-disk CD record file
 * 
 * @author Karsten Lehmann
 */
public class JNACDFileRichTextNavigator extends BaseJNAAPIObject<JNACDFileRichTextNavigatorAllocations> implements RichtextNavigator {
	private long m_position;
	private RichTextRecord<?> m_currentCDRecord;
	private Map<Integer,Integer> m_cdRecordSizeAtIndex;
	private Map<Long,Integer> m_cdRecordIndexAtFilePos;
	private int m_currentCDRecordIndex;
	private long m_lastElementPosition = -1;
	private int m_lastElementIndex = -1;
	
	@SuppressWarnings("rawtypes")
  public JNACDFileRichTextNavigator(IGCDominoClient client, String filePath, InputStream cdFileStream,
			long fileSize,
			boolean autoDelete) throws IOException {
		super(client);
		
		m_cdRecordSizeAtIndex = new TreeMap<>();
		m_cdRecordIndexAtFilePos = new TreeMap<>();
		gotoFirst();
		
		getAllocations().init(filePath, cdFileStream, fileSize, autoDelete);
		setInitialized();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNACDFileRichTextNavigatorAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

		return new JNACDFileRichTextNavigatorAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public Document getParentDocument() {
		return null;
	}
	
	@Override
	public String getItemName() {
		return null;
	}

	/**
	 * Reads the CD record information at the current file position
	 * 
	 * @throws IOException
	 */
	private RichTextRecord<?> readCurrentCDRecordUnchecked() {
		try {
			return readCurrentCDRecord();
		} catch (IOException e) {
			throw new DominoException(MessageFormat.format("Error reading CD record at position {0} of file {1}", m_position, getAllocations().getFilePath()), e);
		}
	}
	
	/**
	 * Reads the CD record information at the current file position
	 * 
	 * @throws IOException
	 */
	private RichTextRecord<?> readCurrentCDRecord() throws IOException {
		checkDisposed();
		
		JNACDFileRichTextNavigatorAllocations allocations = getAllocations();
		SeekableByteChannel fileChannel = allocations.getFileChannel();
		
		fileChannel.position(m_position);
		
		Memory signatureMem = new Memory(2);
		ByteBuffer signatureBuf = signatureMem.getByteBuffer(0, signatureMem.size());
		fileChannel.read(signatureBuf);
		
		short signature = signatureMem.getShort(0);
		int dwLength;

		/* structures used to define and read the signatures 

			 0		   1
		+---------+---------+
		|   Sig   |  Length	|						Byte signature
		+---------+---------+

			 0		   1        2         3
		+---------+---------+---------+---------+
		|   Sig   |   ff    |		Length	   |		Word signature
		+---------+---------+---------+---------+

			 0		   1        2         3          4         5
		+---------+---------+---------+---------+---------+---------+
		|   Sig   |   00	    |                 Length		           | DWord signature
		+---------+---------+---------+---------+---------+---------+

		 */

		short highOrderByte = (short) (signature & 0xFF00);

		switch (highOrderByte) {
		case RichTextConstants.LONGRECORDLENGTH:      /* LSIG */
			Memory intLengthMem = new Memory(4);
			ByteBuffer intLengthBuf = intLengthMem.getByteBuffer(0, intLengthMem.size());
			fileChannel.read(intLengthBuf);
			dwLength = intLengthMem.getInt(0);

			break;

		case RichTextConstants.WORDRECORDLENGTH:      /* WSIG */
			Memory shortLengthMem = new Memory(2);
			ByteBuffer shortLengthBuf = shortLengthMem.getByteBuffer(0, shortLengthMem.size());
			fileChannel.read(shortLengthBuf);
			dwLength = shortLengthMem.getShort(0) & 0xffff;

			break;

		default:                    /* BSIG */
			dwLength = (signature >> 8) & 0x00ff;
			signature &= 0x00FF; /* Length not part of signature */
		}
		
		//file channel position points to the start of data, so reset it to the CD record start
		fileChannel.position(m_position);
		int cdRecordTotalLength = dwLength;
		DisposableMemory cdRecordMem = new DisposableMemory(cdRecordTotalLength);
		int bytesRead = fileChannel.read(cdRecordMem.getByteBuffer(0, cdRecordMem.size()));
		if (bytesRead != cdRecordTotalLength) {
			throw new IllegalStateException(
				MessageFormat.format(
					"Bytes read from CD record file for CD record at index {0} is expected to be {1} but we only could read {2} bytes",
					m_currentCDRecordIndex, cdRecordTotalLength, bytesRead
				)
			);
		}
		
		ByteBuffer data = cdRecordMem.getByteBuffer(0, cdRecordTotalLength).order(ByteOrder.nativeOrder());
		RichTextRecord<?> record = RichTextUtil.encapsulateRecord(signature, data);
		//remember the length of the CD records
		m_cdRecordSizeAtIndex.put(m_currentCDRecordIndex, record.getCDRecordLength());
		m_cdRecordIndexAtFilePos.put(m_position, m_currentCDRecordIndex);

		return record;
	}
	
	@Override
	public boolean isEmpty() {
		checkDisposed();
		
		return getAllocations().getFileSize()<=2;
	}

	@Override
	public boolean gotoFirst() {
		checkDisposed();
		
		if (isEmpty()) {
			return false;
		}
		
		if (m_position!=2 || m_currentCDRecord==null) {
			m_position = 2; // datatype TYPE_COMPOSITE (WORD)
			m_currentCDRecordIndex = 0;
			m_currentCDRecord = readCurrentCDRecordUnchecked();
		}
		return true;
	}

	@Override
	public boolean gotoLast() {
		checkDisposed();
		
		if (m_lastElementPosition!=-1 && m_lastElementIndex!=-1) {
			//we already know the exact position, because we have been there before
			m_position = m_lastElementPosition;
			m_currentCDRecordIndex = m_lastElementIndex;
			m_currentCDRecord = readCurrentCDRecordUnchecked();
			return true;
		}
		else {
			if (gotoFirst()) {
				RichTextRecord<?> lastReadRecord = null;
				long lastReadRecordPosition;
				int lastReadRecordIndex;
				
				do {
					lastReadRecord = readCurrentCDRecordUnchecked();
					lastReadRecordPosition = m_position;
					lastReadRecordIndex = m_currentCDRecordIndex;
				}
				while (gotoNext());
				
				m_position = lastReadRecordPosition;
				m_currentCDRecord = lastReadRecord;
				m_currentCDRecordIndex = lastReadRecordIndex;
				return true;
			}
			else {
				return false;
			}
		}
	}

	@Override
	public boolean gotoNext() {
		checkDisposed();
		
		int cdRecordLength = m_currentCDRecord.getCDRecordLength();
		long nextPosition = m_position + cdRecordLength;
		if ((nextPosition & 1L)==1) {
            nextPosition += 1;
        }
		
		JNACDFileRichTextNavigatorAllocations allocations = getAllocations();
		
		if (nextPosition>=allocations.getFileSize()) {
			return false;
		}
		try {
			allocations.getFileChannel().position(nextPosition);
		} catch (IOException e) {
			throw new DominoException(
				MessageFormat.format(
					"Error navigating to position {0} of file {1} with size {2}",
					nextPosition, allocations.getFilePath(), allocations.getFileSize()
				),
				e
			);
		}
		m_position = nextPosition;
		m_currentCDRecord = readCurrentCDRecordUnchecked();
		return true;
	}

	@Override
	public boolean gotoPrev() {
		checkDisposed();
		
		JNACDFileRichTextNavigatorAllocations allocations = getAllocations();
		SeekableByteChannel fileChannel = allocations.getFileChannel();
		
		if (m_currentCDRecordIndex==-1) {
			return false;
		}
		else if (m_currentCDRecordIndex>0) {
			m_currentCDRecordIndex--;
			long prevRecordLength = m_cdRecordSizeAtIndex.get(m_currentCDRecordIndex);
			long prevPosition = m_position - prevRecordLength;
			try {
				fileChannel.position(prevPosition);
			} catch (IOException e) {
				throw new DominoException(
					MessageFormat.format(
						"Error navigating to position {0} of file {1} with size {2}",
						prevPosition, allocations.getFilePath(), allocations.getFileSize()
					),
					e
				);
			}
			
			m_position = prevPosition;
			m_currentCDRecord = readCurrentCDRecordUnchecked();
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean hasNext() {
		checkDisposed();
		
		if (m_currentCDRecord==null) {
			return false;
		}
		return (m_position + m_currentCDRecord.getCDRecordLength()) < getAllocations().getFileSize();
	}

	@Override
	public boolean hasPrev() {
		checkDisposed();
		
		return m_currentCDRecordIndex>0;
	}

	@Override
	public RichTextRecord<?> getCurrentRecord() {
		checkDisposed();
		
		return m_currentCDRecord;
	}
	
	@Override
	public ByteBuffer getCurrentRecordData() {
		RichTextRecord<?> record = getCurrentRecord();
		if (record==null) {
			return null;
		}
		else {
			return record.getDataWithoutHeader();
		}
	}

	@Override
	public ByteBuffer getCurrentRecordDataWithHeader() {
		RichTextRecord<?> record = getCurrentRecord();
		if (record==null) {
			return null;
		}
		else {
			return record.getData();
		}
	}
	
	@Override
	public int getCurrentRecordHeaderLength() {
		RichTextRecord<?> record = getCurrentRecord();
		if (record==null) {
			return 0;
		}
		else {
			return record.getRecordHeaderLength();
		}
	}
	
	@Override
	public short getCurrentRecordTypeConstant() {
		RichTextRecord<?> record = getCurrentRecord();
		return record==null ? 0 : record.getTypeValue();
	}

	@Override
	public int getCurrentRecordDataLength() {
		RichTextRecord<?> record = getCurrentRecord();
		return record==null ? 0 : record.getPayloadLength();
	}
	
	@Override
	public int getCurrentRecordTotalLength() {
		RichTextRecord<?> record = getCurrentRecord();
		return record==null ? 0 : record.getCDRecordLength();
	}
	

	@Override
	public RichtextPosition getCurrentPosition() {
		checkDisposed();
		
		return new CDFileRichTextPosition(this, m_position);
	}

	@Override
	public void restorePosition(RichtextPosition pos) {
		if (!(pos instanceof CDFileRichTextPosition)) {
			throw new IllegalArgumentException("Invalid position, not generated by this navigator");
		}
		
		CDFileRichTextPosition posImpl = (CDFileRichTextPosition) pos;
		if (posImpl.m_parentNav!=this) {
			throw new IllegalArgumentException("Invalid position, not generated by this navigator");
		}

		if (!gotoFirst()) {
			throw new IllegalStateException(MessageFormat.format("File does not have any content: {0}", getAllocations().getFilePath()));
		}
		long targetFilePos = posImpl.m_filePosition;
		Integer indexAtFilePos = m_cdRecordIndexAtFilePos.get(targetFilePos);
		if (indexAtFilePos==null) {
			throw new IllegalArgumentException("Unknown position");
		}
		m_position = posImpl.m_filePosition;
		m_currentCDRecordIndex = indexAtFilePos;
		m_currentCDRecord = readCurrentCDRecordUnchecked();
	}
	
	@Override
	public void copyCurrentRecordTo(RichTextWriter ct) {
		checkDisposed();
		
		RichTextRecord<?> record = getCurrentRecord();
		if (record==null) {
			throw new IllegalStateException("Current record is null");
		}
		
		ct.addRichTextRecord(record);
	}

	@Override
	protected String toStringLocal() {
		return MessageFormat.format(
			"JNACDFileRichTextNavigator [filepath={0}, filesize={1}]", //$NON-NLS-1$
			getAllocations().getFilePath(), getAllocations().getFileSize()
		);
	}
	
	private static class CDFileRichTextPosition implements RichtextPosition {
		private RichtextNavigator m_parentNav;
		private long m_filePosition;
		
		public CDFileRichTextPosition(RichtextNavigator parentNav, long filePosition) {
			m_parentNav = parentNav;
			m_filePosition = filePosition;
		}
		
		@Override
		public int hashCode() {
			RichtextNavigator nav = m_parentNav;
			
			final int prime = 31;
			int result = 1;
			result = prime * result + (nav == null ? 0 : nav.hashCode());
			result = prime * result + (int) (m_filePosition ^ (m_filePosition >>> 32));
			result = prime * result + (nav == null ? 0 : nav.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			RichtextNavigator nav = m_parentNav;
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CDFileRichTextPosition other = (CDFileRichTextPosition) obj;
			if(nav == null && other.m_parentNav != null) {
				return false;
			}
			if (nav != null && !nav.equals(other.m_parentNav)) {
				return false;
			}
			if (m_filePosition != other.m_filePosition) {
				return false;
			}
			return true;
		}

	}
}