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
package com.hcl.domino.jna.internal;

import java.lang.ref.ReferenceQueue;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.hcl.domino.commons.gc.APIObjectAllocations;
import com.hcl.domino.commons.gc.IAPIObject;
import com.hcl.domino.commons.gc.IGCDominoClient;
import com.hcl.domino.commons.util.NotesErrorUtils;
import com.hcl.domino.jna.BaseJNAAPIObject;
import com.hcl.domino.jna.JNADominoProcess;
import com.hcl.domino.jna.internal.capi.NotesCAPI;
import com.hcl.domino.jna.internal.gc.allocations.LMBCSStringListAllocations;
import com.hcl.domino.jna.internal.gc.handles.DHANDLE;
import com.hcl.domino.jna.internal.gc.handles.LockUtil;
import com.sun.jna.Memory;
import com.sun.jna.ptr.ShortByReference;

/**
 * A textlist implementation that stores the values as LMBCS encoded strings
 * 
 * @author Karsten Lehmann
 */
public class LMBCSStringList extends BaseJNAAPIObject<LMBCSStringListAllocations> implements Iterable<String> {
	private List<String> m_values;
	private boolean m_prefixDataType;
	private int m_listSizeBytes;

	@SuppressWarnings("unchecked")
	public LMBCSStringList(IAPIObject<?> parent, boolean prefixDataType) {
		this(parent, Collections.EMPTY_LIST, prefixDataType);
	}
	
	public LMBCSStringList(IAPIObject<?> parent, List<String> values, boolean prefixDataType) {
		super(parent);
		
		if (values==null) {
			values = Collections.emptyList();
		}
		
		m_values = new ArrayList<>();
		m_prefixDataType = prefixDataType;

		allocate();

		addAll(values);
	}

	@Override
	protected void checkDisposedLocal() {
		getAllocations().checkDisposed();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected LMBCSStringListAllocations createAllocations(IGCDominoClient<?> parentDominoClient,
			APIObjectAllocations parentAllocations, ReferenceQueue<? super IAPIObject> queue) {

		return new LMBCSStringListAllocations(parentDominoClient, parentAllocations, this, queue);
	}

	public boolean isPrefixDataType() {
		return m_prefixDataType;
	}
	
	public int getListSizeInBytes() {
		return m_listSizeBytes;
	}
	
	private void allocate() {
		JNADominoProcess.checkThreadEnabledForDomino();
		
		short result;
		
		DHANDLE.ByReference rethList = DHANDLE.newInstanceByReference();
		ShortByReference retListSize = new ShortByReference();

		result = NotesCAPI.get().ListAllocate((short) 0, 
				(short) 0,
				m_prefixDataType ? 1 : 0, rethList, null, retListSize);
		
		NotesErrorUtils.checkResult(result);

		LMBCSStringListAllocations allocations = getAllocations();
		allocations.setListHandle(rethList);
		LockUtil.lockHandle(rethList, (handleByVal) -> {
			Mem.OSUnlockObject(handleByVal);
			return 0;
		});
		
		m_listSizeBytes = retListSize.getValue() & 0xffff;
	}

	/**
	 * Removes all entries from the list
	 */
	public void clear() {
		if (m_values.isEmpty()) {
			return;
		}
		
		LMBCSStringListAllocations allocations = getAllocations();
		allocations.checkDisposed();
		
		ShortByReference retListSize = new ShortByReference();
		
		short result = LockUtil.lockHandle(allocations.getListHandle(), (handleByVal) -> {
			return NotesCAPI.get().ListRemoveAllEntries(handleByVal, m_prefixDataType ? 1 : 0, retListSize);
		});
		NotesErrorUtils.checkResult(result);
		
		m_listSizeBytes = retListSize.getValue() & 0xffff;
		m_values.clear();
	}
	
	/**
	 * Adds a value to the list
	 * 
	 * @param value value to add
	 */
	public void add(String value) {
		addAll(Arrays.asList(value));
	}
	
	/**
	 * Adds values to the list
	 * 
	 * @param newValues values to add
	 */
	public void addAll(List<String> newValues) {
		if (newValues.isEmpty()) {
			return;
		}
		
		checkDisposed();
		LMBCSStringListAllocations allocations = getAllocations();
		
		if ((m_values.size() + newValues.size())> 65535) {
			throw new IllegalArgumentException(MessageFormat.format("String list size must fit in a WORD ({0}>65535)", m_values.size()));
		}
		
		ShortByReference retListSize = new ShortByReference();
		retListSize.setValue((short) (m_listSizeBytes & 0xffff));

		LockUtil.lockHandle(allocations.getListHandle(), (handleByVal) -> {
			for (int i=0; i<newValues.size(); i++) {
				String currStr = newValues.get(i);
				Memory currStrMem = NotesStringUtils.toLMBCS(currStr, false);

				short entryNo = (short) (m_values.size() & 0xffff);
				
				short result = NotesCAPI.get().ListAddEntry(handleByVal, m_prefixDataType ? 1 : 0, retListSize, entryNo, currStrMem,
						(short) (currStrMem==null ? 0 : (currStrMem.size() & 0xffff)));
				NotesErrorUtils.checkResult(result);
			}
			return 0;
		});
		
		m_listSizeBytes = retListSize.getValue() & 0xffff;
		
		m_values.addAll(newValues);
	}

	@Override
	public Iterator<String> iterator() {
		return m_values.iterator();
	}
	
	public int getSize() {
		return m_values.size();
	}

}
