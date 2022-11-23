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
package com.hcl.domino.jna.internal.adminp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hcl.domino.DominoException;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Item;
import com.hcl.domino.data.Item.ItemFlag;
import com.hcl.domino.data.ItemDataType;
import com.hcl.domino.jna.data.JNADatabase;
import com.hcl.domino.jna.data.JNADocument;
import com.hcl.domino.jna.data.JNAItem;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.LMBCSStringList;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.hcl.domino.jna.internal.structs.NotesAdminpRequestProfileStruct;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;

/**
 * Helper class for AdministrationProcess request creation
 * 
 * @author Karsten Lehmann
 */
public class AdminpRequestProfileEntries {
	private Database m_dbProxy;
	private JNADocument m_adminReqProfileDoc;
	private List<String> m_itemNames;
	private List<Memory> m_lmbcsStrings;
	private List<Memory> m_itemValues;
	private List<LMBCSStringList> m_stringLists;
	
	public AdminpRequestProfileEntries(JNADatabase dbProxy) {
		m_dbProxy = dbProxy;
		m_adminReqProfileDoc = (JNADocument) m_dbProxy.createDocument();
		m_itemNames = new ArrayList<>();
		m_lmbcsStrings = new ArrayList<>();
		m_itemValues = new ArrayList<>();
		m_stringLists = new ArrayList<>();
	}
	
	public void dispose() {
		for (LMBCSStringList currStrList : m_stringLists) {
			currStrList.dispose();
		}
		m_stringLists.clear();
	}
	
	public void add(String itemName, Set<ItemFlag> flags, Object value) {
		m_adminReqProfileDoc.replaceItemValue(itemName, flags, value);
		m_itemNames.add(itemName);
	}
	
	public int size() {
		return m_itemNames.size();
	}
	
	/**
	 * Returns a block of memory that contains the concatenated data of
	 * {@link NotesAdminpRequestProfileStruct} objects
	 * 
	 * @return memory
	 */
	public DisposableMemory toStruct() {
		if (m_itemNames.isEmpty()) {
			throw new IllegalStateException("No items have been added");
		}
		
		DisposableMemory mem = new DisposableMemory(JNANotesConstants.adminpRequestProfileStructSize * m_itemNames.size());
		int offset = 0;
		
		for (int i=0; i<m_itemNames.size(); i++) {
			String currItemName = m_itemNames.get(i);
			
			Optional<Item> currItem = m_adminReqProfileDoc.getFirstItem(currItemName);
			if (!currItem.isPresent()) {
				continue;
			}
			JNAItem currJNAItem = (JNAItem) currItem.get();
			
			Memory currItemNameMem = NotesStringUtils.toLMBCS(currItemName, true);
			//store obj ref to prevent early GC
			m_lmbcsStrings.add(currItemNameMem);
			
			int type = currJNAItem.getTypeValue();
			int flagsAsInt = currJNAItem.getFlagsAsInt();

			NotesAdminpRequestProfileStruct struct = NotesAdminpRequestProfileStruct.newInstance();
			
			struct.chItemNamePtr = currItemNameMem;
			struct.wItemFlags = (short) (flagsAsInt & 0xffff);
			struct.wDataType = (short) (type & 0xffff);

			DisposableMemory itemValueRaw = currJNAItem.getValueRaw(false);
			m_itemValues.add(itemValueRaw);
			int valueLength = (int) itemValueRaw.size();
			
			if (currJNAItem.getType() == ItemDataType.TYPE_TEXT_LIST) {
				//for lists, we create our own private copy that are not disposed with the together document
				DHANDLE.ByReference rethList = DHANDLE.newInstanceByReference();
				ShortByReference retListSize = new ShortByReference();
				Memory retpList = new Memory(Native.POINTER_SIZE);
				
				short result = NotesCAPI.get().ListAllocate((short) 0, 
						(short) 0,
						1, rethList, retpList, retListSize);

				NotesErrorUtils.checkResult(result);

				List<String> strValues = m_adminReqProfileDoc.getAsList(currItemName, String.class, Collections.emptyList());
				
				LockUtil.lockHandle(rethList, (hListByVal) -> {
					Mem.OSUnlockObject(hListByVal);
					
					for (int l=0; l<strValues.size(); l++) {
						String currStr = strValues.get(l);
						Memory currStrMem = NotesStringUtils.toLMBCS(currStr, false);

		        if (currStrMem!=null && currStrMem.size() > 65535) {
		          throw new DominoException(MessageFormat.format("List item at position {0} exceeds max lengths of 65535 bytes", l));
		        }

		        char textSize = currStrMem==null ? 0 : (char) currStrMem.size();

						short localResult = NotesCAPI.get().ListAddEntry(hListByVal, 1, retListSize, (char) l, currStrMem,
						    textSize);
						NotesErrorUtils.checkResult(localResult);
					}
					
					byte[] lmbcsListHandleAsArray = rethList.getPointer().getByteArray(0, rethList.size());
					//keep a local reference to prevent GC from kicking in
					DisposableMemory lmbcsListHandlePtr = new DisposableMemory(lmbcsListHandleAsArray.length);
					m_itemValues.add(lmbcsListHandlePtr);
					lmbcsListHandlePtr.write(0, lmbcsListHandleAsArray, 0, lmbcsListHandleAsArray.length);
					struct.vDataPtr = lmbcsListHandlePtr;
					struct.wDataSize = (short) ((lmbcsListHandleAsArray.length) & 0xffff);
					return 0;
				});
			}
			else {
				struct.vDataPtr = itemValueRaw;
				struct.wDataSize = (short) ((valueLength) & 0xffff);
			}
			
			struct.write();
			byte[] structArr = struct.getPointer().getByteArray(0, JNANotesConstants.adminpRequestProfileStructSize);
			mem.write(offset, structArr, 0, structArr.length);
			
			offset += JNANotesConstants.adminpRequestProfileStructSize;
		}
		
		return mem;
	}
}
