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
package com.hcl.domino.jna.data;

import java.io.ByteArrayOutputStream;
import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import com.hcl.domino.DominoException;
import com.hcl.domino.UserNamesList;
import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.PlatformUtils;
import com.hcl.domino.data.IAdaptable;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.internal.DisposableMemory;
import com.hcl.domino.jna.internal.LinuxNotesNamesListHeader64Struct;
import com.hcl.domino.jna.internal.MacNotesNamesListHeader64Struct;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamesListHeader32Struct;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesNamingUtils.Privileges;
import com.hcl.domino.jna.internal.NotesStringUtils;
import com.hcl.domino.jna.internal.WinNotesNamesListHeader32Struct;
import com.hcl.domino.jna.internal.WinNotesNamesListHeader64Struct;
import com.hcl.domino.jna.internal.gc.allocations.JNAUserNamesListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Pointer;

/**
 * NAMES_LIST structure wrapper that wraps a user names list stored in memory
 * 
 * @author Karsten Lehmann
 */
public class JNAUserNamesList extends BaseJNAAPIObject<JNAUserNamesListAllocations> implements UserNamesList {
	private List<String> m_names;

	public JNAUserNamesList(IAPIObject<?> parent, IAdaptable adaptable) {
		super(parent);

		DHANDLE handle = adaptable.getAdapter(DHANDLE.class);
		if (handle==null) {
			throw new DominoException(0, "Missing DHANDLE which is required for memory management");
		}
		getAllocations().setHandle(handle);
		
		setInitialized();
	}
	
	@Override
	protected void checkDisposedLocal() {
		getAllocations().checkDisposed();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected JNAUserNamesListAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {
		
		return new JNAUserNamesListAllocations(parentDominoClient, parentAllocations, this, queue);
	}
	
	@Override
	public String getPrimaryName() {
		List<String> names = toList();
		return names.isEmpty() ? "" : names.get(0); //$NON-NLS-1$
	}
	
	@Override
	public String toStringLocal() {
		if (isDisposed()) {
			return "JNAUserNamesList [freed]"; //$NON-NLS-1$
		}
		else {
			EnumSet<Privileges> privileges = NotesNamingUtils.getPrivileges(this);
			return MessageFormat.format(
				"JNAUserNamesList [handle={0}, values={1}, privileges={2}]", //$NON-NLS-1$
				getAllocations().getHandle(), toList(), privileges
			);
		}
	}
	
	@Override
	public List<String> toList() {
		checkDisposed();
		JNAUserNamesListAllocations allocations = getAllocations();
		
		if (m_names==null) {
			LockUtil.lockHandle(allocations.getHandle(), (handleByVal) -> {
				Pointer ptr = Mem.OSLockObject(handleByVal);
				try {
					m_names = readNamesList(ptr);
				}
				finally {
					Mem.OSUnlockObject(handleByVal);
				}
				return 0;
			});
		}
		return new ArrayList<>(m_names);
	}
	
	/**
	 * Decodes a usernames list stored in memory
	 * 
	 * @param namesListBufferPtr Pointer to user names list
	 * @return usernames list
	 */
	private static List<String> readNamesList(Pointer namesListBufferPtr) {
		long offset;
		int numNames;
		List<String> names;

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		if (PlatformUtils.is64Bit()) {
			if (PlatformUtils.isWindows()) {
				WinNotesNamesListHeader64Struct namesList = WinNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
				namesList.read();
				
				names = new ArrayList<>(namesList.NumNames);

				offset = namesList.size();
				numNames = namesList.NumNames & 0xffff;
			}
			else if (PlatformUtils.isMac()) {
				MacNotesNamesListHeader64Struct namesList = MacNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
				namesList.read();

				names = new ArrayList<>(namesList.NumNames);

				offset = namesList.size();
				numNames = namesList.NumNames & 0xffff;
			}
			else {
				LinuxNotesNamesListHeader64Struct namesList = LinuxNotesNamesListHeader64Struct.newInstance(namesListBufferPtr);
				namesList.read();

				names = new ArrayList<>(namesList.NumNames);

				offset = namesList.size();
				numNames = namesList.NumNames & 0xffff;
			}
		}
		else {
			if (PlatformUtils.isWindows()) {
				WinNotesNamesListHeader32Struct namesList = WinNotesNamesListHeader32Struct.newInstance(namesListBufferPtr);
				namesList.read();

				names = new ArrayList<>(namesList.NumNames);

				offset = namesList.size();
				numNames = namesList.NumNames & 0xffff;
			}
			else {
				NotesNamesListHeader32Struct namesList = NotesNamesListHeader32Struct.newInstance(namesListBufferPtr);
				namesList.read();

				names = new ArrayList<>(namesList.NumNames);

				offset = namesList.size();
				numNames = namesList.NumNames & 0xffff;

			}
		}
		
		if (numNames==0) {
			return Collections.emptyList();
		}
		
		while (names.size() < numNames) {
			byte b = namesListBufferPtr.getByte(offset);

			if (b == 0) {
			    try(DisposableMemory mem = new DisposableMemory(bOut.size())) {
    				mem.write(0, bOut.toByteArray(), 0, bOut.size());
    				String currUserName = NotesStringUtils.fromLMBCS(mem, bOut.size());
    				names.add(currUserName);
			    }
				bOut.reset();
			}
			else {
				bOut.write(b);
			}
			offset++;
		}

		return names;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> T getAdapterLocal(Class<T> clazz) {
		if (clazz == DHANDLE.class) {
			return (T) getAllocations().getHandle();
		}
		return null;
	}

	@Override
	public Iterator<String> iterator() {
		return toList().iterator();
	}
}
