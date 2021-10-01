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

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.commons.NotYetImplementedException;
import com.hcl.domino.commons.data.AbstractTypedAccess;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.Database.Action;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DocumentProperties;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.JNADocumentAllocations;
import com.hcl.domino.jna.internal.gc.allocations.JNAItemAllocations;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesBlockIdStruct;
import com.hcl.domino.mime.MimeEntity;
import com.hcl.domino.misc.DominoEnumUtil;
import com.hcl.domino.misc.NotesConstants;
import com.hcl.domino.richtext.RichTextConstants;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;

public class JNAItem extends BaseJNAAPIObject<JNAItemAllocations> implements Item {
	private JNADocument m_parentDoc;
	private boolean m_itemFlagsLoaded;
	private int m_itemFlags;
	private byte m_seq;
	private byte m_dupItemId;
	private int m_dataType;
	private int m_valueLength;
	private String m_itemName;

	private NotesBlockIdStruct m_itemBlockId;
	private NotesBlockIdStruct m_valueBlockId;
	/**
	 * Collection of item types that are assumed to contain composite-data records
	 */
	public static final Set<ItemDataType> CD_TYPES = EnumSet.of(
	  ItemDataType.TYPE_COMPOSITE, ItemDataType.TYPE_ACTION,
	  ItemDataType.TYPE_VIEWMAP_DATASET, ItemDataType.TYPE_VIEWMAP_LAYOUT,
	  ItemDataType.TYPE_QUERY
	);
	
	JNAItem(JNADocument parentDoc, NotesBlockIdStruct itemBlockId, int dataType,
			NotesBlockIdStruct valueBlockId) {
		super(parentDoc);
		
		m_parentDoc = parentDoc;
		m_itemBlockId = itemBlockId;
		m_dataType = dataType;
		m_valueBlockId = valueBlockId;
	}
	
	/**
	 * Returns the item block id to read item meta data
	 * 
	 * @return item block id
	 */
	NotesBlockIdStruct getItemBlockId() {
		return m_itemBlockId;
	}

	/**
	 * Returns the value block id to lock the value in memory and decode it
	 * 
	 * @return value block id
	 */
	NotesBlockIdStruct getValueBlockId() {
		return m_valueBlockId;
	}

	/**
	 * Returns a copy of the item raw value
	 * 
	 * @param prefixDataType true to keep the datatype WORD as prefix
	 * @return item value
	 */
	public DisposableMemory getValueRaw(boolean prefixDataType) {
		loadItemNameAndFlags();
		NotesBlockIdStruct valueBlockId = getValueBlockId();
		
		DisposableMemory mem = new DisposableMemory(prefixDataType ? m_valueLength : m_valueLength-2);
		Pointer valuePtr = Mem.OSLockObject(valueBlockId);
		try {
			byte[] valueArr = prefixDataType ? valuePtr.getByteArray(0, m_valueLength) : valuePtr.getByteArray(2, m_valueLength-2);
			mem.write(0, valueArr, 0, valueArr.length);
			return mem;
		}
		finally {
			Mem.OSUnlockObject(valueBlockId);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected JNAItemAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAItemAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	/**
	 * Calls NSFItemQueryEx to read the items name and data
	 */
	private void loadItemNameAndFlags() {
		if (m_itemFlagsLoaded) {
			return;
		}

		checkDisposed();
		
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();
		
		ByteByReference retSeqByte = new ByteByReference();
		ByteByReference retDupItemID = new ByteByReference();

		Memory item_name = new Memory(NotesConstants.MAXUSERNAME);
		ShortByReference retName_len = new ShortByReference();
		ShortByReference retItem_flags = new ShortByReference();
		ShortByReference retDataType = new ShortByReference();
		IntByReference retValueLen = new IntByReference();

		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		NotesBlockIdStruct retValueBid = NotesBlockIdStruct.newInstance();
		
		LockUtil.lockHandle(docAllocations.getNoteHandle(), (noteHandleByVal) -> {
			NotesCAPI.get().NSFItemQueryEx(noteHandleByVal, itemBlockIdByVal, item_name, (short) (item_name.size() & 0xffff),
					retName_len, retItem_flags, retDataType, retValueBid, retValueLen, retSeqByte, retDupItemID);
			
			return 0;
		});
		
		m_dataType = retDataType.getValue();
		m_seq = retSeqByte.getValue();
		m_dupItemId = retDupItemID.getValue();
		m_itemFlags = retItem_flags.getValue() & 0xffff;
		m_itemName = NotesStringUtils.fromLMBCS(item_name, retName_len.getValue() & 0xffff);
		m_valueLength = retValueLen.getValue();
		m_itemFlagsLoaded = true;
	}

	@Override
	public String getName() {
		loadItemNameAndFlags();

		return m_itemName;
	}

	@Override
	public int getTypeValue() {
		return m_dataType;
	}

	@Override
	public ItemDataType getType() {
		return DominoEnumUtil.valueOf(ItemDataType.class, m_dataType)
			.orElse(ItemDataType.TYPE_INVALID_OR_UNKNOWN);
	}

	/**
	 * Returns the value length in bytes
	 * 
	 * @return length
	 */
	@Override
  public int getValueLength() {
		loadItemNameAndFlags();

		return m_valueLength;
	}

	@Override
	public List<Object> getValue() {
		loadItemNameAndFlags();

		int valueLength = getValueLength();
		List<Object> values = m_parentDoc.getItemValue(m_itemName, m_itemBlockId, m_valueBlockId,
				valueLength);
		return values;
	}

	@Override
	public <T> T get(Class<T> valueType, T defaultValue) {
		AbstractTypedAccess typedAccess = new AbstractTypedAccess() {
			
			@Override
			public boolean hasItem(String itemName) {
				return getName().equalsIgnoreCase(itemName);
			}

			@Override
			public List<String> getItemNames() {
				return Arrays.asList(getName());
			}
			
			@Override
			protected List<?> getItemValue(String itemName) {
				return getValue();
			}
			
			@SuppressWarnings("unchecked")
      @Override
			public <U> U get(String itemName, Class<U> valueType, U defaultValue) {
			  // Specialized support for byte[] for the raw data
			  if(byte[].class.equals(valueType)) {
  			  Pointer valuePtr = Mem.OSLockObject(m_valueBlockId);
  		    try {
            return (U)valuePtr.getByteArray(0, m_valueLength);
  		    }
  		    finally {
  		      Mem.OSUnlockObject(m_valueBlockId);
  		    }
			  }
			  
			  return super.get(itemName, valueType, defaultValue);
			};
		};
		
		return typedAccess.get(getName(), valueType, defaultValue);
	}
	
	@Override
	public <T> List<T> getAsList(Class<T> valueType, List<T> defaultValue) {
		AbstractTypedAccess typedAccess = new AbstractTypedAccess() {
			
			@Override
			public boolean hasItem(String itemName) {
				return getName().equalsIgnoreCase(itemName);
			}

			@Override
			public List<String> getItemNames() {
				return Arrays.asList(getName());
			}
			
			@Override
			protected List<?> getItemValue(String itemName) {
				return getValue();
			}
		};
		
		return typedAccess.getAsList(getName(), valueType, defaultValue);
	}
	
	@Override
	public void copyToDocument(Document doc, boolean overwrite) {
		JNADocumentAllocations parentDocAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		JNADocumentAllocations targetDocAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);
		
		parentDocAllocations.checkDisposed();
		targetDocAllocations.checkDisposed();

		if (overwrite) {
			String itemName = getName();
			if (doc.hasItem(itemName)) {
				doc.removeItem(itemName);
			}
		}
		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		short result = LockUtil.lockHandle(targetDocAllocations.getNoteHandle(), (targetDocHandleByVal) -> {
			return NotesCAPI.get().NSFItemCopy(targetDocHandleByVal, itemBlockIdByVal);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public void copyToDocument(Document doc, String newItemName, boolean overwrite) {
		JNADocumentAllocations parentDocAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		JNADocumentAllocations targetDocAllocations = (JNADocumentAllocations) doc.getAdapter(APIObjectAllocations.class);
		
		parentDocAllocations.checkDisposed();
		targetDocAllocations.checkDisposed();

		if (overwrite) {
			String itemName = getName();
			if (doc.hasItem(itemName)) {
				doc.removeItem(itemName);
			}
		}
		
		Memory newItemNameMem = NotesStringUtils.toLMBCS(newItemName, true);

		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		short result = LockUtil.lockHandle(targetDocAllocations.getNoteHandle(), (targetDocHandleByVal) -> {
			return NotesCAPI.get().NSFItemCopyAndRename(targetDocHandleByVal, itemBlockIdByVal, newItemNameMem);
		});
		NotesErrorUtils.checkResult(result);
	}

	@Override
	public int getSequenceNumber() {
		loadItemNameAndFlags();

		return m_seq & 0xff;
	}

	public int getDupItemId() {
		loadItemNameAndFlags();
		
		return m_dupItemId & 0xff;
	}
	
	@Override
	public Set<ItemFlag> getFlags() {
		loadItemNameAndFlags();
		
		Set<ItemFlag> flags = new HashSet<>();
		
		for (ItemFlag currFlag : ItemFlag.values()) {
			if ((m_itemFlags & currFlag.getValue()) == currFlag.getValue()) {
				flags.add(currFlag);
			}
		}
		return flags;
	}

	public int getFlagsAsInt() {
		loadItemNameAndFlags();
		return m_itemFlags;
	}
	
	@Override
	public boolean isSummary() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_SUMMARY) == NotesConstants.ITEM_SUMMARY;
	}

	@Override
	public boolean isNames() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_NAMES) == NotesConstants.ITEM_NAMES;
	}

	@Override
	public boolean isReaders() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_READERS) == NotesConstants.ITEM_READERS;
	}

	@Override
	public boolean isReadWriters() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_READWRITERS) == NotesConstants.ITEM_READWRITERS;
	}

	@Override
	public boolean isProtected() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_PROTECTED) == NotesConstants.ITEM_PROTECTED;
	}

	@Override
	public boolean isEncrypted() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_SEAL) == NotesConstants.ITEM_SEAL;
	}

	@Override
	public boolean isSigned() {
		loadItemNameAndFlags();

		return (m_itemFlags & NotesConstants.ITEM_SIGN) == NotesConstants.ITEM_SIGN;
	}

	public void setItemType(ItemDataType newType) {
	  JNADocumentAllocations docAllocations = (JNADocumentAllocations) getParent().getAdapter(APIObjectAllocations.class);
	  docAllocations.checkDisposed();

	  loadItemNameAndFlags();

	  NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
	  itemBlockIdByVal.pool = m_itemBlockId.pool;
	  itemBlockIdByVal.block = m_itemBlockId.block;

	  int itemFlags = getFlagsAsInt();
	  DisposableMemory itemValue = getValueRaw(false);
	  try {
	    short result = LockUtil.lockHandle(docAllocations.getNoteHandle(), (docHandleByVal) -> {
	      return NotesCAPI.get().NSFItemModifyValue(docHandleByVal, itemBlockIdByVal, 
	          (short) (itemFlags & 0xffff), newType.getValue(), 
	          itemValue, (int) itemValue.size());
	    });
	    NotesErrorUtils.checkResult(result);
	  }
	  finally {
	    itemValue.dispose();
	  }
	}
	
	private void setItemFlags(short newFlags) {
		JNADocumentAllocations docAllocations = (JNADocumentAllocations) getParent().getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();

		loadItemNameAndFlags();

		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		Pointer poolPtr = Mem.OSLockObject(m_itemBlockId);
		try {
			Pointer itemFlagsPtr = poolPtr.share(16);

			short oldFlags = itemFlagsPtr.getShort(0);
			if (oldFlags==m_itemFlags) {
				itemFlagsPtr.setShort(0, newFlags);
				m_itemFlagsLoaded = false;
			}
		}
		finally {
			Mem.OSUnlockObject(m_itemBlockId);
		}
	}

	@Override
	public void setNames(boolean isNames) {
		loadItemNameAndFlags();
		if (isNames) {
			if (!isNames()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_NAMES;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isNames()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_NAMES;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}

	@Override
	public void setReaders(boolean isReaders) {
		loadItemNameAndFlags();
		if (isReaders) {
			if (!isReaders()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_READERS;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isReaders()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_READERS;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}

	@Override
	public void setReadWriters(boolean isReadWriters) {
		loadItemNameAndFlags();
		if (isReadWriters) {
			if (!isReadWriters()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_READWRITERS;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isReadWriters()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_READWRITERS;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}

	@Override
	public void setProtected(boolean isProtected) {
		loadItemNameAndFlags();
		if (isProtected) {
			if (!isProtected()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_PROTECTED;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isProtected()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_PROTECTED;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}

	@Override
	public void setEncrypted(boolean isEncrypted) {
		loadItemNameAndFlags();
		if (isEncrypted) {
			if (!isEncrypted()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_SEAL;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isEncrypted()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_SEAL;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}

	@Override
	public void setSigned(boolean isSigned) {
		loadItemNameAndFlags();
		if (isSigned) {
			if (!isSigned()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_SIGN;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isSigned()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_SIGN;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}

	@Override
	public void setSummary(boolean isSummary) {
		loadItemNameAndFlags();
		if (isSummary) {
			if (!isSummary()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt | NotesConstants.ITEM_SUMMARY;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
		else {
			if (isSummary()) {
				int flagsAsInt = m_itemFlags & 0xffff;
				int newFlagsAsInt = flagsAsInt & ~NotesConstants.ITEM_SUMMARY;
				setItemFlags((short) (newFlagsAsInt & 0xffff));
			}
		}
	}
	
	@Override
	public DocumentProperties getProperties() {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public Optional<MimeEntity> getMimeEntity() {
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	@Override
	public Item convertRFC822TextItem() {
		checkDisposed();

		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		NotesBlockIdStruct.ByValue valueBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		valueBlockIdByVal.pool = m_valueBlockId.pool;
		valueBlockIdByVal.block = m_valueBlockId.block;

		JNADocumentAllocations docAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();

		LockUtil.lockHandle(docAllocations.getNoteHandle(), (noteHdlByVal) -> {
			short result = NotesCAPI.get().MIMEConvertRFC822TextItemByBLOCKID(noteHdlByVal, itemBlockIdByVal,
					valueBlockIdByVal);
			NotesErrorUtils.checkResult(result);

			//force datatype and seq number reload
			m_itemFlagsLoaded = false;
			loadItemNameAndFlags();

			return null;
		});
		
		return this;
	}
	
	@Override
	public void remove() {
		checkDisposed();

		NotesBlockIdStruct.ByValue itemBlockIdByVal = NotesBlockIdStruct.ByValue.newInstance();
		itemBlockIdByVal.pool = m_itemBlockId.pool;
		itemBlockIdByVal.block = m_itemBlockId.block;

		JNADocumentAllocations docAllocations = (JNADocumentAllocations) m_parentDoc.getAdapter(APIObjectAllocations.class);
		docAllocations.checkDisposed();

		short result = LockUtil.lockHandle(docAllocations.getNoteHandle(), (hNoteByVal) -> {
			return NotesCAPI.get().NSFItemDeleteByBLOCKID(hNoteByVal, itemBlockIdByVal);
		});
		NotesErrorUtils.checkResult(result);
		
		dispose();
	}

	/**
	 * Enumerates all CD records if a richtext item (TYPE_COMPOSITE).
	 * 
	 * @param callback callback with direct memory access via pointer
	 * @throws UnsupportedOperationException if item has the wrong type
	 */
	public void enumerateCDRecords(final ICompositeCallbackDirect callback) {
		if (!CD_TYPES.contains(getType())) {
			throw new UnsupportedOperationException(MessageFormat.format("Item is not of type TYPE_COMPOSITE (type found: {0})", getType()));
		}

		Pointer valuePtr = Mem.OSLockObject(m_valueBlockId);

		try {
			int fixedSize;

			int dwFileSize = getValueLength() - 2; //2 -> subtract data type WORD
			int dwFileOffset = 0;

			boolean aborted = false;

			while (dwFileSize>0) {
				Pointer cdRecordPtr = valuePtr.share(2 + dwFileOffset); //2 -> skip data type WORD

				//read signature WORD
				short recordType = cdRecordPtr.getShort(0);

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

				short highOrderByte = (short) (recordType & 0xFF00);

				switch (highOrderByte) {
				case RichTextConstants.LONGRECORDLENGTH:      /* LSIG */
					dwLength = cdRecordPtr.share(2).getInt(0);

					fixedSize = 6; //sizeof(LSIG);

					break;

				case RichTextConstants.WORDRECORDLENGTH:      /* WSIG */
					dwLength = cdRecordPtr.share(2).getShort(0) & 0xffff;

					fixedSize = 4; //sizeof(WSIG);

					break;

				default:                    /* BSIG */
					dwLength = (recordType >> 8) & 0x00ff;
					recordType &= 0x00FF; /* Length not part of signature */
					fixedSize = 2; //sizeof(BSIG);
				}

				//give direct pointer access (internal only)
				if (callback!=null) {
					if (callback.recordVisited(recordType, cdRecordPtr, dwLength) == Action.Stop) {
						aborted=true;
						break;
					}
				}

				if (dwLength>0) {
					dwFileSize -= dwLength;
					dwFileOffset += dwLength;
				}
				else {
					dwFileSize -= fixedSize;
					dwFileOffset += fixedSize;
				}

				/* If we are at an odd file offset, ignore the filler byte */

				if ((dwFileOffset & 1L)==1 && (dwFileSize > 0) ) {
					dwFileSize -= 1;            
					dwFileOffset += 1;
				}
			}

			if (!aborted && dwFileSize>0) {
				//should not happen :-)
				System.out.println(MessageFormat.format(
					"WARNING: Remaining {0} bytes found at the end of the CD record item {1} of document with UNID {2}",
					dwFileSize, getName(), m_parentDoc.getUNID()
				));
			}
		}
		finally {
			Mem.OSUnlockObject(m_valueBlockId);
		}
	}

	/**
	 * Internal interface for direct access to memory structures
	 * 
	 * @author Karsten Lehmann
	 */
	@FunctionalInterface
	public interface ICompositeCallbackDirect {

		/**
		 * Method is called for all CD records in this item
		 * @param signature signature WORD for the record type
		 * @param cdRecordPtr pointer to CD record (header + data)
		 * @param cdRecordLength total length of CD record (BSIG/WSIG/LSIG header plus <code>dataLength</code>
		 * 
		 * @return action value to continue or stop
		 */
		Action recordVisited(short signature, Pointer cdRecordPtr, int cdRecordLength);

	}
}
