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
import com.hcl.domino.jna.internal.JNANotesConstants;
import com.hcl.domino.jna.internal.Mem;
import com.hcl.domino.jna.internal.NotesNamingUtils;
import com.hcl.domino.jna.internal.NotesNamingUtils.Privileges;
import com.hcl.domino.jna.internal.NotesStringUtils;
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
		  m_names = LockUtil.lockHandle(allocations.getHandle(), (handleByVal) -> {
				return Mem.OSLockObject(handleByVal, JNAUserNamesList::readNamesList);
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
      ByteArrayOutputStream bOut = new ByteArrayOutputStream();

      int numNames = Short.toUnsignedInt(namesListBufferPtr.getShort(0));
      long offset;
      if (PlatformUtils.isWindows()) {
        offset = JNANotesConstants.winNamesListHeaderSize64;
      } else if (PlatformUtils.isMac()) {
        offset = JNANotesConstants.macNamesListHeaderSize64;
      } else {
        offset = JNANotesConstants.linuxNamesListHeaderSize64;
      }

      if (numNames == 0) {
        return Collections.emptyList();
      }
      return NotesStringUtils.fromLMBCSStringList(namesListBufferPtr.share(offset), numNames);
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
